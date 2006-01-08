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

import javax.security.cert.CertificateException;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>DigestException</code> class constructors and methods.
 * 
 */
public class CertificateExceptionTest extends PerformanceTest {

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
     * Constructor for CertificateExceptionTests.
     * 
     * @param arg0
     */
    public CertificateExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not CertificateException";

    static String createErr(Exception tE, Exception eE) {
        return "CertificateException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>CertificateException()</code> constructor Assertion:
     * constructs CertificateException with no detail message
     */
    public void testCertificateException01() {
        logln("==test_01: CertificateException==");

        CertificateException tE = new CertificateException();
        assertTrue(errNotExc, tE instanceof CertificateException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>CertificateException(String)</code> constructor
     * Assertion: constructs CertificateException with detail message msg.
     * Parameter <code>msg</code> is not null.
     */
    public void testCertificateException02() {
        logln("==test_02: CertificateException==");

        CertificateException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateException(msgs[i]);
            assertTrue(errNotExc.concat(" (msg: ").concat(msgs[i]).concat(")"),
                    tE instanceof CertificateException);
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
     * Test for <code>CertificateException(String)</code> constructor
     * Assertion: constructs CertificateException when <code>msg</code> is
     * null
     */
    public void testCertificateException03() {
        logln("==test_03: CertificateException==");

        String msg = null;
        CertificateException tE = new CertificateException(msg);
        assertTrue(errNotExc, tE instanceof CertificateException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

}