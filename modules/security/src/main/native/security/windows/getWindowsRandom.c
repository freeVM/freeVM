/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

#undef _WIN32_WINNT
#define _WIN32_WINNT 0x0500

#include <windows.h>
#include <wincrypt.h>

#include "vmi.h"
#include "jni.h"

JNIEXPORT jint JNICALL
Java_org_apache_harmony_security_provider_crypto_RandomBitsSupplier_getWindowsRandom(JNIEnv *env, jclass obj, jbyteArray bytes, jint numBytes)
{
    HCRYPTPROV hcrypt_provider;

    byte *random_bits;

    int b;

    b = CryptAcquireContext( &hcrypt_provider, NULL, NULL, PROV_DSS, CRYPT_VERIFYCONTEXT );

    if ( !b ) {
        return 0;
    }

    random_bits = malloc(numBytes);

    b = CryptGenRandom( hcrypt_provider, numBytes, random_bits );

    if ( !b ) {
        free(random_bits);
        return 0;
    }

    b = CryptReleaseContext(hcrypt_provider, 0);

    (*env)->SetByteArrayRegion(env, bytes, 0, numBytes, (signed char*)random_bits);
    free(random_bits);

    return 1;
}

