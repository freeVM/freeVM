/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "gc_common.h"
#include "../gen/gen.h"
#include "../mark_sweep/gc_ms.h"
#include "../mark_sweep/wspace.h"
#include "collection_scheduler.h"
#include "gc_concurrent.h"
#include "../verify/verify_live_heap.h"

#define NUM_TRIAL_COLLECTION 10

static int64 time_delay_to_start_mark = 0;

void collection_scheduler_initialize(GC* gc)
{
  
  Collection_Scheduler* collection_scheduler = (Collection_Scheduler*) STD_MALLOC(sizeof(Collection_Scheduler));
  assert(collection_scheduler);
  memset(collection_scheduler, 0, sizeof(Collection_Scheduler));
  
  collection_scheduler->gc = gc;
  gc->collection_scheduler = collection_scheduler;
  time_delay_to_start_mark = 0;
  
  return;
}
void collection_scheduler_destruct(GC* gc)
{
  STD_FREE(gc->collection_scheduler);
}

Boolean gc_need_start_concurrent_mark(GC* gc)
{
  if(!USE_CONCURRENT_MARK) return FALSE;
  //FIXME: GEN mode also needs the support of starting mark after thread resume.
#ifdef USE_MARK_SWEEP_GC
  if(gc_is_concurrent_mark_phase() || gc_mark_is_concurrent()) return FALSE;

  int64 time_current = time_now();
  if( time_current - get_collection_end_time() > time_delay_to_start_mark) 
    return TRUE;
  else return FALSE;
#else
  /*FIXME: concurrent mark is not support in GC_GEN*/
  assert(0);
  if(gc_next_collection_kind((GC_Gen*)gc) == MAJOR_COLLECTION)
    return TRUE;
  else 
    return FALSE;
#endif
}

Boolean gc_need_start_concurrent_sweep(GC* gc)
{
  if(!USE_CONCURRENT_SWEEP) return FALSE;

  if(gc_sweep_is_concurrent()) return FALSE;

  /*if mark is concurrent and STW GC has not started, we should start concurrent sweep*/
  if(gc_mark_is_concurrent() && !gc_is_concurrent_mark_phase(gc))
    return TRUE;
  else
    return FALSE;
}

Boolean gc_need_reset_status(GC* gc)
{
  if(gc_sweep_is_concurrent() && !gc_is_concurrent_sweep_phase(gc))
    return TRUE;
  else
    return FALSE;
}

Boolean gc_need_prepare_rootset(GC* gc)
{
  /*TODO: support on-the-fly root set enumeration.*/
  return FALSE;
}

void gc_update_collection_scheduler(GC* gc, int64 time_mutator, int64 time_mark)
{
  //FIXME: support GEN GC.
#ifdef USE_MARK_SWEEP_GC

  Collection_Scheduler* collection_scheduler = gc->collection_scheduler;   
  Space* space = NULL;

  space = (Space*) gc_get_wspace(gc);

  Space_Statistics* space_stat = space->space_statistic;
  
  unsigned int slot_index = collection_scheduler->last_slot_index_in_window;
  unsigned int num_slot   = collection_scheduler->num_slot_in_window;
  
  collection_scheduler->num_obj_traced_window[slot_index] = space_stat->num_live_obj;
  collection_scheduler->size_alloced_window[slot_index] = space_stat->last_size_free_space;

  collection_scheduler->last_mutator_time = time_mutator;
  collection_scheduler->last_collector_time = time_mark;
  
  collection_scheduler->alloc_rate_window[slot_index] 
    = time_mutator == 0 ? 0 : (float)collection_scheduler->size_alloced_window[slot_index] / time_mutator; 
      
  collection_scheduler->trace_rate_window[slot_index]
    = time_mark == 0 ? 0 : (float)collection_scheduler->num_obj_traced_window[slot_index] / time_mark;

  collection_scheduler->num_slot_in_window = num_slot >= STATISTICS_SAMPLING_WINDOW_SIZE ? num_slot : (++num_slot);
  collection_scheduler->last_slot_index_in_window = (++slot_index)% STATISTICS_SAMPLING_WINDOW_SIZE;

  float sum_alloc_rate = 0;
  float sum_trace_rate = 0;

  unsigned int i;
  for(i = 0; i < collection_scheduler->num_slot_in_window; i++){
    sum_alloc_rate += collection_scheduler->alloc_rate_window[i];
    sum_trace_rate += collection_scheduler->trace_rate_window[i];
  }

  TRACE2("gc.con","Allocation Rate: ");
  for(i = 0; i < collection_scheduler->num_slot_in_window; i++){
    TRACE2("gc.con",i+1<<"  "<<collection_scheduler->alloc_rate_window[i]);
  }
  
  TRACE2("gc.con","Tracing Rate: ");

  for(i = 0; i < collection_scheduler->num_slot_in_window; i++){
    TRACE2("gc.con",i+1<<"  "<<collection_scheduler->trace_rate_window[i]);
  }

  float average_alloc_rate = sum_alloc_rate / collection_scheduler->num_slot_in_window;
  float average_trace_rate = sum_trace_rate / collection_scheduler->num_slot_in_window;

  if(average_alloc_rate == 0 || average_trace_rate == 0){
    time_delay_to_start_mark = 0;
  }else{
    float time_alloc_expected = space_stat->size_free_space / average_alloc_rate;
    float time_trace_expected = space_stat->num_live_obj / average_trace_rate;


    if(time_alloc_expected > time_trace_expected){
      if(gc_concurrent_match_algorithm(OTF_REM_OBJ_SNAPSHOT_ALGO)||gc_concurrent_match_algorithm(OTF_REM_NEW_TARGET_ALGO)){
        collection_scheduler->time_delay_to_start_mark = (int64)((time_alloc_expected - time_trace_expected)*0.65);
      }else if(gc_concurrent_match_algorithm(MOSTLY_CONCURRENT_ALGO)){
        collection_scheduler->time_delay_to_start_mark = (int64)(time_mutator* 0.6);
      }
      
    }else{
      collection_scheduler->time_delay_to_start_mark = 0;
    }

    time_delay_to_start_mark = collection_scheduler->time_delay_to_start_mark;
  }
  INFO2("gc.con","[Concurrent GC] concurrent marking will delay "<<(unsigned int)(time_delay_to_start_mark>>10)<<" ms ");
  //[DEBUG] set to 0 for debugging.
  //time_delay_to_start_mark = 0; 
#endif  
  return;
  
}

