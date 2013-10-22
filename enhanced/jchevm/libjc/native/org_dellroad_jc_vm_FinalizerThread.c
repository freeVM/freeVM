
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * $Id$
 */

#include "libjc.h"
#include "org_dellroad_jc_vm_FinalizerThread.h"

/*
 * static native void finalizeObjects()
 */
void _JC_JCNI_ATTR
JCNI_org_dellroad_jc_vm_FinalizerThread_finalizeObjects(_jc_env *env)
{
	if (_jc_gc_finalize(env) != JNI_OK)
		_jc_throw_exception(env);
}

