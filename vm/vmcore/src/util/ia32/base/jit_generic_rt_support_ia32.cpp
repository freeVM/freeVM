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
 * @version $Revision: 1.1.2.2.4.3 $
 */  

//
// This file contains IA32-specific code that does not depend on any VM internals.
// For the most part, this means math helpers.
//

#include <assert.h>
#include <float.h>
#include <math.h>

#define LOG_DOMAIN "vm.helpers"
#include "cxxlog.h"

#include "jit_runtime_support.h"

#include "nogc.h" // for malloc_fixed_code_for_jit()
#include "encoder.h"
#include "vm_stats.h"
#include "vm_arrays.h"

#ifdef PLATFORM_POSIX

#ifndef _isnan
#define _isnan isnan
#endif

#endif // PLATFORM_POSIX

#ifndef NDEBUG
#include "dump.h"
extern bool dump_stubs;
#endif

static uint64 vm_lshl(unsigned count, uint64 n)
{
    assert(!hythread_is_suspend_enabled());
    return n << (count & 0x3f);
} //vm_lshl


// The arguments are:
// edx:eax          - the value to be shifted
// ecx              - how many bits to shift by
// The result is returned in edx:eax.


void * getaddress__vm_lshl_naked()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 12+12;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;

    unsigned n=12;
    M_Base_Opnd mem(esp_reg, 12);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = call(ss, (char *)vm_lshl);
    ss = alu(ss, add_opc,  esp_opnd,  Imm_Opnd(12));
    Imm_Opnd imm(n);
    ss = ret(ss,  imm);
    
    addr = stub;
    assert((ss - stub) <= stub_size);


    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_lshl_naked", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_lshl_naked", stub, stub_size);
    }

    
#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_lshl_naked", ss - stub);
#endif
    return addr;
} //getaddress__vm_lshl_naked


static int64 vm_lshr(unsigned count, int64 n)
{
    assert(!hythread_is_suspend_enabled());
    return n >> (count & 0x3f);
} //vm_lshr


// The arguments are:
// edx:eax          - the value to be shifted
// ecx              - how many bits to shift by
// The result is returned in edx:eax.


void * getaddress__vm_lshr_naked()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 13+12;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;

    unsigned n=12;
    M_Base_Opnd mem(esp_reg, 12);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = call(ss, (char *)vm_lshr);
    ss = alu(ss, add_opc,  esp_opnd,  Imm_Opnd(12));
    Imm_Opnd imm(n);
    ss = ret(ss,  imm);
    
    addr = stub;
    assert((ss - stub) < stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_lshr_naked", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_lshr_naked", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_lshr_naked", ss - stub);
#endif
    return addr;
} //getaddress__vm_lshr_naked


static uint64 vm_lushr(unsigned count, uint64 n)
{
    assert(!hythread_is_suspend_enabled());
    return n >> (count & 0x3f);
} //vm_lushr


// The arguments are:
// edx:eax          - the value to be shifted
// ecx              - how many bits to shift by
// The result is returned in edx:eax.


void * getaddress__vm_lushr_naked()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 12+12;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;

    unsigned n=12;
    M_Base_Opnd mem(esp_reg, 12);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = push(ss,  mem);
    ss = call(ss, (char *)vm_lushr);
    ss = alu(ss, add_opc,  esp_opnd,  Imm_Opnd(12));
    Imm_Opnd imm(n);
    ss = ret(ss,  imm);
    
    addr = stub;
    assert((ss - stub) <= stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_lushr_naked", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_lushr_naked", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_lushr_naked", ss - stub);
#endif
    return addr;
} //getaddress__vm_lushr_naked


static int64 __stdcall vm_lmul(int64 m, int64 n) stdcall__;

static int64 __stdcall vm_lmul(int64 m, int64 n)
{
    assert(!hythread_is_suspend_enabled());

    return m * n;
} //vm_lmul

#ifdef VM_LONG_OPT
static int64 __stdcall vm_lmul_const_multiplier(int64 m, int64 n) stdcall__;

static int64 __stdcall vm_lmul_const_multiplier(int64 m, int64 n)
{
    assert(!hythread_is_suspend_enabled());
    __asm{
        mov  eax,dword ptr [ebp+0ch]
        mov  ecx,dword ptr [ebp+10h]
        mul   ecx 
        mov   ebx,eax
        mov   eax,dword ptr [ebp+08h]
        mul   ecx
        add   edx,ebx
    }
} //vm_lmul_const_multiplier
#endif


static int64 __stdcall do_lrem(int64 m, int64 n) stdcall__;

static int64 __stdcall do_lrem(int64 m, int64 n)
{
    assert(!hythread_is_suspend_enabled());

    return m % n;
} //do_lrem


