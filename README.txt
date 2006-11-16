
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
   
     svn co https://svn.apache.org/repos/asf/incubator/harmony/enhanced/buildtest/trunk
     
2) Copy cc.properties.example to cc.properties, and update file with actual values.

3) With Java, Ant and SVN installed, change into the buildtest/trunk
   directory and type 
   
      ant setup
      
   This should fetch CruiseControl, set it up with the Apache Harmony 
   configuration, and checkout the software to be built and tested
   from Apache Harmony.
   Also, as part of setup command the external libs for classlib and drlvm modules 
   will be downloaded and these modules will be built first time.

   
To kickoff CruiseControl, just type :

      ant
      
   in buildtest/trunk and that will launch CC with the full test set.
   
 To check status, point your browser to 
 
   http://localhost:8080/
   
   
 To Do
 -----
 
 1) Setup defaultcc.properties and personal buildcc.properties
 
 2) Integrate JAPI tool
 
 3) Integrate X tool

