#!/bin/sh
#CLASSLIB=C:/Harmony-contribution/Harmony_svn/classlib
# get the list of all modules

#ls -la | awk '{print ($9)}' | grep -v "\." > modulesList

JRE=$1 
VMNAME=$2 
FORKMODE=$3 
CLASSLIB=$4 
ITER=$5 

echo TESTEDVM  ${JRE}
echo VMNAME  ${VMNAME} 
echo FORKMODE  ${FORKMODE}
echo CLASSLIB ${CLASSLIB}
echo ITER  ${ITER}

cp modulesList ${CLASSLIB}

cd ${CLASSLIB}

#STAMP=`date +%Y%m%d%H%M%S`

cat modulesList | while read moduleName; do

	A=0
	while [ "$A" -lt `expr ${ITER}` ]; do

	echo ITERATION $A 


	ant -Dtest.jre.home=${JRE} -Dhy.test.vm.name=${VMNAME}\
		-Dbuild.module=$moduleName -Dhy.test.forkmode=${FORKMODE} \
		test 2>&1 | tee log_${moduleName}_${A}.txt

	R=`grep "BUILD FAILED" log_${moduleName}_${A}.txt | wc -l` 
		        
	if [ "$R" = 0 ]; then 
	
	    echo module ${moduleName} iteration ${A} passed >> build/STATUS.txt 
	else 
								        
	    mv build/test_report build/test_report_${moduleName}_${A} 
	    echo module ${moduleName} iteration ${A} failed >> build/STATUS.txt 
	fi 													       

	A=`expr $A + 1`

	done

done

cd build

# find errors

find test_report_* -name *Test.xml -exec grep -l "<testsuite errors=\"[1-9]*\" " {} \; > ERRORS.txt

# find failures

find test_report_* -name *Test.xml -exec grep -l "failures=\"[1-9]*\"" {} \; > FAILURES.txt

# find crash candidates

find test_report_* -type f -size 0 | grep *.xml > ZERO_LENGTH.txt

exit 0

