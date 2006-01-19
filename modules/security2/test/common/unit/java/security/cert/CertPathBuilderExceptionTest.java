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

package java.security.cert;

import java.security.cert.CertPathBuilderException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>CertPathBuilderException</code> class constructors and
 * methods.
 * 
 */
public class CertPathBuilderExceptionTest extends PerformanceTest {

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
     * Constructor for CertPathBuilderExceptionTests.
     * 
     * @param arg0
     */
    public CertPathBuilderExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertPathBuilderException";

    static String createErr(Exception tE, Exception eE) {
        return "CertPathBuilderException ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertPathBuilderException()</code> constructor Assertion:
     * constructs CertPathBuilderException with no detail message
     */
    public void testCertPathBuilderException01() {
        logln("==test_01: CertPathBuilderException==");

        CertPathBuilderException tE = new CertPathBuilderException();
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathBuilderException(String)</code> constructor
     * Assertion: constructs CertPathBuilderException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testCertPathBuilderException02() {
        logln("==test_02: CertPathBuilderException==");

        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathBuilderException);
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
     * Test for <code>CertPathBuilderException(String)</code> constructor
     * Assertion: constructs CertPathBuilderException when <code>msg</code> is
     * null
     */
    public void testCertPathBuilderException03() {
        logln("==test_03: CertPathBuilderException==");

        String msg = null;
        CertPathBuilderException tE = new CertPathBuilderException(msg);
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathBuilderException(Throwable)</code> constructor
     * Assertion: constructs CertPathBuilderException when <code>cause</code>
     * is null
     */
    public void testCertPathBuilderException04() {
        logln("==test_04: CertPathBuilderException==");

        Throwable cause = null;
        CertPathBuilderException tE = new CertPathBuilderException(cause);
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathBuilderException(Throwable)</code> constructor
     * Assertion: constructs CertPathBuilderException when <code>cause</code>
     * is not null
     */
    public void testCertPathBuilderException05() {
        logln("==test_05: CertPathBuilderException==");

        CertPathBuilderException tE = new CertPathBuilderException(tCause);
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
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
     * Test for <code>CertPathBuilderException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathBuilderException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testCertPathBuilderException06() {
        logln("==test_06: CertPathBuilderException==");

        CertPathBuilderException tE = new CertPathBuilderException(null, null);
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathBuilderException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathBuilderException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testCertPathBuilderException07() {
        logln("==test_07: CertPathBuilderException==");

        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathBuilderException);
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
     * Test for <code>CertPathBuilderException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathBuilderException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testCertPathBuilderException08() {
        logln("==test_08: CertPathBuilderException==");

        CertPathBuilderException tE = new CertPathBuilderException(null, tCause);
        assertTrue(errNotExc, tE instanceof CertPathBuilderException);
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
     * Test for <code>CertPathBuilderException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathBuilderException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testCertPathBuilderException09() {
        logln("==test_09: CertPathBuilderException==");

        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathBuilderException);
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