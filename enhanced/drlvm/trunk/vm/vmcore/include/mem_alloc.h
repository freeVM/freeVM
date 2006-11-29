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
 * @author Intel, Aleksey Ignatenko, Alexei Fedotov
 * @version $Revision: 1.1.2.1.4.3 $
 */  

#ifndef _MEM_ALLOC_H_
#define _MEM_ALLOC_H_

#include "jit_import.h"
#include "port_vmem.h"

#define DEFAULT_COMMOT_JIT_CODE_POOL_SIZE 32*1024  // pool is used for common stub code
#define DEFAULT_COMMOT_VTABLE_POOL_SIZE_NO_RESIZE 8*1024*1024 // used for comressed VTable pointers
#define DEFAULT_CLASSLOADER_VTABLE_POOL_SIZE 32*1024
#define DEFAULT_CLASSLOADER_JIT_CODE_POOL_SIZE 64*1024
#define DEFAULT_BOOTSTRAP_JIT_CODE_POOL_SIZE 256*1024  
#define DEFAULT_VTABLE_POOL_SIZE   128*1024  

#define MEMORY_UTILIZATION_LIMIT 15

typedef struct PoolDescriptor {
    Byte    *_begin;     // next free byte in memory chunk
    Byte    *_end;       // end of memory chunk
    size_t   _size;      // size of memory chunk
    port_vmem_t*    _descriptor; // for further memory deallocation
    PoolDescriptor* _next; 
} PoolDescriptor;

// PoolManager is a thread safe memory manager
// PoolDescriptor describes allocated memory chunk inside pool
// There are 2 kinds of PoolDescriptor in PoolManager: active and passive
// PoolManager uses active PoolDescriptors for memory allocations, passive PoolDescriptors are filled with allocated memory and not used. 
// Division into active and passive PoolDescriptors is done on the basis of MEMORY_UTILIZATION_LIMIT value. 
// if PoolDescriptors is filled less than (MEMORY_UTILIZATION_LIMIT)% of its size then it is considered to be passive,
// otherwise it is active (allows further memory allocations from it)

class PoolManager {
public:
    PoolManager(size_t initial_size, size_t page_size, bool use_large_pages, bool is_code, bool is_resize_allowed);
    virtual ~PoolManager();
    
    // alloc is synchronized inside the class
    void* alloc(size_t size, size_t alignment, Code_Allocation_Action action);
    inline Byte* get_pool_base();

protected:
    PoolDescriptor*   _active_pool;
    PoolDescriptor*   _passive_pool;
    size_t            _page_size;
    bool              _use_large_pages;
    size_t            _default_pool_size;
    bool              _is_code;
    bool              _is_resize_allowed;

    apr_pool_t* aux_pool;
    apr_thread_mutex_t* aux_mutex;

    Byte   *vtable_pool_start; // for compressed vtable pointers support only!

protected:
    inline PoolDescriptor* allocate_pool_storage(size_t size); // allocate memory for new PoolDescriptor
    inline size_t round_up_to_page_size_multiple(size_t size);
    inline void _lock();
    inline void _unlock();
};


#endif //_MEM_ALLOC_H_

