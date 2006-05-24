
/*
 * Copyright 2005 The Apache Software Foundation or its licensors,
 * as applicable.
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
 *
 * $Id$
 */

#include "libjc.h"
#include "java_lang_VMSystem.h"

/*
 * static final native void arraycopy(Object, int, Object, int, int)
 */
void _JC_JCNI_ATTR
JCNI_java_lang_VMSystem_arraycopy(_jc_env *env, _jc_object *src,
	jint srcOff, _jc_object *dst, jint dstOff, jint len)
{
	_jc_type *setype;
	_jc_type *detype;
	int etype;

	/* Check for null */
	if (src == NULL || dst == NULL) {
		_jc_post_exception(env, _JC_NullPointerException);
		_jc_throw_exception(env);
	}

	/* Verify that objects are arrays with compatible element types */
	if (!_JC_LW_TEST(src->lockword, ARRAY)
	    || ((src->lockword ^ dst->lockword)
	     & (_JC_LW_ARRAY_BIT | (_JC_LW_TYPE_MASK << _JC_LW_TYPE_SHIFT)))
	    != 0) {
		_jc_post_exception(env, _JC_ArrayStoreException);
		_jc_throw_exception(env);
	}

	/* Check bounds */
	if (!_JC_BOUNDS_CHECK((_jc_array *)src, srcOff, len)
	    || !_JC_BOUNDS_CHECK((_jc_array *)dst, dstOff, len)) {
		_jc_post_exception(env, _JC_ArrayIndexOutOfBoundsException);
		_jc_throw_exception(env);
	}

	/* Minor optimization */
	if (len == 0)
		return;

	/* For primitive type, just bulk copy */
	if ((etype = _JC_LW_EXTRACT(src->lockword, TYPE))
	    != _JC_TYPE_REFERENCE) {
		_jc_array *const spary = (_jc_array *)src;
		_jc_array *const dpary = (_jc_array *)dst;
		const size_t esize = _jc_type_sizes[etype];
		const size_t eoffs = _jc_array_head_sizes[etype];

		if (src == dst) {
			memmove((char *)dpary + eoffs + (dstOff * esize),
				(char *)spary + eoffs + (srcOff * esize),
				len * esize);
		} else {
			memcpy( (char *)dpary + eoffs + (dstOff * esize),
				(char *)spary + eoffs + (srcOff * esize),
				len * esize);
		}
		return;
	}

	/* Get array element types */
	setype = src->type->u.array.element_type;
	detype = dst->type->u.array.element_type;

	/*
	 * If array element types are assignable, just bulk copy. Otherwise,
	 * copy one element at a time, checking each for assignability;
	 * we know the two arrays are actually different because they are
	 * not assignable, so there's no possibility of overlap.
	 */
	switch (_jc_assignable_from(env, setype, detype)) {
	case -1:
		_jc_throw_exception(env);

	case 1:					/* assignable */
	    {
		_jc_object_array *const soary = (_jc_object_array *)src;
		_jc_object_array *const doary = (_jc_object_array *)dst;

		if (src == dst) {
			memmove(doary->elems + ~(dstOff + len - 1),
				soary->elems + ~(srcOff + len - 1),
				len * sizeof(*soary->elems));
		} else {
			memcpy( doary->elems + ~(dstOff + len - 1),
				soary->elems + ~(srcOff + len - 1),
				len * sizeof(*soary->elems));
		}
		break;
	    }

	case 0:					/* not assignable */
	    {
		_jc_object_array *const soary = (_jc_object_array *)src;
		_jc_object_array *const doary = (_jc_object_array *)dst;
		int i;

		for (i = 0; i < len; i++) {
			_jc_object *const elem = soary->elems[~(srcOff + i)];

			/* Check for type compatibility */
			if (elem != NULL) {
				switch (_jc_instance_of(env, elem, detype)) {
				case 1:
					break;
				case 0:
					_jc_post_exception(env,
					    _JC_ArrayStoreException);
					/* FALLTHROUGH */
				case -1:
					_jc_throw_exception(env);
				default:
					_JC_ASSERT(JNI_FALSE);
				}
			}
			doary->elems[~(dstOff + i)] = elem;
		}
		break;
	    }

	default:
		_JC_ASSERT(JNI_FALSE);
	}
}

/*
 * static final native int identityHashCode(Object)
 */
jint _JC_JCNI_ATTR
JCNI_java_lang_VMSystem_identityHashCode(_jc_env *env, _jc_object *obj)
{
	return (jint)obj;
}

