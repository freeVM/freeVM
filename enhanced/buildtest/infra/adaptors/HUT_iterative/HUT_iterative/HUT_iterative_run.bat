@echo off

set JRE=%1 
set VMNAME=%2 
set FORKMODE=%3 
set CLASSLIB=%4 
set ITER=%5 
set EXCLUDE=swing

echo TESTEDVM  %JRE%
echo VMNAME  %VMNAME% 
echo FORKMODE  %FORKMODE%
echo CLASSLIB %CLASSLIB%
echo ITER  %ITER%
echo EXCLUDE %EXCLUDE%

copy modulesList %CLASSLIB%

cd %CLASSLIB%

set ANT_COMMAND=%ANT_HOME%\bin\ant.bat
set start=1
set step=1

for /F %%i in (modulesList) do (

	if not %%i==%EXCLUDE% ( 

 	for /L %%A in (%start%,%step%,%ITER%) do (

 		echo %%i

call %ANT_COMMAND% -Dtest.jre.home=%JRE% -Dhy.test.vm.name=%VMNAME% -Dbuild.module=%%i -Dhy.test.forkmode=%FORKMODE% test > log_%%i_%%A.txt 2>&1

		type log_%%i_%%A.txt
		
		findstr /c:"BUILD FAILED" log_%%i_%%A.txt > FF

		for /F "tokens=*" %%j in (FF) do (

			echo RESULT %%j		
			
			if "%%j" == "BUILD FAILED" (
			
  				move build\test_report build\test_report_%%i_%%A
				echo module %%i iteration %%A failed >> build/STATUS.txt

			) 
		)

		findstr /c:"BUILD SUCCESSFUL" log_%%i_%%A.txt > FF

		for /F "tokens=*" %%j in (FF) do (

			echo RESULT %%j		
			
			if "%%j" == "BUILD SUCCESSFUL" (
				echo module %%i iteration %%A passed >> build/STATUS.txt
			) 
		)
							
 	)

        )
)

cd build

rem find errors

findstr /RS "errors=\"[1-9]\" " *Test.xml >> ERRORS.txt

rem find failures

findstr /RS "failures=\"[1-9]*\"" *Test.xml >> FAILURES.txt

rem find crash candidates

dir /O /S | find "Test.xml" | find "     0 TEST" >> CRASHES.txt

exit 0
