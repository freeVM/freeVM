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
 * Common natives supporting the file system interface.
 */

#include <harmony.h>
#include <string.h>

#include "OSFileSystem.h"
#include "IFileSystem.h"

/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    readDirectImpl
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_readDirectImpl
  (JNIEnv * env, jobject thiz, jlong fd, jlong buf, jint offset, jint nbytes)
{
  PORT_ACCESS_FROM_ENV (env);
  return (jlong) hyfile_read ((IDATA) fd, (void *) (buf+offset), (IDATA) nbytes);
}

/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    writeDirectImpl
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_writeDirectImpl
  (JNIEnv * env, jobject thiz, jlong fd, jlong buf, jint offset, jint nbytes)
{
  PORT_ACCESS_FROM_ENV (env);
  return (jlong) hyfile_write ((IDATA) fd, (const void *) (buf+offset),
                               (IDATA) nbytes);
}

/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    readImpl
 * Signature: (J[BII)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_readImpl
  (JNIEnv * env, jobject thiz, jlong fd, jbyteArray byteArray, jint offset,
   jint nbytes)
{
  PORT_ACCESS_FROM_ENV (env);
  jboolean isCopy;
  jbyte *bytes = (*env)->GetByteArrayElements (env, byteArray, &isCopy);
  jlong result;

  result =
    (jlong) hyfile_read ((IDATA) fd, (void *) (bytes + offset),
                         (IDATA) nbytes);
  if (isCopy == JNI_TRUE)
    {
      (*env)->ReleaseByteArrayElements (env, byteArray, bytes, 0);
    }

  return result;
}

/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    writeImpl
 * Signature: (J[BII)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_writeImpl
  (JNIEnv * env, jobject thiz, jlong fd, jbyteArray byteArray, jint offset, jint nbytes)
{
  PORT_ACCESS_FROM_ENV (env);
  jboolean isCopy;
  jbyte *bytes = (*env)->GetByteArrayElements (env, byteArray, &isCopy);
  jlong result;

  result =
    (jlong) hyfile_write ((IDATA) fd, (void *) (bytes + offset),
                         (IDATA) nbytes);
  if (isCopy == JNI_TRUE)
    {
      (*env)->ReleaseByteArrayElements (env, byteArray, bytes, 0);
    }

  return result;
}

/**
 * Seeks a file descriptor to a given file position.
 * 
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be seeked
 * @param offset distance of movement in bytes relative to whence arg
 * @param whence enum value indicating from where the offset is relative
 * The valid values are defined in fsconstants.h.
 * @return the new file position from the beginning of the file, in bytes;
 * or -1 if a problem occurs.
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_seekImpl
  (JNIEnv * env, jobject thiz, jlong fd, jlong offset, jint whence)
{
  PORT_ACCESS_FROM_ENV (env);
  I_32 hywhence = 0;            /* The HY PPL equivalent of our whence arg.*/

  /* Convert whence argument */
  switch (whence)
    {
    case com_ibm_platform_IFileSystem_SEEK_SET:
      hywhence = HySeekSet;
      break;
    case com_ibm_platform_IFileSystem_SEEK_CUR:
      hywhence = HySeekCur;
      break;
    case com_ibm_platform_IFileSystem_SEEK_END:
      hywhence = HySeekEnd;
      break;
    default:
      return -1;
    }

  return (jlong) hyfile_seek ((IDATA) fd, (IDATA) offset, hywhence);
}

/**
 * Flushes a file state to disk.
 *
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be flushed
 * @param metadata if true also flush metadata, otherwise just flush data is possible.
 * @return zero on success and -1 on failure
 *
 * Method:    fflushImpl
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_fflushImpl
  (JNIEnv * env, jobject thiz, jlong fd, jboolean metadata)
{
  PORT_ACCESS_FROM_ENV (env);

  return (jint) hyfile_sync ((IDATA) fd);
}

/**
 * Closes the given file handle
 * 
 * @param env pointer to Java environment
 * @param thiz pointer to object receiving the message
 * @param fd handle of file to be closed
 * @return zero on success and -1 on failure
 *
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    closeImpl
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_closeImpl
  (JNIEnv * env, jobject thiz, jlong fd)
{
  PORT_ACCESS_FROM_ENV (env);

  return (jint) hyfile_close ((IDATA) fd);
}


/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    truncateImpl
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_platform_OSFileSystem_truncateImpl
  (JNIEnv * env, jobject thiz, jlong fd, jlong size)
{
  PORT_ACCESS_FROM_ENV (env);

  return (jint)hyfile_set_length((IDATA)fd, (I_64)size);

}

#define jclSeparator DIR_SEPARATOR
/**
  * This will convert all separators to the proper platform separator
  * and remove duplicates on non POSIX platforms.
  */
void convertToPlatform (char *path)
{
  char *pathIndex;
  int length = strlen (path);

  /* Convert all separators to the same type */
  pathIndex = path;
  while (*pathIndex != '\0')
    {
      if ((*pathIndex == '\\' || *pathIndex == '/')
          && (*pathIndex != jclSeparator))
        *pathIndex = jclSeparator;
      pathIndex++;
    }

  /* Remove duplicate separators */
  if (jclSeparator == '/')
    return;                     /* Do not do POSIX platforms */

  /* Remove duplicate initial separators */
  pathIndex = path;
  while ((*pathIndex != '\0') && (*pathIndex == jclSeparator))
    {
      pathIndex++;
    }
  if ((pathIndex > path) && (length > (pathIndex - path))
      && (*(pathIndex + 1) == ':'))
    {
      /* For Example '////c:/*' */
      int newlen = length - (pathIndex - path);
      memmove (path, pathIndex, newlen);
      path[newlen] = '\0';
    }
  else
    {
      if ((pathIndex - path > 3) && (length > (pathIndex - path)))
        {
          /* For Example '////serverName/*' */
          int newlen = length - (pathIndex - path) + 2;
          memmove (path, pathIndex - 2, newlen);
          path[newlen] = '\0';
        }
    }
  /* This will have to handle extra \'s but currently doesn't */
}

/*
 * Class:     com_ibm_platform_OSFileSystem
 * Method:    openImpl
 * Signature: ([BI)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSFileSystem_openImpl
  (JNIEnv * env, jobject obj, jbyteArray path, jint jflags){
      PORT_ACCESS_FROM_ENV (env);
      I_32 flags = 0;
      I_32 mode = 0; 
      IDATA portFD;
      jsize length;
      char pathCopy[HyMaxPath];

      switch(jflags){
        case com_ibm_platform_IFileSystem_O_RDONLY:
                flags = HyOpenRead;
                mode = 0;
                break;
        case com_ibm_platform_IFileSystem_O_WRONLY:
                flags = HyOpenCreate | HyOpenWrite | HyOpenTruncate;
                mode = 0666;
                break;
        case com_ibm_platform_IFileSystem_O_RDWR:
                flags = HyOpenRead | HyOpenWrite | HyOpenCreate;
                mode = 0666;
                break;
        case com_ibm_platform_IFileSystem_O_APPEND:
                flags = HyOpenWrite | HyOpenCreate | HyOpenAppend; 
                mode = 0666;
                break;
      }

      length = (*env)->GetArrayLength (env, path);
      length = length < HyMaxPath - 1 ? length : HyMaxPath - 1;
      ((*env)->GetByteArrayRegion (env, path, 0, length, pathCopy));
      pathCopy[length] = '\0';
      convertToPlatform (pathCopy);

      portFD = hyfile_open (pathCopy, flags, mode);
      return (jlong)portFD;
  }


