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
 * See official specification at:
 * http://java.sun.com/j2se/1.5.0/docs/guide/jvmti/jvmti.html.
 *
 */

#define LOG_DOMAIN "jvmti"
#include "cxxlog.h"

#include "jvmti.h"
#include "jvmti_internal.h"
#include "jvmti_utils.h"
#include "open/vm_util.h"
#include "environment.h"
#include <string.h>
#include "properties.h"
#include "jvmti_break_intf.h"
#include "interpreter_exports.h"

#include "port_filepath.h"
#include "port_dso.h"
#include <apr_strings.h>

#ifdef PLATFORM_NT
#define AGENT_ONLOAD "_Agent_OnLoad@12"
#define AGENT_ONUNLOAD "_Agent_OnUnload@4"
#define JVM_ONLOAD "_JVM_OnLoad@12"
#define JVM_ONUNLOAD "_JVM_OnUnLoad@4"
#else
#define AGENT_ONLOAD "Agent_OnLoad"
#define AGENT_ONUNLOAD "Agent_OnUnload"
#define JVM_ONLOAD "JVM_OnLoad"
#define JVM_ONUNLOAD "JVM_OnUnLoad"
#endif

static void JNICALL jvmtiUnimpStub(JNIEnv*);
Agent *current_loading_agent;

const struct ti_interface jvmti_table =
{
    (void *)jvmtiUnimpStub,
    jvmtiSetEventNotificationMode,
    (void *)jvmtiUnimpStub,
    jvmtiGetAllThreads,
    jvmtiSuspendThread,
    jvmtiResumeThread,
    jvmtiStopThread,
    jvmtiInterruptThread,
    jvmtiGetThreadInfo,
    jvmtiGetOwnedMonitorInfo,
    jvmtiGetCurrentContendedMonitor,
    jvmtiRunAgentThread,
    jvmtiGetTopThreadGroups,
    jvmtiGetThreadGroupInfo,
    jvmtiGetThreadGroupChildren,
    jvmtiGetFrameCount,
    jvmtiGetThreadState,
    (void *)jvmtiUnimpStub,
    jvmtiGetFrameLocation,
    jvmtiNotifyFramePop,
    jvmtiGetLocalObject,
    jvmtiGetLocalInt,
    jvmtiGetLocalLong,
    jvmtiGetLocalFloat,
    jvmtiGetLocalDouble,
    jvmtiSetLocalObject,
    jvmtiSetLocalInt,
    jvmtiSetLocalLong,
    jvmtiSetLocalFloat,
    jvmtiSetLocalDouble,
    jvmtiCreateRawMonitor,
    jvmtiDestroyRawMonitor,
    jvmtiRawMonitorEnter,
    jvmtiRawMonitorExit,
    jvmtiRawMonitorWait,
    jvmtiRawMonitorNotify,
    jvmtiRawMonitorNotifyAll,
    jvmtiSetBreakpoint,
    jvmtiClearBreakpoint,
    (void *)jvmtiUnimpStub,
    jvmtiSetFieldAccessWatch,
    jvmtiClearFieldAccessWatch,
    jvmtiSetFieldModificationWatch,
    jvmtiClearFieldModificationWatch,
    (void *)jvmtiUnimpStub,
    jvmtiAllocate,
    jvmtiDeallocate,
    jvmtiGetClassSignature,
    jvmtiGetClassStatus,
    jvmtiGetSourceFileName,
    jvmtiGetClassModifiers,
    jvmtiGetClassMethods,
    jvmtiGetClassFields,
    jvmtiGetImplementedInterfaces,
    jvmtiIsInterface,
    jvmtiIsArrayClass,
    jvmtiGetClassLoader,
    jvmtiGetObjectHashCode,
    jvmtiGetObjectMonitorUsage,
    jvmtiGetFieldName,
    jvmtiGetFieldDeclaringClass,
    jvmtiGetFieldModifiers,
    jvmtiIsFieldSynthetic,
    jvmtiGetMethodName,
    jvmtiGetMethodDeclaringClass,
    jvmtiGetMethodModifiers,
    (void *)jvmtiUnimpStub,
    jvmtiGetMaxLocals,
    jvmtiGetArgumentsSize,
    jvmtiGetLineNumberTable,
    jvmtiGetMethodLocation,
    jvmtiGetLocalVariableTable,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiGetBytecodes,
    jvmtiIsMethodNative,
    jvmtiIsMethodSynthetic,
    jvmtiGetLoadedClasses,
    jvmtiGetClassLoaderClasses,
    jvmtiPopFrame,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiRedefineClasses,
    jvmtiGetVersionNumber,
    jvmtiGetCapabilities,
    jvmtiGetSourceDebugExtension,
    jvmtiIsMethodObsolete,
    jvmtiSuspendThreadList,
    jvmtiResumeThreadList,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiGetAllStackTraces,
    jvmtiGetThreadListStackTraces,
    jvmtiGetThreadLocalStorage,
    jvmtiSetThreadLocalStorage,
    jvmtiGetStackTrace,
    (void *)jvmtiUnimpStub,
    jvmtiGetTag,
    jvmtiSetTag,
    jvmtiForceGarbageCollection,
    jvmtiIterateOverObjectsReachableFromObject,
    jvmtiIterateOverReachableObjects,
    jvmtiIterateOverHeap,
    jvmtiIterateOverInstancesOfClass,
    (void *)jvmtiUnimpStub,
    jvmtiGetObjectsWithTags,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiSetJNIFunctionTable,
    jvmtiGetJNIFunctionTable,
    jvmtiSetEventCallbacks,
    jvmtiGenerateEvents,
    jvmtiGetExtensionFunctions,
    jvmtiGetExtensionEvents,
    jvmtiSetExtensionEventCallback,
    jvmtiDisposeEnvironment,
    jvmtiGetErrorName,
    jvmtiGetJLocationFormat,
    jvmtiGetSystemProperties,
    jvmtiGetSystemProperty,
    jvmtiSetSystemProperty,
    jvmtiGetPhase,
    jvmtiGetCurrentThreadCpuTimerInfo,
    jvmtiGetCurrentThreadCpuTime,
    jvmtiGetThreadCpuTimerInfo,
    jvmtiGetThreadCpuTime,
    jvmtiGetTimerInfo,
    jvmtiGetTime,
    jvmtiGetPotentialCapabilities,
    (void *)jvmtiUnimpStub,
    jvmtiAddCapabilities,
    jvmtiRelinquishCapabilities,
    jvmtiGetAvailableProcessors,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiGetEnvironmentLocalStorage,
    jvmtiSetEnvironmentLocalStorage,
    jvmtiAddToBootstrapClassLoaderSearch,
    jvmtiSetVerboseFlag,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    (void *)jvmtiUnimpStub,
    jvmtiGetObjectSize
};

