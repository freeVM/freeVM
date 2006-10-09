/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Oleg V. Khaschansky
 * @version $Revision: 1.2 $
 * 
 */

#include "Exceptions.h"

/*
 * Creates and throws arbitrary exception
 */
void newExceptionByName(JNIEnv *env, const char* name, const char* msg) {
    // Create exception
    jclass cls = (*env)->FindClass(env, name);
    /* if cls is NULL, an exception has already been thrown */
    if (cls != NULL) {
        (*env)->ThrowNew(env, cls, msg);
    }

    (*env)->DeleteLocalRef(env, cls);
}

void newNullPointerException(JNIEnv *env, const char* msg) {
    newExceptionByName(env, "java/lang/NullPointerException", msg);
}