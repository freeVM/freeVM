jEdit4.2 automated GUI test suite
=================================

Main purpose of these tests is to make sure that implementation
under test (Harmony's VM's primarily) is able to run jEdit application
and an user is able to perform some work in jEdit. Tests automation is based
on Abbot Java GUI test automation framework (see http://abbot.sourceforge.net).

There are three tests:

 - "launch" test; This test just checks that jEdit application
   can be launched using runtime under test;

 - "functional" test; This test launches jEdit application and
   checks that some standard work scenario can be performed in jEdit;

 - "stress" test; This test launches jEdit application and checks
   that some work scenario (almost the same as in "functional" test)
   can be performed in jEdit given number of times.

All tests implemented as jUnit testcases.

This readme contains the following chapters:
--------------------------------------------
1. ARCHIVE CONTENTS
2. TOOLS REQUIRED FOR THE BUILD
3. BUILDING
  3.1 BUILD ARTIFACTS
4. RUNNING TESTS
  4.1 ON HARMONY's VM
  4.2 ON RI
  4.3 TEST RUN ARTIFACTS
5. KNOWN ISSUES
6. TODO
7. DISCLAIMER AND LEGAL INFORMATION


1. ARCHIVE CONTENTS
-------------------

The archive contains test source files, test scripts and build/run ant
environment.

Below <INSTALL_DIR> is the location into which you unpacked this archive.

After extracting the archive, the following files and directories appear under 
<INSTALL_DIR>/jedit4.2_test/

  <INSTALL_DIR>/jedit4.2_test/
       |
       +-- build.xml                        - Ant build/run file
       |
       +-- jedit_test.build.properties      - Ant build/run config. Usually
       |                                      it is the only file which should
       |                                      be modified in order to configure
       |                                      regular build/test-run process
       |
       +-- readme.txt                       - This text
       |
       +-- scenario.txt                     - "functional" scenario description
       |
       +-- src/org/apache/harmony/guitests/ - Test sources
       |    |
       |    +-- JEditLaunchTest.java        - "launch" test
       |    +-- JEditFunctionalTest.java    - "functional" test
       |    +-- JEditStressTest.java        - "stress" test
       |    +-- patches/                    - Patches which enable Abbot
       |         |                            framework on Harmony's runtime*
       |         |
       |         +-- patch.txt                       - Patch for Abbot classes
       |
       +-- scripts/                         - Test scripts
            |
            +-- jedit_ln_00.xml             - "launch" test script
            +-- jedit_fn_00.xml             - "functional" test script
            +-- jedit_st_00.xml             - "stress" test script prolog
            +-- jedit_st_01.xml             - "stress" test script body
            +-- jedit_st_fixture.xml        - "stress" test script support
        

*) These small patches are temporary.
  Corresponding Abbot patch is on the project's web-page already:
  http://sourceforge.net/tracker/index.php?func=detail&aid=1618017&group_id=50939&atid=461492


2. TOOLS REQUIRED FOR THE BUILD
----------------------------------------------------------

To build Java** sources contained in the archive, install and
configure the following tools in the user environment:

  Apache Ant     - Build tool: Ant 1.6.5 or higher from http://ant.apache.org

  Junit          - Testing framework 3.8.1 or higher from http://junit.org

  J2SDK 1.5.0    - J2SE** 1.5.0 compatible SDK, for example
                   http://java.sun.com/javase/downloads/index.jsp

  Patch          - Utility for applying patches to source files. On Windows
                   you may use one from Cygwin distribution, for example v2.5.8
                   from ftp://ftp.cise.ufl.edu/pub/mirrors/cygwin/release/patch/


3. BUILDING
-----------

1) Make sure that you have internet connection because external project
   dependencies will be downloaded using it during build. If you connected
   through proxy uncomment and update with actual info two related lines in
   the beginning of 'build.properties' file

2) Set/verify the values for the following environment variables:

    - PATH (Path on Windows) must contain the path to
      patch utility executable, ant bin/ and J2SDK bin/ directories;
    - junit.jar must be in CLASSPATH

3) Build tests by executing the following commands:

    cd <INSTALL_DIR>/jedit4.2_test
    ant setup

