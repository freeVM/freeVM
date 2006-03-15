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

import java.io.IOException;
import java.io.StringWriter;

public class StringWriterTest extends junit.framework.TestCase {

	StringWriter sw;

	/**
	 * @tests java.io.StringWriter#StringWriter()
	 */
	public void test_Constructor() {
		// Test for method java.io.StringWriter()
		assertTrue("Used in tests", true);
	}

	/**
	 * @tests java.io.StringWriter#close()
	 */
	public void test_close() {
		// Test for method void java.io.StringWriter.close()
		try {
			sw.close();
		} catch (IOException e) {
			fail("IOException closing StringWriter : " + e.getMessage());
		}
	}

	/**
	 * @tests java.io.StringWriter#flush()
	 */
	public void test_flush() {
		// Test for method void java.io.StringWriter.flush()
		sw.flush();
		sw.write('c');
		assertTrue("Failed to flush char", sw.toString().equals("c"));
	}

	/**
	 * @tests java.io.StringWriter#getBuffer()
	 */
	public void test_getBuffer() {
		// Test for method java.lang.StringBuffer
		// java.io.StringWriter.getBuffer()

		sw.write("This is a test string");
		StringBuffer sb = sw.getBuffer();
		assertTrue("Incorrect buffer returned", sb.toString().equals(
				"This is a test string"));
	}

	/**
	 * @tests java.io.StringWriter#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.io.StringWriter.toString()
		sw.write("This is a test string");
		assertTrue("Incorrect string returned", sw.toString().equals(
				"This is a test string"));
	}

	/**
	 * @tests java.io.StringWriter#write(char[], int, int)
	 */
	public void test_write$CII() {
		// Test for method void java.io.StringWriter.write(char [], int, int)
		char[] c = new char[1000];
		"This is a test string".getChars(0, 21, c, 0);
		sw.write(c, 0, 21);
		assertTrue("Chars not written properly", sw.toString().equals(
				"This is a test string"));
	}

	/**
	 * @tests java.io.StringWriter#write(int)
	 */
	public void test_writeI() {
		// Test for method void java.io.StringWriter.write(int)
		sw.write('c');
		assertTrue("Char not written properly", sw.toString().equals("c"));
	}

	/**
	 * @tests java.io.StringWriter#write(java.lang.String)
	 */
	public void test_writeLjava_lang_String() {
		// Test for method void java.io.StringWriter.write(java.lang.String)
		sw.write("This is a test string");
		assertTrue("String not written properly", sw.toString().equals(
				"This is a test string"));
	}

	/**
	 * @tests java.io.StringWriter#write(java.lang.String, int, int)
	 */
	public void test_writeLjava_lang_StringII() {
		// Test for method void java.io.StringWriter.write(java.lang.String,
		// int, int)
		sw.write("This is a test string", 2, 2);
		assertTrue("String not written properly", sw.toString().equals("is"));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		sw = new StringWriter();
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
