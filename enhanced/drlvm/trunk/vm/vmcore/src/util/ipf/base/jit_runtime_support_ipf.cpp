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
 * @author Intel, Evgueni Brevnov, Ivan Volosyuk
 * @version $Revision: 1.1.2.1.4.4 $
 */  
//



//MVM
#include <iostream>

#ifdef PLATFORM_POSIX
#include <unistd.h>
#endif

using namespace std;

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <float.h>
#include <math.h>

#include "open/types.h"
#include "open/gc.h"
#include "open/vm_util.h"

#include "environment.h"
#include "Class.h"
#include "object_generic.h"
#include "object_layout.h"
#include "mon_enter_exit.h"
#include "ini.h"
#include "nogc.h"
#include "compile.h"
#include "exceptions.h"
#include "exceptions_jit.h"
#include "vm_strings.h"
#include "vm_arrays.h"
#include "vm_threads.h"
#include "port_malloc.h"
#include "jni_utils.h" // This is for readInternal override

#include "lil.h"
#include "lil_code_generator.h"
#include "jit_runtime_support_common.h"
#include "jit_runtime_support.h"
#include "m2n.h"
#include "../m2n_ipf_internal.h"
#include "merced.h"
#include "Code_Emitter.h"
#include "stub_code_utils.h"
#include "vm_ipf.h"

#define LOG_DOMAIN "vm.helpers"
#include "cxxlog.h"

#include "dump.h"
#include "vm_stats.h"

void gen_convert_managed_to_unmanaged_null_ipf(Emitter_Handle emitter,
                                               unsigned reg);
void gen_convert_unmanaged_to_managed_null_ipf(Emitter_Handle emitter,
                                               unsigned reg);

///////// begin arithmetic helpers

int32 vm_rt_imul(int32 a, int32 b);
int64 vm_rt_lmul(int64 a, int64 b);
int32 vm_rt_irem(int32 a, int32 b);
int64 vm_rt_lrem(int64 a, int64 b);
int32 vm_rt_idiv(int32 a, int32 b);
int64 vm_rt_ldiv(int64 a, int64 b);
float vm_rt_frem(float a, float b);
float vm_rt_fdiv(float a, float b);
double vm_rt_drem(double a, double b);
double vm_rt_ddiv(double a, double b);
int32 vm_rt_f2i(float f);
int64 vm_rt_f2l(float f);
void *get_vm_rt_int_div_address_compactor(void *func, char *stub_name);
void *get_vm_rt_lrem_address();
void *get_vm_rt_ldiv_address();
void *get_vm_rt_irem_address();
void *get_vm_rt_idiv_address();

///////// end arithmetic helpers


///////// begin exceptions

void *gen_vm_rt_exception_throw(Class *exc, char *stub_name);
void gen_vm_rt_athrow_internal_compactor(Merced_Code_Emitter &emitter);
void *get_vm_rt_athrow_naked_compactor();
void *get_vm_rt_idx_out_of_bounds_address();
void *get_vm_rt_array_store_exception_address();
void *get_vm_rt_null_ptr_exception_address();
void *get_vm_rt_divide_by_zero_exception_address();

///////// end exceptions


jlong get_current_time(); // This is for system_currenttimemillis override


//////////////////////////////////////////////////////////////////////
// Object allocation
//////////////////////////////////////////////////////////////////////

static void unimplemented_rt_support_func1(int f, const char *name)
{
    LDIE(34, "This runtime support function is not implemented: f={0}, {1}" << f << name);
} //unimplemented_rt_support_func1



static void unimplemented_rt_support_func()
{
    unimplemented_rt_support_func1(-1, "default");
} //unimplemented_rt_support_func



static ManagedObject* vm_rt_ldc_string(Class *clss, unsigned cp_index)
{
    ManagedObject* s = vm_instantiate_cp_string_slow(clss, cp_index);
    return s;
} //vm_rt_ldc_string




/////////////////////////////////////////////////////////////////
// begin memory allocation


extern "C" ManagedObject *vm_rt_new_resolved(Class *c)
{
    assert(!hythread_is_suspend_enabled());
    assert(strcmp(c->get_name()->bytes, "java/lang/Class")); 
#ifdef VM_STATS
    VM_Statistics::get_vm_stats().num_class_alloc_new_object++;
    c->instance_allocated(c->get_instance_data_size());
#endif //VM_STATS
    return (ManagedObject *)vm_malloc_with_thread_pointer(
            c->get_instance_data_size(), c->get_allocation_handle(),
            vm_get_gc_thread_local());

} //vm_rt_new_resolved
 
// this is a helper routine for rth_get_lil_multianewarray(see jit_runtime_support.cpp).
Vector_Handle
vm_rt_multianewarray_recursive(Class    *c,
                             int      *dims_array,
                             unsigned  dims);

Vector_Handle rth_multianewarrayhelper()
{
    ASSERT_THROW_AREA;
    M2nFrame* m2nf = m2n_get_last_frame();
    const unsigned max_dim = 255;
    int lens[max_dim];

#ifdef VM_STATS
    VM_Statistics::get_vm_stats().num_multianewarray++;  
#endif

    Class* c = (Class*)*m2n_get_arg_word(m2nf, 0);
    unsigned dims = (unsigned)(*m2n_get_arg_word(m2nf, 1) & 0xFFFFffff);
    assert(dims <= max_dim);
    for(unsigned i = 0; i < dims; i++) {
        lens[dims-i-1] = (int)(*m2n_get_arg_word(m2nf, i+2) & 0xFFFFffff);
    }
    return vm_rt_multianewarray_recursive(c, lens, dims);
}

static void *vm_rt_multianewarray_resolved(Class *arr_clss,
                                            int dims,
                                            int dim0,
                                            int dim1,
                                            int dim2,
                                            int dim3,
                                            int dim4,
                                            int dim5
                                            )
{
    // 2000-01-27
    // The code should work for an arbitrary number of args if we called
    // vm_multianewarray_resolved directly from the wrapper.
    // The assert is just supposed to remind me to test this case.
    assert(dims < 7);
    Vector_Handle arr = vm_multianewarray_resolved(arr_clss, dims, dim0, dim1, dim2, dim3, dim4, dim5);
    return arr;
} //vm_rt_multianewarray_resolved



static void gen_vm_rt_ljf_wrapper_code_compactor(Merced_Code_Emitter &emitter, 
                                                  void **func, unsigned num_args,
                                                  M2nPreserveRet pr, bool translate_returned_ref)
{
    int out_arg0 = m2n_gen_push_m2n(&emitter, 0, FRAME_UNKNOWN, false, 0, 0, num_args);

    assert(num_args > 0);
    for (unsigned arg = 0; arg < num_args; arg++) {
        emitter.ipf_mov(out_arg0 + arg, IN_REG0 + arg);
    }

    emit_call_with_gp(emitter, func, true);

    // 20030512 If compressing references, translate a NULL result to a managed null (heap_base).
    if (translate_returned_ref) {
        gen_convert_unmanaged_to_managed_null_ipf((Emitter_Handle)&emitter, RETURN_VALUE_REG);
    }

    m2n_gen_pop_m2n(&emitter, false, pr);

    enforce_calling_conventions(&emitter);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
} //gen_vm_rt_ljf_wrapper_code_compactor



static void *gen_vm_rt_ljf_wrapper_compactor(void **func, int num_args, char* stub_name,
                                              M2nPreserveRet pr, bool translate_returned_ref)
{
    void *addr;

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    gen_vm_rt_ljf_wrapper_code_compactor(emitter, func, num_args, pr, translate_returned_ref);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);

    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();
    
    return addr;
} //gen_vm_rt_ljf_wrapper_compactor



static void *get_vm_rt_ldc_string_address()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }

    ManagedObject* (*p_vm_rt_ldc_string)(Class *clss, unsigned cp_index);
    p_vm_rt_ldc_string = vm_rt_ldc_string;
    addr = gen_vm_rt_ljf_wrapper_compactor((void **)p_vm_rt_ldc_string, /*num_args*/ 2, "rt_ldc_string", 
                                            /*num_rets*/ MPR_Gr, /*translate_returned_ref*/ true);

    return addr;
} //get_vm_rt_ldc_string_address


static void update_allocation_stats(int64 size,
                                    Allocation_Handle ah,
                                    void *thread_pointer)
{
    assert(size>=0 && size <0x100000000);
#ifdef VM_STATS
    VTable *vt = ManagedObject::allocation_handle_to_vtable(ah);
    Class *c = vt->clss;
    c->instance_allocated(c->get_instance_data_size());
#endif
}



static void get_vm_rt_new_with_thread_pointer_compactor(Merced_Code_Emitter &emitter, 
                                                         void **fast_obj_alloc_proc,
                                                         void **slow_obj_alloc_proc,
                                                         bool inlining_allowed)
{
    Boolean use_inline_allocation_sequence = TRUE;
    size_t offset_gc_local = (Byte *)&(p_TLS_vmthread->_gc_private_information) - (Byte *)p_TLS_vmthread;
    unsigned current_offset = 1;
    unsigned limit_offset = 1;
    if (gc_supports_frontier_allocation(&current_offset, &limit_offset) && inlining_allowed)
    {
        current_offset += (unsigned) offset_gc_local;
        limit_offset += (unsigned) offset_gc_local;
    }
    else
        use_inline_allocation_sequence = false;

    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 2, num_out_args = 3;
#ifdef VM_STATS
    void (*p_update_allocation_stats)(int64 size, Allocation_Handle ah, void *thread_pointer);
    p_update_allocation_stats = update_allocation_stats;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_update_allocation_stats,
        out0, save_pfs, save_b0, save_gp);
    emitter.ipf_mov(out0+0, IN_REG0);
    emitter.ipf_mov(out0+1, IN_REG1);
    emitter.ipf_adds(out0+2, (int)offset_gc_local, THREAD_PTR_REG);
    emit_call_with_gp(emitter, (void **)p_update_allocation_stats, false);
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);
#endif // VM_STATS
    
    // 2003-04-03.  Code for mocking up the inlining of the fast-path
    // allocation sequence (for non-arrays only).
    // Do the fast path without even creating a frame.
    // 20030418 If you change this inline sequence, there is another copy in emit_newinstance_override()
    // that must also be changed.
    // 2003-04-25. This should really be refactored!!!
    // 
    //
    // g1 = r4 + offset_gc_local                // address of current
    // g2 = r4 + offset_gc_local+sizeof(void*)  // address of limit
    // r8 = [g1]                                // current
    // g3 = [g2]                                // limit
    // g4 = r8 + in0                            // current+size
    // p1 = (g4 <= g3)                          // check that (current+size <= limit)
    // (p1) [r8] = in1                          // write vtable
    // (p1) [g1] = g4                           // set new current value
    // (p1) return
    //
    //
    // For vector allocations, in0 and in1 need to be swapped; 

    if (use_inline_allocation_sequence)
    {
        emitter.ipf_sxt(sxt_size_4, IN_REG0, IN_REG0);
        emitter.ipf_adds(SCRATCH_GENERAL_REG, (int)current_offset, THREAD_PTR_REG);
        emitter.ipf_adds(SCRATCH_GENERAL_REG2, (int)limit_offset, THREAD_PTR_REG);
        emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, RETURN_VALUE_REG, SCRATCH_GENERAL_REG);
        emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, SCRATCH_GENERAL_REG3, SCRATCH_GENERAL_REG2);
        emitter.ipf_add(SCRATCH_GENERAL_REG4, RETURN_VALUE_REG, IN_REG0);
        emitter.ipf_cmp(icmp_le, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, SCRATCH_GENERAL_REG4, SCRATCH_GENERAL_REG3);
        emitter.ipf_st(int_mem_size_8, mem_st_none, mem_none, RETURN_VALUE_REG, IN_REG1, SCRATCH_PRED_REG);
        emitter.ipf_st(int_mem_size_8, mem_st_none, mem_none, SCRATCH_GENERAL_REG, SCRATCH_GENERAL_REG4, SCRATCH_PRED_REG);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);
    }


    if (!use_inline_allocation_sequence) {
        // Allocate frame and save pfs, b0, and gp
        assert(num_in_args == 2 && num_out_args == 3);
        emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
            fast_obj_alloc_proc,
            out0, save_pfs, save_b0, save_gp);
        // Fast call sequence
        emitter.ipf_mov(out0+0, IN_REG0);
        emitter.ipf_mov(out0+1, IN_REG1);
        emitter.ipf_adds(out0+2, (int)offset_gc_local, THREAD_PTR_REG);
        emit_call_with_gp(emitter, fast_obj_alloc_proc, false);

        // If the fast allocation procedure returned a non-NULL result then return, else fall through to the slow allocation path.
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, 0, RETURN_VALUE_REG);
        emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
        enforce_calling_conventions(&emitter, SCRATCH_PRED_REG2);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG2);
    }

    // push m2n frame
    unsigned out_arg0 = m2n_gen_push_m2n(&emitter, NULL, FRAME_UNKNOWN, false, 0, 0, 3);

    // Slow path
    emitter.ipf_mov(out_arg0+0, IN_REG0);
    emitter.ipf_mov(out_arg0+1, IN_REG1);
    emitter.ipf_adds(out_arg0+2, (int)offset_gc_local, THREAD_PTR_REG);
    emit_call_with_gp(emitter, slow_obj_alloc_proc, true);

    // pop m2n frame and return
    m2n_gen_pop_m2n(&emitter, false, MPR_Gr);
    enforce_calling_conventions(&emitter);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
} //get_vm_rt_new_with_thread_pointer_compactor


