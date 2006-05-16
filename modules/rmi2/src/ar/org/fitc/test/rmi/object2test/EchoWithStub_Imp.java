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

@SuppressWarnings("serial")
public class EchoWithStub_Imp extends Object implements Echo {

    public EchoWithStub_Imp() throws RemoteException {
        super();

    }

    public int msgCount = 0;

    public String echo(String msg) throws RemoteException {
        msgCount += 1;
        return EchoUnicast_Imp.class + " - Ha dicho: " + msg;
    }

}
