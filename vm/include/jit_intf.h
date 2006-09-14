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




#ifndef _JIT_INTF_H_
#define _JIT_INTF_H_


#include "open/types.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef const void *Arg_List_Iterator;       // Java only
typedef const void *Arg_List_Iter;


#include "open/vm.h"
#include "jit_export.h"
#include "jit_import.h"

//
// The implementation of those types is private to the VM.
// The JIT uses them as handles.
//


typedef
enum Method_Side_Effects {
    MSE_Unknown,
    MSE_True,
    MSE_False,
    MSE_True_Null_Param
} Method_Side_Effects; //Method_Side_Effects


///////////////////////////////////////////////////////////////////////////////
// Access flags for Class, Class_Member, Field and Method.
///////////////////////////////////////////////////////////////////////////////
                                    //      used by
                                    //
#define ACC_PUBLIC          0x0001  // Class    Field   Method
#define ACC_PRIVATE         0x0002  //          Field   Method
#define ACC_PROTECTED       0x0004  //          Field   Method
#define ACC_STATIC          0x0008  //          Field   Method
#define ACC_FINAL           0x0010  // Class    Field   Method
#define ACC_SUPER           0x0020  // Class
#define ACC_SYNCHRONIZED    0x0020  //                  Method
#define ACC_BRIDGE          0x0040  //                  Method (this is Java 1.5 flag)
#define ACC_VOLATILE        0x0040  //          Field
#define ACC_VARARGS         0x0080  //                  Method (this is Java 1.5 flag)
#define ACC_TRANSIENT       0x0080  //          Field
#define ACC_NATIVE          0x0100  //                  Method
#define ACC_INTERFACE       0x0200  // Class
#define ACC_ABSTRACT        0x0400  // Class            Method
#define ACC_STRICT          0x0800  //                  Method
// The following are Java 1.5 flags
#define ACC_SYNTHETIC       0x1000  // Class    Field   Method
#define ACC_ANNOTATION      0x2000  // Class
#define ACC_ENUM            0x4000  // Class    Field


//////////////// begin C interface


//
// calls from Compiler to VM
//
//-----------------------------------------------------------------------------
// Constant pool resolution
//-----------------------------------------------------------------------------
//
// The following byte codes reference constant pool entries:
//
//  field
//      getstatic           static field
//      putstatic           static field
//      getfield            non-static field
//      putfield            non-static field
//
//  method
//      invokevirtual       virtual method
//      invokespecial       special method
//      invokestatic        static method
//      invokeinterface     interface method
//
//  class
//      new                 class
//      anewarray           class
//      checkcast           class
//      instanceof          class
//      multianewarray      class
//

//
// For the method invocation byte codes, certain linkage exceptions are thrown 
// at run-time:
//
//  (1) invocation of a native methods throws the UnsatisfiedLinkError if the 
//      code that implements the method cannot be loaded or linked.
//  (2) invocation of an interface method throws 
//          - IncompatibleClassChangeError if the object does not implement 
//            the called method, or the method is implemented as static
//          - IllegalAccessError if the implemented method is not public
//          - AbstractMethodError if the implemented method is abstract
//

VMEXPORT Field_Handle
resolve_field(Compile_Handle h, Class_Handle c, unsigned index);

//
// resolve constant pool reference to a virtual method
// used for invokevirtual and invoke special.
//
// DEPRECATED
//
VMEXPORT Method_Handle 
resolve_nonstatic_method(Compile_Handle h, Class_Handle c, unsigned index);

//
// resolve constant pool reference to a virtual method
// used for invokespecial
//
VMEXPORT Method_Handle 
resolve_special_method(Compile_Handle h, Class_Handle c, unsigned index);

//
// Checks that t inherits (either extends or implements) s
//
VMEXPORT Boolean
vm_instanceof_class(Class *s, Class *t);

//
// resolve constant pool reference to a class
// used for
//      (1) new 
//              - InstantiationError exception if resolved class is abstract
//      (2) anewarray
//      (3) multianewarray
//
// resolve_class_new is used for resolving references to class entries by the
// the new byte code.
//
VMEXPORT Class_Handle 
resolve_class_new(Compile_Handle h, Class_Handle c, unsigned index);

//
// resolve_class is used by all the other byte codes that reference classes,
// as well as exception handlers.
//
VMEXPORT Class_Handle 
resolve_class(Compile_Handle h, Class_Handle c, unsigned index);

//
// Field
//
VMEXPORT Boolean      field_is_public(Field_Handle f);
VMEXPORT unsigned     field_get_flags(Field_Handle f);
VMEXPORT Java_Type    field_get_type(Field_Handle f);
VMEXPORT Boolean      field_is_injected(Field_Handle f);



//
// Method
//
VMEXPORT Boolean      method_is_public(Method_Handle m);
// ? added. Needed in the callvirt opcode (among other places) to see if a "call" should
// be generated rather than a callvirt.


VMEXPORT unsigned     method_get_max_locals(Method_Handle m);
VMEXPORT unsigned     method_get_flags(Method_Handle m);

VMEXPORT Boolean      method_uses_fastcall(Method_Handle m);
VMEXPORT Boolean      method_is_fake(Method_Handle m);


