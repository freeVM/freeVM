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

import java.nio.ByteBuffer;

import junit.framework.TestCase;


/**
 * Tests for <code>MessageDigestSpi</code> constructor and methods
 * 
 */
public class MessageDigestSpiTest extends TestCase {

	public void testEngineGetDigestLength() {
		MyMessageDigest md = new MyMessageDigest();
		if (md.engineGetDigestLength() != 0) {
			fail("engineGetDigestLength failed");
		}
	}

	/*
	 * Class under test for void engineUpdate(ByteBuffer)
	 */
	public void testEngineUpdateByteBuffer() {
		MyMessageDigest md = new MyMessageDigest();
		byte[] b = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

		ByteBuffer buf = ByteBuffer.wrap(b,0,b.length);
		buf.get(b);
		int l = buf.limit();
		md.engineUpdate(buf);
		if (buf.limit() !=l || buf.position() != l) {
			fail("Case 1. Incorrect position");
		}
		
		buf = ByteBuffer.wrap(b,0,b.length);
		buf.get();
		buf.get();
		buf.get();
		md.engineUpdate(buf);
		if (buf.limit() !=l || buf.position() != l) {
			fail("Case 2. Incorrect position");
		}
	}

	/*
	 * Class under test for int engineDigest(byte[], int, int)
	 */
	public void testEngineDigestbyteArrayintint() {
		MyMessageDigest md = new MyMessageDigest();
		byte[] b = new byte[5];
		try {
			md.engineDigest(null, 1, 1);
			fail("No expected NullPointerException");	
		} catch (NullPointerException e) {		
		} catch (DigestException e) {
			fail(e.toString());
		}
		try {
			md.engineDigest(b, 3, 10);
			fail("No expected DigestException");	
		} catch (DigestException e) {
		}
		try {
			if (md.engineDigest(b, 1, 3) != 0) {
				fail("incorrect result");
			}
		} catch (DigestException e) {
			fail(e.toString());
		}
	}

	/*
	 * Class under test for Object clone()
	 */
	public void testClone() {
		MyMessageDigest md = new MyMessageDigest();
		try {
			md.clone();
			fail("No expected CloneNotSupportedException");
		} catch (CloneNotSupportedException e) {			
		}
	}

	private class MyMessageDigest extends MessageDigestSpi {
		
		public void engineReset() {}

		public byte[] engineDigest() {
			return null;
		}

		public void engineUpdate(byte arg0) {}

		public void engineUpdate(byte[] arg0, int arg1, int arg2) {}

		public Object clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException();
		}
	}
}
