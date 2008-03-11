/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef _CALL_BACKS_H_
#define _CALL_BACKS_H_

#include "jni.h"
#include "jvmti.h"

namespace jdwp {

     //-----------------------------------------------------------------------------
   // Heap callbacks, used in Instances, InstanceCounts command
   //-----------------------------------------------------------------------------
    namespace CallBacks {
        /**
         * Describes a reference from an object or the VM (the referrer) 
        * to another object (the referree) or a heap root to a referree. 
        */
        jint JNICALL HeapReferenceCallback
        (jvmtiHeapReferenceKind reference_kind, 
         const jvmtiHeapReferenceInfo* reference_info, 
         jlong class_tag, 
         jlong referrer_class_tag, 
         jlong size, 
         jlong* tag_ptr, 
         jlong* referrer_tag_ptr, 
         jint length, 
         void* user_data) ;

        /**
        * This callback will describe a static field if the object is a class, 
        * and otherwise will describe an instance field. 
        */
        jint JNICALL PrimitiveFieldCallback
        (jvmtiHeapReferenceKind kind, 
         const jvmtiHeapReferenceInfo* info, 
         jlong object_class_tag, 
         jlong* object_tag_ptr, 
         jvalue value, 
         jvmtiPrimitiveType value_type, 
         void* user_data);
     
        /**
        * Describes the values in an array of a primitive type.
        */
         jint JNICALL ArrayPrimitiveValueCallback
        (jlong class_tag, 
         jlong size, 
         jlong* tag_ptr, 
         jint element_count, 
         jvmtiPrimitiveType element_type, 
         const void* elements, 
         void* user_data);
     
        /**
        * Describes the value of a java.lang.String. 
        */ 
        jint JNICALL StringPrimitiveValueCallback
        (jlong class_tag, 
         jlong size, 
         jlong* tag_ptr, 
         const jchar* value, 
         jint value_length, 
         void* user_data);
    } //CallBacks namespace
} // jdwp namesoace
#endif//_CALL_BACKS_H_