static void get_vm_rt_new_vector_with_thread_pointer_compactor(Merced_Code_Emitter &emitter, 
                                                                void **fast_obj_alloc_proc,
                                                                void **slow_obj_alloc_proc)
{
    size_t offset_gc_local = (Byte *)&(p_TLS_vmthread->_gc_private_information) - (Byte *)p_TLS_vmthread;
    
    emitter.ipf_sxt(sxt_size_4, IN_REG1, IN_REG1);

#ifdef VM_STATS
    {
        int out0, save_pfs, save_b0, save_gp;
        const int num_in_args = 2, num_out_args = 3;
        void (*p_vm_new_vector_update_stats)(int length, Allocation_Handle vector_handle, void *tp);
        p_vm_new_vector_update_stats = vm_new_vector_update_stats;
        emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
            (void **)p_vm_new_vector_update_stats,
            out0, save_pfs, save_b0, save_gp);
        emitter.ipf_mov(out0+0, IN_REG0);
        emitter.ipf_mov(out0+1, IN_REG1);
        emitter.ipf_adds(out0+2, (int)offset_gc_local, THREAD_PTR_REG);
        emit_call_with_gp(emitter, (void **)p_vm_new_vector_update_stats, false);
        emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
        enforce_calling_conventions(&emitter);
    }
#endif // VM_STATS


        // Fast call sequence
        // Allocate frame and save pfs, b0, and gp
        int out0, save_pfs, save_b0, save_gp;
        const int num_in_args = 2, num_out_args = 3;
        emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
            fast_obj_alloc_proc,
            out0, save_pfs, save_b0, save_gp);

        emitter.ipf_mov(out0, IN_REG0);
        emitter.ipf_mov(out0+1, IN_REG1);
        emitter.ipf_adds(out0+2, (int)offset_gc_local, THREAD_PTR_REG);
        emit_call_with_gp(emitter, fast_obj_alloc_proc, false);

        // If the fast allocation procedure returned a non-NULL result then return, else fall through to the slow allocation path.
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, 0, RETURN_VALUE_REG);
        emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG2);
        enforce_calling_conventions(&emitter, SCRATCH_PRED_REG2);

    // push m2n frame
    unsigned out_arg0 = m2n_gen_push_m2n(&emitter, NULL, FRAME_UNKNOWN, false, 0, 0, 3);

    // Slow path
    emitter.ipf_mov(out_arg0+0, IN_REG0);
    emitter.ipf_mov(out_arg0+1, IN_REG1);
    emitter.ipf_adds(out_arg0+2, (int)offset_gc_local, THREAD_PTR_REG);
    emit_call_with_gp(emitter, slow_obj_alloc_proc, true);

    // pop m2n frame and return
    m2n_gen_pop_m2n(&emitter, false, MPR_Gr);
    enforce_calling_conventions(&emitter);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
} //get_vm_rt_new_vector_with_thread_pointer_compactor



static void *generate_object_allocation_with_thread_pointer_stub_compactor(void **fast_obj_alloc_proc,
                                                                           void **slow_obj_alloc_proc,
                                                                           char *stub_name,
                                                                           bool is_array_allocator,
                                                                           bool inlining_allowed)
{
    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    if (is_array_allocator)
    {
        get_vm_rt_new_vector_with_thread_pointer_compactor(emitter, fast_obj_alloc_proc, slow_obj_alloc_proc);
    }
    else
    {
        get_vm_rt_new_with_thread_pointer_compactor(emitter, fast_obj_alloc_proc, slow_obj_alloc_proc,inlining_allowed);
    }

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    void *addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);    
    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();


    DUMP_STUB(addr, stub_name, stub_size);

    return addr;
} //generate_object_allocation_with_thread_pointer_stub_compactor


/*
 * inlining_allowed - we can't inline allocations for classes with finalizers so we
 * force a slow path if the class has a finalizer.  true means behave normally and inline
 * if you can.  false means that inlining shall not be done.
 */
static void *get_vm_rt_new_resolved_using_vtable_address_and_size(bool inlining_allowed)
{
    // Since this function can now return two types of stubs, we need two statics, one to
    // save the inlined stub and one to save the non-inlined stub.
    static void *inline_addr = 0;
    static void *non_inline_addr = 0;

    if (inlining_allowed && inline_addr != NULL) {
        return inline_addr;
    }
    if (!inlining_allowed && non_inline_addr != NULL) {
        return non_inline_addr;
    }
    
    Managed_Object_Handle (*p_gc_alloc_fast)(
        unsigned size, 
        Allocation_Handle type,
        void *thread_pointer);
    Managed_Object_Handle (*p_vm_malloc_with_thread_pointer)(
        unsigned size, 
        Allocation_Handle type,
        void *thread_pointer);
    p_gc_alloc_fast = gc_alloc_fast;
    p_vm_malloc_with_thread_pointer = vm_malloc_with_thread_pointer;

    if ( inlining_allowed ) {
        inline_addr = generate_object_allocation_with_thread_pointer_stub_compactor((void **) p_gc_alloc_fast,
                                                                                    (void **) p_vm_malloc_with_thread_pointer,
                                                                                    "rt_new_resolved_using_vtable_address_and_size",
                                                                                    false,
                                                                                    inlining_allowed);
        return inline_addr;
    } else {
        non_inline_addr = generate_object_allocation_with_thread_pointer_stub_compactor((void **) p_gc_alloc_fast,
                                                                                        (void **) p_vm_malloc_with_thread_pointer,
                                                                                        "rt_new_resolved_using_vtable_address_and_size",
                                                                                        false,
                                                                                        inlining_allowed);
        return non_inline_addr;
    }
} //get_vm_rt_new_resolved_using_vtable_address_and_size


// newarray/anewarray helper

// Create a new array (of either primitive or ref type)
// Returns -1 if there is a negative size
static Vector_Handle rth_newarrayhelper()
{
    ASSERT_THROW_AREA;
    M2nFrame* m2nf = m2n_get_last_frame();
    int len = (int)(*m2n_get_arg_word(m2nf, 1) & 0xFFFFffff);
    if (len < 0) return (Vector_Handle)-1;
    Allocation_Handle vh = (Allocation_Handle)*m2n_get_arg_word(m2nf, 0);
    VTable *vector_vtable = ManagedObject::allocation_handle_to_vtable(vh);
    Class* c = vector_vtable->clss;

    return vm_rt_new_vector(c, len);
}

static void *get_vm_rt_new_vector__using_vtable_address()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }

    if (VM_Global_State::loader_env->use_lil_stubs) {
        LilCodeStub* cs = lil_parse_code_stub("entry 0:stdcall::ref;");
        assert(cs);
        cs = lil_parse_onto_end(cs,
            "push_m2n 0, 0;"
            "out platform::ref;"
            "call %0i;"
            "pop_m2n;"
            "jc r=%1i:ref,negsize;"
            "ret;"
            ":negsize;"
            "tailcall %2i;",
            rth_newarrayhelper, (POINTER_SIZE_INT)-1, 
            lil_npc_to_fp(exn_get_rth_throw_negative_array_size()));
        assert(cs && lil_is_valid(cs));
        addr = LilCodeGenerator::get_platform()->compile(cs);

        DUMP_STUB( addr, "rth_newarrayhelper", lil_cs_get_code_size(cs));

        lil_free_code_stub(cs);

    } else {

        Vector_Handle (*p_vm_new_vector_or_null_using_vtable_and_thread_pointer)(int length, Allocation_Handle vector_handle, void *tp);
        Vector_Handle (*p_vm_rt_new_vector_using_vtable_and_thread_pointer)(int length, Allocation_Handle vector_handle, void *tp);
        p_vm_new_vector_or_null_using_vtable_and_thread_pointer = vm_new_vector_or_null_using_vtable_and_thread_pointer;
        p_vm_rt_new_vector_using_vtable_and_thread_pointer = vm_rt_new_vector_using_vtable_and_thread_pointer;

        addr = generate_object_allocation_with_thread_pointer_stub_compactor(
            (void **) p_vm_new_vector_or_null_using_vtable_and_thread_pointer,
            (void **) p_vm_rt_new_vector_using_vtable_and_thread_pointer,
            "rt_new_vector_using_vtable",
            true,
            true);  
    }

    return addr;
} //get_vm_rt_new_vector__using_vtable_address



static void *get_vm_rt_multianewarray_resolved_address()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }
    
    void *(*p_vm_rt_multianewarray_resolved)(
        Class *arr_clss,
        int dims,
        int dim0,
        int dim1,
        int dim2,
        int dim3,
        int dim4,
        int dim5
        );
    p_vm_rt_multianewarray_resolved = vm_rt_multianewarray_resolved;
    addr = gen_vm_rt_ljf_wrapper_compactor((void **)p_vm_rt_multianewarray_resolved, /*num_args*/ 8, "rt_multianewarray_resolved",
                                            /*num_rets*/ MPR_Gr, /*translate_returned_ref*/ true);

    return addr;
} //get_vm_rt_multianewarray_resolved_address



// end memory allocation
/////////////////////////////////////////////////////////////////



