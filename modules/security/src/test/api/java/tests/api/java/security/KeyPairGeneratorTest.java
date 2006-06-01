/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.DSAParameterSpec;

public class KeyPairGeneratorTest extends junit.framework.TestCase {

	/**
	 * @tests java.security.KeyPairGenerator#genKeyPair()
	 */
	public void test_genKeyPair() throws Exception {
       		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
       		gen.initialize(1024);
       		KeyPair pair = gen.genKeyPair();
       		assertNotNull("KeyPair is null", pair);
	}

	/**
	 * @tests java.security.KeyPairGenerator#getAlgorithm()
	 */
	public void test_getAlgorithm() throws Exception {
       		String alg = KeyPairGenerator.getInstance("DSA").getAlgorithm();
       		assertEquals("getAlgorithm returned enexpected value", "DSA", alg);
	}

	/**
	 * @tests java.security.KeyPairGenerator#getInstance(java.lang.String)
	 */
	public void test_getInstanceLjava_lang_String() throws Exception {
		KeyPairGenerator.getInstance("DSA");
	}

	/**
	 * @tests java.security.KeyPairGenerator#getInstance(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_getInstanceLjava_lang_StringLjava_lang_String() throws Exception {
		// Test1: Test with named provider
       		Provider[] providers = Security
       				.getProviders("KeyPairGenerator.DSA");

       		for (int i = 0; i < providers.length; i++) {
       			KeyPairGenerator.getInstance("DSA", providers[i].getName());
       		}// end for


		// Test2: Test with empty provider name - should raise
		// IllegalArgumentException
		try {
			KeyPairGenerator.getInstance("DSA", "");
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * @tests java.security.KeyPairGenerator#getProvider()
	 */
	public void test_getProvider() throws Exception {
       		Provider p = KeyPairGenerator.getInstance("DSA").getProvider();
       		assertNotNull("provider is null", p);
	}

	/**
	 * @tests java.security.KeyPairGenerator#initialize(int)
	 */
	public void test_initializeI() throws Exception {
       		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
       		keyPair.initialize(1024);
	}

	/**
	 * @tests java.security.KeyPairGenerator#initialize(int,
	 *        java.security.SecureRandom)
	 */
	public void test_initializeILjava_security_SecureRandom() throws Exception {
       		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
       		keyPair.initialize(1024, new SecureRandom());
	}

	/**
	 * @tests java.security.KeyPairGenerator#initialize(java.security.spec.AlgorithmParameterSpec)
	 */
	public void test_initializeLjava_security_spec_AlgorithmParameterSpec() throws Exception {
       		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
       		keyPair.initialize(new DSAParameterSpec(BigInteger.ONE,
       				BigInteger.ONE, BigInteger.ONE));
	}

	/**
	 * @tests java.security.KeyPairGenerator#initialize(java.security.spec.AlgorithmParameterSpec,
	 *        java.security.SecureRandom)
	 */
	public void test_initializeLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom() throws Exception {
       		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("DSA");
       		keyPair.initialize(new DSAParameterSpec(BigInteger.ONE,
       				BigInteger.ONE, BigInteger.ONE), new SecureRandom());
	}
}