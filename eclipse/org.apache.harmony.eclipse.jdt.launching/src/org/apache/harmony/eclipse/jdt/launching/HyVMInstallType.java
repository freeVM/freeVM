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

package org.apache.harmony.eclipse.jdt.launching;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;

public class HyVMInstallType extends AbstractVMInstallType {

    static final String LAUNCHER_HOME_TOKEN = "%LAUNCHER_HOME%"; //$NON-NLS-1$
    static final String VMDIR_TOKEN = "%VM_DIR%"; //$NON-NLS-1$

    public IVMInstall doCreateVMInstall(String id) {
        return new HyVMInstall(this, id);
    }

    public String getName() {
        return HyLauncherMessages.getString("HyVMType.name"); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    public IStatus validateInstallLocation(File installLocation) {
        // Check we can find the launcher.
        File java = new File(installLocation, "bin" + File.separator + "java");
        File javaExe = new File(installLocation, "bin" + File.separator + "java.exe");
        if (!(java.isFile() || javaExe.isFile())) {
            if (HyLaunchingPlugin.isDebuggingInstalling()) {
                try {
                    System.out.println(HyLaunchingPlugin.getUniqueIdentifier()
                            + "--> No Harmony launcher detected at location : "
                            + installLocation.getCanonicalPath());
                } catch (IOException e) {
                    // Intentionally empty
                }
            }

            return new Status(IStatus.ERROR, HyLaunchingPlugin.getUniqueIdentifier(), 0,
                    HyLauncherMessages.getString("HyVMType.error.noLauncher"), null);
        }

        // Check we can find the bootclasspath.properties file.
        File bootPropsFile = new File(installLocation, "lib" + File.separator + "boot"
                + File.separator + "bootclasspath.properties");
        if (!bootPropsFile.isFile()) {
            return new Status(IStatus.ERROR, HyLaunchingPlugin.getUniqueIdentifier(), 0,
                    HyLauncherMessages.getString("HyVMType.error.noBootProperties"), null);
        }

        // Everything looks good.
        return new Status(IStatus.OK, HyLaunchingPlugin.getUniqueIdentifier(), 0, "ok",
                null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.launching.IVMInstallType#detectInstallLocation()
     */
    public File detectInstallLocation() {
        // Try to detect whether the current VM is a Harmony installation.
        File home = new File(System.getProperty("java.home")); //$NON-NLS-1$
        IStatus status = validateInstallLocation(home);
        if (status.isOK()) {
            return home;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
     */
    public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {

        // Find kernel types
        List<LibraryLocation> kernelLibraries = getKernelLibraries(installLocation,
                "default", "harmonyvm"); //$NON-NLS-1$ //$NON-NLS-2$
        if (kernelLibraries == null) {
            return new LibraryLocation[] {};
        }

        List<LibraryLocation> bootLibraries = getBootLibraries(installLocation);
        if (bootLibraries == null) {
            return new LibraryLocation[] {};
        }

        // Find the extension class libraries
        List<LibraryLocation> extensions = getExtensionLibraries(installLocation);

        // Combine the libraries result
        LibraryLocation[] allLibraries = new LibraryLocation[kernelLibraries.size()
                + bootLibraries.size() + extensions.size()];

        int libraryCount = 0;

        // Start with the kernel library locations
        for (int i = 0; i < kernelLibraries.size(); i++) {
            allLibraries[libraryCount++] = kernelLibraries.get(i);
        }

        // Append the boot libraries
        for (int i = 0; i < bootLibraries.size(); i++) {
            allLibraries[libraryCount++] = bootLibraries.get(i);
        }

        // Append the extensions libraries
        for (int i = 0; i < extensions.size(); i++) {
            allLibraries[libraryCount++] = extensions.get(i);
        }

        // We are done
        return allLibraries;
    }

    private List<LibraryLocation> getBootLibraries(File installLocation) {
        // The location of the bootclasspath libraries
        File bootDirectory = new File(installLocation, "lib" + File.separator + "boot"); //$NON-NLS-1$ //$NON-NLS-2$

        // Load the bootclasspath properties file to figure out the required
        // libraries
        Properties bootclasspathProperties = new Properties();
        try {
            FileInputStream propertiesStream = new FileInputStream(new File(
                    bootDirectory, "bootclasspath.properties")); //$NON-NLS-1$
            bootclasspathProperties.load(propertiesStream);
            propertiesStream.close();
        } catch (IOException exception) {
            // Cannot find bootclasspath.properties file or cannot read it.
            return null;
        }

        List<String> bootOrder = findBootOrder(bootclasspathProperties, "bootclasspath.");
        if (bootOrder == null) {
            return null;
        }

        List<LibraryLocation> bootLibraries = new ArrayList<LibraryLocation>(bootOrder
                .size());

        // Interpret the key values, in order, as library locations
        for (String bootOrderKey : bootOrder) {
            // Here '14' is the offset past "bootclasspath."
            String orderSuffix = bootOrderKey.substring(14);

            // The library location first...
            String bootLibraryLocation = bootclasspathProperties
                    .getProperty(bootOrderKey);
            File libraryFile = new File(bootDirectory, bootLibraryLocation);
            if (!libraryFile.exists()) {
                // Ignore library descriptions for files that don't exist
                continue;
            }
            IPath libraryPath;
            try {
                libraryPath = new Path(libraryFile.getCanonicalPath());
            } catch (IOException exception1) {
                // Ignore invalid path values.
                continue;
            }

            // The source location can be deduced from the boot library name
            String sourceLocationKey = "bootclasspath.source." + orderSuffix; //$NON-NLS-1$ 
            String sourceLocation = bootclasspathProperties
                    .getProperty(sourceLocationKey);
            IPath sourcePath;
            if (sourceLocation == null) {
                // source location was not specified
                sourcePath = new Path(""); //$NON-NLS-1$
            } else {
                File sourceFile = new File(bootDirectory, sourceLocation);
                if (!sourceFile.exists()) {
                    // If we cannot find the source jar, we default to missing
                    // token
                    sourcePath = new Path("");
                } else {
                    try {
                        sourcePath = new Path(sourceFile.getCanonicalPath());
                    } catch (IOException exception1) {
                        // If we cannot find the source, we default to missing
                        // token
                        sourcePath = new Path(""); //$NON-NLS-1$
                    }
                }
            }

            // The source package root is the offset in the jar where package
            // names begin
            String sourceRootKey = "bootclasspath.source.packageroot." + orderSuffix; //$NON-NLS-1$
            // Default root location is "/"
            String sourceRoot = bootclasspathProperties.getProperty(sourceRootKey, "/"); //$NON-NLS-1$
            IPath sourceRootPath = new Path(sourceRoot);

            // We have everything we need to build up a library location
            LibraryLocation libLocation = new LibraryLocation(libraryPath, sourcePath,
                    sourceRootPath);
            bootLibraries.add(libLocation);
        }
        return bootLibraries;
    }

    private List<String> findBootOrder(Properties bootclasspathProperties,
            final String propertyStartsWith) {

        // Only keep keys that are propertyStartsWith."<something>"
        Set<Object> allKeys = bootclasspathProperties.keySet();
        Set<String> bootKeys = new HashSet<String>(allKeys.size());
        for (Iterator<Object> iter = allKeys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if ((key.startsWith(propertyStartsWith) && (key.indexOf('.',
                    propertyStartsWith.length()) == -1))) { // Ensure there are
                // no more '.'s
                bootKeys.add(key);
            }
        }

        // Now order the keys by their numerical postfix.
        SortedSet<String> bootOrder = new TreeSet<String>(new Comparator() {
            public int compare(Object object1, Object object2) {
                // Use propertyStartsWith.length() to get the offset for the end
                // of e.g. "bootclasspath."
                String str1 = ((String) object1).substring(propertyStartsWith.length());
                String str2 = ((String) object2).substring(propertyStartsWith.length());

                // Puts entries to the end, in any order, if they do not
                // parse.
                int first, second;
                try {
                    first = Integer.parseInt(str1);
                } catch (NumberFormatException exception) {
                    first = Integer.MAX_VALUE;
                }
                try {
                    second = Integer.parseInt(str2);
                } catch (NumberFormatException exception1) {
                    second = Integer.MAX_VALUE;
                }
                if (first == second) {
                    return 0;
                }
                return (first < second) ? -1 : 1;
            }
        });
        bootOrder.addAll(bootKeys);
        return Arrays.asList(bootOrder.toArray(new String[bootOrder.size()]));
    }

    /**
     * Returns a list of default extension jars that should be placed on the
     * build path and runtime classpath, by default.
     * 
     * @param installLocation
     * @return List
     */
    protected List<LibraryLocation> getExtensionLibraries(File installLocation) {
        File extDir = getDefaultExtensionDirectory(installLocation);
        List<LibraryLocation> extensions = new ArrayList<LibraryLocation>();
        if (extDir != null && extDir.exists() && extDir.isDirectory()) {
            String[] names = extDir.list();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                File jar = new File(extDir, name);
                if (jar.isFile()) {
                    int length = name.length();
                    if (length > 4) {
                        String suffix = name.substring(length - 4);
                        if (suffix.equalsIgnoreCase(".zip") || suffix.equalsIgnoreCase(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
                            try {
                                IPath libPath = new Path(jar.getCanonicalPath());
                                LibraryLocation library = new LibraryLocation(libPath,
                                        Path.ROOT, Path.EMPTY);
                                extensions.add(library);
                            } catch (IOException e) {
                                // Ignored.
                            }
                        }
                    }
                }
            }
        }
        return extensions;
    }

    /**
     * Returns the default location of the extension directory, based on the
     * given install location. The resulting file may not exist, or be
     * <code>null</code> if an extension directory is not supported.
     * 
     * @param installLocation
     * @return default extension directory or <code>null</code>
     */
    protected File getDefaultExtensionDirectory(File installLocation) {
        File lib = new File(installLocation, "lib"); //$NON-NLS-1$
        File ext = new File(lib, "ext"); //$NON-NLS-1$
        return ext;
    }

    List<LibraryLocation> getKernelLibraries(File installLocation, String vmdir,
            String vmname) {
        Properties kernelProperties = new Properties();
        File kernelDirectory = new File(installLocation, "bin" + File.separator + vmdir);
        File propertyFile = new File(kernelDirectory, vmname + ".properties"); //$NON-NLS-1$

        try {
            FileInputStream propsFile = new FileInputStream(propertyFile);
            kernelProperties.load(propsFile);
            propsFile.close();
        } catch (IOException ex) {
            System.out
                    .println("Warning: could not open properties file " + propertyFile.getPath()); //$NON-NLS-1$
            return null;
        }

        // If we have a VME v1 style kernel (ie single kernel) then read its
        // location
        if (kernelProperties.getProperty("bootclasspath.kernel") != null) {
            List<LibraryLocation> kernelLibraries = new ArrayList<LibraryLocation>(1);

            String libStr = tokenReplace(installLocation, vmdir, kernelProperties
                    .getProperty("bootclasspath.kernel")); //$NON-NLS-1$
            IPath libraryPath = new Path(libStr);

            String srcStr = tokenReplace(installLocation, vmdir, kernelProperties
                    .getProperty("bootclasspath.source.kernel")); //$NON-NLS-1$
            IPath sourcePath;
            if (srcStr == null) {
                sourcePath = Path.EMPTY;
            } else {
                File sourceFile = new File(srcStr);
                if (!sourceFile.exists()) {
                    sourcePath = Path.EMPTY;
                } else {
                    sourcePath = new Path(srcStr);
                }
            }

            String rootStr = tokenReplace(installLocation, vmdir, kernelProperties
                    .getProperty("bootclasspath.source.packageroot.kernel")); //$NON-NLS-1$
            IPath sourceRootPath;
            if (rootStr == null) {
                sourceRootPath = Path.ROOT;
            } else {
                sourceRootPath = new Path(rootStr);
            }

            // We have everything we need to build up a library location
            LibraryLocation libLocation = new LibraryLocation(libraryPath, sourcePath,
                    sourceRootPath);
            kernelLibraries.add(libLocation);

            return kernelLibraries;
        } // endif VME v1

        // We have a VME v2 style split kernel (luni and security). Prepare to
        // read in multiple kernel locations
        List<String> bootOrder = findBootOrder(kernelProperties, "bootclasspath.kernel.");
        if (bootOrder == null) {
            return null;
        }

        List<LibraryLocation> kernelLibraries = new ArrayList<LibraryLocation>(bootOrder
                .size());

        // Interpret the key values, in order, as library locations
        for (String bootOrderKey : bootOrder) {
            // Here '21' is the offset past "bootclasspath.kernel."
            String orderSuffix = bootOrderKey.substring(21);

            String kernelLibraryLocation = tokenReplace(installLocation, vmdir,
                    kernelProperties.getProperty(bootOrderKey)); //$NON-NLS-1$
            File libraryFile = new File(kernelLibraryLocation);
            if (!libraryFile.exists()) {
                // Ignore library descriptions for files that don't exist
                continue;
            }
            IPath libraryPath = new Path(kernelLibraryLocation);

            String sourceLocation = tokenReplace(installLocation, vmdir, kernelProperties
                    .getProperty("bootclasspath.kernel.source." + orderSuffix)); //$NON-NLS-1$
            IPath sourcePath;
            if (sourceLocation == null) {
                sourcePath = Path.EMPTY;
            } else {
                File sourceFile = new File(sourceLocation);
                if (!sourceFile.exists()) {
                    sourcePath = Path.EMPTY;
                } else {
                    sourcePath = new Path(sourceLocation);
                }
            }

            String rootStr = tokenReplace(
                    installLocation,
                    vmdir,
                    kernelProperties
                            .getProperty("bootclasspath.kernel.source.packageroot." + orderSuffix)); //$NON-NLS-1$
            IPath sourceRootPath;
            if (rootStr == null) {
                sourceRootPath = Path.ROOT;
            } else {
                sourceRootPath = new Path(rootStr);
            }

            // We have everything we need to build up a library location
            LibraryLocation libLocation = new LibraryLocation(libraryPath, sourcePath,
                    sourceRootPath);
            kernelLibraries.add(libLocation);
        }
        return kernelLibraries;
    }

    private String tokenReplace(File installLocation, String vmdir, String str) {
        if (str == null) {
            return null;
        }
        String realHome = installLocation.getPath() + IPath.SEPARATOR + "bin"
                + IPath.SEPARATOR;
        return str.replace(LAUNCHER_HOME_TOKEN, realHome).replace(VMDIR_TOKEN, vmdir);
    }
}
