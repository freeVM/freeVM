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
 * THE FILE HAS BEEN AUTOGENERATED BY IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class org.apache.harmony.misc.accessors.ObjectAccessor */

#ifndef _ORG_APACHE_HARMONY_MISC_ACCESSORS_OBJECTACCESSOR_H
#define _ORG_APACHE_HARMONY_MISC_ACCESSORS_OBJECTACCESSOR_H

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getObject(Ljava/lang/Object;J)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getObject(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getBoolean(Ljava/lang/Object;J)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getBoolean(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getByte(Ljava/lang/Object;J)B
 */
JNIEXPORT jbyte JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getByte(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getShort(Ljava/lang/Object;J)S
 */
JNIEXPORT jshort JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getShort(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getChar(Ljava/lang/Object;J)C
 */
JNIEXPORT jchar JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getChar(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getInt(Ljava/lang/Object;J)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getInt(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getLong(Ljava/lang/Object;J)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getLong(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getFloat(Ljava/lang/Object;J)F
 */
JNIEXPORT jfloat JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getFloat(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getDouble(Ljava/lang/Object;J)D
 */
JNIEXPORT jdouble JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getDouble(JNIEnv *, jobject, 
    jobject, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.newInstance(Ljava/lang/Class;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_newInstance__Ljava_lang_Class_2(JNIEnv *, jobject, 
    jclass);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.newInstance(Ljava/lang/Class;J[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_newInstance__Ljava_lang_Class_2J_3Ljava_lang_Object_2(JNIEnv *, jobject, 
    jclass, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setBoolean(Ljava/lang/Object;JZ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setBoolean(JNIEnv *, jobject, 
    jobject, jlong, jboolean);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setByte(Ljava/lang/Object;JB)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setByte(JNIEnv *, jobject, 
    jobject, jlong, jbyte);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setChar(Ljava/lang/Object;JC)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setChar(JNIEnv *, jobject, 
    jobject, jlong, jchar);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setDouble(Ljava/lang/Object;JD)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setDouble(JNIEnv *, jobject, 
    jobject, jlong, jdouble);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setFloat(Ljava/lang/Object;JF)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setFloat(JNIEnv *, jobject, 
    jobject, jlong, jfloat);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setInt(Ljava/lang/Object;JI)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setInt(JNIEnv *, jobject, 
    jobject, jlong, jint);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setLong(Ljava/lang/Object;JJ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setLong(JNIEnv *, jobject, 
    jobject, jlong, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setShort(Ljava/lang/Object;JS)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setShort(JNIEnv *, jobject, 
    jobject, jlong, jshort);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.hasStaticInitializer(Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_hasStaticInitializer(JNIEnv *, jobject, 
    jclass);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.monitorEnter(Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_monitorEnter(JNIEnv *, jobject, 
    jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.monitorExit(Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_monitorExit(JNIEnv *, jobject, 
    jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticBoolean(Ljava/lang/Class;J)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticBoolean(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticBoolean(Ljava/lang/Class;JZ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticBoolean(JNIEnv *, jobject, 
    jclass, jlong, jboolean);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticByte(Ljava/lang/Class;J)B
 */
JNIEXPORT jbyte JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticByte(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticByte(Ljava/lang/Class;JB)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticByte(JNIEnv *, jobject, 
    jclass, jlong, jbyte);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticChar(Ljava/lang/Class;J)C
 */
JNIEXPORT jchar JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticChar(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticChar(Ljava/lang/Class;JC)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticChar(JNIEnv *, jobject, 
    jclass, jlong, jchar);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticShort(Ljava/lang/Class;J)S
 */
JNIEXPORT jshort JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticShort(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticShort(Ljava/lang/Class;JS)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticShort(JNIEnv *, jobject, 
    jclass, jlong, jshort);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticInt(Ljava/lang/Class;J)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticInt(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticInt(Ljava/lang/Class;JI)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticInt(JNIEnv *, jobject, 
    jclass, jlong, jint);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticLong(Ljava/lang/Class;J)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticLong(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticLong(Ljava/lang/Class;JJ)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticLong(JNIEnv *, jobject, 
    jclass, jlong, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticFloat(Ljava/lang/Class;J)F
 */
JNIEXPORT jfloat JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticFloat(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticFloat(Ljava/lang/Class;JF)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticFloat(JNIEnv *, jobject, 
    jclass, jlong, jfloat);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticDouble(Ljava/lang/Class;J)D
 */
JNIEXPORT jdouble JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticDouble(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticDouble(Ljava/lang/Class;JD)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticDouble(JNIEnv *, jobject, 
    jclass, jlong, jdouble);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticObject(Ljava/lang/Class;J)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticObject(JNIEnv *, jobject, 
    jclass, jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setObject(Ljava/lang/Object;JLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setObject(JNIEnv *, jobject, 
    jobject, jlong, jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.setStaticObject(Ljava/lang/Class;JLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_setStaticObject(JNIEnv *, jobject, 
    jclass, jlong, jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getFieldID(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getFieldID__Ljava_lang_Class_2Ljava_lang_String_2Ljava_lang_String_2(JNIEnv *, jobject, 
    jclass, jstring, jstring);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getFieldID(Ljava/lang/reflect/Field;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getFieldID__Ljava_lang_reflect_Field_2(JNIEnv *, jobject, 
    jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticFieldID(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticFieldID(JNIEnv *, jobject, 
    jclass, jstring, jstring);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getStaticMethodID0(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getStaticMethodID0(JNIEnv *, jclass, 
    jclass, jstring, jstring);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getMethodID0(Ljava/lang/reflect/Member;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getMethodID0__Ljava_lang_reflect_Member_2(JNIEnv *, jclass, 
    jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getMethodID0(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getMethodID0__Ljava_lang_Class_2Ljava_lang_String_2Ljava_lang_String_2(JNIEnv *, jclass, 
    jclass, jstring, jstring);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeStaticVoid(Ljava/lang/Class;J[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeStaticVoid(JNIEnv *, jobject, 
    jclass, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeVirtualVoid(Ljava/lang/Object;J[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeVirtualVoid(JNIEnv *, jobject, 
    jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeNonVirtualVoid(Ljava/lang/Class;Ljava/lang/Object;J[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeNonVirtualVoid(JNIEnv *, jobject, 
    jclass, jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeStaticObject(Ljava/lang/Class;J[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeStaticObject(JNIEnv *, jobject, 
    jclass, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeVirtualObject(Ljava/lang/Object;J[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeVirtualObject(JNIEnv *, jobject, 
    jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeNonVirtualObject(Ljava/lang/Class;Ljava/lang/Object;J[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeNonVirtualObject(JNIEnv *, jobject, 
    jclass, jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeStaticLong(Ljava/lang/Class;J[Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeStaticLong(JNIEnv *, jobject, 
    jclass, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeVirtualLong(Ljava/lang/Object;J[Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeVirtualLong(JNIEnv *, jobject, 
    jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.invokeNonVirtualLong(Ljava/lang/Class;Ljava/lang/Object;J[Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_invokeNonVirtualLong(JNIEnv *, jobject, 
    jclass, jobject, jlong, jobjectArray);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.allocateObject(Ljava/lang/Class;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_allocateObject(JNIEnv *, jobject, 
    jclass);


/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getGlobalReference(Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getGlobalReference(JNIEnv *, jobject, 
    jobject);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.releaseGlobalReference(J)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_releaseGlobalReference(JNIEnv *, jobject, 
    jlong);

/*
 * Method: org.apache.harmony.misc.accessors.ObjectAccessor.getObjectFromReference(J)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_misc_accessors_ObjectAccessor_getObjectFromReference(JNIEnv *, jobject, 
    jlong);

#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_MISC_ACCESSORS_OBJECTACCESSOR_H */

