
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
 * Clip the current top C stack chunk. A thread's C stack is
 * a sequence of disconnected chunks of contiguous C stack frames
 * intersperced with uninteresting stuff like JNI native stack frames
 * and signal frames. Each other than the top chunk also contains
 * a set of saved registers which are scanned conservatively during
 * garbage collection. Saving these registers is called "clipping"
 * and it prevents references from "leaking" across chunk boundaries.
 */
void
_jc_stack_clip(_jc_env *env)
{
	_jc_c_stack *const cstack = env->c_stack;

	/* Sanity check */
	_JC_ASSERT(env == _jc_get_current_env());
	_JC_ASSERT(cstack != NULL);
	_JC_ASSERT(!cstack->clipped);

	/* Grab the current context and clip the Java stack with it */
	sigsetjmp(cstack->regs, JNI_FALSE);

#ifndef NDEBUG
    {
    	_jc_word diff;
	const char *sp;
	const char *here;

	/* Sanity check the _jc_jmpbuf_sp() function */
	sp = _jc_jmpbuf_sp(cstack->regs);
	here = (char *)&sp;
	diff = (sp > here) ? sp - here : here - sp;
	_JC_ASSERT(diff < 0x100);

	/* Mark stack as clipped */
	cstack->clipped = JNI_TRUE;
    }
#endif
}

/*
 * Print out the exception stack trace associated with the
 * currently posted exception, which must not be NULL.
 */
void
_jc_print_stack_trace(_jc_env *env, FILE *fp)
{
	_jc_jvm *const vm = env->vm;
	_jc_saved_frame *frames;
	_jc_object *vmThrowable;
	_jc_byte_array *bytes;
	_jc_object *cause;
	int num_frames;
	_jc_object *e;

	/* Get exception */
	e = env->pending;

again:
	/* Sanity check */
	_JC_ASSERT(e != NULL);
	_JC_ASSERT(_jc_subclass_of(e->type, vm->boot.types.Throwable));

	/* Print exception headling */
	_jc_fprint_exception_headline(env, fp, e);
	_jc_fprintf(vm, fp, "\n");

	/* Get associated VMThrowable */
	if ((vmThrowable = *_JC_VMFIELD(vm, e,
	    Throwable, vmState, _jc_object *)) == NULL)
		goto no_trace;

	/* Get saved frames from 'vmdata' byte[] array */
	if ((bytes = *_JC_VMFIELD(vm, vmThrowable,
	    VMThrowable, vmdata, _jc_byte_array *)) == NULL)
		goto no_trace;

	/* Print stack frames */
	frames = (_jc_saved_frame *)(bytes->elems + _JC_BYTE_ARRAY_PAD);
	num_frames = (bytes->length - _JC_BYTE_ARRAY_PAD) / sizeof(*frames);
	_jc_print_stack_frames(env, fp, num_frames, frames);

no_trace:
	/* Print causing exception, if any */
	if ((cause = *_JC_VMFIELD(vm, e,
	      Throwable, cause, _jc_object *)) != NULL
	    && cause != e) {
		_jc_fprintf(vm, fp, "Caused by ");
		e = cause;
		goto again;
	}
}

/*
 * Print out stack frames from an array of _jc_saved_frame's.
 */
void
_jc_print_stack_frames(_jc_env *env, FILE *fp,
	int num_frames, _jc_saved_frame *frames)
{
	_jc_jvm *const vm = env->vm;
	int i;

	/* Print out stack trace */
	for (i = 0; i < num_frames; i++) {
		_jc_saved_frame *const frame = &frames[i];
		_jc_method *method;
		_jc_type *class;

		/* Get method and class */
		method = frame->method;
		class = method->class;

		/* Print out stack frame */
		_jc_fprintf(vm, fp, "\tat ");
		_jc_fprint_noslash(vm, fp, class->name);
		_jc_fprintf(vm, fp, ".%s(", method->name);
		if (_JC_ACC_TEST(method, NATIVE))
			_jc_fprintf(vm, fp, "Native Method");
		else if (class->u.nonarray.source_file == NULL)
			_jc_fprintf(vm, fp, "Unknown");
		else {
			int jline;

			/* Print source file */
			_jc_fprintf(vm, fp, "%s",
			    class->u.nonarray.source_file);

			/* Print Java line number if known */
			if (frame->ipc != -1) {
				jline = _jc_interp_pc_to_jline(method,
				    frame->ipc);
				if (jline != 0)
					_jc_fprintf(vm, fp, ":%d", jline);
			}
		}
		_jc_fprintf(vm, fp, ")\n");
	}
}

/*
 * Save the current Java stack trace as an array of _jc_saved_frame's.
 *
 * If 'thread' doesn't correspond to the current thread, then the
 * target thread must be running in non-java mode or halted
 * (so that it's top C stack frame is already clipped).
 *
 * The array is stored in the area pointed to by 'frames' (if not NULL).
 * At most 'max' frames are stored.
 *
 * Returns the total number of frames if successful, otherwise -1.
 */
int
_jc_save_stack_frames(_jc_env *env, _jc_env *thread,
	int max, _jc_saved_frame *frames)
{
	_jc_java_stack *jstack;
	int i;

	/* Crawl the stack */
	for (i = 0, jstack = env->java_stack;
	    jstack != NULL; jstack = jstack->next) {
		_jc_saved_frame *const frame = &frames[i];

		/* Sanity check */
		_JC_ASSERT(jstack->method->class != NULL);

		/* Save this one */
		if (i < max) {
			frame->method = jstack->method;
			frame->ipc = jstack->pc != NULL ?
			    jstack->pc - jstack->method->code.insns : -1;
		}
		i++;
	}

	/* Done */
	return i;
}

