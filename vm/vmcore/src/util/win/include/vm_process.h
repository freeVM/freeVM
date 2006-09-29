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
 * @author Intel, Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.3 $
 */  
#ifndef _VM_PROCESS_H_
#define _VM_PROCESS_H_

//
// This file holds the routines needed to implement process related functionality across VM/GC/JIT
// projects. The calls are inlined so that the JIT and GC can use this .h file without having to 
// deal with the overhead of doing .dll calls.
//
// While these can be implemented by direct calls to the related windows API, the Linux and other 
// versions are not as robust as the windows API. This will be dealt with on a case by case basis
// either by documenting this file or by improving the robustness of the systems.
//

#include "platform_lowlevel.h"
#include <process.h>
#include <windows.h>
#include <assert.h>


/*inline void vm_terminate_thread(VmThreadHandle thrdaddr) {
    TerminateThread(thrdaddr,0);    
}*/
#endif