// Inserts code that tests whether the "sub_object" register contains
// an object that is a subclass of Class_Handle in the "super_class"
// register.  The "pred" predicate register is set to true or false
// accordingly.
// The "sub_object" must be non-null.
// If "is_instanceof" is true, the code will unconditionally return
// a 0/1 value.  If false, it will return its input if the type
// check succeeds, and fall through otherwise.
// No VM_STATS data are updated.
// The code is based on the class_is_subtype_fast_no_stats() function.
static void emit_fast_type_check_without_vm_stats(Merced_Code_Emitter& emitter,
                                                   int sub_object, int super_class, int pred,
                                                   bool is_instanceof, int call_label)
{
    // sc1 = super_class->get_offset_of_fast_instanceof_flag()
    // sc3 = [sub_object]
    // sc6 = super_class->get_offset_of_depth()
    // sc4 = offset(superclasses) - 8

    // sc2 = [sc1]
    // sc7 = [sc6]
    // sc5 = sc3 + sc4

    // pred,p0 = cmp.eq sc2, 0
    // sc8 = sc7<<3 + sc5
    // (pred) br.cond call_label

    // sc9 = [sc8]

    // pred,p0 = cmp.eq sc9, super_class
    // br call_label

    // call_label:
    // small_alloc
    // out0 = sc5
    // out1 = super_class
    // call class_is_subtype
    // pred = (v0 != 0)
    // restore b0, gp, ar.pfs

    // end_label:

    const unsigned p0 = 0;
    const unsigned sc1 = SCRATCH_GENERAL_REG;
    const unsigned sc2 = SCRATCH_GENERAL_REG2;
    const unsigned sc3 = SCRATCH_GENERAL_REG3;
    const unsigned sc4 = SCRATCH_GENERAL_REG4;
    const unsigned sc5 = SCRATCH_GENERAL_REG5;
    const unsigned sc6 = SCRATCH_GENERAL_REG6;
    const unsigned sc7 = SCRATCH_GENERAL_REG7;
    const unsigned sc8 = SCRATCH_GENERAL_REG8;
    const unsigned sc9 = SCRATCH_GENERAL_REG9;

    const int offset_is_suitable = (int)Class::get_offset_of_fast_instanceof_flag();
    const int offset_depth = (int)Class::get_offset_of_depth();

    VTable *dummy_vtable = NULL;
    const int offset_superclasses = (int) ((Byte*)&dummy_vtable->superclasses[-1] - (Byte*)dummy_vtable);
    const int offset_clss = (int) ((Byte*)&dummy_vtable->clss - (Byte*)dummy_vtable);

    // sc1 = super_class->get_offset_of_fast_instanceof_flag()
    // sc3 = [sub_object]
    // sc6 = super_class->get_offset_of_depth()
    // sc4 = offset(superclasses) - 8
    emitter.ipf_adds(sc1, offset_is_suitable, super_class);
    if (vm_vtable_pointers_are_compressed())
    {
        emitter.ipf_ld(int_mem_size_4, mem_ld_none, mem_none, sc3, sub_object);
        emitter.ipf_adds(sc6, offset_depth, super_class);
        emit_mov_imm_compactor(emitter, sc4, offset_superclasses + vm_get_vtable_base());
    }
    else
    {
        emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, sc3, sub_object);
        emitter.ipf_adds(sc6, offset_depth, super_class);
        emit_mov_imm_compactor(emitter, sc4, offset_superclasses);
    }

    // sc2 = [sc1]
    // sc7 = [sc6]
    // sc5 = sc3 + sc4
    emitter.ipf_ld(int_mem_size_4, mem_ld_none, mem_none, sc2, sc1);
    emitter.ipf_ld(int_mem_size_4, mem_ld_none, mem_none, sc7, sc6);
    emitter.ipf_add(sc5, sc3, sc4);

    // pred,p0 = cmp.eq sc2, 0
    // sc8 = sc7<<3 + sc5
    // (pred) br.cond call_label
    emitter.ipf_cmp(icmp_eq, cmp_none, pred, p0, sc2, 0);
    emitter.ipf_shladd(sc8, sc7, 3, sc5);
    emitter.ipf_br(br_cond, br_many, br_spnt, br_none, call_label, pred);

    // sc9 = [sc8]
    emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, sc9, sc8);

    // pred,p0 = cmp.eq sc9, super_class
    // br call_label
    emitter.ipf_cmp(icmp_eq, cmp_none, pred, p0, sc9, super_class, false);
    if (is_instanceof)
    {
        emitter.ipf_movi(RETURN_VALUE_REG, 1);
        emitter.ipf_brret(br_many, br_dptk, br_none, BRANCH_RETURN_LINK_REG, pred);
        emitter.ipf_mov(RETURN_VALUE_REG, 0);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
    }
    else
    {
        emitter.ipf_mov(RETURN_VALUE_REG, IN_REG0);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, pred);
    }

    emitter.set_target(call_label);
    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 2, num_out_args = 2;
    Boolean (*p_class_is_subtype)(Class *sub, Class *super);
    p_class_is_subtype = class_is_subtype;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_class_is_subtype,
        out0, save_pfs, save_b0, save_gp);
    // sc3 contains the vtable pointer or the vtable offset
    emit_mov_imm_compactor(emitter, sc2, (vm_vtable_pointers_are_compressed() ? vm_get_vtable_base() : 0) + offset_clss);
    emitter.ipf_add(sc1, sc2, sc3);
    emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, out0+0, sc1);
    emitter.ipf_mov(out0+1, super_class);
    emit_call_with_gp(emitter, (void **)p_class_is_subtype, false);
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);

    if (is_instanceof)
    {
        enforce_calling_conventions(&emitter);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
    }
    else
    {
        emitter.ipf_cmp(icmp_ne, cmp_none, pred, p0, RETURN_VALUE_REG, 0);
        emitter.ipf_mov(RETURN_VALUE_REG, IN_REG0);
        enforce_calling_conventions(&emitter);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, pred);
    }
}


static void emit_vm_stats_update_call(Merced_Code_Emitter& emitter, void **function, int num_args)
{
#ifdef VM_STATS
    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = num_args, num_out_args = num_args;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        function,
        out0, save_pfs, save_b0, save_gp);

    for (int i=0; i<num_args; i++)
    {
        emitter.ipf_mov(out0+i, IN_REG0+i);
    }

    emit_call_with_gp(emitter, function, false);

    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);
    emitter.flush_buffer();
#endif // VM_STATS
}



static void *get_vm_rt_aastore_address_compactor()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    // Allocate frame, save pfs, b0, and gp
    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 3, num_out_args = 3;
    void *(*p_vm_rt_aastore)(Vector_Handle array, int idx, ManagedObject *elem);
    p_vm_rt_aastore = vm_rt_aastore;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_vm_rt_aastore,
        out0, save_pfs, save_b0, save_gp);

    // Call vm_rt_aastore
    emitter.ipf_mov(out0+0, IN_REG0);
    emitter.ipf_mov(out0+1, IN_REG1);
    emitter.ipf_mov(out0+2, IN_REG2);
    emit_call_with_gp(emitter, (void **)p_vm_rt_aastore, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);

    enforce_calling_conventions(&emitter);
    // Check for exception, return if none
    emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, RETURN_VALUE_REG, 0);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Throw exception
    emitter.ipf_mov(IN_REG0, RETURN_VALUE_REG);
    gen_vm_rt_athrow_internal_compactor(emitter);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);
    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();
    
    return addr;
} //get_vm_rt_aastore_address_compactor



static void *get_vm_rt_checkcast_address_compactor()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }
    if (VM_Global_State::loader_env->use_lil_stubs) {
        LilCodeStub *cs = gen_lil_typecheck_stub(true);
        assert(lil_is_valid(cs));
        addr = LilCodeGenerator::get_platform()->compile(cs);

        DUMP_STUB( addr, "vm_rt_checkcast", lil_cs_get_code_size(cs));

        lil_free_code_stub(cs);
        return addr;
    }
    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 1);
    emitter.disallow_instruction_exchange();  //ST REMEMBER TO CHANGE THIS BACK!
    emitter.memory_type_is_unknown();

    const int call_label = 0;

    void (*p_vm_checkcast_update_stats)(ManagedObject *obj, Class *super);
    p_vm_checkcast_update_stats = vm_checkcast_update_stats;
    emit_vm_stats_update_call(emitter, (void **)p_vm_checkcast_update_stats, 2);

    if (VM_Global_State::loader_env->compress_references) {
        // 20030502 Test for a managed (heap_base) instead of unmanaged null (NULL) passed by managed code.
        gen_compare_to_managed_null(emitter, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, SCRATCH_GENERAL_REG);
    } else {
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
    }
    emitter.ipf_mov(RETURN_VALUE_REG, IN_REG0);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    emit_fast_type_check_without_vm_stats(emitter, IN_REG0, IN_REG1, SCRATCH_PRED_REG, false, call_label);

    // Throw exception
    emitter.ipf_mov(IN_REG0, 0);
    uint64 except_clss = (uint64)VM_Global_State::loader_env->java_lang_ClassCastException_Class;
    emit_movl_compactor(emitter, IN_REG1, except_clss);

    gen_vm_rt_athrow_internal_compactor(emitter);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);


    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();

    DUMP_STUB(addr, "vm_rt_checkcast", stub_size);

    return addr;
} //get_vm_rt_checkcast_address_compactor



static void *get_vm_rt_instanceof_address_compactor()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }
    if (VM_Global_State::loader_env->use_lil_stubs) {
        LilCodeStub *cs = gen_lil_typecheck_stub(false);
        assert(lil_is_valid(cs));
        addr = LilCodeGenerator::get_platform()->compile(cs);

        DUMP_STUB( addr, "vm_rt_instanceof", lil_cs_get_code_size(cs));

        lil_free_code_stub(cs);
        return addr;
    }


    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 1);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    const int call_label = 0;

    void (*p_vm_instanceof_update_stats)(ManagedObject *obj, Class *super);
    p_vm_instanceof_update_stats = vm_instanceof_update_stats;
    emit_vm_stats_update_call(emitter, (void **)p_vm_instanceof_update_stats, 2);

    if (VM_Global_State::loader_env->compress_references) {
        // 20030502 Test for a managed (heap_base) instead of unmanaged null (NULL) passed by managed code.
        gen_compare_to_managed_null(emitter, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, SCRATCH_GENERAL_REG);
    } else {
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
    }
    emitter.ipf_mov(RETURN_VALUE_REG, 0);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    emit_fast_type_check_without_vm_stats(emitter, IN_REG0, IN_REG1, SCRATCH_PRED_REG, true, call_label);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);


    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();

    DUMP_STUB(addr, "vm_rt_instanceof", stub_size);

    return addr;
} //get_vm_rt_instanceof_address_compactor



// Emits code that sets dst = src->vt()->clss->array_element_class.
static void emit_get_array_element_class(Merced_Code_Emitter& emitter, int src, int dst)
{
    const int sc1 = SCRATCH_GENERAL_REG;
    const int sc2 = SCRATCH_GENERAL_REG2;
    const int sc3 = SCRATCH_GENERAL_REG3;
    const int sc4 = SCRATCH_GENERAL_REG4;
    const int sc5 = SCRATCH_GENERAL_REG7;

    const int offset_array_element_class = Class::get_offset_of_array_element_class();

    VTable *dummy_vtable = NULL;
    const int offset_clss = (int) ((Byte*)&dummy_vtable->clss - (Byte*)dummy_vtable);

    if (vm_vtable_pointers_are_compressed())
    {
        emitter.ipf_ld(int_mem_size_4, mem_ld_none, mem_none, sc5, src);
        emit_mov_imm_compactor(emitter, sc1, vm_get_vtable_base() + offset_clss);
    }
    else
    {
        emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, sc5, src);
        emit_mov_imm_compactor(emitter, sc1, offset_clss);
    }
    emitter.ipf_add(sc2, sc1, sc5); // sc2 = &src->vt()->clss
    emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, sc3, sc2); // sc3 = src->vt()->clss
    emitter.ipf_adds(sc4, offset_array_element_class, sc3); // sc6 = &src->vt()->clss->array_element_class
    emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, dst, sc4);
}



static void *get_vm_rt_aastore_test_address_compactor()
{
    static void *addr = 0;
    if(addr) {
        return addr;
    }

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 1);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    const int call_label = 0;

    const int sc1 = SCRATCH_GENERAL_REG;
    const int sc10 = sc1 + 9;

    void (*p_vm_aastore_test_update_stats)(ManagedObject *elem, Vector_Handle array);
    p_vm_aastore_test_update_stats = vm_aastore_test_update_stats;
    emit_vm_stats_update_call(emitter, (void **)p_vm_aastore_test_update_stats, 2);

    if (VM_Global_State::loader_env->compress_references) {
        // 20030502 Test for a managed (heap_base) instead of unmanaged null (NULL) passed by managed code.
        gen_compare_to_managed_null(emitter, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, SCRATCH_GENERAL_REG);
    } else {
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
    }
    emitter.ipf_movi(RETURN_VALUE_REG, 1);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Check the array against null.
    if (VM_Global_State::loader_env->compress_references) {
        // 20030502 Test for a managed (heap_base) instead of unmanaged null (NULL) passed by managed code.
        gen_compare_to_managed_null(emitter, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG1, SCRATCH_GENERAL_REG);
    } else {
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG1, 0);
    }
    emitter.ipf_movi(RETURN_VALUE_REG, 0);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Don't bother checking the array type against Object[], since the JITRINO does it already?

    // Need to get array->vt()->clss->array_element_class into sc10.
    emit_get_array_element_class(emitter, IN_REG1, sc10);

    emit_fast_type_check_without_vm_stats(emitter, IN_REG0, sc10, SCRATCH_PRED_REG, true, call_label);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);

    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();


    return addr;
} //get_vm_rt_aastore_test_address_compactor


static Boolean is_class_initialized(Class *clss)
{
#ifdef VM_STATS
    VM_Statistics::get_vm_stats().num_is_class_initialized++;
    clss->initialization_checked();
#endif // VM_STATS
    assert(!hythread_is_suspend_enabled());
    return clss->is_initialized();
} //is_class_initialized

static void *get_vm_rt_initialize_class_compactor()
{
    static void *addr = NULL;
    if (addr != NULL)
    {
        return addr;
    }

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_is_class_initialized);
    // Update the per-class counter.
    // reg0 = in0 + offset(num_class_init_checks)
    // reg1 = [reg0]
    // reg1 = reg1 + 1
    // [reg0] = reg1
    emitter.ipf_adds(SCRATCH_GENERAL_REG, (int)Class::get_offset_of_class_init_checks(), IN_REG0);
    emitter.ipf_ld(int_mem_size_8, mem_ld_none, mem_none, SCRATCH_GENERAL_REG2, SCRATCH_GENERAL_REG);
    emitter.ipf_adds(SCRATCH_GENERAL_REG2, 1, SCRATCH_GENERAL_REG2);
    emitter.ipf_st(int_mem_size_8, mem_st_none, mem_none, SCRATCH_GENERAL_REG, SCRATCH_GENERAL_REG2);
