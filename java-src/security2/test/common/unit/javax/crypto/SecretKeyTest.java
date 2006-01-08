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

import javax.crypto.SecretKey;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>SecretKey</code> class field
 * 
 */
public class SecretKeyTest extends PerformanceTest {

    /**
     * Constructor for SecretKeyTest.
     * 
     * @param arg0
     */
    public SecretKeyTest(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkSecretKey sk = new checkSecretKey();
        assertEquals("Incorrect serialVersionUID", 
                sk.getSerVerUID(), //SecretKey.serialVersionUID
                -4795878709595146952L);
    }
    
    public class checkSecretKey implements SecretKey {
        public String getAlgorithm() {
            return "SecretKey";
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
    }
}