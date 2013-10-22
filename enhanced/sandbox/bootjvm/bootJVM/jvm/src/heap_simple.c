/*!
 * @file heap_simple.c
 *
 * @brief @b simple heap management functions
 *
 * This is the first of probably several implementations of heap
 * management for this JVM implementation.  It uses the simple
 * OS/stdlib resources of @c @b malloc(3)/free(3) as the foundation
 * for its management scheme.  The memory allocation pointer is
 * generated by @c @b malloc(3).  Calling @c @b free(3) with this
 * same pointer marks the block of memory as available for
 * other allocation.
 *
 * The common header file @link jvm/src/heap.h gc.h@endlink defines
 * the prototypes for all heap allocation implementations by way
 * of the @link #CONFIG_HEAP_TYPE_SIMPLE CONFIG_HEAP_TYPE_xxx@endlink
 * symbol definitions.
 *
 *
 * @section Control
 *
 * \$URL$
 *
 * \$Id$
 *
 * Copyright 2005 The Apache Software Foundation
 * or its licensors, as applicable.
 *
 * Licensed under the Apache License, Version 2.0 ("the License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * @version \$LastChangedRevision$
 *
 * @date \$LastChangedDate$
 *
 * @author \$LastChangedBy$
 *
 *         Original code contributed by Daniel Lydick on 09/28/2005.
 *
 * @section Reference
 *
 */

#include "arch.h"
ARCH_SOURCE_COPYRIGHT_APACHE(heap_simple, c,
"$URL$",
"$Id$");


#include <errno.h>

#include "jvmcfg.h"
#include "exit.h"
#include "gc.h"
#include "heap.h"
#include "jvmclass.h"
#include "util.h"


/*!
 * @brief Start up heap management methodology.
 *
 * In a @b malloc/free scheme, there is nothing
 * to do, but in other methods there might be.
 *
 *
 * @param[out]   heap_init_flag   Pointer to rboolean, changed to
 *                                @link #rtrue rtrue@endlink
 *                                to indicate heap now available.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_init_simple(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_init_simple);

    ; /* Nothing to do in this methodology */

    /* Declare this module initialized */
    *heap_init_flag = rtrue;

    return;

} /* END of heap_init_simple() */


/*!
 * @brief Most recent error code from @c @b malloc(3), for use
 * by heap_get_error_simple().
 */
static int heap_last_errno = ERROR0;


/*!
 * @brief Number of calls to @c @b malloc(3).
 *
 * One of three global variables providing rudimentary statistics
 * for heap allocation history.
 *
 * @see heap_free_count
 * @see heap_inuse_count
 */
static rlong heap_malloc_count = 0;

/*!
 * @brief Number of calls to @c @b free(3).
 *
 * One of three global variables providing rudimentary statistics
 * for heap allocation history.
 *
 * @see heap_malloc_count
 * @see heap_inuse_count
 */
static rlong heap_free_count   = 0;

/*!
 * @brief Number of @c @b malloc(3) blocks in use.
 *
 * One of three global variables providing rudimentary statistics
 * for heap allocation history.
 *
 * @see heap_malloc_count
 * @see heap_free_count
 */
static rlong heap_inuse_count   = 0;


/*!
 * @brief Report on status of heap initialization.
 *
 * Report on the heap configuration.
 *
 *
 * @param   heap_init_flag   Pointer to status of heap initialization.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_init_report_simple(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_init_report_simple);

#define SETUP_MSG_DML DML5 /**< Initialization report debug msg level */

    if (rtrue == *heap_init_flag)
    {
        sysDbgMsg(SETUP_MSG_DML,
                  arch_function_name,
                  "heap available");
    }
    else
    {
        sysDbgMsg(SETUP_MSG_DML,
                  arch_function_name,
                  "heap NOT available");
    }

    return;

} /* END of heap_init_report_simple() */


/*!
 * @brief Report on status of heap management.
 *
 * Report on the status of the number of allocations made, freed, and
 * currently in use.
 *
 *
 * @param   heap_init_flag   Pointer to status of heap initialization.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

rvoid heap_report_simple(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_report_simple);

#define CURRENT_MSG_DML DML5/**< Usage report debug msg level */

    if (rtrue == *heap_init_flag)
    {
        sysDbgMsg(CURRENT_MSG_DML,
                  arch_function_name,
                  "malloc = %8x  free = %8x  inuse = %8x\n",
                  heap_malloc_count,
                  heap_free_count,
                  heap_inuse_count);
    }
    else
    {
        sysDbgMsg(CURRENT_MSG_DML,
                  arch_function_name,
                  "heap NOT available");
    }

    return;

} /* END of heap_report_simple() */


