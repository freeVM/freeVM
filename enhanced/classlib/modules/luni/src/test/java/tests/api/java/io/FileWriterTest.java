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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

public class FileWriterTest extends junit.framework.TestCase {

	FileWriter fw;

	FileInputStream fis;

	BufferedWriter bw;

	File f;

	FileOutputStream fos;

	BufferedReader br;

	/**
	 * @tests java.io.FileWriter#FileWriter(java.io.File)
	 */
	public void test_ConstructorLjava_io_File() {
		// Test for method java.io.FileWriter(java.io.File)
		try {
			fos = new FileOutputStream(f.getPath());
			fos.write("Test String".getBytes());
			fos.close();
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(" After test string", 0, 18);
			bw.close();
			br = new BufferedReader(new FileReader(f.getPath()));
			char[] buf = new char[100];
			int r = br.read(buf);
			br.close();
			assertTrue("Failed to write correct chars", new String(buf, 0, r)
					.equals(" After test string"));
		} catch (Exception e) {
			fail("Exception during Constructor test " + e.toString());
		}
	}

	/**
	 * @tests java.io.FileWriter#FileWriter(java.io.FileDescriptor)
	 */
	public void test_ConstructorLjava_io_FileDescriptor() {
		// Test for method java.io.FileWriter(java.io.FileDescriptor)
		try {
			fos = new FileOutputStream(f.getPath());
			fos.write("Test String".getBytes());
			fos.close();
			fis = new FileInputStream(f.getPath());
			br = new BufferedReader(new FileReader(fis.getFD()));
			char[] buf = new char[100];
			int r = br.read(buf);
			br.close();
			fis.close();
			assertTrue("Failed to write correct chars: "
					+ new String(buf, 0, r), new String(buf, 0, r)
					.equals("Test String"));
		} catch (Exception e) {
			fail("Exception during Constructor test " + e.toString());
		}
	}

	/**
	 * @tests java.io.FileWriter#FileWriter(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.FileWriter(java.lang.String)
		try {
			fos = new FileOutputStream(f.getPath());
			fos.write("Test String".getBytes());
			fos.close();
			bw = new BufferedWriter(new FileWriter(f.getPath()));
			bw.write(" After test string", 0, 18);
			bw.close();
			br = new BufferedReader(new FileReader(f.getPath()));
			char[] buf = new char[100];
			int r = br.read(buf);
			br.close();
			assertTrue("Failed to write correct chars", new String(buf, 0, r)
					.equals(" After test string"));
		} catch (Exception e) {
			fail("Exception during Constructor test " + e.toString());
		}
	}

	/**
	 * @tests java.io.FileWriter#FileWriter(java.lang.String, boolean)
	 */
	public void test_ConstructorLjava_lang_StringZ() {
		// Test for method java.io.FileWriter(java.lang.String, boolean)

		try {
			fos = new FileOutputStream(f.getPath());
			fos.write("Test String".getBytes());
			fos.close();
			bw = new BufferedWriter(new FileWriter(f.getPath(), true));
			bw.write(" After test string", 0, 18);
			bw.close();
			br = new BufferedReader(new FileReader(f.getPath()));
			char[] buf = new char[100];
			int r = br.read(buf);
			br.close();
			assertTrue("Failed to append to file", new String(buf, 0, r)
					.equals("Test String After test string"));

			fos = new FileOutputStream(f.getPath());
			fos.write("Test String".getBytes());
			fos.close();
			bw = new BufferedWriter(new FileWriter(f.getPath(), false));
			bw.write(" After test string", 0, 18);
			bw.close();
			br = new BufferedReader(new FileReader(f.getPath()));
			buf = new char[100];
			r = br.read(buf);
			br.close();
			assertTrue("Failed to overwrite file", new String(buf, 0, r)
					.equals(" After test string"));
		} catch (Exception e) {
			fail("Exception during Constructor test " + e.toString());
		}

	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		f = new File(System.getProperty("user.home"), "writer.tst");

		if (f.exists())
			if (!f.delete()) {
				fail("Unable to delete test file");
			}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			bw.close();
		} catch (Exception e) {
		}
		try {
			fis.close();
		} catch (Exception e) {
		}
		f.delete();
	}
}
