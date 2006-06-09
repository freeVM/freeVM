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
 * @author Alexei Fedotov
 * @version $Revision: 1.1.2.2.4.4 $
 */  

#ifndef _VM_COMPONENT_MANAGER_IMPL_H
#define _VM_COMPONENT_MANAGER_IMPL_H

#include "component_manager.h"
#include <apr_thread_rwlock.h>
#include <apr_dso.h>

typedef struct _Dll {
    /**
     * Operating system descriptor.
     */
    apr_dso_handle_t* descriptor;
    char* path;
    struct _Dll* next;
    apr_pool_t* pool;
} _Dll;
typedef const struct _Dll* DllHandle;

struct _InstanceInfo;
struct _ComponentInfo {
    /**
     * A reference to declaring library,
     * NULL for builtin component.
     */
    DllHandle declaring_library;
    OpenComponentHandle component;
    OpenInstanceAllocatorHandle instance_allocator;
    struct _ComponentInfo* next;
    /**
     * The list of instances of the component.
     */
    _InstanceInfo* instances;
    apr_pool_t* pool;
};
typedef const struct _ComponentInfo* ComponentInfoHandle;

struct _InstanceInfo {
    OpenInstanceHandle instance;
    ComponentInfoHandle component_info;
    struct _InstanceInfo* next;
    apr_pool_t* pool;
};
typedef const struct _InstanceInfo* InstanceInfoHandle;

struct _ComponentManagerImpl {
    /**
     * The virtual table for component manager interface
     * functions.
     */
    _OpenComponentManager cm;
    _Dll* libraries;
    _ComponentInfo* components;
    /**
     * Reference counter.
     */
    int num_clients;
    /**
     * The pool from which the <code>_ComponentManagerImpl</code>
     * is allocated. The structure is freed and the pool is
     * destroyed when the reference counter reaches zero.
     */
    apr_pool_t* pool;
};

#endif /* _VM_COMPONENT_MANAGER_IMPL_H */
