/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Andrey Chernyshev
 * @version $Revision: 1.1.2.1.4.4 $
 */  

#define LOG_DOMAIN "kernel"
#include "cxxlog.h"

#include "java_lang_VMThreadManager.h"
#include "open/ti_thread.h"
#include "open/hythread_ext.h"
#include "open/jthread.h"
#include "open/ti_thread.h"
#include "open/thread_externals.h"

/*
 * Class:     java_lang_VMThreadManager
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_VMThreadManager_currentThread
  (JNIEnv * UNREF jenv, jclass clazz)
{
    return jthread_self();
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    holdsLock
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThreadManager_holdsLock
  (JNIEnv * UNREF jenv, jclass clazz, jobject monitor)
{
	return jthread_holds_lock(jthread_self(), monitor);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    interrupt
 * Signature: (Ljava/lang/Thread;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_interrupt
  (JNIEnv * UNREF jenv, jclass clazz, jobject jthread)
{
    return jthread_interrupt(jthread);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    isInterrupted
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThreadManager_isInterrupted__
  (JNIEnv * UNREF jenv, jclass clazz)
{
    return jthread_clear_interrupted(jthread_self());
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    isInterrupted
 * Signature: (Ljava/lang/Thread;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThreadManager_isInterrupted__Ljava_lang_Thread_2
  (JNIEnv * UNREF jenv, jclass clazz, jobject thread)
{
    return jthread_is_interrupted(thread);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    notify
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_notify
  (JNIEnv * UNREF jenv, jclass clazz, jobject monitor)
{
    return jthread_monitor_notify(monitor);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    notifyAll
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_notifyAll
  (JNIEnv * UNREF jenv, jclass clazz, jobject monitor)
{
    return jthread_monitor_notify_all(monitor);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    resume
 * Signature: (Ljava/lang/Thread;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_resume
  (JNIEnv * UNREF jenv, jclass clazz, jobject thread)
{
    return jthread_resume(thread);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    setPriority
 * Signature: (Ljava/lang/Thread;I)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_setPriority
  (JNIEnv * UNREF jenv, jclass clazz, jobject UNREF thread, jint UNREF priority)
{
    return jthread_set_priority(thread, priority);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    sleep
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_sleep
  (JNIEnv * UNREF jenv, jclass clazz, jlong millis, jint nanos)
{
    return jthread_sleep(millis, nanos); 
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    init
 * Signature: (JI)V
 */
JNIEXPORT jlong JNICALL Java_java_lang_VMThreadManager_init
  (JNIEnv *jenv, jclass clazz, jobject thread, jobject ref, jlong oldThread)
{
    return jthread_thread_init(NULL, jenv, thread, ref, oldThread);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    start
 * Signature: (Ljava/lang/Thread;JZI)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_start
  (JNIEnv *jenv, jclass clazz, jobject thread, jlong stackSize, jboolean daemon, jint priority)
{
    jthread_threadattr_t attrs;
    
    attrs.daemon = daemon;
    attrs.priority = priority;
    attrs.stacksize = stackSize > 40000000? 0:(jint)stackSize;
    return (jint)jthread_create(jenv, thread, &attrs);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    stop
 * Signature: (Ljava/lang/Thread;Ljava/lang/Throwable;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_stop
  (JNIEnv *env, jclass clazz, jobject UNREF thread, jthrowable UNREF threadDeathException)
{
    return jthread_exception_stop(thread, threadDeathException);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    suspend
 * Signature: (Ljava/lang/Thread;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_suspend
  (JNIEnv * UNREF jenv, jclass clazz, jobject jthread)
{
    return jthread_suspend(jthread);
}


/*
 * Class:     java_lang_VMThreadManager
 * Method:    wait
 * Signature: (Ljava/lang/Object;JI)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_wait
  (JNIEnv *env, jclass clazz, jobject monitor, jlong millis, jint UNREF nanos)
{
    return jthread_monitor_timed_wait(monitor, millis, nanos);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    yield
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_yield
  (JNIEnv * UNREF jenv, jclass clazz)
{
    return jthread_yield();
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    attach
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThreadManager_attach
  (JNIEnv * UNREF jenv, jclass clazz, jobject java_thread)
{
    jthread_attach(jenv, java_thread);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    isAlive
 * Signature: (Ljava/lang/Thread;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThreadManager_isAlive
  (JNIEnv *jenv, jclass clazz, jobject thread)
{
    hythread_t tm_native_thread;

    tm_native_thread = (hythread_t )vm_jthread_get_tm_data((jthread)thread);
    assert(tm_native_thread);
    if (hythread_is_alive(tm_native_thread))
    { 
        printf("isAlive\n");
        return true;
    }
        printf ("isnot\n");
    return false;
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    join
 * Signature: (Ljava/lang/Thread;JI)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_join
  (JNIEnv * UNREF jenv, jclass clazz, jobject thread, jlong millis, jint nanos)
{
    return jthread_timed_join(thread, millis, nanos);
}

/*
 * Class:     java_lang_VMThreadManager
 * Method:    getState
 * Signature: (Ljava/lang/Thread;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_getState
  (JNIEnv * UNREF jenv, jclass clazz, jobject jthread)
{
    jint state;	
    int stat;
    
    stat = jthread_get_state(jthread, &state);
    assert(stat == TM_ERROR_NONE);
    return state;
}


/*
 * Class:     java_lang_VMThreadManager
 * Method:    yield
 * Signature: ()I
 */

/*
 * ????
JNIEXPORT jint JNICALL Java_java_lang_VMThreadManager_initVMThreadManager
  (JNIEnv * UNREF jenv, jclass clazz)
{
    return hythread_init();
}
*/
