        =====================================================================
        Apply patches to Eclipse Unit Tests 'jdtcorecompiler' suite in order to
        pass on Apache Harmony
        =====================================================================

This document describes how to apply patches to Eclipse Unit Tests 'jdtcorecompiler'
suite in order to pass on Apache Harmony and how to use the Apache Ant script in
this archive to apply the patches.

---------------
Archive Content
---------------

This archive contains script for patching Eclipse Unit Tests 'jdtcorecompiler'
suite. The archive content is as follows:

+<eut.jdtcorecompiler.patching>/
|-+patch/                           - directory containing patches to apply
|   |
|   |--patch_eclipse172820          - patch to workaround EUT issue (see details below)
|
|--build.xml                        - Apache Ant scrip that applies given patches
|
|--properties                       - properties file specifying Eclipse 3.2 and EUT 3.2 locations
|
|--readme.txt                       - This README file

------------------------------------------------------------
Known issues preventing 'jdtcorecompiler' suite from passing
on Harmony that need to be workarounded
------------------------------------------------------------

1. https://bugs.eclipse.org/bugs/show_bug.cgi?id=172820:
Several classes from org/eclipse/jdt/core/tests/compiler, org/eclipse/jdt/core/tests/runtime,
org/eclipse/jdt/core/tests/util use hard-coded class library path that doesn't
work for Apache Harmony. The patch contains workaround for this issue for EUT 3.2,
because its fix is committed to Eclipse 3.3 only.
2. https://bugs.eclipse.org/bugs/show_bug.cgi?id=188127:
Some tests from org.eclipse.jdt.core.tests.eval suite fail because the timeout
for VM launch is not enough for DRLVM. The patch consists in increasing the
timeout to 30 seconds.

------------------------------------
How to patch 'jdtcorecompiler' suite
------------------------------------

Prerequisites: Eclipse SDK 3.2, EUT 3.2 archives, ecj_3.2.jar (Eclipse batch compiler)

1. Download EUT 3.2 and Eclipse 3.2 archives and ecj_3.2.jar from http://www.eclipse.org
2. Unpack Eclipse 3.2 archive
3. Unzip EUT 3.2 archive; in the 'eclipse-testing' directory that is
   created after EUT unpacking unzip eclipse-junit-tests-*.zip, then unzip
   eclipse-testing/eclipse/plugins/org.eclipse.sdk.tests.source_3.2.0.v20060329/src/org.eclipse.jdt.core.tests.compiler_3.2.0/jdtcoretestscompilersrc.zip
4. Apply patches from patch directory to the unpacked jdtcoretestscompiler sources
5. Compile source files using ECJ 3.2 compiler (add jar-s from eclipse plugins
   directory and eclipse-testing/eclipse/plugins directory to the classpath)
6. Put compiled classes to the corresponding locations in
   eclipse-testing/eclipse/plugins/org.eclipse.jdt.core.tests.compiler_3.2.0/jdtcoretestscompiler.jar
7. Pack everything back preserving the archives structure

-----------------------------------------------------
How to patch 'jdtcorecompiler' suite using the script
-----------------------------------------------------

1. Unpack this archive, review properties file and modify it if necessary
   (for example, if you have EUT and/or Eclipse archives locally and don't need
   to download them from http://www.eclipse.org)
2. Make sure you have at least 600Mb of free space on your hard-drive
3. Make sure you have ecj_3.2.jar in your ANT_HOME/lib directory
4. Make sure that PATH environment variable contains JRE 1.5 (use RI) and Apache
   Ant (version >=1.6.5), and that JAVA_HOME and ANT_HOME are properly set up.
   Make sure that 'unzip' tool can also be found in your PATH
4. Change directory to the unpacked 'eut.jdtcorecompiler.patching' directory and run:
   ant
   Patching may take several minutes because it deals with heavy-weight archives
5. Find the patched EUT archive in 'patched' directory
6. If the script failed because of configuration issues you might need to clean
   environment by running
   ant clean




