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
 * @version $Revision: 1.10 $
 */
// TransportManager_pd.cpp
//

/**
 * This header file includes platform depended definitions for types, 
 * constants, include statements and functions for Win32 platform.
 */

#include "TransportManager_pd.h"
#include "TransportManager.h"
#include <process.h>

using namespace jdwp;

#ifdef _WIN64
    // for 64-bit Windows platform
    const char* TransportManager::onLoadDecFuncName = "jdwpTransport_OnLoad";
    const char* TransportManager::unLoadDecFuncName = "jdwpTransport_UnLoad";
#else
    // for 32-bit Windows platform
    const char* TransportManager::onLoadDecFuncName = "_jdwpTransport_OnLoad@16";
    const char* TransportManager::unLoadDecFuncName = "_jdwpTransport_UnLoad@4";
#endif // _WIN64

const char TransportManager::pathSeparator = ';';

void TransportManager::StartDebugger(const char* command) throw(AgentException)
{
    throw NotImplementedException();
}

LoadedLibraryHandler TransportManager::LoadTransport(const char* dirName, const char* transportName)
{
    JDWP_ASSERT(transportName != 0);
    char* transportFullName = 0;
    if (dirName == 0) {
        size_t length = strlen(transportName) + 5;
        transportFullName = static_cast<char *>(GetMemoryManager().Allocate(length JDWP_FILE_LINE));
        sprintf(transportFullName, "%s.dll", transportName);
    } else {
        size_t length = strlen(dirName) + strlen(transportName) + 6;
        transportFullName = static_cast<char *>(GetMemoryManager().Allocate(length JDWP_FILE_LINE));
        sprintf(transportFullName, "%s\\%s.dll", dirName, transportName);
    }
    AgentAutoFree afv(transportFullName JDWP_FILE_LINE);
    LoadedLibraryHandler res = LoadLibrary(transportFullName);
    if (res == 0) {
        JDWP_TRACE_PROG("loading of " << transportFullName << " failed (error code: " << GetLastError() << ")");
    } else {
        JDWP_TRACE_PROG("transport " << transportFullName << " loaded");
    }
    return res;
}
