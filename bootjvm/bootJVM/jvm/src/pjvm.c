/*!
 * @file pjvm.c
 *
 * @brief Main JVM control structure definition.
 *
 * Public symbol defining the main JVM control structure.
 * This structure is defined in @link jvm/src/jvm.h jvm.h@endlink .
 *
 * @section Control
 *
 * \$URL$
 *
 * \$Id$
 *
 * Copyright 2006 The Apache Software Foundation
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
ARCH_SOURCE_COPYRIGHT_APACHE(pjvm, c,
"$URL$",
"$Id$");

#include "jvmcfg.h"
#include "classfile.h"
#include "jvm.h"
#include "pjvm.h"


/*!
 * @brief JVM main operational structure, part of which is
 * used also by the utilities such as JAR.
 *
 * This pointer is initialized by jvm_model_init() and is
 * used @e extensively throughout the code, both directly
 * and through macros like @link #CLASS() CLASS@endlink and
 * @link #OBJECT() OBJECT()@endlink.
 *
 */

rjvm *pjvm = CHEAT_AND_USE_NULL_TO_INITIALIZE;


/* EOF */
