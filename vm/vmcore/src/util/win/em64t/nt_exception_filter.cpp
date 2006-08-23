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
 * @author Intel, Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.5 $
 */  


#include "platform_lowlevel.h"
#include "Class.h"
#include "Environment.h"
#include "gc_for_vm.h"
#include "exceptions.h"
#include "vm_synch.h"
#include "method_lookup.h"
#include "vm_strings.h"
#include "vm_threads.h"
#include "vm_utils.h"
#include "compile.h"
#include "ini.h"
#include "cxxlog.h"

#include "exception_filter.h"

#include "thread_generic.h"



// Afremov Pavel 20050117
#include "../m2n_em64t_internal.h"

int NT_exception_filter(LPEXCEPTION_POINTERS p_NT_exception) 
{

    // this filter catches _all_ null ptr exceptions including those caused by
    // VM internal code.  To elimate confusion over what caused the null ptr
    // exception, we first make sure the exception was thrown inside a Java
    // method else assert(0); <--- means it was thrown by VM C/C++ code.

    Global_Env *env = VM_Global_State::loader_env;

    VM_Code_Type vmct =
        vm_identify_eip((void *)p_NT_exception->ContextRecord->Eip);
    if(vmct != VM_TYPE_JAVA) {
        if (!vm_get_boolean_property_value_with_default("vm.assert_dialog")) {
            WARN("Fatal exception, terminating");
            return EXCEPTION_EXECUTE_HANDLER;
        }
        return EXCEPTION_CONTINUE_SEARCH;
    }

    // since we are now sure NPE occured in java code, gc should also have been disabled
    assert(!hythread_is_suspend_enabled());

    
    volatile ManagedObject *exc = 0;
    Class *exc_clss = 0;
    switch(p_NT_exception->ExceptionRecord->ExceptionCode) {
    case STATUS_ACCESS_VIOLATION:
        // null pointer exception -- see ...\vc\include\winnt.h
        {
            // Lazy exception object creation
            exc_clss = env->java_lang_NullPointerException_Class;
        }
        break;

    case STATUS_INTEGER_DIVIDE_BY_ZERO:
        // divide by zero exception  -- see ...\vc\include\winnt.h
        {
            // Lazy exception object creation
            exc_clss = env->java_lang_ArithmeticException_Class;
        }
        break;

    case STATUS_PRIVILEGED_INSTRUCTION:
        {
            ABORT("Unexpected exception code");
            vm_exit(99553);
        }
        break;

    default:
        return EXCEPTION_CONTINUE_SEARCH;
    }

    Registers regs;
    regs.rax = p_NT_exception->ContextRecord->Eax;
    regs.rcx = p_NT_exception->ContextRecord->Ecx;
    regs.rdx = p_NT_exception->ContextRecord->Edx;
    regs.rdi = p_NT_exception->ContextRecord->Edi;
    regs.rsi = p_NT_exception->ContextRecord->Esi;
    regs.rbx = p_NT_exception->ContextRecord->Ebx;
    regs.rbp = p_NT_exception->ContextRecord->Ebp;
    regs.rip = p_NT_exception->ContextRecord->Eip;
    regs.rsp = p_NT_exception->ContextRecord->Esp;

    exn_athrow_regs(&regs, exc_clss);

    p_NT_exception->ContextRecord->Esp = regs.rsp;
    p_NT_exception->ContextRecord->Eip = regs.rip;
    p_NT_exception->ContextRecord->Ebp = regs.rbp;
    p_NT_exception->ContextRecord->Ebx = regs.rbx;
    p_NT_exception->ContextRecord->Esi = regs.rsi;
    p_NT_exception->ContextRecord->Edi = regs.rdi;
    p_NT_exception->ContextRecord->Eax = regs.rax;
    p_NT_exception->ContextRecord->Ecx = regs.rcx;
    p_NT_exception->ContextRecord->Edx = regs.rdx;

    return EXCEPTION_CONTINUE_EXECUTION;
} //NT_exception_filter

int call_the_run_method3( void * p_xx ){
    LPEXCEPTION_POINTERS p_NT_exception;
    int NT_exception_filter(LPEXCEPTION_POINTERS p_NT_exception);

    // NT null pointer exception support
    __try {  
        call_the_run_method(p_xx);
        return 0;
    }
    __except ( p_NT_exception = GetExceptionInformation(), 
        NT_exception_filter(p_NT_exception) ) {

        ABORT("Uncaught exception");  // get here only if NT_null_ptr_filter() screws up

        return 0;
    }  // NT null pointer exception support

}
