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
 * @author Intel, Gregory Shimansky
 * @version $Revision: 1.1.2.1.4.3 $
 */  


#define LOG_DOMAIN "jni"
#include "cxxlog.h"

#include "platform.h"
#include "Class.h"
#include "environment.h"
#include "object_handles.h"
#include "open/vm_util.h"
#include "vm_threads.h"

#include "ini.h"
#include "vm_arrays.h"
#include "nogc.h"
#include "jni.h"
#include "jni_direct.h"
#include "jni_utils.h"
#include "exceptions.h"

jsize JNICALL GetArrayLength(JNIEnv * UNREF env,
                             jarray array)
{
    TRACE2("jni", "GetArrayLength called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();   //-----------------v
    uint32 length = get_vector_length((Vector_Handle)h->object);
    tmn_suspend_enable();    //---------------------------------^

    return length;
} //GetArrayLength


#define BITS_PER_BYTE 8
#define HIGH_BIT_SET_MASK (1<<((sizeof(unsigned) * BITS_PER_BYTE)-1))
#ifndef NEXT_TO_HIGH_BIT_SET_MASK
#define NEXT_TO_HIGH_BIT_SET_MASK (1<<((sizeof(unsigned) * BITS_PER_BYTE)-2))
#endif /* #ifndef NEXT_TO_HIGH_BIT_SET_MAS */
#define TWO_HIGHEST_BITS_SET_MASK (HIGH_BIT_SET_MASK|NEXT_TO_HIGH_BIT_SET_MASK)

jarray JNICALL NewObjectArray(JNIEnv * UNREF env,
                              jsize length,
                              jclass elementClass,
                              jobject initialElement)
{
    TRACE2("jni", "NewObjectArray called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle elem_handle = (ObjectHandle)initialElement;

    Class* clss = jclass_to_struct_Class(elementClass);

    Class *arr_clss = (Class *)class_get_array_of_class(clss);

    if(!arr_clss) {
         return 0;
    }

    // esostrov: vm_new_vector() may throw an exception. Throwing (not rising) 
    // exceptions is illegal for JNI code. The following code checks and
    // processes a condition that may lead to exception, before calling 
    // vm_new_vector().
    if (0 != (length & TWO_HIGHEST_BITS_SET_MASK)) {
  
        if (length < 0)
            exn_raise_by_name("java/lang/NegativeArraySizeException");
        else
            exn_raise_by_name("java/lang/OutOfMemoryError", 
                    "VM doesn't support arrays of the requested size");

        return NULL;
    }
  tmn_suspend_disable();       //---------------------------------v
 
    Vector_Handle vector = vm_new_vector(arr_clss, length);

    if (exn_raised()) {
         tmn_suspend_enable();
         return NULL;
    }

    ManagedObject *elem;
    if (elem_handle != NULL) {
        elem = elem_handle->object;
    } else {
        elem = NULL;
    }

    // Our GC intializes objects to null, so we have to initialize the array
    // only if the elem is non-null.
    if (elem != NULL) {
        ManagedObject **elems = (ManagedObject **)get_vector_element_address_ref(vector, 0);
        Global_Env *global_env = VM_Global_State::loader_env;
        if (global_env->compress_references) {
            COMPRESSED_REFERENCE elem_offset = compress_reference((ManagedObject *)elem);
            COMPRESSED_REFERENCE *compressed_elems = (COMPRESSED_REFERENCE *)elems;
            for (int i = 0; i < length; i++) {
                compressed_elems[i] = elem_offset;
            }
        } else {
            for (int i = 0; i < length; i++) {
                elems[i] = elem;
            }
        }
    }

    ObjectHandle new_handle = oh_allocate_local_handle();
    new_handle->object = (ManagedObject*)vector;

    tmn_suspend_enable();        //---------------------------------^
    return (jarray)new_handle;
} //NewObjectArray



jobject JNICALL GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index)
{
    TRACE2("jni", "GetObjectArrayElement called");
    assert(hythread_is_suspend_enabled());
    if (array == NULL) {
        // This should not happen.
        ASSERT(0, "Input parameter 'array' is NULL");
        return 0;
    }

    jsize length = GetArrayLength(env, array);
    if ((index < 0) || (index >= length)) {
        char msg[20];
        sprintf(msg, "%d", index);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return 0;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    assert(h->object != NULL);
    void *addr = get_vector_element_address_ref((Vector_Handle)h->object, index);
    ManagedObject *val = get_raw_reference_pointer((ManagedObject **)addr);
    ObjectHandle new_handle = NULL; 
    if (val != NULL) {
       new_handle = oh_allocate_local_handle();
       new_handle->object = val;
    } 

    tmn_suspend_enable();        //---------------------------------^
    return (jobject)new_handle;
} //GetObjectArrayElement



void JNICALL SetObjectArrayElement(JNIEnv *env,
                                   jobjectArray array,
                                   jsize index,
                                   jobject value)
{
    TRACE2("jni", "SetObjectArrayElement called");
    assert(hythread_is_suspend_enabled());
    if (array == NULL) {
        // This should not happen.
        ASSERT(0, "Input parameter 'array' is NULL");
        return;
    }

    jsize length = GetArrayLength(env, array);
    if ((index < 0) || (index >= length)) {
        char msg[20];
        sprintf(msg, "%d", index);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    if (value != NULL) {
        jclass array_jclass = GetObjectClass(env, array);
        jclass actual_element_jclass = GetObjectClass(env, value);
        
        Class* array_class = jclass_to_struct_Class(array_jclass);
        Class* actual_element_class = jclass_to_struct_Class(actual_element_jclass);

        DeleteLocalRef(env, array_jclass);
        DeleteLocalRef(env, actual_element_jclass);

        if (!class_is_subtype_fast(actual_element_class->vtable, array_class->array_element_class)) {
            ThrowNew_Quick(env, "java/lang/ArrayStoreException", actual_element_class->name->bytes);
            return;
        }
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    ManagedObject *elem;
    if (value != NULL) {
        ObjectHandle vh = (ObjectHandle)value;
        elem = vh->object;
    } else {
        elem = NULL;
    }
    Vector_Handle vector = (Vector_Handle)h->object;
    STORE_REFERENCE((ManagedObject *)vector, get_vector_element_address_ref(vector, index), elem);

    tmn_suspend_enable();        //---------------------------------^
} //SetObjectArrayElement




/////////////////////////////////////////////////////////////////////////////
// begin New<Type>Array functions



jbooleanArray JNICALL NewBooleanArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewBooleanArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfBoolean_Class;
    
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jbooleanArray)h;
} //NewBooleanArray



jbyteArray JNICALL NewByteArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewByteArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfByte_Class;
    assert(clss);
 
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jbyteArray)h;
} //NewByteArray



