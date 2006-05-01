rm *.o
rm *.lo
rm *.dll
rm *.la
rm -r .libs
rm *.a

### below are libtool command lines that work fine.  But I use direct gcc command lines because it compiles faster
### and also reduces confusion.
#libtool --mode=compile gcc  -g -O -c -fpic  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm emptystub.c
#libtool --mode=compile gcc  -g -O -c -fpic  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm fileos.c

gcc  -g -O -c  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm emptystub.c
gcc  -g -O -c  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm fileos.c
gcc  -g -O -c  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm file.c
gcc  -g -O -c  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm iohelp.c
gcc  -g -O -c  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm helpers.c

### below are various failed experiments that were run to get "ld" to work, keep for historical purposes
#libtool --mode=compile gcc  -g -O -c -fpic  -DLINUX -D_REENTRANT -O1 -march=pentium3 -fno-exceptions -DIPv6_FUNCTION_SUPPORT  -DHYX86 -I../include -I../luni -I../common -I../zlib -I../zip -I../fdlibm fileos.c
#libtool --tag=CC --mode=link gcc -pedantic -W -Wall -Wmissing-declarations -Wwrite-strings -Wmissing-prototypes -Wno-long-long -Wstrict-prototypes -g -O2 -module -version-info 0:0:0  -o zzz_wjw.dll fileos.o
#ld --dll --export-dynamic -call_shared -L/usr/lib --output-def out.def  --out-implib implib.def --enable-auto-import  -o hynio.dll fileos.o
#libtool --mode=link gcc -module -o xxx.la  fileos.lo -rpath /usr/local/lib  -lm
#libtool --mode=link gcc -g -o libzzz.la fileos.lo -rpath /usr/local/lib -lm
#ld -Bshareable -o libzz.dll fileos.o  -lm
#ld -Bshareable --export-dynamic -o libzz.a junkwjw.o fileos.o -lc

### for some unknown reason, if emptystub.o is removed from the link, ld produces a dll that causes gdb to hang 
ld -Bshareable --export-dynamic --enable-auto-image-base -o libOSFileSystem.a emptystub.o fileos.o file.o helpers.o iohelp.o -lc
