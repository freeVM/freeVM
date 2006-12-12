
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

static Boolean fspace_object_to_be_forwarded(Partial_Reveal_Object *p_obj, Fspace *fspace)
{
  assert(obj_belongs_to_space(p_obj, (Space*)fspace));  
  return forward_first_half? (p_obj < object_forwarding_boundary):(p_obj>=object_forwarding_boundary);
}

static void scan_slot(Collector* collector, Partial_Reveal_Object **p_ref) 
{
  Partial_Reveal_Object *p_obj = *p_ref;
  if (p_obj == NULL) return;  
    
  /* the slot can be in tspace or fspace, we don't care.
     we care only if the reference in the slot is pointing to fspace */
  if (obj_belongs_to_space(p_obj, collector->collect_space))
    collector_tracestack_push(collector, p_ref); 

  return;
}

static void scan_object(Collector* collector, Partial_Reveal_Object *p_obj) 
{
  if (!object_has_ref_field(p_obj)) return;
    
  void *slot;

  /* scan array object */
  if (object_is_array(p_obj)) {
    Partial_Reveal_Object* array = p_obj;
    assert(!obj_is_primitive_array(array));

    int32 array_length = vector_get_length((Vector_Handle) array);        
    for (int i = 0; i < array_length; i++) {
      slot = vector_get_element_address_ref((Vector_Handle) array, i);
      scan_slot(collector, (Partial_Reveal_Object **)slot);
    }   
    return;
  }

  /* scan non-array object */
  int *offset_scanner = init_object_scanner(p_obj);
  while (true) {
    slot = offset_get_ref(offset_scanner, p_obj);
    if (slot == NULL) break;
  
    scan_slot(collector, (Partial_Reveal_Object **)slot);
    offset_scanner = offset_next_ref(offset_scanner);
  }

  scan_weak_reference(collector, p_obj, scan_slot);
  
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
  Space* space = collector->collect_space; 
  GC* gc = collector->gc;
  Partial_Reveal_Object *p_obj = *p_ref;

  if(!obj_belongs_to_space(p_obj, space)) return; 

  /* Fastpath: object has already been forwarded, update the ref slot */
  if(obj_is_forwarded_in_vt(p_obj)) {
    *p_ref = obj_get_forwarding_pointer_in_vt(p_obj);
    return;
  }

  /* only mark the objects that will remain in fspace */
  if(!fspace_object_to_be_forwarded(p_obj, (Fspace*)space)) {
    assert(!obj_is_forwarded_in_vt(p_obj));
    /* this obj remains in fspace, remember its ref slot for next GC if p_ref is not root */
    if( !address_belongs_to_space(p_ref, space) && address_belongs_to_gc_heap(p_ref, gc))
      collector_remset_add_entry(collector, p_ref); 
    
    if(fspace_mark_object((Fspace*)space, p_obj)) 
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
    *p_ref = obj_get_forwarding_pointer_in_vt(p_obj);
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
  collector->trace_stack = pool_get_entry(metadata->free_task_pool);

  /* find root slots saved by 1. active mutators, 2. exited mutators, 3. last cycle collectors */  
  Vector_Block* root_set = pool_get_entry(metadata->gc_rootset_pool);

  /* first step: copy all root objects to trace tasks. */ 
  while(root_set){
    unsigned int* iter = vector_block_iterator_init(root_set);
    while(!vector_block_iterator_end(root_set,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(root_set,iter);
      if(*p_ref == NULL) continue;  /* root ref cann't be NULL, but remset can be */
      if(obj_belongs_to_space(*p_ref, space)){
        collector_tracestack_push(collector, p_ref);
      }
    } 
    vector_block_clear(root_set);
    pool_put_entry(metadata->free_set_pool, root_set);
    root_set = pool_get_entry(metadata->gc_rootset_pool);
  }
  /* put back the last trace_stack task */    
  pool_put_entry(metadata->mark_task_pool, collector->trace_stack);
  
  /* second step: iterate over the trace tasks and forward objects */
  collector->trace_stack = pool_get_entry(metadata->free_task_pool);

retry:
  Vector_Block* trace_task = pool_get_entry(metadata->mark_task_pool);

  while(trace_task){    
    unsigned int* iter = vector_block_iterator_init(trace_task);
    while(!vector_block_iterator_end(trace_task,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(trace_task,iter);
      assert(*p_ref); /* a task can't be NULL, it was checked before put into the task stack */
      /* in sequential version, we only trace same object once, but we were using a local hashset for that,
         which couldn't catch the repetition between multiple collectors. This is subject to more study. */
   
      /* FIXME:: we should not let root_set empty during working, other may want to steal it. 
         degenerate my stack into root_set, and grab another stack */
   
      /* a task has to belong to collected space, it was checked before put into the stack */
      trace_object(collector, p_ref);
    }
    vector_stack_clear(trace_task);
    pool_put_entry(metadata->free_task_pool, trace_task);
    trace_task = pool_get_entry(metadata->mark_task_pool);
  }
  
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

void trace_forward_fspace(Collector* collector) 
{  
  GC* gc = collector->gc;
  Fspace* space = (Fspace*)collector->collect_space;
 
  collector_trace_rootsets(collector);
  
  /* the rest work is not enough for parallelization, so let only one thread go */
  if( collector->thread_handle != 0 ) return;

  collector_process_finalizer_weakref(collector);
  
  gc_update_repointed_refs(collector);
  
  gc_post_process_finalizer_weakref(gc);
  
  reset_fspace_for_allocation(space);  

  return;
  
}

Boolean obj_is_dead_in_minor_forward_collection(Collector *collector, Partial_Reveal_Object *p_obj)
{
  Space *space = collector->collect_space;
  Boolean belong_to_nos = obj_belongs_to_space(p_obj, space);
  
  if(!belong_to_nos)
    return FALSE;
  
  Boolean space_to_be_forwarded = fspace_object_to_be_forwarded(p_obj, (Fspace*)space);
  Boolean forwarded = obj_is_forwarded_in_vt(p_obj);
  Boolean marked = obj_is_marked_in_vt(p_obj);
  
  return (space_to_be_forwarded && !forwarded) || (!space_to_be_forwarded && !marked);
}

void resurrect_obj_tree_after_trace(Collector *collector, Partial_Reveal_Object **p_ref)
{
  GC *gc = collector->gc;
  GC_Metadata* metadata = gc->metadata;
  
  collector->trace_stack = pool_get_entry(metadata->free_task_pool);
  collector_tracestack_push(collector, p_ref);
  pool_put_entry(metadata->mark_task_pool, collector->trace_stack);
  
//collector->rep_set = pool_get_entry(metadata->free_set_pool); /* has got collector->rep_set in caller */
  collector->trace_stack = pool_get_entry(metadata->free_task_pool);
  Vector_Block* trace_task = pool_get_entry(metadata->mark_task_pool);
  while(trace_task){    
    unsigned int* iter = vector_block_iterator_init(trace_task);
    while(!vector_block_iterator_end(trace_task,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(trace_task,iter);
      assert(*p_ref);
      trace_object(collector, p_ref);
    }
    vector_stack_clear(trace_task);
    pool_put_entry(metadata->free_task_pool, trace_task);
    trace_task = pool_get_entry(metadata->mark_task_pool);
  }
  
  trace_task = (Vector_Block*)collector->trace_stack;
  vector_stack_clear(trace_task);
  pool_put_entry(metadata->free_task_pool, trace_task);   
  collector->trace_stack = NULL;
}
