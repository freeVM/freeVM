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

@SuppressWarnings("serial")
public class EchoUnicast_Imp extends UnicastRemoteObject implements Echo {

    public int msgCount = 0;

    public EchoUnicast_Imp() throws RemoteException {
        super();

    }

    public EchoUnicast_Imp(int arg0) throws RemoteException {
        super(arg0);

    }

    public EchoUnicast_Imp(int arg0, RMIClientSocketFactory arg1,
            RMIServerSocketFactory arg2) throws RemoteException {
        super(arg0, arg1, arg2);

    }

    public String echo(String msg) throws RemoteException {

        msgCount += 1;
        return EchoUnicast_Imp.class + " - Have said: " + msg;
    }
}
