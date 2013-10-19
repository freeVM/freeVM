
;     Licensed to the Apache Software Foundation (ASF) under one
;     or more contributor license agreements.  See the NOTICE file
;     distributed with this work for additional information
;     regarding copyright ownership.  The ASF licenses this file
;     to you under the Apache License, Version 2.0 (the
;     "License"); you may not use this file except in compliance
;     with the License.  You may obtain a copy of the License at
;
;         http://www.apache.org/licenses/LICENSE-2.0
;
;     Unless required by applicable law or agreed to in writing,
;     software distributed under the License is distributed on an
;     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
;     KIND, either express or implied.  See the License for the
;     specific language governing permissions and limitations
;     under the License.   

; Set constants
Select 
    Case $CmdLine[0] = 8 OR $CmdLine[0] = 9
        Dim Const $eclipse_home = $CmdLine[1]
        Dim Const $java_home = $CmdLine[2]
        Dim Const $resultsDir = $CmdLine[3]
        Dim Const $iter_num = Number($CmdLine[4])
        Dim Const $time_frame = Number($CmdLine[5])
        Dim Const $delay_factor = Number($CmdLine[6])
        Dim Const $kill_eclipse = Number($CmdLine[7])
        Dim Const $vm_options = $CmdLine[8]
        Dim $vm_debug_options = ""
        If Not FileExists($resultsDir) Then
            ConsoleWrite("ERROR! Result dir = " & $resultsDir & " doesn't exist!" & @LF)
            Exit(1)
        EndIf
        LogWrite(@LF & "--- Automated Eclipse EGA scenario for Eclipse 3.2.1 --- " & @LF & @LF)
        LogWrite("Eclipse Home = " & $eclipse_home & @LF)
        LogWrite("Tested JRE Home = " & $java_home & @LF)
        LogWrite("Tested JRE options for Eclipse launching = " & $vm_options & @LF)
        If $CmdLine[0] = 9 Then
            $vm_debug_options = $CmdLine[9]
            LogWrite("Tested JRE options for debug step = " & $vm_debug_options & @LF)
        EndIf
        If $iter_num < 0 Then
            LogWrite("ERROR! Iteration number < 0!" & @LF)
            LogWrite(@LF & "EGA FAILED!" & @LF)
            Sleep(5000)
            ConsoleWrite("ERROR! Iteration number < 0!" & @LF)
            Exit(1)
        EndIf
        If $iter_num = 0 AND $time_frame <= 0 Then
            LogWrite("ERROR! Time frame for the scenario run <= 0! " & @LF)
            LogWrite(@LF & "EGA FAILED!" & @LF)
            Sleep(5000)
            ConsoleWrite("ERROR! Time frame for the scenario run <= 0! " & @LF)
            Exit(1)
        EndIf
        If $delay_factor < 1 Then
            LogWrite(@LF & "ERROR: Delay factor < 1! " & @LF)
            LogWrite(@LF & "EGA FAILED!" & @LF)
            Sleep(5000)
            ConsoleWrite(@LF & "ERROR: Delay factor < 1! " & @LF)
            Exit(1)
        EndIf
        If $iter_num > 0 Then
            LogWrite("Iteration number = " & $iter_num & @LF)
        EndIf
        If $iter_num = 0 AND $time_frame > 0 Then
            LogWrite("Time frame for the scenario run = " & $time_frame & " hours " & @LF)
        EndIf
        If $delay_factor >= 1 Then
            LogWrite("Delay factor = " & $delay_factor & @LF)
        EndIf
        LogWrite("Kill Eclipse if EGA scenario fails = " & $kill_eclipse & @LF)
    Case Else 
        ConsoleWrite("ERROR! Wrong number of input parameters! STOP.")
        Exit(1)
EndSelect

; Check if some Eclipse window is already olpened
Opt("WinTitleMatchMode", 2)
If WinExists("Eclipse SDK") Then
    LogWrite(@LF & "ERROR! Some Eclipse window is already opened! STOP." & @LF)
    LogWrite(@LF & "EGA FAILED!" & @LF)
    Sleep(5000)
    ConsoleWrite(@LF & "ERROR! Some Eclipse window is already opened! STOP." & @LF)
    Exit(1)
EndIf
    
; Set options
Opt("WinWaitDelay", 1000) ; wait for 1c
Opt("SendKeyDelay", 200*$delay_factor) ; wait for 150mc
Opt("WinTitleMatchMode", 4)
Opt("WinDetectHiddenText", 1)
Opt("TrayIconDebug", 1)
    
