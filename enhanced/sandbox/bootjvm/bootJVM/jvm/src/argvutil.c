/*!
 * @file argvutil.c
 *
 * @brief Utilities in support of interpreting top-level argument
 * vector.
 *
 * The argument vector is passed into all 'C' @b main() programs from
 * its runtime library as @b argc, @b argv, and @b envp.  These values
 * are passed directly into the JVM and its utilities unmodified, where
 * they are parsed.  These utilities are the few common elements that
 * are shared between the JVM and the utility programs.
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
 * @section Reference
 *
 */

#include "arch.h"
ARCH_SOURCE_COPYRIGHT_APACHE(argvutil, c,
"$URL$",
"$Id$");


#include <string.h>

#include "jvmcfg.h" 
#include "classfile.h" 
#include "classpath.h" 
#include "exit.h" 
#include "heap.h" 
#include "jar.h" 
#include "jvm.h" 
#include "util.h" 


/*!
 * @brief Header message for non-standard messages to follow.
 *
 * This message indicates that the following messages are not a
 * part of the standard Java developer's kit syntax.  It should
 * be placed in front of all invocations of the following messages
 * in argument vector parsing routines where display of syntax
 * is performed, namely in displaying "help" messages.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

void argvutil_common_msghdr(void)
{
    fprintfLocalStdout("\nUtility options (non-standard):\n");

    return;

} /* END of argvutil_common_msghdr() */


/*!
 * @brief Non-standard messages common to JVM and all of its utilities.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

void argvutil_common_msgs(void)
{
    fprintfLocalStdout("    %s     %s",
                       JVMCFG_COMMAND_LINE_VERSION_PARM,
                       "Display the program release level.\n");

    fprintfLocalStdout("    %s   %s",
                       JVMCFG_COMMAND_LINE_COPYRIGHT_PARM,
                       "Display the program copyright.\n");

    fprintfLocalStdout("    %s     %s",
                       JVMCFG_COMMAND_LINE_LICENSE_PARM,
                       "Display the program software license.\n");

    fprintfLocalStdout("    %s        %s",
                       JVMCFG_COMMAND_LINE_HELP_PARM,
                       "Display this help message.\n\n");

    return;

} /* END of argvutil_common_msgs() */


/*!
 * @name Common message that varies slightly between
 * JVM and all of its utilities.
 * 
 * There may be a slightly different version of some messages
 * between various programs.  In order to keep them grouped
 * together at display time, they are defined together in one
 * function per group of messages.
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

/*@{ */ /* Begin grouped definitions */

void argvutil_jvm_showmsg(void)
{
    fprintfLocalStdout("    %s        %s",
                       JVMCFG_COMMAND_LINE_SHOW_PARM,
     "Show how command line and environment resolves after parsing.\n");

    return;

} /* END of argvutil_jvm_showmsg() */


void argvutil_jar_showmsg(void)
{

    fprintfLocalStdout("    %s        %s",
                       JVMCFG_COMMAND_LINE_SHOW_PARM,
      "Show how command line resolves after parsing.  May appear\n");
    fprintfLocalStdout("                  %s",
      "  on a command line anywhere before 'member-files' except\n");
    fprintfLocalStdout("                  %s",
      "  between an option and its argument.\n");

    return;

} /* END of argvutil_jar_showmsg() */


/*@} */ /* End of grouped definitions */


/* EOF */
