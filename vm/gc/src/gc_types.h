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
 * @author Ivan Volosyuk
 */

#ifndef __GC_TYPES_H__
#define __GC_TYPES_H__

#define LOG_DOMAIN "gc.verbose"

#include <assert.h>
#include <vector>
#include <list>
#include <open/vm.h>
#include <open/vm_gc.h>
#include <open/gc.h>
#include <port_vmem.h>
#include <apr_time.h>
#include <apr_atomic.h>
#include <cxxlog.h>
#include "slot.h"

static char* gc_version_string() {
#if (defined _DEBUG) || ! (defined NDEBUG)
#define BUILD_MODE "debug"
#else
#define BUILD_MODE "release"
#endif
#ifndef __TIMESTAMP__
#define __TIMESTAMP__
#endif /* TIMESTAMP */
//    return "GC v4.1 " __TIMESTAMP__ " (" BUILD_MODE ")";
    return "GC v4.1 " __TIMESTAMP__ " (" BUILD_MODE ")";
}

/// obtains a spinlock.
inline void spin_lock(volatile int* lock) {
    for(int i = 0; i < 10000; i++) {
        int val = *lock;
        if (val == 1)
            continue;
        assert(val == 0);
        if (apr_atomic_cas32((volatile uint32 *)lock, 1, 0) == 0) return;
    }
    while (true) {
#ifdef _WIN32
        Sleep(1);
#endif
        if (apr_atomic_cas32((volatile uint32 *)lock, 1, 0) == 0) return;
    }
}

/// releases a spinlock.
inline void spin_unlock(volatile int* lock) {
    assert(1 == *lock);
    *lock = 0;
}

enum GC_TYPE {
    GC_COPY,
    GC_FORCED,
    GC_SLIDE_COMPACT,
    GC_CACHE,

    GC_FULL,
};

extern GC_TYPE gc_type;
class Partial_Reveal_Object;

#define THREAD_LOCAL_CLEANED_AREA_SIZE 2048

typedef struct GC_Thread_Info {
    unsigned char *tls_current_free;
    unsigned char *tls_current_cleaned;
    unsigned char *tls_current_ceiling;

    GC_Thread_Info *next;
    GC_Thread_Info **prev;
} GC_Thread_Info;

// Heap layout
#define RESERVED_FOR_HEAP_NULL (4 * 32)

// FLAGS
extern const char *lp_hint; // Use large pages
extern bool ignore_finalizers;
extern bool remember_root_set;

#define field_offset(type,field) ((POINTER_SIZE_INT)&((type*)0)->field)

#define GC_VT_ARRAY 1
#define GC_VT_FINALIZIBLE 2
#define GC_VT_HAS_SLOTS 4
#define GC_VT_FLAGS 7
#define GC_VT_ARRAY_ELEMENT_SHIFT 3
#define GC_VT_ARRAY_ELEMENT_MASK 3
#define GC_VT_ARRAY_FIRST_SHIFT 5

#define GC_VT_REF_TYPE 3

struct GC_VTable_Info {
    // Fields
    unsigned size_and_ref_type;

    // Methods
    POINTER_SIZE_INT flags() { return (POINTER_SIZE_INT)this; }
    GC_VTable_Info *ptr() {
        assert(!is_array());
        return (GC_VTable_Info*) ((POINTER_SIZE_INT)this & ~GC_VT_FLAGS);
    }

    unsigned obj_size() { return ptr()->size_and_ref_type & ~GC_VT_REF_TYPE; }
    WeakReferenceType reference_type() { return (WeakReferenceType) (ptr()->size_and_ref_type & GC_VT_REF_TYPE); }
    int *offset_array() { /* reference array just after this struct */ return (int*)(ptr() + 1); }

    bool is_array() { return flags() & GC_VT_ARRAY; }
    bool is_finalizible() { return flags() & GC_VT_FINALIZIBLE; }
    bool has_slots() { return flags() & GC_VT_HAS_SLOTS; }

    inline unsigned array_size(int length);
};

typedef POINTER_SIZE_INT GC_VT;
typedef uint32 VT32;

typedef struct Partial_Reveal_VTable {
private:
    GC_VTable_Info *gcvt;
public:

    void set_gcvt(struct GC_VTable_Info *new_gcvt) { gcvt = new_gcvt; }
    struct GC_VTable_Info *get_gcvt() { return gcvt; }

} Partial_Reveal_VTable;


class Partial_Reveal_Object {
    private:
    Partial_Reveal_Object();
    VT32 vt_raw;
    unsigned info;
    int array_len;

    public:
    VT32 &vt() { assert(/* alignment check */ !((POINTER_SIZE_INT)this & (GC_OBJECT_ALIGNMENT - 1))); return vt_raw; }
    unsigned &obj_info() { assert(/* alignment check */ !((POINTER_SIZE_INT)this & (GC_OBJECT_ALIGNMENT - 1))); return info; }
    unsigned char &obj_info_byte() { return *(unsigned char*)&obj_info(); }

    Partial_Reveal_VTable *vtable() {
#ifdef POINTER64
        assert(!(vt() & FORWARDING_BIT));
        return ah_to_vtable(vt());
#else
        assert(!(vt() & FORWARDING_BIT));
        return (Partial_Reveal_VTable*) vt();
#endif
    }

    int array_length() { return array_len; }

