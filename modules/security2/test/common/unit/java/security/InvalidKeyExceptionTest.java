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

import java.security.InvalidKeyException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>InvalidKeyException</code> class constructors and methods.
 * 
 */
public class InvalidKeyExceptionTest extends PerformanceTest {

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
     * Constructor for InvalidKeyExceptionTests.
     * 
     * @param arg0
     */
    public InvalidKeyExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not InvalidKeyException";

    static String createErr(Exception tE, Exception eE) {
        return "InvalidKeyException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>InvalidKeyException()</code> constructor Assertion:
     * constructs InvalidKeyException with no detail message
     */
    public void testInvalidKeyException01() {
        logln("==test_01: InvalidKeyException==");

        InvalidKeyException tE = new InvalidKeyException();
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(String)</code> constructor
     * Assertion: constructs InvalidKeyException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testInvalidKeyException02() {
        logln("==test_02: InvalidKeyException==");

        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof InvalidKeyException);
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
     * Test for <code>InvalidKeyException(String)</code> constructor
     * Assertion: constructs InvalidKeyException when <code>msg</code> is null
     */
    public void testInvalidKeyException03() {
        logln("==test_03: InvalidKeyException==");

        String msg = null;
        InvalidKeyException tE = new InvalidKeyException(msg);
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(Throwable)</code> constructor
     * Assertion: constructs InvalidKeyException when <code>cause</code> is
     * null
     */
    public void testInvalidKeyException04() {
        logln("==test_04: InvalidKeyException==");

        Throwable cause = null;
        InvalidKeyException tE = new InvalidKeyException(cause);
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(Throwable)</code> constructor
     * Assertion: constructs InvalidKeyException when <code>cause</code> is
     * not null
     */
    public void testInvalidKeyException05() {
        logln("==test_05: InvalidKeyException==");

        InvalidKeyException tE = new InvalidKeyException(tCause);
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(String, Throwable)</code>
     * constructor Assertion: constructs InvalidKeyException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testInvalidKeyException06() {
        logln("==test_06: InvalidKeyException==");

        InvalidKeyException tE = new InvalidKeyException(null, null);
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(String, Throwable)</code>
     * constructor Assertion: constructs InvalidKeyException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testInvalidKeyException07() {
        logln("==test_07: InvalidKeyException==");

        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof InvalidKeyException);
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
     * Test for <code>InvalidKeyException(String, Throwable)</code>
     * constructor Assertion: constructs InvalidKeyException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testInvalidKeyException08() {
        logln("==test_08: InvalidKeyException==");

        InvalidKeyException tE = new InvalidKeyException(null, tCause);
        assertTrue(errNotExc, tE instanceof InvalidKeyException);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidKeyException(String, Throwable)</code>
     * constructor Assertion: constructs InvalidKeyException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testInvalidKeyException09() {
        logln("==test_09: InvalidKeyException==");

        InvalidKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidKeyException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof InvalidKeyException);
            String getM = tE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);

            try {
                throw tE;
            } catch (Exception e) {
                assertTrue(createErr(tE, e), tE.equals(e));
            }
        }
    }
}