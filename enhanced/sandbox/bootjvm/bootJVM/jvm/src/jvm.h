#ifndef _jvm_h_included_
#define _jvm_h_included_

/*!
 * @file jvm.h
 *
 * @brief Definition of the Java Virtual Machine structures running on
 * this real machine implementation.
 *
 * Everything about the state of the machine is stored here unless it
 * is stored out in the Java code, which is in the heap.
 *
 * The JVM specification is available from Sun Microsystems' web site
 * at http://java.sun.com/docs/books/vmspec/index.html and
 * may be read online at
http://java.sun.com/docs/books/vmspec/2nd-edition/html/VMSpecTOC.doc.html
 *
 * The Java 5 class file format is available as a PDF file separately at
http://java.sun.com/docs/books/vmspec/2nd-edition/ClassFileFormat-final-draft.pdf
 * and was the basis for the ClassFile structure of this implementation.
 *
 *
 * @todo HARMONY-6-jvm-jvm.h-1 Need to verify which web document for the
 *       Java 5 class file definition is either "official",
 *       actually correct, or is the <em>de facto</em> standard.
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

ARCH_HEADER_COPYRIGHT_APACHE(jvm, h,
"$URL$",
"$Id$");


#include <pthread.h> /* For mutex(3THR) functions */

#include "jvalue.h"
#include "class.h"
#include "object.h"
#include "thread.h"


/*!
 * @brief Define a JVM model
 *
 * This model has all the usual components, a program counter,
 * a stack pointer and stack area, and thread defintions.
 *
 *
 * @internal The JVM spec compliance of much code depends
 *           code depends on packed structures, especially
 *           in the class file arena, yet the use of global
 *           project-wide -fpack-struct (GCC version of
 *           structure packing option) conflicts with issues
 *           such as portability.  Since the largest structures
 *           of the code are not inherently portably, they
 *           are packed here with a pragma and the compiler
 *           is free to not pack anything else.
 *
 */
#pragma pack(1)

