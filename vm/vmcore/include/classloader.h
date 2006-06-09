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
 * @author Pavel Pervov, Pavel Rebriy
 * @version $Revision: 1.1.2.8.4.3 $
 */  
#ifndef _CLASSLOADER_H_
#define _CLASSLOADER_H_

#include <sstream>
#include <apr_pools.h>

#include "vm_core_types.h"
#include "object_layout.h"
#include "String_Pool.h"
#include "Class.h"
#include "open/vm.h"
#include "lock_manager.h"
#include "environment.h"
#include "exceptions.h"
#include "natives_support.h"
#include "hashtable.h"
#include "loggerstring.h"
#include "jarfile_support.h"

class ClassTable : public MapEx<const String*, Class* > {};

/*
Concurrent class loading in user class loaders:
1)  resolution & direct defineClass call
    a.  if resolution happens first and finishes successfully,
        java/lang/LinkageError must be thrown for defineClass call
    b.  if defineClass starts first and succeeds, results for class loading in resolution
        should be discarded and already constructed class should be returned
2)  resolution & resolution
    a.  first come, first served; second will get constructed class
3)  direct defineClass call & direct defineClass call
    a.  first come, first served; java/lang/LinkageError must be thrown on the second thread.

ClassCircularityError can be only discovered on the thread, which defines class.
*/

struct ClassLoader
{
    struct FailedClass
    {
        // class name
        const String* m_name;
        // exception object
        ManagedObject* m_exception;

        FailedClass() : m_name(NULL), m_exception(NULL) {}
        FailedClass(const FailedClass& fc) : m_name(fc.m_name), m_exception(fc.m_exception) {}
        bool operator == (FailedClass& fc) { return m_name == fc.m_name; }
    };

    struct LoadingClass
    {
        struct WaitingThread {
            VM_thread* m_waitingThread;
            WaitingThread* m_next;
        };
#ifdef _DEBUG
        // debugging
        const String* m_name;
#endif
        apr_pool_t* m_threadsPool;
        // threads waiting for this class
        WaitingThread* m_waitingThreads;
        // this event is signaled when class loader finishes
        // (successfully or unsuccessfully) loading this class
        VmEventHandle m_loadWaitEvent;
        // thread which owns class definition
        VM_thread* m_defineOwner;
        // thread which first started loading this class
        VM_thread* m_initiatingThread;

        LoadingClass() :
#ifdef _DEBUG
            m_name(NULL),
#endif
            m_threadsPool(NULL),
            m_waitingThreads(NULL),
            m_loadWaitEvent(0),
            m_defineOwner(NULL),
            m_initiatingThread(NULL) {}
        LoadingClass(const LoadingClass& lc) {
#ifdef _DEBUG
            m_name = lc.m_name;
#endif
            m_threadsPool = lc.m_threadsPool;
            m_waitingThreads = lc.m_waitingThreads;
            m_loadWaitEvent = lc.m_loadWaitEvent;
            m_defineOwner = lc.m_defineOwner;
            m_initiatingThread = lc.m_initiatingThread;
        }
        LoadingClass& operator=(const LoadingClass& lc) {
#ifdef _DEBUG
            m_name = lc.m_name;
#endif
            m_threadsPool = lc.m_threadsPool;
            m_waitingThreads = lc.m_waitingThreads;
            m_loadWaitEvent = lc.m_loadWaitEvent;
            m_defineOwner = lc.m_defineOwner;
            m_initiatingThread = lc.m_initiatingThread;

            return *this;
        }
        ~LoadingClass() {
            if(m_threadsPool != NULL) {
                apr_pool_destroy(m_threadsPool);
                m_threadsPool = NULL;
            }
            if(m_loadWaitEvent != 0) {
                vm_destroy_event(m_loadWaitEvent);
                m_loadWaitEvent = 0;
            }
        }

