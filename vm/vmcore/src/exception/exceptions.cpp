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
 * @author Intel, Pavel Afremov
 * @version $Revision: 1.1.2.1.4.4 $
 */

#define LOG_DOMAIN "exn"
#include "clog.h"

#include "heap.h"
#include "classloader.h"
#include "exceptions.h"
#include "exceptions_impl.h"
#include "exceptions_jit.h"
#include "ini.h"
#include "interpreter.h"
#include "jni_utils.h"
#include "m2n.h"
#include "object_handles.h"
#include "vm_arrays.h"
#include "vm_strings.h"


bool exn_raised()
{
    // no need to disable gc for simple null equality check
    return ((NULL != p_TLS_vmthread->thread_exception.exc_object)
        || (NULL != p_TLS_vmthread->thread_exception.exc_class));
}


//FIXME LAZY EXCEPTION (2006.05.06)
//Find all usage and change to lazy use
jthrowable exn_get()
{
    // we can check heap references for equality to NULL
    // without disabling gc, because GC wouldn't change 
    // null to non-null and vice versa.
    if ((NULL == p_TLS_vmthread->thread_exception.exc_object)
        && (NULL == p_TLS_vmthread->thread_exception.exc_class)) {
        return NULL;
    }

    // returned value which will contains jthrowable value of
    // curent thread exception
    jobject exc;

    if (NULL != p_TLS_vmthread->thread_exception.exc_object) {
        tmn_suspend_disable_recursive();
        exc = oh_allocate_local_handle();
        exc->object = (ManagedObject *) p_TLS_vmthread->thread_exception.exc_object;
        tmn_suspend_enable_recursive();
    } else if (NULL != p_TLS_vmthread->thread_exception.exc_class) {
        exc = exn_create((Exception*)&(p_TLS_vmthread->thread_exception));
    } else {
        DIE(("It's impossible internal error in exception handling."));
    }
    return exc;
} // exn_get

Class* exn_get_class() {
    // we can check heap references for equality to NULL
    // without disabling gc, because GC wouldn't change
    // null to non-null and vice versa.
    if ((NULL == p_TLS_vmthread->thread_exception.exc_object)
        && (NULL == p_TLS_vmthread->thread_exception.exc_class)) {
        return NULL;
    }

    Class* result;

    if (NULL != p_TLS_vmthread->thread_exception.exc_object) {
        tmn_suspend_disable_recursive();
        ManagedObject* exn = p_TLS_vmthread->thread_exception.exc_object;
        result = exn->vt()->clss;
        tmn_suspend_enable_recursive();
    } else if (NULL != p_TLS_vmthread->thread_exception.exc_class) {
        result = p_TLS_vmthread->thread_exception.exc_class;
    } else {
        DIE(("It's impossible internal error in exception handling."));
    }
    return result;
}

const char* exn_get_name() {
    Class* exc_class = exn_get_class();

    if (NULL == exc_class) {
        return NULL;
    }

    return class_get_name(exc_class);
}

void exn_clear()
{
    tmn_suspend_disable_recursive();
    clear_exception_internal();
    tmn_suspend_enable_recursive();
}

bool is_unwindable()
{
    M2nFrame* lastFrame = m2n_get_last_frame();
    return !(interpreter_enabled() || (!lastFrame)
         || (m2n_get_frame_type(lastFrame) & FRAME_NON_UNWINDABLE));
}

bool set_unwindable(bool unwindable)
{
    M2nFrame* lastFrame = m2n_get_last_frame();

    if (interpreter_enabled() || (!lastFrame)) {
        assert(!unwindable);
        return false;
    }

    int lastFrameType = m2n_get_frame_type(lastFrame);
    bool previousValue = !(lastFrameType & FRAME_NON_UNWINDABLE);

    if (unwindable) {
        lastFrameType &= ~FRAME_NON_UNWINDABLE;
    } else {
        lastFrameType |= FRAME_NON_UNWINDABLE;
    }
    m2n_set_frame_type( lastFrame, (frame_type) lastFrameType);
    return previousValue;
}

