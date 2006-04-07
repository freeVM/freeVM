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

import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;


/**
 * Tests for <code>NoSuchAlgorithmException</code> class constructors and
 * methods.
 * 
 */
public class NoSuchAlgorithmExceptionTest extends TestCase {

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
     * Constructor for NoSuchAlgorithmExceptionTests.
     * 
     * @param arg0
     */
    public NoSuchAlgorithmExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    static String createErr(Exception tE, Exception eE) {
        return "NoSuchAlgorithmException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>NoSuchAlgorithmException()</code> constructor Assertion:
     * constructs NoSuchAlgorithmException with no detail message
     */
    public void testNoSuchAlgorithmException01() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>NoSuchAlgorithmException(String)</code> constructor
     * Assertion: constructs NoSuchAlgorithmException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testNoSuchAlgorithmException02() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i]);
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
     * Test for <code>NoSuchAlgorithmException(String)</code> constructor
     * Assertion: constructs NoSuchAlgorithmException when <code>msg</code> is
     * null
     */
    public void testNoSuchAlgorithmException03() {
        String msg = null;
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>NoSuchAlgorithmException(Throwable)</code> constructor
     * Assertion: constructs NoSuchAlgorithmException when <code>cause</code>
     * is null
     */
    public void testNoSuchAlgorithmException04() {
        Throwable cause = null;
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>NoSuchAlgorithmException(Throwable)</code> constructor
     * Assertion: constructs NoSuchAlgorithmException when <code>cause</code>
     * is not null
     */
    public void testNoSuchAlgorithmException05() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(tCause);
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
     * Test for <code>NoSuchAlgorithmException(String, Throwable)</code>
     * constructor Assertion: constructs NoSuchAlgorithmException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testNoSuchAlgorithmException06() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>NoSuchAlgorithmException(String, Throwable)</code>
     * constructor Assertion: constructs NoSuchAlgorithmException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testNoSuchAlgorithmException07() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i], null);
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
     * Test for <code>NoSuchAlgorithmException(String, Throwable)</code>
     * constructor Assertion: constructs NoSuchAlgorithmException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testNoSuchAlgorithmException08() {
        NoSuchAlgorithmException tE = new NoSuchAlgorithmException(null, tCause);
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
     * Test for <code>NoSuchAlgorithmException(String, Throwable)</code>
     * constructor Assertion: constructs NoSuchAlgorithmException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testNoSuchAlgorithmException09() {
        NoSuchAlgorithmException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchAlgorithmException(msgs[i], tCause);
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
