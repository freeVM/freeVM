
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
   Note, to run it over em64t platform the 64bit JDK should be installed.

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

   Note also, the MSVC environment also should be correctly set up on Windows box
   or command should be run from MSVC command prompt.

   
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
1) Q.: The test runs OK, but no passed/ failed information available. Why?
   A.: This script was tested and works over the sun jdk only. If you run
it over the BEA jdk it will fail.
