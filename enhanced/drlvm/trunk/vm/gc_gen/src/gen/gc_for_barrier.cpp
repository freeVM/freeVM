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

#include "../gen/gen.h"
#include "../thread/mutator.h"
#include "gc_for_barrier.h"

/* All the write barrier interfaces need cleanup */

Boolean gen_mode;

/* The implementations are only temporary */
static void gc_slot_write_barrier(Managed_Object_Handle *p_slot, 
                      Managed_Object_Handle p_target) 
{
  if(p_target >= nos_boundary && p_slot < nos_boundary){

    Mutator *mutator = (Mutator *)gc_get_tls();
    assert( addr_belongs_to_nos(p_target) && !addr_belongs_to_nos(p_slot)); 
            
    mutator_remset_add_entry(mutator, (Partial_Reveal_Object**)p_slot);
  }
  return;
}

static void gc_object_write_barrier(Managed_Object_Handle p_object) 
{
  
  if( addr_belongs_to_nos(p_object)) return;

  Mutator *mutator = (Mutator *)gc_get_tls();
  
  Partial_Reveal_Object **p_slot; 
  /* scan array object */
  if (object_is_array((Partial_Reveal_Object*)p_object)) {
    Partial_Reveal_Object* array = (Partial_Reveal_Object*)p_object;
    assert(!obj_is_primitive_array(array));
    
    int32 array_length = vector_get_length((Vector_Handle) array);
    for (int i = 0; i < array_length; i++) {
      p_slot = (Partial_Reveal_Object **)vector_get_element_address_ref((Vector_Handle) array, i);
      if( *p_slot != NULL && addr_belongs_to_nos(*p_slot)){
        mutator_remset_add_entry(mutator, p_slot);
      }
    }   
    return;
  }

  /* scan non-array object */
  Partial_Reveal_Object* p_obj =  (Partial_Reveal_Object*)p_object;   
  int *offset_scanner = init_object_scanner(p_obj);
  while (true) {
    p_slot = (Partial_Reveal_Object**)offset_get_ref(offset_scanner, p_obj);
    if (p_slot == NULL) break;  
    if( addr_belongs_to_nos(*p_slot)){
      mutator_remset_add_entry(mutator, p_slot);
    }
    offset_scanner = offset_next_ref(offset_scanner);
  }

  return;
}

void gc_heap_wrote_object (Managed_Object_Handle p_obj_written)
{
  if( !gc_is_gen_mode() ) return;
  if( object_has_ref_field((Partial_Reveal_Object*)p_obj_written)){
    /* for array copy and object clone */
    gc_object_write_barrier(p_obj_written); 
  }
}

/* The following routines were supposed to be the only way to alter any value in gc heap. */
void gc_heap_write_ref (Managed_Object_Handle p_obj_holding_ref, unsigned offset, Managed_Object_Handle p_target) 
{  assert(0); }

/* FIXME:: this is not the right interface for write barrier */
void gc_heap_slot_write_ref (Managed_Object_Handle p_obj_holding_ref,Managed_Object_Handle *p_slot, Managed_Object_Handle p_target)
{  
  *p_slot = p_target;
  
  if( !gc_is_gen_mode() ) return;
  gc_slot_write_barrier(p_slot, p_target); 
}

/* this is used for global object update, e.g., strings. */
void gc_heap_write_global_slot(Managed_Object_Handle *p_slot,Managed_Object_Handle p_target)
{
  *p_slot = p_target;
  
  /* Since globals are roots, no barrier here */
}
