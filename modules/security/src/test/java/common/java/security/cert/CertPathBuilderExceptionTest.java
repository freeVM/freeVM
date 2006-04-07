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

import junit.framework.TestCase;


/**
 * Tests for <code>CertPathBuilderException</code> class constructors and
 * methods.
 * 
 */
public class CertPathBuilderExceptionTest extends TestCase {

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

    static String createErr(Exception tE, Exception eE) {
        return "CertPathBuilderException ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertPathBuilderException()</code> constructor Assertion:
     * constructs CertPathBuilderException with no detail message
     */
    public void testCertPathBuilderException01() {
        CertPathBuilderException tE = new CertPathBuilderException();
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
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i]);
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
        String msg = null;
        CertPathBuilderException tE = new CertPathBuilderException(msg);
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
        Throwable cause = null;
        CertPathBuilderException tE = new CertPathBuilderException(cause);
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
        CertPathBuilderException tE = new CertPathBuilderException(tCause);
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
        CertPathBuilderException tE = new CertPathBuilderException(null, null);
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
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], null);
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
        CertPathBuilderException tE = new CertPathBuilderException(null, tCause);
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
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], tCause);
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