NOTE:
 - You may get help on build/run system usage by executing 'ant'
 command without target specification in <INSTALL_DIR>/jedit4.2_test/ directory.

3.1 BUILD ARTIFACTS

The build produces a set of additional directories placed in the following
tree structure (not fully expanded here):

  <INSTALL_DIR>/jedit4.2_test/
       |
       +-- target/
            +-- classes/
            |       |
            |       +-- tests    - Compiled test classes
            +-- downloads/       - Downloaded external dependencies
            +-- abbot-1.0.0.rc5/      - Abbot framework (patched)
            +-- jedit/                - jEdit4.2final application
        


4. RUNNING TESTS
----------------

The tests may be run on both Harmony's VM's and on the RI.
At the moment of archive creation all three tests pass on RI but
only "launch" test passes on Harmony's VMs. Both "functional"
and "stress" tests fail after several successfull scenario steps
because of errors in Harmony's AWT/Swing implementation.

There is no need in internet connection and patch executable
during tests run.

4.1 ON HARMONY's VM
~~~~~~~~~~~~~~~~~~~

1) As for build process (step 2 above) set/verify
   the values for PATH and CLASSPATH environment variables.
   'javac' must be in your PATH because "functional" and
   "stress" test will execute it from within jEdit

2) Configure runtime under test in 'build.properties' file
   by modifying 'test.java.home' property's value. You must provide
   full path to the root of Harmony's JRE here.

3) Run the tests by executing the following commands:

    cd <INSTALL_DIR>/jedit4.2_test
    ant <test_name>
    
   Where <test_name> is one of

     launch
     functional
     stress

NOTE:
 - DO NOT TOUCH keyboard/mouse during test run.
 - Use 'build.properties' file to set number of "stress" test iterations by
   modifying 'test.stress.iterations' property's value  (default is 2)
 - FOR jEdit USERS: The tests need some initial jEdit configuration. Test run
   system creates it before each new test run. If user's jEdit configuration
   exists in user's home directory <USER_HOME>/.jedit/ it will be saved in
   <USER_HOME>/.jedit.backup/ before the first test run.


4.2 ON RI
~~~~~~~~~

Do it exactly as described in 4.1 above with the following changes in step 2):

2) Configure runtime under test in 'build.properties' file
   by modifying 'test.java.home' property's value providing
   full path to the root dir of RI's JRE (or root dir of RI's JDK);
   Set 'test.vmarg.bootclasspath', 'test.vmarg.ush_prefix' and 
   'test.vmarg.do_not_install_handler' properties' values in
   'build.properties' file to empty as follows:

     test.vmarg.bootclasspath=
     test.vmarg.ush_prefix=
     test.vmarg.do_not_install_handler=

4.3 TEST RUN ARTIFACTS

Any test run produces the following set of additional directories
(not expanded here):

  <INSTALL_DIR>/jedit4.2_test/
       |
       +-- target/
             +-- report/         - Test run reports dir. Contains
             |                     test run reports. Can be
             |                     removed by 'clean.report'
             |                     ant target invocation.
             |
             +-- working/        - Working directory for the tests.
                                   Contains temporary files created
                                   during test run. Removed/created
                                   empty before each new test run.



5. KNOWN ISSUES
---------------

1. At the moment of this archive creation only "launch" test passes on
   Harmony's VMs. Both "functional" and "stress" tests fail after several
   successfull scenario steps because of errors in Harmony's AWT/Swing
   implementation.

2. Because of 1 above only "launch" test can be integrated into CruiseControl
   system. It must be replaced with "functional" one as soon as Harmony's
   AWT/Swing implementation improved.

3. There are patches for Abbot framework in the archive. However they are
   temporary. Corresponding Abbot patch is on the project's web-page already:

    http://sourceforge.net/tracker/index.php?func=detail&aid=1618017&group_id=50939&atid=461492


6. TODO
-------

1. Eliminate Abbot patch (see 3 in chapter 5 above).


7. DISCLAIMER AND LEGAL INFORMATION
------------------------------------

**) Other brands and names are the property of their respective owners.
