/*!
 * @file tmparea.c
 *
 * @brief Create and manage temporary directory area for class files,
 * etc.
 *
 * @deprecated Used for facilitating Java archiving activities
 * during initial development, it is now available for any general
 * purpose should it prove useful in another capacity.
 *
 * @see JVMCFG_TMPAREA_IN_USE
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
ARCH_SOURCE_COPYRIGHT_APACHE(tmparea, c,
"$URL$",
"$Id$");

 
#include "jvmcfg.h"
#include "classfile.h"
#include "exit.h"
#include "heap.h" 
#include "jvm.h"
#include "pjvm.h" 
#include "util.h"


#if JVMCFG_TMPAREA_IN_USE
/*!
 * @brief Initialize the class area of the JVM model
 *
 *
 * @param[in]  argv               Pointer to rboolean, set
 *
 * @param[in]  heap_get_data      Heap allocation function to use
 *                                for geting heap areas to use.
 *
 * @param[in]  heap_free_data     Heap delallocation function to use
 *                                for freeing up heap areas.
 *
 * @param[out] tmparea_init_flag  Pointer to rboolean, changed to
 *                                @link #rtrue rtrue@endlink
 *                                to indicate temporary area
 *                                now available.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
static rchar *env_tmpdir = CHEAT_AND_USE_NULL_TO_INITIALIZE;

rvoid tmparea_init(char     **argv,
                   rvoid     *(* heap_get_data)(rint, rboolean),
                   rvoid      (* heap_free_data)(rvoid *),
                   rboolean  *tmparea_init_flag)
{
    ARCH_FUNCTION_NAME(tmparea_init);

    rchar tmparea[100];

    rvoid *statbfr; /* Portability library does (struct stat) part */
    char *argv0name = portable_strrchr(argv[0],
                                       JVMCFG_PATHNAME_DELIMITER_CHAR);

#ifdef CONFIG_CYGWIN
    if (rnull == argv0name)
    {
        argv0name = portable_strrchr(argv[0],
                                    JVMCFG_PATHNAME_ALT_DELIMITER_CHAR);
    }
#endif

    if (rnull != argv0name)
    {
        argv0name++;
    }
    else
    {
        argv0name = argv[0];
    }

    int pid = portable_getpid();

    env_tmpdir = portable_getenv(JVMCFG_ENVIRONMENT_VARIABLE_TMPDIR);

    if (rnull == env_tmpdir)
    {
        env_tmpdir = JVMCFG_TMPAREA_DEFAULT;
    }

    /* Create temporary area name and allocate string for keeping name*/
    sprintfLocal(tmparea,
                 "%s%ctmp.%s.%d",
                 env_tmpdir,
                 JVMCFG_PATHNAME_DELIMITER_CHAR,
                 argv0name,
                 pid);
    pjvm->tmparea = heap_get_data(portable_strlen(tmparea), rfalse);
    portable_strcpy(pjvm->tmparea, tmparea);

    /* Could use <sys/stat.h> constants for directory creation mode */
    (rvoid) portable_mkdir(pjvm->tmparea, 0755);

    /* Verify existence of directory */
    statbfr = portable_stat(pjvm->tmparea);
    heap_free_data(statbfr);

    if (rnull == statbfr)
    {
        sysErrMsg(arch_function_name,
                  "Cannot create temp directory %s",
                  pjvm->tmparea);
        exit_jvm(EXIT_TMPAREA_MKDIR);
/*NOTREACHED*/
    }
    /* Declare this module initialized */
    *tmparea_init_flag = rtrue;

    return;

} /* END of tmparea_init() */


/*!
 * Retrieve the temporary directory area path.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns Null-terminated string to temp area.
 *
 */
const rchar *tmparea_get(rvoid)
{
    ARCH_FUNCTION_NAME(tmparea_get);

    return((const rchar *) pjvm->tmparea);

} /* END of tmparea_get() */


/*!
 * @brief Shut down the temporary directory area after JVM execution.
 *
 *
 * @param[in]  heap_get_data      Heap allocation function to use
 *                                for geting heap areas to use.
 *
 * @param[in]  heap_free_data     Heap delallocation function to use
 *                                for freeing up heap areas.
 *
 * @param[out] tmparea_init_flag  Pointer to rboolean, changed to
 *                                @link #rfalse rfalse@endlink
 *                                to indicate temporary area
 *                                no longer available.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid tmparea_shutdown(rvoid    *(* heap_get_data)(rint, rboolean),
                       rvoid     (* heap_free_data)(rvoid *),
                       rboolean *tmparea_init_flag)
{
    ARCH_FUNCTION_NAME(tmparea_shutdown);


/* Normal method requires directory to be empty:
 *
 *  int rc = rmdir(pjvm->tmparea);
 *
 *  if (0 != rc)
 *  {
 *      sysErrMsg(arch_function_name,
 *                "Cannot remove temp directory %s",
 *                pjvm->tmparea);
 *      exit_jvm(EXIT_TMPAREA_RMDIR);
**NOTREACHED**
 *  }
 *
 */

    rvoid *statbfr; /* Portability library does (struct stat) part */

/* Since there will be temp files here, cheat just a @e little bit: */


    rchar *rmscript =        /* format spec %s make strlen longer than
                                it needs to be, but it is benign */
        heap_get_data(portable_strlen(JVMCFG_TMPAREA_REMOVE_SCRIPT) +
                          portable_strlen(pjvm->tmparea) +
                          sizeof(rchar) /* NUL byte */,
                      rfalse);

    sprintfLocal(rmscript, JVMCFG_TMPAREA_REMOVE_SCRIPT, pjvm->tmparea);

    /* int rc = */ portable_system(rmscript);

    /* Verify missing directory */
    statbfr = portable_stat(pjvm->tmparea);
    heap_free_data(statbfr);

    if (rnull != statbfr)
    {
        sysErrMsg(arch_function_name,
                  "Cannot remove temp directory %s",
                  pjvm->tmparea);
        heap_free_data(pjvm->tmparea);
        exit_jvm(EXIT_TMPAREA_RMDIR);
/*NOTREACHED*/
    }

    /* Clean up */
    heap_free_data(pjvm->tmparea);
    pjvm->tmparea = (rchar *) rnull;

    /* Declare this module uninitialized */
    *tmparea_init_flag = rfalse;

    return;

} /* END of tmparea_shutdown() */

#endif /* JVMCFG_TMPAREA_IN_USE */

/* EOF */
