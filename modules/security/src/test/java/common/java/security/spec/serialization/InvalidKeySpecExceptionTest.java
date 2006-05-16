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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security.spec.serialization;

import java.security.spec.InvalidKeySpecException;

import org.apache.harmony.security.support.SerializationTest;


/**
 * Test for InvalidKeySpecException seialization 
 *  
 */

public class InvalidKeySpecExceptionTest extends SerializationTest {

    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };

    protected Object[] getData() {
        Exception cause = new Exception(msgs[1]);
        InvalidKeySpecException dExc = new InvalidKeySpecException(msgs[0], cause);
        String msg = null;
        Throwable th = null;
        return new Object[] { new InvalidKeySpecException(), new InvalidKeySpecException(msg),
                new InvalidKeySpecException(msgs[1]),
                new InvalidKeySpecException(new Throwable()), new InvalidKeySpecException(th),
                new InvalidKeySpecException(msgs[1], dExc) };
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InvalidKeySpecExceptionTest.class);
    }
}