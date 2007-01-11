
/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Xiao-Feng Li, 2006/10/05
 */


#include "fspace.h"
#include "../thread/collector.h"
#include "../common/gc_metadata.h"
#include "../finalizer_weakref/finalizer_weakref.h"

#ifdef MARK_BIT_FLIPPING

static void scan_slot(Collector* collector, Partial_Reveal_Object **p_ref) 
{
  Partial_Reveal_Object *p_obj = *p_ref;
  if(p_obj == NULL) return;  
    
  collector_tracestack_push(collector, p_ref); 
  return;
}

static void scan_object(Collector* collector, Partial_Reveal_Object *p_obj) 
{
  if (!object_has_ref_field_before_scan(p_obj)) return;
    
  Partial_Reveal_Object **p_ref;

  if (object_is_array(p_obj)) {   /* scan array object */
  
    Partial_Reveal_Array* array = (Partial_Reveal_Array*)p_obj;
    unsigned int array_length = array->array_len; 
    p_ref = (Partial_Reveal_Object**)((int)array + (int)array_first_element_offset(array));

    for (unsigned int i = 0; i < array_length; i++) {
      scan_slot(collector, p_ref+i);
    }   

  }else{ /* scan non-array object */
    
    unsigned int num_refs = object_ref_field_num(p_obj);
    int* ref_iterator = object_ref_iterator_init(p_obj);
 
    for(unsigned int i=0; i<num_refs; i++){  
      p_ref = object_ref_iterator_get(ref_iterator+i, p_obj);  
      scan_slot(collector, p_ref);
    }    

#ifndef BUILD_IN_REFERENT
    scan_weak_reference(collector, p_obj, scan_slot);
#endif
  
  }

  return;
}

/* NOTE:: At this point, p_ref can be in anywhere like root, and other spaces, but *p_ref must be in fspace, 
   since only slot which points to object in fspace could be added into TraceStack.
   The problem is the *p_ref may be forwarded already so that, when we come here we find it's pointing to tospace.
   We will simply return for that case. It might be forwarded due to:
    1. two difference slots containing same reference; 
    2. duplicate slots in remset ( we use SSB for remset, no duplication filtering.)
   The same object can be traced by the thread itself, or by other thread.
*/

#include "../verify/verify_live_heap.h"
static void forward_object(Collector* collector, Partial_Reveal_Object **p_ref) 
{
  GC* gc = collector->gc;
  Partial_Reveal_Object *p_obj = *p_ref;

  if(!obj_belongs_to_nos(p_obj)){
    if(obj_mark_in_oi(p_obj))
      scan_object(collector, p_obj);
    return;
  }

  /* following is the logic for forwarding */  
  Partial_Reveal_Object* p_target_obj = collector_forward_object(collector, p_obj);
  
  /* if p_target_obj is NULL, it is forwarded by other thread. 
      Note: a race condition here, it might be forwarded by other, but not set the 
      forwarding pointer yet. We need spin here to get the forwarding pointer. 
      We can implement the collector_forward_object() so that the forwarding pointer 
      is set in the atomic instruction, which requires to roll back the mos_alloced
      space. That is easy for thread local block allocation cancellation. */
  if( p_target_obj == NULL ){
    if(collector->result == FALSE ){
      /* failed to forward, let's get back to controller. */
      vector_stack_clear(collector->trace_stack);
      return;
    }

    Partial_Reveal_Object *p_new_obj = obj_get_fw_in_oi(p_obj);
    assert(p_new_obj);
    *p_ref = p_new_obj;
    return;
  }
  /* otherwise, we successfully forwarded */
  *p_ref = p_target_obj;

  /* we forwarded it, we need remember it for verification. */
  if(verify_live_heap) {
    event_collector_move_obj(p_obj, p_target_obj, collector);
  }
  
  scan_object(collector, p_target_obj); 
  return;
}