typedef struct
{
    /*
     * External structures from command line into main(argc, argv, envp)
     */
#if JVMCFG_TMPAREA_IN_USE
    rchar  *tmparea;  /**< Temporary directory for general scratchpad */
#endif

    /* Command line parms */
    rint    argc;     /**< Direct copy of
                             <b><code>main(argc,,)</code></b> */
    rchar **argv;     /**< Direct copy of
                             <b><code>main(,argv,)</code></b> */
    rchar **envp;     /**< Direct copy of
                             <b><code>main(,,envp)</code></b> */

    /* Slices of command line parms */
    rchar *argv0;     /**< Program name, @p @b argv[0] in 'C',
                           $0 in @b sh */

    rchar *argv0name; /**< Program name argv0,but without path
                           component */

    rint   argcj;     /**< Index of argv[] passed to JVM main(). */

    rchar **argvj;    /**< Portion of argv[] passed to JVM main().
                       * Should @b never be @link #rnull rnull@endlink,
                       * but zero args will have argvj[0] as
                       * @link #rnull rnull@endlink, which always
                       * follows last parm (eg, if 3 args, then
                       * argvj[3] == @link #rnull rnull@endlink) */

    /* Debug message level (verbosity) */
    jvm_debug_level_enum debug_message_level; /**< Verbosity of
                                               debug messages in code */

    /* Environment */
    rchar *java_home; /**< @b JAVA_HOME environment variable */

    rchar *classpath; /**< @b CLASSPATH environment variable */

    rchar *bootclasspath; /**< @b BOOTCLASSPATH environment variable */



    /* Will use only @e one of these two startup modes: */
    rchar *startclass;/**< Internal name of JVM start class,
                           as @c @b Lstart/class/name; */

    rchar *startjar;  /**< Name of JAR file containing start class,
                           if any, else @link #rnull rnull@endlink. */

    volatile rboolean timeslice_expired; /**< JVM time slice
                       * processing-- use only ONE mutex for
                       * inter-thread control of @e all thread sleep
                       * timers.  @p @b timeslice_expired is not mutexed
                       * as a producer-consumer item of a single
                       * @c @b volatile byte.
                       *
                       * @todo  HARMONY-6-jvm-jvm.h-2 Verify this is
                       *        okay.  The @link
                                rthread#sleeptime
                                rthread.sleeptime@endlink item is a
                       *        multi-byte integer, and so could be
                       *        unsafe if not mutexed.  (It is still
                       *        made @c @b volatile just to raise the
                       *        awareness of users to this issue.)
                       */

    rbyte unused2[3]; /**< 4-byte alignment */

    pthread_mutex_t  sleeplock; /**< Thread-safe read/update mechanism
                       * for @link rthread#sleeptime
                         rthread.sleeptime@endlink.  See
                       * @link jvm/src/timeslice.c
                         timeslice.c@endlink for details.
                       */


    /*
     * Thread area structures, including program counter
     * and stack pointer
     */


    /*
     * Thread being examined by JVM at the current time
     * Typically accessed as CURRENT_THREAD
     */
    jvm_thread_index current_thread; /**< Thread being manipulated by
                                      *   JVM at the current time.
                                      *   Typically accessed as
                                      *   @link #CURRENT_THREAD
                                          CURRENT_THREAD@endlink
                                      */

    rulong jvm_instruction_count;    /**< Total number of virtual
                                      * instructions run by the JVM.
                                      * See also (rthread)
                                      * @p @b thread_instruction_count
                                      * and
                                      * @p @b pass_instruction_count
                                      */

    jvm_thread_index thread_new_last; /**< Last thread slot to be
                                           allocated by thread_new() */

    rthread thread[JVMCFG_MAX_THREADS]; /**< Table of java.lang.Thread
                                      * structures.  Indexed by
                                      * @link #jvm_thread_index
                                        jvm_thread_index @endlink
                                      * integers.  Typically accessed
                                      * as @link #THREAD() 
                                        THREAD(index) @endlink */


    /*
     * Class area structures
     */

    jvm_class_index class_allocate_last; /**< Last class slot to be
                                          * allocated by
                                          * class_static_new() */

    jvm_class_index class_java_lang_Object; /**< Class slot occupied
                                             * by java.lang.Object */

    jvm_class_index class_primative_boolean;/**< Class slot occupied
                                             * by (jboolean) */

    jvm_class_index class_primative_byte;   /**< Class slot occupied
                                             * by (jbyte) */

    jvm_class_index class_primative_char;   /**< Class slot occupied
                                             * by (jchar) */

    jvm_class_index class_primative_double; /**< Class slot occupied
                                             * by (jdouble) */

    jvm_class_index class_primative_float;  /**< Class slot occupied
                                             * by (jfloat) */

    jvm_class_index class_primative_int;    /**< Class slot occupied
                                             * by (jint) */

    jvm_class_index class_primative_long;   /**< Class slot occupied
                                             * by (jlong) */

    jvm_class_index class_primative_short;  /**< Class slot occupied
                                             * by (jshort) */

    jvm_class_index class_java_lang_Class;  /**< Class slot occupied
                                             * by java.lang.Class */

    jvm_class_index class_java_lang_String; /**< Class slot occupied
                                             * by java.lang.String */

    jvm_class_index class_java_lang_Thread; /**< Class slot occupied
                                             * by java.lang.Thread */


    rclass class[JVMCFG_MAX_CLASSES]; /**< Table of java.lang.Class
                                       * structures.  Indexed by
                                       * @link #jvm_class_index
                                         jvm_class_index @endlink
                                       * integers.  Typically accessed
                                       * as @link #CLASS()
                                         CLASS(index)@endlink */


    /*
     * Object area structures
     */

    jvm_object_hash object_allocate_last; /**< Last object slot to be
                                           * allocated by
                                           * object_instance_new() */

    robject object[JVMCFG_MAX_OBJECTS]; /**< Table of java.lang.Object
                                         * structures.  Indexed by
                                         * @link #jvm_object_hash
                                           jvm_object_hash @endlink
                                         * integers.  Typically accessed
                                         * as @link #OBJECT()
                                           OBJECT(index) @endlink.
                                         *
                                         * The @link #jvm_object_hash
                                           jvm_object_hash @endlink
                                         * index is @e also absolutely
                                         * identical to a Java object
                                         * reference and a Java array
                                         * dimension reference.  */

} rjvm;

#include "pjvm.h"

/*!
 * @internal Remove effects of packing pragma on other code.
 *
 */
#pragma pack()


/* Prototypes for functions in 'jvm.c' */

extern rvoid jvm_manual_thread_run(jvm_thread_index  thridx,
                                   rboolean          shutdown,
                                   rchar            *clsname,
                                   rchar            *mthname,
                                   rchar            *mthdesc);

#endif /* _jvm_h_included_ */

/* EOF */
