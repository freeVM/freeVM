
      =================================================================
                 ECLIPSE LONG-RUNNING SCENARIO FOR BT 2.0
      =================================================================


This archive contains the contribution to the Apache Harmony project from Intel.
The contribution consists from the following component: 
    
    - Eclipse 3.2.1 Geronimo Application (EGA) automated GUI test scenario

The purpose of this test scenario is to make sure that JRE implementation
under test (Harmony primarily) is able to run Eclipse-3.2.1 long enough
to allow a user perform some development work in Eclipse-3.2.1. 

Scenario automation is based on AutoIt3 tool (http://www.autoitscript.com/autoit3) on Windows* 
and X11 GUITest tool (http://sourceforge.net/projects/x11guitest) on Linux.
Geronimo 1.2-beta sources are used as the developing project.

Archive also contains configs and scripts for running EGA scenario
under Build Test Infrastructure 2.0 (HARMONY-3501).

The BT 2.0 provides features which allow:

  - Run EGA on just built Harmony
  - Run EGA in standalone mode (once)
  - Run EGA in continuous mode using Cruise Control Tool 

Please, refer to BT README.txt concerning detailed BuildTest infrastructure description.  
Please, refer to EGA.txt concerning initial (manual) EGA scenario description.


CONTENTS
========

 +/
  |-+/adaptors/                       - Adaptors connecting test suites to the Build Test
  |   |
  |   |-+/ega/                        - EGA adaptor directory
  |         |
  |         |--adaptor.xml            - The adaptor used by BT
  |         |
  |         |--parameters.xml         - Parameters file used by BT
  |  
  |  
  |-+/tests/                          - Test suites connected to Build Test
      |   
      |-+/ega/                        - EGA test scenario directory
            |
            |-- build.xml             - Build file used for EGA setup/run 
            |
            |-- ega_eclipse_3.2.1.au3 - Script for running EGA on Windows*           
            |
            |-- ega_eclipse_3.2.1.pl  - Script for running EGA on Linux 
            |
            |-- ega.properties        - Properties file used for running EGA  
            |                           outside of BT infrastructure        
            |
            |-- EGA.txt               - Manual scenario description
            |
            |-- test.java             - Small class for checking tested JRE options
            |                           passed to the scenario
            |
            |-- README.txt            - This file
  
  
PREREQUISITES
=============

Supported platforms are: Windows*/ia32, Windows*/em64t, 
                         Linux/ia32/gtk, Linux/em64t/gtk

The following tools are needed for standalone EGA scenario setup and run:

1) J2SE 1.5 JDK

2) Apache Ant 1.6.5 or better (http://ant.apache.org)  
   
3) GUI automated testing tool:
             
                   Windows*             |                  Linux
   ---------------------------------------------------------------------------------
                   AutoIt3              |         X11 GUITest-0.20  
   http://www.autoitscript.com/autoit3  | http://sourceforge.net/projects/x11guitest
                                        |       
                                                                                                              
4) Eclipse-3.2.1 (http://www.eclipse.org)
   Installed automatically during setup step

5) Maven 2.0.4 or higher (http://maven.apache.org)
   Installed automatically during setup step
       
6) Web browser (Internet Explorer* on Windows* and Mozilla on Linux are recommended)
                      
EGA run under Build Test may require additional tools and libraries for the framework 
itself or for the projects enqueued to run along with ega.
Please, see BT Readmes for details.

NOTE:

1) On Windows* AutoIt3 home should be added to the PATH before the EGA scenario run 

2) The problems may exist with X11 GUITest installation on Linux/em64t.
   It is recommended to use X11 GUITest-0.20 version.
   The following steps are standard installation: 
       1) perl Makefile.PL
       2) make
       3) make test 
       4) make install
   But for Linux/em64t you may need to correct path to X11 libs 
   in Makefile.PL before the step 1):
   
   'LIBS'  => ['-L/usr/X11R6/lib -L/usr/X/lib -lX11 -lXtst -lXext'], ->
   'LIBS'  => ['-L/usr/X11R6/lib64 -L/usr/X/lib -lX11 -lXtst -lXext'], 
  
   Also, please, do the following change in Makefile generated after the step 1), if needed:
   
   LIBC = /lib64/libc.so.6 ->
   LIBC = /lib64//lib64/libc.so.6


RUN EGA SCENARIO UNDER BUILD TEST
=================================

The following steps should be made to setup EGA:

  1. Check out Build Test 2.0 installer from SVN

       svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/trunk

     NOTE: If HARMONY-3501 is not applied yet, please, 
           download and unpack attached archive to appropriate <BT_DIR>
            
  2. Follow the instructions from <BT_DIR>/README.txt to satisfy general BT
     pre-requisites (install necessary software, specify environment variables,
     specify SVN proxy settings if necessary).

     NOTE: This partly means update <BT_DIR>/buildtest.* script with your personal settings
           If you don't have buildtest.bat/buildtest.sh, set JAVA_HOME, ANT_HOME, SVN_HOME, 
           add corresponding bins to the PATH and call 'ant' (everywhere below) instead of buildtest.
           
  3. Unpack this archive to the <BT_DIR> directory

  4. Setup Build Test 2.0 and required suites with the following command, run from <BT_DIR>:

         buildtest -Dtest.suites="hdk,ega" setup

     NOTE: If you don't won't to use the latest built Harmony as tested JRE you may simply run 
    
         buildtest -Dtest.suites=ega setup
     
  During setup step eclipse-3.2.1, maven-2.0.4 and geronimo-1.2-beta sources
  archives will be downloaded and unpacked to special dirs under BT infrastructure.
  Also full geronimo-1.2-beta build will be prepared, which requires
  additional source download into maven repository.

  NOTE: Size of downloaded software is about 220M.

