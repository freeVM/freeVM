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

package org.apache.harmony.auth.tests.javax.security.sasl.serialization;

import javax.security.sasl.AuthenticationException;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * Test for AuthenticationException serialization
 * 
 */

public class AuthenticationExceptionTest extends SerializationTest {

    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };

    @Override
    protected Object[] getData() {
        String msg = null;
        Exception cause = new Exception(msgs[1]);
        return new Object[] { new AuthenticationException(),
                new AuthenticationException(msg),
                new AuthenticationException(msgs[1]),
                new AuthenticationException(msg, null),
                new AuthenticationException(msgs[0], null),
                new AuthenticationException(msg, cause),
                new AuthenticationException(msgs[1], cause)
                };
    }
}