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
 * @author Intel, Pavel Afremov
 * @version $Revision: 1.1.2.2.4.3 $
 */

#include "platform.h"
#include "vm_process.h"
#include <stdio.h>
#include <assert.h>

#include "lock_manager.h"
#include "object_layout.h"
#include "open/types.h"
#include "Class.h"
#include "open/vm_util.h"
#include "environment.h"
#include "ini.h"
#include "exceptions.h"
#include "compile.h"
#include "nogc.h"
#include "jit_runtime_support.h"
#include "vm_synch.h"
#include "finalize.h"
#include "open/thread.h"

#define LOG_DOMAIN "vm.object_queue"
#include "classloader.h"
#undef LOG_DOMAIN

#include "cxxlog.h"

#include "thread_generic.h"

#ifndef USE_GC_STATIC
__declspec(dllexport) 
#endif

// FINALIZER_THREAD means that thread is finalizer thread
#define FINALIZER_THREAD    0x1

// FINALIZER_STARTER means that thread is running inside run_finalizers function
#define FINALIZER_STARTER   0x2

//
// This code holds the logic that deals with finalization as well as weak/soft/phantom 
// references. Finalization runs arbitrary code that can include synchronization logic.
//
// There are two different times that finalizers and enqueues can be run.
//
// The first is to have a separate thread that receives the objects that need to be
// finalized and executes them sometime after the GC has completed. This is probable
// what was intended by the designers of the finalization and weak/soft/phantom 
// reference features of the language. 
//
// The second approach that can be taken is to run the finalization or enqueues code  
// immediately after the gc is complete prior to resuming the thread that caused the gc
// to happen.
//
// The first scheme was implemented for finalization and second is used for 
// weak/soft/phantom references.
//

VmEventHandle begin_run_finalizer;

class Object_Queue
{
    ManagedObject **objects;
    unsigned capacity;
    unsigned num_objects;

    Lock_Manager objects_lock;
    char*  log_domain;

    void reallocate(unsigned new_capacity);
    
public:
    Object_Queue();
    Object_Queue(char*  log_domain);
    ~Object_Queue(){
        STD_FREE(objects);
    }
    void add_object(ManagedObject *p_obj);
    ManagedObject* remove_object();
    int getLength();
    
    void enumerate_for_gc();
}; //Object_Queue

class Objects_To_Finalize: public Object_Queue
{
    // workaround part to ignore classes java.nio.charset.CharsetEncoder
    // java.io.FileDescriptor & java.io.FileOutputStream during finalization on exit
    bool is_class_ignored(Class* test);
    bool classes_cached;
    Lock_Manager ignore_lock;
    Class* CharsetEncoder;
    Class* FileDescriptor;
    Class* FileOutputStream;
public:
    Objects_To_Finalize() : Object_Queue("finalize") {
        classes_cached = false;
    };
    // redefine of add method 
    void add_object(ManagedObject *p_obj);
    
    void run_finalizers();
    int do_finalization(int quantity);
}; //Objects_To_Finalize

class References_To_Enqueue: public Object_Queue
{
public:
    References_To_Enqueue() : Object_Queue("ref") {};
    void enqueue_references();
}; //References_To_Enqueue



Object_Queue::Object_Queue() {
    Object_Queue::Object_Queue("unknown");
}

Object_Queue::Object_Queue(char*  log_domain)
{
    objects     = 0;
    capacity    = 0;
    num_objects = 0;
    this-> log_domain =  log_domain;
    reallocate(128);
} //Object_Queue::Object_Queue


/**
 * Allocates array to save objects. Should be called from synchronized block.
 * Now it's called from add_object only.
 */
