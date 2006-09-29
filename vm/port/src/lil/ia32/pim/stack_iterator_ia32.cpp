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
 * @author Intel, Pavel Afremov
 * @version $Revision$
 */  


#include "jit_intf_cpp.h"
#include "m2n.h"
#include "m2n_ia32_internal.h"
#include "nogc.h"
#include "method_lookup.h"
#include "stack_iterator.h"
#include "vm_stats.h"
#include "open/types.h"
#include "encoder.h"
#include "interpreter.h"

#include "clog.h"

#include "dump.h"

// Invariants:
//   Native frames:
//     cci should be NULL
//     m2nfl should point to the m2n frame list for the native frame
//     c.p_eip should point to an address that is not a valid IP for managed code
//   Managed frames:
//     cci should point to the code chunk info for the method and ip in question
//     m2nfl should point to the m2n frame immediately preceeding the current one or NULL is there is no preceeding m2n frame
//     the callee saves registers should point to their values at the time the frame was suspended
//     for frames suspended at noncall sites, the caller saves registers should point to their values at the time of suspension
//     c.p_eip and c.esp should point-to/have their values at the time the frame was suspended
//     c.p_eax is also valid for returning a pointer in transfer control

struct StackIterator {
    CodeChunkInfo*    cci;
    JitFrameContext   c;
    M2nFrame*         m2nfl;
    uint32            ip;
};

//////////////////////////////////////////////////////////////////////////
// Utilities

// Goto the managed frame immediately prior to m2nfl
static void si_unwind_from_m2n(StackIterator* si, bool over_popped = true)
{
#ifdef VM_STATS
    VM_Statistics::get_vm_stats().num_unwind_native_frames_all++;
#endif

    M2nFrame* m2nfl = si->m2nfl;
    assert(m2nfl);

    si->m2nfl   = m2n_get_previous_frame(m2nfl);

    TRACE2("si", ("si_unwind_from_m2n, ip = %p",(void*)m2nfl->eip));

    // Is it a normal M2nFrame or one for suspended managed code?
    if ((uint32)m2nfl->p_lm2nf==1) {
        // Suspended managed code, eip is at instruction, esp & registers are in regs structure
        TRACE2("si", ("si_unwind_from_m2n from suspended managed code, ip = %p", 
            (void*)m2nfl->regs->eip));
        si->c.esp = m2nfl->regs->esp;
        si->c.p_eip = &(m2nfl->regs->eip);
        si->c.is_ip_past = FALSE;
        si->c.p_eax = &m2nfl->regs->eax;
        si->c.p_ebx = &m2nfl->regs->ebx;
        si->c.p_ecx = &m2nfl->regs->ecx;
        si->c.p_edx = &m2nfl->regs->edx;
        si->c.p_esi = &m2nfl->regs->esi;
        si->c.p_edi = &m2nfl->regs->edi;
        si->c.p_ebp = &m2nfl->regs->ebp;
    } else if (over_popped &&
            (FRAME_POP_DONE == (FRAME_POP_MASK & m2n_get_frame_type(m2nfl)))) {
        si->c.esp = m2nfl->pop_regs->esp;
        si->c.p_eip = &(m2nfl->pop_regs->eip);
        si->c.is_ip_past = FALSE;
        si->c.p_eax = &m2nfl->pop_regs->eax;
        si->c.p_ebx = &m2nfl->pop_regs->ebx;
        si->c.p_ecx = &m2nfl->pop_regs->ecx;
        si->c.p_edx = &m2nfl->pop_regs->edx;
        si->c.p_esi = &m2nfl->pop_regs->esi;
        si->c.p_edi = &m2nfl->pop_regs->edi;
        si->c.p_ebp = &m2nfl->pop_regs->ebp;
    } else {
        // Normal M2nFrame, eip is past instruction, esp is implicitly address just beyond the frame, callee saves registers in M2nFrame
        si->c.esp   = (uint32)m2nfl + m2n_sizeof_m2n_frame;
        si->c.p_eip = &m2nfl->eip;
        si->c.is_ip_past = TRUE;
        si->c.p_edi = &m2nfl->edi;
        si->c.p_esi = &m2nfl->esi;
        si->c.p_ebx = &m2nfl->ebx;
        si->c.p_ebp = &m2nfl->ebp;
        si->c.p_eip = &m2nfl->eip;
    }
}

