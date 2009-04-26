/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.Map;

/**
 * A {@code Thread} is a concurrent unit of execution. It has its own call stack
 * for methods being invoked, their arguments and local variables. Each virtual
 * machine instance has at least one main {@code Thread} running when it is
 * started; typically, there are several others for housekeeping. The
 * application might decide to launch additional {@code Thread}s for specific
 * purposes.
 * <p>
 * {@code Thread}s in the same VM interact and synchronize by the use of shared
 * objects and monitors associated with these objects. Synchronized methods and
 * part of the API in {@link Object} also allow {@code Thread}s to cooperate.
 * <p>
 * There are basically two main ways of having a {@code Thread} execute
 * application code. One is providing a new class that extends {@code Thread}
 * and overriding its {@link #run()} method. The other is providing a new
 * {@code Thread} instance with a {@link Runnable} object during its creation.
 * In both cases, the {@link #start()} method must be called to actually execute
 * the new {@code Thread}.
 * <p>
 * Each {@code Thread} has an integer priority that basically determines the
 * amount of CPU time the {@code Thread} gets. It can be set using the
 * {@link #setPriority(int)} method. A {@code Thread} can also be made a daemon,
 * which makes it run in the background. The latter also affects VM termination
 * behavior: the VM does not terminate automatically as long as there are
 * non-daemon threads running.
 *
 * @see java.lang.Object
 * @see java.lang.ThreadGroup
 */
public class Thread implements Runnable {

    /*
     * This class must be implemented by the VM vendor. The documented methods must
     * be implemented to support other provided class implementations in this
     * package.
     */

    /**
    * A representation of a thread's state. A given thread may only be in one
    * state at a time.
    *
    * @since 1.5
    */

    /**
     * A representation of a thread's state. A given thread may only be in one
     * state at a time.
     */
    public enum State {
        /**
         * The thread has been created, but has never been started.
         */
        NEW,
        /**
         * The thread may be run.
         */
        RUNNABLE,
        /**
         * The thread is blocked and waiting for a lock.
         */
        BLOCKED,
        /**
         * The thread is waiting.
         */
        WAITING,
        /**
         * The thread is waiting for a specified amount of time.
         */
        TIMED_WAITING,
        /**
         * The thread has been terminated.
         */
        TERMINATED
    }

    /**
     * <p>
     * The maximum priority value allowed for a thread.
     * </p>
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * <p>
     * The minimum priority value allowed for a thread.
     * </p>
     */
    public final static int MIN_PRIORITY = 1;

    /**
     * <p>
     * The normal (default) priority value assigned to threads.
     * </p>
     */
    public final static int NORM_PRIORITY = 5;

    Object slot1;

    Object slot2;

    Object slot3;

    private Runnable action;

    /**
     * Constructs a new Thread with no runnable object and a newly generated
     * name. The new Thread will belong to the same ThreadGroup as the Thread
     * calling this constructor.
     * 
     * @see java.lang.ThreadGroup
     */
    public Thread() {
        super();
    }

    /**
     * Constructs a new Thread with a runnable object and a newly generated
     * name. The new Thread will belong to the same ThreadGroup as the Thread
     * calling this constructor.
     * 
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(Runnable runnable) {
        super();
    }

    /**
     * Constructs a new Thread with a runnable object and name provided. The new
     * Thread will belong to the same ThreadGroup as the Thread calling this
     * constructor.
     * 
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(Runnable runnable, String threadName) {
        super();
    }

    /**
     * Constructs a new Thread with no runnable object and the name provided.
     * The new Thread will belong to the same ThreadGroup as the Thread calling
     * this constructor.
     * 
     * @param threadName Name for the Thread being created
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(String threadName) {
        super();
    }

    /**
     * Constructs a new Thread with a runnable object and a newly generated
     * name. The new Thread will belong to the ThreadGroup passed as parameter.
     * 
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable) {
        super();
    }

    /**
     * Constructs a new Thread with a runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     * 
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @param stack Platform dependent stack size
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable, String threadName, long stack) {
        super();
    }

    /**
     * Constructs a new Thread with a runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     * 
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable, String threadName) {
        super();
    }

    /**
     * Constructs a new Thread with no runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     * 
     * @param group ThreadGroup to which the new Thread will belong
     * @param threadName Name for the Thread being created
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, String threadName) {
        super();
    }

    /**
     * Set the action to be executed when interruption, which is probably be
     * used to implement the interruptible channel. The action is null by
     * default. And if this method is invoked by passing in a non-null value,
     * this action's run() method will be invoked in <code>interrupt()</code>.
     * <p>
     * This is required internally by NIO, so even if it looks like it's
     * useless, don't delete it!
     *
     * @param action the action to be executed when interruption
     */
    @SuppressWarnings("unused")
    private void setInterruptAction(Runnable action) {
        this.action = action;
    }

