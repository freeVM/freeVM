#ifndef _classpath_h_included_
#define _classpath_h_included_

/*!
 * @file classpath.h
 *
 * @brief Implementation of the @b CLASSPATH environment heuristic.
 *
 * The Java Virtual Machine Specification, version 2,
 * says absolutely NOTHING about the @b CLASSPATH
 * item, neither as an environment variable, a JVM command line
 * parameter, or anything.  Instead, section 5.3.1 states, "The
 * Java virtual machine searches for a purported representation
 * of [class] C in a platform-dependent manner.  Note that there
 * is no guarantee that a purported representation found is valid
 * or is a representation of C.  Typically, a class or interface
 * will be represented using a file in a hierarchiacal file system.
 * The name of the class or interface will usually be encoded in
 * the pathname of the file."
 *
 * This is the industry standard practice, as implemented by use
 * of the @b CLASSPATH variable.  This header file defines its
 * characteristics.  The same goes for the JAVA_PATH environment
 * variable, which has no further definitions that its name and
 * string content that contains a path name.  Since these variables
 * are actually a part of standard practice, their names are actually
 * defined in @link jvm/src/jvmcfg.h jvmcfg.h@endlink instead of
 * here, but @b CLASSPATH behavior is defined here.
 *
 * Conventions for OS file systems (see also
 * @link jvm/src/jvmcfg.h jvmcfg.h@endlink for
 * JVMCFG_PATHNAME_xxx and JVMCFG_EXTENSION_xxx definitions).
 *
 * @verbatim
  
   Unix style:     CLASSPATH="/path/name1:/path/name2/filename.jar"
  
   Windows style:  CLASSPATH="c:\path\name1;d:\path\name2\\filename.jar"
  
   @endverbatim
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

ARCH_HEADER_COPYRIGHT_APACHE(classpath, h,
"$URL$",
"$Id$");

#include "jar.h"

/*!
 * @brief Delimiter betwen members of @b CLASSPATH string
 *
 */
#if defined(CONFIG_WINDOWS) || defined(CONFIG_CYGWIN)
#define CLASSPATH_ITEM_DELIMITER_CHAR       ';'

#else
#define CLASSPATH_ITEM_DELIMITER_CHAR       ':'

#endif


/*!
 * @name File extensions
 *
 * @brief Convention for class file names, but not mandatory.
 *
 */

/*@{ */

#define CLASSFILE_EXTENSION_DEFAULT "class"
#define CLASSFILE_EXTENSION_JAR     "jar"
#define CLASSFILE_EXTENSION_ZIP     "zip"

/*@} */


/*!
 * @brief Search result from examining @b CLASSPATH
 *
 *
 * The result of classpath_get_from_cp_entry_utf() is either a class
 * file or a Java archive file member class file.  One of these
 * structure members will be @link #rnull rnull@endlink and one will
 * contain a valid pointer.
 *
 * If it is a Java archive file member, that pointer contains the state
 * <i>of an open JAR file</i>, so it @e must be examined, at least
 * enough to close the file handle.  Otherwise, open file handles will
 * accumulate.
 *
 * One member or the other will be non-null.  If both members are null,
 * then the class was not found in this @b CLASSPATH entry.
 *
 * In both cases, non-null the pointer will need to be freed when
 * processing of that class is done.
 *
 */
typedef struct
{
    rchar     *classfile_name;        /**< Name of class file defining
                                           this class */

    jar_state *jarfile_member_state;  /**< State of open Java archive
                                           file containing this class */

} classpath_search;


/* Prototypes for functions in 'classpath.c' */ 
extern rvoid classpath_init(rvoid);

extern rboolean classpath_isjar(rchar *pclasspath);

extern rchar *classpath_external2internal_classname(rchar *clsname);

extern
  rchar *classpath_external2internal_classname_inplace(rchar *inoutbfr);

extern classpath_search *classpath_get_from_prchar(rchar *clsname);

extern classpath_search *classpath_get_from_cp_entry_utf(
                                            cp_info_mem_align *clsname);

extern rvoid classpath_free_search_result(classpath_search *pcpsr);

extern rvoid classpath_shutdown(rvoid);

#endif /* _classpath_h_included_ */

/* EOF */
