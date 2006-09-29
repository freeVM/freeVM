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
 * @author Andrey Chernyshev
 * @version $Revision: 1.1.2.1.4.4 $
 */  

#include <apr_atomic.h>

#include "jni.h"
#include "Class.h"
#include "object_handles.h"
#include "environment.h"
#include "atomics.h"
#include "vm_arrays.h"
#include "port_atomic.h"


/*
 * Common atomic functions.
 * Platform dependent atomic functions are in corresponding arch/ subfolders.
 */

bool gc_heap_slot_cas_ref_compressed (Managed_Object_Handle p_base_of_object_with_slot,
                                      COMPRESSED_REFERENCE *p_slot,
                                      Managed_Object_Handle expected,
                                      Managed_Object_Handle value)
{
    assert (p_base_of_object_with_slot != NULL);

    COMPRESSED_REFERENCE compressed_expected = compress_reference((ManagedObject*)expected);
    COMPRESSED_REFERENCE compressed_value = compress_reference((ManagedObject*)value);

    bool result =
        (apr_atomic_cas32(
            (uint32*)p_slot, (uint32)compressed_value, (uint32)compressed_expected)
        == (uint32)compressed_expected);

    // TODO: uncomment foolowing line for GC
    //INTERNAL(gc_write_barrier) (p_base_of_object_with_slot);
    return result;
}


bool gc_heap_slot_cas_ref (Managed_Object_Handle p_base_of_object_with_slot,
                           Managed_Object_Handle *p_slot,
                           Managed_Object_Handle expected,
                           Managed_Object_Handle value)
{
    assert (p_base_of_object_with_slot != NULL);
    bool res = (apr_atomic_casptr (
        (volatile void **)p_slot, value, expected) == expected);
    // TODO: uncomment foolowing line for GC 
    //INTERNAL(gc_write_barrier) (p_base_of_object_with_slot);
    return res;
}


JNIEXPORT jlong getFieldOffset(JNIEnv * env, jobject field) 
{
    Field *f = (Field *) FromReflectedField(env, field);
    return f->get_offset();
}

JNIEXPORT jboolean compareAndSetObjectField
(JNIEnv * env, jobject UNREF accesor, jobject obj, jlong offset, jobject expected, jobject value)
{

    assert(hythread_is_suspend_enabled());

    ObjectHandle h = (ObjectHandle)obj;
    ObjectHandle v = (ObjectHandle)value;
    ObjectHandle e = (ObjectHandle)expected;

    tmn_suspend_disable();

    Byte *java_ref = (Byte *)h->object;
    ManagedObject **field_addr = (ManagedObject **)(java_ref + offset);

    ManagedObject *val = (v==NULL)?NULL:v->object;
    ManagedObject *exp = (e==NULL)?NULL:e->object;

    bool result;

    if (VM_Global_State::loader_env->compress_references) {
        result = gc_heap_slot_cas_ref_compressed((Managed_Object_Handle)(java_ref),
                                                 (COMPRESSED_REFERENCE *)(field_addr),
                                                 (Managed_Object_Handle)(exp),
                                                 (Managed_Object_Handle)(val));
    }
    else
    {
        result = gc_heap_slot_cas_ref((Managed_Object_Handle)(java_ref),
                                      (Managed_Object_Handle *)(field_addr),
                                      (Managed_Object_Handle)(exp),
                                      (Managed_Object_Handle)(val));
    }

    tmn_suspend_enable();
    return  (jboolean)(result?JNI_TRUE:JNI_FALSE);
}


JNIEXPORT jboolean compareAndSetBooleanField
(JNIEnv * env, jobject UNREF accesor, jobject obj, jlong offset, jboolean expected, jboolean value)
{
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)obj;

    tmn_suspend_disable();

    Byte *java_ref = (Byte *)h->object;
    jboolean *field_addr = (jboolean *)(java_ref + offset);
    bool result =
        (port_atomic_cas8((uint8 *)field_addr, (uint8)value, (uint8)expected) == (uint8)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}

                  
