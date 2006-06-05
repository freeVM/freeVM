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
package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.apache.harmony.lang.RuntimePermissionCollection;
import org.apache.harmony.misc.EmptyEnum;
import org.apache.harmony.lang.ClassLoaderInfo;
import org.apache.harmony.vm.VMStack;

/**
 * @com.intel.drl.spec_ref 
 * 
 * @author Evgueni Brevnov
 * @version $Revision: 1.1.2.2.4.4 $
 */
public abstract class ClassLoader {

    /**
     * default protection domain. It is initialized in static block.
     */
    private static final ProtectionDomain defaultDomain;

    /**
     * package private to access from the java.lang.Class class.
     */
    static boolean enableAssertions = false;

    /**
     * system default class loader. It is initialized while
     * getSystemClassLoader(..) method is executing.
     */
    private static ClassLoader systemClassLoader = null;

    /**
     * empty set of sertificates
     */
    private static final Certificate[] EMPTY_CERTIFICATES = new Certificate[0];

    /**
     * this field has false as default value, it becomes true if system class
     * loader is initialized.
     */
    private static boolean initialized = false;

    /**
     * package private to access from the java.lang.Class class. The following
     * mapping is used <String name, Boolean flag>, where name - class name,
     * flag - true if assertion is enabled, false if desabled.
     */
    HashMap classAssertionStatus = null;

    /**
     * package private to access from the java.lang.Class class. The following
     * mapping is used <String name, Object[] signers>, where name - class name,
     * signers - array of signers.
     */
    Hashtable classSigners = null;

    /**
     * package private to access from the java.lang.Class class.
     */
    boolean defaultAssertionStatus = false;

    /**
     * package private to access from the java.lang.Class class. The following
     * mapping is used <String name, Boolean flag>, where name - package name,
     * flag - true if assertion is enabled, false if desabled.
     */
    HashMap packageAssertionStatus = new HashMap();

    /**
     * packages defined by this class loader are stored in this hash. The
     * following mapping is used <String name, Package pkg>, where name -
     * package name, pkg - corresponding package.
     */
    private final HashMap definedPackages = new HashMap();

    /**
     * package private to access from the java.lang.Class class. The following
     * mapping is used <String name, Certificate[] certificates>, where name -
     * the name of a package, certificates - array of certificates.
     */
    private final Hashtable packageCertificates = new Hashtable();

    /**
     * parent class loader
     */
    private final ClassLoader parentClassLoader;

