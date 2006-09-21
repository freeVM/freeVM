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
 * @author Intel, Pavel Afremov
 * @version $Revision: 1.1 $
 */


#define LOG_DOMAIN "exn"
#include "clog.h"

#include "Class.h"
#include "open/types.h"

#include "classloader.h"
#include "exceptions.h"
#include "exceptions_impl.h"
#include "environment.h"
#include "dump.h"
#include "heap.h"
#include "interpreter.h"
#include "jit_intf_cpp.h"
#include "lil.h"
#include "lil_code_generator.h"
#include "m2n.h"
#include "mon_enter_exit.h"
#include "stack_iterator.h"
#include "vm_stats.h"

#ifdef _IPF_
#elif defined _EM64T_
#include "../m2n_em64t_internal.h"
#else
#include "../m2n_ia32_internal.h"
#endif


////////////////////////////////////////////////////////////////////////////
// Target_Exception_Handler

Target_Exception_Handler::Target_Exception_Handler(NativeCodePtr start_ip,
    NativeCodePtr end_ip,
    NativeCodePtr handler_ip, Class_Handle exn_class, bool exn_is_dead)
{
    _start_ip = start_ip;
    _end_ip = end_ip;
    _handler_ip = handler_ip;
    _exc = exn_class;
    _exc_obj_is_dead = exn_is_dead;
}

NativeCodePtr Target_Exception_Handler::get_start_ip()
{
    return _start_ip;
}

NativeCodePtr Target_Exception_Handler::get_end_ip()
{
    return _end_ip;
}

NativeCodePtr Target_Exception_Handler::get_handler_ip()
{
    return _handler_ip;
}

Class_Handle Target_Exception_Handler::get_exc()
{
    return _exc;
}

bool Target_Exception_Handler::is_exc_obj_dead()
{
    return _exc_obj_is_dead;
}

#ifdef POINTER64
typedef uint64 NumericNativeCodePtr;
#else
typedef uint32 NumericNativeCodePtr;
#endif

bool Target_Exception_Handler::is_in_range(NativeCodePtr ip, bool is_ip_past)
{
    NumericNativeCodePtr nip = (NumericNativeCodePtr) ip;
    NumericNativeCodePtr sip = (NumericNativeCodePtr) _start_ip;
    NumericNativeCodePtr eip = (NumericNativeCodePtr) _end_ip;

    return (is_ip_past ? sip < nip && nip <= eip : sip <= nip && nip < eip);
}   //Target_Exception_Handler::is_in_range

bool Target_Exception_Handler::is_assignable(Class_Handle exn_class)
{
    if (!_exc)
        return true;
    Class_Handle e = exn_class;
    while (e)
        if (e == _exc)
            return true;
        else
            e = class_get_super_class(e);
    return false;
}   //Target_Exception_Handler::is_assignable

void Target_Exception_Handler::update_catch_range(NativeCodePtr new_start_ip,
    NativeCodePtr new_end_ip)
{
    _start_ip = new_start_ip;
    _end_ip = new_end_ip;
}   //Target_Exception_Handler::update_catch_range

void Target_Exception_Handler::
update_handler_address(NativeCodePtr new_handler_ip)
{
    _handler_ip = new_handler_ip;
}   //Target_Exception_Handler::update_handler_address

//////////////////////////////////////////////////////////////////////////
// Lazy Exception Utilities

// Note: Function runs from unwindable area
static ManagedObject *create_lazy_exception(
    Class_Handle exn_class,
    Method_Handle exn_constr,
    uint8 * exn_constr_args,
    jvalue* vm_exn_constr_args)
{
    assert(!hythread_is_suspend_enabled());

    bool unwindable = set_unwindable(false);
    ManagedObject* result;
    if (NULL == vm_exn_constr_args) {
        result = class_alloc_new_object_and_run_constructor(
            (Class*) exn_class, (Method*) exn_constr, exn_constr_args);
    } else {
        jthrowable exc_object = create_exception(
            (Class*) exn_class, (Method*) exn_constr, vm_exn_constr_args);
        result = exc_object->object;
    }
    set_unwindable(unwindable);
    exn_rethrow_if_pending();
    return result;
}   //create_object_lazily

//////////////////////////////////////////////////////////////////////////
// Main Exception Propogation Function

