/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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

#include <string.h>
#include "iohelp.h"
#include "jclglob.h"
#include <stdio.h>
#include <stdlib.h>
#include "jni.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "com_ibm_platform_OSFileSystem.h"
///////////////gnuclasspathadapter

JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_fflushImpl
  (JNIEnv *env, jobject obj1, jlong fileDescriptor, jboolean bool1)
{
    printf("Java_com_ibm_platform_OSFileSystem_fflushImpl not implemented\n"); //fflush(stdout);
    return 0;
}


JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_seekImpl
  (JNIEnv *env, jobject obj1, jlong fd, jlong offset, jint whence)
{
    jlong position = lseek(fd, 0L, SEEK_SET);
    if (position == -1) {
        printf("Java_com_ibm_platform_OSFileSystem_seekImpl failed --1\n");
        return -1;
    }
    position = lseek(fd, offset, whence);
    return position;
}


JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_readImpl
  (JNIEnv *env, jobject obj1, jlong fd, jbyteArray oneByteArray)
{
    unsigned char zz;
    jboolean bb = 1;
    int stat = read(fd, &zz, 1);
///jbyte *(JNICALL *      GetByteArrayElements)(JNIEnv *env, jbyteArray array, jboolean *isCopy);
    jbyte *theByte  = (*env)->GetByteArrayElements(env, oneByteArray, &bb);
    theByte[0] = (jbyte)zz;
    (*env)->ReleaseByteArrayElements(env, oneByteArray, theByte, 0);
    return 0;
}


JNIEXPORT void JNICALL Java_com_ibm_platform_OSFileSystem_writeImpl
  (JNIEnv *env, jobject obj1, jlong fileDescriptor, jint theCharacterToBeWritten)
{  
    int xx = 0x000000ff & (int)theCharacterToBeWritten;
    char cc = (char)xx;
    int yy = (int)fileDescriptor;
    write(yy, &cc, 1);
}

JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_closeImpl
  (JNIEnv *env, jobject obj1, jlong fd)
{
    return (jint)close(fd);
}


JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_truncateImpl
  (JNIEnv *env, jobject obj1, jlong long1, jlong long2)
{
    printf("Java_com_ibm_platform_OSFileSystem_truncateImpl not implemented\n");
    return 0;
}


JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_openImpl
  (JNIEnv *env, jobject obj1, jbyteArray ba1, jint int1)
{
    int fmode = O_CREAT|O_RDWR;
    jboolean isCopy = 1;
	
    I_32 result;
    char pathCopy[HyMaxPath];
    jsize length = (*env)->GetArrayLength (env, ba1);
    length = length < HyMaxPath - 1 ? length : HyMaxPath - 1;
    ((*env)->GetByteArrayRegion (env, ba1, 0, length, pathCopy));
    pathCopy[length] = '\0';
    ioh_convertToPlatform (pathCopy);

    jlong fd = (jlong)open(pathCopy, fmode, 0755);

    return fd;
}


