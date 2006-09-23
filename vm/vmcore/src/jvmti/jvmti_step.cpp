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
 * @author Pavel Rebriy
 * @version $Revision: $
 */

#include "jvmti.h"
#include "Class.h"
#include "cxxlog.h"
#include "jvmti_utils.h"
#include "jvmti_internal.h"
#include "jit_intf_cpp.h"
#include "stack_iterator.h"
#include "interpreter.h"
#include "method_lookup.h"
#include "open/bytecodes.h"
#include "open/jthread.h"
#include "jvmti_break_intf.h"

static inline short
jvmti_GetHalfWordValue( const unsigned char *bytecode,
                        unsigned location)
{
    short result = (short)( (bytecode[location] << 8)|(bytecode[location + 1]) );
    return result;
} // jvmti_GetHalfWordValue

static inline int
jvmti_GetWordValue( const unsigned char *bytecode,
                    unsigned location)
{
    int result = (int)( (bytecode[location    ] << 24)|(bytecode[location + 1] << 16)
                       |(bytecode[location + 2] << 8) |(bytecode[location + 3]      ) );
    return result;
} // jvmti_GetWordValue

static Method *
jvmti_get_invoked_virtual_method( VM_thread* thread )
{
    ASSERT_NO_INTERPRETER;

#if _IA32_
    // create stack iterator from native
    StackIterator* si = si_create_from_native( thread );
    si_transfer_all_preserved_registers(si);
    assert(si_is_native(si));
    // get java frame
    si_goto_previous(si);
    assert(!si_is_native(si));
    // find correct ip in java frame
    NativeCodePtr ip = si_get_ip(si);
    // get virtual table
    VTable* vtable;
    JitFrameContext* jitContext = si_get_jit_context(si);
    unsigned short code = (*((unsigned short*)((char*)ip)));
    switch( code )
    {
    case 0x50ff:
        vtable = (VTable*)*(jitContext->p_eax);
        break;
    case 0x51ff:
        vtable = (VTable*)*(jitContext->p_ecx);
        break;
    case 0x52ff:
        vtable = (VTable*)*(jitContext->p_edx);
        break;
    case 0x53ff:
        vtable = (VTable*)*(jitContext->p_ebx);
        break;
    default:
        vtable = NULL;
    }
    assert(vtable);
    si_free(si);

    // get method from virtual table
    Method *method = class_get_method_from_vt_offset( vtable, *((char*)ip + 2) );
    return method;

#else // for !_IA32_

    return NULL;
#endif // _IA32_
} // jvmti_get_invoked_virtual_method

