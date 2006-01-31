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
import java.security.interfaces.DSAPrivateKey;

import junit.framework.TestCase;

/**
 * Tests for <code>DSAPrivateKey</code> class field
 * 
 */
public class DSAPrivateKeyTest extends TestCase {

    /**
     * Constructor for DSAPrivateKeyTest.
     * 
     * @param arg0
     */
    public DSAPrivateKeyTest(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkDSAPrivateKey k = new checkDSAPrivateKey();
        assertEquals("Incorrect serialVerstionUID",
                k.getSerVerUID(), //DSAPrivateKey.serialVersionUID 
                7776497482533790279L);
    }
    
    public class checkDSAPrivateKey implements DSAPrivateKey {
        public String getAlgorithm() {
            return "DSAPrivateKey";
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
        public BigInteger getX() {
            return null;
        }
        public DSAParams getParams() {
            return null;
        }        
    }
}