#endif // VM_STATS

    // Check clss->state==ST_Initialized, quick return if true.
    //emitter.ipf_adds(SCRATCH_GENERAL_REG, (int)((Byte*)&dummy->state-(Byte*)dummy), IN_REG0);
    emitter.ipf_adds(SCRATCH_GENERAL_REG3, 0, 0);

    Boolean (*p_is_class_initialized)(Class *clss);
    p_is_class_initialized = is_class_initialized;
    emit_call_with_gp(emitter, (void**)p_is_class_initialized, false);

    emitter.ipf_cmp(icmp_ne, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, RETURN_VALUE_REG, SCRATCH_GENERAL_REG3);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // push m2n frame
    unsigned out_arg0 = m2n_gen_push_m2n(&emitter, NULL, FRAME_JNI, false, 0, 0, 1);
    emitter.ipf_mov(out_arg0+0, IN_REG0);
    void (*p_class_initialize)(Class *clss);
    p_class_initialize = vm_rt_class_initialize;
    emit_call_with_gp(emitter, (void **)p_class_initialize, true);
    // pop m2n frame and return
    m2n_gen_pop_m2n(&emitter, false, MPR_None);
    enforce_calling_conventions(&emitter);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_COLD, CAA_Allocate);
    emitter.copy((char *)addr);

    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();


    return addr;
}

// 20030502 This JIT support routine expects to be called directly from managed code. 
static void *vm_rt_get_interface_vtable(ManagedObject *object, Class *clss)
{
    assert(object != (ManagedObject *)VM_Global_State::loader_env->managed_null);
    void *vt = vm_get_interface_vtable(object, clss);
    return vt;
} //vm_rt_get_interface_vtable




Boolean jit_may_inline_object_synchronization(unsigned *thread_id_register,
                                              unsigned *sync_header_offset,
                                              unsigned *sync_header_width,
                                              unsigned *lock_owner_offset,
                                              unsigned *lock_owner_width,
                                              Boolean  *jit_clears_ccv)
{
    // FIXME: code outdated
    assert(0);
    abort();
#if 0
    if (!vm_get_boolean_property_value_with_default("vm.jit_may_inline_sync"))
         return FALSE;

    *thread_id_register = THREAD_ID_REG;
    *sync_header_offset = ManagedObject::header_offset();
    *sync_header_width = 4;
    *lock_owner_offset = ManagedObject::header_offset() + STACK_KEY_OFFSET;
    *lock_owner_width = 2;
    *jit_clears_ccv = jit_clears_ccv_in_monitor_enter();
    return TRUE;
#endif
}



static void gen_vm_rt_monitorenter_fast_path(Merced_Code_Emitter &emitter, bool check_null)
{
    return; // ichebyki
#if 1
    // FIXME: code outdated
    assert(0);
    abort();
#else
    const int thread_stack_key_reg          = THREAD_ID_REG;
    const int object_stack_key_addr_reg     = SCRATCH_GENERAL_REG4;
    const int object_old_stack_key_reg      = SCRATCH_GENERAL_REG5;
    const int header_offset = ManagedObject::header_offset();

    // We do not implement the IA32 lazy lock scheme that speeds up single-threaded applications since we want to optimize 
    // IPF performance for the multithreaded applications that we expect to be typical of servers. 
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_enter);
#endif

    // Set object_stack_key_addr_reg to the address of the object's stack key field.
    emitter.ipf_adds(object_stack_key_addr_reg, (header_offset + STACK_KEY_OFFSET), IN_REG0);

    if (check_null) {
        // Set SCRATCH_PRED_REG2 to 1 if the object reference is non-null.
#ifdef VM_STATS
        increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_enter_null_check);
#endif
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
#ifdef VM_STATS
        increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_enter_is_null, SCRATCH_PRED_REG);
#endif

        // ==== Instructions below are only executed if the object reference is non-null ====

        // Read the object's stack key (owning thread id) to object_old_stack_key_reg and compare it to FREE_MONITOR in ar.ccv.
        // If the same, write this thread's id to the object's stack key, marking the object locked by the current thread.
        emitter.ipf_movi(SCRATCH_GENERAL_REG, FREE_MONITOR, SCRATCH_PRED_REG2);
        emitter.ipf_mtap(AR_ccv, SCRATCH_GENERAL_REG, SCRATCH_PRED_REG2);
        emitter.ipf_cmpxchg(int_mem_size_2, mem_cmpxchg_acq, mem_none, 
                             object_old_stack_key_reg, object_stack_key_addr_reg, thread_stack_key_reg,
                             SCRATCH_PRED_REG2);

        // Is object_old_stack_key_reg == FREE_MONITOR? If so, it was unlocked before and we succeeded in acquiring the object
        // and can just return. Note the use of "cmp_unc" to unconditionally initialize both SCRATCH_PRED_REG3 and SCRATCH_PRED_REG4
        // to 0 (false) before the comparison.
        emitter.ipf_cmpi(icmp_eq, cmp_unc, SCRATCH_PRED_REG3, SCRATCH_PRED_REG4, 
                          FREE_MONITOR, object_old_stack_key_reg, /*cmp4*/ false, SCRATCH_PRED_REG2);
    } else {
        // 20030115 New code that assumes the reference is always non-null and that FREE_MONITOR is always zero.
        assert(FREE_MONITOR == 0);  // else the code sequence below will break

        // Read the object's stack key (owning thread id) to object_old_stack_key_reg and compare it to FREE_MONITOR in ar.ccv.
        // If the same, write this thread's id to the object's stack key, marking the object locked by the current thread.
        emitter.ipf_mtap(AR_ccv, 0);
        emitter.ipf_cmpxchg(int_mem_size_2, mem_cmpxchg_acq, mem_none, 
                             object_old_stack_key_reg, object_stack_key_addr_reg, thread_stack_key_reg);

        // Is object_old_stack_key_reg == FREE_MONITOR? If so, it was unlocked before and we succeeded in acquiring the object
        // and can just return. 
        emitter.ipf_cmpi(icmp_eq, cmp_none, SCRATCH_PRED_REG3, SCRATCH_PRED_REG4, 
                          FREE_MONITOR, object_old_stack_key_reg, /*cmp4*/ false);
    }

#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_enter_fastcall, SCRATCH_PRED_REG3);
#endif
    enforce_calling_conventions(&emitter, SCRATCH_PRED_REG3);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG3);

    // If a null pointer, then throw a null pointer exception by tailcall to throw null pointer exception code
    if (check_null) {
        unsigned nonnull_target = 1;
        emitter.ipf_br(br_cond, br_many, br_sptk, br_none, nonnull_target, SCRATCH_PRED_REG2);
        emit_movl_compactor(emitter, SCRATCH_GENERAL_REG3, (uint64)get_vm_rt_null_ptr_exception_address());
        emitter.ipf_mtbr(SCRATCH_BRANCH_REG, SCRATCH_GENERAL_REG3);
        emitter.ipf_bri(br_cond, br_many, br_sptk, br_none, SCRATCH_BRANCH_REG);
        emitter.set_target(nonnull_target);
    }

    // If the cmpxchg failed, we fall through and execute the slow monitor enter path by calling vm_monitor_enter.
#endif
} //gen_vm_rt_monitorenter_fast_path



static void gen_vm_rt_monitorexit_fast_path(Merced_Code_Emitter &emitter, bool check_null)
{
    return; // ichebyki
    // FIXME: code is outdated
    assert(0);
    abort();
#if 0
    const int object_stack_key_addr_reg     = SCRATCH_GENERAL_REG3;
    const int object_lock_info_reg          = SCRATCH_GENERAL_REG4;
    const int thread_stack_key_reg          = THREAD_ID_REG;
    const int object_recur_count_addr_reg   = SCRATCH_GENERAL_REG6;
    const int header_offset = ManagedObject::header_offset();

#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit);
#endif
    unsigned end_of_monitorexit_fast_path_target = 1;

    if (check_null) {
        // Branch around the rest of the code emitted by this procedure if the object reference is null. 
#ifdef VM_STATS
        increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_null_check);
#endif
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
#ifdef VM_STATS
        increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_is_null, SCRATCH_PRED_REG);
#endif
        emitter.ipf_br(br_cond, br_few, br_sptk, br_none, end_of_monitorexit_fast_path_target, SCRATCH_PRED_REG);
    }

    // Fastest path: Read the four bytes of the object's lock information (owning thread id, recursion count, contention bit, and hash code)
    // to object_old_lock_info_reg and compare it with the value <current thread id>+0+0+0. If they are the same, clear (zero) the object's 
    // owning thread by writing 0+0+0+0 and return. Emit a st4.rel to ensure that all previous data accesses are made visible prior
    // to the store itself. This ensures other threads will see writes from within the critical section before the freed lock.
    emitter.ipf_adds(SCRATCH_GENERAL_REG, (header_offset + HASH_CONTENTION_AND_RECURSION_OFFSET), IN_REG0);
    emitter.ipf_ld(int_mem_size_4, mem_ld_none, mem_none, object_lock_info_reg, SCRATCH_GENERAL_REG);    
    emitter.ipf_shli(SCRATCH_GENERAL_REG2, thread_stack_key_reg, /*count*/ 16);  // expected lock info: current thread id shifted left by 16 bits
    emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, object_lock_info_reg, SCRATCH_GENERAL_REG2);
    emitter.ipf_st(int_mem_size_4, mem_st_rel, mem_none, SCRATCH_GENERAL_REG, 0, SCRATCH_PRED_REG);
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_fastestcall, SCRATCH_PRED_REG);
#endif
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Set object_stack_key_addr_reg to the address of the object's stack key field (owning thread's id, if any).
    emitter.ipf_adds(object_stack_key_addr_reg, (header_offset + STACK_KEY_OFFSET), IN_REG0);

    // Branch around the rest of the code emitted by this procedure if the current thread doesn't own the object
    // If so, vm_exit_monitor() will be called to raise IllegalMonitorStateException. 
    emitter.ipf_ld(int_mem_size_2, mem_ld_none, mem_none, SCRATCH_GENERAL_REG, object_stack_key_addr_reg);
    emitter.ipf_cmp(icmp_ne, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, thread_stack_key_reg, SCRATCH_GENERAL_REG);
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_unowned_object, SCRATCH_PRED_REG);
#endif
    emitter.ipf_br(br_cond, br_few, br_sptk, br_none, end_of_monitorexit_fast_path_target, SCRATCH_PRED_REG);

    // ==== Instructions below are only executed if the current thread owns the object ====

    // Fast path: If the two bytes of the object's recursion count, contention, and hashcode fields are all zero, release the lock.
    emitter.ipf_adds(SCRATCH_GENERAL_REG, (header_offset + HASH_CONTENTION_AND_RECURSION_OFFSET), IN_REG0);
    emitter.ipf_ld(int_mem_size_2, mem_ld_none, mem_none, object_lock_info_reg, SCRATCH_GENERAL_REG);    
    emitter.ipf_cmpi(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, 0, object_lock_info_reg);
    emitter.ipf_st(int_mem_size_2, mem_st_rel, mem_none, object_stack_key_addr_reg, 0, SCRATCH_PRED_REG);
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_fastcall, SCRATCH_PRED_REG);
#endif
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Slower path: If the recursion count field is nonzero, just decrement the object's recursion count and return.
    emitter.ipf_movi(SCRATCH_GENERAL_REG, 0xff00);                               // just the recursion count
    emitter.ipf_and(SCRATCH_GENERAL_REG2, object_lock_info_reg, SCRATCH_GENERAL_REG);
    emitter.ipf_shrui(SCRATCH_GENERAL_REG2, SCRATCH_GENERAL_REG2, /*count*/ 8);  // shift count right 8 bits
    emitter.ipf_cmpi(icmp_ne, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, 0, SCRATCH_GENERAL_REG2);
    emitter.ipf_adds(SCRATCH_GENERAL_REG, -1, SCRATCH_GENERAL_REG2, SCRATCH_PRED_REG);
    emitter.ipf_adds(object_recur_count_addr_reg, (header_offset + RECURSION_OFFSET), IN_REG0, SCRATCH_PRED_REG);
    emitter.ipf_st(int_mem_size_1, mem_st_rel, mem_none, object_recur_count_addr_reg, SCRATCH_GENERAL_REG, SCRATCH_PRED_REG);
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_decr_rec_count, SCRATCH_PRED_REG);
#endif
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);

    // Slowest path. The contention bit is nonzero. Fall through to call vm_monitor_exit() to release the lock...
#ifdef VM_STATS
    increment_stats_counter(emitter, &VM_Statistics::get_vm_stats().num_monitor_exit_very_slow_path);
#endif

    // If the object reference is null or the current thread doesn't own the object, we branch to here.
    emitter.set_target(end_of_monitorexit_fast_path_target);
#endif
} //gen_vm_rt_monitorexit_fast_path