; Start Eclipse
LogWrite(@LF & "Start:" & @LF)
LogWrite(@TAB & "Run Eclipse" & @LF)
$run = $java_home & "\bin\java.exe " & $vm_options & " -cp """ & $eclipse_home & "\startup.jar"" -Dosgi.install.area=""" & $eclipse_home & """ org.eclipse.core.launcher.Main -debug > " & $resultsDir & "\eclipse.log 2>&1"
;LogWrite(@TAB & "Command line: " & $run & @LF)
Run(@ComSpec & " /c " & $run, "", @SW_HIDE)
LogWrite(@TAB & "wait Eclipse 'Workspace Launcher' window to appear " & @LF)
WinWaitImpl("Workspace Launcher", "", 180)
If Not WinActive("Workspace Launcher") Then WinActivate("Workspace Launcher")
WinWaitActiveImpl("Workspace Launcher", "", 30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select " & @WorkingDir & "\workspace as workspace" & @LF)
ControlFocus("Workspace Launcher", "", "Edit1")
ControlSetText("Workspace Launcher", "", "Edit1", @WorkingDir & "\workspace" )
LogWrite(@TAB & "Press OK button" & @LF)
ControlSend("Workspace Launcher", "", "", "{ENTER}")
WinWaitCloseImpl("Workspace Launcher", "", 60)
LogWrite(@TAB & "wait 'Java - Eclipse SDK' window to appear " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 180)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "window appeared " & @LF)
SleepImpl(3000)

; Prepare Eclipse environment
LogWrite(@LF & "Prepare Eclipse environment:" & @LF)

; Close Welcome page
ControlFocus("Java - Eclipse SDK", "", "ToolbarWindow321") 
ControlClick("Java - Eclipse SDK", "", "ToolbarWindow321") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Welcome page window wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Welcome page window wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000)
LogWrite(@TAB & "Close Welcome page " & @LF)
ControlFocus("Java - Eclipse SDK", "", "ToolbarWindow321") 
ControlSend("Java - Eclipse SDK", "", "ToolbarWindow321", "!+Q") 
WinWaitNotActive("Java - Eclipse SDK", "")
Send("p")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
SleepImpl(3000)

; Uncheck Build Automatically
LogWrite(@TAB & "Uncheck Project->Build Automatically" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Project", "Build Auto&matically" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Project->Build Automatically' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Project->Build Automatically' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf

; Set M2_REPO variable
LogWrite(@TAB & "Select Window->Preferences" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Window", "&Preferences" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Window->Preferences' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Window->Preferences' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Preferences' window to appear " & @LF)
WinWaitImpl("Preferences","",60)
If Not WinActive("Preferences","") Then WinActivate("Preferences","")
WinWaitActiveImpl("Preferences","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select Java->Build Path->Classpath Variables" & @LF)
Send("{ENTER}j{NUMPADADD}b{NUMPADADD}c")
LogWrite(@TAB & "Create M2_REPO variable" & @LF)
Send("!n")
LogWrite(@TAB & "wait 'New Variable Entry' window to appear " & @LF)
WinWaitImpl("New Variable Entry","",60)
If Not WinActive("New Variable Entry","") Then WinActivate("New Variable Entry","")
WinWaitActiveImpl("New Variable Entry","",30)
LogWrite(@TAB & "window appeared " & @LF)
ControlFocus("New Variable Entry", "", "Edit1")
ControlSend("New Variable Entry", "", "Edit1", "M2_REPO")
LogWrite(@TAB & "Set M2_REPO value to " & @WorkingDir & "\project\.m2\repository" & @LF)
ControlFocus("New Variable Entry", "", "Edit2")
ControlSend("New Variable Entry", "", "Edit2", @WorkingDir & "\project\.m2\repository")
LogWrite(@TAB & "Press OK button" & @LF)
ControlFocus("New Variable Entry", "OK", "Button3") 
ControlClick("New Variable Entry", "OK", "Button3") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! New Variable Entry->OK button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! New Variable Entry->OK button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("New Variable Entry", "", 60)
LogWrite(@TAB & "Press OK button to apply new preferences" & @LF)
WinWaitActiveImpl("Preferences","",30)
ControlFocus("Preferences", "OK", "Button26") 
ControlClick("Preferences", "OK", "Button26") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Preferences->OK button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Preferences->OK button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Preferences", "", 60)

; Import geronimo modules
LogWrite(@LF & "Import Geronimo modules into workspace: " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select File->Import..." & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&File", "&Import..." )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Import' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Import' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Import' window to appear " & @LF)
WinWaitImpl("Import","",60)
If Not WinActive("Import","") Then WinActivate("Import","")
WinWaitActiveImpl("Import","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select import existing project" & @LF)
Send("{ENTER}") 
Send("{ENTER}") 
Send("e{ENTER}")
LogWrite(@TAB & "Type path to search projects: " & @WorkingDir & "\project\geronimo-1.2-beta\modules" & @LF)
Send(@WorkingDir & "\project\geronimo-1.2-beta\modules")
Send("!r")
SleepImpl(3000) ; Searching for projects
Send("!p")
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Uncheck all builders" & @LF)
Send("{DOWN 2}{SPACE}") ; geronimo-activemq-gbean
Send("{DOWN 3}{SPACE}") ; geronimo-axis-builder
Send("{DOWN 2}{SPACE}") ; geronimo-client-builder
Send("{DOWN 2}{SPACE}") ; geronimo-clustering-builder-wadi
Send("{DOWN 3}{SPACE}") ; geronimo-connector
Send("{DOWN 1}{SPACE}") ; geronimo-connector-builder
Send("{DOWN 5}{SPACE}") ; geronimo-deployment
Send("{DOWN 1}{SPACE}") ; geronimo-deploy-tool
Send("{DOWN 2}{SPACE}") ; geronimo-directory
Send("{DOWN 1}{SPACE}") ; geronimo-hot-deploy
Send("{DOWN 3}{SPACE}") ; geronimo-j2ee-builder
Send("{DOWN 1}{SPACE}") ; geronimo-j2ee-schema
Send("{DOWN 1}{SPACE}") ; geronimo-jetty
Send("{DOWN 1}{SPACE}") ; geronimo-jetty-builder
Send("{DOWN 1}{SPACE}") ; geronimo-jetty-clustering-wadi
Send("{DOWN 6}{SPACE}") ; geronimo-naming-builder
Send("{DOWN 1}{SPACE}") ; geronimo-persistence-jpa10
Send("{DOWN 1}{SPACE}") ; geronimo-persistence-jpa10-builder
Send("{DOWN 2}{SPACE}") ; geronimo-security-builder
Send("{DOWN 1}{SPACE}") ; geronimo-service-builder
Send("{DOWN 2}{SPACE}") ; geronimo-test-ddbean
Send("{DOWN 1}{SPACE}") ; geronimo-timer
Send("{DOWN 1}{SPACE}") ; geronimo-tomcat
Send("{DOWN 1}{SPACE}") ; geronimo-tomcat-builder
Send("{DOWN 3}{SPACE}") ; geronimo-upgrade
Send("{DOWN 2}{SPACE}") ; geronimo-web-builder
Send("{DOWN 2}{SPACE}") ; geronimo-webservices-builder
SleepImpl(3000) ; just in case
Send("!c")
Send("!f")
LogWrite(@TAB & "Import" & @LF)
WinWaitCloseImpl("Import", "", 900) ; 15min
SleepImpl(6000) ; Refresh

; Import testsupport module
LogWrite(@LF & "Import testsupport module into workspace: " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select File->Import..." & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&File", "&Import..." )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Import' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Import' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Import' window to appear " & @LF)
WinWaitImpl("Import","",60)
If Not WinActive("Import","") Then WinActivate("Import","")
WinWaitActiveImpl("Import","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select import existing project" & @LF)
Send("{ENTER}") 
Send("{ENTER}") 
LogWrite(@TAB & "Type path to search projects: " & @WorkingDir & "\project\geronimo-1.2-beta\testsupport" & @LF)
Send(@WorkingDir & "\project\geronimo-1.2-beta\testsupport")
Send("!r")
SleepImpl(3000) ; Searching for projects
Send("!f")
LogWrite(@TAB & "Import" & @LF)
WinWaitCloseImpl("Import", "", 600) ; 10min
SleepImpl(10000) ; Refresh
LogWrite(@TAB & "Check import result" & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
If StringInStr(StringReplace(WinGetText("Java - Eclipse SDK"), @LF, " "), " 0 errors") = 0 Then
    LogWrite(@TAB & "ERROR! Errors in geronimo project after import. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Errors in geronimo project after import. STOP." & @LF)
    ExitImpl(1)
EndIf

LogWrite(@LF & "Start repeatable part: " & @LF)

;preparation for repeating
Dim $start_time = TimerInit()
Dim $iter = 1
Dim $diff = 0

; REPEAT
If $iter_num <> 0 Then
    Do
        Main($iter)
        $dif = TimerDiff($start_time)/3600000
        $iter = $iter+1
        LogWrite(@LF & "Time passed: " & $dif & " hours " & @LF)
    Until $iter > $iter_num ; run given iteration number
        
Else
    Do
        Main($iter)
        $dif = TimerDiff($start_time)/3600000
        $iter = $iter+1
        LogWrite(@LF & "Time passed: " & $dif & " hours " & @LF)
    Until $dif > $time_frame ; run given iteration number
EndIf

LogWrite(StringFormat(@LF, "Scenario execution time: %d %s", $dif, @LF))

;############################################ Exit Eclipse
ExitImpl(0)




; User-defined functions

Func Main($iteration)
LogWrite(@LF & "-------------------- Iteration " & $iteration & " start ------------------" & @LF)

; Clean projects
LogWrite(@LF & "Clean all Geronimo modules: " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Project->Clean..." & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Project", "Clea&n..." )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Project->Clean' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Project->Clean' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Clean' window to appear " & @LF)
WinWaitImpl("Clean","",60)
If Not WinActive("Clean","") Then WinActivate("Clean","")
WinWaitActiveImpl("Clean","",30)
LogWrite(@TAB & "window appeared " & @LF)
If $iteration = 1 Then
    LogWrite(@TAB & "Uncheck start build immediately" & @LF)
    Send("!b")
EndIf
LogWrite(@TAB & "Clean" & @LF)
Send("{ENTER}")
Dim $cleaning = WinWait("Cleaning selected projects", "", 20)
If $cleaning = 0 Then
        SleepImpl(3000)
Else
    LogWrite(@TAB & "Window 'Cleaning selected projects' appeared " & @LF)
    WinWaitCloseImpl("Cleaning selected projects", "", 600) ; 10min
EndIf
SleepImpl(6000)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Check clean result " & @LF)
If StringInStr(StringReplace(WinGetText("Java - Eclipse SDK"), @LF, " "), " 0 errors") = 0 Then
    LogWrite(@TAB & "ERROR! Errors in geronimo project after clean. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Errors in geronimo project after clean. STOP." & @LF)
    ExitImpl(1)
EndIf

; Build all projects
LogWrite(@LF & "Build all Geronimo modules: " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Project->Build All" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Project", "Build &All" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Project->Build All' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Project->Build All' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Building Workspace' window to appear " & @LF)
WinWaitActiveImpl("Building Workspace","",30)
LogWrite(@TAB & "window appeared " & @LF)
WinWaitCloseImpl("Building Workspace", "", 600) ; 10min
SleepImpl(6000)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Check build result" & @LF)
Send("!wvpp{ENTER}")
If StringInStr(StringReplace(WinGetText("Java - Eclipse SDK"), @LF, " "), " 0 errors") = 0 Then
    LogWrite(@TAB & "ERROR! Errors occurred during full build. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Errors occurred during full build. STOP." & @LF)
    ExitImpl(1)
EndIf
Send("!wvc")
SleepImpl(3000)

; Open Navigator
LogWrite(@LF & "Open Navigator view: " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
Send("!w")
Send("v")
Send("n")
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)

; Fix org.apache.geronimo.common.propertyeditor.PropertyEditorsTest
If $iteration = 1 Then
    LogWrite(@LF & "Fix org.apache.geronimo.common.propertyeditor.PropertyEditorsTest: " & @LF)
    LogWrite(@TAB & "Select 'Navigate->Open Resource'" & @LF)
    $res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Navigate", "Open Reso&urce..." )
    if $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Navigate->Open Resource' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Navigate->Open Resource' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
    LogWrite(@TAB & "wait 'Open Resource' window to appear " & @LF)
    WinWaitImpl("Open Resource","",60)
    If Not WinActive("Open Resource","") Then WinActivate("Open Resource","")
    WinWaitActiveImpl("Open Resource","",30)
    LogWrite(@TAB & "window appeared " & @LF)
    LogWrite(@TAB & "Type PropertyEditorsTest.java into 'Open Resource' dialog" & @LF)
    ControlFocus("Open Resource", "", "Edit1")
    ControlSend("Open Resource", "", "Edit1", "PropertyEditorsTest.java")
    ControlFocus("Open Resource", "OK", "Button2")
    ControlSend("Open Resource", "OK", "Button2", "{ENTER}")
    WinWaitCloseImpl("Open Resource", "", 60)
    LogWrite(@TAB & "wait 'Java - PropertyEditorsTest.java - Eclipse SDK' window to appear " & @LF)
    WinWaitImpl("Java - PropertyEditorsTest.java - Eclipse SDK","",60)
    If Not WinActive("Java - PropertyEditorsTest.java - Eclipse SDK","") Then WinActivate("Java - PropertyEditorsTest.java - Eclipse SDK","")
    WinWaitActiveImpl("Java - PropertyEditorsTest.java - Eclipse SDK","",30)
    LogWrite(@TAB & "window appeared " & @LF)
    Sleep(3000) ; just in case
    LogWrite(@TAB & "Go to line 'protected void setUp() throws Exception {'" & @LF)
    $res=WinMenuSelectItem("Java - PropertyEditorsTest.java - Eclipse SDK", "", "&Navigate", "&Go to Line...")
    If $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Navigate->Goto to Line...' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Navigate->Goto to Line...' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
    LogWrite(@TAB & "wait for window 'Go to Line' to appear, " & @LF)
    WinWaitImpl("Go to Line","",60)
    If Not WinActive("Go to Line","") Then WinActivate("Go to Line","")
    WinWaitActiveImpl("Go to Line","",30)
    LogWrite(@TAB & "window appeared" & @LF)
    ControlFocus("Go to Line", "", "Edit1")
    ControlSetText("Go to Line", "", "Edit1", "52")
    ControlFocus("Go to Line", "OK", "Button1")
    ControlClick("Go to Line", "OK", "Button1") 
    If @error=1 Then     
        LogWrite(@TAB & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
        ExitImpl(1)
    EndIf
    WinWaitCloseImpl("Go to Line", "", 60)
    WinWaitImpl("Java - PropertyEditorsTest.java - Eclipse SDK","",60)
    If Not WinActive("Java - PropertyEditorsTest.java - Eclipse SDK","") Then WinActivate("Java - PropertyEditorsTest.java - Eclipse SDK","")
    WinWaitActiveImpl("Java - PropertyEditorsTest.java - Eclipse SDK","",30)
    Sleep(3000) ; just in case
    LogWrite(@TAB & "Add line Class.forName(""org.apache.geronimo.common.propertyeditor.PropertyEditors""); to setUp() method" & @LF)
    Send("{END}{ENTER}")
    Send("Class.forName(""org.apache.geronimo.common.propertyeditor.PropertyEditors""{END};")
    SleepImpl(3000) ; just in case
    LogWrite(@TAB & "Save PropertyEditorsTest.java " & @LF)
    Send("^s")
    SleepImpl(3000) ; just in case
    LogWrite(@TAB & "Close PropertyEditorsTest.java " & @LF)
    $res=WinMenuSelectItem("Java - PropertyEditorsTest.java - Eclipse SDK", "", "&File", "&Close" )
    if $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
    WinWaitCloseImpl("Java - PropertyEditorsTest.java - Eclipse SDK", "", 60)
    SleepImpl(3000) ; just in case
    WinWaitImpl("Java - Eclipse SDK", "", 60)
    If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
    WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
EndIf

; Run JUnit tests from geronimo-common module
LogWrite(@LF & "Run JUnit tests from geronimo-common module: " & @LF)
LogWrite(@TAB & "Select geronimo-common module" & @LF)
Send("{DOWN 7}")
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Run->Run..." & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Run", "Ru&n..." )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Run->Run' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Run->Run' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Run' window to appear " & @LF)
WinWaitImpl("Run","",60)
If Not WinActive("Run","") Then WinActivate("Run","")
WinWaitActiveImpl("Run","",30)
LogWrite(@TAB & "window appeared " & @LF)
SleepImpl(3000)
LogWrite(@TAB & "Create new JUnit run configuration " & @LF)
Send("{TAB}")
SleepImpl(3000)
Send("{DOWN 4}")
SleepImpl(3000)
Send("{ENTER}")
SleepImpl(3000)
LogWrite(@TAB & "Run" & @LF)
Send("!r")
WinWaitCloseImpl("Run", "", 60)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
While StringInStr(WinGetText("Java - Eclipse SDK"), "Runs:") = 0 
    SleepImpl(1000)
WEnd
SleepImpl(6000) ; just in case
LogWrite(@TAB & "Check run result" & @LF)
If StringInStr(WinGetText("Java - Eclipse SDK"), "Errors: " & @LF & "0") = 0 Then
    LogWrite(@TAB & "ERROR! Geronimo-common JUnit test errors. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Geronimo-common JUnit test errors. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)

; Modify GBeanData class from geronimo-kernel module
LogWrite(@LF & "Modify GBeanData class from geronimo-kernel module: " & @LF)
Send("!w")
Send("v")
Send("n")
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select geronimo-kernel module" & @LF)
Send("{DOWN 9}")
LogWrite(@TAB & "Select Navigate->Go Into" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Navigate", "Go &Into" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Navigate->Go Into' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Navigate->Go Into' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "Open GBeanData.java" & @LF)
Send("{DOWN}s{ENTER}m{ENTER}j{ENTER}o{ENTER}a{ENTER}g{ENTER}g{ENTER}g{DOWN}{ENTER}")
LogWrite(@TAB & "wait 'Java - GBeanData.java - Eclipse SDK' window to appear " & @LF)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK","",60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK","") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK","",30)
LogWrite(@TAB & "window appeared " & @LF)
SleepImpl(3000)
LogWrite(@TAB & "Select Edit->Find/Replace..." & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Edit", "&Find/Replace...")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Edit->Find/Replace...' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Edit->Find/Replace...' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Find/Replace' window to appear " & @LF)
WinWaitImpl("Find/Replace","",60)
If Not WinActive("Find/Replace","") Then WinActivate("Find/Replace","")
WinWaitActiveImpl("Find/Replace","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Find getAbstractName() method " & @LF)
Send("getAbstractName")
Send("{ENTER}")
WinWaitImpl("Find/Replace","",60)
If Not WinActive("Find/Replace","") Then WinActivate("Find/Replace","")
WinWaitActiveImpl("Find/Replace","",30)
Send("{ESC}")
WinWaitCloseImpl("Find/Replace", "", 60)
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
Send("{END}")
LogWrite(@TAB & "Add System.out.println(""Method getAbstractName() was called!""); " & @LF)
Send("{ENTER}")
Send("System.out.println(""Method getAbstractName() was called{!}""" & "{END};")
LogWrite(@TAB & "Save GBeanData.java " & @LF)
Send("^s")
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)