static void JNICALL jvmtiUnimpStub(JNIEnv* UNREF env)
{
    // If we ever get here, we are in an implemented JVMTI function
    // By looking at the call stack and assembly it should be clear which one
    ABORT("Not implemented");
}

jint JNICALL create_jvmti_environment(JavaVM *vm_ext, void **env, jint version)
{
    JavaVM_Internal *vm = (JavaVM_Internal *)vm_ext;
    // FIXME: there should be a check on whether the thread is attached to VM. How?

    jint vmagic = version & JVMTI_VERSION_MASK_INTERFACE_TYPE;
    jint vmajor = version & JVMTI_VERSION_MASK_MAJOR;
    jint vminor = version & JVMTI_VERSION_MASK_MINOR;
    jint vmicro = version & JVMTI_VERSION_MASK_MICRO;
    if (vmagic != JVMTI_VERSION_INTERFACE_JVMTI ||
        vmajor > (JVMTI_VERSION_MAJOR << JVMTI_VERSION_SHIFT_MAJOR) ||
        vminor > (JVMTI_VERSION_MINOR << JVMTI_VERSION_SHIFT_MINOR) ||
        vmicro > (JVMTI_VERSION_MICRO << JVMTI_VERSION_SHIFT_MICRO))
    {
        *env = NULL;
        return JNI_EVERSION;
    }

    TIEnv *newenv;
    jvmtiError error_code;
    error_code = _allocate(sizeof(TIEnv), (unsigned char**)&newenv);
    if (error_code != JVMTI_ERROR_NONE)
    {
        *env = NULL;
        return error_code;
    }

    error_code = newenv->allocate_extension_event_callbacks_table();
    if (error_code != JVMTI_ERROR_NONE)
    {
        _deallocate((unsigned char *)newenv);
        *env = NULL;
        return error_code;
    }

    newenv->functions = &jvmti_table;
    newenv->vm = vm;
    newenv->user_storage = NULL;
    newenv->agent = current_loading_agent;
    memset(&newenv->event_table, 0, sizeof(jvmtiEventCallbacks));
    memset(&newenv->posessed_capabilities, 0, sizeof(jvmtiCapabilities));
    memset(&newenv->global_events, 0, sizeof(newenv->global_events));
    memset(&newenv->event_threads, 0, sizeof(newenv->event_threads));

    // Acquire interface for breakpoint handling
    newenv->brpt_intf =
        vm->vm_env->TI->vm_brpt->new_intf(jvmti_process_breakpoint_event,
                                          PRIORITY_SIMPLE_BREAKPOINT,
                                          interpreter_enabled());

    LMAutoUnlock lock(&vm->vm_env->TI->TIenvs_lock);
    vm->vm_env->TI->addEnvironment(newenv);
    *env = newenv;
    TRACE2("jvmti", "New environment added: " << newenv);

    return JNI_OK;
}

