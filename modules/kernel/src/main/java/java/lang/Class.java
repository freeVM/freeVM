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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;


/**
 * This class must be implemented by the vm vendor. The documented natives must
 * be implemented to support other provided class implementations in this
 * package. An instance of class Class is the in-image representation of a Java
 * class. There are three basic types of Classes
 * <dl>
 * <dt><em>Classes representing object types (classes or interfaces)</em>
 * </dt>
 * <dd>These are Classes which represent the class of a simple instance as
 * found in the class hierarchy. The name of one of these Classes is simply the
 * fully qualified class name of the class or interface that it represents. Its
 * <em>signature</em> is the letter "L", followed by its name, followed by a
 * semi-colon (";").</dd>
 * <dt><em>Classes representing base types</em></dt>
 * <dd>These Classes represent the standard Java base types. Although it is not
 * possible to create new instances of these Classes, they are still useful for
 * providing reflection information, and as the component type of array classes.
 * There is one of these Classes for each base type, and their signatures are:
 * <ul>
 * <li><code>B</code> representing the <code>byte</code> base type</li>
 * <li><code>S</code> representing the <code>short</code> base type</li>
 * <li><code>I</code> representing the <code>int</code> base type</li>
 * <li><code>J</code> representing the <code>long</code> base type</li>
 * <li><code>F</code> representing the <code>float</code> base type</li>
 * <li><code>D</code> representing the <code>double</code> base type</li>
 * <li><code>C</code> representing the <code>char</code> base type</li>
 * <li><code>Z</code> representing the <code>boolean</code> base type</li>
 * <li><code>V</code> representing void function return values</li>
 * </ul>
 * The name of a Class representing a base type is the keyword which is used to
 * represent the type in Java source code (i.e. "int" for the <code>int</code>
 * base type.</dd>
 * <dt><em>Classes representing array classes</em></dt>
 * <dd>These are Classes which represent the classes of Java arrays. There is
 * one such Class for all array instances of a given arity (number of
 * dimensions) and leaf component type. In this case, the name of the class is
 * one or more left square brackets (one per dimension in the array) followed by
 * the signature ofP the class representing the leaf component type, which can
 * be either an object type or a base type. The signature of a Class
 * representing an array type is the same as its name.</dd>
 * </dl>
 * 
 */
public final class Class implements java.io.Serializable {
	private static final long serialVersionUID = 3206093459760846163L;

    Object vmdata;
    ProtectionDomain pd;
	/**
	 * Answers a Class object which represents the class named by the argument.
	 * The name should be the name of a class as described in the class
	 * definition of java.lang.Class, however Classes representing base types
	 * can not be found using this method.
	 * 
	 * @param className
	 *            The name of the non-base type class to find
	 * @return the named Class
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 * @see java.lang.Class
	 */
	public static Class forName(String className) throws ClassNotFoundException {
        boolean initializeBoolean = true; // a guess that somehow allows "hello world" to work on jchevm
        ClassLoader classloader = null;  // a guess that somehow allows "hello world" to work on jchevm
		return VMClass.forName(className, initializeBoolean, classloader);
	}

	/**
	 * Answers a Class object which represents the class named by the argument.
	 * The name should be the name of a class as described in the class
	 * definition of java.lang.Class, however Classes representing base types
	 * can not be found using this method. Security rules will be obeyed.
	 * 
	 * @param className
	 *            The name of the non-base type class to find
	 * @param initializeBoolean
	 *            A boolean indicating whether the class should be initialized
	 * @param classLoader
	 *            The classloader to use to load the class
	 * @return the named class.
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 * @see java.lang.Class
	 */
	public static Class forName(String className, boolean initializeBoolean,
			ClassLoader classLoader) throws ClassNotFoundException {
        initializeBoolean = true; // a guess that somehow allows "hello world" to work on jchevm
        ClassLoader classloader = null;  // a guess that somehow allows "hello world" to work on jchevm
		return VMClass.forName(className, initializeBoolean, classloader);
	}

