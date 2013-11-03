/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef _COMPILE_H_
#define _COMPILE_H_

#include "vm_core_types.h"
#include "environment.h"

class JIT;

extern JIT *jit_compilers[];

void vm_add_jit(JIT *jit);
void vm_delete_all_jits();

/**
 * Protect arguments from GC.
 *
 * The arguments are those passed to the last <code>M2nFrame</code>.
 * The given method's signature describes what arguments were passed.
 */
 
void compile_protect_arguments(Method_Handle method, GcFrame* gc);

/** 
 * Code has been generated by writing it to memory.
 *
 * Ensure that it is visible to all instruction fetchers.
 * The first function flushes a particular block of code,
 * another should be called after all blocks are flushed.
 */
void compile_flush_generated_code_block(U_8*, size_t);
void compile_flush_generated_code();
/**
 * Execution manager uses this method to call back to VM to compile
 * a method using a specific JIT.
 */

JIT_Result compile_do_compilation_jit(Method* method, JIT* jit);

/**
 * Generate a stub in JIT calling convention to pass call
 * compile_me() on the first method invocation.
 */
NativeCodePtr compile_gen_compile_me(Method_Handle method);

/**
 * A function to call on the first method invocation.
 * @return an entry point of successfully compiled method
 * @throws an exception otherwise
 */
NativeCodePtr compile_me(Method* method);

void patch_code_with_threads_suspended(U_8* code_block, U_8* new_code, size_t size);

typedef char* Emitter_Handle;

struct Compilation_Handle {
    Global_Env* env;
    JIT*        jit;
};

Global_Env* compile_handle_to_environment(Compile_Handle);

/**
 * Create a <code>LIL</code> stub for JNI interfacing.
 */

NativeCodePtr compile_create_lil_jni_stub(Method_Handle method, void* func, NativeStubOverride nso);

/**
 * Create a <code>LIL</code> stub for JNI interfacing.
 */

NativeCodePtr interpreter_compile_create_lil_jni_stub(Method_Handle method, void* func, NativeStubOverride nso);


/**
 * Create a <code>LIL</code> stub for <code>PINVOKE</code> interfacing.
 */

NativeCodePtr compile_create_lil_pinvoke_stub(Method_Handle method, void* func, NativeStubOverride nso);

/**
 * Support for stub override code sequences. 
 */

typedef void (Override_Generator)(Emitter_Handle, Method *);
typedef unsigned (Override_Size)(Method *);

typedef struct Stub_Override_Entry {
    const char *class_name;
    const char *method_name;
    const char *descriptor;
    Override_Generator *override_generator;
    Override_Size *override_size;
} Stub_Override_Entry;

/** 
 * Points to an an array of override entries with 
 * <code>sizeof_stub_override_entries</code> entries.
 */
extern Stub_Override_Entry *stub_override_entries;

extern int sizeof_stub_override_entries;

// does this method need an override?
bool needs_override(Method*);


/**
 * Method instrumentation support.
 * Type of the instrumentation procedures called before invoking methods.
 */
typedef void (*MethodInstrumentationProc)(CodeChunkInfo *callee);

/**
 * Interpose on calls to the specified method by calling the <code>instr_proc</code>.
 */
NativeCodePtr compile_do_instrumentation(CodeChunkInfo *callee, MethodInstrumentationProc instr_proc);

/**
 * A <code>MethodInstrumentationProc</code> that records the number of 
 * calls from the caller code chunk to the callee.
 */

void count_method_calls(CodeChunkInfo *callee);

struct DynamicCode
{
    const char *name;
    bool free_name;
    const void *address;
    size_t length;
    DynamicCode *next;
};

/**
 * Adding dynamic generated code info to global list.
 * Is used in JVMTI and native frames interface
 */
DynamicCode* compile_get_dynamic_code_list(void);
/** 
 * Adding dynamic generated code info to global list.
 */
void compile_add_dynamic_generated_code_chunk(const char* name, bool free_name, const void* address, size_t length);
void compile_clear_dynamic_code_list(DynamicCode* list);


enum VM_Code_Type {
    VM_TYPE_JAVA,
    VM_TYPE_UNKNOWN
};

/**
 * Quick inline to call EM lookup and find a compiled method type
 */
static inline VM_Code_Type vm_identify_eip(void *addr)
{
    Global_Env *env = VM_Global_State::loader_env;
    if (NULL == env || NULL == env->em_interface)
        return VM_TYPE_UNKNOWN;

    Method_Handle m = env->em_interface->LookupCodeChunk(addr, FALSE,
        NULL, NULL, NULL);

    if (m == NULL)
        return VM_TYPE_UNKNOWN;
    else
        return VM_TYPE_JAVA;
} //vm_identify_eip


#endif
