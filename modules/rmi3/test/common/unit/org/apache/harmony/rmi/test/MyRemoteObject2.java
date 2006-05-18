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
 * @version $Revision: 1.1.2.4 $
 */
package org.apache.harmony.rmi.test;

import java.rmi.RemoteException;

import java.rmi.server.UnicastRemoteObject;


/**
 * @author  Mikhail A. Markov, Vasily Zakharov
 * @version $Revision: 1.1.2.4 $
 */
public class MyRemoteObject2 extends UnicastRemoteObject
        implements MyRemoteInterface2 {

    String param = null;

    public MyRemoteObject2(String str) throws RemoteException {
        param = str;
    }

    public String test2() throws RemoteException {
        System.out.println("MyRemoteObject2: test2 called");
        return param;
    }
}
