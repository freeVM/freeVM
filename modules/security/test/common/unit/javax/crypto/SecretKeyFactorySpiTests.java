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

package javax.crypto;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import junit.framework.TestCase;


/**
 * Tests for <code>SecretKeyFactorySpi</code> class constructors and methods.
 * 
 */

public class SecretKeyFactorySpiTests extends TestCase {

    /**
     * Constructor for SecretKeyfactorySpiTests.
     * 
     * @param arg0
     */
    public SecretKeyFactorySpiTests(String arg0) {
        super(arg0);
    }

    /**
     * 
     * Test for <code>SecretKeyFactorySpi</code> constructor Assertion:
     * constructs SecretKeyFactorySpi
     */
    public void testSecretKeyFactorySpi01() throws InvalidKeyException,
            InvalidKeySpecException {
        SecretKeyFactorySpi skfSpi = (SecretKeyFactorySpi) new MySecretKeyFactorySpi();
        assertTrue(skfSpi instanceof SecretKeyFactorySpi);
        SecretKey sk = null;
        assertNull("Not null result", skfSpi.engineTranslateKey(sk));

        KeySpec kspec = null;
        assertNull("Not null result", skfSpi.engineGenerateSecret(kspec));

        assertNull("Not null result", skfSpi.engineGetKeySpec(sk, null));
    }
}
