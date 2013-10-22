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
 * @author Intel, Evgueni Brevnov
 * @version $Revision$
 */  

#include "open/types.h"
#include "open/vm_util.h"
#include "environment.h"
#include "encoder.h"
#include "object_handles.h"
#include "vm_threads.h"
#include "compile.h"

#include "nogc.h"
#include "m2n.h"
#include "../m2n_em64t_internal.h"
#include "exceptions.h"
#include "exceptions_jit.h"

#define LOG_DOMAIN "vm.helpers"
#include "cxxlog.h"

#include "dump.h"
#include "vm_stats.h"

void compile_flush_generated_code_block(Byte*, size_t) {
    // Nothing to do on EM64T
}

void compile_flush_generated_code() {
    // Nothing to do on EM64T
}

void patch_code_with_threads_suspended(Byte * UNREF code_block, Byte * UNREF new_code, size_t UNREF size) {
    ABORT("Not supported on EM64T currently");
}

void compile_protect_arguments(Method_Handle method, GcFrame * gc) {
    const unsigned MAX_GP = 6;
    const unsigned MAX_FP = 8;
    // adress of the top of m2n frame
    uint64 * const m2n_base_addr = (uint64 *)m2n_get_frame_base(m2n_get_last_frame());
     // 6(scratched registers on the stack)
    assert(m2n_get_size() % 8 == 0);
    // 15 = 1(alignment) + 8(fp) + 6(gp) registers were preserved on the stack
    uint64 * const inputs_addr = m2n_base_addr - (m2n_get_size() / 8) - 15;
     // 1(return ip);
    uint64 * extra_inputs_addr = m2n_base_addr + 1;

    assert(!hythread_is_suspend_enabled());
    Method_Signature_Handle msh = method_get_signature(method);
    
    unsigned num_gp_used = 0;
    unsigned num_fp_used = 0;
    for(unsigned i = 0; i < method_args_get_number(msh); i++) {
        Type_Info_Handle tih = method_args_get_type_info(msh, i);
        switch (type_info_get_type(tih)) {
        case VM_DATA_TYPE_INT64:
        case VM_DATA_TYPE_UINT64:
        case VM_DATA_TYPE_INT8:
        case VM_DATA_TYPE_UINT8:
        case VM_DATA_TYPE_INT16:
        case VM_DATA_TYPE_UINT16:
        case VM_DATA_TYPE_INT32:
        case VM_DATA_TYPE_UINT32:
        case VM_DATA_TYPE_INTPTR:
        case VM_DATA_TYPE_UINTPTR:
        case VM_DATA_TYPE_BOOLEAN:
        case VM_DATA_TYPE_CHAR:
        case VM_DATA_TYPE_UP:
            if (num_gp_used < MAX_GP) {
                ++num_gp_used;
            } else {
                ++extra_inputs_addr;
            }
            break;
        case VM_DATA_TYPE_CLASS:
        case VM_DATA_TYPE_ARRAY: {
            uint64 * ref_addr;
            if (num_gp_used < MAX_GP) {
                ref_addr =  inputs_addr + num_gp_used;
                ++num_gp_used;
            } else {
                ref_addr = extra_inputs_addr;
                ++extra_inputs_addr;
            }
            gc->add_object((ManagedObject**)ref_addr);
            break;
        }
        case VM_DATA_TYPE_MP: {
            uint64 * ref_addr;
            if (num_gp_used < MAX_GP) {
                ref_addr =  inputs_addr + num_gp_used;
                ++num_gp_used;
            } else {
                ref_addr = extra_inputs_addr;
                ++extra_inputs_addr;
            }
            gc->add_managed_pointer((ManagedPointer*)ref_addr);
            break;
        }
        case VM_DATA_TYPE_F4:
        case VM_DATA_TYPE_F8:
            if (num_fp_used < MAX_FP) {
                ++num_fp_used;
            } else {
                ++extra_inputs_addr;
            }
            break;
        case VM_DATA_TYPE_VALUE:
            DIE("This functionality is not currently supported");
        default:
            ASSERT(0, "Unexpected data type: " << type_info_get_type(tih));
        }
    }
}

