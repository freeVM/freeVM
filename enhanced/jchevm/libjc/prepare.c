
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

/*
 * Prepare a type.
 *
 * NOTE: This assumes the caller is handling synchronization issues.
 */
jint
_jc_prepare_type(_jc_env *env, _jc_type *type)
{
	jint status;

	/* Already prepared? */
	if (_JC_FLG_TEST(type, PREPARED)) {
		_JC_ASSERT(_JC_FLG_TEST(type, LOADED));
		_JC_ASSERT(_JC_FLG_TEST(type, VERIFIED));
		return JNI_OK;
	}

	/* Sanity check */
	_JC_ASSERT(!_JC_FLG_TEST(type, ARRAY));

	/* Verify type first */
	if (!_JC_FLG_TEST(type, VERIFIED)
	    && (status = _jc_verify_type(env, type)) != JNI_OK)
		return status;

	/* Nothing to do for us */

	/* Mark type as prepared */
	type->flags |= _JC_TYPE_PREPARED;

	/* Done */
	return JNI_OK;
}

