/* Copyright 2004, 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Common natives supporting the memory system interface.
 */

#include <sys/mman.h>
#include <string.h>
#include "vmi.h"
#include "OSMemory.h"
#include "IMemorySystem.h"

JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSMemory_malloc
  (JNIEnv * env, jobject thiz, jlong size)
{
  PORT_ACCESS_FROM_ENV (env);

  jclass exceptionClazz;
  void *address = hymem_allocate_memory ((UDATA) size);

  if (address == NULL)
    {
      exceptionClazz = (*env)->FindClass (env, "java/lang/OutOfMemoryError");
      (*env)->ThrowNew (env, exceptionClazz,
			"Insufficient memory available.");
    }

  return (jlong) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_free
  (JNIEnv * env, jobject thiz, jlong address)
{
  PORT_ACCESS_FROM_ENV (env);

  hymem_free_memory ((void *) ((IDATA) address));
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_memmove
  (JNIEnv * env, jobject thiz, jlong destAddress, jlong srcAddress,
   jlong length)
{
  memmove ((void *) ((IDATA) destAddress),
	   (const void *) ((IDATA) srcAddress), (size_t) length);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_memset
  (JNIEnv * env, jobject thiz, jlong address, jbyte value, jlong length)
{
  memset ((void *) ((IDATA) address), (int) value, (size_t) length);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getByteArray
  (JNIEnv * env, jobject thiz, jlong address, jbyteArray byteArray,
   jint offset, jint length)
{
  jboolean isCopy;
  jbyte *bytes = (*env)->GetByteArrayElements (env, byteArray, &isCopy);
  memcpy (bytes + offset, (const void *) ((IDATA) address), (size_t) length);
  if (isCopy == JNI_TRUE)
    {
      (*env)->ReleaseByteArrayElements (env, byteArray, bytes, 0);
    }
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setByteArray
  (JNIEnv * env, jobject thiz, jlong address, jbyteArray byteArray,
   jint offset, jint length)
{
  jboolean isCopy;
  jbyte *bytes = (*env)->GetByteArrayElements (env, byteArray, &isCopy);
  memcpy ((void *) ((IDATA) address),
	  (const jbyte *) ((IDATA) bytes + offset), (size_t) length);
  if (isCopy == JNI_TRUE)
    {
      (*env)->ReleaseByteArrayElements (env, byteArray, bytes, JNI_ABORT);
    }
}

JNIEXPORT jbyte JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getByte
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jbyte *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setByte
  (JNIEnv * env, jobject thiz, jlong address, jbyte value)
{
  *(jbyte *) ((IDATA) address) = value;
}

JNIEXPORT jshort JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getShort
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jshort *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setShort
  (JNIEnv * env, jobject thiz, jlong address, jshort value)
{
  *(jshort *) ((IDATA) address) = value;
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getInt
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jint *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setInt
  (JNIEnv * env, jobject thiz, jlong address, jint value)
{
  *(jint *) ((IDATA) address) = value;
}

JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getLong
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jlong *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setLong
  (JNIEnv * env, jobject thiz, jlong address, jlong value)
{
  *(jlong *) ((IDATA) address) = value;
}

JNIEXPORT jfloat JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getFloat
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jfloat *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setFloat
  (JNIEnv * env, jobject thiz, jlong address, jfloat value)
{
  *(jfloat *) ((IDATA) address) = value;
}

JNIEXPORT jdouble JNICALL Java_org_apache_harmony_luni_platform_OSMemory_getDouble
  (JNIEnv * env, jobject thiz, jlong address)
{
  return *(jdouble *) ((IDATA) address);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_setDouble
  (JNIEnv * env, jobject thiz, jlong address, jdouble value)
{
  *(jdouble *) ((IDATA) address) = value;
}


/*
 * Class:     org_apache_harmony_luni_platform_OSMemory
 * Method:    unmapImpl
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_apache_harmony_luni_platform_OSMemory_unmapImpl
  (JNIEnv * env, jobject thiz, jlong fd)
{
  PORT_ACCESS_FROM_ENV (env);
  hymmap_unmap_file((void *)fd);
}

/*
 * Class:     org_apache_harmony_luni_platform_OSMemory
 * Method:    mmapImpl
 * Signature: (JJJI)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_harmony_luni_platform_OSMemory_mmapImpl
  (JNIEnv * env, jobject thiz, jlong fd, jlong alignment, jlong size, jint mmode)
{
  PORT_ACCESS_FROM_ENV (env);
  void *mapAddress = NULL;
  int prot, flags;
		  
  // Convert from Java mapping mode to port library mapping mode.
  switch (mmode)
    {
      case org_apache_harmony_luni_platform_IMemorySystem_MMAP_READ_ONLY:
	prot = PROT_READ;
	flags = MAP_SHARED;
        break;
      case org_apache_harmony_luni_platform_IMemorySystem_MMAP_READ_WRITE:
	prot = PROT_READ|PROT_WRITE;
	flags = MAP_SHARED;
        break;
      case org_apache_harmony_luni_platform_IMemorySystem_MMAP_WRITE_COPY:
	prot = PROT_READ|PROT_WRITE;
	flags = MAP_PRIVATE;
        break;
      default:
        return -1;
    }

//TODO: how to unmap
 // mapAddress = hymmap_map_filehandler(fd, &mapAddress, mapmode, (IDATA)alignment, (IDATA)size);

   mapAddress = mmap(0,size, prot, flags,fd,alignment);
  if (mapAddress == NULL)
    {
      return -1;
    }
  return (jlong) mapAddress;
}

