/* Copyright 1991, 2005 The Apache Software Foundation or its licensors, as applicable
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

#include "rasthrsup.h"

#include <pthread.h>

#include "hycomp.h"

UDATA
Unix_GetKernelThreadID (void)
{
  pthread_t myThread = pthread_self ();

  /*
   * Convert the local pthread_t variable, which could be a structure or a scalar value, into a UDATA
   * by getting its address, casting that to a UDATA pointer and then dereferencing to get the value
   *
   * The result seems to match the thread id observed in GDB...
   */
  if (sizeof (pthread_t) >= sizeof (UDATA))
    {
      return *((UDATA *) & myThread);
    }
  else
    {
      return 0;
    }
}
