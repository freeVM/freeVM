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
 * JVMTI capability API
 */

#include "jvmti_direct.h"
#include "jvmti_utils.h"
#include "cxxlog.h"
#include "suspend_checker.h"
#include "environment.h"
#include "interpreter_exports.h"

static const jvmtiCapabilities jvmti_supported_interpreter_capabilities =
{
    0, // can_tag_objects
    0, // can_generate_field_modification_events
    0, // can_generate_field_access_events
    1, // can_get_bytecodes
    1, // can_get_synthetic_attribute
    1, // can_get_owned_monitor_info
    1, // can_get_current_contended_monitor
    1, // can_get_monitor_info
    1, // can_pop_frame
    0, // can_redefine_classes
    0, // can_signal_thread
    1, // can_get_source_file_name
    1, // can_get_line_numbers
    1, // can_get_source_debug_extension
    1, // can_access_local_variables
    0, // can_maintain_original_method_order
    1, // can_generate_single_step_events
    1, // can_generate_exception_events
    1, // can_generate_frame_pop_events
    1, // can_generate_breakpoint_events
    1, // can_suspend
    0, // can_redefine_any_class
    0, // can_get_current_thread_cpu_time
    0, // can_get_thread_cpu_time
    1, // can_generate_method_entry_events
    1, // can_generate_method_exit_events
    1, // can_generate_all_class_hook_events
    1, // can_generate_compiled_method_load_events
    0, // can_generate_monitor_events
    0, // can_generate_vm_object_alloc_events
    0, // can_generate_native_method_bind_events
    0, // can_generate_garbage_collection_events
    0  // can_generate_object_free_events
};

static const jvmtiCapabilities jvmti_supported_jit_capabilities =
{
    0, // can_tag_objects
    0, // can_generate_field_modification_events
    0, // can_generate_field_access_events
    1, // can_get_bytecodes
    1, // can_get_synthetic_attribute
    1, // can_get_owned_monitor_info
    1, // can_get_current_contended_monitor
    1, // can_get_monitor_info
    0, // can_pop_frame
    0, // can_redefine_classes
    0, // can_signal_thread
    1, // can_get_source_file_name
    1, // can_get_line_numbers
    1, // can_get_source_debug_extension
    1, // can_access_local_variables
    0, // can_maintain_original_method_order
    0, // can_generate_single_step_events
    1, // can_generate_exception_events
    1, // can_generate_frame_pop_events
    0, // can_generate_breakpoint_events
    1, // can_suspend
    0, // can_redefine_any_class
    0, // can_get_current_thread_cpu_time
    0, // can_get_thread_cpu_time
    0, // can_generate_method_entry_events
    0, // can_generate_method_exit_events
    1, // can_generate_all_class_hook_events
    1, // can_generate_compiled_method_load_events
    0, // can_generate_monitor_events
    0, // can_generate_vm_object_alloc_events
    0, // can_generate_native_method_bind_events
    0, // can_generate_garbage_collection_events
    0  // can_generate_object_free_events
};

// 1 means that corresponding capability can be enabled
// on JVMTI_PHASE_LIVE
static const jvmtiCapabilities jvmti_enable_on_live_flags =
{
    0, // can_tag_objects
    0, // can_generate_field_modification_events
    0, // can_generate_field_access_events
    1, // can_get_bytecodes
    1, // can_get_synthetic_attribute
    1, // can_get_owned_monitor_info
    1, // can_get_current_contended_monitor
    1, // can_get_monitor_info
    0, // can_pop_frame
    0, // can_redefine_classes
    0, // can_signal_thread
    1, // can_get_source_file_name
    1, // can_get_line_numbers
    1, // can_get_source_debug_extension
    0, // can_access_local_variables
    0, // can_maintain_original_method_order
    0, // can_generate_single_step_events
    0, // can_generate_exception_events
    0, // can_generate_frame_pop_events
    0, // can_generate_breakpoint_events
    1, // can_suspend
    0, // can_redefine_any_class
    0, // can_get_current_thread_cpu_time
    0, // can_get_thread_cpu_time
    0, // can_generate_method_entry_events
    0, // can_generate_method_exit_events
    1, // can_generate_all_class_hook_events
    1, // can_generate_compiled_method_load_events
    0, // can_generate_monitor_events
    0, // can_generate_vm_object_alloc_events
    0, // can_generate_native_method_bind_events
    0, // can_generate_garbage_collection_events
    0  // can_generate_object_free_events
};