void DebugUtilsTI::setExecutionMode(Global_Env *p_env)
{
    for (int i = 0; i < p_env->vm_arguments.nOptions; i++) {
        char *option = p_env->vm_arguments.options[i].optionString;

        if (!strncmp(option, "-agentlib:", 10) ||
            !strncmp(option, "-agentpath:", 11) ||
            !strncmp(option, "-Xrun", 5))
        {
            TRACE2("jvmti", "Enabling EM JVMTI mode");
            add_pair_to_properties(p_env->properties, "vm.jvmti.enabled", "true");
            break;
        }
    }
}

DebugUtilsTI::DebugUtilsTI() :
    agent_counter(1),
    access_watch_list(NULL),
    modification_watch_list(NULL),
    status(false),
    agents(NULL),
    p_TIenvs(NULL),
    MAX_NOTIFY_LIST(1000),
    loadListNumber(0),
    prepareListNumber(0),
    global_capabilities(0),
    single_step_enabled(false)
{
    jvmtiError UNUSED res = _allocate( MAX_NOTIFY_LIST * sizeof(Class**),
        (unsigned char**)&notifyLoadList );
    assert(res == JVMTI_ERROR_NONE);
    res = _allocate( MAX_NOTIFY_LIST * sizeof(Class**),
        (unsigned char**)&notifyPrepareList );
    assert(res == JVMTI_ERROR_NONE);
    vm_brpt = new VMBreakPoints();
    assert(vm_brpt);
    return;
}

DebugUtilsTI::~DebugUtilsTI()
{
    ReleaseNotifyLists();
    delete vm_brpt;
    return;
}

void DebugUtilsTI::SetPendingNotifyLoadClass( Class *klass )
{
    assert(loadListNumber < MAX_NOTIFY_LIST);
    notifyLoadList[loadListNumber++] = klass;
}

void DebugUtilsTI::SetPendingNotifyPrepareClass( Class *klass )
{
    assert(prepareListNumber < MAX_NOTIFY_LIST);
    notifyPrepareList[prepareListNumber++] = klass;
}

unsigned DebugUtilsTI::GetNumberPendingNotifyLoadClass()
{
    return loadListNumber;
}

unsigned DebugUtilsTI::GetNumberPendingNotifyPrepareClass()
{
    return prepareListNumber;
}

Class * DebugUtilsTI::GetPendingNotifyLoadClass( unsigned number )
{
    assert(number < loadListNumber);
    return notifyLoadList[number];
}

Class * DebugUtilsTI::GetPendingNotifyPrepareClass( unsigned number )
{
    assert(number < prepareListNumber);
    return notifyPrepareList[number];
}

