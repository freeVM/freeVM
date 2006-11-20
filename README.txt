
Build-Test Intfrastructure
==========================

This is the build/test infrastructure to help continually test
the Apache Harmony project.

Prereguisites 
-------------
Basically, you need the tools needed to build the Apache Harmony 
components, like Java, Ant, C/C++ compilers, etc.  Please see
the other parts of the project for information on the necessary 
toolchains.  You need the following as the bare minimum to get 
CI up and working.

1) Java JDK v5

2) Apache Ant : http://ant.apache.org/  Please get the 
    latest release (1.6 or better) and install
	as per project intstructions.

3) Subversion client : http://subversion.tigris.org
	
To Setup
--------

1) Get the /harmony/enhanced/buildtest/ part of the Apache
   Harmony project.  If you are reading this, you either have
   it via a zip or tgz, or have checked it out from the project
   Subversion repository.  In the event that you don't have it
   you can get it via subversion : 
   
     svn co https://svn.apache.org/repos/asf/harmony/enhanced/buildtest/trunk
     
2) Copy cc.properties.example to cc.properties, and update file with actual values.

3) With Java, Ant and SVN installed, change into the buildtest/trunk
   directory and type 
   
      ant setup
      
   This should fetch CruiseControl, set it up with the Apache Harmony 
   configuration, and checkout the software to be built and tested
   from Apache Harmony.
   Also, as part of setup command the external libs for classlib and drlvm modules 
   will be downloaded and these modules will be built first time.

   Note, JAVA_HOME and ANT_HOME environment variable should be correctly set up
   in your environment. Also ANT_OPTS variable should be set up to something like
   set/export ANT_OPTS=-Xmx400M to compile Harmony classes.

   
To kickoff CruiseControl, just type :

      ant
      
   in buildtest/trunk and that will launch CC with the test set.
   
 To check status, point your browser to 
 
   http://localhost:8080/
   

 To Do
 -----
 
 1) Integrate JAPI tool
 
 2) Integrate X tool

FAQ
---
1) Q.: The classlib project build/ test runs failed with the message like:
"No supported regular expression matcher found". Why?
   A.: Update cruisecontrol.bat (or cruisecontrol.sh) file to add the 
ant-apache-regexp.jar archives from ant/lib directory to the 'CRUISE_PATH'.

2) Q.: The classes can't be compiled under CC. Why?
   A.: Usually, no compiler available in the class path. Please, add the 
ecj_3.2.jar archive to the 'CRUISE_PATH' variable.

3) Q.: The test runs OK, but no passed/ failed information available. Why?
   A.: This script was tested and works over the sun jdk only. If you run
it over the BEA jdk it will fail.

4) Q.: The results of CC are not emailed. Why?
   A.: uncomment the string "REM set CC_OPTS=-Xms128m -Xmx256m" and update
it as "set CC_OPTS=-Xms128m -Xmx600m"

5) Q.: The size of emailed notification is too big. How it can be reduced?
   A.: To reduce the emailed notification size please commented out the string
69-77 in the file <root>/cc/webapps/cruisecontrol/xsl/errors.xsl. In this case
the email will not include warning messages (actually we have a lot of them).

