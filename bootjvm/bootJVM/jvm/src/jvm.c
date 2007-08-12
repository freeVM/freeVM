/*!
 * @file jvm.c
 *
 * @brief Java Virtual Machine implementation on this real machine.
 *
 * Main entry point to the JVM and top-level implementation.
 *
 * @note Notice that the "Main Page" documentation resides in this
 *       source file.  In order for the users of Unix @b man(1) format
 *       of these documents to see the "Main Page" item that is present
 *       on the HTML format of these documents, please refer to the
 *       source code of this file.
 *
 * @todo HARMONY-6-jvm-jvm.c-1 Need to verify which web document for the
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

/*!
 * @mainpage
 *
 * This implementation of the Java Virtual Machine corresponds roughly
 * to these various source files for the overall structure.  Details
 * not found in them are located throughout the body of the source code.
 * The following table shows this overall code structure.  It is based
 * upon the table of contents to the above JVM specification document.
 * Where a @b .h C header file name is listed, the corresponding
 * @b .c C source file is of less interest to this structure than
 * the header that publishes it to the major subsystems, which are
 * listed by their @b .c C source file names.  Across the top of each
 * page of documentation in most formats is is a line containing links
 * to key indices across the documentation.  Other formats may not have
 * the links, but will have the same pages available.  These categories
 * are:
 *
 * <ul>
 * <li><b>Main Page:</b>         This starting point for all
 *                               documentation.
 *
 * </li>
 * <li><b>Namespace List:</b>    Not really used since this is not a C++
 *                               code base.  However, it does contain
 *                               Java package name levels for test
 *                               classes and core JDK classes.
 *
 * </li>
 * <li><b>Alphabetical List:</b> Index of data structures, with name and
 *                               definition reference only.
 *
 * </li>
 * <li><b>Data Structures:</b>   Index of data structures, with name,
 *                               description, and definition reference.
 *
 * </li>
 * <li><b>Directories:</b>       Index of file system directories which
 *                               contain source files for this project.
 *
 * </li>
 * <li><b>File List:</b>         Index of source files, by directory,
 *                               containing names, descriptions, and
 *                               references to source code.
 *
 * </li>
 * <li><b>Data Fields:</b>       Index of member fields and methods in
 *                               Java classes, 'C' structs, and 'C'
 *                               unions, by member name.
 *
 * </li>
 * <li><b>Globals:</b>           Global name space functions, variables,
 *                               type definitions, enumerations, and
 *                               pre-processor definitions, by category
 *                               and by name.  (The term "global" does
 *                               @e not apply to symbol scope.  Both
 *                               global scope and file scope symbols are
 *                               listed here.  The global name space is
 *                               an OO concept typically used by Doxygen
 *                               to document C++ symbols.  It is
 *                               meaningful here only in terms of Java
 *                               classes.)  The categories are:
 *
 *     <ul>
 *     <li><b>Functions:</b>         Function definitions, by name.
 *     </li>
 *     <li><b>Variables:</b>         Variable definitions, by name.
 *     </li>
 *     <li><b>Typedefs:</b>          Type definitions, by name.
 *                                   (@b N.B.  <em>It is here that the
 *                                   convention of using letter @c @b j
 *                                   and letter @c @b r for Java and
 *                                   real machine domain data types is
 *                                   most obvious.</em>)
 *     </li>
 *     <li><b>Enumerations:</b>      Enumerations, by name.
 *     </li>
 *     <li><b>Enumerators:</b>       Enumeration components, by name.
 *     </li>
 *     <li><b>Defines:</b>           C pre-processor definitions, by
 *                                   name.
 *
 *     </li>
 *     </ul>
 * </li>
 * <li><b>Data Fields:</b>       Member fields and methods in Java
 *                               classes, 'C' structs, and 'C' unions,
 *                               by member name.
 *
 * </li>
 * </ul>
 * 
 * The following table shows the JVM specification section number
 * in that document and a general location at which to start when
 * looking for its functionality.  Any sections not listed below
 * are likely to @e not have yet been implemented here or are
 * more applicable to the class library and/or compiler.
 *
 * <ul>
 * <li><b>1.2 The Java Virtual Machine:</b>  The main entry point
 *                         to to this JVM implementation is found in
 *                         @link #jvm() jvm.c@endlink.  It sets up an
 *                         @link #rjvm rjvm@endlink structure using a
 *                         pervasive global variable named
 *                         @c @b pjvm to refer to a JVM
 *                         structure.  It stands for, "pointer to JVM."
 *                         Variable names beginning with @b p stand for
 *                         &quot;<b>p</b>ointer to....&quot;  Data
 *                         structure names beginning with @b r stand for
 *                         &quot;<b>r</b>eal machine data type,&quot;
 *                         while data structure names beginning with
 *                         @b j stand for "<b>J</b>ava virtual machine
 *                         data type."
 * </li>
 * <li><b>2.1 Unicode:</b> Utilities found in
 *                         @link jvm/src/unicode.c unicode.c@endlink
 * </li>
 * <li><b>2.3 Literals:</b> Definitions found in
 *                         @link jvm/src/jrtypes.c jrtypes.c@endlink
 * </li>
 * <li><b>2.4.1 Primative Types and Values:</b>  Initialized in
 *                         @link #class_load_primative class.c@endlink
 * </li>
 * <li><b>2.4.7 The class 'Object':</b>  Implemented in
 *                       @link jvm/src/jlObject.c jlObject.c@endlink
 * </li>
 * <li><b>2.4.8 The class 'String':</b>  Implemented in
 *                       @link jvm/src/jlString.c jlString.c@endlink
 * </li>
 * <li><b>2.5 Variables:</b>  Implemented by
 *                         @link #jvalue jvalue@endlink members of
 *                         @link #rclass class.h@endlink and
 *                         @link #rclass class.h@endlink.
 * </li>
 * <li><b>2.6 Conversions and Promotions:</b>  All actions on variables
 *                         are performed in the JVM inner loop in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.6.6 Value Set Conversion:</b>  NOT IMPLEMENTED.  It is
 *                         assumed that the platform's native floating
 *                         point hardware implements the IEEE floating
 *                         point used by the JVM and with the same
 *                         constraints, which is not likely to be a
 *                         completely valid presupposition.  FP-strict
 *                         expressions from section 2.18 are likewise
 *                         NOT IMPLEMENTED.
 * </li>
 * <li><b>2.6.7 Assignment Conversion:</b> All actions on variables are
 *                         performed in the JVM inner loop in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.8 Classes:</b> Implemented in
 *                         @link jvm/src/class.c class.c@endlink
 * </li>
 * <li><b>2.9 Fields:</b>  Implemented in
 *                         @link jvm/src/class.c class.c@endlink and
 *                         @link jvm/src/field.c field.c@endlink
 * </li>
 * <li><b>2.10 Methods:</b>  Implemented in
 *                         @link jvm/src/class.c class.c@endlink and
 *                         @link jvm/src/method.c method.c@endlink
 * </li>
 * <li><b>2.11 Static Initializers:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.12 Constructors:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.13 Interfaces:</b>  Implemented in
 *                         @link jvm/src/class.c class.c@endlink and
 *                         @link jvm/src/method.c method.c@endlink
 * </li>
 * <li><b>2.15 Arrays:</b> Implemented in
 *                         @link jvm/src/class.c class.c@endlink and
 *                         @link jvm/src/object.c object.c@endlink
 * </li>
 * <li><b>2.16 Exceptions:</b>  Implemented in
 *                       @link jvm/src/exit.c exit.c@endlink and
 *                       @link jvm/src/thread.c thread.c@endlink and
 *                       @link jvm/src/opcode.c opcode.c@endlink and
 *                       @link jvm/src/jvmclass.h jvmclass.h@endlink
 *                         and all source code referencing the classes
 *                         defined in this header.  Most code that calls
 *                         @c @b setjmp(3) or @c @b longjmp(3) is
 *                         processing an exception of some type, with
 *                         the single exclusion of the @c @b while()
 *                         loop of the inner JVM execution in
 *                         @link #opcode_run() opcode.c@endlink .
 * </li>
 * <li><b>2.17 Execution:</b>  Implemented in
 *                         @link jvm/src/jvm.c jvm.c@endlink and
 *                         @link jvm/src/opcode.c opcode.c@endlink .
 * </li>
 * <li><b>2.17.1 Virtual Machine Start-up:</b>  Implemented in
 *                         @link jvm/src/jvm.c jvm.c@endlink .
 * </li>
 * <li><b>2.17.2 Loading:</b>  Implemented in
 *                 @link jvm/src/classfile.c classfile.c@endlink and
 *                 @link jvm/src/cfattrib.c cfattrib.c@endlink and
 *                 @link #class_static_new() class.c@endlink.  The
 *                 occurrence of a newly loaded Java class is called
 *                 a "class static instance" in this implementation.
 *                 This is over and against the term for an instance
 *                 of an object of this class type, which is known as
 *                 an "object instance".
 * </li>
 * <li><b>2.17.3 Linking:</b>  Implemented in
 *                         @link jvm/src/linkage.c linkage.c@endlink
 * </li>
 * <li><b>2.17.4,5 Initialization:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.17.6 Creation of New Class Instances:</b>  Implemented in
 *                       @link #object_instance_new() object.c@endlink .
 *                         The occurrence of a newly created Java object
 *                         is called an "object instance" in this
 *                         implementation.  This is over and against
 *                         the term for the loading and definition of
 *                         a Java class itself, which is known as a
 *                         "class static instance", of which type this
 *                         object is one specific
 *                         @c @b instanceof among many.
 * </li>
 * <li><b>2.17.7 Finalization of Class Instances:</b>  Implemented in
 *                         @link jvm/src/unicode.c unicode.c@endlink
 * </li>
 * <li><b>2.17.8 Unloading of Classes and Interfaces:</b>  Implemented
 *                         in @link #class_static_delete class.c@endlink
 * </li>
 * <li><b>2.17.9 Virtural Machine Exit:</b>  Implemented in
 *                         @link #jvm_shutdown() jvm.c@endlink as a
 *                         consequence of an exception in
 *                         @link #exit_jvm() exit.c@endlink or of
 *                         completion of execution in
 *                         @link #jvm() jvm.c@endlink as a
 * </li>
 * <li><b>2.18 FP-strict Expressions:</b>  NOT IMPLEMENTED.  Value set
 *                         conversions expressions from section 2.6.6
 *                         are likewise NOT IMPLEMENTED.
 * </li>
 * <li><b>2.19 Threads:</b> Implemented in
 *                         @link jvm/src/threadstate.c
                           threadstate.c@endlink, as driven by
 *                         @link #jvm_run() jvm.c@endlink and
 *                         the infrastructure of
 *                         @link jvm/src/thread.c thread.c@endlink
 * </li>
 * <li><b>3.2 Data types:</b>  Implemented in
 *                         @link jvm/src/class.c class.c@endlink
 * </li>
 * <li><b>3.3 Primative Types and Values:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>3.4 Reference Types and Values:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink as
 *                         supported by the infrastructure of
 *                         @link jvm/src/class.c class.c@endlink
 * </li>
 * <li><b>3.5.1 The PC Register:</b>  Implemented in
 *                         @link #jvm_pc jvmreg.h@endlink
 * </li>
 * <li><b>3.5.2 Java Virtual Machine stacks:</b>  Implemented in
 *                         @link #jvm_sp jvmreg.h@endlink
 * </li>
 * <li><b>3.5.3 Heap:</b>  Interface defined in
 *                         @link jvm/src/heap.h heap.h@endlink and
 *                         implemented twice in
 *                         @link jvm/src/heap_simple.c
                           heap_simple.c@endlink and in
 *                         @link jvm/src/heap_bimodal.c
                           heap_bimodal.c@endlink .  The choice of
 *                         implementation is done at configuration time
 *                         using @link config.sh config.sh@endlink .
 *                         A generic "roll your own" option is available
 *                         for those who are implementing heap modules.
 *                         Three heap regions are defined, one for
 *                         class file and method storage, one for
 *                         JVM stack areas, and one for general-purpose
 *                         data areas.
 * </li>
 * <li><b>3.5.4 Method Area:</b>  One of the heap storage areas defined
 *                         in @link jvm/src/heap.h heap.h@endlink,
 *                         where @e all class file related structures
 *                         are stored.
 * </li>
 * <li><b>3.5.5 Runtime Constant Pool:</b>  Implemented as a part of
 *                         the method area storage (section 3.5.4)
 *                         in the heap (section 3.5.3).
 * </li>
 * <li><b>3.5.6 Native Method Stacks:</b>  Since all JVM activities
 *                         except the millisecond time slice (see
 *                         @link jvm/src/timeslice.c
                           timeslice.c@endlink) operate on the same
 *                         POSIX thread, native methods use the same
 *                         real machine stack as the main JVM code's
 *                         POSIX thread.  See also native method
 *                         invocation in @link #native_run_method()
                           native.c@endlink
 * </li>
 * <li><b>3.6 Frames:</b>  Implemented in
 *                         @link jvm/src/jvmreg.h jvmreg.h@endlink
 * </li>
 * <li><b>3.6.1 Local Variables:</b>  Implemented inside the JVM stack
 *                         frame (per section 3.6), found in
 *                         @link jvm/src/jvmreg.h jvmreg.h@endlink
 * </li>
 * <li><b>3.6.2 Operand Stacks:</b>  Implemented on the top of the JVM
 *                         stack frame (per section 3.6), found in
 *                         @link jvm/src/jvmreg.h jvmreg.h@endlink
 * </li>
 * <li><b>3.6.3 Dynamic Linking:</b>  Implemented in
 *                         @link #opcode_run() opcode.c@endlink and
 *                         @link #linkage_resolve_class()
                           linkage.c@endlink
 * </li>
 * <li><b>3.6.4 Normal Method Invocation Completion:</b>  Implemented in
 *                         @link #opcode_run opcode.c@endlink
 * </li>
 * <li><b>3.6.5 Abrupt Method Invocation Completion:</b>  Implemented in
 *                         @link #opcode_run opcode.c@endlink
 * </li>
 * <li><b>3.7 Representation of Objects:</b>  Implemented in
 *                       @link jvm/src/object.c object.c@endlink and
 *                       @link jvm/src/object.h object.h@endlink and
 *                       @link jvm/src/jlObject.c jlObject.c@endlink
 * </li>
 * <li><b>3.8 Floating-Point Arithmetic:</b>  See comments above on
 *                         section 2.6.6 about how floating point
 *                         arithmetic is implemented.
 * </li>
 * <li><b>3.9 Specially Named Initialization Methods:</b>  Implemented
 *                         in @link #opcode_run() opcode.c@endlink
 * </li>
 * <li><b>2.16 Exceptions:</b>  See comments above on section 2.16.
 * </li>
 * <li><b>3.11 Instruction Set Summary:</b>  The JVM outer loop is
 *                         implemented in
 *                         @link #jvm_run() jvm.c@endlink , while the
 *                         inner look is implemented in
 *                         @link #opcode_run() opcode.c@endlink .
 *                         All of the <b>section 3.11.x</b> headings
 *                         fall under the category of the inner loop.
 * </li>
 * <li><b>3.12 Class Libraries:</b>  Only minimal support is provided
 *                         for class libraries, and that only enough
 *                         to start the machine.  See especially support
 *                         for the environment variable @b BOOTCLASSPATH
 *                         and its command line override equivalent as
 *                         implemented in
 *                         @link #classpath_get_from_prchar()
                           classpath.c@endlink
 * </li>
 * <li><b>4 The ClassFile Structure:</b>  Implemented in
 *                 @link jvm/src/classfile.c classfile.c@endlink and
 *                 @link jvm/src/cfattrib.c cfattrib.c@endlink and
 *                 extensively referenced throughout the code in
 *                 @link jvm/src/classfile.h classfile.h@endlink .
 *                 A pervasive local variable named
 *                 @c @b pcfs (or prefixed this way)
 *                 is found <em>all over</em> the code and refers to
 *                 a ClassFile data structure.  It stands for, "pointer
 *                 to ClassFile structure."
 * </li>
 * <li><b>5 Linking, Loading, and Initializing:</b>  Implemented in
 *                         structures of
 *                     @link jvm/src/classfile.h classfile.h@endlink
 *                         by the late binding logic of
 *                         @link jvm/src/linkage.c linkage.c@endlink
 *                         that is typically initiated by
 *                         @link #opcode_run opcode.c@endlink .
 *                         Classes are dynamically loaded by
 *                         @link #class_static_new() class.c@endlink
 *                         objects are dynamically created by
 *                         @link #object_instance_new() object.c@endlink
 * </li>
* <li><b>5.3.1 Loading Using the Bootstrap Class Loader:</b> Implemented
 *                in @link #class_load_from_cp_entry_utf class.c@endlink
 * </li>
 * <li><b>5.3.3 Creating Array Classes:</b>  are part and parcel of all
 *                         other class creations, with support built in
 *                         to its implementation in
 *                         @link #class_static_new() class.c@endlink
 * </li>
 * <li><b>5.6 Binding Native Method Implementations:</b>  Implemented in
 *                  @link #native_locate_local_method() native.c@endlink
 * </li>
 * <li><b>6 The Java Virtual Machine Instruction Set:</b>  Implemented
 *                         in @link #opcode_run() opcode.c@endlink with
 *                         opcode definitions (per section 9) in
 *                         @link jvm/src/opcode.h opcode.h@endlink
 * </li>
 * <li><b>8 Threads and Locks:</b>  See comments above on section 2.19.
 *                         See also the native implementation of a
 *                         part of @c @b java.lang.Thread in
 *                       @link jvm/src/jlThread.c jlThread.c@endlink
 * </li>
 * <li><b>9 Opcode Mnemonics By Opcode:</b>  Implemented in
 *                         @link jvm/src/opcode.h opcode.h@endlink
 * </li>
 * </ul>
 *
 * The garbage collection system is designed to be configurable like
 * the heap is (see comments above on section 3.5.3).  Its interface
 * is defined in @link jvm/src/gc.h gc.h@endlink and a sample stub
 * implementation is found in @link jvm/src/gc_stub.c
   gc_stub.c@endlink . The choice of implementation is done at
 * configuration time using @link config.sh config.sh@endlink .
 * A generic "roll your own" option is available for those who are
 * implementing garbage collection modules.
 *
 * (The following description of source code directories is also found
 * in @link ./README README@endlink for display as a simple text file.)
 *
 * Several directories are provided within the source tree.  Each one
 * provides one component of the project.  These are referenced in
 * @link ./config.sh ./config.sh@endlink and @link ./Makefile
   ./Makefile@endlink by these same names, and are also used in the
 * built-in bug tracking numbers described below.  These component
 * and directory names are:
 *
 * <ul>
 * <li><b>jvm:</b></li>       Source code for JVM, including a main()
 *                            wrapper.  Builds binary file 
 *                            @c @b jvm/bin/bootjvm .
 *
 * </li>
 * <li><b>libjvm:</b></li>    For building @c @b jvm as a statically
 *                            linked library archive, less main()
 *                            wrapper. Builds library archive
 *                            @c @b libjvm/lib/libjvm.a .  Source
 *                            code comes from the @c @b jvm
 *                            directory.
 *
 * </li>
 * <li><b>main:</b></li>      A simple main() wrapper that links
 *                            @c @b libjvm/lib/libjvm.a and builds the
 *                            JVM binary file @c @b main/bin/bootjvm .
 *                            Source code comes from the @c @b jvm
 *                            directory.
 *
 * </li>
 * <li><b>jar:</b></li>       A simple main() wrapper that links
 *                            @c @b libjvm/lib/libjvm.a and builds the
 *                            JAR binary file @c @b jar/bin/jar .
 *
 * </li>
 * <li><b>jni:</b></li>       Source code for a sample JNI shared
 *                            library
 *        <code><b>jni/src/harmony/generic/0.0/lib/bootjvm.so</b></code>
 *                            for linking with JNI code (currently only
 *                            a staticly linked binary file
 *                           <code><b>bin/bootjvm</b></code>), but needs
 *                            the build directives to be functional, as
 *                            it currently links statically with a
 *                            main() into a binary just like @c @b jvm .
 *                            This directory contains a tree for JNI
 *                            implementations from any supplier that
 *                            wants to support the Harmony project.
 *                            Currently, there is one JNI implementation
 *                            here, found in
 *                     <code><b>jni/src/harmony/generic/0.0</b></code> .
 *                            The source Java classes are compiled
 *                            into the directory
 *           <code><b>jni/src/harmony/generic/0.0/lib/classes</b></code>
 *                            and corresponding Java archive in
 *       <code><b>jni/src/harmony/generic/0.0/bin/bootjvm.jar</b></code>
 *
 * </li>
 * <li><b>test:</b></li>      Builds numerous Java test classes compiled
 *                            into @c @b test/lib/classes and archived
 *                            into the corresponding Java archive
 *                            <code><b>test/bin/boottest.jar</b></code>,
 *                            used for driving development work.
 *
 * </li>
 * <li><b>org:</b></li>       Not built directly, but referenced by Java
 *                            code for package @c @b org.apache.harmony
 *                            referencing the class @link
                              org/apache/harmony/Copyright.java
                              Copyright@endlink in support of the
 *                            @link getsvndata.sh getsvndata.sh@endlink
 *                            administrative script, which makes SVN
 *                            revision information available at
 *                            run-time for tracking compilation
 *                            integrity and run-time feature sets.
 *                            This is equivalent to the functionality
 *                            of the 'C' header file
 *                            @link jvm/include/arch.h
                              jvm/include/arch.h@endlink as found
 *                            in the macro @link
                              #ARCH_SOURCE_COPYRIGHT_APACHE()
                              ARCH_SOURCE_COPYRIGHT_APACHE()@endlink
 *                            found in every 'C' source and header file.
 *
 * </li>
 * <li><b>support:</b></li>   Not built at all.  Contains shell script
 *                            include files, Makefile include files,
 *                            and similar utilities.
 *
 * </li>
 * </ul>
 *
 * With the exception of the Java test classes in @c @b test/src
 * all source code is found in @c @b jvm/src and in the directory
 * tree @c @b jni/src/vendor/product/version .  The purpose of
 * @C @b libjvm and @c @b main is for demonstrating various
 * possible organizations for the source code, namely for building
 * a static library archive and for linking it.
 *
 * There are a few references to JNI (Java Native Interface) in the
 * outline above.  The following subset of @c @b java.lang
 * classes are directly implemented in the interior of this JVM:
 *
 * <ul>
 * <li>@c @b java.lang.Object
 * </li>
 * <li>@c @b java.lang.Class
 * </li>
 * <li>@c @b java.lang.String
 * </li>
 * <li>@c @b java.lang.Thread
 * </li>
 * </ul>
 *
 * This JNI subset is implemented here in the following groups
 * of source files:
 *
 * <ul>
 * <li>
 * Java source code for the four classes:
 *
 * <ul>
 * <li>@link jni/src/harmony/generic/0.0/src/java/lang/Object.java
       jni/src/harmony/generic/0.0/src/java/lang/Object.java@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java/lang/Class.java
       jni/src/harmony/generic/0.0/src/java/lang/Class.java@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java/lang/String.java
       jni/src/harmony/generic/0.0/src/java/lang/String.java@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java/lang/Thread.java
       jni/src/harmony/generic/0.0/src/java/lang/Thread.java@endlink
 * </li>
 * </ul>
 * </li>
 *
 * <li>
 * JNI headers derived from Java source code (typically via @b javah):
 *
 * <ul>
 * <li>@link jni/src/harmony/generic/0.0/include/java_lang_Object.h
      jni/src/harmony/generic/0.0/include/java_lang_Object.h@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/include/java_lang_Class.h
       jni/src/harmony/generic/0.0/include/java_lang_Class.h@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/include/java_lang_String.h
      jni/src/harmony/generic/0.0/include/java_lang_String.h@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/include/java_lang_Thread.h
      jni/src/harmony/generic/0.0/include/java_lang_Thread.h@endlink
 * </li>
 * </ul>
 * </li>
 *
 * <li>
 * C source implementation of native methods defined by JNI headers:
 *
 * <ul>
 * <li>@link jni/src/harmony/generic/0.0/src/java_lang_Object.c
       jni/src/harmony/generic/0.0/src/java_lang_Object.c@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java_lang_Class.c
       jni/src/harmony/generic/0.0/src/java_lang_Class.c@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java_lang_String.c
       jni/src/harmony/generic/0.0/src/java_lang_String.c@endlink
 * </li>
 * <li>@link jni/src/harmony/generic/0.0/src/java_lang_Thread.c
       jni/src/harmony/generic/0.0/src/java_lang_Thread.c@endlink
 * </li>
 * </ul>
 * </li>
 *
 * <li>
 * JVM core implementation of native methods "local native methods"
 * of these classes:
 *
 * <ul>
 * <li>@link jvm/src/jlObject.c jvm/src/jlObject.c@endlink
 * </li>
 * <li>@link jvm/src/jlClass.c jvm/src/jlClass.c@endlink
 * </li>
 * <li>@link jvm/src/jlString.c jvm/src/jlString.c@endlink
 * </li>
 * <li>@link jvm/src/jlThread.c jvm/src/jlThread.c@endlink
 * </li>
 * </ul>
 * </li>
 *
 * <li>
 * Headers for JVM core implementation code of "local native methods",
 * including native method ordinals used by tables in
 * @link jvm/src/native.c native.c@endlink:
 *
 * <ul>
 * <li>@link jvm/include/jlObject.h
             jvm/include/jlObject.h@endlink
 * </li>
 * <li>@link jvm/include/jlClass.h
             jvm/include/jlClass.h@endlink
 * </li>
 * <li>@link jvm/include/jlString.h
             jvm/include/jlString.h@endlink
 * </li>
 * <li>@link jvm/include/jlThread.h
             jvm/include/jlThread.h@endlink
 * </li>
 * </ul>
 * </li>
 *
 * <li>
 * JVM connection to JNI and implementation and coordination of
 * support for "local native methods":
 *
 * <ul>
 * <li>@link jvm/src/native.c jvm/src/native.c@endlink
 * </li>
 * <li>@link jvm/src/native.h jvm/src/native.h@endlink
 * </li>
 * </ul>
 * </li>
 * </ul>
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
 * @attention For those who want to get started immediately and
 *            without benefit of seeing the overall structure
 *            of the system, please at least take a look at the
 *            @link ./INSTALL INSTALL@endlink file at the top of
 *            the installation tree.  Between this file and the
 *            @link ./README README@endlink file, a quick startup
 *            is possible.
 *
 * @todo HARMONY-6-jvm-jvm.c-2 An enhanced startup narrative on the
 *       main page might be useful.
 *
 * @attention There are @e many to-do items in the
 *            @link ./README README@endlink file at the top of
 *            the installation tree.  These items has been
 *            specifically reserved for the project team to
 *            work on in order to gain experience with this
 *            code and provide a forum for JVM architectural
 *            discussions as well as to implement in their own
 *            right as a part of a working JVM.
 *
 *
 * @attention There is an informal @@bug and @@todo trackingsystem
 *            in place for the working out of pre-existing items
 *            that were known when the original code contribution
 *            was made.  @@bug items start at 1001 in each source
 *            file, while @@todo items start at 1.  The original
 *            contribution, <b>HARMONY-6</b> in the Apache JIRA
 *            system, starts each item number, followed by its
 *            component, its source file, and its item number.
 *            Thus @@todo <b>HARMONY-jvm-jvm.c-3</b> refers to
 *            something in the 'jvm' directory, namely the source
 *            file 'jvm.c', item 3.  Once these initial
 *            items are resolved, this system is likely to become
 *            obsolete in favor of the Apache JIRA system.
 *
 *
 * @todo HARMONY-6-jvm-jvm.c-3 Section 2.6.6, value set conversions
 *       of floating point numbers are not implemented.  See note
 *       in table in this section.  Likewise for section 2.18,
 *       FP-strict expressions, and section 3.8, floating point
 *       arithmetic.
 * @attention The virtual execution engine is still under development
 *            as this initial contribution is being made.  While the
 *            project team is working on learning what is inside the
 *            code, this final module will be completed.  The relevant
 *            code is found in @link #opcode_run() opcode_run()@endlink
 *            in source file @link jvm/src/opcode.c opcode.c@endlink
 *
 * @todo      HARMONY-6-jvm-jvm.c-4 The virtual execution engine is
 *            still under development as this initial contribution
 *            is being made.  While the
 *            project team is working on learning what is inside the
 *            code, this final module will be completed.  The relevant
 *            code is found in @link #opcode_run() opcode_run()@endlink
 *            in source file @link jvm/src/opcode.c opcode.c@endlink
 *
 * @bug HARMONY-6-jvm-jvm.c-1001 The virtual execution engine is still
 *      under development as this initial contribution is being made.
 *      While the project team is working on learning what is inside
 *      the code, this final module will be completed.  The relevant
 *      code is found in @link #opcode_run() opcode_run()@endlink
 *      in source file @link jvm/src/opcode.c opcode.c@endlink
 *
 * @attention For a list of contributors, please see
 *            the @link ./AUTHORS AUTHORS@endlink file.
 *
 */