jthrowable exn_create(Exception* exception) {
    return create_exception(exception);
}

jthrowable exn_create(Class* exc_class)
{
    return exn_create(exc_class, NULL , NULL);
}

jthrowable exn_create(Class* exc_class, jthrowable exc_cause)
{
    return exn_create(exc_class, NULL, exc_cause);
}

jthrowable exn_create(Class* exc_class, const char* exc_message)
{
    return exn_create(exc_class, exc_message , NULL);
}

jthrowable exn_create(Class* exc_class, const char* exc_message, jthrowable exc_cause)
{
    ASSERT_RAISE_AREA;
    jthrowable exc_object = create_exception(exc_class, exc_message, exc_cause);

    if (exc_object == NULL) {
        exc_object = create_exception(exc_class, exc_message , NULL);

        if (exc_object == NULL) {
            return NULL;
        }
        init_cause(exc_object, exc_cause);
    }
    return exc_object;
}

jthrowable exn_create(const char* exc_name)
{
    return exn_create(exc_name, NULL, NULL);
}

jthrowable exn_create(const char* exc_name, jthrowable exc_cause)
{
    return exn_create(exc_name, NULL, exc_cause);
}   // exn_create(const char* exc_name, jthrowable cause)

jthrowable exn_create(const char* exc_name,const char *exc_message)
{
    return exn_create(exc_name, exc_message, NULL);
} // exn_create(const char *exception_name, const char *exc_message)

jthrowable exn_create(const char *exc_name, const char *exc_message, jthrowable cause)
{
    ASSERT_RAISE_AREA;
    Class *exc_class = get_exc_class(exc_name);

    if (exc_class == NULL) {
        assert(exn_raised());
        return NULL;
    }
    return exn_create(exc_class, exc_message, cause);
}   // exn_create

void exn_throw_object(jthrowable exc_object) {
    assert(!hythread_is_suspend_enabled());
    assert(is_unwindable());
    // XXX salikh: change to unconditional thread_disable_suspend()
    // we use conditional until all of the VM
    // is refactored to be definitely gc-safe.

    exn_throw_object_internal(exc_object);
}

void exn_throw_by_class(Class* exc_class)
{
    exn_throw_by_class(exc_class, NULL, NULL);
}

void exn_throw_by_class(Class* exc_class, jthrowable exc_cause)
{
    exn_throw_by_class(exc_class, NULL, exc_cause);
}

void exn_throw_by_class(Class* exc_class, const char* exc_message)
{
    exn_throw_by_class(exc_class, exc_message, NULL);
}

void exn_throw_by_class(Class* exc_class, const char* exc_message,
    jthrowable exc_cause)
{
    assert(!hythread_is_suspend_enabled());
    assert(is_unwindable());

    exn_throw_by_class_internal(exc_class, exc_message, exc_cause);
}

void exn_throw_by_name(const char* exc_name)
{
    exn_throw_by_name(exc_name, NULL, NULL);
}

void exn_throw_by_name(const char* exc_name, jthrowable exc_cause)
{
    exn_throw_by_name(exc_name, NULL, exc_cause);
}

void exn_throw_by_name(const char* exc_name, const char* exc_message)
{
    exn_throw_by_name(exc_name, exc_message, NULL);
}

void exn_throw_by_name(const char* exc_name, const char* exc_message,
    jthrowable exc_cause)
{
    assert(!hythread_is_suspend_enabled());
    assert(is_unwindable());

    exn_throw_by_name_internal(exc_name, exc_message, exc_cause);
}

void exn_raise_object(jthrowable exc_object)
{
    assert(!is_unwindable());
    assert(exc_object);
    exn_raise_object_internal(exc_object);
}

void exn_raise_by_class(Class* exc_class)
{
    exn_raise_by_class(exc_class, NULL, NULL);
}