void DebugUtilsTI::ReleaseNotifyLists()
{
    if( notifyLoadList ) {
        _deallocate( (unsigned char*)notifyLoadList );
        notifyLoadList = NULL;
        loadListNumber = MAX_NOTIFY_LIST;
    }
    if( notifyPrepareList ) {
        _deallocate( (unsigned char*)notifyPrepareList );
        notifyPrepareList = NULL;
        prepareListNumber = MAX_NOTIFY_LIST;
    }
    return;
}

int DebugUtilsTI::getVersion(char* UNREF version)
{
    return 0;
}

// Return lib name and options string if there are any options
static char *parse_agent_option(apr_pool_t* pool, const char *str, const char *option_str,
                         const char option_separator, char **options)
{
    int cmd_length = strlen(option_str);
    const char *lib_name = str + cmd_length;
    char *opts_start = strchr(lib_name, option_separator);
    int lib_name_length;

    if (NULL == opts_start)
        lib_name_length = strlen(lib_name);
    else
    {
        lib_name_length = opts_start - lib_name;
        opts_start++;
    }

    char *path = apr_pstrdup(pool, lib_name);
    path[lib_name_length] = '\0';
    *options = opts_start;
    return path;
}

bool open_agent_library(Agent *agent, const char *lib_name, bool print_error)
{
    if (APR_SUCCESS != apr_dso_load(&agent->agentLib, lib_name, agent->pool))
    {
        if (print_error) {
            char buf[256];
            WARN("Failed to open agent library " << lib_name << " : " 
                << apr_dso_error(agent->agentLib, buf, 256));
        }
        return false;
    }
    else
        return true;
}

bool find_agent_onload_function(Agent *agent, const char *function_name)
{
    apr_dso_handle_sym_t handle = 0;
    apr_status_t status = apr_dso_sym(&handle, agent->agentLib, function_name);
    agent->Agent_OnLoad_func = (f_Agent_OnLoad)handle;
    return status == APR_SUCCESS;
}

bool find_agent_onunload_function(Agent *agent, const char *function_name)
{
    apr_dso_handle_sym_t handle = 0;
    apr_status_t status = apr_dso_sym(&handle, agent->agentLib, function_name);
    agent->Agent_OnUnLoad_func = (f_Agent_OnUnLoad)handle;
    return status == APR_SUCCESS;
}

jint load_agentpath(Agent *agent, const char *str, JavaVM_Internal *vm)
{
    char *lib_name, *agent_options;
    lib_name = parse_agent_option(agent->pool, str, "-agentpath:", '=', &agent_options);
    if (!open_agent_library(agent, lib_name, true))
        return -1;

    const char *callback = AGENT_ONLOAD, *callback_unload = AGENT_ONUNLOAD;
    if (!find_agent_onload_function(agent, callback))
    {
        char buf[256];
        WARN("No agent entry function found in library " << lib_name << " : " 
            << apr_dso_error(agent->agentLib, buf, 256));
        return -1;
    }
#ifdef _DEBUG
    if (NULL == agent_options)
    {
        TRACE2("jvmti", "Calling onload in lib " << lib_name << " with NULL options");
    }
    else
    {
        TRACE2("jvmti", "Calling onload in lib " << lib_name << " with options " << agent_options);
    }
#endif
    find_agent_onunload_function(agent, callback_unload);
    assert(agent->Agent_OnLoad_func);
    jint result = agent->Agent_OnLoad_func(vm, agent_options, NULL);
    if (0 != result)
        WARN("Agent library " << lib_name << " initialization function returned " << result);
    return result;
}

static void generate_platform_lib_name(apr_pool_t* pool, JavaVM_Internal *vm, 
                                       const char *lib_name,
                                       char **p_path1, char **p_path2)
{
    const char *vm_libs = vm->vm_env->properties.get("vm.boot.library.path")->as_string();
    assert(vm_libs);
    char *path1 = apr_pstrdup(pool, vm_libs);
    char *path2 = port_dso_name_decorate(lib_name, pool);
    path1 = port_filepath_merge(path1, path2, pool);
    *p_path1 = path1;
    *p_path2 = path2;
}