#ifdef COMPACT_FIELD
VMEXPORT Boolean class_is_compact_field() ;
#endif

VMEXPORT unsigned     method_number_throws(Method_Handle m);
VMEXPORT Class_Handle method_get_throws(Method_Handle m, unsigned idx);


VMEXPORT Method_Side_Effects method_get_side_effects(Method_Handle m);
VMEXPORT void method_set_side_effects(Method_Handle m, Method_Side_Effects mse);


VMEXPORT Java_Type    method_get_return_type(Method_Handle m);
VMEXPORT Class_Handle method_get_return_type_class(Method_Handle m);

VMEXPORT Arg_List_Iterator  initialize_arg_list_iterator(const char *descr);
VMEXPORT Arg_List_Iterator  method_get_argument_list(Method_Handle m);
VMEXPORT Java_Type          curr_arg(Arg_List_Iterator iter);
VMEXPORT Class_Handle       get_curr_arg_class(Arg_List_Iterator iter,
                                                Method_Handle m);
VMEXPORT Arg_List_Iterator  advance_arg_iterator(Arg_List_Iterator iter);


VMEXPORT void method_set_inline_assumption(Compile_Handle h,
                                            Method_Handle caller,
                                            Method_Handle callee);


//
// Class
//        
VMEXPORT char*        class_get_java_name(Class_Handle ch);
VMEXPORT unsigned     class_get_flags(Class_Handle cl);
//VMEXPORT ClassLoaderHandle class_get_classloader(Class_Handle ch);
VMEXPORT unsigned     class_number_fields(Class_Handle ch);
VMEXPORT Field_Handle class_get_field(Class_Handle ch, unsigned idx);
VMEXPORT int          class_get_super_offset();

VMEXPORT int          class_get_depth(Class_Handle cl);
//#endif
//::
VMEXPORT Boolean      class_has_non_default_finalizer(Class_Handle cl);
VMEXPORT unsigned     class_number_implements(Class_Handle ch);
VMEXPORT Class_Handle class_get_implements(Class_Handle ch, unsigned idx);
VMEXPORT const char  *class_get_source_file_name(Class_Handle cl);

// Returns TRUE is the class is an interface.
// is replaced with class_property_is_interface2.
VMEXPORT Boolean class_property_is_interface(Class_Handle ch);

VMEXPORT Boolean        class_get_array_num_dimensions(Class_Handle cl);

VMEXPORT ClassLoaderHandle class_get_class_loader(Class_Handle c);

VMEXPORT void
class_throw_linking_error_for_jit(Class_Handle ch, unsigned cp_index, unsigned opcode);

void
class_throw_linking_error(Class_Handle ch, unsigned cp_index, unsigned opcode);

VMEXPORT Class_Handle
class_load_class_by_name(const char *name,
                         Class_Handle c);


VMEXPORT Class_Handle
class_load_class_by_descriptor(const char *descr,
                               Class_Handle c);

VMEXPORT Method_Handle
class_lookup_method_recursively(Class_Handle clss,
                                const char *name,
                                const char *descr);

// This function is for native library support
// It takes a class name with .s not /s.
VMEXPORT Class_Handle class_find_loaded(ClassLoaderHandle, const char*);

// This function is for native library support
// It takes a class name with .s not /s.
VMEXPORT Class_Handle class_find_class_from_loader(ClassLoaderHandle, const char*, Boolean init);

//
// The following do not cause constant pools to be resolve, if they are not
// resolved already
//
VMEXPORT Class_Handle resolve_class_from_constant_pool(Class_Handle c_handle,
                                                        unsigned index);
VMEXPORT const char  *const_pool_get_field_name(Class_Handle cl,
                                                 unsigned index);
VMEXPORT const char  *const_pool_get_field_class_name(Class_Handle cl,
                                                       unsigned index);
VMEXPORT const char  *const_pool_get_field_descriptor(Class_Handle cl,
                                                       unsigned index);
VMEXPORT const char  *const_pool_get_method_name(Class_Handle cl,
                                                  unsigned index);
VMEXPORT const char  *const_pool_get_method_class_name(Class_Handle cl,
                                                        unsigned index);
VMEXPORT const char  *const_pool_get_method_descriptor(Class_Handle cl,
                                                        unsigned index);
VMEXPORT const char  *const_pool_get_class_name(Class_Handle cl,
                                                 unsigned index);
VMEXPORT const char  *const_pool_get_interface_method_name(Class_Handle cl,
                                                            unsigned index);
VMEXPORT const char  *const_pool_get_interface_method_class_name(Class_Handle cl,
                                                                  unsigned index);
VMEXPORT const char  *const_pool_get_interface_method_descriptor(Class_Handle cl,
                                                                  unsigned index);

VMEXPORT unsigned thread_get_suspend_request_offset();

VMEXPORT unsigned thread_get_thread_state_flag_offset(); //temporary: for EscapeAnalysis prototype

VMEXPORT Compile_Handle jit_get_comp_handle(JIT_Handle j);

// Needed for DLL problems
VMEXPORT void core_free(void*);


// Deprecated.  Please use vector_first_element_offset instead.
//VMEXPORT int get_array_offset(Java_Type element_type);


//////////////// end C interface

#ifdef __cplusplus
}
#endif

#endif
