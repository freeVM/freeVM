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
 * @author Gregory Shimansky
 * @version $Revision: 1.1.2.1.4.4 $
 */
/*
 * JVMTI classes API
 */

#define LOG_DOMAIN "jvmti"
#include "cxxlog.h"

#include "jvmti_direct.h"
#include "jvmti_utils.h"
#include "Class.h"
#include "object_handles.h"
#include "jni_utils.h"
#include "jvmti_internal.h"
#include "open/vm_util.h"
#include "vm_strings.h"
#include "environment.h"
#include "classloader.h"

#include "suspend_checker.h"

/*
 * The static function that extracts class from the handle.
 *
 * Also, it checks if TI environmemt is valid, the function may be called
 * in current phase, and the results pointers are not NULL.
 *
 * @param env - the TI environment this function is called for.
 * @param phase_mask - the phase mask this function may be called in.
 * @param handle - the handle to extract the class from.
 * @param p1 - the pointer to be checked if NULL.
 * @param p2 - the pointer to be checked if NULL.
 * @param errorCode - keeps the error status if error has happen.
 * @return the corresponding class structure or NULL if wrong handler
 *         is specified or class was unloaded or any pointer is NULL.
 */
static inline Class* get_class_from_handle(jvmtiEnv* UNREF env, jvmtiPhase UNREF phase_mask,
                                           jclass handle, void* p1, void *p2, jvmtiError* errorCode)
{
    assert(hythread_is_suspend_enabled());

    if (NULL == handle || p1 == NULL || p2 == NULL)
    {
        *errorCode = JVMTI_ERROR_NULL_POINTER;
        return NULL;
    }

    Class *cl = jclass_to_struct_Class(handle);
    return cl;
}

/* It is the same to previous function, but it checks one pointer only. */
static inline Class* get_class_from_handle(jvmtiEnv* env, jvmtiPhase phase_mask,
                                           jclass handle, void* p1, jvmtiError* errorCode)
{
    return get_class_from_handle(env, phase_mask, handle, p1, (void*) 1,
                                 errorCode);
}

