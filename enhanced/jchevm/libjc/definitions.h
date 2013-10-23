
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

#ifndef _DEFINITIONS_H_
#define _DEFINITIONS_H_

/*
 * We like this definition for NULL because it generates more
 * gcc compiler warnings than just plain "0" would.
 */
#undef NULL
#define NULL	((void *)0)

/*
 * Round up 'x' to a multiple of 'y' (must be a power of two).
 */
#define _JC_ROUNDUP2(x, y)	(((x) + ((y) - 1)) & ~((y) - 1))

/*
 * Count how many 'y' required to cover 'x'.
 */
#define _JC_HOWMANY(x, y)	(((x) + ((y) - 1)) / (y))

/*
 * Number of bits in a '_jc_word' type.
 */
#define _JC_BITS_PER_WORD	(sizeof(_jc_word) << 3)

/*
 * Value of a hex digit.
 */
#define _JC_HEXVAL(c)	(isdigit(c) ? (c) - '0' : tolower(c) - 'a' + 10)

/*
 * Name of the system thread group.
 */
#define _JC_SYSTEM_THREADGROUP_NAME	"Internal"

/*
 * Thread status values.
 *
 * Each running thread is either running in Java mode or native mode.
 * In native mode, the thread is not allowed to directly touch any VM state.
 * In either case, the thread is "halting" if another thread wishes for
 * it to halt. Once it does halt, it enters the halted state.
 */
enum {
	_JC_THRDSTAT_RUNNING_NORMAL = 1,	/* running in java mode */
	_JC_THRDSTAT_HALTING_NORMAL,		/* java, halt requested */
	_JC_THRDSTAT_RUNNING_NONJAVA,		/* running in native mode */
	_JC_THRDSTAT_HALTING_NONJAVA,		/* native, halt requested */
	_JC_THRDSTAT_HALTED			/* halted */
};

/*
 * Thread interrupt status. These values are used to implement
 * Thread.interrupt() and friends.
 */
enum {
	_JC_INTERRUPT_CLEAR = 0,	/* not interrupted, not interruptible */
	_JC_INTERRUPT_INTERRUPTIBLE,	/* sleeping away and interruptible */
	_JC_INTERRUPT_SET,		/* interrupted, but not handled yet */
	_JC_INTERRUPT_INTERRUPTED,	/* waking up to handle interruption */
};

/*
 * Verbosity flags.
 */
enum {
	_JC_VERBOSE_CLASS,
	_JC_VERBOSE_GC,
	_JC_VERBOSE_JNI,
	_JC_VERBOSE_EXCEPTIONS,
	_JC_VERBOSE_RESOLUTION,
	_JC_VERBOSE_INIT,
	_JC_VERBOSE_JNI_INVOKE,
	_JC_VERBOSE_MAX
};

/*
 * Exceptions generated by the virtual machine itself.
 * We have special handling for these to avoid recursively
 * trying to throw the same exception over and over again.
 */
enum {
	_JC_AbstractMethodError,
	_JC_ArithmeticException,
	_JC_ArrayIndexOutOfBoundsException,
	_JC_ArrayStoreException,
	_JC_ClassCastException,
	_JC_ClassCircularityError,
	_JC_ClassFormatError,
	_JC_ClassNotFoundException,
	_JC_CloneNotSupportedException,
	_JC_ExceptionInInitializerError,
	_JC_IOException,
	_JC_IllegalAccessError,
	_JC_IllegalAccessException,
	_JC_IllegalArgumentException,
	_JC_IllegalMonitorStateException,
	_JC_IllegalThreadStateException,
	_JC_IncompatibleClassChangeError,
	_JC_InstantiationError,
	_JC_InstantiationException,
	_JC_InternalError,
	_JC_InterruptedException,
	_JC_InvocationTargetException,
	_JC_LinkageError,
	_JC_NegativeArraySizeException,
	_JC_NoClassDefFoundError,
	_JC_NoSuchFieldError,
	_JC_NoSuchMethodError,
	_JC_NullPointerException,
	_JC_OutOfMemoryError,
	_JC_StackOverflowError,
	_JC_ThreadDeath,
	_JC_UnsatisfiedLinkError,
	_JC_UnsupportedClassVersionError,
	_JC_VMEXCEPTION_MAX
};

