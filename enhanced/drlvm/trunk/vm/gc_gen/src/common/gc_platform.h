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

#ifndef _GC_PLATFORM_H_
#define _GC_PLATFORM_H_

#include <assert.h>
#include <ctype.h>

#include <apr_time.h>
#include <apr_atomic.h>

#include <open/hythread_ext.h>


#ifndef _DEBUG

//#define RELEASE_DEBUG

#ifdef RELEASE_DEBUG
#undef assert
#define assert(x) do{ if(!(x)) __asm{int 3}}while(0)
#endif

#endif //_DEBUG

#define USEC_PER_SEC INT64_C(1000000)

#define VmThreadHandle  void*
#define VmEventHandle   hysem_t
#define THREAD_OK       TM_ERROR_NONE

inline int vm_wait_event(VmEventHandle event)
{   IDATA stat = hysem_wait(event);
    assert(stat == TM_ERROR_NONE); return stat;
}

inline int vm_set_event(VmEventHandle event)
{   IDATA stat = hysem_post(event);
    assert(stat == TM_ERROR_NONE); return stat;
}

inline int vm_reset_event(VmEventHandle event)
{   IDATA stat = hysem_set(event,0);
    assert(stat == TM_ERROR_NONE); return stat;
}

inline int vm_create_event(VmEventHandle* event)
{  return hysem_create(event, 0, 1); }

inline void vm_thread_yield()
{  hythread_yield(); }

inline void* vm_thread_local()
{  return hythread_self();  }

inline int vm_create_thread(int (*func)(void*), void *data)
{ 
  hythread_t* ret_thread = NULL;
  UDATA stacksize = 0;
  UDATA priority = 0;
  UDATA suspend = 0;
  
  return hythread_create(ret_thread, stacksize, priority, suspend, 
                             (hythread_entrypoint_t)func, data);
}

inline void *atomic_casptr(volatile void **mem, void *with, const void *cmp) {
  return apr_atomic_casptr(mem, with, cmp);
}

inline uint32 atomic_cas32(volatile apr_uint32_t *mem,
                                           apr_uint32_t swap,
                                           apr_uint32_t cmp) 
{  return (uint32)apr_atomic_cas32(mem, swap, cmp); }

inline uint32 atomic_inc32(volatile apr_uint32_t *mem)
{  return (uint32)apr_atomic_inc32(mem); }

inline uint32 atomic_dec32(volatile apr_uint32_t *mem)
{  return (uint32)apr_atomic_dec32(mem); }

inline uint32 atomic_add32(volatile apr_uint32_t *mem, apr_uint32_t val) 
{  return (uint32)apr_atomic_add32(mem, val); }

inline Boolean pool_create(apr_pool_t **newpool, apr_pool_t *parent) 
{  return (Boolean)apr_pool_create(newpool, parent);}

inline void pool_destroy(apr_pool_t *p) 
{  apr_pool_destroy(p); }

#ifndef _WIN32
#include <sys/mman.h>
#endif

inline void *vm_map_mem(void* start, unsigned int size) 
{
  void* address;
#ifdef _WIN32
  address = VirtualAlloc(start, size, MEM_RESERVE|MEM_COMMIT, PAGE_READWRITE);
#else
  address = mmap(start, size, PROT_READ|PROT_WRITE, MAP_FIXED|MAP_PRIVATE|MAP_ANONYMOUS, -1, 0);
  if(address == MAP_FAILED) address = NULL;
    
#endif /* ifdef _WIN32 else */

  return address;
}

inline Boolean vm_unmap_mem(void* start, unsigned int size) 
{
  unsigned int result;
#ifdef _WIN32
  result = VirtualFree(start, 0, MEM_RELEASE);
#else
  result = munmap(start, size);
  if(result == -1) result = 0;
    
#endif /* ifdef _WIN32 else */

  return result;
}

inline void *vm_alloc_mem(void* start, unsigned int size) 
{
  void* address;
#ifdef _WIN32
  address = VirtualAlloc(start, size, MEM_RESERVE|MEM_COMMIT, PAGE_READWRITE);
#else
  address = mmap(start, size, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0);
  if(address == MAP_FAILED) address = NULL;
    
#endif /* ifdef _WIN32 else */

  return address;
}

inline Boolean vm_free_mem(void* start, unsigned int size) 
{
  return vm_unmap_mem(start, size);
}

inline void *vm_reserve_mem(void* start, unsigned int size) 
{
  void* address;
#ifdef _WIN32
  address = VirtualAlloc(start, size, MEM_RESERVE, PAGE_READWRITE);
#else
  address = mmap(start, size, PROT_READ|PROT_WRITE, MAP_FIXED|MAP_PRIVATE|MAP_ANONYMOUS, -1, 0);
  if(address == MAP_FAILED) address = NULL;
    
#endif /* ifdef _WIN32 else */

  return address;
}

inline void *vm_commit_mem(void* start, unsigned int size) 
{
  void* address;
#ifdef _WIN32
  address = VirtualAlloc(start, size, MEM_COMMIT, PAGE_READWRITE);
#else
    
#endif /* ifdef _WIN32 else */

  return address;
}

inline Boolean vm_decommit_mem(void* start, unsigned int size) 
{
  unsigned int result;
#ifdef _WIN32
  result = VirtualFree(start, size, MEM_DECOMMIT);
#else
    
#endif /* ifdef _WIN32 else */

  return result;
}

inline int64 time_now() 
{  return apr_time_now(); }

inline void string_to_upper(char* s)
{
  while(*s){
    *s = toupper(*s);
    s++;
  }
}  

#ifdef PLATFORM_POSIX
#define max(x, y) ((x)>(y)?(x):(y))
#endif

typedef volatile unsigned int SpinLock;

enum Lock_State{
  FREE_LOCK,
  LOCKED
};

#define try_lock(x) (!atomic_cas32(&(x), LOCKED, FREE_LOCK))
#define lock(x) while( !try_lock(x)){ while( x==LOCKED );}
#define unlock(x) do{ x = FREE_LOCK;}while(0)

#endif //_GC_PLATFORM_H_
