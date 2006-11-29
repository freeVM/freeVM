/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @version $Revision: 1.14 $
 */

// TransportManager_pd.cpp
//

/**
 * This header file includes platform depended definitions for types, 
 * constants, include statements and functions for Linux platform.
 */

#include "TransportManager_pd.h"
#include "TransportManager.h"

using namespace jdwp;

const char* TransportManager::onLoadDecFuncName = "jdwpTransport_OnLoad";
const char* TransportManager::unLoadDecFuncName = "jdwpTransport_UnLoad";
const char TransportManager::pathSeparator = ':';

void TransportManager::StartDebugger(const char* command) throw(AgentException)
{
    throw NotImplementedException();
}

ProcPtr jdwp::GetProcAddress(LoadedLibraryHandler libHandler, const char* procName)
{
    dlerror();
    ProcPtr res = (ProcPtr)dlsym(libHandler, procName);
    char* errorMessage = 0;
    if (errorMessage = dlerror()) {
        JDWP_TRACE_PROG("free library failed (error: " << errorMessage << ")");
    }
    return res;
}

bool jdwp::FreeLibrary(LoadedLibraryHandler libHandler)
{
    dlerror();
    if (dlclose(libHandler) != 0) {
        JDWP_TRACE_PROG("free library failed (error: " << dlerror() << ")");
        return false;
    }
    return true;
}

LoadedLibraryHandler TransportManager::LoadTransport(const char* dirName, const char* transportName)
{
    JDWP_ASSERT(transportName != 0);
    dlerror();
    char* transportFullName = 0;
    if (dirName == 0) {
        size_t length = strlen(transportName) + 7;
        transportFullName = static_cast<char *>(GetMemoryManager().Allocate(length JDWP_FILE_LINE));
        sprintf(transportFullName, "lib%s.so", transportName);
    } else {
        size_t length = strlen(dirName) + strlen(transportName) + 8;
        transportFullName = static_cast<char *>(GetMemoryManager().Allocate(length JDWP_FILE_LINE));
        sprintf(transportFullName, "%s/lib%s.so", dirName, transportName);
    }
    AgentAutoFree afv(transportFullName JDWP_FILE_LINE);
    LoadedLibraryHandler res = dlopen(transportFullName, RTLD_LAZY);
    if (res == 0) {
        JDWP_TRACE_PROG("loading of " << transportFullName << " failed (error: " << dlerror() << ")");
    } else {
        JDWP_TRACE_PROG("transport " << transportFullName << " loaded");
    }
    return res;
}
