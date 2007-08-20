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

/**
 * @author Xiao-Feng Li, 2006/10/05
 */

#ifndef _GC_GEN_H_
#define _GC_GEN_H_


#include "../common/gc_common.h"
#include "../thread/gc_thread.h"
#include "../trace_forward/fspace.h"
#include "../mark_compact/mspace.h"
#include "../los/lspace.h"
#include "../finalizer_weakref/finalizer_weakref_metadata.h"

#ifdef GC_GEN_STATS
struct GC_Gen_Stats;
#endif

enum Write_Barrier_Kind{
  WRITE_BARRIER_NIL,  
  WRITE_BARRIER_SLOT,  
  WRITE_BARRIER_OBJECT,
  WRITE_BARRIER_UPDATE      
}; 

/* some globals */
extern POINTER_SIZE_INT NOS_SIZE;

/* heap size limit is not interesting. only for manual tuning purpose */
extern POINTER_SIZE_INT min_heap_size_bytes;
extern POINTER_SIZE_INT max_heap_size_bytes;

/* fspace size is variable, adjusted adaptively within the range */
extern POINTER_SIZE_INT min_nos_size_bytes;
extern POINTER_SIZE_INT max_nos_size_bytes;

struct Gen_Mode_Adaptor;

typedef struct GC_Gen {
  /* <-- First couple of fields overloaded as GC */
  void* heap_start;
  void* heap_end;
  POINTER_SIZE_INT reserved_heap_size;
  POINTER_SIZE_INT committed_heap_size;
  unsigned int num_collections;
  int64 time_collections;
  float survive_ratio;  
  
  /* mutation related info */
  Mutator *mutator_list;
  SpinLock mutator_list_lock;
  unsigned int num_mutators;

  /* collection related info */    
  Collector** collectors;
  unsigned int num_collectors;
  unsigned int num_active_collectors; /* not all collectors are working */

  /* metadata is the pool for rootset, markstack, etc. */  
  GC_Metadata* metadata;
  Finref_Metadata *finref_metadata;

  unsigned int collect_kind; /* MAJOR or MINOR */
  unsigned int last_collect_kind;
  unsigned int cause;/*GC_CAUSE_LOS_IS_FULL, GC_CAUSE_NOS_IS_FULL, or GC_CAUSE_RUNTIME_FORCE_GC*/  
  Boolean collect_result; /* succeed or fail */
  
  Boolean generate_barrier;

  /* FIXME:: this is wrong! root_set belongs to mutator */
  Vector_Block* root_set;
  Vector_Block* weak_root_set;
  Vector_Block* uncompressed_root_set;
  
  //For_LOS_extend
  Space_Tuner* tuner;
  
  /* system info */
  unsigned int _system_alloc_unit;
  unsigned int _machine_page_size_bytes;
  unsigned int _num_processors;
  /* END of GC --> */
  
  Block* blocks;
  Fspace *nos;
  Mspace *mos;
  Lspace *los;
      
  Boolean force_major_collect;
  Gen_Mode_Adaptor* gen_mode_adaptor;
  Boolean force_gen_mode;

#ifdef GC_GEN_STATS
  GC_Gen_Stats* stats; /*used to record stats when collection*/
#endif

} GC_Gen;

//////////////////////////////////////////////////////////////////////////////////////////

void gc_gen_initialize(GC_Gen *gc, POINTER_SIZE_INT initial_heap_size, POINTER_SIZE_INT final_heap_size);
void gc_gen_destruct(GC_Gen *gc);
void gc_gen_collection_verbose_info(GC_Gen *gc, int64 pause_time, int64 mutator_time);
void gc_gen_space_verbose_info(GC_Gen *gc);
void gc_gen_initial_verbose_info(GC_Gen *gc);
void gc_gen_wrapup_verbose(GC_Gen* gc);
                        
inline POINTER_SIZE_INT gc_gen_free_memory_size(GC_Gen* gc)
{  return space_free_memory_size((Blocked_Space*)gc->nos) +
          space_free_memory_size((Blocked_Space*)gc->mos) +
          lspace_free_memory_size(gc->los);  }
                    
inline POINTER_SIZE_INT gc_gen_total_memory_size(GC_Gen* gc)
{  return space_committed_size((Space*)gc->nos) +
          space_committed_size((Space*)gc->mos) +
          lspace_committed_size(gc->los);  }

/////////////////////////////////////////////////////////////////////////////////////////

inline void gc_nos_initialize(GC_Gen* gc, void* start, POINTER_SIZE_INT nos_size, POINTER_SIZE_INT commit_size)
{ fspace_initialize((GC*)gc, start, nos_size, commit_size); }

