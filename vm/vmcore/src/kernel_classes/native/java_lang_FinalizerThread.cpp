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
 * @author Intel, Pavel Afremov
 * @version $Revision: 1.1.2.2.4.3 $
 */

/**
 * @file java_lang_FinalizerThread.cpp
 *
 * This file is a part of kernel class natives VM core component.
 * It contains implementation for native methods of java_lang_FinalizerThread kernel
 * class. Not all of the methods are implemented now.
 */

#include "java_lang_FinalizerThread.h"
#include "open/gc.h"
#include "open/hythread_ext.h"
#include "finalize.h"
#include "port_sysinfo.h"
#include "vm_threads.h"

/**
 * Implements getObject(..) method.
 * For details see kernel classes component documentation.
 *
 * Class:     java_lang_FinalizerThread
 * Method:    getObject
 * Signature: ()I;
 */
JNIEXPORT jint JNICALL Java_java_lang_FinalizerThread_getProcessorsQuantity
  (JNIEnv *, jclass)
{
    return (jint) port_CPUs_number();
}

/**
 * Implements doFinalization(..) method.
 * For details see kernel classes component documentation.
 *
 * Class:     java_lang_FinalizerThread
 * Method:    doFinalization
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_java_lang_FinalizerThread_doFinalization
  (JNIEnv *, jclass, jint quantity)
{
    return (jint) vm_do_finalization(quantity);
}
/**
 * Implements getFinalizersQuantity() method.
 * For details see kernel classes component documentation.
 *
 * Class:     java_lang_FinalizerThread
 * Method:    getFinalizersQuantity
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_FinalizerThread_getFinalizersQuantity
  (JNIEnv *, jclass)
{
    return (jint) vm_get_finalizable_objects_quantity();
}

/**
 * Implements fillFinalizationQueueOnExit() method.
 * For details see kernel classes component documentation.
 *
 * Class:     java_lang_FinalizerThread
 * Method:    fillFinalizationQueueOnExit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_FinalizerThread_fillFinalizationQueueOnExit
  (JNIEnv *, jclass)
{
    tmn_suspend_disable();
    gc_finalize_on_exit();
    tmn_suspend_enable();
}
