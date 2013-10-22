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
 * @author Artem Aliev
 * @version $Revision: 1.1.2.11 $
 */  

/** 
 * @file thread_helpers.cpp
 * @brief Set of VM helpers
 *
 * This file contatins the set of "VM helpers" which help to optimize monitors perforamance
 * in the code generated by JIT compiler. Typically, these functions will be called by JIT,
 * but VM also could also use them with care.
 */  

#include <open/hythread_ext.h>
#include <open/thread_helpers.h>
#include "thread_private.h"
#include "open/thread_externals.h"
#include "open/jthread.h"

#include <assert.h>


/**
  *  Generates tmn_self() call.
  *  The code should not contains safepoint.
  *  The code uses and doesn't restore eax register.
  *  
  *  @return tm_self() in eax register
  */
char* gen_hythread_self_helper(char *ss) {
#ifdef FS14_TLS_USE
    //ss = mov(ss,  eax_opnd,  M_Base_Opnd(fs_reg, 0x14));
    *ss++ = (char)0x64;
    *ss++ = (char)0xa1;
    *ss++ = (char)0x14;
    *ss++ = (char)0x00;
    *ss++ = (char)0x00;
    *ss++ = (char)0x00;
#else
    ss = call(ss, (char *)hythread_self);
#endif
    return ss;
}


/**
  *  Generates fast path of monitor enter
  *  the code should not contains safepoint.
  *  
  *  @param[in] ss buffer to put the assembly code to
  *  @param[in] input_param1 register which should point to the object lockword.
  *  If input_param1 == ecx it reduces one register mov.
  *  the code use and do not restore ecx, edx, eax registers
  *  
  *  @return 0 if success in eax register
  */
char* gen_monitorenter_fast_path_helper(char *ss, const R_Opnd & input_param1) {

    if (&input_param1 != &ecx_opnd) {
        ss = mov(ss, ecx_opnd,  input_param1);
    }
#ifdef ASM_MONITOR_HELPER
    signed offset2; 
    //get self_id 
    ss = gen_hythread_self_helper(ss);
    ss = mov(ss,  edx_opnd,  M_Base_Opnd(eax_reg, (uint32)&((HyThread *)0)->thread_id) );
#ifdef LOCK_RESERVATION
    //get lock_id
    ss = mov(ss, eax_opnd,  M_Base_Opnd(ecx_reg, 0));
   // move thread_id to AX
    ss = shift(ss, ror_opc,  eax_opnd,  Imm_Opnd(16));

    // test this recursion call
    ss = alu(ss, cmp_opc,  edx_opnd,  eax_opnd, size_16);
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0));
    char *backpatch_address__recursion_inc = ((char *)ss) - 1;

    // test the lock is busy  
    ss = test(ss,  eax_opnd,  eax_opnd, size_16);
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, 0));
    char *backpatch_address__inline_monitor_failed = ((char *)ss) - 1;
#else
    ss = alu(ss, xor_opc,  eax_opnd,  eax_opnd);
#endif
    // the lock is free or  not reserved
    ss = prefix(ss, lock_prefix);   
    ss = cmpxchg(ss,  M_Base_Opnd(ecx_reg, 2),  edx_opnd, size_16);
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, 0));
    char *backpatch_address__inline_monitor_failed2 = ((char *)ss) - 1;

#ifdef LOCK_RESERVATION
    // if this is initial reservation also increase the recursion
    ss = mov(ss, edx_opnd, eax_opnd);
    // eax stil ROR so ROR the mask
    ss = alu(ss, and_opc,  edx_opnd,  Imm_Opnd(0x0400ffff));
    ss = test(ss,  edx_opnd,  edx_opnd);
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0));
    char *backpatch_address__recursion_inc2 = ((char *)ss) - 1;
#endif
    ss = ret(ss,  Imm_Opnd(4));

#ifdef LOCK_RESERVATION
   // increase recurison brench
    signed offset = (signed)ss - (signed)backpatch_address__recursion_inc - 1;
    *backpatch_address__recursion_inc = (char)offset;
    
    // test recursion overflow
    // eax stil ROR so ROR the mask
    ss = alu(ss, cmp_opc,  eax_opnd,  Imm_Opnd(0xf4000000));
    ss = branch8(ss, Condition_A,  Imm_Opnd(size_8, 0));
    char *backpatch_address__inline_monitor_failed3 = ((char *)ss) - 1;

    offset2 = (signed)ss - (signed)backpatch_address__recursion_inc2 - 1;
    *backpatch_address__recursion_inc2 = (char)offset2;

   // restore lock_id
    ss = shift(ss, ror_opc,  eax_opnd,  Imm_Opnd(16));
    ss = alu(ss, add_opc,  eax_opnd,  Imm_Opnd(size_16, 0x800), size_16);
    ss = mov(ss,  M_Base_Opnd(ecx_reg, 0), eax_opnd, size_16);

    ss = ret(ss,  Imm_Opnd(4));

    offset = (signed)ss - (signed)backpatch_address__inline_monitor_failed - 1;
    *backpatch_address__inline_monitor_failed = (char)offset;
    offset = (signed)ss - (signed)backpatch_address__inline_monitor_failed3 - 1;  
    *backpatch_address__inline_monitor_failed3 = (char)offset;
#endif
    offset2 = (signed)ss - (signed)backpatch_address__inline_monitor_failed2 - 1;
    *backpatch_address__inline_monitor_failed2 = (char)offset2;


