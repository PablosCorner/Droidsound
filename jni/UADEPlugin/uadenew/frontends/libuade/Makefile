LIBDIR = /usr/local/lib
INCLUDEDIR = /usr/local/include
PKGCONFIGDIR = /usr/local/lib/pkgconfig
SHAREDSUFFIX = so

AR = ar
CC = gcc
CFLAGS = -Wall -O2 -I../include -I../common -fPIC -shared -fPIC -shared -g  -I/usr/local/include
CLIBS =  -L/usr/local/lib -Wl,-rpath,/usr/local/lib -lm -lbencodetools

all:	libuade.$(SHAREDSUFFIX) libuade.a

%.o:	%.c
	$(CC) $(CFLAGS) -c $<

include ../common/Makefile.common

libuade.$(SHAREDSUFFIX):	$(COMMONMODULES)
	$(CC) -Wl,-soname,$@ -shared -o $@ $(CLIBS) $(COMMONMODULES)

libuade.a:	$(COMMONMODULES)
	$(AR) rcs $@ $(COMMONMODULES)

install:	libuade.$(SHAREDSUFFIX)
	mkdir -p "$(INCLUDEDIR)"/uade "$(LIBDIR)" "$(PKGCONFIGDIR)"
	install -m 644 $< "$(LIBDIR)"/
	install -m 644 ../include/uade/options.h ../include/uade/uade.h "$(INCLUDEDIR)"/uade/
	install -m 644 ../../../libuade.pc "$(PKGCONFIGDIR)"/

clean:	
	rm -f *.o *.so *.a
