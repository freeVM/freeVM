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
##       under the License.    
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

    StartApp
    RunApp
/;

 # Set option
 SetKeySendDelay(150);

 # Current directory
 $curdir = `pwd`;
 $curdir =~ s/\n//;

 # Set variables
 $vm_options = "";
 if ((scalar(@ARGV) == 4) || (scalar(@ARGV) == 5)) {
    $os_arch = $ARGV[0];
 	$eclipse_home = $ARGV[1];
    $java_home = $ARGV[2];
	$resultsDir = $ARGV[3];
	if(scalar(@ARGV) == 5) {
	    $vm_options = $ARGV[4];
	}
    LogWrite("\nEclipse Home = $eclipse_home\n");
	LogWrite("Tested JRE = $java_home\n");
	LogWrite("Tested JRE options for Eclipse launching = $vm_options\n\n");
 } else {
    print("ERROR: Wrong number of input parameters! \n\nEHWA FAILED!\n");
    die "ERROR: Wrong number of input parameters!";
 }

 LogWrite("--- Automated EHWA scenario for Eclipse 3.2.1 --- \n\n");

 # Check if some Eclipse window is already olpened
 my ($eclipse_win) = FindWindowLike('Eclipse SDK');
 if (($eclipse_win)) {
	LogWrite("\nERROR: Some Eclipse window is already opened! \n\n EHWA FAILED!\n");
    die "ERROR: Some Eclipse window is already opened!"
 }

 LogWrite("Start: \n\n");

 LogWrite("Run Eclipse \n");
 $result = StartApp("$java_home/bin/java $vm_options -cp \"$eclipse_home/startup.jar\" -Dosgi.install.area=\"$eclipse_home\" org.eclipse.core.launcher.Main -ws gtk -os linux -arch $os_arch -debug > $resultsDir/eclipse.log 2\>\&1");

 if ($result == 0) {	
	LogWrite("ERROR: Can't start Eclipse! \n EHWA FAILED!\n");
    die "ERROR: Can't start Eclipse!"
 }

 # Select default workspace
 LogWrite("Select workspace \n");
 my $winID = WinWaitImpl('Workspace Launcher', 180);
 SleepImpl();
 WinSendKeys($curdir.'/workspace', 'Workspace Launcher');
 LogWrite("Press 'OK' button \n");
 SleepImpl();
 WinSendKeys('{ENT}', 'Workspace Launcher');
 WinWaitCloseImpl($winID);

 # Close Welcome page
 LogWrite("Close Welcome page \n");
 $winID = WinWaitImpl('Java - Eclipse SDK', 180);
 SleepImpl();
 WinSendKeys('%({+}{-})', 'Java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('c', 'Java - Eclipse SDK');
 SleepImpl();

 # Create new Java project
 LogWrite("Create new project \n");
 WinSendKeys('^(n)', 'Java - Eclipse SDK');
 SleepImpl();
 $winID = WinWaitImpl('New');
 LogWrite("Select new Java project \n");
 WinSendKeys('{ENT}', 'New');
 SleepImpl();
 WinSendKeys('{DOW 2} {ENT}', 'New');

 # Wait New Java Project window to appear      
 $winID = WinWaitImpl('New Java Project');
 LogWrite("Type 'EHWA' as project name \n");
 WinSendKeys('EHWA', 'New Java Project');
 SleepImpl();
 LogWrite("Press 'Finish' button \n");
 WinSendKeys('{ENT}', 'New Java Project');
 WinWaitCloseImpl($winID);

 # Create New Java Class
 LogWrite("Create new Java class \n");
 $winID = WinWaitImpl('Java - Eclipse SDK'); 
 SleepImpl();
 WinSendKeys('^(n)', 'Java - Eclipse SDK');

 # Wait 'New' dialog window to appear
 $winID = WinWaitImpl('New');
 SleepImpl();

 # Select new Java class
 LogWrite("Select new Java class for 'EHWA' project \n");
 WinSendKeys('c', 'New');
 SleepImpl();
 WinSendKeys('{ENT 2}', 'New');

 # Wait 'Java Class' window to appear
 $winID = WinWaitImpl('Java Class');

 # Create EHWA class
 LogWrite("Type 'EHWA' as class name, press 'Finish' button \n");
 SleepImpl();
 WinSendKeys('EHWA', 'Java Class');
 LogWrite("Select the 'public static void main(String[] args)' checkbox \n"); 
 SleepImpl();
 WinSendKeys('%(g)', 'Java Class');
 LogWrite("Press 'Finish' button \n");
 SleepImpl();
 WinSendKeys('%(f)', 'Java Class');
 WinWaitCloseImpl($winID);   

 # Paste System.out.println("Hello, world"); into EHWA class
 LogWrite("Put System.out.println(\"Hello, world\"); into EHWA class \n");
 $winID = WinWaitImpl('Java - EHWA.java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('{DOW 8}', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('{TAB}', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('System.out.println{(}"Hello, World{RIG}{RIG};', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();

 # Save EHWA class
 LogWrite("Save EHWA class \n");
 WinSendKeys('^(s)', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();

 # Run EHWA class
 LogWrite("Run EHWA class \n");
 WinSendKeys('%(r)s', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('{ENT}', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl(10000);
 my @winIds = FindWindowLike('Progress Information');
 if (scalar(@winIds) > 0) {
     WinWaitCloseImpl($winIds[0], 300);
 }

 # Exit Eclipse
 LogWrite("Exit Eclipse \n");
 $winID = WinWaitImpl('Java - EHWA.java - Eclipse SDK');
 WinSendKeys('%(f)', 'Java - EHWA.java - Eclipse SDK');
 SleepImpl();
 WinSendKeys('x', 'Java - EHWA.java - Eclipse SDK');
 WinWaitCloseImpl($winID);
 SleepImpl();

 # EHWA passed
 LogWrite("\nEHWA PASSED! \n");
 exit 0;


### User-defined functions ###

sub WinWaitImpl {    
      my $winName = shift;
      my $delay = shift || 120;       
      LogWrite(" wait window '".$winName."' to appear ...\n");
      my @wins = WaitWindowViewable($winName, 0, $delay);
      if(scalar(@wins) > 0) {
        my $win = @wins[0];        
      	LogWrite(" window '".$winName."' appeared\n");
	    if (GetInputFocus() != $win) {
	        SetInputFocus($win);
	        if (GetInputFocus() != $win) {  
		        LogWrite("ERROR: Unable to set focus to '".$winName."' window! \n");
		        PrintScreen();
		        KillEclipse();	
 		        LogWrite("\n\nEHWA FAILED! \n");
    	    	die "ERROR: Unable to set focus to '".$winName."' window!"; 
	        }
	    }
	    return $win;	
      } else {
          LogWrite("ERROR: Window '".$winName."' didn't appear for ".$delay." sec! \n");
          PrintScreen();
	      KillEclipse();
	      LogWrite("\n\nEHWA FAILED! \n");
          die "ERROR: Window '".$winName."' didn't appear for ".$delay." sec!";	
      }      
}

sub WinWaitCloseImpl {    
      my $win = shift;
      my $winName = GetWindowName($win);
      my $delay = shift || 120;     
      LogWrite(" wait window '".$winName."' to close ...\n");
      my $result = WaitWindowClose($win, $delay);
      if($result > 0) {                
          LogWrite(" window '".$winName."' successfully closed \n");
      } else { 
          LogWrite("ERROR: Failed to close '".$winName."' window during ".$delay." sec! \n");
          PrintScreen();
	      KillEclipse();
	      LogWrite("\n\nEHWA FAILED! \n");
	      die "ERROR: Failed to close '".$winName."' window during ".$delay." sec!";	
      }      
}

sub WinSendKeys {
    my $keys = shift;
    my $target_win_name = shift;
    my @ids = WaitWindowViewable($target_win_name, 0, 5);
    if (scalar(@ids) > 0) {
        my $focus_win_id = GetInputFocus();
        my $focus_win_name = GetWindowName($focus_win_id);
        my $target_win_id = @ids[0];
        if ($focus_win_id == $target_win_id) {
	        SendKeys($keys);
	    } else {
	        if (index($focus_win_name, "error") != -1 || index($focus_win_name, "Error") != -1 ) {
	            LogWrite("\nERROR: Some error window is appeared: '".$focus_win_name."'! \n");	
		        PrintScreen();
		        KillEclipse();
		        LogWrite("\n\nEHWA FAILED! \n");
    	        die "ERROR: Some error window is appeared: '".$focus_win_name."'!"; 
	        } else {
                SendKeys($keys);
	        } 
	    }
    } else {
	    LogWrite("\nERROR: Window '".$target_win_name."' isn't viewable! \n");
	    PrintScreen();
	    KillEclipse();
	    LogWrite("\n\nEHWA FAILED! \n");
    	die "ERROR: Window '".$target_win_name."' isn't viewable!"; 
    }      
}

sub LogWrite {
	open(LOG, ">>$resultsDir/EHWA.log") || die("ERROR: Can't open log file $resultsDir/EHWA.log! \n");
	my $text = shift;
	print LOG $text;
	close(LOG);
}

sub KillEclipse {
    if (-e "$curdir/workspace/.metadata/.log") { 
        my $result =`cp -f $curdir/workspace/.metadata/.log $resultsDir/workspace.log`;
    }
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
	}
}

sub SleepImpl {
      $delay = shift || 6000;
      SendKeys('{PAUSE '.$delay.'}');
}

### END ###

