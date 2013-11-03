                    ========================================
                    Harmony Build and Testing Infrastructure
                    ========================================

Harmony BTI is intended to organize overall project Build & Testing 
activities in one place. This infrastructure provides the means to:
    - configure and launch test runs on the base of integrated test suites,
    - implement and integrate new test suites,
    - organize automated builds and code integrity checking processes


-------
Prepare
-------

To get Harmony BTI working on your system, you should have the following tools
installed:

  1) JDK version 1.5.0:
     http://java.sun.com
     http://www.jrockit.com/

  2) Apache Ant, version 1.6.5
     http://ant.apache.org
  
  3) Subversion tool (svn)
     http://subversion.tigris.org/

Some of the test suites (involving CLASSLIB or DRLVM builds) 
need C compiler. It can be either:
    - gcc for Linux*, or 
    - Microsoft* Visual Studio .NET* 2003 for Windows*


---------------
Getting started
---------------

To get started with Harmony BTI go through the points below. For quick start you
can skip long explanations - just read the headers of the items and execute
the commands started with #>


1. Get the infrastructure from its SVN repository:

    #> svn checkout http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/trunk/infra

2. Install the BTI: Tune environment variables in buildtest scripts 
downloaded during the first step, select test suites to execute and invoke
install target. For example,
    
    #> buildtest -Dtest.suites="classlib,drlvm,scimark" install

This sample configuration means: execute classlib building (test that it can
be built), then on the base of built classlib do build of drlvm (test that it
also can be built), and after that run the SciMark benchmark on the product of
drlvm test suite (which is built JVM).

Such a meaning is reached by the following information provided by test
suites: information about dependencies between the suites, and information
about the product provided by one suite to another. This information is
described in parameters.xml file placed under 'adaptors' dir for each of test
suites. This file is an Ant xml property file and having some semantics defined
on each of the properties. 

So the property 'depends' defined in parameters.xml file lists the test
suites on which this test suite depends in default configuration. For example
look in adaptors/scimark/parameters.xml file: by default the scimark test
suite depends on drlvm: if drlvm and scimark are selected for test run
together, then scimark is always executed after drlvm and it takes JVM
provided by drlvm as JVM to test. If drlvm test suite is not selected for
run, then scimark's 'tested.runtime' "required" parameter is not resolved
and BTI asks to provide this value in 'framework.local.properties' file.

drlvm test suite provides information about built JVM to other test suites
by means of "shared" 'jvm.location' parameter. 
To get this value scimark test suite in its parameters.xml file
makes assignment of its "required" parameter 'tested.runtime' to drlvm's
"shared" parameter 'jvm.location'.

That's the base means used to describe the execution order of the test suites
and the values of required parameters needed for test suite execution.

The command line above complies with default configuration expected by selected
test suites. But what if it does not? What if we chose the following
configuration to be executed: -Dtest.suites="hdk,scimark" (do build HDK and
execute SciMark on top of it). In this case we need to change scimark's
dependency information and the information from where to get the JVM to test.
It is done by means of the following properties specified in
framework.local.properties file before 'buildtest setup' execution:

scimark.parameters.depends=hdk
  - tells to BTI to execute scimark after hdk
scimark.parameters.required.tested.runtime=${hdk.parameters.shared.binaries.jre.dir}/bin/java
  - tells to BTI where to take JVM for scimark

These properties redefines the parameters defined in scimark's 
parameters.xml file.

That's all about dependencies between the test suites.

'buildtest install' command generates required-parameters.properties file. 
It contains all of the assignments of "required" parameters. 
If some of the "required" parameters were not evaluated, 
BTI will report an ERROR and demand to specify the values for
unspecified parameters. You should provide them directly in 
framework.local.properties file and repeat 'buildtest install'. 

3. Setup test suites:

    #> buildtest setup

This command launches setup target for each installed suite. 

4. To launch the configured test run, type the following:

    #> buildtest run

It executes selected test suites. BTI executes each of the suites and
reports SUCCESS if there are no test failures and ERROR otherwise.
To launch continuous test run, type the following:
    
    #> buildtest run-cc

It launches CC controlled continuous test execution. There are two modes of
continuous execution: SVN modifications triggered and scheduled runs. SVN
modifications triggered runs are executed each time SVN modification occurs.
The SVN repositories controlled by test suites are defined by cc.usesvn*
properties defined in parameters.xml file. For example look in the parameters
for classlib or hdk test suites. Scheduled runs are launched at the particular
time specified by the following properties in framework.local.properties file:

framework.parameters.schedule.day=Tuesday
 - Do launch test suites execution on particular day. 
   If this property is not defined, the test run is launched everyday.
framework.parameters.schedule.time=1530
 - Do launch test suites execution at this particular time.
   Should be defined for scheduled execution mode.



*) Other brands and names are the property of their respective owners