    /**
     * Returns the number of active {@code Thread}s in the running {@code
     * Thread}'s group and its subgroups.
     * 
     * @return the number of {@code Thread}s
     */
    public static int activeCount() {
        return 0;
    }

    /**
     * Is used for operations that require approval from a SecurityManager. If
     * there's none installed, this method is a no-op. If there's a
     * SecurityManager installed, {@link SecurityManager#checkAccess(Thread)} is
     * called for that SecurityManager.
     * 
     * @throws SecurityException
     *             if a SecurityManager is installed and it does not allow
     *             access to the Thread.
     *
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public final void checkAccess() {
        return;
    }

    /**
     * Returns the number of stack frames in this thread.
     * 
     * @return Number of stack frames
     * @deprecated The results of this call were never well defined. To make
     *             things worse, it would depend on whether the Thread was
     *             suspended or not, and suspend was deprecated too.
     */
    @Deprecated
    public int countStackFrames() {
        return 0;
    }

    /**
     * Returns the Thread of the caller, that is, the current Thread.
     *
     * @return the current Thread.
     */
    public static Thread currentThread() {
        return null;
    }

    /**
     * Destroys the receiver without any monitor cleanup.
     *
     * @deprecated Not implemented.
     */
    @Deprecated
    public void destroy() {
        return;
    }

    /**
     * Prints to the standard error stream a text representation of the current
     * stack for this Thread.
     * 
     * @see Throwable#printStackTrace()
     */
    public static void dumpStack() {
        return;
    }

    /**
     * Copies an array with all Threads which are in the same ThreadGroup as the
     * receiver - and subgroups - into the array <code>threads</code> passed as
     * parameter. If the array passed as parameter is too small no exception is
     * thrown - the extra elements are simply not copied.
     * 
     * @param threads
     *            array into which the Threads will be copied
     * @return How many Threads were copied over
     * @throws SecurityException
     *             if the installed SecurityManager fails
     *             {@link SecurityManager#checkAccess(Thread)}
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public static int enumerate(Thread[] threads) {
        return 0;
    }

    /**
     * Returns the stack traces of all the currently live threads and puts them
     * into the given map.
     * <p>
     * The <code>RuntimePermission("getStackTrace")</code> and
     * <code>RuntimePermission("modifyThreadGroup")</code> are checked before
     * returning a result.
     *
     * @return A Map of current Threads to StackTraceElement arrays.
     * @throws SecurityException
     *             if the current SecurityManager fails the
     *             {@link SecurityManager#checkPermission(java.security.Permission)}
     *             call.
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        return Collections.emptyMap();
    }

    /**
     * Returns the context ClassLoader for this Thread.
     * <p>
     * If the conditions
     * <ol>
     * <li>there is a security manager
     * <li>the caller's class loader is not null
     * <li>the caller's class loader is not the same as the requested
     * context class loader and not an ancestor thereof
     * </ol>
     * are satisfied, a security check for
     * <code>RuntimePermission("getClassLoader")</code> is performed first.
     *
     * @return ClassLoader The context ClassLoader
     * @see java.lang.ClassLoader
     * @see #getContextClassLoader()
     *
     * @throws SecurityException
     *             if the aforementioned security check fails.
     */
    public ClassLoader getContextClassLoader() {
        return null;
    }

    /**
     * Returns the default exception handler that's executed when uncaught
     * exception terminates a thread.
     *
     * @return an {@link UncaughtExceptionHandler} or <code>null</code> if
     *         none exists.
     * @since 1.5
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return null;
    }

    /**
     * Returns the thread's identifier. The ID is a positive <code>long</code>
     * generated on thread creation, is unique to the thread, and doesn't change
     * during the lifetime of the thread; the ID may be reused after the thread
     * has been terminated.
     *
     * @return the thread's ID.
     * @since 1.5
     */
    public long getId() {
        return 1L;
    }

