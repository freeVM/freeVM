/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tools.msgstool;

import java.io.File;


/**
 * This is the entry point for the msgtool tool.
 */
public final class Main {
    public static final String HELP_MSG = "-help";
    
    /**
     * Prints the usage information.
     */
    public static void printUsage() {
        System.out.println("Usage: " + MsgClassGenerator.class.getName()
                + " [options]");
        System.out.println();
        System.out.println("[options]");
        System.out.println();
        System.out.println("    -help                   Print help message");
        System.out.println();
        System.out.println("    -d <dst path>           Destination path where to copy");
        System.out.println("                            generated files that will be");
        System.out.println("                            created if necessary.");
        System.out.println("                            If this option is not specified");
        System.out.println("                            current user dir is used.");
        System.out.println();
        System.out.println("    -m <modules>            Property file, where ");
        System.out.println("                            the list of modules is specified.");
        System.out.println("                            If this option is not specified");
        System.out.println("                            default modules.properties file is used.");
        System.out.println();
    }

    public static void main(String[] args) {
        File propFile = null;
        File dstDir = null;
        
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-help")) {
                printUsage();
                return;
            } 
            
            if (args[i].equals("-d")) {
                i++;
                dstDir = new File(args[i]);
            } else if (args[i].equals("-m")) {
                i++;
                propFile = new File(args[i]);
            }
            i++;
        }


        /* Invoke the messages classes generator */
        MsgClassGenerator.run(dstDir, propFile);
    }

    /**
     * Default constructor.
     */
    public Main() {
        super();
    }

}
