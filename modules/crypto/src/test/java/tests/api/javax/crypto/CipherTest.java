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

package tests.api.javax.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import tests.support.resource.Support_Resources;

public class CipherTest extends junit.framework.TestCase {

	/**
	 * @tests javax.crypto.Cipher#getInstance(java.lang.String)
	 */
	public void test_getInstanceLjava_lang_String() {
		try {
			Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			assertNotNull("Received a null Cipher instance", cipher);
		} catch (Exception e) {
			fail("Could not find cipher");
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getInstance(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_getInstanceLjava_lang_StringLjava_lang_String() {
		try {
			Provider[] providers = Security.getProviders("Cipher.DES");
			if (providers != null) {
				for (int i = 0; i < providers.length; i++) {
					Cipher cipher = Cipher.getInstance("DES", providers[i]
							.getName());
					assertNotNull("Cipher.getInstance() returned a null value",
							cipher);

					// Exception case
					try {
						cipher = Cipher.getInstance("DoBeDoBeDo", providers[i]);
						fail("Should have thrown an NoSuchAlgorithmException");
					} catch (NoSuchAlgorithmException e) {
						// Expected
					} catch (Exception e) {
						fail("Expected an NoSuchAlgorithmException but got a "
								+ e);
					}
				}// end for
			} else {
				fail("No installed providers support Cipher.DES");
			}
		} catch (Exception e) {
			fail("Unexpected exception finding cipher : " + e);
		}

		// Exception case
		try {
			Cipher.getInstance("DES", (String) null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// Expected
		} catch (Exception e) {
			fail("Expected an IllegalArgumentException but got a " + e);
		}

		// Exception case
		try {
			Cipher.getInstance("DES", "IHaveNotBeenConfigured");
			fail("Should have thrown an NoSuchProviderException");
		} catch (NoSuchProviderException e) {
			// Expected
		} catch (Exception e) {
			fail("Expected an NoSuchProviderException but got a " + e);
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getInstance(java.lang.String,
	 *        java.security.Provider)
	 */
	public void test_getInstanceLjava_lang_StringLjava_security_Provider() {
		try {
			Provider[] providers = Security.getProviders("Cipher.DES");
			if (providers != null) {
				for (int i = 0; i < providers.length; i++) {
					Cipher cipher = Cipher.getInstance("DES", providers[i]);
					assertNotNull("Cipher.getInstance() returned a null value",
							cipher);
				}// end for
			} else {
				fail("No installed providers support Cipher.DES");
			}
		} catch (Exception e) {
			fail("Unexpected exception finding cipher : " + e);
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getProvider()
	 */
	public void test_getProvider() {
		try {
			Provider[] providers = Security.getProviders("Cipher.AES");
			if (providers != null) {
				for (int i = 0; i < providers.length; i++) {
					Provider provider = providers[i];
					Cipher cipher = Cipher.getInstance("AES", provider
							.getName());
					Provider cipherProvider = cipher.getProvider();
					assertTrue("Cipher provider is not the same as that "
							+ "provided as parameter to getInstance()",
							cipherProvider.equals(provider));
				}// end for
			} else {
				fail("No providers support Cipher.AES");
			}
		} catch (Exception e) {
			fail("Unexpected exception " + e);
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getAlgorithm()
	 */
	public void test_getAlgorithm() {
		final String algorithm = "DESede/CBC/PKCS5Padding";
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			assertTrue("Cipher algorithm does not match", cipher.getAlgorithm()
					.equals(algorithm));
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getBlockSize()
	 */
	public void test_getBlockSize() {
		final String algorithm = "DESede/CBC/PKCS5Padding";
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			assertTrue("Block size does not match", cipher.getBlockSize() == 8);
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	/**
	 * @tests javax.crypto.Cipher#getOutputSize(int)
	 */
	public void test_getOutputSizeI() {
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);
		} catch (Exception e) {
			fail("Setup failed");
		}

		// A 25-byte input could result in at least 4 8-byte blocks
		int result = cipher.getOutputSize(25);
		assertTrue("Output size too small", result > 31);

		// A 8-byte input should result in 2 8-byte blocks
		result = cipher.getOutputSize(8);
		assertTrue("Output size too small", result > 15);
	}

	/**
	 * @tests javax.crypto.Cipher#getIV()
	 * @tests javax.crypto.Cipher#init(int, java.security.Key,
	 *        java.security.AlgorithmParameters)
	 */
	public void test_getIV() {
		/*
		 * If this test is changed, implement the following:
		 * test_initILjava_security_KeyLjava_security_AlgorithmParameters()
		 */
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		byte[] iv = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Unexpected NoSuchAlgorithmException : " + e);
			}

			cipherKey = kg.generateKey();

			iv = new byte[8];
			sr.nextBytes(iv);
			AlgorithmParameters ap = AlgorithmParameters.getInstance(algorithm);
			ap.init(iv, "RAW");

			cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap);

		} catch (Exception e) {
			fail("Setup error");
		}

		byte[] cipherIV = cipher.getIV();

		assertTrue("IVs differ", bytesArraysAreEqual(cipherIV, iv));
	}

	/**
	 * @tests javax.crypto.Cipher#getParameters()
	 * @tests javax.crypto.Cipher#init(int, java.security.Key,
	 *        java.security.AlgorithmParameters, java.security.SecureRandom)
	 */
	public void test_getParameters() {

		/*
		 * If this test is changed, implement the following:
		 * test_initILjava_security_KeyLjava_security_AlgorithmParametersLjava_security_SecureRandom()
		 */
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		byte[] apEncoding = null;

		byte[] iv = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			iv = new byte[8];
			sr.nextBytes(iv);

			AlgorithmParameters ap = AlgorithmParameters.getInstance("DESede");
			ap.init(iv, "RAW");
			apEncoding = ap.getEncoded("ASN.1");

			cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap, sr);

		} catch (Exception e) {
			fail("Setup error");
		}

		try {
			byte[] cipherParmsEnc = cipher.getParameters().getEncoded("ASN.1");
			assertTrue("Parameters differ", bytesArraysAreEqual(apEncoding,
					cipherParmsEnc));
		} catch (IOException e) {
			fail("Parameter encoding problem");
		}

	}

	/**
	 * @tests javax.crypto.Cipher#init(int, java.security.Key)
	 */
	public void test_initILjava_security_Key() {
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		Cipher cipher = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");

		} catch (Exception e) {
			fail("Setup error");
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}

	/**
	 * @tests javax.crypto.Cipher#init(int, java.security.Key,
	 *        java.security.SecureRandom)
	 */
	public void test_initILjava_security_KeyLjava_security_SecureRandom() {
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");

		} catch (Exception e) {
			fail("Setup error");
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);
		} catch (Exception e) {
			fail("Unexpected exception : " + e);
		}
	}

	/**
	 * @tests javax.crypto.Cipher#init(int, java.security.Key,
	 *        java.security.spec.AlgorithmParameterSpec)
	 */
	public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpec() {
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		byte[] iv = null;
		AlgorithmParameterSpec ivAVP = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			iv = new byte[8];
			sr.nextBytes(iv);
			ivAVP = new IvParameterSpec(iv);

			cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
		} catch (Exception e) {
			fail("Setup error");
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivAVP);
		} catch (Exception e) {
			fail("Unexpected exception : " + e);
		}

		byte[] cipherIV = cipher.getIV();

		assertTrue("IVs differ", bytesArraysAreEqual(cipherIV, iv));
	}

	/**
	 * @tests javax.crypto.Cipher#init(int, java.security.Key,
	 *        java.security.spec.AlgorithmParameterSpec,
	 *        java.security.SecureRandom)
	 */
	public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom() {
		final String algorithm = "DESede";
		final int keyLen = 168;

		Key cipherKey = null;
		SecureRandom sr = new SecureRandom();
		Cipher cipher = null;

		byte[] iv = null;
		AlgorithmParameterSpec ivAVP = null;

		try {
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance(algorithm);
				kg.init(keyLen, new SecureRandom());
			} catch (NoSuchAlgorithmException e) {
				fail("Caught a NoSuchAlgorithmException : " + e);
			}
			cipherKey = kg.generateKey();

			iv = new byte[8];
			sr.nextBytes(iv);
			ivAVP = new IvParameterSpec(iv);

			cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
		} catch (Exception e) {
			fail("Setup error");
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivAVP, sr);
		} catch (Exception e) {
			fail("Unexpected exception : " + e);
		}