static void trace_object(Collector* collector, Partial_Reveal_Object **p_ref)
{ 
  forward_object(collector, p_ref);

  Vector_Block* trace_stack = (Vector_Block*)collector->trace_stack;
  while( !vector_stack_is_empty(trace_stack)){
    p_ref = (Partial_Reveal_Object **)vector_stack_pop(trace_stack); 
    forward_object(collector, p_ref);
    trace_stack = (Vector_Block*)collector->trace_stack;
  }
  return; 
}
 
/* for tracing phase termination detection */
static volatile unsigned int num_finished_collectors = 0;

static void collector_trace_rootsets(Collector* collector)
{
  GC* gc = collector->gc;
  GC_Metadata* metadata = gc->metadata;
  
  unsigned int num_active_collectors = gc->num_active_collectors;
  atomic_cas32( &num_finished_collectors, 0, num_active_collectors);

  Space* space = collector->collect_space;
  collector->trace_stack = free_task_pool_get_entry(metadata);

  /* find root slots saved by 1. active mutators, 2. exited mutators, 3. last cycle collectors */  
  Vector_Block* root_set = pool_iterator_next(metadata->gc_rootset_pool);

  /* first step: copy all root objects to trace tasks. */ 
  while(root_set){
    unsigned int* iter = vector_block_iterator_init(root_set);
    while(!vector_block_iterator_end(root_set,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(root_set,iter);

      Partial_Reveal_Object* p_obj = *p_ref;
      assert(p_obj != NULL);  /* root ref cann't be NULL, but remset can be */

      collector_tracestack_push(collector, p_ref);
    } 
    root_set = pool_iterator_next(metadata->gc_rootset_pool);
  }
  /* put back the last trace_stack task */    
  pool_put_entry(metadata->mark_task_pool, collector->trace_stack);
  
  /* second step: iterate over the trace tasks and forward objects */
  collector->trace_stack = free_task_pool_get_entry(metadata);

retry:
  Vector_Block* trace_task = pool_get_entry(metadata->mark_task_pool);

  while(trace_task){    
    unsigned int* iter = vector_block_iterator_init(trace_task);
    while(!vector_block_iterator_end(trace_task,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(trace_task,iter);
      trace_object(collector, p_ref);
      
      if(collector->result == FALSE)  break; /* force return */
 
    }
    vector_stack_clear(trace_task);
    pool_put_entry(metadata->free_task_pool, trace_task);

    if(collector->result == FALSE){
      gc_task_pool_clear(metadata->mark_task_pool);
      break; /* force return */
    }
    
    trace_task = pool_get_entry(metadata->mark_task_pool);
  }
  
  /* A collector comes here when seeing an empty mark_task_pool. The last collector will ensure 
     all the tasks are finished.*/
     
  atomic_inc32(&num_finished_collectors);
  while(num_finished_collectors != num_active_collectors){
    if( pool_is_empty(metadata->mark_task_pool)) continue;
    /* we can't grab the task here, because of a race condition. If we grab the task, 
       and the pool is empty, other threads may fall to this barrier and then pass. */
    atomic_dec32(&num_finished_collectors);
    goto retry; 
  }

  /* now we are done, but each collector has a private stack that is empty */  
  trace_task = (Vector_Block*)collector->trace_stack;
  vector_stack_clear(trace_task);
  pool_put_entry(metadata->free_task_pool, trace_task);   
  collector->trace_stack = NULL;
  
  return;
}

void nongen_forward_pool(Collector* collector) 
{  
  GC* gc = collector->gc;
  Fspace* space = (Fspace*)collector->collect_space;
  
  collector_trace_rootsets(collector);  
  /* the rest work is not enough for parallelization, so let only one thread go */
  if( collector->thread_handle != 0 ) return;

  gc->collect_result = gc_collection_result(gc);
  if(!gc->collect_result) return;

  if(!IGNORE_FINREF )
    collector_identify_finref(collector);
#ifndef BUILD_IN_REFERENT
  else {
      gc_set_weakref_sets(gc);
      update_ref_ignore_finref(collector);
    }
#endif
  
  gc_fix_rootset(collector);
  
  if(!IGNORE_FINREF )
    gc_put_finref_to_vm(gc);
  
  fspace_reset_for_allocation(space);  

  return;
  
}

#endif /* MARK_BIT_FLIPPING */
