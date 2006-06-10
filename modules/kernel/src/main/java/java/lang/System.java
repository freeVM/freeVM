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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import gnu.classpath.VMSystemProperties;


/**
 * Class System provides a standard place for programs to find system related
 * information. All System API is static.
 * 
 */
public final class System {

	// The standard input, output, and error streams.
	// Typically, these are connected to the shell which
	// ran the Java program.
	/**
	 * Default input stream
	 */
	public static final InputStream in;

	/**
	 * Default output stream
	 */
	public static final PrintStream out;

	/**
	 * Default error output stream
	 */
	public static final PrintStream err;

	// The System Properties table
	private static Properties systemProperties;

	// The System default SecurityManager
	private static SecurityManager security;

	// Initialize all the slots in System on first use.
	static {
		ensureProperties();
        System.loadLibrary("hyluni");
        System.loadLibrary("hyarchive");
        System.loadLibrary("hyauth");
        System.loadLibrary("hyprt");
        System.loadLibrary("hysig");
        System.loadLibrary("hytext");
        System.loadLibrary("hythr");
        System.loadLibrary("hyzlib");
		// Fill in the properties from the VM information.
		// Set up standard in, out, and err.
		err = new String.ConsolePrintStream(new FileOutputStream(FileDescriptor.err));
		out = new String.ConsolePrintStream(new BufferedOutputStream(
				new FileOutputStream(FileDescriptor.out)));
		in = new BufferedInputStream(new FileInputStream(FileDescriptor.in));
        systemProperties.setProperty("user.dir", getcwd());
        ClassLoader.setupSystemClassLoader();
	}

    // FIXME: should be somewhere in jchevm or classlib
    private static native String getcwd();

	/**
	 * Sets the value of the static slot "in" in the receiver to the passed in
	 * argument.
	 * 
	 * @param newIn
	 *            the new value for in.
	 */
	public static void setIn(InputStream newIn) {
        throw new RuntimeException("not implemented");
	}

	/**
	 * Sets the value of the static slot "out" in the receiver to the passed in
	 * argument.
	 * 
	 * @param newOut
	 *            the new value for out.
	 */
	public static void setOut(java.io.PrintStream newOut) {
            setPrintStream("out", newOut);
	}

	/**
	 * Sets the value of the static slot "err" in the receiver to the passed in
	 * argument.
	 * 
	 * @param newErr
	 *            the new value for err.
	 */
	public static void setErr(java.io.PrintStream newErr) {
            setPrintStream("err", newErr);
	}

        private static native void setPrintStream(String field,
                                                  java.io.PrintStream stream);

	/**
	 * Prevents this class from being instantiated.
	 */
	private System() {
	}

	/**
	 * Copies the contents of <code>array1</code> starting at offset
	 * <code>start1</code> into <code>array2</code> starting at offset
	 * <code>start2</code> for <code>length</code> elements.
	 * 
	 * @param array1
	 *            the array to copy out of
	 * @param start1
	 *            the starting index in array1
	 * @param array2
	 *            the array to copy into
	 * @param start2
	 *            the starting index in array2
	 * @param length
	 *            the number of elements in the array to copy
	 */
	public static void arraycopy(Object array1, int start1, Object array2,
			int start2, int length) {
        VMSystem.arraycopy(array1, start1, array2, start2, length);
	}

	/**
	 * Answers the current time expressed as milliseconds since the time
	 * 00:00:00 UTC on January 1, 1970.
	 * 
	 * @return the time in milliseconds.
	 */
    public static native long currentTimeMillis();

	private static final int InitLocale = 0;

	private static final int PlatformEncoding = 1;

	private static final int FileEncoding = 2;

	private static final int OSEncoding = 3;

