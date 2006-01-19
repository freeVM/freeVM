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

package javax.security.cert;

import javax.security.cert.CertificateExpiredException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>DigestException</code> class constructors and methods.
 * 
 */
public class CertificateExpiredExceptionTest extends PerformanceTest {

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
     * Constructor for CertificateExpiredExceptionTests.
     * 
     * @param arg0
     */
    public CertificateExpiredExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertificateExpiredException";

    static String createErr(Exception tE, Exception eE) {
        return "CertificateExpiredException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertificateExpiredException()</code> constructor
     * Assertion: constructs CertificateExpiredException with no detail message
     */
    public void testCertificateExpiredException01() {
        logln("==test_01: CertificateExpiredException==");

        CertificateExpiredException tE = new CertificateExpiredException();
        assertTrue(errNotExc, tE instanceof CertificateExpiredException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateExpiredException(String)</code> constructor
     * Assertion: constructs CertificateExpiredException with detail message
     * msg. Parameter <code>msg</code> is not null.
     */
    public void testCertificateExpiredException02() {
        logln("==test_02: CertificateExpiredException==");

        CertificateExpiredException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateExpiredException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateExpiredException);
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
     * Test for <code>CertificateExpiredException(String)</code> constructor
     * Assertion: constructs CertificateExpiredException when <code>msg</code>
     * is null
     */
    public void testCertificateExpiredException03() {
        logln("==test_03: CertificateExpiredException==");

        String msg = null;
        CertificateExpiredException tE = new CertificateExpiredException(msg);
        assertTrue(errNotExc, tE instanceof CertificateExpiredException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

}