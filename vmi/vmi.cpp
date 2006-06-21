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
 * @author Euguene Ostrovsky
 * @version $Revision: 1.1.2.1.4.5 $
 */  
#include <assert.h>

#include "vmi.h"
#include <stdarg.h>
#include <malloc.h>
#include <string.h>
#include <sys/time.h>

extern VMInterfaceFunctions_ vmi_impl;

VMInterface vmi = &vmi_impl;

struct VM {
    VMInterface vmi;
    JNIEnv *jni_env;
    JavaVM *vm;
} vm_info;

HyPortLibrary portLib;
HyPortLibrary* portLibPointer = NULL;
HyZipCachePool* zipCachePool = NULL;

VMInterface* JNICALL 
VMI_GetVMIFromJNIEnv(JNIEnv *env) 
{
    vm_info.vmi = &vmi_impl;
    vm_info.jni_env = env;
    return (VMInterface*)&vm_info;
}

VMInterface* JNICALL 
VMI_GetVMIFromJavaVM(JavaVM *vm) 
{
    vm_info.vmi = &vmi_impl;
    vm_info.vm = vm;
    return (VMInterface*)&vm_info;
}

//////////////////////////////////////
//  VMI structure member functions  //
//////////////////////////////////////

vmiError JNICALL CheckVersion(VMInterface *vmi, vmiVersion *version)
{
    assert(/* vmi.dll:  unimplemented. */0);
    return VMI_ERROR_UNIMPLEMENTED;
}

JavaVM *JNICALL GetJavaVM(VMInterface *vmi)
{
    assert(/* vmi.dll:  unimplemented. */0);
    return NULL;
}

//#ifdef __cplusplus
//extern "C" {
//#endif
//JNIEXPORT int j9port_allocate_library(void *version, J9PortLibrary **portLibrary);
//#ifdef __cplusplus
//}
//#endif
 
HyPortLibrary *JNICALL GetPortLibrary(VMInterface *vmi)
{
    static int initialized = 0;

    if (! initialized)
    {
        int rc;
        HyPortLibraryVersion portLibraryVersion;
        HYPORT_SET_VERSION(&portLibraryVersion, HYPORT_CAPABILITY_MASK);

        rc = hyport_init_library(&portLib, &portLibraryVersion, 
                sizeof(HyPortLibrary));

        if (0 != rc) return NULL;

        initialized = 1;

    // FIXME: portlib is used in VMI_zipCachePool - we need to
    //        know there is portLib is initialized there already.
    portLibPointer = &portLib;
    }
    return &portLib;
}




UDATA JNICALL HYVMLSAllocKeys(JNIEnv *env, UDATA *pInitCount,...) {
    //fprintf(stderr, "HYVMLSAllocKeys\n");
    return 0;
}

void JNICALL HYVMLSFreeKeys(JNIEnv *env, UDATA *pInitCount,...) {
    //fprintf(stderr, "HYVMLSFreeKeys - not implemented\n");
}

char somedata[1024];

void* JNICALL HYVMLSGet(JNIEnv *env, void *key) {
    //fprintf(stderr, "HYVMLSGet[%p] =", key);
    //fprintf(stderr, " %p\n", res);
    return key;
}
void* JNICALL HYVMLSSet(JNIEnv *env, void **pKey, void *value) {
    //fprintf(stderr, "HYVMLSSet[%p] %p\n", pKey, value);
    *pKey = value;
    return 0;
}


HyVMLSFunctionTable vmls_inst = {
    &HYVMLSAllocKeys,
    &HYVMLSFreeKeys,
    &HYVMLSGet,
    &HYVMLSSet
};

/*
 * Returns a pointer to Local Storage Function Table. 
 */
HyVMLSFunctionTable* JNICALL 
GetVMLSFunctions(VMInterface *vmi)
{
    //fprintf(stderr, "GetVMLSFunctions\n");
    HyVMLSFunctionTable *pl = &vmls_inst;
    return pl;
}

HyZipCachePool* JNICALL GetZipCachePool(VMInterface *vmi)
{
    // FIXME: thread unsafe implementation...
    if (zipCachePool != NULL)
    {
        return zipCachePool;
    }
    assert(portLibPointer);
    zipCachePool = zipCachePool_new(&portLib);
    assert(zipCachePool);
    return zipCachePool;
}

JavaVMInitArgs* JNICALL GetInitArgs(VMInterface *vmi)
{
    JavaVMInitArgs *args = (JavaVMInitArgs*) malloc(sizeof(JavaVMInitArgs));
    args->version = JNI_VERSION_1_2;
	args->nOptions = 0;
	args->options = 0;
	args->ignoreUnrecognized = TRUE;
    fprintf(stderr, "GetInitArgs\n");
    return args;
}

