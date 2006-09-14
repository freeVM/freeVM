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
 * @author Gregory Shimansky
 * @version $Revision: 1.1.2.1.4.5 $
 */  
/*
 * JVMTI local variables API
 */

#include "jvmti_direct.h"
#include "jvmti_utils.h"
#include "vm_threads.h"
#include "interpreter_exports.h"
#include "object_handles.h"
#include "environment.h"
#include "open/vm_util.h"
#include "cxxlog.h"
#include "thread_generic.h"
#include "open/jthread.h"
#include "suspend_checker.h"
#include "stack_iterator.h"
#include "stack_trace.h"
#include "jit_intf_cpp.h"

/*
 * Local Variable functions:
 *
 *     Get Local Variable - Object
 *     Get Local Variable - Int
 *     Get Local Variable - Long
 *     Get Local Variable - Float
 *     Get Local Variable - Double
 *     Set Local Variable - Object
 *     Set Local Variable - Int
 *     Set Local Variable - Long
 *     Set Local Variable - Float
 *     Set Local Variable - Double
 *
 * These functions are used to retrieve or set the value of a
 * local variable. The variable is identified by the depth of the
 * frame containing its value and the variable's slot number within
 * that frame. The mapping of variables to slot numbers can be
 * obtained with the function GetLocalVariableTable.
 *
 * OPTIONAL Functionality
 */

static JNIEnv * jvmti_test_jenv = jni_native_intf;

static jvmtiError
GetLocal_checkArgs(jvmtiEnv* env,
                        jthread *thread,
                        jint depth,
                        jint UNREF slot,
                        void* value_ptr)
{
    jint state;
    jvmtiError err;

    // TODO: check error condition: JVMTI_ERROR_MUST_POSSESS_CAPABILITY

    if (*thread == 0) {
        *thread = getCurrentThread();
    }

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    err = jvmtiGetThreadState(env, *thread, &state);

    if (err != JVMTI_ERROR_NONE) {
        return err;
    }

    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    if ((state & JVMTI_THREAD_STATE_ALIVE) == 0) {
        return JVMTI_ERROR_THREAD_NOT_ALIVE;
    }

    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    if (depth < 0) {
        return JVMTI_ERROR_ILLEGAL_ARGUMENT;
    }

    // check error condition: JVMTI_ERROR_NULL_POINTER
    if (value_ptr == 0) {
        return JVMTI_ERROR_NULL_POINTER;
    }

    return JVMTI_ERROR_NONE;
}

#define GET_JIT_FRAME_CONTEXT                               \
    StackIterator *si = si_create_from_native(vm_thread);   \
                                                            \
    if (!si_get_method(si)) /* Skip native VM frame */      \
        si_goto_previous(si);                               \
                                                            \
    while (depth > 0 && !si_is_past_end(si))                \
    {                                                       \
        if (si_get_method(si))                              \
            depth -= 1 + si_get_inline_depth(si);           \
        si_goto_previous(si);                               \
    }                                                       \
                                                            \
    if (si_is_past_end(si))                                 \
    {                                                       \
        if (thread_suspended)                               \
            jthread_resume(thread);                         \
        si_free(si);                                        \
        return JVMTI_ERROR_NO_MORE_FRAMES;                  \
    }                                                       \
                                                            \
    if (si_is_native(si))                                   \
    {                                                       \
        if (thread_suspended)                               \
            jthread_resume(thread);                         \
        si_free(si);                                        \
        return JVMTI_ERROR_OPAQUE_FRAME;                    \
    }                                                       \
                                                            \
    JitFrameContext *jfc = si_get_jit_context(si);          \
    CodeChunkInfo *cci = si_get_code_chunk_info(si);        \
    JIT *jit = cci->get_jit();                              \
    Method *method = cci->get_method();

