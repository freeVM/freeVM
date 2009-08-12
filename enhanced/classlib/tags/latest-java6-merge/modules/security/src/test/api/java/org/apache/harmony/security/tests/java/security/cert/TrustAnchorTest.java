/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.tests.support.TestKeyPair;
import org.apache.harmony.security.tests.support.cert.TestUtils;

import junit.framework.TestCase;

/**
 * Unit tests for <code>TrustAnchor</code>
 */
public class TrustAnchorTest extends TestCase {
    private static final String keyAlg = "DSA";
    // Sample of some valid CA name
    private static final String validCaNameRfc2253 =
        "CN=Test CA,"+
        "OU=Testing Division,"+
        "O=Test It All,"+
        "L=Test Town,"+
        "ST=Testifornia,"+
        "C=Testland";

    /**
     * Test #1 for <code>TrustAnchor(String, PublicKey, byte[])</code> constructor<br> 
     * Assertion: creates <code>TrustAnchor</code> instance<br>
     * Test preconditions: valid parameters passed<br>
     * Expected: must pass without any exceptions
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorStringPublicKeybyteArray01()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1
        new TrustAnchor(validCaNameRfc2253, pk, getFullEncoding());
        // sub testcase 2
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingPSOnly());        
        // sub testcase 3
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingESOnly());        
        // sub testcase 4
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingNoMinMax());        
    }

    /**
     * Test #2 for <code>TrustAnchor(String, PublicKey, byte[])</code> constructor<br> 
     * Assertion: creates <code>TrustAnchor</code> instance<br>
     * Test preconditions: <code>null</code> as nameConstraints passed<br>
     * Expected: must pass without any exceptions
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorStringPublicKeybyteArray02()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        new TrustAnchor(validCaNameRfc2253, pk, null);
    }

    /**
     * Test #3 for <code>TrustAnchor(String, PublicKey, byte[])</code> constructor<br> 
     * Assertion: nameConstraints cloned by the constructor<br>
     * Test preconditions: modify passed nameConstraints<br>
     * Expected: modification must not change object internal state
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorStringPublicKeybyteArray03()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        byte[] nc = getEncodingPSOnly();
        byte[] ncCopy = nc.clone();
        // sub testcase 5 - nameConstraints can be null
        TrustAnchor ta = new TrustAnchor(validCaNameRfc2253, pk, ncCopy);
        // modify
        ncCopy[0]=(byte)0;
        // check that above modification did not change
        // object internal state
        assertTrue(Arrays.equals(nc, ta.getNameConstraints()));
    }

    /**
     * Test #4 for <code>TrustAnchor(String, PublicKey, byte[])</code> constructor<br> 
     * Assertion: <code>NullPointerException</code> if <code>caName</code>
     * or <code>caPublicKey</code> parameter is <code>null</code><br>
     * Test preconditions: pass <code>null</code> as mentioned parameter<br>
     * Expected: NullPointerException
     */
    public final void testTrustAnchorStringPublicKeybyteArray04()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1: 'caName' param is null
        try {
            new TrustAnchor((String)null, pk, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

        // sub testcase 2: 'caPublicKey' param is null
        try {
            new TrustAnchor(validCaNameRfc2253, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

        // sub testcase 3: 'caName' and 'caPublicKey' params are null
        try {
            new TrustAnchor((String)null, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

        // sub testcase 4: 'caName' param is empty
        try {
            new TrustAnchor("", pk, getEncodingPSOnly());
            fail("IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }

        // sub testcase 5: 'caName' param is incorrect distinguished name
        try {
            new TrustAnchor("AID.11.12=A", pk, getEncodingPSOnly());
            fail("IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }
    }

    /**
     * Test #1 for <code>TrustAnchor(X500Principal, PublicKey, byte[])</code> constructor<br> 
     * Assertion: creates <code>TrustAnchor</code> instance<br>
     * Test preconditions: valid parameters passed<br>
     * Expected: must pass without any exceptions
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray01()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        // sub testcase 1
        new TrustAnchor(x500p, pk, getFullEncoding());
        // sub testcase 2
        new TrustAnchor(x500p, pk, getEncodingPSOnly());        
        // sub testcase 3
        new TrustAnchor(x500p, pk, getEncodingESOnly());        
        // sub testcase 4
        new TrustAnchor(x500p, pk, getEncodingNoMinMax());        
    }

    /**
     * Test #2 for <code>TrustAnchor(X500Principal, PublicKey, byte[])</code> constructor<br> 
     * Assertion: creates <code>TrustAnchor</code> instance<br>
     * Test preconditions: <code>null</code> as nameConstraints passed<br>
     * Expected: must pass without any exceptions
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray02()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        X500Principal x500p = new X500Principal(validCaNameRfc2253);

        new TrustAnchor(x500p, pk, null);
    }

    /**
     * Test #3 for <code>TrustAnchor(X500Principal, PublicKey, byte[])</code> constructor<br> 
     * Assertion: nameConstraints cloned by the constructor<br>
     * Test preconditions: modify passed nameConstraints<br>
     * Expected: modification must not change object internal state
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray03()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        byte[] nc = getEncodingPSOnly();
        byte[] ncCopy = nc.clone();
        // sub testcase 5 - nameConstraints can be null
        TrustAnchor ta = new TrustAnchor(new X500Principal(validCaNameRfc2253),
                pk, ncCopy);
        // modify
        ncCopy[0]=(byte)0;
        // check that above modification did not change
        // object internal state
        assertTrue(Arrays.equals(nc, ta.getNameConstraints()));
    }

    /**
     * Test #4 for <code>TrustAnchor(X500Principal, PublicKey, byte[])</code> constructor<br> 
     * Assertion: <code>NullPointerException</code> if <code>caPrincipal</code>
     * or <code>caPublicKey</code> parameter is <code>null</code><br>
     * Test preconditions: pass <code>null</code> as mentioned parameter<br>
     * Expected: NullPointerException
     * @throws InvalidKeySpecException
     */
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray04()
            throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        // sub testcase 1
        try {
            new TrustAnchor((X500Principal)null,
                    pk, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

        // sub testcase 2
        try {
            new TrustAnchor(x500p, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

        // sub testcase 3
        try {
            new TrustAnchor((X500Principal)null, null,
                    getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }

    }

    /**
     * Test #1 for <code>getCAPublicKey()</code> method<br>
     *  
     * Assertion: returns most trusted CA public key</code><br>
     * Test preconditions: valid name passed to the constructor<br>
     * Expected: the same name must be returned by the method<br>
     * 
     */
    public final void testGetCAPublicKey01() throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertEquals("equals1", pk, ta.getCAPublicKey());
        // sub testcase 2
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", pk, ta.getCAPublicKey());
    }


    /**
     * Test #1 for <code>getCAName()</code> method<br>
     *  
     * Assertion: returns most trusted CA name as <code>String</code><br>
     * Test preconditions: valid name passed to the constructor<br>
     * Expected: the same name must be returned by the method<br>
     * @throws InvalidKeySpecException
     */
    public final void testGetCAName01() throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertEquals("equals1", validCaNameRfc2253, ta.getCAName());
        // sub testcase 2
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", validCaNameRfc2253, ta.getCAName());
    }

    /**
     * Test #2 for <code>getCAName()</code> method<br>
     *  
     * Assertion: returns ... <code>null</code> if <code>TrustAnchor</code>
     * was not specified as trusted certificate<br>
     * Test preconditions: test object is not specified as trusted certificate<br>
     * Expected: <code>null</code> as return value<br>
     * @throws InvalidKeySpecException
     */
    public final void testGetTrustedCer02() throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertNull("null1", ta.getTrustedCert());
        // sub testcase 2
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertNull("null2", ta.getTrustedCert());
    }


    /**
     * Test #1 for <code>getCA()</code> method<br>
     *  
     * Assertion: returns most trusted CA<br>
     * Test preconditions: valid CA or CA name passed to the constructor<br>
     * Expected: the same CA ot the CA with the same name must be returned
     * by the method<br>
     * @throws InvalidKeySpecException
     */
    public final void testGetCA01() throws Exception {

        PublicKey pk = new TestKeyPair(keyAlg).getPublic();

        // sub testcase 1
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        X500Principal ca = ta.getCA();
        assertEquals("equals1", validCaNameRfc2253, ca.getName());
        // sub testcase 2
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", x500p, ta.getCA());
    }

    //
    // Private stuff
    //
    
    /*
     * The following methods return valid DER encoding
     * for the following ASN.1 definition (as specified in RFC 3280 -
     *  Internet X.509 Public Key Infrastructure.
     *  Certificate and Certificate Revocation List (CRL) Profile.
     *  http://www.ietf.org/rfc/rfc3280.txt):
     * 
     *  NameConstraints ::= SEQUENCE {
     *             permittedSubtrees       [0]     GeneralSubtrees OPTIONAL,
     *             excludedSubtrees        [1]     GeneralSubtrees OPTIONAL }
     *
     *        GeneralSubtrees ::= SEQUENCE SIZE (1..MAX) OF GeneralSubtree
     *
     *        GeneralSubtree ::= SEQUENCE {
     *             base                    GeneralName,
     *             minimum         [0]     BaseDistance DEFAULT 0,
     *             maximum         [1]     BaseDistance OPTIONAL }
     *
     *        BaseDistance ::= INTEGER (0..MAX)
     *
     *        GeneralName ::= CHOICE {
     *             otherName                       [0]     OtherName,
     *             rfc822Name                      [1]     IA5String,
     *             dNSName                         [2]     IA5String,
     *             x400Address                     [3]     ORAddress,
     *             directoryName                   [4]     Name,
     *             ediPartyName                    [5]     EDIPartyName,
     *             uniformResourceIdentifier       [6]     IA5String,
     *             iPAddress                       [7]     OCTET STRING,
     *             registeredID                    [8]     OBJECT IDENTIFIER}
     */ 

    //
    // Full NameConstraints encoding
    // (generated by own encoder class created during test development)
    //
    // @return Full NameConstraints encoding
    // with all OPTIONAL values presented.
    //
    private static final byte[] getFullEncoding() {
        // DO NOT MODIFY!
        return new byte[] {
                (byte)0x30,(byte)0x81,(byte)0x8c,(byte)0xa0,
                (byte)0x44,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x66,(byte)0x6f,(byte)0x6f,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x62,(byte)0x61,(byte)0x72,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x12,(byte)0x86,
                (byte)0x0a,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x6d,(byte)0x75,(byte)0x75,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0xa1,(byte)0x44,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x66,(byte)0x6f,
                (byte)0x6f,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x62,(byte)0x61,
                (byte)0x72,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x12,(byte)0x86,(byte)0x0a,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x6d,(byte)0x75,
                (byte)0x75,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01
        };
    }

    //
    // NameConstraints encoding without excludedSubtrees
    // (generated by own encoder class created during test development)
    //
    // @return NameConstraints encoding with 
    // permittedSubtrees only; all OPTIONAL
    // values in permittedSubtrees are presented.
    //
    private static final byte[] getEncodingPSOnly() {
        // DO NOT MODIFY!
        return new byte[] {
                (byte)0x30,(byte)0x46,(byte)0xa0,(byte)0x44,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x62,
                (byte)0x61,(byte)0x72,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x12,(byte)0x86,(byte)0x0a,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
        };
    }

    //
    // NameConstraints encoding without permittedSubtrees
    // (generated by own encoder class created during test development)
    //
    // @return NameConstraints encoding with 
    // excludedSubtrees only; all OPTIONAL
    // values in excludedSubtrees are presented.
    //
    private static final byte[] getEncodingESOnly() {
        // DO NOT MODIFY!
        return new byte[] {
                (byte)0x30,(byte)0x46,(byte)0xa1,(byte)0x44,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70, // http
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66, // ://f
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63, // oo.c
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01, // om
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x62,
                (byte)0x61,(byte)0x72,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x12,(byte)0x86,(byte)0x0a,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
        };
    }

    //
    // NameConstraints full encoding with all (OPTIONAL)
    // minimum/maximum GeneralSubtree fields OMITTED
    // (generated by own encoder class created during test development)
    //
    // @return Full NameConstraints encoding
    // with all (OPTIONAL) minimum/maximum
    // GeneralSubtree fields OMITTED
    //
    private static final byte[] getEncodingNoMinMax() {
        // DO NOT MODIFY!
        return new byte[] {
                (byte)0x30,(byte)0x68,(byte)0xa0,(byte)0x32,
                (byte)0x30,(byte)0x10,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x30,(byte)0x10,
                (byte)0x86,(byte)0x0e,(byte)0x66,(byte)0x69,
                (byte)0x6c,(byte)0x65,(byte)0x3a,(byte)0x2f,
                (byte)0x2f,(byte)0x62,(byte)0x61,(byte)0x72,
                (byte)0x2e,(byte)0x63,(byte)0x6f,(byte)0x6d,
                (byte)0x30,(byte)0x0c,(byte)0x86,(byte)0x0a,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0xa1,(byte)0x32,
                (byte)0x30,(byte)0x10,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x30,(byte)0x10,
                (byte)0x86,(byte)0x0e,(byte)0x68,(byte)0x74,
                (byte)0x74,(byte)0x70,(byte)0x3a,(byte)0x2f,
                (byte)0x2f,(byte)0x62,(byte)0x61,(byte)0x72,
                (byte)0x2e,(byte)0x63,(byte)0x6f,(byte)0x6d,
                (byte)0x30,(byte)0x0c,(byte)0x86,(byte)0x0a,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,
        };
    }

}
