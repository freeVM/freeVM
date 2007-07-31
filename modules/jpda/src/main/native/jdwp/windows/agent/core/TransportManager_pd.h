/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Revision: 1.5.2.1 $
 */

/**
 * @file
 * TransportManager_pd.h
 *
 * The given header file includes platform depended declarations for types, 
 * constants, include statements and functions for the Win32 platform.
 */

#ifndef _TRANSPORT_MANAGER_PD_H_
#define _TRANSPORT_MANAGER_PD_H_

#define WIN32_LEAN_AND_MEAN  // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>

namespace jdwp {

    typedef HMODULE LoadedLibraryHandler;

}//jdwp

#endif // _TRANSPORT_MANAGER_PD_H_
