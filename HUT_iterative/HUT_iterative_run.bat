@echo off

set JRE=%1 
set VMNAME=%2 
set FORKMODE=%3 
set CLASSLIB=%4 
set ITER=%5 

echo TESTEDVM  %JRE%
echo VMNAME  %VMNAME% 
echo FORKMODE  %FORKMODE%
echo CLASSLIB %CLASSLIB%
echo ITER  %ITER%

copy modulesList %CLASSLIB%

cd %CLASSLIB%

set ANT_COMMAND=%ANT_HOME%\bin\ant.bat
set start=1
set step=1

for /F %%i in (modulesList) do (
 	for /L %%A in (%start%,%step%,%ITER%) do (

 		echo %%i
call %ANT_COMMAND% -Dtest.jre.home=%JRE% -Dhy.test.vm.name=%VMNAME% -Dbuild.module=%%i -Dhy.test.forkmode=%FORKMODE% test 2>&1 > log_%%i_%%A.txt  

rem		type log_%%i_%%A.txt

  		move build\test_report build\test_report_%%i_%%A
 	)
)

for /F %%i in (modulesList) do (
 	for /L %%A in (%start%,%step%,%ITER%) do (	

		echo test_report_%%i_%%A >> build\test_report_length.txt
		dir build\test_report_%%i_%%A | find /C "Test.xml" >> build\test_report_length.txt

 	)
)

cd build

rem find errors

findstr /RS "errors=\"[1-9]\" " *Test.xml >> ERRORS.txt

rem find failures

findstr /RS "failures=\"[1-9]*\"" *Test.xml >> FAILURES.txt

rem find crash candidates

dir /O /S | find "Test.xml" | find "     0 TEST" >> ZERO_LENGTH.txt

