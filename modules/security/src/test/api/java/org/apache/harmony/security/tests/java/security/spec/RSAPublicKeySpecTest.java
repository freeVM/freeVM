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

package org.apache.harmony.security.tests.java.security.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import junit.framework.TestCase;

/**
 * Tests for <code>RSAPublicKeySpec</code> class fields and methods
 * 
 */
public class RSAPublicKeySpecTest extends TestCase {

    /**
     * Constructor for RSAPublicKeySpecTest.
     * @param name
     */
    public RSAPublicKeySpecTest(String name) {
        super(name);
    }


    /**
     * Test #1 for <code>RSAPublicKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPublicKeySpec</code>
     * object using valid parameters
     */
    public final void testRSAPublicKeySpec01() {
        KeySpec ks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        
        assertTrue(ks instanceof RSAPublicKeySpec);
    }

    /**
     * Test #2 for <code>RSAPublicKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPublicKeySpec</code>
     * object using valid parameters
     */
    public final void testRSAPublicKeySpec02() {
        KeySpec ks =
            new RSAPublicKeySpec(null, null);

        assertTrue(ks instanceof RSAPublicKeySpec);
    }
    
    /**
     * Test for <code>getModulus()</code> method<br>
     * Assertion: returns modulus
     */
    public final void testGetModulus() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(1234567890L),
                                 BigInteger.valueOf(3L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getModulus()));
    }

    /**
     * Test for <code>getPublicExponent()</code> method<br>
     * Assertion: returns public exponent
     */
    public final void testGetPublicExponent() {
        RSAPublicKeySpec rpks =
            new RSAPublicKeySpec(BigInteger.valueOf(3L),
                                 BigInteger.valueOf(1234567890L));
        assertTrue(BigInteger.valueOf(1234567890L).equals(rpks.getPublicExponent()));
    }

}
