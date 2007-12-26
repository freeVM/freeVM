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

#ifndef _COLLECTOR_ALLOC_H_
#define _COLLECTOR_ALLOC_H_

#include "gc_thread.h"

#ifdef USE_32BITS_HASHCODE
#include "../common/hashcode.h"
#endif

#include "../semi_space/sspace.h"

extern Space_Alloc_Func mos_alloc;

//FIXME: MINOR_ALGO is static
extern unsigned int MINOR_ALGO;

/* NOS forward obj to other space in MINOR_COLLECTION */
FORCE_INLINE Partial_Reveal_Object* collector_forward_object(Collector* collector, Partial_Reveal_Object* p_obj)
{
  Obj_Info_Type oi = get_obj_info_raw(p_obj);

  /* forwarded by somebody else */
  if (oi & FORWARD_BIT){
     return NULL;
  }
  
  /* otherwise, try to alloc it. mos should always has enough space to hold nos during collection */
  unsigned int size = vm_object_size(p_obj);
  
#ifdef USE_32BITS_HASHCODE
  Boolean obj_is_set_hashcode = hashcode_is_set(p_obj);
  Boolean obj_hashcode_attached = FALSE;
  if(obj_is_set_hashcode){
    size += GC_OBJECT_ALIGNMENT;
   /* the tospace of semispace GC may have objects with hashcode attached*/
   obj_hashcode_attached = hashcode_is_attached(p_obj);
  }
#endif

  Partial_Reveal_Object* p_targ_obj = NULL;

  Allocator* allocator = (Allocator*)collector;
  
  /* can also use collector->collect_space->collect_algorithm */
  if( MINOR_ALGO == MINOR_NONGEN_SEMISPACE_POOL){

    p_targ_obj = (Partial_Reveal_Object*)semispace_forward_obj(p_obj, size, allocator);
    if( !p_targ_obj )
      allocator = ((Collector*)collector)->backup_allocator;

  }else{ /* other non-ss algorithms. FIXME:: I am going to remove this branch if it has no perf impact. */
    
    if(is_collector_local_alloc){  /* try local alloc first if collector supports it. Marksweep doesn't. */
      p_targ_obj = thread_local_alloc(size, allocator);
    }
  }
    
  if(!p_targ_obj){
    p_targ_obj = (Partial_Reveal_Object*)mos_alloc(size, allocator);
  }
    
  if(p_targ_obj == NULL){
    /* failed to forward an obj */
    collector->result = FALSE;
    TRACE2("gc.collect", "failed to forward an obj, minor collection failed.");
    return NULL;
  }
    
  /* else, take the obj by setting the forwarding flag atomically 
     we don't put a simple bit in vt because we need compute obj size later. */
  Obj_Info_Type target_oi = (Obj_Info_Type)obj_ptr_to_ref(p_targ_obj);
  if (oi != atomic_casptrsz((volatile POINTER_SIZE_INT*)get_obj_info_addr(p_obj), (target_oi |FORWARD_BIT), oi)) {
    /* forwarded by other, we need unalloc the allocated obj. We may waste some space if the allocation switched
       block. The remaining part of the switched block cannot be revivied for next allocation of 
       object that has smaller size than this one. */
    assert( obj_is_fw_in_oi(p_obj));
    thread_local_unalloc(size, allocator);
    return NULL;
  }

  assert((((POINTER_SIZE_INT)p_targ_obj) % GC_OBJECT_ALIGNMENT) == 0);

#ifdef USE_32BITS_HASHCODE
  if(obj_is_set_hashcode && !obj_hashcode_attached){ 
    size -= GC_OBJECT_ALIGNMENT;  //restore object size for memcpy from original object
    oi = forward_obj_attach_hashcode(p_targ_obj, p_obj ,oi, size);  //get oi for following set_obj_info
  }
#endif //USE_32BITS_HASHCODE

  memcpy(p_targ_obj, p_obj, size);  //copy once. 

  /* restore oi, which currently is the forwarding pointer. 
     for semispace GC, p_targ_obj is still in NOS, we should clear its oi mark_bits. */
  if( obj_belongs_to_nos(p_targ_obj) || gc_is_gen_mode() )
    /* we need clear the bit to give a clean status (it's possibly unclean due to partial forwarding) */
    set_obj_info(p_targ_obj, oi&DUAL_MARKBITS_MASK);
  else{
#ifdef MARK_BIT_FLIPPING 
  /* we mark it to make the object look like other original live objects in MOS */
    set_obj_info(p_targ_obj, oi|FLIP_MARK_BIT);
#else 
    set_obj_info(p_targ_obj, oi);  
#endif // MARK_BIT_FLIPPING 
  }
  
#ifdef USE_32BITS_HASHCODE
  if(obj_hashcode_attached){
    /* this is tricky. In fallback compaction, we need iterate the heap for live objects, 
       so we need know the exact object size. The hashbit of original copy is overwritten by forwarding pointer.
       We use this bit in VT to indicate the original copy has attached hashcode.
       We can't set the bit earlier before the memcopy. */
    obj_sethash_in_vt(p_obj);  
  }
#endif //USE_32BITS_HASHCODE

  return p_targ_obj;  
 
}

#endif /* _COLLECTOR_ALLOC_H_ */