/*!
 * @brief Simple heap allocation method that uses only @c @b malloc(3)
 * and @c @b free(3).
 *
 *
 * @param size         Number of bytes to allocate
 *
 * @param clrmem_flag  Set memory to all zeroes
 *                     (@link #rtrue rtrue@endlink) or not
 *                      (@link #rfalse rfalse@endlink).
 *                     If @link #rtrue rtrue@endlink, clear the
 *                     allocated block, otherwise
 *                     return it with its existing contents.
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.
 *          If size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let caller croak
 *          on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error, but
 *          do @e not return.
 *
 *
 * @throws JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         @link #JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         if no memory is available.@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_INTERNALERROR 
           @link #JVMCLASS_JAVA_LANG_INTERNALERROR
 *         if other allocation error@endlink.
 *
 */
static rvoid *heap_get_common_simple(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_comon_simple);

    rvoid *rc;
    int sizelocal = (int) size;

    rc = portable_malloc(sizelocal);

    /*
     * If specific errors are returned, GC could free up some heap,
     * so run it and try again-- ONCE.  If it fails a second time,
     * so be it.  Let the application deal with the problem.
     */
    if (rnull == rc)
    {
        switch(errno)
        {
            case ENOMEM:
            case EAGAIN:
                GC_RUN(rtrue);
                rc = portable_malloc(sizelocal);

                if (rnull == rc)
                {
                    switch(errno)
                    {
                        case ENOMEM:
                        case EAGAIN:
                            exit_throw_exception(EXIT_HEAP_ALLOC,
                                   JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR);
/*NOTREACHED*/
                        default:
                            /*
                             * Preserve errno for later inspection.
                             * By doing it this way, other OS system
                             * calls will not interfere with its value
                             * and it can be inspected at leisure.
                             */
                            heap_last_errno = errno;

                            exit_throw_exception(EXIT_HEAP_ALLOC,
                                      JVMCLASS_JAVA_LANG_INTERNALERROR);
/*NOTREACHED*/
                    }
                }
                break;

            default:
                /*
                 * Preserve errno for later inspection.
                 * By doing it this way, other OS system
                 * calls will not interfere with its value
                 * and it can be inspected at leisure.
                 */
                heap_last_errno = errno;

                exit_throw_exception(EXIT_HEAP_ALLOC,
                                     JVMCLASS_JAVA_LANG_INTERNALERROR);
/*NOTREACHED*/
        }
    }

    /* Clear block if requested */
    if (rtrue == clrmem_flag)
    {
        rbyte *pb = (rbyte *) rc;

        int i;
        for (i = 0; i < sizelocal; i++)
        {
            pb[i] = '\0';
        }
    }

    heap_malloc_count++;
    heap_inuse_count++;

    return(rc);

} /* END of heap_get_common_simple() */


/*!
 * @brief Allocate memory for a method from heap to caller.
 *
 * When finished, this pointer should be sent back
 * to @link #heap_free_method_simple() heap_free_method_simple()@endlink
 * for reallocation.
 *
 * @note This implementation makes no distinction betwen
 *       <b>method area heap </b> and any other usage.  Other
 *       implementations may choose to implement the
 *       JVM Spec section 3.5.4 more rigorously.
 *
 *
 * @param   size         Number of bytes to allocate
 *
 * @param   clrmem_flag  Set memory to all zeroes
 *                       (@link #rtrue rtrue@endlink) or
 *                       not (@link #rfalse rfalse@endlink)
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.
 *          If size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and
 *          let caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error, but
 *          do @e not return.
 *
 *
 * @throws JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         @link #JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         if no memory is available@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_INTERNALERROR 
           @link #JVMCLASS_JAVA_LANG_INTERNALERROR
 *         if other allocation error@endlink.
 *
 */
rvoid *heap_get_method_simple(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_method_simple);

    return(heap_get_common_simple(size, clrmem_flag));

} /* END of heap_get_method_simple() */


/*!
 * @brief Allocate memory for a stack area from heap to caller.
 *
 * When finished, this pointer should be sent back
 * to @link #heap_free_stack_simple() heap_free_stack_simple()@endlink
 * for reallocation.
 *
 *
 * @param   size         Number of bytes to allocate
 *
 * @param   clrmem_flag  Set memory to all zeroes
 *                       (@link #rtrue rtrue@endlink) or
 *                       not (@link #rfalse rfalse@endlink)
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.
 *          If size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and
 *          let caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error, but
 *          do @e not return.
 *
 *
 * @throws JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         @link #JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         if no memory is available@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_INTERNALERROR 
           @link #JVMCLASS_JAVA_LANG_INTERNALERROR
 *         if other allocation error@endlink.
 *
 *
 *
 */
