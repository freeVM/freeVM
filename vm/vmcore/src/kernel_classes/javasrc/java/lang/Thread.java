/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Roman S. Bushmanov
 * @version $Revision: 1.1.2.4.4.4 $
 */

package java.lang;

import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.harmony.lang.RuntimePermissionCollection;
import org.apache.harmony.fortress.security.SecurityUtils;
import org.apache.harmony.vm.VMStack;

/**
 * @com.intel.drl.spec_ref 
 */
public class Thread implements Runnable {

    /**
     * @com.intel.drl.spec_ref
     */
    public static final int MAX_PRIORITY = 10;

    /**
     * @com.intel.drl.spec_ref
     */
    public static final int MIN_PRIORITY = 1;

    /**
     * @com.intel.drl.spec_ref
     */
    public static final int NORM_PRIORITY = 5;

    /**
     * Indent string used to print stack trace
     */
    private static final String STACK_TRACE_INDENT = "    ";

    /**
     * Counter used to generate default thread names
     */
    private static int threadCounter = 0;

    /**
     * This thread's thread group
     */
    ThreadGroup group;

    /**
     * This thread's context class loader
     */
    private ClassLoader contextClassLoader;

    /**
     * Indicates whether this thread was marked as daemon
     */
    private boolean daemon;

    /**
     * Thread's name
     */
    private String name;

    /**
     * Thread's priority
     */
    private int priority;

    /**
     * Stack size to be passed to VM for thread execution
     */
    private long stackSize;

    /**
     * Indicates if the thread was already started
     */
    boolean started = false;

    
    /**
     * Indicates if the thread is alive.
     */
    boolean isAlive = false;

    /**
     * Thread's target - a <code>Runnable</code> object whose <code>run</code>
     * method should be invoked
     */
    private Runnable target;

    /**
     * This map is used to provide <code>ThreadLocal</code> functionality.
     * Maps <code>ThreadLocal</code> object to value. Lazy initialization is
     * used to avoid circular dependance.
     */
    private Map<ThreadLocal<Object>, Object> localValues = null;

    /**
     * Uncaught exception handler for this thread
     */
    private UncaughtExceptionHandler exceptionHandler = null;

    /**
     * Default uncaught exception handler
     */
    private static UncaughtExceptionHandler defaultExceptionHandler = null;

    /**
     * Thread's ID
     */
    private long threadId;

    /**
     * Counter used to generate thread's ID
     */
    private static long threadOrdinalNum = 0;

    /**
     * Synchronization is done using internal lock.
     */
    Object lock = new Object();

    /**
     * generates a unique thread ID
     */
    private static synchronized long getNextThreadId() {
            return ++threadOrdinalNum;
    }

