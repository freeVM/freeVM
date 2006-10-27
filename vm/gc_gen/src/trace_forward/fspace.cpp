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

#include "platform.h"

#include "fspace.h"

float NURSERY_OBJECT_FORWARDING_RATIO = FORWARD_ALL;
//float NURSERY_OBJECT_FORWARDING_RATIO = FORWARD_HALF;

void* nos_boundary = null; /* this is only for speeding up write barrier */

Boolean forward_first_half;;
void* object_forwarding_boundary=NULL;

void fspace_save_reloc(Fspace* fspace, Partial_Reveal_Object** p_ref)
{
  Block_Header* block = GC_BLOCK_HEADER(p_ref);
  block->reloc_table->push_back(p_ref);
  return;
}

void  fspace_update_reloc(Fspace* fspace)
{
  SlotVector* reloc_table;
  /* update refs in fspace */
  Block* blocks = fspace->blocks;
  for(unsigned int i=0; i < fspace->num_managed_blocks; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    reloc_table = block->reloc_table;
    for(unsigned int j=0; j < reloc_table->size(); j++){
      Partial_Reveal_Object** p_ref = (*reloc_table)[j];
      Partial_Reveal_Object* p_target_obj = get_forwarding_pointer_in_obj_info(*p_ref);
      *p_ref = p_target_obj;
    }
    reloc_table->clear();
  }
  
  return;  
}  

Boolean fspace_mark_object(Fspace* fspace, Partial_Reveal_Object *p_obj)
{  
  obj_mark_in_vt(p_obj);

  unsigned int obj_word_index = OBJECT_WORD_INDEX_TO_MARKBIT_TABLE(p_obj);
  unsigned int obj_offset_in_word = OBJECT_WORD_OFFSET_IN_MARKBIT_TABLE(p_obj); 	
	
  unsigned int *p_word = &(GC_BLOCK_HEADER(p_obj)->mark_table[obj_word_index]);
  unsigned int word_mask = (1<<obj_offset_in_word);
	
  unsigned int result = (*p_word)|word_mask;
	
  if( result==(*p_word) ) return FALSE;
  
  *p_word = result; 
  
   return TRUE;
}

Boolean fspace_object_is_marked(Partial_Reveal_Object *p_obj, Fspace* fspace)
{
  assert(p_obj);
  
#ifdef _DEBUG //TODO:: Cleanup
  unsigned int obj_word_index = OBJECT_WORD_INDEX_TO_MARKBIT_TABLE(p_obj);
  unsigned int obj_offset_in_word = OBJECT_WORD_OFFSET_IN_MARKBIT_TABLE(p_obj); 	
	
  unsigned int *p_word = &(GC_BLOCK_HEADER(p_obj)->mark_table[obj_word_index]);
  unsigned int word_mask = (1<<obj_offset_in_word);
	
  unsigned int result = (*p_word)|word_mask;
	
  if( result==(*p_word) )
    assert( obj_is_marked_in_vt(p_obj));
  else 
    assert(!obj_is_marked_in_vt(p_obj));
    
#endif

  return (obj_is_marked_in_vt(p_obj));
    
}

static void fspace_destruct_blocks(Fspace* fspace)
{ 
  Block* blocks = (Block*)fspace->blocks; 
  for(unsigned int i=0; i < fspace->num_managed_blocks; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    delete block->reloc_table;
    block->reloc_table = NULL;
  }
  
  return;
}

static void fspace_init_blocks(Fspace* fspace)
{ 
  Block* blocks = (Block*)fspace->heap_start; 
  Block_Header* last_block = (Block_Header*)blocks;
  unsigned int start_idx = fspace->first_block_idx;
  for(unsigned int i=0; i < fspace->num_managed_blocks; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    block->free = (void*)((unsigned int)block + GC_BLOCK_HEADER_SIZE_BYTES);
    block->ceiling = (void*)((unsigned int)block + GC_BLOCK_SIZE_BYTES); 
    block->base = block->free;
    block->block_idx = i + start_idx;
    block->status = BLOCK_FREE;  
    block->reloc_table = new SlotVector();
    last_block->next = block;
    last_block = block;
  }
  last_block->next = NULL;
  fspace->blocks = blocks;
   
  return;
}