void
jvmti_SingleStepLocation( VM_thread* thread,
                          Method *method,
                          unsigned bytecode_index,
                          jvmti_StepLocation **next_step,
                          unsigned *count)
{
    assert(next_step);
    assert(count);
    ASSERT_NO_INTERPRETER;

    // get method bytecode array and code length
    const unsigned char *bytecode = method->get_byte_code_addr();
    unsigned len = method->get_byte_code_size();
    unsigned location = bytecode_index;
    assert(location < len);

    // initialize step location count
    *count = 0;

    // parse bytecode
    jvmtiError error;
    bool is_wide = false;
    int offset;
    do {

        switch( bytecode[location] )
        {
        // wide instruction
        case OPCODE_WIDE:           /* 0xc4 */
            assert( !is_wide );
            location++;
            is_wide = true;
            continue;

        // if instructions
        case OPCODE_IFEQ:           /* 0x99 + s2 */
        case OPCODE_IFNE:           /* 0x9a + s2 */
        case OPCODE_IFLT:           /* 0x9b + s2 */
        case OPCODE_IFGE:           /* 0x9c + s2 */
        case OPCODE_IFGT:           /* 0x9d + s2 */
        case OPCODE_IFLE:           /* 0x9e + s2 */

        case OPCODE_IF_ICMPEQ:      /* 0x9f + s2 */
        case OPCODE_IF_ICMPNE:      /* 0xa0 + s2 */
        case OPCODE_IF_ICMPLT:      /* 0xa1 + s2 */
        case OPCODE_IF_ICMPGE:      /* 0xa2 + s2 */
        case OPCODE_IF_ICMPGT:      /* 0xa3 + s2 */
        case OPCODE_IF_ICMPLE:      /* 0xa4 + s2 */

        case OPCODE_IF_ACMPEQ:      /* 0xa5 + s2 */
        case OPCODE_IF_ACMPNE:      /* 0xa6 + s2 */

        case OPCODE_IFNULL:         /* 0xc6 + s2 */
        case OPCODE_IFNONNULL:      /* 0xc7 + s2 */
            assert( !is_wide );
            offset = (int)location + jvmti_GetHalfWordValue( bytecode, location + 1 );
            location += 3;
            *count = 2;
            error = _allocate( sizeof(jvmti_StepLocation) * 2, (unsigned char**)next_step );
            assert( error == JVMTI_ERROR_NONE );
            (*next_step)[0].method = method;
            (*next_step)[0].location = location;
            (*next_step)[0].native_location = NULL;
            (*next_step)[1].method = method;
            (*next_step)[1].location = offset;
            (*next_step)[1].native_location = NULL;
            break;

        // goto instructions
        case OPCODE_GOTO:           /* 0xa7 + s2 */
        case OPCODE_JSR:            /* 0xa8 + s2 */
            assert( !is_wide );
            offset = (int)location + jvmti_GetHalfWordValue( bytecode, location + 1 );
            *count = 1;
            error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
            assert( error == JVMTI_ERROR_NONE );
            (*next_step)->method = method;
            (*next_step)->location = offset;
            (*next_step)->native_location = NULL;
            break;
        case OPCODE_GOTO_W:         /* 0xc8 + s4 */
        case OPCODE_JSR_W:          /* 0xc9 + s4 */
            assert( !is_wide );
            offset = (int)location + jvmti_GetWordValue( bytecode, location + 1 );
            *count = 1;
            error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
            assert( error == JVMTI_ERROR_NONE );
            (*next_step)->method = method;
            (*next_step)->location = offset;
            (*next_step)->native_location = NULL;
            break;

        // tableswitch instruction
        case OPCODE_TABLESWITCH:    /* 0xaa + pad + s4 * (3 + N) */
            assert( !is_wide );
            location = (location + 4)&(~0x3U);
            {
                int low = jvmti_GetWordValue( bytecode, location + 4 );
                int high = jvmti_GetWordValue( bytecode, location + 8 );
                int number = high - low + 2;

                *count = number;
                error = _allocate( sizeof(jvmti_StepLocation) * number, (unsigned char**)next_step );
                assert( error == JVMTI_ERROR_NONE );
                (*next_step)[0].method = method;
                (*next_step)[0].location = (int)bytecode_index
                    + jvmti_GetWordValue( bytecode, location );
                (*next_step)[0].native_location = NULL;
                location += 12;
                for( int index = 1; index < number; index++, location += 4 ) {
                    (*next_step)[index].method = method;
                    (*next_step)[index].location = (int)bytecode_index
                        + jvmti_GetWordValue( bytecode, location );
                    (*next_step)[index].native_location = NULL;
                }
            }
            break;

        // lookupswitch instruction
        case OPCODE_LOOKUPSWITCH:   /* 0xab + pad + s4 * 2 * (N + 1) */
            assert( !is_wide );
            location = (location + 4)&(~0x3U);
            {
                int number = jvmti_GetWordValue( bytecode, location + 4 ) + 1;

                *count = number;
                error = _allocate( sizeof(jvmti_StepLocation) * number, (unsigned char**)next_step );
                assert( error == JVMTI_ERROR_NONE );
                (*next_step)[0].method = method;
                (*next_step)[0].location = (int)bytecode_index
                    + jvmti_GetWordValue( bytecode, location );
                (*next_step)[0].native_location = NULL;
                location += 12;
                for( int index = 1; index < number; index++, location += 8 ) {
                    (*next_step)[index].method = method;
                    (*next_step)[index].location = (int)
                        + jvmti_GetWordValue( bytecode, location );
                    (*next_step)[index].native_location = NULL;
                }
            }
            break;

        // athrow and invokeinterface instruction
        case OPCODE_ATHROW:         /* 0xbf */
        case OPCODE_INVOKEINTERFACE:/* 0xb9 + u2 + u1 + u1 */
            assert( !is_wide );
            // instructions are processed in helpers
            break;

        // return instructions
        case OPCODE_IRETURN:        /* 0xac */
        case OPCODE_LRETURN:        /* 0xad */
        case OPCODE_FRETURN:        /* 0xae */
        case OPCODE_DRETURN:        /* 0xaf */
        case OPCODE_ARETURN:        /* 0xb0 */
        case OPCODE_RETURN:         /* 0xb1 */
            assert( !is_wide );
            {
                error = jvmti_get_next_bytecodes_from_native( 
                    thread, next_step, count, true );
                assert( error == JVMTI_ERROR_NONE );
            }
            break;

        // invokes instruction
        case OPCODE_INVOKESPECIAL:  /* 0xb7 + u2 */
        case OPCODE_INVOKESTATIC:   /* 0xb8 + u2 */
            assert( !is_wide );
            {
                unsigned short index = jvmti_GetHalfWordValue( bytecode, location + 1 );
                Class *klass = method_get_class( method );
                assert( cp_is_resolved(klass->const_pool, index) );

                if( !method_is_native( klass->const_pool[index].CONSTANT_ref.method ) ) {
                    *count = 1;
                    error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
                    assert( error == JVMTI_ERROR_NONE );
                    (*next_step)->method = klass->const_pool[index].CONSTANT_ref.method;
                    (*next_step)->location = 0;
                    (*next_step)->native_location = NULL;
                }
            }
            break;

        // invokevirtual instruction
        case OPCODE_INVOKEVIRTUAL:  /* 0xb6 + u2 */
            assert( !is_wide );
            {
                Method *func = jvmti_get_invoked_virtual_method( thread );
                if( !method_is_native(func) ) {
                    *count = 1;
                    error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
                    assert( error == JVMTI_ERROR_NONE );
                    (*next_step)->method = func;
                    (*next_step)->location = 0;
                    (*next_step)->native_location = NULL;
                }
            }
            break;

        case OPCODE_MULTIANEWARRAY: /* 0xc5 + u2 + u1 */
            assert( !is_wide );
            location++;

        case OPCODE_IINC:           /* 0x84 + u1|u2 + s1|s2 */
            if( is_wide ) {
                location += 2;
                is_wide = false;
            }

        case OPCODE_SIPUSH:         /* 0x11 + s2 */
        case OPCODE_LDC_W:          /* 0x13 + u2 */
        case OPCODE_LDC2_W:         /* 0x14 + u2 */
        case OPCODE_GETSTATIC:      /* 0xb2 + u2 */
        case OPCODE_PUTSTATIC:      /* 0xb3 + u2 */
        case OPCODE_GETFIELD:       /* 0xb4 + u2 */
        case OPCODE_PUTFIELD:       /* 0xb5 + u2 */
        case OPCODE_NEW:            /* 0xbb + u2 */
        case OPCODE_ANEWARRAY:      /* 0xbd + u2 */
        case OPCODE_CHECKCAST:      /* 0xc0 + u2 */
        case OPCODE_INSTANCEOF:     /* 0xc1 + u2 */
            assert( !is_wide );
            location++;

        case OPCODE_ILOAD:          /* 0x15 + u1|u2 */
        case OPCODE_LLOAD:          /* 0x16 + u1|u2 */
        case OPCODE_FLOAD:          /* 0x17 + u1|u2 */
        case OPCODE_DLOAD:          /* 0x18 + u1|u2 */
        case OPCODE_ALOAD:          /* 0x19 + u1|u2 */
        case OPCODE_ISTORE:         /* 0x36 + u1|u2 */
        case OPCODE_LSTORE:         /* 0x37 + u1|u2 */
        case OPCODE_FSTORE:         /* 0x38 + u1|u2 */
        case OPCODE_DSTORE:         /* 0x39 + u1|u2 */
        case OPCODE_ASTORE:         /* 0x3a + u1|u2 */
            if( is_wide ) {
                location++;
                is_wide = false;
            }

        case OPCODE_BIPUSH:         /* 0x10 + s1 */
        case OPCODE_LDC:            /* 0x12 + u1 */
        case OPCODE_NEWARRAY:       /* 0xbc + u1 */
            assert( !is_wide );
            location++;

        default:
            assert( !is_wide );
            assert( bytecode[bytecode_index] < OPCODE_COUNT );
            assert( bytecode[bytecode_index] != _OPCODE_UNDEFINED );

            location++;
            *count = 1;
            error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
            assert( error == JVMTI_ERROR_NONE );
            (*next_step)->method = method;
            (*next_step)->location = location;
            (*next_step)->native_location = NULL;
            break;

        // ret instruction
        case OPCODE_RET:            /* 0xa9 + u1|u2  */
            // FIXME - need to obtain return address from stack.
            DIE2("jvmti", "SingleStepLocation: not implemented ret instruction");
            break;
        }
        break;
    } while( true );

    for( unsigned index = 0; index < *count; index++ ) {
        TRACE2( "jvmti.break.ss", "Step: " << class_get_name(method_get_class(method))
            << "." << method_get_name(method) << method_get_descriptor(method)
            << " :" << bytecode_index << "\n      -> "
            << class_get_name(method_get_class((*next_step)[index].method))
            << "." << method_get_name((*next_step)[index].method)
            << method_get_descriptor((*next_step)[index].method)
            << " :" << (*next_step)[index].location )
    }

    return;
} // jvmti_SingleStepLocation