static char* get_reg(char* ss, R_Opnd* dst, Reg_No dst_reg, Reg_No si_ptr_var, unsigned offset)
{
    ss = mov(ss, *dst,  M_Base_Opnd(si_ptr_var, offset));
    ss = mov(ss, *dst,  M_Base_Opnd(dst_reg, 0));
    return ss;
}

typedef void (__cdecl *transfer_control_stub_type)(StackIterator*);

static transfer_control_stub_type gen_transfer_control_stub()
{
    static transfer_control_stub_type addr = NULL;
    if (addr) {
        return addr;
    }

    const int stub_size = 64;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_COLD, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;

    //
    // ************* LOW LEVEL DEPENDENCY! ***************
    // This code sequence must be atomic.  The "atomicity" effect is achieved by
    // changing the esp at the very end of the sequence.
    //

    M_Base_Opnd m1(esp_reg, 4);
    ss = mov(ss,  edx_opnd,  m1);

    ss = get_reg(ss, &ebx_opnd, ebx_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_eip);

    M_Base_Opnd m2(edx_reg, (int)&((StackIterator*)0)->c.esp);
    ss = mov(ss,  ecx_opnd,  m2);

    ss = alu(ss, sub_opc, ecx_opnd, Imm_Opnd(4));
    ss = mov(ss,  m1,  ecx_opnd);

    ss = get_reg(ss, &esi_opnd, esi_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_esi);
    ss = get_reg(ss, &edi_opnd, edi_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_edi);
    ss = get_reg(ss, &ebp_opnd, ebp_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_ebp);

    M_Base_Opnd m3(ecx_reg, 0);
    ss = mov(ss,  m3,  ebx_opnd);

    ss = get_reg(ss, &eax_opnd, eax_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_eax);
    ss = get_reg(ss, &ebx_opnd, ebx_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_ebx);
    ss = get_reg(ss, &ecx_opnd, ecx_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_ecx);
    ss = get_reg(ss, &edx_opnd, edx_reg, edx_reg, (unsigned)&((StackIterator*)0)->c.p_edx);

    ss = mov(ss,  esp_opnd,  m1);
    ss = ret(ss);

    addr = (transfer_control_stub_type)stub;
    assert(ss-stub <= stub_size);

    /*
       The following code will be generated:

        mov         edx,dword ptr [esp+4]
        mov         ebx,dword ptr [edx+0Ch]
        mov         ebx,dword ptr [ebx]
        mov         ecx,dword ptr [edx+4]
        sub         ecx,4
        mov         dword ptr [esp+4],ecx
        mov         esi,dword ptr [edx+14h]
        mov         esi,dword ptr [esi]
        mov         edi,dword ptr [edx+10h]
        mov         edi,dword ptr [edi]
        mov         ebp,dword ptr [edx+8]
        mov         ebp,dword ptr [ebp]
        mov         dword ptr [ecx],ebx
        mov         eax,dword ptr [edx+1Ch]
        mov         eax,dword ptr [eax]
        mov         ebx,dword ptr [edx+18h]
        mov         ebx,dword ptr [ebx]
        mov         ecx,dword ptr [edx+20h]
        mov         ecx,dword ptr [ecx]
        mov         edx,dword ptr [edx+24h]
        mov         edx,dword ptr [edx]
        mov         esp,dword ptr [esp+4]
        ret
    */

    DUMP_STUB(stub, "getaddress__transfer_control", ss - stub);

    return addr;
}

//////////////////////////////////////////////////////////////////////////
// Stack Iterator Interface

StackIterator* si_create_from_native()
{
    ASSERT_NO_INTERPRETER
    // Allocate iterator
    StackIterator* res = (StackIterator*)STD_MALLOC(sizeof(StackIterator));
    assert(res);
    memset(res, 0, sizeof(StackIterator));

    res->cci = NULL;
    res->m2nfl = m2n_get_last_frame();
    res->ip = 0;
    res->c.p_eip = &res->ip;

    return res;
}

StackIterator* si_create_from_native(VM_thread* thread)
{
    ASSERT_NO_INTERPRETER
    // Allocate iterator
    StackIterator* res = (StackIterator*)STD_MALLOC(sizeof(StackIterator));
    assert(res);
    memset(res, 0, sizeof(StackIterator));

    // Setup current frame
    res->cci = NULL;
    res->m2nfl = m2n_get_last_frame(thread);
    res->ip = 0;
    res->c.p_eip = &res->ip;

    return res;
}

