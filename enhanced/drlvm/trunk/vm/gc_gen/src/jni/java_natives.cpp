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
 
#include <open/vm_gc.h>
#include <jni.h>
#include "open/vm_util.h"
#include "environment.h"
#include "../thread/gc_thread.h"
#include "../gen/gen.h"
#include "java_support.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_apache_harmony_drlvm_gc_gen_GCHelper
 * Method:    TLSFreeOffset
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_harmony_drlvm_gc_1gen_GCHelper_TLSGCOffset(JNIEnv *e, jclass c)
{
    return (jint)tls_gc_offset;
}

JNIEXPORT jobject JNICALL Java_org_apache_harmony_drlvm_gc_1gen_GCHelper_getNosBoundary(JNIEnv *e, jclass c)
{
    return (jobject)nos_boundary;
}

JNIEXPORT jboolean JNICALL Java_org_apache_harmony_drlvm_gc_1gen_GCHelper_getGenMode(JNIEnv *e, jclass c)
{
    return (jboolean)gc_is_gen_mode();
}

JNIEXPORT void JNICALL Java_org_apache_harmony_drlvm_gc_1gen_GCHelper_helperCallback(JNIEnv *e, jclass c)
{
    java_helper_inlined = TRUE;

    POINTER_SIZE_INT obj = *(POINTER_SIZE_INT*)c;
    /* a trick to get the GCHelper_class j.l.c in order to manipulate its 
       fields in GC native code */ 
    Class_Handle *vm_class_ptr = (Class_Handle *)(obj + VM_Global_State::loader_env->vm_class_offset);
    GCHelper_clss = *vm_class_ptr;
}

#ifdef __cplusplus
}
#endif