#include "arch.h"
ARCH_SOURCE_COPYRIGHT_APACHE(jvm, c,
"$URL$",
"$Id$");


#include <signal.h>

#define PORTABLE_JMP_BUF_VISIBLE
#include "jvmcfg.h" 
#include "cfmacros.h" 
#include "classfile.h" 
#include "classpath.h" 
#include "exit.h" 
#include "gc.h" 
#include "jvm.h" 
#include "jvmclass.h"
#include "linkage.h" 
#include "method.h" 
#include "nts.h" 
#include "opcode.h" 
#include "utf.h" 
#include "util.h" 


/*!
 * @name Roll call globals for JVM initialization and shutdown.
 *
 * @brief Determine if a portion of the JVM has been initialized or not.
 *
 * At the very end of initializing a part of the JVM, if everything
 * was set up properly, then the associated flag is set to
 * @link #rtrue rtrue@endlink.  If an error occurs such that
 * jvm_shutdown() is initiated via EXIT_INIT(), then only those
 * components that were fully and properly initialized will be
 * cleaned up.  This helps to avoid unnecessary checks for null pointers
 * and invalid data values in uninitialized memory areas.
 *
 */

/*@{ */ /* Begin grouped definitions */

rboolean jvm_completely_initialized = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
rboolean jvm_timeslice_initialized  = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
rboolean jvm_class_initialized      = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
rboolean jvm_object_initialized     = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
rboolean jvm_thread_initialized     = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
rboolean jvm_classpath_initialized  = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
static
rboolean jvm_argv_initialized       = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
#if JVMCFG_TMPAREA_IN_USE
static
rboolean jvm_tmparea_initialized    = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
#endif
static
rboolean jvm_model_initialized      = CHEAT_AND_USE_FALSE_TO_INITIALIZE;
static
rboolean jvm_heap_initialized       = CHEAT_AND_USE_FALSE_TO_INITIALIZE;

