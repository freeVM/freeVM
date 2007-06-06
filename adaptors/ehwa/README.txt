
      =================================================================
          ECLIPSE 3.2.1 HELLO WORLD APPLICATION SCENARIO FOR BT 2.0
      =================================================================

This archive contains configs and scripts for running EHWA scenario
under Build Test Infrastructure 2.0 (HARMONY-3501).

The BT 2.0 provides features which allow:

  - Run EHWA on just built classlib and drlvm
  - Run EHWA in standalone mode (once)
  - Run EHWA in continuous mode using Cruise Control Tool 
  

Please, refer to ./tests/EHWA.txt concerning initial (manual) EHWA scenario description.
Please, refer to ./tests/README.txt concerning EHWA run outside of the Build Test Infrastructure.


CONTENTS
========

 +/
  |-+/adaptors/                        - Adaptors connecting test suites to the Build Test
  |   |
  |   |-+/ehwa/                        - EHWA adaptor directory
  |         |
  |         |--adaptor.xml             - The adaptor used by BT
  |         |
  |         |--parameters.xml          - Parameters file used by BT
  |         |
  |         |--README.txt              - This file
  |  
  |  
  |-+/tests/                           - Test suites connected to Build Test
      |   
      |-+/ehwa/                        - EHWA test scenario directory
           |
           |-- ehwa_eclipse_3.2.1.au3 - Script for running EHWA on Windows           
           |
           |-- ehwa_eclipse_3.2.1.pl  - Script for running EHWA on Linux 
           |
           |-- ehwa_run.xml           - Scenario ant run file; launches proper script        
           |
           |-- EHWA.txt               - Manual scenario description
           |
           |-- README.txt             - Description of EHWA run outside of Build Test Infrastructure               
 
  
PREREQUISITES
=============

Supported platforms are: Windows*/ia32, Windows*/em64t, 
                         Linux/ia32/gtk, Linux/em64t/gtk
                         
EHWA run under Build Test requires the tools and libraries as framework itself,
namely J2SDK* 1.5, Apache Ant 1.6.5, Subversion tool, and etc. 
Please, see BT/REDAME.txt for details.

Note: BT 2.0 only works properly with Sun JDK* 1.5! 

All other external dependencies, such as Eclipse 3.2.1 and xalan.jar 
will be automatically downloaded on the setup phase.
                    
Following tools must be additionally installed for the scenario run:

                 Windows              |                  Linux
 ---------------------------------------------------------------------------------
                 AutoIt3              |         X11 GUITest-0.20  or better  
 http://www.autoitscript.com/autoit3  | http://sourceforge.net/projects/x11guitest
                                      |                                         

NOTE:

1) On Windows* AutoIt3 home should be added to the PATH before the EHWA scenario run 

2) The problems may exist with X11 GUITest installation on Linux/em64t.
   It is recommended to use X11 GUITest-0.20 version.
   The following steps are standard installation: 
       1) perl Makefile.PL
       2) make
       3) make test 
       4) make install
       (or set PERL5LIB env.var to $AUTOIT_HOME/blib/lib:$AUTOIT_HOME/blib/arch)
   But for Linux/em64t you may need to correct path to X11 libs 
   in Makefile.PL before the step 1):
   
   'LIBS'  => ['-L/usr/X11R6/lib -L/usr/X/lib -lX11 -lXtst -lXext'], ->
   'LIBS'  => ['-L/usr/X11R6/lib64 -L/usr/X/lib -lX11 -lXtst -lXext'], 
  
   Also, please, do the following change in Makefile generated after the step 1), if needed:
   
   LIBC = /lib64/libc.so.6 ->
   LIBC = /lib64//lib64/libc.so.6


EHWA SCENARIO RUN
==================

The following steps should be made to setup EHWA:

  1. Check out Build Test 2.0 installer from SVN

          svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/trunk

      NOTE: If HARMONY-3501 is not applied yet, please, 
            download and unpack attached archive to appropriate <BT_DIR>
            
  2. Follow the instructions from <BT_DIR>/README.txt to satisfy general BT
     pre-requisites (install necessary software, specify environment variables,
     specify SVN proxy settings if necessary).

     NOTE: This partly means update <BT_DIR>/buildtest.* script with your personal settings
           If you don't have buildtest.bat/buildtest.sh set JAVA_HOME, ANT_HOME, SVN_HOME, 
           add corresponding bins to the PATH and call 'ant' (everywhere below) instead of buildtest.
           
  3. Unpack this archive to the <BT_DIR> directory

  4. Setup Build Test 2.0 and required suites with the following command, run from <BT_DIR>:

         buildtest -Dtest.suites="classlib,drlvm,ehwa" setup

     NOTE: If you don't won't to use the latest built Harmony JRE as tested JRE you may simply run 
    
         buildtest -Dtest.suites=ehwa setup


The following steps should be made to run EHWA on the tested JRE:

  5. If needed, correct values of required parameters in <BT_DIR>/required-parameters.properties 
     file, generated after the setup step. Required parameter for the EHWA run is JRE under test.
     By default it is set to the last built Harmony JRE.
     
  6. If needed, set values of the optional parameters in <BT_DIR>/framework.local.properties file.
     
     If you want to pass some options to the tested JRE use the following property:
    
     ehwa.parameters.optional.tested.jre.options=<Tested JRE options>
     - sets JRE options for eclipse launching, default is "-showversion"
     
     Cruise Control Web Port and Port for JMX console can be specified by the following properties:
         framework.parameters.cc.jmxport
         framework.parameters.cc.webport
     Please, see BT Readmes for more information.
    
  7. Run EHWA on the previously built Harmony JRE the with command:

         buildtest -Dtest.suites="classlib,drlvm,ehwa" run

     Or run the EHWA in continuous mode under Cruise Control with command:

         buildtest -Dtest.suites="classlib,drlvm,ehwa" run-cc

  8. Run EHWA on explicitely defined JRE with command:
     Use <BT_DIR>/required-parameters.properties file to define tested JRE 

           ant -Dtest.suites=ehwa run
     Or
           ant -Dtest.suites=ehwa run-cc


NOTE: Do not any movements on the screen during EHWA run, this may cause the scenario fail! 
      It is better to use a remote machine for running EHWA scenario.
  
      
To check task status, if run EHWA under Cruise Control, 
point your browser to  http://localhost:< framework.parameters.cc.jmxport>/ 
- default is http://localhost:8080/


NOTE: If you run EHWA under Cruise Control along with classlib and drlvm task, 
      EHWA will be run every time when some changes occurred in classlib or drlvm

      If you run EHWA under Cruise Control on some explicitely defined JRE,
      you should do force build from http://localhost:8080/ page to start EHWA execution.


Please, see EHWA run results in <BT_DIR>/cc/build/results/ehwa
Results are stored in the timestamp dirs under <BT_DIR>/cc/build/results/ehwa dir.
Results contains EHWA log and may contain Eclipse launching log, 
Eclipse workspace log and picture of the screen if an error has occurred.
Logs will be also sent to the email indicated in <BT_DIR>/framework.local.properties file

For more information about Build Test configuration, please, 
see <BT_DIR>/README.txt and <BT_DIR>/SPEC.txt 


RUN EHWA OUTSIDE OF BUILD TEST
==============================

To run the scenario outside of Build Test Infrastructure you only need to have data 
under ./tests/ehwa dir. For more information, please, refer to ./tests/ehwa/README.txt. 


DISCLAIMER AND LEGAL INFORMATION
================================

*) Other brands and names are the property of their respective owners.