void * getaddress__vm_const_lrem_naked()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 200 ;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
    char* ss = stub ;

    ss = push(ss,  ebp_opnd);
    ss = mov(ss,  ebp_opnd,  esp_opnd);
    ss = push(ss,  ebx_opnd);
    ss = push(ss,  esi_opnd);
    ss = push(ss,  edi_opnd);

    ss = mov(ss,  esi_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edi_opnd,  M_Base_Opnd(ebp_reg, +0x10) );
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(edi_reg, +0x28) );
    ss = alu(ss, cmp_opc,  esi_opnd,  eax_opnd);
    ss = branch8(ss, Condition_AE,  Imm_Opnd(size_8, 0x60)); // jae slower
    ss = mov(ss,  ebx_opnd,  M_Base_Opnd(edi_reg, +0x30) );
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(ebp_reg, +0x8) );
    ss = mov(ss,  ecx_opnd,  Imm_Opnd(0x20) );
    ss = alu(ss, sub_opc,  ecx_opnd,  ebx_opnd);
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0x5)); // je l_is_32
    ss = shift(ss, shld_opc,  eax_opnd,  edx_opnd); //shld eax,edx,cl
    ss = shift(ss, shl_opc,  edx_opnd); //shld eax,edx,cl
//l_is_32:
    ss = mov(ss,  ebx_opnd,  edx_opnd);
    ss = shift(ss, sar_opc,  ebx_opnd,  Imm_Opnd(0x1f)); //shld eax,edx,cl
    ss = mov(ss,  esi_opnd,  M_Base_Opnd(edi_reg, +0x38) );
    ss = alu(ss, and_opc,  esi_opnd,  ebx_opnd);
    ss = alu(ss, add_opc,  esi_opnd,  edx_opnd);
    ss = mov(ss,  ecx_opnd,  eax_opnd);
    ss = alu(ss, cmp_opc,  eax_opnd,  ebx_opnd);
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0x0b)); // je mul_zero
    ss = alu(ss, sub_opc,  eax_opnd,  ebx_opnd);
    ss = mov(ss,  ebx_opnd,  eax_opnd);
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(edi_reg, +0x34) );
    ss = mul(ss,  edx_opnd);
    ss = jump8(ss,  Imm_Opnd(size_8, 0x4)); // jmp next1
//mul_zero:
    ss = alu(ss, xor_opc,  edx_opnd,  edx_opnd);
    ss = alu(ss, xor_opc,  eax_opnd,  eax_opnd);
    ss = alu(ss, add_opc,  eax_opnd,  esi_opnd);
    ss = alu(ss, adc_opc,  edx_opnd,  Imm_Opnd(0));
    ss = alu(ss, add_opc,  ecx_opnd,  edx_opnd);
    ss = mov(ss,  esi_opnd,  ecx_opnd);
    ss = mov(ss,  eax_opnd,  esi_opnd);
    ss = _not(ss,  eax_opnd);
    ss = mov(ss,  ebx_opnd,  M_Base_Opnd(edi_reg, +0x28) );
    ss = mul(ss,  ebx_opnd);
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edi_opnd,  M_Base_Opnd(ebp_reg, +0x8) );
    ss = alu(ss, sub_opc,  ecx_opnd,  ebx_opnd);
    ss = alu(ss, add_opc,  eax_opnd,  edi_opnd);
    ss = alu(ss, adc_opc,  edx_opnd,  ecx_opnd);
    ss = mov(ss,  edi_opnd,  edx_opnd);
    ss = alu(ss, and_opc,  edi_opnd,  ebx_opnd);
    ss = alu(ss, add_opc,  eax_opnd,  edi_opnd);
    ss = alu(ss, xor_opc,  edx_opnd,  edx_opnd);
    ss = jump8(ss, Imm_Opnd(size_8, 0x11)); // jmp end
//slower:
    ss = push(ss,  M_Base_Opnd(edi_reg, +0x2c) );
    ss = push(ss,  M_Base_Opnd(edi_reg, +0x28) );
    ss = push(ss,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = push(ss,  M_Base_Opnd(ebp_reg, +0x8) );
    ss = call(ss, (char *)do_lrem);
//end:

    ss = pop(ss,  edi_opnd);
    ss = pop(ss,  esi_opnd);
    ss = pop(ss,  ebx_opnd);
    ss = mov(ss,  esp_opnd,  ebp_opnd);
    ss = pop(ss,  ebp_opnd);

    ss = ret(ss,  Imm_Opnd(0x0c));

    assert((ss - stub) <= stub_size);
    addr = stub;

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_const_lrem_naked", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_const_lrem_naked", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump( stub, "getaddress__vm_const_lrem_naked", ss - stub);
#endif
    return addr;
} //getaddress__vm_const_lrem_naked

static void *getaddress__vm_const_ldiv_naked()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }

