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

package org.apache.harmony.tests.java.lang.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import tests.support.Support_Exec;

public class InstrumentTestHelper {
    private Manifest manifest;

    private List<Class> classes = new ArrayList<Class>();

    private String jarName;

    private Class mainClass;

    private String commandAgent;

    private String commandAgentOptions;

    private List<String> classpath = new ArrayList<String>();

    private String stdOut;

    private String stdErr;

    private int exitCode;

    private String mainJarName;

    private List<File> toBeCleaned = new ArrayList<File>();

    public static final String PREMAIN_CLASS = "Premain-Class";

    public static final String AGENT_CLASS = "Agent-Class";

    public static final String BOOT_CLASS_PATH = "Boot-Class-Path";

    public static final String CAN_REDEFINE_CLASSES = "Can-Redefine-Classes";

    public static final String CAN_RETRANSFORM_CLASSES = "Can-Retransform-Classes";

    public static final String CAN_SET_NATIVE_METHOD_PREFIX = "Can-Set-Native-Method-Prefix";

    public static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    /*
     * This variable is just for debugging. set to false, generated jar won't be
     * deleted after testing, so you can verify if the jar was generated as you
     * expected.
     */
    private boolean isDeleteJarOnExit = true;

    public void clean() {
        for (File file : toBeCleaned) {
            file.delete();
        }
    }

    public InstrumentTestHelper() {
        manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
    }

    public void addClasses(List<Class> added) {
        classes.addAll(added);
    }

    public void addManifestAttributes(String key, String value) {
        manifest.getMainAttributes().putValue(key, value);
    }

    public String getCommandAgent() {
        return commandAgent;
    }

    public void setCommandAgent(String commandAgent) {
        this.commandAgent = commandAgent;
    }

    public String getCommandAgentOptions() {
        return commandAgentOptions;
    }

    public void setCommandAgentOptions(String commandAgentOptions) {
        this.commandAgentOptions = commandAgentOptions;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        if (jarName.endsWith(".jar")) {
            this.jarName = jarName;
            mainJarName = jarName.substring(0, jarName.length() - 4)
                    + ".main.jar";
        } else {
            this.jarName = jarName + ".jar";
            mainJarName = jarName + ".main.jar";
        }
    }

    public Class getMainClass() {
        return mainClass;
    }

    public void setMainClass(Class mainClass) {
        this.mainClass = mainClass;
    }

    public void run() throws Exception {
        generateJars();
        runAgentTest();
    }

    private void runAgentTest() throws IOException, InterruptedException {
        List<String> execArgs = new ArrayList<String>(4);

        // add classpath string
        StringBuilder classPathString = new StringBuilder();
        String[] classpath = getClasspath();
        if (classpath != null && classpath.length > 0) {
            boolean isFirst = true;
            for (String element : classpath) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    classPathString.append(File.pathSeparator);
                }
                classPathString.append(element);
            }
        }

        if (classPathString.toString().length() != 0) {
            execArgs.add("-cp");
            execArgs.add(classPathString.toString());
        }

        execArgs.add("-javaagent:" + commandAgent
                     + ((commandAgentOptions != null
                         && commandAgentOptions.trim().length() != 0)
                        ? "=" + commandAgentOptions
                        : ""));
        execArgs.add(mainClass.getName());

        Object[] res = Support_Exec.runJava(execArgs, null, false);
        exitCode = ((Integer)res[0]).intValue();
        stdOut = (String)res[1];
        stdErr = (String)res[2];
    }

    private void generateJars() throws FileNotFoundException, IOException,
            URISyntaxException {

        File agentJar = generateJar(jarName, manifest, classes,
                isDeleteJarOnExit);
        toBeCleaned.add(agentJar);
        commandAgent = agentJar.getAbsolutePath();

        List<Class> list = new ArrayList<Class>();
        list.add(mainClass);

        File mainJarFile = generateJar(mainJarName, null, list,
                isDeleteJarOnExit);
        toBeCleaned.add(mainJarFile);

        classpath.add(mainJarFile.getAbsolutePath());
    }

    private static File calculateJarDir() throws MalformedURLException,
            URISyntaxException {
        URL location = ClassLoader.getSystemResource(InstrumentTestHelper.class
                .getName().replace(".", "/")
                + ".class");
        String locationStr = location.toString();

        // calculate jarDir
        File jarDir = null;
        if ("jar".equals(location.getProtocol())) {
            URL jar = null;
            int index = locationStr.indexOf(".jar!") + 4;
            if (locationStr.startsWith("jar:")) {
                jar = new URL(locationStr.substring(4, index));
            } else {
                jar = new URL(locationStr.substring(0, index));
            }
            jarDir = new File(jar.toURI()).getParentFile();
        } else {
            int index = locationStr.lastIndexOf(InstrumentTestHelper.class
                    .getName().replace(".", "/"));
            URL jar = new URL(locationStr.substring(0, index));
            jarDir = new File(jar.toURI());
        }
        return jarDir;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public String[] getClasspath() {
        return classpath.toArray(new String[0]);
    }

    public void setClasspath(String[] pathes) {
        classpath.addAll(Arrays.asList(pathes));
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isDeleteJarOnExit() {
        return isDeleteJarOnExit;
    }

    public void setDeleteJarOnExit(boolean isDeleteJarOnExit) {
        this.isDeleteJarOnExit = isDeleteJarOnExit;
    }

    public static File generateJar(String jarName, Manifest manifest,
            List<Class> classes, boolean isDeleteOnExit)
            throws FileNotFoundException, IOException, URISyntaxException {
        File jarDir = calculateJarDir();

        File jarFile = new File(jarDir, jarName);

        JarOutputStream out = null;
        if (manifest == null) {
            out = new JarOutputStream(new FileOutputStream(jarFile));
        } else {
            out = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        }

        for (Iterator<Class> iter = classes.iterator(); iter.hasNext();) {
            Class element = iter.next();
            String name = element.getName().replace(".", "/") + ".class";
            InputStream in = ClassLoader.getSystemResourceAsStream(name);
            byte[] bs = new byte[1024];
            int count = 0;
            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);
            while ((count = in.read(bs)) != -1) {
                out.write(bs, 0, count);
            }
            in.close();
        }
        out.close();

        if (isDeleteOnExit) {
            jarFile.deleteOnExit();
        }

        return jarFile;
    }
}
