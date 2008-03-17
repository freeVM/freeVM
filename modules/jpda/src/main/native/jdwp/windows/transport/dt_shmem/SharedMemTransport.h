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

/*
 * Includes and defines for the Shared Memory Transport module
 */

#ifndef _SHAREDMEMTRANSPORT_H
#define _SHAREDMEMTRANSPORT_H

#include <windows.h>

typedef CRITICAL_SECTION CriticalSection;
typedef DWORD ThreadId_t;

#include "jni.h"
#include "jdwpTransport.h"
#include "LastTransportError.h"

#define DEFAULT_ADDRESS_NAME "sharedmem"
#define DEFAULT_MUTEX_SUFFIX "mutex"
#define DEFAULT_ACCEPT_EVENT_SUFFIX "acceptEvent"
#define DEFAULT_ATTACH_EVENT_SUFFIX "attachEvent"
#define MAX_PACKET_SIZE 267
#define SHARED_MEM_SIZE 4096
#define JDWP_HANDSHAKE "JDWP-Handshake"

static jdwpTransportError JNICALL ShMemTran_GetCapabilities(jdwpTransportEnv* env, JDWPTransportCapabilities* capabilitiesPtr);
static jdwpTransportError JNICALL ShMemTran_Attach(jdwpTransportEnv* env, const char* address, jlong attachTimeout, jlong handshakeTimeout);
static jdwpTransportError JNICALL ShMemTran_StartListening(jdwpTransportEnv* env, const char* address, char** actualAddress);
static jdwpTransportError JNICALL ShMemTran_StopListening(jdwpTransportEnv* env);
static jdwpTransportError JNICALL ShMemTran_Accept(jdwpTransportEnv* env, jlong acceptTimeout, jlong handshakeTimeout);
static jboolean JNICALL ShMemTran_IsOpen(jdwpTransportEnv* env);
static jdwpTransportError JNICALL ShMemTran_Close(jdwpTransportEnv* env);
static jdwpTransportError JNICALL ShMemTran_ReadPacket(jdwpTransportEnv* env, jdwpPacket* packet);
static jdwpTransportError JNICALL ShMemTran_WritePacket(jdwpTransportEnv* env, const jdwpPacket* packet);
static jdwpTransportError JNICALL ShMemTran_GetLastError(jdwpTransportEnv* env, char** message);
extern "C" JNIEXPORT jint JNICALL jdwpTransport_OnLoad(JavaVM *vm, jdwpTransportCallback* callback, jint version, jdwpTransportEnv** env);
extern "C" JNIEXPORT void JNICALL jdwpTransport_UnLoad(jdwpTransportEnv** env);

typedef struct sharedMemListener_struct {
    char mutexName[75];
    char acceptEventName[75];
    char attachEventName[75];
    bool isListening;
    bool isAccepted;
    int acceptPid;
    int attachPid;
} sharedMemListener;

typedef struct sharedMemTransport_struct {
    char name[75];
    HANDLE mutexHandle;
    HANDLE acceptEventHandle;
    HANDLE attachEventHandle;
    HANDLE sharedMemoryHandle;
    sharedMemListener* listener;
} sharedMemTransport;

struct internalEnv {
    JavaVM *jvm;                    // the JNI invocation interface, provided 
                                    // by the agent 
    void* (*alloc)(jint numBytes);  // function for allocating an area of memory, 
                                    // provided by the agent 
    void (*free)(void *buffer);     // function for deallocating an area of memory, 
                                    // provided by the agent
    sharedMemTransport *transport;  // Shared memory transport structure
    LastTransportError *lastError;  // pointer to the last transport error
};

#endif /* _SHAREDMEMTRANSPORT_H */
