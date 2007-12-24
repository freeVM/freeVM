#!/usr/bin/perl 

## ===================================================================
##     Licensed to the Apache Software Foundation (ASF) under one
##     or more contributor license agreements.  See the NOTICE file
##     distributed with this work for additional information
##     regarding copyright ownership.  The ASF licenses this file
##     to you under the Apache License, Version 2.0 (the
##     "License"); you may not use this file except in compliance
##     with the License.  You may obtain a copy of the License at
##
##         http://www.apache.org/licenses/LICENSE-2.0
##
##     Unless required by applicable law or agreed to in writing,
##     software distributed under the License is distributed on an
##     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
##     KIND, either express or implied.  See the License for the
##     specific language governing permissions and limitations 
##     under the License.    
## ===================================================================

use X11::GUITest qw/

    FindWindowLike
    
    GetWindowName

    SetInputFocus
    GetInputFocus

    WaitWindowViewable
    WaitWindowClose

    SendKeys
    SetKeySendDelay

    PressKey
    ReleaseKey

    StartApp
    RunApp
/;

 # Set option
 SetKeySendDelay(200);

 # Current directory
 $curdir = `pwd`;
 $curdir =~ s/\n//;

 # Set variables
 if ((scalar(@ARGV) == 9) || (scalar(@ARGV) == 10)) {
    $eclipse_home = $ARGV[0];
    $java_home = $ARGV[1];
    $resultsDir = $ARGV[2];
    $os_arch = $ARGV[3];
    $iter_num = $ARGV[4];
    $time_frame = $ARGV[5];
    $delay_factor = $ARGV[6];
    $eclipse_kill = $ARGV[7];
    $vm_options = $ARGV[8];
    $vm_debug_options = "";
    if (-e -d $resultsDir) { 
            die "ERROR: Result dir = $resultsDir doesn't exist!";
    }
    LogWrite("\n--- Automated EGA scenario for Eclipse 3.2.1 --- \n\n");
    LogWrite("\nEclipse Home = $eclipse_home\n");
    LogWrite("Tested JRE Home = $java_home\n");
    LogWrite("OS_ARCH = $os_arch\n\n");
    LogWrite("Tested JRE options for Eclipse launching = $vm_options\n");
    if (scalar(@ARGV) == 10) {
        $vm_debug_options = $ARGV[9];
        LogWrite("Tested JRE options for debug step = $vm_debug_options\n");
    }
    LogWrite("\n");
    if ($iter_num < 0) {
        LogWrite("\nERROR: Iteration number < 0! \n\nEGA FAILED!\n");
        die "ERROR: Iteration number < 0!";
    }
    if (($iter_num == 0) && ($time_frame <= 0)) {
        LogWrite("\nERROR: Time frame for the scenario run <= 0! \n\nEGA FAILED!\n");
        die "ERROR: Time frame for the scenario run <= 0!";
    }
        if ($delay_factor < 1) {
        LogWrite("\nERROR: Delay factor < 1! \n\nEGA FAILED!\n");
        die "ERROR: Delay factor < 1!";
    }
    if (($eclipse_kill != 0) && ($eclipse_kill != 1)) {
        LogWrite("\nERROR: Wrong 'kill Eclipse' flag value! \n\nEGA FAILED!\n");
        die "ERROR: Wrong 'kill Eclipse' flag value!";
    }
    if ($iter_num > 0) { LogWrite("Iteration number = $iter_num \n"); }
    if (($iter_num == 0) && ($time_frame > 0)) { 
        LogWrite("Time frame for the scenario run = $time_frame hours \n"); 
    }
    if ($delay_factor >= 1) { LogWrite("Delay factor = $delay_factor \n"); }
    LogWrite("Kill Eclipse if EGA scenario fails = $eclipse_kill \n");
 } else {
    die "ERROR: Wrong number of input parameters!";
 }

 # Check if some Eclipse window is already olpened
 my ($eclipse_win) = FindWindowLike('Eclipse SDK');
 if (($eclipse_win)) {
    LogWrite("\nERROR: Some Eclipse window is already opened! \n\n EGA FAILED!\n");
    die "ERROR: Some Eclipse window is already opened!"
 }

 LogWrite("Start: \n");

 LogWrite("\tRun Eclipse \n");
 $result = StartApp("$java_home/bin/java $vm_options -cp \"$eclipse_home/startup.jar\" -Dosgi.install.area=\"$eclipse_home\" org.eclipse.core.launcher.Main -ws gtk -os linux -arch $os_arch -debug > $resultsDir/eclipse.log 2\>\&1");

 if ($result == 0) {    
     LogWrite("\nERROR: Can't start Eclipse! \n\n EGA FAILED!\n");
     die "ERROR: Can't start Eclipse!"
 }

 # Select default workspace
 LogWrite("\tSelect workspace \n");
 my $winID = WinWaitImpl('Workspace Launcher');
 LogWrite("\tSelect ".$curdir."/workspace as workspace \n");
 WinSendKeys($curdir.'/workspace', 'Workspace Launcher');
 LogWrite("\tPress 'OK' button \n");
 WinSendKeys('{ENT}', 'Workspace Launcher');
 WinWaitCloseImpl($winID);

 # Prepare Eclipse environment
 LogWrite("\nPrepare Eclipse environment: \n");

 # Close Welcome page
 LogWrite("\tClose Welcome page \n");
 $winID = WinWaitImpl('Java - Eclipse SDK');
 WinSendKeys('%({+}{-})', 'Java - Eclipse SDK');
 SendKeys('c');
 Sleep(3000);

 # Uncheck Project->Build Automatically
 LogWrite("\tUncheck Project->Build Automatically \n");
 WinSendKeys('%(p)m', 'Java - Eclipse SDK');
 Sleep(3000);

 # Set M2_REPO variable
 LogWrite("\tSelect Window->Preferences \n");
 WinSendKeys('%(w)p', 'Java - Eclipse SDK');
 $winID = WinWaitImpl('Preferences');
 LogWrite("\tSelect Java->Build Path->Classpath Variables \n");
 Sleep(3000);
 WinSendKeys('{ENT}{DOW 4}', 'Preferences');
 PressKey('{NUM}'); 
 WinSendKeys('{+}{DOW 2}{+}{DOW}', 'Preferences');
 ReleaseKey('{NUM}');
 Sleep(10000); 
 LogWrite("\tCreate M2_REPO variable \n");
 WinSendKeys('%(n)', 'Preferences');
 $winID2 = WinWaitImpl('New Variable Entry');
 Sleep(3000);
 WinSendKeys('%(n)', 'New Variable Entry');
 WinSendKeys('M2_REPO', 'New Variable Entry');
 LogWrite("\tSet M2_REPO value to ".$curdir."/project/.m2/repository \n");
 WinSendKeys('%(p)', 'New Variable Entry');
 WinSendKeys($curdir.'/project/.m2/repository', 'New Variable Entry');
 Sleep(3000);
 LogWrite("\tPress OK button\n");
 WinSendKeys('{ENT}', 'New Variable Entry');
 WinWaitCloseImpl($winID2);
 LogWrite("\tPress OK button to apply new preferences \n");
 Sleep(3000);
 WinSendKeys('{TAB 3}{ENT}', 'Preferences');
 WinWaitCloseImpl($winID);
 Sleep(3000);

 # Import geronimo modules
 WinWaitImpl('Java - Eclipse SDK');
 LogWrite("\nImport geronimo modules into workspace: \n");
 LogWrite("\tSelect File->Import... \n");
 WinSendKeys('%(f)i', 'Java - Eclipse SDK');
 $winID = WinWaitImpl('Import');
 Sleep(3000);
 LogWrite("\tSelect import existing project \n");
 WinSendKeys('{ENT}', 'Import');  
 Sleep(3000);
 PressKey('{NUM}'); 
 Sleep(3000);
 WinSendKeys('{+}', 'Import'); 
 Sleep(3000);
 ReleaseKey('{NUM}');
 Sleep(3000);
 WinSendKeys('{DOW 3}{ENT}', 'Import');
 Sleep(3000);
 LogWrite("\tType path to search projects: ".$curdir."/project/geronimo-1.2-beta/modules \n");
 WinSendKeys('%(t)', 'Import');
 WinSendKeys($curdir.'/project/geronimo-1.2-beta/modules', 'Import');
 Sleep(3000);
 WinSendKeys('%(e)', 'Import');
 Sleep(10000); # Searching for projects
 WinSendKeys("%(p)", 'Import');
 Sleep(3000);
 LogWrite("\tUncheck all builders \n");
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-activemq-gbean
 WinSendKeys("{DOW 3}{SPA}", 'Import') ; #geronimo-axis-builder
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-client-builder
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-clustering-builder-wadi
 WinSendKeys("{DOW 3}{SPA}", 'Import') ; #geronimo-connector
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-connector-builder
 WinSendKeys("{DOW 5}{SPA}", 'Import') ; #geronimo-deployment
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-deploy-tool
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-directory
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-hot-deploy
 WinSendKeys("{DOW 3}{SPA}", 'Import') ; #geronimo-j2ee-builder
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-j2ee-schema
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-jetty
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-jetty-builder
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-jetty-clustering-wadi
 WinSendKeys("{DOW 6}{SPA}", 'Import') ; #geronimo-naming-builder
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-persistence-jpa10
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-persistence-jpa10-builder
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-security-builder
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-service-builder
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-test-ddbean
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-timer
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-tomcat
 WinSendKeys("{DOW 1}{SPA}", 'Import') ; #geronimo-tomcat-builder
 WinSendKeys("{DOW 3}{SPA}", 'Import') ; #geronimo-upgrade
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-web-builder
 WinSendKeys("{DOW 2}{SPA}", 'Import') ; #geronimo-webservices-builder
 Sleep(3000);
 WinSendKeys('%(c)', 'Import');
 Sleep(3000);
 LogWrite("\tImport \n");
 WinSendKeys('%(f)', 'Import');
 WinWaitCloseImpl($winID, 600);
 Sleep(3000);

 # Import testsupport module
 LogWrite("\nImport testsupport module into workspace: \n");
 WinWaitImpl('Java - Eclipse SDK');
 LogWrite("\tSelect File->Import... \n");
 WinSendKeys('%(f)i', 'Java - Eclipse SDK');
 $winID = WinWaitImpl('Import');
 Sleep(3000);
 LogWrite("\tSelect import existing project \n");
 WinSendKeys('{ENT}{ENT}', 'Import');  
 Sleep(3000);
 LogWrite("\tType path to search projects: ".$curdir."/project/geronimo-1.2-beta/testsupport \n");
 WinSendKeys('%(t)', 'Import');
 WinSendKeys($curdir.'/project/geronimo-1.2-beta/testsupport', 'Import');
 Sleep(3000);
 WinSendKeys('%(e)', 'Import');
 Sleep(10000); # Searching for projects
 LogWrite("\tImport \n");
 WinSendKeys('%(f)', 'Import');
 WinWaitCloseImpl($winID, 300);
 Sleep(3000);


 ########## REPEATABLE PART ##########

 LogWrite("\nStart repetable part: \n");

 # preparation for repeating
 $start_time = time();
 $time_dif = 0;
 $iter = 1;

 # repeat
 if ($iter_num != 0) {
    while($iter <= $iter_num) { # run given iteration number
        Main($iter);
        $iter = $iter+1;
        $end_time = time();
        $time_dif = ($end_time - $start_time)/3600;
        LogWrite("\nTime passed: ".$time_dif."h \n");
    }
 } else {
    while($time_dif < $time_frame) { # run given time
        Main($iter);
        $iter = $iter+1;
        $end_time = time();
        $time_dif = ($end_time - $start_time)/3600;
        LogWrite("\nTime passed: ".$time_dif."h \n");
    }
 }
 
 LogWrite("\nScenario execution time: ".$time_dif."h \n")/3600;

 ########## END REPEATABLE PART ###########

 # Exit Eclipse
 ExitEclipse();

 # EGA passed
 LogWrite("\nEGA PASSED! \n");
 exit 0;