/******************************************************************************
 * fast long division in this function
 ******************************************************************************/
    const int stub_size = 420;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
    char* ss = stub ;

    ss = push(ss,  ebp_opnd);
    ss = mov(ss,  ebp_opnd,  esp_opnd);
    ss = alu(ss, sub_opc,  esp_opnd,  Imm_Opnd(0x4c)) ;
    ss = push(ss,  ebx_opnd);
    ss = push(ss,  esi_opnd);
    ss = push(ss,  edi_opnd);

    ss = mov(ss,  esi_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = alu(ss, or_opc,  esi_opnd,  esi_opnd) ;
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, 0x28)); // jne fast_64
    ss = mov(ss,  edi_opnd,  M_Base_Opnd(ebp_reg, +0x10) );
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(edi_reg, +0x1c) );
    ss = alu(ss, or_opc,  ecx_opnd,  ecx_opnd) ;
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0x21)); // jne fast_64_2
    /*******************************************************************************
     * Fast 32/32
     *******************************************************************************/
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +0x08) );
    ss = mov(ss,  esi_opnd,  eax_opnd );
    ss = mul(ss,  ecx_opnd) ;
    ss = alu(ss, sub_opc,  esi_opnd,  edx_opnd) ;
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(edi_reg, +0x0c) );
    ss = shift(ss, shr_opc,  esi_opnd) ;
    ss = alu(ss, add_opc,  edx_opnd,  esi_opnd) ;
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(edi_reg, +0x20) );
    ss = shift(ss, shr_opc,  edx_opnd) ;
    ss = mov(ss,  eax_opnd,  edx_opnd );
    ss = alu(ss, xor_opc,  edx_opnd,  edx_opnd) ;
    ss = jump32(ss, Imm_Opnd(0x130)); // jmp end
//fast_64:
    ss = mov(ss,  edi_opnd,  M_Base_Opnd(ebp_reg, +0x10) );
//fast_64_2:
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(edi_reg, +0x28) );
    ss = alu(ss, or_opc,  esi_opnd,  esi_opnd) ;
    ss = branch8(ss, Condition_L,  Imm_Opnd(size_8, 0x68)); // js fast_64_64
    ss = alu(ss, cmp_opc,  esi_opnd,  eax_opnd) ;
    ss = branch8(ss, Condition_AE,  Imm_Opnd(size_8, 0x64)); // jae fast_64_64
    /*******************************************************************************
     * Fast 64/32
     *******************************************************************************/
    ss = mov(ss,  ebx_opnd,  M_Base_Opnd(edi_reg, +0x30) );
    ss = mov(ss,  eax_opnd,  esi_opnd );
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(ebp_reg, +0x8) );
    ss = mov(ss,  ecx_opnd,  Imm_Opnd(0x20) );
    ss = alu(ss, sub_opc,  ecx_opnd,  ebx_opnd) ;
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0x5)); // je l_is_32
    ss = shift(ss, shld_opc,  eax_opnd,  edx_opnd) ;
    ss = shift(ss, shl_opc,  edx_opnd) ;
//l_is_32:
    ss = mov(ss,  ebx_opnd,  edx_opnd );
    ss = shift(ss, sar_opc,  ebx_opnd,  Imm_Opnd(0x1f)) ;
    ss = mov(ss,  esi_opnd,  M_Base_Opnd(edi_reg, +0x38) );
    ss = alu(ss, and_opc,  esi_opnd,  ebx_opnd) ;
    ss = alu(ss, add_opc,  esi_opnd,  edx_opnd) ;
    ss = mov(ss,  ecx_opnd,  eax_opnd );
    ss = alu(ss, cmp_opc,  eax_opnd,  ebx_opnd) ;
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0xb)); // je mul_zero
    ss = alu(ss, sub_opc,  eax_opnd,  ebx_opnd) ;
    ss = mov(ss,  ebx_opnd,  eax_opnd );
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(edi_reg, +0x34) );
    ss = mul(ss,  edx_opnd) ;
    ss = jump8(ss, Imm_Opnd(size_8, 0x4)); // jmp next1
//mul_zero:
    ss = alu(ss, xor_opc,  edx_opnd,  edx_opnd) ;
    ss = alu(ss, xor_opc,  eax_opnd,  eax_opnd) ;
//next1:
    ss = alu(ss, add_opc,  eax_opnd,  esi_opnd) ;
    ss = alu(ss, adc_opc,  edx_opnd,  Imm_Opnd(0)) ;
    ss = alu(ss, add_opc,  ecx_opnd,  edx_opnd) ;
    ss = mov(ss,  esi_opnd,  ecx_opnd );
    ss = mov(ss,  eax_opnd,  esi_opnd );
    ss = _not(ss,  eax_opnd) ;
    ss = mov(ss,  ebx_opnd,  M_Base_Opnd(edi_reg, +0x28) );
    ss = mul(ss,  ebx_opnd) ;
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edi_opnd,  M_Base_Opnd(ebp_reg, +0x8) );
    ss = alu(ss, sub_opc,  ecx_opnd,  ebx_opnd) ;
    ss = alu(ss, add_opc,  eax_opnd,  edi_opnd) ;
    ss = alu(ss, adc_opc,  edx_opnd,  ecx_opnd) ;
    ss = mov(ss,  ecx_opnd,  esi_opnd );
    ss = _not(ss,  ecx_opnd) ;
    ss = mov(ss,  eax_opnd,  edx_opnd );
    ss = alu(ss, sub_opc,  eax_opnd,  ecx_opnd) ;
    ss = alu(ss, xor_opc,  edx_opnd,  edx_opnd) ;
    ss = jump32(ss, Imm_Opnd(0xbe)); // jmp end
    /*******************************************************************************
     * Fast 64/64
     *******************************************************************************/
