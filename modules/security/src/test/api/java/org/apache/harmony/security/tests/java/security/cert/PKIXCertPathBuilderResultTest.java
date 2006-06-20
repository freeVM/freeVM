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

package org.apache.harmony.security.tests.java.security.cert;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.spec.InvalidKeySpecException;

import org.apache.harmony.security.tests.support.cert.MyCertPath;
import org.apache.harmony.security.tests.support.cert.TestUtils;

import junit.framework.TestCase;

/**
 * Tests for <code>PKIXCertPathBuilderResult</code>
 * 
 */
public class PKIXCertPathBuilderResultTest extends TestCase {
    /**
     * Cert path encoding stub
     */
    private static final byte[] testEncoding = new byte[] {
            (byte)1, (byte)2, (byte)3, (byte)4, (byte)5
    };

    /**
     * PublicKey stub
     */
    private static PublicKey testPublicKey = new PublicKey() {
        public String getAlgorithm() {
            return "NeverMind";
        }
        public String getFormat() {
            return "NeverMind";
        }
        public byte[] getEncoded() {
            return new byte[] {};
        }
    };


    /**
     * Constructor for PKIXCertPathBuilderResultTest.
     * @param name
     */
    public PKIXCertPathBuilderResultTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test #1 for <code>PKIXCertPathBuilderResult(CertPath, TrustAnchor,
     *   PolicyNode, PublicKey)</code> constructor<br>
     * Assertion: Creates an instance of <code>PKIXCertPathBuilderResult</code>
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public final void testPKIXCertPathBuilderResult01()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        assertTrue(r instanceof PKIXCertPathBuilderResult);
    }

    /**
     * Test #2 for <code>PKIXCertPathBuilderResult(CertPath, TrustAnchor,
     *   PolicyNode, PublicKey)</code> constructor<br>
     * Assertion: plicy tree parameter may be <code>null</code>
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public final void testPKIXCertPathBuilderResult02()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    null,
                    testPublicKey);
        assertTrue(r instanceof PKIXCertPathBuilderResult);
    }

    /**
     * Test #3 for <code>PKIXCertPathBuilderResult(CertPath, TrustAnchor,
     *   PolicyNode, PublicKey)</code> constructor<br>
     * Assertion: <code>NullPointerException</code>
     * if certPath is <code>null</code>
     */
    public final void testPKIXCertPathBuilderResult03() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }

        try {
            // pass null
            new PKIXCertPathBuilderResult(
                    null,
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #4 for <code>PKIXCertPathBuilderResult(CertPath, TrustAnchor,
     *   PolicyNode, PublicKey)</code> constructor<br>
     * Assertion: <code>NullPointerException</code>
     * if trustAnchor is <code>null</code>
     */
    public final void testPKIXCertPathBuilderResult04() {
        try {
            // pass null
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    null,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #5 for <code>PKIXCertPathBuilderResult(CertPath, TrustAnchor,
     *   PolicyNode, PublicKey)</code> constructor<br>
     * Assertion: <code>NullPointerException</code>
     * if publicKey is <code>null</code>
     */
    public final void testPKIXCertPathBuilderResult05() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }

        try {
            // pass null
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test for <code>getCertPath()</code> method<br>
     * Assertion: the built and validated <code>CertPath</code>
     * (never <code>null</code>)
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public final void testGetCertPath() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }

        CertPath cp = new MyCertPath(testEncoding);
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    cp,
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);

        // must return the same reference
        // as passed to the constructor
        assertSame(cp, r.getCertPath());
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: the printable representation of this object
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public final void testToString()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);

        assertNotNull(r.toString());
    }

}
