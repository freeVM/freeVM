/*!
 * @file heap_bimodal.c
 *
 * @brief @b bimodal heap management functions
 *
 * This is the second of two implementations of heap management.
 * It extends the simple malloc/free scheme by adding several
 * large allocations for requests smaller than a certain size.
 * This was added due to an apparent internal limit in @c @b malloc(3)
 * or perhaps a kernel limit that after a certain number of
 * calls and/or bytes of allocation and freeing, returns NULL
 * for no apparent reason.
 *
 * The common header file @link jvm/src/heap.h gc.h@endlink defines
 * the prototypes for all heap allocation implementations by way
 * of the @link #CONFIG_HEAP_TYPE_SIMPLE CONFIG_HEAP_TYPE_xxx@endlink
 * symbol definitions.
 *
 *
 * Here is a note taken from the original project
 * @link ./README README@endlink file:
 *
 *     <i>(16) When nearing the end of the initial development, I ran
 *     across what is probably a memory configuration limit on my
 *     Solaris platform, which I did not bother to track down, but
 *     rather work around.  It seems that when calling malloc(3C) or
 *     malloc(3MALLOC), after 2,280 malloc() allocations and 612 free()
 *     invocations, there is something under the covers that does a
 *     @b SIGSEGV, and it can happen in either routine.  I therefore
 *     extended the heap mechanism to allocate 1M slots of 'n' bytes
 *     for small allocations up to this size.  Everything else still
 *     uses malloc().  In this way, I was able to finish development
 *     on the JVM and release it to the ASF in a more timely manner.
 *     In other words, I will let the team fix it!  I am not sure that
 *     the real project wants a static 'n + 1' MB data area just
 *     hanging around the runtime just because I did not take time to
 *     tune the system configuration!</i>
 *
 * This modified algorithm makes two @link #portable_malloc()
   portablemalloc()@endlink calls for each of a number of
 * graded allocation sizes, one containing an  array of fixed
 * size slots of (@link #rbyte rbyte@endlink), and one containing
 * an array of (@link #rboolean rboolean@endlink) for
 * @link #rtrue rtrue@endlink/@link #rfalse rfalse@endlink on
 * whether a fixed-size memory slot is in use or not.  Allocation
 * requests beyond a certain limit produces a call to
 * @link #portable_malloc() portable_malloc()@endlink
 * for that specific request.
 *
 * When it comes time to free the allocation, the pointer is
 * used to decide which block it belongs to and clears the
 * (@link #rboolean rboolean@endlink) associated with that
 * slot.  If a slot is not found, it is assumed to be a request
 * that @link #portable_free() portable_free()@endlink handles.
 *
 *
 * @todo HARMONY-6-jvm-heap_bimodal.c-1 Track calls to portable_malloc()
 *       in some way so that when a heap shutdown occurs (presumably
 *       due to a major failure or at exit time), all heap blocks are
 *       properly freed.  Although this is not really a bug, and
 *       although the code is fairly careful about freeing blocks when
 *       done with them, this enhancement would strengthen this heap
 *       allocation methodology considerably when the JVM is called
 *       as part of another program instead of directly stand-alone.
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
ARCH_SOURCE_COPYRIGHT_APACHE(heap_bimodal, c,
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
 * @name Small heap allocation areas.
 *
 * For allocations up to @b n bytes, use this instead of system
 * allocation and thus minimize number of calls to @b malloc()
 * and @b free().  After 2,280 calls to @b malloc(3C) and 612 to
 * @b free(3C), the library would kick out a @b SIGSEGV for no apparent
 * reason.  Use of @b malloc(3MALLOC) and @b free(3MALLOC) did
 * the same thing, with @b -lmalloc.  Use of @b -lbsdmalloc was
 * at an even lower number.  Therefore, it seems best to delay the
 * issue (See quotation above from @link ./README README@endlink file.)
 *
 * The value of the @link #HEAP_SLOT_SIZE_8 HEAP_SLOT_SIZE_nn@endlink
 * definition <b>ABSOLUTELY MUST BE A MULTIPLE of 4!!! </b>
 * (prefer 8 for future 64-bit implementation).  This is
 * <b><code>sizeof(rvoid *)</code></b> and must be such to always
 * fundamentally avoid @b SIGSEGV on 4-byte accesses.
 *
 */

/*@{ */ /* Begin grouped definitions */
 
#define HEAP_SLOT_SIZE_8             8
#define HEAP_SLOT_SIZE_16           16
#define HEAP_SLOT_SIZE_24           24
#define HEAP_SLOT_SIZE_32           32
#define HEAP_SLOT_SIZE_48           48
#define HEAP_SLOT_SIZE_64           64
#define HEAP_SLOT_SIZE_96           96
#define HEAP_SLOT_SIZE_128         128
#define HEAP_SLOT_SIZE_192         192
#define HEAP_SLOT_SIZE_256         256
#define HEAP_SLOT_SIZE_384         384
#define HEAP_SLOT_SIZE_512         512
#define HEAP_SLOT_SIZE_768         768
#define HEAP_SLOT_SIZE_1024       1024
#define HEAP_SLOT_SIZE_1536       1536
#define HEAP_SLOT_SIZE_2048       2048
#define HEAP_SLOT_SIZE_3072       3072
#define HEAP_SLOT_SIZE_4096       4096
#define HEAP_SLOT_SIZE_6144       6144
#define HEAP_SLOT_SIZE_8192       8192
#define HEAP_SLOT_SIZE_12288     12288
#define HEAP_SLOT_SIZE_16384     16384
#define HEAP_SLOT_SIZE_24576     24576
#define HEAP_SLOT_SIZE_32768     32768
#define HEAP_SLOT_SIZE_49152     49152
#define HEAP_SLOT_SIZE_65536     65536
#define HEAP_SLOT_SIZE_98304     98304
#define HEAP_SLOT_SIZE_131072   131072
#define HEAP_SLOT_SIZE_196608   196608
#define HEAP_SLOT_SIZE_262144   262144
#define HEAP_SLOT_SIZE_393216   393216
#define HEAP_SLOT_SIZE_524288   524288
#define HEAP_SLOT_SIZE_786432   786432
#define HEAP_SLOT_SIZE_1048576 1048576