StackIterator* si_create_from_registers(Registers* regs, bool is_ip_past, M2nFrame* lm2nf)
{
    ASSERT_NO_INTERPRETER
    // Allocate iterator
    StackIterator* res = (StackIterator*)STD_MALLOC(sizeof(StackIterator));
    assert(res);
    memset(res, 0, sizeof(StackIterator));

    // Setup current frame
    res->cci = vm_methods->find((NativeCodePtr)regs->eip, is_ip_past);
    assert(res->cci);
    res->c.esp = regs->esp;
    res->c.p_eip = &regs->eip;
    res->c.p_ebp = &regs->ebp;
    res->c.p_edi = &regs->edi;
    res->c.p_esi = &regs->esi;
    res->c.p_ebx = &regs->ebx;
    res->c.p_eax = &regs->eax;
    res->c.p_ecx = &regs->ecx;
    res->c.p_edx = &regs->edx;
    res->c.is_ip_past = is_ip_past;
    res->m2nfl = lm2nf;

    return res;
}

// On IA32 all registers are preserved automatically, so this is a nop.
void si_transfer_all_preserved_registers(StackIterator*)
{
    // Do nothing
}

bool si_is_past_end(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    return si->cci==NULL && si->m2nfl==NULL;
}

void si_goto_previous(StackIterator* si, bool over_popped)
{
    ASSERT_NO_INTERPRETER
    if (si->cci) {
        TRACE2("si", ("si_goto_previous from ip = %p (%s%s)",
            (void*)si_get_ip(si),
            method_get_name(si->cci->get_method()),
            method_get_descriptor(si->cci->get_method())));
        assert(si->cci->get_jit() && si->cci->get_method());
        si->cci->get_jit()->unwind_stack_frame(si->cci->get_method(), si_get_jit_context(si));
        si->c.is_ip_past = TRUE;
    } else {
        TRACE2("si", ("si_goto_previous from ip = %p (M2N)",
            (void*)si_get_ip(si)));
        if (!si->m2nfl) return;
        si_unwind_from_m2n(si, over_popped);
    }
    si->cci = vm_methods->find(si_get_ip(si), true);
    if (si->cci) {
        TRACE2("si", ("si_goto_previous to ip = %p (%s%s)",
            (void*)si_get_ip(si),
            method_get_name(si->cci->get_method()),
            method_get_descriptor(si->cci->get_method())));
    } else {
        TRACE2("si", ("si_goto_previous to ip = %p (M2N)",
            (void*)si_get_ip(si)));
    }
}

StackIterator* si_dup(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    StackIterator* res = (StackIterator*)STD_MALLOC(sizeof(StackIterator));
    memcpy(res, si, sizeof(StackIterator));
    // If si uses itself for IP then res should also to avoid problems if si is deallocated first.
    if (si->c.p_eip == &si->ip)
        res->c.p_eip = &res->ip;
    return res;
}

void si_free(StackIterator* si)
{
    STD_FREE(si);
}

NativeCodePtr si_get_ip(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    return (NativeCodePtr)*si->c.p_eip;
}

void si_set_ip(StackIterator* si, NativeCodePtr ip, bool also_update_stack_itself)
{
    if (also_update_stack_itself) {
        *(si->c.p_eip) = (uint32)ip;
    } else {
        si->ip = (uint32)ip;
        si->c.p_eip = &si->ip;
    }
}

// Set the code chunk in the stack iterator
void si_set_code_chunk_info(StackIterator* si, CodeChunkInfo* cci)
{
    ASSERT_NO_INTERPRETER
    assert(si);
    si->cci = cci;
}

CodeChunkInfo* si_get_code_chunk_info(StackIterator* si)
{
    return si->cci;
}

JitFrameContext* si_get_jit_context(StackIterator* si)
{
    return &si->c;
}

bool si_is_native(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    return si->cci==NULL;
}

M2nFrame* si_get_m2n(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    return si->m2nfl;
}

void** si_get_return_pointer(StackIterator* si)
{
    return (void**) si->c.p_eax;
}

void si_set_return_pointer(StackIterator* si, void** return_value)
{
    si->c.p_eax = (uint32*)return_value;
}

#ifdef _WIN32

