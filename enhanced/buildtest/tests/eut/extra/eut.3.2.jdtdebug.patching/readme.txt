        =====================================================================
        Apply patches to Eclipse Unit Tests 'jdtcorecompiler' suite in order to
        pass on Apache Harmony
        =====================================================================

This document describes how to apply patches to Eclipse Unit Tests 'jdtdebug'
suite in order to pass on Apache Harmony and how to use the Apache Ant script in
this archive to apply the patches.

---------------
Archive Content
---------------

This archive contains script for patching Eclipse Unit Tests 'jdtcorecompiler'
suite. The archive content is as follows:

+<eut.3.2.jdtdebug.patching>/
|-+patch/                           - directory containing patches to apply
|   |
|   |--testprograms                 - patches for simple classes used as the code to debug
|   |
|   |--tests                        - jdtdebug tests patches
|
|--build.xml                        - Apache Ant scrip that applies given patches
|
|--properties                       - properties file specifying Eclipse 3.2 and EUT 3.2 locations
|
|--readme.txt                       - This README file

------------------------------------------------------------
Known issues preventing 'jdtdebug' suite from passing
on Harmony that need to be workarounded
------------------------------------------------------------

1. https://bugs.eclipse.org/bugs/show_bug.cgi?id=162366
Several jdtdebug tests depend on VM behavior
2. https://bugs.eclipse.org/bugs/show_bug.cgi?id=193488
org.eclipse.jdt.debug.test.stepping.StepIntoSelectionTests depend on VM behavior.
One of these tests leaves open error dialog window 'Execution did not enter
"step" before the current method returned.', which requires user
interaction to finish tests execution.

------------------------------------
How to patch 'jdtdebug' suite
------------------------------------

Prerequisites: Eclipse SDK 3.2, EUT 3.2 archives, ecj_3.2.jar (Eclipse batch compiler)

1. Download EUT 3.2 and Eclipse 3.2 archives and ecj_3.2.jar from http://www.eclipse.org
2. Unpack Eclipse 3.2 archive
3. Unzip EUT 3.2 archive; in the 'eclipse-testing' directory that is
   created after EUT unpacking unzip eclipse-junit-tests-*.zip, then unzip
   eclipse-testing/eclipse/plugins/org.eclipse.sdk.tests.source_3.2.0.v20060329/src/org.eclipse.jdt.debug.tests_3.1.0/javadebugtestssrc.zip
4. Apply patches from 'patch/tests' directory to the unpacked jdtdebug sources
5. Apply patches from 'patch/testprograms' directory to eclipse-testing/eclipse/plugins/org.eclipse.jdt.debug.tests_3.1.0/testprograms
6. Compile source files using ECJ 3.2 compiler (add jar-s from eclipse plugins
   directory and eclipse-testing/eclipse/plugins directory to the classpath)
7. Put compiled classes to the corresponding locations in
   eclipse-testing/eclipse/plugins/org.eclipse.jdt.debug.tests_3.1.0/javadebugtests.jar
8. Pack everything back preserving the archives structure

-----------------------------------------------------
How to patch 'jdtdebug' suite using the script
-----------------------------------------------------

1. Unpack this archive, review properties file and modify it if necessary
   (for example, if you have EUT and/or Eclipse archives locally and don't need
   to download them from http://www.eclipse.org)
2. Make sure you have at least 600Mb of free space on your hard-drive
3. Make sure you have ecj_3.2.jar in your ANT_HOME/lib directory
4. Make sure that PATH environment variable contains JRE 1.5 (use RI) and Apache
   Ant (version >=1.6.5), and that JAVA_HOME and ANT_HOME are properly set up.
   Make sure that 'unzip' tool can also be found in your PATH
4. Change directory to the unpacked 'eut.3.2.jdtdebug.patching' directory and run:
   ant
   Patching may take several minutes because it deals with heavy-weight archives
5. Find the patched EUT archive in 'patched' directory
6. If the script failed because of configuration issues you might need to clean
   environment by running
   ant clean