    Partial_Reveal_Object **get_array_elements(GC_VTable_Info *gcvt) {
        assert(gcvt->is_array());
        assert(gcvt->has_slots());
        return (Partial_Reveal_Object**)
            ((unsigned char*) this + (gcvt->flags() >> GC_VT_ARRAY_FIRST_SHIFT));
    }

#if _DEBUG
    void valid() {
        assert((vt() & FORWARDING_BIT) == 0);
        Class_Handle c = allocation_handle_get_class(vt());
        assert(class_get_allocation_handle(c) == vt());
    }
#else
    void valid() {}
#endif
};


unsigned
GC_VTable_Info::array_size(int length) {
    assert(is_array());
    unsigned f = flags();
    unsigned element_shift = f >> GC_VT_ARRAY_ELEMENT_SHIFT;
    unsigned first_element = element_shift >> (GC_VT_ARRAY_FIRST_SHIFT - GC_VT_ARRAY_ELEMENT_SHIFT);
    return (first_element + (length << (element_shift & GC_VT_ARRAY_ELEMENT_MASK)) + (GC_OBJECT_ALIGNMENT - 1)) & ~(GC_OBJECT_ALIGNMENT - 1);
}

static inline int get_object_size(Partial_Reveal_Object *obj, GC_VTable_Info *gcvt) {
    if (gcvt->is_array()) {
        return gcvt->array_size(obj->array_length());
    } else {
        return gcvt->obj_size();
    }
}

static inline bool
is_array_of_primitives(Partial_Reveal_Object *p_obj)
{
    GC_VTable_Info *gcvt = p_obj->vtable()->get_gcvt();
    return gcvt->is_array() && !gcvt->has_slots();
}

template <typename T>
static inline T round_down(T value, int round) {
    assert((round & (round - 1)) == 0);
    return (T) (((POINTER_SIZE_INT)value) & ~(round - 1));
}

template <typename T>
static inline T round_up(T value, int round) {
    assert((round & (round - 1)) == 0);
    return (T) ((((POINTER_SIZE_INT)value) + round - 1) & ~(round - 1));
}

inline POINTER_SIZE_INT mb(POINTER_SIZE_INT size) {
    int m = 1024 * 1024;
    return (size + m/2-1)/m;
}

struct OldObjects {
    Ptr end;
    Ptr pos;
    Ptr pos_limit;

    // position before gc
    Ptr prev_pos;
};

struct HeapSegment {
    Ptr base; // base pointer
    Ptr ceiling; // upper bound
    size_t size;
    size_t max_size;
    Ptr pos; // current allocation position
    Ptr pos_limit; // end of continuous allocation region

    Ptr roots_start;
    Ptr roots_pos;
    Ptr roots_end;

    Ptr compaction_region_start() { return old_objects.end; }  // compaction region
    Ptr compaction_region_end() { return ceiling; }

    Ptr allocation_region_start() { return old_objects.end; } // allocation region
    Ptr allocation_region_end() { return ceiling; }

    OldObjects old_objects;

    // data for gc algorithm switching
    float working_set_size;
    float Tcompact;
    float Tcopy;
    float dS_copy;

    // data of evacuation area prediction
    POINTER_SIZE_SINT incr_abs;
    float incr_rel;
    unsigned char *predicted_pos;

    GC_TYPE next_gc;
};

extern HeapSegment heap;

// GLOBALS
extern Ptr heap_base;

extern int pending_finalizers;
extern uint32 chunk_size;
extern bool cleaning_needed;
extern std::vector<unsigned char*> pinned_areas;
extern unsigned pinned_areas_pos;
extern std::vector<unsigned char*> old_pinned_areas;
extern unsigned old_pinned_areas_pos;

extern unsigned int heap_mark_phase;
extern unsigned int prev_mark_phase;
extern GC_Thread_Info *thread_list;
extern int num_threads;
extern int global_referent_offset;
extern apr_time_t gc_start, gc_end;
extern int gc_num;
extern apr_time_t total_gc_time;
extern apr_time_t total_user_time;
extern apr_time_t max_gc_time;
extern int gc_algorithm;
extern int gc_adaptive;

// for slide compaction algorithms
extern unsigned char *mark_bits;
extern int mark_bits_size;


// FUNCTIONS PROTOTYPES

const char *gc_name(GC_TYPE gc);

unsigned char * copy_gc(int size);
void force_gc();
unsigned char * full_gc(int size);
unsigned char * slide_gc(int size);
unsigned char* allocate_from_chunk(int size);

void gc_reserve_mark_bits();
void gc_unreserve_mark_bits();
void gc_allocate_mark_bits();
void gc_deallocate_mark_bits();
void init_gcvt();
void deinit_gcvt();

void init_select_gc();
void select_force_gc();
unsigned char* select_gc(int size);
void after_copy_gc();
void after_slide_gc();

void heap_extend(size_t size);
void heap_shrink(size_t size);

void process_finalizible_objects_on_exit();
void *alloc_large_pages(size_t size, const char *hint);
bool place_into_old_objects(unsigned char *&newpos, unsigned char *&endpos, int size);

//// OPTIONAL FEATURES /////////

//#define DEBUG_HASHCODE
#ifdef DEBUG_HASHCODE
inline int gen_hashcode(void *addr) {
    return (((int)addr >> 2) & 0x7e) | 0x3a00;
}

inline void check_hashcode(int hash) {
    assert((hash & ~0x7e) == 0x3a00);
}
#else /* DEBUG_HASHCODE */
inline int gen_hashcode(void *addr) { return (int)(POINTER_SIZE_INT)addr; }
inline void check_hashcode(int hash) {}
#endif /* DEBUG_HASHCODE */

#endif /* __GC_TYPES_H__ */