jint load_agentlib(Agent *agent, const char *str, JavaVM_Internal *vm)
{
    char *lib_name, *agent_options;
    lib_name = parse_agent_option(agent->pool, str, "-agentlib:", '=', &agent_options);
    char *path1, *path2, *path;
    generate_platform_lib_name(agent->pool, vm, lib_name, &path1, &path2);

    bool status = open_agent_library(agent, path1, false);
    if (!status)
    {
        status = open_agent_library(agent, path2, true);
        if (!status)
        {
            WARN("Failed to open agent library " << path2);
            return -1;
        }
        else
            path = path2;
    }
    else
    {
        path = path1;
    }

    const char *callback = AGENT_ONLOAD, *callback_unload = AGENT_ONUNLOAD;

    if (!find_agent_onload_function(agent, callback))
    {
        char buf[256];
        WARN("No agent entry function found in library " << path << " : " 
            << apr_dso_error(agent->agentLib, buf, 256));
        return -1;
    }
#ifdef _DEBUG
    if (NULL == agent_options)
    {
        TRACE2("jvmti", "Calling onload in lib " << path << " with NULL options");
    }
    else
    {
        TRACE2("jvmti", "Calling onload in lib " << path << " with options " << agent_options);
    }
#endif
    find_agent_onunload_function(agent, callback_unload);
    assert(agent->Agent_OnLoad_func);
    jint result = agent->Agent_OnLoad_func(vm, agent_options, NULL);
    if (0 != result)
        WARN("Agent library " << path << " initialization function returned " << result);
    return result;
}

jint load_xrun(Agent *agent, const char *str, JavaVM_Internal *vm)
{
    char *lib_name, *agent_options;
    lib_name = parse_agent_option(agent->pool, str, "-xrun", ':', &agent_options);
    char *path1, *path2, *path;
    generate_platform_lib_name(agent->pool, vm, lib_name, &path1, &path2);

    bool status = open_agent_library(agent, path1, false);
    if (!status)
    {
        status = open_agent_library(agent, path2, true);
        if (!status)
        {
            WARN("Failed to open agent library " << path2);
            return -1;
        }
        else
            path = path2;
    }
    else
    {
        path = path1;
    }

    const char *callback1 = AGENT_ONLOAD, *callback1_unload = AGENT_ONUNLOAD;
    const char *callback2 = JVM_ONLOAD, *callback2_unload = JVM_ONUNLOAD;

    if (!find_agent_onload_function(agent, callback1))
    {
        if (!find_agent_onload_function(agent, callback2))
        {
            char buf[256];
            WARN("No agent entry function found in library" << path << " : " 
                << apr_dso_error(agent->agentLib, buf, 256));
            return -1;
        }
        else
            find_agent_onunload_function(agent, callback2_unload);
    }
    else
        find_agent_onunload_function(agent, callback1_unload);
#ifdef _DEBUG
    if (NULL == agent_options)
    {
        TRACE2("jvmti", "Calling onload in lib " << path << " with NULL options");
    }
    else
    {
        TRACE2("jvmti", "Calling onload in lib " << path << " with options " << agent_options);
    }
#endif
    assert(agent->Agent_OnLoad_func);
    jint result = agent->Agent_OnLoad_func(vm, agent_options, NULL);
    if (0 != result)
        WARN("Agent library " << path << " initialization function returned " << result);
    return result;
}

