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

#include "mspace_collect_compact.h"


Boolean IS_MOVE_COMPACT;

struct GC_Gen;
Space* gc_get_nos(GC_Gen* gc);

static volatile Block_Header* next_block_for_compact;
static volatile Block_Header* next_block_for_target;

void mspace_update_info_after_space_tuning(Mspace* mspace)
{
  Space_Tuner *tuner = mspace->gc->tuner;
  POINTER_SIZE_INT tune_size = tuner->tuning_size;
  unsigned int tune_blocks = (unsigned int)(tune_size >> GC_BLOCK_SHIFT_COUNT);
  
  if(tuner->kind == TRANS_FROM_MOS_TO_LOS){
    mspace->blocks = &mspace->blocks[tune_blocks];
    mspace->heap_start = mspace->blocks;
    mspace->committed_heap_size -= tune_size;
    mspace->reserved_heap_size -= tune_size;
    mspace->first_block_idx += tune_blocks;
    mspace->num_managed_blocks -= tune_blocks;
    mspace->num_total_blocks -= tune_blocks;
    if(mspace->num_used_blocks > tune_blocks) mspace->num_used_blocks -= tune_blocks;
    else mspace->num_used_blocks = 0;
  }else if(tuner->kind == TRANS_FROM_LOS_TO_MOS){
    mspace->blocks = (Block*)((POINTER_SIZE_INT)mspace->blocks - tune_size);
    mspace->heap_start = (void*)(mspace->blocks);
    mspace->committed_heap_size += tune_size;
    mspace->first_block_idx -= tune_blocks;
    mspace->num_managed_blocks += tune_blocks;
    mspace->num_total_blocks += tune_blocks;
  }
}

void mspace_reset_after_compaction(Mspace* mspace)
{
  unsigned int old_num_used = mspace->num_used_blocks;
  unsigned int new_num_used = mspace->free_block_idx - mspace->first_block_idx;
  unsigned int num_used = old_num_used>new_num_used? old_num_used:new_num_used;
  
  Block* blocks = mspace->blocks;
  unsigned int i;
  for(i=0; i < num_used; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    assert(!((POINTER_SIZE_INT)block % GC_BLOCK_SIZE_BYTES));
    block->status = BLOCK_USED;
    block->free = block->new_free;
    block->new_free = block->base;
    block->src = NULL;
    block->next_src = NULL;
    assert(!block->dest_counter);

    if(i >= new_num_used){
      block->status = BLOCK_FREE; 
      block->free = GC_BLOCK_BODY(block);
    }
  }
  mspace->num_used_blocks = new_num_used;
  /*For_statistic mos infomation*/
  mspace->surviving_size = new_num_used * GC_BLOCK_SIZE_BYTES;
  mspace->alloced_size = 0;
  
  /* we should clear the remaining blocks which are set to be BLOCK_COMPACTED or BLOCK_TARGET */
  for(; i < mspace->num_managed_blocks; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    assert(block->status& (BLOCK_COMPACTED|BLOCK_TARGET|BLOCK_DEST));
    block->status = BLOCK_FREE;
    block->src = NULL;
    block->next_src = NULL;
    block->free = GC_BLOCK_BODY(block);
    assert(!block->dest_counter);
  }
}

void gc_reset_block_for_collectors(GC* gc, Mspace* mspace)
{
  unsigned int free_blk_idx = mspace->first_block_idx;
  for(unsigned int i=0; i<gc->num_active_collectors; i++){
    Collector* collector = gc->collectors[i];
    unsigned int collector_target_idx = collector->cur_target_block->block_idx;
    if(collector_target_idx > free_blk_idx)
      free_blk_idx = collector_target_idx;
    collector->cur_target_block = NULL;
    collector->cur_compact_block = NULL;
  }
  mspace->free_block_idx = free_blk_idx+1;  
  return;
}

