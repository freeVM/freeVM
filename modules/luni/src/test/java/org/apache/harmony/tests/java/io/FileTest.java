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

package org.apache.harmony.tests.java.io;

import java.io.File;

import junit.framework.TestCase;

public class FileTest extends TestCase {

	/**
	 * @tests java.io.File#File(java.io.File, java.lang.String)
	 */
	public void test_ConstructorLjava_io_FileLjava_lang_String() {
		// Regression test for HARMONY-21
		File path = new File("/dir/file");
		File root = new File("/");
		File file = new File(root, "/dir/file");
		assertEquals("Assert 1: wrong path result ", path.getPath(), file
				.getPath());
		assertTrue("Assert 1.1: path not absolute ", new File("\\\\\\a\b").isAbsolute());
		
		// Test data used in a few places below
		String dirName = System.getProperty("user.dir");
		String fileName = "input.tst";

		// Check filename is preserved correctly
		File d = new File(dirName);
		File f = new File(d, fileName);
		if (!dirName
				.regionMatches((dirName.length() - 1), File.separator, 0, 1)) {
			dirName += File.separator;
		}
		dirName += fileName;
		assertTrue("Assert 2: Created incorrect file " + f.getPath(), f
				.getPath().equals(dirName));

		// Check null argument is handled
		try {
			f = new File(d, null);
			fail("Assert 3: NullPointerException not thrown.");
		} catch (NullPointerException e) {
			// Expected.
		}

		f = new File((File) null, fileName);
		assertTrue("Assert 4: Created incorrect file " + f.getPath(), f
				.getAbsolutePath().equals(dirName));
	}
}