void Object_Queue::reallocate(unsigned new_capacity)
{
    ManagedObject **new_table =
        (ManagedObject **)STD_MALLOC(new_capacity * sizeof(ManagedObject *));
        
    // print trace info about queue enlarge
    TRACE2( log_domain, "The " <<  log_domain << " queue capacity is enlarged to " 
            << new_capacity << " units.");
        
    // asserts that queue information is correct 
    assert(new_table);
    assert(num_objects <= capacity);
    
    // if queue already contains objects, copyes it to new array and free memory for previous one.
    if(objects) {
        memcpy(new_table, objects, num_objects * sizeof(ManagedObject *));
        STD_FREE(objects);
    }
    
    // saves new queue information
    objects = new_table;
    capacity = new_capacity;
} //Object_Queue::reallocate


/**
 * Adds object to queue
 */
void Object_Queue::add_object(ManagedObject *p_obj)
{
    objects_lock._lock();
    
    // reallocates if there is not enough place to save new one
    if(num_objects >= capacity) {
        reallocate(capacity * 2);
    }
    
    // asserts that queue information is correct 
    assert(num_objects < capacity);

    // adds object to queue
    objects[num_objects++] = p_obj;
    objects_lock._unlock();
} //Object_Queue::add_object

/**
 * Removes latest object from queue and returns it as result.
 */
ManagedObject*
Object_Queue::remove_object() {
    ManagedObject* removed_object;
    objects_lock._lock();

    // if there is any objects in queue, remove the latest from there to return as result
    if (0 < num_objects) {
        removed_object = this->objects[--num_objects];
    } else {
        removed_object = NULL;
    }
    objects_lock._unlock();
    return removed_object;
} //Object_Queue::remove_object

/**
 * Returns length of queue
 */
int Object_Queue::getLength() {
   unsigned result;
   
   // synchronization used there to avoid of retur very old value
   objects_lock._lock();
   result = this->num_objects;
   objects_lock._unlock();
   
   // there unfresh value can be returned, but this value was correct after fgunction start 
   return result;
}

/**
 * Enumerates queue for GC
 */
void Object_Queue::enumerate_for_gc()
{
    // locks here because some native code can work during gc untill tmn_suspend_disable() call
    objects_lock._lock();

    // print trace info about queue enlarge
    TRACE2( log_domain, "The " <<  log_domain << " queue length is " << num_objects << " units");

    // enumerate elements in the queue
    for(unsigned i = 0; i < num_objects; i++) {
        vm_enumerate_root_reference((void **)&(objects[i]), FALSE);
    }
    
    // unlock 
    objects_lock._unlock();
} //Object_Queue::enumerate_for_gc

void Objects_To_Finalize::add_object(ManagedObject *p_obj)
{
    Class* finalizer_thread = VM_Global_State::loader_env->finalizer_thread;

    if (!finalizer_thread) {
        return;
    } else {
        Object_Queue::add_object(p_obj);
    }
} //Objects_To_Finalize::add_object

// workaround method to ignore classes java.nio.charset.CharsetEncoder
// java.io.FileDescriptor & java.io.FileOutputStream during finalization on exit
bool Objects_To_Finalize::is_class_ignored(Class* test) {
    assert(!tmn_is_suspend_enabled());
    
    if (!classes_cached) {
    
        tmn_suspend_enable();
        
        String* CharsetEncoderName =
                VM_Global_State::loader_env->string_pool.lookup("com/ibm/icu4jni/charset/CharsetEncoderICU");
        String* FileDescriptorName =
                VM_Global_State::loader_env->string_pool.lookup("java/io/FileDescriptor");
        String* FileOutputStreamName =
                VM_Global_State::loader_env->string_pool.lookup("java/io/FileOutputStream");
        
        Class* CharsetEncoder = 
            VM_Global_State::loader_env->bootstrap_class_loader->LoadVerifyAndPrepareClass(
                VM_Global_State::loader_env, CharsetEncoderName);
        Class* FileDescriptor = 
            VM_Global_State::loader_env->bootstrap_class_loader->LoadVerifyAndPrepareClass(
                VM_Global_State::loader_env, FileDescriptorName);
        Class* FileOutputStream = 
            VM_Global_State::loader_env->bootstrap_class_loader->LoadVerifyAndPrepareClass(
                VM_Global_State::loader_env, FileOutputStreamName);
        
        tmn_suspend_disable();
        ignore_lock._lock();
        this->CharsetEncoder = CharsetEncoder;
        this->FileDescriptor = FileDescriptor;
        this->FileOutputStream = FileOutputStream;
        classes_cached = true;
        ignore_lock._unlock();
    }
    return ((test==CharsetEncoder) 
            || (test==FileDescriptor) 
            || (test==FileOutputStream));
}