/*
 * Get Potential Capabilities
 *
 * Returns via capabilities_ptr the JVMTI features that can
 * potentially be possessed by this environment at this time.
 *
 * @note REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiGetPotentialCapabilities(jvmtiEnv* env,
                              jvmtiCapabilities* capabilities_ptr)
{
    TRACE2("jvmti.capability", "GetPotentialCapabilities called");
    SuspendEnabledChecker sec;

    // Check given env & current phase.
    jvmtiPhase phases[] = {JVMTI_PHASE_ONLOAD, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (NULL == capabilities_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    jvmtiPhase phase;
    jvmtiError errorCode = jvmtiGetPhase(env, &phase);

    if (errorCode != JVMTI_ERROR_NONE)
        return errorCode;

    if (JVMTI_PHASE_ONLOAD == phase)
        *capabilities_ptr = interpreter_enabled() ?
            jvmti_supported_interpreter_capabilities : jvmti_supported_jit_capabilities;
    else
    {
        // Add all capabilities from supported on live phase to already posessed capabilities
        TIEnv *ti_env = reinterpret_cast<TIEnv *>(env);
        unsigned char* puchar_ptr = (unsigned char*)capabilities_ptr;
        unsigned char* enable_ptr = (unsigned char*)&jvmti_enable_on_live_flags;

        *capabilities_ptr = ti_env->posessed_capabilities;

        for (int i = 0; i < int(sizeof(jvmtiCapabilities)); i++)
            puchar_ptr[i] |= enable_ptr[i]; 
    }

    return JVMTI_ERROR_NONE;
}

/*
 * Add Capabilities
 *
 * Set new capabilities by adding the capabilities pointed to by
 * capabilities_ptr. All previous capabilities are retained.
 * Typically this function is used in the OnLoad function.
 *
 * @note REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiAddCapabilities(jvmtiEnv* env,
                     const jvmtiCapabilities* capabilities_ptr)
{
    TRACE2("jvmti.capability", "AddCapabilities called");
    SuspendEnabledChecker sec;

    // Check given env & current phase.
    jvmtiPhase phases[] = {JVMTI_PHASE_ONLOAD, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (NULL == capabilities_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    jvmtiPhase phase;
    jvmtiError errorCode = jvmtiGetPhase(env, &phase);

    if (errorCode != JVMTI_ERROR_NONE)
        return errorCode;

    TIEnv *ti_env = reinterpret_cast<TIEnv *>(env);
    jvmtiCapabilities posessed = ti_env->posessed_capabilities;
    
    const jvmtiCapabilities* available_caps =
        (phase == JVMTI_PHASE_LIVE) ? &jvmti_enable_on_live_flags :
        (interpreter_enabled() ?
            &jvmti_supported_interpreter_capabilities :
            &jvmti_supported_jit_capabilities);

    unsigned char* requested = (unsigned char*)capabilities_ptr;
    unsigned char* available = (unsigned char*)available_caps;
    unsigned char* p_posessed = (unsigned char*)&posessed;

    // Allow to turn on any capabilities that are listed in potential capabilities
    for (int i = 0; i < int(sizeof(jvmtiCapabilities)); i++)
    {
        unsigned char adding_new = requested[i] & ~p_posessed[i];

        if (adding_new & ~available[i])
            return JVMTI_ERROR_NOT_AVAILABLE;

        p_posessed[i] |= adding_new;
    }

    // Add new capabilities after checking was done
    ti_env->posessed_capabilities = posessed;

    // Update global capabilities
    DebugUtilsTI *ti = ti_env->vm->vm_env->TI;
    if (capabilities_ptr->can_generate_method_entry_events)
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_ENTRY);

    if (capabilities_ptr->can_generate_method_exit_events)
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_EXIT);

    if (capabilities_ptr->can_generate_frame_pop_events)
    {
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_EXIT);
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_FRAME_POP_NOTIFICATION);
    }

    if (capabilities_ptr->can_generate_single_step_events)
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_SINGLE_STEP);

    if (capabilities_ptr->can_generate_exception_events)
        ti->set_global_capability(DebugUtilsTI::TI_GC_ENABLE_EXCEPTION_EVENT);

    return JVMTI_ERROR_NONE;
}

/*
 * Relinquish Capabilities
 *
 * Remove the capabilities pointed to by capabilities_ptr.
 * Some implementations may allow only one environment to have
 * capability (see the capability introduction).
 *
 * @note REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiRelinquishCapabilities(jvmtiEnv* env,
                            const jvmtiCapabilities* capabilities_ptr)
{
    TRACE2("jvmti.capability", "RelinquishCapabilities called");
    SuspendEnabledChecker sec;

    // Check given env & current phase.
    jvmtiPhase phases[] = {JVMTI_PHASE_ONLOAD, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (NULL == capabilities_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    TIEnv *ti_env = reinterpret_cast<TIEnv *>(env);
    unsigned char* p_posessed = (unsigned char*)&ti_env->posessed_capabilities;
    unsigned char* puchar_ptr = (unsigned char*)capabilities_ptr;

    jvmtiCapabilities removed_caps;
    unsigned char* removed_ptr = (unsigned char*)&removed_caps;

    // Remove all bits set in capabilities_ptr
    // FIXME: disable corresponding parts of VM according to removed capabilities
    for (int i = 0; i < int(sizeof(jvmtiCapabilities)); i++)
    {
        removed_ptr[i] = (p_posessed[i] & puchar_ptr[i]);
        p_posessed[i] &= ~removed_ptr[i];
    }

    DebugUtilsTI* ti = ti_env->vm->vm_env->TI;
    ti_env = ti->getEnvironments();

    while (NULL != ti_env)
    {
        TIEnv* next_env = ti_env->next;
        unsigned char* p_posessed = (unsigned char*)&ti_env->posessed_capabilities;

        // clear 'removed_caps' capabilities that posessed in any environment
        for (int i = 0; i < int(sizeof(jvmtiCapabilities)); i++)
            removed_ptr[i] &= ~p_posessed[i];

        ti_env = next_env;
    }

    // Now removed_ptr contains capabilities removed from all environments
    if (removed_caps.can_generate_method_entry_events)
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_ENTRY);

    if (removed_caps.can_generate_method_exit_events)
    {
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_EXIT);
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_FRAME_POP_NOTIFICATION);
    }

    if (removed_caps.can_generate_frame_pop_events)
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_FRAME_POP_NOTIFICATION);

    if (removed_caps.can_generate_single_step_events)
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_SINGLE_STEP);

    if (removed_caps.can_generate_exception_events)
        ti->reset_global_capability(DebugUtilsTI::TI_GC_ENABLE_EXCEPTION_EVENT);

    return JVMTI_ERROR_NONE;
}

/*
 * Get Capabilities
 *
 * Returns via capabilities_ptr the optional JVMTI features which
 * this environment currently possesses. An environment does not
 * possess a capability unless it has been successfully added with
 * AddCapabilities. An environment only loses possession of a
 * capability if it has been relinquished with
 * RelinquishCapabilities. Thus, this function returns the net
 * result of the AddCapabilities and RelinquishCapabilities calls
 * which have been made.
 *
 * @note REQUIRED Functionality.
 */
jvmtiError JNICALL
jvmtiGetCapabilities(jvmtiEnv* env,
                     jvmtiCapabilities* capabilities_ptr)
{
    TRACE2("jvmti.capability", "GetCapabilities called");
    SuspendEnabledChecker sec;

    // Can be called from any phase
    // Check only given env.
    jvmtiPhase* phases = NULL;

    CHECK_EVERYTHING();

    if (NULL == capabilities_ptr)
        return JVMTI_ERROR_NULL_POINTER;

    TIEnv *ti_env = reinterpret_cast<TIEnv *>(env);

    *capabilities_ptr = ti_env->posessed_capabilities;

    return JVMTI_ERROR_NONE;
}

