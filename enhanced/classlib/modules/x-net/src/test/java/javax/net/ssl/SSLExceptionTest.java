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

package javax.net.ssl;

import javax.net.ssl.SSLException;

import junit.framework.TestCase;


/**
 * Tests for <code>SSLException</code> class constructors and methods.
 * 
 */
public class SSLExceptionTest extends TestCase {

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
     * Constructor for SSLExceptionTests.
     * 
     * @param arg0
     */
    public SSLExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not SSLException";

    static String createErr(Exception tE, Exception eE) {
        return "SSLException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>SSLException(String)</code> constructor Assertion:
     * constructs SSLException with detail message msg. Parameter
     * <code>msg</code> is not null.
     */
    public void testSSLException01() {
        SSLException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new SSLException(msgs[i]);
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
     * Test for <code>SSLException(String)</code> constructor Assertion:
     * constructs SSLException when <code>msg</code> is null
     */
    public void testSSLException02() {
        String msg = null;
        SSLException tE = new SSLException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>DigestException(Throwable)</code> constructor Assertion:
     * constructs DigestException when <code>cause</code> is null
     */
    public void testSSLException03() {
        Throwable cause = null;
        SSLException tE = new SSLException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>SSLException(Throwable)</code> constructor Assertion:
     * constructs SSLException when <code>cause</code> is not null
     */
    public void testSSLException04() {
        SSLException tE = new SSLException(tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        // SSLException is subclass of IOException, but IOException has not
        // constructors with Throwable parameters
        if (tE.getCause() != null) {
            //	assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>SSLException(String, Throwable)</code> constructor
     * Assertion: constructs SSLException when <code>cause</code> is null
     * <code>msg</code> is null
     */
    public void testSSLException05() {
        SSLException tE = new SSLException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>SSLException(String, Throwable)</code> constructor
     * Assertion: constructs SSLException when <code>cause</code> is null
     * <code>msg</code> is not null
     */
    public void testSSLException06() {
        SSLException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new SSLException(msgs[i], null);
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
     * Test for <code>SSLException(String, Throwable)</code> constructor
     * Assertion: constructs SSLException when <code>cause</code> is not null
     * <code>msg</code> is null
     */
    public void testSSLException07() {
        SSLException tE = new SSLException(null, tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        // SSLException is subclass of IOException, but IOException has not
        // constructors with Throwable parameters
        if (tE.getCause() != null) {
            //	assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>SSLException(String, Throwable)</code> constructor
     * Assertion: constructs SSLException when <code>cause</code> is not null
     * <code>msg</code> is not null
     */
    public void testSSLException08() {
        SSLException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new SSLException(msgs[i], tCause);
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
            // SSLException is subclass of IOException, but IOException has not
            // constructors with Throwable parameters
            if (tE.getCause() != null) {
                //	assertNotNull("getCause() must not return null",
                // tE.getCause());
                assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
            }
            try {
                throw tE;
            } catch (Exception e) {
                assertTrue(createErr(tE, e), tE.equals(e));
            }
        }
    }

}
