/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */    
/**
 * @author Ivan G. Popov
 * @version $Revision: 1.2 $
 */

/**
 * Created on 29.01.2006
 */

package org.apache.harmony.test.stress.jpda.jdwp.scenario.share;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import org.apache.harmony.test.stress.jpda.jdwp.share.JDWPQADebuggeeWrapper;
import org.apache.harmony.share.framework.jpda.LogWriter;
import org.apache.harmony.share.framework.jpda.TestErrorException;
import org.apache.harmony.share.framework.jpda.TestOptions;

/**
 * This class provides DebuggeeWrapper implementation based on JUnit framework.
 * Debuggee is always launched on local machine and attaches to debugger.
 */
public class JDWPQAManualDebuggeeWrapper extends JDWPQADebuggeeWrapper {

    private BufferedReader reader = null;

    /**
     * Creates new instance with given data.
     * @param settings test run options
     * @param logWriter where to print log messages
     */
    public JDWPQAManualDebuggeeWrapper(TestOptions settings, LogWriter logWriter, boolean server) {
        super(settings, logWriter, server);
        reader = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * Get response from user and check if it is as expected.
     */
    private void checkUserResponse(String expected) throws IOException {
        String response = reader.readLine();
        if (!expected.equals(response)) {
            throw new TestErrorException("Unexpected user response: " + response
                                            + " (expected: " + expected + ")");
        }
    }

    /**
     * Asks user to launche process with given command line and waits for confirmation.
     * 
     * @param cmdLine command line
     * @return null instead of associated Process object
     * @throws IOException if user does not confirm process launching
     */
    protected Process launchProcess(String cmdLine) throws IOException {
        getLogWriter().println("\n>>> Start debuggee VM with this command line:\n" + cmdLine);
        
        getLogWriter().println("\n>>> Confirm that debuggee VM has started [yes/no]:");
        checkUserResponse("yes");
        return null;
    }

    /**
     * Waits for user to confirm that launched process has exited.
     * 
     * @param should be null instead of assciated Process object
     * @throws IOException if user does not confirm process exit
     */
    protected void WaitForProcessExit(Process process)  throws IOException {
        getLogWriter().println("\n>>> Confirm that debuggee VM has exited [yes/no]:");
        checkUserResponse("yes");
    }
}
