#ifndef _heap_h_included_
#define _heap_h_included_

/*!
 * @file heap.h
 *
 * @brief Heap management API
 *
 * The two examples of modular development here are the two heap
 * management schemes,
 * @link jvm/src/heap_simple.c heap_simple.c@endlink
 * and @link jvm/src/heap_bimodal.c heap_bimodal.c@endlink.
 * This common header file defines the prototypes for both by way
 * of the @link #CONFIG_HEAP_TYPE CONFIG_HEAP_TYPE@endlink
 * symbol definition.
 *
 * Each heap allocation algorithm will have a section just like
 * those defined here in this file so that these macros will expand
 * to point to the API as implemented by that algoritm.  Simply
 * replicate one of the definition sections and change the function
 * names from @b _simple (et al) to @b _newalgorithm .
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

ARCH_HEADER_COPYRIGHT_APACHE(heap, h,
"$URL$",
"$Id$");

/*!
 * @internal  For utilities that need to use
 * @link #CONFIG_HEAP_TYPE_SIMPLE CONFIG_HEAP_TYPE_SIMPLE@endlink,
 * the other types may be overridden and the simple heap mechanism
 * set in place.  This is typically implemented as a compiler command
 * line option.
 */
#ifdef JVMCFG_OVERRIDE_HEAP_TYPE

#undef CONFIG_HEAP_TYPE
#define CONFIG_HEAP_TYPE simple

#ifndef CONFIG_HEAP_TYPE_SIMPLE
#define CONFIG_HEAP_TYPE_SIMPLE
#endif

#endif

#ifdef CONFIG_HEAP_TYPE_SIMPLE
/*!
 * @name Simple heap model definitions
 *
 * @brief Expand the @b HEAP_xxx() macros into the simple heap model as
 * implemented by @link jvm/src/heap_simple.c heap_simple.c@endlink.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define HEAP_INIT        heap_init_simple
#define HEAP_INIT_REPORT heap_init_report_simple
#define HEAP_REPORT      heap_report_simple
#define HEAP_SHUTDOWN    heap_shutdown_simple
#define HEAP_GET_METHOD  heap_get_method_simple
#define HEAP_GET_STACK   heap_get_stack_simple
#define HEAP_GET_DATA    heap_get_data_simple
#define HEAP_FREE_METHOD heap_free_method_simple
#define HEAP_FREE_STACK  heap_free_stack_simple
#define HEAP_FREE_DATA   heap_free_data_simple
#define HEAP_GET_ERROR   heap_get_error_simple

/*@} */ /* End of grouped definitions */
#endif

#ifndef JVMCFG_OVERRIDE_HEAP_TYPE
#ifdef CONFIG_HEAP_TYPE_BIMODAL
/*!
 * @name Bimodal heap model definitions
 *
 * @brief Expand the @b HEAP_xxx() macros into the bimidal heap model as
 * implemented by
 * @link jvm/src/heap_bimodal.c heap_bimodal.c@endlink.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define HEAP_INIT        heap_init_bimodal
#define HEAP_INIT_REPORT heap_init_report_bimodal
#define HEAP_REPORT      heap_report_bimodal
#define HEAP_SHUTDOWN    heap_shutdown_bimodal
#define HEAP_GET_METHOD  heap_get_method_bimodal
#define HEAP_GET_STACK   heap_get_stack_bimodal
#define HEAP_GET_DATA    heap_get_data_bimodal
#define HEAP_FREE_METHOD heap_free_method_bimodal
#define HEAP_FREE_STACK  heap_free_stack_bimodal
#define HEAP_FREE_DATA   heap_free_data_bimodal
#define HEAP_GET_ERROR   heap_get_error_bimodal

/*@} */ /* End of grouped definitions */
#endif
#endif

/*************************/

/* Prototypes for functions in 'heap_XXX.c' */

extern rvoid HEAP_INIT(rboolean *heap_init_flag);
extern rvoid HEAP_INIT_REPORT(rboolean *heap_init_flag);
extern rvoid HEAP_REPORT(rboolean *heap_init_flag);
extern rvoid HEAP_SHUTDOWN(rboolean *heap_init_flag);

extern rvoid *HEAP_GET_METHOD(rint size, rboolean clrmem_flag);
extern rvoid *HEAP_GET_STACK(rint size, rboolean clrmem_flag);
extern rvoid *HEAP_GET_DATA(rint size, rboolean clrmem_flag);

extern rvoid HEAP_FREE_METHOD(rvoid *heap_block);
extern rvoid HEAP_FREE_STACK(rvoid *heap_block);
extern rvoid HEAP_FREE_DATA(rvoid *heap_block);

extern int  HEAP_GET_ERROR(rvoid *badptr);

/*************************/
#endif /* _heap_h_included_ */


/* EOF */
