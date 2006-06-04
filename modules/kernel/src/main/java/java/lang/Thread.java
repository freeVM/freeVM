/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

/**
 * This class must be implemented by the vm vendor. The documented methods must
 * be implemented to support other provided class implementations in this
 * package. A Thread is a unit of concurrent execution in Java. It has its own
 * call stack for methods being called and their parameters. Threads in the same
 * VM interact and synchronize by the use of shared Objects and monitors
 * associated with these objects. Synchronized methods and part of the API in
 * Object also allow Threads to cooperate. When a Java program starts executing
 * there is an implicit Thread (called "main") which is automatically created by
 * the VM. This Thread belongs to a ThreadGroup (also called "main") which is
 * automatically created by the bootstrap sequence by the VM as well.
 * 
 * @see java.lang.Object
 * @see java.lang.ThreadGroup
 */
public class Thread implements Runnable {

	public final static int MAX_PRIORITY = 10; // Maximum allowed priority for

	// a thread
	public final static int MIN_PRIORITY = 1; // Minimum allowed priority for

	// a thread
	public final static int NORM_PRIORITY = 5; // Normal priority for a thread

    Runnable runnableSlot;
    //////String threadNameSlot;  //maybe same as next one
    String name;
    /////ThreadGroup threadGroupSlot;  //maybe same as next one
    ThreadGroup group;
    ////VMThread threadSlot;  //maybe same as next one
    final VMThread vmThread;  
    ///boolean isDaemonSlot;  //maybe this is the same as daemon
    boolean daemon;
    ThreadLocal threadLocalSlot;
    long stackSlot;
    int priority;


    public Thread(VMThread vmt1, String s1, int i1, boolean b1) 
    {
        super();
        vmThread = vmt1;
        name = s1;
        priority = i1;
        daemon = b1;
    }

