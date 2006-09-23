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
 * @author Ivan Volosyuk
 * @version $Revision: 1.23.20.3 $
 */  
#include <stdlib.h>
#include <stdio.h>
#include "open/types.h"
#include "open/vm.h"
#include "platform_lowlevel.h"
#include "interpreter_exports.h"
#include "jit_export.h"

typedef void *GC_Enumeration_Handle;

#ifndef PLATFORM_POSIX
#define EXPORT __declspec(dllexport)
#else 
#define EXPORT
#endif

extern "C" {
    extern void EXPORT JIT_init(JIT_Handle h, const char* name);
    EXPORT extern void JIT_deinit(JIT_Handle h);
    EXPORT extern void JIT_unwind_stack_frame(JIT_Handle, Method_Handle, JitFrameContext *);
    EXPORT extern void JIT_get_root_set_from_stack_frame(JIT_Handle, Method_Handle, GC_Enumeration_Handle, JitFrameContext *);
    EXPORT extern void JIT_get_root_set_for_thread_dump(JIT_Handle, Method_Handle, GC_Enumeration_Handle, JitFrameContext *);
    EXPORT extern Boolean JIT_can_enumerate(JIT_Handle, Method_Handle, NativeCodePtr);
    EXPORT extern void JIT_fix_handler_context(JIT_Handle, Method_Handle, JitFrameContext *);
    EXPORT extern void * JIT_get_address_of_this(JIT_Handle, Method_Handle, const JitFrameContext *);
    EXPORT extern Boolean JIT_call_returns_a_reference(JIT_Handle, Method_Handle, const JitFrameContext *);
    EXPORT extern JIT_Result JIT_gen_method_info(JIT_Handle,Compile_Handle, Method_Handle, JIT_Flags);
    EXPORT extern JIT_Result JIT_compile_method(JIT_Handle,Compile_Handle, Method_Handle, JIT_Flags);
    EXPORT extern void JIT_init_with_data(JIT_Handle, void *);
    EXPORT extern Boolean JIT_supports_compressed_references(JIT_Handle);
    EXPORT extern void JIT_execute_method(JIT_Handle,jmethodID method, jvalue *return_value, jvalue *args);
}

EXPORT void JIT_unwind_stack_frame(JIT_Handle, Method_Handle, JitFrameContext *) { abort(); }
EXPORT void JIT_get_root_set_from_stack_frame(JIT_Handle, Method_Handle, GC_Enumeration_Handle, JitFrameContext *) { abort(); }
EXPORT void JIT_get_root_set_for_thread_dump(JIT_Handle, Method_Handle, GC_Enumeration_Handle, JitFrameContext *) {return;}
EXPORT Boolean JIT_can_enumerate(JIT_Handle, Method_Handle, NativeCodePtr) { abort(); return true; }
EXPORT void JIT_fix_handler_context(JIT_Handle, Method_Handle, JitFrameContext *) { abort(); }
EXPORT void * JIT_get_address_of_this(JIT_Handle, Method_Handle, const JitFrameContext *) { abort(); }
EXPORT Boolean JIT_call_returns_a_reference(JIT_Handle, Method_Handle, const JitFrameContext *) { abort(); return true; }
EXPORT JIT_Result JIT_gen_method_info(JIT_Handle,Compile_Handle, Method_Handle, JIT_Flags) { abort(); }
EXPORT void JIT_init_with_data(JIT_Handle, void *) { abort(); }


struct StackTraceFrame;

extern bool interpreter_st_get_frame(unsigned target_depth, StackTraceFrame* stf);
extern jvmtiError interpreter_ti_getFrameLocation(
        struct jvmtiEnv_struct *,class VM_thread *,int, struct _jmethodID * *,int64 *);
extern jvmtiError interpreter_ti_getLocal32( struct jvmtiEnv_struct *,class VM_thread *,int,int,int *);
extern jvmtiError interpreter_ti_getLocal64( struct jvmtiEnv_struct *,class VM_thread *,int,int,int64 *);
extern jvmtiError interpreter_ti_getObject( struct jvmtiEnv_struct *,class VM_thread *,int,int,struct _jobject * *);
extern jvmtiError interpreter_ti_getStackTrace(struct jvmtiEnv_struct *,class VM_thread *,int,int,jvmtiFrameInfo *,int *);

extern jvmtiError interpreter_ti_get_frame_count( struct jvmtiEnv_struct *,class VM_thread *,int *);
extern jvmtiError interpreter_ti_notify_frame_pop (jvmtiEnv*, VM_thread *thread, int depth);
extern jvmtiError interpreter_ti_setLocal32( struct jvmtiEnv_struct *,class VM_thread *,int,int,int);
extern jvmtiError interpreter_ti_setLocal64( struct jvmtiEnv_struct *,class VM_thread *,int,int,int64);
extern jvmtiError interpreter_ti_setObject( struct jvmtiEnv_struct *,class VM_thread *,int,int,struct _jobject *);

