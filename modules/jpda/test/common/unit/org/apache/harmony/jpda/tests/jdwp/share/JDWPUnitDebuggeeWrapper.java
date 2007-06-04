/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Vitaly A. Provodin
 * @version $Revision: 1.4 $
 */

/**
 * Created on 29.01.2005
 */
package org.apache.harmony.jpda.tests.jdwp.share;

import java.io.IOException;

import org.apache.harmony.jpda.tests.framework.LogWriter;
import org.apache.harmony.jpda.tests.framework.StreamRedirector;
import org.apache.harmony.jpda.tests.framework.TestErrorException;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPDebuggeeWrapper;
import org.apache.harmony.jpda.tests.framework.jdwp.TransportWrapper;
import org.apache.harmony.jpda.tests.share.JPDATestOptions;

/**
 * This class provides DebuggeeWrapper implementation based on JUnit framework.
 * Debuggee is always launched on local machine and attaches to debugger.
 */
public class JDWPUnitDebuggeeWrapper extends JDWPDebuggeeWrapper {

    /**
     * Target VM debuggee process.
     */
    public Process process;

    /**
     * Auxiliary options passed to the target VM on its launch.
     */
    public String savedVMOptions = null;

    StreamRedirector errRedir;

    StreamRedirector outRedir;

    TransportWrapper transport;

    /**
     * Creates new instance with given data.
     * 
     * @param settings
     *            test run options
     * @param logWriter
     *            where to print log messages
     */
    public JDWPUnitDebuggeeWrapper(JPDATestOptions settings, LogWriter logWriter) {
        super(settings, logWriter);
    }

    /**
     * Launches new debuggee process according to test run options and
     * establishes JDWP connection.
     */
    public void start() {
        boolean isListenConnection = settings.isListenConnectorKind();
        transport = createTransportWrapper();
        String address = settings.getTransportAddress();

        if (isListenConnection) {
            try {
                address = transport.startListening(address);
            } catch (IOException e) {
                throw new TestErrorException(e);
            }
            logWriter.println("Listening on: " + address);
        } else {
            logWriter.println("Attach to: " + address);
        }

        String cmdLine = settings.getDebuggeeJavaPath() + " -cp "
                + settings.getDebuggeeClassPath() + " -agentlib:"
                + settings.getDebuggeeAgentName() + "="
                + settings.getDebuggeeAgentOptions(address, isListenConnection)
                + " " + settings.getDebuggeeVMExtraOptions() + " "
                + (savedVMOptions != null ? savedVMOptions : "") + " "
                + settings.getDebuggeeClassName();

        logWriter.println("Launch: " + cmdLine);

        try {
            process = launchProcess(cmdLine);
            if (process != null) {
                logWriter.println("Start redirectors");
                errRedir = new StreamRedirector(process.getErrorStream(),
                        logWriter, "STDERR");
                errRedir.start();
                outRedir = new StreamRedirector(process.getInputStream(),
                        logWriter, "STDOUT");
                outRedir.start();
            }
            openConnection();

            logWriter.println("Established connection");

        } catch (Exception e) {
            throw new TestErrorException(e);
        }
    }

    /**
     * Closes all connections, stops redirectors, and waits for debuggee process
     * exit for default timeout.
     */
    public void stop() {
        disposeConnection();

        if (process != null) {
            logWriter.println("Waiting for debuggee exit");
            try {
                WaitForProcessExit(process);
            } catch (IOException e) {
                logWriter.println("IOException in stopping process: " + e);
            }

            logWriter.println("Waiting for redirectors");
            if (outRedir != null) {
                outRedir.exit();
                try {
                    outRedir.join(settings.getTimeout());
                } catch (InterruptedException e) {
                    logWriter
                            .println("InterruptedException in stopping outRedirector: "
                                    + e);
                }
            }
            if (errRedir != null) {
                errRedir.exit();
                try {
                    errRedir.join(settings.getTimeout());
                } catch (InterruptedException e) {
                    logWriter
                            .println("InterruptedException in stopping errRedirector: "
                                    + e);
                }
            }
        }

        closeConnection();
        if (settings.isListenConnectorKind()) {
            try {
                transport.stopListening();
            } catch (IOException e) {
                logWriter
                        .println("IOException in stopping transport listening: "
                                + e);
            }
        }
    }

    /**
     * Launches process with given command line.
     * 
     * @param cmdLine
     *            command line
     * @return associated Process object or null if not available
     * @throws IOException
     *             if error occurred in launching process
     */
    protected Process launchProcess(String cmdLine) throws IOException {
        process = Runtime.getRuntime().exec(cmdLine);
        return process;
    }

    /**
     * Waits for launched process to exit.
     * 
     * @param process
     *            associated Process object or null if not available
     * @throws IOException
     *             if any exception occurs in waiting
     */
    protected void WaitForProcessExit(Process process) throws IOException {
        ProcessWaiter thrd = new ProcessWaiter();
        thrd.start();
        try {
            thrd.join(settings.getTimeout());
        } catch (InterruptedException e) {
            throw new TestErrorException(e);
        }

        if (thrd.isAlive()) {
            thrd.interrupt();
        }

        try {
            int exitCode = process.exitValue();
            logWriter.println("Finished debuggee with exit code: " + exitCode);
        } catch (IllegalThreadStateException e) {
            logWriter.printError("Enforced debuggee termination");
            process.destroy();
            throw new TestErrorException("Debuggee process did not finish during timeout", e);
        }

        // dispose any resources of the process
        process.destroy();
    }

    /**
     * Opens connection with debuggee.
     */
    protected void openConnection() {
        try {
            if (settings.isListenConnectorKind()) {
                logWriter.println("Accepting JDWP connection");
                transport.accept(settings.getTimeout(), settings.getTimeout());
            } else {
                String address = settings.getTransportAddress();
                logWriter.println("Attaching for JDWP connection");
                transport.attach(address, settings.getTimeout(), settings
                        .getTimeout());
            }
            setConnection(transport);
        } catch (IOException e) {
            logWriter.printError(e);
            throw new TestErrorException(e);
        }
    }

    /**
     * Disposes JDWP connection stored in VmMirror.
     */
    protected void disposeConnection() {
        if (vmMirror != null) {
            try {
                vmMirror.dispose();
            } catch (Exception e) {
                logWriter
                        .println("Ignoring exception in disposing debuggee VM: "
                                + e);
            }
        }
    }

    /**
     * Closes JDWP connection stored in VmMirror.
     */
    protected void closeConnection() {
        if (vmMirror != null) {
            try {
                vmMirror.closeConnection();
            } catch (IOException e) {
                logWriter.println("Ignoring exception in closing connection: "
                        + e);
            }
        }
    }

    /**
     * Separate thread for waiting for process exit for specified timeout.
     */
    class ProcessWaiter extends Thread {
        public void run() {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                logWriter
                        .println("Ignoring exception in ProcessWaiter thread interrupted: "
                                + e);
            }
        }
    }
}
