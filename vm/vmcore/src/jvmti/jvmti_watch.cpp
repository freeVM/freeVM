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
 * @version $Revision: 1.1.2.1.4.4 $
 */
/*
 * JVMTI watchpoints API
 */

#define LOG_DOMAIN "jvmti.watch"

//#include "environment.h"
//#include "jvmti_direct.h"
//#include "jvmti_utils.h"
//#include "jvmti_internal.h"
//#include "cxxlog.h"
//#include "suspend_checker.h"

#include <string.h>
#include "Class.h"
#include "vm_strings.h"
#include "jvmti_direct.h"
#include "Class.h"
#include "object_handles.h"
#include "jvmti_utils.h"
#include "cxxlog.h"

#include "jvmti_interface.h"
#include "suspend_checker.h"
#include "jvmti_internal.h"
#include "environment.h"


enum Watch_Type
{
    ACCESS,
    MODIFICATION
};

static jvmtiError set_field_watch(jvmtiEnv* env, jclass klass, jfieldID field, Watch_Type watch_type)
{
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    switch (watch_type)
    {
    case ACCESS:
        CHECK_CAPABILITY(can_generate_field_access_events);
        break;

    case MODIFICATION:
        CHECK_CAPABILITY(can_generate_field_modification_events);
        break;
    }

    if (! is_valid_class_object(klass))
        return JVMTI_ERROR_INVALID_CLASS;

    if (! field)
        return JVMTI_ERROR_INVALID_FIELDID;

    TIEnv *p_env = (TIEnv *)env;
    DebugUtilsTI *ti = p_env->vm->vm_env->TI;

    Watch** p_watch_list;
    switch (watch_type)
    {
    case ACCESS:
        p_watch_list = ti->get_access_watch_list();
        break;

    case MODIFICATION:
        p_watch_list = ti->get_modification_watch_list();
        break;
    }

    // Find watch for this field if it exists already
    Watch *w = ti->find_watch(p_watch_list, field);

    jvmtiError errorCode;

    if (NULL == w)
    {
        errorCode = _allocate(sizeof(Watch), (unsigned char **)&w);
        if (JVMTI_ERROR_NONE != errorCode)
            return errorCode;

        TIEnvList *el;
        errorCode = _allocate(sizeof(TIEnvList), (unsigned char **)&el);
        if (JVMTI_ERROR_NONE != errorCode)
        {
            _deallocate((unsigned char *)w);
            return errorCode;
        }

        w->field = field;
        w->next = NULL;
        w->envs = NULL;

        el->env = p_env;
        w->add_env(el);
        ti->add_watch(p_watch_list, w);

        // enable field tracking
        switch (watch_type)
        {
        case ACCESS:
            ((Field*) field)->set_track_access(true);
            break;

        case MODIFICATION:
            ((Field*) field)->set_track_modification(true);
            break;
        }
    }
    else
    {
        if (NULL != w->find_env(p_env))
            return JVMTI_ERROR_DUPLICATE;

        TIEnvList *el;
        errorCode = _allocate(sizeof(TIEnvList), (unsigned char **)&el);
        if (JVMTI_ERROR_NONE != errorCode)
            return errorCode;

        el->env = p_env;
        w->add_env(el);
    }

    switch (watch_type)
    {
    case ACCESS:
        TRACE("SetFieldAccessWatch successfull");
        break;

    case MODIFICATION:
        TRACE("SetFieldModificationWatch successfull");
        break;
    }

    return JVMTI_ERROR_NONE;
} // set_field_watch

static jvmtiError clear_field_watch(jvmtiEnv* env, jclass klass, jfieldID field, Watch_Type watch_type)
{
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    switch (watch_type)
    {
    case ACCESS:
        CHECK_CAPABILITY(can_generate_field_access_events);
        break;

    case MODIFICATION:
        CHECK_CAPABILITY(can_generate_field_modification_events);
        break;
    }

    if (! is_valid_class_object(klass))
        return JVMTI_ERROR_INVALID_CLASS;

    if (! field)
        return JVMTI_ERROR_INVALID_FIELDID;

    TIEnv *p_env = (TIEnv *)env;
    DebugUtilsTI *ti = p_env->vm->vm_env->TI;

    Watch** p_watch_list;
    switch (watch_type)
    {
    case ACCESS:
        p_watch_list = ti->get_access_watch_list();
        break;

    case MODIFICATION:
        p_watch_list = ti->get_modification_watch_list();
        break;
    }

    // Find watch for this field if it exists already
    Watch *w = ti->find_watch(p_watch_list, field);

    if (NULL == w)
        return JVMTI_ERROR_NOT_FOUND;

    TIEnvList *el = w->find_env(p_env);
    if (NULL == el)
        return JVMTI_ERROR_NOT_FOUND;

    w->remove_env(el);
    if (NULL == w->envs)
    {
        // disable field tracking
        switch (watch_type)
        {
        case ACCESS:
            ((Field*) field)->set_track_access(false);
            break;

        case MODIFICATION:
            ((Field*) field)->set_track_modification(false);
            break;
        }

        ti->remove_watch(p_watch_list, w);
    }

    return JVMTI_ERROR_NONE;
} // clear_field_watch

/*
 * Set Field Access Watch
 *
 * Generate a FieldAccess event when the field specified by klass
 * and field is about to be accessed.
 *
 * OPTIONAL Functionality
 */
jvmtiError JNICALL
jvmtiSetFieldAccessWatch(jvmtiEnv* env,
                         jclass klass,
                         jfieldID field)
{
    TRACE("SetFieldAccessWatch called");

    return set_field_watch(env, klass, field, ACCESS);
}

/*
 * Clear Field Access Watch
 *
 * Cancel a field access watch previously set by SetFieldAccessWatch,
 * on the field specified by klass and field.
 *
 * OPTIONAL Functionality
 */
jvmtiError JNICALL
jvmtiClearFieldAccessWatch(jvmtiEnv* env,
                           jclass klass,
                           jfieldID field)
{
    TRACE("ClearFieldAccessWatch called");

    return clear_field_watch(env, klass, field, ACCESS);
}

/*
 * Set Field Modification Watch
 *
 * Generate a FieldModification event when the field specified
 * by klass and field is about to be modified.
 *
 * OPTIONAL Functionality
 */
jvmtiError JNICALL
jvmtiSetFieldModificationWatch(jvmtiEnv* env,
                               jclass klass,
                               jfieldID field)
{
    TRACE("SetFieldModificationWatch called");

    return set_field_watch(env, klass, field, MODIFICATION);
}

/*
 * Clear Field Modification Watch
 *
 * Cancel a field modification watch previously set by
 * SetFieldModificationWatch, on the field specified by klass and
 * field.
 *
 * OPTIONAL Functionality
 */
jvmtiError JNICALL
jvmtiClearFieldModificationWatch(jvmtiEnv* env,
                                 jclass klass,
                                 jfieldID field)
{
    TRACE("ClearFieldModificationWatch called");

    return clear_field_watch(env, klass, field, MODIFICATION);
}

void jvmti_field_access_callback(Field_Handle field,
                                       Method_Handle method,
                                       jlocation location,
                                       jobject* object)
{
    tmn_suspend_enable();

    jvmti_process_field_access_event(field, (jmethodID) method, location,
             object);

    tmn_suspend_disable();
}

void jvmti_field_modification_callback(Field_Handle field,
                                       Method_Handle method,
                                       jlocation location,
                                       jobject* object,
                                       jvalue* new_value)
{
    tmn_suspend_enable();

    jvmti_process_field_modification_event(field, (jmethodID) method, location,
            object, *new_value);

    tmn_suspend_disable();
}
