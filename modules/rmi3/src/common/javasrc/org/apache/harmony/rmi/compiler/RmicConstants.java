/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
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
 * @author  Vasily Zakharov
 * @version $Revision: 1.1.2.1 $
 */
package org.apache.harmony.rmi.compiler;


/**
 * This interface contains various useful constants for RMI Compiler.
 *
 * @author  Vasily Zakharov
 * @version $Revision: 1.1.2.1 $
 */
interface RmicConstants {

    /**
     * System-dependent end-of-line string
     * {@link System#getProperty(String) System.getProperty("line.separator")}.
     */
    String EOLN = System.getProperty("line.separator");

    /**
     * Stub classes name suffix (<code>_Stub</code>).
     */
    String stubSuffix = "_Stub";

    /**
     * Skeleton classes name suffix (<code>_Skel</code>).
     */
    String skelSuffix = "_Skel";

    /**
     * Java source files name suffix (<code>.java</code>).
     */
    String javaSuffix = ".java";

    /**
     * Java class files name suffix (<code>.class</code>).
     */
    String classSuffix = ".class";

    /**
     * Method variables name prefix.
     */
    String methodVarPrefix = "$method_";

    /**
     * Parameters name prefix.
     */
    String paramPrefix = "$param_";

    /**
     * Array type prefix in parameter name.
     */
    String arrayPrefix = "arrayOf_";

    /**
     * Return result variable name.
     */
    String retVarName = "$result";

    /**
     * interfaceHash variable name.
     */
    String interfaceHashVarName = "interfaceHash";

    /**
     * useNewInvoke variable name.
     */
    String useNewInvoke = "useNewInvoke";

    /**
     * Input object stream name.
     */
    String inputStreamName = "in";

    /**
     * Output object stream name.
     */
    String outputStreamName = "out";

    /**
     * Version was not set, initial version value.
     *
     * @todo: all VERSION constants should be moved to enum for Java 5.0.
     */
    int VERSION_NOT_SET = 0;

    /**
     * Option to create RMI v1.1 stub.
     */
    int VERSION_V11 = 1;

    /**
     * Option to create RMI v1.2 stub.
     */
    int VERSION_V12 = 2;

    /**
     * Option to create RMI v1.1/v1.2 compatible stub.
     */
    int VERSION_VCOMPAT = 3;

    /**
     * Option to create IDL stub.
     */
    int VERSION_IDL = 4;

    /**
     * Option to create IIOP stub.
     */
    int VERSION_IIOP = 5;

    /**
     * Smallest possible option value.
     */
    int MIN_VERSION = VERSION_V11;

    /**
     * Largest possible option value.
     */
    int MAX_VERSION = VERSION_IIOP;
}