	/**
	 * Answers an array containing all public class members of the class which
	 * the receiver represents and its superclasses and interfaces
	 * 
	 * @return the class' public class members
	 * @throws SecurityException
	 *             If member access is not allowed
	 * @see java.lang.Class
	 */
	public Class[] getClasses() {
        
        Class lastClass = this;
        int xx;
        for (xx = 0; ; xx++) 
        {
            lastClass = VMClass.getSuperclass(lastClass);
            if (lastClass == null) break;
        }
        Class [] retArray = new Class[xx + 1];
        lastClass = this;
        for (int yy = 0; yy < xx;) 
        {
            retArray[yy] = lastClass;
            lastClass = VMClass.getSuperclass(lastClass);
        }
		return retArray;
	}

	/**
	 * Verify the specified Class using the VM byte code verifier.
	 * 
	 * @throws VerifyError if the Class cannot be verified
	 */
	void verify() {
        //  fixit -- cheat for now, assuming only verifiable classes for "hello world" demos
		return;
	}

	/**
	 * Answers the classloader which was used to load the class represented by
	 * the receiver. Answer null if the class was loaded by the system class
	 * loader
	 * 
	 * @return the receiver's class loader or nil
	 * @see java.lang.ClassLoader
	 */
	public ClassLoader getClassLoader() {
        // fixit -- need to add security checks
        return VMClass.getClassLoader(this);
	}

	/**
	 * This must be provided by the vm vendor, as it is used by other provided
	 * class implementations in this package. Outside of this class, it is used
	 * by SecurityManager.checkMemberAccess(), classLoaderDepth(),
	 * currentClassLoader() and currentLoadedClass(). Return the ClassLoader for
	 * this Class without doing any security checks. The bootstrap ClassLoader
	 * is returned, unlike getClassLoader() which returns null in place of the
	 * bootstrap ClassLoader.
	 * 
	 * @return the ClassLoader
	 * @see ClassLoader#isSystemClassLoader()
	 */
	ClassLoader getClassLoaderImpl() {
		return VMClass.getClassLoader(this);
	};

	/**
	 * Answers a Class object which represents the receiver's component type if
	 * the receiver represents an array type. Otherwise answers nil. The
	 * component type of an array type is the type of the elements of the array.
	 * 
	 * @return the component type of the receiver.
	 * @see java.lang.Class
	 */
	public Class getComponentType() {
		return VMClass.getComponentType (this);
	};

	/**
	 * Answers a public Constructor object which represents the constructor
	 * described by the arguments.
	 * 
	 * @param parameterTypes
	 *            the types of the arguments.
	 * @return the constructor described by the arguments.
	 * @throws NoSuchMethodException
	 *             if the constructor could not be found.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getConstructors
	 */
	public Constructor getConstructor(Class parameterTypes[])
			throws NoSuchMethodException, SecurityException {
 
        boolean publicOnly = true;  //fixit -- find out what publicOnly should be set to
        Constructor[] conArray = VMClass.getDeclaredConstructors(this, publicOnly);
        for (int ii = 0; ii < conArray.length; ii++)
        {
            Class [] ca = conArray[ii].getParameterTypes();
            int jj;
            for (jj = 0; jj < ca.length; jj++) 
            {
                if (parameterTypes[jj] != ca[jj])
                    break;
            }
            if (jj == parameterTypes.length)  // its an exact match
                return conArray[jj];
        }
        NoSuchMethodException nsme = new NoSuchMethodException();
        throw nsme;
	}

	/**
	 * Answers an array containing Constructor objects describing all
	 * constructors which are visible from the current execution context.
	 * 
	 * @return all visible constructors starting from the receiver.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getMethods
	 */
	public Constructor[] getConstructors() throws SecurityException {
        boolean publicOnly = false;  // fixit -- find out what publicOnly should be set to
		return VMClass.getDeclaredConstructors(this, publicOnly);
	}

	/**
	 * Answers an array containing all class members of the class which the
	 * receiver represents. Note that some of the fields which are returned may
	 * not be visible in the current execution context.
	 * 
	 * @return the class' class members
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see java.lang.Class
	 */
	public Class[] getDeclaredClasses() throws SecurityException {
        boolean publicOnly = false;  // fixit -- find out what publicOnly should be set to
		return VMClass.getDeclaredClasses(this, publicOnly);
	}

