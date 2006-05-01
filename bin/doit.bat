cd bin

del com_ibm_platform_OSFileSystem.h
javah.exe  -bootclasspath . com.ibm.platform.OSFileSystem
copy com_ibm_platform_OSFileSystem.h  c:\temp2\Harmony\native-src\linux.IA32\luni_gnuclasspathadapter

del java_io_File.h
javah.exe -bootclasspath .  java.io.File
copy java_io_File.h                   c:\temp2\Harmony\native-src\linux.IA32\luni_gnuclasspathadapter

cd ..
