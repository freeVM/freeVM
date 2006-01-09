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

import java.security.cert.CertificateParsingException;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>CertificateParsingException</code> class constructors and
 * methods.
 * 
 */
public class CertificateParsingExceptionTest extends PerformanceTest {

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
     * Constructor for CertificateParsingExceptionTests.
     * 
     * @param arg0
     */
    public CertificateParsingExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertificateParsingException";

    static String createErr(Exception tE, Exception eE) {
        return "CertificateParsingException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertificateParsingException()</code> constructor
     * Assertion: constructs CertificateParsingException with no detail message
     */
    public void testCertificateParsingException01() {
        logln("==test_01: CertificateParsingException==");

        CertificateParsingException tE = new CertificateParsingException();
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateParsingException(String)</code> constructor
     * Assertion: constructs CertificateParsingException with detail message
     * msg. Parameter <code>msg</code> is not null.
     */
    public void testCertificateParsingException02() {
        logln("==test_02: CertificateParsingException==");

        CertificateParsingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateParsingException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateParsingException);
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
     * Test for <code>CertificateParsingException(String)</code> constructor
     * Assertion: constructs CertificateParsingException when <code>msg</code>
     * is null
     */
    public void testCertificateParsingException03() {
        logln("==test_03: CertificateParsingException==");

        String msg = null;
        CertificateParsingException tE = new CertificateParsingException(msg);
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateParsingException(Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is null
     */
    public void testCertificateParsingException04() {
        logln("==test_04: CertificateParsingException==");

        Throwable cause = null;
        CertificateParsingException tE = new CertificateParsingException(cause);
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateParsingException(Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is not null
     */
    public void testCertificateParsingException05() {
        logln("==test_05: CertificateParsingException==");

        CertificateParsingException tE = new CertificateParsingException(tCause);
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
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
     * Test for <code>CertificateParsingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testCertificateParsingException06() {
        logln("==test_06: CertificateParsingException==");

        CertificateParsingException tE = new CertificateParsingException(null,
                null);
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateParsingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testCertificateParsingException07() {
        logln("==test_07: CertificateParsingException==");

        CertificateParsingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateParsingException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateParsingException);
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
     * Test for <code>CertificateParsingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testCertificateParsingException08() {
        logln("==test_08: CertificateParsingException==");

        CertificateParsingException tE = new CertificateParsingException(null,
                tCause);
        assertTrue(errNotExc, tE instanceof CertificateParsingException);
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
     * Test for <code>CertificateParsingException(String, Throwable)</code>
     * constructor Assertion: constructs CertificateParsingException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testCertificateParsingException09() {
        logln("==test_09: CertificateParsingException==");

        CertificateParsingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateParsingException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateParsingException);
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