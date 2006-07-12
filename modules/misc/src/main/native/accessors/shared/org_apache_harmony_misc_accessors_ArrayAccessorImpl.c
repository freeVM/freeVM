/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Andrey Y. Chernyshev
 * @version $Revision$
 */
#include "MemMacros.h"
#include "org_apache_harmony_misc_accessors_ArrayAccessor.h"

/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    staticLockArray
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticLockArray
  (JNIEnv *env, jclass clazz, jobject array) {
    return addr2jlong((*env)->GetPrimitiveArrayCritical(env, (jarray)array, NULL));
}


/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    staticUnlockArray
 * Signature: (Ljava/lang/Object;J)V
 */
JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticUnlockArray
  (JNIEnv *env, jclass clazz, jobject array, jlong addr) {
    (*env)->ReleasePrimitiveArrayCritical(env, (jarray)array,  jlong2addr(jlong, addr), 0);
}

/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    staticUnlockArrayNoCopy
 * Signature: (Ljava/lang/Object;J)V
 */
JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticUnlockArrayNoCopy
  (JNIEnv *env, jclass clazz, jobject array, jlong addr) {
    (*env)->ReleasePrimitiveArrayCritical(env, (jarray)array,  jlong2addr(jlong, addr), JNI_ABORT);
}



/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    staticPin<Type>Array
 * Signature: (Ljava/lang/Object;)J
 * Method:    staticUnpin<Type>Array
 * Signature: (Ljava/lang/Object;J)V
 * Method:    staticUnpin<Type>ArrayNoCopy
 * Signature: (Ljava/lang/Object;J)V
 */
#define pinFunctions(TypeArray, TypeArrayNoCopy, TypeArrayElements, typeArray, t) \
JNIEXPORT jlong JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticPin##TypeArray \
(JNIEnv *env, jclass clss, jobject array) { \
    jboolean isCopy; \
    return addr2jlong((*env)->Get##TypeArrayElements(env, (typeArray)array, &isCopy)); \
} \
JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticUnpin##TypeArray \
(JNIEnv *env, jclass clss, jobject array, jlong addr) { \
  (*env)->Release##TypeArrayElements(env, (typeArray)array, jlong2addr(t, addr), 0); \
} \
JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_staticUnpin##TypeArrayNoCopy \
(JNIEnv *env, jclass clss, jobject array, jlong addr) { \
  (*env)->Release##TypeArrayElements(env, (typeArray)array, jlong2addr(t, addr), JNI_ABORT); \
}

pinFunctions(ByteArray, ByteArrayNoCopy, ByteArrayElements, jbyteArray, jbyte)
pinFunctions(CharArray, CharArrayNoCopy, CharArrayElements, jcharArray, jchar)
pinFunctions(ShortArray, ShortArrayNoCopy, ShortArrayElements, jshortArray, jshort)
pinFunctions(IntArray, IntArrayNoCopy, IntArrayElements, jintArray, jint)
pinFunctions(LongArray, LongArrayNoCopy, LongArrayElements, jlongArray, jlong)
pinFunctions(BooleanArray, BooleanArrayNoCopy, BooleanArrayElements, jbooleanArray, jboolean)
pinFunctions(FloatArray, FloatArrayNoCopy, FloatArrayElements, jfloatArray, jfloat)
pinFunctions(DoubleArray, DoubleArrayNoCopy, DoubleArrayElements, jdoubleArray, jdouble)



/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    getElement
 * Signature: ([TI)T
 * Method:    setElement
 * Signature: ([TIT)V
 */
#define setGetFunctions(TI, TIT, t, typeArray) \
 JNIEXPORT t JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_getElement___3##TI \
  (JNIEnv *env, jobject obj, typeArray array, jint index) { \
    t* ptr = (t*)(*env)->GetPrimitiveArrayCritical(env, (jarray)array, NULL); \
    t res = ptr[index]; \
    (*env)->ReleasePrimitiveArrayCritical(env, (jarray)array, ptr, 0); \
    return res; \
  } \
 JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_setElement___3##TIT \
(JNIEnv *env, jobject obj, typeArray array, jint index, t value) { \
    t* ptr = (t*)(*env)->GetPrimitiveArrayCritical(env, (jarray)array, NULL); \
    ptr[index] = value; \
    (*env)->ReleasePrimitiveArrayCritical(env, (jarray)array, ptr, 0); \
}

setGetFunctions(BI, BIB, jbyte, jbyteArray);
setGetFunctions(ZI, ZIZ, jboolean, jbooleanArray);
setGetFunctions(SI, SIS, jshort, jshortArray);
setGetFunctions(CI, CIC, jchar, jcharArray);
setGetFunctions(II, III, jint, jintArray);
setGetFunctions(JI, JIJ, jlong, jlongArray);
setGetFunctions(FI, FIF, jfloat, jfloatArray);
setGetFunctions(DI, DID, jdouble, jdoubleArray);

/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    getElement
 * Signature: ([Ljava/lang/Object;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_getElement___3Ljava_lang_Object_2I
(JNIEnv *env, jobject obj, jobjectArray array, jint index) {
    return (*env)->GetObjectArrayElement(env, array, index);
}

/*
 * Class:     org_apache_harmony_misc_accessors_ArrayAccessor
 * Method:    setElement
 * Signature: ([Ljava/lang/Object;ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_apache_harmony_misc_accessors_ArrayAccessor_setElement___3Ljava_lang_Object_2ILjava_lang_Object_2
(JNIEnv *env, jobject obj, jobjectArray array, jint index, jobject value) {
    (*env)->SetObjectArrayElement(env, array, index, value);
}