	/**
	 * Answers a Constructor object which represents the constructor described
	 * by the arguments.
	 * 
	 * @param parameterTypes
	 *            the types of the arguments.
	 * @return the constructor described by the arguments.
	 * @throws NoSuchMethodException
	 *             if the constructor could not be found.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getConstructors
	 */
	public Constructor getDeclaredConstructor(Class parameterTypes[])
			throws NoSuchMethodException, SecurityException {
        boolean publicOnly = true;  //fixit -- find out what publicOnly should be set to
        Constructor[] conArray = VMClass.getDeclaredConstructors(this, publicOnly);

        for (int ii = 0; ii < conArray.length; ii++)
        {
            Class [] ca = conArray[ii].getParameterTypes();
            int jj;
            for (jj = 0; jj < ca.length; jj++) 
            {
                if (parameterTypes[jj] != ca[jj])
                    break;
            }
            if (jj == parameterTypes.length)  // its an exact match
                return conArray[jj];
        }
        NoSuchMethodException nsme = new NoSuchMethodException();
        throw nsme;
	}

	/**
	 * Answers an array containing Constructor objects describing all
	 * constructor which are defined by the receiver. Note that some of the
	 * fields which are returned may not be visible in the current execution
	 * context.
	 * 
	 * @return the receiver's constructors.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getMethods
	 */
	public Constructor[] getDeclaredConstructors() throws SecurityException {
        boolean publicOnly = false;  // fixit -- find out what publicOnly should be set to
        return VMClass.getDeclaredConstructors(this, publicOnly);
	}

	/**
	 * Answers a Field object describing the field in the receiver named by the
	 * argument. Note that the Constructor may not be visible from the current
	 * execution context.
	 * 
	 * @param name
	 *            The name of the field to look for.
	 * @return the field in the receiver named by the argument.
	 * @throws NoSuchFieldException
	 *             if the requested field could not be found
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getDeclaredFields
	 */
	public Field getDeclaredField(String name) throws NoSuchFieldException,
			SecurityException {
        boolean publicOnly = false;  //fixit -- find out what public only should be set to
        Field [] fields = VMClass.getDeclaredFields(this, publicOnly);
        for (int ii = 0; ii < fields.length; ii++) 
        {
            if (fields[ii].toString() == name) return fields[ii];
        }
		NoSuchFieldException nsfe = new NoSuchFieldException();
        throw nsfe;
	}

	/**
	 * Answers an array containing Field objects describing all fields which are
	 * defined by the receiver. Note that some of the fields which are returned
	 * may not be visible in the current execution context.
	 * 
	 * @return the receiver's fields.
	 * @throws SecurityException
	 *             If member access is not allowed
	 * @see #getFields
	 */
	public Field[] getDeclaredFields() throws SecurityException {
        boolean publicOnly = false;  // fixit -- find out what publicOnly should be set to
        return VMClass.getDeclaredFields(this, publicOnly);
	}

	/**
	 * Answers a Method object which represents the method described by the
	 * arguments. Note that the associated method may not be visible from the
	 * current execution context.
	 * 
	 * @param name
	 *            the name of the method
	 * @param parameterTypes
	 *            the types of the arguments.
	 * @return the method described by the arguments.
	 * @throws NoSuchMethodException
	 *             if the method could not be found.
	 * @throws SecurityException
	 *             If member access is not allowed
	 * @see #getMethods
	 */
	public Method getDeclaredMethod(String name, Class parameterTypes[])
			throws NoSuchMethodException, SecurityException {
        boolean publicOnly = false;  //fixit -- find out what publicOnly should be set to
        Method [] methods = VMClass.getDeclaredMethods(this, publicOnly);
        for (int ii = 0; ii < methods.length; ii++) 
        {
            //fixit ---- oops, need to match on parameterTypes also
            if (methods[ii].toString() == name) return methods[ii];
        }
        NoSuchMethodException nsme = new NoSuchMethodException();
        throw nsme;
	}