// This function propagates an exception to its handler.
// It can only be called for the current thread.
// The input stack iterator provides the starting point for propogation.  If the top frame is an M2nFrame, it is ignored.
// Let A be the current frame, let B be the most recent M2nFrame prior to A.
// The exception is propagated to the first managed frame between A and B that has a handler for the exception,
// or to the native code that managed frame immediately after B if no such managed frame exists.
// If exn_obj is nonnull then it is the exception, otherwise the exception is an instance of
// exn_class created using the given constructor and arguments (a null exn_constr indicates the default constructor).
// The stack iterator is mutated to represent the context that should be resumed.
// The client should either use si_transfer_control to resume it, or use an OS context mechanism
// copied from the final stack iterator.

static void exn_propagate_exception(
    StackIterator * si,
    ManagedObject ** exn_obj,
    Class_Handle exn_class,
    Method_Handle exn_constr,
    uint8 * jit_exn_constr_args,
    jvalue* vm_exn_constr_args)
{
    assert(!hythread_is_suspend_enabled());
    ASSERT_NO_INTERPRETER;
    assert(*exn_obj || exn_class);

    // Save the throw context
    StackIterator *throw_si = si_dup(si);

    // Determine the type of the exception for the type tests below.
    if (*exn_obj)
        exn_class = (*exn_obj)->vt()->clss;

#ifdef VM_STATS
    ((Class *) exn_class)->num_throws++;
    VM_Statistics::get_vm_stats().num_exceptions++;
#endif // VM_STATS

    // Skip first frame if it is an M2nFrame (which is always a transition from managed to the throw code).
    // The M2nFrame will be removed from the thread's M2nFrame list but transfer control or copy to registers.
    if (si_is_native(si)) {
        si_goto_previous(si);
    }

    Method *interrupted_method = NULL;
    NativeCodePtr interrupted_method_location = NULL;
    JIT *interrupted_method_jit = NULL;

    if (!si_is_native(si))
    {
        CodeChunkInfo *interrupted_cci = si_get_code_chunk_info(si);
        assert(interrupted_cci);
        interrupted_method = interrupted_cci->get_method();
        interrupted_method_location = si_get_ip(si);
        interrupted_method_jit = interrupted_cci->get_jit();
    }

    // Remove single step breakpoints which could have been set on the
    // exception bytecode
    DebugUtilsTI *ti = VM_Global_State::loader_env->TI;
    if (ti->isEnabled() && ti->is_single_step_enabled())
    {
        VM_thread *vm_thread = p_TLS_vmthread;
        if (NULL != vm_thread->ss_state)
        {
            LMAutoUnlock lock(&ti->brkpntlst_lock);
            jvmti_remove_single_step_breakpoints(ti, vm_thread);
        }
    }

    bool same_frame = true;
    while (!si_is_past_end(si) && !si_is_native(si)) {
        CodeChunkInfo *cci = si_get_code_chunk_info(si);
        assert(cci);
        Method *method = cci->get_method();
        JIT *jit = cci->get_jit();
        assert(method && jit);
        NativeCodePtr ip = si_get_ip(si);
        bool is_ip_past = !!si_get_jit_context(si)->is_ip_past;

#ifdef VM_STATS
        cci->num_throws++;
#endif // VM_STATS

        // Examine this frame's exception handlers looking for a match
        unsigned num_handlers = cci->get_num_target_exception_handlers();
        for (unsigned i = 0; i < num_handlers; i++) {
            Target_Exception_Handler_Ptr handler =
                cci->get_target_exception_handler_info(i);
            if (!handler)
                continue;
            if (handler->is_in_range(ip, is_ip_past)
                && handler->is_assignable(exn_class)) {
                // Found a handler that catches the exception.
#ifdef VM_STATS
                cci->num_catches++;
                if (same_frame)
                    VM_Statistics::get_vm_stats().num_exceptions_caught_same_frame++;
                if (handler->is_exc_obj_dead())
                    VM_Statistics::get_vm_stats().num_exceptions_dead_object++;
#endif // VM_STATS
                // Setup handler context
                jit->fix_handler_context(method, si_get_jit_context(si));
                si_set_ip(si, handler->get_handler_ip(), false);

                // Start single step in exception handler
                if (ti->isEnabled() && ti->is_single_step_enabled() &&
                    ti->getPhase() == JVMTI_PHASE_LIVE)
                {
                    VM_thread *vm_thread = p_TLS_vmthread;
                    if (NULL != vm_thread->ss_state)
                    {
                        LMAutoUnlock lock(&ti->brkpntlst_lock);

                        uint16 bc;
                        OpenExeJpdaError UNREF result =
                            jit->get_bc_location_for_native(
                                method, handler->get_handler_ip(), &bc);
                        assert(EXE_ERROR_NONE == result);

                        jvmti_StepLocation method_start = {(Method *)method, bc};

                        jvmtiError UNREF errorCode =
                            jvmti_set_single_step_breakpoints(ti, vm_thread,
                                &method_start, 1);
                        assert(JVMTI_ERROR_NONE == errorCode);
                    }
                }

                // Create exception if necessary
                if (!*exn_obj) {
                    if (handler->is_exc_obj_dead()) {
#ifdef VM_STATS
                        VM_Statistics::get_vm_stats().num_exceptions_object_not_created++;
#endif // VM_STATS
                    }
                    else {
                        *exn_obj =
                            create_lazy_exception(exn_class, exn_constr,
                                jit_exn_constr_args, vm_exn_constr_args);
                    }
                }

                BEGIN_RAISE_AREA;
                // Reload exception object pointer because it could have
                // moved while calling JVMTI callback
                *exn_obj = jvmti_jit_exception_event_callback_call(*exn_obj,
                    interrupted_method_jit, interrupted_method,
                    interrupted_method_location,
                    jit, method, handler->get_handler_ip());
                END_RAISE_AREA;

                TRACE2("exn", ("setting return pointer to %d", exn_obj));

                si_set_return_pointer(si, (void **) exn_obj);
                si_free(throw_si);
                return;
            }
        }

        // No appropriate handler found, undo synchronization
        if (method->is_synchronized()) {
            bool unwindable = set_unwindable(false);
            if (method->is_static()) {
                assert(!hythread_is_suspend_enabled());
                TRACE2("tm.locks", ("unlock staic sync methods... %x",  exn_obj));
                vm_monitor_exit(struct_Class_to_java_lang_Class(method->
                        get_class()));
            }
            else {
                void **p_this =
                    (void **) jit->get_address_of_this(method,
                    si_get_jit_context(si));
                TRACE2("tm.locks", ("unlock sync methods...%x" , *p_this));
                vm_monitor_exit((ManagedObject *) * p_this);
            }
            exn_clear();
            set_unwindable(unwindable);
        }

        jvalue ret_val = {(jlong)0};
        jvmti_process_method_exit_event(reinterpret_cast<jmethodID>(method),
            JNI_TRUE, ret_val);

        // Goto previous frame
        si_goto_previous(si);
        same_frame = false;
    }

    // Exception propagates to the native code

    // The current thread exception is set to the exception and we return 0/NULL to the native code
    if (*exn_obj == NULL) {
        *exn_obj =
            create_lazy_exception(exn_class, exn_constr,
                jit_exn_constr_args, vm_exn_constr_args);
    }
    assert(!hythread_is_suspend_enabled());

    CodeChunkInfo *catch_cci = si_get_code_chunk_info(si);
    Method *catch_method = NULL;
    if (catch_cci)
        catch_method = catch_cci->get_method();

    // Reload exception object pointer because it could have
    // moved while calling JVMTI callback
    *exn_obj = jvmti_jit_exception_event_callback_call(*exn_obj,
        interrupted_method_jit, interrupted_method, interrupted_method_location,
        NULL, NULL, NULL);

    set_exception_object_internal(*exn_obj);

    *exn_obj = NULL;
    si_set_return_pointer(si, (void **) exn_obj);
    si_free(throw_si);
}   //exn_propagate_exception

