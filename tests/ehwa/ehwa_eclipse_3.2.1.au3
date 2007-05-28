
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

; Set options
Opt("WinWaitDelay", 1000) ; wait for 1c
Opt("SendKeyDelay", 150) ; wait for 150mc
Opt("WinTitleMatchMode", 4)
Opt("WinDetectHiddenText", 1)
Opt("TrayIconDebug", 1)

; Set variables
Select 
	Case $CmdLine[0] = 3 OR $CmdLine[0] = 4
		Dim Const $eclipse_home = $CmdLine[1]
		Dim Const $tested_jre = $CmdLine[2]
		Dim Const $resultsDir = $CmdLine[3]
		Dim $jre_options = ""
		If $CmdLine[0] = 4 Then
			$jre_options  = $CmdLine[4]
		EndIf
	Case Else 
		ConsoleWrite("ERROR! Wrong number of input parameters! STOP." & @LF & "EHWA FAILED!" & @LF)
		Sleep(3000)
		Exit(1)
EndSelect

LogWrite(@LF & "--- Automated Eclipse EHWA scenario for Eclipse 3.2.1 ---" & @LF & @LF)

; Check if some Eclipse window is already olpened
Opt("WinTitleMatchMode", 2)
If WinExists("Eclipse SDK") Then
	LogWrite("ERROR! Some Eclipse window is already opened. STOP." & @LF & "EHWA FAILED!" & @LF)
	Sleep(3000)
	Exit(1)
EndIf

LogWrite("Eclipse Home = " & $eclipse_home & @LF)
LogWrite("Tested JRE = " &  $tested_jre & @LF)
LogWrite("Tested JRE options for Eclipse launching = " & $jre_options & @LF)

