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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.2.4.3 $
 */

/**
 * @file
 *  
 * The file contains description of public component manager handles
 * and interfaces for initialization and dynamic linking of VM components.
 */
    
#ifndef _OPEN_COMPMGR_H
#define _OPEN_COMPMGR_H

#include <apr-1/apr_pools.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * The <code>_OpenInterface</code> structure represents
 * a virtual table of interface functions.
 */
struct _OpenInterface
{
    void* func1;
    /*
     * A real interface can contain more
     * function pointers.
     *
     * void* func2;
     * void* func3;
     */
};

/**
 * @name Handles
 * Handles are opaque for an interface user. The user
 * operates with handles by means of interface functions.
 */

/**
 * @ingroup Handles
 * The handle of the abstract component interface.
 */
typedef const struct _OpenInterface* OpenInterfaceHandle;

/**
 * Default component interface. Each component must
 * provide implementation of the functions from this
 * interface.
 */
struct _OpenComponent
{
    /**
     * Returns a component name.
     */
    const char* (*GetName) ();
    /**
     * Returns a component version which is numbers separated with dots.
     * Implementors must check a major version number for compatibility.
     */
    const char* (*GetVersion) ();
    /**
     * Returns human-readable description of the component.
     */
    const char* (*GetDescription) ();
    /**
     * Returns the human-readable vendor string, e.&nbsp;g.,
     * name of the organization which provides this component.
     */
    const char* (*GetVendor) ();
    /**
     * Queries optional properties.
     * @param key property name
     * @return a string, true/false for boolean properties
     */
    const char* (*GetProperty) (const char* key);

    /**
     * Exposes a NULL terminated list of component-specific
     * interface names for the given component.
     *
     * This default component interface is not included.
     * @return a pointer to an internal component manager structure,
     * which should not be modified or freed.
     */
    const char** (*ListInterfaceNames) ();

    /**
     * Exposes a component interface.
     * @param[out] p_intf on return, points to an interface handle
     * @param intf_name an interface name
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*GetInterface) (OpenInterfaceHandle* p_intf,
            const char* intf_name);

    /**
     * The call to this function frees all component resources.
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*Free) ();

};
/**
 * @ingroup Handles
 * The handle of the open component.
 */
typedef const struct _OpenComponent* OpenComponentHandle;

/**
 * The generic component instance.
 */
struct _OpenInstance
{
    OpenComponentHandle intf;
};
/**
 * @ingroup Handles
 * The handle of the open instance.
 */
typedef const struct _OpenInstance* OpenInstanceHandle;

/**
 * The private component interface used by a component manager to allocate
 * and dispose instances.
 */
struct _OpenInstanceAllocator {
    /**
     * The component provides a constructor-like method for instance
     * initialization which is called by component manager when a
     * new instance is created.
     *
     * @param[out] p_instance on return, points to handle of a new instance
     * @param pool created by a component manager for a lifetime of
     * the instance, the component could use the pool for allocation
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*CreateInstance) (OpenInstanceHandle* p_instance,
                           apr_pool_t* pool);

    /**
     * Free memory and other resources for a given instance.
     * @param instance a handle of an instance to free
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*FreeInstance) (OpenInstanceHandle instance);
};
/**
 * @ingroup Handles
 * The handle of the instance allocator interface.
 */
typedef const struct _OpenInstanceAllocator* OpenInstanceAllocatorHandle;

/**
 * The virtual table which contains public component manager
 * interface. The interface allows accessing components by name
 * and operating with component instances.
 *
 * It is safe to call these functions from multiple threads.
 */
struct _OpenComponentManager
{

    /**
     * Get a default interface of a registered component by name.
     * @param[out] p_component on return, points to
     * a handle of default component interface
     * @param name a component name
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*GetComponent) (OpenComponentHandle* p_component,
            const char* name);

    /**
     * Create a new component instance and register it in
     * a component manager.
     * @param[out] p_instance on return, points to a handle of a newly
     * created instance
     * @param name a name of interface
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*CreateInstance) (OpenInstanceHandle* p_instance,
            const char* name);

    /**
     * Unregister the instance in a component manager,
     * free memory and other resources held by the instance.
     * @param instance a handle to the instance to Free
     * @return APR_SUCCESS if successful, otherwise a non-zero error code
     */
    int (*FreeInstance) (OpenInstanceHandle instance);

};
/**
 * @ingroup Handles
 * The handle of the component manager interface.
 */
typedef const struct _OpenComponentManager* OpenComponentManagerHandle;

/**
 * The generic component initialization function type.
 * @param[out] p_component on return, points to a handle of a default
 * component interface
 * @param[out] p_allocator on return, points to a handle of a private
 * instance allocation interface
 * @param pool a memory pool with the component lifetime
 * @return APR_SUCCESS if successful, otherwise a non-zero error code
 */
typedef int
(*OpenComponentInitializer) (OpenComponentHandle* p_component,
                             OpenInstanceAllocatorHandle* p_allocator,
                             apr_pool_t* pool);

#ifdef __cplusplus
}
#endif

#endif                          /* _OPEN_COMPMGR_H */
