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
 * @author Gregory Shimansky
 * @version $Revision: 1.1.2.1.4.4 $
 */  
/*
 * JVMTI object API
 */

#include "jvmti_direct.h"
#include "jvmti_utils.h"
#include "cxxlog.h"
#include "object_handles.h"
#include "object_generic.h"
#include "mon_enter_exit.h"

#include "thread_manager.h"
#include "suspend_checker.h"
#include "open/vm.h"
#include "vm_arrays.h"

/*
 * Get Object Size
 *
 * For the object indicated by object, return via size_ptr the
 * size of the object.
 *
 * REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiGetObjectSize(jvmtiEnv* env,
                   jobject object,
                   jlong* size_ptr)
{
    TRACE2("jvmti.object", "GetObjectSize called");
    SuspendEnabledChecker sec;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (NULL == object)
        return JVMTI_ERROR_INVALID_OBJECT;
    if (NULL == size_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    tmn_suspend_disable();       //---------------------------------v
    ObjectHandle h = (ObjectHandle)object;
    ManagedObject *mo = h->object;

    // Check that reference pointer points to the heap
    if (mo < (ManagedObject *)VM_Global_State::loader_env->heap_base ||
        mo > (ManagedObject *)VM_Global_State::loader_env->heap_end)
    {
        tmn_suspend_enable();
        return JVMTI_ERROR_INVALID_OBJECT;
    }

    Class *object_clss = mo->vt()->clss;
    if(object_clss->is_array())
        *size_ptr = object_clss->calculate_array_size(get_vector_length((Vector_Handle)mo));
    else
        *size_ptr = class_get_boxed_data_size(object_clss);

    tmn_suspend_enable();
    return JVMTI_ERROR_NONE;
}

/*
 * Get Object Hash Code
 *
 * For the object indicated by object, return via hash_code_ptr
 * a hash code.
 *
 * REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiGetObjectHashCode(jvmtiEnv* env,
                       jobject object,
                       jint* hash_code_ptr)
{
    TRACE2("jvmti.object", "GetObjectHashCode called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (NULL == object)
        return JVMTI_ERROR_INVALID_OBJECT;
    if (NULL == hash_code_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    tmn_suspend_disable();
    ManagedObject* p_obj;
    if (object != NULL)
        p_obj = ((ObjectHandle)object)->object;
    else
        p_obj = NULL;
    *hash_code_ptr = generic_hashcode(p_obj);
    TRACE2("jvmti-object", "Object " <<
        ((NULL == p_obj) ? "is null" : p_obj->vt()->clss->get_name()->bytes) <<
        " hash code = " << *hash_code_ptr);
    tmn_suspend_enable();

    return JVMTI_ERROR_NONE;
}

/*
 * Get Object Monitor Usage
 *
 * Get information about the object's monitor. The fields of the
 * jvmtiMonitorUsage structure are filled in with information about
 * usage of the monitor.
 *
 * OPTIONAL Functionality.
 */
jvmtiError JNICALL
jvmtiGetObjectMonitorUsage(jvmtiEnv* env,
                           jobject object,
                           jvmtiMonitorUsage* info_ptr)
{
    TRACE2("jvmti.object", "GetObjectMonitorUsage called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};
    CHECK_EVERYTHING();
    CHECK_CAPABILITY(can_get_monitor_info);

    if (NULL == object)
        return JVMTI_ERROR_INVALID_OBJECT;
    if (NULL == info_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    int enter_wait_count = 0;
    int notify_wait_count = 0;
    jthread_iterator_t  iterator = jthread_iterator_create();
    jthread thread = jthread_iterator_next(&iterator);
    jobject monitor = NULL;
    assert(thread);
    while (thread)
    {
        jthread_get_contended_monitor(thread, &monitor);
        if (monitor && monitor->object == object->object)
            enter_wait_count++;
        jthread_get_wait_monitor(thread, &monitor);
        if (monitor && monitor->object == object->object)
            notify_wait_count++;
        
        thread = jthread_iterator_next(&iterator);
    }

    jthread* enter_wait_array = NULL;
 	if (enter_wait_count > 0){
		jvmtiError jvmti_error = _allocate(sizeof(jthread*) *
				enter_wait_count, (unsigned char **)&enter_wait_array);
		if (JVMTI_ERROR_NONE != jvmti_error)
		{
			jthread_iterator_release(&iterator);
			return jvmti_error;
		}
	}

    jthread* notify_wait_array = NULL;
	if (notify_wait_count > 0){
		jvmtiError jvmti_error = _allocate(sizeof(jthread*) *
				notify_wait_count, (unsigned char **)&notify_wait_array);
		if (JVMTI_ERROR_NONE != jvmti_error)
		{
			jthread_iterator_release(&iterator);
			return jvmti_error;
		}
	}

    int ii = 0, jj = 0;
    info_ptr->owner = NULL;

    jthread_iterator_reset(&iterator);
    thread = jthread_iterator_next(&iterator);
    while (thread)
    {
        jthread_get_contended_monitor(thread, &monitor);
        if (monitor && monitor->object == object->object)
            enter_wait_array[ii++] = thread;
        
        jthread_get_wait_monitor(thread, &monitor);
        if (monitor && monitor->object == object->object)
            notify_wait_array[jj++] = thread;
        
        thread = jthread_iterator_next(&iterator);
    }
    jthread_iterator_release(&iterator);

    jthread_get_lock_owner(object, &info_ptr->owner);
    info_ptr->entry_count = jthread_get_lock_recursion(object, info_ptr->owner);
    info_ptr->waiter_count = enter_wait_count;
    info_ptr->waiters = enter_wait_array;
    info_ptr->notify_waiter_count = notify_wait_count;
    info_ptr->notify_waiters = notify_wait_array;

    return JVMTI_ERROR_NONE;
}

