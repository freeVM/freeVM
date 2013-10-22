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
 *  @author Artem Aliev
 *  @version $Revision: 1.1.2.1.4.4 $
 */  

/*
 * THE FILE HAS BEEN AUTOGENERATED BY INTEL IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class java.util.concurrent.locks.LockSupport */

#ifndef _JAVA_UTIL_CONCURRENT_LOCKS_LOCKSUPPORT_H
#define _JAVA_UTIL_CONCURRENT_LOCKS_LOCKSUPPORT_H

#ifdef __cplusplus
extern "C" {
#endif


/* Native methods */

/*
 * Method: java.util.concurrent.locks.LockSupport.unpark(Ljava/lang/Thread;)V
 */
JNIEXPORT void JNICALL
Java_java_util_concurrent_locks_LockSupport_unpark(JNIEnv *, jclass, 
    jobject);

/*
 * Method: java.util.concurrent.locks.LockSupport.park()V
 */
JNIEXPORT void JNICALL
Java_java_util_concurrent_locks_LockSupport_park(JNIEnv *, jclass);

/*
 * Method: java.util.concurrent.locks.LockSupport.parkNanos(J)V
 */
JNIEXPORT void JNICALL
Java_java_util_concurrent_locks_LockSupport_parkNanos(JNIEnv *, jclass, 
    jlong);

/*
 * Method: java.util.concurrent.locks.LockSupport.parkUntil(J)V
 */
JNIEXPORT void JNICALL
Java_java_util_concurrent_locks_LockSupport_parkUntil(JNIEnv *, jclass, 
    jlong);


#ifdef __cplusplus
}
#endif

#endif /* _JAVA_UTIL_CONCURRENT_LOCKS_LOCKSUPPORT_H */
