                 =======================================================    
                 Iterative runs of class library unit tests under BT 2.0
                 =======================================================    

---------------
Archive Content
---------------

This archive contains adaptor and custom configuration for iterative runs of classlib 
unit tests (HUT) under BT 2.0

 +/
  |-+adaptors/             		- Adaptors connecting test suites to Build Test
  |  |
  |  |-+HUT_iterative/       		- HUT iterative runs adaptor directory
  |     |
  |     |--adaptor.xml     		- The adaptor
  |     |
  |     |--parameters.xml  		- Parameters file
  |     |
  |     |-+cc		   		- Cruise Control custom configurtion directory	
  |        |
  |        |--HUT_iterative-project.xml   - Cruise Control configuration file
  |	   |
  |	   |--publisher.xml		- HUT iterative runs publisher
  |        |
  |        |-+xsl			- xslt files
  |     |
  |     |-+HUT_iterative		- scripts to iteratively run HUT
  |        |
  |        |--HUT_iterative_run.bat
  |        |
  |        |--HUT_iteartive_run.sh
  |        |
  |        |--modulesList		- the list of classlib modules to run tests for
  |
  |-README_HUT_iterative.txt		- Readme File



------------------------------------------------
Integration of HUT iterative runs under BT 2.0
------------------------------------------------

1. Follow general instructions of README.txt for BT 2.0 (pre-intagration or 

   post-integration usage).

2. unzip BT-HUT_iteartive.zip into BT infra directory.

4. Perform setup:

   ant -Dtest.suites="classlib,drlvm" setup

   There is no special setup for iteartive runs of classlib tests, as well as

   there is no special place for HUT_iterative test suite.

4. Set values of <BT infra>/scripts/local.properties.template in generated file:

     framework.local.properties

5. Run the HUT iteartively on previously built JRE:

     ant -Dtest.suites="classlib,drlvm,HUT_iteartive" run

6. Run the suite under Cruise Control: 

     ant -Dtest.suites="classlib,drlvm,HUT_iterative" run-cc    


7. Iterative test run results are accumulated 

   in ${classlib.trunk}/build/test_report_${moduleName}_${iteration} directories. 

   After test cycle is completed, the results are merged and e-mailed 

   to the address predefined in framework.local.properties file, if any.

   The FAILURES, ERRORS, CRASHES statistics are attached. 

   Zip file with test reports, logs and statistics is available:

   ${classlib.trunk}/results_${VMNAME}_<timestamp>.zip 

   The zip archives can be removed:

   ant -Dtest.suites="HUT_iterative" clean

     

----------
Disclaimer
----------

*) Other brands and names are the property of their respective owners.