/*@} */ /* End of grouped definitions */


/*!
 * @brief Initialize JVM internal structures.
 *
 * Wipe structure, set to all zeroes.  Certain of the @b xxx_init()
 * functions depend on a zeroed structure at initialization time.
 * Once cleared, store off command line parameters from main()
 * as passed into @link #jvm_init() jvm_init()@endlink.
 *
 * Must run HEAP_INIT() before calling this function.
 *
 * DO NOT use sysDbgMsg() here as message level cannot be set
 * until pjvm is initialized.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
static rvoid jvm_model_init()
{
    ARCH_FUNCTION_NAME(jvm_model_init);

    /*
     * Allocate AND INITIALIZE TO ZERO the main JVM storage area.
     * When allocated, the pointer gets stored in the global
     * area pjvm.  This will be used @e everywhere in the code.
     */
    pjvm = HEAP_GET_DATA(sizeof(rjvm), rtrue);

    /* Declare this module initialized */
    jvm_model_initialized = rtrue;

    return;
} /* END of jvm_model_init() */


/*!
 * @brief Shut down the JVM model after JVM execution is finished.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
static rvoid jvm_model_shutdown()
{
    ARCH_FUNCTION_NAME(jvm_model_shutdown);

    HEAP_FREE_DATA(pjvm);

    pjvm = (rjvm *) rnull;

    /* Declare this module uninitialized */
    jvm_model_initialized = rfalse;

    return;

} /* END of jvm_model_shutdown() */


