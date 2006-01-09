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

package javax.crypto;

import javax.crypto.IllegalBlockSizeException;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>IllegalBlockSizeException</code> class constructors and
 * methods.
 * 
 */
public class IllegalBlockSizeExceptionTest extends PerformanceTest {

    public static void main(String[] args) {
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for IllegalBlockSizeExceptionTests.
     * 
     * @param arg0
     */
    public IllegalBlockSizeExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not IllegalBlockSizeException";

    static String createErr(Exception tE, Exception eE) {
        return "IllegalBlockSizeException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>IllegalBlockSizeException()</code> constructor
     * Assertion: constructs IllegalBlockSizeException with no detail message
     */
    public void testIllegalBlockSizeException01() {
        logln("==test_01: IllegalBlockSizeException==");

        IllegalBlockSizeException tE = new IllegalBlockSizeException();
        assertTrue(errNotExc, tE instanceof IllegalBlockSizeException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>IllegalBlockSizeException(String)</code> constructor
     * Assertion: constructs IllegalBlockSizeException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testIllegalBlockSizeException02() {
        logln("==test_02: IllegalBlockSizeException==");

        IllegalBlockSizeException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new IllegalBlockSizeException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof IllegalBlockSizeException);
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

    /**
     * Test for <code>IllegalBlockSizeException(String)</code> constructor
     * Assertion: constructs IllegalBlockSizeException when <code>msg</code>
     * is null
     */
    public void testIllegalBlockSizeException03() {
        logln("==test_03: IllegalBlockSizeException==");

        String msg = null;
        IllegalBlockSizeException tE = new IllegalBlockSizeException(msg);
        assertTrue(errNotExc, tE instanceof IllegalBlockSizeException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

}