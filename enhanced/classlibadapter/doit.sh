
### below are various command line configurations, keep for historical reasons
#./jc.exe --classpath=. --bootclasspath=/usr/local/unzipped_glibj:/usr/local/unzipped_jc:. hello
#./jc.exe --classpath=. --bootclasspath=/usr/local/unzipped_harmony_classlib/bin:.  --verbose=class --verbose=jni  hello
#gdb  run ./jc.exe --classpath=. --bootclasspath=/usr/local/unzipped_harmony_classlib/bin:.  --verbose=class hello
#gdb  --args ./jc.exe  --classpath=. --bootclasspath=/usr/local/unzipped_glibj:/usr/local/unzipped_jc:. hello
#gdb  --args ./jc.exe  --classpath=. --bootclasspath=/usr/local/unzipped_harmony_classlib/bin:/usr/local/unzipped_jc:.  --verbose=class  hello
#gdb  --args ./jc.exe  --classpath=. --bootclasspath=/cygdrive/c/temp2/Harmony/bin:/usr/local/unzipped_jc:.  --verbose=class  hello
#gdb  --args ./jc.exe  --classpath=. --bootclasspath=/cygdrive/c/temp2/Harmony/bin:/usr/local/unzipped_jc:.  Hello

gdb  --args ./jc.exe --classpath=. --bootclasspath=/cygdrive/c/temp2/Harmony/bin:/usr/local/unzipped_jc:.  Hello