#define HEAP_SLOT_SIZE_LARGEST HEAP_SLOT_SIZE_1048576

/*@} */ /* End of grouped definitions */

/*!
 * @name Number of slots of size <b>n</b>.
 *
 * Any number of slots is possible, up to the reasonable
 * resource limits of the machine.
 *
 * @internal Using a @link #HEAP_ATOM_SIZE HEAP_ATOM_SIZE@endlink
 * of <b><code>16 * 1024</code></b> for all of following increments
 * of 8-bytethrough 1M-byte slots, a grand total of 0x230d4d2b bytes
 * (560.83 MB) is allocated, 0x22b55800 bytes of data (555.33 MB)
 * plus 0x57f52b bytes of control in 0x57f52b (5,764,395) slots.
 *
 * Changes to the value of @link #HEAP_ATOM_SIZE HEAP_ATOM_SIZE@endlink
 * directly affect the overall heap size.  Any integer value will work:
 *
 * <ul>
 * <li><b>16</b>   Data size @b 555.33MB plus control size @b 5629.79KB
 * </li>
 * <li><b> 8</b>   Data size @b 277.17MB plus control size @b 2814.64KB
 * </li>
 * <li><b> 4</b>   Data size @b 138.33MB plus control size @b 1407.32KB
 * </li>
 * <li><b> 2</b>   Data size @b 68.67MB plus control size @b 703.66KB
 * </li>
 * <li><b> 1</b>   Data size @b 34.08MB plus control size @b 351.83KB
 * </li>
 * </ul>
 *
 * For example, changing the constant from <b><code>16 * 1024</code></b>
 * to <b><code>8 * 1024</code></b> cuts this in half to 277 MB plus
 * 2.8 MB of total heap space.
 *
 * Changing it to <b><code>4 * 1024</code></b> cuts it in half again
 * to 138 MB plus 1.4 MB of total heap space.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define HEAP_ATOM                    ( 1 * 1024) /**< Smallest sizing
                                                      increment */
#define HEAP_NUMBER_OF_SLOTS_8       (HEAP_ATOM * 128        )
#define HEAP_NUMBER_OF_SLOTS_16      (HEAP_ATOM *  64        )
#define HEAP_NUMBER_OF_SLOTS_24      (HEAP_ATOM *  32 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_32      (HEAP_ATOM *  32        )
#define HEAP_NUMBER_OF_SLOTS_48      (HEAP_ATOM *  16 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_64      (HEAP_ATOM *  16        )
#define HEAP_NUMBER_OF_SLOTS_96      (HEAP_ATOM *   8 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_128     (HEAP_ATOM *   8        )
#define HEAP_NUMBER_OF_SLOTS_192     (HEAP_ATOM *   4 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_256     (HEAP_ATOM *   4        )
#define HEAP_NUMBER_OF_SLOTS_384     (HEAP_ATOM *   2 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_512     (HEAP_ATOM *   2        )
#define HEAP_NUMBER_OF_SLOTS_768     (HEAP_ATOM *   1 * 3 / 2)
#define HEAP_NUMBER_OF_SLOTS_1024    (HEAP_ATOM *   1        )
#define HEAP_NUMBER_OF_SLOTS_1536    (HEAP_ATOM *   2 / 3    )
#define HEAP_NUMBER_OF_SLOTS_2048    (HEAP_ATOM *   1 / 2    )
#define HEAP_NUMBER_OF_SLOTS_3072    (HEAP_ATOM *   1 / 3    )
#define HEAP_NUMBER_OF_SLOTS_4096    (HEAP_ATOM *   1 / 4    )
#define HEAP_NUMBER_OF_SLOTS_6144    (HEAP_ATOM *   1 / 6    )
#define HEAP_NUMBER_OF_SLOTS_8192    (HEAP_ATOM *   1 / 8    )
#define HEAP_NUMBER_OF_SLOTS_12288   (HEAP_ATOM *   1 / 12   )
#define HEAP_NUMBER_OF_SLOTS_16384   (HEAP_ATOM *   1 / 16   )
#define HEAP_NUMBER_OF_SLOTS_24576   (HEAP_ATOM *   1 / 24   )
#define HEAP_NUMBER_OF_SLOTS_32768   (HEAP_ATOM *   1 / 32   )
#define HEAP_NUMBER_OF_SLOTS_49152   (HEAP_ATOM *   1 / 48   )
#define HEAP_NUMBER_OF_SLOTS_65536   (HEAP_ATOM *   1 / 64   )
#define HEAP_NUMBER_OF_SLOTS_98304   (HEAP_ATOM *   1 / 96   )
#define HEAP_NUMBER_OF_SLOTS_131072  (HEAP_ATOM *   1 / 128  )
#define HEAP_NUMBER_OF_SLOTS_196608  (HEAP_ATOM *   1 / 192  )
#define HEAP_NUMBER_OF_SLOTS_262144  (HEAP_ATOM *   1 / 256  )
#define HEAP_NUMBER_OF_SLOTS_393216  (HEAP_ATOM *   1 / 384  )
#define HEAP_NUMBER_OF_SLOTS_524288  (HEAP_ATOM *   1 / 512  )
#define HEAP_NUMBER_OF_SLOTS_786432  (HEAP_ATOM *   1 / 768  )
#define HEAP_NUMBER_OF_SLOTS_1048576 (HEAP_ATOM *   1 / 1024  )

/*@} */ /* End of grouped definitions */