/*    BEGIN COMPILE-ME STUBS    */

// compile_me stack frame
//    m2n frame
//    8 byte alignment
//    6 xmm registers
//    6 gp registers
//    method handle
const int32 stack_size = m2n_get_size() + 8 + 120;

NativeCodePtr compile_jit_a_method(Method * method);

static NativeCodePtr compile_get_compile_me_generic() {
    static NativeCodePtr addr = NULL;
    if (addr) {
        return addr;
    }

    const int STUB_SIZE = 357;
    char * stub = (char *) malloc_fixed_code_for_jit(STUB_SIZE,
        DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
    addr = stub;
#ifndef NDEBUG
    memset(stub, 0xcc /*int 3*/, STUB_SIZE);
#endif
    assert(stack_size % 8 == 0);
    assert(stack_size % 16 != 0);
    // set up stack frame
    stub = alu(stub, sub_opc, rsp_opnd, Imm_Opnd(stack_size));
    // TODO: think over saving xmm registers conditionally
    stub = movq(stub, M_Base_Opnd(rsp_reg, 112), xmm7_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 104), xmm6_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 96), xmm5_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 88), xmm4_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 80), xmm3_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 72), xmm2_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 64), xmm1_opnd);
    stub = movq(stub, M_Base_Opnd(rsp_reg, 56), xmm0_opnd);
    // we need to preserve all general purpose registers here
    // to protect managed objects from GC during compilation
    stub = mov(stub, M_Base_Opnd(rsp_reg, 48), r9_opnd);
    stub = mov(stub, M_Base_Opnd(rsp_reg, 40), r8_opnd);
    stub = mov(stub, M_Base_Opnd(rsp_reg, 32), rcx_opnd);
    stub = mov(stub, M_Base_Opnd(rsp_reg, 24), rdx_opnd);
    stub = mov(stub, M_Base_Opnd(rsp_reg, 16), rsi_opnd);
    stub = mov(stub, M_Base_Opnd(rsp_reg, 8), rdi_opnd);
    // push m2n to the stack
    // skip m2n frame, 6 xmm registers, 6 gp registers and method handle
    
    stub = m2n_gen_push_m2n(stub, NULL,
        FRAME_COMPILATION, false, 0, 0, stack_size);
    // restore Method_Handle
    stub = mov(stub, rdi_opnd, M_Base_Opnd(rsp_reg, 0));
    // compile the method
    stub = call(stub, (char *)&compile_jit_a_method);

    // rethrow exception if it panding
    stub = push(stub, rax_opnd);
    stub = call(stub, (char *)&exn_rethrow_if_pending);
    stub = pop(stub, rax_opnd);

    // restore gp inputs from the stack
    // NOTE: m2n_gen_pop_m2n must not destroy inputs
    stub = pop(stub, rdi_opnd);
    stub = pop(stub, rdi_opnd);
    stub = pop(stub, rsi_opnd);
    stub = pop(stub, rdx_opnd);
    stub = pop(stub, rcx_opnd);
    stub = pop(stub, r8_opnd);
    stub = pop(stub, r9_opnd);
    // restore fp inputs from the stack
    stub = movq(stub, xmm0_opnd, M_Base_Opnd(rsp_reg, 0));
    stub = movq(stub, xmm1_opnd, M_Base_Opnd(rsp_reg, 8));
    stub = movq(stub, xmm2_opnd, M_Base_Opnd(rsp_reg, 16));
    stub = movq(stub, xmm3_opnd, M_Base_Opnd(rsp_reg, 24));
    stub = movq(stub, xmm4_opnd, M_Base_Opnd(rsp_reg, 32));
    stub = movq(stub, xmm5_opnd, M_Base_Opnd(rsp_reg, 40));
    stub = movq(stub, xmm6_opnd, M_Base_Opnd(rsp_reg, 48));
    stub = movq(stub, xmm7_opnd, M_Base_Opnd(rsp_reg, 56));
    // pop m2n from the stack
    const int32 bytes_to_m2n_bottom = 72;
    stub = m2n_gen_pop_m2n(stub, false, 0, bytes_to_m2n_bottom, 1);
    // adjust stack pointer
    stub = alu(stub, add_opc, rsp_opnd, Imm_Opnd(bytes_to_m2n_bottom + m2n_get_size()));
    // transfer control to the compiled code
    stub = jump(stub, rax_opnd);
    
    assert(stub - (char *)addr <= STUB_SIZE);

