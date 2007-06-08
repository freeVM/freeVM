/*
 * Copyright 2007 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Aleksey Ignatenko
 * @version $Revision: 1.0 $
 */

/**
 * NOTE: this class is not a test for running, it is a base class for other tests which
 *            process classes from "java.home"'s jar files.
 *
 * The class does:
 *    1. Reads parameter, which is:
 *            param[0] - number of threads to launch for parallel classes processing
 *            param[1] - flag indicates what OS is used, true == linux, it is required not to initialize loaded classes as it requires 
 *            XServer to be up.
 *
 *    2. Gets names of all classes from all jar files found in "java.home" and its subdirectories.
 *
 *    2. Starts param[0] threads. 
 *          Each thread:
 *              For each class name:
 *                 - tries to load the class by Class.forName() (classes are not initialized on linux because it requires XServer to be up)
 *                 - if class could not be loaded for any reason, it is ignored, otherwise, 
 *                 - testContent() method which must be overridden in real tests is invoked.
 *
 *    3. If there are no unexpected exceptions, PASS status is returned, otherwise, FAIL
 */

package org.apache.harmony.test.reliability.vm.classloading;

import java.util.ArrayList;
import java.lang.reflect.*;
import java.lang.annotation.*;

import org.apache.harmony.test.reliability.share.Test;
import org.apache.harmony.test.reliability.share.JarFilesScanner;


public class ClassMultiTestBase extends Test implements Runnable{
    volatile boolean failed = false;
    final static String classFilesExt = ".class";
    final static char slashCharDelimiter1 = '/';
    final static char slashCharDelimiter2 = '\\';
    final static char dotCharDelimiter = '.';
    final static int NUMBER_OF_THREADS = 3;
    int numberOfThreads = NUMBER_OF_THREADS;
    int classCounter = 0;
    ArrayList<String> jarFiles;
    
    void testContent(Class cls) {
        fail("The " + this.getClass().getName() + " class is for infra purposes only! - NOT TEST!");
    }
    
    public int test(String []args){
        parseParams(args);        
        jarFiles = new JarFilesScanner().getClassFilesInJRE();

        
        Thread[] thrds = new Thread[numberOfThreads];
        for (int i = 0; i< thrds.length; i++){
            thrds[i] = new Thread(this);
            thrds[i].start();
        }
        
        for (int i = 0; i< thrds.length; i++){
            try {
                thrds[i].join();
            } catch (InterruptedException e) {
                failed = true;
                log.add("Failed to join thread " + e);
            }
        }

        if (failed){
            return fail("FAILED");
        }
        //System.out.println("Number of classes tested "+ classCounter);
        return pass("OK");
    }
    
    
    public void run(){
        for (int i=0; i<jarFiles.size(); i++){
            Class jlClass = null;
            String classPureName = jarFiles.get(i).substring(0, jarFiles.get(i).length()-classFilesExt.length());
            classPureName = classPureName.replace(slashCharDelimiter1, dotCharDelimiter);
            classPureName = classPureName.replace(slashCharDelimiter2, dotCharDelimiter);
            try {
                // do not initialize loaded classes
                jlClass = Class.forName(classPureName, false, this.getClass().getClassLoader());
            } catch (Throwable e) {
                continue;
            }
            if (jlClass != null){
                classCounter++;
                try{
                    testContent(jlClass);
                } catch (Throwable e){
                    if (e.getClass().getName() == "java.lang.InternalError") continue; // enables 100% pass on RI
                    //log.add("Failed to test class: " + classPureName + " Issue:" + e);
                    //e.printStackTrace();
                    failed = true;
                }
            }
        }
    }

    public void parseParams(String[] params) {
        if (params.length >= 1) {
            numberOfThreads = Integer.parseInt(params[0]);
        }
    }

}

