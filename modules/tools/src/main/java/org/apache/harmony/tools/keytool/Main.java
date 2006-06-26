/*
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tools.keytool;

/**
 * The main class that bundles command line parsing, interaction with the user
 * and work with keys and certificates.
 *
 * Class that implements the functionality of the key and certificate management
 * tool.
 */
public class Main {

    /**
     * Does the actual work with keys and certificates, based on the parameter
     * param. If something goes wrong an exception is thrown.
     */
    static void doWork(KeytoolParameters param) throws Exception {
        switch (param.getCommand()) {
            case EXPORT:
                CertExporter.exportCert(param);
                break;
            case LIST:
                KeyStoreCertPrinter.list(param);
                break;
            case KEYCLONE:
                EntryManager.keyClone(param);
                break;
            case DELETE:
                EntryManager.delete(param);
                break;
            case STOREPASSWD:
                KeyStoreLoaderSaver.storePasswd(param);
                break;
            case KEYPASSWD:
                EntryManager.keyPasswd(param);
                break;
            // TODO: calls for other options.    
        }
    }

    /**
     * The main method to run from another program.
     * 
     * @param args -
     *            command line with options.
     */
    public static void run(String[] args) throws Exception {
        KeytoolParameters param = ArgumentsParser.parseArgs(args);

        if (param == null) {
            System.out.println("Help message is printed here");
            System.exit(-1);
        }

        Command command = param.getCommand();

        // all commands except printcert and help work with a store
        if (command != Command.PRINTCERT && command != Command.HELP) {
            // all commands that work with store except list and export 
            // need store password to with keystore. 
            if (param.getStorePass() == null && command != Command.LIST
                    && command != Command.EXPORT) {
                throw new KeytoolException(
                        "Must specify store password to work with this command.");
            }
            // load the keystore
            KeyStoreLoaderSaver.loadStore(param);
            // prompt for additional parameters if some of the expected
            // ones have not been specified.
            //ArgumentsParser.getAdditionalParameters(param);
        }

        // print the warning if store password is not set
        if (param.getStorePass() == null) {
            System.out
                    .println("\nWARNING!!!\nThe integrity of the keystore data "
                            + "has NOT been checked!\n"
                            + "To check it you must provide your keystore password!\n");
        }

        // the work is being done here
        doWork(param);

        if (param.isNeedSaveKS()) {
            // if the program should output additional information, do it
            if (param.isVerbose()) {
                System.out.println("[Saving " + param.getStorePath() + "]");
            }
            // save the store
            KeyStoreLoaderSaver.saveStore(param);
        }
    }

    /**
     * The main method to run from command line.
     * 
     * @param args -
     *            command line with options.
     */
    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            // System.out.println("Keytool error: " + e);
            e.printStackTrace();
        }
    }
}