static void
jvmti_setup_jit_single_step(DebugUtilsTI *ti, VMBreakInterface* intf,
                            Method* m, jlocation location)
{
    VM_thread* vm_thread = p_TLS_vmthread;
    jvmti_StepLocation *locations;
    unsigned locations_count;

    jvmti_SingleStepLocation(vm_thread, m, (unsigned)location,
                            &locations, &locations_count);

    jvmti_remove_single_step_breakpoints(ti, vm_thread);

    jvmti_set_single_step_breakpoints(ti, vm_thread, locations, locations_count);
}

// Callback function for JVMTI single step processing
static bool jvmti_process_jit_single_step_event(VMBreakInterface* intf, VMBreakPointRef* bp_ref)
{
    VMBreakPoint* bp = bp_ref->brpt;
    assert(bp);

    TRACE2("jvmti.break.ss", "SingleStep occured: "
        << class_get_name(method_get_class((Method*)bp->method)) << "."
        << method_get_name((Method*)bp->method)
        << method_get_descriptor((Method*)bp->method)
        << " :" << bp->location << " :" << bp->addr);

    DebugUtilsTI *ti = VM_Global_State::loader_env->TI;
    if (!ti->isEnabled() || ti->getPhase() != JVMTI_PHASE_LIVE)
        return false;

    JVMTISingleStepState* sss = p_TLS_vmthread->ss_state;

    if (!sss || !ti->is_single_step_enabled())
        return false;

    jlocation location = bp->location;
    jmethodID method = bp->method;
    Method* m = (Method*)method;
    NativeCodePtr addr = bp->addr;
    assert(addr);
    assert(bp_ref->data == NULL);
    
    hythread_t h_thread = hythread_self();
    jthread j_thread = jthread_get_java_thread(h_thread);
    ObjectHandle hThread = oh_allocate_local_handle();
    hThread->object = (Java_java_lang_Thread *)j_thread->object;
    tmn_suspend_enable();

    JNIEnv *jni_env = (JNIEnv *)jni_native_intf;
    TIEnv *env = ti->getEnvironments();
    TIEnv *next_env;

    while (NULL != env)
    {
        next_env = env->next;

        jvmtiEventSingleStep func =
            (jvmtiEventSingleStep)env->get_event_callback(JVMTI_EVENT_SINGLE_STEP);

        if (NULL != func)
        {
            if (env->global_events[JVMTI_EVENT_SINGLE_STEP - JVMTI_MIN_EVENT_TYPE_VAL])
            {
                TRACE2("jvmti.break.ss",
                    "Calling JIT global SingleStep breakpoint callback: "
                    << class_get_name(method_get_class((Method*)method)) << "."
                    << method_get_name((Method*)method)
                    << method_get_descriptor((Method*)method)
                    << " :" << location << " :" << addr);
                // fire global event
                intf->unlock();
                func((jvmtiEnv*)env, jni_env, (jthread)hThread, method, location);
                intf->lock();
                TRACE2("jvmti.break.ss",
                    "Finished JIT global SingleStep breakpoint callback: "
                    << class_get_name(method_get_class((Method*)method)) << "."
                    << method_get_name((Method*)method)
                    << method_get_descriptor((Method*)method)
                    << " :" << location << " :" << addr);

                env = next_env;
                continue;
            }

            TIEventThread* next_et;
            bool found = false;
            // fire local events
            for (TIEventThread* et = env->event_threads[JVMTI_EVENT_SINGLE_STEP - JVMTI_MIN_EVENT_TYPE_VAL];
                 et != NULL; et = next_et)
            {
                next_et = et->next;

                if (et->thread == hythread_self())
                {
                    TRACE2("jvmti.break.ss",
                        "Calling JIT local SingleStep breakpoint callback: "
                        << class_get_name(method_get_class((Method*)method)) << "."
                        << method_get_name((Method*)method)
                        << method_get_descriptor((Method*)method)
                        << " :" << location << " :" << addr);
                    found = true;
                    intf->unlock();
                    func((jvmtiEnv*)env, jni_env,
                        (jthread)hThread, method, location);
                    intf->lock();
                    TRACE2("jvmti.break.ss",
                        "Finished JIT local SingleStep breakpoint callback: "
                        << class_get_name(method_get_class((Method*)method)) << "."
                        << method_get_name((Method*)method)
                        << method_get_descriptor((Method*)method)
                        << " :" << location << " :" << addr);
                }
            }

            env = next_env;
        }
    }

    // Set breakpoints on bytecodes after the current one
    if (ti->is_single_step_enabled())
        jvmti_setup_jit_single_step(ti, intf, m, location);

    tmn_suspend_disable();
    oh_discard_local_handle(hThread);

    return true;
}