// Emit code to convert a struct Class* argument in r32 to the corresponding java_lang_Class reference. 
void gen_convert_class_arg(Merced_Code_Emitter &emitter, bool check_null)
{
    // First make sure the struct Class* argument is non-NULL.
    ManagedObject *(*p_struct_Class_to_java_lang_Class)(Class *clss);
    p_struct_Class_to_java_lang_Class = struct_Class_to_java_lang_Class;
    if (check_null) {
        unsigned end_of_conversion_target = 0;

        // Branch around the conversion code if the object pointer is NULL. 
        emitter.ipf_cmp(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, IN_REG0, 0);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_clr, end_of_conversion_target, SCRATCH_PRED_REG);

        // Call struct_Class_to_java_lang_Class() to convert the struct Class* argument to a java_lang_Class reference. 
        int out_arg0 = m2n_gen_push_m2n(&emitter, 0, FRAME_UNKNOWN, false, 0, 0, 1);
        emitter.ipf_mov(out_arg0, IN_REG0);
        emit_call_with_gp(emitter, (void **)p_struct_Class_to_java_lang_Class, true);
        m2n_gen_pop_m2n(&emitter, false, MPR_Gr);
        enforce_calling_conventions(&emitter);

        // Overwrite the struct Class* argument with the returned java_lang_Class reference.
        emitter.ipf_mov(IN_REG0, RETURN_VALUE_REG);

        emitter.set_target(end_of_conversion_target);
    } else {
        // Call struct_Class_to_java_lang_Class() to convert the struct Class* argument to a java_lang_Class reference. 
        int out_arg0 = m2n_gen_push_m2n(&emitter, 0, FRAME_UNKNOWN, false, 0, 0, 1);
        emitter.ipf_mov(out_arg0, IN_REG0);
        emit_call_with_gp(emitter, (void **)p_struct_Class_to_java_lang_Class, true);
        m2n_gen_pop_m2n(&emitter, false, MPR_Gr);
        enforce_calling_conventions(&emitter);

        // Overwrite the struct Class* argument with the returned java_lang_Class reference.
        emitter.ipf_mov(IN_REG0, RETURN_VALUE_REG);
    }
} //gen_convert_class_arg



static void *gen_vm_rt_monitor_wrapper(void **slow_path_func, void (*gen_fast_path)(Merced_Code_Emitter &, bool),
                                        bool check_null, bool static_operation,
                                        char *slow_path_stub_name, char *fast_path_stub_name)
{
    tl::MemoryPool mem_pool;
    // Branch target 0 is used by gen_convert_class_arg and target 1 is used by gen_vm_rt_monitorexit_fast_path.
    Merced_Code_Emitter emitter(mem_pool, 2, /*nTargets*/ 2);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    if (check_null) {
        // If compressing references, convert the first argument reference, if null, from a managed null (represented by heap_base)
        // to an unmanaged one (NULL/0). 
        gen_convert_managed_to_unmanaged_null_ipf((Emitter_Handle)&emitter, IN_REG0);
    }
    if (static_operation) {
        // A static monitor enter/exit. Convert the struct Class* argument to the corresponding java_lang_Class reference
        gen_convert_class_arg(emitter, check_null);
    }

    // Emit the fast path. If this succeeds, it returns to the caller, bypassing the slow path code.
    gen_fast_path(emitter, check_null);
    emitter.flush_buffer();

    // If the fast path fails, control drops through to the slow path, which does a procedure call.
    gen_vm_rt_ljf_wrapper_code_compactor(emitter, slow_path_func, /*num_args*/ 1,
                                          /*num_rets*/ MPR_Gr, /*translate_returned_ref*/ false);
    emitter.flush_buffer();
    size_t total_stub_size = emitter.get_size();

    void *stub = (void *)malloc_fixed_code_for_jit(total_stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)stub);
    flush_hw_cache((Byte *)stub, total_stub_size);
    sync_i_cache();

    return stub;
} //gen_vm_rt_monitor_wrapper



static void *get_vm_rt_monitor_enter_address(bool check_null)
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }
    void (*p_vm_monitor_enter)(ManagedObject *p_obj);
    p_vm_monitor_enter = vm_monitor_enter;
    addr = gen_vm_rt_monitor_wrapper((void **)p_vm_monitor_enter, gen_vm_rt_monitorenter_fast_path,
                                      check_null, /*static_operation*/ false,
                                      "rt_monitor_enter_slowpath", "rt_monitor_enter_fastpath");
    return addr;
} //get_vm_rt_monitor_enter_address



static void *get_vm_rt_monitor_exit_address(bool check_null)
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }
    void (*p_vm_monitor_exit)(ManagedObject *p_obj);
    p_vm_monitor_exit = vm_monitor_exit;
    addr = gen_vm_rt_monitor_wrapper((void **)p_vm_monitor_exit, gen_vm_rt_monitorexit_fast_path, 
                                      check_null, /*static_operation*/ false,
                                      "rt_monitor_exit_slowpath", "rt_monitor_exit_fastpath");
    return addr;
} //get_vm_rt_monitor_exit_address



static void *get_vm_rt_monitor_enter_static_address()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }
    // 20030220 The "static" monitor enter function is only called with a struct Class pointer, which can't be NULL.
    void (*p_vm_monitor_enter)(ManagedObject *p_obj);
    p_vm_monitor_enter = vm_monitor_enter;
    addr = gen_vm_rt_monitor_wrapper((void **)p_vm_monitor_enter, gen_vm_rt_monitorenter_fast_path, 
                                      /*check_null*/ false, /*static_operation*/ true,
                                      "rt_monitor_enter_static_slowpath", "rt_monitor_enter_static_fastpath");
    return addr;
} //get_vm_rt_monitor_enter_static_address



static void *get_vm_rt_monitor_exit_static_address()
{
    static void *addr = 0;
    if (addr) {
        return addr;
    }
    // 20030220 The "static" monitor exit function is only called with a struct Class pointer, which can't be NULL.
    void (*p_vm_monitor_exit)(ManagedObject *p_obj);
    p_vm_monitor_exit = vm_monitor_exit;
    addr = gen_vm_rt_monitor_wrapper((void **)p_vm_monitor_exit, gen_vm_rt_monitorexit_fast_path, 
                                      /*check_null*/ false, /*static_operation*/ true,
                                      "rt_monitor_exit_static_slowpath", "rt_monitor_exit_static_fastpath");
    return addr;
} //get_vm_rt_monitor_exit_static_address


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


void emit_hashcode_override(Emitter_Handle eh, Method *method)
{
    Merced_Code_Emitter *mce = (Merced_Code_Emitter *)eh;
    Merced_Code_Emitter &emitter = *mce;

    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 1, num_out_args = 1;
    long (*p_generic_hashcode)(ManagedObject *p_obj);
    p_generic_hashcode = generic_hashcode;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_generic_hashcode,
        out0, save_pfs, save_b0, save_gp);

    emitter.ipf_mov(out0, IN_REG0);
    emit_call_with_gp(emitter, (void **)p_generic_hashcode, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);

    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
} //emit_hashcode_override

void emit_arraycopy_override(Emitter_Handle eh, Method *method)
{
    Merced_Code_Emitter *mce = (Merced_Code_Emitter *)eh;
    Merced_Code_Emitter &emitter = *mce;

    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 5, num_out_args = 5;
    ArrayCopyResult (*p_array_copy)(ManagedObject* src, int32 src_off, ManagedObject* dst, int32 dst_off, int32 count);
    p_array_copy = array_copy;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_array_copy,
        out0, save_pfs, save_b0, save_gp);
    emitter.ipf_mov(out0+0, IN_REG0);
    emitter.ipf_mov(out0+1, IN_REG1);
    emitter.ipf_mov(out0+2, IN_REG2);
    emitter.ipf_mov(out0+3, IN_REG3);
    emitter.ipf_mov(out0+4, IN_REG4);
    
    emit_call_with_gp(emitter, (void **)p_array_copy, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);

    emitter.ipf_cmpi(icmp_eq, cmp_none, SCRATCH_PRED_REG, SCRATCH_PRED_REG2, ACR_Okay, RETURN_VALUE_REG);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG, SCRATCH_PRED_REG);
} //emit_arraycopy_override

void emit_system_currenttimemillis_override(Emitter_Handle eh, Method *method)
{
    Merced_Code_Emitter *mce = (Merced_Code_Emitter *)eh;
    Merced_Code_Emitter &emitter = *mce;

    jlong (*func)() = get_current_time;

    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 0, num_out_args = 1;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)func,
        out0, save_pfs, save_b0, save_gp);

    emit_call_with_gp(emitter, (void **)func, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);

    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
} //emit_system_currenttimemillis_override

int readinternal_override(JNIEnv *jenv,
                          Java_java_io_FileInputStream *pThis,
                          int fd,
                          Vector_Handle pArrayOfByte,
                          int  offset,
                          int len)
{
    // Read into byte array "count" bytes starting at index "offset"
    
    // Check if we have been passed a null pointer
    if (!pArrayOfByte) {
        throw_exception_from_jni(jenv, "java/lang/NullPointerException", 
            "Null pointer passed to read()");
        return 0;
    }

    int array_len = jenv->GetArrayLength((jbyteArray)(&pArrayOfByte));
    if (len < 0 || offset < 0 || offset + len > array_len) {
        throw_exception_from_jni(jenv, "java/lang/ArrayIndexOutOfBoundsException", 
            "Index check failed");
        return 0;
    }

    // Get the array reference.
    void *bufp = get_vector_element_address_int8(pArrayOfByte, offset);
    int ret = read (fd, bufp, len);
    
    if (ret == 0) {
        return -1;
    }
    
    // No "interrupted" check for now.
    if (ret == -1) {
        // Throw IOException since read failed somehow.. use strerror(errno)
        throw_exception_from_jni(jenv, "java/io/IOException", 0);
        return 0;
    }
    assert(ret >= 0);
    
    return ret;
}
void emit_readinternal_override(Emitter_Handle eh, Method *method)
{
    Merced_Code_Emitter *mce = (Merced_Code_Emitter *)eh;
    Merced_Code_Emitter &emitter = *mce;
    
    const int num_in = 5;
    const int num_out = 6;
    const int num_local = 5;
    const int save_pfs = IN_REG0 + num_in;
    const int save_b0 = save_pfs + 1;
    const int save_gp = save_b0 + 1;
    const int out0 = IN_REG0 + num_in + num_local;
    
    emitter.ipf_alloc(save_pfs, num_in, num_local, num_out, 0);
    emitter.ipf_mfbr(save_b0, BRANCH_RETURN_LINK_REG);
    emitter.ipf_mov(save_gp, GP_REG);
    
    emit_movl_compactor(emitter, out0+0, (uint64)p_TLS_vmthread->jni_env);
    emitter.ipf_mov(out0+1, IN_REG0);
    emitter.ipf_mov(out0+2, IN_REG1);
    emitter.ipf_mov(out0+3, IN_REG2);
    emitter.ipf_mov(out0+4, IN_REG3);
    emitter.ipf_mov(out0+5, IN_REG4);

    int (*func)(JNIEnv*, Java_java_io_FileInputStream*, int, Vector_Handle, int, int) = readinternal_override;
    emit_call_with_gp(emitter, (void **)func, false);
    
    // Restore pfs, b0, and gp
    emitter.ipf_mov(GP_REG, save_gp);
    emitter.ipf_mtap(AR_pfs, save_pfs);
    emitter.ipf_mtbr(BRANCH_RETURN_LINK_REG, save_b0);
    enforce_calling_conventions(&emitter);
    
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
}


static void collect_char_arraycopy_stats(ManagedObject *src,
                                         int64 srcOffset,
                                         ManagedObject *dst,
                                         int64 dstOffset,
                                         int64 length);