#ifndef _IPF_
// Alexei
// Check if we could proceed with destructive stack unwinding,
// i. e. the last GC frame is created before the last m2n frame.
// We use here a knowledge that the newer stack objects
// have smaller addresses for ia32 and em64t architectures.
static bool UNUSED is_gc_frame_before_m2n_frame()
{
    if (p_TLS_vmthread->gc_frames) {
        POINTER_SIZE_INT m2n_address =
            (POINTER_SIZE_INT) m2n_get_last_frame();
        POINTER_SIZE_INT gc_frame_address =
            (POINTER_SIZE_INT) p_TLS_vmthread->gc_frames;
        // gc frame is created before the last m2n frame
        return m2n_address < gc_frame_address;
    }
    else {
        return true;    // no gc frames - nothing to be broken
    }
}
#endif // _IPF_

void exn_throw_for_JIT(ManagedObject* exn_obj, Class_Handle exn_class,
    Method_Handle exn_constr, uint8* jit_exn_constr_args, jvalue* vm_exn_constr_args)
{
/*
 * !!!! NO LOGGER IS ALLOWED IN THIS FUNCTION !!!
 * !!!! RELEASE BUILD WILL BE BROKEN          !!!
 * !!!! NO TRACE2, INFO, WARN, ECHO, ASSERT, ...
 */
    assert(!hythread_is_suspend_enabled());
    ASSERT_NO_INTERPRETER

    if ((exn_obj == NULL) && (exn_class == NULL)) {
        exn_class = VM_Global_State::loader_env->java_lang_NullPointerException_Class;
    }
    ManagedObject* local_exn_obj = exn_obj;
    StackIterator* si = si_create_from_native();

#ifndef _IPF_
    assert(is_gc_frame_before_m2n_frame());
#endif // _IPF_

    if (si_is_past_end(si)) {
        //FIXME LAZY EXCEPTION (2006.05.12)
        // should be replaced by lazy version
        set_exception_object_internal(local_exn_obj);
        return;
    }

    si_transfer_all_preserved_registers(si);
    exn_propagate_exception(si, &local_exn_obj, exn_class, exn_constr,
        jit_exn_constr_args, vm_exn_constr_args);
    si_transfer_control(si);
}   //exn_throw_for_JIT