        bool CreateWaitingEvent(const String* className);
        void WaitLoading() {
            assert(m_loadWaitEvent != 0);
            vm_wait_for_single_object(m_loadWaitEvent, INFINITE);
        }
        void SignalLoading() {
            if(m_loadWaitEvent != 0)
                vm_set_event(m_loadWaitEvent);
        }
        bool IsDefiner(VM_thread* thread) { return m_defineOwner == thread; }
        bool HasDefiner() { return m_defineOwner != NULL; }
        void EnqueueInitiator(VM_thread* new_definer, ClassLoader* cl, const String* clsname);
        void ChangeDefinerAndInitiator(VM_thread* new_definer, ClassLoader* cl, const String* clsname);
        bool IsInitiator(VM_thread* thread) { return m_initiatingThread == thread; }
        void SetInitiator(VM_thread* thread) { m_initiatingThread = thread; }
        void UpdateInitiator(VM_thread* thread) { m_initiatingThread = thread; }
        // this operation should be synchronized
        bool AlreadyWaiting(VM_thread* thread);
        // this operation should be synchronized
        void AddWaitingThread(VM_thread* thread, ClassLoader* cl, const String* clsname);
        // this operation should be synchronized
        void RemoveWaitingThread(VM_thread* thread, ClassLoader* cl, const String* clsname);
        bool HasWaitingThreads() { return (m_waitingThreads != NULL); }
    };

public:
    friend LoggerString& operator << (LoggerString& log, FailedClass& lc);
    friend LoggerString& operator << (LoggerString& log, LoadingClass& lc);

private:
    class FailedClasses : public MapEx<const String*, FailedClass> {};
    class LoadingClasses : public MapEx<const String*, LoadingClass > {};
    class ReportedClasses : public MapEx<const String*, ManagedObject* > {};

    class JavaTypes : public MapEx<const String*, TypeDesc* > {};

    friend class GlobalClassLoaderIterator;
public:
    ClassLoader() : m_loader(NULL), m_parent(NULL), m_package_table(NULL), 
        m_loadedClasses(NULL), m_failedClasses(NULL), 
        m_loadingClasses(NULL), m_reportedClasses(NULL), m_javaTypes(NULL), m_nativeLibraries(NULL),
        m_markBit(0), m_unloading(false), m_fullSize(0), m_verifyData(NULL)
    {
        apr_pool_create(&pool, 0);
    }

    virtual ~ClassLoader();

    void ClassClearInternals(Class*); // clean internals when class is destroyed

    virtual bool Initialize( ManagedObject* loader = NULL );

    Class* LookupClass(const String* name) { 
        LMAutoUnlock aulock(&m_lock);
        Class** klass = m_loadedClasses->Lookup(name);
        return klass?*klass:NULL;
    }
    void InsertClass(Class* clss) {
        LMAutoUnlock aulock(&m_lock);
        m_loadedClasses->Insert(clss->name, clss);
    }
    Class* AllocateAndReportInstance(const Global_Env* env, Class* klass);
    Class* NewClass(const Global_Env* env, const String* name);
    ManagedObject** RegisterClassInstance(const String* className, ManagedObject* instance);
    Class* DefineClass(Global_Env* env, const char* class_name,
        uint8* bytecode, unsigned offset, unsigned length, const String** res_name = NULL);
    virtual Class* LoadClass( Global_Env* UNREF env, const String* UNREF name)
        { return NULL; }
    Class* LoadVerifyAndPrepareClass( Global_Env* env, const String* name);
    virtual void ReportException(const char* exn_name, std::stringstream& message_stream);
    virtual void ReportFailedClass(Class* klass, const char* exnclass, std::stringstream& exnmsg);
    void ReportFailedClass(Class* klass, const jthrowable exn);
    virtual void ReportFailedClass(const char* name, const char* exnclass, std::stringstream& exnmsg);
    jthrowable GetClassError(const char* name) {
        LMAutoUnlock aulock(&m_lock);
        String* nameString = VM_Global_State::loader_env->string_pool.lookup(name);
        FailedClass* fc = m_failedClasses->Lookup(nameString);
        if(!fc) return NULL;
        return (jthrowable)(&(fc->m_exception));
    }
    void LoadNativeLibrary( const char *name );
    GenericFunctionPointer LookupNative(Method*);
    void SetVerifyData( void *data ) { m_verifyData = data; }
    void* GetVerifyData( void ) { return m_verifyData; }
    void Lock() { m_lock._lock(); }
    void Unlock() { m_lock._unlock(); }
    void LockTypesCache() { m_types_cache_lock._lock(); }
    void UnlockTypesCache() { m_types_cache_lock._unlock(); }
    static void LockLoadersTable() { m_tableLock._lock(); }
    static void UnlockLoadersTable() { m_tableLock._unlock(); }
protected:
    Class* StartLoadingClass(Global_Env* env, const String* className);
    bool FinishLoadingClass(Global_Env *env, Class* clss,
        unsigned int* super_class_cp_index);
    void RemoveLoadingClass(const String* className, LoadingClass* loading);
    void SuccessLoadingClass(const String* className);
    void AddFailedClass(const String* className, const jthrowable exn);
    void FailedLoadingClass(const String* className);

public:
    bool IsBootstrap() { return m_loader == NULL; }
    void Mark() { m_markBit = 1; }
    bool NotMarked() { return (m_markBit == 0); }
    unsigned GetFullSize();
    ManagedObject* GetLoader() { return m_loader; }
    ClassLoader* GetParent() { return m_parent; }
    Package_Table* getPackageTable() { return m_package_table; }
    ClassTable* GetLoadedClasses() { return m_loadedClasses; }
    FailedClasses* GetFailedClasses() { return m_failedClasses; }
    LoadingClasses* GetLoadingClasses() { return m_loadingClasses; }
    ReportedClasses* GetReportedClasses() { return m_reportedClasses; }
    JavaTypes* GetJavaTypes() { return m_javaTypes; }

