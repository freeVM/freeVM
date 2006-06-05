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
 * @author Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.4 $
 */  
#ifndef _PORT_MALLOC_H_
#define _PORT_MALLOC_H_

#include <malloc.h>

// FIXME
// Very basic memory allocation utilities

#define STD_FREE(p) free(p)
#define STD_MALLOC(s) malloc(s)
#define STD_CALLOC(n, s) calloc(n, s)
#define STD_REALLOC(p, s) realloc(p, s)
#define STD_ALLOCA(s) alloca(s)

#endif // _PORT_MALLOC_H_