void jvmti_set_single_step_breakpoints(DebugUtilsTI *ti, VM_thread *vm_thread,
    jvmti_StepLocation *locations, unsigned locations_number)
{
    // Function is always executed under global TI breakpoints lock
    ASSERT_NO_INTERPRETER;

    JVMTISingleStepState *ss_state = vm_thread->ss_state;

    if (NULL == ss_state->predicted_breakpoints)
    {
        // Create SS breakpoints list
        // Single Step must be processed earlier then Breakpoints
        ss_state->predicted_breakpoints =
            ti->vm_brpt->new_intf(jvmti_process_jit_single_step_event,
                PRIORITY_SINGLE_STEP_BREAKPOINT, false);
        assert(ss_state->predicted_breakpoints);
    }

    for (unsigned iii = 0; iii < locations_number; iii++)
    {
        TRACE2("jvmti.break.ss", "Set single step breakpoint: "
            << class_get_name(method_get_class(locations[iii].method)) << "."
            << method_get_name(locations[iii].method)
            << method_get_descriptor(locations[iii].method)
            << " :" << locations[iii].location
            << " :" << locations[iii].native_location);

        VMBreakPointRef* ref =
            ss_state->predicted_breakpoints->add((jmethodID)locations[iii].method,
                                                  locations[iii].location,
                                                  locations[iii].native_location,
                                                  NULL);
        assert(ref);
    }
}