unsigned int gc_decide_marker_number(GC* gc)
{
  unsigned int num_active_marker;
  Collection_Scheduler* collection_scheduler = gc->collection_scheduler;   

  if(NUM_TRIAL_COLLECTION == 0 || gc->num_collections < NUM_TRIAL_COLLECTION){
    /*Start trial cycle, collection set to 1 in trial cycle and */
    num_active_marker = 1;
  }else{
    num_active_marker = collection_scheduler->last_marker_num;
    int64 c_time = collection_scheduler->last_collector_time;
    int64 m_time = collection_scheduler->last_mutator_time;
    int64 d_time = collection_scheduler->time_delay_to_start_mark;

    if(num_active_marker == 0) num_active_marker = 1;

    if(c_time > m_time || (float)d_time > (m_time - c_time) * 0.75){      
      INFO2("gc.con","[Concurrent GC] increase marker number.");
      num_active_marker ++;
      if(num_active_marker > gc->num_markers) num_active_marker = gc->num_markers;
    }else if((float)d_time < (m_time - c_time) * 0.25){
      INFO2("gc.con","[Concurrent GC] decrease marker number.");
      num_active_marker --;
      if(num_active_marker == 0)  num_active_marker = 1;
    }
  }
  
  INFO2("gc.con","[Concurrent GC] marker num : "<<num_active_marker<<" ");

  
  collection_scheduler->last_marker_num = num_active_marker;
  return num_active_marker;
}

Boolean gc_try_schedule_collection(GC* gc, unsigned int gc_cause)
{

  if(!try_lock(gc->collection_scheduler_lock)) return FALSE;

  gc_check_concurrent_phase(gc);

  if(gc_need_prepare_rootset(gc)){
    /*TODO:Enable concurrent rootset enumeration.*/
    assert(0);
  }
  
  if(gc_need_start_concurrent_mark(gc)){
    vm_gc_lock_enum();
    INFO2("gc.con", "\nCon GC: concurrent mark start ...\n");
    gc_start_concurrent_mark(gc);
    vm_gc_unlock_enum();
    unlock(gc->collection_scheduler_lock);
    return TRUE;
  }

  if(gc_need_start_concurrent_sweep(gc)){
    gc->num_collections++;
    gc_start_concurrent_sweep(gc);
    unlock(gc->collection_scheduler_lock);
    return TRUE;
  }

  if(gc_need_reset_status(gc)){
    vm_gc_lock_enum();
    int disable_count = hythread_reset_suspend_disable();    
    gc_prepare_rootset(gc);
    gc_reset_after_concurrent_collection(gc);
    vm_resume_threads_after();    
    hythread_set_suspend_disable(disable_count);
    vm_gc_unlock_enum();
    unlock(gc->collection_scheduler_lock);
    return TRUE;
  }
  unlock(gc->collection_scheduler_lock);
  return FALSE;

}


