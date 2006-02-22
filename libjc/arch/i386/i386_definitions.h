
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
 * $Id$
 */

#ifndef _ARCH_I386_DEFINITIONS_H_
#define _ARCH_I386_DEFINITIONS_H_

#if !defined(__i386__)
#error "This include file is for the i386 architecture only"
#endif

#define	_JC_PAGE_SHIFT		12		/* 4096 byte pages */

#define _JC_STACK_ALIGN		2

#define _JC_BIG_ENDIAN		0

/* Fixes for Cygwin */
#ifdef __CYGWIN__
#undef _JC_LIBRARY_FMT
#define _JC_LIBRARY_FMT		"cyg%s.dll"
#define sched_get_priority_max(x)	(15)
#define sched_get_priority_min(x)	(1)
#endif

#endif	/* _ARCH_I386_DEFINITIONS_H_ */