/*
 * Macros to convert between splay tree nodes and the larger
 * structure containing the _jc_splay_node node structure.
 */
#define _JC_NODE2ITEM(tree, node)					\
	((void *)((char *)(node) - (tree)->offset))
#define _JC_ITEM2NODE(tree, item)					\
	((_jc_splay_node *)((char *)(item) + (tree)->offset))

/*
 * Macros to convert between internal and JNI VM and thread pointers.
 */
#define _JC_ENV2JNI(env)						\
	((JNIEnv *)((char *)(env) + _JC_OFFSETOF(_jc_env, jni_interface)))
#define _JC_JNI2ENV(jni_ptr)						\
	((_jc_env *)((char *)(jni_ptr) - _JC_OFFSETOF(_jc_env, jni_interface)))
#define _JC_JVM2JNI(jvm)						\
	((JavaVM *)((char *)(jvm) + _JC_OFFSETOF(_jc_jvm, jni_interface)))
#define _JC_JNI2JVM(jni_ptr)						\
	((_jc_jvm *)((char *)(jni_ptr) - _JC_OFFSETOF(_jc_jvm, jni_interface)))

/*
 * Some pseudo-bytecodes
 */
#define _JC_ldc_string		0xca
#define _JC_invokestatic2	0xcb
#define _JC_getstatic_z		0xcc
#define _JC_getstatic_b		0xcd
#define _JC_getstatic_c		0xce
#define _JC_getstatic_s		0xcf
#define _JC_getstatic_i		0xd0
#define _JC_getstatic_j		0xd1
#define _JC_getstatic_f		0xd2
#define _JC_getstatic_d		0xd3
#define _JC_getstatic_l		0xd4
#define _JC_putstatic_z		0xd5
#define _JC_putstatic_b		0xd6
#define _JC_putstatic_c		0xd7
#define _JC_putstatic_s		0xd8
#define _JC_putstatic_i		0xd9
#define _JC_putstatic_j		0xda
#define _JC_putstatic_f		0xdb
#define _JC_putstatic_d		0xdc
#define _JC_putstatic_l		0xdd
#define _JC_getfield_z		0xde
#define _JC_getfield_b		0xdf
#define _JC_getfield_c		0xe0
#define _JC_getfield_s		0xe1
#define _JC_getfield_i		0xe2
#define _JC_getfield_j		0xe3
#define _JC_getfield_f		0xe4
#define _JC_getfield_d		0xe5
#define _JC_getfield_l		0xe6
#define _JC_putfield_z		0xe7
#define _JC_putfield_b		0xe8
#define _JC_putfield_c		0xe9
#define _JC_putfield_s		0xea
#define _JC_putfield_i		0xeb
#define _JC_putfield_j		0xec
#define _JC_putfield_f		0xed
#define _JC_putfield_d		0xee
#define _JC_putfield_l		0xef
#define _JC_failure		0xff

/*
 * Lockword layout
 * ---------------
 *
 * The lockword is a _jc_word (of which we only use the bottom 32 bits).
 * The top bits are used for locking, the bottom for misc type info that
 * helps us avoid dereferencing obj->type.
 *
 *   Bits	Meaning
 *   ----	-------
 *     31	0 = lock is THIN, 1 = lock is FAT
 *  16-30	FAT: fat lock ID
 *  21-30	THIN: owner thread ID (or zero if not locked)
 *  16-20	THIN: number of times locked - 1
 *  10-15	Number of reference fields in object (0x3f = overflow)
 *	9	Finalize bit: object requires finalization (heap objects only)
 *	9	Visited bit: object seen this GC cycle (stack objects only)
 *      8	Live bit: object is live (reachable from root set)
 *      7	Keep bit: don't reclaim object (e.g., finalize() reachable)
 *      6	Special bit: special GC handling required (e.g., ClassLoader)
 *	5	0 = non-array, 1 = array
 *    1-4	Array: element_type->flags & _JC_TYPE_MASK
 *		Non-array: _JC_TYPE_REFERENCE
 *      0	Always 1 to distinguish from a reference (aligned pointer)
 */