// Throw an exception in the current thread.
// Must be called with an M2nFrame on the top of the stack, and throws to the previous managed
// frames or the previous M2nFrame.
// Exception defined as in previous function.
// Does not return.

void exn_athrow(ManagedObject* exn_obj, Class_Handle exn_class,
    Method_Handle exn_constr, uint8* exn_constr_args)
{
    exn_throw_for_JIT(exn_obj, exn_class, exn_constr, exn_constr_args, NULL);
}


// Throw an exception in the current thread.
// Must be called with the current thread "suspended" in managed code and regs holds the suspended values.
// Exception defined as in previous two functions.
// Mutates the regs value, which should be used to "resume" the managed code.

void exn_athrow_regs(Registers * regs, Class_Handle exn_class)
{
    assert(exn_class);
#ifndef _IPF_
    M2nFrame *m2nf = m2n_push_suspended_frame(regs);
    StackIterator *si = si_create_from_native();
    ManagedObject *local_exn_obj = NULL;
    exn_propagate_exception(si, &local_exn_obj, exn_class, NULL, NULL, NULL);
    si_copy_to_registers(si, regs);
    si_free(si);
    STD_FREE(m2nf);
#endif
}   //exn_athrow_regs

//////////////////////////////////////////////////////////////////////////
// Exception Catch support

// exception catch callback to restore stack after Stack Overflow Error
void exception_catch_callback() {
    if (p_TLS_vmthread->restore_guard_page) {
#ifndef _EM64T_
        set_guard_stack();
#endif
    }
}

// exception catch support for JVMTI, also restore stack after Stack Overflow Error
void jvmti_exception_catch_callback(Registers* regs) {
    if (p_TLS_vmthread->restore_guard_page) {
#ifndef _EM64T_
        set_guard_stack();
#endif
    }

    M2nFrame *m2nf = m2n_push_suspended_frame(regs);

    printf("jvmti_exception_catch_callback\n");
    st_print();

    StackIterator *si = si_create_from_native();

    if (si_is_native(si)) {
        si_goto_previous(si);
    }

    if (!si_is_native(si))
    {
        CodeChunkInfo* catch_cci = si_get_code_chunk_info(si);
        assert(catch_cci);
        Method* catch_method = catch_cci->get_method();
        NativeCodePtr catch_method_location = si_get_ip(si);
        JIT* catch_method_jit = catch_cci->get_jit();
#ifndef _EM64T_
        //
        // FIXME: implement si_get_return_pointer() for EM64T
        //
        ManagedObject** exn_obj = (ManagedObject**) si_get_return_pointer(si);
        *exn_obj = jvmti_jit_exception_catch_event_callback_call( *exn_obj,
                catch_method_jit, catch_method, catch_method_location);
#else
        assert(0);
#endif
    }

    m2n_set_last_frame(m2n_get_previous_frame(m2nf));
    STD_FREE(m2nf);
}