    /**
     * Returns the name of the Thread.
     *
     * @return the Thread's name
     */
    public final String getName() {
        return null;
    }

    /**
     * Returns the priority of the Thread.
     *
     * @return the Thread's priority
     * @see Thread#setPriority
     */
    public final int getPriority() {
        return 0;
    }

    /**
     * Returns the a stack trace representing the current execution state of
     * this Thread.
     * <p>
     * The <code>RuntimePermission("getStackTrace")</code> is checked before
     * returning a result.
     *
     * @return An array of StackTraceElements.
     * @throws SecurityException if the current SecurityManager fails the
     *         {@link SecurityManager#checkPermission(java.security.Permission)}
     *         call.
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    /**
     * Returns the current state of the Thread. This method is useful for
     * monitoring purposes.
     *
     * @return a {@link State} value.
     * @since 1.5
     */
    public State getState() {
        return null;
    }

    /**
     * Returns the ThreadGroup to which this Thread belongs.
     * 
     * @return the Thread's ThreadGroup
     */
    public final ThreadGroup getThreadGroup() {
        return null;
    }

    /**
     * A sample implementation of this method is provided by the reference
     * implementation. It must be included, as it is called by ThreadLocal.get()
     * and InheritableThreadLocal.get(). Return the value associated with the
     * ThreadLocal in the receiver
     * 
     * @param local ThreadLocal to perform the lookup
     * @return the value of the ThreadLocal
     * @see #setThreadLocal
     */
    Object getThreadLocal(ThreadLocal<?> local) {
        return null;
    }

