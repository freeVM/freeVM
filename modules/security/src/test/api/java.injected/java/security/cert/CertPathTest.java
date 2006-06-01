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

import org.apache.harmony.security.tests.support.cert.MyCertPath;

import junit.framework.TestCase;


/**
 * Tests for <code>CertPath</code> fields and methods
 * 
 */
public class CertPathTest extends TestCase {
    /**
     * Meaningless cert path encoding just for testing purposes
     */
    private static final byte[] testEncoding = new byte[] {
            (byte)1, (byte)2, (byte)3, (byte)4, (byte)5
    };

    /**
     * Constructor for CertPathTest.
     * @param name
     */
    public CertPathTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: returns hash of the <code>Certificate</code> instance
     */
    public final void testHashCode() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);

        assertTrue(cp1.hashCode() == cp2.hashCode());
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: hash code of equal objects should be the same
     */
    public final void testHashCodeEqualsObject() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue((cp1.hashCode() == cp2.hashCode()) && cp1.equals(cp2));
    }

    /**
     * Test for <code>getType()</code> method<br>
     * Assertion: returns cert path type
     */
    public final void testGetType() {
        assertEquals("MyEncoding", new MyCertPath(testEncoding).getType());
    }

    /**
     * Test #1 for <code>equals(Object)</code> method<br>
     * Assertion: object equals to itself 
     */
    public final void testEqualsObject01() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object equals to other <code>CertPath</code>
     * instance with the same state
     */
    public final void testEqualsObject02() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp2) && cp2.equals(cp1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to <code>null</code>
     */
    public final void testEqualsObject03() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals(null));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to other which is not
     * instance of <code>CertPath</code>
     */
    public final void testEqualsObject04() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals("MyEncoding"));
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: returns string representation of
     * <code>CertPath</code> object
     */
    public final void testToString() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertNotNull(cp1.toString());
    }

    //
    // the following tests just call methods
    // that are abstract in <code>CertPath</code>
    // (So they just like signature tests)
    //

    /**
     * This test just calls <code>getCertificates()</code> method<br>
     */
    public final void testGetCertificates() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getCertificates();
    }

    /**
     * This test just calls <code>getEncoded()</code> method<br>
     *
     * @throws CertificateEncodingException
     */
    public final void testGetEncoded() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded();
    }

    /**
     * This test just calls <code>getEncoded(String)</code> method<br>
     *
     * @throws CertificateEncodingException
     */
    public final void testGetEncodedString() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded("MyEncoding");
    }

    /**
     * This test just calls <code>getEncodings()</code> method<br>
     */
    public final void testGetEncodings() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncodings();
    }

}