; Create JUnit test
LogWrite(@LF & "Create JUnit test: " & @LF)
Send("!w")
Send("v")
Send("n")
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Open geronimo-kernel/src/test/java/org/apache/geronimo/gbean folder" & @LF)
Send("t{ENTER}j{ENTER}o{ENTER}a{ENTER}g{ENTER}g{ENTER}")
LogWrite(@TAB & "Select File->New->JUnit Test Case" & @LF)
Send("!f")
Send("n")
Send("{DOWN 10}")
Send("{ENTER}")
LogWrite(@TAB & "wait 'New JUnit Test Case' window to appear " & @LF)
WinWaitImpl("New JUnit Test Case","",60)
If Not WinActive("New JUnit Test Case","") Then WinActivate("New JUnit Test Case","")
WinWaitActiveImpl("New JUnit Test Case","",30)
LogWrite(@TAB & "Enter SimpleGBeanDataTest as test name " & @LF)
Send("!m")
Send("SimpleGBeanDataTest")
LogWrite(@TAB & "Enter org.apache.geronimo.gbean.GBeanData as class under test " & @LF)
Send("!l")
Send("org.apache.geronimo.gbean.GBeanData")
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Press Next button " & @LF)
ControlFocus("New JUnit Test Case", "&Next >", "Button18") 
ControlClick("New JUnit Test Case", "&Next >", "Button18") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! New JUnit Test Case->Next button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! New JUnit Test Case->Next button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Enter getAbstractName() as method under test " & @LF)
Send("{DOWN 5}{SPACE}")
LogWrite(@TAB & "Press Finish button " & @LF)
Send("!f")
WinWaitCloseImpl("New JUnit Test Case", "", 60)
WinWaitImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", 60)
If Not WinActive("Java - SimpleGBeanDataTest.java - Eclipse SDK", "") Then WinActivate("Java - SimpleGBeanDataTest.java - Eclipse SDK","")
WinWaitActiveImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Find string fail(""Not yet implemented"");, " & @LF)
Send("{DOWN 7}{END}")
LogWrite(@TAB & "Delete string fail(""Not yet implemented"");, " & @LF)
Send("+{HOME}")
Send("{DEL}")
LogWrite(@TAB & "Add string System.out.println(new GBeanData().getAbstractName());, " & @LF)
Send("System.out.println(new GBeanData().getAbstractName()" & "{END};")
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Save SimpleGBeanDataTest.java " & @LF)
Send("^s")
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Close SimpleGBeanDataTest.java " & @LF)
$res=WinMenuSelectItem("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", "&File", "&Close" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", 60)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)

; Build project
LogWrite(@LF & "Build geronimo-kernel module: " & @LF)
Send("!w")
Send("v")
Send("n")
Send("{SPACE}")
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Project->Build Project " & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Project", "&Build Project" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Project->Build Project' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Project->Build Project' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(6000) ; build project
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Check build result" & @LF)
Send("!wvpp{ENTER}")
If StringInStr(StringReplace(WinGetText("Java - GBeanData.java - Eclipse SDK"), @LF, " "), " 0 errors") = 0 Then
    LogWrite(@TAB & "ERROR! Geronimo-kernel build errors. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Geronimo-kernel build errors. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000) ; just in case

; Run SimpleGBeanDataTest
LogWrite(@LF & "Run SimpleGBeanDataTest: " & @LF)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
Send("!wvn")
LogWrite(@TAB & "Select Run->Run As->JUnit Test" & @LF)
Send("!r")
Send("s")
Send("1")
While StringInStr(WinGetText("Java - GBeanData.java - Eclipse SDK"), "<terminated>") = 0 
    SleepImpl(1000)
WEnd
SleepImpl(6000) ; just in case 
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Check GBeanTest run result" & @LF)
If StringInStr(WinGetText("Java - GBeanData.java - Eclipse SDK"), "Errors: " & @LF & "0") = 0 Then
    LogWrite(@TAB & "ERROR! Junit test run errors. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Junit test run errors. STOP." & @LF)
    ExitImpl(1)
EndIf

; Set breakpoint
LogWrite(@LF & "Set breakpoint in GBeanData.java: " & @LF)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK","",60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK","") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK","",30)
Send("{F12}") ; switch to main pane
LogWrite(@TAB & "Go to line System.out.println(""Method getAbstractName() was called!"");" & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Navigate", "&Go to Line...")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Navigate->Goto Line...' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Navigate->Goto Line...' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait for window 'Go to Line' to appear, " & @LF)
WinWaitImpl("Go to Line","",60)
If Not WinActive("Go to Line","") Then WinActivate("Go to Line","")
WinWaitActiveImpl("Go to Line","",30)
LogWrite(@TAB & "window appeared" & @LF)
ControlFocus("Go to Line", "", "Edit1")
ControlSetText("Go to Line", "", "Edit1", "75")
ControlFocus("Go to Line", "OK", "Button1")
ControlClick("Go to Line", "OK", "Button1") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Go to Line", "", 60)
LogWrite(@TAB & "Set breakpoint at line 75 System.out.println(""Method getAbstractName() was called!"");" & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Run", "Toggle Line Brea&kpoint")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Run->Toggle Line Breakpoint' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Run->Toggle Line Breakpoint' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000)

; Debug
LogWrite(@LF & "Debug: " & @LF)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Run->Debug..." & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Run", "De&bug...")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Run->Debug...' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Run->Debug...' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Debug' window to appear " & @LF)
WinWaitImpl("Debug","",60)
If Not WinActive("Debug","") Then WinActivate("Debug","")
WinWaitActiveImpl("Debug","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select GBeanTest run configuration " & @LF)
SleepImpl(3000)
Send("{ENTER}")
SleepImpl(3000)
If StringLen($vm_debug_options) > 0 Then
    LogWrite(@TAB & "Add vm options '" & $vm_debug_options & "' for debug step " & @LF)
     Send("{TAB 2}{RIGHT}")
     SleepImpl(3000)
     Send("!g")
     SleepImpl(3000)
     Send($vm_debug_options)
     SleepImpl(3000)
     Send("!y")
EndIf
LogWrite(@TAB & "Press Debug button" & @LF)
Send("{ENTER}")
LogWrite(@TAB & "wait 'Confirm Perspective Switch' window to appear " & @LF)
WinWaitImpl("Confirm Perspective Switch","",60)
If Not WinActive("Confirm Perspective Switch","") Then WinActivate("Confirm Perspective Switch","")
WinWaitActiveImpl("Confirm Perspective Switch","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Press Yes button" & @LF)
ControlFocus("Confirm Perspective Switch", "&Yes", "Button2")
ControlClick("Confirm Perspective Switch", "&Yes", "Button2") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Confirm Perspective Switch->Yes button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Confirm Perspective Switch->Yes button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Confirm Perspective Switch", "", 60)
WinWaitImpl("Debug - GBeanData.java - Eclipse SDK","",60)
If Not WinActive("Debug - GBeanData.java - Eclipse SDK","") Then WinActivate("Debug - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Debug - GBeanData.java - Eclipse SDK","",30)
If StringInStr(WinGetText("Debug - GBeanData.java - Eclipse SDK"), "javaw.exe") = 0 Then
    LogWrite(@TAB & "ERROR! Debug wasn't started. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Debug wasn't started. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000)
LogWrite(@TAB & "Step over " & @LF)
$res=WinMenuSelectItem("Debug - GBeanData.java - Eclipse SDK", "", "&Run", "Step &Over")
If $res=0 Then
    Sleep(6000)
    $res=WinMenuSelectItem("Debug - GBeanData.java - Eclipse SDK", "", "&Run", "Step &Over")
    If $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
EndIf
; Console: Method getAbstractName() was called!
If StringInStr(WinGetText("Debug - GBeanData.java - Eclipse SDK"), "javaw.exe") = 0 Then
    LogWrite(@TAB & "ERROR! Debug run stopped. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Debug run stopped. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Step over " & @LF)
$res=WinMenuSelectItem("Debug - GBeanData.java - Eclipse SDK", "", "&Run", "Step &Over")
If $res=0 Then
    Sleep(6000)
    $res=WinMenuSelectItem("Debug - GBeanData.java - Eclipse SDK", "", "&Run", "Step &Over")
    If $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
EndIf
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Step over " & @LF)
$res=WinMenuSelectItem("Debug - SimpleGBeanDataTest.java - Eclipse SDK", "", "&Run", "Step &Over")
If $res=0 Then
    Sleep(6000)
    $res=WinMenuSelectItem("Debug - SimpleGBeanDataTest.java - Eclipse SDK", "", "&Run", "Step &Over")
    If $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Run->Step Over' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
EndIf
; Console: null
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Resume " & @LF)
$res=WinMenuSelectItem("Debug - SimpleGBeanDataTest.java - Eclipse SDK", "", "&Run", "Resu&me")
If $res=0 Then
    Sleep(6000)
    $res=WinMenuSelectItem("Debug - SimpleGBeanDataTest.java - Eclipse SDK", "", "&Run", "Resu&me")
    If $res=0 Then
        LogWrite(@TAB & "ERROR! Menu 'Run->Resume' wasn't found. Stop." & @LF)
        PrintErrorScreen()
        ConsoleWrite(@LF & "ERROR! Menu 'Run->Resume' wasn't found. Stop." & @LF)
        ExitImpl(1)
    EndIf
EndIf
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Check debug result" & @LF)
WinWaitImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK","",60)
If Not WinActive("Debug - SimpleGBeanDataTest.java - Eclipse SDK","") Then WinActivate("Debug - SimpleGBeanDataTest.java - Eclipse SDK","")
WinWaitActiveImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK","",30)
If StringInStr(WinGetText("Debug - SimpleGBeanDataTest.java - Eclipse SDK"), "<terminated> SimpleGBeanDataTest [JUnit]") = 0 Then
    LogWrite(@TAB & "ERROR! Debug was not terminated. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Debug was not terminated. STOP." & @LF)
    ExitImpl(1)
EndIf
If StringInStr(WinGetText("Debug - SimpleGBeanDataTest.java - Eclipse SDK"), "Runs: " & @LF & "1/1" & @LF & "Errors: " & @LF & "0" & @LF & "Failures: " & @LF & "0") = 0 Then
    LogWrite(@TAB & "ERROR! Debug errors. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Debug errors. STOP." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000)

; Switch to Java perspective
LogWrite(@LF & "Switch to Java perspective: " & @LF)
WinWaitImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK","",60)
If Not WinActive("Debug - SimpleGBeanDataTest.java - Eclipse SDK","") Then WinActivate("Debug - SimpleGBeanDataTest.java - Eclipse SDK","")
WinWaitActiveImpl("Debug - SimpleGBeanDataTest.java - Eclipse SDK","",30)
LogWrite(@TAB & "Switch perspective" & @LF)
Send("!w")
Send("o")
Send("{ENTER}")
LogWrite(@TAB & "wait for window 'Java - SimpleGBeanDataTest.java - Eclipse SDK' to appear, " & @LF)
WinWaitImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK","",60)
If Not WinActive("Java - SimpleGBeanDataTest.java - Eclipse SDK","") Then WinActivate("Java - SimpleGBeanDataTest.java - Eclipse SDK","")
WinWaitActiveImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK","",30)
LogWrite(@TAB & "window appeared" & @LF)
LogWrite(@TAB & "Close SimpleGBeanDataTest.java" & @LF)
$res=WinMenuSelectItem("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", "&File", "&Close" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Java - SimpleGBeanDataTest.java - Eclipse SDK", "", 60)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK", "") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK", "", 30)

; Unset breakpoint
LogWrite(@LF & "Unset breakpoint: " & @LF)
Send("{F12}") ; switch to main pane
LogWrite(@TAB & "Select Navigate->Go to Line..." & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Navigate", "&Go to Line...")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Navigate->Goto Line...' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Navigate->Goto Line...' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Go to Line' window to appear " & @LF)
WinWaitImpl("Go to Line","",60)
If Not WinActive("Go to Line","") Then WinActivate("Go to Line","")
WinWaitActiveImpl("Go to Line","",30)
LogWrite(@TAB & "window appeared" & @LF)
LogWrite(@TAB & "Select line 75 of GBeanData.java" & @LF)
ControlFocus("Go to Line", "", "Edit1")
ControlSetText("Go to Line", "", "Edit1", "75")
LogWrite(@TAB & "Press OK button" & @LF)
ControlFocus("Go to Line", "OK", "Button1")
ControlClick("Go to Line", "OK", "Button1") 
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Go to Line->OK button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Go to Line", "", 60)
LogWrite(@TAB & "Select Run->Toggle Line Breakpoint to unset breakpoint" & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&Run", "Toggle Line Brea&kpoint")
If $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Run->Toggle Line Breakpoint' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Run->Toggle Line Breakpoint' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
SleepImpl(3000) ; just in case

; Remove GBeanData.java modifications
LogWrite(@LF & "Remove GBeanData.java modifications: " & @LF)
WinWaitImpl("Java - GBeanData.java - Eclipse SDK","",60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK","") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK","",30)
LogWrite(@TAB & "Delete string System.out.println(""Method getAbstractName() was called!""); " & @LF)
Send("{END}")
Send("+{HOME}")
Send("{DEL}")
;Send("{BACKSPACE 6}")
LogWrite(@TAB & "Save GBeanData.java " & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&File", "&Save" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Save' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Save' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
WinWaitImpl("Java - GBeanData.java - Eclipse SDK","",60)
If Not WinActive("Java - GBeanData.java - Eclipse SDK","") Then WinActivate("Java - GBeanData.java - Eclipse SDK","")
WinWaitActiveImpl("Java - GBeanData.java - Eclipse SDK","",30)
LogWrite(@TAB & "Close GBeanData.java " & @LF)
$res=WinMenuSelectItem("Java - GBeanData.java - Eclipse SDK", "", "&File", "&Close" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'File->Close' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Java - GBeanData.java - Eclipse SDK", "", 60)
SleepImpl(3000) ; just in case
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)

; Remove JUnit test
LogWrite(@LF & "Remove JUnit test: " & @LF)
Send("!w")
Send("v")
Send("n")
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Select Edit->Delete" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Edit", "&Delete" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Edit->Delete' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Edit->Delete' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Confirm Resource Delete' window to appear" & @LF)
WinWaitImpl("Confirm Resource Delete", "", 60)
If Not WinActive("Confirm Resource Delete", "") Then WinActivate("Confirm Resource Delete","")
WinWaitActiveImpl("Confirm Resource Delete", "", 30)
LogWrite(@TAB & "window appeared" & @LF)
LogWrite(@TAB & "Press OK button" & @LF)
Send("{ENTER}")
WinWaitCloseImpl("Confirm Resource Delete", "", 60)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)

; Remove run configurations
LogWrite(@LF & "Remove run configurations: " & @LF)
LogWrite(@TAB & "Select Run->Run..." & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Run", "Ru&n..." )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Run->Run' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Run->Run' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Run' window to appear " & @LF)
WinWaitImpl("Run","",60)
If Not WinActive("Run","") Then WinActivate("Run","")
WinWaitActiveImpl("Run","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Delete SimpleGBeanDataTest run configuration " & @LF)
SleepImpl(3000)
Send("{ENTER}")
SleepImpl(3000)
Send("{DEL}")
LogWrite(@TAB & "wait 'Confirm Launch Configuration Deletion' window to appear " & @LF)
WinWaitImpl("Confirm Launch Configuration Deletion","",60)
If Not WinActive("Confirm Launch Configuration Deletion","") Then WinActivate("Confirm Launch Configuration Deletion","")
WinWaitActiveImpl("Confirm Launch Configuration Deletion","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Press Yes button " & @LF)
ControlFocus("Confirm Launch Configuration Deletion", "&Yes", "Button1")
ControlClick("Confirm Launch Configuration Deletion", "&Yes", "Button1")
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Confirm Launch Configuration Deletion->Yes button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Confirm Launch Configuration Deletion->Yes button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Confirm Launch Configuration Deletion", "", 60)
LogWrite(@TAB & "Delete geronimo-common run configuration" & @LF)
Send("{DEL}")
LogWrite(@TAB & "wait 'Confirm Launch Configuration Deletion' window to appear " & @LF)
WinWaitImpl("Confirm Launch Configuration Deletion","",60)
If Not WinActive("Confirm Launch Configuration Deletion","") Then WinActivate("Confirm Launch Configuration Deletion","")
WinWaitActiveImpl("Confirm Launch Configuration Deletion","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Press Yes button " & @LF)
ControlFocus("Confirm Launch Configuration Deletion", "&Yes", "Button1")
ControlClick("Confirm Launch Configuration Deletion", "&Yes", "Button1")
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Confirm Launch Configuration Deletion->Yes button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Confirm Launch Configuration Deletion->Yes button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Confirm Launch Configuration Deletion", "", 60)
Send("{HOME}")
LogWrite(@TAB & "Close 'Run' window" & @LF)
ControlFocus("Run", "Close", "Button5")
ControlClick("Run", "Close", "Button5")
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Run->Close button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Run->Close button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Run", "", 60)

; Reset perspective
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@LF & "Reset perspective: " & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Window", "&Reset Perspective" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Window->Reset Perspective' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Window->Reset Perspective' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait for window 'Reset Perspective' to appear, " & @LF)
WinWaitImpl("Reset Perspective", "", 60)
If Not WinActive("Reset Perspective", "") Then WinActivate("Reset Perspective","")
WinWaitActiveImpl("Reset Perspective", "", 30)
LogWrite(@TAB & "window appeared" & @LF)
LogWrite(@TAB & "Press OK" & @LF)
ControlFocus("Reset Perspective", "OK", "Button1")
ControlClick("Reset Perspective", "OK", "Button1")
If @error=1 Then     
    LogWrite(@TAB & "ERROR! Reset Perspective->Close button wasn't found. STOP." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Reset Perspective->Close button wasn't found. STOP." & @LF)
    ExitImpl(1)
EndIf
WinWaitCloseImpl("Reset Perspective", "", 60)
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Restore folders structure in package explorer view" & @LF)
Send("{UP 5}{NUMPADSUB}{UP 1}{NUMPADSUB}{UP 4}{NUMPADSUB}{HOME}")
SleepImpl(3000) ; just in case

; Help
LogWrite(@LF & "Help: " & @LF)
LogWrite(@TAB & "Select Help->Help Contents" & @LF)
$res=WinMenuSelectItem("Java - Eclipse SDK", "", "&Help", "&Help Contents" )
if $res=0 Then
    LogWrite(@TAB & "ERROR! Menu 'Help->Help Contents' wasn't found. Stop." & @LF)
    PrintErrorScreen()
    ConsoleWrite(@LF & "ERROR! Menu 'Help->Help Contents' wasn't found. Stop." & @LF)
    ExitImpl(1)
EndIf
LogWrite(@TAB & "wait 'Help - Eclipse SDK' window to appear " & @LF)
WinWaitImpl("Help - Eclipse SDK","",60)
If Not WinActive("Help - Eclipse SDK") Then WinActivate("Help - Eclipse SDK")
WinWaitActiveImpl("Help - Eclipse SDK","",30)
LogWrite(@TAB & "'Help - Eclipse SDK' window opened " & @LF)
SleepImpl(3000) ; just in case
LogWrite(@TAB & "Close Help " & @LF)
WinClose("Help - Eclipse SDK")
WinWaitCloseImpl("Help - Eclipse SDK", "", 60)
LogWrite(@TAB & "End Help " & @LF)
SleepImpl(3000) ; just in case

LogWrite(@LF & "-------------------- Iteration " & $iteration & " end --------------------" & @LF)
SleepImpl(3000) ; just in case
EndFunc

Func LogWrite($string)
    Dim Const $logfile = $resultsDir & "\EGA.log"
    Local Const $log = FileOpen($logfile, 1)    
    FileWrite($logfile, $string)
    FileClose($log)
EndFunc

Func SleepImpl($sleep_time)
    Sleep($sleep_time*$delay_factor)
EndFunc

Func WinWaitImpl($wname, $wtext, $wdelay) 
    Dim $newdelay = 120
    Dim $wreturn = WinWait($wname, $wtext, $wdelay)
    If $wreturn=0 Then
        LogWrite(@TAB & "WARNING! Window '" & $wname & "' didn't appear during " & $wdelay & "c. Add extra delay for " & $newdelay & "c." & @LF)
        $wreturn = WinWait($wname, $wtext, $newdelay)
        If $wreturn=0 Then
            LogWrite(@TAB & "ERROR! Window '" & $wname & "' didn't appear during additional " & $newdelay & "c. STOP." & @LF)
            PrintErrorScreen()
            ConsoleWrite(@LF & "ERROR! Window '" & $wname & "' didn't appear during additional " & $newdelay & "c. STOP." & @LF)
            ExitImpl(1)
        Else
            LogWrite(@TAB & "Window '" & $wname & "' appeared in additional time!" & @LF)
        EndIf
    EndIf
EndFunc


Func WinWaitActiveImpl($wname, $wtext, $wdelay) 
    Dim $newdelay =  120
    Dim $wreturn = WinWaitActive($wname, $wtext, $wdelay)
    If $wreturn=0 Then
        LogWrite(@TAB & "WARNING! Window '" & $wname & "' didn't become active during " & $wdelay & "c. Add extra delay for " & $newdelay & "c." & @LF)
        $wreturn = WinWaitActive($wname, $wtext, $newdelay)
        If $wreturn=0 Then
            LogWrite(@TAB & "ERROR! Window '" & $wname & "' didn't become active during additional " & $newdelay & "c. STOP." & @LF)
            PrintErrorScreen()
            ConsoleWrite(@LF & "ERROR! Window '" & $wname & "' didn't become active during additional " & $newdelay & "c. STOP." & @LF)
            ExitImpl(1)
        Else
            LogWrite(@TAB & "Window '" & $wname & "' became active in additional time!" & @LF)
        EndIf
    EndIf
EndFunc

Func WinWaitCloseImpl($wname, $wtext, $wdelay) 
    Dim $newdelay = 120
    LogWrite(@TAB & "Closing window '" & $wname & "'" & @LF)
    Dim $wreturn = WinWaitClose($wname, $wtext, $wdelay)
    If $wreturn=0 Then
        LogWrite(@TAB & "WARNING! Window '" & $wname & "' didn't close during " & $wdelay & "c. Add extra delay for " & $newdelay & "c." & @LF)
        $wreturn = WinWaitClose($wname, $wtext, $newdelay)
        If $wreturn=0 Then
            LogWrite(@TAB & "ERROR! Window '" & $wname & "' didn't close during additional " & $newdelay & "c. STOP." & @LF)
            PrintErrorScreen()
            ConsoleWrite(@LF & "ERROR! Window '" & $wname & "' didn't close during additional " & $newdelay & "c. STOP." & @LF)
            ExitImpl(1)
        Else
            LogWrite(@TAB & "Window '" & $wname & "' closed in additional time!" & @LF)
        EndIf
    EndIf
EndFunc

Func ExitImpl($error_code)
    Opt("WinTitleMatchMode",2)
    LogWrite(@LF & "Exit Eclipse: " & @LF)
    If WinExists("Eclipse SDK") Then
        If $error_code = 0 Then ExitEclipse($error_code)
        If $error_code = 1 AND $kill_eclipse = 1 Then ExitEclipse($error_code)
    Else
        LogWrite(@TAB & "Eclipse wasn't found" & @LF)
        $error_code = 1
    EndIf
    If $error_code = 0 Then 
        LogWrite(@LF & "EGA PASSED!" & @LF)
    Else 
        LogWrite(@LF & "EGA FAILED!" & @LF)
    EndIf
    Sleep(10000)
    FileCopy(@WorkingDir & "/workspace/.metadata/.log", $resultsDir  & "/workspace.log", 1)
    Exit($error_code)
EndFunc

Func ExitEclipse($error_code)
    If Not WinActive("Eclipse SDK","") Then WinActivate("Eclipse SDK","") 
    $wr = WinWaitActive("Eclipse SDK", "", 30)
    If $wr=1 Then
        $title = WinGetTitle("Eclipse SDK")
        $exit = WinMenuSelectItem($title, "", "&File", "E&xit" )
        If $exit=1 Then
            Send("!f")
            Send("x")
        Else 
            LogWrite(@TAB & "Menu """ & $title & "->File->Exit"" wasn't found" & @LF)
            KillEclipse()
        EndIf
        WinWaitClose("Progress Information", "", 120)
        $wr = WinWaitClose("Eclipse SDK", "", 30)
        If $wr=1 Then
            LogWrite(@TAB & "Eclipse was found and closed." & @LF)
        Else 
            KillEclipse()
        EndIf
    Else
        KillEclipse()
    EndIf
EndFunc

Func KillEclipse() 
    LogWrite(@TAB & "Can't exit Eclipse! Trying to kill ..." & @LF)
    WinKill("Eclipse SDK")
    If Not WinExists("Eclipse SDK") Then
        LogWrite(@TAB & "Eclipse window was found and killed" & @LF)
    Else 
        $pid = WinGetProcess("Eclipse SDK")
        If ProcessExists($pid) Then
            ProcessClose($pid)
        EndIf
        Sleep(3000)
        If ProcessExists($pid) Then
            LogWrite(@TAB & "Can't kill Eclipse!" & @LF)
        Else
            LogWrite(@TAB & "Eclipse was killed!" & @LF)
        EndIf
    EndIf
EndFunc

Func PrintErrorScreen()
    Opt("WinTitleMatchMode", 2)
    LogWrite(@LF & "An ERROR has ocurred!" & @LF)
    If Not WinExists("Eclipse SDK") Then
        ConsoleWrite(@LF & "ERROR: Window 'Eclipse SDK' doesn't exist!" & @LF)
        ExitImpl(1)
    EndIf
    LogWrite(@LF & "Print error screen:" & @LF)
     $file = FileOpen($resultsDir & "\error.bmp", 2)
    FileClose($file) 
    Send("{PRINTSCREEN}")
    LogWrite(@TAB & "Open mspaint to save errror screen" & @LF)
    Run("mspaint " & $resultsDir & "\error.bmp")
    LogWrite(@TAB & "Wait for 'error - Paint' window" & @LF)
    $result = WinWait(" - Paint", "", 180)
    If $result=0 Then
            LogWrite(@TAB & "ERROR! Paint window didn't appear during 3 min. Can't save error screen." & @LF)
            ConsoleWrite(@LF & "ERROR! Paint window didn't appear during 3 min. Can't save error screen." & @LF)
            ExitImpl(1)
    EndIf
    If Not WinActive(" - Paint") Then WinActivate("- Paint")
    WinWaitActive(" - Paint", "", 180)
    $result = WinMenuSelectItem(" - Paint", "", "&Edit", "&Paste")
    If $result = 0 Then
        LogWrite(@TAB & "Mspaint menu 'Edit -> Paste' wasn't found" & @LF)
        LogWrite(@TAB & "Can't save error picture!" & @LF)
        ConsoleWrite(@LF & "Mspaint menu 'Edit -> Paste' wasn't found" & @LF & "Can't save error picture!" & @LF)
        ExitImpl(1)
    EndIf
    LogWrite(@TAB & "Save error screen" & @LF)
    $result = WinMenuSelectItem(" - Paint", "", "&File", "&Save")
    if $result = 0 Then
        LogWrite(@TAB & "Mspaint menu 'File -> Save' wasn't found" & @LF)
        LogWrite(@TAB & "Can't save error picture!" & @LF)
        ConsoleWrite(@LF & "Mspaint menu 'File -> Save' wasn't found" & @LF & "Can't save error picture!" & @LF)
        ExitImpl(1)
    EndIf
    LogWrite(@TAB & "Exit mspaint" & @LF)
    $result = WinMenuSelectItem(" - Paint", "", "&File", "E&xit")
    if $result = 0 Then
        LogWrite(@TAB & "Mspaint menu 'File -> Exit' wasn't found"& @LF & "Can't exit mspaint!" & @LF)
        ConsoleWrite(@LF & "Mspaint menu 'File -> Exit' wasn't found" & @LF & "Can't exit mspaint!" & @LF)
        Exitimpl(1)
    EndIf
    WinWaitClose(" - Paint", "", 30)
    If WinExists("Eclipse SDK") Then
        LogWrite(@TAB & "Switch to Eclipse" & @LF)
        If Not WinActive("Eclipse SDK") Then WinActivate("Eclipse SDK")
        WinWaitActive("Eclipse SDK", "", 120)
    EndIf
    Opt("WinTitleMatchMode", 4)
EndFunc


; 