/*!
 * @def MANUAL_THREAD_STARTUP()
 *
 * @brief Bring a thread up from the @b NEW condition all
 * the way to @b RUNNING.
 *
 * The JVM thread state machine normally takes @b NEW threads
 * and moves them along through the states as requested by
 * Java code, in particular methods in @c @b java.lang.Thread .
 * However, when starting up the JVM, methods like @c @b \<clinit\> and
 * @c @b \<init\> are run from within the initialization.  As such,
 * there must be a mechanism to drive the thread machine into the
 * @b RUNNING state for these special methods.  This macro implements
 * such a requirement and is used in several places in
 * @link #jvm_manual_thread_run() jvm_manual_thread_run()@endlink and
 * @link #jvm_init() jvm_init()@endlink.
 *
 * Notice that all functions run if predecessor returns
 * @link #rtrue rtrue@endlink.  Notice also that
 * threadstate_request_runnable() is a part
 * of the processing of threadstate_process_start() and
 * so is not needed here.
 *
 *
 * @param  thridx   Thread table index of thread containing code to run.
 *
 *
 * @returns  @link #rtrue rtrue@endlink when startup proceeded all
 *           the way from the @b NEW state through to @b RUNNING,
 *           @link #rfalse rfalse@endlink otherwise.
 *
 */
#define MANUAL_THREAD_STARTUP(thridx)               \
((rtrue == threadstate_request_start(thridx))     && \
 (rtrue == threadstate_activate_start(thridx))    && \
 (rtrue == threadstate_process_start(thridx))     && \
 (rtrue == threadstate_activate_runnable(thridx)) && \
 (rtrue == threadstate_process_runnable(thridx))  && \
 (rtrue == threadstate_request_running(thridx))   && \
 (rtrue == threadstate_activate_running(thridx)))



/*!
 * @def MANUAL_THREAD_SHUTDOWN()
 *
 * @brief Bring a thread down from the @B RUNNING state all the
 * way through @B DEAD to an empty thread table slot.
 *
 * See rationale above for
 * @link #MANUAL_THREAD_STARTUP MANUAL_THREAD_STARTUP@endlink.
 * Notice that all functions run if predecessor returns
 * @link #rtrue rtrue@endlink.  Notice also that
 * threadstate_request_dead() is a part of the processing of
 * threadstate_process_complete() and so is not needed here.
 *
 *
 *
 * @param  thridx   Thread table index of thread to be killed.
 *
 *
 * @returns  @link #rtrue rtrue@endlink when shutdown proceeded all
 *           the way from the @b RUNNING state through to an empty slot,
 *           @link #rfalse rfalse@endlink otherwise.
 *
 */
#define MANUAL_THREAD_SHUTDOWN(thridx)              \
((rtrue == threadstate_request_complete(thridx))  && \
 (rtrue == threadstate_activate_complete(thridx)) && \
 (rtrue == threadstate_process_complete(thridx))  && \
 (rtrue == threadstate_activate_dead(thridx))     && \
 (rtrue == thread_die(thridx)))