#define _JC_LW_FAT_BIT			0x80000000
#define _JC_LW_FAT_ID_MASK		0x7fff0000
#define _JC_LW_FAT_ID_SHIFT		16
#define _JC_LW_THIN_TID_MASK		0x7fe00000
#define _JC_LW_THIN_TID_SHIFT		21
#define _JC_LW_THIN_COUNT_MASK		0x001f0000
#define _JC_LW_THIN_COUNT_SHIFT		16
#define _JC_LW_INFO_MASK		0x0000ffff
#define _JC_LW_INFO_SHIFT		0
#define _JC_LW_REF_COUNT_MASK		0x0000fc00
#define _JC_LW_REF_COUNT_SHIFT		10
#define _JC_LW_FINALIZE_BIT		0x00000200
#define _JC_LW_VISITED_BIT		0x00000200
#define _JC_LW_LIVE_BIT			0x00000100
#define _JC_LW_KEEP_BIT			0x00000080
#define _JC_LW_SPECIAL_BIT		0x00000040
#define _JC_LW_ARRAY_BIT		0x00000020
#define _JC_LW_TYPE_MASK		0x0000001e
#define _JC_LW_TYPE_SHIFT		1
#define _JC_LW_ODD_BIT			0x00000001

/* Macros for testing and extracting lockword bits */
#define _JC_LW_TEST(w, bit)						\
	(((w) & _JC_LW_ ## bit ## _BIT) != 0)
#define _JC_LW_EXTRACT(w, fld)						\
	(((_jc_word)(w) & _JC_LW_ ## fld ## _MASK) >> _JC_LW_ ## fld ## _SHIFT)
#define _JC_LW_MAX(fld)							\
	(((_jc_word)_JC_LW_ ## fld ## _MASK >> _JC_LW_ ## fld ## _SHIFT) + 1)

/* Hard limits derived from the sizes of the above bit fields */
#define _JC_MAX_THREADS			_JC_LW_MAX(THIN_TID)
#define _JC_MAX_FATLOCKS		_JC_LW_MAX(FAT_ID)
#define _JC_MAX_THIN_RECURSION		_JC_LW_MAX(THIN_COUNT)

/*
 * Access to special object fields.
 */
#ifndef NDEBUG
#define _JC_VMFIELD(_vm, _obj0, _class, _field, _type)			\
    ({									\
	_jc_object *const _obj = (_obj0);				\
	_jc_field *const _f = _vm->boot.fields._class._field;		\
									\
	_JC_ASSERT(_obj->type != NULL);					\
	_JC_ASSERT(_jc_subclass_of(_obj->type, _f->class));		\
	_JC_ASSERT(!_JC_ACC_TEST(_f, STATIC));				\
	(_type *)((char *)_obj + _f->offset);				\
    })
#define _JC_VMSTATICFIELD(_vm, _class, _field, _type)			\
    ({									\
	_jc_field *const _f = _vm->boot.fields._class._field;		\
									\
	_JC_ASSERT(_JC_ACC_TEST(_f, STATIC));				\
	(_type *)((char *)_vm->boot.types._class->u.nonarray		\
	    .class_fields + _vm->boot.fields._class._field->offset);	\
    })
#else
#define _JC_VMFIELD(_vm, _obj, _class, _field, _type)			\
	((_type *)((char *)(_obj)					\
	    + _vm->boot.fields._class._field->offset))
#define _JC_VMSTATICFIELD(_vm, _class, _field, _type)			\
	((_type *)((char *)_vm->boot.types._class->u.nonarray		\
	    .class_fields + _vm->boot.fields._class._field->offset))
#endif

/*
 * Branch prediction
 */
#ifdef __GNUC__
#define _JC_LIKELY(x)	__builtin_expect((x), 1)
#define _JC_UNLIKELY(x)	__builtin_expect((x), 0)
#else
#define _JC_LIKELY(x)	(x)
#define _JC_UNLIKELY(x)	(x)
#endif

/*
 * How many references in a native reference frame.
 *
 * Do not change this, as the structure of 'struct _jc_native_frame'
 * depends on it.
 */
#define _JC_NATIVE_REFS_PER_FRAME		29
#define _JC_NATIVE_REFS_MIN_PER_FRAME		16	/* per the JNI spec */

/*
 * Maximum number of free thread structures to keep around.
 */
#define _JC_MAX_FREE_THREADS			128

/*
 * Length of thread status buffer (used for debugging).
 */
#define _JC_MAX_TEXT_STATUS			64

/*
 * Size of an uni-allocator page header.
 */
#define _JC_UNI_HDR_SIZE						\
	_JC_ROUNDUP2(sizeof(_jc_uni_pages), _JC_FULL_ALIGNMENT)

/*
 * This is the padding required at the beginning of a byte[] array
 * to ensure universal alignment.
 */
#define _JC_BYTE_ARRAY_PAD						\
	(_JC_ROUNDUP2(_JC_OFFSETOF(_jc_byte_array, elems),		\
	    _JC_FULL_ALIGNMENT) - _JC_OFFSETOF(_jc_byte_array, elems))

/*
 * Minimum number of uni-allocator pages to grab when additional
 * class loader memory is needed.
 */
#define _JC_CL_ALLOC_MIN_PAGES			32

/*
 * How many class loader implicit references to allocate at a time
 */
#define _JC_CL_ALLOC_IMPLICIT_REFS		32

/*
 * Java stack parameters
 */
#define _JC_JAVA_STACK_DEFAULT			"4090"
#define _JC_JAVA_STACK_MARGIN			100

/*
 * Short name and handle for internal java.lang native library.
 */
#define _JC_INTERNAL_NATIVE_LIBRARY		"JC virtual machine"
#define _JC_INTERNAL_LIBRARY_HANDLE		((void *)1)

/*
 * Number of pages to reserve for low memory situations where
 * an OutOfMemoryError must be thrown.
 */
#define _JC_HEAP_RESERVED_PAGES			10

/*
 * How many heap pages to initialize at a time
 */
#define _JC_HEAP_INIT_PAGES(heap)					\
	((heap)->num_pages == 0 ?					\
	    (2 * 1024 * 1024 / _JC_PAGE_SIZE) :				\
	    (heap)->max_pages / 8)

/*
 * Minimum number of references an object must contain before we
 * consider putting in a skip word at the beginning of the heap block.
 */
#define _JC_SKIPWORD_MIN_REFS			3

/*
 * Bits defined for 'flags' field in a native reference frame.
 * The lower three bits are defined here. The top 29 bits are
 * used as a free entry bitmap.
 */
#define _JC_NATIVE_REF_ODD_BIT			0x00000001
#define _JC_NATIVE_REF_STACK_ALLOC		0x00000002
#define _JC_NATIVE_REF_EXTENSION		0x00000004
#define _JC_NATIVE_REF_FREE_BITS		0xfffffff8

#define _JC_NATIVE_REF_IS_FREE(frame, i)				\
	(((frame)->flags & (1 << (i + 3))) != 0)
#define _JC_NATIVE_REF_ANY_FREE(frame)					\
	(((frame)->flags & _JC_NATIVE_REF_FREE_BITS) != 0)
#define _JC_NATIVE_REF_ANY_USED(frame)					\
	(((frame)->flags & _JC_NATIVE_REF_FREE_BITS)			\
	    != _JC_NATIVE_REF_FREE_BITS)
#define _JC_NATIVE_REF_MARK_FREE(frame, i)				\
    do {								\
	((frame)->flags |= (1 << (i + 3)));				\
	((frame)->weak &= (1 << (i + 3)));				\
    } while (0)
#define _JC_NATIVE_REF_MARK_IN_USE(frame, i, weak)			\
    do {								\
	((frame)->flags &= ~(1 << (i + 3)));				\
	if (weak)							\
	    ((frame)->weak |= (1 << (i + 3)));				\
    } while (0)
#define _JC_NATIVE_REF_IS_WEAK(frame, i)				\
	(((frame)->weak & (1 << (i + 3))) != 0)

/* Size of one page */
#define _JC_PAGE_SIZE			(1 << _JC_PAGE_SHIFT)

/* Convert page index to _jc_word pointer */
#define _JC_PAGE_ADDR(ptr, i)						\
	((_jc_word *)((char *)(ptr) + (i) * _JC_PAGE_SIZE))

/* Convert page pointer to page index */
#define _JC_PAGE_INDEX(heap, ptr)					\
	(((char *)(ptr) - (char *)(heap)->pages) / _JC_PAGE_SIZE)

/* How to compute the instanceof hash table hash bucket */
#define _JC_INSTANCEOF_BUCKET(t)					\
	((int)(((_jc_word)(t)) / 30031) & (_JC_INSTANCEOF_HASHSIZE - 1))

/*
 * Heap page info layout
 * ---------------------
 *
 * Each page of the heap has an info word at its beginning, except for
 * pages that are the second or later pages in a multi-page large object.
 *
 *   Bits	Meaning
 *   ----	-------
 *  30-31	00 = free, 01 = small, 10 = large, 11 = being allocated
 *  24-29	SMALL: block size index
 *   0-23	SMALL: next page in list (all ones for end of list)
 *   0-29	LARGE: number of pages in the range
 *   0-29	ALLOC: number of pages in the range being allocated
 */
#define _JC_HEAP_PTYPE_MASK		0xc0000000
#define _JC_HEAP_PTYPE_SHIFT		30
#define _JC_HEAP_NPAGES_MASK		0x3fffffff
#define _JC_HEAP_NPAGES_SHIFT		0
#define _JC_HEAP_BSI_MASK		0x3f000000
#define _JC_HEAP_BSI_SHIFT		24
#define _JC_HEAP_NEXT_MASK		0x00ffffff
#define _JC_HEAP_NEXT_SHIFT		0

/*
 * Heap page types, contained in the 'PTYPE' bits.
 */
#define _JC_HEAP_PAGE_FREE		0
#define _JC_HEAP_PAGE_SMALL		1
#define _JC_HEAP_PAGE_LARGE		2
#define _JC_HEAP_PAGE_ALLOC		3

/*
 * Heap block header/skip word (optional).
 */
#define _JC_HEAP_BTYPE_MASK		0x0000001f
#define _JC_HEAP_BTYPE_SHIFT		0
#define _JC_HEAP_SKIP_MASK		0xffffffe0
#define _JC_HEAP_SKIP_SHIFT		5

/*
 * Values for BTYPE bits in the first word of a heap block.
 *
 * In the case of _JC_HEAP_BLOCK_SKIP, the upper 27 bits contain the offset
 * in words from the start of the heap block to the object header.
 *
 * If the value of BTYPE is other than listed here, then the first word
 * of the block is also the first word of the object.
 */
#define _JC_HEAP_BLOCK_FREE		0x00000001	/* free block */
#define _JC_HEAP_BLOCK_ALLOC		0x00000003	/* being allocated */
#define _JC_HEAP_BLOCK_SKIP		0x0000001f	/* skipword + object */

/* Macros for extracting heap bits */
#define _JC_HEAP_EXTRACT(w, fld)					\
	(((_jc_word)(w) & _JC_HEAP_ ## fld ## _MASK)			\
	    >> _JC_HEAP_ ## fld ## _SHIFT)
#define _JC_HEAP_MAX(fld)						\
	(((_jc_word)_JC_HEAP_ ## fld ## _MASK				\
		>> _JC_HEAP_ ## fld ## _SHIFT) + 1)

/* Determine if two pointers point within the same page */
#define _JC_HEAP_SAME_PAGE(ptr1, ptr2)					\
	((((_jc_word)(ptr1) ^ (_jc_word)(ptr2))				\
	    & ~(_JC_PAGE_SIZE - 1)) == 0)

/* Determine if an address lies within heap memory */
#define _JC_IN_HEAP(heap, addr)						\
	((char *)(addr) >= (char *)(heap)->pages			\
	    && (char *)(addr) < (char *)(heap)->pages			\
	      + ((heap)->num_pages * _JC_PAGE_SIZE))

/* Offset from the start of a heap page to the start of the first block */
#define _JC_HEAP_BLOCK_OFFSET						\
    (_JC_HOWMANY(sizeof(_jc_word), _JC_FULL_ALIGNMENT) * _JC_FULL_ALIGNMENT)

/*
 * Random portability fixes.
 */
#ifndef ULLONG_MAX
#define ULLONG_MAX	(~(unsigned long long)0)
#endif
#ifndef SIZE_T_MAX
#define SIZE_T_MAX	(~(size_t)0)
#endif
#if HAVE_INTTYPES_H
#include <inttypes.h>
#ifdef PRIxFAST64
#define _JC_JLONG_FMT	PRIxFAST64
#else
#define _JC_JLONG_FMT	"llx"		/* a guess */
#endif
#endif	/* HAVE_INTTYPES_H */

#define _JC_LIBRARY_PATH	_JC_CLASSPATH_HOME "/lib/classpath"

#define _JC_BOOT_CLASS_PATH						\
    _AC_DATADIR "/jc/jc.zip"						\
    _JC_PATH_SEPARATOR _JC_CLASSPATH_HOME "/share/classpath/glibj.zip"

/*
 * Core classes that must be loaded by the boot loader.
 */
#define _JC_CORE_CLASS(name)						\
	(strncmp((name), "java/", 5) == 0				\
	    || strncmp((name), "gnu/java/", 9) == 0)

/*
 * Class path entry types.
 */
#define _JC_CPATH_UNKNOWN		0
#define _JC_CPATH_DIRECTORY		1
#define _JC_CPATH_ZIPFILE		2
#define _JC_CPATH_ERROR			3

/*
 * Array bounds check for offset + length. Returns true if bounds are OK.
 *
 * We have to be careful because of potential 32-bit overflow.
 */
#define _JC_BOUNDS_CHECK(ary, off, len)					\
    ({									\
	const jint _alen = (ary)->length;				\
	const jint _off = (off);					\
	const jint _len = (len);					\
	const jint _end = (_off + _len);				\
									\
	((_len | _off | _end) >= 0) && _end <= _alen;			\
    })

/*
 * _JC_FULL_ALIGNMENT is the minimum alignment that satisfies all alignment
 * requirements. This is also the aligment guaranteed by malloc().
 */
struct _jc_align {
	char		c;
	_jc_value	u;
};
#define _JC_FULL_ALIGNMENT						\
	((char *)&((struct _jc_align *)0)->u - (char *)0)

/* Bit mask for page alignment */
#define _JC_PAGE_MASK	(_JC_PAGE_SIZE - 1)

/*
 * Splay tree node comparision function type.
 */
typedef int	_jc_splay_cmp_t(const void *item1, const void *item2);

/*
 * Store exception information in the thread structure.
 */
#define _JC_EX_STORE(env, e, fmt, args...)				\
    do {								\
	_jc_env *const _env = (env);					\
									\
	_env->ex.num = _JC_ ## e;					\
	snprintf(_env->ex.msg, sizeof(_env->ex.msg), fmt , ## args);	\
    } while (0)

#ifndef NDEBUG
#define _JC_EX_RESET(env)						\
    do {								\
	(env)->ex.num = -1;						\
    } while (0)
#else
#define _JC_EX_RESET(env)	do { } while (0)
#endif

/*
 * Allocate some memory on the stack, and throw a StackOverflowError
 * if that fails. This does not directly check for stack overflow, which
 * is especially hard to do in native threads. We rely on "reasonable"
 * values being allocated, within the stack space used by internal code.
 *
 * If unsuccessful an exception is stored (not posted).
 */
#define _JC_STACK_ALLOC(env, size)					\
    ({									\
	_jc_env *const _env = (env);					\
	void *_mem;							\
									\
	if ((_mem = alloca(size)) == NULL) { 				\
		_env->ex.num = _JC_StackOverflowError;			\
		*_env->ex.msg = '\0';					\
	}								\
	_mem;								\
    })

/*
 * Macros for formatting arbitrary strings into stack-allocated buffers.
 *
 * If unsuccessful an exception is stored (not posted).
 */
#define _JC_FORMAT_STRING(env, fmt, args...)				\
    ({									\
	const char *const _fmt = (fmt);					\
	size_t _len;							\
	char *_buf;							\
									\
	/* Print into nonexistent buffer to compute length */		\
	_len = snprintf(NULL, 0, _fmt , ## args);			\
									\
	/* Allocate buffer, then print into it */			\
	if ((_buf = _JC_STACK_ALLOC((env), _len + 1)) != NULL)		\
		sprintf(_buf, _fmt , ## args);				\
									\
	/* Return buffer */						\
	_buf;								\
    })

#define _JC_FORMAT_STRINGV(env, fmt, args)				\
    ({									\
	const char *const _fmt = (fmt);					\
	const va_list _args = (args);					\
	size_t _len;							\
	char *_buf;							\
									\
	/* Print into nonexistent buffer to compute length */		\
	_len = vsnprintf(NULL, 0, _fmt, _args);				\
									\
	/* Allocate buffer, then print into it */			\
	if ((_buf = _JC_STACK_ALLOC((env), _len + 1)) != NULL)		\
		vsprintf(_buf, _fmt, _args);				\
									\
	/* Return buffer */						\
	_buf;								\
    })

/*
 * Macro for testing class, field, or method access flags.
 */
#define _JC_ACC_TEST(thing, flag)					\
	(((thing)->access_flags & _JC_ACC_ ## flag) != 0)
#define _JC_FLG_TEST(thing, flag)					\
	(((thing)->flags & _JC_TYPE_ ## flag) != 0)

/*
 * Assertion macro.
 */
#ifndef NDEBUG
#define _JC_ASSERT(x)							\
    do {								\
	if (!(x)) {							\
		fprintf(stderr, "jc: assertion failure: %s\n", #x);	\
		fprintf(stderr, "jc: location: file `%s' line %d\n",	\
		    __FILE__, __LINE__);				\
		abort();						\
	}								\
    } while (0)
#else
#define _JC_ASSERT(x)	do { } while (0)
#endif

/*
 * Mutex macros, in debugging and normal versions.
 */
#ifndef NDEBUG
#define _JC_MUTEX_LOCK(env, mutex)					\
    do {								\
	pthread_mutex_t *const _mutex = &(mutex);			\
	_jc_env *const _env = (env);					\
	int _r;								\
									\
	_JC_ASSERT(_env == NULL || (mutex ## _owner) != _env);		\
	_r = pthread_mutex_lock(_mutex);				\
	_JC_ASSERT(_r == 0);						\
	(mutex ## _owner) = _env;					\
    } while (0)
#define _JC_MUTEX_UNLOCK(env, mutex)					\
    do {								\
	pthread_mutex_t *const _mutex = &(mutex);			\
	_jc_env *const _env = (env);					\
	int _r;								\
									\
	_JC_ASSERT((mutex ## _owner) == _env);				\
	(mutex ## _owner) = NULL;					\
	_r = pthread_mutex_unlock(_mutex);				\
	_JC_ASSERT(_r == 0);						\
    } while (0)
#define _JC_MUTEX_ASSERT(env, mutex)					\
    do {								\
	_JC_ASSERT((mutex ## _owner) == (env));				\
    } while (0)
#define _JC_COND_WAIT(env, cond, mutex)					\
    do {								\
	pthread_cond_t *const _cond = &(cond);				\
	pthread_mutex_t *const _mutex = &(mutex);			\
	_jc_env *const _env = (env);					\
	int _r;								\
									\
	_JC_ASSERT((mutex ## _owner) == _env);				\
	(mutex ## _owner) = NULL;					\
	_r = pthread_cond_wait(_cond, _mutex);				\
	_JC_ASSERT(_r == 0);						\
	_JC_ASSERT((mutex ## _owner) == NULL);				\
	(mutex ## _owner) = _env;					\
    } while (0)
#define _JC_COND_TIMEDWAIT(env, cond, mutex, wakeup)			\
    do {								\
	pthread_cond_t *const _cond = &(cond);				\
	pthread_mutex_t *const _mutex = &(mutex);			\
	_jc_env *const _env = (env);					\
	int _r;								\
									\
	_JC_ASSERT((mutex ## _owner) == _env);				\
	(mutex ## _owner) = NULL;					\
	_r = pthread_cond_timedwait(_cond, _mutex, (wakeup));		\
	_JC_ASSERT(_r == 0 || _r == ETIMEDOUT);				\
	_JC_ASSERT((mutex ## _owner) == NULL);				\
	(mutex ## _owner) = _env;					\
    } while (0)
#define _JC_COND_SIGNAL(cond)						\
    do {								\
	pthread_cond_t *const _cond = &(cond);				\
	int _r;								\
									\
	_r = pthread_cond_signal(_cond);				\
	_JC_ASSERT(_r == 0);						\
    } while (0)
#define _JC_COND_BROADCAST(cond)					\
    do {								\
	pthread_cond_t *const _cond = &(cond);				\
	int _r;								\
									\
	_r = pthread_cond_broadcast(_cond);				\
	_JC_ASSERT(_r == 0);						\
    } while (0)

#else

#define _JC_MUTEX_LOCK(env, mutex)	pthread_mutex_lock(&(mutex))
#define _JC_MUTEX_UNLOCK(env, mutex)	pthread_mutex_unlock(&(mutex))
#define _JC_MUTEX_ASSERT(env, mutex)	do { } while (0)
#define _JC_COND_WAIT(env, cond, mutex)					\
	pthread_cond_wait(&(cond), &(mutex))
#define _JC_COND_TIMEDWAIT(env, cond, mutex, wakeup)			\
	pthread_cond_timedwait(&(cond), &(mutex), (wakeup))
#define _JC_COND_SIGNAL(cond)		pthread_cond_signal(&(cond))
#define _JC_COND_BROADCAST(cond)	pthread_cond_broadcast(&(cond))
#endif	/* NDEBUG */

/*
 * Verbosity.
 */
#define VERBOSE_INDX(indx, vm, fmt, args...)				\
    do {								\
	if (((vm)->verbose_flags & (1 << (indx))) != 0) {		\
		_jc_printf((vm), "[verbose %s: " fmt "]\n",		\
		    _jc_verbose_names[indx] , ## args);			\
	}								\
    } while (0)

#define VERBOSE(flag, vm, fmt, args...)					\
	VERBOSE_INDX(_JC_VERBOSE_ ## flag, vm, fmt , ## args)

#endif	/* _DEFINITIONS_H_ */