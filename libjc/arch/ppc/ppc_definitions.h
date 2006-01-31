
/*
 * Copyright 2005 The Apache Software Foundation or its licensors,
 * as applicable.
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
 *
 */

#ifndef _ARCH_PPC_DEFINITIONS_H_
#define _ARCH_PPC_DEFINITIONS_H_

#if !defined(__ppc__)
#error "This include file is for the ppc architecture only"
#endif

#define	_JC_PAGE_SHIFT		12		/* 4096 byte pages */

#define _JC_STACK_ALIGN		2

#define _JC_BIG_ENDIAN		1

#endif	/* _ARCH_PPC_DEFINITIONS_H_ */

