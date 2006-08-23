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
 * @author Artem Aliev
 * @version $Revision: 1.1.2.1.4.4 $
 */  
#include "thread_generic.h"
#include "open/hythread_ext.h"


#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_java_lang_VMStart_joinAllNonDaemonThreads (JNIEnv * UNREF jenv, jclass UNREF starter)
{
    hythread_wait_for_all_nondaemon_threads();
}

#ifdef __cplusplus
}
#endif
