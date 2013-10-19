/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */    

/**
 * @author Vladimir Nenashev
 * @version $Revision: 1.4 $
 */

#include<stdio.h>
#include<jni.h>
#include"LocalRefsTest2.h"

JNIEXPORT void JNICALL
Java_org_apache_harmony_test_stress_jni_localrefs_LocalRefsTest2_init(JNIEnv* env,
                                                                      jclass c) {
  LocalRefsTest_init(env, c);
}

JNIEXPORT void JNICALL
Java_org_apache_harmony_test_stress_jni_localrefs_LocalRefsTest2_nativeMethod(JNIEnv* env,
                                                                              jobject thisObject,
                                                                              jint cnt) {
  int i;
  printf("\nNative code: creating %d local refs to \"this\"...\n", cnt);
  for (i = 0; i < cnt; i++) {
    jobject obj;
    if( (*env)->EnsureLocalCapacity(env, 1) ) {
      obj = NULL;
    } else {
      obj = (*env)->NewLocalRef(env, thisObject);
    }
    if (obj == NULL) {
      printf("\nNative code: NULL returned at i=%d\n", i);
      return;
    } else {
      int mod = cnt / 50;
      if (mod == 0 || (i % mod) == 0) {
          printf("Native code: creating local references: %d created\n", i);
      }
    }
  }
  return;
}
