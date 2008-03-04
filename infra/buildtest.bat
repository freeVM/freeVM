@rem Tune environment:
@set COMPILER_CFG_SCRIPT=C:\Program Files\Microsoft Visual Studio .NET 2003\Common7\Tools\vsvars32.bat
@set JAVA_HOME=C:\ws\bin\jdk1.5.0_06
@set ANT_HOME=C:\ws\bin\apache-ant-1.6.5
@set SVN_HOME=C:\ws\bin\svn-win32-1.4.2
@set ANT_OPTS=-XX:MaxPermSize=512m -Xmx1000M -Dhttp.proxyHost=my.proxy.com -Dhttp.proxyPort=1111

@set PATH=%SVN_HOME%\bin;%PATH%;%ANT_HOME%\bin
@set CLASSPATH=.;%CD%\build\classes

@call "%COMPILER_CFG_SCRIPT%"
call ant %* 2>&1
