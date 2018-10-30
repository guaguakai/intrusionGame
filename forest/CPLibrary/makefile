CCC = MACOSX_DEPLOYMENT_TARGET=10.7 clang++
CONCERTDIR = ./concert
CPLEXDIR = ./cplex
CFLAGS = -DIL_STD -O -DNDEBUG -I./include -I$(CONCERTDIR)/include -fPIC -pedantic -Wall -Wno-long-long -fexceptions -m64 -fno-strict-aliasing -DILOUSEMT -D_REENTRANT -DILM_REENTRANT
LDFLAGS = -L./lib/x86-64_osx/static_pic -lcp -L$(CPLEXDIR)/lib/x86-64_osx/static_pic -lcplex -L$(CONCERTDIR)/lib/x86-64_osx/static_pic -lconcert  -lpthread -framework CoreFoundation -framework IOKit

JAVA = $(JAVA_HOME)/bin/java
JAVAC = $(JAVA_HOME)/bin/javac
JAVAFLAGS = 
CPCLASSPATH = ./cpoptimizer/lib/ILOG.CP.jar
MODELPATH = ./lib/*.jar
GRAPHPATH = ./lib/jgrapht-jdk1.6.jar
LIBRARYPATH = ./cpoptimizer/bin/x86-64_osx:$(CPLEXDIR)/bin/x86-64_osx
CPPEXDIR = ./cpoptimizer/examples/src/cpp
JAVAEXDIR = ./src


all: cpp java

cpp: CPProblem

java: CPProblem.class

execute: execute_cpp execute_java

clean:
	rm -f CPProblem.class
FORCE:

$(JAVAC):
	@echo "####################################"
	@echo "Error: cannot find the javac command"
	@echo "Please ensure that JAVA_HOME is set correctly"
	@echo "Currently, JAVA_HOME = $(JAVA_HOME)"
	@exit 1


TestDo: CPProblem.class
	rm -f TestDo.class
	$(JAVAC) -d . -classpath $(MODELPATH) $(JAVAEXDIR)/TestDo.java
	DYLD_LIBRARY_PATH=$(LIBRARYPATH):$$DYLD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(MODELPATH):. TestDo

CPProblem: CPProblem.class CPProblem.run

CPProblem.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(MODELPATH) $(JAVAEXDIR)/CPProblem.java
CPProblem.run: CPProblem.class FORCE
	DYLD_LIBRARY_PATH=$(LIBRARYPATH):$$DYLD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(MODELPATH):. CPProblem