void jvmti_remove_single_step_breakpoints(DebugUtilsTI *ti, VM_thread *vm_thread)
{
    // Function is always executed under global TI breakpoints lock
    JVMTISingleStepState *ss_state = vm_thread->ss_state;

    TRACE2("jvmti.break.ss", "Remove single step breakpoints");

    if (ss_state && ss_state->predicted_breakpoints)
        ss_state->predicted_breakpoints->remove_all();
}

jvmtiError jvmti_get_next_bytecodes_from_native(VM_thread *thread,
    jvmti_StepLocation **next_step,
    unsigned *count,
    bool stack_step_up)
{
    ASSERT_NO_INTERPRETER;

    *count = 0;
    // create stack iterator, current stack frame should be native
    StackIterator *si = si_create_from_native(thread);
    si_transfer_all_preserved_registers(si);
    assert(si_is_native(si));
    // get previous stack frame, it should be java frame
    si_goto_previous(si);

    if (si_is_past_end(si))
    {
        si_free(si);
        return JVMTI_ERROR_NONE;
    }

    assert(!si_is_native(si));
    if( stack_step_up ) {
        // get previous stack frame
        si_goto_previous(si);
    }
    if (!si_is_native(si)) {
        // stack frame is java frame, get frame method and location
        uint16 bc = 0;
        CodeChunkInfo *cci = si_get_code_chunk_info(si);
        Method *func = cci->get_method();
        NativeCodePtr ip = si_get_ip(si);
        JIT *jit = cci->get_jit();
        OpenExeJpdaError UNREF result =
                    jit->get_bc_location_for_native(func, ip, &bc);
        assert(result == EXE_ERROR_NONE);
        TRACE2( "jvmti.break.ss", "SingleStep method IP: " << ip );

        // set step location structure
        *count = 1;
        jvmtiError error = _allocate( sizeof(jvmti_StepLocation), (unsigned char**)next_step );
        if( error != JVMTI_ERROR_NONE ) {
            si_free(si);
            return error;
        }
        (*next_step)->method = func;
        // IP in stack iterator points to a bytecode next after the one
        // which caused call of the method. So next location is the 'bc' which
        // IP points to.
        (*next_step)->location = bc;
        (*next_step)->native_location = ip;
    }
    si_free(si);
    return JVMTI_ERROR_NONE;
} // jvmti_get_next_bytecodes_from_native

