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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.1.4.3 $
 */  


#define LOG_DOMAIN "vm.core"
#include "cxxlog.h"
#include <assert.h>
#include "ini.h"
#include "environment.h"
#include "open/em_vm.h"


void
vm_execute_java_method_array(jmethodID method, jvalue *result, jvalue *args) {
    // TODO: select jit which compiled the method
    assert(!hythread_is_suspend_enabled());
    //FIXME integration
    //DEBUG_PUSH_LOCK(JAVA_CODE_PSEUDO_LOCK);
    assert(NULL != VM_Global_State::loader_env);
    assert(NULL != VM_Global_State::loader_env->em_interface);
    assert(NULL != VM_Global_State::loader_env->em_interface->ExecuteMethod);

    DebugUtilsTI *ti = VM_Global_State::loader_env->TI;
    if (ti->isEnabled() && ti->is_single_step_enabled() &&
        ti->getPhase() == JVMTI_PHASE_LIVE)
    {
        VM_thread *vm_thread = p_TLS_vmthread;
        if (NULL != vm_thread->ss_state)
        {
            // Start single stepping a new Java method
            LMAutoUnlock lock(&ti->brkpntlst_lock);
            jvmti_remove_single_step_breakpoints(ti, vm_thread);

            jvmti_StepLocation method_start = {(Method *)method, 0};
            jvmtiError UNREF errorCode = jvmti_set_single_step_breakpoints(
                ti, vm_thread, &method_start, 1);
            assert(JVMTI_ERROR_NONE == errorCode);
        }
    }

    VM_Global_State::loader_env->em_interface->ExecuteMethod(method, result, args);
    //DEBUG_POP_LOCK(JAVA_CODE_PSEUDO_LOCK);

    // gregory - When method executes *return bytecode in Java it will
    // set a breakpoint on the bytecode after invoke* or another
    // bytecode which caused managed to VM transition. This is a
    // general case of *return handling. So it seems here we don't
    // have to do anything, *return handling will set a breakpoint up
    // the stack in java automatically.
}

