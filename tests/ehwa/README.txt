       =================================================================
             Eclipse 3.2.1 Hello World Application (EHWA) Scenario
       =================================================================

This directory contains scripts to automatically run EHWA scenario on the tested JRE.

Main purpose of this test scenario is to make sure that JRE implementation
under test is able to run simple Hello World Application in Eclipse.

Scenario automation is based on AutoIt3 tool on Windows* and X11 GUITest tool on Linux.

This README file contains short description how to run EHWA in standalone mode.
Please, refer to EHWA.txt concerning initial (manual) EHWA scenario description.


PREREQUISITES
=============

Supported platforms are: Windows*/ia32, Windows*/em64t, 
                         Linux/ia32/gtk, Linux/em64t/gtk
                         
The following tools are needed for EHWA scenario run:

1) J2SDK* 1.5.0 (http://java.sun.com, http://www.jrockit.com)

2) Apache Ant 1.6.5 or better (http://ant.apache.org) 

3) Eclipse SDK 3.2.1 (http://www.eclipse.org)

4)                 Windows              |                  Linux
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
   But for Linux/em64t you may need to correct path to X11 libs 
   in Makefile.PL before the step 1):
   
   'LIBS'  => ['-L/usr/X11R6/lib -L/usr/X/lib -lX11 -lXtst -lXext'], ->
   'LIBS'  => ['-L/usr/X11R6/lib64 -L/usr/X/lib -lX11 -lXtst -lXext'], 
  
   Also, please, do the following change in Makefile generated after the step 1), if needed:
   
   LIBC = /lib64/libc.so.6 ->
   LIBC = /lib64//lib64/libc.so.6



CONTENTS
========

Here and below <INSTALL_DIR> is the directory containing EHWA scripts.

<INSTALL_DIR>
       |
       +-- ehwa_eclipse_3.2.1.au3  -  Script for running EHWA on Windows           
       |
       +-- ehwa_eclipse_3.2.1.pl   -  Script for running EHWA on Linux 
       |
       +-- ehwa_run.xml            -  Scenario ant run file; launches proper script        
       |
       +-- EHWA.txt                -  Manual scenario description
       |
       +-- README.txt              -  This file         
           


EHWA SCENARIO RUN
=================

To run EHWA on Eclipse 3.2.1 scenario, please, do the following:

1) Set up required tools: Ant, AutoIt3/X11 GUITest, JDK, Eclipse 3.2.1.

2) Change directory to <INSTALL_DIR>

3) Run ehwa_run.xml as follows:
   
   ant -buildfile ehwa_run.xml -Declipse.home=<Eclipse-3.2.1 Home> \
                               -Dtested.jre=<Path to JRE under test> \
                               [-Dtested.jre.options=<Tested JRE options>] \
                               [-Dresult.dir=<Dir for storing EHWA run results>]
 
Where tested.jre.options property is empty by default and
result.dir is <INSTALL_DIR> by default
                                  
NOTE: Use only 'clean' Eclipse, which isn't bound to any working projects, 
      for running EHWA scenario, because Eclipse config will be cleaned 
      during the scenario run!

NOTE: Do not any movements on the screen during EHWA run,
      this may cause the scenario fail. 
      It is better to use a remote machine for running EHWA scenario.
  
 
Please, see EHWA run results in result.dir.
Results are stored in the timestamps dir under result.dir.
It contains EHWA log and may contain Eclipse launching log, 
Eclipse workspace log and picture of the screen if an error has occurred.


DISCLAIMER AND LEGAL INFORMATION
================================

*) Other brands and names are the property of their respective owners.





