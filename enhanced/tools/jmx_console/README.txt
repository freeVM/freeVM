Apache Harmony JMX Console
========================== 

1. TOOLS REQUIRED FOR THE BUILD
-------------------------------

To build the JMX Console plug-in, install and configure the following tools:

+ Apache Ant            - Apache Ant version 1.6.4 or higher
                          http://ant.apache.org

+ Eclipse              - Eclipse SDK version 3.1.1 or higher
                          http://download.eclipse.org/eclipse/downloads/index.php
                          
+ SWT                  - SWT Graphical Library version 3.1.x
						  http://www.eclipse.org/swt/				


2. BUILDING THE JMX CONSOLE PLUG-IN
------------------------------------

1. Change the current directory to make/.
            
2. Edit the external_tools.properties file
   The property eclipse.path must point to the Eclipse folder.

3. Run Apache Ant by typing: ant

The build produces a set of .class and .jar files. 
These files are placed in the following directory tree structure:

<EXTRACT_DIR>/build
       |
       +---classes                                            compiled class files
       |
       +---jar
             |
             \---org.apache.harmony.x.management.console_3.1.1.jar   plug-in archive
             \---JMXConsole.jar                                      standalone console   



3. RUNNING THE JMX CONSOLE PLUG-IN
-----------------------------------

To run the JMX Console, follow the steps described below. 

 1.  Copy file build/jar/org.apache.harmony.x.management.console_3.1.1.jar
     into the plugins/ folder of Eclipse.
 
 2.  Start Eclipse. 

 3.  Go Window > Open Perspective > Other...  and select 
     the JMX Perspective option from the list.
 
 4.  Go File > New > JMX Console > New Connection Wizard. 

 5.  Select the Local VM tab and connect to the local VM. 

 6.  The tree structure appear on the left in your workspace. 


4. RUNNING THE STANDALONE JMX CONSOLE
--------------------------------------

To run the JMX Console, follow the steps described below. 
 1. Install swt into folder C:/swt
 
 2. Run console using command:
 	
 	java -Djava.library.path=c:/swt -classpath "C:\swt\swt.jar;.;JMXConsole.jar" org.apache.harmony.x.management.console.standalone.Main

 3. Connect to MBean server using dialog box.
 

5. KNOWN ISSUES
---------------

- The supplied auxiliary views have not been tested properly.

- In the UI, blank squares are used instead of icons.


6. TODO
-------

- Implement a customized view for JVM.

- Design icons.


