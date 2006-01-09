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

package java.security.serialization;

import java.security.NoSuchAlgorithmException;

import com.openintel.drl.security.test.SerializationTest;

/**
 * Test for NoSuchAlgorithmException seialization 
 *  
 */

public class NoSuchAlgorithmExceptionTest extends SerializationTest {

    public static String[] msgs = {
            "New message",
            "Long message for Exception. Long message for Exception. Long message for Exception." };

    protected Object[] getData() {
        Exception cause = new Exception(msgs[1]);
        NoSuchAlgorithmException dExc = new NoSuchAlgorithmException(msgs[0], cause);
        String msg = null;
        Throwable th = null;
        return new Object[] { new NoSuchAlgorithmException(), new NoSuchAlgorithmException(msg),
                new NoSuchAlgorithmException(msgs[1]),
                new NoSuchAlgorithmException(new Throwable()), new NoSuchAlgorithmException(th),
                new NoSuchAlgorithmException(msgs[1], dExc) };
    }

    protected void assertDeserialized(Object oref, Object otest) {
        NoSuchAlgorithmException ref = (NoSuchAlgorithmException) oref;
        NoSuchAlgorithmException test = (NoSuchAlgorithmException) otest;
        String s = ref.getMessage();
        Throwable th = ref.getCause();
        if (s == null) {
            assertNull(test.getMessage());
        } else {
            assertEquals(test.getMessage(), s);
        }
        if (th == null) {
            assertNull(test.getCause());
        } else {
            Throwable th1 = test.getCause();
            assertEquals(th1.getClass(), th.getClass());
            String s1 = th.getMessage();
            if (s1 == null) {
                assertNull(th1.getMessage());
            } else {
                assertEquals(th1.getMessage(), s1);
            }
        }
    }
    
    public static void main(String[] args) {
       junit.textui.TestRunner.run(NoSuchAlgorithmExceptionTest.class);
    }
}