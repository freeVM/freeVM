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

package javax.net.ssl.serialization;

import javax.net.ssl.SSLKeyException;

import org.apache.harmony.security.test.SerializationTest;


/**
 * Test for SSLKeyException seialization
 * 
 */

public class SSLKeyExceptionTest extends SerializationTest {

    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };

    protected Object[] getData() {
        return new Object[] { new SSLKeyException(null),
                new SSLKeyException(msgs[0]), new SSLKeyException(msgs[1]) };
    }

    protected void assertDeserialized(Object oref, Object otest) {
        SSLKeyException ref = (SSLKeyException) oref;
        SSLKeyException test = (SSLKeyException) otest;
        String s = ref.getMessage();
        if (s == null) {
            assertNull(test.getMessage());
        } else {
            assertEquals(test.getMessage(), s);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SSLKeyExceptionTest.class);
    }
}