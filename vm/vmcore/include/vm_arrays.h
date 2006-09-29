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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.1.4.3 $
 */  


#ifndef _VM_ARRAYS_H_
#define _VM_ARRAYS_H_

#include "environment.h"
#include "open/gc.h"
#include "object_layout.h"
#include "open/types.h"
#include "open/vm_util.h"
#include "heap.h"

inline unsigned vm_array_size(VTable *vector_vtable, int length)
{
    unsigned array_element_size = vector_vtable->array_element_size;
    assert(array_element_size);
    unsigned first_elem_offset;
    if(array_element_size < 8) {
        first_elem_offset = VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4;
    } else {
        first_elem_offset = VM_VECTOR_FIRST_ELEM_OFFSET_8;
    }
    unsigned size = first_elem_offset + length * array_element_size;
    size = ( ((size + (GC_OBJECT_ALIGNMENT - 1)) & (~(GC_OBJECT_ALIGNMENT - 1))) );
    assert((size % GC_OBJECT_ALIGNMENT) == 0);
    return size;
} //vm_array_size


inline VTable *get_vector_vtable(Vector_Handle vector)
{
    ManagedObject *v = (ManagedObject *)vector;
    return v->vt();
} //get_vector_vtable



inline int32 get_vector_length(Vector_Handle vector)
{
    VM_Vector *v = (VM_Vector *)vector;
    return v->get_length();
} //get_vector_length



inline void set_vector_length(Vector_Handle vector, int32 length)
{
    VM_Vector *v = (VM_Vector *)vector;
    v->set_length(length);
} //set_vector_length



inline int8 *get_vector_element_address_bool(Vector_Handle vector, int32 idx)
{
    return (int8 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(int8)));
} //get_vector_element_address_bool



inline int8 *get_vector_element_address_int8(Vector_Handle vector, int32 idx)
{
    return (int8 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(int8)));
} //get_vector_element_address_int8



inline int16 *get_vector_element_address_int16(Vector_Handle vector, int32 idx)
{
    return (int16 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(int16)));
} //get_vector_element_address_int16



inline uint16 *get_vector_element_address_uint16(Vector_Handle vector, int32 idx)
{
    return (uint16 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(uint16)));
} //get_vector_element_address_uint16



inline int32 *get_vector_element_address_int32(Vector_Handle vector, int32 idx)
{
    return (int32 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(int32)));
} //get_vector_element_address_int32



inline int64 *get_vector_element_address_int64(Vector_Handle vector, int32 idx)
{
    return (int64 *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_8 + ((POINTER_SIZE_INT)idx * sizeof(int64)));
} //get_vector_element_address_int64



inline float *get_vector_element_address_f32(Vector_Handle vector, int32 idx)
{
    return (float *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_1_2_4 + ((POINTER_SIZE_INT)idx * sizeof(float)));
} //get_vector_element_address_f32



inline double *get_vector_element_address_f64(Vector_Handle vector, int32 idx)
{
    return (double *)((POINTER_SIZE_INT)vector + (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_8 + ((POINTER_SIZE_INT)idx * sizeof(double)));
} //get_vector_element_address_f64



// 20030321 Be careful with the result of this procedure: if references are compressed the result will be
// the address of an element containing a COMPRESSED_REFERENCE, not a ManagedObject *.
inline ManagedObject **get_vector_element_address_ref(Vector_Handle vector, int32 idx)
{
    return (ManagedObject **)((POINTER_SIZE_INT)vector + 
                                      (POINTER_SIZE_INT)VM_VECTOR_FIRST_ELEM_OFFSET_REF + 
                                      ((POINTER_SIZE_INT)idx * 
                                       (VM_Global_State::loader_env->compress_references? sizeof(COMPRESSED_REFERENCE) : sizeof(ManagedObject *))));
} //get_vector_element_address_ref





// Shallow copy an array

enum ArrayCopyResult { ACR_Okay, ACR_NullPointer, ACR_TypeMismatch, ACR_BadIndices };

ArrayCopyResult array_copy(ManagedObject* src, int32 src_off, ManagedObject* dst, int32 dst_off, int32 count);

#endif //_VM_ARRAYS_H_
