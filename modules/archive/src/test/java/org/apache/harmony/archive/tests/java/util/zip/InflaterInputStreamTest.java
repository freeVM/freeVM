/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.harmony.archive.tests.java.util.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class InflaterInputStreamTest extends TestCase {

	// files hyts_constru(O),hyts_constru(OD),hyts_constru(ODI) needs to be
	// included as resources
	byte outPutBuf[] = new byte[500];

	class MyInflaterInputStream extends InflaterInputStream {
		MyInflaterInputStream(InputStream in) {
			super(in);
		}

		MyInflaterInputStream(InputStream in, Inflater infl) {
			super(in, infl);
		}

		MyInflaterInputStream(InputStream in, Inflater infl, int size) {
			super(in, infl, size);
		}

		void myFill() throws IOException {
			fill();
		}
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream)
	 */
	public void test_ConstructorLjava_io_InputStream() throws IOException {
		int result = 0;
		int buffer[] = new int[500];
		InputStream infile = Support_Resources
				.getStream("hyts_constru(O).txt");

		InflaterInputStream inflatIP = new InflaterInputStream(infile);

		int i = 0;
		while ((result = inflatIP.read()) != -1) {
			buffer[i] = result;
			i++;
		}
		inflatIP.close();
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream,
	 *        java.util.zip.Inflater)
	 */
	public void test_ConstructorLjava_io_InputStreamLjava_util_zip_Inflater() throws IOException {
		byte byteArray[] = new byte[100];
		InputStream infile = Support_Resources.getStream("hyts_constru(OD).txt");
		Inflater inflate = new Inflater();
		InflaterInputStream inflatIP = new InflaterInputStream(infile,
				inflate);

		inflatIP.read(byteArray, 0, 5);// ony suppose to read in 5 bytes
		inflatIP.close();
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream,
	 *        java.util.zip.Inflater, int)
	 */
	public void test_ConstructorLjava_io_InputStreamLjava_util_zip_InflaterI() throws IOException {
		int result = 0;
		int buffer[] = new int[500];
		InputStream infile = Support_Resources.getStream("hyts_constru(ODI).txt");
		Inflater inflate = new Inflater();
		InflaterInputStream inflatIP = new InflaterInputStream(infile,
				inflate, 1);

		int i = 0;
		while ((result = inflatIP.read()) != -1) {
			buffer[i] = result;
			i++;
		}
		inflatIP.close();
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#read()
	 */
	public void test_read() throws IOException {
		int result = 0;
		int buffer[] = new int[500];
		byte orgBuffer[] = { 1, 3, 4, 7, 8 };
		InputStream infile = Support_Resources
				.getStream("hyts_constru(OD).txt");
		Inflater inflate = new Inflater();
		InflaterInputStream inflatIP = new InflaterInputStream(infile,
				inflate);

		int i = 0;
		while ((result = inflatIP.read()) != -1) {
			buffer[i] = result;
			i++;
		}
		inflatIP.close();

		for (int j = 0; j < orgBuffer.length; j++) {
			assertTrue(
				"orginal compressed data did not equal decompressed data",
				buffer[j] == orgBuffer[j]);
		}
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#read(byte[], int, int)
	 */
	public void test_read$BII() {
		// TODO
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#skip(long)
	 */
	public void test_skipJ() throws IOException {
		InputStream is = Support_Resources.getStream("hyts_available.tst");
		InflaterInputStream iis = new InflaterInputStream(is);

		// Tests for skipping a negative number of bytes.
		try {
			iis.skip(-3);
			fail("IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
            // Expected
		}
		assertEquals("Incorrect Byte Returned.", 5, iis.read());

		try {
			iis.skip(Integer.MIN_VALUE);
			fail("IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
            // Expected
		}
		assertEquals("Incorrect Byte Returned.", 4, iis.read());

		// Test to make sure the correct number of bytes were skipped
		assertEquals("Incorrect Number Of Bytes Skipped.", 3, iis.skip(3));

		// Test to see if the number of bytes skipped returned is true.
		assertEquals("Incorrect Byte Returned.", 7, iis.read());

		assertEquals("Incorrect Number Of Bytes Skipped.", 0, iis.skip(0));
		assertEquals("Incorrect Byte Returned.", 0, iis.read());

		// Test for skipping more bytes than available in the stream
		assertEquals("Incorrect Number Of Bytes Skipped.", 2, iis.skip(4));
		assertEquals("Incorrect Byte Returned.", -1, iis.read());
		iis.close();
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#skip(long)
	 */
	public void test_skipJ2() throws IOException {
		int result = 0;
		int buffer[] = new int[100];
		byte orgBuffer[] = { 1, 3, 4, 7, 8 };

        // testing for negative input to skip
		InputStream infile = Support_Resources
				.getStream("hyts_constru(OD).txt");
		Inflater inflate = new Inflater();
		InflaterInputStream inflatIP = new InflaterInputStream(infile,
				inflate, 10);
		long skip;
		try {
			skip = inflatIP.skip(Integer.MIN_VALUE);
			fail("Expected IllegalArgumentException when skip() is called with negative parameter");
		} catch (IllegalArgumentException e) {
            // Expected
		}
		inflatIP.close();

		// testing for number of bytes greater than input.
		InputStream infile2 = Support_Resources
				.getStream("hyts_constru(OD).txt");
		InflaterInputStream inflatIP2 = new InflaterInputStream(infile2);

		// looked at how many bytes the skip skipped. It is
		// 5 and its supposed to be the entire input stream.

		skip = inflatIP2.skip(Integer.MAX_VALUE);
		// System.out.println(skip);
		assertEquals("method skip() returned wrong number of bytes skiped",
				5, skip);

		// test for skiping of 2 bytes
		InputStream infile3 = Support_Resources
				.getStream("hyts_constru(OD).txt");
		InflaterInputStream inflatIP3 = new InflaterInputStream(infile3);
		skip = inflatIP3.skip(2);
		assertEquals("the number of bytes returned by skip did not correspond with its input parameters",
				2, skip);
		int i = 0;
		result = 0;
		while ((result = inflatIP3.read()) != -1) {
			buffer[i] = result;
			i++;
		}
		inflatIP2.close();

		for (int j = 2; j < orgBuffer.length; j++) {
			assertTrue(
				"orginal compressed data did not equal decompressed data",
				buffer[j - 2] == orgBuffer[j]);
		}
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#available()
	 */
	public void test_available() throws IOException {
		InputStream is = Support_Resources.getStream("hyts_available.tst");
		InflaterInputStream iis = new InflaterInputStream(is);

		int available;
		int read;
		for (int i = 0; i < 11; i++) {
			read = iis.read();
			available = iis.available();
			if (read == -1)
				assertEquals("Bytes Available Should Return 0 ",
						0, available);
			else
				assertEquals("Bytes Available Should Return 1.",
						1, available);
		}

		iis.close();
		try {
			iis.available();
			fail("available after close should throw IOException.");
		} catch (IOException e) {
            // Expected
		}
	}

	/**
	 * @tests java.util.zip.InflaterInputStream#close()
	 */
	public void test_close() throws IOException {
		InflaterInputStream iin = new InflaterInputStream(
				new ByteArrayInputStream(new byte[0]));
		iin.close();

        // test for exception
		iin.close();
	}
}
