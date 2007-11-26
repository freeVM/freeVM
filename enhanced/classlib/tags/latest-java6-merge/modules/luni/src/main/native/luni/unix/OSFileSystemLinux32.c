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

/*
 * Linux32 specific natives supporting the file system interface.
 */

#include <sys/uio.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#if defined(FREEBSD) || defined(AIX) || defined(ZOS)
#include <sys/types.h>
#include <sys/socket.h>
#else
#include <sys/sendfile.h>
#endif
#include "vmi.h"
#include "iohelp.h"
#include "nethelp.h"

#include "IFileSystem.h"
#include "OSFileSystem.h"

typedef int OSSOCKET;   
typedef struct hysocket_struct
{
  OSSOCKET sock;
  U_16 family;
} hysocket_struct;


/**
 * Lock the file identified by the given handle.
 * The range and lock type are given.
 */
JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_lockImpl
  (JNIEnv * env, jobject thiz, jlong handle, jlong start, jlong length,
   jint typeFlag, jboolean waitFlag)
{
  int rc;
  int waitMode = (waitFlag) ? F_SETLKW : F_SETLK;
  struct flock lock = { 0 };

  // If start or length overflow the max values we can represent, then max them out.
#if __WORDSIZE==32
#define MAX_INT 0x7fffffffL
  if (start > MAX_INT)
    {
      start = MAX_INT;
    }
  if (length > MAX_INT)
    {
      length = MAX_INT;
    }
#endif

  lock.l_whence = SEEK_SET;
  lock.l_start = start;
  lock.l_len = length;

  if ((typeFlag & org_apache_harmony_luni_platform_IFileSystem_SHARED_LOCK_TYPE) ==
      org_apache_harmony_luni_platform_IFileSystem_SHARED_LOCK_TYPE)
    {
      lock.l_type = F_RDLCK;
    }
  else
    {
      lock.l_type = F_WRLCK;
    }

  do
    {
      rc = fcntl (handle, waitMode, &lock);
    }
  while ((rc < 0) && (errno == EINTR));

  return (rc == -1) ? -1 : 0;
}

/**
 * Unlocks the specified region of the file.
 */
JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_unlockImpl
  (JNIEnv * env, jobject thiz, jlong handle, jlong start, jlong length)
{
  int rc;
  struct flock lock = { 0 };

  // If start or length overflow the max values we can represent, then max them out.
#if __WORDSIZE==32
#define MAX_INT 0x7fffffffL
  if (start > MAX_INT)
    {
      start = MAX_INT;
    }
  if (length > MAX_INT)
    {
      length = MAX_INT;
    }
#endif

  lock.l_whence = SEEK_SET;
  lock.l_start = start;
  lock.l_len = length;
  lock.l_type = F_UNLCK;

  do
    {
      rc = fcntl (handle, F_SETLKW, &lock);
    }
  while ((rc < 0) && (errno == EINTR));

  return (rc == -1) ? -1 : 0;
}