/*!
 * @brief Manually start a thread in the JVM execution engine, but
 * outside of the normal runtime loop.
 *
 * This is used typically by initialization of the main JVM engine.
 * All parameters except thridx are for the purpose of reporting
 * errors.  The thread must have its code loaded and ready to run,
 * and must be in the @b NEW state.  It will be moved into the
 * @b RUNNING state, allowed to run to completion, and then
 * optionally taken through the @b COMPLETE state into @b DEAD,
 * shut down, and the thread terminated.
 *
 * The thread is typically set up via thread_class_load() in
 * preparation to call this function.
 *
 *
 * @param  thridx   Thread index of thread containing an executable
 *                    Java program.
 *
 * @param  shutdown @link #rtrue rtrue@endlink if thread should be
 *                  shut down after it finishes running, otherwise
 *                  @link #rfalse rfalse@endlink.
 *
 * @param  clsname  Null-terminated string of name of class
 *
 * @param  mthname  Null-terminated string of method to run in class
 *
 * @param  mthdesc  Null-terminated string of method signature
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid jvm_manual_thread_run(jvm_thread_index  thridx,
                            rboolean          shutdown,
                            rchar            *clsname,
                            rchar            *mthname,
                            rchar            *mthdesc)
{
    ARCH_FUNCTION_NAME(jvm_manual_thread_run);

    /*
     * If thread is not @b RUNNING, then it must @b NEW.
     * Move it to @b RUNNING so that Java byte codes may
     * be run.
     */
    if (THREAD_STATE_RUNNING != THIS_STATE(thridx))
    {
        if (MANUAL_THREAD_STARTUP(thridx))
        {
            /* Used in place of threadstat_activate_running(): */
            /* (Extracted from STATE_DEFINITION() macro)       */
            if (THREAD_STATE_RUNNING == NEXT_STATE(thridx))
            {
                PREV_STATE(thridx) = THIS_STATE(thridx);
                THIS_STATE(thridx) = NEXT_STATE(thridx);
            }
        }
        else
        {
            /* Something went wrong starting method, so quit */
            sysErrMsg(arch_function_name,
                      "Cannot manually start %s %s%s",
                      clsname,
                      mthname,
                      mthdesc);
            exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
        }
    }

    /*
     * The above step should have thread @b RUNNING now.
     *
     * Therefore, this is not necessary:
     *
     *     if (THREAD_STATE_RUNNING == THIS_STATE(thridx))...
     */

    if (rfalse == opcode_run(thridx, rfalse))
    {
        /* Problem running method, so quit */
        sysErrMsg(arch_function_name,
                  "Cannot manually start %s %s%s",
                  clsname,
                  mthname,
                  mthdesc);
        exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
    }

    /*
     * Leave thread in RUNNING state or take it to thread_die(),
     * that is, through COMPLETE, through DEAD, to deallocation
     */
    if (rfalse == shutdown)
    {
        return; /* Leave it in RUNNING state */
    }

    /* Shut it down */
    if (MANUAL_THREAD_SHUTDOWN(thridx))
    {
        ; /* Everything stopped correctly */
    }
    else
    {
        /* Problem stopping method, so quit */
        sysErrMsg(arch_function_name,
                  "Cannot stop manually stop %s %s%s",
                  clsname,
                  mthname,
                  mthdesc);
        exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
    }

} /* END of jvm_manual_thread_run() */


/*!
 * @def STATE_MODEL_SWITCH()
 *
 * @brief State model switch statement macro, less the default case.
 *
 * This macro is used in @link #jvm_run() jvm_run()@endlink to
 * implement all three phases of the JVM thread state model for
 * requesting, activating, and processing a thread state.
 * The three phases, @b REQUEST, @b ACTIVATE, and @b PROCESS,
 * are instantiated by passing different parameters to the macro.
 *
 * @param  somestate_rc   Name of return code variable from @b casebody
 *
 * @param  casebody       Macro name of code to process as the body of
 *                        each case, not including @c @b break
 *                        statement.
 *
 *
 * @returns Each @b casebody returns @link #rtrue rtrue@endlink or
 *          @link #rfalse rfalse@endlink to @b somestate_rc,
 *          which is passed out of the switch() statement.
 *
 * The final case @e does include a @c @b break statement for
 * completeness, even though each invocation of this macro is also
 * followed by a @c @b break statement in its @c @b switch().
 * 
 */
#define STATE_MODEL_SWITCH(somestate_rc, casebody) \
    case THREAD_STATE_NEW:     somestate_rc = casebody(new); break;    \
    case THREAD_STATE_START:   somestate_rc = casebody(start); break;  \
    case THREAD_STATE_RUNNABLE:somestate_rc = casebody(runnable);break;\
    case THREAD_STATE_RUNNING: somestate_rc = casebody(running); break;\
    case THREAD_STATE_COMPLETE:somestate_rc = casebody(complete);break;\
    case THREAD_STATE_BLOCKINGEVENT:                                   \
                               somestate_rc = casebody(blockingevent); \
                               break;                                  \
    case THREAD_STATE_BLOCKED: somestate_rc = casebody(blocked); break;\
    case THREAD_STATE_UNBLOCKED:somestate_rc=casebody(unblocked);break;\
    case THREAD_STATE_SYNCHRONIZED:                                    \
                               somestate_rc = casebody(synchronized);  \
                               break;                                  \
    case THREAD_STATE_RELEASE: somestate_rc = casebody(release); break;\
    case THREAD_STATE_WAIT:    somestate_rc = casebody(wait); break;   \
    case THREAD_STATE_NOTIFY:  somestate_rc = casebody(notify); break; \
    case THREAD_STATE_LOCK:    somestate_rc = casebody(lock); break;   \
    case THREAD_STATE_ACQUIRE: somestate_rc = casebody(acquire); break;\
    case THREAD_STATE_DEAD:    somestate_rc = casebody(dead); break;   \
    case THREAD_STATE_BADLOGIC:somestate_rc = casebody(badlogic); break;


/*!
 * @brief Initialize the Java Virtual Machine.
 *
 * Set up the pieces necessary to start the JVM running, then
 * load up @c @b java.lang.Object and the basic classes
 * that it references, all in the class table.  These include:
 *
 * @verbatim
  
       java.lang.Object
       <pseudo-class> (byte)
       <pseudo-class> (character)
       <pseudo-class> (double)
       <pseudo-class> (float)
       <pseudo-class> (int)
       <pseudo-class> (long)
       <pseudo-class> (short)
       <pseudo-class> (boolean)
       java.lang.Class
       java.lang.String
       java.lang.Thread
       <startup class from command line>
  
   @endverbatim
 *
 * All of these will have their @c @b \<clinit\> class initialization
 * methods invoked, if any.  The following objects will be created
 * (where @c @b pjvm->argcj is the number of command line
 * arguments passed into the program which get passed through to
 * the JVM main() method):
 *
 * <ul>
 * <li> 1 instance of @c @b java.lang.String[] with a size
 *      of @c @b pjvm->argcj array elements, plus one
 *      @c @b java.lang.Object superclass object.
 * </li>
 *
 * <li> @c @b pjvm->argc instances of
 *      @c @b java.lang.String plus one
 *      @c @b java.lang.Object superclass
 *      object per String instance.
 * </li>
 * </ul>
 *
 *
 * @param argc  Number of entries in @c @b argv[]
 *                (per @c @b main() entry)
 *
 * @param argv  Command line parameters from main()
 *
 * @param envp  Environment pointer from main()
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 *
 */