void Objects_To_Finalize::run_finalizers()
{
    assert(tmn_is_suspend_enabled());

    int num_objects = getLength();

    if (num_objects == 0) {
        return;
    }

    if ((p_TLS_vmthread->finalize_thread_flags & (FINALIZER_STARTER | FINALIZER_THREAD)) != 0) {
        TRACE2("finalize", "recursive finalization prevented");
        return;
    }

    p_TLS_vmthread->finalize_thread_flags |= FINALIZER_STARTER;
    TRACE2("finalize", "run_finalizers() started");
    
    jvalue args[1];
    args[0].z = false;

    Class* finalizer_thread = VM_Global_State::loader_env->finalizer_thread;
    
    if (!finalizer_thread) {
        p_TLS_vmthread->finalize_thread_flags &= ~FINALIZER_STARTER;
        return;
    }

    Method* finalize_meth = class_lookup_method_recursive(finalizer_thread,
        "startFinalization", "(Z)V");
    assert(finalize_meth);

    tmn_suspend_disable();
    vm_execute_java_method_array((jmethodID) finalize_meth, 0, args);
    tmn_suspend_enable();

    if (exn_raised()) {
        jobject exc = exn_get();
        tmn_suspend_disable();
        assert(exc->object->vt()->clss);
        INFO2("finalize", "Uncaught exception "
            << class_get_name(exc->object->vt()->clss)
            << " while running a wakeFinalization in FinalizerThread");
        tmn_suspend_enable();
        exn_clear();
    }
    p_TLS_vmthread->finalize_thread_flags &= ~FINALIZER_STARTER;
} //Objects_To_Finalize::run_finalizers

int Objects_To_Finalize::do_finalization(int quantity) {
    //SetThreadPriority(GetCurrentThread(),THREAD_PRIORITY_HIGHEST);
    p_TLS_vmthread->finalize_thread_flags = FINALIZER_THREAD;

    int i;
    tmn_suspend_disable();
    ObjectHandle handle = oh_allocate_local_handle();
    tmn_suspend_enable();
    jvalue args[1];
    args[0].l = (jobject) handle;

    assert(VM_Global_State::loader_env->finalizer_thread);
    jboolean* finalizer_shutdown = VM_Global_State::loader_env->finalizer_shutdown;
    assert(finalizer_shutdown);
    jboolean* finalizer_on_exit = VM_Global_State::loader_env->finalizer_on_exit;
    assert(finalizer_on_exit);

    for (i=0; ((i<quantity)||(0==quantity)); i++) {
    
        // shutdown flag in FinalizerThread set after finalization on exit is completed
        if (*finalizer_shutdown) {
            return i;
        }

        tmn_suspend_disable();
        ManagedObject* object = remove_object();
        handle->object = object;
        tmn_suspend_enable();

        if (object == NULL) {
            return i;
        }
        
        tmn_suspend_disable();
        assert(handle->object->vt()->clss);
        Class *clss = handle->object->vt()->clss;
        assert(clss);
        
        if ((*finalizer_on_exit)  && is_class_ignored(clss)) {
            tmn_suspend_enable();
            continue;
        }
        
        Method *finalize = class_lookup_method_recursive(clss, "finalize", "()V");
        assert(finalize);
        TRACE2("finalize", "finalize object " << class_get_name(handle->object->vt()->clss));
        vm_execute_java_method_array( (jmethodID) finalize, 0, args);
        tmn_suspend_enable();
        
        if (exn_raised()) {
            jobject exc = exn_get();
            tmn_suspend_disable();
            assert(exc->object->vt()->clss);
            assert(handle->object->vt()->clss);
            INFO2("finalize", "Uncaught exception "
                << class_get_name(exc->object->vt()->clss)
                << " while running a finalize of the object"
                << class_get_name(object->vt()->clss) << ".");
            tmn_suspend_enable();
            exn_clear();
        }
    }
    return i;
} //Objects_To_Finalize::do_finalization