    static {
        // Initializes default protection domain
        CodeSource cs = new CodeSource(null, (Certificate[])null);
        PermissionCollection perm = Policy.getPolicy().getPermissions(cs);
        defaultDomain = new ProtectionDomain(cs, perm);
        // Check whether we should enable assertions
        enableAssertions = VMExecutionEngine.getAssertionStatus(null) > 0
            ? true : false;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    protected ClassLoader() {
        //assert systemClassLoader != null
        // TODO XXX: use systemClassLoader field instead of
        // getSystemClassLoader() method.
        this(getSystemClassLoader());
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    protected ClassLoader(ClassLoader parent) {
        SecurityManager sc = System.getSecurityManager();
        if (sc != null) {
            sc.checkCreateClassLoader();
        }
        parentClassLoader = parent;
        // this field is used to determine whether class loader was initialized
        // properly.
        classAssertionStatus = new HashMap();
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static ClassLoader getSystemClassLoader() {
        if (!initialized) {
            // we assume only one thread will initialize system class loader. So
            // we don't synchronize initSystemClassLoader() method.
            initSystemClassLoader();
            // system class loader is initialized properly.
            initialized = true;
            // setContextClassLoader(...) method throws SecurityException if
            // current thread isn't allowed to set systemClassLoader as a
            // context class loader. Actually, it is abnormal situation if
            // thread can not change his own context class loader.
            Thread.currentThread().setContextClassLoader(systemClassLoader);
        }
        //assert initialized;
        SecurityManager sc = System.getSecurityManager();
        if (sc != null) {
            // we use VMClassRegistry.getClassLoader(...) method instead of
            // Class.getClassLoader() due to avoid redundant security
            // checking
            ClassLoader callerLoader = VMClassRegistry.getClassLoader(VMStack
                .getCallerClass(0));
            if (callerLoader != null && callerLoader != systemClassLoader) {
                sc.checkPermission(RuntimePermissionCollection.GET_CLASS_LOADER_PERMISSION);
            }
        }
        return systemClassLoader;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static URL getSystemResource(String name) {
        //assert systemClassLoader != null;
        // TODO XXX: use systemClassLoader field instead of
        // getSystemClassLoader() method.
        return getSystemClassLoader().getResource(name);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static InputStream getSystemResourceAsStream(String name) {
        //assert systemClassLoader != null;
        // TODO XXX: use systemClassLoader field instead of
        // getSystemClassLoader() method.
        return getSystemClassLoader().getResourceAsStream(name);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static Enumeration getSystemResources(String name)
        throws IOException {
        //assert systemClassLoader != null;
        // TODO XXX: use systemClassLoader field instead of
        // getSystemClassLoader() method.
       return getSystemClassLoader().getResources(name);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public void clearAssertionStatus() {
        synchronized (classAssertionStatus) {
            defaultAssertionStatus = false;
            packageAssertionStatus = new HashMap();
            classAssertionStatus = new HashMap();
        }
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public final ClassLoader getParent() {
        SecurityManager sc = System.getSecurityManager();
        if (sc != null) {
            ClassLoader callerLoader = VMClassRegistry.getClassLoader(VMStack
                .getCallerClass(0));
            if (callerLoader != null && !callerLoader.isSameOrAncestor(this)) {
                sc.checkPermission(RuntimePermissionCollection.GET_CLASS_LOADER_PERMISSION);
            }
        }
        return parentClassLoader;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public URL getResource(String name) {
        checkInitialized();
        URL foundResource = (parentClassLoader == null)
            ? BootstrapLoader.findResource(name)
            : parentClassLoader.getResource(name);
        return foundResource == null ? findResource(name) : foundResource;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public InputStream getResourceAsStream(String name) {
        try {
            URL foundResource = getResource(name);
            return foundResource.openStream();
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public final Enumeration getResources(String name) throws IOException {
        checkInitialized();
        ClassLoader cl = this;
        final ArrayList foundResources = new ArrayList();
        Enumeration resourcesEnum;
        do {
            resourcesEnum = cl.findResources(name);
            if (resourcesEnum != null && resourcesEnum.hasMoreElements()) {
                foundResources.add(resourcesEnum);
            }            
        } while ((cl = cl.parentClassLoader) != null);
        resourcesEnum = BootstrapLoader.findResources(name);
        if (resourcesEnum != null && resourcesEnum.hasMoreElements()) {
            foundResources.add(resourcesEnum);
        }
        return new Enumeration() {

                private int position = foundResources.size() - 1;

                public boolean hasMoreElements() {
                    while (position >= 0) {
                        if (((Enumeration)foundResources.get(position))
                            .hasMoreElements()) {
                            return true;
                        }
                        position--;
                    }
                    return false;
                }

                public Object nextElement() {
                    while (position >= 0) {
                        try {
                            return ((Enumeration)foundResources.get(position))
                            .nextElement();
                        } catch (NoSuchElementException e) {                            
                        }
                        position--;
                    }
                    throw new NoSuchElementException();
                }
            };
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void setClassAssertionStatus(String name, boolean flag) {
        if (name != null) {
            synchronized (classAssertionStatus) {
                enableAssertions = true;
                classAssertionStatus.put(name, Boolean.valueOf(flag));
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public void setDefaultAssertionStatus(boolean flag) {
        synchronized (classAssertionStatus) {
            enableAssertions = true;
            defaultAssertionStatus = flag;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     * Empty string is used to denote default package.
     */
    public void setPackageAssertionStatus(String name, boolean flag) {
        if (name == null) {
            name = "";
        }
        synchronized (classAssertionStatus) {
            enableAssertions = true;
            packageAssertionStatus.put(name, Boolean.valueOf(flag));
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final Class defineClass(byte[] data, int offset, int len)
        throws ClassFormatError {
        return defineClass(null, data, offset, len);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final Class defineClass(String name, byte[] data, int offset, int len)
        throws ClassFormatError {
        return defineClass(name, data, offset, len, null);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final synchronized Class defineClass(String name, byte[] data,
                                             int offset, int len,
                                             ProtectionDomain domain)
        throws ClassFormatError {
        checkInitialized();
        if (offset < 0 || len < 0 || offset + len > data.length) {
            throw new IndexOutOfBoundsException("Check your arguments");
        }
        if (domain == null) {
            domain = defaultDomain;
        }
        Certificate[] certs = null;
        String packageName = null;
        if (name != null) {
            if (name.startsWith("java.")) {
                throw new SecurityException(
                    "It is not allowed to define classes inside the java.* package");
            }
            int lastDot = name.lastIndexOf('.');
            packageName = lastDot == -1 ? "" : name.substring(0, lastDot);
            certs = getCertificates(packageName, domain.getCodeSource());
        }
        Class clazz = VMClassRegistry
            .defineClass(name, this, data, offset, len);
        clazz.setProtectionDomain(domain);
        if (certs != null) {
            packageCertificates.put(packageName, certs);
        }
        return clazz;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Package definePackage(String name, String specTitle,
                                    String specVersion, String specVendor,
                                    String implTitle, String implVersion,
                                    String implVendor, URL sealBase)
        throws IllegalArgumentException {
        if (getPackage(name) != null) {
            throw new IllegalArgumentException("Package " + name
                + "has been already defined.");
        }
        Package pkg = new Package(name, specTitle, specVersion, specVendor,
            implTitle, implVersion, implVendor, sealBase);
        synchronized (definedPackages) {
            definedPackages.put(name, pkg);
        }
        return pkg;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException("Can not find class " + name);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected String findLibrary(String name) {
        return null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final Class findLoadedClass(String name) {
        return VMClassRegistry.findLoadedClass(name, this);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected URL findResource(String name) {
        return null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Enumeration findResources(String name) throws IOException {
        return EmptyEnum.getInstance();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final Class findSystemClass(String name)
        throws ClassNotFoundException {
        // assert systemClassLoader != null;
        // TODO XXX: use systemClassLoader field instead of
        // getSystemClassLoader() method.
        return getSystemClassLoader().loadClass(name, false);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Package getPackage(String name) {
        checkInitialized();
        Package pkg = null;
        if (name == null) {
            throw new NullPointerException();
        }
        synchronized (definedPackages) {
            pkg = (Package)definedPackages.get(name);
        }
        if (pkg == null) {
            if (parentClassLoader == null) {
                pkg = BootstrapLoader.getPackage(name);
            } else {
                pkg = parentClassLoader.getPackage(name);
            }
        }
        return pkg;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Package[] getPackages() {
        checkInitialized();
        ArrayList packages = new ArrayList();
        fillPackages(packages);
        return (Package[])packages.toArray(new Package[packages.size()]);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        checkInitialized();
        Class clazz = findLoadedClass(name);
        if (clazz == null) {
            if (parentClassLoader == null) {
                clazz = VMClassRegistry.findLoadedClass(name, null);
            } else {
                try {
                    clazz = parentClassLoader.loadClass(name);
                } catch (ClassNotFoundException e) {
                }
            }
            if (clazz == null) {
                clazz = findClass(name);
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final void resolveClass(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        VMClassRegistry.linkClass(clazz);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected final void setSigners(Class clazz, Object[] signers) {
        checkInitialized();
        String name = clazz.getName();
        try {
            ClassLoader classLoader = VMClassRegistry.getClassLoader(clazz);
            if (classLoader.classSigners == null) {
                classLoader.classSigners = new Hashtable();
            }
            classLoader.classSigners.put(name, signers);
        } catch (NullPointerException e) {
        }
    }

    /*
     * NON API SECTION
     */

    final boolean isSameOrAncestor(ClassLoader loader) {
        while (loader != null) {
            if (this == loader) {
                return true;
            }
            loader = loader.parentClassLoader;
        }
        return false;
    }

    /**
     * This method should be called from each method that performs unsafe
     * actions.
     */
    private void checkInitialized() {
        if (classAssertionStatus == null) {
            throw new SecurityException(
                "Class loader was not initialized properly.");
        }
    }

    /**
     * Neither certs1 nor certs2 cann't be equal to null.
     */
    private boolean compareAsSet(Certificate[] certs1, Certificate[] certs2) {
        // TODO Is it possible to have multiple instances of same
        // certificate in array? This implementation assumes that it is
        // not possible.
        if (certs1.length != certs1.length) {
            return false;
        }
        if (certs1.length == 0) {
            return true;
        }
        boolean[] hasEqual = new boolean[certs1.length];
        for (int i = 0; i < certs1.length; i++) {
            boolean isMatch = false;
            for (int j = 0; j < certs2.length; j++) {
                if (!hasEqual[j] && certs1[i].equals(certs2[j])) {
                    hasEqual[j] = isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes the system class loader.
     */
    private static void initSystemClassLoader() {
        if (systemClassLoader != null) {
            throw new IllegalStateException(
                "Recursive invocation while initializing system class loader");
        }
        systemClassLoader = SystemClassLoader.getInstance();
        String className = System.getProperty("java.system.class.loader");
        if (className != null) {
            try {
                final Class userClassLoader = systemClassLoader
                    .loadClass(className);
                if (ClassLoader.class.isAssignableFrom(userClassLoader)) {
                    throw new Error(userClassLoader.toString()
                        + " must inherit java.lang.ClassLoader");
                }
                systemClassLoader = (ClassLoader)AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            Constructor c = userClassLoader
                                .getConstructor(new Class[] { ClassLoader.class });
                            return c
                                .newInstance(new Object[] { systemClassLoader });
                        }
                    });
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            } catch (PrivilegedActionException e) {
                throw new Error(e.getCause());
            }
        }
    }

    /**
     * Helper method for the getPackages() method.
     */
    private void fillPackages(ArrayList packages) {
        if (parentClassLoader == null) {
            packages.addAll(BootstrapLoader.getPackages());
        } else {
            parentClassLoader.fillPackages(packages);
        }
        synchronized (definedPackages) {
            packages.addAll(definedPackages.values());
        }
    }

    /**
     * Helper method for defineClass(...)
     * 
     * @return null if the package already has the same set of certificates, if
     *         first class in the package is being defined then array of
     *         certificates extracted from codeSource is returned.
     * @throws SecurityException if the package has different set of
     *         certificates than codeSource
     */
    private Certificate[] getCertificates(String packageName,
                                          CodeSource codeSource) {
        Certificate[] definedCerts = (Certificate[])packageCertificates
            .get(packageName);
        Certificate[] classCerts = codeSource != null
            ? codeSource.getCertificates() : EMPTY_CERTIFICATES;
        classCerts = classCerts != null ? classCerts : EMPTY_CERTIFICATES;
        // not first class in the package
        if (definedCerts != null) {
            if (!compareAsSet(definedCerts, classCerts)) {
                throw new SecurityException("It is prohobited to define a "
                    + "class which has different set of signers than "
                    + "other classes in this package");
            }
            return null;
        }
        return classCerts;
    }

    /**
     * Helper method to avoid StringTokenizer using.
     */
    private static String[] fracture(String str, String sep) {
        if (str.length() == 0) {
            return new String[0];
        }
        ArrayList res = new ArrayList();
        int in = 0;
        int curPos = 0;
        int i = str.indexOf(sep);
        int len = sep.length();
        while (i != -1) {
            String s = str.substring(curPos, i); 
            res.add(s);
            in++;
            curPos = i + len;
            i = str.indexOf(sep, curPos);
        }

        len = str.length();
        if (curPos <= len) {
            String s = str.substring(curPos, len); 
            in++;
            res.add(s);
        }

        return (String[]) res.toArray(new String[in]);
    }
   
    /* IBM SPECIFIC PART */
    
    static final ClassLoader getStackClassLoader(int depth) {
        Class clazz = VMStack.getCallerClass(depth);
        return clazz != null ? VMClassRegistry.getClassLoader(clazz) : null;
    }
    
    final boolean  isSystemClassLoader () {
        return ClassLoaderInfo.isSystemClassLoader(this); 
    }

    static final void loadLibraryWithClassLoader (String libName, ClassLoader loader) {
        SecurityManager sc = System.getSecurityManager();
        if (sc != null) {
                sc.checkLink(libName);
        }
        if (loader != null) {
                String fullLibName = loader.findLibrary(libName);
                if (fullLibName != null) {
                        loadLibrary(fullLibName, loader, null);
                        return;
                }
        }       
                String path = System.getProperty("java.library.path", "");
                path += System.getProperty("vm.boot.library.path", "");
        loadLibrary(libName, loader, path);
    }
   
    static final void loadLibrary (String libName, ClassLoader loader, String libraryPath) {
        SecurityManager sc = System.getSecurityManager();
        if (sc != null) {
                sc.checkLink(libName);
        }
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");
        String st[] = fracture(libraryPath, pathSeparator);
        int l = st.length;
        for (int i = 0; i < l; i++) {
            try {
                VMClassRegistry.loadLibrary(st[i] + fileSeparator + libName, loader);
                return;
            } catch (UnsatisfiedLinkError e) {
            }
        }
        throw new UnsatisfiedLinkError(libName);
    } 
   
    /* END OF IBM SPECIFIC PART */

    static final class BootstrapLoader {

        // TODO avoid security checking
        private static final String bootstrapPath = System
            .getProperty("vm.boot.class.path", "");

        private static URLClassLoader resourceFinder = null;

        private static final HashMap systemPackages = new HashMap();

        /**
         * This class contains static methods only. So it should not be
         * instantiated.
         */
        private BootstrapLoader() {
        }

        public static URL findResource(String name) {
            if (resourceFinder == null) {
                initResourceFinder();
            }
            return resourceFinder.findResource(name);
        }

        public static Enumeration findResources(String name) throws IOException {
            if (resourceFinder == null) {
                initResourceFinder();
            }
            return resourceFinder.findResources(name);
        }

        public static Package getPackage(String name) {
            synchronized (systemPackages) {
                updatePackages();
                return (Package)systemPackages.get(name.toString());
            }
        }

        public static Collection getPackages() {
            synchronized (systemPackages) {
                updatePackages();
                return systemPackages.values();
            }
        }

        private static void initResourceFinder() {
            synchronized (bootstrapPath) {
                if (resourceFinder != null) {
                    return;
                }                
                // -Xbootclasspath:"" should be interpreted as nothing defined,
                // like we do below:
                String st[] = fracture(bootstrapPath, File.pathSeparator);
                int l = st.length;
                ArrayList urlList = new ArrayList();
                for (int i = 0; i < l; i++) {
                    try {
                        urlList.add(new File(st[i]).toURI().toURL());
                    } catch (MalformedURLException e) {
                    }
                }
                URL[] urls = new URL[urlList.size()];
                resourceFinder = new URLClassLoader((URL[])urlList
                    .toArray(urls), null);
            }
        }

        private static void updatePackages() {
            String[][] packages = VMClassRegistry.getSystemPackages(systemPackages.size());
            if (null == packages) {
                return;
            }
            for (int i = 0; i < packages.length; i++) {
                
                String name = packages[i][0];
                if (systemPackages.containsKey(name)) {
                    continue;
                }             
                
                String jarURL = packages[i][1];             
                systemPackages.put(name, new Package(name, jarURL));
            }
        }
    }

    private static final class SystemClassLoader {

        private static URLClassLoader instance;

        static {
            ArrayList urlList = new ArrayList();
            // TODO avoid security checking?
            String extDirs = System.getProperty("java.ext.dirs", "");

            // -Djava.ext.dirs="" should be interpreted as nothing defined,
            // like we do below:
            String st[] = fracture(extDirs, File.pathSeparator);
            int l = st.length;
            for (int i = 0; i < l; i++) {
                try {
                    File dir = new File(st[i]).getAbsoluteFile();
                    File[] files = dir.listFiles();
                    for (int j = 0; j < files.length; j++) {
                        urlList.add(files[j].toURI().toURL());
                    }
                } catch (Exception e) {
                }
            }
            // TODO avoid security checking?
            String classPath = System.getProperty("java.class.path",
                    File.pathSeparator);
            st = fracture(classPath, File.pathSeparator);
            l = st.length;
            for (int i = 0; i < l; i++) {
                try {
                    if(st[i].length() == 0) {
                        st[i] = ".";
                    }
                    urlList.add(new File(st[i]).toURI().toURL());
                } catch (MalformedURLException e) {
                }
            }
            instance = URLClassLoader.newInstance((URL[])urlList
                .toArray(new URL[urlList.size()]), null);
        }

        /**
         * This class contains static methods only. So it should not be
         * instantiated.
         */
        private SystemClassLoader() {
        }

        public static ClassLoader getInstance() {
            return instance;
        }
    }
}