jcharArray JNICALL NewCharArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewCharArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfChar_Class;
 
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jcharArray)h;
} //NewCharArray



jshortArray JNICALL NewShortArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewShortArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfShort_Class;
 
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();

    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jshortArray)h;
} //NewShortArray



jintArray JNICALL NewIntArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewIntArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfInt_Class;
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jintArray)h;
} //NewIntArray



jlongArray JNICALL NewLongArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewLongArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfLong_Class;
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jlongArray)h;
} //NewLongArray



jfloatArray JNICALL NewFloatArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewFloatArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfFloat_Class;
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jfloatArray)h;
} //NewFloatArray



jdoubleArray JNICALL NewDoubleArray(JNIEnv * UNREF env, jsize length)
{
    TRACE2("jni", "NewDoubleArray called");
    assert(hythread_is_suspend_enabled());
    Class *clss = VM_Global_State::loader_env->ArrayOfDouble_Class;
    unsigned sz = vm_array_size(clss->vtable, length);
    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = oh_allocate_local_handle();
    Vector_Handle array = gc_alloc(sz, clss->allocation_handle, vm_get_gc_thread_local());
    if (NULL == array)
    {
        tmn_suspend_enable();
        return NULL;
    }

    set_vector_length(array, length);

#ifdef VM_STATS
    clss->num_allocations++;
    clss->num_bytes_allocated += sz;
#endif //VM_STATS

    h->object = (ManagedObject *)array;

    tmn_suspend_enable();        //---------------------------------^

    return (jdoubleArray)h;
} //NewDoubleArray