/*!
 * @brief Describe a heap allocation area
 *
 * Each heap allocation area stores requests of up to <b>n</b> bytes
 * arranged as an array of slots of this size.  As requests for
 * allocation come in, the next slot is found, its boolean flag
 * is set, and the address of the slot is returned.  When requests for
 * dellocation arrive, the pointer is compared against the range of
 * addresses in the appropriate area (of <b>n</b> bytes and larger),
 * and the flag for that slot is cleared.  This is the structure
 * where this algorithm is implemented.
 *
 */
typedef struct
{
   rboolean *pslot_in_use_flag;/**< Pointer to boolean marker for each
                                    <b>n</b>-sized slots in use. */

   rbyte    *pslot;            /**< Pointer to array of <b>n</b>-size
                                    slots. */

   rulong    slot_alloc_count; /**< Number of <b>n</b>-size allocations
                                    that have been requested. */

   rulong    slot_free_count;  /**< Number of <b>n</b>-sized allocations
                                    that have been freed up. */

   rulong    slot_inuse_count; /**< Number of <b>n</b>-sized allocations
                                    currently in use */

} heaparea;

#define HEAP_ALLOCATION_INIT(allocname)                      \
    heaparea heaparea_##allocname =                          \
    {                                                        \
        pslot_in_use_flag: CHEAT_AND_USE_NULL_TO_INITIALIZE, \
        pslot:             CHEAT_AND_USE_NULL_TO_INITIALIZE, \
        slot_alloc_count:  0,                                \
        slot_free_count:   0,                                \
        slot_inuse_count:  0,                                \
    }

HEAP_ALLOCATION_INIT(8);
HEAP_ALLOCATION_INIT(16);
HEAP_ALLOCATION_INIT(24);
HEAP_ALLOCATION_INIT(32);
HEAP_ALLOCATION_INIT(48);
HEAP_ALLOCATION_INIT(64);
HEAP_ALLOCATION_INIT(96);
HEAP_ALLOCATION_INIT(128);
HEAP_ALLOCATION_INIT(192);
HEAP_ALLOCATION_INIT(256);
HEAP_ALLOCATION_INIT(384);
HEAP_ALLOCATION_INIT(512);
HEAP_ALLOCATION_INIT(768);
HEAP_ALLOCATION_INIT(1024);
HEAP_ALLOCATION_INIT(1536);
HEAP_ALLOCATION_INIT(2048);
HEAP_ALLOCATION_INIT(3072);
HEAP_ALLOCATION_INIT(4096);
HEAP_ALLOCATION_INIT(6144);
HEAP_ALLOCATION_INIT(8192);
HEAP_ALLOCATION_INIT(12288);
HEAP_ALLOCATION_INIT(16384);
HEAP_ALLOCATION_INIT(24576);
HEAP_ALLOCATION_INIT(32768);
HEAP_ALLOCATION_INIT(49152);
HEAP_ALLOCATION_INIT(65536);
HEAP_ALLOCATION_INIT(98304);
HEAP_ALLOCATION_INIT(131072);
HEAP_ALLOCATION_INIT(196608);
HEAP_ALLOCATION_INIT(262144);
HEAP_ALLOCATION_INIT(393216);
HEAP_ALLOCATION_INIT(524288);
HEAP_ALLOCATION_INIT(786432);
HEAP_ALLOCATION_INIT(1048576);

/* Instantiate _only_ for the counters. The pointers are obviously N/A*/
HEAP_ALLOCATION_INIT(malloc);


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
rvoid heap_init_bimodal(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_init_bimodal);

    rlong heapidx;

    /* Set up slot flags */

