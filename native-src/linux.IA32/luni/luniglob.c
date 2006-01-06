/* Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* HarmonyDoxygen */
/**
 * @file
 * @ingroup HarmonyNatives
 * @brief Harmony LUNI natives initialization API.
 */

#include <string.h>
#include "jcl.h"
#include "jclglob.h"

static UDATA keyInitCount = 0;

void *JCL_ID_CACHE = NULL;

/* props_file_entry */

#include "hypool.h"

/**
  * A structure that captures a single key-value setting from the properties file.
  */
typedef struct props_file_entry
{
  char *key;                            /**< The key as it appears in the properties file */
  char *value;                          /**< The value as it appears in the properties file */
} props_file_entry;

static jint readClassPathFromPropertiesFile (JavaVM * vm);
static jint decodeProperty (HyPortLibrary * portLibrary, char **scanCursor,
                            HyPool * properties);
static jint readPropertiesFile (HyPortLibrary * portLibrary, char *filename,
                                HyPool * properties);
static char *concat (HyPortLibrary * portLibrary, ...);
static void freeReferences (JNIEnv * env);

/**
 * This DLL is being loaded, do any initialization required.
 * This may be called more than once.
 */
jint JNICALL
JNI_OnLoad (JavaVM * vm, void *reserved)
{
  JniIDCache *idCache;
  JNIEnv *env;
  void *keyInitCountPtr = GLOBAL_DATA (keyInitCount);
  void **jclIdCache = GLOBAL_DATA (JCL_ID_CACHE);
  jint rcBpInit;
  VMInterface *vmInterface;
  JavaVMInitArgs *vmArgs;
  JavaVMOption *currentOption;
  int i;
  int bootClassPathSet = 0;

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

      /* This allocate must actually be done by hymem_allocate_memory */
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
      /* Grab the VM command line arguments */  
      vmArgs = (*vmInterface)->GetInitArgs (vmInterface);
      if (!vmArgs)
        {
          return JNI_ERR;
        }
  
      /* Before we try to set the bootclasspath, check that it has not been specified
         explicitly on the command line */
      for ( i = 0; i < vmArgs->nOptions; i++ ) 
        {
          currentOption = &(vmArgs->options[i]);
          if ( strstr( currentOption->optionString, "-Xbootclasspath:" ) )
            { 
              bootClassPathSet = 1;
            }
        }
    
      /* Only read bootsclasspath.properties if -Xbootclasspath: has not been specified */
      if (0 == bootClassPathSet) 
        {
          /* Initialize bootstrap classpath */
          rcBpInit = readClassPathFromPropertiesFile (vm);
          if (JNI_OK != rcBpInit)
            {
              goto fail2;
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
void JNICALL
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

/**
 * Concatenates a variable number of null-terminated strings into a single string
 * using the specified port library to allocate memory.  The variable number of
 * strings arguments must be terminated by a single NULL value.
 *
 * @param portLibrary - The port library used to allocate memory.
 * @return The concatenated string.
 */

static char *
concat (HyPortLibrary * portLibrary, ...)
{
  PORT_ACCESS_FROM_PORT (portLibrary);
  va_list argp;
  char *concatenated;
  UDATA concatenatedSize = 0;

  /* Walk the variable arguments once to compute the final size */
  va_start (argp, portLibrary);
  while (1)
    {
      char *chunk = va_arg (argp, char *);
      if (chunk)
        {
          concatenatedSize += strlen (chunk);
        }
      else
        {
          break;
        }
    }
  va_end (argp);

  /* Allocate concatenated space */
  concatenated =
    hymem_allocate_memory (concatenatedSize + 1 /* for null terminator */ );
  if (!concatenated)
    {
      return NULL;
    }
  concatenated[0] = '\0';

  /* Walk again concatenating the pieces */
  va_start (argp, portLibrary);
  while (1)
    {
      char *chunk = va_arg (argp, char *);
      if (chunk)
        {
          strcat (concatenated, chunk);
        }
      else
        {
          break;
        }
    }
  va_end (argp);

  return concatenated;
}

/**
  * Read the properties file specified by <tt>filename</tt> into the pool of <tt>properties</tt>.
  *
  * @param portLibrary - The port library used to interact with the platform.
  * @param filename - The file from which to read data using hyfile* functions.
  * @param properties - A pool that will contain property file entries.
  *
  * @return JNI_OK on success, or a JNI error code on failure.
  */
static jint
readPropertiesFile (HyPortLibrary * portLibrary, char *filename,
                    HyPool * properties)
{
  PORT_ACCESS_FROM_PORT (portLibrary);
  IDATA propsFD = -1;
  I_64 seekResult;
  IDATA fileSize;
  IDATA bytesRemaining;
  jint returnCode = JNI_OK;
  char *fileContents = NULL;
  char *writeCursor;
  char *scanCursor, *scanLimit;

  /* Determine the file size, fail if > 2G */
  seekResult = hyfile_length (filename);
  if ((seekResult <= 0) || (seekResult > 0x7FFFFFFF))
    {
      return JNI_ERR;
    }
  fileSize = (IDATA) seekResult;

  /* Open the properties file */
  propsFD = hyfile_open (filename, (I_32) HyOpenRead, (I_32) 0);
  if (propsFD == -1)
    {
      /* Could not open the file */
      return JNI_ERR;
    }

  /* Allocate temporary storage */
  fileContents = hymem_allocate_memory (fileSize);
  if (!fileContents)
    {
      return JNI_ENOMEM;
    }

  /* Initialize the read state */
  bytesRemaining = fileSize;
  writeCursor = fileContents;

  /* Suck the file into memory */
  while (bytesRemaining > 0)
    {
      IDATA bytesRead = hyfile_read (propsFD, writeCursor, bytesRemaining);
      if (bytesRead == -1)
        {
          /* Read failed */
          returnCode = JNI_ERR;
          goto bail;
        }

      /* Advance the read state */
      bytesRemaining -= bytesRead;
      writeCursor += bytesRead;
    }

  /* Set up scan and limit points */
  scanCursor = fileContents;
  scanLimit = fileContents + fileSize;

  /* Now crack the properties */
  while (scanCursor < scanLimit)
    {

      /* Decode a property and advance the scan cursor */
      int numberDecoded = decodeProperty (PORTLIB, &scanCursor, properties);

      /* Bail if we encounter an error */
      if (numberDecoded < 0)
        {
          returnCode = JNI_ENOMEM;
          break;
        }
    }

bail:
  if (propsFD != -1)
    {
      hyfile_close (propsFD);
    }
  if (fileContents)
    {
      hymem_free_memory (fileContents);
    }

  return returnCode;
}

/**
   * Scans the buffer specified by scanCursor and attempts to locate the next
  *  key-value pair separated by the '=' sign, and terminated by the platform line
  * delimiter.
  * 
  * If a key-value pair is located a new props_file_entry structure will be allocated in
  * the <tt>properties</tt> pool, and the key and value will be copied.  The scanCursor
  * will be advanced past the entire property entry on success.
  *
  *
  * @param portLibrary - The port library used to interact with the platform.
  * @param scanCursor - A null-terminated string containing one or more (or partial) properties.
  * @param properties - A pool from which props_file_entry structures are allocated.
  *
  * @return The number of properties read, -1 on error.
  * @note This function modifies the buffer as properties are consumed.
  */
static jint
decodeProperty (HyPortLibrary * portLibrary, char **scanCursor,
                HyPool * properties)
{
  PORT_ACCESS_FROM_PORT (portLibrary);
  props_file_entry *property;
  int keyLength, valueLength;
  char *equalSign;
  char *lineDelimiter;
  char *propertyBuffer = *scanCursor;

  lineDelimiter = strstr (propertyBuffer, PLATFORM_LINE_DELIMITER);
  if (lineDelimiter)
    {
      /* Hammer the line delimiter to be a null */
      *lineDelimiter = '\0';
      *scanCursor = lineDelimiter + strlen (PLATFORM_LINE_DELIMITER);
    }
  else
    {
      /* Assume the entire text is a single token */
      *scanCursor = propertyBuffer + strlen (propertyBuffer) - 1;
    }

  /* Now find the '=' character */
  equalSign = strchr (propertyBuffer, '=');
  if (!equalSign)
    {
      /* Malformed, assume it's a comment and move on */
      return 0;
    }

  /* Allocate a new pool entry */
  property = pool_newElement (properties);
  if (!property)
    {
      return 0;
    }

  /* Compute the key length and allocate memory */
  keyLength = equalSign - propertyBuffer;
  property->key =
    hymem_allocate_memory (keyLength + 1 /* for null terminator */ );
  if (!property->key)
    {
      goto bail;
    }

  /* Compute the value length and allocate memory */
  memcpy (property->key, propertyBuffer, keyLength);
  property->key[keyLength] = '\0';

  /* Compute the value length and allocate memory */
  valueLength = strlen (propertyBuffer) - keyLength - 1 /* for equal sign */ ;
  property->value =
    hymem_allocate_memory (valueLength + 1 /* for null terminator */ );
  if (!property->value)
    {
      goto bail;
    }

  /* Compute the value length and allocate memory */
  memcpy (property->value, equalSign + 1, valueLength);
  property->value[valueLength] = '\0';

  /* Advance the scan cursor */
  return 1;

bail:
  if (property != NULL)
    {
      if (property->key)
        {
          hymem_free_memory (property->key);
        }
      if (property->value)
        {
          hymem_free_memory (property->value);
        }
      pool_removeElement (properties, property);
    }

  return -1;
}

/**
 * Initializes the bootstrap classpath used by the VM with entries suitable for this
 * class library configuration.  Stores the result into a system property named
 * 'com.ibm.oti.system.class.path'.
 *
 * Reads the bootclasspath.properties file a line at a time 
 *
 * @param vm - The JavaVM from which port library and VMI interfaces can be obtained.
 *
 * @return - JNI_OK on success.
 */
static jint
readClassPathFromPropertiesFile (JavaVM * vm)
{
  VMInterface *vmInterface;
  HyPortLibrary *privatePortLibrary;
  char *javaHome;
  char bootDirectory[HyMaxPath];
  char propsFile[HyMaxPath];
  char cpSeparator[2];
  char *bootstrapClassPath = NULL;
  char *fileWritePos;
  vmiError rcGetProperty;
  jint returnCode = JNI_OK;
  IDATA propsFD = -1;
  HyPool *properties;

  /* Query the VM interface */
  vmInterface = VMI_GetVMIFromJavaVM (vm);
  if (!vmInterface)
    {
      return JNI_ERR;
    }

  /* Extract the port library */
  privatePortLibrary = (*vmInterface)->GetPortLibrary (vmInterface);
  if (!privatePortLibrary)
    {
      return JNI_ERR;
    }

  /* Make a string version of the CP separator */
  cpSeparator[0] = hysysinfo_get_classpathSeparator ();
  cpSeparator[1] = '\0';

  /* Load the java.home system property */
  rcGetProperty =
    (*vmInterface)->GetSystemProperty (vmInterface, "java.home", &javaHome);
  if (VMI_ERROR_NONE != rcGetProperty)
    {
      return JNI_ERR;
    }

  /* Locate the boot directory in ${java.home}\lib\boot */
  strcpy (bootDirectory, javaHome);

  /* Tack on a trailing separator if needed */
  if (bootDirectory[strlen (javaHome) - 1] != DIR_SEPARATOR)
    {
      strcat (bootDirectory, DIR_SEPARATOR_STR);
    }

  /* Now tack on the lib/boot portion */
  strcat (bootDirectory, "lib");
  strcat (bootDirectory, DIR_SEPARATOR_STR);
  strcat (bootDirectory, "boot");
  strcat (bootDirectory, DIR_SEPARATOR_STR);

  /* Build up the location of the properties file relative to java.home */
  propsFile[0] = '\0';
  strcat (propsFile, bootDirectory);
  strcat (propsFile, "bootclasspath.properties");

  /* Create a pool to hold onto properties */
  properties =
    pool_new (sizeof (props_file_entry), 0, 0, 0, POOL_FOR_PORT (PORTLIB));
  if (!properties)
    {
      returnCode = JNI_ENOMEM;
      goto cleanup;
    }

  /* Pull the properties into the pool */
  if (JNI_OK == readPropertiesFile (PORTLIB, propsFile, properties))
    {
      pool_state poolState;

      props_file_entry *property = pool_startDo (properties, &poolState);
      while (property)
        {
          int digit;
          int tokensScanned =
            sscanf (property->key, "bootclasspath.%d", &digit);

          /* Ignore anything except bootclasspath.<digit> */
          if (tokensScanned == 1)
            {

              /* The first and subsequent entries are handled slightly differently */
              if (bootstrapClassPath)
                {
                  char *oldPath = bootstrapClassPath;
                  bootstrapClassPath =
                    concat (PORTLIB, bootstrapClassPath, cpSeparator,
                            bootDirectory, property->value, NULL);
                  hymem_free_memory (oldPath);
                }
              else
                {
                  bootstrapClassPath =
                    concat (PORTLIB, "", bootDirectory, property->value,
                            NULL);
                }

              if (!bootstrapClassPath)
                {
                  returnCode = JNI_ENOMEM;      /* bail - memory allocate must have failed */
                  break;
                }
            }

          /* Advance to next element */
          property = (props_file_entry *) pool_nextDo (&poolState);
        }
    }

cleanup:

  /* Free the properties pool (remember to free structure elements) */
  if (properties)
    {
      pool_state poolState;
      props_file_entry *property = pool_startDo (properties, &poolState);

      while (property)
        {
          if (property->key)
            {
              hymem_free_memory (property->key);
            }
          if (property->value)
            {
              hymem_free_memory (property->value);
            }
          property = (props_file_entry *) pool_nextDo (&poolState);
        }
      pool_kill (properties);
    }

  /* Commit the full bootstrap class path into the VMI */
  if (bootstrapClassPath)
    {
      vmiError rcSetProperty = (*vmInterface)->SetSystemProperty (vmInterface,
                                                                  "com.ibm.oti.system.class.path",
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