void References_To_Enqueue::enqueue_references()
{
    TRACE2("ref", "enqueue_references() started");

    jvalue args[1], r;

    tmn_suspend_disable();
    ObjectHandle handle = oh_allocate_local_handle();
    tmn_suspend_enable();

    args[0].l = (jobject) handle;

    while(true) {
        tmn_suspend_disable();
        ManagedObject* object = remove_object();
        handle->object = object;
        tmn_suspend_enable();

        if (object == NULL) {
            return;
        }
        tmn_suspend_disable();
        assert(handle->object->vt()->clss);
        Class *clss = handle->object->vt()->clss;
        TRACE2("ref","Enqueueing reference " << (handle->object));
        Method *enqueue = class_lookup_method_recursive(clss, "enqueue", "()Z");
        assert(enqueue);
        vm_execute_java_method_array( (jmethodID) enqueue, &r, args);
        tmn_suspend_enable();

        if (exn_raised()) {
            jobject exc = exn_get();
            tmn_suspend_disable();
            assert(exc->object->vt()->clss);
            assert(object->vt()->clss);
            INFO2("ref", "Uncaught exception "
                << class_get_name(exc->object->vt()->clss)
                << " while running a enqueue method of the object"
                << class_get_name(object->vt()->clss) << ".");
            tmn_suspend_enable();
            exn_clear();
        }
    }

    TRACE2("ref", "enqueue_references() completed");
} //Objects_To_Finalize::notify_reference_queues


static Objects_To_Finalize objects_to_finalize;

void vm_finalize_object(Managed_Object_Handle p_obj)
{
    objects_to_finalize.add_object((ManagedObject *)p_obj);
} //vm_finalize_object

void vm_run_pending_finalizers()
{
    NativeObjectHandles nhs;
    assert(tmn_is_suspend_enabled());
    objects_to_finalize.run_finalizers();
} //vm_run_pending_finalizers

int vm_do_finalization(int quantity)
{
    assert(tmn_is_suspend_enabled());
    return objects_to_finalize.do_finalization(quantity);
} //vm_run_pending_finalizers

bool is_it_finalize_thread() {
    return ((p_TLS_vmthread->finalize_thread_flags & FINALIZER_THREAD) != 0);
}

void vm_enumerate_objects_to_be_finalized()
{
    TRACE2("enumeration", "enumeration objects to be finalized");
    INFO2("stats.finalize", "Enumerating finalize queue");
    objects_to_finalize.enumerate_for_gc();
} //vm_enumerate_objects_to_be_finalized

int vm_get_finalizable_objects_quantity()
{
    return objects_to_finalize.getLength();
}
// -- Code to deal with Reference Queues that need to be notified.

static References_To_Enqueue references_to_enqueue;

void vm_enumerate_references_to_enqueue()
{
    TRACE2("enumeration", "enumeration pending references to be enqueued");
    INFO2("stats.finalize", "Enumerating reference queue");
    references_to_enqueue.enumerate_for_gc();
} //vm_enumerate_references_to_enqueue

void vm_enqueue_reference(Managed_Object_Handle obj)
{
    TRACE2("ref", obj << " is being added to enqueue list");
    references_to_enqueue.add_object((ManagedObject *)obj);
} // vm_enqueue_reference

void vm_enqueue_references()
{ 
    references_to_enqueue.enqueue_references();
} //vm_enqueue_references
