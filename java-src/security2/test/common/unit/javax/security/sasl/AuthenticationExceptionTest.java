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

package javax.security.sasl;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for constructors and methods of AuthenticationException class
 * 
 */
public class AuthenticationExceptionTest extends PerformanceTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AuthenticationExceptionTest.class);
    }

    /**
     * Constructor for AuthenticationExceptionTests.
     * 
     * @param arg0
     */
    public AuthenticationExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    private static String errNotExc = "Exception is not AuthenticationException";

    static String createErr(Exception tE, Exception eE) {
        return "AuthenticationException: ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>AuthenticationException()</code> constructor 
     * Assertion: constructs AuthenticationException with null message and 
     * null root exception. 
     */
    public void testAuthenticationException01() {
        AuthenticationException tE;
        tE = new AuthenticationException();
        assertTrue(errNotExc, tE instanceof AuthenticationException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }

    /**
     * Test for <code>AuthenticationException(String detail)</code> constructor 
     * Assertion:
     * constructs AuthenticationException with defined detail message. 
     * Parameter <code>detail</code> is not null.
     */
    public void testAuthenticationException02() {
        AuthenticationException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new AuthenticationException(msgs[i]);
            assertTrue(errNotExc.concat(" (detail: ").concat(msgs[i]).concat(")"),
                    tE instanceof AuthenticationException);
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
     * Test for <code>AuthenticationException(String detail)</code> constructor 
     * Assertion: constructs AuthenticationException when <code>detail</code> is null
     */
    public void testAuthenticationException03() {
        String msg = null;
        AuthenticationException tE = new AuthenticationException(msg);
        assertTrue(errNotExc, tE instanceof AuthenticationException);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>AuthenticationException(String detail, Throwable ex)</code> constructor
     * Assertion: constructs AuthenticationException when <code>ex</code> is null
     * <code>detail</code> is null
     */
    public void testAuthenticationException04() {
        AuthenticationException tE = new AuthenticationException(null, null);
        assertTrue(errNotExc, tE instanceof AuthenticationException);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>AuthenticationException(String detail, Throwable ex)</code> constructor
     * Assertion: constructs AuthenticationException when <code>ex</code> is null
     * <code>detail</code> is not null
     */
    public void testAuthenticationException05() {
        AuthenticationException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new AuthenticationException(msgs[i], null);
            assertTrue(errNotExc.concat(" (detail: ").concat(msgs[i]).concat(")"),
                    tE instanceof AuthenticationException);
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
     * Test for <code>AuthenticationException(String detail, Throwable  ex)</code> constructor
     * Assertion: constructs AuthenticationException when <code>ex</code> is not null
     * <code>detail</code> is null
     */
    public void testAuthenticationException06() {        
        AuthenticationException tE = new AuthenticationException(null, tCause);
        assertTrue(errNotExc, tE instanceof AuthenticationException);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertEquals("getCause() must return ".concat(tCause.toString()),
                tE.getCause(), tCause);
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>AuthenticationException(String detail, Throwable ex)</code> constructor
     * Assertion: constructs AuthenticationException when <code>ex</code> is not null
     * <code>detail</code> is not null
     */
    public void testAuthenticationException07() {
        AuthenticationException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new AuthenticationException(msgs[i], tCause);
            assertTrue(errNotExc.concat(" (detail: ").concat(msgs[i]).concat(")"),
                    tE instanceof AuthenticationException);
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
            assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
            try {
                throw tE;
            } catch (Exception e) {
                assertTrue(createErr(tE, e), tE.equals(e));
            }
        }
    }
    
    /**
     * Test for <code>toString()</code> method
     * Assertion: returns not null string
     */
    public void testToString() {
        Throwable[] th = {
                null,
                new Exception(
                        "New Exception for toString() method verification"),
                new Throwable(), new Exception("exception", new Exception()) };

        AuthenticationException eT;
        eT = new AuthenticationException();
        assertNotNull("Incorrect null string", eT.toString());
        for (int i = 0; i < msgs.length; i++) {
            eT = new AuthenticationException(msgs[i]);
            assertTrue("Incorrect result string", eT.toString()
                    .indexOf(msgs[i]) >= 0);

            for (int j = 0; j < th.length; j++) {
                eT = new AuthenticationException(msgs[i], th[j]);
                assertTrue("Incorrect result string", eT.toString().indexOf(
                        msgs[i]) >= 0);
                if (th[j] != null) {
                    assertTrue("Incorrect result string", eT.toString()
                            .indexOf(th[j].toString()) >= 0);
                }
            }
        }
    }
}