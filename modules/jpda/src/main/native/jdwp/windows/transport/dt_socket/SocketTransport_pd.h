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
 * @version $Revision: 1.7.2.1 $
 */

/**
 * @file
 * SocketTransport_pd.h
 *
 * The given header file includes platform depended declarations and definitions 
 * for types, constants, include statements and functions for the Win32 platform.
 */

#ifndef _SOCKETTRANSPORT_PD_H
#define _SOCKETTRANSPORT_PD_H

#include <Winsock2.h>
#include <Ws2tcpip.h>

typedef CRITICAL_SECTION CriticalSection;
typedef DWORD ThreadId_t;

#include "jdwpTransport.h"
#include "LastTransportError.h"
#include "SocketTransport.h"

typedef int socklen_t;

const int SOCKETWOULDBLOCK = WSAEWOULDBLOCK;
const int SOCKET_ERROR_EINTR = WSAEINTR;

/**
 * Returns the error status for the last failed operation. 
 */
static inline int
GetLastErrorStatus()
{
    return WSAGetLastError();
} //GetLastErrorStatus()

/**
 * Initializes critical section lock objects.
 */
static inline void
InitializeCriticalSections(jdwpTransportEnv* env)
{
    InitializeCriticalSection(&(((internalEnv*)env->functions->reserved1)->readLock));
    InitializeCriticalSection(&(((internalEnv*)env->functions->reserved1)->sendLock));
} //InitializeCriticalSections()

/**
 * Releases all resources used by critical-section lock objects.
 */
static inline void
DeleteCriticalSections(jdwpTransportEnv* env)
{
    DeleteCriticalSection(&(((internalEnv*)env->functions->reserved1)->readLock));
    DeleteCriticalSection(&(((internalEnv*)env->functions->reserved1)->sendLock));
} //DeleteCriticalSections()

/**
 * Waits for ownership of the read critical-section object.
 */
static inline void
EnterCriticalReadSection(jdwpTransportEnv* env)
{
    EnterCriticalSection(&(((internalEnv*)env->functions->reserved1)->readLock));
} //EnterCriticalReadSection()

/**
 * Waits for ownership of the send critical-section object.
 */
static inline void
EnterCriticalSendSection(jdwpTransportEnv* env)
{
    EnterCriticalSection(&(((internalEnv*)env->functions->reserved1)->sendLock));
} //EnterCriticalSendSection()

/**
 * Releases ownership of the read critical-section object.
 */
static inline void
LeaveCriticalReadSection(jdwpTransportEnv* env)
{
    LeaveCriticalSection(&(((internalEnv*)env->functions->reserved1)->readLock));
} //LeaveCriticalReadSection()

/**
 * Releases ownership of the send critical-section object.
 */
static inline void
LeaveCriticalSendSection(jdwpTransportEnv* env)
{
    LeaveCriticalSection(&(((internalEnv*)env->functions->reserved1)->sendLock));
} //LeaveCriticalSendSection()

static inline bool ThreadId_equal(ThreadId_t treadId1, ThreadId_t treadId2)
{
    return (treadId1 == treadId2);
} // ThreadId_equal()

#endif // _SOCKETTRANSPORT_PD_H
