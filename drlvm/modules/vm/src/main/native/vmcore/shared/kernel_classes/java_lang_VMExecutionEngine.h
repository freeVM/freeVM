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

/** 
 * @author Euguene Ostrovsky
 */  

/*
 * THE FILE HAS BEEN AUTOGENERATED BY INTEL IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class java.lang.VMExecutionEngine */

#ifndef _JAVA_LANG_VMEXECUTIONENGINE_H
#define _JAVA_LANG_VMEXECUTIONENGINE_H

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
/*
 * Method: java.lang.VMExecutionEngine.exit(IZ[Ljava/lang/Runnable;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMExecutionEngine_exit
  (JNIEnv *, jclass, jint, jboolean, jobjectArray);

/*
 * Method: java.lang.VMExecutionEngine.getAssertionStatus(Ljava/lang/Class;ZI)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMExecutionEngine_getAssertionStatus
  (JNIEnv *, jclass, jclass, jboolean, jint);

/*
 * Method: java.lang.VMExecutionEngine.getAvailableProcessors()I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMExecutionEngine_getAvailableProcessors
  (JNIEnv *, jclass);

/*
 * Method: java.lang.VMExecutionEngine.getProperties()Ljava/util/Properties;
 */
JNIEXPORT jobject JNICALL Java_java_lang_VMExecutionEngine_getProperties
  (JNIEnv *, jclass);

/*
 * Method: java.lang.VMExecutionEngine.loadLibrary()
 */
JNIEXPORT void JNICALL Java_java_lang_VMExecutionEngine_loadLibrary
  (JNIEnv *, jclass, jstring);

/*
 * Method: java.lang.VMExecutionEngine.traceInstructions(Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMExecutionEngine_traceInstructions
  (JNIEnv *, jclass, jboolean);

/*
 * Method: java.lang.VMExecutionEngine.traceMethodCalls(Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMExecutionEngine_traceMethodCalls
  (JNIEnv *, jclass, jboolean);



/*
* Class:     java_lang_VMExecutionEngine
* Method:    currentTimeMillis
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_java_lang_VMExecutionEngine_currentTimeMillis
(JNIEnv *, jclass);

/*
* Class:     java_lang_VMExecutionEngine
* Method:    nanoTime
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_java_lang_VMExecutionEngine_nanoTime
(JNIEnv *, jclass);

/*
* Class:     java_lang_VMExecutionEngine
* Method:    mapLibraryName
* Signature: (Ljava/lang/String;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_java_lang_VMExecutionEngine_mapLibraryName
(JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif

#endif /* _JAVA_LANG_VMEXECUTIONENGINE_H */

