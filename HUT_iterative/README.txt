
HUT_iterative directory of Build-Test Intfrastructure
======================================================

This directory contains config and scripts to iteratively run
classlib tests of the Apache Harmony project.
	
To Setup
--------

1) Copy cc.properties.example to cc.properties, and update file with actual values.

2) With Java, Ant and SVN installed, change into the buildtest/trunk
   directory and type 
   
      ant -Dmodule=HUT_iterative setup
      
   This should fetch CruiseControl, set it up with the Apache Harmony 
   configuration, and checkout the software to be built and tested
   from Apache Harmony.
   Also, as part of setup command the external libs for classlib and drlvm modules 
   will be downloaded and these modules will be built first time.

   Note, JAVA_HOME and ANT_HOME environment variable should be correctly set up
   in your environment. Also ANT_OPTS variable should be set up to something like
   set/export ANT_OPTS=-Xmx400M to compile Harmony classes.

   
To kickoff CruiseControl with iterative classlib tests run, just type :
----------------------------------------------------------------------

      ant
      
   in buildtest/trunk and that will launch CC with the test set.
   
 To check status, point your browser to 
 
   http://localhost:8080/

Iterative test run results 
--------------------------
are accumulated in classlib/trunk/build/test_report_${moduleName}_${iteration} 
directories. After test cycle is complete, the results are merged and e-mailed 
to the address predefined in cc.properties file. The FAILURES, ERRORS, ZERO_LENGTH 
statistics are attached. Zip file with test reports, logs and statistics is available:
classlib/trunk/results_drlvm_<timestamp>.zip 
   
FAQ
---
1) Q.: The test runs OK, but no passed/ failed information available. Why?
   A.: This script was tested and works over the sun jdk only. If you run
it over the BEA jdk it will fail.

