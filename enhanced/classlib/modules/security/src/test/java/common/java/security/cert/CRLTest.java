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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security.cert;

import org.apache.harmony.security.cert.MyCRL;
import junit.framework.TestCase;


/**
 * Tests for <code>java.security.cert.CRL</code> fields and methods
 * 
 */
public class CRLTest extends TestCase {

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
     * Constructor for CRLTest.
     * @param name
     */
    public CRLTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test #1 for <code>getType()</code> method<br>
     * Assertion: returns <code>CRL</code> type
     */
    public final void testGetType01() {
        CRL crl = new MyCRL("TEST_TYPE");
        assertTrue("TEST_TYPE".equals(crl.getType()));
    }

    /**
     * Test #2 for <code>getType()</code> method<br>
     * Assertion: returns <code>CRL</code> type
     */
    public final void testGetType02() {
        CRL crl = new MyCRL(null);
        assertTrue(crl.getType() == null);
    }

    //
    // the following tests just call methods
    // that are abstract in <code>Certificate</code>
    // (So they just like signature tests)
    //

    /**
     * Test for <code>toString()</code> method
     */
    public final void testToString() {
        CRL crl = new MyCRL("TEST_TYPE");
        crl.toString();
    }

    /**
     * Test for <code>isRevoked()</code> method
     */
    public final void testIsRevoked() {
        CRL crl = new MyCRL("TEST_TYPE");
        crl.isRevoked(null);
    }

}
