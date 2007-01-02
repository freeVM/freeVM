
 
This is a very simple finalizer probe called Finx.java.  Its intended use is for discovering the 
behavior of finalizer threads of various JVM implementations.

This probe is single threaded and has three different execution modes: "java Finx 0", 
"java Finx 1" and "java Finx 2".  By running each of the modes on WindowsXP and using 
Microsoft's Process Viewer, one can learn what the different JVM threads are doing.

Mode 0
This mode intentionally does not create any finalizable objects.  
The main() method simply runs a cpu intensive workload forever.  
After every 1000000 loops main() will print a distinctive string that includes a loop count.
Use Mode 0 to establish a baseline for a give JVM.  Let Mode 0 run for a few minutes, then use
Microsoft Process Viewer to learn things like how many OS threads the JVM is running, what
are each thread's priority level, what percent time is spent in user vs. supervisor mode.  Also 
it should be easy to determine which thread is running main() since main() will grab most
of the CPU time. 
 
Mode 1
main() creates 100K finalizable objects that are intentionally shoved into a state where their 
finalizer needs to be called.  main() then proceeds to run the same cpu intensive workload as above.  
The finalize() method will execute just one call of the same cpu intensive workload then returns.  
This simulates a short running finalizer.  finalize() prints a distinctive string to make it easy to 
quickly read the output which is comingled with main(). Use Mode 1 to learn things like which thread(s) are
running the finalizers and how much CPU time is spent on finalizers vs. main().  The rate of progress
of finalizers vs. main() is easy to estimate.  After each 1M CPU intensive loops, both main() and finalize() will
print a rolling count.  Simply count the number of main() print outs vs. the number of finalize() print outs.
 
Mode 2
This mode is identical to Mode 1 except the finalize() method calls the cpu intensive workload endlessly.
Use this mode to determine JVM behavior when there are 100K finalizable objects and each object's finalization
takes forever.  Investigate if a JVM will force a long running finalizer to abort.  Does the JVM boost the
priority of the long running finalizer?  Lower the priority?  Add more finalization threads?

SMP
All the above modes can be run on UP, 2-way, 4-way, etc machines to determine how a specific JVM handles
finalizer threads.  Use this probe to answer questions like how many finalizer threads does a 
JVM have on 2-way vs 4-way vs 8-way?  

OS
This probe was built on windowsXP.  Its untested on Linux but it should simply work.  Most likely
Linux will exhibit slightly different OS thread scheduling behavior than Windows.
