/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.test.func.api.java.rmi.basicsecurity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.harmony.test.func.api.java.rmi.share.ServerInterface;

/**
 * Server can unite given line with it's own and return the result.
 * 
 */

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

    protected ServerImpl() throws RemoteException {
        super();
    }

    private static final long serialVersionUID = -1849424287100211994L;

    public String remoteMethod(String arg) {
        return "Remote method called. Result is: " + arg;
    }
}