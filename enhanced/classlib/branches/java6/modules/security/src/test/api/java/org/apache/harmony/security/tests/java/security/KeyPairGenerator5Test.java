/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.security.tests.java.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.DSAParameterSpec;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.DSAParams;

public class KeyPairGenerator5Test extends junit.framework.TestCase {

   private class MockKeyPairGenerator extends KeyPairGenerator
   {

    protected MockKeyPairGenerator(String algorithm) {
        super(algorithm);        
    }       
   }
   
   
   public void test_generateKeyPair()
   {
       MockKeyPairGenerator mockKeyPairGenerator = new MockKeyPairGenerator("MOCKKEYPAIRGENERATOR");
       assertNull(mockKeyPairGenerator.generateKeyPair());
   }
}