/**
 * Returns the granularity of the starting address for virtual memory allocation.
 * (It's the same as the page size.)
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    getAllocGranularity
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_getAllocGranularity
  (JNIEnv * env, jobject thiz)
{
  static int allocGranularity = 0;
  if(allocGranularity == 0){
    allocGranularity = getpagesize();
  }
  return allocGranularity;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    readvImpl
 * Signature: (J[J[I[I)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_readvImpl
  (JNIEnv *env, jobject thiz, jlong fd, jlongArray jbuffers, jintArray joffsets, jintArray jlengths, jint size){
  PORT_ACCESS_FROM_ENV (env);
  jboolean bufsCopied = JNI_FALSE;
  jboolean offsetsCopied = JNI_FALSE;
  jboolean lengthsCopied = JNI_FALSE;
  jlong *bufs; 
  jint *offsets;
  jint *lengths;
  int i = 0;
  long totalRead = 0;  
  struct iovec *vectors = (struct iovec *)hymem_allocate_memory(size * sizeof(struct iovec));
  if(vectors == NULL){
    return -1;
  }
  bufs = (*env)->GetLongArrayElements(env, jbuffers, &bufsCopied);
  offsets = (*env)->GetIntArrayElements(env, joffsets, &offsetsCopied);
  lengths = (*env)->GetIntArrayElements(env, jlengths, &lengthsCopied);
  while(i < size){
    vectors[i].iov_base = (void *)((IDATA)(bufs[i]+offsets[i]));
    vectors[i].iov_len = lengths[i];
    i++;
  }
  totalRead = readv(fd, vectors, size);
  if(bufsCopied){
    (*env)->ReleaseLongArrayElements(env, jbuffers, bufs, JNI_ABORT);
  }
  if(offsetsCopied){
    (*env)->ReleaseIntArrayElements(env, joffsets, offsets, JNI_ABORT);
  }
  if(lengthsCopied){
    (*env)->ReleaseIntArrayElements(env, jlengths, lengths, JNI_ABORT);
  }
  hymem_free_memory(vectors);
  return totalRead;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    writevImpl
 * Signature: (J[J[I[I)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_writevImpl
  (JNIEnv *env, jobject thiz, jlong fd, jlongArray jbuffers, jintArray joffsets, jintArray jlengths, jint size){
  PORT_ACCESS_FROM_ENV (env);
  jboolean bufsCopied = JNI_FALSE;
  jboolean offsetsCopied = JNI_FALSE;
  jboolean lengthsCopied = JNI_FALSE;
  jlong *bufs; 
  jint *offsets;
  jint *lengths;
  int i = 0;
  long totalWritten = 0;  
  struct iovec *vectors = (struct iovec *)hymem_allocate_memory(size * sizeof(struct iovec));
  if(vectors == NULL){
    return -1;
  }
  bufs = (*env)->GetLongArrayElements(env, jbuffers, &bufsCopied);
  offsets = (*env)->GetIntArrayElements(env, joffsets, &offsetsCopied);
  lengths = (*env)->GetIntArrayElements(env, jlengths, &lengthsCopied);
  while(i < size){
    vectors[i].iov_base = (void *)((IDATA)(bufs[i]+offsets[i]));
    vectors[i].iov_len = lengths[i];
    i++;
  }
  totalWritten = writev(fd, vectors, size);
  if(bufsCopied){
    (*env)->ReleaseLongArrayElements(env, jbuffers, bufs, JNI_ABORT);
  }
  if(offsetsCopied){
    (*env)->ReleaseIntArrayElements(env, joffsets, offsets, JNI_ABORT);
  }
  if(lengthsCopied){
    (*env)->ReleaseIntArrayElements(env, jlengths, lengths, JNI_ABORT);
  }
  hymem_free_memory(vectors);
  return totalWritten;
}

/*
 * Class:     org_apache_harmony_luni_platform_OSFileSystem
 * Method:    transferImpl
 * Signature: (JLjava/io/FileDescriptor;JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSFileSystem_transferImpl
  (JNIEnv *env, jobject thiz, jlong fd, jobject sd, jlong offset, jlong count)
{
  OSSOCKET socket;
  //TODO IPV6
  hysocket_t hysocketP =
    (hysocket_t)getJavaIoFileDescriptorContentsAsAPointer (env,sd);
  if(hysocketP == NULL)
    return -1;
  socket = hysocketP->sock;
#if defined(AIX) || defined(ZOS)
  {
    struct sf_parms parms;
    parms.file_descriptor = (int)fd;
    parms.file_offset = (off64_t)offset;
    parms.file_bytes = count;
    parms.header_data = 0;
    parms.header_length = 0;
    parms.trailer_data = 0;
    parms.trailer_length = 0;
    return send_file(&socket, &parms, 0);
  }
#else
  /* Value of offset is checked in jint scope (checked in java layer)
   The conversion here is to guarantee no value lost when converting offset to off_t
   */
  off_t off = offset;
#if !defined(FREEBSD)
  return sendfile(socket,(int)fd,(off_t *)&off,(size_t)count);	
#else
  return sendfile(fd, socket, off, (size_t)count, NULL, NULL, 0);
#endif
#endif
}