jint DebugUtilsTI::Init()
{
    phase = JVMTI_PHASE_ONLOAD;
    Agent* agent = this->getAgents();

    /*
     * 0. create default jvmtiEnv
     * 1. exclude name of DLL from char*
     * 2. exclude options from char*
     * 3. load DLL and start AgentOnLoad and AgentOnUnload
     * 4. filling of internal structure of TI
     */

    /* ************************************************************** */

    /*
     * If agentsList is NULL -> it means that there were not any agents in
     * command line -> and it means that TI is disabled.
     */
    if (agent==NULL)
        return 0;
    else
    {
        status = true;
        JavaVM_Internal *vm;
        jni_native_intf->GetJavaVM((JavaVM**)&vm);
        // FIXME: workaround to let get_vm_thread_ptr_safe function and other JNI code
        // to work in OnLoad phase
        NativeObjectHandles noh;

        while (agent)
        {
            int result = 0;
            const char *str = agent->agentName;
            agent->agent_id = agent_counter++;
            agent->dynamic_agent = JNI_FALSE;

            TRACE2("jvmti", "Agent str = " << str);

            current_loading_agent = agent;
            if (strncmp(str, "-agentpath:",  11) == 0)
                result = load_agentpath(agent, str, vm);
            else if (strncmp(str, "-agentlib:", 10) == 0)
                result = load_agentlib(agent, str, vm);
            else if (strncmp(str, "-Xrun:", 5) == 0)
                result = load_xrun(agent, str, vm);
            else
                DIE("Unknown agent loading option " << str);
            current_loading_agent = NULL;


            if (0 != result)
                return result;
            agent = agent->next;
        }
    }

    nextPhase(JVMTI_PHASE_PRIMORDIAL);

    return 0;
}

/* Calls Agent_OnUnlod() for agents where it was found, then unloads agents.
*/
void DebugUtilsTI::Shutdown()
{
    JavaVM *vm;
    jni_native_intf->GetJavaVM(&vm);

    Agent* agent = this->getAgents();

    while (agent != NULL)
    {
        if (agent->Agent_OnUnLoad_func != NULL)
        {
            TRACE2("jvmti", "Calling OnUnload in lib " << agent->agentName);
            agent->Agent_OnUnLoad_func(vm);
        }

        if (APR_SUCCESS != apr_dso_unload(agent->agentLib))
        {
            char buf[256];
            WARN("Failed to unload agent library " << agent->agentName << " : " 
                << apr_dso_error(agent->agentLib, buf, 256));
        }

        agent = agent->next;
    }
}

void DebugUtilsTI::addAgent(const char* str) {
    Agent* newagent = new Agent(str);

    newagent->next = getAgents();
    agents = newagent;
}

Agent* DebugUtilsTI::getAgents() {
    return this->agents;
}

void DebugUtilsTI::setAgents(Agent *agents) {
    this->agents = agents;
}

bool DebugUtilsTI::isEnabled() {
    return status;
}

void DebugUtilsTI::setEnabled() {
    this->status = true;
    return;
}

void DebugUtilsTI::setDisabled() {
    this->status = false;
    return;
}

jvmtiError jvmti_translate_jit_error(OpenExeJpdaError error)
{
    switch (error)
    {
    case EXE_ERROR_NONE:
        return JVMTI_ERROR_NONE;
    case EXE_ERROR_INVALID_METHODID:
        return JVMTI_ERROR_INTERNAL;
    case EXE_ERROR_INVALID_LOCATION:
        return JVMTI_ERROR_INTERNAL;
    case EXE_ERROR_TYPE_MISMATCH:
        return JVMTI_ERROR_TYPE_MISMATCH;
    case EXE_ERROR_INVALID_SLOT:
        return JVMTI_ERROR_INVALID_SLOT;
    case EXE_ERROR_UNSUPPORTED:
        return JVMTI_ERROR_INTERNAL;
    default:
        return JVMTI_ERROR_INTERNAL;
    }
}

void jvmti_get_compilation_flags(OpenMethodExecutionParams *flags)
{
    DebugUtilsTI *ti = VM_Global_State::loader_env->TI;

    if (!ti->isEnabled())
        return;

    flags->exe_do_code_mapping = flags->exe_do_local_var_mapping = 1;

    if (ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_ENTRY))
        flags->exe_notify_method_entry = 1;

    if (ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_METHOD_EXIT) ||
        ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_FRAME_POP_NOTIFICATION))
        flags->exe_notify_method_exit = 1;

    if (ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_FIELD_ACCESS_EVENT))
        flags->exe_notify_field_access = true;
    if (ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_FIELD_MODIFICATION_EVENT))
        flags->exe_notify_field_modification = true;

    if (ti->get_global_capability(DebugUtilsTI::TI_GC_ENABLE_POP_FRAME)) {
        flags->exe_restore_context_after_unwind = true;
        flags->exe_provide_access_to_this = true;
    }
}

