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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.1.4.3 $
 */  





#ifndef _JIT_INTF_CPP_H_
#define _JIT_INTF_CPP_H_


#include "open/types.h"
#include "jit_intf.h"
#include "jit_export_jpda.h"
#include "jni.h"
#include "ini.h"


//
// A JIT is invoked through virtual functions declared in this class.
//
class JIT {

public:

    JIT_Flags  jit_flags; // KEN fix later: if jit keeps jit_flags, there is no need to pass flags

    //
    // Gracefully terminate the JIT.
    //
    virtual ~JIT() {};

    /**
     * Description of JIT command line options.
     */
    virtual const char*
    command_line_argument_description() const {
        return "This JIT takes no command line options";
    };

    /**
     * Parse JIT command line arguments.
     */
    virtual void 
    next_command_line_argument(const char *, const char *) {};


    //
    // The VM call into JIT to compile a method to generate method info
    //
    virtual JIT_Result 
    gen_method_info(Compile_Handle      compilation,              // in
                    Method_Handle       method,                   // in
                    JIT_Flags           flags                     // in
                    ) = 0;
    //
    // The VM call into JIT to compile a method.
    //
    // Arguments:
    // - compilation -- A compilation handle must be passed as an argument for
    //                  some VM functions called by the JIT.
    // - method      -- A handle for the method which corresponds to the frame
    //                  that we want to unwind.
    // - flags       -- Currently unused.
    // 
    virtual JIT_Result 
    compile_method(Compile_Handle     compilation,   // in
                   Method_Handle      method,        // in
                   JIT_Flags          flags          // in
                   ) = 0;

    virtual JIT_Result 
    compile_method_with_params(Compile_Handle            compilation,   // in
                               Method_Handle             method,        // in
                               OpenMethodExecutionParams flags          // in
                               ) = 0;

    virtual void
    method_was_overridden(Method_Handle    UNREF   caller,
                          Method_Handle    UNREF   callee
                          ) {};



    //
    // Unwind a stack frame for a method give the context.
    //
    // Arguments:
    // - method   -- A handle for the method which corresponds to the frame
    //               that we want to unwind.
    // - context  -- The register context.
    // 
    virtual void
    unwind_stack_frame(Method_Handle       method,                // in
                       JitFrameContext    *context                // in out
                       ) = 0;



    //
    // Call from the VM into the JIT.
    // The JIT:
    // 1. Enumerates all references for the current stack frame.
    //    The enumeration is precise, i.e., non-references are not
    //    enumerated.
    //    Enumeration can be done with either vm_enumerate_root_reference
    //    or vm_enumerate_compressed_root_reference.
    //
    // Arguments:
    // - method      -- A handle for the method which corresponds to the frame
    //                  of the exception handler.
    // - enum_handle -- This value should used as an argument to
    //                  vm_enumerate_root_reference
    // - context     -- The register context.
    //                  Note that the eip corresponds to the location which throws
    //                  the exception not to the beggining address of the handler.
    //
    virtual void 
    get_root_set_from_stack_frame(Method_Handle           method,         // in
                                  GC_Enumeration_Handle   enum_handle,    // in
                                  const JitFrameContext   *context        // in
                                  ) = 0;

    virtual void 
    get_root_set_for_thread_dump(Method_Handle           method,         // in
                                  GC_Enumeration_Handle   enum_handle,    // in
                                  const JitFrameContext   *context        // in
                                  ) = 0;
    
    virtual uint32
    get_inline_depth(InlineInfoPtr  ptr,
                     uint32         offset) { return 0; }


    virtual Method_Handle
    get_inlined_method(InlineInfoPtr  ptr,
                       uint32         offset,
                       uint32         inline_depth) { return NULL; }

    virtual Boolean
    can_enumerate(Method_Handle method,
                  NativeCodePtr eip
                  ) = 0;



    //
    // Called by the VM before control is transferred to an exception handler.
    // The JIT can restore the registers to the values expected by the handler.
    //
    // Arguments:
    // - method   -- A handle for the method which corresponds to the frame
    //               of the exception handler.
    // - context  -- The register context.
    //               Note that the eip corresponds to the location which throws
    //               the exception not to the beggining address of the handler.
    //
    virtual void
    fix_handler_context(Method_Handle      method,              // in
                        JitFrameContext   *context              // in out
                        ) = 0;



    //
    // Returns the address of the this pointer of the method.
    // This function is called when an exception is propagated out of
    // a stack frame of a synchronized method.  The VM uses this address
    // to call monitor_exit for the object associated with the method.
    //
    // Arguments:
    // - method   -- A handle for the method which corresponds to the current frame.
    // - context  -- The register context.
    //
    virtual void *
    get_address_of_this(Method_Handle            method,              // in
                        const JitFrameContext   *context              // in
                        ) = 0;




    //
    // This function is called for a call site (ie with and eip pointing to
    // the instruction following a call).
    // Iff the function at this call site (be it a Java method or a runtime
    // support function) returns a reference, the result is TRUE.
    // This function is used during GC to decide if eax contains a reference
    // and if it should be enumerated as part of the root set.
    //
    virtual Boolean
    call_returns_a_reference(Method_Handle            method,              // in
                             const JitFrameContext   *context              // in
                             ) = 0;

    //
    // thread for triggering recompilation
    //
    virtual void 
    thread_recompile_methods() = 0;

    // Patch the direct call address to new target.
    virtual void 
    rewrite_direct_call(int UNREF rewrite_type,
                        Method_Handle UNREF mh,
                        void * UNREF addr1,
                        void * UNREF addr2){ };


    virtual Boolean
    extended_class_callback(Class_Handle   UNREF extended_class,
                            Class_Handle   UNREF new_class,
                            void         * UNREF callback_data) { return FALSE; };

    virtual Boolean
    overridden_method_callback(Method_Handle  UNREF overridden_method,
                                                           Method_Handle  UNREF new_method,
                               void          * UNREF callback_data) { return FALSE; };

    virtual Boolean
    recompiled_method_callback(Method_Handle   UNREF recompiled_method,
                               void          * UNREF callback_data) { return FALSE; };

    // Returns TRUE if the JIT will compress references within objects and vector elements by representing 
    // them as offsets rather than raw pointers. The JIT should call the VM function vm_references_are_compressed()
    // during initialization in order to decide whether it should compress references.
    virtual Boolean 
    supports_compressed_references() = 0;

    virtual Boolean
    code_block_relocated(Method_Handle method, int id, NativeCodePtr old_address, NativeCodePtr new_address) = 0;

    /**
     * Execute java method from VM.
     * Moved from init_{ARCH}.cpp (was: vm_execute_java_method_array() )
     */
    virtual void
    execute_method(jmethodID method, jvalue *return_value, jvalue *args) = 0;

    virtual OpenExeJpdaError get_bc_location_for_native(Method_Handle method,
        NativeCodePtr native_pc, uint16 *bc_pc) = 0;

    virtual OpenExeJpdaError get_native_location_for_bc(Method_Handle method,
        uint16 bc_pc, NativeCodePtr *native_pc) = 0;

    virtual OpenExeJpdaError get_local_var(Method_Handle  method,
        const JitFrameContext *context, uint16 var_num, VM_Data_Type var_type,
        void *value_ptr) = 0;

    virtual OpenExeJpdaError set_local_var(Method_Handle  method,
        const JitFrameContext *context, uint16 var_num, VM_Data_Type var_type,
        void *value_ptr) = 0;
}; //JIT

#endif