The following steps should be made to run EGA on the tested JRE:

  5. If needed, correct values of required parameters in <BT_DIR>/required-parameters.properties 
     file, generated after the setup step. Required parameter for the EGA run is JRE under test.
     By default it is set to the last built Harmony HDK.
     
  6. If needed, set values of the optional parameters in <BT_DIR>/framework.local.properties file.
     
     If you want to pass some options to the tested JRE use the following properties:
    
       ega.parameters.optional.tested.jre.options=<Tested JRE options>
           - Sets JRE options for eclipse launching, default is "-showversion -Xms512M -Xmx1024M".
     
       ega.parameters.optional.tested.jre.options.for.debug=<Tested JRE options> 
           - Sets JRE options for the scenario debug step, default is empty string.
             See tests/ega/EGA.txt concerning debug step description.
     
     To configure scenario run, use the following options:
     
       ega.parameters.optional.iteration.num=<N>
           - Sets th number of iterations for the scenario to pass, default value is 0.

       ega.parameters.optional.run.time=<N>         
           - Sets time in hours for the scenario to pass, default value is 48.
             Ignored if used along with non-zero iteration.num.                 

       ega.parameters.optional.delay.factor=<N>     
           - Sets factor used in time delays inside the scenario, default value is 2.

       ega.parameters.optional.kill.eclipse=<0|1>    
           - Define kill Eclipse or not if the scenario fails, default value is 1.
             0 - not kill, 1 - kill 
                 
     If you want to receive email notification with EGA run results use the properties listed below.
     NOTE: This feature is applied only if run EGA under Cruise Control
     
       framework.parameters.usemail=<smtp server name>
           - Sets smtp server name, default is framework.parameters.usemail if specified.
     
       framework.parameters.usemail.to=<some email address list>
           - Recipients list, default is framework.parameters.usemail.to if specified.
      
       framework.parameters.usemail.from=<some email address>
           - Sender, default is framework.parameters.usemail.from if specified.
   
       framework.parameters.usemail.always=<true/false>
           - Set to false if you want to receive mail notification only on EGA status change.
             Default is false.
     
     Similarly you may set in framework.local.properties any value of ega optional parameters 
     indicated in adapters/ega/parameters.xml inside optional tag. 
     
     Cruise Control Web Port and Port for JMX console can be specified by the following properties:
     (please, see BT Readmes for more information)
     
       framework.parameters.cc.jmxport=<PortNum> - default is 8000
       framework.parameters.cc.webport=<PortNum> - default is 8080
       
    
  7. Run EGA on the previously built Harmony HDK with the command:

         buildtest -Dtest.suites="hdk,ega" run

     Or run the EGA in continuous mode under Cruise Control with command:

         buildtest -Dtest.suites="hdk,ega" run-cc

  8. Run EGA on explicitly defined JRE with command:
     (use <BT_DIR>/required-parameters.properties file to define tested JRE)

           ant -Dtest.suites=ega run
     Or
           ant -Dtest.suites=ega run-cc


NOTE: Do not any movements on the screen during EGA run, this may causes the scenario fail! 
      It is better to use a remote machine for running EGA scenario.
  
      
To check task status, if run EGA under Cruise Control, 
point your browser to http://localhost:<framework.parameters.cc.jmxport>/ 
Default is http://localhost:8080/


NOTE: If you run EGA under Cruise Control along with hdk task, 
      EGA will be run every time when some changes occurred in hdk modules

      If you run EGA under Cruise Control on some explicitly defined JRE,
      you should do force build from http://localhost:8080/ page to start EGA execution.

Please, see EGA run results in <BT_DIR>/cc/build/results/ega
Results are stored in the timestamp dirs under <BT_DIR>/cc/build/results/ega dir.
Results contains EGA log and may contain Eclipse launching log, 
Eclipse workspace log and picture of the screen if an error has occurred.
Logs will be also sent to the email indicated in <BT_DIR>/framework.local.properties file

For more information about Build Test configuration, please, 
see <BT_DIR>/README.txt and <BT_DIR>/SPEC.txt 


RUN EGA OUTSIDE OF BUILD TEST
==============================

