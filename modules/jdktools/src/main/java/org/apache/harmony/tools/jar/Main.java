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

package org.apache.harmony.tools.jar;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This is a tool that allows you to create/unpack jar/zip 
 * archives 
 */
public class Main {

    /**
     * Prints the usage information.
     */
    public static void usage() {
        System.out.println("Usage:");
        System.out.println("  jar c[v0Mmf] [-C dir] [manifest_file] [jar_file] [input_files]");
        System.out.println("  jar u[v0Mmf] [-C dir] [manifest_file] [jar_file] [input_files]");
        System.out.println("  jar x[vf] [jar_file] [input_files]");
        System.out.println("  jar t[vf] [jar_file] [input_files]");
        System.out.println("  jar i jar_file");
        System.out.println("");
        System.out.println("Examples:");
        System.out.println("  To create a jar file called my.jar with manifest mymanifest.mf containing");
        System.out.println("  a.class and b.class, execute the following:");
        System.out.println("     jar cmf mymanifest.mf my.jar a.class b.class");
        System.out.println("  To extract a jar file called my.jar with verbose output, execute the");
        System.out.println("  following:");
        System.out.println("     jar xvf my.jar");
    }

    /**
     * A convenient way to run this tool from a command line.
     */
    public static void main(String args[]) throws Exception {
        if (args.length == 0) {
            // No options specified - just print usage string
            usage();
            return;
        }

        // Strip '-' at the start of options
        if (args[0].charAt(0) == '-') {
            args[0] = args[0].substring(1);
        }

        // Check for expected/unexpected options
        switch (args[0].charAt(0)) {
        case 'c':
            createJar(args);
            break;
        case 'u':
            updateJar(args);
            break;
        case 'x':
            extractJar(args);
            break;
        case 't':
            listJar(args);
            break;
        case 'i':
            indexJar(args);
            break;
        default:
            System.out.println("Error: Illegal option '"+args[0].charAt(0)+"'");
            usage();
            return;
        }
    }

    /**
     * Creates a jar file with the specified arguments
     */
    private static void createJar(String[] args) {
        System.out.println("Error: Jar creation not yet implemented");
    }

    /**
     * Updates a jar file with the specified arguments
     */
    private static void updateJar(String[] args) {
        System.out.println("Error: Jar update not yet implemented");
    }

    /**
     * Extracts a jar file with the specified arguments
     */
    private static void extractJar(String[] args) {
        System.out.println("Error: Jar extraction not yet implemented");
    }

    /**
     * Lists contents of jar file with the specified arguments
     */
    private static void listJar(String[] args) throws Exception {
        boolean verboseFlag = false, fileFlag = false;

        // Check for expected and unexpected flags
        for (int i=1; i<args[0].length(); i++) {
            switch (args[0].charAt(i)) {
            case 'v':
                verboseFlag = true;
                break;
            case 'f':
                fileFlag = true;
                break;
            default:
                System.out.println("Error: Illegal option for -t: '"+args[0].charAt(i)+"'");
                return;
            }
        }

        if (fileFlag && args.length<2) {
            // Options specify 'f' but there is no filename present
            System.out.println("Error: No file name specified for 'f' option");
            return;
        }

        ZipInputStream zis;
        if (!fileFlag) {
            // Read from stdin
            if (verboseFlag) System.out.println("Reading input from stdin");
            zis = new ZipInputStream(System.in);
        } else {
            // Read from the specified file
            if (verboseFlag) System.out.println("Reading jar file: "+args[1]);
            zis = new ZipInputStream(new FileInputStream(new File(args[1])));
        }

        // Read the zip entries - format and print their data
        ZipEntry ze;
        if (verboseFlag) System.out.println("Listing files:");        
        while ((ze = zis.getNextEntry()) != null) {
            Date date = new Date(ze.getTime());
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyy");
            String formattedDate = dateFormat.format(date);
            System.out.printf("%6d %s %s\n", ze.getSize(), formattedDate, ze.getName());
        }

        zis.close();
    }

    /**
     * Indexes a jar file with the specified arguments
     */
    private static void indexJar(String[] args) {
        System.out.println("Error: Jar indexing not yet implemented");
    }

}
