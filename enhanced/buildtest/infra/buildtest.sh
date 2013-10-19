# Tune environment:
export COMPILER_CFG_SCRIPT=
export JAVA_HOME=/usr/jdk1.5.0_06/jdk
export ANT_HOME=/usr/lib/ant
export SVN_HOME=/usr/local
export ANT_OPTS="-XX:MaxPermSize=512m -Xmx700M -Dhttp.proxyHost=my.proxy.com -Dhttp.proxyPort=1111"

export PATH=$SVN_HOME/bin:$PATH:$ANT_HOME/bin
export CLASSPATH=.:$PWD/build/classes

$COMPILER_CFG_SCRIPT

ant $*