    // ClassLoaders collection interface and data
    // ppervov: I think we need separate class for this entity
    static ClassLoader* FindByObject( ManagedObject* loader );
    // ppervov: NOTE: LookupLoader has side effect of adding 'loader' to the collection
    VMEXPORT static ClassLoader* LookupLoader( ManagedObject* loader );
    static void UnloadClassLoader( ManagedObject* loader );
    static void gc_enumerate();
    static void ClearMarkBits();
    static void StartUnloading();
    static void PrintUnloadingStats();
    static unsigned GetClassLoaderNumber() { return m_nextEntry; }
    static ClassLoader** GetClassLoaderTable() { return m_table; }
    inline void* Alloc(size_t size) {
        assert(pool);
        Lock();
        void* ptr = apr_palloc(pool, size);
        Unlock();
        return ptr;
    }
private:
    static Lock_Manager m_tableLock;
    static unsigned m_capacity;
    static unsigned m_nextEntry;
    static ClassLoader** m_table;
    static unsigned m_unloadedBytes;
    static ClassLoader* AddClassLoader( ManagedObject* loader );
    static void ReallocateTable( unsigned int new_capacity );

protected:
    // data
    ManagedObject* m_loader;
    ClassLoader* m_parent;
    Package_Table* m_package_table;
    ClassTable* m_loadedClasses;
    FailedClasses* m_failedClasses;
    LoadingClasses* m_loadingClasses;
    ReportedClasses* m_reportedClasses;
    JavaTypes* m_javaTypes;
    NativeLibraryList m_nativeLibraries;
    Lock_Manager m_lock;
    Lock_Manager m_types_cache_lock;
    unsigned m_markBit:1;
    unsigned m_unloading;
    unsigned m_fullSize;
    void* m_verifyData;
    apr_pool_t* pool;

    // methods
    Class* WaitDefinition(Global_Env* env, const String* className);
    Class* SetupAsArray(Global_Env* env, const String* klass);
    Package* ProvidePackage(Global_Env* env, const String *class_name, const char *jar);

private:
    Class* InitClassFields(const Global_Env* env, Class* clss, const String* name);
    void FieldClearInternals(Class*); // clean Field internals in Class
}; // class ClassLoader

inline LoggerString& operator << (LoggerString& log, ClassLoader::FailedClass& fc)
{
    log << fc.m_name->bytes << " status: " << fc.m_exception;
    return log;
}