void exn_raise_by_class(Class* exc_class, jthrowable exc_cause)
{
    exn_raise_by_class(exc_class, NULL, exc_cause);
}

void exn_raise_by_class(Class* exc_class, const char* exc_message)
{
    exn_raise_by_class(exc_class, exc_message, NULL);
}

void exn_raise_by_class(Class* exc_class, const char* exc_message,
    jthrowable exc_cause)
{
    assert(!is_unwindable());
    assert(exc_class);
    exn_raise_by_class_internal(exc_class, exc_message, exc_cause);
}

void exn_raise_by_name(const char* exc_name)
{
    exn_raise_by_name(exc_name, NULL, NULL);
}

void exn_raise_by_name(const char* exc_name, jthrowable exc_cause)
{
    exn_raise_by_name(exc_name, NULL, exc_cause);
}

void exn_raise_by_name(const char* exc_name, const char* exc_message)
{
    exn_raise_by_name(exc_name, exc_message, NULL);
}

void exn_raise_by_name(const char* exc_name, const char* exc_message,
    jthrowable exc_cause)
{
    assert(!is_unwindable());
    assert(exc_name);
    exn_raise_by_name_internal(exc_name, exc_message, exc_cause);
}

static void check_pop_frame(ManagedObject *exn) {
    if (exn == VM_Global_State::loader_env->popFrameException->object) {
        exn_clear();
        frame_type type = m2n_get_frame_type(m2n_get_last_frame());

        if (FRAME_POP_NOW == (FRAME_POP_MASK & type)) {
            jvmti_jit_do_pop_frame();
        }
    }
}

void exn_rethrow()
{
#ifndef VM_LAZY_EXCEPTION
    ManagedObject *exn = get_exception_object_internal();
    assert(exn);
    clear_exception_internal();

    check_pop_frame(exn);

    exn_throw_for_JIT(exn, NULL, NULL, NULL, NULL);
#else
    if (NULL != p_TLS_vmthread->thread_exception.exc_object) {
        ManagedObject* exn_mng_object = p_TLS_vmthread->thread_exception.exc_object;
        clear_exception_internal();

        check_pop_frame(exn_mng_object);

        exn_throw_for_JIT(exn_mng_object, NULL, NULL, NULL, NULL);
    } else if (NULL != p_TLS_vmthread->thread_exception.exc_class) {
        Class * exc_class = p_TLS_vmthread->thread_exception.exc_class;
        const char* exc_message = p_TLS_vmthread->thread_exception.exc_message;
        jthrowable exc_cause = oh_allocate_local_handle();
        exc_cause->object = p_TLS_vmthread->thread_exception.exc_cause;
        clear_exception_internal();
        exn_throw_by_class_internal(exc_class, exc_message, exc_cause);
    } else {
        DIE(("There is no exception."));
    }
#endif
    DIE(("It's Unreachable place."));
}   //exn_rethrow

void exn_rethrow_if_pending()
{
    if (exn_raised()) {
        exn_rethrow();
    }
}   //exn_rethrow_if_pending

//////////////////////////////////////////////////////////////////////////
// Java Stack Trace Utilities
#define STF_AS_JLONG 5

// prints stackTrace via java
inline void exn_java_print_stack_trace(FILE * UNREF f, jthrowable exc)
{
    // finds java environment
    JNIEnv *jenv = jni_native_intf;

    // finds class of Throwable
    jclass throwableClazz = FindClass(jenv, VM_Global_State::loader_env->JavaLangThrowable_String);

    // tries to print stackTrace via java
    jmethodID printStackTraceID =
        GetMethodID(jenv, throwableClazz, "printStackTrace", "()V");
    CallVoidMethod(jenv, exc, printStackTraceID);
}