/* It is the same to previous function, but it checks no pointers. */
static inline Class* get_class_from_handle(jvmtiEnv* env, jvmtiPhase phase_mask,
                                           jclass handle, jvmtiError* errorCode)
{
    return get_class_from_handle(env, phase_mask, handle, (void *) 1, (void*) 1,
                                 errorCode);
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetLoadedClasses(jvmtiEnv* env,
                      jint* classes_num,
                      jclass** classes)
{
    TRACE2("jvmti.class", "GetLoadedClasses called");
    SuspendEnabledChecker sec;
    Class **klass;
    unsigned int index;
    unsigned int count;
    unsigned int number;
    unsigned int cl_count;
    jvmtiError errorCode;
    ClassLoader* classloader;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (classes_num == NULL || classes == NULL)
    {
        return JVMTI_ERROR_NULL_POINTER;
    }

    /**
     * Get the number of loaded classes by bootstrap loader
     */
    ClassTable* tbl =
        VM_Global_State::loader_env->bootstrap_class_loader->GetLoadedClasses();
    count = tbl->GetItemCount();

    /**
     * Count classes in user class loaders which are loaded by these class loaders
     */
    cl_count = ClassLoader::GetClassLoaderNumber();
    for( index = 0; index < cl_count; index++ )
    {
        classloader = (ClassLoader::GetClassLoaderTable())[index];
        tbl = classloader->GetLoadedClasses();
        ClassTable::iterator it;
        for(it = tbl->begin(); it != tbl->end(); it++)
        {
            klass = &it->second;
            if ((*klass)->class_loader != classloader)
                continue;
            count++;
        }
    }

    count -= 9; // number of primitive types; see JVM TI spec.

    /**
     * No loaded classes
     */
    if (!count)
    {
        *classes = NULL;
        *classes_num = 0;
        return JVMTI_ERROR_NONE;
    }

    /**
     * Allocate memory to be filled with class pointers
     */
    errorCode = _allocate( (sizeof(jclass) * count), (unsigned char**) classes );
    if (errorCode != JVMTI_ERROR_NONE) {
        return errorCode;
    }

    /**
     * Set resulting class table
     * First class loader is bootstrap class loader
     */
    number = 0;
    index = 0;
    classloader = VM_Global_State::loader_env->bootstrap_class_loader;
    do
    {
        /**
         * Create jclass handle for classes and set in jclass table
         */
        ClassTable::iterator it;
        tbl = classloader->GetLoadedClasses();
        for(it = tbl->begin(); it != tbl->end(); it++)
        {
            klass = &it->second;
            if((*klass)->is_primitive)
                continue;
            if ((*klass)->class_loader != classloader)
                continue;
            // create a new jclass handle for Class
            tmn_suspend_disable();
            ObjectHandle new_handle = oh_allocate_local_handle();
            new_handle->object = struct_Class_to_java_lang_Class( *klass );
            tmn_suspend_enable();

            // set created handle into jclass handle table
            (*classes)[number++] = (jclass)new_handle;
        }

        /**
         * Get next class loader
         */
        if( index < cl_count ) {
            classloader = (ClassLoader::GetClassLoaderTable())[index++];
        } else {
            break;
        }
    } while( true );
    assert( number == count );

    /**
     * Set class number
     */
    *classes_num = number;

    return JVMTI_ERROR_NONE;
} // jvmtiGetLoadedClasses

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassLoaderClasses(jvmtiEnv* env,
                           jobject initiating_loader,
                           jint* class_count_ptr,
                           jclass** classes_ptr)
{
    TRACE2("jvmti.class", "GetClassLoaderClasses called");
    SuspendEnabledChecker sec;
    unsigned index,
             count;
    Class **klass;
    ClassTable *tbl;
    ClassLoader* classloader;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (class_count_ptr == NULL || classes_ptr == NULL) {
        return JVMTI_ERROR_NULL_POINTER;
    }

    /**
     * Get class loader
     */
    if( initiating_loader == NULL ) {
        // get bootstrap class loader
        classloader = VM_Global_State::loader_env->bootstrap_class_loader;
    } else {
        // get user defined class loader
        classloader = ClassLoader::FindByObject((((ObjectHandle)initiating_loader)->object));
    }

    /**
     * Get the number of loaded classes
     */
    tbl = classloader->GetInitiatedClasses();
    if( !(count = tbl->GetItemCount()) ) {
        // no loaded classes
        *classes_ptr = NULL;
        *class_count_ptr = 0;
        return JVMTI_ERROR_NONE;
    }

    /**
     * Allocate memory to be filled with class pointers
     */
    errorCode = _allocate( (sizeof(jclass) * count), (unsigned char**)classes_ptr );
    if (errorCode != JVMTI_ERROR_NONE) {
        return errorCode;
    }

    /**
     * Create jclass handle for classes and set in jclass table
     */
    index = 0;
    ClassTable::iterator it;
    for(it = tbl->begin(); it != tbl->end(); it++)
    {
        klass = &it->second;
        // create a new jclass handle for Class
        tmn_suspend_disable();
        ObjectHandle new_handle = oh_allocate_local_handle();
        new_handle->object = struct_Class_to_java_lang_Class( *klass );
        tmn_suspend_enable();

        // set created handle into jclass handle table
        (*classes_ptr)[index++] = (jclass)new_handle;
    }
    assert( index == count );

    /**
     * Set class number
     */
    *class_count_ptr = count;

    return JVMTI_ERROR_NONE;
} // jvmtiGetClassLoaderClasses

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassSignature( jvmtiEnv* env,
                        jclass handle,
                        char** sig,
                        char** gen_sig)
{
    TRACE2("jvmti.class", "GetClassSignature called");
    SuspendEnabledChecker sec;
    int len,
        sig_len;
    char *pointer,
         *signature;
    Class *klass;
    jvmtiError result;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    /**
     * Get class from handle, set error if need it
     */
    klass = get_class_from_handle( env,
                jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE),
                handle, &result );
    if( klass == NULL ) {
        // check getting class
        return result;
    }

    if( sig ) {
        // get class signature length
        sig_len = GetClassSignatureLength( klass );
        // allocate memory for class signature
        result = _allocate( sig_len + 1, (unsigned char**)&signature );
        if( result != JVMTI_ERROR_NONE ) {
            return result;
        }
        // get class signature
        GetClassSignature( klass, signature );
        TRACE2("jvmti-class", "Class signature = " << signature);
        // allocate memory for class UTF8 signature
        len = get_utf8_length_of_8bit( (const uint8*)signature, sig_len);
        result = _allocate( len + 1, (unsigned char**)&pointer );
        if( result != JVMTI_ERROR_NONE ) {
            return result;
        }
        // copy class UTF8 signature
        utf8_from_8bit( pointer, (const uint8*)signature, sig_len );
        // set class UTF8 signature
        *sig = pointer;
        // free memory for class signature
        _deallocate( (unsigned char*)signature );
    }

    // set generic signature of the class
    if( gen_sig ) *gen_sig = NULL;

    return JVMTI_ERROR_NONE;
} // jvmtiGetClassSignature

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassStatus(jvmtiEnv* env, jclass handle, jint* status_ptr)
{
    TRACE2("jvmti.class", "GetClassStatus called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), handle, status_ptr,
            &errorCode);
    if (cl == NULL)
        return errorCode;

    *status_ptr = 0;

    if( class_is_primitive( cl ) ) {
        *status_ptr = JVMTI_CLASS_STATUS_PRIMITIVE;
    } else if( class_is_array( cl ) ) {
        *status_ptr = JVMTI_CLASS_STATUS_ARRAY;
    } else {
        switch( cl->state )
        {
        case ST_Start:
        case ST_LoadingAncestors:
            break;
        case ST_Loaded:
        case ST_InstanceSizeComputed:
            if( cl->is_verified ) {
                *status_ptr |= JVMTI_CLASS_STATUS_VERIFIED;
            }
            break;
        case ST_Prepared:
        case ST_Initializing:
            *status_ptr |= JVMTI_CLASS_STATUS_PREPARED
                | JVMTI_CLASS_STATUS_VERIFIED;
            break;
        case ST_Initialized:
            *status_ptr |= JVMTI_CLASS_STATUS_INITIALIZED
                | JVMTI_CLASS_STATUS_PREPARED
                | JVMTI_CLASS_STATUS_VERIFIED;
            break;
        case ST_Error:
            *status_ptr |= JVMTI_CLASS_STATUS_ERROR
                | JVMTI_CLASS_STATUS_INITIALIZED
                | JVMTI_CLASS_STATUS_PREPARED
                | JVMTI_CLASS_STATUS_VERIFIED;
            break;
        default:
            return JVMTI_ERROR_INTERNAL;
        }
    }

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetSourceFileName(jvmtiEnv* env, jclass handle, char** res)
{
    TRACE2("jvmti.class", "GetSourceFileName called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();
    CHECK_CAPABILITY(can_get_source_file_name);

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), handle, res,
            &errorCode);
    if( cl == NULL ) return errorCode;

    if( cl->is_primitive || cl->is_array )
    {
        TRACE2("jvmti.class", "GetSourceFileName called, name = " << cl->name->bytes <<
            " file name is absent");
        return JVMTI_ERROR_ABSENT_INFORMATION;
    }
    if(!cl->src_file_name) return JVMTI_ERROR_ABSENT_INFORMATION;

    TRACE2("jvmti.class", "GetSourceFileName called, name = " << cl->name->bytes <<
        " file name = " << cl->src_file_name->bytes);
    errorCode = _allocate( cl->src_file_name->len + 1, (unsigned char**)res );
    if( errorCode != JVMTI_ERROR_NONE ) return errorCode;

    strcpy( *res, cl->src_file_name->bytes );

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassModifiers(jvmtiEnv* env, jclass handle, jint* modifiers_ptr)
{
    TRACE2("jvmti.class", "GetClassModifiers called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), handle, modifiers_ptr,
            &errorCode);
    if( cl == NULL ) return errorCode;

    *modifiers_ptr = 0;
    if( class_is_public( cl ) ) *modifiers_ptr |= ACC_PUBLIC;
    if( class_is_final( cl ) ) *modifiers_ptr |= ACC_FINAL;
    if( class_is_super( cl ) ) *modifiers_ptr |= ACC_SUPER;
    if( class_is_interface( cl ) ) *modifiers_ptr |= ACC_INTERFACE;
    if( class_is_abstract( cl ) ) *modifiers_ptr |= ACC_ABSTRACT;

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassMethods(jvmtiEnv* env, jclass handle, jint* method_count_ptr,
                     jmethodID** methods_ptr)
{
    TRACE2("jvmti.class", "GetClassMethods called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), handle, method_count_ptr, methods_ptr,
            &errorCode);
    if( cl == NULL ) return errorCode;

    /*
     * Check class status. If class is not on PREPARED status, GetClassMethods(...)
     * function returns JVMTI_ERROR_CLASS_NOT_PREPARED
     */
    if ((cl->state == ST_Start) ||
            (cl->state == ST_LoadingAncestors) ||
            (cl->state == ST_Loaded) ||
            (cl->state == ST_InstanceSizeComputed))
        return JVMTI_ERROR_CLASS_NOT_PREPARED;

    errorCode = _allocate( cl->n_methods*sizeof(jmethodID), (unsigned char**)methods_ptr );
    if( errorCode != JVMTI_ERROR_NONE ) return errorCode;
    *method_count_ptr = cl->n_methods;
    for( short i = 0; i < cl->n_methods; i++ ) (*methods_ptr)[i] = (jmethodID)&(cl->methods[i]);

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetClassFields(jvmtiEnv* env, jclass handle, jint* field_count_ptr,
                    jfieldID** fields_ptr)
{
    TRACE2("jvmti.class", "GetClassFields called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), handle,
            field_count_ptr, fields_ptr, &errorCode);
    if( cl == NULL ) return errorCode;

    /*
     * Check class status. If class is not on PREPARED status, GetClassMethods(...)
     * function returns JVMTI_ERROR_CLASS_NOT_PREPARED
     */
    if ((cl->state == ST_Start) ||
            (cl->state == ST_LoadingAncestors) ||
            (cl->state == ST_Loaded) ||
            (cl->state == ST_InstanceSizeComputed))
        return JVMTI_ERROR_CLASS_NOT_PREPARED;

    errorCode = _allocate( cl->n_fields*sizeof(jfieldID), (unsigned char**)fields_ptr );
    if( errorCode != JVMTI_ERROR_NONE ) return errorCode;
    *field_count_ptr = cl->n_fields;
    for( short i = 0; i < cl->n_fields; i++ ) (*fields_ptr)[i] = (jfieldID)&(cl->fields[i]);

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetImplementedInterfaces(jvmtiEnv* env, jclass klass, jint* interface_count_ptr,
                              jclass** interfaces_ptr)
{
    TRACE2("jvmti.class", "GetImplementedInterfaces called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(klass))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
            jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE), klass,
            interface_count_ptr, interfaces_ptr, &errorCode);
    if( cl == NULL ) return errorCode;

    /*
     * Check class status. If class is not on PREPARED status, GetClassMethods(...)
     * function returns JVMTI_ERROR_CLASS_NOT_PREPARED
     */
    if ((cl->state == ST_Start) ||
            (cl->state == ST_LoadingAncestors) ||
            (cl->state == ST_Loaded) ||
            (cl->state == ST_InstanceSizeComputed))
        return JVMTI_ERROR_CLASS_NOT_PREPARED;

    errorCode = _allocate( cl->n_superinterfaces*sizeof(jclass),
        reinterpret_cast<unsigned char**>(interfaces_ptr) );
    if( errorCode != JVMTI_ERROR_NONE ) return errorCode;
    ObjectHandle jclss;
    for( int i = 0; i < cl->n_superinterfaces; i++ )
    {
        tmn_suspend_disable();
        jclss = oh_allocate_local_handle();
        jclss->object = struct_Class_to_java_lang_Class( cl->superinterfaces[i].clss );
        tmn_suspend_enable();
        (*interfaces_ptr)[i] = (jclass)jclss;
    }
    *interface_count_ptr = cl->n_superinterfaces;

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiIsInterface(jvmtiEnv* env, jclass handle, jboolean* is_interface_ptr)
{
    TRACE2("jvmti.class", "IsInterface called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
                                      jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE),
                                      handle, is_interface_ptr, &errorCode);

    if (cl == NULL)
        return errorCode;

    TRACE2("jvmti.class", "IsInterface: class = " << cl->name->bytes);
    *is_interface_ptr = (jboolean)(class_is_interface(cl) ? JNI_TRUE : JNI_FALSE);
    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiIsArrayClass(jvmtiEnv* env, jclass handle, jboolean* is_array_class_ptr)
{
    TRACE2("jvmti.class", "IsArrayClass called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* cl = get_class_from_handle(env,
                                      jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE),
                                      handle, is_array_class_ptr, &errorCode);

    if (cl == NULL)
        return errorCode;

    TRACE2("jvmti.class", "IsArrayClass: class = " << cl->name->bytes);
    *is_array_class_ptr = (jboolean)(cl->is_array ? JNI_TRUE : JNI_FALSE);
    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL jvmtiGetClassLoader(jvmtiEnv* env, jclass handle, jobject* classloader_ptr)
{
    TRACE2("jvmti.class", "GetClassLoader called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* clss = get_class_from_handle(env,
                                      jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE),
                                      handle, classloader_ptr, &errorCode);
    if (clss == NULL)
        return errorCode;

    tmn_suspend_disable();
    ManagedObject* cl = clss->class_loader->GetLoader();
    if( !cl ) {
        *classloader_ptr = NULL;
    } else {
        ObjectHandle oh = oh_allocate_local_handle();
        oh->object = cl;
        *classloader_ptr = (jobject)oh;
    }
    tmn_suspend_enable();

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiGetSourceDebugExtension(jvmtiEnv* env, jclass handle, char** source_debug_extension_ptr)
{
    TRACE2("jvmti.class", "GetSourceDebugExtension called");
    SuspendEnabledChecker sec;
    jvmtiError errorCode;

    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_START, JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    if (! is_valid_class_object(handle))
        return JVMTI_ERROR_INVALID_CLASS;

    Class* clss = get_class_from_handle(env,
                                      jvmtiPhase (JVMTI_PHASE_START | JVMTI_PHASE_LIVE),
                                      handle, source_debug_extension_ptr, &errorCode);
    if (clss == NULL)
        return errorCode;

    if( !clss->sourceDebugExtension ) return JVMTI_ERROR_ABSENT_INFORMATION;

    errorCode = _allocate( clss->sourceDebugExtension->len + 1,
        reinterpret_cast<unsigned char**>(source_debug_extension_ptr) );
    if( errorCode != JVMTI_ERROR_NONE )
        return errorCode;

    strcpy( *source_debug_extension_ptr, clss->sourceDebugExtension->bytes );

    return JVMTI_ERROR_NONE;
}

/*
 * @see official specification for details.
 */
jvmtiError JNICALL
jvmtiRedefineClasses(jvmtiEnv* env, jint UNREF class_count,
                     const jvmtiClassDefinition* UNREF class_definitions)
{
    TRACE2("jvmti.class", "RedefineClasses called");
    SuspendEnabledChecker sec;
    /*
     * Check given env & current phase.
     */
    jvmtiPhase phases[] = {JVMTI_PHASE_LIVE};

    CHECK_EVERYTHING();

    //TBD

    return JVMTI_NYI;
}