	/**
	 * If systemProperties is unset, then create a new one based on the values
	 * provided by the virtual machine.
	 */
	private static void ensureProperties() {
		systemProperties = VMSystemProperties.getSystemProperties();
		String platformEncoding = null;
		String fileEncoding, osEncoding = null;
		String definedFileEncoding = getEncoding(FileEncoding);
		String definedOSEncoding = getEncoding(OSEncoding);
		if (definedFileEncoding != null) {
			fileEncoding = definedFileEncoding;
			// if file.encoding is defined, and os.encoding is not, use the
			// detected
			// platform encoding for os.encoding
			if (definedOSEncoding == null) {
				platformEncoding = getEncoding(PlatformEncoding);
				osEncoding = platformEncoding;
			} else {
				getEncoding(InitLocale);
			}
		} else {
			platformEncoding = getEncoding(PlatformEncoding);
			fileEncoding = platformEncoding;
		}
		// if os.encoding is not defined, file.encoding will be used
		if (osEncoding == null)
			osEncoding = definedOSEncoding;
		if (osEncoding != null)
			systemProperties.put("os.encoding", osEncoding);

		systemProperties.put("file.encoding", fileEncoding);


		String[] list = getPropertyList();
		for (int i = 0; i < list.length; i += 2) {
			String key = list[i];
			if (key == null)
				break;
			systemProperties.put(key, list[i + 1]);
		}

		String consoleEncoding = (String) systemProperties
				.get("console.encoding");
		if (consoleEncoding == null) {
			if (platformEncoding == null)
				platformEncoding = getEncoding(PlatformEncoding);
			consoleEncoding = platformEncoding;
			systemProperties.put("console.encoding", consoleEncoding);
        }

		systemProperties.put("com.ibm.oti.configuration", "clear");
		systemProperties.put("com.ibm.oti.configuration.dir", "jclClear");
		systemProperties.put("com.ibm.oti.jcl.build", "plugin");
	}

	/**
	 * Causes the virtual machine to stop running, and the program to exit. If
	 * runFinalizersOnExit(true) has been invoked, then all finalizers will be
	 * run first.
	 * 
	 * @param code
	 *            the return code.
	 * 
	 * @throws SecurityException
	 *             if the running thread is not allowed to cause the vm to exit.
	 * 
	 * @see SecurityManager#checkExit
	 */
	public static void exit(int code) {
		VMRuntime.exit(code);
	}

	/**
	 * Indicate to the virtual machine that it would be a good time to collect
	 * available memory. Note that, this is a hint only.
	 */
	public static void gc() {
		VMRuntime.gc();
	}

	/**
	 * Returns an environment variable.
	 * 
	 * @param var
	 *            the name of the environment variable
	 * @return the value of the specified environment variable
	 * @deprecated Use System.getProperty()
	 */
	public static String getenv(String var) {
        //System.out.println("System.getenv() --- security has been turned off");
		if (var == null) {
			throw new NullPointerException();
		}
        //fixit -- get security out of the way while bringing up simple "hello world" demo
        /*
		SecurityManager secMgr = System.getSecurityManager();
		if (secMgr != null) {
			secMgr.checkPermission(new RuntimePermission("getenv." + var));
		}
		throw new Error();
        */
        return null;
	}

	/**
	 * Answers the system properties. Note that this is not a copy, so that
	 * changes made to the returned Properties object will be reflected in
	 * subsequent calls to getProperty and getProperties.
	 * <p>
	 * Security managers should restrict access to this API if possible.
	 * 
	 * @return the system properties
	 */
	public static Properties getProperties() {
        //fixit -- get security out of the way while bringing up simple "hello world" demo
        /*
		SecurityManager secMgr = System.getSecurityManager();
		if (secMgr != null) {
			secMgr.checkPropertiesAccess();
		}
        */
		return systemProperties;
	}

	/**
	 * Answers the system properties without any security checks. This is used
	 * for access from within java.lang.
	 * 
	 * @return the system properties
	 */
	static Properties internalGetProperties() {
		return systemProperties;
	}

