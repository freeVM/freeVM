/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.tools.javac;


/**
 * This is the entry point for the javac tool.
 */
public final class Main {

    public static void main(String[] args) {

        /* Give me something to do */
        if (args.length == 0) {
            new Compiler().printUsage();
            return;
        }

        /* Add in the base class library code to compile against */
        String[] newArgs = addBootclasspath(args);

        /* Invoke the compiler */
        Compiler.main(newArgs);
    }

    /**
     * Default constructor.
     */
    public Main() {
        super();
    }

    /*
     * Set up the compiler option to compile against the running JRE class
     * libraries.
     */
    public static String[] addBootclasspath(String[] args) {
        String[] result = new String[args.length + 2];
        System.arraycopy(args, 0, result, 0, args.length);
        result[args.length] = "-classpath";
        result[args.length + 1] = System.getProperty(
                "com.ibm.oti.system.class.path", ".");
        return result;
    }
}
