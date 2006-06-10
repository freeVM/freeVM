#!/bin/bash

DIR=$PWD
BASEDIR=$(dirname $PWD)
PATH=$JAVA_HOME/bin:$PATH

die() {
   echo "$@"
   exit
}

check_classlib() {
	somefile=$(ls $1/make/build-java.xml 2>/dev/null)
	[ -f "$somefile" ] || return 1
	export CLASSLIB_HOME=$(dirname "$(dirname "$somefile")")
}

detect_classlib() {
        check_classlib $CLASSLIB_HOME && return
	echo "No valid CLASSLIB_HOME set, trying to guess..."
        check_classlib $BASEDIR/CLASSLIB && return
	check_classlib $BASEDIR/classlib && return
	check_classlib $BASEDIR/'*' && return
	die "No CLASSLIB_HOME detected"
}

detect_classlib
echo Using CLASSLIB_HOME = $CLASSLIB_HOME


KERNEL=$DIR/modules/kernel/src/main/java
LUNI=$DIR/modules/luni/src/main/java
NIO=$DIR/modules/nio/src/main/java

BOOT=$CLASSLIB_HOME/deploy/jdk/jre/lib/boot
CP=$BOOT/nio.jar:$BOOT/luni.jar:$BOOT/annotation.jar:$KERNEL
#JAVAC="java -jar $CLASSLIB_HOME/depends/jars/ecj_3.2RC5/ecj_3.2RC5.jar -source 1.5 -target jsr14"
JAVAC="javac -source 1.5 -target jsr14"

(cd $KERNEL; $JAVAC -classpath $CP $(find . -name *.java)) || die
(cd $NIO; $JAVAC -classpath $CP $(find . -name *.java)) || die

g++ -Wall -shared -o libvmi.so vmi/*.cpp -I$CLASSLIB_HOME/deploy/include -I$CLASSLIB_HOME/deploy/jdk/include -I$CLASSLIB_HOME/native-src/linux.IA32/include -I$CLASSLIB_HOME/native-src/shared/include -DLINUX -L$CLASSLIB_HOME/deploy/jdk/jre/bin -lhyprt -lhythr -lhysig -L$CLASSLIB_HOME/native-src/linux.IA32/lib -lhyzip -lhypool 
