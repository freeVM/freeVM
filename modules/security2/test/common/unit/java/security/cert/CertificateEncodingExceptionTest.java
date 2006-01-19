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

import java.security.cert.CertificateEncodingException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>CertificateEncodingException</code> class constructors and
 * methods.
 * 
 */
public class CertificateEncodingExceptionTest extends PerformanceTest {

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
     * Constructor for CertificateEncodingExceptionTests.
     * 
     * @param arg0
     */
    public CertificateEncodingExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertificateEncodingException";

    static String createErr(Exception tE, Exception eE) {
        return "CertificateEncodingException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertificateEncodingException()</code> constructor
     * Assertion: constructs CertificateEncodingException with no detail message
     */
    public void testCertificateEncodingException01() {
        logln("==test_01: CertificateEncodingException==");

        CertificateEncodingException tE = new CertificateEncodingException();
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateEncodingException(String)</code> constructor
     * Assertion: constructs CertificateEncodingException with detail message
     * msg. Parameter <code>msg</code> is not null.
     */
    public void testCertificateEncodingException02() {
        logln("==test_02: CertificateEncodingException==");

        CertificateEncodingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateEncodingException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateEncodingException);
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
     * Test for <code>CertificateEncodingException(String)</code> constructor
     * Assertion: constructs CertificateEncodingException when <code>msg</code>
     * is null
     */
    public void testCertificateEncodingException03() {
        logln("==test_03: CertificateEncodingException==");

        String msg = null;
        CertificateEncodingException tE = new CertificateEncodingException(msg);
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateEncodingException(Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is null
     */
    public void testCertificateEncodingException04() {
        logln("==test_04: CertificateEncodingException==");

        Throwable cause = null;
        CertificateEncodingException tE = new CertificateEncodingException(
                cause);
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateEncodingException(Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is not null
     */
    public void testCertificateEncodingException05() {
        logln("==test_05: CertificateEncodingException==");

        CertificateEncodingException tE = new CertificateEncodingException(
                tCause);
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
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
     * Test for <code>CertificateEncodingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testCertificateEncodingException06() {
        logln("==test_06: CertificateEncodingException==");

        CertificateEncodingException tE = new CertificateEncodingException(
                null, null);
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateEncodingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testCertificateEncodingException07() {
        logln("==test_07: CertificateEncodingException==");

        CertificateEncodingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateEncodingException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateEncodingException);
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
     * Test for <code>CertificateEncodingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testCertificateEncodingException08() {
        logln("==test_08: CertificateEncodingException==");

        CertificateEncodingException tE = new CertificateEncodingException(
                null, tCause);
        assertTrue(errNotExc, tE instanceof CertificateEncodingException);
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
     * Test for <code>CertificateEncodingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateEncodingException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testCertificateEncodingException09() {
        logln("==test_09: CertificateEncodingException==");

        CertificateEncodingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateEncodingException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateEncodingException);
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