		byte[] cipherIV = cipher.getIV();

		assertTrue("IVs differ", bytesArraysAreEqual(cipherIV, iv));
	}

	/**
	 * @tests javax.crypto.Cipher#update(byte[], int, int)
	 */
	public void test_update$BII() throws Exception {
		for (int index = 1; index < 4; index++) {
			Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");

			byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".key");
			DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
			Key k = skf.generateSecret(keySpec);

			byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".iv");
			IvParameterSpec iv = new IvParameterSpec(ivMaterial);

			c.init(Cipher.DECRYPT_MODE, k, iv);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] input = new byte[256];
			String resPath = "hyts_" + "des-ede3-cbc.test" + index
					+ ".ciphertext";
			InputStream is = Support_Resources.getStream(resPath);

			int bytesRead = is.read(input, 0, 256);
			while (bytesRead > 0) {
				byte[] output = c.update(input, 0, bytesRead);
				if (output != null) {
					baos.write(output);
				}
				bytesRead = is.read(input, 0, 256);
			}

			byte[] output = c.doFinal();
			if (output != null) {
				baos.write(output);
			}

			byte[] decipheredCipherText = baos.toByteArray();
			is.close();

			byte[] plaintextBytes = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".plaintext");
			if (bytesArraysAreEqual(plaintextBytes, decipheredCipherText) == false) {
				fail("Operation produced incorrect results");
			}
		}// end for
	}

	/**
	 * @tests javax.crypto.Cipher#doFinal()
	 */
	public void test_doFinal() throws Exception {
		for (int index = 1; index < 4; index++) {
			Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");

			byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".key");
			DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
			Key k = skf.generateSecret(keySpec);

			byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".iv");
			IvParameterSpec iv = new IvParameterSpec(ivMaterial);

			c.init(Cipher.ENCRYPT_MODE, k, iv);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] input = new byte[256];
			String resPath = "hyts_" + "des-ede3-cbc.test" + index
					+ ".plaintext";
			InputStream is = Support_Resources.getStream(resPath);

			int bytesRead = is.read(input, 0, 256);
			while (bytesRead > 0) {
				byte[] output = c.update(input, 0, bytesRead);
				if (output != null) {
					baos.write(output);
				}
				bytesRead = is.read(input, 0, 256);
			}
			byte[] output = c.doFinal();
			if (output != null) {
				baos.write(output);
			}
			byte[] encryptedPlaintext = baos.toByteArray();
			is.close();

			byte[] cipherText = loadBytes("hyts_" + "des-ede3-cbc.test"
					+ index + ".cipherText");
			if (!bytesArraysAreEqual(encryptedPlaintext, cipherText)) {
				fail("Operation produced incorrect results");
			}
		}// end for
	}

	private byte[] loadBytes(String resPath) {
		try {
			InputStream is = Support_Resources.getStream(resPath);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int readlen;
			while ((readlen = is.read(buff)) > 0) {
				out.write(buff, 0, readlen);
			}
			is.close();
			return out.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	private boolean bytesArraysAreEqual(byte[] arr1, byte[] arr2) {
		if (arr1.length != arr2.length) {
			return false;
		}

		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}
		return true;
	}
}