#endif //ASM_MONITOR_HELPER
    // the second attempt to lock monitor
    ss = push(ss,  ecx_opnd);
    ss = call(ss, (char *)hythread_thin_monitor_try_enter);
    ss = alu(ss, add_opc, esp_opnd, Imm_Opnd(4)); // pop parameters

    return ss;
}

/**
  *  Generates slow path of monitor enter.
  *  This code could block on monitor and contains safepoint.
  *  The appropriate m2n frame should be generated and
  *  
  *  @param[in] ss buffer to put the assembly code to
  *  @param[in] input_param1 register should point to the jobject(handle)
  *  If input_param1 == eax it reduces one register mov.
  *  the code use and do not restore ecx, edx, eax registers
  *  @return 0 if success in eax register
  */
char* gen_monitorenter_slow_path_helper(char *ss, const R_Opnd & input_param1) {
    if (&input_param1 != &eax_opnd) {
        ss = mov(ss, eax_opnd,  input_param1);
    }

    ss = push(ss, eax_opnd); // push the address of the handle
    ss = call(ss, (char *)jthread_monitor_enter);
    ss = alu(ss, add_opc, esp_opnd, Imm_Opnd(4)); // pop parameters
    return ss;
}

/**
  *  Generates monitor exit.
  *  The code should not contain safepoints.
  *  
  *  @param[in] ss buffer to put the assembly code to
  *  @param[in] input_param1 register should point to the lockword in object header.
  *  If input_param1 == ecx it reduce one register mov.
  *  The code use and do not restore eax registers.
  *  @return 0 if success in eax register
  */
char* gen_monitor_exit_helper(char *ss, const R_Opnd & input_param1) {
    if (&input_param1 != &ecx_opnd) {
        ss = mov(ss, ecx_opnd,  input_param1);
    }
#ifdef ASM_MONITOR_HELPER
    ss = mov(ss, eax_opnd,  M_Base_Opnd(ecx_reg, 0));
    ss = mov(ss, edx_opnd,  eax_opnd);
    ss = alu(ss, and_opc,  eax_opnd,  0x8000f800);
    ss = test(ss,  eax_opnd,   eax_opnd);
    ss = branch8(ss, Condition_Z,  Imm_Opnd(size_8, 0));
    char *backpatch_address__thin_monitor = ((char *)ss) - 1;
    ss = alu(ss, and_opc,  eax_opnd,  0x80000000);
    ss = test(ss,  eax_opnd,   eax_opnd);
    ss = branch8(ss, Condition_NZ,  Imm_Opnd(size_8, 0));
    char *backpatch_address__fat_monitor = ((char *)ss) - 1;
    
    // recursion or reservation => dec recursion count
   ss = alu(ss, sub_opc,  edx_opnd, Imm_Opnd(0x800));
   ss = mov(ss, M_Base_Opnd(ecx_reg,0),  edx_opnd);
   ss = ret(ss,  Imm_Opnd(4));

    signed offset = (signed)ss - (signed)backpatch_address__thin_monitor - 1;
    *backpatch_address__thin_monitor = (char)offset;
    ss = mov(ss, M_Base_Opnd(ecx_reg, 2), Imm_Opnd(size_16, 0), size_16);
    ss = ret(ss,  Imm_Opnd(4));


   offset = (signed)ss - (signed)backpatch_address__fat_monitor - 1;
   *backpatch_address__fat_monitor = (char)offset;

#endif

    ss = push(ss,  ecx_opnd);
    ss = call(ss, (char *)hythread_thin_monitor_exit);
    ss = alu(ss, add_opc, esp_opnd, Imm_Opnd(4)); // pop parameters
    return ss;
}

/** 
  *  Generates slow path of monitor exit.
  *  This code could block on monitor and contains safepoint.
  *  The appropriate m2n frame should be generated and
  *  
  *  @param[in] ss buffer to put the assembly code to
  *  @param[in] input_param1 register should point to the jobject(handle)
  *  If input_param1 == eax it reduces one register mov.
  *  the code use and do not restore ecx, edx, eax registers
  *  @return 0 if success in eax register
  */
char* gen_monitorexit_slow_path_helper(char *ss, const R_Opnd & input_param1) {
    if (&input_param1 != &eax_opnd) { 
        ss = mov(ss, eax_opnd,  input_param1);
    }
    
    ss = push(ss, eax_opnd); // push the address of the handle
    ss = call(ss, (char *)jthread_monitor_exit);
    ss = alu(ss, add_opc, esp_opnd, Imm_Opnd(4)); // pop parameters
    return ss;
}   

/**
  * Generates fast accessor to the TLS for the given key.<br>
  * Example:
  * <pre><code>
  * get_thread_ptr = get_tls_helper(vm_thread_block_key);
  * ...
  * self = get_thread_ptr();
  * </code></pre>
  *
  * @param[in] key TLS key
  * @return fast accessor to key, if one exist
  */
fast_tls_func* get_tls_helper(hythread_tls_key_t key) {
    //     return tm_self_tls->thread_local_storage[key];
    unsigned key_offset = (unsigned)&(((hythread_t)(0))->thread_local_storage[key]);
        
    const int stub_size = 126;
    char *stub = (char *)malloc(stub_size);
    memset(stub, 0xcc /*int 3*/, stub_size);

    char *ss = stub;
    
    ss = gen_hythread_self_helper(ss);
    ss = mov(ss,  eax_opnd,  M_Base_Opnd(eax_reg, key_offset));
    ss = ret(ss,  Imm_Opnd(0));

    assert((ss - stub) < stub_size);

    return (fast_tls_func*) stub;
}