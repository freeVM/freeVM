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

package java.security.interfaces;


import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

import junit.framework.TestCase;

/**
 * Tests for <code>RSAPrivateKey</code> class field
 * 
 */
public class RSAPrivateKeyTest extends TestCase {

    /**
     * Constructor for RSAPrivateKeyTest.
     * 
     * @param arg0
     */
    public RSAPrivateKeyTest(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkRSAPrivateKey key = new checkRSAPrivateKey();
        assertEquals("Incorrect serialVersionUID",
                key.getSerVerUID(), //RSAPrivateKey.serialVersionUID, 
                5187144804936595022L);
    }
    public class checkRSAPrivateKey implements RSAPrivateKey {
        public String getAlgorithm() {
            return "RSAPrivateKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
        public BigInteger getPrivateExponent() {
            return null;
        }
        public BigInteger getModulus() {
            return null;
        }
    }
}
