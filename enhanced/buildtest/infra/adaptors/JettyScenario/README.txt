                         ========================    
                         Jetty automated scenario
                         ========================    

--------------
Scenario files
--------------

This is automated scenario which performs user-like actions such as viewing
and navigating with browser through web site hosted by Mortbay* Jetty
application server. The scenario consists of the following files:

 +/
  |-+adaptors/             - Adaptors connecting test suites to Build Test
  |  |
  |  |-+JettyScenario/     - Jetty scenario adaptor directory
  |     |
  |     |--adaptor.xml     - The adaptor
  |     |
  |     |--parameters.xml  - Parameters file
  |     |
  |     |--publisher.xml   - publisher for running in Cruise Control mode
  |     |
  |     |--README.txt      - This readme file
  |
  |-+tests/                - Tests suites placed under Build Test repository
     |
     |-+JettyScenario/     - Jetty scenario root directory
        |
        |-+resources/      - Resources required for Jetty scenario
        |
        |-+src/            - Jetty scenario sources


----------------
Scenario purpose
----------------

The Jetty scenario was created in order to test if some JRE (e.g. Apache
Harmony) is able to run Jetty server and it works just fine. This scenario
is far from covering the whole Jetty functionality, for now it just tests
some basic things such as:

 o Server successfully starts and stops using given JRE
 o Server successfully deploys its test web applications
 o Some of the test applications works properly, i.e. user addresses web
   site links and receives valid responses (pages)

The scenario contains 5 testcases which test the following:

 o Initial(home) page of the site hosted by Jetty
 o Page with static content of large size
 o Servlet-based pages
 o JSP-based page
 o Page with static content archived with GZip


------------------
Scenario mechanism
------------------

The scenario uses Gargoyle Software* HtmlUnit framework which emulates web
browser. Using this tool Jetty scenario simulates user actions as they were
done with usual browser (e.g. Netscape or Firefox). Actions are implemented
in java using HtmlUnit API in terms of HTML language. HtmlUnit in its turn
converts API calls to HTTP requests and receives responses for them. Then
scenario checks if responses received for certain requests are valid, thus
checking that server generating responses works fine.


-------------------
Scenario automation
-------------------

The scenario is automated using new Build Test infrastructure (HARMONY-3501).
The new BT provides features which allow:

  o Run the scenario on just built classlib and drlvm
  o Run the scenario in standalone mode (once)
  o Run the scenario in continuous mode using Cruise Control tool


------------------
Scenario execution
------------------

The following steps should by made to run Jetty scenario:

1. Check out Build Test from SVN

     svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/branches/2.0

2. Setup Build Test and required suites with the following command:

     ant -Dtest.suites="classlib,drlvm,JettyScenario" setup

3. Set values of required parameters in generated file:

     required-parameters.properties

   Note, that if you need to run scneario on previously built drlvm and
   classlib you must not modify parameters jetty.server.jre and 
   scenario.framework.jre.

   If you need you may also explicitely set values of optional parameters
   in adaptors/JettyScenario/parameters.xml file.

4. Run the scneario on previously built drlvm and classib with command:

     ant -Dtest.suites="classlib,drlvm,JettyScenario" run

   or run the scenario in continuous mode with command:

     ant -Dtest.suites="classlib,drlvm,JettyScenario" run-cc
    
5. Run the scneario on explicitely defined JRE with command:

     ant -Dtest.suites=JettyScenario run

   or

     ant -Dtest.suites=JettyScenario run-cc


-----------------------------------------------
Tools and libraries required for Jetty scenario
-----------------------------------------------

The scenario requires the same tools and libraries preinstalled on your system
as Build Test framework itself:

  1) JDK version 1.5.0
     http://java.sun.com
     http://www.jrockit.com/

  2) Apache Ant, version 1.6.5 or higher 
     http://ant.apache.org

  3) Subversion tool (svn)
     http://subversion.tigris.org/

All other external dependencies such as Jetty, HtmlUnit, JUnit will be
automatically downloaded on the setup phase.


----------
Disclaimer
----------

*) Other brands and names are the property of their respective owners.