	/**
	 * Answers an array containing Method objects describing all methods which
	 * are defined by the receiver. Note that some of the methods which are
	 * returned may not be visible in the current execution context.
	 * 
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @return the receiver's methods.
	 * @see #getMethods
	 */
	public Method[] getDeclaredMethods() throws SecurityException {
        boolean publicOnly = false;  //fixit -- find out what publicOnly should be set to
        return VMClass.getDeclaredMethods(this, publicOnly);
	}

	/**
	 * Answers the class which declared the class represented by the receiver.
	 * This will return null if the receiver is a member of another class.
	 * 
	 * @return the declaring class of the receiver.
	 */
	public Class getDeclaringClass() {
        return VMClass.getDeclaringClass(this);
	}

	/**
	 * Answers a Field object describing the field in the receiver named by the
	 * argument which must be visible from the current execution context.
	 * 
	 * @param name
	 *            The name of the field to look for.
	 * @return the field in the receiver named by the argument.
	 * @throws NoSuchFieldException
	 *             If the given field does not exist
	 * @throws SecurityException
	 *             If access is denied
	 * @see #getDeclaredFields
	 */
	public Field getField(String name) throws NoSuchFieldException,
			SecurityException {
        boolean publicOnly = true;  //fixit -- find out what publicOnly should be set to
        Field [] fields = VMClass.getDeclaredFields(this, publicOnly);
        for (int ii = 0; ii < fields.length; ii++) 
        {
            if (fields[ii].toString() == name) return fields[ii];
        }
		NoSuchFieldException nsfe = new NoSuchFieldException();
        throw nsfe;
	}

	/**
	 * Answers an array containing Field objects describing all fields which are
	 * visible from the current execution context.
	 * 
	 * @return all visible fields starting from the receiver.
	 * @throws SecurityException
	 *             If member access is not allowed
	 * @see #getDeclaredFields
	 */
	public Field[] getFields() throws SecurityException {
        boolean publicOnly = true;  //fixit -- find out what publicOnly should be set to
        Field[] fld = VMClass.getDeclaredFields(this, publicOnly);
        return fld;
	}

	/**
	 * Answers an array of Class objects which match the interfaces specified in
	 * the receiver classes <code>implements</code> declaration
	 * 
	 * @return Class[] the interfaces the receiver claims to implement.
	 */
	public Class[] getInterfaces() {
		return VMClass.getInterfaces(this);
	};

	/**
	 * Answers a Method object which represents the method described by the
	 * arguments.
	 * 
	 * @param name
	 *            String the name of the method
	 * @param parameterTypes
	 *            Class[] the types of the arguments.
	 * @return Method the method described by the arguments.
	 * @throws NoSuchMethodException
	 *             if the method could not be found.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getMethods
	 */
	public Method getMethod(String name, Class parameterTypes[])
			throws NoSuchMethodException, SecurityException {
        boolean publicOnly = false;  //fixit -- find out what publicOnly should be set to
        Method [] methods = VMClass.getDeclaredMethods(this, publicOnly);
        for (int ii = 0; ii < methods.length; ii++) 
        {
            // fixit need to add code that matches on parameterTypes also
            if (methods[ii].toString() == name) 
            {
                Class [] paramArray = methods[ii].getParameterTypes();
                for (int jj = 0; jj < paramArray.length; jj++) 
                {
                   if (paramArray[jj] != parameterTypes[jj])
                       break;
                }
                return methods[ii];
            }
        }
        NoSuchMethodException nsme = new NoSuchMethodException();
        throw nsme;
	}

	/**
	 * Answers an array containing Method objects describing all methods which
	 * are visible from the current execution context.
	 * 
	 * @return Method[] all visible methods starting from the receiver.
	 * @throws SecurityException
	 *             if member access is not allowed
	 * @see #getDeclaredMethods
	 */
	public Method[] getMethods() throws SecurityException {
        boolean publicOnly = false;  //fixit -- find out what publicOnly should be set to
        return VMClass.getDeclaredMethods(this, publicOnly);
	}