// prints stackTrace via jni
inline void exn_jni_print_stack_trace(FILE * f, jthrowable exc)
{
    assert(hythread_is_suspend_enabled());
    // finds java environment
    JNIEnv *jenv = jni_native_intf;

    // finds class of Throwable
    jclass throwableClazz = FindClass(jenv, VM_Global_State::loader_env->JavaLangThrowable_String);

    // print exception message
    if (ExceptionCheck(jenv))
        return;

    jmethodID getMessageId = GetMethodID(jenv, throwableClazz, "getMessage",
        "()Ljava/lang/String;");
    jstring message = CallObjectMethod(jenv, exc, getMessageId);

    if (ExceptionCheck(jenv))
        return;

    tmn_suspend_disable();
    const char *exceptionNameChars = exc->object->vt()->clss->name->bytes;
    tmn_suspend_enable();
    const char *messageChars = GetStringUTFChars(jenv, message, false);
    fprintf(f, "\n%s : %s\n", exceptionNameChars, messageChars);

    // gets stack trace to print it
    jmethodID getStackTraceID =
        GetMethodID(jenv, throwableClazz, "getStackTrace",
        "()[Ljava/lang/StackTraceElement;");
    jobjectArray stackTrace = CallObjectMethod(jenv, exc, getStackTraceID);

    if (ExceptionCheck(jenv) || !stackTrace)
        return;
    int stackTraceLenth = GetArrayLength(jenv, stackTrace);

    // finds all required JNI IDs from StackTraceElement to avoid finding in cycle
    jclass stackTraceElementClazz = struct_Class_to_java_lang_Class_Handle(
        VM_Global_State::loader_env->java_lang_StackTraceElement_Class);
    jmethodID getClassNameId =
        GetMethodID(jenv, stackTraceElementClazz, "getClassName",
        "()Ljava/lang/String;");
    jmethodID getMethodNameId =
        GetMethodID(jenv, stackTraceElementClazz, "getMethodName",
        "()Ljava/lang/String;");
    jmethodID getFileNameId =
        GetMethodID(jenv, stackTraceElementClazz, "getFileName",
        "()Ljava/lang/String;");
    jmethodID getLineNumberId =
        GetMethodID(jenv, stackTraceElementClazz, "getLineNumber", "()I");
    jmethodID isNativeMethodId =
        GetMethodID(jenv, stackTraceElementClazz, "isNativeMethod", "()Z");

    // prints stack trace line by line
    // Afremov Pavel 20050120 it's necessary to skip some line sof stack trace ganaration
    for (int itemIndex = 0; itemIndex < stackTraceLenth; itemIndex++) {
        // gets stack trace element (one line in stack trace)
        jobject stackTraceElement =
            GetObjectArrayElement(jenv, stackTrace, itemIndex);

        // prints begin of stack trace line
        fprintf(f, " at ");

        // gets and prints information about class and method
        jstring className =
            CallObjectMethod(jenv, stackTraceElement, getClassNameId);

        if (ExceptionCheck(jenv))
            return;
        jstring methodName =
            CallObjectMethod(jenv, stackTraceElement, getMethodNameId);

        if (ExceptionCheck(jenv))
            return;
        const char *classNameChars =
            GetStringUTFChars(jenv, className, false);
        fprintf(f, "%s.", classNameChars);
        const char *methodNameChars =
            GetStringUTFChars(jenv, methodName, false);
        fprintf(f, "%s", methodNameChars);

        // gets information about java file name
        jstring fileName =
            CallObjectMethod(jenv, stackTraceElement, getFileNameId);

        if (ExceptionCheck(jenv))
            return;

        // if it's known source ...
        if (fileName) {
            // gets line number and prints it after file name
            const char *fileNameChars =
                GetStringUTFChars(jenv, fileName, false);
            jint sourceLineNumber =
                CallIntMethod(jenv, stackTraceElement, getLineNumberId);
            if (ExceptionCheck(jenv))
                return;
            fprintf(f, " (%s:", fileNameChars);
            fprintf(f, " %d)", sourceLineNumber);
        }
        // if it's unknown source
        else {
            jboolean isNative =
                CallBooleanMethod(jenv, stackTraceElement, isNativeMethodId);
            if (ExceptionCheck(jenv))
                return;

            // if it's native
            if (isNative) {
                fprintf(f, " (Native Method)");
            }
            // or not
            else {
                fprintf(f, " (Unknown Source)");
            }
        }

        // prints end of stack trace line
        fprintf(f, "\n");
    }

    // gets caused exception
    jmethodID getCauseId =
        GetMethodID(jenv, throwableClazz, "getCause",
        "()Ljava/lang/Throwable;");
    jobject causedExc = CallObjectMethod(jenv, exc, getCauseId);
    if (ExceptionCheck(jenv))
        return;

    if (causedExc) {
        // if there is caused exception ...
        tmn_suspend_disable();  // -----------------vvv
        bool same_exception = false;
        if (causedExc->object == exc->object)
            same_exception = true;
        tmn_suspend_enable();   // ------------------^^^
        if (!same_exception) {
            // tries to print it
            fprintf(f, "caused by\n");
            exn_jni_print_stack_trace(f, causedExc);
        }
    }

    assert(hythread_is_suspend_enabled());
}