static void *gen_faster_char_arraycopy_no_exc()
{
    static void *addr = NULL;
    if (addr != NULL)
        return addr;

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 11);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    emitter.ipf_sxt(sxt_size_4, IN_REG1, IN_REG1);
    emitter.ipf_sxt(sxt_size_4, IN_REG3, IN_REG3);
    emitter.ipf_sxt(sxt_size_4, IN_REG4, IN_REG4);

    // There are 5 arguments: src, srcOffset, dst, dstOffset, length
    //
    // p1,p2 = (length == 0)
    // p3,p4 = (length >= 32)
    // (p1) br.ret
    // p5,p6 = (length == 1)
    // sc1 = srcOffset<<1 + src
    // sc2 = dstOffset<<1 + dst
    // sc7 = length - 2
    // sc3 = ar.lc
    // (p3) br mem_cpy
    //
    // p1,p2 = (dstOffset > srcOffset)
    // ar.lc = sc7
    // (p1) br copyDown
    //
    // sc4 = sc1 + first_element_offset
    // sc5 = sc2 + first_element_offset
    // sc6 = [sc4], 2
    // (p5) br.cond loop1Exit
    // loop1:
    // [sc5],2 = sc6
    // sc6 = [sc4], 2
    // br.loopc loop1
    // loop1Exit:
    // [sc5],2 = sc6
    // ar.lc = sc3
    // br.ret
    //
    // copyDown:
    // sc4 = sc1 + first_element_offset-2
    // sc5 = sc2 + first_element_offset-2
    // sc4 = length<<1 + sc4
    // sc5 = length<<1 + sc5
    // sc6 = [sc4], -2
    // (p5) br.cond loop2Exit
    // loop2:
    // [sc5],-2 = sc6
    // sc6 = [sc4], -2
    // br.cloop loop2
    // loop2Exit:
    // [sc5],-2 = sc6
    // ar.lc = sc3
    // br.ret
    //
    // mem_cpy:
    // alloc, save b0, save gp
    // br.call memmove(dst, src, length*2)
    // restore b0, gp, pfs
    // br.ret

    void (*p_collect_char_arraycopy_stats)(
        ManagedObject *src,
        int64 srcOffset,
        ManagedObject *dst,
        int64 dstOffset,
        int64 length);
    p_collect_char_arraycopy_stats = collect_char_arraycopy_stats;
    emit_vm_stats_update_call(emitter, (void **)p_collect_char_arraycopy_stats, 5);

    const int src = IN_REG0;
    const int srcOffset = IN_REG1;
    const int dst = IN_REG2;
    const int dstOffset = IN_REG3;
    const int length = IN_REG4;
    const int first_element_offset = vector_first_element_offset(VM_DATA_TYPE_CHAR);
    const int sc1 = SCRATCH_GENERAL_REG;
    const int sc2 = SCRATCH_GENERAL_REG2;
    const int sc3 = SCRATCH_GENERAL_REG3;
    const int sc4 = SCRATCH_GENERAL_REG4;
    const int sc5 = SCRATCH_GENERAL_REG5;
    const int sc6 = SCRATCH_GENERAL_REG6;
    const int sc7 = SCRATCH_GENERAL_REG7;
    const int sc8 = SCRATCH_GENERAL_REG8;
    const int sc9 = SCRATCH_GENERAL_REG9;
    const int sc10 = SCRATCH_GENERAL_REG10;
    const int sc11 = SCRATCH_GENERAL_REG11;
    const int sc12 = SCRATCH_GENERAL_REG12;
    const int sc13 = SCRATCH_GENERAL_REG13;
    const int sc17 = SCRATCH_GENERAL_REG17;
    const int sc18 = SCRATCH_GENERAL_REG18;
    const int p0 = 0;
    const int p1 = SCRATCH_PRED_REG;
    const int p2 = SCRATCH_PRED_REG2;
    const int p3 = SCRATCH_PRED_REG3;
    const int p4 = SCRATCH_PRED_REG4;
    const int p5 = SCRATCH_PRED_REG5;
    const int p6 = SCRATCH_PRED_REG6;
    const int loop1 = 0;
    const int loop2 = 1;
    const int copyDown = 2;
    const int mem_cpy = 3;
    const int loop1Exit = 4;
    const int loop2Exit = 5;
    const int loop3 = 6;
    const int mem_cpy_final = 7;
//    const int mem_cpy_pre_final = 8;
    const int loop1ExitA = 9;
    const int loop2ExitA = 10;

    {
        // sc1 = srcOffset<<1 + src
        // sc2 = dstOffset<<1 + dst
        // p3,p0 = (length > 32)
        // p4,p0 = (length < 2)
        // sc8 = length >> 1
        // (p3) br mem_cpy
        // 
        // p6,p0 = (length < 4)
        // sc9 = length & 1
        // p1,p0 = (dstOffset > srcOffset)
        // sc18 = length<<1 + sc1
        // sc3 = ar.lc
        // sc7 = sc8 - 2
        // 
        // p2,p0 = (sc9 != 0)
        // ar.lc = sc7
        // sc18 = sc18 + first_element_offset-2
        // (p1) p1,p0 = cmp.unc (src == dst)
        // (p1) br copyDown
        // 
        // sc17 =.2 [sc18] // prefetch
        // sc4 = sc1 + first_element_offset
        // sc5 = sc2 + first_element_offset
        // sc10 = sc1 + (first_element_offset+2)
        // sc11 = sc2 + (first_element_offset+2)
        // (p4) br.cond loop1ExitA
        // 
        // sc6 =.2 [sc4],4
        // sc12 =.2 [sc10],4
        // (p6) br.cond loop1Exit
        // 
        // loop1:
        // [sc5],4 =.2 sc6
        // [sc11],4 =.2 sc12
        // sc6 =.2 [sc4],4
        // sc12 =.2 [sc10],4
        // br.loopc loop1
        // 
        // loop1Exit:
        // [sc5],4 =.2 sc6
        // [sc11],4 =.2 sc12
        // 
        // loop1ExitA:
        // (p2) sc6 =.2 [sc4],4
        // ar.lc = sc3
        // (p2) [sc5],4 =.2 sc6
        // br.ret
        // 
        // copyDown:
        // sc4 = sc1 + first_element_offset-2
        // sc5 = sc2 + first_element_offset-2
        // sc10 = sc1 + first_element_offset-4
        // sc11 = sc2 + first_element_offset-4
        // sc18 = sc1 + first_element_offset-2
        // 
        // sc17 =.2 [sc18] // prefetch
        // sc4 = length<<1 + sc4
        // sc5 = length<<1 + sc5
        // sc10 = length<<1 + sc10
        // sc11 = length<<1 + sc11
        // (p4) br.cond loop2ExitA
        // 
        // sc6 =.2 [sc4],-4
        // sc12 =.2 [sc10],-4
        // (p6) br.cond loop2Exit
        // 
        // loop2:
        // [sc5],-4 =.2 sc6
        // [sc11],-4 =.2 sc12
        // sc6 =.2 [sc4],-4
        // sc12 =.2 [sc10],-4
        // br.cloop loop2
        // 
        // loop2Exit:
        // [sc5],-4 =.2 sc6
        // [sc11],-4 =.2 sc12
        // 
        // loop2ExitA:
        // (p2) sc6 =.2 [sc4],-4
        // ar.lc = sc3
        // (p2) [sc5],-4 =.2 sc6
        // br.ret


        // sc1 = srcOffset<<1 + src
        // sc2 = dstOffset<<1 + dst
        // p3,p0 = (length > 32)
        // p4,p0 = (length < 2)
        // sc8 = length >> 1
        // (p3) br mem_cpy
        emitter.ipf_shladd(sc1, srcOffset, 1, src);
        emitter.ipf_shladd(sc2, dstOffset, 1, dst);
        emitter.ipf_cmpi(icmp_lt, cmp_none, p3, p0, 32, length);
        emitter.ipf_cmpi(icmp_gt, cmp_none, p4, p0, 2, length);
        emitter.ipf_shri(sc8, length, 1);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, mem_cpy, p3);
        emitter.flush_buffer();

        // p6,p0 = (length < 4)
        // sc9 = length & 1
        // p1,p0 = (dstOffset > srcOffset)
        // sc18 = length<<1 + sc1
        // sc3 = ar.lc
        // sc7 = sc8 - 2
        emitter.ipf_cmpi(icmp_gt, cmp_none, p6, p0, 4, length);
        emitter.ipf_andi(sc9, 1, length);
        emitter.ipf_cmp(icmp_gt, cmp_none, p1, p0, dstOffset, srcOffset);
        emitter.ipf_shladd(sc18, length, 1, sc1);
        emitter.ipf_mfap(sc3, AR_lc);
        emitter.ipf_adds(sc7, -2, sc8);
        emitter.flush_buffer();

        // p2 = (sc9 != 0)
        // ar.lc = sc7
        // sc18 = sc18 + first_element_offset-2
        // (p1) p1,p0 = cmp.unc (src == dst)
        // (p1) br copyDown
        emitter.ipf_cmp(icmp_ne, cmp_none, p2, p0, sc9, 0);
        emitter.ipf_mtap(AR_lc, sc7);
        emitter.ipf_adds(sc18, first_element_offset-2, sc18);
        emitter.ipf_cmp(icmp_eq, cmp_unc, p1, p0, src, dst, false, p1);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, copyDown, p1);
        emitter.flush_buffer();

        // sc17 =.2 [sc18] // prefetch
        // sc4 = sc1 + first_element_offset
        // sc5 = sc2 + first_element_offset
        // sc10 = sc1 + (first_element_offset+2)
        // sc11 = sc2 + (first_element_offset+2)
        // (p4) br.cond loop1ExitA
        emitter.ipf_ld(int_mem_size_2, mem_ld_none, mem_none, sc18, sc18);
        emitter.ipf_adds(sc4, first_element_offset, sc1);
        emitter.ipf_adds(sc5, first_element_offset, sc2);
        emitter.ipf_adds(sc10, first_element_offset+2, sc1);
        emitter.ipf_adds(sc11, first_element_offset+2, sc2);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, loop1ExitA, p4);
        emitter.flush_buffer();

        // sc6 =.2 [sc4],4
        // sc12 =.2 [sc10],4
        // (p6) br.cond loop1Exit
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, 4);
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc12, sc10, 4);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, loop1Exit, p6);
        emitter.flush_buffer();

        // loop1:
        // [sc5],4 =.2 sc6
        // [sc11],4 =.2 sc12
        // sc6 =.2 [sc4],4
        // sc12 =.2 [sc10],4
        // br.cloop loop1
        emitter.set_target(loop1);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, 4);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc11, sc12, 4);
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, 4);
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc12, sc10, 4);
        emitter.ipf_br(br_cloop, br_few, br_sptk, br_none, loop1);

        // loop1Exit:
        // [sc5],4 =.2 sc6
        // [sc11],4 =.2 sc12
        emitter.set_target(loop1Exit);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, 4);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc11, sc12, 4);

        // loop1ExitA:
        // (p2) sc6 =.2 [sc4],4
        // ar.lc = sc3
        // (p2) [sc5],4 =.2 sc6
        // br.ret
        emitter.set_target(loop1ExitA);
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, 4, p2);
        emitter.ipf_mtap(AR_lc, sc3);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, 4, p2);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);

        // copyDown:
        // sc4 = sc1 + first_element_offset-2
        // sc5 = sc2 + first_element_offset-2
        // sc10 = sc1 + first_element_offset-4
        // sc11 = sc2 + first_element_offset-4
        // sc18 = sc1 + first_element_offset-2
        emitter.set_target(copyDown);
        emitter.ipf_adds(sc4, first_element_offset-2, sc1);
        emitter.ipf_adds(sc5, first_element_offset-2, sc2);
        emitter.ipf_adds(sc10, first_element_offset-4, sc1);
        emitter.ipf_adds(sc11, first_element_offset-4, sc2);
        emitter.ipf_adds(sc18, first_element_offset-2, sc1);
        emitter.flush_buffer();

        // sc17 =.2 [sc18] // prefetch
        // sc4 = length<<1 + sc4
        // sc5 = length<<1 + sc5
        // sc10 = length<<1 + sc10
        // sc11 = length<<1 + sc11
        // (p4) br.cond loop2ExitA
        emitter.ipf_ld(int_mem_size_2, mem_ld_none, mem_none, sc17, sc18);
        emitter.ipf_shladd(sc4, length, 1, sc4);
        emitter.ipf_shladd(sc5, length, 1, sc5);
        emitter.ipf_shladd(sc10, length, 1, sc10);
        emitter.ipf_shladd(sc11, length, 1, sc11);
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, loop2ExitA, p4);
        emitter.flush_buffer();

        // sc6 =.2 [sc4],-4
        // sc12 =.2 [sc10],-4
        // (p6) br.cond loop2Exit
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, unsigned(-4));
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc12, sc10, unsigned(-4));
        emitter.ipf_br(br_cond, br_few, br_spnt, br_none, loop2Exit, p6);
        // 
        // loop2:
        // [sc5],-4 =.2 sc6
        // [sc11],-4 =.2 sc12
        // sc6 =.2 [sc4],-4
        // sc12 =.2 [sc10],-4
        // br.cloop loop2
        emitter.set_target(loop2);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, unsigned(-4));
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc11, sc12, unsigned(-4));
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, unsigned(-4));
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc12, sc10, unsigned(-4));
        emitter.ipf_br(br_cloop, br_few, br_sptk, br_none, loop2);

        // loop2Exit:
        // [sc5],-4 =.2 sc6
        // [sc11],-4 =.2 sc12
        emitter.set_target(loop2Exit);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, unsigned(-4));
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc11, sc12, unsigned(-4));

        // loop2ExitA:
        // (p2) sc6 =.2 [sc4],-4
        // ar.lc = sc3
        // (p2) [sc5],-4 =.2 sc6
        // br.ret
        emitter.set_target(loop2ExitA);
        emitter.ipf_ld_inc_imm(int_mem_size_2, mem_ld_none, mem_none, sc6, sc4, unsigned(-4), p2);
        emitter.ipf_mtap(AR_lc, sc3);
        emitter.ipf_st_inc_imm(int_mem_size_2, mem_st_none, mem_none, sc5, sc6, unsigned(-4), p2);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
    }

    // mem_cpy:
    // p1,p0 = cmp.ne srcOffset, 0
    // sc8 = andi 3, length
    // sc9 = shri 2, length
    // sc5 = src + first_element_offset
    // sc3 = ar.lc
    // sc10 = 128

    // sc7 =.8 [sc5],8
    // sc4 = sc9 - 2
    // p2,p0 = cmp.ne dstOffset, 0
    // p3,p0 = cmp.gt length, sc10
    // p4,p0 = cmp.ne sc8, 0
    // (p1) br.cond mem_cpy_final
    //
    // nop
    // ar.lc = sc4
    // sc6 = dst + first_element_offset
    // (p2) br.cond mem_cpy_pre_final
    // (p3) br.cond mem_cpy_pre_final
    // (p4) br.cond mem_cpy_pre_final
    //
    // loop3:
    // [sc6],8 =.8 sc7
    // sc7 =.8 [sc5],8
    // br.cloop loop3
    //
    // [sc6],8 =.8 sc7
    // ar.lc = sc3
    // br.ret

    // mem_cpy_pre_final:
    // ar.lc = sc3
    //
    // mem_cpy_final:
    // ...
    // 
    emitter.set_target(mem_cpy);
    {
        // p1,p0 = cmp.ne srcOffset, 0
        // sc9 = shri 3, length
        // p2,p0 = cmp.ne dstOffset, 0
        // sc8 = andi 7, length
        // (p1) br.cond mem_cpy_final
        // (p2) br.cond mem_cpy_final
        // 
        // sc5 = src + first_element_offset
        // sc18 = src + first_element_offset+64
        // p3,p0 = cmp.lt 16, sc9
        // p4,p0 = cmp.ne sc8, 0
        // (p3) br.cond mem_cpy_final
        // (p4) br.cond mem_cpy_final
        // 
        // sc11 = src + (first_element_offset+8)
        // sc3 = ar.lc
        // sc4 = sc9 - 2
        // sc7 =.8 [sc5],16
        // sc17 =.8 [sc18],64 // prefetch
        // p5,p0 = cmp.le 64, length
        // 
        // sc12 = dst + (first_element_offset+8)
        // ar.lc = sc4
        // p6,p0 = cmp.le 96, length
        // sc13 =.8 [sc11],16
        // (p5) sc17 =.8 [sc18],64 // prefetch
        // sc6 = dst + first_element_offset
        // 
        // loop3:
        // [sc6],16 =.8 sc7
        // [sc12],16 =.8 sc13
        // sc7 =.8 [sc5],16
        // sc13 =.8 [sc11],16
        // br.cloop loop3
        // 
        // [sc6],16 =.8 sc7
        // [sc12],16 =.8 sc13
        // ar.lc = sc3
        // br.ret


        // p1,p0 = cmp.ne srcOffset, 0
        // sc9 = shri 3, length
        // p2,p0 = cmp.ne dstOffset, 0
        // sc8 = andi 7, length
        // (p1) br.cond mem_cpy_final
        // (p2) br.cond mem_cpy_final
        emitter.ipf_cmp(icmp_ne, cmp_none, p1, p0, srcOffset, 0);
        emitter.ipf_shri(sc9, length, 3);
        emitter.ipf_cmp(icmp_ne, cmp_none, p2, p0, dstOffset, 0);
        emitter.ipf_andi(sc8, 7, length);
        emitter.ipf_br(br_cond, br_many, br_spnt, br_none, mem_cpy_final, p1);
        emitter.ipf_br(br_cond, br_many, br_spnt, br_none, mem_cpy_final, p2);
        emitter.flush_buffer();

        // sc5 = src + first_element_offset
        // sc18 = src + first_element_offset+64
        // p3,p0 = cmp.lt 16, sc9
        // p4,p0 = cmp.ne sc8, 0
        // (p3) br.cond mem_cpy_final
        // (p4) br.cond mem_cpy_final
        emitter.ipf_adds(sc5, first_element_offset, src);
        emitter.ipf_adds(sc18, first_element_offset+64, src);
        emitter.ipf_cmpi(icmp_lt, cmp_none, p3, p0, 16, sc9);
        emitter.ipf_cmp(icmp_ne, cmp_none, p4, p0, sc8, 0);
        emitter.ipf_br(br_cond, br_many, br_spnt, br_none, mem_cpy_final, p3);
        emitter.ipf_br(br_cond, br_many, br_spnt, br_none, mem_cpy_final, p4);
        emitter.flush_buffer();

        // sc11 = src + (first_element_offset+8)
        // sc3 = ar.lc
        // sc4 = sc9 - 2
        // sc7 =.8 [sc5],16
        // sc17 =.8 [sc18],64 // prefetch
        // p5,p0 = cmp.le 64, length
        emitter.ipf_adds(sc11, first_element_offset+8, src);
        emitter.ipf_mfap(sc3, AR_lc);
        emitter.ipf_adds(sc4, -2, sc9);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc7, sc5, 16);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc17, sc18, 64);
        emitter.ipf_cmpi(icmp_le, cmp_none, p5, p0, 64, length);
        emitter.flush_buffer();

        emitter.ipf_adds(sc12, first_element_offset+8, dst);
        emitter.ipf_mtap(AR_lc, sc4);
        emitter.ipf_cmpi(icmp_le, cmp_none, p6, p0, 96, length);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc13, sc11, 16);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc17, sc18, 64, p5);
        emitter.ipf_adds(sc6, first_element_offset, dst);
        emitter.flush_buffer();

        emitter.set_target(loop3);
        emitter.ipf_st_inc_imm(int_mem_size_8, mem_st_none, mem_none, sc6, sc7, 16);
        emitter.ipf_st_inc_imm(int_mem_size_8, mem_st_none, mem_none, sc12, sc13, 16);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc7, sc5, 16);
        emitter.ipf_ld_inc_imm(int_mem_size_8, mem_ld_none, mem_none, sc13, sc11, 16);
        emitter.ipf_br(br_cloop, br_few, br_sptk, br_none, loop3);

        emitter.ipf_st_inc_imm(int_mem_size_8, mem_st_none, mem_none, sc6, sc7, 16);
        emitter.ipf_st_inc_imm(int_mem_size_8, mem_st_none, mem_none, sc12, sc13, 16);
        emitter.ipf_mtap(AR_lc, sc3);
        emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);
    }

    // mem_cpy:
    // alloc, save b0, save gp
    // br.call memmove(dst, src, length*2)
    // restore b0, gp, pfs
    // br.ret
    emitter.set_target(mem_cpy_final);
    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = 5, num_out_args = 3;
    void *(*p_memmove)(void *dst, const void *src, size_t length);
    p_memmove = memmove;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_memmove,
        out0, save_pfs, save_b0, save_gp);
    emitter.ipf_shladd(sc1, srcOffset, 1, src);
    emitter.ipf_shladd(sc2, dstOffset, 1, dst);
    emitter.ipf_adds(out0+0, first_element_offset, sc2);
    emitter.ipf_adds(out0+1, first_element_offset, sc1);
    emitter.ipf_add(out0+2, length, length);
    emit_call_with_gp(emitter, (void **)p_memmove, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);

    emitter.flush_buffer();
    size_t size = emitter.get_size();

    addr = (void *)malloc_fixed_code_for_jit(size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);
    flush_hw_cache((Byte *)addr, size);
    sync_i_cache();


    return addr;
}