#if 0
    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("compile_me_generic", stub, STUB_SIZE);
        jvmti_send_dynamic_code_generated_event("compile_me_generic", stub, STUB_SIZE);
    }
#endif

    DUMP_STUB(addr, "compileme_generic", stub - (char *)addr);

    return addr;
}

NativeCodePtr compile_gen_compile_me(Method_Handle method) {
    int STUB_SIZE = 32;
#ifdef VM_STATS
    ++VM_Statistics::get_vm_stats().num_compileme_generated;
#endif
    char * stub = (char *) malloc_fixed_code_for_jit(STUB_SIZE,
        DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
    NativeCodePtr addr = stub; 
#ifndef NDEBUG
    memset(stub, 0xcc /*int 3*/, STUB_SIZE);
#endif

#ifdef VM_STATS
    // FIXME: vm_stats_total is not yet initialized :-(
    //stub = mov(stub, r9_opnd, (int64)&VM_Statistics::get_vm_stats().num_compileme_used);
    //stub = inc(stub, M_Base_Opnd(r9_reg, 0));
#endif
    // preserve method handle
    stub = mov(stub, r10_opnd, Imm_Opnd(size_64, (int64)method));
    stub = mov(stub, M_Base_Opnd(rsp_reg, -stack_size), r10_opnd);
    // transfer control to generic part
    stub = jump(stub, (char *)compile_get_compile_me_generic());
    assert(stub - (char *)addr <= STUB_SIZE);


#if 0
    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        char * name;
        const char * c = class_get_name(method_get_class(method));
        const char * m = method_get_name(method);
        const char * d = method_get_descriptor(method);
        size_t sz = strlen(c)+strlen(m)+strlen(d)+12;
        name = (char *)STD_MALLOC(sz);
        sprintf(name, "compileme.%s.%s%s", c, m, d);
        jvmti_add_dynamic_generated_code_chunk(name, stub, STUB_SIZE);
        jvmti_send_dynamic_code_generated_event(name, stub, STUB_SIZE);
    }
#endif


#ifndef NDEBUG
    static unsigned done = 0;
    // dump first 10 compileme stubs
    if (dump_stubs && ++done <= 10) {
        char * buf;
        const char * c = class_get_name(method_get_class(method));
        const char * m = method_get_name(method);
        const char * d = method_get_descriptor(method);
        size_t sz = strlen(c)+strlen(m)+strlen(d)+12;
        buf = (char *)STD_MALLOC(sz);
        sprintf(buf, "compileme.%s.%s%s", c, m, d);
        assert(strlen(buf) < sz);
        DUMP_STUB(addr, buf, stub - (char *)addr);
        STD_FREE(buf);
    }
#endif
    return addr;
}

/*    END COMPILE-ME STUBS    */


/*    BEGIN SUPPORT FOR STUB OVERRIDE CODE SEQUENCES    */

static Stub_Override_Entry _stub_override_entries_base[] = {};

Stub_Override_Entry * stub_override_entries = &(_stub_override_entries_base[0]);

int sizeof_stub_override_entries = sizeof(_stub_override_entries_base) / sizeof(_stub_override_entries_base[0]);

/*    END SUPPORT FOR STUB OVERRIDE CODE SEQUENCES    */