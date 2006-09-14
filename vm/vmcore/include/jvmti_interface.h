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
 *  @author Gregory Shimansky
 *  @version $Revision: 1.1.2.1.4.4 $
 */

#ifndef _JVMTI_INTF_H
#define _JVMTI_INTF_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Method enter callback which is called from all JIT compiled methods,
 * This callback is called before synchronization is done for synchronized
 * methods. Garbage collector should be enabled.
 * @param method - handle of the called method
 */
void jvmti_method_enter_callback(Method_Handle method);

/**
 * Method exit callback which is called from all JIT compiled methods.
 * This callback is called after synchronization is done for synchronized
 * methods. Garbage collector should be enabled.
 * @param method - handle of the exiting method
 * @param return_value - the return value of the method if method is not void
 */
void jvmti_method_exit_callback(Method_Handle method, jvalue* return_value);

/**
 * Field access callback which is called from JITted code compiled with <field access> flag whenever
 * access of field which has <field access mask> set occures.
 * Garbage collector must be enabled.
 * @param field - handle of the field under access
 * @param method - handle of the method, which accesses field
 * @param location - location of code which accesses field
 * @param object - pointer to the reference of the object, which field is beeng
 *      accessed or NULL for static field
 */
void jvmti_field_access_callback(Field_Handle field,
                                       Method_Handle method,
                                       jlocation location,
                                       jobject* object);

/**
 * Field modification callback which is called from JITted code compiled with <field modification> flag whenever
 * modification of field which has <field modification mask> set occures.
 * Garbage collector must be enabled.
 * @param field - handle of the field under modification
 * @param method - handle of the method, which modifies field
 * @param location - location of code which modifies field
 * @param object - pointer to the reference of the object, which field is beeng
 *      modified or NULL for static field
 * @param new_value - pointer to the new value for the field
 */
void jvmti_field_modification_callback(Field_Handle field,
                                       Method_Handle method,
                                       jlocation location,
                                       jobject* object,
                                       jvalue* new_value);


#ifdef __cplusplus
}
#endif

#endif // _JVMTI_INTF_H