#ifdef VM_STATS
static unsigned compute_index(int64 value, bool is_length)
{
    if (value == 0)
        return 0; // zero
    if (is_length && value > 32)
        return 3; // large
    if (value % 2 == 0)
        return 2; // even
    return 1; // odd
}
#endif // VM_STATS

static void collect_char_arraycopy_stats(ManagedObject *src,
                                         int64 srcOffset,
                                         ManagedObject *dst,
                                         int64 dstOffset,
                                         int64 length)
{
#ifdef VM_STATS
    // 2003-07-24.  The following code helps to find the distribution of parameters
    // to the char arraycopy routine.  It is useful in the debugger only.
#define COUNT_MAX 128
    static unsigned srcOffsetCount[COUNT_MAX], dstOffsetCount[COUNT_MAX], lengthCount[COUNT_MAX];
    static unsigned src0_dst0_lengthEven, src0_dstEven_lengthEven, srcEven_dstEven_lengthEven;
    static unsigned lengthShort;
    static unsigned srcOffset80[COUNT_MAX], dstOffset80[COUNT_MAX];
    static unsigned iteration = 0;
    static unsigned properties[4][4][4];
    iteration ++;
    if (iteration >= 1000000)
    {
        int i,j,k;
        for (i=0; i<COUNT_MAX; i++)
        {
            srcOffsetCount[i] = dstOffsetCount[i] = lengthCount[i] = srcOffset80[i] = dstOffset80[i] = 0;
        }
        for (i=0; i<4; i++)
            for (j=0; j<4; j++)
                for (k=0; k<4; k++)
                    properties[i][j][k] = 0;
        src0_dst0_lengthEven = src0_dstEven_lengthEven = srcEven_dstEven_lengthEven = 0;
        lengthShort = 0;
        iteration = 0;
    }
    if (length <= COUNT_MAX)
    {
        lengthShort ++ ;
        if (srcOffset < COUNT_MAX)
            srcOffsetCount[srcOffset] ++;
        if (dstOffset < COUNT_MAX)
            dstOffsetCount[dstOffset] ++;
        if (length < COUNT_MAX)
            lengthCount[length] ++;
        if (srcOffset == 0 && dstOffset == 0 && (length & 0x1) == 0)
            src0_dst0_lengthEven ++;
        if (srcOffset == 0 && (dstOffset & 0x1) == 0 && (length & 0x1) == 0)
            src0_dstEven_lengthEven ++;
        if ((srcOffset & 0x1) == 0 && (dstOffset & 0x1) == 0 && (length & 0x1) == 0)
            srcEven_dstEven_lengthEven ++;
    }
    if (length == 80)
    {
        if (srcOffset < COUNT_MAX)
            srcOffset80[srcOffset] ++;
        if (dstOffset < COUNT_MAX)
            dstOffset80[dstOffset] ++;
    }
    properties[compute_index(srcOffset, false)][compute_index(dstOffset, false)][compute_index(length, true)] ++;
    // 2003-07-24.  End of experimental code section.

    uint16 *dst_addr = get_vector_element_address_uint16(dst, (int32)dstOffset);
    uint16 *src_addr = get_vector_element_address_uint16(src, (int32)srcOffset);
    VM_Statistics::get_vm_stats().num_char_arraycopies++;
    if (dst_addr == src_addr) {
        VM_Statistics::get_vm_stats().num_same_array_char_arraycopies++;
    }
    if (srcOffset == 0) {
        VM_Statistics::get_vm_stats().num_zero_src_offset_char_arraycopies++;
    }
    if (dstOffset == 0) {
        VM_Statistics::get_vm_stats().num_zero_dst_offset_char_arraycopies++;
    }
    if ((((POINTER_SIZE_INT)dst_addr & 0x7) == 0) && (((POINTER_SIZE_INT)src_addr & 0x7) == 0)) {
        VM_Statistics::get_vm_stats().num_aligned_char_arraycopies++;
    }
    VM_Statistics::get_vm_stats().total_char_arraycopy_length += length;
    VM_Statistics::get_vm_stats().char_arraycopy_count[get_log2((int)length)]++;
    bool both_aligned = (((POINTER_SIZE_INT)dst_addr & 0x7) == 0) && (((POINTER_SIZE_INT)src_addr & 0x7) == 0);
    if (both_aligned && (src != dst) && (length < 32)) {
        uint64 num_bytes_to_copy  = 2*length;
        uint64 num_uint64_to_copy = num_bytes_to_copy/sizeof(uint64);
        VM_Statistics::get_vm_stats().num_fast_char_arraycopies++;
        VM_Statistics::get_vm_stats().total_fast_char_arraycopy_uint64_copies += num_uint64_to_copy;
        VM_Statistics::get_vm_stats().char_arraycopy_uint64_copies[get_log2((int)num_uint64_to_copy)]++;
    }
#endif // VM_STATS
}