inline void gc_nos_destruct(GC_Gen* gc)
{ fspace_destruct(gc->nos); }

inline void gc_mos_initialize(GC_Gen* gc, void* start, POINTER_SIZE_INT mos_size, POINTER_SIZE_INT commit_size)
{ mspace_initialize((GC*)gc, start, mos_size, commit_size); }

inline void gc_mos_destruct(GC_Gen* gc)
{ mspace_destruct(gc->mos); }

inline void gc_los_initialize(GC_Gen* gc, void* start, POINTER_SIZE_INT los_size)
{ lspace_initialize((GC*)gc, start, los_size); }

inline void gc_los_destruct(GC_Gen* gc)
{ lspace_destruct(gc->los); }

inline Space* space_of_addr(GC* gc, void* addr)
{
  assert(address_belongs_to_gc_heap(addr, gc));
  if( addr > nos_boundary) return (Space*)((GC_Gen*)gc)->nos;
  if( addr > los_boundary) return (Space*)((GC_Gen*)gc)->mos;
  return (Space*)((GC_Gen*)gc)->los;
}

void* mos_alloc(unsigned size, Allocator *allocator);
void* nos_alloc(unsigned size, Allocator *allocator);
void* los_alloc(unsigned size, Allocator *allocator);
void* los_try_alloc(POINTER_SIZE_INT size, GC* gc);

Space* gc_get_nos(GC_Gen* gc);
Space* gc_get_mos(GC_Gen* gc);
Space* gc_get_los(GC_Gen* gc);

void gc_set_nos(GC_Gen* gc, Space* nos);
void gc_set_mos(GC_Gen* gc, Space* mos);
void gc_set_los(GC_Gen* gc, Space* los);

void gc_decide_collection_algorithm(GC_Gen* gc, char* minor_algo, char* major_algo);
void gc_decide_collection_kind(GC_Gen* gc, unsigned int cause);

void gc_gen_adapt(GC_Gen* gc, int64 pause_time);

void gc_gen_reclaim_heap(GC_Gen* gc);

void gc_gen_assign_free_area_to_mutators(GC_Gen* gc);

void gc_gen_adjust_heap_size(GC_Gen* gc, int64 pause_time);

void gc_gen_update_space_before_gc(GC_Gen* gc);
void gc_gen_update_space_after_gc(GC_Gen* gc);

void gc_gen_mode_adapt_init(GC_Gen *gc);

void gc_gen_iterate_heap(GC_Gen *gc);

extern Boolean GEN_NONGEN_SWITCH ;

inline Boolean obj_is_dead_in_gen_minor_gc(Partial_Reveal_Object *p_obj)
{
  /*
   * The first condition is for supporting switch between nongen and gen minor collection
   * With this kind of switch dead objects in MOS & LOS may be set the mark or fw bit in oi
   */
  return obj_belongs_to_nos(p_obj) && !obj_is_marked_or_fw_in_oi(p_obj);
}

inline Boolean obj_is_dead_in_nongen_minor_gc(Partial_Reveal_Object *p_obj)
{
  return (obj_belongs_to_nos(p_obj) && !obj_is_fw_in_oi(p_obj))
          || (!obj_belongs_to_nos(p_obj) && !obj_is_marked_in_oi(p_obj));
}

inline Boolean obj_is_dead_in_major_gc(Partial_Reveal_Object *p_obj)
{
  return !obj_is_marked_in_vt(p_obj);
}

// clear the two least significant bits of p_obj first
inline Boolean gc_obj_is_dead(GC *gc, Partial_Reveal_Object *p_obj)
{
  assert(p_obj);
  if(gc_match_kind(gc, MINOR_COLLECTION)){
    if(gc_is_gen_mode())
      return obj_is_dead_in_gen_minor_gc(p_obj);
    else
      return obj_is_dead_in_nongen_minor_gc(p_obj);
  } else {
    return obj_is_dead_in_major_gc(p_obj);
  }
}

extern Boolean forward_first_half;
extern void* object_forwarding_boundary;

inline Boolean fspace_obj_to_be_forwarded(Partial_Reveal_Object *p_obj)
{
  if(!obj_belongs_to_nos(p_obj)) return FALSE;
  return forward_first_half? (p_obj < object_forwarding_boundary):(p_obj>=object_forwarding_boundary);
}

inline Boolean obj_need_move(GC *gc, Partial_Reveal_Object *p_obj)
{
  if(gc_is_gen_mode() && gc_match_kind(gc, MINOR_COLLECTION))
    return fspace_obj_to_be_forwarded(p_obj);
  
  Space *space = space_of_addr(gc, p_obj);
  return space->move_object;
}

#endif /* ifndef _GC_GEN_H_ */