### User-defined functions ###

sub Main {

 my $iteration = shift;

 LogWrite("\n-------------------- Iteration ".$iteration." start ------------------\n");

 # Clean project
 LogWrite("\nClean all Geronimo modules: \n");
 WinWaitImpl('Java - Eclipse SDK');
 LogWrite("\tSelect Project->Clean... \n");
 WinSendKeys('%(p)n', 'Java - Eclipse SDK');
 $winID = WinWaitImpl("Clean");
 Sleep(3000);
 if ($iteration == 1) {
    LogWrite("\tUncheck start build immediatelly \n");
    WinSendKeys("%(b)", "Clean");
 }
 Sleep(3000);
 WinSendKeys("{ENT}", "Clean");
 WinWaitCloseImpl($winID);
 my ($cleaning) = WaitWindowViewable('Cleaning selected projects', 0, 15);
 if (($cleaning)) {
    LogWrite("\twindow 'Cleaning selected projects' (".($cleaning).") appeared\n");
    WinWaitCloseImpl(($cleaning), 300);
 } else {
    Sleep(5000);
 }
 Sleep(5000); # Need some check here "Operation in progress" and "0 errors"!

 # Build all Geronimo modules
 LogWrite("\nBuild all Geronimo modules: \n");
 WinWaitImpl('Java - Eclipse SDK');
 LogWrite("\tSelect Project->Build All \n");
 WinSendKeys('%(p)a', 'Java - Eclipse SDK');
 $winID = WinWaitImpl("Building Workspace", false);
 WinWaitCloseImpl($winID, 600);
 Sleep(10000); # Need some check here "Operation in progress" and "0 errors"!
 
 # Open Navigator view
 WinWaitImpl('Java - Eclipse SDK');
 LogWrite("\nOpen Navigator view: \n");
 WinSendKeys('%(w)v{DOW 6}{ENT}', 'Java - Eclipse SDK');
 Sleep(3000);

 # Fix org.apache.geronimo.common.propertyeditor.PropertyEditorsTest
 if ($iteration == 1) {
     LogWrite("\nFix org.apache.geronimo.common.propertyeditor.PropertyEditorsTest: \n");
     LogWrite("\tSelect 'Navigate->Open Resource' \n");
     WinSendKeys('%(n)u', 'Java - Eclipse SDK');
     $winID = WinWaitImpl('Open Resource');
     LogWrite("\tType PropertyEditorsTest.java into 'Open Resource' dialog \n");
     Sleep(3000);
     WinSendKeys('PropertyEditorsTest.java', 'Open Resource');
     Sleep(3000);
     WinSendKeys('{ENT}', 'Open Resource');
     WinWaitCloseImpl($winID);
     WinWaitImpl("Java - PropertyEditorsTest.java - Eclipse SDK");
     Sleep(5000);
     LogWrite("\tGo to line 'protected void setUp() throws Exception {' \n");
     WinSendKeys('^(l)', 'Java - PropertyEditorsTest.java - Eclipse SDK');
     $winID = WinWaitImpl("Go to Line");
     Sleep(3000);
     WinSendKeys('52', 'Go to Line');
     Sleep(3000);
     WinSendKeys('{ENT}', 'Go to Line');
     WinWaitCloseImpl($winID);
     Sleep(3000);
     $winID = WinWaitImpl("Java - PropertyEditorsTest.java - Eclipse SDK");
     Sleep(3000);
     LogWrite("\tAdd line Class.forName(\"org.apache.geronimo.common.propertyeditor.PropertyEditors\"); to setUp() method \n");
     WinSendKeys('{END}{ENT}', 'Java - PropertyEditorsTest.java - Eclipse SDK');
     WinSendKeys('Class.forName{(}"org.apache.geronimo.common.propertyeditor.PropertyEditors"{END};', 'Java - PropertyEditorsTest.java - Eclipse SDK');
     Sleep(3000);
     LogWrite("\tSave PropertyEditorsTest.java \n");
     WinSendKeys('^(s)', 'Java - PropertyEditorsTest.java - Eclipse SDK');
     Sleep(3000); 
     LogWrite("\tClose PropertyEditorsTest.java \n");
     WinSendKeys('%(f)c', 'Java - PropertyEditorsTest.java - Eclipse SDK');
     Sleep(3000); 
     WinWaitImpl("Java - Eclipse SDK");
}

 # Run JUnit tests from geronimo-common module
 LogWrite("\nRun JUnit tests from geronimo-common module: \n");
 LogWrite("\tSelect geronimo-common module \n");
 WinSendKeys("{DOW 7}", 'Java - Eclipse SDK');
 Sleep(3000);
 LogWrite("\tOpen Run->Run \n");
 WinSendKeys('%(r)n', 'Java - Eclipse SDK');
 $winID = WinWaitImpl("Run");
 Sleep(3000);
 LogWrite("\tCreate new JUnit run configuration \n");
 WinSendKeys("{TAB}", "Run");
 Sleep(3000);
 WinSendKeys("{DOW 4}", "Run");
 Sleep(3000);
 WinSendKeys("{SPA}", "Run");
 Sleep(3000);
 LogWrite("\tRun \n");
 WinSendKeys("%(r)", "Run");
 WinWaitCloseImpl($winID);
 Sleep(20000); # Need some check here "Operation in progress" and "0 errors"!
 if ($iteration > 50) { Sleep(20000); }

 # Modify GBeanData class from geronimo-kernel module
 LogWrite("\nModify GBeanData class from geronimo-kernel module: \n");
 WinWaitImpl('Java - Eclipse SDK');
 WinSendKeys('%(w)v{DOW 6}{ENT}', 'Java - Eclipse SDK');
 Sleep(3000);
 LogWrite("\tSelect geronimo-kernel module \n");
 WinSendKeys("{DOW 9}", 'Java - Eclipse SDK');
 Sleep(3000);
 LogWrite("\tSelect Navigate->Go Into\n");
 WinSendKeys('%(n)i', 'Java - Eclipse SDK');
 Sleep(3000);
 LogWrite("\tOpen GBeanData.java \n");
 WinSendKeys("{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 10}{ENT}", 'Java - Eclipse SDK');
 WinWaitImpl("Java - GBeanData.java - Eclipse SDK");
 Sleep(6000);
 LogWrite("\tSelect Edit->Find/Replace...\n");
 WinSendKeys('%(e)f', "Java - GBeanData.java - Eclipse SDK");
 $winID = WinWaitImpl("Find/Replace");
 LogWrite("\tFind getAbstractName() method \n");
 WinSendKeys("getAbstractName", "Find/Replace");
 WinSendKeys("{ENT}", "Find/Replace");
 Sleep(3000);
 WinSendKeys("{ESC}", "Find/Replace");
 WinWaitCloseImpl($winID);
 Sleep(3000);
 WinSendKeys("{END}", "Java - GBeanData.java - Eclipse SDK");
 LogWrite("\tAdd System.out.println(\"Method getAbstractName() was called!\"); \n");
 WinSendKeys("{ENT}", "Java - GBeanData.java - Eclipse SDK");
 WinSendKeys('System.out.println{(}"Method getAbstractName{(}{)} was called{!}"{END};', "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tSave Class.java \n");
 WinSendKeys("^(s)", "Java - GBeanData.java - Eclipse SDK");
 Sleep(10000);

 # Create JUnit test
 LogWrite("\nCreate JUnit test: \n");
 WinSendKeys("%(w)v{DOW 6}{ENT}", "Java - GBeanData.java - Eclipse SDK");
 LogWrite("\tOpen geronimo-kernel/src/test/java/org/apache/geronimo/gbean folder\n");
 WinSendKeys("{DOW 20}{ENT}{DOW 2}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}{DOW 1}{ENT}", "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tSelect File->New->JUnit Test Case \n");
 WinSendKeys("%(f)n{DOW 10}{ENT}", "Java - GBeanData.java - Eclipse SDK");
 $winID = WinWaitImpl("New JUnit Test Case");
 LogWrite("\tEnter SimpleGBeanDataTest as test name \n");
 WinSendKeys("%(m)", "New JUnit Test Case");
 WinSendKeys("SimpleGBeanDataTest", "New JUnit Test Case");
 LogWrite("\tEnter org.apache.geronimo.gbean.GBeanData as class under test \n");
 WinSendKeys("%(l)", "New JUnit Test Case");
 WinSendKeys("org.apache.geronimo.gbean.GBeanData", "New JUnit Test Case");
 Sleep(3000); 
 LogWrite("\tPress Next button \n");
 WinSendKeys("{TAB 2}{ENT}", "New JUnit Test Case");
 Sleep(3000);
 LogWrite("\tEnter getAbstractName() as method under test \n");
 WinSendKeys("{DOW 5}{SPA}", "New JUnit Test Case");
 LogWrite("\tPress Finish button \n");
 WinSendKeys("%(f)", "New JUnit Test Case");
 WinWaitCloseImpl($winID);
 $winID = WinWaitImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tFind string fail(\"Not yet implemented\"); \n");
 WinSendKeys("{DOW 7}{END}", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 LogWrite("\tDelete string fail(\"Not yet implemented\"); \n");
 WinSendKeys("+({HOM})", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 WinSendKeys("{DEL}", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 LogWrite("\tAdd string System.out.println(new GBeanData().getAbstractName()); \n");
 WinSendKeys("System.out.println{(}new GBeanData{(}{)}.getAbstractName{(}{)}{END};", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tSave SimpleGBeanDataTest.java \n");
 WinSendKeys("^(s)", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tClose SimpleGBeanDataTest.java \n");
 WinSendKeys("%(f)c", "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 WinWaitImpl("Java - GBeanData.java - Eclipse SDK");

 # Build project
 LogWrite("\nBuild project: \n");
 Sleep(3000);
 WinSendKeys('{F12}', "Java - GBeanData.java - Eclipse SDK");
 WinSendKeys('%(p)b', "Java - GBeanData.java - Eclipse SDK");
 Sleep(10000); # Need some check here "Operation in progress" and "0 errors"!

 # Run SimpleGBeanDataTest
 LogWrite("\nRun SimpleGBeanDataTest: \n");
 WinSendKeys("%(w)v{DOW 6}{ENT}", "Java - GBeanData.java - Eclipse SDK");
 LogWrite("\tSelect Run->Run As->JUnit Test\n");
 WinSendKeys("%(r)s1", "Java - GBeanData.java - Eclipse SDK");
 Sleep(10000); # Need some check here "Operation in progress" and "0 errors"!

 # Set breakpoint
 LogWrite("\nSet breakpoint in GBeanData.java: \n");
 WinWaitImpl("Java - GBeanData.java - Eclipse SDK");
 WinSendKeys("{F12}", "Java - GBeanData.java - Eclipse SDK"); #switch to main pane
 Sleep(3000);
 LogWrite("\tGo to line: System.out.println(\"Method getAbstractName() was called!\");\n");
 WinSendKeys("^(l)", "Java - GBeanData.java - Eclipse SDK");
 $winID = WinWaitImpl("Go to Line");
 Sleep(3000);
 WinSendKeys("75", "Java - GBeanData.java - Eclipse SDK");
 WinSendKeys("{ENT}", "Java - GBeanData.java - Eclipse SDK");
 WinWaitCloseImpl($winID);
 Sleep(3000);
 LogWrite("\tSet breakpoint\n");
 #WinSendKeys("^(+(b))", "Java - GBeanData.java - Eclipse SDK");
 WinSendKeys("%(r)kk{ENT}", "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);

 # Debug
 LogWrite("\nDebug: \n");
 WinWaitImpl("Java - GBeanData.java - Eclipse SDK");
 LogWrite("\tSelect Run->Debug... \n");
 WinSendKeys("%(r)b", "Java - GBeanData.java - Eclipse SDK");
 $winID = WinWaitImpl("Debug");
 LogWrite("\tSelect SimpleGBeanDataTest run configuration \n");
 Sleep(6000);
 WinSendKeys("{ENT}", "Debug");
 Sleep(6000);
 if (length($vm_debug_options) > 0) {
    LogWrite("\tAdd vm options $vm_debug_options for debug step\n");
    WinSendKeys("{TAB 2}{RIG}", "Debug");
    Sleep(6000);
    WinSendKeys("%(g)", "Debug");
    Sleep(6000);
    WinSendKeys("$vm_debug_options", "Debug");
    Sleep(6000);
    WinSendKeys("%(y)", "Debug");
 }
 LogWrite("\tPress Debug button \n");
 WinSendKeys("{ENT}", "Debug");
 WinWaitCloseImpl($winID);
 $winID = WinWaitImpl("Confirm Perspective Switch");
 LogWrite("\tPress Yes button \n");
 WinSendKeys("%(y)", "Confirm Perspective Switch");
 WinWaitCloseImpl($winID);
 Sleep(6000);
 LogWrite("\tStep over \n");
 WinSendKeys("%(r)o", "Debug - GBeanData.java - Eclipse SDK");
 Sleep(6000);
 LogWrite("\tStep over \n");
 WinSendKeys("%(r)o", "Debug - GBeanData.java - Eclipse SDK");
 Sleep(6000);
 WinWaitImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK");
 LogWrite("\tStep over \n");
 WinSendKeys("%(r)o", "Debug - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(6000);
 LogWrite("\tResume \n");
 WinSendKeys("%(r)mm{ENT}", "Debug - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(6000); # Some check is needed here!!!

 # Switch to Java view
 LogWrite("\nSwitch to Java perspective: \n");
 WinWaitImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK");
 LogWrite("\tSwitch perspective\n");
 WinSendKeys("%(w)o{ENT}", "Debug - SimpleGBeanDataTest.java - Eclipse SDK");
 WinWaitImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tClose SimpleGBeanDataTest.java \n");
 WinSendKeys('%(f)c', "Java - SimpleGBeanDataTest.java - Eclipse SDK");
 WinWaitImpl("Java - GBeanData.java - Eclipse SDK");

 # Unset breakpoint
 LogWrite("\nUnset breakpoint: \n");
 Sleep(3000);
 LogWrite("\tSelect Navigate->Go to Line... \n");
 WinSendKeys("^(l)", "Java - GBeanData.java - Eclipse SDK");
 $winID = WinWaitImpl("Go to Line");
 Sleep(3000);
 LogWrite("\tSelect line 75 of GBeanData.java \n");
 WinSendKeys("75", "Go to Line");
 LogWrite("\tPress OK button \n");
 WinSendKeys("{ENT}", "Go to Line");
 WinWaitCloseImpl($winID);
 Sleep(3000);
 LogWrite("\tUnset breakpoint \n");
 WinSendKeys("^(+(b))", "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);

 # Remove GBeanData.java modifications
 LogWrite("\nRemove GBeanData.java modifications: \n");
 LogWrite("\tDelete string System.out.println(\"Method getAbstractName() was called!\"); \n");
 WinSendKeys("{END}+({HOM}){DEL}{BS 6}", "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tSave GBeanData.java\n");
 WinSendKeys("^(s)", "Java - GBeanData.java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tClose GBeanData.java \n");
 WinSendKeys("%(f)c", "Java - GBeanData.java - Eclipse SDK");
 Sleep(6000);
 WinWaitImpl("Java - Eclipse SDK");

 # Remove JUnit test
 LogWrite("\nRemove JUnit test: \n");
 WinSendKeys("%(w)v{DOW 6}{ENT}", "Java - Eclipse SDK"); 
 LogWrite("\tSelect Edit->Delete \n");
 WinSendKeys("%(e)d", "Java - Eclipse SDK"); 
 $winID = WinWaitImpl("Confirm Resource Delete");
 LogWrite("\tPress OK button \n");
 WinSendKeys("{ENT}", "Confirm Resource Delete");
 WinWaitCloseImpl($winID);
 Sleep(3000);

 # Remove run configurations
 LogWrite("\nRemove run configurations: \n");
 WinSendKeys("%(r)n", "Java - Eclipse SDK");
 $winID = WinWaitImpl("Run");
 Sleep(6000);
 LogWrite("\tDelete configuration \n");
 WinSendKeys("{ENT}", "Run");
 Sleep(6000);
 WinSendKeys("{DEL}", "Run");
 $winID2 = WinWaitImpl("Confirm Launch Configuration Deletion");
 Sleep(3000);
 WinSendKeys("%(y)", "Confirm Launch Configuration Deletion");
 WinWaitCloseImpl($winID2);
 Sleep(3000);
 LogWrite("\tDelete configuration \n");
 WinSendKeys("{DEL}", "Run");
 $winID2 = WinWaitImpl("Confirm Launch Configuration Deletion");
 Sleep(3000);
 WinSendKeys("%(y)", "Confirm Launch Configuration Deletion");
 WinWaitCloseImpl($winID2);
 Sleep(3000);
 WinSendKeys("{HOM}", "Run");
 Sleep(3000);
 WinSendKeys("{ESC}", "Run");
 WinWaitCloseImpl($winID);
 WinWaitImpl("Java - Eclipse SDK");

 # Reset perspective
 LogWrite("\nReset perspective: \n");
 WinSendKeys("%(w)r", "Java - Eclipse SDK");  
 $winID = WinWaitImpl("Reset Perspective");
 Sleep(3000);
 LogWrite("\tPress OK button \n");
 WinSendKeys("{ENT}", "Reset Perspective");
 WinWaitCloseImpl($winID);
 WinWaitImpl("Java - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tRestore folders structure in package explorer view \n");
 WinSendKeys("{HOM}{DOW 24}", "Java - Eclipse SDK");
 PressKey('{NUM}'); 
 WinSendKeys('{-}{UP 4}{-}{UP 4}{-}', "Java - Eclipse SDK");
 ReleaseKey('{NUM}');
 WinSendKeys("{HOM}", "Java - Eclipse SDK");
 Sleep(3000);

 # Help - Mozilla must be the default browser 
 LogWrite("\nHelp: \n");
 my $delete_locks = `find ~/.mozilla/ -name "*lock*" -exec rm \{\} \\;`;
 Sleep(6000);
 LogWrite("\tSelect Help->Help Contents \n");
 WinSendKeys("%(h)h", "Java - Eclipse SDK");
 Sleep(6000);
 my @winIds = FindWindowLike('Select User Profile');
 if (scalar(@winIds) > 0) {
    SetInputFocus($winIds[0]);
    WinSendKeys('{ENT}', 'Select User Profile');
    WinWaitCloseImpl($winIds[0]);
 }
 $winID = WinWaitImpl("Help - Eclipse SDK");
 Sleep(3000);
 LogWrite("\tClose Help \n");
 WinSendKeys('%(f)q', "Help - Eclipse SDK");
 WinWaitCloseImpl($winID);
 LogWrite("\tEnd Help \n");
 Sleep(6000);
 LogWrite("\n-------------------- Iteration ".$iteration." end --------------------\n");
}

sub LogWrite {
    open(LOG, ">>$resultsDir/EGA.log") || die("ERROR: Can't open log file $resultsDir/EGA.log! \n");
    my $text = shift;
    print LOG $text;
    close(LOG);
}

sub ExitEclipse { 
    LogWrite("\nExit Eclipse: \n");
    $winID = WinWaitImpl('Java - Eclipse SDK');
    WinSendKeys('%(f)x', 'Java - Eclipse SDK');
    WinWaitCloseImpl($winID);
    Sleep(10000);
}

sub KillEclipse {
    if (-e "$curdir/workspace/.metadata/.log") { 
        my $result =`cp -f $curdir/workspace/.metadata/.log $resultsDir/workspace.log`;
    }
    if ($eclipse_kill == 0) { return; }
    LogWrite("\nKill Eclipse: \n");
    my $user = `whoami`;
    $user =~ s/\n//;
    my $proc_info = `ps U $user | grep $eclipse_home/startup.jar | grep -v grep`;
    my @procs = split(/\n/, $proc_info);
    my $length = scalar(@procs); 
    if ($length < 1) {
        LogWrite("\tEclipse process wasn't found \n");
        return;
    } else {
        LogWrite("\tFound ".$length." Eclipse processes \n");
        for ($i = 0; $i < $length; $i++) {
            LogWrite("\tProcess ".$i.": \n");
            my $proc_ID = $procs[$i];
            $proc_ID =~ s/\s*(\d\d*).*/\1/;
            if ($proc_ID != 0) {
                my $result = kill(9, $proc_ID);
                if ($result == 1) {
                    LogWrite("\tEclipse process was killed \n");
                } else {
                    if ($result == 0) {
                        LogWrite("\tERROR: Eclipse process can't be killed! \n");    
                    }
                    if ($result > 1) {
                        LogWrite("\tERROR: More than one process was killed! \n");
                    }
                }
            }            
        }
    }
}

sub PrintScreen {
    my $user = `whoami`;
    $user =~ s/\n//;
    my $proc_info = `ps U $user | grep $eclipse_home/startup.jar | grep -v grep`;
    my @procs = split(/\n/, $proc_info);
    my $length = scalar(@procs); 
    if ($length < 1) { return; }
    LogWrite("\nPrint error screen picture: \n");
    $result = RunApp("xwd -root -screen -silent -out $resultsDir/error.xwd");
    if ($result != 0) {    
        LogWrite("\tERROR: Can't save screen picture to $resultsDir/error.xwd! \n");
    } else {
        LogWrite("\tPicture was saved to $resultsDir/error.xwd \n");
        LogWrite("\tPlease, use command 'xwud -in ./error.xwd' to view the error screen picture \n");
    }
}

sub WinWaitImpl {    
      my $winName = shift;
      my $setFocus = shift || true;
      my $delay = 100*$delay_factor;   
      LogWrite("\twait window '".$winName."' to appear ...\n");
      my @wins = WaitWindowViewable($winName, 0, $delay);
      if(scalar(@wins) > 0) {
        my $win = $wins[0];        
        LogWrite("\twindow '".$winName."' (".$win.") appeared\n");
        Sleep(200);
        if ((GetInputFocus() != $win) && $setFocus){
            SetInputFocus($win);
            if (GetInputFocus() != $win) { 
                LogWrite("\nERROR: Unable to set focus to '".$winName."' window! \n");
                PrintScreen();
                KillEclipse();
                LogWrite("\n\nEGA FAILED! \n");
                die "ERROR: Unable to set focus to '".$winName."' window!"; 
             }
         }
         return $win;    
      } else {
          LogWrite("\nERROR: Window '".$winName."' didn't appear for ".$delay." sec! \n");
          PrintScreen();
          KillEclipse();
          LogWrite("\n\nEGA FAILED! \n");
          die "ERROR: Window '".$winName."' didn't appear for ".$delay." sec!";    
      }      
}

sub WinWaitCloseImpl {    
      my $win = shift;
      my $winName = GetWindowName($win);
      my $delay = shift || 100; 
      $delay = $delay*$delay_factor;     
      LogWrite("\twait window (".$win.") to close ...\n");
      my $result = WaitWindowClose($win, $delay);
      if($result > 0) {                
          LogWrite("\twindow (".$win.") successfully closed \n");
      } else { 
          LogWrite("\nERROR: Failed to close (".$win.") window during ".$delay." sec! \n");
          PrintScreen();
          KillEclipse();
          LogWrite("\n\nEGA FAILED! \n");
          die "ERROR: Failed to close (".$win.") window during ".$delay." sec!";    
      }      
}

sub WinSendKeys {
    my $keys = shift;
    my $target_win_name = shift;
    my @ids = WaitWindowViewable($target_win_name, 0, 5);
    if (scalar(@ids) > 0) {
        my $focus_win_id = GetInputFocus();
        my $focus_win_name = GetWindowName($focus_win_id);
        my $target_win_id = $ids[0];
        if ($focus_win_id == $target_win_id) {
            SendKeys($keys);
        } else {
            #LogWrite("WARNING: Focus is not on window '".$target_win_name."' (".$target_win_id.") but on window '".$focus_win_name."' (".$focus_win_id.")! \n");
            if (index($focus_win_name, "error") != -1 || index($focus_win_name, "Error") != -1 ) {
                LogWrite("\nERROR: Some error window is appeared: '".$focus_win_name."'! \n");    
                PrintScreen();
                KillEclipse();
                LogWrite("\n\nEGA FAILED! \n");
                die "ERROR: Some error window is appeared: '".$focus_win_name."'!"; 
            } else {
                SendKeys($keys);
            }
        }
    } else {
        LogWrite("\nERROR: Window '".$target_win_name."' isn't viewable! \n");
        PrintScreen();
        KillEclipse();
        LogWrite("\n\nEGA FAILED! \n");
        die "ERROR: Window '".$target_win_name."' isn't viewable!"; 
    }      
}

sub Sleep {
    my $delay = shift;
    $delay = $delay*$delay_factor;
    #LogWrite("\tSleep for $delay mc \n");
    SendKeys('{PAUSE '.$delay.'}');
}



### END ###