inline void exn_native_print_stack_trace(FILE * f, ManagedObject * exn)
{
//Afremov Pavel 20050119 Should be changed when classpath will raplaced by DRL
    assert(gid_throwable_traceinfo);

    // ? 20030428: This code should be elsewhere!
    unsigned field_offset = ((Field *) gid_throwable_traceinfo)->get_offset();
    ManagedObject **field_addr =
        (ManagedObject **) ((Byte *) exn + field_offset);
    Vector_Handle stack_trace =
        (Vector_Handle) get_raw_reference_pointer(field_addr);
    if (stack_trace) {
#if defined (__INTEL_COMPILER)
#pragma warning( push )
#pragma warning (disable:1683)  // to get rid of remark #1683: explicit conversion of a 64-bit integral type to a smaller integral type
#endif
        unsigned depth =
            (unsigned) *get_vector_element_address_int64(stack_trace, 0);

#if defined (__INTEL_COMPILER)
#pragma warning( pop )
#endif

        StackTraceFrame *stf =
            (StackTraceFrame *) get_vector_element_address_int64(stack_trace,
            1);

        ExpandableMemBlock buf;
        for (unsigned i = 0; i < depth; i++)
            st_print_frame(&buf, stf + i);
        fprintf(f, "%s", buf.toString());
    }
}

// prints stack trace using 3 ways: via java, via jni, and native
void exn_print_stack_trace(FILE * f, jthrowable exc)
{
    assert(hythread_is_suspend_enabled());
    // saves curent thread exception and clear to allow java to work
    jthrowable cte = exn_get();
    /*
    Afremov Pavel 20050120
    FIXME:Don't work under JIT, Fix requred
    // 1 way -> tries to print stacktrace via java
    exn_java_print_stack_trace(f, exn);

    // if everything ok ...
    if (!exn_raised())
    {
        // restores curent thread exception and returns
        exn_raise_object(cte);
        return;
    }
    */
    // clears exception to allow java to work
    exn_clear();

    // 2 way -> tries to print using jni access to class method
    exn_jni_print_stack_trace(f, exc);

    // if everything OK ...
    if (!exn_raised())
    {
        // restores curent thread exception and returns
        exn_raise_object(cte);
        return;
    }

    // 3 way -> last
    tmn_suspend_disable();

    ManagedObject *exn = exc->object;
    exn_native_print_stack_trace(f, exn);

    tmn_suspend_enable();

    fflush(f);

    // restore curent thread exception
    exn_raise_object(cte);

}


//////////////////////////////////////////////////////////////////////////
// Uncaught Exceptions

void print_uncaught_exception_message(FILE * f, char *context_message,
    jthrowable exc)
{
    assert(hythread_is_suspend_enabled());

    tmn_suspend_disable();
    fprintf(f, "** During %s uncaught exception: %s\n", context_message,
        exc->object->vt()->clss->name->bytes);
    tmn_suspend_enable();

    exn_print_stack_trace(f, exc);
}
