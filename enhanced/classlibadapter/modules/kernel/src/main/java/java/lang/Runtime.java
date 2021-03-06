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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;


/**
 * This class, with the exception of the exec() APIs, must be implemented by the
 * vm vendor. The exec() APIs must first do any required security checks, and
 * then call com.ibm.oti.lang.SystemProcess.create(). The Runtime interface.
 */

public class Runtime {
	/**
	 * Execute progAray[0] in a seperate platform process The new process
	 * inherits the environment of the caller.
	 * 
	 * @param progArray
	 *            the array containing the program to execute as well as any
	 *            arguments to the program.
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String[] progArray) throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Execute progArray[0] in a seperate platform process The new process uses
	 * the environment provided in envp
	 * 
	 * @param progArray
	 *            the array containing the program to execute a well as any
	 *            arguments to the program.
	 * @param envp
	 *            the array containing the environment to start the new process
	 *            in.
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String[] progArray, String[] envp)
			throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
 		return null;
	}

	/**
	 * Execute progArray[0] in a seperate platform process The new process uses
	 * the environment provided in envp
	 * 
	 * @param progArray
	 *            the array containing the program to execute a well as any
	 *            arguments to the program.
	 * @param envp
	 *            the array containing the environment to start the new process
	 *            in.
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String[] progArray, String[] envp, File directory)
			throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Execute program in a seperate platform process The new process inherits
	 * the environment of the caller.
	 * 
	 * @param prog
	 *            the name of the program to execute
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String prog) throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Execute prog in a seperate platform process The new process uses the
	 * environment provided in envp
	 * 
	 * @param prog
	 *            the name of the program to execute
	 * @param envp
	 *            the array containing the environment to start the new process
	 *            in.
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String prog, String[] envp) throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Execute prog in a seperate platform process The new process uses the
	 * environment provided in envp
	 * 
	 * @param prog
	 *            the name of the program to execute
	 * @param envp
	 *            the array containing the environment to start the new process
	 *            in.
	 * @param directory
	 *            the initial directory for the subprocess, or null to use the
	 *            directory of the current process
	 * @exception java.io.IOException
	 *                if the program cannot be executed
	 * @exception SecurityException
	 *                if the current SecurityManager disallows program execution
	 * @see SecurityManager#checkExec
	 */
	public Process exec(String prog, String[] envp, File directory)
			throws java.io.IOException {
        //fixit -- always returning "null" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Causes the virtual machine to stop running, and the program to exit. If
	 * runFinalizersOnExit(true) has been invoked, then all finalizers will be
	 * run first.
	 * 
	 * @param code
	 *            the return code.
	 * @exception SecurityException
	 *                if the running thread is not allowed to cause the vm to
	 *                exit.
	 * @see SecurityManager#checkExit
	 */
	public void exit(int code) {
        VMRuntime.exit(code);
		return;
	}

	/**
	 * Answers the amount of free memory resources which are available to the
	 * running program.
	 * 
	 */
	public long freeMemory() {
        long ll = VMRuntime.freeMemory();
		return ll;
	};

	/**
	 * Indicates to the virtual machine that it would be a good time to collect
	 * available memory. Note that, this is a hint only.
	 * 
	 */
	public void gc() {
        VMRuntime.gc();
		return;
	};

    private static Runtime runtime;

    static {
        runtime = new Runtime();
    }

    private Runtime() {}

	/**
	 * Return the single Runtime instance
	 * 
	 */
	public static Runtime getRuntime() {
        // FIXME: all the security checks
        return runtime;
	}

    /**
     * Implementation of Runtime.load() without security checks.
     * For java.lang.* classes.
     * @param ClassLoader the classloader of class who initiated the loading.
     */
    void loadInternal(String pathName, ClassLoader classLoader) {
        VMRuntime.nativeLoad(
                pathName, classLoader);
    }

    /**
     * Implementation of Runtime.loadLibrary() without security checks.
     * For java.lang.* classes.
     */
    void loadLibraryInternal(String libName, ClassLoader classLoader) {
        String path = null;
        if (classLoader != null) {
            path = classLoader.findLibrary(libName);
        }

        if (path != null) {
            VMRuntime.nativeLoad(
                    path, classLoader);
            return;
        }

        String fileName = System.mapLibraryName(libName);
        String libraryPath = System.getProperty("java.library.path");
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");

        StringTokenizer tokens = new StringTokenizer(libraryPath, pathSeparator);

        // FIXME: too many exceptions thrown here, should be other way
        UnsatisfiedLinkError ule = null;
        while (tokens.hasMoreElements()) {
            String dir = tokens.nextToken();
            path = dir + fileSeparator + fileName;
            try {
                int res = VMRuntime.nativeLoad(
                        path, classLoader);
                if (res == 0) continue;
                return;
            } catch (UnsatisfiedLinkError e) {
                ule = e;
            }
        }
        if (ule != null) throw ule;
        throw new UnsatisfiedLinkError("failed loading " + fileName + " java.library.path=" + libraryPath);
    }

	/**
	 * Loads and links the library specified by the argument.
	 * 
	 * @param pathName
	 *            the absolute (ie: platform dependent) path to the library to
	 *            load
	 * @exception UnsatisfiedLinkError
	 *                if the library could not be loaded
	 * @exception SecurityException
	 *                if the library was not allowed to be loaded
	 */
	public void load(String pathName) {
        // FIXME: all the security checks
        ClassLoader classLoader = ClassLoader.callerClassLoader();
        loadInternal(pathName, classLoader);
	}

	/**
	 * Loads and links the library specified by the argument.
	 * 
	 * @param libName
	 *            the name of the library to load
	 * @exception UnsatisfiedLinkError
	 *                if the library could not be loaded
	 * @exception SecurityException
	 *                if the library was not allowed to be loaded
	 */
	public void loadLibrary(String libName) {
        // FIXME: all the security checks
        ClassLoader classLoader = ClassLoader.callerClassLoader();
        loadLibraryInternal(libName, classLoader);
	}

	/**
	 * Provides a hint to the virtual machine that it would be useful to attempt
	 * to perform any outstanding object finalizations.
	 * 
	 */
	public void runFinalization() {
        VMRuntime.runFinalization();
		return;
	};

	/**
	 * Ensure that, when the virtual machine is about to exit, all objects are
	 * finalized. Note that all finalization which occurs when the system is
	 * exiting is performed after all running threads have been terminated.
	 * 
	 * @param run
	 *            true means finalize all on exit.
	 * @deprecated This method is unsafe.
	 */
	public static void runFinalizersOnExit(boolean run) {
        VMRuntime.runFinalizersOnExit(run);
		return;
	};

	/**
	 * Answers the total amount of memory resources which is available to (or in
	 * use by) the running program.
	 * 
	 */
	public long totalMemory() {
        long ll = VMRuntime.totalMemory();
		return ll;
	};

	public void traceInstructions(boolean enable) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return;
	}