//////////////////////////////////////////////////////////////////////////
// Runtime Exception Support

// rt_throw takes an exception and throws it
NativeCodePtr exn_get_rth_throw()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed:ref:void;"
        "push_m2n 0, 0;"
        "m2n_save_all;" "out platform:ref,pint,pint,pint:void;");
    assert(cs);

    if (VM_Global_State::loader_env->compress_references)
        cs = lil_parse_onto_end(cs,
            "jc i0=%0i:ref,%n;"
            "o0=i0;" "j %o;" ":%g;" "o0=0:ref;" ":%g;", Class::heap_base);
    else
        cs = lil_parse_onto_end(cs, "o0=i0;");
    assert(cs);

    lil_parse_onto_end(cs,
        "o1=0;" "o2=0;" "o3=0;" "call.noret %0i;", exn_athrow);
    assert(cs);

    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw


static void rth_throw_lazy(Method * exn_constr)
{
#if defined(_IPF_) || defined(_EM64T_)
    ABORT("Lazy exceptions are not supported on this platform");
#else
    uint8 *args = (uint8 *) (m2n_get_args(m2n_get_last_frame()) + 1);   // +1 to skip constructor
    args += exn_constr->get_num_arg_bytes() - 4;
    exn_athrow(NULL, *(Class_Handle *) args, exn_constr, args);
#endif
}   //rth_throw_lazy


// rt_throw_lazy takes a constructor, the class for that constructor, and arguments for that constructor.
// it throws a (lazily created) instance of that class using that constructor and arguments.
NativeCodePtr exn_get_rth_throw_lazy()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed:pint:void;"
        "push_m2n 0, 0;"
        "m2n_save_all;" "in2out platform:void;" "call.noret %0i;",
        rth_throw_lazy);
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_lazy", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw_lazy


// rt_throw_lazy_trampoline takes an exception class as first standard place
// and throws a (lazily created) instance of that class using the default constructor
NativeCodePtr exn_get_rth_throw_lazy_trampoline()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 1:managed::void;"
        "push_m2n 0, 0;"
        "m2n_save_all;"
        "out platform:ref,pint,pint,pint:void;"
        "o0=0:ref;" "o1=sp0;" "o2=0;" "o3=0;" "call.noret %0i;",
        exn_athrow);
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_lazy_trampoline", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw_lazy_trampoline


// rth_throw_null_pointer throws a null pointer exception (lazily)
NativeCodePtr exn_get_rth_throw_null_pointer()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    Class *exn_clss =
        VM_Global_State::loader_env->java_lang_NullPointerException_Class;
    LilCodeStub *cs =
        lil_parse_code_stub("entry 0:managed::void;" "std_places 1;"
        "sp0=%0i;" "tailcall %1i;",
        exn_clss,
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_null_pointer", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_null_pointer

// Return the type of illegal monitor state exception
Class_Handle exn_get_illegal_monitor_state_exception_type()
{
    static Class *exn_clss = NULL;

    if (exn_clss != NULL) {
        return exn_clss;
    }

    Global_Env *env = VM_Global_State::loader_env;
    String *exc_str =
        env->string_pool.lookup("java/lang/IllegalMonitorStateException");
    exn_clss =
        env->bootstrap_class_loader->LoadVerifyAndPrepareClass(env, exc_str);
    assert(exn_clss);

    return exn_clss;
}

// rth_throw_illegal_monitor_state throws an java.lang.IllegalMonitorStateException (lazily)
NativeCodePtr exn_get_rth_throw_illegal_monitor_state() {
    static NativeCodePtr addr = NULL;

    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_get_illegal_monitor_state_exception_type(),
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline())
    );
    assert(lil_is_valid(cs));

    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_illegal_monitor_state", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_null_pointer


// rth_throw_array_index_out_of_bounds throws an array index out of bounds exception (lazily)
NativeCodePtr exn_get_rth_throw_array_index_out_of_bounds()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    Global_Env *env = VM_Global_State::loader_env;
    Class *exn_clss = env->java_lang_ArrayIndexOutOfBoundsException_Class;
    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_clss,
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_array_index_out_of_bounds", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw_array_index_out_of_bounds


