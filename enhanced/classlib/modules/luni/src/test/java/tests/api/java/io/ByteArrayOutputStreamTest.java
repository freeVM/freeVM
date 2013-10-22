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

package tests.api.java.io;

/**
 * Automated Test Suite for class java.io.ByteArrayOutputStream
 * 
 * @see java.io.ByteArrayOutputStream
 */

public class ByteArrayOutputStreamTest extends junit.framework.TestCase {

	java.io.ByteArrayOutputStream bos = null;

	public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			bos.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#ByteArrayOutputStream(int)
	 */
	public void test_ConstructorI() {
		// Test for method java.io.ByteArrayOutputStream(int)
		bos = new java.io.ByteArrayOutputStream(100);
		assertTrue("Failed to create stream", bos.size() == 0);
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#ByteArrayOutputStream()
	 */
	public void test_Constructor() {
		// Test for method java.io.ByteArrayOutputStream()
		bos = new java.io.ByteArrayOutputStream();
		assertTrue("Failed to create stream", bos.size() == 0);
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#close()
	 */
	public void test_close() {
		// Test for method void java.io.ByteArrayOutputStream.close()

		assertTrue(
				"close() does nothing for this implementation of OutputSteam",
				true);

		// The spec seems to say that a closed output stream can't be written
		// to. We don't throw an exception if attempt is made to write.
		// Right now our implementation doesn't do anything testable but
		// should we decide to throw an exception if a closed stream is
		// written to, the appropriate test is commented out below.

		/***********************************************************************
		 * java.io.ByteArrayOutputStream bos = new
		 * java.io.ByteArrayOutputStream(); bos.write (fileString.getBytes(), 0,
		 * 100); try { bos.close(); } catch (java.io.IOException e) {
		 * fail("IOException closing stream"); } try { bos.write
		 * (fileString.getBytes(), 0, 100); bos.toByteArray(); fail("Wrote
		 * to closed stream"); } catch (Exception e) { }
		 **********************************************************************/
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#reset()
	 */
	public void test_reset() {
		// Test for method void java.io.ByteArrayOutputStream.reset()
		bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, 100);
		bos.reset();
		assertTrue("reset failed", bos.size() == 0);
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#size()
	 */
	public void test_size() {
		// Test for method int java.io.ByteArrayOutputStream.size()
		bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, 100);
		assertTrue("size test failed", bos.size() == 100);
		bos.reset();
		assertTrue("size test failed", bos.size() == 0);
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#toByteArray()
	 */
	public void test_toByteArray() {
		// Test for method byte [] java.io.ByteArrayOutputStream.toByteArray()
		byte[] bytes;
		byte[] sbytes = fileString.getBytes();
		bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, fileString.length());
		bytes = bos.toByteArray();
		for (int i = 0; i < fileString.length(); i++)
			assertTrue("Error in byte array", bytes[i] == sbytes[i]);
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#toString(java.lang.String)
	 */
	public void test_toStringLjava_lang_String() {
		// Test for method java.lang.String
		// java.io.ByteArrayOutputStream.toString(java.lang.String)
		java.io.ByteArrayOutputStream bos;
		bos = new java.io.ByteArrayOutputStream();
		try {
			bos.write(fileString.getBytes(), 0, fileString.length());
			assertTrue("Returned incorrect 8859-1 String", bos.toString(
					"8859_1").equals(fileString));
		} catch (java.io.UnsupportedEncodingException e) {
			fail(
					"Threw an UnsupportedEncodingException for ISO 8859-1 encoding");
		}
		bos = new java.io.ByteArrayOutputStream();
		try {
			bos.write(fileString.getBytes(), 0, fileString.length());
			assertTrue("Returned incorrect 8859-2 String", bos.toString(
					"8859_2").equals(fileString));
		} catch (java.io.UnsupportedEncodingException e) {
			fail(
					"Threw an UnsupportedEncodingException for 8859-2 encoding");
		}
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String
		// java.io.ByteArrayOutputStream.toString()
		java.io.ByteArrayOutputStream bos = null;
		bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, fileString.length());
		assertTrue("Returned incorrect String", bos.toString().equals(
				fileString));
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#toString(int)
	 */
	public void test_toStringI() {
		// Test for method java.lang.String
		// java.io.ByteArrayOutputStream.toString(int)
		java.io.ByteArrayOutputStream bos = null;
		bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, fileString.length());
		assertTrue("Returned incorrect String",
				bos.toString(5).length() == fileString.length());
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.ByteArrayOutputStream.write(int)
		bos = new java.io.ByteArrayOutputStream();
		bos.write('t');
		byte[] result = bos.toByteArray();
		assertTrue("Wrote incorrect bytes",
				new String(result, 0, result.length).equals("t"));
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#write(byte[], int, int)
	 */
	public void test_write$BII() {
		// Test for method void java.io.ByteArrayOutputStream.write(byte [],
		// int, int)
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, 100);
		byte[] result = bos.toByteArray();
		assertTrue("Wrote incorrect bytes",
				new String(result, 0, result.length).equals(fileString
						.substring(0, 100)));
	}

	/**
	 * @tests java.io.ByteArrayOutputStream#writeTo(java.io.OutputStream)
	 */
	public void test_writeToLjava_io_OutputStream() {
		// Test for method void
		// java.io.ByteArrayOutputStream.writeTo(java.io.OutputStream)
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
		bos.write(fileString.getBytes(), 0, 100);
		try {
			bos.writeTo(bos2);
		} catch (java.io.IOException e) {
			fail("Threw IOException during writeTo : " + e.getMessage());
		}
		assertTrue("Returned incorrect String", bos2.toString().equals(
				fileString.substring(0, 100)));
	}
}