jvmtiError JNICALL
jvmtiGetLocalObject(jvmtiEnv* env,
                    jthread thread,
                    jint depth,
                    jint slot,
                    jobject* value_ptr)
{
    TRACE2("jvmti.locals", "GetLocalObject called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    // check error condition: JVMTI_ERROR_NULL_POINTER
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, value_ptr);
    if (err != JVMTI_ERROR_NONE)
        return err;

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // check error condition: JVMTI_ERROR_INVALID_SLOT
        // check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        err = interpreter.interpreter_ti_getObject(env,
            vm_thread, depth, slot, value_ptr);
    else
    {
        GET_JIT_FRAME_CONTEXT;
        ManagedObject *obj;

        tmn_suspend_disable();
        OpenExeJpdaError result = jit->get_local_var(method, jfc, slot,
            VM_DATA_TYPE_MP, &obj);
        si_free(si);

        if (result == EXE_ERROR_NONE)
        {
            ObjectHandle oh = oh_allocate_local_handle();
            oh->object = obj;
            *value_ptr = oh;
        }
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiGetLocalInt(jvmtiEnv* env,
                 jthread thread,
                 jint depth,
                 jint slot,
                 jint* value_ptr)
{
    TRACE2("jvmti.locals", "GetLocalInt called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    // check error condition: JVMTI_ERROR_NULL_POINTER
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, value_ptr);
    if (err != JVMTI_ERROR_NONE)
        return err;

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // check error condition: JVMTI_ERROR_INVALID_SLOT
        // check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        err = interpreter.interpreter_ti_getLocal32(env,
            vm_thread, depth, slot, value_ptr);
    else
    {
        GET_JIT_FRAME_CONTEXT;

        tmn_suspend_disable();
        OpenExeJpdaError result = jit->get_local_var(method, jfc, slot,
            VM_DATA_TYPE_INT32, value_ptr);
        si_free(si);
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiGetLocalLong(jvmtiEnv* env,
                  jthread thread,
                  jint depth,
                  jint slot,
                  jlong* value_ptr)
{
    TRACE2("jvmti.locals", "GetLocalLong called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    // check error condition: JVMTI_ERROR_NULL_POINTER
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, value_ptr);
    if (err != JVMTI_ERROR_NONE)
        return err;

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // check error condition: JVMTI_ERROR_INVALID_SLOT
        // check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        err = interpreter.interpreter_ti_getLocal64(env,
            vm_thread, depth, slot, value_ptr);
    else
    {
        GET_JIT_FRAME_CONTEXT;

        tmn_suspend_disable();
        OpenExeJpdaError result = jit->get_local_var(method, jfc, slot,
            VM_DATA_TYPE_INT64, value_ptr);
        si_free(si);
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiGetLocalFloat(jvmtiEnv* env,
                   jthread thread,
                   jint depth,
                   jint slot,
                   jfloat* value_ptr)
{
    TRACE2("jvmti.locals", "GetLocalFloat called");
    SuspendEnabledChecker sec;
    return jvmtiGetLocalInt(env, thread, depth, slot, (jint*)value_ptr);
}

jvmtiError JNICALL
jvmtiGetLocalDouble(jvmtiEnv* env,
                    jthread thread,
                    jint depth,
                    jint slot,
                    jdouble* value_ptr)
{
    TRACE2("jvmti.locals", "GetLocalDouble called");
    SuspendEnabledChecker sec;
    return jvmtiGetLocalLong(env, thread, depth, slot, (jlong*)value_ptr);
}

jvmtiError JNICALL
jvmtiSetLocalObject(jvmtiEnv* env,
                    jthread thread,
                    jint depth,
                    jint slot,
                    jobject value)
{
    TRACE2("jvmti.locals", "SetLocalObject called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, &value);
    if (err != JVMTI_ERROR_NONE)
        return err;

    // check error condition: JVMTI_ERROR_INVALID_OBJECT
    if (value != 0) {
        ObjectHandle handle = (ObjectHandle) value;
        tmn_suspend_disable();
        ManagedObject *obj = handle->object;

        if (obj < (ManagedObject *)Class::heap_base ||
            obj > (ManagedObject *)Class::heap_end)
        {
            tmn_suspend_enable();
            return JVMTI_ERROR_INVALID_OBJECT;
        }

        Class *clss = obj->vt()->clss;
        ManagedObject *clsObj = struct_Class_to_java_lang_Class(clss);
        if (clsObj->vt()->clss != VM_Global_State::loader_env->JavaLangClass_Class) {
            tmn_suspend_enable();
            return JVMTI_ERROR_INVALID_OBJECT;
        }
        tmn_suspend_enable();
    }

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // TODO: check error condition: JVMTI_ERROR_INVALID_SLOT
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        // TODO: check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // TODO: check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        err = interpreter.interpreter_ti_setObject(env,
            vm_thread, depth, slot, value);
    else
    {
        GET_JIT_FRAME_CONTEXT;

        tmn_suspend_disable();
        ObjectHandle oh;
        if (NULL != value)
        {
            ObjectHandle obj = value;
            oh = oh_allocate_local_handle();
            oh->object = obj->object;
        }

        OpenExeJpdaError result = jit->set_local_var(method, jfc, slot,
            VM_DATA_TYPE_MP, &oh);
        si_free(si);
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiSetLocalInt(jvmtiEnv* env,
                 jthread thread,
                 jint depth,
                 jint slot,
                 jint value)
{
    TRACE2("jvmti.locals", "SetLocalInt called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    // check error condition: JVMTI_ERROR_NULL_POINTER
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, &value);
    if (err != JVMTI_ERROR_NONE)
        return err;

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // check error condition: JVMTI_ERROR_INVALID_SLOT
        // check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        err = interpreter.interpreter_ti_setLocal32(env,
            vm_thread, depth, slot, value);
    else
    {
        GET_JIT_FRAME_CONTEXT;

        tmn_suspend_disable();
        OpenExeJpdaError result = jit->set_local_var(method, jfc, slot,
            VM_DATA_TYPE_INT32, &value);
        si_free(si);
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiSetLocalLong(jvmtiEnv* env,
                  jthread thread,
                  jint depth,
                  jint slot,
                  jlong value)
{
    TRACE2("jvmti.locals", "SetLocalLong called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    CHECK_CAPABILITY(can_access_local_variables);

    // check error condition: JVMTI_ERROR_INVALID_THREAD
    // check error condition: JVMTI_ERROR_THREAD_NOT_ALIVE
    // check error condition: JVMTI_ERROR_ILLEGAL_ARGUMENT
    // check error condition: JVMTI_ERROR_NULL_POINTER
    jvmtiError err = GetLocal_checkArgs(env, &thread, depth, slot, &value);
    if (err != JVMTI_ERROR_NONE)
        return err;

    bool thread_suspended = false;
    // Suspend thread before getting stacks
    VM_thread *vm_thread;
    if (NULL != thread)
    {
        // Check that this thread is not current
        vm_thread = get_vm_thread_ptr_safe(jvmti_test_jenv, thread);
        if (vm_thread != p_TLS_vmthread)
        {
            jthread_suspend(thread);
            thread_suspended = true;
        }
    }
    else
        vm_thread = p_TLS_vmthread;

    if (interpreter_enabled())
        // check error condition: JVMTI_ERROR_INVALID_SLOT
        // check error condition: JVMTI_ERROR_OPAQUE_FRAME
        // check error condition: JVMTI_ERROR_NO_MORE_FRAMES
        // TODO: check error condition: JVMTI_ERROR_TYPE_MISMATCH
        err = interpreter.interpreter_ti_setLocal64(env,
            vm_thread, depth, slot, value);
    else
    {
        GET_JIT_FRAME_CONTEXT;

        tmn_suspend_disable();
        OpenExeJpdaError result = jit->set_local_var(method, jfc, slot,
            VM_DATA_TYPE_INT64, &value);
        si_free(si);
        tmn_suspend_enable();

        err = jvmti_translate_jit_error(result);
    }

    if (thread_suspended)
        jthread_resume(thread);

    return err;
}

jvmtiError JNICALL
jvmtiSetLocalFloat(jvmtiEnv* env,
                   jthread thread,
                   jint depth,
                   jint slot,
                   jfloat value)
{
    TRACE2("jvmti.locals", "SetLocalFloat called");
    SuspendEnabledChecker sec;
    jint v = *(jint*)&value;
    return jvmtiSetLocalInt(env, thread, depth, slot, v);
}

jvmtiError JNICALL
jvmtiSetLocalDouble(jvmtiEnv* env,
                    jthread thread,
                    jint depth,
                    jint slot,
                    jdouble value)
{
    TRACE2("jvmti.locals", "SetLocalDouble called");
    SuspendEnabledChecker sec;
    jlong v = *(jlong*)&value;
    return jvmtiSetLocalLong(env, thread, depth, slot, v);
}
