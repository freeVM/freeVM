/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, 
 * as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Viacheslav G. Rybalov
 * @version $Revision: 1.6.2.1 $
 */

/**
 * @file
 * TransportManager_pd.h
 *
 * The given header file includes platform depended declarations for types
 * and constants, and statements and functions for the Linux platform.  
 */

#ifndef _TRANSPORT_MANAGER_PD_H_
#define _TRANSPORT_MANAGER_PD_H_

#include <dlfcn.h>
#include <unistd.h>

namespace jdwp {

    typedef void* LoadedLibraryHandler;

    typedef void (*ProcPtr)();

    ProcPtr GetProcAddress(LoadedLibraryHandler libHandler, const char* procName);

    bool FreeLibrary(LoadedLibraryHandler libHandler);

}//jdwp

#endif // _TRANSPORT_MANAGER_PD_H_
