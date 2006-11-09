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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.3.4.3 $
 */  

#define LOG_DOMAIN "enumeration"
#include "cxxlog.h"

#include "root_set_enum_internal.h"
#include "GlobalClassLoaderIterator.h"
#include "jit_intf_cpp.h"
#include "interpreter.h"
#include "vm_stats.h"
#include "m2n.h"
#include "open/vm_util.h"
#include "open/gc.h"
#include "finalize.h"
#include "cci.h"

void vm_enumerate_interned_strings()
{
    TRACE2("enumeration", "vm_enumerate_interned_strings()");
    // string enumeration should be done in stop_the_world phase.
    // see String_Pool for more information
    String *ps = VM_Global_State::loader_env->string_pool.get_first_string_intern();
    // 20030405 Don't enumerate references that are *unmanaged null* (i.e. zero/NULL)
    // since vm_enumerate_root_reference() expects to be called with slots containing managed refs.
    if (VM_Global_State::loader_env->compress_references) {
        while (ps != NULL) {
            COMPRESSED_REFERENCE compressed_ref = ps->intern.compressed_ref;
            assert(is_compressed_reference(compressed_ref));
            assert(compressed_ref != 0);
            vm_enumerate_compressed_root_reference((COMPRESSED_REFERENCE *)&ps->intern.compressed_ref, 
                VM_Global_State::loader_env->pin_interned_strings);
            ps = VM_Global_State::loader_env->string_pool.get_next_string_intern();
        }
    } else {
        while (ps != NULL) {
            ManagedObject* s = ps->intern.raw_ref;
            assert(s != NULL);
            vm_enumerate_root_reference((void **)&(ps->intern.raw_ref), 
                VM_Global_State::loader_env->pin_interned_strings);
            ps = VM_Global_State::loader_env->string_pool.get_next_string_intern();
        }
    }
} //vm_enumerate_interned_strings




// Enumerate all globally visible classes and their static fields.

void vm_enumerate_static_fields()
{
    TRACE2("enumeration", "vm_enumerate_static_fields()");
    Global_Env *global_env = VM_Global_State::loader_env;
    ManagedObject** ppc;
    GlobalClassLoaderIterator ClIterator;
    ClassLoader *cl = ClIterator.first();
    while(cl) {
        GlobalClassLoaderIterator::ClassIterator itc;
        GlobalClassLoaderIterator::ReportedClasses RepClasses = cl->GetReportedClasses();
        for (itc = RepClasses->begin(); itc != RepClasses->end(); itc++)
        {
            ppc = &itc->second;
            assert(*ppc);
            Class* c = jclass_to_struct_Class((jclass)ppc);

            if(c->in_error()) {
                vm_enumerate_root_reference(
                        (void**)c->get_error_cause() ,FALSE);
            }
            vm_enumerate_root_reference((void**)ppc, FALSE);
            ConstPoolEntry* cp = c->get_constant_pool().get_error_chain();
            while(cp) {
                vm_enumerate_root_reference((void**)(&(cp->error.cause)), FALSE);
                cp = cp->error.next;
            }
            // Finally enumerate the static fields of the class
            unsigned n_fields = c->get_number_of_fields();
            if(c->is_at_least_prepared()) {
                // Class has been prepared, so we can iterate over all its fields.
                for(unsigned i = 0; i < n_fields; i++) {
                    Field* f = c->get_field(i);
                    if(f->is_static()) {
                        char desc0 = f->get_descriptor()->bytes[0];
                        if(desc0 == 'L' || desc0 == '[') {
                            // The field is static and it is a reference.
                            if (global_env->compress_references) {
                                vm_enumerate_compressed_root_reference((uint32 *)f->get_address(), FALSE);
                            } else {
                                vm_enumerate_root_reference((void **)f->get_address(), FALSE);
                            }
                        }
                    }
                }
            }
        }
        cl = ClIterator.next();
    }
} //vm_enumerate_static_fields




// This is the main function used to enumerate Java references by the VM and the JITs.  
// It is part of the JIT-VM interface.
// 20030405 Note: When compressing references, vm_enumerate_root_reference() expects to be called with slots
// containing *managed* refs (represented by heap_base if null, not 0/NULL), so those refs must not be NULL. 
void 
vm_enumerate_root_reference(void **ref, Boolean is_pinned)
{
    TRACE2("vm.enum", "vm_enumerate_root_reference(" 
            << ref << " -> " << *ref << ")");
#if _DEBUG
        if (VM_Global_State::loader_env->compress_references) {
            // 20030324 DEBUG: verify the slot whose reference is being passed.
            ManagedObject **p_obj = (ManagedObject **)ref;  
            ManagedObject* obj = *p_obj;
            assert(obj != NULL);    // See the comment at the top of the procedure.
            if ((void *)obj != VM_Global_State::loader_env->heap_base) {
                assert(((POINTER_SIZE_INT)VM_Global_State::loader_env->heap_base <= (POINTER_SIZE_INT)obj)
                    && ((POINTER_SIZE_INT)obj <= (POINTER_SIZE_INT)VM_Global_State::loader_env->heap_end));
                (obj->vt())->clss->get_name()->bytes;
            } 
        }
#endif // _DEBUG

    gc_add_root_set_entry((Managed_Object_Handle *)ref, is_pinned);
} //vm_enumerate_root_reference



