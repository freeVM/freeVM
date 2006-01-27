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

import java.security.cert.CertStoreException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>CertStoreException</code> class constructors and methods.
 * 
 */
public class CertStoreExceptionTest extends PerformanceTest {

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
     * Constructor for CertStoreExceptionTests.
     * 
     * @param arg0
     */
    public CertStoreExceptionTest(String arg0) {
        super(arg0);
    }

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertStoreException";

    static String createErr(Exception tE, Exception eE) {
        return "CertStoreException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertStoreException()</code> constructor Assertion:
     * constructs CertStoreException with no detail message
     */
    public void testCertStoreException01() {
        CertStoreException tE = new CertStoreException();
        assertTrue(errNotExc, tE instanceof CertStoreException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertStoreException(String)</code> constructor Assertion:
     * constructs CertStoreException with detail message msg. Parameter
     * <code>msg</code> is not null.
     */
    public void testCertStoreException02() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertStoreException);
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
     * Test for <code>CertStoreException(String)</code> constructor Assertion:
     * constructs CertStoreException when <code>msg</code> is null
     */
    public void testCertStoreException03() {
        String msg = null;
        CertStoreException tE = new CertStoreException(msg);
        assertTrue(errNotExc, tE instanceof CertStoreException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertStoreException(Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is
     * null
     */
    public void testCertStoreException04() {
        Throwable cause = null;
        CertStoreException tE = new CertStoreException(cause);
        assertTrue(errNotExc, tE instanceof CertStoreException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertStoreException(Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is not
     * null
     */
    public void testCertStoreException05() {
        CertStoreException tE = new CertStoreException(tCause);
        assertTrue(errNotExc, tE instanceof CertStoreException);
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
     * Test for <code>CertStoreException(String, Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is
     * null <code>msg</code> is null
     */
    public void testCertStoreException06() {
        CertStoreException tE = new CertStoreException(null, null);
        assertTrue(errNotExc, tE instanceof CertStoreException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertStoreException(String, Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is
     * null <code>msg</code> is not null
     */
    public void testCertStoreException07() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i], null);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertStoreException);
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
     * Test for <code>CertStoreException(String, Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is not
     * null <code>msg</code> is null
     */
    public void testCertStoreException08() {
        CertStoreException tE = new CertStoreException(null, tCause);
        assertTrue(errNotExc, tE instanceof CertStoreException);
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
     * Test for <code>CertStoreException(String, Throwable)</code> constructor
     * Assertion: constructs CertStoreException when <code>cause</code> is not
     * null <code>msg</code> is not null
     */
    public void testCertStoreException09() {
        CertStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertStoreException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertStoreException);
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