If you don't won’t to run EGA under BT you just need ./test directory data
from this archive. Unpack it for any convenient <EXTRACT_DIR> and
do the following steps for EGA setup/run.

 1) Before the EGA setup update ega.properties file with the actual values if needed. 
    For the build step you may need to update Eclipse 3.2.1 download address, 
    Maven download address and version, Geronimo 1.2-beta download address.

 2) Set JAVA_HOME to JDK 1.5

 3) To setup EGA scenario call the following command from <EXTRACT_DIR>:

      ant [setup] [-Dhttp.proxyHost=<host>] [-Dhttp.proxyPort=<port>]
    
    The following data must appear under <EXTRACT_DIR> directory after setup:

    <EXTRACT_DIR>
       |
       +-- /eclipse-3.2.1/            -  Eclipse 3.2.1 home dir
       |
       +-- /maven-2.0.4/              -  Maven home dir
       |
       +-- /project/                  -  Eclipse test project
       |     |
       |     +-- /.m2/                -  Maven repository location
       |     |
       |     +-- /geronimo-1.2-beta/  -  Geronimo sources
       |
       +-- eclipse-SDK-3.2.1-win32.*  -  Eclipse archive
       |
       +-- geronimo-1.2-beta-src.*    -  Geronimo sources archive
       |
       +-- maven-2.0.4-bin.*          -  Maven archive

 4) Update 'EGA scenario run configuration' properties in ega.properties 
    file with the personal settings (or you may pass it from command line).
    Default values will be used for optional properties if they are not set.
   
    EGA scenario uses the following eight properties during run step:
      1. tested.jre   - JRE under test (required) 
      2. eclipse.home - Eclipse 3.2.1 home directory, default value is ./eclipse-3.2.1-os-arch
      3. iteration.num                
      4. run.time                    
      5. delay.factor                
      6. kill.eclipse                                   
      7. tested.jre.options                           
      8. tested.jre.options.for.debug 
      
    These properties were already discussed above in 'Run EGA Scenario under BT' section.
    You may find properties definitions and defaults there or in the ega.properties file.
    
 5) Run EGA from <EXTRACT_DIR> as follows:
   
       ant run 
           
    Options for the scenario run may be also passed from command line:
     
       ant run  -Dtested.jre=<tested_jre_home> \
                 [-Declipse.home=<eclipse_home>] \
                 [-Diteration.num=<N>] \
                 [-Drun.time=<N>] \
                 [-Ddelay.factor=<N>] \
                 [-Dkill.eclipse ={0,1}] \
                 [-Dvm.options="<opt1 opt2 ...>"] \
                 [-Dvm.debug.options="<opt1 opt2 ...>"]

 The following data must appear under <EXTRACT_DIR> directory after the run:

 <EXTRACT_DIR>
       |
       +-- /workspace/               -  Eclipse 3.2.1 workspace dir
       |
       +-- /results/                 -  EGA run results
            |
            +-- /<date_time>/        
                  |
                  +-- EGA.log        -  Scenario log
                  |
                  +-- eclipse.log    -  Eclipse log
                  |
                  +-- workspace.log  -  Eclipse .log file copied from workspace\.metadata
                  |
                  +-- error.*        -  Screen snapshot (present if EGA run fails) 
            
      
 NOTE: Use only 'clean' Eclipse, which isn't bound to any working projects, 
       for running EGA scenario (<EXTRACT_DIR>/eclipse-3.2.1/ is provided), 
       because Eclipse configuration will be cleaned during the scenario run!


KNOWN ISSUES
============

    1) Using long paths may cause Geronimo build to behaves unexpectedly 
       when it hits the 260 char limit for filenames on Windows*.
       Please, place <BT_DIR> or <EXTRACT_DIR> as close as possible 
       to disk root to avoid Geronimo build crash during EGA setup.

    2) If any problems appear building Geronimo schemas it is 
       strongly recommended to build the scenario using Sun JDK* 1.5.
       
    3) If the scenario often fails to observe some window, 
       but no configuration or JRE errors is visible, 
       it is recommended to increase delay.factor.
     
    4) If scenario fails during Help step it's recommended
       to set Internet Explorer* on Windows* and Mozilla on Linux
       as default browsers.
     
    5) If the scenario fails to observe some menu on Windows*,
       it is recommended to uncheck 'Hide underlined letters 
       for keyboard navigation until I press Alt key' in
       Display properties -> Appearance -> Effects.
    
    6) If 'Cannot connect to VM' window appears during scenario 
       debug step on Harmony Linux em64t build this most probably means 
       that Harmony can work only in interpreter mode during debug.
       Please, use tested.jre.options.for.debug=-Xint to fix the problem. 
  
    7) Note that Harmony built from classlib&drlvm is not enough 
       to run EGA scenario because it needs JDWP libraries,
       so Harmony HDK build may be used for EGA run.
 
    8) If any unexpected problems appear during scenario contiguous run
       under Cruise Control, it is strongly recommended to run BT framework 
       using Sun JDK* 1.5.
      
    9) If use Remote Desktop Connection for the scenario run you should not 
       minimize remote desktop window. Also, please, turn off screen saver. 
       These are needed for correct work of GUI automation tools.
           
           
DISCLAIMER AND LEGAL INFORMATION
================================

*) Other brands and names are the property of their respective owners.








