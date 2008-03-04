                           ===================================================
                           Run TPTP test using Build Test 2.0
                           ====================================================

---------------
Archive Content
---------------

This archive contains adaptor and custom configuration for running TPTP Eclipse
Profiler test suite under Build Test 2.0

 +/
  |-+adaptors/
  |  |
  |  |-+tptp/                     - TPTP test suite adaptor directory
  |     |
  |     |--adaptor.xml            - The adaptor
  |     |
  |     |--parameters.xml         - Parameters file
  |     |
  |     |--README_tptp_tests.txt  - This readme file
  |
  |-+tests/
     |
     |-+tptp/                     - Scripts which will be used during tests running

------------------------------------------------
TPTP test suite running under BT 2.0
------------------------------------------------
1. Install the TPTP Agent Controller and TPTP tests. To do that follow the
   instructions from this guide:

   http://dev.eclipse.org/viewcvs/index.cgi/test-results/platform/org.eclipse.tptp.ac.testautomation/automation-files/notes/building_tptp_jvmti_tests.txt?root=TPTP_Project&view=co&pathrev=HEAD


2. Perform setup:

   ant -Dtest.suites="classlib,drlvm,tptp" setup


3. Set required parameters in : <BT-infrastructure>/required-parameters.properties

   tptp.agent.controller.home = Location of the Agent Controller binaries
   tptp.tests.build.home = Location of the built TPTP test suite


4. Run the suite on previously built JRE:

     ant -Dtest.suites="classlib,drlvm,tptp" run


5. To run tests under Cruise Control:

   - set up framework properties in <BT-infrastructure>/framework.local.properties

   - run the suite under Cruise Control: 

     ant -Dtest.suites="classlib,drlvm,tptp" run-cc    

The results of tests execution are set to the <BT-infrastructure>/build/results/tptp directory