//fast_64_64
    ss = alu(ss, or_opc, esi_opnd, esi_opnd) ;
    ss = branch8(ss, Condition_GE, Imm_Opnd(size_8, 0x21)); // jge positive
    ss = mov(ss, edx_opnd, esi_opnd );
    ss = mov(ss, ecx_opnd, esi_opnd );
    ss = shift(ss, sar_opc, ecx_opnd, Imm_Opnd(0x1f)) ;
    ss = shift(ss, shr_opc, esi_opnd, Imm_Opnd(0x1f)) ;
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x0c), ecx_opnd );
    ss = mov(ss, eax_opnd, M_Base_Opnd(ebp_reg, +0x8) );
    ss = alu(ss, xor_opc, eax_opnd, ecx_opnd) ;
    ss = alu(ss, xor_opc, edx_opnd, ecx_opnd) ;
    ss = alu(ss, add_opc, eax_opnd, esi_opnd) ;
    ss = alu(ss, adc_opc, edx_opnd, Imm_Opnd(0) ) ;
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x04), edx_opnd );
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x08), eax_opnd );
    ss = jump8(ss,Imm_Opnd(size_8, 0x0e)); // jmp Cal_t1
//positive:
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x04), esi_opnd );
    ss = mov(ss, eax_opnd, M_Base_Opnd(ebp_reg, +0x8) );
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x08), eax_opnd );
    ss = alu(ss, xor_opc, esi_opnd, esi_opnd) ;
    ss = mov(ss, M_Base_Opnd(ebp_reg, -0x0c), esi_opnd );
//Cal_t1:
    ss = mov(ss, ecx_opnd, eax_opnd );
    ss = mov(ss, ebx_opnd, M_Base_Opnd(edi_reg, +0x0) );
    ss = mul(ss, ebx_opnd) ;
    ss = alu(ss, sub_opc, eax_opnd, Imm_Opnd(0x1) ) ;
    ss = alu(ss, sbb_opc, edx_opnd, Imm_Opnd(0x1) ) ;
    ss = alu(ss, add_opc, eax_opnd, esi_opnd ) ;
    ss = alu(ss, adc_opc, edx_opnd, Imm_Opnd(0) ) ;
    ss = mov(ss, edi_opnd, edx_opnd );
    ss = mov(ss, eax_opnd, M_Base_Opnd(ebp_reg, -0x4) );
    ss = mul(ss, ebx_opnd) ;
    ss = alu(ss, add_opc, eax_opnd, edi_opnd ) ;
    ss = alu(ss, adc_opc, edx_opnd, Imm_Opnd(0) ) ;
    ss = mov(ss, edi_opnd, edx_opnd );
    ss = mov(ss, esi_opnd, eax_opnd );
    ss = mov(ss, eax_opnd, ecx_opnd );
    ss = mov(ss, ebx_opnd, M_Base_Opnd(ebp_reg, +0x10) );
    ss = mov(ss, ebx_opnd, M_Base_Opnd(ebx_reg, +0x04) );
    ss = mul(ss, ebx_opnd) ;
    ss = alu(ss, add_opc, eax_opnd, esi_opnd ) ;
    ss = alu(ss, adc_opc, edx_opnd, edi_opnd ) ;
    ss = mov(ss, edi_opnd, Imm_Opnd(0) );
    ss = alu(ss, adc_opc, edi_opnd, Imm_Opnd(0) ) ;
    ss = mov(ss, esi_opnd, edx_opnd );
    ss = mov(ss, eax_opnd, M_Base_Opnd(ebp_reg, -0x4) );
    ss = mul(ss, ebx_opnd) ;
    ss = alu(ss, add_opc, eax_opnd, esi_opnd ) ;
    ss = alu(ss, adc_opc, edx_opnd, edi_opnd ) ;
    ss = mov(ss, edi_opnd, M_Base_Opnd(ebp_reg, -0x0c) );
    ss = mov(ss, ebx_opnd, edi_opnd );
    ss = _not(ss, edi_opnd) ;
    ss = alu(ss, xor_opc, eax_opnd, edi_opnd) ;
    ss = alu(ss, xor_opc, edx_opnd, edi_opnd) ;
    ss = mov(ss, edi_opnd, M_Base_Opnd(ebp_reg, +0x08) );
    ss = mov(ss, esi_opnd, M_Base_Opnd(ebp_reg, +0x0c) );
    ss = alu(ss, add_opc, eax_opnd, edi_opnd ) ;
    ss = alu(ss, adc_opc, edx_opnd, esi_opnd ) ;
    ss = mov(ss, ecx_opnd, M_Base_Opnd(ebp_reg, +0x10) );
    ss = mov(ss, ecx_opnd, M_Base_Opnd(ecx_reg, +0x10) );
    ss = alu(ss, cmp_opc, ecx_opnd, Imm_Opnd(0x20) ) ;
    ss = branch8(ss, Condition_GE, Imm_Opnd(size_8, 0x07)); // jge biger_than_32
    ss = shift(ss, shrd_opc, eax_opnd, edx_opnd ) ;
    ss = shift(ss, sar_opc, edx_opnd ) ;
    ss = jump8(ss,Imm_Opnd(size_8, 0x0a)); // jmp next
