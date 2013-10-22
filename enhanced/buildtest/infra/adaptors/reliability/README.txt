                 ============================================
                 Reliability test suite automation for BT 2.0
                 ============================================

---------------
Archive Content
---------------

This archive contains adaptor and custom configuration for running Reliability
test suite under BT 2.0

 +/
  |-+adaptors/                  - Adaptors connecting test suites to Build Test
     |
     |-+reliability/            - Reliability test suite adaptor directory
        |
        |--adaptor.xml          - The adaptor
        |
        |--parameters.xml       - Parameters file
        |
        |--README.txt           - This readme File



---------------------------------------
Reliability test suite run under BT 2.0
---------------------------------------

1. Check out Build Test from SVN:

     svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/branches/2.0

2. Install Build Test:

     ant install

3. Perform setup:

     ant -Dtest.suites="classlib,drlvm,reliability" setup

4. Set values of framework properties in generated file:

     framework.local.properties

5. Run the suite on previously built JRE:

     ant -Dtest.suites="classlib,drlvm,reliability" run

6. Run the suite under Cruise Control: 

     ant -Dtest.suites="classlib,drlvm,reliability" run-cc    


----------
Disclaimer
----------

*) Other brands and names are the property of their respective owners.
