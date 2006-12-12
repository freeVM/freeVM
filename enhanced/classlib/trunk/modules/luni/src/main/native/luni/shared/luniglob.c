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

/* HarmonyDoxygen */
/**
 * @file
 * @ingroup HarmonyNatives
 * @brief Harmony LUNI natives initialization API.
 */
#include <search.h>
#include <string.h>
#include <stdlib.h>
#include "vmi.h"
#include "jclglob.h"
#include "hyport.h"
#include "strhelp.h"
#include "jsig.h"
#include "hycomp.h"

static UDATA keyInitCount = 0;

void *JCL_ID_CACHE = NULL;

static int props_compare(const void *arg1, const void *arg2);
static jint readClassPathFromPropertiesFile (VMInterface *vmInterface);
static void freeReferences (JNIEnv * env);

/**
 * This DLL is being loaded, do any initialization required.
 * This may be called more than once.
 */
JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void *reserved)
{
  JniIDCache *idCache;
  JNIEnv *env;
  void *keyInitCountPtr = GLOBAL_DATA (keyInitCount);
  void **jclIdCache = GLOBAL_DATA (JCL_ID_CACHE);
  VMInterface *vmInterface;
  char *bootPath = NULL;

#if defined(LINUX)
  /* all UNIX platforms */
  HySignalHandler previousGpHandler;
  HySigSet (SIGPIPE, SIG_IGN, previousGpHandler);
#endif

  /* Query the VM interface */
  vmInterface = VMI_GetVMIFromJavaVM (vm);
  if (!vmInterface)
    {
      goto fail;
    }

  if ((*vm)->GetEnv (vm, (void **) &env, JNI_VERSION_1_2) == JNI_OK)
    {
      PORT_ACCESS_FROM_ENV (env);

      if (HY_VMLS_FNTBL (env)->
          HYVMLSAllocKeys (env, keyInitCountPtr, jclIdCache, NULL))
        {
          goto fail;
        }

      idCache = (JniIDCache *) hymem_allocate_memory (sizeof (JniIDCache));
      if (!idCache)
        goto fail2;

      memset (idCache, 0, sizeof (JniIDCache));
      HY_VMLS_SET (env, *jclIdCache, idCache);

      JCL_CACHE_SET (env, realPortArray, NULL);
      JCL_CACHE_SET (env, synthPortArray, NULL);
      JCL_CACHE_SET (env, portListLen, 0);

      /* Attach to the common library */
      if (JNI_OK != ClearLibAttach (env))
        {
          goto fail2;
        }

       /* Should we check if bootclasspath is already set to avoid unexpected overriding? */
       /*  But this seems to conflict with default IBM VME settings...*/
       /* (*vmInterface)->GetSystemProperty (vmInterface, BOOTCLASSPATH_PROPERTY, &bootPath);*/
       if (!bootPath) 
       {
           int i;
           int bootClassPathSet = 0;

            /* Grab the VM command line arguments */  
           JavaVMInitArgs *vmArgs = (*vmInterface)->GetInitArgs (vmInterface);
           if (!vmArgs) {
               goto fail2;
           }
      
           /* Before we try to set the bootclasspath, check that it has not been specified
            explicitly on the command line */
           for ( i = 0; i < vmArgs->nOptions; i++ ) 
           {
                JavaVMOption *currentOption = &(vmArgs->options[i]);
                if ( strstr( currentOption->optionString, "-Xbootclasspath:" ) )
                { 
                    bootClassPathSet = 1;
                    break;
                }
           }
        
           /* Only read bootsclasspath.properties if -Xbootclasspath: has not been specified */
           if (0 == bootClassPathSet) 
           {
               /* Initialize bootstrap classpath */
               if (JNI_OK != readClassPathFromPropertiesFile (vmInterface))
               {
                   goto fail2;
               }
           }
       }
       return JNI_VERSION_1_2;
    }

fail2:
  HY_VMLS_FNTBL (env)->HYVMLSFreeKeys (env, keyInitCountPtr, jclIdCache, NULL);
fail:
  return 0;
}

/**
 * This DLL is being unloaded, do any clean up required.
 * This may be called more than once!!
 */
JNIEXPORT void JNICALL
JNI_OnUnload (JavaVM * vm, void *reserved)
{
  JNIEnv *env;
  void *keyInitCountPtr = GLOBAL_DATA (keyInitCount);
  void **jclIdCache = GLOBAL_DATA (JCL_ID_CACHE);

  int i, listlen;
  char **portArray, **portArray2;

  if ((*vm)->GetEnv (vm, (void **) &env, JNI_VERSION_1_2) == JNI_OK)
    {
      JniIDCache *idCache = (JniIDCache *) HY_VMLS_GET (env, *jclIdCache);

      if (idCache)
        {
          PORT_ACCESS_FROM_ENV (env);

          /* Detach from the common library */
          ClearLibDetach (env);

          /*free the arrays of available portnames */
          portArray = JCL_CACHE_GET (env, realPortArray);
          if (portArray != NULL)
            {
              portArray2 = JCL_CACHE_GET (env, synthPortArray);
              listlen = JCL_CACHE_GET (env, portListLen);
              for (i = 0; i < listlen; i++)
                {
                  if (portArray[i] != NULL)
                    hymem_free_memory (portArray[i]);
                  if (portArray2[i] != NULL)
                    hymem_free_memory (portArray2[i]);
                }
              hymem_free_memory (portArray);
              hymem_free_memory (portArray2);
              JCL_CACHE_SET (env, realPortArray, NULL);
              JCL_CACHE_SET (env, synthPortArray, NULL);
              JCL_CACHE_SET (env, portListLen, 0);
            }

          /* Free any global references */
          freeReferences (env);

          /* Free VMLS keys */
          idCache = (JniIDCache *) HY_VMLS_GET (env, *jclIdCache);
          HY_VMLS_FNTBL (env)->HYVMLSFreeKeys (env, keyInitCountPtr,
                                              jclIdCache, NULL);
          hymem_free_memory (idCache);
        }
    }
}