// end New<Type>Array functions
/////////////////////////////////////////////////////////////////////////////



/////////////////////////////////////////////////////////////////////////////
// begin Get<Type>ArrayElements functions


jboolean *JNICALL GetBooleanArrayElements(JNIEnv * UNREF env,
                                          jbooleanArray array,
                                          jboolean *isCopy)
{
    TRACE2("jni", "GetBooleanArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jboolean *)get_vector_element_address_bool(java_array, 0);
    } else {
        jboolean *primitive_array = (jboolean *)STD_MALLOC(sizeof(jboolean) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_bool(java_array, 0), sizeof(jbyte) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetBooleanArrayElements



jbyte *JNICALL GetByteArrayElements(JNIEnv * UNREF env,
                                    jbyteArray array,
                                    jboolean *isCopy)
{
    TRACE2("jni", "GetByteArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jbyte *)get_vector_element_address_int8(java_array, 0);
    } else {
        jbyte *primitive_array = (jbyte *)STD_MALLOC(sizeof(jbyte) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_int8(java_array, 0), sizeof(jbyte) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetByteArrayElements



jchar *JNICALL GetCharArrayElements(JNIEnv * UNREF env,
                                    jcharArray array,
                                    jboolean *isCopy)
{
    TRACE2("jni", "GetCharArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jchar *)get_vector_element_address_uint16(java_array, 0);
    } else {
        jchar *primitive_array = (jchar *)STD_MALLOC(sizeof(jchar) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_uint16(java_array, 0), sizeof(jchar) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetCharArrayElements



jshort *JNICALL GetShortArrayElements(JNIEnv * UNREF env,
                                      jshortArray array,
                                      jboolean *isCopy)
{
    TRACE2("jni", "GetShortArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jshort *)get_vector_element_address_int16(java_array, 0);
    } else {
        jshort *primitive_array = (jshort *)STD_MALLOC(sizeof(jshort) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_int16(java_array, 0), sizeof(jshort) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetShortArrayElements



jint *JNICALL GetIntArrayElements(JNIEnv *env,
                                  jintArray array,
                                  jboolean *isCopy)
{
    TRACE2("jni", "GetIntArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jint *)get_vector_element_address_int32(java_array, 0);
    } else {
        jsize length = GetArrayLength(env, array);

        jint *primitive_array = (jint *)STD_MALLOC(sizeof(jint) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_int32(java_array, 0), sizeof(jint) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetIntArrayElements



jlong *JNICALL GetLongArrayElements(JNIEnv * UNREF env,
                                    jlongArray array,
                                    jboolean *isCopy)
{
    TRACE2("jni", "GetLongArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jlong *)get_vector_element_address_int64(java_array, 0);
    } else {
        jlong *primitive_array = (jlong *)STD_MALLOC(sizeof(jlong) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_int64(java_array, 0), sizeof(jlong) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetLongArrayElements



jfloat *JNICALL GetFloatArrayElements(JNIEnv *env,
                                      jfloatArray array,
                                      jboolean *isCopy)
{
    TRACE2("jni", "GetFloatArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jfloat *)get_vector_element_address_f32(java_array, 0);
    } else {
        jsize length = GetArrayLength(env, array);

        jfloat *primitive_array = (jfloat *)STD_MALLOC(sizeof(jfloat) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_f32(java_array, 0), sizeof(jfloat) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetFloatArrayElements



jdouble *JNICALL GetDoubleArrayElements(JNIEnv * UNREF env,
                                        jdoubleArray array,
                                        jboolean *isCopy)
{
    TRACE2("jni", "GetDoubleArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v
    Vector_Handle java_array = (Vector_Handle)h->object;
    int length = get_vector_length(java_array);
    Boolean is_pinned = gc_is_object_pinned((ManagedObject *)java_array);
    tmn_suspend_enable();        //---------------------------------^

    if(is_pinned) {
        // No copy needed.
        if(isCopy) {
            *isCopy = JNI_FALSE;
        }
        Vector_Handle java_array = (Vector_Handle)h->object;
        return (jdouble *)get_vector_element_address_f64(java_array, 0);
    } else {
        jdouble *primitive_array = (jdouble *)STD_MALLOC(sizeof(jdouble) * length);

        tmn_suspend_disable();       //---------------------------------v

        Vector_Handle java_array = (Vector_Handle)h->object;
        memcpy(primitive_array, get_vector_element_address_f64(java_array, 0), sizeof(jdouble) * length);

        tmn_suspend_enable();        //---------------------------------^

        if(isCopy) {
            *isCopy = JNI_TRUE;
        }
        return primitive_array;
    }
} //GetDoubleArrayElements



// end Get<Type>ArrayElements functions
/////////////////////////////////////////////////////////////////////////////



/////////////////////////////////////////////////////////////////////////////
// begin Release<Type>ArrayElements functions


void JNICALL ReleaseBooleanArrayElements(JNIEnv *env,
                                         jbooleanArray array,
                                         jboolean *elems,
                                         jint mode)
{
    TRACE2("jni", "ReleaseBooleanArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jboolean *elems_in_array = (jboolean *)get_vector_element_address_bool(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_bool(java_array, 0), elems, sizeof(jboolean) * length);
            gc_heap_wrote_object((ManagedObject *)java_array);

            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseBooleanArrayElements



void JNICALL ReleaseByteArrayElements(JNIEnv *env,
                                      jbyteArray array,
                                      jbyte *elems,
                                      jint mode)
{
    TRACE2("jni", "ReleaseByteArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jbyte *elems_in_array = (jbyte *)get_vector_element_address_int8(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_int8(java_array, 0), elems, sizeof(jbyte) * length);
            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseByteArrayElements



void JNICALL ReleaseCharArrayElements(JNIEnv *env,
                                      jcharArray array,
                                      jchar *elems,
                                      jint mode)
{
    TRACE2("jni", "ReleaseCharArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jchar *elems_in_array = (jchar *)get_vector_element_address_uint16(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_uint16(java_array, 0), elems, sizeof(jchar) * length);
            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseCharArrayElements



void JNICALL ReleaseShortArrayElements(JNIEnv *env,
                                       jshortArray array,
                                       jshort *elems,
                                       jint mode)
{
    TRACE2("jni", "ReleaseShortArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jshort *elems_in_array = (jshort *)get_vector_element_address_int16(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_int16(java_array, 0), elems, sizeof(jshort) * length);
            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseShortArrayElements



void JNICALL ReleaseIntArrayElements(JNIEnv *env,
                                     jintArray array,
                                     jint *elems,
                                     jint mode)
{
    TRACE2("jni", "ReleaseIntArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jint *elems_in_array = (jint *)get_vector_element_address_int32(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_int32(java_array, 0), elems, sizeof(jint) * length);
            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseIntArrayElements



void JNICALL ReleaseLongArrayElements(JNIEnv *env,
                                      jlongArray array,
                                      jlong *elems,
                                      jint mode)
{
    TRACE2("jni", "ReleaseLongArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jlong *elems_in_array = (jlong *)get_vector_element_address_int64(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_int64(java_array, 0), elems, sizeof(jlong) * length);
            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseLongArrayElements



void JNICALL ReleaseFloatArrayElements(JNIEnv *env,
                                       jfloatArray array,
                                       jfloat *elems,
                                       jint mode)
{
    TRACE2("jni", "ReleaseFloatArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jfloat *elems_in_array = (jfloat *)get_vector_element_address_f32(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_f32(java_array, 0), elems, sizeof(jfloat) * length);

            gc_heap_wrote_object ((ManagedObject *)java_array);
            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseFloatArrayElements



void JNICALL ReleaseDoubleArrayElements(JNIEnv *env,
                                        jdoubleArray array,
                                        jdouble *elems,
                                        jint mode)
{
    TRACE2("jni", "ReleaseDoubleArrayElements called");
    assert(hythread_is_suspend_enabled());
    ObjectHandle h = (ObjectHandle)array;
    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    jdouble *elems_in_array = (jdouble *)get_vector_element_address_f64(java_array, 0);
    bool no_copy = (elems_in_array == elems);

    tmn_suspend_enable();        //---------------------------------^

    if(no_copy) {
        // Nothing to do.
        return;
    }

    switch(mode) {
    case 0:
    case JNI_COMMIT:
        {
            jsize length = GetArrayLength(env, array);
            ObjectHandle h = (ObjectHandle)array;
            tmn_suspend_disable();       //---------------------------------v

            Vector_Handle java_array = (Vector_Handle)h->object;
            memcpy(get_vector_element_address_f64(java_array, 0), elems, sizeof(jdouble) * length);

            tmn_suspend_enable();        //---------------------------------^

            if(mode == 0) {
                STD_FREE(elems);
            }
        }
        break;
    case JNI_ABORT:
        STD_FREE(elems);
        break;
    default:
        ASSERT(0, "Unexpected value of 'mode' input parameter");
        break;
    }
} //ReleaseDoubleArrayElements



// end Release<Type>ArrayElements functions
/////////////////////////////////////////////////////////////////////////////



/////////////////////////////////////////////////////////////////////////////
// begin Get<Type>ArrayRegion functions


void JNICALL GetBooleanArrayRegion (JNIEnv *env,
                                    jobjectArray array,
                                    jsize start,
                                    jsize len,
                                    jboolean *buf)
{
    TRACE2("jni", "GetBooleanArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_bool(java_array, start), sizeof(jboolean) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetBooleanArrayRegion



void JNICALL GetByteArrayRegion    (JNIEnv *env,
                                    jobjectArray array,
                                    jsize start,
                                    jsize len,
                                    jbyte *buf)
{
    TRACE2("jni", "GetByteArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_int8(java_array, start), sizeof(jbyte) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetByteArrayRegion



void JNICALL GetCharArrayRegion(JNIEnv *env,
                                jobjectArray array,
                                jsize start,
                                jsize len,
                                jchar *buf)
{
    TRACE2("jni", "GetCharArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_uint16(java_array, start), sizeof(jchar) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetCharArrayRegion



void JNICALL GetShortArrayRegion(JNIEnv *env,
                                 jobjectArray array, 
                                 jsize start,
                                 jsize len,
                                 jshort *buf)
{
    TRACE2("jni", "GetShortArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_int16(java_array, start), sizeof(jshort) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetShortArrayRegion



void JNICALL GetIntArrayRegion(JNIEnv *env,
                               jobjectArray array,
                               jsize start,
                               jsize len,
                               jint *buf)
{
    TRACE2("jni", "GetIntArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_int32(java_array, start), sizeof(jint) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetIntArrayRegion



void JNICALL GetLongArrayRegion(JNIEnv *env,
                                jobjectArray array,
                                jsize start,
                                jsize len,
                                jlong *buf)
{
    TRACE2("jni", "GetLongArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_int64(java_array, start), sizeof(jlong) * len);

    tmn_suspend_enable();        //---------------------------------^
}  //GetLongArrayRegion



void JNICALL GetFloatArrayRegion(JNIEnv *env,
                                 jobjectArray array,
                                 jsize start,
                                 jsize len,
                                 jfloat *buf)
{
    TRACE2("jni", "GetFloatArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_f32(java_array, start), sizeof(jfloat) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetFloatArrayRegion



void JNICALL GetDoubleArrayRegion(JNIEnv *env,
                                  jobjectArray array,
                                  jsize start,
                                  jsize len,
                                  jdouble *buf)
{
    TRACE2("jni", "GetDoubleArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(buf, get_vector_element_address_f64(java_array, start), sizeof(jdouble) * len);

    tmn_suspend_enable();        //---------------------------------^
} //GetDoubleArrayRegion


// end Get<Type>ArrayRegion functions
/////////////////////////////////////////////////////////////////////////////



/////////////////////////////////////////////////////////////////////////////
// begin Set<Type>ArrayRegion functions


void JNICALL SetBooleanArrayRegion(JNIEnv *env,
                                   jobjectArray array,
                                   jsize start,
                                   jsize len,
                                   jboolean *buf)
{
    TRACE2("jni", "SetBooleanArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_bool(java_array, start), buf, sizeof(jboolean) * len);
    
    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetBooleanArrayRegion



void JNICALL SetByteArrayRegion(JNIEnv *env,
                                jobjectArray array,
                                jsize start,
                                jsize len,
                                jbyte *buf)
{
    TRACE2("jni", "SetByteArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_int8(java_array, start), buf, sizeof(jbyte) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetByteArrayRegion



void JNICALL SetCharArrayRegion(JNIEnv *env,
                                jobjectArray array,
                                jsize start,
                                jsize len,
                                jchar *buf)
{
    TRACE2("jni", "SetCharArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_uint16(java_array, start), buf, sizeof(jchar) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetCharArrayRegion



void JNICALL SetShortArrayRegion(JNIEnv *env,
                                 jobjectArray array,
                                 jsize start,
                                 jsize len,
                                 jshort *buf)
{
    TRACE2("jni", "SetShortArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_int16(java_array, start), buf, sizeof(jshort) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetShortArrayRegion



void JNICALL SetIntArrayRegion(JNIEnv *env,
                               jobjectArray array,
                               jsize start,
                               jsize len,
                               jint *buf)
{
    TRACE2("jni", "SetIntArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_int32(java_array, start), buf, sizeof(jint) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetIntArrayRegion



void JNICALL SetLongArrayRegion(JNIEnv *env,
                                jobjectArray array,
                                jsize start,
                                jsize len,
                                jlong *buf)
{
    TRACE2("jni", "SetLongArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_int64(java_array, start), buf, sizeof(jlong) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetLongArrayRegion



void JNICALL SetFloatArrayRegion(JNIEnv *env,
                                 jobjectArray array,
                                 jsize start,
                                 jsize len,
                                 jfloat *buf)
{
    TRACE2("jni", "SetFloatArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_f32(java_array, start), buf, sizeof(jfloat) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetFloatArrayRegion



void JNICALL SetDoubleArrayRegion(JNIEnv *env,
                                  jobjectArray array,
                                  jsize start,
                                  jsize len,
                                  jdouble *buf)
{
    TRACE2("jni", "SetDoubleArrayRegion called");
    assert(hythread_is_suspend_enabled());
    jsize length = GetArrayLength(env, array);
    jsize end = start + len;
    if(start < 0 || len < 0 || end > length) {
        char msg[30];
        sprintf(msg, "%d..%d", start, end);
        ThrowNew_Quick(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
        return;
    }

    ObjectHandle h = (ObjectHandle)array;

    tmn_suspend_disable();       //---------------------------------v

    Vector_Handle java_array = (Vector_Handle)h->object;
    memcpy(get_vector_element_address_f64(java_array, start), buf, sizeof(jdouble) * len);

    gc_heap_wrote_object ((ManagedObject *)java_array);

    tmn_suspend_enable();        //---------------------------------^
} //SetDoubleArrayRegion



// end Set<Type>ArrayRegion functions
/////////////////////////////////////////////////////////////////////////////


