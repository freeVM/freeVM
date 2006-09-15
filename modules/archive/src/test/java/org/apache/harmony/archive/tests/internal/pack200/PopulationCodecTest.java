/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, 
 *  as applicable.
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
package org.apache.harmony.archive.tests.internal.pack200;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.harmony.archive.internal.pack200.Codec;
import org.apache.harmony.archive.internal.pack200.Pack200Exception;
import org.apache.harmony.archive.internal.pack200.PopulationCodec;

public class PopulationCodecTest extends TestCase {
	public void testPopulationCodec() throws IOException, Pack200Exception {
		checkDecode(new byte[] { 4,5,6,4,2,1,3,0,7 }, new long[]{ 5,4,6,7 }, Codec.BYTE1);
		// Codec.SIGNED5 can be trivial for small n, because the encoding is 2n if even, 2n-1 if odd
		// Therefore they're left here to explain what the values are :-)
		checkDecode(new byte[] { 4*2,4*2-1,6*2,4*2,2*2,1*2,3*2,0,7*2 }, new long[]{ -4,4,6,7 }, Codec.SIGNED5);
		checkDecode(new byte[] { 4*2-1,4*2,6*2,4*2,2*2,1*2,3*2,0,7*2 }, new long[]{ 4,-4,6,7 }, Codec.SIGNED5);
		checkDecode(new byte[] { 1,1,1 }, new long[]{ 1 }, Codec.BYTE1);
		checkDecode(new byte[] { 2,2,1 }, new long[]{ 2 }, Codec.BYTE1);
		checkDecode(new byte[] { 1,1,2 }, new long[]{ -1 }, Codec.SIGNED5);
		checkDecode(new byte[] { 2,2,0,1,3 }, new long[]{ 3,2 }, Codec.BYTE1);
		checkDecode(new byte[] { 1,2,3,4,4,2,3,4,0,1 }, new long[]{ 2,3,4,1 }, Codec.BYTE1);
		checkDecode(new byte[] { 3,2,1,4,4,2,3,4,0,1 }, new long[]{ 2,1,4,1 }, Codec.BYTE1);
		checkDecode(new byte[] { 3,2,1,4,1,2,3,4,0,1 }, new long[]{ 2,1,4,1 }, Codec.BYTE1);
	}

	private void checkDecode(byte[] data, long[] expectedResult, Codec codec) throws IOException, Pack200Exception {
		InputStream in = new ByteArrayInputStream(data );
		
		long[] result = new PopulationCodec(codec,codec,codec).decode(expectedResult.length,in);
		assertEquals(expectedResult.length,result.length);
		for(int i=0;i<expectedResult.length;i++) {
			assertEquals(expectedResult[i],result[i]);
		}
		assertEquals(0,in.available());
	}

}
