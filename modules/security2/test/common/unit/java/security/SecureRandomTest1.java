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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package java.security;

import org.apache.harmony.security.test.PerformanceTest;

/**
 * Tests for <code>SecureRandom</code> constructor and methods
 * 
 */
public class SecureRandomTest1 extends PerformanceTest {

	/**
	 * SRProvider
	 */
	Provider p;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		p = new SRProvider();
		Security.insertProviderAt(p, 1);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Security.removeProvider(p.getName());
	}

	public final void testNext() {
		SecureRandom sr = new SecureRandom();
		if (sr.next(1) != 1 || sr.next(2) != 3 || sr.next(3) != 7) {
			fail("next failed");			
		}
	}

	/*
	 * Class under test for void setSeed(long)
	 */
	public final void testSetSeedlong() {
		SecureRandom sr = new SecureRandom();
		sr.setSeed(12345);
		if (!RandomImpl.runEngineSetSeed) {
			fail("setSeed failed");
		}	
	}

	public final void testNextBytes() {
		byte[] b = new byte[5];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(b);
		for (int i = 0; i < b.length; i++) {
			if (b[i] != (byte)(i + 0xF1)) {
				fail("nextBytes failed");
			}
		}
	}

	/*
	 * Class under test for void SecureRandom()
	 */
	public final void testSecureRandom() {
		SecureRandom sr = new SecureRandom();
		if (!sr.getAlgorithm().equals("someRandom")  ||
				sr.getProvider()!= p) {
			fail("incorrect SecureRandom implementation" + p.getName());
		}	
	}

	/*
	 * Class under test for void SecureRandom(byte[])
	 */
	public final void testSecureRandombyteArray() {
		byte[] b = {1,2,3};
		SecureRandom sr = new SecureRandom(b);
		if (!RandomImpl.runEngineSetSeed) {
			fail("No setSeed");
		}
	}

	/*
	 * Class under test for SecureRandom getInstance(String)
	 */
	public final void testGetInstanceString() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("someRandom");	
		} catch (NoSuchAlgorithmException e) {
			fail(e.toString());
		}
		if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
			fail("getInstance failed");
		}	
	}

	/*
	 * Class under test for SecureRandom getInstance(String, String)
	 */
	public final void testGetInstanceStringString() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("someRandom", "SRProvider");	
		} catch (NoSuchAlgorithmException e) {
			fail(e.toString());
		} catch (NoSuchProviderException e) {
			fail(e.toString());
		}
		if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
			fail("getInstance failed");
		}	
	}

	/*
	 * Class under test for SecureRandom getInstance(String, Provider)
	 */
	public final void testGetInstanceStringProvider() {
		SecureRandom sr = null;
		Provider p = new SRProvider();
		try {
			sr = SecureRandom.getInstance("someRandom", p);	
		} catch (NoSuchAlgorithmException e) {
			fail(e.toString());
		}
		if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
			fail("getInstance failed");
		}	
	}

	/*
	 * Class under test for void setSeed(byte[])
	 */
	public final void testSetSeedbyteArray() {
		byte[] b = {1,2,3};
		SecureRandom sr = new SecureRandom();
		sr.setSeed(b);
		if (!RandomImpl.runEngineSetSeed) {
			fail("setSeed failed");
		}
	}

	public final void testGetSeed() {
		byte[] b = SecureRandom.getSeed(4);
		if( b.length != 4) {
			fail("getSeed failed");
		}
	}

	public final void testGenerateSeed() {
		SecureRandom sr = new SecureRandom();
		byte[] b = sr.generateSeed(4);
		for (int i = 0; i < b.length; i++) {
			if (b[i] != (byte)i) {
				fail("generateSeed failed");
			}
		}
	}
	
	class SRProvider extends Provider {
		public SRProvider() {
			super("SRProvider", 1.0, "SRProvider for testing");
			put("SecureRandom.someRandom", "java.security.RandomImpl");
		}
	}
}