extern unsigned int interpreter_st_get_interrupted_method_native_bit(class VM_thread *);
extern void interpreter_enumerate_thread(class VM_thread *);
extern void interpreter_st_get_trace(VM_thread *thread, unsigned int *,struct StackTraceFrame * *);
uint64* interpreter_get_stacked_register_address(uint64* bsp, unsigned reg);
extern void interpreter_execute_method(Method *method, jvalue *return_value, jvalue *args);

extern void interpreter_ti_set_notification_mode(jvmtiEvent event_type, bool enable);
extern jbyte interpreter_ti_set_breakpoint(jmethodID method, jlocation location);
extern void interpreter_ti_clear_breakpoint(jmethodID method, jlocation location, jbyte saved);
extern jvmtiError interpreter_ti_pop_frame(jvmtiEnv*, VM_thread *thread);
extern void stack_dump(VM_thread *thread);

extern FrameHandle* interpreter_get_last_frame(class VM_thread *thread);
extern FrameHandle* interpreter_get_prev_frame(FrameHandle* frame);
extern bool is_frame_in_native_frame(FrameHandle* frame, void* begin, void* end);

void EXPORT JIT_init(JIT_Handle UNREF h, const char* UNREF name) {
    Interpreter *interpreter = interpreter_table();

    interpreter->interpreter_st_get_frame = &interpreter_st_get_frame;
    interpreter->interpreter_st_get_trace = &interpreter_st_get_trace;
    interpreter->interpreter_enumerate_thread = &interpreter_enumerate_thread;

    interpreter->interpreter_get_last_frame = &interpreter_get_last_frame;
    interpreter->interpreter_get_prev_frame = &interpreter_get_prev_frame;
    interpreter->is_frame_in_native_frame = &is_frame_in_native_frame;
#ifdef _IPF_
    interpreter->interpreter_get_stacked_register_address = &interpreter_get_stacked_register_address;
#endif
    interpreter->interpreter_ti_getFrameLocation = &interpreter_ti_getFrameLocation;
    interpreter->interpreter_ti_getLocal32 = &interpreter_ti_getLocal32;
    interpreter->interpreter_ti_getLocal64 = &interpreter_ti_getLocal64;
    interpreter->interpreter_ti_getObject = &interpreter_ti_getObject;
    interpreter->interpreter_ti_getStackTrace = &interpreter_ti_getStackTrace;
    interpreter->interpreter_ti_get_frame_count = &interpreter_ti_get_frame_count;
    interpreter->interpreter_ti_setLocal32 = &interpreter_ti_setLocal32;
    interpreter->interpreter_ti_setLocal64 = &interpreter_ti_setLocal64;
    interpreter->interpreter_ti_setObject = &interpreter_ti_setObject;
    interpreter->interpreter_st_get_interrupted_method_native_bit = &interpreter_st_get_interrupted_method_native_bit;
    interpreter->interpreter_ti_set_notification_mode = &interpreter_ti_set_notification_mode;
    interpreter->interpreter_ti_set_breakpoint = &interpreter_ti_set_breakpoint;
    interpreter->interpreter_ti_clear_breakpoint = &interpreter_ti_clear_breakpoint;
    interpreter->interpreter_ti_notify_frame_pop = &interpreter_ti_notify_frame_pop;
    interpreter->interpreter_ti_pop_frame = &interpreter_ti_pop_frame;
    interpreter->stack_dump = &stack_dump;

#ifdef _WIN32
    if (!vm_get_boolean_property_value_with_default("vm.assert_dialog"))
    {
        disable_assert_dialogs();
    }
#endif
}

EXPORT Boolean JIT_supports_compressed_references(JIT_Handle UNREF jh) {
#if defined _IPF_ || defined _EM64T_
    return true;
#else
    return false;
#endif
}

EXPORT JIT_Result JIT_compile_method(JIT_Handle UNREF jh,Compile_Handle, Method_Handle, JIT_Flags) {
   fprintf(stderr, "interpreter: Compile a method\n");
   return JIT_FAILURE;
}

EXPORT JIT_Result JIT_compile_method_with_params(JIT_Handle UNREF jh,Compile_Handle, Method_Handle, OpenMethodExecutionParams) {
   fprintf(stderr, "interpreter: Compile a method\n");
   return JIT_FAILURE;
}

EXPORT void JIT_deinit(JIT_Handle UNREF h) {}

EXPORT void JIT_execute_method(JIT_Handle UNREF jh,jmethodID m, jvalue *return_value, jvalue *args) {
    Method *method = (Method*) m;
//    fprintf(stderr, "interpreter execute method\n");
    interpreter_execute_method(method, return_value, args);
}