	/**
	 * Answers the value of a particular system property. Answers null if no
	 * such property exists,
	 * <p>
	 * The properties currently provided by the virtual machine are:
	 * 
	 * <pre>
	 *       java.vendor.url
	 *       java.class.path
	 *       user.home
	 *       java.class.version
	 *       os.version
	 *       java.vendor
	 *       user.dir
	 *       user.timezone
	 *       path.separator
	 *       os.name
	 *       os.arch
	 *       line.separator
	 *       file.separator
	 *       user.name
	 *       java.version
	 *       java.home
	 * </pre>
	 * 
	 * @param prop
	 *            the system property to look up
	 * @return the value of the specified system property, or null if the
	 *         property doesn't exist
	 */
	public static String getProperty(String prop) {
		return getProperty(prop, null);
	}

	/**
	 * Answers the value of a particular system property. If no such property is
	 * found, answers the defaultValue.
	 * 
	 * @param prop
	 *            the system property to look up
	 * @param defaultValue
	 *            return value if system property is not found
	 * @return the value of the specified system property, or defaultValue if
	 *         the property doesn't exist
	 */
	public static String getProperty(String prop, String defaultValue) {
		if (prop.length() == 0)
			throw new IllegalArgumentException();
        //fixit -- get security out of the way while bringing up simple "hello world" demo
        /*
		SecurityManager secMgr = System.getSecurityManager();
		if (secMgr != null) {
			secMgr.checkPropertyAccess(prop);
		}
        */
		return systemProperties.getProperty(prop, defaultValue);
	}

	/**
	 * Sets the value of a particular system property.
	 * 
	 * @param prop
	 *            the system property to change
	 * @param value
	 *            the value to associate with prop
	 * @return the old value of the property, or null
	 */
	public static String setProperty(String prop, String value) {
		if (prop.length() == 0)
			throw new IllegalArgumentException();
        //fixit -- get security out of the way while bringing up simple "hello world" demo
        /*
		SecurityManager secMgr = System.getSecurityManager();
		if (secMgr != null) {
			secMgr.checkPermission(new PropertyPermission(prop, "write"));
		}
        */
		return (String) systemProperties.setProperty(prop, value);
	}

	/**
	 * Answers an array of Strings containing key..value pairs (in consecutive
	 * array elements) which represent the starting values for the system
	 * properties as provided by the virtual machine.
	 * 
	 * @return the default values for the system properties.
	 */
	//fixit  below is a non-native version for getting "hello world" to work -----  private static native String[] getPropertyList()
    private static String[] getPropertyList() 
    {
        //fixit --- apparently the below code never executes for simple "hello world" demo
        // when getPropertyList is actually called, the hope is that it will cause an out of
        // java heap memory exception which in turn bring us to implementing the correct native method
        String[] str = new String[99999];
        return str;
    }

	/**
	 * Return the requested encoding. 0 - initialize locale 1 - detected
	 * platform encoding 2 - command line defined file.encoding 3 - command line
	 * defined os.encoding
	 */
	//fixit  below is a non-native version for getting "hello world" to work ---- private static native String getEncoding(int type);
    private static String getEncoding(int type) 
    {
        // FIXME: not correct, need to get from system
        return "ISO-8859-1";
        /*
        String str = "java.lang.System.getEncoding() type = ???";
        if (type == 0)
            str = null; ////"java.lang.System.getEncoding() type = 0";
        if (type == 1)
            str = null; ///"java.lang.System.getEncoding() type = 1";
        if (type == 2)
            str = null; ///"java.lang.System.getEncoding() type = 2";
        if (type == 3)
            str = "Cp1252";
        return str;
        */
    }

	/**
	 * Answers the active security manager.
	 * 
	 * @return the system security manager object.
	 */
	public static SecurityManager getSecurityManager() {
		return security;
	}