struct GC_Gen;
void gc_set_nos(GC_Gen* gc, Space* space);
void fspace_initialize(GC* gc, void* start, unsigned int fspace_size) 
{    
  assert( (fspace_size%GC_BLOCK_SIZE_BYTES) == 0 );
  Fspace* fspace = (Fspace *)STD_MALLOC(sizeof(Fspace));
  assert(fspace);
  memset(fspace, 0, sizeof(Fspace));
    
  fspace->reserved_heap_size = fspace_size;
  fspace->num_total_blocks = fspace_size >> GC_BLOCK_SHIFT_COUNT;

  void* reserved_base = start;
  int status = port_vmem_commit(&reserved_base, fspace_size, gc->allocated_memory); 
  assert(status == APR_SUCCESS && reserved_base == start);
    
  memset(reserved_base, 0, fspace_size);
  fspace->committed_heap_size = fspace_size;
  fspace->heap_start = reserved_base;
  fspace->heap_end = (void *)((unsigned int)reserved_base + fspace->reserved_heap_size);
  fspace->num_managed_blocks = fspace_size >> GC_BLOCK_SHIFT_COUNT;
  
  fspace->first_block_idx = GC_BLOCK_INDEX_FROM(gc->heap_start, reserved_base);
  fspace->ceiling_block_idx = fspace->first_block_idx + fspace->num_managed_blocks - 1;
  
  fspace->num_used_blocks = 0;
  fspace->free_block_idx = fspace->first_block_idx;
  
  fspace_init_blocks(fspace);
  
  fspace->obj_info_map = new ObjectMap();
  fspace->mark_object_func = fspace_mark_object;
  fspace->save_reloc_func = fspace_save_reloc;
  fspace->update_reloc_func = fspace_update_reloc;

  fspace->move_object = TRUE;
  fspace->num_collections = 0;
  fspace->gc = gc;
  gc_set_nos((GC_Gen*)gc, (Space*)fspace);
  /* above is same as Mspace init --> */
  
  fspace->remslot_sets = new std::vector<RemslotSet *>();
  fspace->rem_sets_lock = FREE_LOCK;

  nos_boundary = fspace->heap_end;

  forward_first_half = TRUE;
  object_forwarding_boundary = (void*)&fspace->blocks[fspace->first_block_idx + (unsigned int)(fspace->num_managed_blocks * NURSERY_OBJECT_FORWARDING_RATIO)];

  return;
}

void fspace_destruct(Fspace *fspace) 
{
  fspace_destruct_blocks(fspace);
  port_vmem_decommit(fspace->heap_start, fspace->committed_heap_size, fspace->gc->allocated_memory);
  STD_FREE(fspace);  
 
}
 
void reset_fspace_for_allocation(Fspace* fspace)
{ 
  if( NURSERY_OBJECT_FORWARDING_RATIO == FORWARD_ALL ||
            fspace->gc->collect_kind == MAJOR_COLLECTION )
  {
    fspace->free_block_idx = fspace->first_block_idx;
    fspace->ceiling_block_idx = fspace->first_block_idx + fspace->num_managed_blocks - 1;  
    forward_first_half = TRUE; /* only useful for not-FORWARD_ALL*/
  }else{    
    if(forward_first_half){
      fspace->free_block_idx = fspace->first_block_idx;
      fspace->ceiling_block_idx = ((Block_Header*)object_forwarding_boundary)->block_idx - 1;
    }else{
      fspace->free_block_idx = ((Block_Header*)object_forwarding_boundary)->block_idx;
      fspace->ceiling_block_idx = fspace->first_block_idx + fspace->num_managed_blocks - 1;
    }
    forward_first_half = ~forward_first_half;
  }

  unsigned int first_idx = fspace->free_block_idx;
  unsigned int last_idx = fspace->ceiling_block_idx;
  Block* blocks = fspace->blocks;
  unsigned int num_freed = 0;
  for(unsigned int i = first_idx; i <= last_idx; i++){
    Block_Header* block = (Block_Header*)&(blocks[i]);
    if(block->status == BLOCK_FREE) continue;
    block_clear_mark_table(block); 
    block->status = BLOCK_FREE; 
    block->free = GC_BLOCK_BODY(block);
    num_freed ++;
  }
  fspace->num_used_blocks = fspace->num_used_blocks - num_freed;

}

void collector_execute_task(GC* gc, TaskType task_func, Space* space);

/* world is stopped when starting fspace_collection */      
void fspace_collection(Fspace *fspace)
{
  fspace->num_collections++;  
  
  GC* gc = fspace->gc;

  if(gc_requires_barriers()){ 
    /* generational GC. Only trace (mark) nos */
    collector_execute_task(gc, (TaskType)trace_forward_fspace, (Space*)fspace);
  }else{
    /* non-generational GC. Mark the whole heap (nos, mos, and los) */
    collector_execute_task(gc, (TaskType)mark_copy_fspace, (Space*)fspace);
  }
  
  return; 
}