JNIEXPORT jboolean compareAndSetIntField
(JNIEnv * env, jobject UNREF accesor, jobject obj, jlong offset, jint expected, jint value)
{

    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)obj;

    tmn_suspend_disable();

    Byte *java_ref = (Byte *)h->object;
    jint *field_addr = (jint *)(java_ref + offset);
    bool result =
      (apr_atomic_cas32((uint32 *)field_addr, (uint32)value, (uint32)expected) == (uint32)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}


JNIEXPORT jboolean compareAndSetLongField
(JNIEnv * env, jobject UNREF accesor, jobject obj, jlong offset, jlong expected, jlong value)
{
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)obj;

    tmn_suspend_disable();

    Byte *java_ref = (Byte *)h->object;
    jlong *field_addr = (jlong *)(java_ref + offset);

    bool result =
      (port_atomic_cas64((uint64 *)field_addr, (uint64)value, (uint64)expected) == (uint64)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}


JNIEXPORT jboolean compareAndSetObjectArray
(JNIEnv * UNREF env, jobject UNREF self, jobjectArray array, jint index, jobject expected, jobject value)
{
    assert(hythread_is_suspend_enabled());

    tmn_suspend_disable();

    Vector_Handle vector_handle = (Vector_Handle) ((ObjectHandle) array)->object;

    ManagedObject ** element_address =
            get_vector_element_address_ref(vector_handle, index);

    ManagedObject *exp = (expected==NULL) ? NULL : ((ObjectHandle) expected)->object;
    ManagedObject *val = (value==NULL) ? NULL : ((ObjectHandle) value)->object;

    bool result;

    if (VM_Global_State::loader_env->compress_references) {
        result = gc_heap_slot_cas_ref_compressed((Managed_Object_Handle)(vector_handle),
                                                 (COMPRESSED_REFERENCE *)(element_address),
                                                 (Managed_Object_Handle)(exp),
                                                 (Managed_Object_Handle)(val));
    }
    else
    {
        result = gc_heap_slot_cas_ref((Managed_Object_Handle)(vector_handle),
                                      (Managed_Object_Handle *)(element_address),
                                      (Managed_Object_Handle)(exp),
                                      (Managed_Object_Handle)(val));
    }

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}


JNIEXPORT jboolean compareAndSetBooleanArray
(JNIEnv * UNREF env, jobject UNREF self, jbooleanArray array, jint index, jboolean expected, jboolean value)
{
    assert(hythread_is_suspend_enabled());

    tmn_suspend_disable();

    jboolean * field_addr = (jboolean *) get_vector_element_address_int8(
            (Vector_Handle) ((ObjectHandle) array)->object,
            index);

    bool result =
        (port_atomic_cas8((uint8 *)field_addr, (uint8)value, (uint8)expected) == (uint8)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}

                  
JNIEXPORT jboolean compareAndSetIntArray
(JNIEnv * UNREF env, jobject UNREF self, jintArray array, jint index, jint expected, jint value)
{
    assert(hythread_is_suspend_enabled());

    tmn_suspend_disable();

    jint * field_addr = get_vector_element_address_int32(
            (Vector_Handle) ((ObjectHandle) array)->object,
            index);

    bool result =
      (apr_atomic_cas32((uint32 *)field_addr, (uint32)value, (uint32)expected) == (uint32)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}


JNIEXPORT jboolean compareAndSetLongArray
(JNIEnv * UNREF env, jobject UNREF self, jlongArray array, jint index, jlong expected, jlong value)
{
    assert(hythread_is_suspend_enabled());

    tmn_suspend_disable();

    jlong * field_addr = get_vector_element_address_int64(
            (Vector_Handle) ((ObjectHandle) array)->object,
            index);

    bool result =
      (port_atomic_cas64((uint64 *)field_addr, (uint64)value, (uint64)expected) == (uint64)expected);

    tmn_suspend_enable();
    return (jboolean)(result?JNI_TRUE:JNI_FALSE);
}
