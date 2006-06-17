/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Rustem Rafikov
 * @version $Revision$
 */

#include <jni.h>
#include <X11/Xlib.h>
#include "nativelib_common.h"

#ifndef _Included_com_openintel_drl_nativebridge_linux_ErrorHandler
#define _Included_com_openintel_drl_nativebridge_linux_ErrorHandler

#ifdef __cplusplus
extern "C" {
#endif

int errorHandler(Display* d, XErrorEvent* env);

JNIEXPORT jboolean JNICALL Java_org_apache_harmony_awt_nativebridge_linux_ErrorHandler_isError (JNIEnv *, jclass);

JNIEXPORT jstring JNICALL Java_org_apache_harmony_awt_nativebridge_linux_ErrorHandler_getInfo (JNIEnv *, jclass);


#ifdef __cplusplus
}
#endif
#endif
