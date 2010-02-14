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

#include <stdlib.h>
#include <time.h>
#include <limits.h>

#include "vmi.h"
#include "jni.h"
#include "hyport.h"
#include "hycomp.h"

JNIEXPORT jint JNICALL
Java_org_apache_harmony_security_provider_crypto_RandomBitsSupplier_getUnixSystemRandom(JNIEnv *env, jclass obj, jbyteArray bytes, jint numBytes)
{
    PORT_ACCESS_FROM_ENV(env);
    jbyte *randomBits = hymem_allocate_memory(numBytes * sizeof(jbyte));

    clock_t processTime = clock();
    time_t currentTime = time(NULL);

    int i;

    // Check for error return values
    if ((!randomBits) || (-1 == processTime) || (-1 == currentTime)) {
        return 0;
    }

    // Seed the random number generator
    srandom(abs((currentTime * processTime * (long)randomBits) % INT_MAX));

    // Generate numBytes of random numbers
    for (i=0; i<numBytes; i++) {
        randomBits[i] = (jbyte) (random() % 128);
    }

    // Copy the randomly generated bytes into the Java byte array
    (*env)->SetByteArrayRegion(env, bytes, 0, numBytes, randomBits);

    hymem_free_memory(randomBits);

    return 1;
}