rvoid *heap_get_stack_simple(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_stack_simple);

    return(heap_get_common_simple(size, clrmem_flag));

} /* END of heap_get_stack_simple() */


/*!
 * @brief Allocate memory for a data area from heap to caller.
 *
 * When finished, this pointer should be sent back
 * to @link #heap_free_data_simple() heap_free_data_simple()@endlink
 * for reallocation.
 *
 *
 * @param   size         Number of bytes to allocate
 *
 * @param   clrmem_flag  Set memory to all zeroes
 *                       (@link #rtrue rtrue@endlink) or
 *                       not (@link #rfalse rfalse@endlink)
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.
 *          If size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and
 *          let caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error, but
 *          do @e not return.
 *
 *
 * @throws JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         @link #JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR
 *         if no memory is available@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_INTERNALERROR 
           @link #JVMCLASS_JAVA_LANG_INTERNALERROR
 *         if other allocation error@endlink.
 *
 *
 *
 */
rvoid *heap_get_data_simple(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_data_simple);

    return(heap_get_common_simple(size, clrmem_flag));

} /* END of heap_get_data_simple() */


/*********************************************************************/
/*!
 * @brief Release a previously allocated block back into the heap for
 * future reallocation.
 *
 * If a @link #rnull rnull@endlink pointer is passed in, ignore
 * the request.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by one of the
                        @link #heap_get_data_simple()
                        heap_get_XXX_simple()@endlink
 *                      functions.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 *
 */
static rvoid heap_free_common_simple(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_common_simple);

    void *pheap_block_local = (void *) pheap_block;

    /* Ignore @link #rnull rnull@endlink pointer */
    if (rnull != pheap_block_local)
    {
        /* Free larger requests */
        heap_free_count++;
        heap_inuse_count--;

        portable_free(pheap_block_local);
    }

    return;

} /* END of heap_free_common_simple() */


/*!
 * @brief Release a previously allocated @b method block back into
 * the heap for future reallocation.
 *
 * @note  This implementation makes no distinction betwen
 *        <b>method area heap</b> and any other usage.  Other
 *        implementations may choose to implement the
 *        JVM Spec section 3.5.4 more rigorously.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by
 *                      @link #heap_get_method_simple()
                        heap_get_method_simple()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_method_simple(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_method_simple);

    heap_free_common_simple(pheap_block);

} /* END of heap_free_method_simple() */


/*!
 * @brief Release a previously allocated @b stack block back into
 * the heap for future reallocation.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by
 *                      @link #heap_get_stack_simple()
                        heap_get_stack_simple()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_stack_simple(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_stack_simple);

    heap_free_common_simple(pheap_block);

} /* END of heap_free_stack_simple() */


/*!
 * @brief Release a previously allocated @b data block back
 * into the heap for future reallocation.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by
 *                      @link #heap_get_data_simple()
                        heap_get_data_simple()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_data_simple(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_data_simple);

    heap_free_common_simple(pheap_block);

} /* END of heap_free_data_simple() */


/*!
 * @brief Allocation failure diagnostic.
 *
 * Returns an @b errno value per @b "errno.h" if a
 * @link #rnull rnull@endlink pointer
 * is passed in, namely from the most recent call to a heap
 * allocation function.  It may only be called once before the
 * value is cleared.  If a non-null pointer is passed in,
 * @link #ERROR0 ERROR0@endlink is returned and the error status is
 * again cleared.
 *
 *
 * @param   badptr   Return value from heap allocation function.
 *
 *
 * @returns @link #ERROR0 ERROR0@endlink when no error was found
 *          or non-null @b badptr given.
 *          @link #heap_last_errno heap_last_errno@endlink
 *          value otherwise.
 *
 */
int  heap_get_error_simple(rvoid *badptr)
{
    ARCH_FUNCTION_NAME(heap_get_error_simple);

    int rc;
    void *badptrlocal = (void *) badptr;

    if (rnull == badptrlocal)
    {
        rc = heap_last_errno;
        heap_last_errno = ERROR0;
        return(rc);
    }
    else
    {
        heap_last_errno = ERROR0;

        return(ERROR0);
    }

} /* END of heap_get_error_simple() */


/*!
 * @brief Shut down up heap management after JVM execution is finished.
 *
 *
 * @param[out]   heap_init_flag   Pointer to rboolean, changed to
 *                                @link #rtrue rtrue@endlink
 *                                to indicate heap no longer available.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_shutdown_simple(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_shutdown_simple);

    heap_last_errno = ERROR0;

    heap_report_simple(heap_init_flag);

    /* Declare this module uninitialized */
    *heap_init_flag = rfalse;

    return;

} /* END of heap_shutdown_simple() */



/* EOF */