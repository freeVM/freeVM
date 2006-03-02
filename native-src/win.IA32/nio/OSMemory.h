/* Copyright 2004,2006 The Apache Software Foundation or its licensors, as applicable
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

#include <jni.h>
/* Header for class com_ibm_platform_OSMemory */

#ifndef _Included_com_ibm_platform_OSMemory
#define _Included_com_ibm_platform_OSMemory
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: POINTER_SIZE */
/* Inaccessible static: NATIVE_ORDER */
/* Inaccessible static: singleton */
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    isLittleEndianImpl
 * Signature: ()Z
 */
  JNIEXPORT jboolean JNICALL Java_com_ibm_platform_OSMemory_isLittleEndianImpl
    (JNIEnv *, jclass);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getPointerSizeImpl
 * Signature: ()I
 */
  JNIEXPORT jint JNICALL Java_com_ibm_platform_OSMemory_getPointerSizeImpl
    (JNIEnv *, jclass);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    malloc
 * Signature: (J)J
 */
  JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSMemory_malloc
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    free
 * Signature: (J)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_free
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    memset
 * Signature: (JBJ)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_memset
    (JNIEnv *, jobject, jlong, jbyte, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    memmove
 * Signature: (JJJ)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_memmove
    (JNIEnv *, jobject, jlong, jlong, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getByteArray
 * Signature: (J[BII)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_getByteArray
    (JNIEnv *, jobject, jlong, jbyteArray, jint, jint);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setByteArray
 * Signature: (J[BII)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setByteArray
    (JNIEnv *, jobject, jlong, jbyteArray, jint, jint);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setCharArray
 * Signature: (J[CII)V
 */
JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setCharArray
  (JNIEnv *, jobject, jlong, jcharArray, jint, jint);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getByte
 * Signature: (J)B
 */
  JNIEXPORT jbyte JNICALL Java_com_ibm_platform_OSMemory_getByte
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setByte
 * Signature: (JB)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setByte
    (JNIEnv *, jobject, jlong, jbyte);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getShort
 * Signature: (J)S
 */
  JNIEXPORT jshort JNICALL Java_com_ibm_platform_OSMemory_getShort
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setShort
 * Signature: (JS)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setShort
    (JNIEnv *, jobject, jlong, jshort);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getInt
 * Signature: (J)I
 */
  JNIEXPORT jint JNICALL Java_com_ibm_platform_OSMemory_getInt
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setInt
 * Signature: (JI)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setInt
    (JNIEnv *, jobject, jlong, jint);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getLong
 * Signature: (J)J
 */
  JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSMemory_getLong
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setLong
 * Signature: (JJ)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setLong
    (JNIEnv *, jobject, jlong, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getFloat
 * Signature: (J)F
 */
  JNIEXPORT jfloat JNICALL Java_com_ibm_platform_OSMemory_getFloat
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setFloat
 * Signature: (JF)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setFloat
    (JNIEnv *, jobject, jlong, jfloat);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getDouble
 * Signature: (J)D
 */
  JNIEXPORT jdouble JNICALL Java_com_ibm_platform_OSMemory_getDouble
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setDouble
 * Signature: (JD)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setDouble
    (JNIEnv *, jobject, jlong, jdouble);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    getAddress
 * Signature: (J)J
 */
  JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSMemory_getAddress
    (JNIEnv *, jobject, jlong);
/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    setAddress
 * Signature: (JJ)V
 */
  JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_setAddress
    (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    loadImpl
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_platform_OSMemory_loadImpl
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    isLoadedImpl
 * Signature: (Lcom/ibm/platform/struct/PlatformAddress;J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ibm_platform_OSMemory_isLoadedImpl
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    flushImpl
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_platform_OSMemory_flushImpl
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    mmapImpl
 * Signature: (JJJII)J
 */
JNIEXPORT jlong JNICALL Java_com_ibm_platform_OSMemory_mmapImpl
  (JNIEnv *, jobject, jlong, jlong, jlong, jint);

/*
 * Class:     com_ibm_platform_OSMemory
 * Method:    unmapImpl
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ibm_platform_OSMemory_unmapImpl
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