    /*
     * used to generate a default thread name
     */
    private static final String THREAD = "Thread-";

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread() {
        this(null, null, THREAD, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(Runnable target) {
        this(null, target, THREAD, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(Runnable target, String name) {
        this(null, target, name, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(String name) {
        this(null, null, name, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(ThreadGroup group, Runnable target) {
        this(group, target, THREAD, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(ThreadGroup group, Runnable target, String name) {
        this(group, target, name, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(ThreadGroup group, Runnable target, String name,
                  long stackSize) {

        Thread currentThread = VMThreadManager.currentThread();
        SecurityManager securityManager = System.getSecurityManager();

            ThreadGroup threadGroup = null;
            if (group != null) {
                if (securityManager != null) {
                    securityManager.checkAccess(group);
                }
                threadGroup = group;
            } else if (securityManager != null) {
                threadGroup = securityManager.getThreadGroup();
            }
            if (threadGroup == null) {
                threadGroup = currentThread.group;
            }
            this.group = threadGroup;
            // throws NullPointerException if the given name is null
            this.name = (name != THREAD) ? this.name = name.toString() : THREAD
                    + threadCounter++;
            this.daemon = currentThread.daemon;
            this.contextClassLoader = currentThread.contextClassLoader;
            this.target = target;
            this.stackSize = stackSize;
            this.priority = currentThread.priority;
            initializeInheritableLocalValues(currentThread);
    
        checkGCWatermark();
        
        ThreadWeakRef oldRef = ThreadWeakRef.poll();
        ThreadWeakRef newRef = new ThreadWeakRef(this);
        
        long oldPointer = (oldRef == null)? 0 : oldRef.getNativeAddr();
        long newPointer = VMThreadManager.init(this, newRef, oldPointer);
        if (newPointer == 0) {
           throw new OutOfMemoryError("Failed to create new thread");   
        }
        newRef.setNativeAddr(newPointer);
        
        this.threadId = getNextThreadId();
        SecurityUtils.putContext(this, AccessController.getContext());
        checkAccess(); 
        threadGroup.add(this);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    Thread(boolean nativeThread) {
        VMThreadManager.attach(this);
        this.name = "System thread";
        this.group = new ThreadGroup();
        this.group.add(this);
        this.daemon = false;
        this.started = true;
        this.priority = NORM_PRIORITY;
        // initialize the system class loader and set it as context
        // classloader
        ClassLoader.getSystemClassLoader();
        
        this.threadId = getNextThreadId();
        SecurityUtils.putContext(this, AccessController.getContext());
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread(ThreadGroup group, String name) {
        this(group, null, name, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static int activeCount() {
        return currentThread().group.activeCount();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static Thread currentThread() {
        return VMThreadManager.currentThread();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void dumpStack() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        System.err.println("Stack trace");
        for (int i = 0; i < stack.length; i++) {
            System.err.println(STACK_TRACE_INDENT + stack[i]);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static int enumerate(Thread[] list) {
        return currentThread().group.enumerate(list);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static boolean holdsLock(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return VMThreadManager.holdsLock(object);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static boolean interrupted() {
        return VMThreadManager.isInterrupted();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void sleep(long millis) throws InterruptedException {
        sleep(millis, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void sleep(long millis, int nanos)
        throws InterruptedException {
        if (millis < 0 || nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                "Arguments don't match the expected range!");
        }
        int status = VMThreadManager.sleep(millis, nanos);
        if (status == VMThreadManager.TM_ERROR_INTERRUPT) {
            throw new InterruptedException();        
        } else if (status != VMThreadManager.TM_ERROR_NONE) {
            throw new InternalError(
                "Thread Manager internal error " + status);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void yield() {
        int status = VMThreadManager.yield();
        if (status != VMThreadManager.TM_ERROR_NONE) {
            throw new InternalError(
                "Thread Manager internal error " + status);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void checkAccess() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccess(this);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int countStackFrames() {
        return 0; //deprecated
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void destroy() {
        // this method is not implemented
        throw new NoSuchMethodError();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager
                .checkPermission(RuntimePermissionCollection.GET_STACK_TRACE_PERMISSION);
            securityManager
                .checkPermission(RuntimePermissionCollection.MODIFY_THREAD_GROUP_PERMISSION);
        }
        
        // find the initial ThreadGroup in the tree
        ThreadGroup parent = new ThreadGroup(currentThread().getThreadGroup(), "Temporary");
        ThreadGroup newParent = parent.getParent();
        parent.destroy();
        while (newParent != null) {
            parent = newParent;
            newParent = parent.getParent();
        }
        int threadsCount = parent.activeCount() + 1;
        int count;
        Thread[] liveThreads;
        while (true) {
            liveThreads = new Thread[threadsCount];
            count = parent.enumerate(liveThreads);
            if (count == threadsCount) {
                threadsCount *= 2;
            } else {
                break;
            }
        }
        Map<Thread, StackTraceElement[]> map = new HashMap<Thread, StackTraceElement[]>(count + 1);
        for (int i = 0; i < count; i++) {
            StackTraceElement[] ste = liveThreads[i].getStackTrace();
            if (ste.length != 0) {
                map.put(liveThreads[i], ste);
            }
        }
        return map;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ClassLoader getContextClassLoader() {
        synchronized (lock) {
            // First, if the conditions
            //    1) there is a security manager
            //    2) the caller's class loader is not null
            //    3) the caller's class loader is not the same as or an
            //    ancestor of contextClassLoader
            // are satisfied we should perform a security check.
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                //the first condition is satisfied
                ClassLoader callerClassLoader = VMClassRegistry
                    .getClassLoader(VMStack.getCallerClass(0));
                if (callerClassLoader != null) {
                    //the second condition is satisfied
                    ClassLoader classLoader = contextClassLoader;
                    while (classLoader != null) {
                        if (classLoader == callerClassLoader) {
                            //the third condition is not satisfied
                            return contextClassLoader;
                        }
                        classLoader = classLoader.getParent();
                    }
                    //the third condition is satisfied
                    securityManager
                        .checkPermission(RuntimePermissionCollection.GET_CLASS_LOADER_PERMISSION);
                }
            }
            return contextClassLoader;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final String getName() {
        return name;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public StackTraceElement[] getStackTrace() {
        if (currentThread() != this) {
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager
                    .checkPermission(RuntimePermissionCollection.GET_STACK_TRACE_PERMISSION);
            }
        }
        StackTraceElement ste[] = VMStack.getThreadStackTrace(this);
        if (ste != null) {
            return ste;
        } else {
            return  new StackTraceElement[0];
        }
    }
    
    /**
     * @com.intel.drl.spec_ref
     */
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public long getId() {        
        return threadId;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void interrupt() {
        synchronized (lock) {
            checkAccess();
            int status = VMThreadManager.interrupt(this);
            if (status != VMThreadManager.TM_ERROR_NONE) {
                throw new InternalError(
                    "Thread Manager internal error " + status);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final boolean isAlive() {
        synchronized (lock) {
            return this.isAlive;
        }
    }


    /**
     * @com.intel.drl.spec_ref
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean isInterrupted() {
        return VMThreadManager.isInterrupted(this);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void join() throws InterruptedException {
        synchronized (lock) {
            while (isAlive()) {
                lock.wait();
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void join(long millis) throws InterruptedException {
        if (millis == 0) {
            join();
            return;
        }
        
        synchronized (lock) {
            long end = System.currentTimeMillis() + millis;
            while(isAlive()) {
            lock.wait(millis);
                millis = end - System.currentTimeMillis();
                if (millis <= 0) return;
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void join(long millis, int nanos)
        throws InterruptedException {
        if (millis == 0 && nanos == 0) {
            join();
            return;
        }
        
        synchronized (lock) {
            long end = System.nanoTime() + 1000000*millis + (long)nanos;
            long rest;
            while (isAlive()) {
            lock.wait(millis, nanos);
                rest = end - System.nanoTime();
                if (rest <= 0) return;
                nanos  = (int)(rest % 1000000);
                millis = rest / 1000000;
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void resume() {
        synchronized (lock) {
            checkAccess();
            int status = VMThreadManager.resume(this);
            if (status != VMThreadManager.TM_ERROR_NONE) {
                throw new InternalError(
                    "Thread Manager internal error " + status);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setContextClassLoader(ClassLoader classLoader) {
        synchronized (lock) {
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager
                    .checkPermission(RuntimePermissionCollection.SET_CONTEXT_CLASS_LOADER_PERMISSION);
            }
            contextClassLoader = classLoader;
        }
    }

    /**
     * @com.intel.drl.spec_ref We assume that 'active thread' means the same as
     *                         'alive thread'.
     */
    public final void setDaemon(boolean daemon) {
        synchronized (lock) {
            checkAccess();
            if (isAlive()) {
                throw new IllegalThreadStateException();
            }
            this.daemon = daemon;
        }
    }

    /**
     * @com.intel.drl.spec_ref New name should not be <code>null</code>.
     * @throws NullPointerException if new name is <code>null</code>
     */
    public final void setName(String name) {
        checkAccess();
        // throws NullPointerException if the given name is null
        this.name = name.toString();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void setPriority(int priority) {
        checkAccess();
        if (priority > MAX_PRIORITY || priority < MIN_PRIORITY) {
            throw new IllegalArgumentException("Wrong Thread priority value");
        }
        ThreadGroup threadGroup = group;
        this.priority = (priority > threadGroup.maxPriority)
            ? threadGroup.maxPriority : priority;
        int status = VMThreadManager.setPriority(this, this.priority);
        if (status != VMThreadManager.TM_ERROR_NONE) {
        //    throw new InternalError(
        //        "Thread Manager internal error " + status);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void start() {
        synchronized (lock) {
            if (started) {
                //this thread was started
                throw new IllegalThreadStateException(
                        "This thread was already started!");
            }

            this.isAlive = true;
            
            if (VMThreadManager.start(this, stackSize, daemon, priority) != 0) {
                throw new OutOfMemoryError("Failed to start new thread");
            } 
            
            started = true;
        }
    }

    /*
     * This method serves as a wrapper around Thread.run() method to meet 
     * specification requirements in regard to ucaught exception catching.
     */
    void runImpl() {
        try {
            run();
        } catch (Throwable e) {
           getUncaughtExceptionHandler().uncaughtException(this, e);
        } finally {
            group.remove(this);
            synchronized(lock) {
                this.isAlive = false;
                lock.notifyAll();
            }
        }
    }

    

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Thread.State  getState() {
        
        int state = (VMThreadManager.getState(this));

        if (0 != (state & VMThreadManager.JVMTI_THREAD_STATE_TERMINATED)) {         
            return State.TERMINATED;
        } else if  (0 != (state & VMThreadManager.JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT)) {
            return State.TIMED_WAITING;
        } else if (0 != (state & VMThreadManager.JVMTI_THREAD_STATE_WAITING) 
                || 0 != (state & VMThreadManager.JVMTI_THREAD_STATE_PARKED)) {
            return State.WAITING;
        } else if (0 != (state & VMThreadManager.JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER)) {
            return State.BLOCKED;
        } else if (0 != (state & VMThreadManager.JVMTI_THREAD_STATE_ALIVE)) {
            return State.RUNNABLE;
        } else { 
            return State.NEW;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void stop() {
        stop(new ThreadDeath());
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void stop(Throwable throwable) {
        synchronized (lock) {
            if (throwable == null) {
                throw new NullPointerException("The argument is null!");
            }
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager.checkAccess(this);
                if (currentThread() != this) {
                    securityManager
                        .checkPermission(RuntimePermissionCollection.STOP_THREAD_PERMISSION);
                }
            }
            int status = VMThreadManager.stop(this, throwable);
            if (status != VMThreadManager.TM_ERROR_NONE) {
                throw new InternalError(
                    "Thread Manager internal error " + status);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final void suspend() {
        synchronized (lock) {
            checkAccess();
            int status = VMThreadManager.suspend(this);
            if (status != VMThreadManager.TM_ERROR_NONE) {
                throw new InternalError(
                    "Thread Manager internal error " + status);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        ThreadGroup threadGroup = group;
        return "Thread[" + name + "," + priority + ","
            + ( (threadGroup == null) ? "" : threadGroup.name) + "]";
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultExceptionHandler;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void setDefaultUncaughtExceptionHandler(
                                                          UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm
                .checkPermission(RuntimePermissionCollection.SET_DEFAULT_UNCAUGHT_EXCEPTION_HANDLER_PERMISSION);
        }
        defaultExceptionHandler = eh;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        if (exceptionHandler != null) {
            return exceptionHandler;
        }
        return getThreadGroup();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm
                .checkPermission(RuntimePermissionCollection.MODIFY_THREAD_PERMISSION);
        }
        exceptionHandler = eh;
    }

    /**
     * Associates the value specified to the <code>ThreadLocal</code> object
     * given. <br>
     * This nethod is designed to provide <code>ThreadLocal</code>
     * functionality.
     */
    void setThreadLocal(ThreadLocal<Object> local, Object value) {
        if (localValues == null) {
            localValues = new HashMap<ThreadLocal<Object>, Object>();
        }
        localValues.put(local, value);
    }

    /**
     * Returns the value associated with the <code>ThreadLocal</code> object
     * specified. If no value is associated, returns the value produced by
     * <code>initialValue()</code> method called for this object and
     * associates this value to <code>ThreadLocal</code> object. <br>
     * This nethod is designed to provide <code>ThreadLocal</code>
     * functionality.
     */
    Object getThreadLocal(ThreadLocal<Object> local) {
        Object value;
        if (localValues == null) {
            localValues = new HashMap<ThreadLocal<Object>, Object>();
            value = local.initialValue();
            localValues.put(local, value);
            return value;
        }
        if (localValues.containsKey(local)) {
            return localValues.get(local);
        }
        value = local.initialValue();
        localValues.put(local, value);
        return value;
    }
    
    /**
     * Removes the association (if any) between the <code>ThreadLocal</code> object
     * given and this thread's value. <br>
     * This nethod is designed to provide <code>ThreadLocal</code>
     * functionality.
     */
    void removeLocalValue(ThreadLocal<Object> local) {
        if (localValues != null) {
            localValues.remove(local);
        }
    }
    
    /**
     * Initializes local values represented by
     * <code>InheritableThreadLocal</code> objects having local values for the
     * parent thread <br>
     * This method is designed to provide the functionality of
     * <code>InheritableThreadLocal</code> class <br>
     * This method should be called from <code>Thread</code>'s constructor.
     */
    private void initializeInheritableLocalValues(Thread parent) {
        Map<ThreadLocal<Object>, Object> parentLocalValues = parent.localValues;
        if (parentLocalValues == null) {
           return;
        }
        localValues = new HashMap<ThreadLocal<Object>, Object>(parentLocalValues.size());
        for (Iterator<ThreadLocal<Object>> it = parentLocalValues.keySet().iterator(); it.hasNext();) {
            ThreadLocal<Object> local = it.next();
            if (local instanceof InheritableThreadLocal) {
                Object parentValue = parentLocalValues.get(local);
                InheritableThreadLocal<Object> iLocal = (InheritableThreadLocal<Object>) local;
                localValues.put(local, iLocal.childValue(parentValue));
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static interface UncaughtExceptionHandler {

        /**
         * @com.intel.drl.spec_ref
         */
        void uncaughtException(Thread t, Throwable e);
    }

    /*
     * Number of threads that was created w/o garbage collection.
     */ 
    private static int       currentGCWatermarkCount = 0;

    /*
     * Max number of threads to be created w/o GC, required collect dead Thread 
     * references.
     */
    private static final int GC_WATERMARK_MAX_COUNT     = 700;
    
    /*
     * Checks if more then GC_WATERMARK_MAX_COUNT threads was created and calls
     * System.gc() to ensure that dead thread references was callected.
     */
    private void             checkGCWatermark() {
        if (++currentGCWatermarkCount % GC_WATERMARK_MAX_COUNT == 0) {
            System.gc();
        }
    }
}
