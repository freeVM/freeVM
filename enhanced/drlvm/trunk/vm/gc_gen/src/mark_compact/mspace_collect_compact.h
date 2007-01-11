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
 * @author Xiao-Feng Li, 2006/12/12
 */

#ifndef _MSPACE_COLLECT_COMPACT_H_
#define _MSPACE_COLLECT_COMPACT_H_

#include "mspace.h"
#include "../thread/collector.h"     
#include "../common/space_tuner.h"

void gc_reset_block_for_collectors(GC* gc, Mspace* mspace);
void gc_init_block_for_collectors(GC* gc, Mspace* mspace);

void update_mspace_info_for_los_extension(Mspace* mspace);
void mspace_reset_after_compaction(Mspace* mspace);

Block_Header* mspace_get_first_compact_block(Mspace* mspace);
Block_Header* mspace_get_first_target_block(Mspace* mspace);
Block_Header* mspace_get_next_compact_block(Collector* collector, Mspace* mspace);
Block_Header* mspace_get_next_target_block(Collector* collector, Mspace* mspace);

void slide_compact_mspace(Collector* collector);
void move_compact_mspace(Collector* collector);

void fallback_mark_scan_heap(Collector* collector);

void mspace_extend_compact(Collector *collector);

extern Boolean IS_MOVE_COMPACT;

#endif /* _MSPACE_COLLECT_COMPACT_H_ */