	/**
	 * Constructs a new Thread with no runnable object and a newly generated
	 * name. The new Thread will belong to the same ThreadGroup as the Thread
	 * calling this constructor.
	 * 
	 * @see java.lang.ThreadGroup
	 */
	public Thread() {
        super();
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with a runnable object and a newly generated
	 * name. The new Thread will belong to the same ThreadGroup as the Thread
	 * calling this constructor.
	 * 
	 * @param runnable
	 *            a java.lang.Runnable whose method <code>run</code> will be
	 *            executed by the new Thread
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 */
	public Thread(Runnable runnable) {
		super();
        runnableSlot = runnable;
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with a runnable object and name provided. The new
	 * Thread will belong to the same ThreadGroup as the Thread calling this
	 * constructor.
	 * 
	 * @param runnable
	 *            a java.lang.Runnable whose method <code>run</code> will be
	 *            executed by the new Thread
	 * @param threadName
	 *            Name for the Thread being created
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 */
	public Thread(Runnable runnable, String threadName) {
		super();
        runnableSlot = runnable;
        name = threadName;
        vmThread = new VMThread(this);

	}

	/**
	 * Constructs a new Thread with no runnable object and the name provided.
	 * The new Thread will belong to the same ThreadGroup as the Thread calling
	 * this constructor.
	 * 
	 * @param threadName
	 *            Name for the Thread being created
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 */
	public Thread(String threadName) {
        super();
        name = threadName;
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with a runnable object and a newly generated
	 * name. The new Thread will belong to the ThreadGroup passed as parameter.
	 * 
	 * @param group
	 *            ThreadGroup to which the new Thread will belong
	 * @param runnable
	 *            a java.lang.Runnable whose method <code>run</code> will be
	 *            executed by the new Thread
	 * @exception SecurityException
	 *                if <code>group.checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception IllegalThreadStateException
	 *                if <code>group.destroy()</code> has already been done
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public Thread(ThreadGroup gr, Runnable runnable) {
        super();
        group = gr;
        runnableSlot = runnable;
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with a runnable object, the given name and
	 * belonging to the ThreadGroup passed as parameter.
	 * 
	 * @param group
	 *            ThreadGroup to which the new Thread will belong
	 * @param runnable
	 *            a java.lang.Runnable whose method <code>run</code> will be
	 *            executed by the new Thread
	 * @param threadName
	 *            Name for the Thread being created
	 * @param stack
	 *            Platform dependent stack size
	 * @exception SecurityException
	 *                if <code>group.checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception IllegalThreadStateException
	 *                if <code>group.destroy()</code> has already been done
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public Thread(ThreadGroup gr, Runnable runnable, String threadName,
			long stack) {
		super();
        group = gr;
        runnableSlot = runnable;
        name = threadName;
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with a runnable object, the given name and
	 * belonging to the ThreadGroup passed as parameter.
	 * 
	 * @param group
	 *            ThreadGroup to which the new Thread will belong
	 * @param runnable
	 *            a java.lang.Runnable whose method <code>run</code> will be
	 *            executed by the new Thread
	 * @param threadName
	 *            Name for the Thread being created
	 * @exception SecurityException
	 *                if <code>group.checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception IllegalThreadStateException
	 *                if <code>group.destroy()</code> has already been done
	 * @see java.lang.ThreadGroup
	 * @see java.lang.Runnable
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public Thread(ThreadGroup gr, Runnable runnable, String threadName) {
        super();
        group = gr;
        runnableSlot = runnable;
        name = threadName;
        vmThread = new VMThread(this);
	}

	/**
	 * Constructs a new Thread with no runnable object, the given name and
	 * belonging to the ThreadGroup passed as parameter.
	 * 
	 * @param group
	 *            ThreadGroup to which the new Thread will belong
	 * @param threadName
	 *            Name for the Thread being created
	 * @exception SecurityException
	 *                if <code>group.checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception IllegalThreadStateException
	 *                if <code>group.destroy()</code> has already been done
	 * @see java.lang.ThreadGroup
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public Thread(ThreadGroup gr, String threadName) {
		super();
        group = gr;
        name = threadName;
        vmThread = new VMThread(this);
	}

	/**
	 * Returns the number of active threads in the running thread's ThreadGroup
	 * 
	 * @return Number of Threads
	 */
	public static int activeCount() {
		return 1; //fixit -- this works for simple "hello world" bring up but is obviously wrong
	}

	/**
	 * This method is used for operations that require approval from a
	 * SecurityManager. If there's none installed, this method is a no-op. If
	 * there's a SecurityManager installed ,
	 * <code>checkAccess(Ljava.lang.Thread;)</code> is called for that
	 * SecurityManager.
	 * 
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public final void checkAccess() {
		return;  //fixit -- this works for simple "hello world" bring up but is obviously wrong
	}

	/**
	 * Returns the number of stack frames in this thread.
	 * 
	 * @return Number of stack frames
	 * @deprecated The results of this call were never well defined. To make
	 *             things worse, it would depend if the Thread was suspended or
	 *             not, and suspend was deprecated too.
	 */
	public int countStackFrames() {
		return vmThread.countStackFrames();
	}

	/**
	 * Answers the instance of Thread that corresponds to the running Thread
	 * which calls this method.
	 * 
	 * @return a java.lang.Thread corresponding to the code that called
	 *         <code>currentThread()</code>
	 */
	public static Thread currentThread() {
		return VMThread.currentThread();
	};

	/**
	 * Destroys the receiver without any monitor cleanup. Not implemented.
	 * 
	 */
	public void destroy() {
		return;  //fixit -- a "nop" is a reasonable implementation of a seldom used API, its never called for simple "hello world" app
	}

	/**
	 * Prints a text representation of the stack for this Thread.
	 * 
	 */
	public static void dumpStack() {
        try {
            throw new Exception("stack dump");
        } catch (Throwable t) {
            t.printStackTrace();
        }
	}

	/**
	 * Copies an array with all Threads which are in the same ThreadGroup as the
	 * receiver - and subgroups - into the array <code>threads</code> passed
	 * as parameter. If the array passed as parameter is too small no exception
	 * is thrown - the extra elements are simply not copied.
	 * 
	 * @param threads
	 *            array into which the Threads will be copied
	 * @return How many Threads were copied over
	 * @exception SecurityException
	 *                if the installed SecurityManager fails
	 *                <code>checkAccess(Ljava.lang.Thread;)</code>
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 */
	public static int enumerate(Thread[] threads) {
		return 0;  //fixit -- returning zero is good enough for simple "hello world" demo
	}

	/**
	 * Returns the context ClassLoader for the receiver.
	 * 
	 * @return ClassLoader The context ClassLoader
	 * @see java.lang.ClassLoader
	 * @see #getContextClassLoader()
	 */
	public ClassLoader getContextClassLoader() {
        //fixit -- returning null is good enough for simple "hello world" demo
		return null;
	}

	/**
	 * Answers the name of the receiver.
	 * 
	 * @return the receiver's name (a java.lang.String)
	 */
	public final String getName() {
        //String ss = name + this.toString();
		return this.toString();  // fixit -- find out the proper construction for thread names
	}

	/**
	 * Answers the priority of the receiver.
	 * 
	 * @return the receiver's priority (an <code>int</code>)
	 * @see Thread#setPriority
	 */
	public final int getPriority() {
		return 0;  //fixit -- correct thread priority is not essential to demoing simple "hello world"
	}

	/**
	 * Answers the ThreadGroup to which the receiver belongs
	 * 
	 * @return the receiver's ThreadGroup
	 */
	public final ThreadGroup getThreadGroup() {
		return group;  //fixit -- ThreadGroups are not essential to demoing simple "hello world"
	}

	/**
	 * A sample implementation of this method is provided by the reference
	 * implementation. It must be included, as it is called by ThreadLocal.get()
	 * and InheritableThreadLocal.get(). Return the value associated with the
	 * ThreadLocal in the receiver
	 * 
	 * @param local
	 *            ThreadLocal to perform the lookup
	 * @return the value of the ThreadLocal
	 * @see #setThreadLocal
	 */
	Object getThreadLocal(ThreadLocal local) {
        return threadLocalSlot.get();  //fixit -- this code looks correct.  It needs to be verified/confirmed it is correct
	}

	/**
	 * Posts an interrupt request to the receiver
	 * 
	 * @exception SecurityException
	 *                if <code>group.checkAccess()</code> fails with a
	 *                SecurityException
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager
	 * @see Thread#interrupted
	 * @see Thread#isInterrupted
	 */
	public void interrupt() {
        vmThread.interrupt();
		return;
	}

	/**
	 * Answers a <code>boolean</code> indicating whether the current Thread (
	 * <code>currentThread()</code>) has a pending interrupt request (
	 * <code>true</code>) or not (<code>false</code>). It also has the
	 * side-effect of clearing the flag.
	 * 
	 * @return a <code>boolean</code>
	 * @see Thread#currentThread
	 * @see Thread#interrupt
	 * @see Thread#isInterrupted
	 */
	public static boolean interrupted() {
		return VMThread.interrupted();
	};

	/**
	 * Answers <code>true</code> if the receiver has already been started and
	 * still runs code (hasn't died yet). Answers <code>false</code> either if
	 * the receiver hasn't been started yet or if it has already started and run
	 * to completion and died.
	 * 
	 * @return a <code>boolean</code>
	 * @see Thread#start
	 */
	public final boolean isAlive() {
		return vmThread.isAlive();
	}

	/**
	 * Answers a <code>boolean</code> indicating whether the receiver is a
	 * daemon Thread (<code>true</code>) or not (<code>false</code>) A
	 * daemon Thread only runs as long as there are non-daemon Threads running.
	 * When the last non-daemon Thread ends, the whole program ends no matter if
	 * it had daemon Threads still running or not.
	 * 
	 * @return a <code>boolean</code>
	 * @see Thread#setDaemon
	 */
	public final boolean isDaemon() {
		return daemon;
	}

	/**
	 * Answers a <code>boolean</code> indicating whether the receiver has a
	 * pending interrupt request (<code>true</code>) or not (
	 * <code>false</code>)
	 * 
	 * @return a <code>boolean</code>
	 * @see Thread#interrupt
	 * @see Thread#interrupted
	 */
	public boolean isInterrupted() {
		return vmThread.isInterrupted();
	}

	/**
	 * Blocks the current Thread (<code>Thread.currentThread()</code>) until
	 * the receiver finishes its execution and dies.
	 * 
	 * @exception InterruptedException
	 *                if <code>interrupt()</code> was called for the receiver
	 *                while it was in the <code>join()</code> call
	 * @see Object#notifyAll
	 * @see java.lang.ThreadDeath
	 */
	public final void join() throws InterruptedException {
        //fixit -- obviously incorrect but it seems OK for simple "hello world" demo
		return;
	}

	/**
	 * Blocks the current Thread (<code>Thread.currentThread()</code>) until
	 * the receiver finishes its execution and dies or the specified timeout
	 * expires, whatever happens first.
	 * 
	 * @param timeoutInMilliseconds
	 *            The maximum time to wait (in milliseconds).
	 * @exception InterruptedException
	 *                if <code>interrupt()</code> was called for the receiver
	 *                while it was in the <code>join()</code> call
	 * @see Object#notifyAll
	 * @see java.lang.ThreadDeath
	 */
	public final void join(long timeoutInMilliseconds)
			throws InterruptedException {
        //fixit -- obviously incorrect but it seems OK for simple "hello world" demo
		return;
	}

	/**
	 * Blocks the current Thread (<code>Thread.currentThread()</code>) until
	 * the receiver finishes its execution and dies or the specified timeout
	 * expires, whatever happens first.
	 * 
	 * @param timeoutInMilliseconds
	 *            The maximum time to wait (in milliseconds).
	 * @param nanos
	 *            Extra nanosecond precision
	 * @exception InterruptedException
	 *                if <code>interrupt()</code> was called for the receiver
	 *                while it was in the <code>join()</code> call
	 * @see Object#notifyAll
	 * @see java.lang.ThreadDeath
	 */
	public final void join(long timeoutInMilliseconds, int nanos)
			throws InterruptedException {
        //fixit -- obviously incorrect but it seems OK for simple "hello world" demo
		return;
	}

	/**
	 * This is a no-op if the receiver was never suspended, or suspended and
	 * already resumed. If the receiver is suspended, however, makes it resume
	 * to the point where it was when it was suspended.
	 * 
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @see Thread#suspend()
	 * @deprecated Used with deprecated method Thread.suspend().
	 */
	public final void resume() {
        //fixit -- obviously incorrect but it seems OK for simple "hello world" demo
		return;
	}

	/**
	 * Calls the <code>run()</code> method of the Runnable object the receiver
	 * holds. If no Runnable is set, does nothing.
	 * 
	 * @see Thread#start
	 */
	public void run() {
        if (runnableSlot == null)
            return;
        runnableSlot.run();
	}

	/**
	 * Set the context ClassLoader for the receiver.
	 * 
	 * @param cl
	 *            The context ClassLoader
	 * @see java.lang.ClassLoader
	 * @see #getContextClassLoader()
	 */
	public void setContextClassLoader(ClassLoader cl) {
        //fixit -- this incorrect code is good enough to allow simple "hello world" demo to run
		return;
	}

	/**
	 * Set if the receiver is a daemon Thread or not. This can only be done
	 * before the Thread starts running.
	 * 
	 * @param isDaemon
	 *            A boolean indicating if the Thread should be daemon or not
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @see Thread#isDaemon
	 */
	public final void setDaemon(boolean isDaemon) {
		daemon = isDaemon;
	}

	/**
	 * Sets the name of the receiver.
	 * 
	 * @param threadName
	 *            new name for the Thread
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @see Thread#getName
	 */
	public final void setName(String threadName) {
		name = threadName;
	}

	/**
	 * Sets the priority of the receiver. Note that the final priority set may
	 * not be the parameter that was passed - it will depend on the receiver's
	 * ThreadGroup. The priority cannot be set to be higher than the receiver's
	 * ThreadGroup's maxPriority().
	 * 
	 * @param priority
	 *            new priority for the Thread
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception IllegalArgumentException
	 *                if the new priority is greater than Thread.MAX_PRIORITY or
	 *                less than Thread.MIN_PRIORITY
	 * @see Thread#getPriority
	 */
	public final void setPriority(int priority) {
        vmThread.nativeSetPriority(priority);
		return;  //fixit -- this is incorrect code but its good enough for simple "hello world" demo
	}

	/**
	 * A sample implementation of this method is provided by the reference
	 * implementation. It must be included, as it is called by ThreadLocal.set()
	 * and InheritableThreadLocal.set(). Set the value associated with the
	 * ThreadLocal in the receiver to be <code>value</code>.
	 * 
	 * @param local
	 *            ThreadLocal to set
	 * @param value
	 *            new value for the ThreadLocal
	 * @see #getThreadLocal
	 */
	void setThreadLocal(ThreadLocal local, Object value) {
        threadLocalSlot.set(value);
		return;
	}

	/**
	 * Causes the thread which sent this message to sleep an interval of time
	 * (given in milliseconds). The precision is not guaranteed - the Thread may
	 * sleep more or less than requested.
	 * 
	 * @param time
	 *            The time to sleep in milliseconds.
	 * @exception InterruptedException
	 *                if <code>interrupt()</code> was called for this Thread
	 *                while it was sleeping
	 * @see Thread#interrupt()
	 */

	public static void sleep(long time) throws InterruptedException {
        //fixit -- this is incorrect code that is good enough to allow simple "hello world" demo to run
		return;
	}

	/**
	 * Causes the thread which sent this message to sleep an interval of time
	 * (given in milliseconds). The precision is not guaranteed - the Thread may
	 * sleep more or less than requested.
	 * 
	 * @param time
	 *            The time to sleep in milliseconds.
	 * @param nanos
	 *            Extra nanosecond precision
	 * @exception InterruptedException
	 *                if <code>interrupt()</code> was called for this Thread
	 *                while it was sleeping
	 * @see Thread#interrupt()
	 */
	public static void sleep(long time, int nanos) throws InterruptedException {
        //fixit -- this is incorrect code that is good enough to allow simple "hello world" demo to run
		return;
	};

	/**
	 * Starts the new Thread of execution. The <code>run()</code> method of
	 * the receiver will be called by the receiver Thread itself (and not the
	 * Thread calling <code>start()</code>).
	 * 
	 * @exception IllegalThreadStateException
	 *                Unspecified in the Java language specification
	 * @see Thread#run
	 */
	public void start() {
        int stackSize = 1024 * 64;
        if (java.lang.ThreadGroup.root == null) 
        {
            java.lang.ThreadGroup tg = new java.lang.ThreadGroup();
            java.lang.ThreadGroup.root = tg;
        }
        group = java.lang.ThreadGroup.root; //wjw temporary, during bringup
        try {
            synchronized (vmThread) {
                vmThread.start(stackSize);
                vmThread.wait();
            }
        } catch (Throwable t) {
            System.err.println("error starting thread");
            t.printStackTrace();
        }
	}

	/**
	 * Requests the receiver Thread to stop and throw ThreadDeath. The Thread is
	 * resumed if it was suspended and awakened if it was sleeping, so that it
	 * can proceed to throw ThreadDeath.
	 * 
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @deprecated
	 */
	public final void stop() {
        //fixit -- doing a "nop" will allow simple "hello world" app to run
		return;
	}

	/**
	 * Requests the receiver Thread to stop and throw the
	 * <code>throwable()</code>. The Thread is resumed if it was suspended
	 * and awakened if it was sleeping, so that it can proceed to throw the
	 * <code>throwable()</code>.
	 * 
	 * @param throwable
	 *            Throwable object to be thrown by the Thread
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @exception NullPointerException
	 *                if <code>throwable()</code> is <code>null</code>
	 * @deprecated
	 */
	public final void stop(Throwable throwable) {
        //fixit -- doing a "nop" will allow simple "hello world" app to run
		return;
	}

	/**
	 * This is a no-op if the receiver is suspended. If the receiver
	 * <code>isAlive()</code> however, suspended it until
	 * <code>resume()</code> is sent to it. Suspend requests are not queued,
	 * which means that N requests are equivalenet to just one - only one resume
	 * request is needed in this case.
	 * 
	 * @exception SecurityException
	 *                if <code>checkAccess()</code> fails with a
	 *                SecurityException
	 * @see Thread#resume()
	 * @deprecated May cause deadlocks.
	 */
	public final void suspend() {
        //fixit -- doing a "nop" will allow simple "hello world" app to run
		return;
	}

	/**
	 * Answers a string containing a concise, human-readable description of the
	 * receiver.
	 * 
	 * @return a printable representation for the receiver.
	 */
	public String toString() {
		return name.toString();
	}

	/**
	 * Causes the thread which sent this message to yield execution to another
	 * Thread that is ready to run. The actual scheduling is
	 * implementation-dependent.
	 * 
	 */
	public static void yield() {
		VMThread.yield();
	};

	/**
	 * Returns whether the current thread has a monitor lock on the specified
	 * object.
	 * 
	 * @param object
	 *            the object to test for the monitor lock
	 * @return true when the current thread has a monitor lock on the specified
	 *         object
	 */
	public static boolean holdsLock(Object object) {
		return false;  //fixit -- simply returning "false" is good enough to get simple "hello world" demo going
	};

}

