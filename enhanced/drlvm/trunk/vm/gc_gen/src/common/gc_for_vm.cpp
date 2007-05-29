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

#include <cxxlog.h>
#include "vm_threads.h"
#include "compressed_ref.h"

#include "../gen/gen.h"
#include "interior_pointer.h"
#include "../thread/collector.h"
#include "../verify/verify_live_heap.h"
#include "../finalizer_weakref/finalizer_weakref.h"
#ifdef USE_32BITS_HASHCODE
#include "hashcode.h"
#endif

static GC* p_global_gc = NULL;
Boolean mutator_need_block;

void gc_tls_init();

Boolean gc_requires_barriers() 
{   return p_global_gc->generate_barrier; }

int gc_init() 
{      
  assert(p_global_gc == NULL);
  GC* gc = (GC*)STD_MALLOC(sizeof(GC_Gen));
  assert(gc);
  memset(gc, 0, sizeof(GC));  
  p_global_gc = gc;

  gc_parse_options(gc);
  
  gc_tls_init();

  gc_metadata_initialize(gc); /* root set and mark stack */
  
  gc_gen_initialize((GC_Gen*)gc, min_heap_size_bytes, max_heap_size_bytes);

#ifndef BUILD_IN_REFERENT
  gc_finref_metadata_initialize(gc);
#endif
  collector_initialize(gc);
  gc_init_heap_verification(gc);
  
  mutator_need_block = FALSE;

  return JNI_OK;
}

void gc_wrapup() 
{ 
  GC* gc =  p_global_gc;
  gc_gen_destruct((GC_Gen*)gc);
  gc_metadata_destruct(gc); /* root set and mark stack */
#ifndef BUILD_IN_REFERENT
  gc_finref_metadata_destruct(gc);
#endif
  collector_destruct(gc);

  if( verify_live_heap ){
    gc_terminate_heap_verification(gc);
  }

  STD_FREE(p_global_gc);

  p_global_gc = NULL;
}

#ifdef COMPRESS_REFERENCE
Boolean gc_supports_compressed_references()
{
  vtable_base = vm_get_vtable_base();
  return TRUE;
}
#endif

/* this interface need reconsidering. is_pinned is unused. */
void gc_add_root_set_entry(Managed_Object_Handle *ref, Boolean is_pinned) 
{
  Partial_Reveal_Object** p_ref = (Partial_Reveal_Object**)ref;
  Partial_Reveal_Object* p_obj = *p_ref;
  /* we don't enumerate NULL reference and nos_boundary
     FIXME:: nos_boundary is a static field in GCHelper.java for fast write barrier, not a real object reference 
     this should be fixed that magic Address field should not be enumerated. */
#ifdef COMPRESS_REFERENCE
  if (p_obj == (Partial_Reveal_Object*)HEAP_NULL || p_obj == NULL || p_obj == nos_boundary ) return;
#else
  if (p_obj == NULL || p_obj == nos_boundary ) return;
#endif  
  assert( !obj_is_marked_in_vt(p_obj));
  /* for Minor_collection, it's possible for p_obj be forwarded in non-gen mark-forward GC. 
     The forward bit is actually last cycle's mark bit.
     For Major collection, it's possible for p_obj be marked in last cycle. Since we don't
     flip the bit for major collection, we may find it's marked there.
     So we can't do assert about oi except we really want. */
  assert( address_belongs_to_gc_heap(p_obj, p_global_gc));
  gc_rootset_add_entry(p_global_gc, p_ref);
} 

void gc_add_root_set_entry_interior_pointer (void **slot, int offset, Boolean is_pinned) 
{  
  add_root_set_entry_interior_pointer(slot, offset, is_pinned); 
}

void gc_add_compressed_root_set_entry(REF* ref, Boolean is_pinned)
{
  REF *p_ref = (REF *)ref;
  if(read_slot(p_ref) == NULL) return;
  Partial_Reveal_Object* p_obj = read_slot(p_ref);
  assert(!obj_is_marked_in_vt(p_obj));
  assert( address_belongs_to_gc_heap(p_obj, p_global_gc));
  gc_compressed_rootset_add_entry(p_global_gc, p_ref);
}

/* VM to force GC */
void gc_force_gc() 
{
  vm_gc_lock_enum();
  gc_reclaim_heap(p_global_gc, GC_CAUSE_RUNTIME_FORCE_GC);  
  vm_gc_unlock_enum();
}

void* gc_heap_base_address() 
{  return gc_heap_base(p_global_gc); }

void* gc_heap_ceiling_address() 
{  return gc_heap_ceiling(p_global_gc); }

/* this is a contract between vm and gc */
void mutator_initialize(GC* gc, void* tls_gc_info);
void mutator_destruct(GC* gc, void* tls_gc_info); 
void gc_thread_init(void* gc_info)
{  mutator_initialize(p_global_gc, gc_info);  }

void gc_thread_kill(void* gc_info)
{  mutator_destruct(p_global_gc, gc_info);  }

int64 gc_free_memory() 
{
  return (int64)gc_gen_free_memory_size((GC_Gen*)p_global_gc);
}