#ifdef _MSC_VER
#pragma warning(push)
#pragma warning(disable : 4733) // Inline asm assigning to 'FS:0' : handler not registered as safe handler
#endif
/**
 * Temporary workaround and detection of problem.
 *
 * This is a hack function to detect problems with C++ object living on
 * while destructive unwinding is used. Known problem is logger in release
 * mode. No (even potential) logging or other c++ objects created on stack
 * is alowed in any functions in the stack when calling destructive unwinding.
 *
 * The function prints warning message and corrects exception handlers if
 * the objects exist.
 *
 * See also function: JIT_execute_method_default
 */
void cpp_set_last_frame(M2nFrame* lm2nf)
{
    TRACE2("exn",("Unwinding.... lm2nf=0x%08x\n", lm2nf));
    void *handler;
    __asm {
        mov eax, fs:[0]
        mov handler, eax
    }

    TRACE2("exn",(" handler=0x%p\n", handler));
    if (!(handler < lm2nf)) {
        return;
    }

    // finding lastFrame > lm2nf, should be last
    void *lastFrame = p_TLS_vmthread->lastFrame;
    TRACE2("exn",("  curr = 0x%p\n", lastFrame));

    if (!(lastFrame < lm2nf)) {
        printf("Unwinding:  Lost lastFrame\n");
        __asm { int 3 }
    }

    while(true) {
        void *prevFrame = *(void**)lastFrame;
        TRACE2("exn",("  prev = 0x%p\n", prevFrame));
        if (prevFrame == 0) {
            break;
        }
        if (prevFrame < lm2nf) {
            fprintf(stderr, "ERROR:  Unwinding native code! Error\n");
            __asm { int 3 }
        } else {
            break;
        }
        lastFrame = prevFrame;
    }

    if (!(handler < lastFrame)) {
        TRACE2("exn",("all ok\n"));
        return;
    }

    // NO CXX LOGGER PLEASE! doesn't work with destructive unwinding!
    INFO2("exn", ("ERROR: Destructive unwinding: C++ objects detected on stack!\n"));

    while(handler < lastFrame) {
        INFO2("exn", ("  droping 0x%p\n", handler));
        handler = *(int**)handler;
    }
    INFO2("exn", (" setting curr 0x%p\n", handler));
    __asm {
        mov eax, handler
        mov fs:[0], eax
    }
}
#ifdef _MSC_VER
#pragma warning(pop)
#endif
#endif // _WIN32

void si_transfer_control(StackIterator* si)
{
/* !!!! NO CXX LOGGER IS ALLOWED IN THIS FUNCTION !!!
 * !!!! RELEASE BUILD WILL BE BROKEN          !!!*/
    // 1. Copy si to stack
    void* null_pointer = NULL;
    StackIterator local_si;
    memcpy(&local_si, si, sizeof(StackIterator));

    if (NULL == si->c.p_eax)
        local_si.c.p_eax = (uint32*)&null_pointer;
    if (NULL == si->c.p_ebx)
        local_si.c.p_ebx = (uint32*)&null_pointer;
    if (NULL == si->c.p_ecx)
        local_si.c.p_ecx = (uint32*)&null_pointer;
    if (NULL == si->c.p_edx)
        local_si.c.p_edx = (uint32*)&null_pointer;

    if (si->c.p_eip == &si->ip)
        local_si.c.p_eip = &local_si.ip;
    si_free(si);

    // 2. Set the M2nFrame list
    m2n_set_last_frame(local_si.m2nfl);
#ifdef _WIN32 // Workaround and detection of possible problems with
              // objects on stack.
    cpp_set_last_frame(local_si.m2nfl);
#endif // _WIN32

    TRACE2("exn", ("generating control transfer stub"));
    // 3. Call the stub
    transfer_control_stub_type tcs = gen_transfer_control_stub();
    TRACE2("exn", ("tcs"));
    tcs(&local_si);
}

void si_copy_to_registers(StackIterator* si, Registers* regs)
{
    ASSERT_NO_INTERPRETER
    m2n_set_last_frame(si->m2nfl);
    regs->esp = si->c.esp;
    regs->eip = *si->c.p_eip;
    regs->ebp = *si->c.p_ebp;
    regs->edi = *si->c.p_edi;
    regs->esi = *si->c.p_esi;
    regs->ebx = *si->c.p_ebx;
    regs->eax = *si->c.p_eax;
}

void si_set_callbak(StackIterator* si, NativeCodePtr* callback) {
    si->c.esp = si->c.esp - 4;
    *((uint32*) si->c.esp) = *(si->c.p_eip);
    si->c.p_eip = ((uint32*)callback);
}

void si_reload_registers()
{
    // Do nothing
}