    /**
     * Returns the thread's uncaught exception handler. If not explicitly set,
     * then the ThreadGroup's handler is returned. If the thread is terminated,
     * then <code>null</code> is returned.
     *
     * @return an {@link UncaughtExceptionHandler} instance or {@code null}.
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return null;
    }

    /**
     * Posts an interrupt request to this {@code Thread}. Unless the caller is
     * the {@link #currentThread()}, the method {@code checkAccess()} is called
     * for the installed {@code SecurityManager}, if any. This may result in a
     * {@code SecurityException} being thrown. The further behavior depends on
     * the state of this {@code Thread}:
     * <ul>
     * <li>
     * {@code Thread}s blocked in one of {@code Object}'s {@code wait()} methods
     * or one of {@code Thread}'s {@code join()} or {@code sleep()} methods will
     * be woken up, their interrupt status will be cleared, and they receive an
     * {@link InterruptedException}.
     * <li>
     * {@code Thread}s blocked in an I/O operation of an
     * {@link java.nio.channels.InterruptibleChannel} will have their interrupt
     * status set and receive an
     * {@link java.nio.channels.ClosedByInterruptException}. Also, the channel
     * will be closed.
     * <li>
     * {@code Thread}s blocked in a {@link java.nio.channels.Selector} will have
     * their interrupt status set and return immediately. They don't receive an
     * exception in this case.
     * <ul>
     * 
     * @throws SecurityException
     *             if <code>checkAccess()</code> fails with a SecurityException
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     * @see Thread#interrupted
     * @see Thread#isInterrupted
     */
    public void interrupt() {
        if (action != null) {
            action.run();
        }
        return;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the current Thread (
     * <code>currentThread()</code>) has a pending interrupt request (<code>
     * true</code>) or not (<code>false</code>). It also has the side-effect of
     * clearing the flag.
     * 
     * @return a <code>boolean</code> indicating the interrupt status
     * @see Thread#currentThread
     * @see Thread#interrupt
     * @see Thread#isInterrupted
     */
    public static boolean interrupted() {
        return false;
    }

    /**
     * Returns <code>true</code> if the receiver has already been started and
     * still runs code (hasn't died yet). Returns <code>false</code> either if
     * the receiver hasn't been started yet or if it has already started and run
     * to completion and died.
     * 
     * @return a <code>boolean</code> indicating the lifeness of the Thread
     * @see Thread#start
     */
    public final boolean isAlive() {
        return false;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the receiver is a
     * daemon Thread (<code>true</code>) or not (<code>false</code>) A
     * daemon Thread only runs as long as there are non-daemon Threads running.
     * When the last non-daemon Thread ends, the whole program ends no matter if
     * it had daemon Threads still running or not.
     *
     * @return a <code>boolean</code> indicating whether the Thread is a daemon
     * @see Thread#setDaemon
     */
    public final boolean isDaemon() {
        return false;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the receiver has a
     * pending interrupt request (<code>true</code>) or not (
     * <code>false</code>)
     *
     * @return a <code>boolean</code> indicating the interrupt status
     * @see Thread#interrupt
     * @see Thread#interrupted
     */
    public boolean isInterrupted() {
        return false;
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies.
     *
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join() throws InterruptedException {
        return;
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies or the specified timeout
     * expires, whatever happens first.
     *
     * @param millis The maximum time to wait (in milliseconds).
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join(long millis) throws InterruptedException {
        return;
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies or the specified timeout
     * expires, whatever happens first.
     *
     * @param millis The maximum time to wait (in milliseconds).
     * @param nanos Extra nanosecond precision
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join(long millis, int nanos) throws InterruptedException {
        return;
    }

    /**
     * Resumes a suspended Thread. This is a no-op if the receiver was never
     * suspended, or suspended and already resumed. If the receiver is
     * suspended, however, makes it resume to the point where it was when it was
     * suspended.
     * 
     * @throws SecurityException
     *             if <code>checkAccess()</code> fails with a SecurityException
     * @see Thread#suspend()
     * @deprecated Used with deprecated method {@link Thread#suspend}
     */
    @Deprecated
    public final void resume() {
        return;
    }

    /**
     * Calls the <code>run()</code> method of the Runnable object the receiver
     * holds. If no Runnable is set, does nothing.
     *
     * @see Thread#start
     */
    public void run() {
        return;
    }

    /**
     * Set the context ClassLoader for the receiver.
     * <p>
     * The <code>RuntimePermission("setContextClassLoader")</code>
     * is checked prior to setting the handler.
     *
     * @param cl The context ClassLoader
     * @throws SecurityException if the current SecurityManager fails the
     *         checkPermission call.
     * @see java.lang.ClassLoader
     * @see #getContextClassLoader()
     */
    public void setContextClassLoader(ClassLoader cl) {
        return;
    }

    /**
     * Set if the receiver is a daemon Thread or not. This can only be done
     * before the Thread starts running.
     * 
     * @param isDaemon
     *            indicates whether the Thread should be daemon or not
     * @throws SecurityException
     *             if <code>checkAccess()</code> fails with a SecurityException
     * @see Thread#isDaemon
     */
    public final void setDaemon(boolean isDaemon) {
        return;
    }

    /**
     * Sets the default uncaught exception handler. This handler is invoked in
     * case any Thread dies due to an unhandled exception.
     * <p>
     * The <code>RuntimePermission("setDefaultUncaughtExceptionHandler")</code>
     * is checked prior to setting the handler.
     *
     * @param handler
     *            The handler to set or <code>null</code>.
     * @throws SecurityException
     *             if the current SecurityManager fails the checkPermission
     *             call.
     * @since 1.5
     */
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        return;
    }

    /**
     * Sets the name of the Thread.
     *
     * @param threadName the new name for the Thread
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @see Thread#getName
     */
    public final void setName(String threadName) {
        return;
    }

    /**
     * Sets the priority of the Thread. Note that the final priority set may not
     * be the parameter that was passed - it will depend on the receiver's
     * ThreadGroup. The priority cannot be set to be higher than the receiver's
     * ThreadGroup's maxPriority().
     * 
     * @param priority
     *            new priority for the Thread
     * @throws SecurityException
     *             if <code>checkAccess()</code> fails with a SecurityException
     * @throws IllegalArgumentException
     *             if the new priority is greater than Thread.MAX_PRIORITY or
     *             less than Thread.MIN_PRIORITY
     * @see Thread#getPriority
     */
    public final void setPriority(int priority) {
        return;
    }

    /**
     * A sample implementation of this method is provided by the reference
     * implementation. It must be included, as it is called by ThreadLocal.set()
     * and InheritableThreadLocal.set(). Set the value associated with the
     * ThreadLocal in the receiver to be <code>value</code>.
     * 
     * @param local ThreadLocal to set
     * @param value new value for the ThreadLocal
     * @see #getThreadLocal
     */
    void setThreadLocal(ThreadLocal<?> local, Object value) {
        return;
    }

    /**
     * <p>
     * Sets the uncaught exception handler. This handler is invoked in case this
     * Thread dies due to an unhandled exception.
     *
     * @param handler
     *            The handler to set or <code>null</code>.
     * @throws SecurityException
     *             if the current SecurityManager fails the checkAccess call.
     * @since 1.5
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        return;
    }

    /**
     * Causes the thread which sent this message to sleep for the given interval
     * of time (given in milliseconds). The precision is not guaranteed - the
     * Thread may sleep more or less than requested.
     * 
     * @param time
     *            The time to sleep in milliseconds.
     * @throws InterruptedException
     *             if <code>interrupt()</code> was called for this Thread while
     *             it was sleeping
     * @see Thread#interrupt()
     */
    public static void sleep(long time) throws InterruptedException {
        return;
    }

    /**
     * Causes the thread which sent this message to sleep for the given interval
     * of time (given in milliseconds and nanoseconds). The precision is not
     * guaranteed - the Thread may sleep more or less than requested.
     * 
     * @param millis
     *            The time to sleep in milliseconds.
     * @param nanos
     *            Extra nanosecond precision
     * @throws InterruptedException
     *             if <code>interrupt()</code> was called for this Thread while
     *             it was sleeping
     * @see Thread#interrupt()
     */
    public static void sleep(long millis, int nanos) throws InterruptedException {
        return;
    }

    /**
     * Starts the new Thread of execution. The <code>run()</code> method of
     * the receiver will be called by the receiver Thread itself (and not the
     * Thread calling <code>start()</code>).
     *
     * @throws IllegalThreadStateException if the Thread has been started before
     * @see Thread#run
     */
    public void start() {
        return;
    }

    /**
     * Requests the receiver Thread to stop and throw ThreadDeath. The Thread is
     * resumed if it was suspended and awakened if it was sleeping, so that it
     * can proceed to throw ThreadDeath.
     *
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @deprecated because stopping a thread in this manner is unsafe and can
     * leave your application and the VM in an unpredictable state.
     */
    @Deprecated
    public final void stop() {
        return;
    }

    /**
     * Requests the receiver Thread to stop and throw the
     * <code>throwable()</code>. The Thread is resumed if it was suspended
     * and awakened if it was sleeping, so that it can proceed to throw the
     * <code>throwable()</code>.
     *
     * @param throwable Throwable object to be thrown by the Thread
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @throws NullPointerException if <code>throwable()</code> is
     *         <code>null</code>
     * @deprecated because stopping a thread in this manner is unsafe and can
     * leave your application and the VM in an unpredictable state.
     */
    @Deprecated
    public final void stop(Throwable throwable) {
        return;
    }

    /**
     * Suspends this Thread. This is a no-op if the receiver is suspended. If
     * the receiver <code>isAlive()</code> however, suspended it until <code>
     * resume()</code> is sent to it. Suspend requests are not queued, which
     * means that N requests are equivalent to just one - only one resume
     * request is needed in this case.
     * 
     * @throws SecurityException
     *             if <code>checkAccess()</code> fails with a SecurityException
     * @see Thread#resume()
     * @deprecated May cause deadlocks.
     */
    @Deprecated
    public final void suspend() {
        return;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * Thread. It includes the Thread's name, priority, and group name.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return null;
    }

    /**
     * Causes the calling Thread to yield execution time to another Thread that
     * is ready to run. The actual scheduling is implementation-dependent.
     */
    public static void yield() {
        return;
    }

    /**
     * Returns whether the current thread has a monitor lock on the specified
     * object.
     * 
     * @param object the object to test for the monitor lock
     * @return true when the current thread has a monitor lock on the specified
     *         object
     */
    public static boolean holdsLock(Object object) {
        return false;
    }

    /**
     * Implemented by objects that want to handle cases where a thread is being
     * terminated by an uncaught exception. Upon such termination, the handler
     * is notified of the terminating thread and causal exception. If there is
     * no explicit handler set then the thread's group is the default handler.
     */
    public static interface UncaughtExceptionHandler {
        /**
         * The thread is being terminated by an uncaught exception. Further
         * exceptions thrown in this method are prevent the remainder of the
         * method from executing, but are otherwise ignored.
         * 
         * @param thread the thread that has an uncaught exception
         * @param ex the exception that was thrown
         */
        void uncaughtException(Thread thread, Throwable ex);
    }
}
