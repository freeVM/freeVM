/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file
 * @brief Extended Treading and synchronization support
 * @details
 * Thread Manager native interface. Provides basic capablitites for managing native threads in the system.
 *
 * <p>
 * <h2>Overview</h2>
 * The Thread Manager (TM) component provides various threading functionality for VM and class libraries code.
 * provides support for threading inside the virtual machine and for class libraries. 
 * The implementation of thread management uses the black-box approach exposing two interfaces: Java and native. 
 * The whole component is divided into two layers: the middle layer of native threading with the layer of 
 * Java threading on top. The implementation of Thread manager is based solely on the Apache Portable Runtime (APR) layer. 
 * <p>
 * The top layer provides the following functionality (see jthread, thread_ti.h): 
 * <ul>
 * <li> Maps Java threads onto OS native threads
 * <li> Support kernel classes support 
 * <li> Support JVMTI
 * </ul>
 * The middle layer does the following (see hythread.h, hythread_ext.h): 
 * <ul>
 * <li> Provides API for native threading
 * <li> Wrapping 223shield224 on top of porting layer
 * <li> Provides support for safe suspension and interrupt
 * <li> Additional functionality that missed in porting layer
 * </ul>
 * <p> 
 * 
 * <h2>Interaction</h2>
 * The following components are primary customers of the Thread Manager:
 * <ul>
 * <li> Kernel classes 
 * <li> Garbage collector
 * <li> java.util.concurrent classes
 * <li> java.lang.management classes
 * <li> JVMTI
 * </ul>
 * These components interact with different parts of thread management: some work with Java threads and objects, 
 * while others interact with the middle layer of native threading. Whenever the component operates with Java 
 * objects and threads, it would call the top (Java) interface functions. Otherwise, it would call the middle (native) 
 * interface functions.
 * <h2>Safe suspension</h2>
 * One of the key features of thread management in the VM is the safe suspension functionality. 
 * The purpose of the safe suspension mechanism is mostly to ensure that:
 * <ul>
 * <li> Suspended thread can be safely explored by the Garbage Collector while enumerating live references;
 * <li> Suspended thread is guaranteed to do not hold any system-critical locks which can be requested by the other parts of VM, 
 * such as the locks associated with the native memory heap.
 * </ul>
 * At every time moment, thread can be in one of three possible states:
 * <ul>
 * <li> Safe region. A period of time during thread execution when the thread can be safely suspended. This would typically be a region of a
 native C code where java objects and stack are not changed. An example is native file I/O call which reads a big amount of data from disk.
 * <li> Unsafe region. A period of time during thread execution when the code is in process of changing java objects or impacts java stack, 
such as parts of the JITT-ed java code or JNI function calls. Suspending a thread in that region would typically be unsafe.
 * <li> Safe point. A single point during the thread execution time when the thread can safely suspended. For example, JIT may want to injec
t the safe points in the Java code between the selected chunks of the assembly code, those size is determined in terms of performance balanc
e between safe point function call overhead and suspension time overhead.
 * </ul>
 * Each thread managed by TM has the following fields:
 * <ul>
 * <li>    safe_region 226 boolean flag which reports whether suspend is safe at this moment or not.
 * <li>    request 226 integer indicating if any request for suspension were made for this thread
 * <li>    suspend_count 226 integer indicating how many requests for suspension were made for this thread
 * <li>    safe_region_event 226 event indicating that thread has reached safe region
 * <li>    resume_event 226 event indicating that thread needs to be awakened
 * </ul>
 * The suspension algorithm typically invokes two threads 226 223suspender224 thread and 223suspendee224 thread.
 * A typical example is than "suspender" is GC thread, and the "suspendee" is Java thread. 
 * The safe thread suspension algorithm works as follows:
 * <pre>
 *   1.  GC thread invokes thread_supend() method of a thread manager which does the following:
 *       a.  If  Java thread was already requested to suspend, increase the suspend_count count and return;
 *       b.  Increase suspend_count for Java thread;
 *       c.  If Java thread is currently in unsafe region, wait while it reaches the safe region.
 *   2.  GC thread, after completing the enumeration-related activities, calls the resume_thread() method which does the following:
 *       a.  If suspend_count was previously set, decrease the number of suspend requests;
 *       b.  If the number of suspend requests reaches zero, notifies the Java thread that it can wake up now.
 *   3.  A Java thread may reach safe point, which is denoted by calling the safe_point() method in the Java thread. 
 *       The safe_point() method does the following:
 *       a.  If there was request for suspension then: 
 *           i.  notify GC thread that it can proceed with enumeration activities
 *           ii. wait for the resume notification to come from the GC thread
 *   4.  A Java thread may enter the safe region, which is denoted by calling the suspend_enable() method in the Java thread. 
 *       The suspend_enable() method does the following:
 *       a.  Sets the flag safe_region to true;
 *       b.  If there was a request for suspension, notifies the GC thread that Java thread has reached safe region so GC may 
 *           proceed with enumeration activities
 *   5.  A Java thread may leave the safe region, which is denoted by calling the suspend_disable() method in the Java thread. 
 *       The suspend_disable() method does the following:
 *       a.  Sets the flag safe_region to false;
 *       b.  Calls the safe_point() method.
 * </pre>
 *
 * For more detailes see thread manager component documentation located at vm/thread/doc/ThreadManager.htm
 *
 */

