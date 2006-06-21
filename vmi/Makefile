all: ../libvmi.so

CXXFLAGS = \
  -Wall -DLINUX \
  -I$(CLASSLIB_HOME)/deploy/include -I$(CLASSLIB_HOME)/deploy/jdk/include

LDFLAGS= \
  -L$(CLASSLIB_HOME)/deploy/jdk/jre/bin -lhyprt -lhythr -lhysig \
  -L$(CLASSLIB_HOME)/deploy/lib -lhyzip -lhypool

OBJ=vmi.o java_lang_reflect_Array.o

../libvmi.so: $(OBJ)
	$(CXX) -shared $+ -o $@ $(LDFLAGS)

clean:
	-rm -f *.o ../libvmi.so