/* java heap size.*/
int64 gc_total_memory() 
{
  return (int64)((POINTER_SIZE_INT)gc_gen_total_memory_size((GC_Gen*)p_global_gc)); 
}

int64 gc_max_memory() 
{
  return (int64)((POINTER_SIZE_INT)gc_gen_total_memory_size((GC_Gen*)p_global_gc)); 
}

int64 gc_get_collection_count()
{
  GC* gc =  p_global_gc;
  if (gc != NULL) {
    return (int64) gc->num_collections;
  } else {
    return -1;
  }
}

int64 gc_get_collection_time()
{
  GC* gc =  p_global_gc;
  if (gc != NULL) {
    return (int64) gc->time_collections;
  } else {
    return -1;
  }
}

void gc_vm_initialized()
{ return; }

Boolean gc_is_object_pinned (Managed_Object_Handle obj)
{  return 0; }

void gc_pin_object (Managed_Object_Handle* p_object) 
{  return; }

void gc_unpin_object (Managed_Object_Handle* p_object) 
{  return; }

Managed_Object_Handle gc_get_next_live_object(void *iterator) 
{  assert(0); return NULL; }

unsigned int gc_time_since_last_gc()
{  assert(0); return 0; }

#ifndef USE_32BITS_HASHCODE
#define GCGEN_HASH_MASK 0x1fc
int32 gc_get_hashcode(Managed_Object_Handle p_object) 
{  
   Partial_Reveal_Object *obj = (Partial_Reveal_Object *)p_object;
   if(!obj) return 0;
   assert(address_belongs_to_gc_heap(obj, p_global_gc));
   Obj_Info_Type info = get_obj_info_raw(obj);
   int hash = info & GCGEN_HASH_MASK;
   if (!hash) {
       hash = (int)((((POINTER_SIZE_INT)obj) >> 3) & GCGEN_HASH_MASK);
       if(!hash)  hash = (0x173 & GCGEN_HASH_MASK);
       unsigned int new_info = (unsigned int)(info | hash);
       while (true) {
         unsigned int temp = atomic_cas32((volatile unsigned int*)(&obj->obj_info), new_info, info);
         if (temp == info) break;
         info = get_obj_info_raw(obj);
         new_info = (unsigned int)(info | hash);
       }
   }
   return hash;
}
#else //USE_32BITS_HASHCODE
int32 gc_get_hashcode(Managed_Object_Handle p_object)
{
  Partial_Reveal_Object* p_obj = (Partial_Reveal_Object*)p_object;
  if(!p_obj) return 0;
  assert(address_belongs_to_gc_heap(p_obj, p_global_gc));
  Obj_Info_Type info = get_obj_info_raw(p_obj);
  unsigned int new_info = 0;
  int hash;
  
  switch(info & HASHCODE_MASK){
    case HASHCODE_SET_UNALLOCATED:
      hash = hashcode_gen((void*)p_obj);
      break;
    case HASHCODE_SET_ATTACHED:
      hash = hashcode_lookup(p_obj,info);
      break;
    case HASHCODE_SET_BUFFERED:
      hash = hashcode_lookup(p_obj,info);
      break;
    case HASHCODE_UNSET:
      new_info = (unsigned int)(info | HASHCODE_SET_BIT);
      while (true) {
        unsigned int temp = atomic_cas32((volatile unsigned int*)(&p_obj->obj_info), new_info, info);
        if (temp == info) break;
        info = get_obj_info_raw(p_obj);
        new_info =  (unsigned int)(info | HASHCODE_SET_BIT);
      }
      hash = hashcode_gen((void*)p_obj);
      break;
    default:
      assert(0);
  }
  return hash;
}
#endif //USE_32BITS_HASHCODE

void gc_finalize_on_exit()
{
  if(!IGNORE_FINREF )
    put_all_fin_on_exit(p_global_gc);
}

/* for future use
 * void gc_phantom_ref_enqueue_hook(void *p_reference)
 * {
 *   if(special_reference_type((Partial_Reveal_Object *)p_reference) == PHANTOM_REFERENCE){
 *     Partial_Reveal_Object **p_referent_field = obj_get_referent_field(p_reference);
 *     *p_referent_field = (Partial_Reveal_Object *)((unsigned int)*p_referent_field | PHANTOM_REF_ENQUEUED_MASK | ~PHANTOM_REF_PENDING_MASK);
 *   }
 * }
 */

extern Boolean JVMTI_HEAP_ITERATION;
void gc_iterate_heap() {
    // data structures in not consistent for heap iteration
    if (!JVMTI_HEAP_ITERATION) return;

    gc_gen_iterate_heap((GC_Gen *)p_global_gc);
}

void gc_set_mutator_block_flag()
{  mutator_need_block = TRUE; }

Boolean gc_clear_mutator_block_flag()
{
  UNSAFE_REGION_START
  Boolean old_flag = mutator_need_block;
  mutator_need_block = FALSE;
  UNSAFE_REGION_END
  return old_flag;
}
