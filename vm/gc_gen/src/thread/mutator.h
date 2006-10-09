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
 * @author Xiao-Feng Li, 2006/10/05
 */

#ifndef _MUTATOR_H_
#define _MUTATOR_H_

#include "../common/gc_common.h"

/* Mutator thread local information for GC */
typedef struct Mutator {
  /* <-- first couple of fields are overloaded as Alloc_Context */
	void*	free;
	void*	ceiling;
  void* curr_alloc_block;
  Space* alloc_space;
  GC* gc;
  VmThreadHandle thread_handle;   /* This thread; */
  /* END of Alloc_Context --> */
  
  RemslotSet *remslot;
  RemobjSet *remobj;
  Mutator *next;  /* The gc info area associated with the next active thread. */
} Mutator;

void mutator_initialize(GC* gc, void* tls_gc_info);
void mutator_destruct(GC* gc, void* tls_gc_info); 
void mutator_reset(GC *gc);

#endif /*ifndef _MUTATOR_H_ */