static int props_compare(const void *arg1, const void *arg2)
{
    key_value_pair p1 = *(key_value_pair*)arg1;
    key_value_pair p2 = *(key_value_pair*)arg2;
    size_t l1 = strlen(p1.key);
    size_t l2 = strlen(p2.key);
    if (l1 < l2)
        return -1;
    if (l2 < l1)
        return 1;
    return strcmp(p1.key, p2.key);
}

/**
 * Initializes the bootstrap classpath used by the VM with entries suitable for this
 * class library configuration.  Stores the result into a system property named
 * 'org.apache.harmony.boot.class.path'.
 *
 * @param vmInterface - the VMI interface pointer.
 *
 * @return - JNI_OK on success, or a JNI error code on failure.
 */
static jint
readClassPathFromPropertiesFile (VMInterface *vmInterface)
{
    HyPortLibrary *privatePortLibrary;
    char *javaHome;
    char *bootDirectory;
    char *propsFile;
    char *bootstrapClassPath = NULL;
    vmiError rcGetProperty;
    jint returnCode;
    key_value_pair * props = NULL;
    U_32 number;

    /* Extract the port library */
    privatePortLibrary = (*vmInterface)->GetPortLibrary (vmInterface);
    if (!privatePortLibrary)
    {
        return JNI_ERR;
    }

    /* Load the java.home system property */
    rcGetProperty =
        (*vmInterface)->GetSystemProperty (vmInterface, "java.home", &javaHome);
    if (VMI_ERROR_NONE != rcGetProperty)
    {
        return JNI_ERR;
    }

    /* Locate the boot directory in ${java.home}\lib\boot */
    bootDirectory = str_concat(PORTLIB, javaHome, DIR_SEPARATOR_STR, "lib", 
        DIR_SEPARATOR_STR, "boot", DIR_SEPARATOR_STR, NULL);
    if (!bootDirectory) 
    {
        return JNI_ENOMEM;
    }

    /* Build up the location of the properties file relative to java.home */
    propsFile = str_concat(PORTLIB, bootDirectory, "bootclasspath.properties", NULL);
    if (!propsFile) 
    {
        returnCode = JNI_ENOMEM;
        goto cleanup;
    }

    returnCode = properties_load(PORTLIB, propsFile, &props, &number);

    bootstrapClassPath = "";

    if (JNI_OK == returnCode && number != 0)
    {
        unsigned i = 0;
        /* Make a string version of the CP separator */
        char cpSeparator[] = {(char)hysysinfo_get_classpathSeparator (), '\0'};
		
		/* Read current value of bootclasspath property */
        rcGetProperty = (*vmInterface)->GetSystemProperty (vmInterface,
            BOOTCLASSPATH_PROPERTY,
            &bootstrapClassPath);

        if (VMI_ERROR_NONE != rcGetProperty)
        {
            returnCode = JNI_ERR;
            goto cleanup;
        }

        qsort(props, number, sizeof(key_value_pair), props_compare);

        for (;i < number; i++) 
        {
            int digit;
            int tokensScanned =
                sscanf (props[i].key, "bootclasspath.%d", &digit);
            /* Ignore anything except bootclasspath.<digit> */
            if (tokensScanned == 1)
            {
                char *oldPath = bootstrapClassPath;
                bootstrapClassPath = str_concat (PORTLIB, 
                    bootstrapClassPath, cpSeparator,
                    bootDirectory, props[i].value, NULL);
                if (i != 0) 
                {
                    hymem_free_memory (oldPath);
                }

                if (!bootstrapClassPath)
                {
                    returnCode = JNI_ENOMEM;      /* bail - memory allocate must have failed */
                    break;
                }
            }
        }
    }
    
cleanup:
    if (props) {
        properties_free(PORTLIB, props);
    }
    if (bootDirectory) {
        hymem_free_memory(bootDirectory);
    }
    if (propsFile) {
        hymem_free_memory(propsFile);
    }

    /* Commit the full bootstrap class path into the VMI */
    if (bootstrapClassPath)
    {
        vmiError rcSetProperty = (*vmInterface)->SetSystemProperty (vmInterface,
            BOOTCLASSPATH_PROPERTY,
            bootstrapClassPath);
        if (VMI_ERROR_NONE != rcSetProperty)
        {
            returnCode = JNI_ERR;
        }
        hymem_free_memory (bootstrapClassPath);
    }

    return returnCode;
}

/**
 * @internal
 */
static void
freeReferences (JNIEnv * env)
{
  jclass classRef;

  /* clean up class references */
  classRef = JCL_CACHE_GET (env, CLS_java_lang_Boolean);
  if (classRef)
    (*env)->DeleteWeakGlobalRef (env, (jweak) classRef);

  classRef = JCL_CACHE_GET (env, CLS_java_lang_Byte);
  if (classRef)
    (*env)->DeleteWeakGlobalRef (env, (jweak) classRef);

  classRef = JCL_CACHE_GET (env, CLS_java_lang_Integer);
  if (classRef)
    (*env)->DeleteWeakGlobalRef (env, (jweak) classRef);

  classRef = JCL_CACHE_GET (env, CLS_java_net_InetAddress);
  if (classRef)
    (*env)->DeleteWeakGlobalRef (env, (jweak) classRef);

  classRef = JCL_CACHE_GET (env, CLS_array_of_byte);
  if (classRef)
    (*env)->DeleteWeakGlobalRef (env, (jweak) classRef);
}
