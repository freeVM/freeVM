#include <jni.h>

extern "C" {

/*
 * Class:     java_lang_reflect_Array
 * Method:    newInstance
 * Signature: (Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_newInstance__Ljava_lang_Class_2I
  (JNIEnv *env, jclass, jclass cls, jint len) {
    return env->NewObjectArray(len, cls, NULL);
}

/*
 * Class:     java_lang_reflect_Array
 * Method:    getLength
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Array_getLength
  (JNIEnv *env, jclass, jobject obj) {
    return env->GetArrayLength((jarray) obj);
}

#if 0
/*
 * Class:     java_lang_reflect_Array
 * Method:    get
 * Signature: (Ljava/lang/Object;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_get
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getBoolean
 * Signature: (Ljava/lang/Object;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_reflect_Array_getBoolean
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getByte
 * Signature: (Ljava/lang/Object;I)B
 */
JNIEXPORT jbyte JNICALL Java_java_lang_reflect_Array_getByte
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getChar
 * Signature: (Ljava/lang/Object;I)C
 */
JNIEXPORT jchar JNICALL Java_java_lang_reflect_Array_getChar
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getDouble
 * Signature: (Ljava/lang/Object;I)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_reflect_Array_getDouble
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getFloat
 * Signature: (Ljava/lang/Object;I)F
 */
JNIEXPORT jfloat JNICALL Java_java_lang_reflect_Array_getFloat
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getInt
 * Signature: (Ljava/lang/Object;I)I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Array_getInt
  (JNIEnv *, jclass, jobject, jint);


/*
 * Class:     java_lang_reflect_Array
 * Method:    getLong
 * Signature: (Ljava/lang/Object;I)J
 */
JNIEXPORT jlong JNICALL Java_java_lang_reflect_Array_getLong
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getShort
 * Signature: (Ljava/lang/Object;I)S
 */
JNIEXPORT jshort JNICALL Java_java_lang_reflect_Array_getShort
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    newInstance
 * Signature: (Ljava/lang/Class;[I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_newInstance__Ljava_lang_Class_2_3I
  (JNIEnv *, jclass, jclass, jintArray);


/*
 * Class:     java_lang_reflect_Array
 * Method:    set
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_set
  (JNIEnv *, jclass, jobject, jint, jobject);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setBoolean
 * Signature: (Ljava/lang/Object;IZ)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setBoolean
  (JNIEnv *, jclass, jobject, jint, jboolean);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setByte
 * Signature: (Ljava/lang/Object;IB)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setByte
  (JNIEnv *, jclass, jobject, jint, jbyte);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setChar
 * Signature: (Ljava/lang/Object;IC)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setChar
  (JNIEnv *, jclass, jobject, jint, jchar);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setDouble
 * Signature: (Ljava/lang/Object;ID)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setDouble
  (JNIEnv *, jclass, jobject, jint, jdouble);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setFloat
 * Signature: (Ljava/lang/Object;IF)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setFloat
  (JNIEnv *, jclass, jobject, jint, jfloat);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setInt
 * Signature: (Ljava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setInt
  (JNIEnv *, jclass, jobject, jint, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setLong
 * Signature: (Ljava/lang/Object;IJ)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setLong
  (JNIEnv *, jclass, jobject, jint, jlong);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setShort
 * Signature: (Ljava/lang/Object;IS)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setShort
  (JNIEnv *, jclass, jobject, jint, jshort);
#endif

}