void gc_init_block_for_collectors(GC* gc, Mspace* mspace)
{
  unsigned int i;
  Block_Header* block;
  Space_Tuner* tuner = gc->tuner;
  Block_Header* nos_last_block;
  Block_Header* mos_first_block = (Block_Header*)&mspace->blocks[0];  
  unsigned int trans_blocks = (unsigned int)(tuner->tuning_size >> GC_BLOCK_SHIFT_COUNT);  
  
  /*Needn't change LOS size.*/
  if(tuner->kind == TRANS_NOTHING){
    for(i=0; i<gc->num_active_collectors; i++){
      Collector* collector = gc->collectors[i];
      block = (Block_Header*)&mspace->blocks[i];
      collector->cur_target_block = block;
      collector->cur_compact_block = block;
      block->status = BLOCK_TARGET;
    }
    
    block = (Block_Header*)&mspace->blocks[i];
    next_block_for_target = block;
    next_block_for_compact = block;
    return;
  }
  //For_LOS_extend
  else if(tuner->kind == TRANS_FROM_MOS_TO_LOS)
  {
    Blocked_Space* nos = (Blocked_Space*)gc_get_nos((GC_Gen*)gc);
    if(nos->num_managed_blocks)
      nos_last_block = (Block_Header*)&nos->blocks[nos->num_managed_blocks-1];
    else
      /*If nos->num_managed_blocks is zero, we take mos_last_block as nos_last_block instead.*/
      nos_last_block = (Block_Header*)&mspace->blocks[mspace->num_managed_blocks - 1];

    nos_last_block->next = mos_first_block;
    ((Block_Header*)&(mspace->blocks[trans_blocks - 1]))->next = NULL;
    
    for(i=0; i< gc->num_active_collectors; i++){
      Collector* collector = gc->collectors[i];
      block = (Block_Header*)&mspace->blocks[i + trans_blocks];
      collector->cur_target_block = block;
      collector->cur_compact_block = block;
      block->status = BLOCK_TARGET;
    }
    
    block = (Block_Header*)&mspace->blocks[i+trans_blocks];
    next_block_for_target = block;
    next_block_for_compact = block;
    return;
  }else
  {
    gc_space_tuner_init_fake_blocks_for_los_shrink(gc);

    Collector* collector = gc->collectors[0];
    collector->cur_target_block = tuner->interim_blocks;
    collector->cur_target_block->status = BLOCK_TARGET;

    if(trans_blocks >= gc->num_active_collectors)
      collector->cur_compact_block = mos_first_block;
    else
      collector->cur_compact_block = gc->tuner->interim_blocks;

    collector->cur_compact_block->status = BLOCK_IN_COMPACT;
    
    for(i=1; i< gc->num_active_collectors; i++){
      collector = gc->collectors[i];
      collector->cur_target_block = gc->collectors[i - 1]->cur_target_block->next;
      collector->cur_target_block->status = BLOCK_TARGET;
      collector->cur_compact_block = gc->collectors[i - 1]->cur_compact_block->next;
      collector->cur_compact_block->status = BLOCK_IN_COMPACT;
    }
    next_block_for_target = collector->cur_target_block->next;    
    next_block_for_compact = collector->cur_compact_block->next;
  }
}

Block_Header* mspace_get_first_compact_block(Mspace* mspace)
{ return (Block_Header*)mspace->blocks; }

Block_Header* mspace_get_first_target_block(Mspace* mspace)
{ return (Block_Header*)mspace->blocks; }

Block_Header* mspace_get_next_compact_block(Collector* collector, Mspace* mspace)
{ 
  /* firstly put back the compacted block. If it's not BLOCK_TARGET, it will be set to BLOCK_COMPACTED */
  unsigned int block_status = collector->cur_compact_block->status;
  assert( block_status & (BLOCK_IN_COMPACT|BLOCK_TARGET));
  if( block_status == BLOCK_IN_COMPACT)
    collector->cur_compact_block->status = BLOCK_COMPACTED;

  Block_Header* cur_compact_block = (Block_Header*)next_block_for_compact;
  
  while(cur_compact_block != NULL){
    Block_Header* next_compact_block = cur_compact_block->next;

    Block_Header* temp = (Block_Header*)atomic_casptr((volatile void **)&next_block_for_compact, next_compact_block, cur_compact_block);
    if(temp != cur_compact_block){
      cur_compact_block = (Block_Header*)next_block_for_compact;
      continue;
    }
    /* got it, set its state to be BLOCK_IN_COMPACT. It must be the first time touched by compactor */
    block_status = cur_compact_block->status;
    assert( !(block_status & (BLOCK_IN_COMPACT|BLOCK_COMPACTED|BLOCK_TARGET)));
    cur_compact_block->status = BLOCK_IN_COMPACT;
    collector->cur_compact_block = cur_compact_block;
    return cur_compact_block;
      
  }
  /* run out space blocks for compacting */
  return NULL;
}