; Start
LogWrite(@LF & "Start:" & @LF)
LogWrite(@TAB & "Run Eclipse" & @LF)
$run =  $tested_jre & "\bin\java.exe "  & $jre_options & " -cp """ & $eclipse_home & "\startup.jar"" -Dosgi.install.area=""" & $eclipse_home & """ org.eclipse.core.launcher.Main -debug > " & $resultsDir & "\eclipse.log 2>&1"
Run(@ComSpec & " /c " & $run, "", @SW_HIDE)


; 1
LogWrite(@TAB & "Wait Eclipse 'Workspace Launcher' window to appear " & @LF)
WinWaitImpl("Workspace Launcher", "", 180)
If Not WinActive("Workspace Launcher", "Select a workspace") Then WinActivate("Workspace Launcher")
WinWaitActiveImpl("Workspace Launcher", "", 30)
LogWrite(@TAB & "window appeared " & @LF)

; 2
LogWrite(@TAB & "Select " & @WorkingDir & "\workspace as workspace" & @LF)
ControlFocus("Workspace Launcher", "", "Edit1")
ControlSetText("Workspace Launcher", "", "Edit1", @WorkingDir & "\workspace" )
LogWrite(@TAB & "Press OK button" & @LF)
ControlSend("Workspace Launcher", "", "", "{ENTER}")
WinWaitCloseImpl("Workspace Launcher", "", 60)

; 3
LogWrite(@TAB & "Wait 'Java - Eclipse SDK' window to appear " & @LF)
WinWaitImpl("Java - Eclipse SDK", "", 180)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "window appeared " & @LF)
ControlFocus("Java - Eclipse SDK", "", "ToolbarWindow321") 
ControlClick("Java - Eclipse SDK", "", "ToolbarWindow321") 
If @error=1 Then 	
	LogWrite(@TAB & "ERROR! Welcome page window wasn't found. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
LogWrite(@TAB & "Close Welcome page " & @LF)
ControlFocus("Java - Eclipse SDK", "", "ToolbarWindow321") 
ControlSend("Java - Eclipse SDK", "", "ToolbarWindow321", "!+Q") 
WinWaitNotActive("Java - Eclipse SDK", "")
Send("p")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
Sleep(3000)

; 4
LogWrite(@TAB & "Create new  Java project" & @LF)
Send("!f")
Send("n")
Send("r")

; 5
LogWrite(@TAB & "Wait 'New Project' window to appear" & @LF)
WinWaitImpl("New Project","",60)
If Not WinActive("New Project","") Then WinActivate("New Project","")
WinWaitActiveImpl("New Project","",30)
LogWrite(@TAB & "window appeared " & @LF)
LogWrite(@TAB & "Select Java project" & @LF)
ControlFocus("New Project", "", "Edit1")
ControlSend("New Project", "", "Edit1", "{ENTER}")
LogWrite(@TAB & "Press Next button" & @LF)
ControlFocus("New Project", "&Next >", "Button2",)
ControlClick("New Project", "&Next >", "Button2") 
If @error=1 Then 	
	LogWrite(@TAB & "ERROR! Next button wasn't found. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
WinWaitCloseImpl("New Project", "", 60)

; 6
LogWrite(@TAB & "Wait 'New Java Project' window to appear " & @LF)
WinWaitImpl("New Java Project","",60)
If Not WinActive("New Java Project") Then WinActivate("New Java Project")
WinWaitActiveImpl("New Java Project","",30)
LogWrite(@TAB & "window appeared, " & @LF)
LogWrite(@TAB & "Type EHWA as a project name" & @LF)
ControlFocus("New Java Project", "", "Edit2") 
ControlSetText("New Java Project", "", "Edit2", "EHWA" )
LogWrite(@TAB & "Press Finish button" & @LF)
ControlFocus("New Java Project", "&Finish", "Button27",)
ControlClick("New Java Project", "&Finish", "Button27") 
If @error=1 Then 	
	LogWrite(@TAB & "ERROR! Finish button wasn't found. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
LogWrite(@TAB & "Wait while project is being created ... " & @LF)
WinWaitClose("New Java Project", "", 60)
LogWrite(@TAB & "Project was created" & @LF)

; 7
WinWaitImpl("Java - Eclipse SDK", "", 60)
If Not WinActive("Java - Eclipse SDK", "") Then WinActivate("Java - Eclipse SDK","")
WinWaitActiveImpl("Java - Eclipse SDK", "", 30)
LogWrite(@TAB & "Create new Java class" & @LF)
$result=WinMenuSelectItem("Java - Eclipse SDK", "", "&File", "&New", "Class")
if $result=0 Then
	LogWrite(@TAB & "ERROR! Menu 'File -> New -> Class' wasn't found. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
LogWrite(@TAB & "Wait 'New Java Class' window to appear" & @LF)
WinWaitImpl("New Java Class","",60)
If Not WinActive("New Java Class") Then WinActivate("New Java Class")
WinWaitActiveImpl("New Java Class","",30)
LogWrite(@TAB & "window appeared " & @LF)

; 8
LogWrite(@TAB & "Type EHWA as class name" & @LF)
ControlFocus("New Java Class", "", "Edit4")
ControlSetText("New Java Class", "", "Edit4", "EHWA" )

;9
LogWrite(@TAB & "Select 'public static void main(String[] args)' checkbox" & @LF)
ControlFocus("New Java Class", "", "Button15")
ControlCommand("New Java Class", "", "Button15", "Check", "")

; 10
LogWrite(@TAB & "Click Finish button" & @LF)
ControlFocus("New Java Class", "&Finish", "Button19")
ControlClick("New Java Class", "&Finish", "Button19")
If @error=1 Then 	
	LogWrite(@TAB & "ERROR! Finish button wasn't found. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
LogWrite(@TAB & "Wait while EHWA class is being created ..." & @LF)
WinWaitClose("New Java Class", "", 60)

; 11
WinWaitImpl("Java - EHWA.java - Eclipse SDK","",60)
If Not WinActive("New Java Class") Then WinActivate("Java - EHWA.java - Eclipse SDK")
WinWaitActiveImpl("Java - EHWA.java - Eclipse SDK","",30)
LogWrite(@TAB & "EHWA class was successfully created" & @LF)
Sleep(3000)
LogWrite(@TAB & "Add System.out.println(""Hello, world!""); string in the main method of EHWA class" & @LF)
ControlFocus("Java - EHWA.java - Eclipse SDK", "", "SWT_Window039")
ControlSend("Java - EHWA.java - Eclipse SDK", "", "SWT_Window039", "{DOWN 8}{TAB}")
ControlSend("Java - EHWA.java - Eclipse SDK", "", "SWT_Window039", "System.out.println(""Hello, world{!}"");")
Sleep(3000)

; 12
WinWaitImpl("Java - EHWA.java - Eclipse SDK","",60)
If Not WinActive("Java - EHWA.java - Eclipse SDK","") Then WinActivate("Java - EHWA.java - Eclipse SDK","")
WinWaitActiveImpl("Java - EHWA.java - Eclipse SDK","",30)
LogWrite(@TAB & "Save EHWA class " & @LF)
$result=WinMenuSelectItem("Java - EHWA.java - Eclipse SDK", "", "&File", "&Save")
if $result=0 Then
	LogWrite(@TAB & "ERROR! Menu 'File->Save' wasn't found. Stop." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf
LogWrite(@TAB & "Open console" & @LF)
Send("!w")
Send("v")
Send("c")
Sleep(3000)

; 13
WinWaitImpl("Java - EHWA.java - Eclipse SDK","",60)
If Not WinActive("Java - EHWA.java - Eclipse SDK","") Then WinActivate("Java - EHWA.java - Eclipse SDK","")
WinWaitActiveImpl("Java - EHWA.java - Eclipse SDK","",30)
LogWrite(@TAB & "Run EHWA class" & @LF)
Send("!r")
Send("n")
LogWrite(@TAB & "Wait 'Run' window to appear " & @LF)
WinWaitImpl("Run","",60)
If Not WinActive("Run") Then WinActivate("Run")
WinWaitActiveImpl("Run","",30)
LogWrite(@TAB & "window appeared " & @LF)

; 14
LogWrite(@TAB & "Select Java application " & @LF)
ControlFocus("Run", "", "Edit1")
ControlSend("Run", "", "Edit1", "{ENTER}")
ControlFocus("Run", "", "SysTreeView321")
ControlSend("Run", "", "SysTreeView321", "{j 2}")
LogWrite(@TAB & "Create new run configuration" & @LF)
ControlSend("Run", "", "SysTreeView321", "{ENTER}")

; 15
WinWaitImpl("Run","",60)
If Not WinActive("Run","") Then WinActivate("Run","")
WinWaitActiveImpl("Run","",30)
LogWrite(@TAB & "Wait while EHWA run configuration is being created ..." & @LF)
$iter = 0
While StringInStr(WinGetText("Run"), "EHWA") = 0 
	$iter = $iter+1
	Sleep(500)
	If $iter = 360 Then
		LogWrite(@TAB & "ERROR! Can't create EHWA run configuration diring 3 min. STOP." & @LF)
		PrintErrorScreen()
		ExitImpl(1)
	EndIf
WEnd
LogWrite(@TAB & "Run EHWA class" & @LF)
ControlFocus("Run", "&Run", "Button55")
ControlSend("Run", "&Run", "Button55", "{ENTER}")
WinWaitClose("Run", "", 60)

; 16
WinWaitActiveImpl("Java - EHWA.java - Eclipse SDK","",30)
LogWrite(@TAB & "Check Run result" & @LF)
$iter = 0
While StringInStr(WinGetText("Java - EHWA.java - Eclipse SDK"), "<terminated>") = 0 
	$iter = $iter+1
	Sleep(500)
	If $iter = 360 Then
		LogWrite(@TAB & "ERROR! EHWA run didn't stop diring 3 min. STOP." & @LF)
		PrintErrorScreen()
		ExitImpl(1)
	EndIf
WEnd
Sleep(3000)
If Not WinExists("Java - EHWA.java - Eclipse SDK") Then
	LogWrite(@TAB & "ERROR! Window 'Java - EHWA.java - Eclipse SDK' doesn't exists. STOP." & @LF)
	PrintErrorScreen()
	ExitImpl(1)
EndIf

; Exit 
ExitImpl(0)

; Auxiliary functions

Func LogWrite($string)
	Dim Const $logfile = $resultsDir & "\EHWA.log"
	Local Const $log = FileOpen($logfile, 1)	
	FileWrite($logfile, $string)
	FileClose($log)
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
			ExitImpl(1)
		Else
			LogWrite(@TAB & "Window '" & $wname & "' closed in additional time!" & @LF)
		EndIf
	EndIf
EndFunc

Func ExitImpl($error_code)
	Opt("WinTitleMatchMode",2)
	LogWrite(@LF & "Exit Eclipse: " & @LF)
	if WinExists("Eclipse SDK") Then
		If Not WinActive("Eclipse SDK","") Then WinActivate("Eclipse SDK","")
		$wr = WinWaitActive("Eclipse SDK", "", 30)
		If $wr=1 Then
			$title = WinGetTitle("Eclipse SDK")
			$exit = WinMenuSelectItem($title, "", "&File", "E&xit" )
			if $exit=0 Then
				LogWrite(@TAB & "Menu """ & $title & "->File->Exit"" wasn't found" & @LF)
				Send("!f")
				Send("x")
			EndIf
			WinWaitClose("Progress Information", "", 120)
			$wr = WinWaitClose("Eclipse SDK", "", 60)
			If $wr=1 Then
				LogWrite(@TAB & "Eclipse was found and closed." & @LF)
			Else 
				KillEclipse()
			EndIf
		Else
			KillEclipse()
		EndIf
	Else
		LogWrite(@TAB & "Eclipse wasn't found" & @LF)
		$error_code=1
	EndIf
	Sleep(5000)
	If $error_code=0 Then 
		LogWrite(@LF & "EHWA PASSED!" & @LF)
	Else 
		LogWrite(@LF & "EHWA FAILED!" & @LF)
	EndIf
	FileCopy(@WorkingDir & "/workspace/.metadata/.log", $resultsDir  & "/workspace.log", 1)
	Exit($error_code)
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
			ExitImpl(1)
	EndIf
	If Not WinActive(" - Paint") Then WinActivate("- Paint")
    WinWaitActive(" - Paint", "", 180)
	$result = WinMenuSelectItem(" - Paint", "", "&Edit", "&Paste")
	If $result = 0 Then
		LogWrite(@TAB & "Mspaint menu 'Edit -> Paste' wasn't found" & @LF)
		LogWrite(@TAB & "Can't save error picture!" & @LF)
		ExitImpl(1)
	EndIf
	LogWrite(@TAB & "Save error screen" & @LF)
	$result = WinMenuSelectItem(" - Paint", "", "&File", "&Save")
	if $result = 0 Then
		LogWrite(@TAB & "Mspaint menu 'File -> Save' wasn't found" & @LF)
		LogWrite(@TAB & "Can't save error picture!" & @LF)
		ExitImpl(1)
	EndIf
	LogWrite(@TAB & "Exit mspaint" & @LF)
	$result = WinMenuSelectItem(" - Paint", "", "&File", "E&xit")
	if $result = 0 Then
		LogWrite(@TAB & "Mspaint menu 'File -> Exit' wasn't found" & @LF)
		LogWrite(@TAB & "Can't save error picture!" & @LF)
		Exitimpl(1)
	EndIf
	WinWaitClose(" - Paint", "", 60)
	If WinExists("Eclipse SDK") Then
		LogWrite(@TAB & "Switch to Eclipse" & @LF)
		If Not WinActive("Eclipse SDK") Then WinActivate("Eclipse SDK")
		WinWaitActive("Eclipse SDK", "", 120)
	EndIf
	Opt("WinTitleMatchMode", 4)
EndFunc

;