//biger_than_32:
    ss = alu(ss, sub_opc,  ecx_opnd,  Imm_Opnd(0x20) ) ;
    ss = mov(ss,  eax_opnd,  edx_opnd );
    ss = shift(ss, sar_opc,  edx_opnd,  Imm_Opnd(0x1f) ) ;
    ss = shift(ss, sar_opc,  eax_opnd ) ;
//next:
    ss = alu(ss, sub_opc,  eax_opnd,  ebx_opnd ) ;
    ss = alu(ss, sbb_opc,  edx_opnd,  ebx_opnd ) ;
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(ebp_reg, +0x10) );
    ss = mov(ss,  ecx_opnd,  M_Base_Opnd(ecx_reg, +0x14) );
    ss = test(ss,  ecx_opnd,  ecx_opnd) ;
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0x08)); // je end
    ss = alu(ss, xor_opc,  eax_opnd,  ecx_opnd) ;
    ss = alu(ss, xor_opc,  edx_opnd,  ecx_opnd) ;
    ss = alu(ss, sub_opc,  eax_opnd,  ecx_opnd) ;
    ss = alu(ss, sbb_opc,  edx_opnd,  ecx_opnd) ;
    
//end:
    ss = pop(ss,  edi_opnd);
    ss = pop(ss,  esi_opnd);
    ss = pop(ss,  ebx_opnd);
    ss = mov(ss,  esp_opnd,  ebp_opnd);
    ss = pop(ss,  ebp_opnd);
    ss = ret(ss,  Imm_Opnd(0x0c));

    assert((ss - stub) <= stub_size);
    addr = stub;

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_const_ldiv_naked", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_const_ldiv_naked", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_const_ldiv_naked", ss - stub);
#endif
    return addr;
} //getaddress__vm_ldiv_naked


static double vm_rt_ddiv(double a, double b)
{
    double result = a / b;
    return result;
} //vm_rt_ddiv

static int32 d2i_infinite(double d)
{
#ifdef __INTEL_COMPILER
#pragma warning(disable: 4146)
#endif
    if(_isnan(d)) {
            return 0;
        } else if(d > (double)2147483647) {
            return 2147483647;      // maxint
        } else if(d < (double)(-2147483647-1)) {
            return (-2147483647-1);     // minint
        } else {
            ABORT("The above should exhaust all possibilities");
            return 0;
        }
#ifdef __INTEL_COMPILER
#pragma warning(default: 4146)
#endif
}

static short fpstatus = 0x0e7f;
void *getaddress__vm_d2i()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 55;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = fld(ss,  M_Base_Opnd(esp_reg, 4), 1);

    ss = fnstcw(ss, M_Base_Opnd(esp_reg, -8) );
    ss = fldcw(ss, M_Opnd((unsigned)&fpstatus));


    ss = fist(ss, M_Base_Opnd(esp_reg, -4), false, true);
    ss = fldcw(ss, M_Base_Opnd(esp_reg, -8) );
    ss = mov(ss, eax_opnd, M_Base_Opnd(esp_reg, -4) );
    ss = alu(ss, cmp_opc, eax_opnd, Imm_Opnd(0x80000000) );
    ss = branch8(ss, Condition_Z, Imm_Opnd(size_8, 0));
    char *backpatch_address__infinite = ((char *)ss) - 1;
    ss = ret(ss,  Imm_Opnd(8));
    
    signed offset = (signed)ss-(signed)backpatch_address__infinite - 1;
    *backpatch_address__infinite = (char)offset;
    ss = push(ss,  M_Base_Opnd(esp_reg, 8));
    ss = push(ss,  M_Base_Opnd(esp_reg, 8));
    ss = call(ss, (char *)d2i_infinite);
    ss = alu(ss, add_opc,  esp_opnd,  Imm_Opnd(8));
    ss = ret(ss,  Imm_Opnd(8));

    addr = stub;
    assert((ss - stub) <= stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_d2i", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_d2i", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_d2i", ss - stub);
#endif
    return addr;    
} //getaddress__vm_d2i

void *getaddress__vm_d2l()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 45;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = push(ss,  ebp_opnd);
    ss = mov(ss,  ebp_opnd,  esp_opnd);
    ss = push(ss,  ebx_opnd);
    ss = push(ss,  esi_opnd);
    ss = push(ss,  edi_opnd);


    ss = fld(ss,  M_Base_Opnd(ebp_reg, +0x14), 1);
    ss = wait(ss);
    ss = fnstcw(ss,  M_Base_Opnd(ebp_reg, +8) );
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +8), size_16);
    ss = alu(ss, or_opc,  eax_opnd,  Imm_Opnd(size_16, 0xc7f), size_16);
    ss = mov(ss,  M_Base_Opnd(ebp_reg, +0x0c),  eax_opnd, size_16);

    ss = fldcw(ss,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = fist(ss,  M_Base_Opnd(ebp_reg, +0x0c), true, true);
    ss = fldcw(ss,  M_Base_Opnd(ebp_reg, +8) );
    
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(ebp_reg, +0x10) );

    ss = pop(ss,  edi_opnd);
    ss = pop(ss,  esi_opnd);
    ss = pop(ss,  ebx_opnd);
    ss = pop(ss,  ebp_opnd);

    ss = ret(ss);

    addr = stub;
    assert((ss - stub) <= stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_d2l", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_d2l", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_d2l", ss - stub);
#endif
   return addr;       
} //getaddress__vm_d2l