inline LoggerString& operator << (LoggerString& log, ClassLoader::LoadingClass& lc)
{
    log 
#ifdef _DEBUG
        << lc.m_name->bytes
#endif
        << " thread: " << lc.m_defineOwner << " " << lc.m_initiatingThread;
    return log;
}

#define REPORT_FAILED_CLASS_CLASS(loader, klass, exnname, exnmsg)   \
    {                                                               \
        std::stringstream ss;                                       \
        ss << exnmsg;                                               \
        loader->ReportFailedClass(klass, exnname, ss);              \
    }

#define REPORT_FAILED_CLASS_CLASS_EXN(loader, klass, exnhandle) \
    {                                                           \
        loader->ReportFailedClass(klass, exnhandle);            \
    }

#define REPORT_FAILED_CLASS_NAME(loader, name, exnname, exnmsg) \
    {                                                           \
        std::stringstream ss;                                   \
        ss << exnmsg;                                           \
        loader->ReportFailedClass(name, exnname, ss);           \
    }


class BootstrapClassLoader : public ClassLoader
{
public:
    struct BCPElement {
        bool m_isJarFile;
        const String* m_path;
        JarFile* m_jar;
        BCPElement* m_next;
    };
    struct BCPElements {
        BCPElements() : m_first(NULL), m_last(NULL) {}
        BCPElement *m_first;
        BCPElement *m_last;
    };

    BootstrapClassLoader(Global_Env* env);
    virtual ~BootstrapClassLoader();
    virtual Class* LoadClass(Global_Env* env, const String* name);
    virtual bool Initialize( ManagedObject* loader = NULL );
    // reloading error reporting in bootstrap class loader
    virtual void ReportException(const char* exn_name, std::stringstream& message_stream);
    virtual void ReportFailedClass(Class* klass, const char* exnclass, std::stringstream& exnmsg) {
        if(! m_env->IsReadyForExceptions()) {
            ReportAndExit(exnclass, exnmsg);
        }
        ClassLoader::ReportFailedClass(klass, exnclass, exnmsg);
    }
    virtual void ReportFailedClass(const char* name, const char* exnclass, std::stringstream& exnmsg) {
        if(! m_env->IsReadyForExceptions()) {
            ReportAndExit(exnclass, exnmsg);
        }
        ClassLoader::ReportFailedClass(name, exnclass, exnmsg);
    }

    // primitive types are introduced for caching purpose
    TypeDesc* get_primitive_type(Kind k){
        assert (k <= K_LAST_PRIMITIVE ); // primitive types are limited by K_LAST_PRIMITIVE bound
        return primitive_types[k];
    }

private:
    void ReportAndExit(const char* exnclass, std::stringstream& exnmsg);
    Class* LoadFromFile(const String* className);
    Class* LoadFromClassFile(const String* dir_name, const char* class_name_in_fs,
        const String* class_name, bool* not_found);
    Class* LoadFromJarFile( JarFile* jar_file,
        const char* class_name_in_jar, const String* class_name, bool* not_found);
    void SetClasspathFromProperty(const char* prop_string, apr_pool_t *tmp_pool);
    void SetBCPElement(const char *path, apr_pool_t *tmp_pool);
    void SetClasspathFromJarFile(JarFile *jar, apr_pool_t *tmp_pool);

    BCPElements m_BCPElements;
    Global_Env* m_env;
    // primitive types array, K_LAST_PRIMITIVE - upper bound of primitive types
    TypeDesc* primitive_types[K_LAST_PRIMITIVE + 1]; 
}; // class BootstrapClassLoader

class UserDefinedClassLoader : public ClassLoader
{
public:
    UserDefinedClassLoader() {}
    virtual Class* LoadClass(Global_Env* env, const String* name);
}; // class UserDefinedClassLoader

/**
 * Function looks for method in native libraries of class loader.
 *
 * @param method - searching native method structure
 *
 * @return Pointer to found native function.
 *
 * @note Function raises <code>UnsatisfiedLinkError</code> with method name
 *       in exception message if specified method is not found.
 */
VMEXPORT GenericFunctionPointer
classloader_find_native(const Method_Handle method);

#endif // _CLASSLOADER_H_