#warning Change rboolean control array to bit array in rbyte
#define SETUP_HEAP_AREA(allocsize)                                     \
    heaparea_##allocsize.pslot_in_use_flag =                           \
        (rboolean *) portable_malloc(sizeof(rboolean) *                \
                                     HEAP_NUMBER_OF_SLOTS_##allocsize);\
    if (rnull == heaparea_##allocsize.pslot_in_use_flag)               \
    {                                                                  \
        sysErrMsg(arch_function_name,                                  \
                  "Cannot allocate %d-byte slot flag storage",         \
                  allocsize);                                          \
        exit_jvm(EXIT_HEAP_ALLOC);                                     \
/*NOTREACHED*/                                                         \
    }                                                                  \
    /* Initialize flag array to @link #rfalse rfalse@endlink */        \
    for (heapidx = 0;                                                  \
         heapidx < HEAP_NUMBER_OF_SLOTS_##allocsize;                   \
         heapidx++)                                                    \
    {                                                                  \
        heaparea_##allocsize.pslot_in_use_flag[heapidx] = rfalse;      \
    }                                                                  \
                                                                       \
    /* Set up slot storage itself, do not need to initialize */        \
    heaparea_##allocsize.pslot =                                       \
        (rbyte *) (portable_malloc(sizeof(rbyte) *                     \
                                       HEAP_SLOT_SIZE_##allocsize *    \
                                   HEAP_NUMBER_OF_SLOTS_##allocsize)); \
                                                                       \
    if (rnull == heaparea_##allocsize.pslot)                           \
    {                                                                  \
        portable_free(heaparea_##allocsize.pslot_in_use_flag);         \
                                                                       \
        sysErrMsg(arch_function_name,                                  \
                  "Cannot allocate %d-byte slot storage",              \
                  allocsize);                                          \
        exit_jvm(EXIT_HEAP_ALLOC);                                     \
/*NOTREACHED*/                                                         \
    }

    SETUP_HEAP_AREA(8);
    SETUP_HEAP_AREA(16);
    SETUP_HEAP_AREA(24);
    SETUP_HEAP_AREA(32);
    SETUP_HEAP_AREA(48);
    SETUP_HEAP_AREA(64);
    SETUP_HEAP_AREA(96);
    SETUP_HEAP_AREA(128);
    SETUP_HEAP_AREA(192);
    SETUP_HEAP_AREA(256);
    SETUP_HEAP_AREA(384);
    SETUP_HEAP_AREA(512);
    SETUP_HEAP_AREA(768);
    SETUP_HEAP_AREA(1024);
    SETUP_HEAP_AREA(1536);
    SETUP_HEAP_AREA(2048);
    SETUP_HEAP_AREA(3072);
    SETUP_HEAP_AREA(4096);
    SETUP_HEAP_AREA(6144);
    SETUP_HEAP_AREA(8192);
    SETUP_HEAP_AREA(12288);
    SETUP_HEAP_AREA(16384);
    SETUP_HEAP_AREA(24576);
    SETUP_HEAP_AREA(32768);
    SETUP_HEAP_AREA(49152);
    SETUP_HEAP_AREA(65536);
    SETUP_HEAP_AREA(98304);
    SETUP_HEAP_AREA(131072);
    SETUP_HEAP_AREA(196608);
    SETUP_HEAP_AREA(262144);
    SETUP_HEAP_AREA(393216);
    SETUP_HEAP_AREA(524288);
    SETUP_HEAP_AREA(786432);
    SETUP_HEAP_AREA(1048576);

    /* Declare this module initialized */
    *heap_init_flag = rtrue;

    return;

} /* END of heap_init_bimodal() */


/*!
 * @brief Most recent error code from @c @b malloc(3), for use
 * by heap_get_error_bimodal().
 *
 */
static int heaparea_malloc_last_errno = ERROR0;


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
rvoid heap_init_report_bimodal(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_init_report_bimodal);

#define SETUP_MSG_DML DML5 /**< Initialization report debug msg level */

    ruint  this_size_slots      = 0;
    ruint  total_slots          = 0;
    ruint  this_size_data_bytes = 0;
    rulong total_data_bytes     = 0;
    ruint  this_size_ctl_bytes  = 0;
    ruint  total_ctl_bytes      = 0;

#define SETUP_MSG(allocsize)                                           \
    this_size_slots       = (HEAP_NUMBER_OF_SLOTS_##allocsize);        \
    total_slots          += this_size_slots;                           \
                                                                       \
    this_size_data_bytes  =(sizeof(rbyte)* HEAP_SLOT_SIZE_##allocsize *\
                                HEAP_NUMBER_OF_SLOTS_##allocsize);     \
    total_data_bytes     += this_size_data_bytes;                      \
                                                                       \
    this_size_ctl_bytes   = (sizeof(rboolean) *                        \
                                HEAP_NUMBER_OF_SLOTS_##allocsize);     \
    total_ctl_bytes      += this_size_ctl_bytes;                       \
                                                                       \
    sysDbgMsg(SETUP_MSG_DML,                                           \
              arch_function_name,                                      \
              "size:  %6x  count: %8x -- %10x bytes data + %6x ctl",   \
              (HEAP_SLOT_SIZE_##allocsize),                            \
              this_size_slots,                                         \
              this_size_data_bytes,                                    \
              this_size_ctl_bytes); /* Extra ; */

    if (rtrue == *heap_init_flag)
    {
        SETUP_MSG(8);
        SETUP_MSG(16);
        SETUP_MSG(24);
        SETUP_MSG(32);
        SETUP_MSG(48);
        SETUP_MSG(64);
        SETUP_MSG(96);
        SETUP_MSG(128);
        SETUP_MSG(192);
        SETUP_MSG(256);
        SETUP_MSG(384);
        SETUP_MSG(512);
        SETUP_MSG(768);
        SETUP_MSG(1024);
        SETUP_MSG(1536);
        SETUP_MSG(2048);
        SETUP_MSG(3072);
        SETUP_MSG(4096);
        SETUP_MSG(6144);
        SETUP_MSG(8192);
        SETUP_MSG(12288);
        SETUP_MSG(16384);
        SETUP_MSG(24576);
        SETUP_MSG(32768);
        SETUP_MSG(49152);
        SETUP_MSG(65536);
        SETUP_MSG(98304);
        SETUP_MSG(131072);
        SETUP_MSG(196608);
        SETUP_MSG(262144);
        SETUP_MSG(393216);
        SETUP_MSG(524288);
        SETUP_MSG(786432);
        SETUP_MSG(1048576);

        sysDbgMsg(SETUP_MSG_DML,
                  arch_function_name,
             "total:         slots: %8x -- %10llx bytes data + %6x ctl",
                  total_slots,
                  total_data_bytes,
                  total_ctl_bytes);
    }
    else
    {
        sysDbgMsg(SETUP_MSG_DML,
                  arch_function_name,
                  "heap NOT available");
    }

    return;

} /* END of heap_init_report_bimodal() */


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
rvoid heap_report_bimodal(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_report_bimodal);

#define CURRENT_MSG_DML DML5/**< Usage report debug msg level */

#define CURRENT_MSG(allocsize)                                         \
    sysDbgMsg(CURRENT_MSG_DML,                                         \
              arch_function_name,                                      \
              "size: %6x  alloc = %8llx  free = %8llx  inuse = %8llx", \
              HEAP_SLOT_SIZE_##allocsize,                              \
              heaparea_##allocsize.slot_alloc_count,                   \
              heaparea_##allocsize.slot_free_count,                    \
              heaparea_##allocsize.slot_inuse_count)

    if (rtrue == *heap_init_flag)
    {
        CURRENT_MSG(8);
        CURRENT_MSG(16);
        CURRENT_MSG(24);
        CURRENT_MSG(32);
        CURRENT_MSG(48);
        CURRENT_MSG(64);
        CURRENT_MSG(96);
        CURRENT_MSG(128);
        CURRENT_MSG(192);
        CURRENT_MSG(256);
        CURRENT_MSG(384);
        CURRENT_MSG(512);
        CURRENT_MSG(768);
        CURRENT_MSG(1024);
        CURRENT_MSG(1536);
        CURRENT_MSG(2048);
        CURRENT_MSG(3072);
        CURRENT_MSG(4096);
        CURRENT_MSG(6144);
        CURRENT_MSG(8192);
        CURRENT_MSG(12288);
        CURRENT_MSG(16384);
        CURRENT_MSG(24576);
        CURRENT_MSG(32768);
        CURRENT_MSG(49152);
        CURRENT_MSG(65536);
        CURRENT_MSG(98304);
        CURRENT_MSG(131072);
        CURRENT_MSG(196608);
        CURRENT_MSG(262144);
        CURRENT_MSG(393216);
        CURRENT_MSG(524288);
        CURRENT_MSG(786432);
        CURRENT_MSG(1048576);

        sysDbgMsg(CURRENT_MSG_DML,
                  arch_function_name,
                "dynamic:     malloc = %l8x  free = %l8x  inuse = %l8x",
                  heaparea_malloc.slot_alloc_count,
                  heaparea_malloc.slot_free_count,
                  heaparea_malloc.slot_inuse_count);
    }
    else
    {
        sysDbgMsg(CURRENT_MSG_DML,
                  arch_function_name,
                  "heap NOT available");
    }

    return;

} /* END of heap_report_bimodal() */


/*!
 * @brief Original heap allocation method that uses
 * @e only @c @b malloc(3) and @c @b free(3).
 *
 *
 * @param size         Number of bytes to allocate
 *
 * @param clrmem_flag  Set memory to all zeroes
 *                     (@link #rtrue rtrue@endlink) or not
 *                     (@link #rfalse rfalse@endlink).
 *                     If @link #rtrue rtrue@endlink,
 *                     clear the allocated block, otherwise
 *                     return it with its existing contents.
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.  If
 *          size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let caller croak
 *          on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error,
 *          but do @e not return.
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
static rvoid *heap_get_common_simple_bimodal(rint size,
                                             rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_common_simple_bimodal);

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
                            heaparea_malloc_last_errno = errno;

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
                heaparea_malloc_last_errno = errno;

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

    heaparea_malloc.slot_alloc_count++;
    heaparea_malloc.slot_inuse_count++;

    return(rc);

} /* END of heap_get_common_simple_bimodal() */


/*!
 * @brief Allocate memory from heap to caller, judging which mode
 * to use for allocation.
 *
 * When finished, this pointer should be sent back to
 * @link #heap_free_data_bimodal() heap_free_xxxx_bimodal()@endlink
 * for reallocation.
 *
 * @warning  Much of the JVM initialization ABSOLUTELY DEPENDS on
 *           setting of the @b clrmem_flag value to
 *           @link #rtrue rtrue@endlink so that allocated
 *           structures contain all zeroes.  If the heap
 *           allocation scheme changes, this functionality needs
 *           to be brought forward or change much of the code, not
 *           only init code, but throughout the whole corpus.
 *
 * @remarks  If @c @b malloc(3) returns an error other than out of
 *           memory errors, then the system @b errno is saved out into
 *           @link #heaparea_malloc_last_errno
                    heaparea_malloc_last_errno@endlink
 *           for retrieval by @c @b perror(3) or other user response.
 *           This is typically useful for system-level debugging
 *           when the OS or OS resources, security, etc., may be
 *           getting in the way of proper allocation.
 *
 *
 * @param   size         Number of bytes to allocate
 *
 * @param   clrmem_flag  Set memory to all zeroes
 *                       (@link #rtrue rtrue@endlink) or
 *                       not (@link #rfalse rfalse@endlink).
 *                       If @link #rtrue rtrue@endlink,
 *                       clear the allocated block, otherwise
 *                       return it with its existing contents.
 *
 *
 * @returns (@link #rvoid rvoid@endlink *) to allocated area.
 *          This pointer may be cast to any desired data type.  If
 *          size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let caller croak
 *          on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error,
 *          but do @e not return.
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
static rvoid *heap_get_common_bimodal(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_common_bimodal);

    int sizelocal = (int) size;

    rvoid *rc; /* Separate LOCATE_SLOT calc. from return() for debug */
    rc = (rvoid *) rnull;

    /*
     * Return rnull pointer when zero size requested.
     * Let caller fix that problem.
     */
    if (0 == sizelocal)
    {
        return((rvoid *) rnull);
    }

    errno = ERROR0;   /* Clear out error code before calling */


    /* Use pre-allocated area for small requests */
    if (HEAP_SLOT_SIZE_LARGEST >= sizelocal)
    {
#define HEAPIDXLAST(allocsize)                        \
    static rlong heapidxLAST_##allocsize =            \
                 HEAP_NUMBER_OF_SLOTS_##allocsize - 1; /* Extra ; */

        HEAPIDXLAST(8);
        HEAPIDXLAST(16);
        HEAPIDXLAST(24);
        HEAPIDXLAST(32);
        HEAPIDXLAST(48);
        HEAPIDXLAST(64);
        HEAPIDXLAST(96);
        HEAPIDXLAST(128);
        HEAPIDXLAST(192);
        HEAPIDXLAST(256);
        HEAPIDXLAST(384);
        HEAPIDXLAST(512);
        HEAPIDXLAST(768);
        HEAPIDXLAST(1024);
        HEAPIDXLAST(1536);
        HEAPIDXLAST(2048);
        HEAPIDXLAST(3072);
        HEAPIDXLAST(4096);
        HEAPIDXLAST(6144);
        HEAPIDXLAST(8192);
        HEAPIDXLAST(12288);
        HEAPIDXLAST(16384);
        HEAPIDXLAST(24576);
        HEAPIDXLAST(32768);
        HEAPIDXLAST(49152);
        HEAPIDXLAST(65536);
        HEAPIDXLAST(98304);
        HEAPIDXLAST(131072);
        HEAPIDXLAST(196608);
        HEAPIDXLAST(262144);
        HEAPIDXLAST(393216);
        HEAPIDXLAST(524288);
        HEAPIDXLAST(786432);
        HEAPIDXLAST(1048576);

        rlong heapidx;  /* Just in case of large number of slots */
        rlong count;

        /*
         * Mark last allocated-- faster than always starting at 0.
         * Scan flag array for first open slot
         */

#define LOCATE_SLOT(allocsize)                                     \
                                                                   \
        for (count = 0, heapidx = 1 + heapidxLAST_##allocsize;     \
             count < HEAP_NUMBER_OF_SLOTS_##allocsize;             \
             count++, heapidx++)                                   \
        {                                                          \
            /* Wrap around last allocation to beginning */         \
            if (HEAP_NUMBER_OF_SLOTS_##allocsize == heapidx)       \
            {                                                      \
                heapidx = 0;                                       \
            }                                                      \
                                                                   \
            if (rfalse ==                                          \
                heaparea_##allocsize.pslot_in_use_flag[heapidx])   \
            {                                                      \
                /* Reserve a slot, return its data area pointer */ \
                heaparea_##allocsize.pslot_in_use_flag[heapidx] =  \
                                                            rtrue; \
                                                                   \
                /* Also report which slot was last allocated */    \
                heapidxLAST_##allocsize = heapidx;                 \
                                                                   \
                /* Count slot allocations */                       \
                heaparea_##allocsize.slot_alloc_count++;           \
                                                                   \
                /* Count slot allocations in use */                \
                heaparea_##allocsize.slot_inuse_count++;           \
                                                                   \
                rc = (rvoid *)                                     \
                     &heaparea_##allocsize.pslot[heapidx *         \
                                             sizeof(rbyte) *       \
                                      HEAP_SLOT_SIZE_##allocsize]; \
                                                                   \
                /* Clear block if requested */                     \
                if (rtrue == clrmem_flag)                          \
                {                                                  \
                    rbyte *pb = (rbyte *) rc;                      \
                                                                   \
                    rint i;                                        \
                    for (i = 0; i < sizelocal; i++)                \
                    {                                              \
                        pb[i] = '\0';                              \
                    }                                              \
                }                                                  \
                break;                                             \
            }                                                      \
        }                                                          \
        if (rnull != rc)                                           \
        {                                                          \
            return(rc);                                            \
        }

#define LOCATE_SLOT_2X(allocsize)                          \
        /* Study heap twice, before and after GC */        \
        LOCATE_SLOT(allocsize);                            \
                                                           \
        /* If could not allocate, do one retry after GC */ \
        GC_RUN(rtrue);                                     \
                                                           \
        /* Scan a second time for first open slot */       \
                                                           \
        LOCATE_SLOT(allocsize);                            \
                                                           \
        if (rnull != rc)                                   \
        {                                                  \
            return(rc);                                    \
        }

        /*
         * Scan slot for first fit into best size,
         * then into larger size.
         */
        if (     8 >= sizelocal)  { LOCATE_SLOT_2X(8); }
        if (    16 >= sizelocal)  { LOCATE_SLOT_2X(16); }
        if (    24 >= sizelocal)  { LOCATE_SLOT_2X(24); }
        if (    32 >= sizelocal)  { LOCATE_SLOT_2X(32); }
        if (    48 >= sizelocal)  { LOCATE_SLOT_2X(48); }
        if (    64 >= sizelocal)  { LOCATE_SLOT_2X(64); }
        if (    96 >= sizelocal)  { LOCATE_SLOT_2X(96); }
        if (   128 >= sizelocal)  { LOCATE_SLOT_2X(128); }
        if (   192 >= sizelocal)  { LOCATE_SLOT_2X(192); }
        if (   256 >= sizelocal)  { LOCATE_SLOT_2X(256); }
        if (   384 >= sizelocal)  { LOCATE_SLOT_2X(384); }
        if (   512 >= sizelocal)  { LOCATE_SLOT_2X(512); }
        if (   768 >= sizelocal)  { LOCATE_SLOT_2X(768); }
        if (  1024 >= sizelocal)  { LOCATE_SLOT_2X(1024); }
        if (  1536 >= sizelocal)  { LOCATE_SLOT_2X(1536); }
        if (  2048 >= sizelocal)  { LOCATE_SLOT_2X(2048); }
        if (  3072 >= sizelocal)  { LOCATE_SLOT_2X(3072); }
        if (  4096 >= sizelocal)  { LOCATE_SLOT_2X(4096); }
        if (  6144 >= sizelocal)  { LOCATE_SLOT_2X(6144); }
        if (  8192 >= sizelocal)  { LOCATE_SLOT_2X(8192); }
        if ( 12288 >= sizelocal)  { LOCATE_SLOT_2X(12288); }
        if ( 16384 >= sizelocal)  { LOCATE_SLOT_2X(16384); }
        if ( 24576 >= sizelocal)  { LOCATE_SLOT_2X(24576); }
        if ( 32768 >= sizelocal)  { LOCATE_SLOT_2X(32768); }
        if ( 49152 >= sizelocal)  { LOCATE_SLOT_2X(49152); }
        if ( 65536 >= sizelocal)  { LOCATE_SLOT_2X(65536); }
        if ( 98304 >= sizelocal)  { LOCATE_SLOT_2X(98304); }
        if (131072 >= sizelocal)  { LOCATE_SLOT_2X(131072); }
        if (196608 >= sizelocal)  { LOCATE_SLOT_2X(196608); }
        if (262144 >= sizelocal)  { LOCATE_SLOT_2X(262144); }
        if (393216 >= sizelocal)  { LOCATE_SLOT_2X(393216); }
        if (524288 >= sizelocal)  { LOCATE_SLOT_2X(524288); }
        if (786432 >= sizelocal)  { LOCATE_SLOT_2X(786432); }
        if (1048576 >= sizelocal) { LOCATE_SLOT_2X(1048576); }


/*
 * Permit an attempt to do malloc() if no slot available.
 * If not slot, then let malloc() throw the error instead.
 */
#if 1
    }
#else
/*  {   ... match braces for editors */

        /* Sorry, nothing available, throw error */

        heaparea_malloc_last_errno = ERROR0; /* No OS error */

        exit_throw_exception(EXIT_HEAP_ALLOC,
                             JVMCLASS_JAVA_LANG_OUTOFMEMORYERROR);
    }
    else
#endif
    {
        return(heap_get_common_simple_bimodal(sizelocal, clrmem_flag));
    }
/*NOTREACHED*/
    return((rvoid *) rnull); /* Satisfy compiler */

} /* END of heap_get_common_bimodal() */


/*!
 * @brief Allocate memory for a @b method from heap to caller.
 *
 * When finished, this pointer should be sent back to
 * @link #heap_free_method_bimodal() heap_free_method_bimodal()@endlink
 * for reallocation.
 *
 * @remarks This implementation makes no distinction betwen
 *          "method area heap" and any other usage.  Other
 *          implementations may choose to implement the
 *          JVM Spec section 3.5.4 more rigorously.
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
 *          This pointer may be cast to any desired data type.  If
 *          size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let
 *          caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error,
 *          but do @e not return.
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
rvoid *heap_get_method_bimodal(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_method_bimodal);

    return(heap_get_common_bimodal(size, clrmem_flag));

} /* END of heap_get_method_bimodal() */


/*!
 * @brief Allocate memory for a @b stack area from heap to caller.
 *
 * When finished, this pointer should be sent back
 * to @link #heap_free_stack_bimodal() heap_free_stack_bimodal()@endlink
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
 *          This pointer may be cast to any desired data type.  If
 *          size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let
 *          caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error,
 *          but do @e not return.
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
rvoid *heap_get_stack_bimodal(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_stack_bimodal);

    return(heap_get_common_bimodal(size, clrmem_flag));

} /* END of heap_get_stack_bimodal() */


/*!
 * @brief Allocate memory for a @b data area from heap to caller.
 *
 * When finished, this pointer should be sent back
 * to @link #heap_free_data_bimodal() heap_free_data_bimodal()@endlink
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
 *          This pointer may be cast to any desired data type.  If
 *          size of zero bytes is requested, return
 *          @link #rnull rnull@endlink and let
 *          caller croak on @b SIGSEGV.  If no memory is available
 *          or some OS system call error happened, throw error,
 *          but do @e not return.
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
rvoid *heap_get_data_bimodal(rint size, rboolean clrmem_flag)
{
    ARCH_FUNCTION_NAME(heap_get_data_bimodal);

    return(heap_get_common_bimodal(size, clrmem_flag));

} /* END of heap_get_data_bimodal() */


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
 *                      @link #heap_get_data_bimodal()
                        heap_get_XXX_bimodal()@endlink functions.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 *
 */
static rvoid heap_free_common_bimodal(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_common_bimodal);

    void *pheap_block_local = (void *) pheap_block;

    /* Ignore @link #rnull rnull@endlink pointer */
    if (rnull != pheap_block_local)
    {
        /*
         * Free pre-allocated area from deallocation of
         * small requests, all of which were allocated
         * in these blocks.
         */

#define UNLOCATE_SLOT(allocsize)                                  \
        if ((((rbyte *) &heaparea_##allocsize.pslot[0]) <=        \
             ((rbyte *) pheap_block_local))                       \
            &&                                                    \
            (((rbyte *)                                           \
              &heaparea_##allocsize.pslot[sizeof(rbyte)  *        \
                          HEAP_SLOT_SIZE_##allocsize *            \
                          HEAP_NUMBER_OF_SLOTS_##allocsize]) >    \
             ((rbyte *) pheap_block_local)))                      \
        {                                                         \
            rlong heapidx = (((rbyte *) pheap_block_local) -      \
                             (&heaparea_##allocsize.pslot[0])) /  \
                            HEAP_SLOT_SIZE_##allocsize;           \
                                                                  \
            if (rfalse ==                                         \
                heaparea_##allocsize.pslot_in_use_flag[heapidx])  \
            {                                                     \
                sysErrMsg(arch_function_name,                     \
                          "Cannot deallocate free storage slot "  \
                            "%ld in %d-byte area, ptr=%p",        \
                          heapidx,                                \
                          allocsize,                              \
                          pheap_block_local);                     \
                exit_jvm(EXIT_HEAP_FREE);                         \
/*NOTREACHED*/                                                    \
            }                                                     \
                                                                  \
            heaparea_##allocsize.pslot_in_use_flag[heapidx] =     \
                                                          rfalse; \
                                                                  \
            /* Count slots freed */                               \
            heaparea_##allocsize.slot_free_count++;               \
                                                                  \
            /* Count slot allocations in use */                   \
            heaparea_##allocsize.slot_inuse_count--;              \
                                                                  \
            return;                                               \
        }

        /*
         * Attempt to locate in the best fit areas.  In this
         * case, order makes no difference and the mechanism is
         * a simple range test, not a table search.
         */
        UNLOCATE_SLOT(8);
        UNLOCATE_SLOT(16);
        UNLOCATE_SLOT(24);
        UNLOCATE_SLOT(32);
        UNLOCATE_SLOT(48);
        UNLOCATE_SLOT(64);
        UNLOCATE_SLOT(96);
        UNLOCATE_SLOT(128);
        UNLOCATE_SLOT(192);
        UNLOCATE_SLOT(256);
        UNLOCATE_SLOT(384);
        UNLOCATE_SLOT(512);
        UNLOCATE_SLOT(768);
        UNLOCATE_SLOT(1024);
        UNLOCATE_SLOT(1536);
        UNLOCATE_SLOT(2048);
        UNLOCATE_SLOT(3072);
        UNLOCATE_SLOT(4096);
        UNLOCATE_SLOT(6144);
        UNLOCATE_SLOT(8192);
        UNLOCATE_SLOT(12288);
        UNLOCATE_SLOT(16384);
        UNLOCATE_SLOT(24576);
        UNLOCATE_SLOT(32768);
        UNLOCATE_SLOT(49152);
        UNLOCATE_SLOT(65536);
        UNLOCATE_SLOT(98304);
        UNLOCATE_SLOT(131072);
        UNLOCATE_SLOT(196608);
        UNLOCATE_SLOT(262144);
        UNLOCATE_SLOT(393216);
        UNLOCATE_SLOT(524288);
        UNLOCATE_SLOT(786432);
        UNLOCATE_SLOT(1048576);

        /* Free larger requests that used portable_malloc() */
        heaparea_malloc.slot_free_count++;
        heaparea_malloc.slot_inuse_count--;

        portable_free(pheap_block_local);

        return;
    }

    return;

} /* END of heap_free_common_bimodal() */


/*!
 * @brief Release a previously allocated @b method block back into
 * the heap for future reallocation.
 *
 * @remarks  This implementation makes no distinction between
 *           <b>method area heap</b> and any other usage.  Other
 *           implementations may choose to implement the
 *           JVM Spec section 3.5.4 more rigorously.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by @link #heap_get_method_bimodal()
                        heap_get_method_bimodal()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_method_bimodal(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_method_bimodal);

    heap_free_common_bimodal(pheap_block);

} /* END of heap_free_method_bimodal() */


/*!
 * @brief Release a previously allocated @b stack block back into
 * the heap for future reallocation.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by @link #heap_get_stack_bimodal()
                        heap_get_stack_bimodal()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_stack_bimodal(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_stack_bimodal);

    heap_free_common_bimodal(pheap_block);

} /* END of heap_free_stack_bimodal() */


/*!
 * @brief Release a previously allocated @b data block back
 * into the heap for future reallocation.
 *
 *
 * @param  pheap_block  An (@link #rvoid rvoid@endlink *) previously
 *                      returned by @link #heap_get_data_bimodal()
                        heap_get_data_bimodal()@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid heap_free_data_bimodal(rvoid *pheap_block)
{
    ARCH_FUNCTION_NAME(heap_free_data_bimodal);

    heap_free_common_bimodal(pheap_block);

} /* END of heap_free_data_bimodal() */


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
 *          @link #heaparea_malloc_last_errno
                   heaparea_malloc_last_errno@endlink
 *          value otherwise.
 *
 */
int  heap_get_error_bimodal(rvoid *badptr)
{
    ARCH_FUNCTION_NAME(heap_get_error_bimodal);

    int rc;
    void *badptrlocal = (void *) badptr;

    if (rnull == badptrlocal)
    {
        rc = heaparea_malloc_last_errno;
        heaparea_malloc_last_errno = ERROR0;
        return(rc);
    }
    else
    {
        heaparea_malloc_last_errno = ERROR0;

        return(ERROR0);
    }

} /* END of heap_get_error_bimodal() */


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
rvoid heap_shutdown_bimodal(rboolean *heap_init_flag)
{
    ARCH_FUNCTION_NAME(heap_shutdown_bimodal);

    heaparea_malloc_last_errno = ERROR0;

#define HEAP_ALLOCATION_SHUTDOWN(allocsize)                    \
{                                                              \
    if (heaparea_##allocsize.pslot_in_use_flag)                \
    {                                                          \
        portable_free(heaparea_##allocsize.pslot_in_use_flag); \
    }                                                          \
    if (heaparea_##allocsize.pslot)                            \
    {                                                          \
        portable_free(heaparea_##allocsize.pslot);             \
    }                                                          \
}

    heap_report_bimodal(heap_init_flag);

    HEAP_ALLOCATION_SHUTDOWN(8);
    HEAP_ALLOCATION_SHUTDOWN(16);
    HEAP_ALLOCATION_SHUTDOWN(24);
    HEAP_ALLOCATION_SHUTDOWN(32);
    HEAP_ALLOCATION_SHUTDOWN(48);
    HEAP_ALLOCATION_SHUTDOWN(64);
    HEAP_ALLOCATION_SHUTDOWN(96);
    HEAP_ALLOCATION_SHUTDOWN(128);
    HEAP_ALLOCATION_SHUTDOWN(192);
    HEAP_ALLOCATION_SHUTDOWN(256);
    HEAP_ALLOCATION_SHUTDOWN(384);
    HEAP_ALLOCATION_SHUTDOWN(512);
    HEAP_ALLOCATION_SHUTDOWN(768);
    HEAP_ALLOCATION_SHUTDOWN(1024);
    HEAP_ALLOCATION_SHUTDOWN(1536);
    HEAP_ALLOCATION_SHUTDOWN(2048);
    HEAP_ALLOCATION_SHUTDOWN(3072);
    HEAP_ALLOCATION_SHUTDOWN(4096);
    HEAP_ALLOCATION_SHUTDOWN(6144);
    HEAP_ALLOCATION_SHUTDOWN(8192);
    HEAP_ALLOCATION_SHUTDOWN(12288);
    HEAP_ALLOCATION_SHUTDOWN(16384);
    HEAP_ALLOCATION_SHUTDOWN(24576);
    HEAP_ALLOCATION_SHUTDOWN(32768);
    HEAP_ALLOCATION_SHUTDOWN(49152);
    HEAP_ALLOCATION_SHUTDOWN(65536);
    HEAP_ALLOCATION_SHUTDOWN(98304);
    HEAP_ALLOCATION_SHUTDOWN(131072);
    HEAP_ALLOCATION_SHUTDOWN(196608);
    HEAP_ALLOCATION_SHUTDOWN(262144);
    HEAP_ALLOCATION_SHUTDOWN(393216);
    HEAP_ALLOCATION_SHUTDOWN(524288);
    HEAP_ALLOCATION_SHUTDOWN(786432);
    HEAP_ALLOCATION_SHUTDOWN(1048576);

    if (heaparea_malloc.pslot_in_use_flag)
    {
        portable_free(heaparea_malloc.pslot_in_use_flag);
    }
    if (heaparea_malloc.pslot)
    {
        portable_free(heaparea_malloc.pslot);
    }

    /* Declare this module uninitialized */
    *heap_init_flag = rfalse;

    return;

} /* END of heap_shutdown_bimodal() */


/* EOF */