static int64 __stdcall vm_d2l(double d) stdcall__;

static int64 __stdcall vm_d2l(double d)
{
    assert(!hythread_is_suspend_enabled());

#ifdef VM_STATS
    vm_stats_total.num_d2l++;
#endif

    int64 result;

    int64 (*gad2l)(int, int, int, double);
    gad2l = (int64 ( *)(int, int, int, double) )getaddress__vm_d2l();

    result = gad2l(0, 0, 0, d);

#if defined (__INTEL_COMPILER) || defined (_MSC_VER)
#pragma warning( push )
#pragma warning (disable:4146)// disable warning 4146: unary minus operator applied to unsigned type, result still unsigned
#endif
    // 0x80000000 is the integer indefinite value
    if(0x80000000 == *(uint32*)((char*)&result+4)) {

#ifdef PLATFORM_POSIX
        if (isnan(d))
            return 0;
#else
        if (_isnan(d))
            return 0;
#endif 

        if(d >= (double)(__INT64_C(0x7fffffffffffffff))) {
            return __INT64_C(0x7fffffffffffffff);      // maxint
        } else if(d < (double)-__INT64_C(0x8000000000000000)) {
            return -__INT64_C(0x8000000000000000);     // minint
        } else {
            ABORT("The above should exhaust all possibilities");
            return result;
        }

    } else {
        return result;
    }

#if defined (__INTEL_COMPILER) || defined (_MSC_VER)
#pragma warning( pop )
#endif
} //vm_d2l

static int32 f2i_infinite(float f)
{
#ifdef __INTEL_COMPILER
#pragma warning(disable: 4146)
#endif
    if(_isnan(f)) {
            return 0;
        } else if(f > (double)2147483647) {
            return 2147483647;      // maxint
        } else if(f < (double)(-2147483647-1)) {
            return (-2147483647-1);     // minint
        } else {
            ABORT("The above should exhaust all possibilities");
            return 0;
        }
#ifdef __INTEL_COMPILER
#pragma warning(default: 4146)
#endif
}

void *getaddress__vm_f2i()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 64;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = fld(ss,  M_Base_Opnd(esp_reg, 4), 0);
    ss = wait(ss); //I dont think this is necessary, cuz fist will guarantee the context
    ss = fnstcw(ss, M_Base_Opnd(esp_reg, -8) );
    ss = mov(ss, eax_opnd, M_Base_Opnd(esp_reg, -8), size_16);
    ss = alu(ss, or_opc, eax_opnd, Imm_Opnd(size_16, 0xc00), size_16);
    ss = mov(ss, M_Base_Opnd(esp_reg, -0xc), eax_opnd, size_16);
    ss = fldcw(ss, M_Base_Opnd(esp_reg, -0xc) );
    ss = fist(ss, M_Base_Opnd(esp_reg, -4), false, true);
    ss = fldcw(ss, M_Base_Opnd(esp_reg, -8) );
    ss = mov(ss, eax_opnd, M_Base_Opnd(esp_reg, -4) );
    ss = alu(ss, cmp_opc, eax_opnd, Imm_Opnd(0x80000000) );
    ss = branch8(ss, Condition_Z, Imm_Opnd(size_8, 0));
    char *backpatch_address__infinite = ((char *)ss) - 1;
    ss = ret(ss,  Imm_Opnd(4));

    signed offset=0;

    offset = (signed)ss-(signed)backpatch_address__infinite - 1;
    *backpatch_address__infinite = (char)offset;
    ss = push(ss,  M_Base_Opnd(esp_reg, 4));
    ss = call(ss, (char *)f2i_infinite);
    ss = alu(ss, add_opc,  esp_opnd,  Imm_Opnd(4));
    ss = ret(ss,  Imm_Opnd(4));

    addr = stub;
    assert((ss - stub) <= stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_f2i", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_f2i", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_f2i", ss - stub);
#endif
    return addr;    
} //getaddress__vm_f2i

static void *getaddress__vm_f2l()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 100;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = push(ss,  ebp_opnd);
    ss = mov(ss,  ebp_opnd,  esp_opnd);
    ss = push(ss,  ebx_opnd);
    ss = push(ss,  esi_opnd);
    ss = push(ss,  edi_opnd);

    ss = fld(ss,  M_Base_Opnd(ebp_reg, +0x14), 0);
    ss = wait(ss);
    ss = fnstcw(ss,  M_Base_Opnd(ebp_reg, +8) );
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +8), size_16);
    ss = alu(ss, or_opc,  eax_opnd,  Imm_Opnd(size_16, 0xc7f), size_16);
    ss = mov(ss,  M_Base_Opnd(ebp_reg, +0x0c),  eax_opnd, size_16);

    ss = fldcw(ss,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = fist(ss,  M_Base_Opnd(ebp_reg, +0x0c), true, true);
    ss = fldcw(ss,  M_Base_Opnd(ebp_reg, +8) );
    
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(ebp_reg, +0x0c) );
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(ebp_reg, +0x10) );

    ss = pop(ss,  edi_opnd);
    ss = pop(ss,  esi_opnd);
    ss = pop(ss,  ebx_opnd);
    ss = pop(ss,  ebp_opnd);

    ss = ret(ss);

    addr = stub;
    assert((ss - stub) < stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_f2l", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_f2l", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_f2l", ss - stub);
#endif
    return addr;    
} //getaddress__vm_f2l