jvmtiError DebugUtilsTI::jvmti_single_step_start(void)
{
    assert(hythread_is_suspend_enabled());

    VMBreakPoints* vm_brpt = VM_Global_State::loader_env->TI->vm_brpt;
    LMAutoUnlock lock(vm_brpt->get_lock());

    hythread_iterator_t threads_iterator;

    // Suspend all threads except current
    IDATA tm_ret = hythread_suspend_all(&threads_iterator, NULL);
    if (TM_ERROR_NONE != tm_ret)
        return JVMTI_ERROR_INTERNAL;

    hythread_t ht;

    // Set single step in all threads
    while ((ht = hythread_iterator_next(&threads_iterator)) != NULL)
    {
        VM_thread *vm_thread = get_vm_thread(ht);
        if( !vm_thread ) {
            // Skip thread that isn't started yet. SingleStep state
            // will be enabled for it in
            // jvmti_send_thread_start_end_event
            continue;
        }

        // Init single step state for the thread
        jvmtiError errorCode = _allocate(sizeof(JVMTISingleStepState),
            (unsigned char **)&vm_thread->ss_state);

        if (JVMTI_ERROR_NONE != errorCode)
        {
            hythread_resume_all(NULL);
            return errorCode;
        }

        vm_thread->ss_state->predicted_breakpoints = NULL;

        jvmti_StepLocation *locations;
        unsigned locations_number;

        errorCode = jvmti_get_next_bytecodes_from_native(
            vm_thread, &locations, &locations_number, false);

        if (JVMTI_ERROR_NONE != errorCode)
        {
            hythread_resume_all(NULL);
            return errorCode;
        }

        jvmti_set_single_step_breakpoints(this, vm_thread, locations, locations_number);
    }
    
    single_step_enabled = true;

    tm_ret = hythread_resume_all(NULL);
    if (TM_ERROR_NONE != tm_ret)
        return JVMTI_ERROR_INTERNAL;

    return JVMTI_ERROR_NONE;
}

jvmtiError DebugUtilsTI::jvmti_single_step_stop(void)
{
    assert(hythread_is_suspend_enabled());

    VMBreakPoints* vm_brpt = VM_Global_State::loader_env->TI->vm_brpt;
    LMAutoUnlock lock(vm_brpt->get_lock());

    hythread_iterator_t threads_iterator;

    // Suspend all threads except current
    IDATA tm_ret = hythread_suspend_all(&threads_iterator, NULL);
    if (TM_ERROR_NONE != tm_ret)
        return JVMTI_ERROR_INTERNAL;

    hythread_t ht;

    // Clear single step in all threads
    while ((ht = hythread_iterator_next(&threads_iterator)) != NULL)
    {
        VM_thread *vm_thread = get_vm_thread(ht);
        if( !vm_thread ) {
            // Skip thread that isn't started yet. No need to disable
            // SingleStep state for it
            continue;
        }

        jvmti_remove_single_step_breakpoints(this, vm_thread);
        vm_brpt->release_intf(vm_thread->ss_state->predicted_breakpoints);
        _deallocate((unsigned char *)vm_thread->ss_state);
        vm_thread->ss_state = NULL;
    }

    single_step_enabled = false;

    tm_ret = hythread_resume_all(NULL);
    if (TM_ERROR_NONE != tm_ret)
        return JVMTI_ERROR_INTERNAL;

    return JVMTI_ERROR_NONE;
}
