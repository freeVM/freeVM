/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
/**
 * @author Hugo Beilis
 * @author Osvaldo Demo
 * @author Jorge Rafael
 * @version 1.0
 */
package ar.org.fitc.test.rmi.object2test;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class BigEcho_imp extends UnicastRemoteObject implements BigEcho {

    private static final long serialVersionUID = 1L;

    public BigEcho_imp() throws RemoteException {
        super();
    }

    public BigEcho_imp(int port) throws RemoteException {
        super(port);
    }

    public BigEcho_imp(int port, RMIClientSocketFactory csf,
            RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
    }

    public void echo() throws RemoteException {
    }

    public void echo(Object... objs) throws RemoteException {
    }

    public Object echoAndReturn() throws RemoteException {
        return null;
    }

    public Object echoAndReturn(Object... objs) throws RemoteException {
        return null;
    }

}