static int64 __stdcall vm_f2l(float f) stdcall__;

static int64 __stdcall vm_f2l(float f)
{
    assert(!hythread_is_suspend_enabled());

#ifdef VM_STATS
    vm_stats_total.num_f2l++;
#endif

    int64 result;

    int64 (*gaf2l)(int, int, int, float);
    gaf2l = (int64 ( *)(int, int, int, float) )getaddress__vm_f2l();

    result = gaf2l(0, 0, 0, f);

#if defined (__INTEL_COMPILER) || defined (_MSC_VER)
#pragma warning( push )
#pragma warning (disable:4146)// disable warning 4146: unary minus operator applied to unsigned type, result still unsigned
#endif
    // 0x80000000 is the integer indefinite value
    if(0x80000000 == *(uint32*)((char*)&result+4)) {
        if(_isnan(f)) {
            return 0;
        } else if(f >= __INT64_C(0x7fffffffffffffff) ) {
            return __INT64_C(0x7fffffffffffffff);      // maxint
        } else if(f < (double)__INT64_C(-0x8000000000000000) ) {
            return __INT64_C(-0x8000000000000000);     // minint
        } else {
            ABORT("The above should exhaust all possibilities");
            return result;
        }
    } else {
        return result;
    }
#if defined (__INTEL_COMPILER) || defined (_MSC_VER)
#pragma warning( pop )
#endif
} //vm_f2l


//
// If fprem succeeds in producing a remainder that is less than the
// modulus, the function is complete and the C2 flag is cleared.
// Otherwise, C2 is set, and the result on the top of the fp stack
// is the partial remainder.  We need to re-execute the fprem instruction
// (using the partial remainder) until C2 is cleared.
//


void *getaddress__vm_frem()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 24;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = fld(ss,  M_Base_Opnd(esp_reg, 4), 0);
    ss = fld(ss,  M_Base_Opnd(esp_reg, 8), 0);

//rem_not_complete:
    int rem_not_complete = (int)ss;

    ss = fprem(ss);
    ss = fnstsw(ss); 
    ss = alu(ss, and_opc,  eax_opnd,  Imm_Opnd(size_16, 0x400), size_16);

    int offset = rem_not_complete - (int)ss - 2;
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, offset)); // jne rem_not_complete

    ss = fst(ss, 1, true);

    ss = ret(ss, Imm_Opnd(8));

    addr = stub;
    assert((ss - stub) < stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_frem", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_frem", stub, stub_size);
    }

#ifndef NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_frem", ss - stub);
#endif
    return addr;    
} //getaddress__vm_frem



void *getaddress__vm_drem()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }

    const int stub_size = 24;
    char *stub = (char *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);
#ifdef _DEBUG
    memset(stub, 0xcc /*int 3*/, stub_size);
#endif
    char *ss = stub;    

    ss = fld(ss,  M_Base_Opnd(esp_reg, 4), 1);  // 2nd arg: denominator
    ss = fld(ss,  M_Base_Opnd(esp_reg, 12), 1); // 1st arg: numerator
    // at this point, st0 has numerator, st1 has denominator

//rem_not_complete:
    int rem_not_complete = (int)ss;

    ss = fprem(ss);
    ss = fnstsw(ss); 
    ss = alu(ss, and_opc,  eax_opnd,  Imm_Opnd(size_16, 0x400), size_16);

    int offset = rem_not_complete - (int)ss - 2;
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, offset)); // jne rem_not_complete

    // st0 has the result, st1 has the original denominator
    // Need to pop st(1) before returning.
    ss = fst(ss, 1, true);

    ss = ret(ss, Imm_Opnd(0x10));

    addr = stub;
    assert((ss - stub) < stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
    {
        jvmti_add_dynamic_generated_code_chunk("vm_drem", stub, stub_size);
        jvmti_send_dynamic_code_generated_event("vm_drem", stub, stub_size);
    }

#ifndef  NDEBUG
    if (dump_stubs)
        dump(stub, "getaddress__vm_drem", ss - stub);
#endif
    return addr;    
} //getaddress__vm_drem

#ifdef VM_STATS // exclude remark in release mode (defined but not used)
// Return the log base 2 of the integer operand. If the argument is less than or equal to zero, return zero.
static int get_log2(int value)
{
    register int n = value;
    register int result = 0;

    while (n > 1) {
        n = n >> 1;
        result++;
    }
    return result;
} //get_log2
#endif