	/**
	 * Answers an integer which which is the receiver's modifiers. Note that the
	 * constants which describe the bits which are returned are implemented in
	 * class java.lang.reflect.Modifier which may not be available on the
	 * target.
	 * 
	 * @return the receiver's modifiers
	 */
	public int getModifiers() {
        boolean ignoreInnerClassesAttrib = false;
		return VMClass.getModifiers(this, ignoreInnerClassesAttrib);
	};

	/**
	 * Answers the name of the class which the receiver represents. For a
	 * description of the format which is used, see the class definition of
	 * java.lang.Class.
	 * 
	 * @return the receiver's name.
	 * @see java.lang.Class
	 */
	public String getName() {
	    return VMClass.getName(this);
	};

	/**
	 * Answers the ProtectionDomain of the receiver.
	 * <p>
	 * Note: In order to conserve space in embedded targets, we allow this
	 * method to answer null for classes in the system protection domain (i.e.
	 * for system classes). System classes are always given full permissions
	 * (i.e. AllPermission). This is not changeable via the
	 * java.security.Policy.
	 * 
	 * @return ProtectionDomain the receiver's ProtectionDomain.
	 * @see java.lang.Class
	 */
	public ProtectionDomain getProtectionDomain() {
        //fixit -- need a java security expert to tell us what this code should be
		return null;
	}

	/**
	 * Answers the ProtectionDomain of the receiver.
	 * <p>
	 * This method is for internal use only.
	 * 
	 * @return ProtectionDomain the receiver's ProtectionDomain.
	 * @see java.lang.Class
	 */
	ProtectionDomain getPDImpl() {
        //fixit -- need a java security expert to tell us what this code should be
		return null;
	};

	/**
	 * Answers a read-only stream on the contents of the resource specified by
	 * resName. The mapping between the resource name and the stream is managed
	 * by the class' class loader.
	 * 
	 * @param resName
	 *            the name of the resource.
	 * @return a stream on the resource.
	 * @see java.lang.ClassLoader
	 */
	public URL getResource(String resName) {
        // fixit -- need to find an expert on getResource()
        // for "hello world", returning null works OK
		return null;
	}

	/**
	 * Answers a read-only stream on the contents of the resource specified by
	 * resName. The mapping between the resource name and the stream is managed
	 * by the class' class loader.
	 * 
	 * @param resName
	 *            the name of the resource.
	 * @return a stream on the resource.
	 * @see java.lang.ClassLoader
	 */
	public InputStream getResourceAsStream(String resName) {
        // fixit -- need to find an expert on getResource()
        // for "hello world", returning null works OK
		return null;
	}

	/**
	 * Answers the signers for the class represented by the receiver, or null if
	 * there are no signers.
	 * 
	 * @return the signers of the receiver.
	 * @see #getMethods
	 */
	public Object[] getSigners() {
        // fixit -- might be something related to Java security
		return null;
	}

	/**
	 * Answers the Class which represents the receiver's superclass. For Classes
	 * which represent base types, interfaces, and for java.lang.Object the
	 * method answers null.
	 * 
	 * @return the receiver's superclass.
	 */
	public Class getSuperclass() {
	    return VMClass.getSuperclass(this);
	};

	/**
	 * Answers true if the receiver represents an array class.
	 * 
	 * @return <code>true</code> if the receiver represents an array class
	 *         <code>false</code> if it does not represent an array class
	 */
	public boolean isArray() {
		return VMClass.isArray(this);
	};

	/**
	 * Answers true if the type represented by the argument can be converted via
	 * an identity conversion or a widening reference conversion (i.e. if either
	 * the receiver or the argument represent primitive types, only the identity
	 * conversion applies).
	 * 
	 * @return <code>true</code> the argument can be assigned into the
	 *         receiver <code>false</code> the argument cannot be assigned
	 *         into the receiver
	 * @param cls
	 *            Class the class to test
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public boolean isAssignableFrom(Class cls) {
		return VMClass.isAssignableFrom(this, cls);
	};

	/**
	 * Answers true if the argument is non-null and can be cast to the type of
	 * the receiver. This is the runtime version of the <code>instanceof</code>
	 * operator.
	 * 
	 * @return <code>true</code> the argument can be cast to the type of the
	 *         receiver <code>false</code> the argument is null or cannot be
	 *         cast to the type of the receiver
	 * @param object
	 *            Object the object to test
	 */
	public boolean isInstance(Object object) {
		return VMClass.isInstance(this, object);
	};

