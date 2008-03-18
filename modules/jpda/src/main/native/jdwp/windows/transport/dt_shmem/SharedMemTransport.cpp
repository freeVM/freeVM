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
 * This is the implementation of JDWP Shared Memory transport. 
 */

#include "SharedMemTransport.h"

#include <string.h>

/**
 * This function sets internalEnv message and status code of 
 * last transport error 
 */
static void 
SetLastTranError(jdwpTransportEnv* env, const char* messagePtr, int errorStatus)
{
    internalEnv* ienv = (internalEnv*)env->functions->reserved1;
    if (ienv->lastError != 0) {
        ienv->lastError->insertError(messagePtr, errorStatus);
    } else {
        ienv->lastError = new(ienv->alloc, ienv->free) LastTransportError(messagePtr, errorStatus, ienv->alloc, ienv->free);
    }
    return;
} // SetLastTranError

/* 
 * This is a utility function to check the validity of the given shared memory addresses.
 * Addresses must be non-null and must not contain the '\' (backslash) character.
 */
static jdwpTransportError
CheckAddress(jdwpTransportEnv* env, const char* address) {
    if (NULL == address) {
        SetLastTranError(env, "Address is NULL", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    if (strchr(address, '\\') != NULL) {
        SetLastTranError(env, "Specified address contains a backslash", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    return JDWPTRANSPORT_ERROR_NONE;
} // CheckAddress

/* 
 * Utility function that creates a new shared memory space with baseAddress as the root of it's name
 * Parameters:
 *   env - the jdwp transport environment [in]
 *   resultHandle - the handle of the shared memory space created by this function [out]
 *   resultAddress - the name of the shared memory space created by this function [out]
 *   baseAddress - the root name used to create resultAddress [in]
 * Return: JDWPTRANSPORT_ERROR_NONE if successful, any other value indicates an error.
 */
static jdwpTransportError 
CreateAddressFromBase(jdwpTransportEnv* env, HANDLE *resultHandle, char resultAddress[], const char* baseAddress) {
    int addressSuffixInt = 1;
    HANDLE handle = NULL;

    while (true) {
        /* Create an incrementing number to add to the end of baseAddress to create the shared memory address */
        char addressSuffixString[5];
        if (sprintf(addressSuffixString, "%d", addressSuffixInt) <0) {
            /* sprintf has encountered an error - set the error code and return */
            SetLastTranError(env, "sprintf error while generating shared memory address name", 0);
            return JDWPTRANSPORT_ERROR_INTERNAL;
        }

        if (sprintf(resultAddress, "%s%s", baseAddress, addressSuffixString) <0) {
            /* hystr_printf has encountered an error - set the error code and return */
            SetLastTranError(env, "hystr_printf error while generating shared memory address name", 0);
            return JDWPTRANSPORT_ERROR_INTERNAL;
        }

        /* Attempt to create the shared memory space */
        handle = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, SHARED_MEM_SIZE, resultAddress);  
        if (NULL == handle) 
        { 
            SetLastTranError(env, "Could not create shared memory space", GetLastError());
            return JDWPTRANSPORT_ERROR_INTERNAL;
        }

        if (ERROR_ALREADY_EXISTS != GetLastError()) {
            /* We have created a new handle successfully so break the loop */
            break;
        }

        /* The shared memory already exists, so close our handle and try again with a new name */
        CloseHandle(handle);
        addressSuffixInt++;
    }

    *resultHandle = handle;

    return JDWPTRANSPORT_ERROR_NONE;
} // CreateAddressFromBase

/**
 * This function implements jdwpTransportEnv::GetCapabilities
 */
static jdwpTransportError JNICALL
ShMemTran_GetCapabilities(jdwpTransportEnv* env, JDWPTransportCapabilities* capabilitiesPtr) 
{
    memset(capabilitiesPtr, 0, sizeof(JDWPTransportCapabilities));
    capabilitiesPtr->can_timeout_attach = 1;
    capabilitiesPtr->can_timeout_accept = 1;
    capabilitiesPtr->can_timeout_handshake = 1;

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_GetCapabilities

/**
 * This function implements jdwpTransportEnv::StartListening
 */
static jdwpTransportError JNICALL 
ShMemTran_StartListening(jdwpTransportEnv* env, const char* address, char** actualAddress)
{
    HANDLE writeHandle;
    char writeAddress[75];
    jdwpTransportError res;
    if ((address != NULL) && (strcmp(address, "\0") != 0)) {
        /* If an address has been specified, check it is valid */
        res = CheckAddress(env, address);
        if (res != JDWPTRANSPORT_ERROR_NONE) {
            return res;
        }

        strcpy(writeAddress, address);

        /* Attempt to create the shared memory space */
        writeHandle = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, SHARED_MEM_SIZE, writeAddress);  
        if (NULL == writeHandle) 
        { 
            SetLastTranError(env, "Could not create shared memory space", GetLastError());
            return JDWPTRANSPORT_ERROR_INTERNAL;
        }

        /* If the memory space already exists, return with an error */
        if (ERROR_ALREADY_EXISTS == GetLastError()) {
            CloseHandle(writeHandle);
            SetLastTranError(env, "Specified shared memory address already in use", GetLastError());
            return JDWPTRANSPORT_ERROR_INTERNAL;
        }
    } else {
        /* No address was specified at the command line, so generate one */
        char *defaultAddress = DEFAULT_ADDRESS_NAME;
        res = CreateAddressFromBase(env, &writeHandle, writeAddress, defaultAddress);
        if (JDWPTRANSPORT_ERROR_NONE != res) {
            /* Error message will already have been set in CreateAddressFromBase() */
            return res;
        }
    }

    /* Create the shared memory transport */
    sharedMemTransport *transport = (sharedMemTransport*)(((internalEnv*)env->functions->reserved1)->alloc)(sizeof(sharedMemTransport));

    /* We have successfully got our shared memory address and handle - record them for future use */
    *actualAddress = (char*)(((internalEnv*)env->functions->reserved1)->alloc)(strlen(writeAddress));
    strcpy(*actualAddress, writeAddress);
    strcpy(transport->name, writeAddress);
    transport->sharedMemoryHandle = writeHandle;

    /* Open the shared memory region */
    void *writeRegion = MapViewOfFile(writeHandle, FILE_MAP_ALL_ACCESS, 0, 0, 0);                   
    if (NULL == writeRegion) 
    { 
        SetLastTranError(env, "MapViewOfFile failed on server shared memory", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Create mutex for this shared memory */
    /* TODO: Make sure all the handles created and stored here are closed later! */
    char *defaultMutexSuffix = DEFAULT_MUTEX_SUFFIX;
    char *writeMutexName = (char*)(((internalEnv*)env->functions->reserved1)->alloc)(strlen(writeAddress) + strlen(defaultMutexSuffix) + 1);
    sprintf(writeMutexName, "%s%s", writeAddress, defaultMutexSuffix);

    /* Create accept event */
    char *defaultAcceptEventSuffix = DEFAULT_ACCEPT_EVENT_SUFFIX;
    char *acceptEventName = (char*)(((internalEnv*)env->functions->reserved1)->alloc)(strlen(writeAddress) + strlen(defaultAcceptEventSuffix) + 1);
    sprintf(acceptEventName, "%s%s", writeAddress, defaultAcceptEventSuffix);

    HANDLE acceptEventHandle = CreateEvent(NULL, FALSE, FALSE, acceptEventName);
    if (NULL == acceptEventHandle) 
    { 
        SetLastTranError(env, "Failed to create accept event", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    transport->acceptEventHandle = acceptEventHandle;

    /* Create attach event */
    char *defaultAttachEventSuffix = DEFAULT_ATTACH_EVENT_SUFFIX;
    char *attachEventName = (char*)(((internalEnv*)env->functions->reserved1)->alloc)(strlen(writeAddress) + strlen(defaultAttachEventSuffix) + 1);
    sprintf(attachEventName, "%s%s", writeAddress, defaultAttachEventSuffix);

    HANDLE attachEventHandle = CreateEvent(NULL, FALSE, FALSE, attachEventName);
    if (NULL == attachEventHandle) 
    { 
        SetLastTranError(env, "Failed to create attach event", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    transport->attachEventHandle = attachEventHandle;

    /* Allocate the transport listener and fill in it's fields */
    sharedMemListener *listener = (sharedMemListener*)(((internalEnv*)env->functions->reserved1)->alloc)(sizeof(sharedMemListener));
    strcpy(listener->mutexName, writeMutexName);
    strcpy(listener->acceptEventName, acceptEventName);
    strcpy(listener->attachEventName, attachEventName);
    listener->isListening = true;
    listener->isAccepted = false;
    listener->acceptPid = 0;
    listener->attachPid = (int)GetCurrentProcessId();
    transport->listener = listener;

    /* Create the mutex */
    HANDLE writeMutexHandle = CreateMutex(NULL, FALSE, writeMutexName);

    /* Check we successfully opened the mutex */
    if (writeMutexHandle == NULL) 
    {
        SetLastTranError(env, "Unable to open mutex", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* If the mutex already exists then there has been an error */
    if (GetLastError() == ERROR_ALREADY_EXISTS) {
        CloseHandle(writeMutexHandle);
        SetLastTranError(env, "Mutex already exists", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Store the mutex name for future use */
    transport->mutexHandle = writeMutexHandle;

    ((internalEnv*)env->functions->reserved1)->transport = transport;

    /* Make sure the mutex is available */
    DWORD rc = WaitForSingleObject(writeMutexHandle, INFINITE);
    if (WAIT_FAILED == rc) {
        SetLastTranError(env, "Failed to acquire mutex", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Write the transport listener into shared memory */
    CopyMemory(writeRegion, listener, sizeof(sharedMemListener));

    /* Release the mutex */
    if (!ReleaseMutex(writeMutexHandle)) 
    { 
        SetLastTranError(env, "Failed to release mutex", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    } 

    UnmapViewOfFile(writeRegion);

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_StartListening

/**
 * This function implements jdwpTransportEnv::Accept
 */
static jdwpTransportError JNICALL 
ShMemTran_Accept(jdwpTransportEnv* env, jlong acceptTimeout,
        jlong handshakeTimeout)
{
    if (acceptTimeout < 0) {
        SetLastTranError(env, "acceptTimeout timeout is negative", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    if (handshakeTimeout < 0) {
        SetLastTranError(env, "handshakeTimeout timeout is negative", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    /* Get the shared memory transport */
    sharedMemTransport *transport = ((internalEnv*)env->functions->reserved1)->transport;

    /* Wait for the attach to be signalled */
    DWORD rc;
    if (acceptTimeout != 0) {
        rc = WaitForSingleObject(transport->attachEventHandle, (DWORD)acceptTimeout);
    } else {
        rc = WaitForSingleObject(transport->attachEventHandle, INFINITE);
    }

    if (WAIT_FAILED == rc) {
        SetLastTranError(env, "Failed waiting for attach event", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Open the shared memory region */
    void *readRegion = MapViewOfFile(transport->sharedMemoryHandle, FILE_MAP_ALL_ACCESS, 0, 0, 0);                   
    if (NULL == readRegion) 
    { 
        SetLastTranError(env, "MapViewOfFile failed on server shared memory", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_Accept

/**
 * This function implements jdwpTransportEnv::Attach
 */
static jdwpTransportError JNICALL 
ShMemTran_Attach(jdwpTransportEnv* env, const char* address,
        jlong attachTimeout, jlong handshakeTimeout)
{
    if ((0 == address) || (0 == *address)) {
        SetLastTranError(env, "address is missing", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    if (attachTimeout < 0) {
        SetLastTranError(env, "attachTimeout timeout is negative", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    if (handshakeTimeout < 0) {
        SetLastTranError(env, "handshakeTimeout timeout is negative", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    /* Check the specified address is valid */
    jdwpTransportError res;
    res = CheckAddress(env, address);
    if (res != JDWPTRANSPORT_ERROR_NONE) {
        SetLastTranError(env, "transport is currently in listen mode", 0);
        return res;
    }

    /* Attempt to open the shared memory space at "address" */
    HANDLE readHandle = OpenFileMapping(FILE_MAP_ALL_ACCESS, FALSE, address);
    if (NULL == readHandle) 
    { 
        SetLastTranError(env, "Could not open shared memory at the specified address", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Open the shared memory region */
    void *readRegion = MapViewOfFile(readHandle, FILE_MAP_ALL_ACCESS, 0, 0, 0);                   
    if (NULL == readRegion) 
    { 
        SetLastTranError(env, "MapViewOfFile failed on server shared memory", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    /* Allocate the shared memory transport structure */
    sharedMemTransport *transport = (sharedMemTransport*)(((internalEnv*)env->functions->reserved1)->alloc)(sizeof(sharedMemTransport));
    ((internalEnv*)env->functions->reserved1)->transport = transport;
    strcpy(transport->name, address);
    
    /* Read the sharedMemListener from shared memory */
    sharedMemListener* listener = (sharedMemListener*)(((internalEnv*)env->functions->reserved1)->alloc)(sizeof(sharedMemListener));
    CopyMemory(listener, readRegion, sizeof(sharedMemListener));
    ((internalEnv*)env->functions->reserved1)->transport->listener = listener;
    char *mutexName = (char*)listener->mutexName;

    /* Open the mutex */
    HANDLE mutexHandle = OpenMutex(MUTEX_ALL_ACCESS, FALSE, mutexName);

    /* Check we successfully opened the mutex */
    if (NULL == mutexHandle) 
    {
        SetLastTranError(env, "Unable to open mutex", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

   /* If the mutex does not already exist then there has been an error */
   if (GetLastError() == ERROR_FILE_NOT_FOUND) {
       SetLastTranError(env, "Could not open existing mutex", GetLastError());
       return JDWPTRANSPORT_ERROR_INTERNAL;
   }

   /* Store the mutex */
   transport->mutexHandle = mutexHandle;

   /* TODO: Need to write something to memory here after the listener structure */

   /* Open the attach event */
   HANDLE attachEventHandle = OpenEvent(EVENT_MODIFY_STATE, FALSE, listener->attachEventName);

   if (NULL == attachEventHandle) {
       SetLastTranError(env, "Could not open attach event", GetLastError());
       return JDWPTRANSPORT_ERROR_INTERNAL;
   }

   transport->attachEventHandle = attachEventHandle;

   /* Open the accept event */
   HANDLE acceptEventHandle = OpenEvent(EVENT_ALL_ACCESS, FALSE, listener->acceptEventName);

   if (NULL == acceptEventHandle) {
       SetLastTranError(env, "Could not open accept event", GetLastError());
       return JDWPTRANSPORT_ERROR_INTERNAL;
   }

   transport->acceptEventHandle = acceptEventHandle;

   /* Trigger the attach event */
   BOOL success = SetEvent(attachEventHandle);

   if (!success) {
       SetLastTranError(env, "Error setting attach event", GetLastError());
       return JDWPTRANSPORT_ERROR_INTERNAL;
   }

    /* Wait for the accept event response to be signalled */
    DWORD rc = WaitForSingleObject(acceptEventHandle, (DWORD)attachTimeout);
    if (WAIT_FAILED == rc) {
        SetLastTranError(env, "Failed waiting for accept event", GetLastError());
        return JDWPTRANSPORT_ERROR_INTERNAL;
    }

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_Attach

/*
 * Clean up all the allocated memory in the internalEnv structure
 */
static jdwpTransportError
CleanupTransport(jdwpTransportEnv* env)
{
    /* TODO: close handles and free allocated structures */
    sharedMemTransport *transport = ((internalEnv*)env->functions->reserved1)->transport;

    if (NULL == transport) return JDWPTRANSPORT_ERROR_NONE;

    /* Free the listener structure */
    if (transport->listener) {
        (((internalEnv*)env->functions->reserved1)->free)(transport->listener);
    }

    /* Close handles stored in the transport structure */
    if (transport->mutexHandle) CloseHandle(transport->mutexHandle);
    if (transport->acceptEventHandle) CloseHandle(transport->acceptEventHandle);
    if (transport->attachEventHandle) CloseHandle(transport->attachEventHandle);
    if (transport->sharedMemoryHandle) CloseHandle(transport->sharedMemoryHandle);

    (((internalEnv*)env->functions->reserved1)->free)(transport);
    ((internalEnv*)env->functions->reserved1)->transport = NULL;

    return JDWPTRANSPORT_ERROR_NONE;
}

/**
 * This function implements jdwpTransportEnv::StopListening
 */
static jdwpTransportError JNICALL 
ShMemTran_StopListening(jdwpTransportEnv* env)
{
    /* Call isOpen to check if there is a connection - if not, just return */
    if (!env->IsOpen()) return JDWPTRANSPORT_ERROR_NONE;

    return CleanupTransport(env);
} // ShMemTran_StopListening

/**
 * This function implements jdwpTransportEnv::IsOpen
 */
static jboolean JNICALL 
ShMemTran_IsOpen(jdwpTransportEnv* env)
{
    /* Simple check to see if transport structure and shared memory handle is initialised */
    sharedMemTransport *transport = ((internalEnv*)env->functions->reserved1)->transport;
    if ((transport) && (transport->sharedMemoryHandle)) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
} // ShMemTran_IsOpen

/**
 * This function implements jdwpTransportEnv::Close
 */
static jdwpTransportError JNICALL 
ShMemTran_Close(jdwpTransportEnv* env)
{
    /* Call isOpen to check if there is a connection - if not, just return */
    if (!env->IsOpen()) return JDWPTRANSPORT_ERROR_NONE;

    return CleanupTransport(env);
} // ShMemTran_Close

/**
 * This function implements jdwpTransportEnv::ReadPacket
 */
static jdwpTransportError JNICALL 
ShMemTran_ReadPacket(jdwpTransportEnv* env, jdwpPacket* packet)
{
    /* Check packet is non-null */
    if (NULL == packet) {
        SetLastTranError(env, "Packet pointer is NULL", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    /* TODO: implement */

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_ReadPacket

/**
 * This function implements jdwpTransportEnv::WritePacket
 */
static jdwpTransportError JNICALL 
ShMemTran_WritePacket(jdwpTransportEnv* env, const jdwpPacket* packet)
{
    if (NULL == packet) {
        SetLastTranError(env, "Packet pointer is NULL", 0);
        return JDWPTRANSPORT_ERROR_ILLEGAL_ARGUMENT;
    }

    /* TODO: implement */

    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_WritePacket

/**
 * This function implements jdwpTransportEnv::GetLastError
 */
static jdwpTransportError JNICALL 
ShMemTran_GetLastError(jdwpTransportEnv* env, char** message)
{
    *message = ((internalEnv*)env->functions->reserved1)->lastError->GetLastErrorMessage();
    if (0 == *message) {
        return JDWPTRANSPORT_ERROR_MSG_NOT_AVAILABLE;
    }
    return JDWPTRANSPORT_ERROR_NONE;
} // ShMemTran_GetLastError


/**
 * This function is called by agent when the library is loaded 
 * It initialises the jdwpTransportNativeInterface structure.
 */
extern "C" JNIEXPORT jint JNICALL 
jdwpTransport_OnLoad(JavaVM *vm, jdwpTransportCallback* callback,
             jint version, jdwpTransportEnv** env)
{
    if (version != JDWPTRANSPORT_VERSION_1_0) {
        return JNI_EVERSION;
    }

    internalEnv* iEnv = (internalEnv*)callback->alloc(sizeof(internalEnv));
    if (iEnv == 0) {
        return JNI_ENOMEM;
    }
    iEnv->jvm = vm;
    iEnv->alloc = callback->alloc;
    iEnv->free = callback->free;
    iEnv->transport = NULL;
    iEnv->lastError = NULL;

    jdwpTransportNativeInterface_* envTNI = (jdwpTransportNativeInterface_*)callback->alloc(sizeof(jdwpTransportNativeInterface_));
    if (0 == envTNI) {
        callback->free(iEnv);
        return JNI_ENOMEM;
    }

    envTNI->GetCapabilities = &ShMemTran_GetCapabilities;
    envTNI->Attach = &ShMemTran_Attach;
    envTNI->StartListening = &ShMemTran_StartListening;
    envTNI->StopListening = &ShMemTran_StopListening;
    envTNI->Accept = &ShMemTran_Accept;
    envTNI->IsOpen = &ShMemTran_IsOpen;
    envTNI->Close = &ShMemTran_Close;
    envTNI->ReadPacket = &ShMemTran_ReadPacket;
    envTNI->WritePacket = &ShMemTran_WritePacket;
    envTNI->GetLastError = &ShMemTran_GetLastError;
    envTNI->reserved1 = iEnv;

    _jdwpTransportEnv* resEnv = (_jdwpTransportEnv*)callback->alloc(sizeof(_jdwpTransportEnv));
    if (0 == resEnv) {
        callback->free(iEnv);
        callback->free(envTNI);
        return JNI_ENOMEM;
    }

    resEnv->functions = envTNI;
    *env = resEnv;

    return JNI_OK;
} // jdwpTransport_OnLoad

/**
 * This function may be called by the agent before library 
 * unloading. 
 */
extern "C" JNIEXPORT void JNICALL 
jdwpTransport_UnLoad(jdwpTransportEnv** env)
{
    /* TODO: Free memory allocated in OnLoad */
    ((internalEnv*)(*env)->functions->reserved1)->free((*env)->functions->reserved1);
} // jdwpTransport_UnLoad