Block_Header* mspace_get_next_target_block(Collector* collector, Mspace* mspace)
{    
  Block_Header* cur_target_block = (Block_Header*)next_block_for_target;
  
  /* firstly, we bump the next_block_for_target global var to the first non BLOCK_TARGET block
     This need not atomic op, because the global var is only a hint. */
  while(cur_target_block->status == BLOCK_TARGET){
      cur_target_block = cur_target_block->next;
  }
  next_block_for_target = cur_target_block;

  /* cur_target_block has to be BLOCK_IN_COMPACT|BLOCK_COMPACTED|BLOCK_TARGET. Reason: 
     Any block after it must be either BLOCK_TARGET, or: 
     1. Since cur_target_block < cur_compact_block, we at least can get cur_compact_block as target.
     2. For a block that is >=cur_target_block and <cur_compact_block. 
        Since it is before cur_compact_block, we know it must be a compaction block of some thread. 
        So it is either BLOCK_IN_COMPACT or BLOCK_COMPACTED. 
     We care only the BLOCK_COMPACTED block or own BLOCK_IN_COMPACT. But I can't make the assert
     as below because of a race condition where the block status is not yet updated by other thread.
    assert( cur_target_block->status & (BLOCK_IN_COMPACT|BLOCK_COMPACTED|BLOCK_TARGET)); 
  */

  /* mos may be out of space, so we can use nos blocks for compaction target.
   * but we can't use the blocks which are given to los when los extension happens.
   * in this case, an out-of-mem should be given to user.
   */
  GC* gc = collector->gc;
  Blocked_Space* nos = (Blocked_Space*)gc_get_nos((GC_Gen*)gc);
  Block_Header *nos_end; 
  if( nos->num_managed_blocks != 0)
    nos_end = ((Block_Header *)&nos->blocks[nos->num_managed_blocks-1])->next;
  else 
    nos_end = ((Block_Header *)&mspace->blocks[mspace->num_managed_blocks-1])->next;

  while( cur_target_block != nos_end){
    //For_LOS_extend
    //assert( cur_target_block <= collector->cur_compact_block);
    Block_Header* next_target_block = cur_target_block->next;
    volatile unsigned int* p_block_status = &cur_target_block->status;
    unsigned int block_status = cur_target_block->status;
    //assert( block_status & (BLOCK_IN_COMPACT|BLOCK_COMPACTED|BLOCK_TARGET));

    /* if it is not BLOCK_COMPACTED, let's move on to next except it's own cur_compact_block */
    if(block_status != BLOCK_COMPACTED){
      if(cur_target_block == collector->cur_compact_block){
        assert( block_status == BLOCK_IN_COMPACT);
        *p_block_status = BLOCK_TARGET;
        collector->cur_target_block = cur_target_block;
        return cur_target_block;
      }
      /* it's not my own cur_compact_block, it can be BLOCK_TARGET or other's cur_compact_block */
      cur_target_block = next_target_block;
      continue;
    }    
    /* else, find a BLOCK_COMPACTED before own cur_compact_block */    
    unsigned int temp = atomic_cas32(p_block_status, BLOCK_TARGET, BLOCK_COMPACTED);
    if(temp == BLOCK_COMPACTED){
      collector->cur_target_block = cur_target_block;
      return cur_target_block;
    }
    /* missed it, it must be set by other into BLOCK_TARGET */
    assert(temp == BLOCK_TARGET); 
    cur_target_block = next_target_block;     
  }
  /* mos is run out for major collection */
  return NULL;  
}

void mspace_collection(Mspace* mspace) 
{
  mspace->num_collections++;

  GC* gc = mspace->gc;  

  /* init the pool before starting multiple collectors */

  pool_iterator_init(gc->metadata->gc_rootset_pool);

  /* dual mark bits will consume two bits in obj info, that makes current 
     header hashbits only 5 bits. That's not enough. We implement on-demend
     hash field allocation in obj during moving. move_compact doesn't support it.
     Dual mark bits is used for MINOR_NONGEN_FORWARD algorithm */

  //For_LOS_extend
  if(gc->tuner->kind != TRANS_NOTHING){
    collector_execute_task(gc, (TaskType)slide_compact_mspace, (Space*)mspace);
  }else if (gc_match_kind(gc, FALLBACK_COLLECTION)){
    collector_execute_task(gc, (TaskType)slide_compact_mspace, (Space*)mspace);  
    //IS_MOVE_COMPACT = TRUE;
    //collector_execute_task(gc, (TaskType)move_compact_mspace, (Space*)mspace);
    //IS_MOVE_COMPACT = FALSE;
  }else{

    switch(mspace->collect_algorithm){
      case MAJOR_COMPACT_SLIDE:
        collector_execute_task(gc, (TaskType)slide_compact_mspace, (Space*)mspace);    
        break;
        
      case MAJOR_COMPACT_MOVE:
        IS_MOVE_COMPACT = TRUE;
        collector_execute_task(gc, (TaskType)move_compact_mspace, (Space*)mspace);
        IS_MOVE_COMPACT = FALSE;
        break;
  
      default:
        printf("\nThe speficied major collection algorithm doesn't exist!\n");
        exit(0);
        break;
    }

  }  

  return;  
} 