	/**
	 * Answers an integer hash code for the parameter. The hash code returned is
	 * the same one that would be returned by java.lang.Object.hashCode(),
	 * whether or not the object's class has overridden hashCode(). The hash
	 * code for null is 0.
	 * 
	 * @param anObject
	 *            the object
	 * @return the hash code for the object
	 * 
	 * @see java.lang.Object#hashCode
	 */
	// not the correct interface --->  public static native int identityHashCode(Object anObject);
    public static int identityHashCode(Object anObject)
    {
        int hash = VMSystem.identityHashCode(anObject);
        return hash;
    }


	/**
	 * Loads the specified file as a dynamic library.
	 * 
	 * @param pathName
	 *            the path of the file to be loaded
	 */
	public static void load(String pathName) {
        // FIXME: all the security checks
        ClassLoader classLoader = ClassLoader.callerClassLoader();
        Runtime.getRuntime().loadInternal(pathName, classLoader);
	}

	/**
	 * Loads and links the library specified by the argument.
	 * 
	 * @param libName
	 *            the name of the library to load
	 * 
	 * @throws UnsatisfiedLinkError
	 *             if the library could not be loaded
	 * @throws SecurityException
	 *             if the library was not allowed to be loaded
	 */
	public static void loadLibrary(String libName) {
        // FIXME: all the security checks
        ClassLoader classLoader = ClassLoader.callerClassLoader();
        Runtime.getRuntime().loadLibraryInternal(libName, classLoader);
	}

	/**
	 * Provides a hint to the virtual machine that it would be useful to attempt
	 * to perform any outstanding object finalizations.
	 */
	public static void runFinalization() {
		VMRuntime.runFinalization();
	}

	/**
	 * Ensure that, when the virtual machine is about to exit, all objects are
	 * finalized. Note that all finalization which occurs when the system is
	 * exiting is performed after all running threads have been terminated.
	 * 
	 * @param flag
	 *            true means finalize all on exit.
	 * 
	 * @deprecated This method is unsafe.
	 */
	public static void runFinalizersOnExit(boolean flag) {
		VMRuntime.runFinalizersOnExit(flag);
	}

	/**
	 * Answers the system properties. Note that the object which is passed in
	 * not copied, so that subsequent changes made to the object will be
	 * reflected in calls to getProperty and getProperties.
	 * <p>
	 * Security managers should restrict access to this API if possible.
	 * 
	 * @param p
	 *            the property to set
	 */
	public static void setProperties(Properties p) {
		SecurityManager secMgr = System.getSecurityManager();
		if (secMgr != null) {
			secMgr.checkPropertiesAccess();
		}
		if (p == null) {
			ensureProperties();
		} else {
			systemProperties = p;
		}
	}

	/**
	 * Sets the active security manager. Note that once the security manager has
	 * been set, it can not be changed. Attempts to do so will cause a security
	 * exception.
	 * 
	 * @param s
	 *            the new security manager
	 * 
	 * @throws SecurityException
	 *             if the security manager has already been set.
	 */
	public static void setSecurityManager(final SecurityManager s) {
		//final SecurityManager currentSecurity = security;
		try {
			// Preload classes used for checkPackageAccess(),
			// otherwise we could go recursive
			s.checkPackageAccess("java.lang");
		} catch (Exception e) {
		}

		security = s;
	}

	/**
	 * Answers the platform specific file name format for the shared library
	 * named by the argument.
	 * 
	 * @param userLibName
	 *            the name of the library to look up.
	 * @return the platform specific filename for the library
	 */
	//fixit -- the original stub declares this routine a native method.  Is this required?
    // public static native String mapLibraryName(String userLibName);
    public static String mapLibraryName(String libName) {
        return VMRuntime.mapLibraryName(libName);
    }

	/**
	 * Sets the value of the named static field in the receiver to the passed in
	 * argument.
	 * 
	 * @param fieldName
	 *            the name of the field to set, one of in, out, or err
	 * @param stream
	 *            the new value of the field
	 */
	//fixit -- the original stub declares this routine a native method.  Is this required?
	//private static native void setFieldImpl(String fieldName, Object stream);
    /*private static void setFieldImpl(String fieldName, Object stream) 
    {
        return;
    }*/

}

