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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author  Mikhail A. Markov
 * @version $Revision: 1.7.4.3 $
 */
package java.rmi.server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.LoggingPermission;

import org.apache.harmony.rmi.common.RMILog;
import org.apache.harmony.rmi.server.ServerConnectionManager;


/**
 * @com.intel.drl.spec_ref
 *
 * @author  Mikhail A. Markov
 * @version $Revision: 1.7.4.3 $
 */
public abstract class RemoteServer extends RemoteObject {

    private static final long serialVersionUID = -4100238210092549637L;

    /**
     * @com.intel.drl.spec_ref
     */
    protected RemoteServer(RemoteRef ref) {
        super(ref);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected RemoteServer() {
        super();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static String getClientHost() throws ServerNotActiveException {
        String host = ServerConnectionManager.getClientHost();

        if (host == null) {
            throw new ServerNotActiveException(
                    "There are no in-progress RMI calls in this thread.");
        }
        return host;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static PrintStream getLog() {
        return RMILog.getServerCallsLog().getPrintStream();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static void setLog(OutputStream out) {
        SecurityManager mgr = System.getSecurityManager();

        if (mgr != null) {
            mgr.checkPermission(new LoggingPermission("control", null));
        }
        RMILog.getServerCallsLog().setOutputStream(out);
    }
}