static void *create_direct_helper_call_wrapper(void **fptr, int num_args, const char *stub_name)
{
    if (fptr[1] == get_vm_gp_value())
        return fptr[0];
    assert(num_args <= 8);
    // alloc
    // save b0, gp, pfs
    // set new gp
    // copy arguments
    // call
    // restore b0, gp, pfs
    // return

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = num_args, num_out_args = num_args;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        fptr,
        out0, save_pfs, save_b0, save_gp);
    for (int i=0; i<num_args; i++)
    {
        emitter.ipf_mov(out0+i, IN_REG0+i);
    }
    emit_call_with_gp(emitter, fptr, false);

    // Restore pfs, b0, and gp
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);
    emitter.ipf_brret(br_many, br_sptk, br_none, BRANCH_RETURN_LINK_REG);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    void *addr = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)addr);

    flush_hw_cache((Byte *)addr, stub_size);
    sync_i_cache();
    
    return addr;
}


static void *get_aastore_test_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
    {
        addr = get_vm_rt_aastore_test_address_compactor();
    }
    return addr;
}


static void *get_instanceof_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
    {
        addr = get_vm_rt_instanceof_address_compactor();
    }
    return addr;
}


static void *get_interface_vtable_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
    {
        void *(*p_vm_rt_get_interface_vtable)(ManagedObject *object, Class *clss);
        p_vm_rt_get_interface_vtable = vm_rt_get_interface_vtable;
        addr = create_direct_helper_call_wrapper((void **)p_vm_rt_get_interface_vtable, 2, "get_interface_vtable_wrapper_compactor");
    }
    return addr;
}


static void *get_chararraycopy_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
    {
        addr = gen_faster_char_arraycopy_no_exc();
    }
    return addr;
}


static void *get_fdiv_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_fdiv, 2, "fdiv_wrapper_compactor");
    // maybe it should take 0 arguments since both are floating point
    return addr;
}


static void *get_frem_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_frem, 2, "frem_wrapper_compactor");
    // maybe it should take 0 arguments since both are floating point
    return addr;
}


static void *get_drem_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_drem, 2, "drem_wrapper_compactor");
    // maybe it should take 0 arguments since both are floating point
    return addr;
}


static void *get_f2i_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_f2i, 1, "f2i_wrapper_compactor");
    // maybe it should take 0 arguments it is floating point
    return addr;
}


static void *get_f2l_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_f2l, 1, "f2l_wrapper_compactor");
    // maybe it should take 0 arguments it is floating point
    return addr;
}


static void *get_lmul_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_lmul, 2, "lmul_wrapper_compactor");
    return addr;
}


static void *get_imul_compactor()
{
    static void *addr = NULL;
    if (addr == NULL)
        addr = create_direct_helper_call_wrapper((void **)vm_rt_imul, 2, "imul_wrapper_compactor");
    return addr;
}



#ifdef VM_STATS

static void increment_helper_count(VM_RT_SUPPORT f) {
#if _DEBUG
    static bool print_helper_info = false;
    if (print_helper_info) {
        int   num_args = vm_helper_get_numargs(f);
        const char *fn_name = vm_helper_get_name(f);
        printf("Calling helper %s, %d args\n", fn_name, num_args);
    }
#endif // _DEBUG

    // Increment the number of times that f was called by a JIT. 
    VM_Statistics::get_vm_stats().rt_function_calls.add((void *)f, /*value*/ 1, /*value1*/ NULL);
} //increment_helper_count


void *emit_counting_wrapper_for_jit_helper(VM_RT_SUPPORT f, void *helper, int num_args, const char *helper_name)
{
    // If we already created a wrapper for this JIT helper, return it.
    static SimpleHashtable helper_wrapper_map(53);
    int ignored;
    void *wrapper;
    if (helper_wrapper_map.lookup((void *)f, &ignored, &wrapper)) {
        return wrapper;
    }

    // Create a new counting wrapper for helper f. It calls a procedure to increment the number of times the JIT 
    // has called the helper, then jumps to that helper. It does a jump rather than a call 1) to have the helper return
    // directly to the original caller and 2) to ensure that the current frame can become the m2n frame in case the
    // helper throws an exception or the stack is otherwise unwound.
    assert(num_args <= 8);
    // alloc
    // save b0, gp, pfs
    // save fp args
    // set new gp for increment_helper_count()
    // call increment_helper_count(f)
    // Restore fp args
    // restore original b0, gp, pfs
    // branch to (don't call) helper

    tl::MemoryPool mem_pool;
    Merced_Code_Emitter emitter(mem_pool, 2, 0);
    emitter.disallow_instruction_exchange();
    emitter.memory_type_is_unknown();

    // Allocate frame, save pfs, b0, and gp
    int out0, save_pfs, save_b0, save_gp;
    const int num_in_args = num_args;
    const int num_out_args = ((num_args > 0)? num_args : 1);
    void (*p_increment_helper_count)(VM_RT_SUPPORT f);
    p_increment_helper_count = increment_helper_count;
    emit_alloc_for_single_call(emitter, num_in_args, num_out_args,
        (void **)p_increment_helper_count,
        out0, save_pfs, save_b0, save_gp);

    // Save fp args
    unsigned fpreg;
    for (fpreg = 8;  fpreg <= 15;  fpreg++) {
        emitter.ipf_stf_inc_imm(float_mem_size_8, mem_st_spill, mem_none, SP_REG, fpreg, unsigned(-16));
    }

    // Call increment_helper_count. 
    emit_movl_compactor(emitter, out0, (uint64)f);
    emit_call_with_gp(emitter, (void **)p_increment_helper_count, false);

    // Restore fp args
    emitter.ipf_adds(SP_REG, 16, SP_REG);
    for (fpreg = 15;  fpreg > 8;  fpreg--) {
        emitter.ipf_ldf_inc_imm(float_mem_size_8, mem_ld_fill, mem_none, fpreg, SP_REG, 16);
    }
    emitter.ipf_ldf(float_mem_size_8, mem_ld_fill, mem_none, fpreg, SP_REG);

    // Restore pfs, b0, and gp.
    emit_dealloc_for_single_call(emitter, save_pfs, save_b0, save_gp);
    enforce_calling_conventions(&emitter);

    // Branch to the JIT runtime helper.
    emit_movl_compactor(emitter, SCRATCH_GENERAL_REG, (uint64)helper);
    emitter.ipf_mtbr(SCRATCH_BRANCH_REG, SCRATCH_GENERAL_REG);
    emitter.ipf_bri(br_cond, br_many, br_sptk, br_none, SCRATCH_BRANCH_REG);

    emitter.flush_buffer();
    size_t stub_size = emitter.get_size();
    wrapper = (void *)malloc_fixed_code_for_jit(stub_size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_MAX/2, CAA_Allocate);
    emitter.copy((char *)wrapper);

    flush_hw_cache((Byte *)wrapper, stub_size);
    sync_i_cache();
    
    helper_wrapper_map.add((void *)f, /*value*/ 0, /*value1*/ wrapper);
    return wrapper;
} //emit_counting_wrapper_for_jit_helper


static void register_rt_support_addr_request(VM_RT_SUPPORT f) {
    // Increment the number of times that f was requested by a JIT. This is not the number of calls to that function,
    // but this does tell us how often a call to that function is compiled into JITted code.
    VM_Statistics::get_vm_stats().rt_function_requests.add((void *)f, /*value*/ 1, /*value1*/ NULL);
} //register_rt_support_addr_request

#endif //VM_STATS



void *vm_get_rt_support_addr(VM_RT_SUPPORT f)
{
    bool dereference_fptr = true;
    void *fptr = (void*)unimplemented_rt_support_func; // gashiman - added type conversion
                                                       // because gcc complained otherwise

#ifdef VM_STATS
    register_rt_support_addr_request(f);
#endif // VM_STATS

        NativeCodePtr res = rth_get_lil_helper(f);
        if (res) return res;

 
    switch(f) {
    case VM_RT_LDC_STRING:
        fptr = get_vm_rt_ldc_string_address();
        dereference_fptr = false;
        break;
    case VM_RT_MULTIANEWARRAY_RESOLVED:
        fptr = get_vm_rt_multianewarray_resolved_address();
        dereference_fptr = false;
        break;
    case VM_RT_NEW_RESOLVED_USING_VTABLE_AND_SIZE:
        fptr = get_vm_rt_new_resolved_using_vtable_address_and_size(true); // true == inline if possible
        dereference_fptr = false;
        break;
    case VM_RT_NEW_VECTOR_USING_VTABLE:
        fptr = get_vm_rt_new_vector__using_vtable_address();
        dereference_fptr = false;
        break;
    case VM_RT_AASTORE:
        fptr = get_vm_rt_aastore_address_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_AASTORE_TEST:
        fptr = get_aastore_test_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_IDX_OUT_OF_BOUNDS:
        fptr = get_vm_rt_idx_out_of_bounds_address();
        dereference_fptr = false;
        break;
    case VM_RT_ARRAY_STORE_EXCEPTION:
        fptr = get_vm_rt_array_store_exception_address();
        dereference_fptr = false;
        break;
    case VM_RT_NULL_PTR_EXCEPTION:
        fptr = get_vm_rt_null_ptr_exception_address();
        dereference_fptr = false;
        break;
    case VM_RT_DIVIDE_BY_ZERO_EXCEPTION:
        fptr = get_vm_rt_divide_by_zero_exception_address();
        dereference_fptr = false;
        break;
    case VM_RT_THROW:
    case VM_RT_THROW_SET_STACK_TRACE:
        fptr = get_vm_rt_athrow_naked_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_F2I:
        fptr = get_f2i_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_F2L:
        fptr = get_f2l_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_LMUL:
        fptr = get_lmul_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_LREM:
        fptr = get_vm_rt_lrem_address();
        dereference_fptr = false;
        break;
    case VM_RT_LDIV:
        fptr = get_vm_rt_ldiv_address();
        dereference_fptr = false;
        break;
    case VM_RT_IMUL:
        fptr = get_imul_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_IREM:
        fptr = get_vm_rt_irem_address();
        dereference_fptr = false;
        break;
    case VM_RT_IDIV:
        fptr = get_vm_rt_idiv_address();
        dereference_fptr = false;
        break;
    case VM_RT_FREM:
        fptr = get_frem_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_FDIV:
        fptr = get_fdiv_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_DREM:
        fptr = get_drem_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_DDIV:
        fptr = get_fdiv_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_CHECKCAST:
        fptr = get_vm_rt_checkcast_address_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_INSTANCEOF:
        fptr = get_instanceof_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_ENTER:
        fptr =  get_vm_rt_monitor_enter_address(true);
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_ENTER_STATIC:
        fptr =  get_vm_rt_monitor_enter_static_address();
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_ENTER_NON_NULL:
        fptr =  get_vm_rt_monitor_enter_address(false);
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_EXIT:
        fptr =  get_vm_rt_monitor_exit_address(true);
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_EXIT_STATIC:
        fptr =  get_vm_rt_monitor_exit_static_address();
        dereference_fptr = false;
        break;
    case VM_RT_MONITOR_EXIT_NON_NULL:
        fptr =  get_vm_rt_monitor_exit_address(false);
        dereference_fptr = false;
        break;
    case VM_RT_GET_INTERFACE_VTABLE_VER0:
        fptr = get_interface_vtable_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_INITIALIZE_CLASS:
        fptr = get_vm_rt_initialize_class_compactor();
        dereference_fptr = false;
        break;
    case VM_RT_CHAR_ARRAYCOPY_NO_EXC:
        fptr = get_chararraycopy_compactor();
        dereference_fptr = false;
        break;
    default:
        printf("vm_get_rt_support_addr: unimplemented function on IPF: f=%d\n", f);
        break;
    }
    assert(fptr);

    void *helper;
    assert(!dereference_fptr);
    if(dereference_fptr) {
        helper = *(void **)fptr;
    } else {
        helper = fptr;
    }

#ifdef VM_STATS
    if (true) {
        int   num_args = vm_helper_get_numargs(f);
        const char *helper_name = vm_helper_get_name(f);
        void *wrapper = emit_counting_wrapper_for_jit_helper(f, helper, num_args, helper_name);
        return wrapper;
    } else
#endif // VM_STATS
    return helper;
} //vm_get_rt_support_addr

/* 03/07/30: temporary interface change!!! */
void *vm_get_rt_support_addr_optimized(VM_RT_SUPPORT f, Class_Handle c) {
    Class *clss = (Class*) c;
    if (clss == NULL)
    {
        return vm_get_rt_support_addr(f);
    }

    switch (f) {
    case VM_RT_NEW_RESOLVED_USING_VTABLE_AND_SIZE:
        if (class_is_finalizable(c))
            return get_vm_rt_new_resolved_using_vtable_address_and_size(false); // false == inline if possible (i.e., don't inline!)
        else
            return vm_get_rt_support_addr(f);
        break;
    default:
        return vm_get_rt_support_addr(f);
        break;
    }
}

