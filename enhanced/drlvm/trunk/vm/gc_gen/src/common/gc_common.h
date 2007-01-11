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

#ifndef _GC_COMMON_H_
#define _GC_COMMON_H_

#include "port_vmem.h"

#include "platform_lowlevel.h"

#include "open/types.h"
#include "open/vm_gc.h"
#include "open/vm.h"
#include "open/gc.h"
#include "port_malloc.h"

#include "gc_for_class.h"
#include "gc_platform.h"

#include "../gen/gc_for_barrier.h"

#define null 0

#define KB  (1<<10)
#define MB  (1<<20)

#define BYTES_PER_WORD 4
#define BITS_PER_BYTE 8 
#define BITS_PER_WORD 32

#define MASK_OF_BYTES_PER_WORD (BYTES_PER_WORD-1) /* 0x11 */

#define BIT_SHIFT_TO_BYTES_PER_WORD 2 /* 2 */
#define BIT_SHIFT_TO_BITS_PER_BYTE 3
#define BIT_SHIFT_TO_BITS_PER_WORD 5
#define BIT_SHIFT_TO_KILO 10 

#define BIT_MASK_TO_BITS_PER_WORD ((1<<BIT_SHIFT_TO_BITS_PER_WORD)-1)

#define GC_OBJ_SIZE_THRESHOLD (4*KB)

typedef void (*TaskType)(void*);

enum Collection_Algorithm{
  COLLECTION_ALGOR_NIL,
  
  /*minor nongen collection*/
  MINOR_NONGEN_FORWARD_POOL,
  
  /* minor gen collection */
  MINOR_GEN_FORWARD_POOL,
  
  /* major collection */
  MAJOR_COMPACT_SLIDE,
  MAJOR_COMPACT_MOVE
  
};

enum Collection_Kind {
  MINOR_COLLECTION,
  MAJOR_COLLECTION,
  FALLBACK_COLLECTION  
};

extern Boolean IS_FALLBACK_COMPACTION;  /* only for mark/fw bits debugging purpose */

enum GC_CAUSE{
  GC_CAUSE_NIL,
  GC_CAUSE_NOS_IS_FULL,
  GC_CAUSE_LOS_IS_FULL,
  GC_CAUSE_RUNTIME_FORCE_GC
};

inline POINTER_SIZE_INT round_up_to_size(POINTER_SIZE_INT size, int block_size) 
{  return (size + block_size - 1) & ~(block_size - 1); }

inline POINTER_SIZE_INT round_down_to_size(POINTER_SIZE_INT size, int block_size) 
{  return size & ~(block_size - 1); }

/****************************************/
/* Return a pointer to the ref field offset array. */
inline int* object_ref_iterator_init(Partial_Reveal_Object *obj)
{
  GC_VTable_Info *gcvt = obj_get_gcvt(obj);  
  return gcvt->gc_ref_offset_array;    
}

inline Partial_Reveal_Object** object_ref_iterator_get(int* iterator, Partial_Reveal_Object* obj)
{
  return (Partial_Reveal_Object**)((int)obj + *iterator);
}

inline int* object_ref_iterator_next(int* iterator)
{
  return iterator+1;
}

/* original design */
inline int *init_object_scanner (Partial_Reveal_Object *obj) 
{
  GC_VTable_Info *gcvt = obj_get_gcvt(obj);  
  return gcvt->gc_ref_offset_array;
}

inline void *offset_get_ref(int *offset, Partial_Reveal_Object *obj) 
{    return (*offset == 0)? NULL: (void*)((Byte*) obj + *offset); }

inline int *offset_next_ref (int *offset) 
{  return offset + 1; }

/****************************************/

inline Boolean obj_is_marked_in_vt(Partial_Reveal_Object *obj) 
{  return ((POINTER_SIZE_INT)obj_get_vt_raw(obj) & CONST_MARK_BIT); }

inline Boolean obj_mark_in_vt(Partial_Reveal_Object *obj) 
{  
  Partial_Reveal_VTable* vt = obj_get_vt_raw(obj);
  if((unsigned int)vt & CONST_MARK_BIT) return FALSE;
  obj_set_vt(obj, (unsigned int)vt | CONST_MARK_BIT);
  return TRUE;
}

inline void obj_unmark_in_vt(Partial_Reveal_Object *obj) 
{ 
  Partial_Reveal_VTable* vt = obj_get_vt_raw(obj);
  obj_set_vt(obj, (unsigned int)vt & ~CONST_MARK_BIT);
}

inline Boolean obj_is_marked_or_fw_in_oi(Partial_Reveal_Object *obj)
{ return get_obj_info_raw(obj) & DUAL_MARKBITS; }


inline void obj_clear_dual_bits_in_oi(Partial_Reveal_Object *obj)
{  
  Obj_Info_Type info = get_obj_info_raw(obj);
  set_obj_info(obj, (unsigned int)info & DUAL_MARKBITS_MASK);
}

/****************************************/
#ifndef MARK_BIT_FLIPPING

inline Partial_Reveal_Object *obj_get_fw_in_oi(Partial_Reveal_Object *obj) 
{
  assert(get_obj_info_raw(obj) & CONST_FORWARD_BIT);
  return (Partial_Reveal_Object*) (get_obj_info_raw(obj) & ~CONST_FORWARD_BIT);
}

inline Boolean obj_is_fw_in_oi(Partial_Reveal_Object *obj) 
{  return (get_obj_info_raw(obj) & CONST_FORWARD_BIT); }

inline void obj_set_fw_in_oi(Partial_Reveal_Object *obj,void *dest)
{  
  assert(!(get_obj_info_raw(obj) & CONST_FORWARD_BIT));
  set_obj_info(obj,(Obj_Info_Type)dest | CONST_FORWARD_BIT); 
}


