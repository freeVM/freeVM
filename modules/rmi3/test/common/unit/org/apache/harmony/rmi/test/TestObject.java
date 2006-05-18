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
 * @author  Mikhail A. Markov, Vasily Zakharov
 * @version $Revision: 1.1.2.1 $
 */
package org.apache.harmony.rmi.test;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * @author  Mikhail A. Markov, Vasily Zakharov
 * @version $Revision: 1.1.2.1 $
 */
public class TestObject implements MyRemoteInterface1, TestInterface {

    Remote r;

    String msg;

    public TestObject() {
        msg = null;
    }

    public TestObject(String msg) {
        this.msg = msg;
    }

    public String test1() throws RemoteException {
        if (msg == null) {
            System.out.println("TestObject.test1() started");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {}

            System.out.println("TestObject.test1() complete");
            return null;
        } else {
            return msg;
        }
    }

    public void runRemote(Remote r) throws RemoteException {
        this.r = r;
    }
}
