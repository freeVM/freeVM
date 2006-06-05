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
 * @author Aleksey Ignatenko 
 * @version $Revision: 1.1.2.1.4.3 $
 */  

// excluding remarks: 
#if defined (__INTEL_COMPILER)
#pragma warning (disable:1684)    // conversion from pointer to same-sized integral type (potential portability problem)
#pragma warning (disable:279)    // controlling expression is constant
#pragma warning (disable:444)   // destructor for base class is not virtual
#pragma warning (disable:981) // operands are evaluated in unspecified order
#pragma warning (disable:181) // argument is incompatible with corresponding format string conversion (in printf)
#pragma warning (disable:1599) // declaration hides variable "dclazz" (declared at line NN)
#endif // _MSC_VER

#if defined(_MSC_VER) && !defined (__INTEL_COMPILER) /* Microsoft C Compiler ONLY */
#pragma warning (disable:4800)    // forcing value to bool 'true' or 'false' (performance warning)
#endif

//Warning appears when local variable is defined and not used
#if defined (__INTEL_COMPILER) && defined(__GNUC__)
#pragma warning (disable:177)
#define UNUSED
#elif defined(__GNUC__)
#define UNUSED __attribute__((unused))
#else // !__GNUC__
#if defined (__INTEL_COMPILER)
#pragma warning (disable:177)
#elif defined (_MSC_VER)
#pragma warning (disable:4189)
#endif // _MSC_VER
#define UNUSED
#endif // !__GNUC__

//Warning appears when function parameter is not used in function body
#if defined (__INTEL_COMPILER) && defined(__GNUC__)
#pragma warning (disable:869)
#define UNREF
#elif defined(__GNUC__)
#define UNREF __attribute__((unused))
#else // !__GNUC__
#if defined (__INTEL_COMPILER)
#pragma warning (disable:869)
#elif defined (_MSC_VER)
#pragma warning (disable:4100)
#endif // _MSC_VER
#define UNREF
#endif // !__GNUC__