// Return the type of negative array size exception
Class_Handle exn_get_negative_array_size_exception_type()
{
    assert(hythread_is_suspend_enabled());
    Class *exn_clss;


    Global_Env *env = VM_Global_State::loader_env;
    String *exc_str =
        env->string_pool.lookup("java/lang/NegativeArraySizeException");
    exn_clss =
        env->bootstrap_class_loader->LoadVerifyAndPrepareClass(env, exc_str);
    assert(exn_clss);


    return exn_clss;
}

// rth_throw_negative_array_size throws a negative array size exception (lazily)
NativeCodePtr exn_get_rth_throw_negative_array_size()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_get_negative_array_size_exception_type(),
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_negative_array_size", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_negative_array_size


// Return the type of illegal state exception
Class_Handle exn_get_illegal_state_exception_type()
{
    assert(hythread_is_suspend_enabled());
    Class *exn_clss;


    Global_Env *env = VM_Global_State::loader_env;
    String *exc_str =
        env->string_pool.lookup("java/lang/IllegalMonitorStateException");
    exn_clss =
        env->bootstrap_class_loader->LoadVerifyAndPrepareClass(env, exc_str);
    assert(exn_clss);

    return exn_clss;
}

// rth_throw_negative_array_size throws a negative array size exception (lazily)
NativeCodePtr exn_get_rth_throw_illegal_state_exception()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_get_illegal_state_exception_type(),
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_illegal_state_exception", lil_cs_get_code_size(cs));
    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_illegal_state_exception


// rth_throw_array_store throws an array store exception (lazily)
NativeCodePtr exn_get_rth_throw_array_store()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    Global_Env *env = VM_Global_State::loader_env;
    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        env->java_lang_ArrayStoreException_Class,
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_array_store", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_array_store


// rth_throw_arithmetic throws an arithmetic exception (lazily)
NativeCodePtr exn_get_rth_throw_arithmetic()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    Global_Env *env = VM_Global_State::loader_env;
    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        env->java_lang_ArithmeticException_Class,
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_arithmetic", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);

    return addr;
}   //exn_get_rth_throw_arithmetic


// Return the type of class cast exception
Class_Handle exn_get_class_cast_exception_type()
{
    assert(hythread_is_suspend_enabled());
    Class *exn_clss;

    Global_Env *env = VM_Global_State::loader_env;
    String *exc_str = env->string_pool.lookup("java/lang/ClassCastException");
    exn_clss =
        env->bootstrap_class_loader->LoadVerifyAndPrepareClass(env, exc_str);
    assert(exn_clss);

    return exn_clss;
}

// rth_throw_class_cast_exception throws a class cast exception (lazily)
NativeCodePtr exn_get_rth_throw_class_cast_exception()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_get_class_cast_exception_type(),
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(cs && lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_class_cast_exception", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw_class_cast_exception


// Return the type of incompatible class change exception
Class_Handle exn_get_incompatible_class_change_exception_type()
{
    assert(hythread_is_suspend_enabled());
    Class *exn_clss;

    Global_Env *env = VM_Global_State::loader_env;
    String *exc_str =
        env->string_pool.lookup("java/lang/IncompatibleClassChangeError");
    exn_clss =
        env->bootstrap_class_loader->LoadVerifyAndPrepareClass(env, exc_str);
    assert(exn_clss);


    return exn_clss;
}

// rth_throw_incompatible_class_change_exception throws an incompatible class change exception (lazily)
NativeCodePtr exn_get_rth_throw_incompatible_class_change_exception()
{
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    LilCodeStub *cs = lil_parse_code_stub("entry 0:managed::void;"
        "std_places 1;" "sp0=%0i;" "tailcall %1i;",
        exn_get_incompatible_class_change_exception_type(),
        lil_npc_to_fp(exn_get_rth_throw_lazy_trampoline()));
    assert(cs && lil_is_valid(cs));
    addr = LilCodeGenerator::get_platform()->compile(cs);

    DUMP_STUB(addr, "rth_throw_incompatible_class_change_exception", lil_cs_get_code_size(cs));

    lil_free_code_stub(cs);
    return addr;
}   //exn_get_rth_throw_incompatible_class_change_exception

