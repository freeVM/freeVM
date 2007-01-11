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
 * @author Li-Gang Wang, 2006/11/29
 */

#ifndef _FINREF_METADATA_H_
#define _FINREF_METADATA_H_

#include "../common/gc_common.h"
#include "../utils/vector_block.h"
#include "../utils/sync_pool.h"

#define POOL_SEGMENT_NUM 256

typedef struct Finref_Metadata{
  void *pool_segments[POOL_SEGMENT_NUM];  // malloced free pool segments' addresses array
  unsigned int num_alloc_segs;              // next available position in pool_segments array
  
  Pool *free_pool;                        // list of free buffers for the five pools below
  
  Pool *obj_with_fin_pool;                // list of objects that have finalizer;
                                          // these objects are added in when they are allocated
  Pool *finalizable_obj_pool;             // temporary buffer for finalizable objects identified during one single GC
  
  Pool *softref_pool;                     // temporary buffer for soft references identified during one single GC
  Pool *weakref_pool;                     // temporary buffer for weak references identified during one single GC
  Pool *phanref_pool;                     // temporary buffer for phantom references identified during one single GC
  
  Pool *repset_pool;                      // repointed reference slot sets
  
  Vector_Block *finalizable_obj_set;      // buffer for finalizable_objects_pool
  Vector_Block *repset;                   // buffer for repset_pool
  
  Boolean pending_finalizers;             // there are objects waiting to be finalized
  Boolean pending_weakrefs;               // there are weak references waiting to be enqueued
  
  unsigned int gc_referent_offset;        // the referent field's offset in Reference Class
}Finref_Metadata;

extern unsigned int get_gc_referent_offset(void);
extern void set_gc_referent_offset(unsigned int offset);

extern void gc_finref_metadata_initialize(GC *gc);
extern void gc_finref_metadata_destruct(GC *gc);
extern void gc_finref_metadata_verify(GC *gc, Boolean is_before_gc);
extern void gc_reset_finref_metadata(GC *gc);
extern Vector_Block *finref_get_free_block(void);

extern void mutator_add_finalizer(Mutator *mutator, Partial_Reveal_Object *ref);
extern void gc_add_finalizable_obj(GC *gc, Partial_Reveal_Object *ref);
extern void collector_add_softref(Collector *collector, Partial_Reveal_Object *ref);
extern void collector_add_weakref(Collector *collector, Partial_Reveal_Object *ref);
extern void collector_add_phanref(Collector *collector, Partial_Reveal_Object *ref);
extern void finref_repset_add_entry(GC *gc, Partial_Reveal_Object **ref);

extern Boolean obj_with_fin_pool_is_empty(GC *gc);
extern Boolean finalizable_obj_pool_is_empty(GC *gc);
extern Boolean softref_pool_is_empty(GC *gc);
extern Boolean weakref_pool_is_empty(GC *gc);
extern Boolean phanref_pool_is_empty(GC *gc);
extern Boolean finref_repset_pool_is_empty(GC *gc);

extern void gc_clear_weakref_pools(GC *gc);


/* called before loop of recording finalizable objects */
inline void gc_reset_finalizable_objects(GC *gc)
{
  Finref_Metadata *metadata = gc->finref_metadata;
  
  assert(!metadata->finalizable_obj_set);
  metadata->finalizable_obj_set = pool_get_entry(metadata->free_pool);
}
/* called after loop of recording finalizable objects */
inline void gc_put_finalizable_objects(GC *gc)
{
  Finref_Metadata *metadata = gc->finref_metadata;
  
  pool_put_entry(metadata->finalizable_obj_pool, metadata->finalizable_obj_set);
  metadata->finalizable_obj_set = NULL;
}

/* called before loop of recording repointed reference */
inline void finref_reset_repset(GC *gc)
{
  Finref_Metadata *metadata = gc->finref_metadata;
  
  assert(!metadata->repset);
  metadata->repset = pool_get_entry(metadata->free_pool);
}
/* called after loop of recording repointed reference */
inline void finref_put_repset(GC *gc)
{
  Finref_Metadata *metadata = gc->finref_metadata;
  
  pool_put_entry(metadata->repset_pool, metadata->repset);
  metadata->repset = NULL;
}

#endif // _FINREF_METADATA_H_