#if !defined(HYTHREAD_EXT_H)
#define HYTHREAD_EXT_H

#include "open/types.h"

#if defined(__cplusplus)
extern "C" {
#endif

#include "open/hythread.h"

#include <open/types.h>
#include <apr_pools.h>
#include <apr_thread_mutex.h>
#include <apr_thread_cond.h>
#include <apr_thread_rwlock.h>
#include <apr_portable.h>

#include <assert.h>
#include "apr_thread_ext.h"

//@{
/**
 * Opaque structures
 */
//@{

#ifdef __linux__
#include <pthread.h>
#define hymutex_t pthread_mutex_t
#define hycond_t pthread_cond_t
#endif // __linux__

#ifdef _WIN32
#define hymutex_t CRITICAL_SECTION
#define hycond_t struct HyCond
#include "hycond_win.h"
#endif // _WIN32

#if !defined (_IPF_)
//use lock reservation
#define LOCK_RESERVATION
#endif // !defined (_IPF_)

typedef struct HyLatch *hylatch_t;

typedef struct HyThread *hythread_iterator_t;
typedef struct HyThreadLibrary *hythread_library_t;

typedef U_32  hythread_thin_monitor_t;

typedef void (*hythread_event_callback_proc)(void);

 
//@}
/** @name Thread Manager initialization / shutdown
 */
//@{
//temporary, should be static for the file containing dinamic library 
//initialization
////

IDATA VMCALL hythread_global_lock();
IDATA VMCALL hythread_global_unlock();
void VMCALL hythread_init(hythread_library_t lib);
void VMCALL hythread_shutdown();
IDATA VMCALL hythread_lib_create(hythread_library_t * lib);
void VMCALL hythread_lib_destroy(hythread_library_t lib);
hythread_group_t VMCALL get_java_thread_group(void);

//@}
/** @name  Basic manipulation 
 */
//@{

IDATA VMCALL hythread_attach_ex(hythread_t *handle, hythread_library_t lib);
IDATA VMCALL hythread_attach_to_group(hythread_t *handle, hythread_library_t lib, hythread_group_t group);
IDATA hythread_create_with_group(hythread_t *ret_thread, hythread_group_t group, UDATA stacksize, UDATA priority, UDATA suspend, hythread_entrypoint_t func, void *data);
UDATA VMCALL hythread_clear_interrupted_other(hythread_t thread);
IDATA VMCALL hythread_join(hythread_t t);
IDATA VMCALL hythread_join_timed(hythread_t t, I_64 millis, IDATA nanos);
IDATA VMCALL hythread_join_interruptable(hythread_t t, I_64 millis, IDATA nanos);
IDATA VMCALL hythread_get_self_id();
IDATA VMCALL hythread_get_id(hythread_t t);
hythread_t VMCALL hythread_get_thread(IDATA id);
IDATA VMCALL hythread_struct_init(hythread_t *ret_thread);
IDATA VMCALL hythread_cancel_all(hythread_group_t group);
IDATA hythread_group_create(hythread_group_t *group);
IDATA VMCALL hythread_group_release(hythread_group_t group);
IDATA VMCALL hythread_group_get_list(hythread_group_t **list, int* size);

UDATA VMCALL hythread_tls_get_offset(hythread_tls_key_t key);
UDATA VMCALL hythread_tls_get_request_offset();
UDATA VMCALL hythread_get_thread_times(hythread_t thread, int64* pkernel, int64* puser);
UDATA hythread_get_thread_stacksize(hythread_t thread);
UDATA VMCALL hythread_uses_fast_tls(void);
IDATA VMCALL hythread_get_hythread_offset_in_tls(void);

IDATA VMCALL hythread_thread_lock(hythread_t thread);
IDATA VMCALL hythread_thread_unlock(hythread_t thread);
IDATA VMCALL hythread_get_state(hythread_t thread);
IDATA VMCALL hythread_set_state(hythread_t thread, IDATA state);
int VMCALL hythread_reset_suspend_disable();
void VMCALL hythread_set_suspend_disable(int count);
int VMCALL hythread_is_fat_lock(hythread_thin_monitor_t lockword);
hythread_monitor_t VMCALL hythread_inflate_lock(hythread_thin_monitor_t *lockword_ptr);
IDATA VMCALL hythread_owns_thin_lock(hythread_t thread, hythread_thin_monitor_t lockword);
IDATA VMCALL hythread_unreserve_lock(hythread_thin_monitor_t *lockword_ptr);
IDATA VMCALL hythread_get_thread_id_offset();
IDATA VMCALL hythread_set_thread_stop_callback(hythread_t thread, hythread_event_callback_proc stop_callback);
IDATA VMCALL hythread_wait_for_nondaemon_threads(hythread_t thread, IDATA threads_to_keep);
IDATA VMCALL hythread_increase_nondaemon_threads_count(hythread_t thread);
IDATA VMCALL hythread_decrease_nondaemon_threads_count(hythread_t thread, IDATA threads_to_keep);

//@}
/** @name Conditional variable
 */
//@{

IDATA VMCALL hycond_create (hycond_t *cond);
IDATA VMCALL hycond_wait (hycond_t *cond, hymutex_t *mutex);
IDATA VMCALL hycond_wait_timed (hycond_t *cond, hymutex_t *mutex, I_64 millis, IDATA nanos);
IDATA VMCALL hycond_wait_timed_raw(hycond_t * cond, hymutex_t * mutex, I_64 ms, IDATA nano);
IDATA VMCALL hycond_wait_interruptable (hycond_t *cond, hymutex_t *mutex, I_64 millis, IDATA nanos);
IDATA VMCALL hycond_notify (hycond_t *cond);
IDATA VMCALL hycond_notify_all (hycond_t *cond);
IDATA VMCALL hycond_destroy (hycond_t *cond);

//@}
/** @name Safe suspension support
 */
//@{

hy_inline IDATA VMCALL hythread_is_suspend_enabled();
hy_inline void VMCALL hythread_suspend_enable();
hy_inline void VMCALL hythread_suspend_disable();
void hythread_safe_point();
void hythread_safe_point_other(hythread_t thread);
void VMCALL hythread_exception_safe_point();
IDATA VMCALL hythread_suspend_other(hythread_t thread);

IDATA VMCALL hythread_set_safepoint_callback(hythread_t thread, hythread_event_callback_proc callback);
IDATA VMCALL hythread_suspend_all(hythread_iterator_t *t, hythread_group_t group);
IDATA VMCALL hythread_resume_all(hythread_group_t  group);

//@}
/** @name Latch
 */
//@{

IDATA VMCALL hylatch_create(hylatch_t *latch, IDATA count);
IDATA VMCALL hylatch_wait(hylatch_t latch);
IDATA VMCALL hylatch_wait_timed(hylatch_t latch, I_64 ms, IDATA nano);
IDATA VMCALL hylatch_wait_interruptable(hylatch_t latch, I_64 ms, IDATA nano);
IDATA VMCALL hylatch_set(hylatch_t latch, IDATA count);
IDATA VMCALL hylatch_count_down(hylatch_t latch);
IDATA VMCALL hylatch_get_count(IDATA *count, hylatch_t latch);
IDATA VMCALL hylatch_destroy(hylatch_t latch);

//@}
/** @name Thread iterator support
 */
//@{

hythread_iterator_t VMCALL hythread_iterator_create(hythread_group_t group);
IDATA VMCALL hythread_iterator_release(hythread_iterator_t *it);
IDATA VMCALL hythread_iterator_reset(hythread_iterator_t *it);
hythread_t VMCALL hythread_iterator_next(hythread_iterator_t *it);
IDATA VMCALL hythread_iterator_has_next(hythread_iterator_t it);
IDATA VMCALL hythread_iterator_size(hythread_iterator_t iterator);

//@}
/** @name Semaphore
 */
//@{

IDATA hysem_create(hysem_t *sem, UDATA initial_count, UDATA max_count);
IDATA VMCALL hysem_wait_timed(hysem_t sem, I_64 ms, IDATA nano);
IDATA VMCALL hysem_wait_interruptable(hysem_t sem, I_64 ms, IDATA nano);
IDATA VMCALL hysem_getvalue(IDATA *count, hysem_t sem);
IDATA hysem_set(hysem_t sem, IDATA count);

//@}
/** @name Mutex
 */
//@{

IDATA hymutex_create (hymutex_t *mutex, UDATA flags);
IDATA hymutex_lock(hymutex_t *mutex);
IDATA hymutex_trylock (hymutex_t *mutex);
IDATA hymutex_unlock (hymutex_t *mutex);
IDATA hymutex_destroy (hymutex_t *mutex);

//@}
/** @name Thin monitors support
 */
//@{

IDATA VMCALL hythread_thin_monitor_create(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_enter(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_try_enter(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_exit(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_wait(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_wait_timed(hythread_thin_monitor_t *lockword_ptr, I_64 ms, IDATA nano);
IDATA VMCALL hythread_thin_monitor_wait_interruptable(hythread_thin_monitor_t *lockword_ptr, I_64 ms, IDATA nano);
IDATA VMCALL hythread_thin_monitor_notify(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_notify_all(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_destroy(hythread_thin_monitor_t *lockword);
hythread_t VMCALL hythread_thin_monitor_get_owner(hythread_thin_monitor_t *lockword);
IDATA VMCALL hythread_thin_monitor_get_recursion(hythread_thin_monitor_t *lockword);

void VMCALL hythread_native_resource_is_live(U_32);
void VMCALL hythread_reclaim_resources();

IDATA VMCALL hythread_monitor_interrupt_wait(hythread_monitor_t mon_ptr,
					     hythread_t thread);

IDATA VMCALL hythread_monitor_interrupt_wait(hythread_monitor_t mon_ptr,
					     hythread_t thread);

//@}
/** @name State query
 */
//@{

int VMCALL hythread_is_alive(hythread_t thread) ;
int VMCALL hythread_is_terminated(hythread_t thread) ;
int VMCALL hythread_is_runnable(hythread_t thread) ;
int VMCALL hythread_is_blocked_on_monitor_enter(hythread_t thread) ;
int VMCALL hythread_is_waiting(hythread_t thread) ;
int VMCALL hythread_is_waiting_indefinitely(hythread_t thread) ;
int VMCALL hythread_is_waiting_with_timeout(hythread_t thread) ;
int VMCALL hythread_is_sleeping(hythread_t thread) ;
int VMCALL hythread_is_in_monitor_wait(hythread_t thread) ;
int VMCALL hythread_is_parked(hythread_t thread) ;
int VMCALL hythread_is_suspended(hythread_t thread) ;
int VMCALL hythread_is_interrupted(hythread_t thread) ;
int VMCALL hythread_is_in_native(hythread_t thread) ;
int VMCALL hythread_is_daemon(hythread_t thread) ;




// inline functions declarations


/**
 * Returns non-zero if thread is suspended.
 */
hy_inline IDATA VMCALL hythread_is_suspend_enabled(){
    return ((HyThread_public *)tm_self_tls)->disable_count == 0;
}


/**
 * Denotes the beginning of the code region where safe suspension is possible.
 *
 * The method decreases the disable_count field. The disable_count could be
 * recursive, so safe suspension region is enabled on value 0.
 *
 * <p>
 * A thread marks itself with functions hythread_suspend_enable()
 * and hythread_suspend_disable() in order to denote a safe region of code.
 * A thread may also call hythread_safe_point() method to denote a selected
 * point where safe suspension is possible.
 */
hy_inline void VMCALL hythread_suspend_enable() {
    assert(!hythread_is_suspend_enabled());

#ifdef FS14_TLS_USE
    // the macros could work for WIN32
    __asm {
        mov eax, fs:[0x14]
        dec[eax] HyThread_public.disable_count
    }
#else
    {
        register hythread_t thread = tm_self_tls;
        ((HyThread_public *)thread)->disable_count--;
    }
#endif
}

/**
 * Denotes the end of the code region where safe suspension was possible.
 *
 * The method increases the disable_count field. The disable_count could be
 * recursive, so safe suspension region is enabled on value 0.
 * If there was a suspension request set for this thread, the method invokes
 * hythread_safe_point().
 * <p>
 * A thread marks itself with functions hythread_suspend_enable()
 * and hythread_suspend_disable() in order to denote a safe region of code.
 * A thread may also call hythread_safe_point() method to denote a selected
 * point where safe suspension is possible.
 */
hy_inline void VMCALL hythread_suspend_disable()
{
    register hythread_t thread;

    // Check that current thread is in default thread group.
    // Justification: GC suspends and enumerates threads from
    // default group only.
    assert(((HyThread_public *)hythread_self())->group == get_java_thread_group());

#ifdef FS14_TLS_USE
    // the macros could work for WIN32
    __asm {
        mov eax, fs:[0x14]
        inc[eax] HyThread_public.disable_count
        mov eax,[eax] HyThread_public.request
        test eax, eax
        jnz suspended
    }
    return;

  suspended:
    thread = tm_self_tls;

#else
    thread = tm_self_tls;
    ((HyThread_public *)thread)->disable_count++;
#endif

    if (((HyThread_public *)thread)->request && 
	((HyThread_public *)thread)->disable_count == 1) {
        // enter to safe point if suspend request was set
        // and suspend disable was made a moment ago
        // (it's a point of entry to the unsafe region)
        hythread_safe_point_other(thread);
    }
    return;
}

#define TM_THREAD_VM_TLS_KEY 0
#define TM_THREAD_QUANTITY_OF_PREDEFINED_TLS_KEYS 1

 //@}
 /**
  * TM Thread states constants. They are compatible with JVMTI.
  */
//@{

#define TM_THREAD_STATE_ALIVE JVMTI_THREAD_STATE_ALIVE  // 0x0001 Thread is alive. Zero if thread is new (not started) or terminated.  
#define TM_THREAD_STATE_TERMINATED JVMTI_THREAD_STATE_TERMINATED // 0x0002Thread has completed execution.  
#define TM_THREAD_STATE_RUNNABLE JVMTI_THREAD_STATE_RUNNABLE // 0x0004 Thread is runnable.  
#define TM_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER // 0x0400  Thread is waiting to enter a synchronization block/method or, after an Object.wait(), waiting to re-enter a synchronization block/method.  
#define TM_THREAD_STATE_WAITING JVMTI_THREAD_STATE_WAITING // 0x0080  Thread is waiting.  
#define TM_THREAD_STATE_WAITING_INDEFINITELY JVMTI_THREAD_STATE_WAITING_INDEFINITELY // 0x0010  Thread is waiting without a timeout. For example, Object.wait().  
#define TM_THREAD_STATE_WAITING_WITH_TIMEOUT JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT // 0x0020  Thread is waiting with a maximum time to wait specified. For example, Object.wait(long).  
#define TM_THREAD_STATE_SLEEPING JVMTI_THREAD_STATE_SLEEPING // 0x0040  Thread is sleeping -- Thread.sleep(long).  
#define TM_THREAD_STATE_IN_MONITOR_WAIT JVMTI_THREAD_STATE_IN_OBJECT_WAIT // 0x0100  Thread is waiting on an object monitor -- Object.wait.  
#define TM_THREAD_STATE_PARKED JVMTI_THREAD_STATE_PARKED // 0x0200  Thread is parked, for example: LockSupport.park, LockSupport.parkUtil and LockSupport.parkNanos.  
#define TM_THREAD_STATE_UNPARKED  0x0800 // 0x0800  Thread is unparked, to track staled unparks;
#define TM_THREAD_STATE_SUSPENDED JVMTI_THREAD_STATE_SUSPENDED // 0x100000  Thread suspended. java.lang.Thread.suspend() or a JVMTI suspend function (such as SuspendThread) has been called on the thread. If this bit is set, the other bits refer to the thread state before suspension.  
#define TM_THREAD_STATE_INTERRUPTED JVMTI_THREAD_STATE_INTERRUPTED // 0x200000  Thread has been interrupted.  
#define TM_THREAD_STATE_IN_NATIVE JVMTI_THREAD_STATE_IN_NATIVE // 0x400000  Thread is in native code--that is, a native method is running which has not called back into the VM or Java programming language code. 

#define TM_THREAD_STATE_ALLOCATED JVMTI_THREAD_STATE_VENDOR_1 // 0x10000000 Thread just has been allocated.
#define TM_THREAD_STATE_RESERVED1 JVMTI_THREAD_STATE_VENDOR_2 // 0x20000000 Defined by VM vendor.  
#define TM_THREAD_STATE_RESERVED2 JVMTI_THREAD_STATE_VENDOR_3 // 0x40000000 Defined by VM vendor 

#define TM_MUTEX_DEFAULT  0   
#define TM_MUTEX_NESTED   1  
#define TM_MUTEX_UNNESTED 2 

#define WAIT_INTERRUPTABLE    1
#define WAIT_NONINTERRUPTABLE 0

/**
 * TM functions error codes (they are mostly coming from APR).
 */
#define TM_ERROR_NONE (0)  // No error has occurred. This is the error code that is returned on successful completion of the function. 
#define TM_ERROR_INVALID_MONITOR (50) // Monitor pointer provided to the function id invalid
#define TM_ERROR_NOT_MONITOR_OWNER (51) // Thread is not owner of the monitor
#define TM_ERROR_INTERRUPT (52) // The call has been interrupted before completion. 
#define TM_ERROR_NULL_POINTER (100)  // Pointer is unexpectedly NULL. 
#define TM_ERROR_OUT_OF_MEMORY (110) // The function attempted to allocate memory and no more memory was available for allocation. 
#define TM_ERROR_ACCESS_DENIED (111) // The desired functionality has not been enabled in this virtual machine. 
#define TM_ERROR_WRONG_PHASE (112)  // The desired functionality is not available in the current phase. Always returned if the virtual machine has completed running. 
#define TM_ERROR_INTERNAL (113) // An unexpected internal error has occurred. 
#define TM_ERROR_UNATTACHED_THREAD (115) // The thread being used to call this function is not attached to the virtual machine. Calls must be made from attached threads. See AttachCurrentThread in the JNI invocation API. 
#define TM_ERROR_INVALID_ENVIRONMENT (116) // The JVMTI environment provided is no longer connected or is not an environment. 
#define TM_ERROR_MAX_THREADS (117) //Max number of threads exceeded
#define TM_ERROR_ILLEGAL_STATE (51) //incorrect syncronizer state For example monitor wait without lock.
#define TM_ERROR_RUNNING_THREADS (119) //error relesing group/destroying library if there are running threads
#define TM_ERROR_EBUSY  APR_EBUSY // returned by try_lock 
#define TM_ERROR_TIMEOUT  APR_TIMEUP  // returned by try_lock 

#define TM_ERROR_START 1000

// possible values for tm_status_t
#define TM_OS_ERROR (TM_ERROR_START+1)

#define TM_MAX_OWNED_MONITOR_NUMBER 200 //FIXME: switch to dynamic resize
// if default stack size is not through -Xss parameter, it is 256kb
#define TM_DEFAULT_STACKSIZE (512 * 1024)

#if defined(__cplusplus)
}
#endif

#endif /* HYTHREAD_EXT_H */