	public void traceMethodCalls(boolean enable) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return;
	}

	/**
	 * @deprecated Use InputStreamReader
	 */
	public InputStream getLocalizedInputStream(InputStream stream) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * @deprecated Use OutputStreamWriter
	 */
	public OutputStream getLocalizedOutputStream(OutputStream stream) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return null;
	}

	/**
	 * Registers a new virtual-machine shutdown hook.
	 * 
	 * @param hook
	 *            the hook (a Thread) to register
	 */
	public void addShutdownHook(Thread hook) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return;
	}

	/**
	 * De-registers a previously-registered virtual-machine shutdown hook.
	 * 
	 * @param hook
	 *            the hook (a Thread) to de-register
	 * @return true if the hook could be de-registered
	 */
	public boolean removeShutdownHook(Thread hook) {
        //fixit -- doing a "nop" is good enough to allow simple "hello world" to work
		return false;
	}

	/**
	 * Causes the virtual machine to stop running, and the program to exit.
	 * Finalizers will not be run first. Shutdown hooks will not be run.
	 * 
	 * @param code
	 *            the return code.
	 * @exception SecurityException
	 *                if the running thread is not allowed to cause the vm to
	 *                exit.
	 * @see SecurityManager#checkExit
	 */
	public void halt(int code) {
        VMRuntime.exit(code);
		return;
	}

	/**
	 * Return the number of processors, always at least one.
	 */
	public int availableProcessors() {
        //fixit --- when we need to run on multiprocessor machines
		return 1;
	}

	/**
	 * Return the maximum memory that will be used by the virtual machine, or
	 * Long.MAX_VALUE.
	 */
	public long maxMemory() {
        long ll = VMRuntime.maxMemory();
		return ll;
	}

}

