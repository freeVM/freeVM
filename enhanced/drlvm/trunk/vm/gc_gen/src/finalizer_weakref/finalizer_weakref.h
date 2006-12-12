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
 * @author Li-Gang Wang, 2006/11/30
 */

#ifndef _FINALIZER_WEAKREF_H_
#define _FINALIZER_WEAKREF_H_

#include "finalizer_weakref_metadata.h"
#include "../thread/collector.h"

/* Phantom status: for future use
 * #define PHANTOM_REF_ENQUEUE_STATUS_MASK 0x3
 * #define PHANTOM_REF_ENQUEUED_MASK 0x1
 * #define PHANTOM_REF_PENDING_MASK 0x2
 *
 * inline Partial_Reveal_Object *get_reference_pointer(Partial_Reveal_Object *p_obj)
 * {
 *   return (Partial_Reveal_Object *)((unsigned int)(p_obj)&(~PHANTOM_REF_ENQUEUE_STATUS_MASK));
 * }
 * inline void update_reference_pointer(Partial_Reveal_Object **p_ref, Partial_Reveal_Object *p_target_obj)
 * {
 *   unsigned int temp = (unsigned int)*p_ref;
 * 
 *   temp &= PHANTOM_REF_ENQUEUE_STATUS_MASK;
 *   temp |= (unsigned int)p_target_obj;
 *   *p_ref = (Partial_Reveal_Object *)temp;
 * }
 */

inline Partial_Reveal_Object **obj_get_referent_field(Partial_Reveal_Object *p_obj)
{
  assert(p_obj);
  return (Partial_Reveal_Object **)(( Byte*)p_obj+get_gc_referent_offset());
}

typedef void (* Scan_Slot_Func)(Collector *collector, Partial_Reveal_Object **p_ref);
inline void scan_weak_reference(Collector *collector, Partial_Reveal_Object *p_obj, Scan_Slot_Func scan_slot)
{
  WeakReferenceType type = special_reference_type(p_obj);
  if(type == NOT_REFERENCE)
    return;
  unsigned int collect_kind = collector->gc->collect_kind;
  Partial_Reveal_Object **p_referent_field = obj_get_referent_field(p_obj);
  Partial_Reveal_Object *p_referent = *p_referent_field;
  if (!p_referent) return;
  switch(type){
    case SOFT_REFERENCE :
      if(collect_kind==MINOR_COLLECTION)
        scan_slot(collector, p_referent_field);
      else
        collector_softref_set_add_entry(collector, p_obj);
      break;
    case WEAK_REFERENCE :
      collector_weakref_set_add_entry(collector, p_obj);
      break;
    case PHANTOM_REFERENCE :
      collector_phanref_set_add_entry(collector, p_obj);
      break;
    default :
      assert(0);
      break;
  }
}


extern void mutator_reset_objects_with_finalizer(Mutator *mutator);
extern void gc_set_objects_with_finalizer(GC *gc);
extern void collector_reset_weakref_sets(Collector *collector);

extern void collector_process_finalizer_weakref(Collector *collector);
extern void gc_post_process_finalizer_weakref(GC *gc);
extern void process_objects_with_finalizer_on_exit(GC *gc);

extern void gc_update_finalizer_weakref_repointed_refs(GC* gc);
extern void gc_activate_finalizer_weakref_threads(GC *gc);

#endif // _FINALIZER_WEAKREF_H_
