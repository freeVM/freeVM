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
 * THE FILE HAS BEEN AUTOGENERATED BY THE IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class org.apache.harmony.lang.management.ThreadMXBeanImpl */

#ifndef _ORG_APACHE_HARMONY_LANG_MANAGEMENT_THREADMXBEANIMPL_H
#define _ORG_APACHE_HARMONY_LANG_MANAGEMENT_THREADMXBEANIMPL_H

#ifdef __cplusplus
extern "C" {
#endif


/* Static final fields */

#undef org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_NONE
#define org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_NONE 0L

#undef org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_INTERRUPT
#define org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_INTERRUPT 52L

#undef org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_ILLEGAL_STATE
#define org_apache_harmony_lang_management_ThreadMXBeanImpl_TM_ERROR_ILLEGAL_STATE 118L


/* Native methods */

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.findMonitorDeadlockedThreadsImpl()[J
 */
JNIEXPORT jlongArray JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_findMonitorDeadlockedThreadsImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getAllThreadIdsImpl()[J
 */
JNIEXPORT jlongArray JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getAllThreadIdsImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getDaemonThreadCountImpl()I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getDaemonThreadCountImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getPeakThreadCountImpl()I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getPeakThreadCountImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadCountImpl()I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadCountImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadCpuTimeImpl(J)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadCpuTimeImpl(JNIEnv *, jobject,
    jlong);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadByIdImpl(J)Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadByIdImpl(JNIEnv *, jobject,
    jlong);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getObjectThreadIsBlockedOnImpl(Ljava/lang/Thread;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getObjectThreadIsBlockedOnImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadOwningObjectImpl(Ljava/lang/Object;)Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadOwningObjectImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isSuspendedImpl(Ljava/lang/Thread;)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isSuspendedImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadWaitedCountImpl(Ljava/lang/Thread;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadWaitedCountImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadWaitedTimeImpl(Ljava/lang/Thread;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadWaitedTimeImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadBlockedTimeImpl(Ljava/lang/Thread;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadBlockedTimeImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadBlockedCountImpl(Ljava/lang/Thread;)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadBlockedCountImpl(JNIEnv *, jobject,
    jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.createThreadInfoImpl(JLjava/lang/String;Ljava/lang/Thread$State;ZZJJJJLjava/lang/String;JLjava/lang/String;[Ljava/lang/StackTraceElement;)Ljava/lang/management/ThreadInfo;
 */
JNIEXPORT jobject JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_createThreadInfoImpl(JNIEnv *, jobject,
    jlong, jstring, jobject, jboolean, jboolean, jlong, jlong, jlong, jlong, jstring, jlong, jstring, jobjectArray);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getThreadUserTimeImpl(J)J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getThreadUserTimeImpl(JNIEnv *, jobject,
    jlong);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.getTotalStartedThreadCountImpl()J
 */
JNIEXPORT jlong JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_getTotalStartedThreadCountImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isCurrentThreadCpuTimeSupportedImpl()Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isCurrentThreadCpuTimeSupportedImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isThreadContentionMonitoringEnabledImpl()Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isThreadContentionMonitoringEnabledImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isThreadContentionMonitoringSupportedImpl()Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isThreadContentionMonitoringSupportedImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isThreadCpuTimeEnabledImpl()Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isThreadCpuTimeEnabledImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.isThreadCpuTimeSupportedImpl()Z
 */
JNIEXPORT jboolean JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_isThreadCpuTimeSupportedImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.resetPeakThreadCountImpl()V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_resetPeakThreadCountImpl(JNIEnv *, jobject);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.setThreadContentionMonitoringEnabledImpl(Z)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_setThreadContentionMonitoringEnabledImpl(JNIEnv *, jobject,
    jboolean);

/*
 * Method: org.apache.harmony.lang.management.ThreadMXBeanImpl.setThreadCpuTimeEnabledImpl(Z)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_lang_management_ThreadMXBeanImpl_setThreadCpuTimeEnabledImpl(JNIEnv *, jobject,
    jboolean);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_LANG_MANAGEMENT_THREADMXBEANIMPL_H */