static rvoid jvm_init(int argc, char **argv, char **envp)
{
    ARCH_FUNCTION_NAME(jvm_init);

    /* Clear all initialization roll call globals */
    jvm_completely_initialized = rfalse;
    jvm_timeslice_initialized  = rfalse;
    jvm_class_initialized      = rfalse;
    jvm_object_initialized     = rfalse;
    jvm_thread_initialized     = rfalse;
    jvm_classpath_initialized  = rfalse;
    jvm_argv_initialized       = rfalse;
#if JVMCFG_TMPAREA_IN_USE
    jvm_tmparea_initialized    = rfalse;
#endif
    jvm_model_initialized      = rfalse;
    jvm_heap_initialized       = rfalse;

    /********** Arm java.lang.LinkageError handler ***/

                      exit_exception_setup();
    int nonlocal_rc = EXIT_EXCEPTION_SETUP();

    if (EXIT_MAIN_OKAY == nonlocal_rc)
    {
        opcode_calling_java_lang_linkageerror = rfalse;
    }
    else
    {
        /*
         * Should always be false after exit_throw_exception()
         * unless someone called it with a null pointer
         */
        if (rnull == exit_LinkageError_subclass)
        {
            exit_LinkageError_subclass = "unknown";
        }

        fprintfLocalStderr("%s:  Error %d (%s): %s\n",
                           arch_function_name,
                           nonlocal_rc,
                           exit_get_name(nonlocal_rc),
                           exit_LinkageError_subclass);

        exit_jvm(nonlocal_rc);
    }


    /********** Initialize heap management ***********/

    HEAP_INIT(&jvm_heap_initialized);


    /********** Initialize entire JVM area ***********/

    jvm_model_init(argc, argv, envp);

    /********** Initialize message verbosity *********/

    jvmutil_set_dml(DMLDEFAULT);

    HEAP_INIT_REPORT(&jvm_heap_initialized);


#if JVMCFG_TMPAREA_IN_USE
    /********** Initialize temp area *****************/

    tmparea_init(argv,
                 HEAP_GET_DATA,
                 HEAP_FREE_DATA,
                 &jvm_tmparea_initialized);
#endif

    /********** Parse command line, environment,etc. */

    jvmargv_init(argc, argv, envp, &jvm_argv_initialized);


    /********** Initialize the @b CLASSPATH **********/

    classpath_init();


    /********** Initialize the thread area ***********/

    thread_init();


    /********** Initialize the object area ***********/

    /* Need this first because class_init() allocates object slots */
    object_init();


    /********** Initialize the class area ************/

    class_init();


    /********** Initialize the time slice area *******/

    timeslice_init();


    /********** Load java.lang.Object ****************/

    jvm_class_index clsidx;

    clsidx = class_load_from_prchar(JVMCLASS_JAVA_LANG_OBJECT,
                           /* The startup classes have local bindings */
                                    rtrue,
                                    (jint *) rnull);


    /********** Load primatives of all types *********/

    /* Loaded on system thread */
    pjvm->class_primative_byte   =class_load_primative(BASETYPE_CHAR_B);
    pjvm->class_primative_char   =class_load_primative(BASETYPE_CHAR_C);
    pjvm->class_primative_double =class_load_primative(BASETYPE_CHAR_D);
    pjvm->class_primative_float  =class_load_primative(BASETYPE_CHAR_F);
    pjvm->class_primative_int    =class_load_primative(BASETYPE_CHAR_I);
    pjvm->class_primative_long   =class_load_primative(BASETYPE_CHAR_J);
    pjvm->class_primative_short  =class_load_primative(BASETYPE_CHAR_S);
    pjvm->class_primative_boolean=class_load_primative(BASETYPE_CHAR_Z);

    if((jvm_class_index_null == pjvm->class_primative_byte)    ||
       (jvm_class_index_null == pjvm->class_primative_char)    ||
       (jvm_class_index_null == pjvm->class_primative_double)  ||
       (jvm_class_index_null == pjvm->class_primative_float)   ||
       (jvm_class_index_null == pjvm->class_primative_int)     ||
       (jvm_class_index_null == pjvm->class_primative_long)    ||
       (jvm_class_index_null == pjvm->class_primative_short)   ||
       (jvm_class_index_null == pjvm->class_primative_boolean))
    {
        sysErrMsg(arch_function_name,
                  "Cannot load primative classes for java.lang.Class");
        exit_jvm(EXIT_JVM_CLASS);
/*NOTREACHED*/
    }


    /********** Load java.lang.Class *****************/

    /********** Load java.lang.String ****************/

    /********** Load java.lang.Thread ****************/

    /********** Load, resolve, and @c @b \<clinit\> all classes *****/

    /*
     * Loading has been performed, but not resolve or run the
     * JVM manually to process the @c @b \<clinit\> method, so go ahead
     * and invoke using the normal multi-stage entry point here.
     * It is designed to only process those stages that remain
     * unprocessed from earlier activity.
     */
    jvm_class_index clsidxOBJECT =
        class_load_resolve_clinit(JVMCLASS_JAVA_LANG_OBJECT,
                                  jvm_thread_index_null,
                                  rtrue,
                                  rtrue);
#if 0
    /* Enable for diagnostics when desired */
    cfmsgs_show_constant_pool(CLASS_OBJECT_LINKAGE(clsidxOBJECT)->pcfs);
#endif

    jvm_class_index clsidxCLASS =
        class_load_resolve_clinit(JVMCLASS_JAVA_LANG_CLASS,
                                  jvm_thread_index_null,
                                  rtrue,
                                  rtrue);
#if 0
    /* Enable for diagnostics when desired */
    cfmsgs_show_constant_pool(CLASS_OBJECT_LINKAGE(clsidxCLASS)->pcfs);
#endif

    jvm_class_index clsidxSTRING =
        class_load_resolve_clinit(JVMCLASS_JAVA_LANG_STRING,
                                  jvm_thread_index_null,
                                  rtrue,
                                  rtrue);
#if 0
    /* Enable for diagnostics when desired */
    cfmsgs_show_constant_pool(CLASS_OBJECT_LINKAGE(clsidxSTRING)->pcfs);
#endif

    HEAP_REPORT(&jvm_heap_initialized);

    /*!
     * @todo HARMONY-6-jvm-jvm.c-7 Locate the 'value' and 'length'
     *       fields for this JVM's definition of java.lang.String.
     *       Do this _only_ if using this JVM's definition!!!  It is
     *       not guaranteed that other JDK's will call the internal
     *       fields by these same names.  The current implementation
     *       defaults to some compile-time definitions, but these are
     *       @e never provably valid unless this JVM's definition of
     *       java.lang.String is used anyway, so wean the source base
     *       away from such shortcuts and do it correctly here:
     */
#ifndef CONFIG_HACKED_BOOTCLASSPATH
#endif

    jvm_class_index clsidxTHREAD =
        class_load_resolve_clinit(JVMCLASS_JAVA_LANG_THREAD,
                                  jvm_thread_index_null,
                                  rtrue,
                                  rtrue);
#if 0
    /* Enable for diagnostics when desired */
    cfmsgs_show_constant_pool(CLASS_OBJECT_LINKAGE(clsidxTHREAD)->pcfs);
#endif

    HEAP_REPORT(&jvm_heap_initialized);

    /********** Load java.lang.String[] (1 dim array) */

    jint *string1 = HEAP_GET_DATA( 1 * sizeof(jint), rfalse);

    string1[0] = pjvm->argcj; /* Number of java command line parms */

    cp_info_mem_align *pcpclsname =
        nts_prchar2utf_classname(JVMCLASS_JAVA_LANG_STRING, 1);

    rchar *pclsname = utf_utf2prchar(PTR_THIS_CP_Utf8(pcpclsname));

    clsidx = class_load_from_prchar(pclsname,
                                    rtrue,
                                    string1);

    HEAP_FREE_DATA(pcpclsname);

    HEAP_FREE_DATA(pclsname);

    /********** Load misc java.lang.* for runtime ******/
    /*          (with manual linkage error checking) */

    (rvoid) class_load_resolve_clinit(
                 JVMCLASS_JAVA_LANG_STACKTRACEELEMENT,
                 jvm_thread_index_null,
                 rtrue,
                 rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_THROWABLE,
                                       jvm_thread_index_null,
                                       rtrue,
                                       rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_ERROR,
                                       jvm_thread_index_null,
                                       rtrue,
                                       rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_LINKAGEERROR,
                                       jvm_thread_index_null,
                                       rtrue,
                                       rfalse);

    HEAP_REPORT(&jvm_heap_initialized);


    /******* Re-arm java.lang.LinkageError handler ***/

                  exit_exception_setup();
    nonlocal_rc = EXIT_EXCEPTION_SETUP();

    if (EXIT_MAIN_OKAY == nonlocal_rc)
    {
        opcode_calling_java_lang_linkageerror = rfalse;
    }
    else
    {
        /*
         *  This handler invokes opcode_load_run_throwable(),
         *  so exit_exception_setup() @e must be rearmed
         *  @e again when it enters that function!!!
         */

        /* Should always be true via exit_throw_exception() */
        if (rnull == exit_LinkageError_subclass)
        {
            exit_LinkageError_subclass = "unknown";
        }

        fprintfLocalStderr("%s:  Error %d (%s): %s\n",
                           arch_function_name,
                           nonlocal_rc,
                           exit_get_name(nonlocal_rc),
                           exit_LinkageError_subclass);

        opcode_load_run_throwable(exit_LinkageError_subclass,
                                  exit_LinkageError_thridx);

        exit_jvm(nonlocal_rc);
    }


    /********** Load misc java.lang.* for runtime ******/
    /*          (with NORMAL linkage error checking) */

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_EXCEPTION,
                                      jvm_thread_index_null,
                                      rtrue,
                                      rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_SYSTEM,
                                      jvm_thread_index_null,
                                      rtrue,
                                      rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_RUNTIME,
                                      jvm_thread_index_null,
                                      rtrue,
                                      rfalse);

    (rvoid) class_load_resolve_clinit(JVMCLASS_JAVA_LANG_THREADGROUP,
                                      jvm_thread_index_null,
                                      rtrue,
                                      rfalse);

    /********** Load startup class @c @b \<clinit\> **********/

    rchar *startup = (rnull != pjvm->startjar)
                        ? pjvm->startjar
                        : pjvm->startclass;

    /* Chk missing entry point */
    if (rnull == startup)
    {
        sysErrMsg(arch_function_name, "Missing startup class name");
        exit_jvm(EXIT_JVM_CLASS);
/*NOTREACHED*/
    }

    /*
     * Keep JAR file as-is, but convert regular class definition to
     * internal form.  Why?  So all references in the JVM are to
     * internal form, not just those coming from class files.
     *
     * Do @e not modify @b startup in place since it may come ultimately
     * from the @c @b argv[] list (Translation:  "Be nice to
     * the runtime environment!")
     */
    if (rnull != pjvm->startjar)
    {
        jar_state *pjs =
            jarutil_find_member(pjvm->startjar,
                                JVMCFG_JARFILE_MANIFEST_FILENAME,
                                HEAP_GET_DATA,
                                HEAP_FREE_DATA);

        if (rnull == pjs)
        {
            sysErrMsg(arch_function_name,
                      "Invalid startup JAR file name");
            exit_jvm(EXIT_JVM_CLASS);
/*NOTREACHED*/
        }

        if (JAR_OKAY != pjs->jar_code)
        {
            portable_close(pjs->fd);

            if (pjs->jar_msg)
            {
            sysErrMsg(arch_function_name,
                     "Invalid or missing startup JAR manifest file: %s",
                      pjs->jar_msg);
            }
            else
            {
            sysErrMsg(arch_function_name,
                      "Invalid or missing startup JAR manifest file");
            }
            HEAP_FREE_DATA(pjs);

            exit_jvm(EXIT_JVM_CLASS);
/*NOTREACHED*/
        }

        /* JAR file manifest contains class name for main() entry */
        jarutil_read_current_member(pjs);

        /* Extract startup class from manifest */
        startup = manifest_get_main_from_bfr(pjs->bfr,
                                pjs->directory_entry.uncompressed_size);

        portable_close(pjs->fd);
        HEAP_FREE_DATA(pjs);

        if (rnull == startup)
        {
            sysErrMsg(arch_function_name,
                      "Startup class not found in JAR manifest file");
            exit_jvm(EXIT_MANIFEST_JAR);
/*NOTREACHED*/
        }

        /*!
         * @internal If startup class was found in '-jar filename' parm,
         *           then when it comes time to load that class below,
         *           it will be found at the very beginning of
         *           @b CLASSPATH since 'filename' was inserted there
         *           during classpath_init() name insertions.
         */
    }
    else
    {
        startup = classpath_external2internal_classname(startup);
    }

    jvm_class_index clsidxSTARTUP =
        class_load_resolve_clinit(startup,
                                  jvm_thread_index_null,
                                  rtrue,
                                  rfalse);

    if (jvm_class_index_null == clsidxSTARTUP)
    {
        sysErrMsg(arch_function_name, "Cannot load class %s", startup);
        exit_jvm(EXIT_JVM_CLASS);
/*NOTREACHED*/
    }


    /********** Load java.lang.String[] args data *****/

    jint *pjargc = HEAP_GET_DATA(1 * sizeof(jint), rfalse);

    /*!
     * @internal It is only possible to load command line parameters
     *           if @e not "borrowing" the java.lang.String from a
     *           pre-existing JDK.  The version from this source
     *           base @e must be used to make this possible.
     */
    pjargc[0] =