static void vm_rt_char_arraycopy_no_exc(ManagedObject *src,
                                         int32 srcOffset,
                                         ManagedObject *dst,
                                         int32 dstOffset,
                                         int32 length)
{
    // 20030303 Use a C loop to (hopefully) speed up short array copies.

    // Check that the array references are non-null.
    assert(src && dst); 
    // Check that the arrays are arrays of 16 bit characters.
    Class * UNUSED src_class = src->vt()->clss;
    assert(src_class);
    Class * UNUSED dst_class = dst->vt()->clss;
    assert(dst_class);
    assert((src_class->is_array) && (dst_class->is_array));
    assert((src_class->is_array_of_primitives) && (dst_class->is_array_of_primitives));
    assert(strcmp(src_class->name->bytes, "[C") == 0);
    assert(strcmp(dst_class->name->bytes, "[C") == 0);
    // Check the offsets
    assert(srcOffset >= 0);
    assert(dstOffset >= 0);
    assert(length >= 0);
    assert((srcOffset + length) <= get_vector_length((Vector_Handle)src));
    assert((dstOffset + length) <= get_vector_length((Vector_Handle)dst));

    tmn_suspend_disable();       // vvvvvvvvvvvvvvvvvvv

    register uint16 *dst_addr = get_vector_element_address_uint16(dst, dstOffset);
    register uint16 *src_addr = get_vector_element_address_uint16(src, srcOffset);

#ifdef VM_STATS
    vm_stats_total.num_char_arraycopies++;
    if (dst_addr == src_addr) {
        vm_stats_total.num_same_array_char_arraycopies++;
    }
    if (srcOffset == 0) {
        vm_stats_total.num_zero_src_offset_char_arraycopies++;
    }
    if (dstOffset == 0) {
        vm_stats_total.num_zero_dst_offset_char_arraycopies++;
    }
    if ((((POINTER_SIZE_INT)dst_addr & 0x7) == 0) && (((POINTER_SIZE_INT)src_addr & 0x7) == 0)) {
        vm_stats_total.num_aligned_char_arraycopies++;
    }
    vm_stats_total.total_char_arraycopy_length += length;
    vm_stats_total.char_arraycopy_count[get_log2(length)]++;
#endif //VM_STATS

    // 20030219 The length threshold 32 here works well for SPECjbb and should be reasonable for other applications.
    if (length < 32) {
        register int i;
        if (src_addr > dst_addr) {
            for (i = length;  i > 0;  i--) {
                *dst_addr++ = *src_addr++;
            }
        } else {
            // copy down, from higher address to lower
            src_addr += length-1;
            dst_addr += length-1;
            for (i = length;  i > 0;  i--) {
                *dst_addr-- = *src_addr--;
            }
        }
    } else {
        memmove(dst_addr, src_addr, (length * sizeof(uint16)));
    }

    tmn_suspend_enable();        // ^^^^^^^^^^^^^^^^^^^
} //vm_rt_char_arraycopy_no_exc


static int32 vm_rt_imul_common(int32 v1, int32 v2)
{
    return v1 * v2;
} //vm_rt_imul_common



static int32 vm_rt_idiv_common(int32 v1, int32 v2)
{
    assert(v2);
    return v1 / v2;
} //vm_rt_idiv_common



static int32 vm_rt_irem_common(int32 v1, int32 v2)
{
    assert(v2);
    return v1 % v2;
} //vm_rt_irem_common


void *get_generic_rt_support_addr_ia32(VM_RT_SUPPORT f)
{
    switch(f) {
    case VM_RT_F2I:
        return getaddress__vm_f2i();
    case VM_RT_F2L:
        return (void *)vm_f2l;
    case VM_RT_D2I:
        return getaddress__vm_d2i();
    case VM_RT_D2L:
        return (void *)vm_d2l; 
    case VM_RT_LSHL:
        return getaddress__vm_lshl_naked();
    case VM_RT_LSHR:
        return getaddress__vm_lshr_naked();
    case VM_RT_LUSHR:
        return getaddress__vm_lushr_naked();
    case VM_RT_FREM:
        return getaddress__vm_frem();
    case VM_RT_DREM:
        return getaddress__vm_drem();
    case VM_RT_LMUL:
        return (void *)vm_lmul;
#ifdef VM_LONG_OPT
    case VM_RT_LMUL_CONST_MULTIPLIER:
        return (void *)vm_lmul_const_multiplier;
#endif
    case VM_RT_CONST_LDIV:
        return getaddress__vm_const_ldiv_naked() ;
    case VM_RT_CONST_LREM:
        return getaddress__vm_const_lrem_naked() ;
    case VM_RT_DDIV:
        return (void *)vm_rt_ddiv;

    case VM_RT_IMUL:
        return (void *)vm_rt_imul_common;
    case VM_RT_IDIV:
        return (void *)vm_rt_idiv_common;
    case VM_RT_IREM:
        return (void *)vm_rt_irem_common;
    case VM_RT_CHAR_ARRAYCOPY_NO_EXC:
        return (void *)vm_rt_char_arraycopy_no_exc;

    default:
        ABORT("Unexpected helper id");
        return 0;
    }
}