// Resembles vm_enumerate_root_reference() but is passed the address of a uint32 slot containing a compressed reference.
VMEXPORT void vm_enumerate_compressed_root_reference(uint32 *ref, Boolean is_pinned)
{
    assert(VM_Global_State::loader_env->compress_references);

#if _DEBUG
        // 20030324 Temporary: verify the slot whose reference is being passed.
        COMPRESSED_REFERENCE compressed_ref = *ref;
        ManagedObject* obj = (ManagedObject *)uncompress_compressed_reference(compressed_ref);
        bool is_null    = (compressed_ref == 0);
        bool is_in_heap = (((POINTER_SIZE_INT)VM_Global_State::loader_env->heap_base <= (POINTER_SIZE_INT)obj)
            && ((POINTER_SIZE_INT)obj <= (POINTER_SIZE_INT)VM_Global_State::loader_env->heap_end));
        if (is_null || is_in_heap) {
            // Make sure the reference is valid.
            if (!is_null) {
                (obj->vt())->clss->get_name()->bytes;
            }                                                               
        } else {
            ASSERT(0, "Bad slot pointer");
        }
#endif // _DEBUG

    gc_add_compressed_root_set_entry(ref, is_pinned);
} //vm_enumerate_compressed_root_reference



// This is the main function used to enumerate interior pointers by the JITS.
// It is part of the JIT-VM interface and is currently used only by IPF Java JITs.
void 
vm_enumerate_root_interior_pointer(void **slot, int offset, Boolean is_pinned)
{
    gc_add_root_set_entry_interior_pointer(slot, offset, is_pinned);
} //vm_enumerate_root_interior_pointer

void vm_enumerate_root_interior_pointer_with_base(void **slot_root, void **slot_base, Boolean is_pinned)
{
    int offset = (int)(POINTER_SIZE_INT)(*((Byte**)slot_root)-*((Byte**)slot_base));
    gc_add_root_set_entry_interior_pointer(slot_root, offset, is_pinned);
}

void 
vm_enumerate_root_set_global_refs()
{
    // ! The enumeration code is duplicated in !
    // ! ti_enumerate_globals(), plase apply   !
    // ! all changes there too.                !

    ////////////////////////////////////////
    ///// First enumerate strong pointers

    // Static fields of all classes
    vm_enumerate_static_fields();
    vm_enumerate_objects_to_be_finalized();
    vm_enumerate_references_to_enqueue();
    oh_enumerate_global_handles();

    ////////////////////////////////////////
    //// Now enumerate weak pointers
    vm_enumerate_interned_strings();

    extern void vm_enumerate_root_set_mon_arrays();
    vm_enumerate_root_set_mon_arrays();

    ClassLoader::gc_enumerate();

    // this enumeration part is needed only for real garbage collection,
    // and not needed for JVMTI IterateOverReachableObjects
    if (VM_Global_State::loader_env->TI->isEnabled()) {
        VM_Global_State::loader_env->TI->enumerate();
    }

} //vm_enumerate_root_set_global_refs



//
// Enumerate references associated with a thread which are not stored on
// the thread's stack.
//
VMEXPORT // temporary solution for interpreter unplug
void vm_enumerate_root_set_single_thread_not_on_stack(VM_thread *thread)
{
    assert(thread);
    if (thread->thread_exception.exc_object != NULL) {
        vm_enumerate_root_reference((void **)&(thread->thread_exception.exc_object), FALSE);
    }
    if (thread->thread_exception.exc_cause != NULL) {
        vm_enumerate_root_reference((void **)&(thread->thread_exception.exc_cause), FALSE);
    }
    if (thread->p_exception_object_ti != NULL) {
        vm_enumerate_root_reference((void **)&(thread->p_exception_object_ti), FALSE);
    }

    if (thread->native_handles)
        thread->native_handles->enumerate();
    if (thread->gc_frames) {
        thread->gc_frames->enumerate();
    }
} //vm_enumerate_root_set_single_thread_not_on_stack


// Enumerate references associated with a thread which are stored on the thread's stack
// (including local handles of M2nFrames) given a stack iterator for the thread's entire stack.
// Consumes the iterator.
void vm_enumerate_root_set_single_thread_on_stack(StackIterator* si)
{
    ASSERT_NO_INTERPRETER
    while (!si_is_past_end(si)) {
        CodeChunkInfo* cci = si_get_code_chunk_info(si);
        if (cci) {
#ifdef VM_STATS
            vm_stats_inc(VM_Statistics::get_vm_stats().num_unwind_java_frames_gc);
            vm_stats_inc(cci->num_unwind_java_frames_gc);
#endif
            TRACE2("enumeration", "enumerating eip=" << (void *) si_get_ip(si)
                << " is_first=" << !si_get_jit_context(si)->is_ip_past
                << " " << class_get_name(method_get_class(cci->get_method()))
                << "." << method_get_name(cci->get_method())
                << method_get_descriptor(cci->get_method()));
            cci->get_jit()->get_root_set_from_stack_frame(cci->get_method(), 0, si_get_jit_context(si));
            TRACE2("enumeration", "enumerated eip=" << (void *) si_get_ip(si)
                << " is_first=" << !si_get_jit_context(si)->is_ip_past
                << " " << class_get_name(method_get_class(cci->get_method()))
                << "." << method_get_name(cci->get_method())
                << method_get_descriptor(cci->get_method()));
        } else {
#ifdef VM_STATS
            vm_stats_inc(VM_Statistics::get_vm_stats().num_unwind_native_frames_gc);
#endif
            TRACE2("enumeration", "enumeration local handles " 
                << (m2n_get_method(si_get_m2n(si)) ? method_get_name(m2n_get_method(si_get_m2n(si))) : "")
                << (m2n_get_method(si_get_m2n(si)) ? method_get_descriptor(m2n_get_method(si_get_m2n(si))) : ""));
            oh_enumerate_handles(m2n_get_local_handles(si_get_m2n(si)));
        }
        si_goto_previous(si, false);
    }
    si_free(si);
}
