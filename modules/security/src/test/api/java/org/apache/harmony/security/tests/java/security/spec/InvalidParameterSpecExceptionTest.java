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

package org.apache.harmony.security.tests.java.security.spec;

import java.security.spec.InvalidParameterSpecException;

import junit.framework.TestCase;

/**
 * Tests for <code>InvalidParameterSpecException</code> class constructors and
 * methods.
 * 
 */
public class InvalidParameterSpecExceptionTest extends TestCase {

    public static void main(String[] args) {
    }

    /**
     * Constructor for InvalidParameterSpecExceptionTests.
     * 
     * @param arg0
     */
    public InvalidParameterSpecExceptionTest(String arg0) {
        super(arg0);
    }

    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    static Throwable tCause = new Throwable("Throwable for exception");

    static String createErr(Exception tE, Exception eE) {
        return "InvalidParameterSpecException:  ".concat(tE.toString()).concat(
                " is not equal to caught exception: ").concat(eE.toString());
    }

    /**
     * Test for <code>InvalidParameterSpecException()</code> constructor
     * Assertion: constructs InvalidParameterSpecException with no detail
     * message
     */
    public void testInvalidParameterSpecException01() {
        InvalidParameterSpecException tE = new InvalidParameterSpecException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

    /**
     * Test for <code>InvalidParameterSpecException(String)</code> constructor
     * Assertion: constructs InvalidParameterSpecException with detail message
     * msg. Parameter <code>msg</code> is not null.
     */
    public void testInvalidParameterSpecException02() {
        InvalidParameterSpecException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new InvalidParameterSpecException(msgs[i]);
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
     * Test for <code>InvalidParameterSpecException(String)</code> constructor
     * Assertion: constructs InvalidParameterSpecException when <code>msg</code>
     * is null
     */
    public void testInvalidParameterSpecException03() {
        String msg = null;
        InvalidParameterSpecException tE = new InvalidParameterSpecException(
                msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        try {
            throw tE;
        } catch (Exception e) {
            assertTrue(createErr(tE, e), tE.equals(e));
        }
    }

}
