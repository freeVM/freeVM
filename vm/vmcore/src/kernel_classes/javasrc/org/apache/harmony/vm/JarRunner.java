/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.harmony.vm;

import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import java.lang.reflect.Method;

import java.net.URLClassLoader;
import java.net.URL;

/**
 *  Small class to launch jars.  Used by VM for the 
 *   "java -jar foo.jar" use case.
 *   
 *  main() entry point takes array of arguments, with
 *  the jarname as the first arg
 *
 */
public class JarRunner { 

    /**
     * Expect jar filename as the first arg
     */
    public static void main(String[] args) throws Exception { 
        
        if (args.length == 0) {
            throw new Exception("No jar name specified");
        }
        
        String jarName = args[0];

        /*
         *  open the jar and get the manifest, and main class name
         */
        
        JarFile jarFile = new JarFile(jarName);
        Manifest manifest = jarFile.getManifest();
        
        if (manifest == null) {
            throw new Exception("No manifest in jar " + jarName);
        }
        
        Attributes attrbs = manifest.getMainAttributes();
        String className = attrbs.getValue(Attributes.Name.MAIN_CLASS);

        if (className == null || className.length() == 0) {
            throw new Exception ("Empty/Null Main-class specified in " + jarName);
        }
        
        /*
         *  now suck the jar in via a classloader, and try to invoke main
         *  on the mainclass
         */
        
        URL[] loaderArgs = {  new URL("jar:file:"+jarName + "!/") };

        MyLoader urlLoader = new MyLoader(loaderArgs);
        Class mainClass = urlLoader.findClass(className);        
        Method mainMethod = mainClass.getMethod("main", args.getClass());
        
        /*
         *  copy the args, skipping the first which is the jarname
         */
        String newArgs[] = new String[args.length - 1];
        
        for (int i=1; i < args.length; i++) {
            newArgs[i-1] = args[i];
        }
            
        mainMethod.invoke(null, (java.lang.Object) newArgs);
    }
    
    static class MyLoader extends URLClassLoader { 
        
        MyLoader(URL[] urls) {
            super(urls);
        }
        
        public Class findClass(String name) throws ClassNotFoundException { 
            return super.findClass(name);
        }
    }
}