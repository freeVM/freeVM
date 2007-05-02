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
 * @version $Revision: 1.5.2.1 $
 */

/**
 * @file
 * SocketTransport_pd.h
 *
 * The given header file includes platform depended declarations and definitions 
 * for types, constants, include statements and functions for the Linux platform.
 */

#ifndef _SOCKETTRANSPORT_PD_H
#define _SOCKETTRANSPORT_PD_H


#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <netdb.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <arpa/inet.h>
#include <netinet/tcp.h> 
#include <pthread.h>

typedef pthread_mutex_t CriticalSection;
typedef int SOCKET;
typedef pthread_t ThreadId_t;

#include "jdwpTransport.h"
#include "LastTransportError.h"
#include "SocketTransport.h"

typedef timeval TIMEVAL;
typedef int BOOL;

const int TRUE = 1;
const int SOCKET_ERROR = -1;
const int SOCKET_ERROR_EINTR = EINTR;
const int INVALID_SOCKET = -1;
const int SD_BOTH = 2;
const int SOCKETWOULDBLOCK = EINPROGRESS;

/**
 * Returns the error status for the last failed operation. 
 */
static inline int
GetLastErrorStatus()
{
    return errno;
}

/**
 * Retrieves the number of milliseconds, substitute for the corresponding Win32 
 * function.
 */
static inline long 
GetTickCount(void)
{
    struct timeval t;
    gettimeofday(&t, 0);
    return t.tv_sec * 1000 + (t.tv_usec/1000);
}

/**
 * Closes socket, substitute for the corresponding Win32 function.
 */
static inline int 
closesocket(SOCKET s)
{
    return close(s);
}

/**
 * Closes socket, substitute for the corresponding Win32 function.
 */
static inline int 
ioctlsocket( SOCKET s, long cmd, u_long* argp)
{
    return ioctl(s, cmd, argp);
}

/**
 * Initializes critical-section lock objects.
 */
static inline void
InitializeCriticalSections(jdwpTransportEnv* env)
{
    pthread_mutex_init(&(((internalEnv*)env->functions->reserved1)->readLock), 0);
    pthread_mutex_init(&(((internalEnv*)env->functions->reserved1)->sendLock), 0);
}
 
/**
 * Releases all resources used by critical-section lock objects.
 */
static inline void
DeleteCriticalSections(jdwpTransportEnv* env)
{
    pthread_mutex_destroy(&(((internalEnv*)env->functions->reserved1)->readLock));
    pthread_mutex_destroy(&(((internalEnv*)env->functions->reserved1)->sendLock));
}

/**
 * Waits for ownership of the read critical-section object.
 */
static inline void
EnterCriticalReadSection(jdwpTransportEnv* env)
{
    pthread_mutex_lock(&(((internalEnv*)env->functions->reserved1)->readLock));
}

/**
 * Waits for ownership of the send critical-section object.
 */
static inline void
EnterCriticalSendSection(jdwpTransportEnv* env)
{
    pthread_mutex_lock(&(((internalEnv*)env->functions->reserved1)->sendLock));
}

/**
 * Releases ownership of the read critical-section object.
 */
static inline void
LeaveCriticalReadSection(jdwpTransportEnv* env)
{
    pthread_mutex_unlock(&(((internalEnv*)env->functions->reserved1)->readLock));
}

/**
 * Releases ownership of the send critical-section object.
 */
static inline void
LeaveCriticalSendSection(jdwpTransportEnv* env)
{
    pthread_mutex_unlock(&(((internalEnv*)env->functions->reserved1)->sendLock));
}

static inline ThreadId_t 
GetCurrentThreadId()
{
    return pthread_self();
} // GetCurrentThreadId()

static inline bool 
ThreadId_equal(ThreadId_t treadId1, ThreadId_t treadId2)
{
    return pthread_equal(treadId1, treadId2) != 0 ? true : false;
} // ThreadId_equal()

#endif //_SOCKETTRANSPORT_PD_H