#ifndef CONFIG_HACKED_BOOTCLASSPATH
                pjvm->argcj
#else
                0    /* Not possible without built-in String class */
#endif
                ;

    jvm_object_hash objhashjargs =
        object_instance_new(OBJECT_STATUS_ARRAY,
                            CLASS_OBJECT_LINKAGE(
                                    pjvm->class_java_lang_String)->pcfs,
                            pjvm->class_java_lang_String,
                            1,
                            pjargc,
                            rfalse,
                            jvm_thread_index_null,
                            (CONSTANT_Utf8_info *) rnull);


#ifndef CONFIG_HACKED_BOOTCLASSPATH
    /*
     * Explicitly cast the untyped
     * @link robject#arraydata arraydata@endlink
     * member for object hash
     */
    jvm_object_hash *pjargv = 
        (jvm_object_hash *) OBJECT(objhashjargs).arraydata;

    if (0 < pjvm->argcj)
    {
        rint i;
        for (i = 0; i < pjvm->argcj; i++)
        {
            cp_info_mem_align *putfarg = nts_prchar2utf(pjvm->argvj[i]);

            /*!
             * @todo HARMONY-6-jvm-jvm.c-8 This invocation needs better
             *       unit testing with real data.
             */
            pjargv[i] = object_instance_new(OBJECT_STATUS_STRING,
                            CLASS_OBJECT_LINKAGE(
                                    pjvm->class_java_lang_String)->pcfs,
                            pjvm->class_java_lang_String,
                            LOCAL_CONSTANT_NO_ARRAY_DIMS,
                            (jint *) rnull,
                            rfalse,
                            jvm_thread_index_null,
                            PTR_THIS_CP_Utf8(putfarg));

            HEAP_FREE_DATA(putfarg);

            if (jvm_object_hash_null == pjargv[i])
            {
                sysErrMsg(arch_function_name,
                          "Cannot allocate String[] object");
                exit_jvm(EXIT_JVM_OBJECT);
/*NOTREACHED*/
            }

            /* Add reference to command line parameter string */
            (rvoid) GC_OBJECT_MKREF_FROM_OBJECT(jvm_object_hash_null,
                                                pjargv[i]);
        }
    }
#endif

    /********** POP_FRAME()/-mod PC-/PUSH_FRAME() ****/

/*!
 * @todo HARMONY-6-jvm-jvm.c-6 POP_FRAME() and do new PUSH_FRAME()
 *       with startup class PC
 *
 *        (Do <b><code>new Thread()</code></b> and hack the PC with
 *        startup class @c @b main() entry.)
 */


    /********** Load startup class main() ************/

    /* Loaded on arbitrary user thread, but NOT on system thread */

    if (jvm_method_index_bad ==
        method_find_by_prchar(clsidxSTARTUP,
                              JVMCFG_MAIN_METHOD,
                              JVMCFG_MAIN_PARMS))
    {
        sysErrMsg(arch_function_name,
                  "Cannot load main method %s %s%s",
                  startup,
                  JVMCFG_MAIN_METHOD,
                  JVMCFG_MAIN_PARMS);
        exit_jvm(EXIT_JVM_METHOD);
/*NOTREACHED*/
    }

    /*
     * Mark startup class as having a reference, namely,
     * the reference from the command line invocation.
     * This way it cannot get marked for garbage collection.
     */
    (rvoid) GC_CLASS_MKREF_FROM_CLASS(jvm_class_index_null,
                                      clsidxSTARTUP);

    jvm_thread_index thridx = thread_class_load(startup,
                                                JVMCFG_MAIN_METHOD,
                                                JVMCFG_MAIN_PARMS,
                                                THREAD_PRIORITY_NORM,
                                                rfalse,
                                                rfalse,
                                                rfalse);

    /********** Insert (String[] args) for main() ****/

    PUT_LOCAL_VAR(thridx, JVMCFG_MAIN_PARM_ARGV_INDEX, objhashjargs);


    /********** Request startup of main() ************/

    if (MANUAL_THREAD_STARTUP(thridx))
    { 
        ; /* Nothing to do except go run main() in main JVM loop */
    }
    else 
    {
        /* Something went wrong, so quit */
        sysErrMsg(arch_function_name,
                  "Cannot start %s %s%s",
                  startup,
                  JVMCFG_MAIN_METHOD,
                  JVMCFG_MAIN_PARMS);
        exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
    }

    if (rnull == pjvm->startjar)
    {
        HEAP_FREE_DATA(startup);
    }

    /********** main() is in the RUNNING state *******/
    /********** and its PC is at 1st instruction... **/

    /********** Declare the JVM completely set up ****/
    jvm_completely_initialized = rtrue;

    HEAP_REPORT(&jvm_heap_initialized);

    return;

} /* END of jvm_init() */


/*!
 * @brief Start up JVM execution engine and let Java program code
 * take over.
 *
 * At this point, the class containing main() has been loaded and its
 * class initialization has been performed.  The program counter is
 * at the very first instruction of main() and the thread is in the
 * @b RUNNING state.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
static rvoid jvm_run()
{
    ARCH_FUNCTION_NAME(jvm_run);

    sysDbgMsg(DMLMIN, arch_function_name, "started");

    /*
     * Run the virtual machine as long as there are user threads,
     * that is, as long as the ISDAEMON bit is clear on at least
     * one thread table entry.
     */
    rint no_user_threads = rtrue;
    while (rtrue == no_user_threads)
    {
        /* Tested once per whole pass:
         *     thread1 ISDAEMON
         *     AND thread 2 ISDAEMON
         *     AND thread 3 ISDAEMON
         *     AND ...
         *
         * If next while() iteration starts, at least ONE thread had
         * the ISDAEMON bit off, meaning there was at least one user
         * thread in the JVM, so keep running unto expression is
         * @link #rtrue rtrue@endlink.
         */
        rint no_user_threads = rtrue;

                                 /* JVMCFG_NULL_THREAD--not activated */
        for (CURRENT_THREAD = JVMCFG_SYSTEM_THREAD;
             CURRENT_THREAD < JVMCFG_MAX_THREADS;
             CURRENT_THREAD++)
        {
            /* Check if this thread is alive */
            if (!(THREAD_STATUS_INUSE & THREAD(CURRENT_THREAD).status))
            {
                continue;
            }

            switch(CURRENT_THREAD)
            {
                case CHEAT_AND_ALLOW_NULL_THREAD_INDEX:
                    /* Never activated */
                    continue;

                case JVMCFG_SYSTEM_THREAD:
                    /*
                     * Reserved for use by internal logic that
                     * invokes thread_new_system()
                     */
                    continue;

                case JVMCFG_GC_THREAD:
                    /* Default garbage collector is a native method */
                    GC_RUN(rtrue);
                    continue;
            }

            /* Check if any user threads are alive (loop test) */
            if (!(THREAD_STATUS_ISDAEMON &
                  THREAD(CURRENT_THREAD).status))
            {
                no_user_threads = rfalse;
            }

            /*!
             * @verbatim
              
               State transition and processing table
               =====================================
              
              
               STATE MODEL PHASE 1:  VERIFY CHANGE
                  Verify whether or not a requested state transition
                  is valid.  Return rtrue if valid, otherwise rfalse.
              
               STATE MODEL PHASE 2:  PERFORM CHANGE
                  Perform known-valid requested state transition.
                  Return rtrue if transition occurred, otherwise rfalse.
              
               STATE MODEL PHASE 3:  PROCESS STATE
                  Perform activities in all states, as long as the
                  state is valid.  Return rtrue if processing occurred,
                  otherwise rfalse.
              
               This logic is the essence of what each of
               the pieces for each state in
               @link src/threadstate.c threadstate.c@endlink
               is all about.  See further comments there.
               @endverbatim
             */

            /* STATE MODEL PHASE 1 (see definition above) */
            rint nextstate_rc;

            if (THIS_STATE(CURRENT_THREAD) !=
                NEXT_STATE(CURRENT_THREAD))
            {
                switch (NEXT_STATE(CURRENT_THREAD))
                {
                    STATE_MODEL_SWITCH(nextstate_rc,
                                     CURRENT_THREAD_REQUEST_NEXT_STATE);
                        break;

                    default:
                        /* Complain and go to BADLOGIC state forever */
                        sysErrMsg(arch_function_name,
                                "illegal next state %d this=%d prev=$d",
                                  NEXT_STATE(CURRENT_THREAD),
                                  THIS_STATE(CURRENT_THREAD),
                                  PREV_STATE(CURRENT_THREAD));
                        nextstate_rc =
                            CURRENT_THREAD_REQUEST_NEXT_STATE(badlogic);
                        break;

                } /* switch (NEXT_STATE(CURRENT_THREAD)) */

            } /* if this_state != next_state */

            if (rfalse == nextstate_rc)
            {
                sysErrMsg(arch_function_name,
                          "Unable to move thread %d to '%s' state",
                          CURRENT_THREAD,
                          thread_state_get_name(
                              NEXT_STATE(CURRENT_THREAD)));
                CURRENT_THREAD_REQUEST_NEXT_STATE(badlogic);
            }


            /* STATE MODEL PHASE 2 (see definition above) */

            if (THIS_STATE(CURRENT_THREAD) !=
                NEXT_STATE(CURRENT_THREAD))
            {
                switch (THIS_STATE(CURRENT_THREAD))
                {
                    STATE_MODEL_SWITCH(nextstate_rc,
                                    CURRENT_THREAD_ACTIVATE_THIS_STATE);
                        break;

                    default:
                        /*
                         * Should @e never happen, the switch() default
                         * above should have thrown this thread into
                         * the BADLOGIC state.
                         */
                        sysErrMsg(arch_function_name,
                                  "illegal thread state %d",
                                  NEXT_STATE(CURRENT_THREAD));
                        exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
                }

                if (rfalse == nextstate_rc)
                {
                    sysErrMsg(arch_function_name,
                           "Unable to activate thread %d in '%s' state",
                              CURRENT_THREAD,
                              thread_state_get_name(
                                           NEXT_STATE(CURRENT_THREAD)));

                    /*
                     * This will cause a @c @b break in the
                     * @c @b while() loop
                     */
                    CURRENT_THREAD_REQUEST_NEXT_STATE(badlogic);
                }

            } /* if this_state != next_state */

            /*
             * STATE MODEL PHASE 3 (see definition above)
             */
            rint thisstate_rc;

            if (THIS_STATE(CURRENT_THREAD) ==
                NEXT_STATE(CURRENT_THREAD))
            {
                switch (THIS_STATE(CURRENT_THREAD))
                {
                    STATE_MODEL_SWITCH(thisstate_rc,
                                     CURRENT_THREAD_PROCESS_THIS_STATE);
                        break;

                    default:
                        /*
                         * Should @e never happen, the switch() default
                         * above should have thrown this thread into
                         * the BADLOGIC state.
                         */
                        sysErrMsg(arch_function_name,
                                  "illegal thread state %d",
                                  NEXT_STATE(CURRENT_THREAD));
                        exit_jvm(EXIT_JVM_THREAD);
/*NOTREACHED*/
                }


                if (rfalse == nextstate_rc)
                {
                    sysErrMsg(arch_function_name,
                            "Unable to process thread %d to '%s' state",
                              CURRENT_THREAD,
                              thread_state_get_name(
                                  THIS_STATE(CURRENT_THREAD)));
                    CURRENT_THREAD_REQUEST_NEXT_STATE(badlogic);
                }

            } /* if this_state == next_state */

        } /* for current_thread */

    } /* while no_user_threads */

    sysDbgMsg(DMLMIN, arch_function_name, "finished");

} /* END of jvm_run() */