vmiError JNICALL 
GetSystemProperty(VMInterface *vmi, char *key, char **valuePtr)
{
    fprintf(stderr, "GetSystemProperty: %s\n", key);
    VM *vm = (VM*)vmi;
    JNIEnv *e = vm->jni_env;
    *valuePtr = NULL;

    if (key == NULL) return VMI_ERROR_NONE;

    jstring str = e->NewStringUTF(key);
    if (!str) return VMI_ERROR_OUT_OF_MEMORY;

    jclass system = e->FindClass("java/lang/System");
    if (!system) {
        fprintf(stderr, "VMI Failed to find class java/lang/System\n");
        e->ExceptionDescribe();
        return VMI_ERROR_UNKNOWN;
    }

    jmethodID getProperty = e->GetStaticMethodID(system, "getProperty",
            "(Ljava/lang/String;)Ljava/lang/String;");
    if (!getProperty) return VMI_ERROR_UNKNOWN;

    jstring prop = (jstring) e->CallStaticObjectMethod(system, getProperty, str);
    if (e->ExceptionOccurred()) return VMI_ERROR_UNKNOWN;
    if (!prop) return VMI_ERROR_NONE;

    int len = e->GetStringUTFLength(prop);
    char *res = (char*) malloc(len + 1);
    if (!res) return VMI_ERROR_OUT_OF_MEMORY;

    jboolean copy;
    const char *chars = e->GetStringUTFChars(prop, &copy);
    if (!chars) return VMI_ERROR_OUT_OF_MEMORY;
    memcpy(res, chars, len);
    res[len] = 0;
    e->ReleaseStringUTFChars(prop, chars);

    fprintf(stderr, "%s = %s\n", key, res);
    *valuePtr = res;
    return VMI_ERROR_NONE;
}

vmiError JNICALL
SetSystemProperty(VMInterface *vmi, char *key, char *value)
{
    fprintf(stderr, "SetSystemProperty: %s = %s\n", key, value);
    return VMI_ERROR_NONE;
}

vmiError JNICALL CountSystemProperties(VMInterface *vmi, int *countPtr)
{
    *countPtr = 0;
    fprintf(stderr, "CountSystemProperties\n");
    return VMI_ERROR_NONE;
}

vmiError JNICALL IterateSystemProperties(VMInterface *vmi,
        vmiSystemPropertyIterator iterator, void *userData)
{
    fprintf(stderr, "IterateSystemProperties\n");
    return VMI_ERROR_NONE;
}

VMInterfaceFunctions_ vmi_impl = {
    &CheckVersion,
    &GetJavaVM,
    &GetPortLibrary,
    &GetVMLSFunctions,
    &GetZipCachePool,
    &GetInitArgs,
    &GetSystemProperty,
    &SetSystemProperty,
    &CountSystemProperties,
    &IterateSystemProperties,
};


extern "C" {
    /*
     * Class:     java_lang_System
     * Method:    getcwd
     * Signature: ()Ljava/lang/String;
     */
    JNIEXPORT jstring JNICALL Java_java_lang_System_getcwd
        (JNIEnv *env, jclass)
    {
        int len = 128;
        while (true) {
            char *buf = (char*) malloc(len);
            if (getcwd(buf, len) != NULL) {
                jstring str = env->NewStringUTF(buf);
                free(buf);
                return str;
            }
            free(buf);
            len *= 2;
        }
    }

    /*
     * Class:     java_lang_System
     * Method:    currentTimeMillis
     * Signature: ()J
     */
    JNIEXPORT jlong JNICALL Java_java_lang_System_currentTimeMillis
        (JNIEnv *, jclass)
    {
        struct timeval tv;
        gettimeofday(&tv, NULL);
        return tv.tv_sec * (jlong)1000 + tv.tv_usec / 1000;
    }

    /*
     * Class:     java_lang_System
     * Method:    setPrintStream
     * Signature: (Ljava/lang/String;Ljava/io/PrintStream;)V
     */
    JNIEXPORT void JNICALL Java_java_lang_System_setPrintStream
        (JNIEnv *env, jclass c, jstring field, jobject stream)
    {
        jfieldID fid;
        const char *field_name = env->GetStringUTFChars(field, 0);
        if (!field_name) {
            return;
        }
        fid = env->GetStaticFieldID(c, field_name, "Ljava/io/PrintStream;");
        env->ReleaseStringUTFChars(field, field_name);
        if (!fid) {
            return;
        }
        env->SetStaticObjectField(c, fid, stream);
        return;
    }

    /*
     * Class:     java_lang_System
     * Method:    setInImpl
     * Signature: (Ljava/io/InputStream;)V
     */
    JNIEXPORT void JNICALL Java_java_lang_System_setInImpl
        (JNIEnv *env, jclass c, jobject stream)
    {
        jfieldID fid;
        fid = env->GetStaticFieldID(c, "in", "Ljava/io/InputStream;");
        if (!fid) {
            return;
        }
        env->SetStaticObjectField(c, fid, stream);
        return;
    }

    JNIEXPORT void JNICALL Java_java_lang_VMThread_attach
        (JNIEnv *env, jobject) {
        hythread_attach(NULL);
    }

    JNIEXPORT void JNICALL Java_java_lang_VMThread_destroy
        (JNIEnv *env, jobject) {
        hythread_detach(NULL);
    }
}
