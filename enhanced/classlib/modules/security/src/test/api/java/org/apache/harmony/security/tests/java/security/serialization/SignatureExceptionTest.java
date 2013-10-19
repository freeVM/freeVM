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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security.serialization;

import java.security.SignatureException;

import org.apache.harmony.testframework.serialization.SerializationTest;


/**
 * Test for SignatureException seialization 
 *  
 */

public class SignatureExceptionTest extends SerializationTest {

    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };

    protected Object[] getData() {
        Exception cause = new Exception(msgs[1]);
        SignatureException dExc = new SignatureException(msgs[0], cause);
        String msg = null;
        Throwable th = null;
        return new Object[] { new SignatureException(), new SignatureException(msg),
                new SignatureException(msgs[1]),
                new SignatureException(new Throwable()), new SignatureException(th),
                new SignatureException(msgs[1], dExc) };
    }

    public static void main(String[] args) {
       junit.textui.TestRunner.run(SignatureExceptionTest.class);
    }
}