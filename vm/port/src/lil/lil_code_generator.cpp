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
 * @author Intel, Evgueni Brevnov, Ivan Volosyuk
 * @version $Revision: 1.1.2.1.4.4 $
 */  


#include "nogc.h"
#include "jvmti_direct.h"
#include "environment.h"
#include "compile.h"

#include "jit_intf.h"
#include "lil.h"
#include "lil_code_generator.h"

#ifdef _IA32_
#include "lil_code_generator_ia32.h"
#elif _EM64T_
#include "lil_code_generator_em64t.h"
#elif _IPF_
#include "lil_code_generator_ipf.h"
#endif


LilCodeGenerator* LilCodeGenerator::get_platform()
{
#ifdef _IA32_
    static LilCodeGeneratorIa32 cg;
#elif _EM64T_
    static LilCodeGeneratorEM64T cg;
#elif _IPF_
    static LilCodeGeneratorIpf cg;
#endif
    return (LilCodeGenerator*)&cg;
}

LilCodeGenerator::LilCodeGenerator()
{
}

NativeCodePtr LilCodeGenerator::compile(LilCodeStub* cs)
{
    size_t stub_size;
    NativeCodePtr stub = compile_main(cs, &stub_size);
    lil_cs_set_code_size(cs, stub_size);
    
    compile_add_dynamic_generated_code_chunk("unknown", stub, stub_size);

    if (VM_Global_State::loader_env->TI->isEnabled())
        jvmti_send_dynamic_code_generated_event("unknown", stub, stub_size);

    return stub;
}


NativeCodePtr LilCodeGenerator::allocate_memory(size_t size)
{
    NativeCodePtr buf = malloc_fixed_code_for_jit(size, DEFAULT_CODE_ALIGNMENT, CODE_BLOCK_HEAT_DEFAULT, CAA_Allocate);

    // Check for 16-byte alignment
    assert((((POINTER_SIZE_INT)buf)&15)==0);
    return buf;
}
