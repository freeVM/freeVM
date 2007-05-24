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

#ifndef _SPACE_TUNER_H_
#define _SPACE_TUNER_H_

#include "gc_common.h"
#include "gc_space.h"

#define GC_LOS_MIN_VARY_SIZE ( 2 * MB )
//#define GC_FIXED_SIZE_TUNER

extern POINTER_SIZE_INT max_heap_size_bytes;

//For_LOS_extend
enum Transform_Kind {
  TRANS_NOTHING = 0,
  TRANS_FROM_LOS_TO_MOS = 0x1,
  TRANS_FROM_MOS_TO_LOS = 0x2,
};

typedef struct Space_Tuner{
    Transform_Kind kind;
    /*This flag is set if the los tuning status changes in the process of tuning*/
    Boolean reverse;
    POINTER_SIZE_INT tuning_size;
    /*Used for LOS_Shrink*/
    Block_Header* interim_blocks;
    /*This flag is set when tuning strategy decide to tune los size.
      *i.e. wasted memory is greater than wast_threshold.
      */
    Boolean need_tune;
    /*This flag is set if gc is caused by los alloc failure.*/
    Boolean force_tune;
    
    /*LOS alloc speed since last major*/
    POINTER_SIZE_INT speed_los;
    POINTER_SIZE_INT old_speed_los;
    /*MOS alloc speed since last major*/
    POINTER_SIZE_INT speed_mos;
    POINTER_SIZE_INT old_speed_mos;
    
    /*Total wasted memory of los science last los variation*/
    POINTER_SIZE_INT wast_los;
    /*Total wasted memory of mos science last los variation*/
    POINTER_SIZE_INT wast_mos;

    POINTER_SIZE_INT current_dw;
    /*NOS survive size of last minor, this could be the least meaningful space unit when talking about tuning.*/
    POINTER_SIZE_INT current_ds;

    /*Threshold for deta waste*/
    POINTER_SIZE_INT threshold_waste;
    /*Minimun tuning size for los variation*/
    POINTER_SIZE_INT min_tuning_size;
}Space_Tuner;

void gc_compute_space_tune_size_before_marking(GC* gc, unsigned int cause);
void gc_compute_space_tune_size_after_marking(GC *gc);
void gc_space_tuner_reset(GC* gc);
void gc_space_tuner_initialize(GC* gc);
void gc_space_tuner_init_fake_blocks_for_los_shrink(GC* gc);
void gc_space_tuner_release_fake_blocks_for_los_shrink(GC* gc);

#endif /* _SPACE_TUNER_H_ */
