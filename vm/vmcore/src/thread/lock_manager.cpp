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


#include "platform.h"
#include "lock_manager.h"
#include "vm_threads.h"
#include "exceptions.h"
#include "vm_synch.h"

//void vm_thread_enumerate_from_native(VM_thread *thread); // unused anywhere

Lock_Manager *p_jit_a_method_lock;
Lock_Manager *p_vtable_patch_lock;
Lock_Manager *p_meth_addr_table_lock;
Lock_Manager *p_thread_lock;
Lock_Manager *p_method_call_lock;
Lock_Manager *p_handle_lock;
 
extern hythread_library_t hythread_lib;

VMEXPORT void jit_lock() {
    p_jit_a_method_lock->_lock();
}

VMEXPORT void jit_unlock() {
    p_jit_a_method_lock->_unlock();
}

Lock_Manager::Lock_Manager()
{   // init thread menager if needed
    hythread_init(hythread_lib);
    UNREF IDATA stat = hymutex_create (&lock, TM_MUTEX_NESTED);
    assert(stat==TM_ERROR_NONE);
}

Lock_Manager::~Lock_Manager()
{
    UNREF IDATA stat = hymutex_destroy (lock);
    assert(stat==TM_ERROR_NONE);
}

void Lock_Manager::_lock()
{
    UNREF IDATA stat = hymutex_lock(lock);
    assert(stat==TM_ERROR_NONE);
}

bool Lock_Manager::_tryLock()
{     
    IDATA stat = hymutex_trylock(lock);
    return stat==TM_ERROR_NONE;    
}

void Lock_Manager::_unlock()
{
    UNREF IDATA stat = hymutex_unlock(lock);
    assert(stat==TM_ERROR_NONE);
}


void Lock_Manager::_lock_enum()
{   
    Lock_Manager::_lock_enum_or_null(0);
}


void Lock_Manager::_unlock_enum()
{
   Lock_Manager::_unlock();
}


//
// If a lock is immediately available return true
//      otherwise return NULL
//
//  Use unlock_or_null so that the names match.
//

bool Lock_Manager::_lock_or_null()
{
    return Lock_Manager::_tryLock();
}


void Lock_Manager::_unlock_or_null()
{
    Lock_Manager::_unlock_enum();
}


void Lock_Manager::_unlock_enum_or_null() 
{
     Lock_Manager::_unlock_enum();
} 

bool Lock_Manager::_lock_enum_or_null(bool UNREF return_null_on_fail)
{
    IDATA stat = hymutex_lock(lock);
    return stat==TM_ERROR_NONE;
}
