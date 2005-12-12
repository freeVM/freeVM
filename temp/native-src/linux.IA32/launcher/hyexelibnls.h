/* Copyright 1991, 2004 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#if !defined(hyexelibnls_h)
#define hyexelibnls_h

#include "hyport.h"
/* 0x4558454c = EXEL */

#define HYNLS_EXELIB_VERSION__MODULE 0x4558454c
#define HYNLS_EXELIB_VERSION__ID 50
#define HYNLS_EXELIB_VERSION HYNLS_EXELIB_VERSION__MODULE, HYNLS_EXELIB_VERSION__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY__ID 51
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_TO_FIND_JLS__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_TO_FIND_JLS__ID 52
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_TO_FIND_JLS HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_TO_FIND_JLS__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_TO_FIND_JLS__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BA__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BA__ID 53
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BA HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BA__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BA__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_CLASSNAME__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_CLASSNAME__ID 54
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_CLASSNAME HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_CLASSNAME__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_CLASSNAME__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY_CONVERTING__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY_CONVERTING__ID 55
#define HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY_CONVERTING HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY_CONVERTING__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_OUT_OF_MEMORY_CONVERTING__ID
#define HYNLS_EXELIB_CLASS_DOES_NOT_IMPL_MAIN__MODULE 0x4558454c
#define HYNLS_EXELIB_CLASS_DOES_NOT_IMPL_MAIN__ID 56
#define HYNLS_EXELIB_CLASS_DOES_NOT_IMPL_MAIN HYNLS_EXELIB_CLASS_DOES_NOT_IMPL_MAIN__MODULE, HYNLS_EXELIB_CLASS_DOES_NOT_IMPL_MAIN__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_ARG_ARRAY__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_ARG_ARRAY__ID 57
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_ARG_ARRAY HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_ARG_ARRAY__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_ARG_ARRAY__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BYTE_ARRAY__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BYTE_ARRAY__ID 58
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BYTE_ARRAY HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BYTE_ARRAY__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_BYTE_ARRAY__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_ARG__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_ARG__ID 59
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_ARG HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_ARG__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JLS_FOR_ARG__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_SET_ARRAY_ELEM__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_SET_ARRAY_ELEM__ID 60
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_SET_ARRAY_ELEM HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_SET_ARRAY_ELEM__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_SET_ARRAY_ELEM__ID
#define HYNLS_EXELIB_VM_STARTUP_ERR_OUT_OF_MEMORY__MODULE 0x4558454c
#define HYNLS_EXELIB_VM_STARTUP_ERR_OUT_OF_MEMORY__ID 61
#define HYNLS_EXELIB_VM_STARTUP_ERR_OUT_OF_MEMORY HYNLS_EXELIB_VM_STARTUP_ERR_OUT_OF_MEMORY__MODULE, HYNLS_EXELIB_VM_STARTUP_ERR_OUT_OF_MEMORY__ID
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JAVA_VM__MODULE 0x4558454c
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JAVA_VM__ID 62
#define HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JAVA_VM HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JAVA_VM__MODULE, HYNLS_EXELIB_INTERNAL_VM_ERR_FAILED_CREATE_JAVA_VM__ID

#endif // hyexelibnls_h
