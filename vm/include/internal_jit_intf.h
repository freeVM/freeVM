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
 * @author Intel, Alexei Fedotov
 * @version $Revision: 1.1.2.1.4.3 $
 */  



#ifndef _INTERNAL_JIT_INTF_H_
#define _INTERNAL_JIT_INTF_H_
#include "open/types.h"


VMEXPORT char *gen_setup_j2n_frame(char *s);
VMEXPORT char *gen_pop_j2n_frame(char *s);

#endif // _INTERNAL_JIT_INTF_H_

