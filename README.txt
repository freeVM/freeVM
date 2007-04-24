                           =========================
                           Build Test Infrastructure
                           =========================

---------------
Archive Content
---------------

This archive contains improved and restructured Build Test (BT) Infrastructure.
New BT architecture brings the clear structure on the trunk as well as several
useful means simplifying test suite execution and new test suite integration.

  +/
   |-+scripts/      - BT Framework Implementation
   |
   |-+adaptors/     - Adaptors connecting test suites to BT
   |
   |-+tests/        - Tests suites placed under BuildTest repository
   |
   |-build.xml      - Main Build File
   |
   |-buildtest.dtd  - DTD for Main Build File
   |
   |-README.txt     - Readme File
   |
   |-SPEC.txt       - BT Specification


-------------------------
Pre-requisites for BT Use
-------------------------

To use BT following tools are required to be preinstalled on your system:

  1) JDK version 1.5.0
     http://java.sun.com
     http://www.jrockit.com/

  2) Apache Ant, version 1.6 or higher 
     http://ant.apache.org

  3) C compiler, either gcc for Linux*, or one of the following for Windows*:
         + Microsoft* 32-bit C/C++ Compiler v.7 or higher,
         + Windows* platform SDK,
         + Microsoft* Visual Studio .NET* 2003 or higher
           http://www.microsoft.com/downloads/

  4) Subversion tool (svn)
     http://subversion.tigris.org/


=======================
Using BT Infrastructure
=======================

Assumed BT usage (not actual, but after integration into repository, see next
section for current use) is as follows:

  ---------------------------------------
* Test Execution (post integration vision)
  ----------------------------------------

  To launch some of the integrated test suites user should perform following 
steps:
    
  1. Getting BT Infra
    
      #> svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/trunk
     
     It will download ONLY Main Build file and README's from SVN repository
     Such a trick is needed for traffic and checkout time economy.

  2. Setting Up and getting needed Test Suites:
  
    #> buildtest -Dtest.suites="classlib,drlvm" setup

    It will:
      1. Download BT Framework implementation (./scripts dir from BT trunk) if
         it has not been done yet.
      2. Set Up the Framework
      3. Download adaptors for classlib and drlvm test suites already 
         integrated into BT (if it has not been done yet).
      4. Launch Set Up for selected Test Suites. These Set Ups will download
         (or update) needed test sources for selected Test Suites and prepare
         them for execution.
      5. Extract REQUIRED parameters needed to be specified by used and
         request user to provide the values for them.

  3. User provides REQUIRED values for the build configuration

  4. Execution of selected test suites:

    #> buildtest
      or 
    #> buildtest run

  5. Execution under Cruise Control system:

    #> buildtest cc-run


  ------------------------------------
* Test Execution (pre-integration use)
  ------------------------------------

As it is not under SVN yet, to run the suites user should perform the following
actions:

  1. Setting Up and getting needed Test Suites:
  
    #> buildtest.bat -Dtest.suites="classlib,drlvm" setup

  2. User provides REQUIRED values for the build configuration

  3. Run the suites:

    #> buildtest.bat -Dtest.suites="classlib,drlvm" run
 
     or under CC:

    #> buildtest.bat -Dtest.suites="classlib,drlvm" run-cc


==========================
New Test Suite Integration
==========================

To Integrate your Test Suite (say, 'my-test-suite') into new BT Framework you
should perform the following steps:

   1. Implement Adaptor interface for your Test Suite:
     : implement _setup_, _run_, _clean_ targets in 
          ${root.dir}/my-test-suite/adaptor.xml
     * More on _Adaptors_ see at "SPEC.txt/Test Suite Adaptors" section

   2. Define the Parameters of the Test Suite in 
          ${root.dir}/my-test-suite/parameters.xml 
      file:
       - <required> section - list all properties required to be set up by user
       - <shared> section - list all the values to be shared by the test suite
       - <external> section - list all external dependencies to be downloaded
       - <optional> section - list optional parameters for low-level tuning (not
         supported at this development stage)
       - depends attribute - coma separated list of dependencies on other suites
     * More on _Parameters_ see at "SPEC.txt/Test Suite Parameters" section

   3. Place adaptor.xml and parameters.xml under adaptor/${suite.name}
      directory

   4. If there is a reason to place sources of new Test Suite under BT
      SVN, place them under tests/${suite.name} directory of BT repository and
      use this location as a pointer to the sources.
      -----
      Note: Assumed BT usage does not suppose that all of the BT trunk files
      ----- will be checked out. So adaptor.xml should check out the sources
            by itself.

   5. To run your 'my-test-suite' suite alone:
      1. Set up environment variables in buildtest.bat
      2. If you're using proxy for internet connection, set it up at ANT_OPTS
         variable.
      3. Type:
            #> buildtest.bat -Dtest.suites=my-test-suite setup
         to setup BT Infra.
      4. Specify all of the required parameters in generated
            required-parameters.properties
         file.
      5. Type:
            #> buildtest.bat -Dtest.suites=my-test-suite run
         to execute your suite alone.
      6. Type:
            #> buildtest.bat -Dtest.suites=my-test-suite run-cc
         to launch continuous execution of your test suite.

   6. Do 5. without
        -Dtest.suites=my-test-suite
      parameter to setup and execute all of the installed test suites.


--------------------------
Active Workspace Structure
--------------------------

  +/
   |-+build/
   |  |
   |  |-+cc/        - CC related stuff
   |  |
   |  |-+checkouts/ - externally checkouted test suites (not included in BUILDTEST)
   |  |
   |  |-+lib/       - external libraries needed for fwk and test suites
   |  |
   |  |-+tmp/       - temporary files
   |
   |
   |-+scripts/      - Framework implementation
   |
   |-+adaptors/     - Test Suites adaptors implementations
   |
   |-+tests/        - Tests Suites placed under BUILDTEST and dowlnoaded on 'setup' stage
   |
   |-build.xml      - Main Build file
   |
   |-buildtest.dtd  - DTD for Main Build File
   |
   |-README.txt     - Readme file
   |
   |-SPEC.txt       - BT Specification













