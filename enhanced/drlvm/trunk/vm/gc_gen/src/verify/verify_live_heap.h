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

#ifndef _VERIFY_LIVE_HEAP_H_
#define _VERIFY_LIVE_HEAP_H_

#include "../common/gc_common.h"

extern Boolean verify_live_heap;
void gc_init_heap_verification(GC* gc);
void gc_terminate_heap_verification(GC* gc);
void event_collector_move_obj(Partial_Reveal_Object *p_old, Partial_Reveal_Object *p_new, Collector* collector);
void gc_verify_heap(GC* gc, Boolean is_before_gc);

/* functions used in fall back compaction and the out-of-space cases*/
void event_collector_doublemove_obj(Partial_Reveal_Object *p_old, Partial_Reveal_Object *p_new, Collector* collector);

#endif
