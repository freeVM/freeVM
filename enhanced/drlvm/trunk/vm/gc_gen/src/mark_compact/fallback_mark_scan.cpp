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

#include "../common/gc_metadata.h"
#include "../thread/collector.h"
#include "../gen/gen.h"
#include "../finalizer_weakref/finalizer_weakref.h"

static void scan_slot(Collector* collector, Partial_Reveal_Object** p_ref)
{
  Partial_Reveal_Object* p_obj = *p_ref;
  if(p_obj==NULL) return;

  collector_tracestack_push(collector, p_ref);
  
  return;
}

static void scan_object(Collector* collector, Partial_Reveal_Object **p_ref)
{
  Partial_Reveal_Object *p_obj = *p_ref;
  assert(p_obj);
  
  if(obj_belongs_to_nos(p_obj) && obj_is_fw_in_oi(p_obj)){
    assert(obj_get_vt(p_obj) == obj_get_vt(obj_get_fw_in_oi(p_obj)));
    p_obj = obj_get_fw_in_oi(p_obj);
    assert(p_obj);
    *p_ref = p_obj;
  }
  
  if(!obj_mark_in_vt(p_obj))
    return;
  
  if( !object_has_ref_field(p_obj) ) return;
  
    /* scan array object */
  if (object_is_array(p_obj)) {
    Partial_Reveal_Object* array = p_obj;
    assert(!obj_is_primitive_array(array));
    
    int32 array_length = vector_get_length((Vector_Handle) array);
    for (int i = 0; i < array_length; i++) {
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object**)vector_get_element_address_ref((Vector_Handle) array, i);
      scan_slot(collector, p_ref);
    }   
    return;
  }

  /* scan non-array object */
  int *offset_scanner = init_object_scanner(p_obj);
  while (true) {
    Partial_Reveal_Object** p_ref = (Partial_Reveal_Object**)offset_get_ref(offset_scanner, p_obj);
    if (p_ref == NULL) break; /* terminating ref slot */
  
    scan_slot(collector, p_ref);
    offset_scanner = offset_next_ref(offset_scanner);
  }

#ifndef BUILD_IN_REFERENT
  scan_weak_reference(collector, p_obj, scan_slot);
#endif
  
  return;
}


static void trace_object(Collector* collector, Partial_Reveal_Object **p_ref)
{ 
  scan_object(collector, p_ref);
  
  Vector_Block* trace_stack = collector->trace_stack;
  while( !vector_stack_is_empty(trace_stack)){
    p_ref = (Partial_Reveal_Object **)vector_stack_pop(trace_stack); 
    scan_object(collector, p_ref);
    trace_stack = collector->trace_stack;
  }
    
  return; 
}

/* for marking phase termination detection */
static volatile unsigned int num_finished_collectors = 0;

void fallback_mark_scan_heap(Collector* collector)
{ 
  GC* gc = collector->gc;
  GC_Metadata* metadata = gc->metadata;
  
  assert(gc->collect_kind == FALLBACK_COLLECTION);

  /* reset the num_finished_collectors to be 0 by one collector. This is necessary for the barrier later. */
  unsigned int num_active_collectors = gc->num_active_collectors;
  atomic_cas32( &num_finished_collectors, 0, num_active_collectors);
   
  collector->trace_stack = free_task_pool_get_entry(metadata);

  Vector_Block* root_set = pool_iterator_next(metadata->gc_rootset_pool);

  /* first step: copy all root objects to mark tasks. 
      FIXME:: can be done sequentially before coming here to eliminate atomic ops */ 
  while(root_set){
    unsigned int* iter = vector_block_iterator_init(root_set);
    while(!vector_block_iterator_end(root_set,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object** )*iter;
      iter = vector_block_iterator_advance(root_set,iter);

      Partial_Reveal_Object* p_obj = *p_ref;
      /* root ref can't be NULL, (remset may have NULL ref entry, but this function is only for MAJOR_COLLECTION */
      assert(p_obj != NULL);
      
      collector_tracestack_push(collector, p_ref);

    } 
    root_set = pool_iterator_next(metadata->gc_rootset_pool);
  }
  /* put back the last trace_stack task */    
  pool_put_entry(metadata->mark_task_pool, collector->trace_stack);
  
  /* second step: iterate over the mark tasks and scan objects */
  /* get a task buf for the mark stack */
  collector->trace_stack = free_task_pool_get_entry(metadata);

retry:
  Vector_Block* mark_task = pool_get_entry(metadata->mark_task_pool);
  
  while(mark_task){
    unsigned int* iter = vector_block_iterator_init(mark_task);
    while(!vector_block_iterator_end(mark_task,iter)){
      Partial_Reveal_Object** p_ref = (Partial_Reveal_Object **)*iter;
      iter = vector_block_iterator_advance(mark_task,iter);

      /* FIXME:: we should not let mark_task empty during working, , other may want to steal it. 
         degenerate my stack into mark_task, and grab another mark_task */
      trace_object(collector, p_ref);
    } 
    /* run out one task, put back to the pool and grab another task */
   vector_stack_clear(mark_task);
   pool_put_entry(metadata->free_task_pool, mark_task);
   mark_task = pool_get_entry(metadata->mark_task_pool);      
  }
  
  /* termination detection. This is also a barrier.
     NOTE:: We can simply spin waiting for num_finished_collectors, because each 
     generated new task would surely be processed by its generating collector eventually. 
     So code below is only for load balance optimization. */
  atomic_inc32(&num_finished_collectors);
  while(num_finished_collectors != num_active_collectors){
    if( !pool_is_empty(metadata->mark_task_pool)){
      atomic_dec32(&num_finished_collectors);
      goto retry;  
    }
  }
     
  /* put back the last mark stack to the free pool */
  mark_task = (Vector_Block*)collector->trace_stack;
  vector_stack_clear(mark_task);
  pool_put_entry(metadata->free_task_pool, mark_task);   
  collector->trace_stack = NULL;
  
  return;
}