inline Boolean obj_is_marked_in_oi(Partial_Reveal_Object *obj) 
{  return ( get_obj_info_raw(obj) & CONST_MARK_BIT ); }

inline Boolean obj_mark_in_oi(Partial_Reveal_Object *obj) 
{  
  Obj_Info_Type info = get_obj_info_raw(obj);
  if ( info & CONST_MARK_BIT ) return FALSE;

  set_obj_info(obj, info|CONST_MARK_BIT);
  return TRUE;
}

inline void obj_unmark_in_oi(Partial_Reveal_Object *obj) 
{  
  Obj_Info_Type info = get_obj_info_raw(obj);
  info = info & ~CONST_MARK_BIT;
  set_obj_info(obj, info);
  return;
}

/* **********************************  */
#else /* ifndef MARK_BIT_FLIPPING */

inline void mark_bit_flip()
{ 
  FLIP_FORWARD_BIT = FLIP_MARK_BIT;
  FLIP_MARK_BIT ^= DUAL_MARKBITS; 
}

inline Partial_Reveal_Object *obj_get_fw_in_oi(Partial_Reveal_Object *obj) 
{
  assert(get_obj_info_raw(obj) & FLIP_FORWARD_BIT);
  return (Partial_Reveal_Object*) get_obj_info(obj);
}

inline Boolean obj_is_fw_in_oi(Partial_Reveal_Object *obj) 
{  return (get_obj_info_raw(obj) & FLIP_FORWARD_BIT); }

inline void obj_set_fw_in_oi(Partial_Reveal_Object *obj, void *dest)
{ 
  assert(IS_FALLBACK_COMPACTION || (!(get_obj_info_raw(obj) & FLIP_FORWARD_BIT))); 
  /* This assert should always exist except it's fall back compaction. In fall-back compaction
     an object can be marked in last time minor collection, which is exactly this time's fw bit,
     because the failed minor collection flipped the bits. */

  /* It's important to clear the FLIP_FORWARD_BIT before collection ends, since it is the same as
     next minor cycle's FLIP_MARK_BIT. And if next cycle is major, it is also confusing
     as FLIP_FORWARD_BIT. (The bits are flipped only in minor collection). */
  set_obj_info(obj,(Obj_Info_Type)dest | FLIP_FORWARD_BIT); 
}

inline Boolean obj_mark_in_oi(Partial_Reveal_Object* p_obj)
{
  Obj_Info_Type info = get_obj_info_raw(p_obj);
  assert((info & DUAL_MARKBITS ) != DUAL_MARKBITS);
  
  if( info & FLIP_MARK_BIT ) return FALSE;  
  
  info = info & DUAL_MARKBITS_MASK;
  set_obj_info(p_obj, info|FLIP_MARK_BIT);
  return TRUE;
}

inline Boolean obj_unmark_in_oi(Partial_Reveal_Object* p_obj)
{
  Obj_Info_Type info = get_obj_info_raw(p_obj);
  info = info & DUAL_MARKBITS_MASK;
  set_obj_info(p_obj, info);
  return TRUE;
}

inline Boolean obj_is_marked_in_oi(Partial_Reveal_Object* p_obj)
{
  Obj_Info_Type info = get_obj_info_raw(p_obj);
  return (info & FLIP_MARK_BIT);
}

#endif /* MARK_BIT_FLIPPING */

/* all GCs inherit this GC structure */
struct Mutator;
struct Collector;
struct GC_Metadata;
struct Finref_Metadata;
struct Vector_Block;
struct Space_Tuner;

typedef struct GC{
  void* heap_start;
  void* heap_end;
  unsigned int reserved_heap_size;
  unsigned int committed_heap_size;
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
  
  /* metadata is the pool for rootset, tracestack, etc. */  
  GC_Metadata* metadata;
  Finref_Metadata *finref_metadata;

  unsigned int collect_kind; /* MAJOR or MINOR */
  unsigned int last_collect_kind;
  Boolean collect_result; /* succeed or fail */

  Boolean generate_barrier;
  
  /* FIXME:: this is wrong! root_set belongs to mutator */
  Vector_Block* root_set;

  //For_LOS_extend
  Space_Tuner* tuner;

}GC;

void mark_scan_pool(Collector* collector);

inline void mark_scan_heap(Collector* collector)
{
    mark_scan_pool(collector);    
}

inline void* gc_heap_base(GC* gc){ return gc->heap_start; }
inline void* gc_heap_ceiling(GC* gc){ return gc->heap_end; }
inline Boolean address_belongs_to_gc_heap(void* addr, GC* gc)
{
  return (addr >= gc_heap_base(gc) && addr < gc_heap_ceiling(gc));
}

void gc_parse_options(GC* gc);
void gc_reclaim_heap(GC* gc, unsigned int gc_cause);

/* generational GC related */

extern Boolean NOS_PARTIAL_FORWARD;

//#define STATIC_NOS_MAPPING

#ifdef STATIC_NOS_MAPPING

  //#define NOS_BOUNDARY ((void*)0x2ea20000)  //this is for 512M
  #define NOS_BOUNDARY ((void*)0x40000000) //this is for 256M

	#define nos_boundary NOS_BOUNDARY

#else /* STATIC_NOS_MAPPING */

	extern void* nos_boundary;

#endif /* STATIC_NOS_MAPPING */

inline Boolean addr_belongs_to_nos(void* addr)
{ return addr >= nos_boundary; }

inline Boolean obj_belongs_to_nos(Partial_Reveal_Object* p_obj)
{ return addr_belongs_to_nos(p_obj); }

extern void* los_boundary;

inline Boolean obj_is_moved(Partial_Reveal_Object* p_obj)
{ return p_obj >= los_boundary; }

#endif //_GC_COMMON_H_