/*!
 * @brief Shut down the Java Virtual Machine.
 *
 * Use a global for roll call of initialization
 * to determine whether each area has been set
 * up properly in case of abort during initialization.
 *
 *
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

rvoid jvm_shutdown()
{
    ARCH_FUNCTION_NAME(jvm_shutdown);

    /*
     * Re-arm @c @b java.lang.LinkageError handler for
     * the final time.
     */

                      exit_exception_setup();
    int nonlocal_rc = EXIT_EXCEPTION_SETUP();

    if (EXIT_MAIN_OKAY == nonlocal_rc)
    {
        opcode_calling_java_lang_linkageerror = rfalse;
    }
    else
    {
        /*
         * Should always be false after exit_throw_exception()
         * unless someone called it with a null pointer
         */
        if (rnull == exit_LinkageError_subclass)
        {
            exit_LinkageError_subclass = "unknown";
        }

        fprintfLocalStderr("%s:  Error %d (%s): %s\n",
                           arch_function_name,
                           nonlocal_rc,
                           exit_get_name(nonlocal_rc),
                           exit_LinkageError_subclass);

        return; /* Give up, don't run any error handlers */
    }


    /*
     * Clean up thread stack areas, ClassFile storage, etc.
     * This process is effectively the reverse of jvm_init().
     * Notice, however, that jvmargv_shutdown() and
     * classpath_shutdown() are reversed from this
     * normal order.
     */

    jvm_completely_initialized = rfalse;

    if (rtrue == jvm_timeslice_initialized)
    {
        timeslice_shutdown();
    }

    if (rtrue == jvm_class_initialized)
    {
        class_shutdown_1();
    }

    if (rtrue == jvm_object_initialized)
    {
        object_shutdown();
    }

    if (rtrue == jvm_class_initialized)
    {
        class_shutdown_2();
    }

    if (rtrue == jvm_thread_initialized)
    {
        thread_shutdown();
    }

    if (rtrue == jvm_argv_initialized)
    {
        jvmargv_shutdown(&jvm_argv_initialized);
    }

    if (rtrue == jvm_classpath_initialized)
    {
        classpath_shutdown();
    }

#if JVMCFG_TMPAREA_IN_USE
    if (rtrue == jvm_tmparea_initialized)
    {
        tmparea_shutdown(HEAP_GET_DATA,
                         HEAP_FREE_DATA,
                        &jvm_tmparea_initialized);
    }
#endif

    if (rtrue == jvm_model_initialized)
    {
        jvm_model_shutdown();
    }

    if (rtrue == jvm_heap_initialized)
    {
        HEAP_SHUTDOWN(&jvm_heap_initialized);
    }

    return;

} /* END of jvm_shutdown() */


/*!
 * @brief Common signal handler to shut down JVM upon receipt of
 * common signals.
 *
 *
 * @param  sig   Signal number
 *
 *
 * @returns non-local return via exit_jvm()
 *
 */
rvoid jvm_signal(int sig)
{
    ARCH_FUNCTION_NAME(jvm_signal);

    sysErrMsg(arch_function_name, "received signal %d", sig);

    exit_jvm(EXIT_JVM_SIGNAL);
/*NOTREACHED*/
} /* END of jvm_signal() */


/*!
 * @brief Main entry point for this library implementing the
 * Java Virtural Machine.
 *
 * The arguments are identical to those passed to a 'C' lanaguage
 * @c @b main() program, and typically come directly from it.
 *
 * @param  argc  Number of elements in @c @b argv array, per
 *               @c @b main() command line
 *
 * @param  argv  Array of arguments, per
 *               @c @b main() command line
 *
 * @param  envp  Environment variable array, per
 *               @c @b main() command line
 *
 *
 *
 * @returns exit code from the @link #exit_jvm() exit(jvm()@endlink
 *          encountered that initiated shutdown of the JVM.
 *
 *
 * @see @link #main() main()@endlink
 *
 */

int jvm(int argc, char **argv, char **envp)
{
    ARCH_FUNCTION_NAME(jvm);

    int exit_rc;

    /*
     * Protect JVM shutdown and heap free mechanism
     * with non-local error return.  When setting
     * it up, @link #EXIT_INIT() EXIT_INIT()@endlink returns @link
       #EXIT_MAIN_OKAY exit code enumeration EXIT_MAIN_OKAY@endlink,
     * but when invoking @link #exit_jvm() exit_jvm(EXIT_xxx)@endlink,
     * return code @link #EXIT_MAIN_OKAY EXIT_xxx@endlink is returned,
     * see @link src/exit.h exit.h@endlink for particulars.
     */
    exit_rc = EXIT_INIT();

    if (EXIT_MAIN_OKAY != exit_rc)
    {
        /* Re-arm handler to simply exit */
        int rearm_exit_rc = EXIT_INIT();

        if (EXIT_MAIN_OKAY != rearm_exit_rc)
        {
            /*Don't attempt other processing-- potential infinite loop*/

            /* Just minimal cleanup and quit */
            JVMCFG_DEBUG_ECLIPSE_FLUSH_STDIO_BETTER_EXIT;
            return(rearm_exit_rc);
        }

        jvm_shutdown();

        JVMCFG_DEBUG_ECLIPSE_FLUSH_STDIO_BETTER_EXIT;

        /* Exit code from JVM */
        return(exit_rc);
    }

    /*
     * Trap various standard signals to stop JVM
     * (must call EXIT_INIT() first)
     */
    signal(SIGHUP, jvm_signal);
    signal(SIGINT, jvm_signal);
    signal(SIGTERM, jvm_signal);

    /*
     * Normal path from EXIT_INIT():
     *
     * Initialize JVM model, init all structures, load prequisite
     * classes (Object, String[]), including @c @b \<clinit\>,
     * load requested user class, run its @c @b \<clinit\> .
     */
    jvm_init(argc, argv, envp);

    /* Now that JVM is initialized and user class loaded, go run it*/
    jvm_run();

    jvm_shutdown();

    JVMCFG_DEBUG_ECLIPSE_FLUSH_STDIO_BETTER_EXIT;

    /* Exit code from JVM */
    return(EXIT_MAIN_OKAY);

} /* END of jvm() */


/* EOF */
