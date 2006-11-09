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
 * @author Pavel Pervov
 * @version $Revision: 1.1.2.3.4.4 $
 */  

#define LOG_DOMAIN "classloader"
#include "cxxlog.h"

#include "Class.h"
#include "open/jthread.h"
#include "open/gc.h"
#include "exceptions.h"
#include "thread_manager.h"
#include "vm_strings.h"
#include "classloader.h"
#include "ini.h"
#include "vm_threads.h"

// Initializes a class.

void Class::initialize()
{
    ASSERT_RAISE_AREA;
    assert(!exn_raised());
    assert(!hythread_is_suspend_enabled());

    // the following code implements the 11-step class initialization program
    // described in page 226, section 12.4.2 of Java Language Spec, 1996
    // ISBN 0-201-63451-1

    TRACE2("class.init", "initializing class " << m_name->bytes);

    // ---  step 1   ----------------------------------------------------------

    assert(!hythread_is_suspend_enabled());
    jobject jlc = struct_Class_to_java_lang_Class_Handle(this);
    jthread_monitor_enter(jlc);

    // ---  step 2   ----------------------------------------------------------
    TRACE2("class.init", "initializing class " << m_name->bytes << " STEP 2" );

    while(m_initializing_thread != p_TLS_vmthread && is_initializing()) {
        jthread_monitor_wait(jlc);
        if(exn_raised()) {
            jthread_monitor_exit(jlc);
            return;
        }
    }

    // ---  step 3   ----------------------------------------------------------
    if(m_initializing_thread == p_TLS_vmthread) {
        jthread_monitor_exit(jlc);
        return;
    }

    // ---  step 4   ----------------------------------------------------------
    if(is_initialized()) {
        jthread_monitor_exit(jlc);
        return;
    }

    // ---  step 5   ----------------------------------------------------------
    if(in_error()) {
        jthread_monitor_exit(jlc);
        tmn_suspend_enable();
        exn_raise_by_name("java/lang/NoClassDefFoundError", m_name->bytes);
        tmn_suspend_disable();
        return;
    }

    // ---  step 6   ----------------------------------------------------------
    TRACE2("class.init", "initializing class " << m_name->bytes << "STEP 6" );

    assert(m_state == ST_ConstraintsVerified);
    m_state = ST_Initializing;
    assert(m_initializing_thread == 0);
    m_initializing_thread = p_TLS_vmthread;
    jthread_monitor_exit(jlc);

    // ---  step 7 ------------------------------------------------------------

    if(has_super_class()) {
        class_initialize_ex(get_super_class());

        if(get_super_class()->in_error()) { 
            jthread_monitor_enter(jlc);
            tmn_suspend_enable();
            REPORT_FAILED_CLASS_CLASS_EXN(m_class_loader, this,
                get_super_class()->get_error_cause());
            tmn_suspend_disable();
            m_initializing_thread = NULL;
            m_state = ST_Error;
            assert(!hythread_is_suspend_enabled());
            jthread_monitor_notify_all(jlc);
            jthread_monitor_exit(jlc);
            return;
        }
    }

    // ---  step 8   ----------------------------------------------------------

    Method* meth = m_static_initializer;
    if(meth == NULL) {
        jthread_monitor_enter(jlc);
        m_state = ST_Initialized;
        TRACE2("classloader", "class " << m_name->bytes << " initialized");
        m_initializing_thread = NULL;
        assert(!hythread_is_suspend_enabled());
        jthread_monitor_notify_all(jlc);
        jthread_monitor_exit(jlc);
        return;
    }

    TRACE2("class.init", "initializing class " << m_name->bytes << " STEP 8" );
    jthrowable p_error_object;

    assert(!hythread_is_suspend_enabled());
    vm_execute_java_method_array((jmethodID) meth, 0, 0);
    p_error_object = exn_get();

    // ---  step 9   ----------------------------------------------------------
    TRACE2("class.init", "initializing class " << m_name->bytes << " STEP 9" ); 

    if(!p_error_object) {
        jthread_monitor_enter(jlc);
        m_state = ST_Initialized;
        TRACE2("classloader", "class " << m_name->bytes << " initialized");
        m_initializing_thread = NULL;
        assert(m_error == NULL);
        assert(!hythread_is_suspend_enabled());
        jthread_monitor_notify_all(jlc);
        jthread_monitor_exit(jlc);
        return;
    }

    // ---  step 10  ----------------------------------------------------------

    if(p_error_object) {
        assert(!hythread_is_suspend_enabled());
        exn_clear();
        Class* p_error_class = p_error_object->object->vt()->clss;
        Class* jle = VM_Global_State::loader_env->java_lang_Error_Class;
        while(p_error_class && p_error_class != jle) {
            p_error_class = p_error_class->get_super_class();
        }
        assert(!hythread_is_suspend_enabled());
        if((!p_error_class) || (p_error_class != jle) ) {
#ifdef _DEBUG_REMOVED
            Class* eiie = VM_Global_State::loader_env->java_lang_ExceptionInInitializerError_Class;
            assert(eiie);
#endif
            tmn_suspend_enable();

            p_error_object = exn_create("java/lang/ExceptionInInitializerError",
                p_error_object);
            tmn_suspend_disable();
        }
        tmn_suspend_enable();
        set_error_cause(p_error_object);
        tmn_suspend_disable();

        // ---  step 11  ----------------------------------------------------------
        assert(!hythread_is_suspend_enabled());
        jthread_monitor_enter(jlc);
        m_state = ST_Error;
        m_initializing_thread = NULL;
        assert(!hythread_is_suspend_enabled());
        jthread_monitor_notify_all(jlc);
        jthread_monitor_exit(jlc);
        exn_raise_object(p_error_object);
    }
    // end of 11 step class initialization program
} //class_initialize1


void class_initialize_from_jni(Class *clss)
{
    ASSERT_RAISE_AREA;
    assert(hythread_is_suspend_enabled());

    // check verifier constraints
    if(!clss->verify_constraints(VM_Global_State::loader_env)) {
        if (!exn_raised()) {
            tmn_suspend_disable();
            exn_raise_object(class_get_error(clss->get_class_loader(), clss->get_name()->bytes));
            tmn_suspend_enable();
        }
        return;
    }

    tmn_suspend_disable();
    if (class_needs_initialization(clss)) {
        clss->initialize();
    }
    tmn_suspend_enable();
} // class_initialize_from_jni


void class_initialize(Class *clss)
{
    ASSERT_RAISE_AREA;
    class_initialize_ex(clss);
}

void class_initialize_ex(Class *clss)
{
    ASSERT_RAISE_AREA;
    assert(!hythread_is_suspend_enabled());

    // check verifier constraints
    tmn_suspend_enable();
    if(!clss->verify_constraints(VM_Global_State::loader_env)) {
        if (!exn_raised()) {
            tmn_suspend_disable();
            exn_raise_object(class_get_error(clss->get_class_loader(), clss->get_name()->bytes));
        }
        return;
    }
    tmn_suspend_disable();
    
    if(class_needs_initialization(clss)) {
        clss->initialize();
    }
} // class_initialize_ex
