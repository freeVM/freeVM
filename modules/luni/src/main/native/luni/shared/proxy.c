/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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

#include "iohelp.h"
#include "exceptions.h"
#include "jclglob.h"

jclass JNICALL
Java_java_lang_reflect_Proxy_defineClassImpl (JNIEnv * env, jclass recvClass,
                                              jobject classLoader,
                                              jstring className,
                                              jbyteArray classBytes)
{
  const char *name;
  jbyte *bytes;
  jclass returnClass;
  jint length;

  name = (*env)->GetStringUTFChars (env, className, NULL);
  if (!name)
    {
      throwNewOutOfMemoryError (env, "");
      return 0;
    };
  bytes = (*env)->GetByteArrayElements (env, classBytes, NULL);
  if (!bytes)
    {
      (*env)->ReleaseStringUTFChars (env, className, name);
      throwNewOutOfMemoryError (env, "");
      return 0;
    }
  length = (*env)->GetArrayLength (env, classBytes);

  returnClass = (*env)->DefineClass (env, name, classLoader, bytes, length);

  (*env)->ReleaseByteArrayElements (env, classBytes, bytes, JNI_COMMIT);
  (*env)->ReleaseStringUTFChars (env, className, name);
  return returnClass;
}