	/**
	 * Answers true if the receiver represents an interface.
	 * 
	 * @return <code>true</code> if the receiver represents an interface
	 *         <code>false</code> if it does not represent an interface
	 */
	public boolean isInterface() {
		return VMClass.isInterface(this);
	}

	/**
	 * Answers true if the receiver represents a base type.
	 * 
	 * @return <code>true</code> if the receiver represents a base type
	 *         <code>false</code> if it does not represent a base type
	 */
	public boolean isPrimitive() {
		return VMClass.isPrimitive(this);
	};

	/**
	 * Answers a new instance of the class represented by the receiver, created
	 * by invoking the default (i.e. zero-argument) constructor. If there is no
	 * such constructor, or if the creation fails (either because of a lack of
	 * available memory or because an exception is thrown by the constructor),
	 * an InstantiationException is thrown. If the default constructor exists,
	 * but is not accessible from the context where this message is sent, an
	 * IllegalAccessException is thrown.
	 * 
	 * @return a new instance of the class represented by the receiver.
	 * @throws IllegalAccessException
	 *             if the constructor is not visible to the sender.
	 * @throws InstantiationException
	 *             if the instance could not be created.
	 */
	public Object newInstance() throws IllegalAccessException,
			InstantiationException {
        // fixit -- returning null works OK for simple "hello world"
		return null;
	}

	/**
	 * Answers a string containing a concise, human-readable description of the
	 * receiver.
	 * 
	 * @return a printable representation for the receiver.
	 */
	public String toString() {
        //System.out.println("Class.toString() -- not implemented");
        return this.toString();  /// is this right???????
	}

	/**
	 * Returns the Package of which this class is a member. A class has a
	 * Package iff it was loaded from a SecureClassLoader
	 * 
	 * @return Package the Package of which this class is a member or null
	 */
	public Package getPackage() {
        // fixit -- returning null works OK for simple "hello world" app
		return null;
	}

	/**
	 * Returns the assertion status for this class. Assertion is
	 * enabled/disabled based on classloader default, package or class default
	 * at runtime
	 * 
	 * @return the assertion status for this class
	 */
	public boolean desiredAssertionStatus() {
        //fixit -- returning false works OK for simple "hello world" app
		return false;
	}

	/**
	 * This must be provided by the vm vendor, as it is used by other provided
	 * class implementations in this package. This method is used by
	 * SecurityManager.classDepth(), and getClassContext() which use the
	 * parameters (-1, false) and SecurityManager.classLoaderDepth(),
	 * currentClassLoader(), and currentLoadedClass() which use the parameters
	 * (-1, true). Walk the stack and answer an array containing the maxDepth
	 * most recent classes on the stack of the calling thread. Starting with the
	 * caller of the caller of getStackClasses(), return an array of not more
	 * than maxDepth Classes representing the classes of running methods on the
	 * stack (including native methods). Frames representing the VM
	 * implementation of java.lang.reflect are not included in the list. If
	 * stopAtPrivileged is true, the walk will terminate at any frame running
	 * one of the following methods: <code><ul>
	 * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedAction;)Ljava/lang/Object;</li>
	 * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;</li>
	 * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;</li>
	 * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;</li>
	 * </ul></code> If one of the doPrivileged methods is found, the walk terminate
	 * and that frame is NOT included in the returned array. Notes:
	 * <ul>
	 * <li>This method operates on the defining classes of methods on stack.
	 * NOT the classes of receivers.</li>
	 * <li>The item at index zero in the result array describes the caller of
	 * the caller of this method.</li>
	 * </ul>
	 * 
	 * @param maxDepth
	 *            maximum depth to walk the stack, -1 for the entire stack
	 * @param stopAtPrivileged
	 *            stop at priviledged classes
	 * @return the array of the most recent classes on the stack
	 */
	static final Class[] getStackClasses(int maxDepth, boolean stopAtPrivileged) {
        //fixit -- returning null works OK for simple "hello world" app
		return null;
	};

}

