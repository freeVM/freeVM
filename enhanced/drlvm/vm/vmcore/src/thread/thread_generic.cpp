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
 * @version $Revision: 1.1.2.5.4.5 $
 */  


#define LOG_DOMAIN "thread"
#include "cxxlog.h"

#include "platform.h"
#include "vm_process.h"
#include <assert.h>

//MVM
#include <iostream>

using namespace std;

#include <signal.h>
#include <stdlib.h>

#if defined (PLATFORM_NT)
#include <direct.h>
#elif defined (PLATFORM_POSIX)
#include <sys/time.h>
#include <unistd.h>
#endif

#include <apr_pools.h>

#include "open/hythread.h"
#include "open/jthread.h"
#include "open/thread_externals.h"
#include "open/types.h"
#include "open/vm_util.h"
#include "open/gc.h"

#include "environment.h"
#include "vm_strings.h"
#include "object_layout.h"
#include "Class.h"
#include "classloader.h"
#include "vm_threads.h"
#include "nogc.h"
#include "ini.h"
#include "m2n.h"
#include "exceptions.h"
#include "jit_intf.h"
#include "exception_filter.h"
#include "vm_threads.h"
#include "jni_utils.h"
#include "object.h"
#include "platform_core_natives.h"
#include "heap.h"
#include "verify_stack_enumeration.h"
#include "sync_bits.h"
#include "vm_stats.h"
#include "native_utils.h"
#include "thread_manager.h"
#include "object_generic.h"
#include "thread_generic.h"
#include "mon_enter_exit.h"
#include "jni_direct.h"
#include "port_malloc.h"


#ifdef PLATFORM_NT
// wjw -- following lines needs to be generic for all OSs
#include "java_lang_thread_nt.h"
#endif

#ifdef _IPF_
#include "java_lang_thread_ipf.h"
#include "../m2n_ipf_internal.h"
#elif defined _EM64T_
#include "java_lang_thread_em64t.h"
#include "../m2n_em64t_internal.h"
#else
#include "java_lang_thread_ia32.h"
#include "../m2n_ia32_internal.h"
#endif

extern struct JNINativeInterface_ jni_vtable;

/**
 * Runs java.lang.Thread.detach() method.
 */
static IDATA run_java_detach(jthread java_thread) {
    static Method * detach = NULL;
    const char * method_name = "detach";
    const char * descriptor = "(Ljava/lang/Throwable;)V";
    jvalue args[2];
    JNIEnv * jni_env;
    Global_Env * vm_env;
    Class * thread_class;

    assert(hythread_is_suspend_enabled());

    jni_env = jthread_get_JNI_env(java_thread);
    vm_env = jni_get_vm_env(jni_env);
    thread_class = vm_env->java_lang_Thread_Class;

    if (detach == NULL) {
        detach = class_lookup_method(thread_class, method_name, descriptor);
        if (detach == NULL) {
            TRACE("Failed to find thread's detach method " << descriptor << " , exception = " << exn_get());
            return TM_ERROR_INTERNAL;
        }
    }

    // Initialize arguments.
    args[0].l = java_thread;
    args[1].l = exn_get();
    exn_clear();

    hythread_suspend_disable();
    vm_execute_java_method_array((jmethodID) detach, 0, args);
    hythread_suspend_enable();

    if (exn_raised()) {
        TRACE("java.lang.Thread.detach(Throwable) method completed with an exception: " << exn_get_name());
        return TM_ERROR_INTERNAL;
    }
    return TM_ERROR_NONE;
}

/**
 * Attaches thread current thread to VM.
 */
jint vm_attach(JavaVM * java_vm, JNIEnv ** p_jni_env) {
    M2nFrame * p_m2n;
    VM_thread * p_vm_thread;
    ObjectHandles * p_handles;
    
    // It seems to be reasonable to have suspend enabled state here.
    // It is unsafe to perform operations which require suspend disabled
    // mode until current thread is not attaced to VM.
    assert(hythread_is_suspend_enabled());

    p_vm_thread = p_TLS_vmthread;
    if (p_vm_thread != NULL) {
        if (java_vm != p_vm_thread->jni_env->vm) {
            return TM_ERROR_INTERNAL;
        }
        *p_jni_env = p_vm_thread->jni_env;
        return JNI_OK;
    }

    p_vm_thread = get_a_thread_block((JavaVM_Internal *)java_vm);
    if (p_vm_thread == NULL) {
        TRACE2("thread", "can't get a thread block for a new thread");
        return JNI_ENOMEM;
    }
    
    // Create JNI environment for current thread.
    p_vm_thread->jni_env = (JNIEnv_Internal *) apr_palloc(p_vm_thread->pool, sizeof(JNIEnv_Internal));
    
    // Initialize JNI environment.
    p_vm_thread->jni_env->functions = &jni_vtable;
    p_vm_thread->jni_env->vm = (JavaVM_Internal *)java_vm;
    p_vm_thread->jni_env->reserved0 = (void *)0x1234abcd;
    *p_jni_env = p_vm_thread->jni_env;
    
    // Create top level M2N frame.
    p_m2n = (M2nFrame*) apr_palloc(p_vm_thread->pool, sizeof(M2nFrame));
    // Create local handles.
    p_handles = (ObjectHandles*) apr_palloc(p_vm_thread->pool, sizeof(ObjectHandlesNew));
    if (p_vm_thread->jni_env == NULL || p_m2n == NULL ||p_handles == NULL) {
        TRACE2("thread", "can't get a thread block for a new thread");
        return JNI_ENOMEM;
    }
    
    init_stack_info();

    m2n_null_init(p_m2n);
    m2n_set_last_frame(p_m2n);

    oh_null_init_handles(p_handles);

    m2n_set_local_handles(p_m2n, p_handles);
    m2n_set_frame_type(p_m2n, FRAME_NON_UNWINDABLE);
    gc_thread_init(&p_vm_thread->_gc_private_information);

    assert(hythread_is_suspend_enabled());
    return JNI_OK;
}

/**
 * Detaches current thread from VM.
 */
jint vm_detach(jthread java_thread) {
    VM_thread * p_vm_thread;
    jint status;
    
    assert(hythread_is_suspend_enabled());

    status = run_java_detach(java_thread);
    if (status != JNI_OK) return status;

    hythread_suspend_disable();

    p_vm_thread = get_thread_ptr();

    // Notify GC about thread detaching.
    gc_thread_kill(&p_vm_thread->_gc_private_information);
    assert(p_vm_thread->gc_frames == 0);
    // Remove current VM_thread from TLS.
    set_TLS_data(NULL);
    // Destroy current VM_thread structure.
    apr_pool_destroy(p_vm_thread->pool);
    
    hythread_suspend_enable();

    return JNI_OK;

/** TODO: Check if we need these actions!!!
    jint monitor_count;
    jobject * monitor_objects;
    
#ifndef NDEBUG
    hythread_t tm_native_thread = jthread_get_native_thread();
    assert(tm_native_thread);
    assert(tm_native_thread == hythread_self());
#endif

    // 2) release all owned monitors if any.
    status = jthread_get_owned_monitors(java_thread, &monitor_count, &monitor_objects);
    // TODO: how to deal with OutOfMemoryError?
    assert(status != TM_ERROR_NONE);
    for (int i = 0; i < monitor_count; i++) {
        jthread_monitor_notify_all(monitor_objects[i]);
        jthread_monitor_exit(monitor_objects[i]);
    }
    // 3) Remove tm_thread_t pointer from java.lang.Thread object.
    vm_jthread_set_tm_data(jthread java_thread, NULL);
*/
}