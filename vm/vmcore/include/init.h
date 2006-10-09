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
 * @author Euguene Ostrovsky
 * @version $Revision: 1.1.2.1.4.6 $
 */  


#ifndef _INIT_H
#define _INIT_H

#include "environment.h"

jint vm_attach_internal(JNIEnv ** p_jni_env, jthread * java_thread,
                        JavaVM * java_vm, jobject group,
                        char * name, jboolean daemon);
jint vm_init1(JavaVM_Internal * java_vm, JavaVMInitArgs * vm_arguments);
jint vm_init2(JNIEnv * jni_env);
jint vm_destroy(JavaVM_Internal * java_vm, jthread java_thread);
void vm_exit(int exit_code);

void initialize_vm_cmd_state(Global_Env *p_env, JavaVMInitArgs* arguments);
void set_log_levels_from_cmd(JavaVMInitArgs* vm_arguments);
void parse_vm_arguments(Global_Env *p_env);
void parse_jit_arguments(JavaVMInitArgs* vm_arguments);
void print_generic_help();

#endif //_INIT_H
