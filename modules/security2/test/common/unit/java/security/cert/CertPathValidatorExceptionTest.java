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

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPath;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>CertPathValidatorException</code> class constructors and
 * methods.
 * 
 */
public class CertPathValidatorExceptionTest extends PerformanceTest {

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
     * Constructor for CertPathValidatorExceptionTests.
     * 
     * @param arg0
     */
    public CertPathValidatorExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertPathValidatorException";

    static String createErr(Exception tE, Exception eE) {
        return "CertPathValidatorException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertPathValidatorException()</code> constructor
     * Assertion: constructs CertPathValidatorException with no detail message
     */
    public void testCertPathValidatorException01() {
        logln("==test_01: CertPathValidatorException==");

        CertPathValidatorException tE = new CertPathValidatorException();
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathValidatorException(String)</code> constructor
     * Assertion: constructs CertPathValidatorException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testCertPathValidatorException02() {
        logln("==test_02: CertPathValidatorException==");

        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathValidatorException);
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
     * Test for <code>CertPathValidatorException(String)</code> constructor
     * Assertion: constructs CertPathValidatorException when <code>msg</code>
     * is null
     */
    public void testCertPathValidatorException03() {
        logln("==test_03: CertPathValidatorException==");

        String msg = null;
        CertPathValidatorException tE = new CertPathValidatorException(msg);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathValidatorException(Throwable)</code> constructor
     * Assertion: constructs CertPathValidatorException when <code>cause</code>
     * is null
     */
    public void testCertPathValidatorException04() {
        logln("==test_04: CertPathValidatorException==");

        Throwable cause = null;
        CertPathValidatorException tE = new CertPathValidatorException(cause);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathValidatorException(Throwable)</code> constructor
     * Assertion: constructs CertPathValidatorException when <code>cause</code>
     * is not null
     */
    public void testCertPathValidatorException05() {
        logln("==test_05: CertPathValidatorException==");

        CertPathValidatorException tE = new CertPathValidatorException(tCause);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
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
     * Test for <code>CertPathValidatorException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is null <code>msg</code> is null
     */
    public void testCertPathValidatorException06() {
        logln("==test_06: CertPathValidatorException==");

        CertPathValidatorException tE = new CertPathValidatorException(null,
                null);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertPathValidatorException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is null <code>msg</code> is not null
     */
    public void testCertPathValidatorException07() {
        logln("==test_07: CertPathValidatorException==");

        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathValidatorException);
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
     * Test for <code>CertPathValidatorException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is not null <code>msg</code> is null
     */
    public void testCertPathValidatorException08() {
        logln("==test_08: CertPathValidatorException==");

        CertPathValidatorException tE = new CertPathValidatorException(null,
                tCause);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
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
     * Test for <code>CertPathValidatorException(String, Throwable)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is not null <code>msg</code> is not null
     */
    public void testCertPathValidatorException09() {
        logln("==test_09: CertPathValidatorException==");

        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertPathValidatorException);
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

    /**
     * Test for
     * <code>CertPathValidatorException(String, Throwable, CertPath, int)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is null <code>msg</code> is null
     * <code>certPath</code> is null <code>index</code> is -1
     */
    public void testCertPathValidatorException10() {
        logln("==test_10: CertPathValidatorException==");

        CertPathValidatorException tE = new CertPathValidatorException(null,
                null, null, -1);
        assertTrue(errNotExc, tE instanceof CertPathValidatorException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        assertNull("getCertPath() must return null", tE.getCertPath());
        assertEquals("getIndex() must be -1", tE.getIndex(), -1);
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for
     * <code>CertPathValidatorException(String, Throwable, CertPath, int)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> is null <code>msg</code> is null
     * <code>certPath</code> is null <code>index</code> not -1 throws:
     * IllegalArgumentException
     */
    public void testCertPathValidatorException11() {
        logln("==test_11: CertPathValidatorException==");

        CertPathValidatorException tE;
        int[] indx = { 0, 1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE };
        for (int j = 0; j < indx.length; j++) {
            for (int i = 0; i < msgs.length; i++) {
                try {
                    tE = new CertPathValidatorException(msgs[i], tCause, null,
                            indx[j]);
                    fail("Error. IllegalArgumentException was not thrown as expected. "
                            + " msg: "
                            + msgs[i]
                            + ", certPath is null and index is " + indx[j]);
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    /**
     * Test for
     * <code>CertPathValidatorException(String, Throwable, CertPath, int)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> not null <code>msg</code> not null
     * <code>certPath</code> is null <code>index</code> is -1
     */
    public void testCertPathValidatorException12() {
        logln("==test_12: CertPathValidatorException==");

        CertPathValidatorException tE;

        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, null, -1);
                assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(
                        ")"), tE instanceof CertPathValidatorException);
                String getM = tE.getMessage();
                String toS = tCause.toString();
                if (msgs[i].length() > 0) {
                    assertTrue("getMessage() must contain ".concat(msgs[i]),
                            getM.indexOf(msgs[i]) != -1);
                    if (!getM.equals(msgs[i])) {
                        assertTrue("getMessage() should contain ".concat(toS),
                                getM.indexOf(toS) != -1);
                    }
                }
                assertNotNull("getCause() must not return null", tE.getCause());
                assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
                assertNull("getCertPath() must return null", tE.getCertPath());
                assertEquals("getIndex() must return -1", tE.getIndex(), -1);
                try {
                    throw tE;
                } catch (Exception e) {
                    assertTrue(createErr(tE, e), tE.equals(e));
                }
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected exception: " + e.toString()
                        + " Parameters: msg: " + msgs[i]
                        + ", certPath is null and index is -1");
            }
        }
    }

    /**
     * Test for
     * <code>CertPathValidatorException(String, Throwable, CertPath, int)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> not null <code>msg</code> not null
     * <code>certPath</code> not null <code>index</code>< -1 || >=
     * certPath.getCertificates().size() throws: IndexOutOfBoundsException
     */
    public void testCertPathValidatorException13() {
        logln("==test_13: CertPathValidatorException==");

        CertPathValidatorException tE;
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        int[] indx = { -2, -100, 0, 1, 100, Integer.MAX_VALUE,
                Integer.MIN_VALUE };
        for (int j = 0; j < indx.length; j++) {
            for (int i = 0; i < msgs.length; i++) {
                try {
                    tE = new CertPathValidatorException(msgs[i], tCause, cp,
                            indx[j]);
                    fail("IndexOutOfBoundsException was not thrown as expected. "
                            + " msg: "
                            + msgs[i]
                            + ", certPath is null and index is " + indx[j]);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }

    /**
     * Test for
     * <code>CertPathValidatorException(String, Throwable, CertPath, int)</code>
     * constructor Assertion: constructs CertPathValidatorException when
     * <code>cause</code> not null <code>msg</code> not null
     * <code>certPath</code> not null <code>index</code><
     * certPath.getCertificates().size()
     */
    public void testCertPathValidatorException14() {
        logln("==test_14: CertPathValidatorException==");

        CertPathValidatorException tE;
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, cp, -1);
                assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(
                        ")"), tE instanceof CertPathValidatorException);
                String getM = tE.getMessage();
                String toS = tCause.toString();
                if (msgs[i].length() > 0) {
                    assertTrue("getMessage() must contain ".concat(msgs[i]),
                            getM.indexOf(msgs[i]) != -1);
                    if (!getM.equals(msgs[i])) {
                        assertTrue("getMessage() should contain ".concat(toS),
                                getM.indexOf(toS) != -1);
                    }
                }
                assertNotNull("getCause() must not return null", tE.getCause());
                assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
                assertNotNull("getCertPath() must not return null", tE
                        .getCertPath());
                assertEquals(
                        "getCertPath() must return ".concat(cp.toString()), tE
                                .getCertPath(), cp);
                assertEquals("getIndex() must return -1", tE.getIndex(), -1);
                try {
                    throw tE;
                } catch (Exception e) {
                    assertTrue(createErr(tE, e), tE.equals(e));
                }

            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected IndexOutOfBoundsException was thrown. "
                        + e.toString());
            }
        }
    }
}