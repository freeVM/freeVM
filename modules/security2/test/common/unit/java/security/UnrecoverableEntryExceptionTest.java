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

package java.security;

import org.apache.harmony.security.test.PerformanceTest;

/**
 * Tests for <code>UnrecoverableEntryException</code> class
 * 
 */

public class UnrecoverableEntryExceptionTest extends PerformanceTest {

    /**
     * Constructor for UnrecoverableEntryExceptionTest.
     * 
     * @param arg0
     */
    public UnrecoverableEntryExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static String errNotExc = "Not UnrecoverableEntryException object";

    static String createErr(Exception tE, Exception eE) {
        return "UnrecoverableEntryException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /*
     * Class under test for void UnrecoverableEntryException()
     */
    public void testUnrecoverableEntryException() {
        UnrecoverableEntryException tE = new UnrecoverableEntryException();
        assertTrue(errNotExc, tE instanceof UnrecoverableEntryException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /*
     * Class under test for void UnrecoverableEntryException(String)
     */
    public void testUnrecoverableEntryExceptionString() {
        UnrecoverableEntryException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new UnrecoverableEntryException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof UnrecoverableEntryException);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
            try {
                throw tE;
            } catch (Exception e) {
                assertTrue(createErr(tE, e), tE.equals(e));
            }
        }
    }
}