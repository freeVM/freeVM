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
package tests.api.java.util.zip;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tests.support.Support_PlatformFile;
import tests.support.resource.Support_Resources;

public class ZipFileTest extends junit.framework.TestCase {

	// the file hyts_zipFile.zip in setup must be included as a resource
	String tempFileName;

	private java.util.zip.ZipFile zfile;

	/**
	 * @tests java.util.zip.ZipFile#ZipFile(java.io.File)
	 */
	public void test_ConstructorLjava_io_File() {
		// Test for method java.util.zip.ZipFile(java.io.File)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.zip.ZipFile#ZipFile(java.io.File, int)
	 */
	public void test_ConstructorLjava_io_FileI() {
		try {
			zfile.close(); // about to reopen the same temp file
			File file = new File(tempFileName);
			ZipFile zip = new ZipFile(file, ZipFile.OPEN_DELETE
					| ZipFile.OPEN_READ);
			zip.close();
			assertTrue("Zip should not exist", !file.exists());
		} catch (IOException e) {
			fail("Unexpected exception: " + e);
		}
	}

	/**
	 * @tests java.util.zip.ZipFile#ZipFile(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.util.zip.ZipFile(java.lang.String)
		/*
		 * try { zfile = new java.util.zip.ZipFile(zipName); zfile.close(); }
		 * catch (java.io.IOException e) {fail( "Failed to construct
		 * ZipFile" );}
		 */
	}

	protected ZipEntry test_finalize1(ZipFile zip) {
		return zip.getEntry("File1.txt");
	}

	protected ZipFile test_finalize2(File file) {
		try {
			return new ZipFile(file);
		} catch (IOException e) {
			fail("Unexpected exception: " + e);
		}
		return null;
	}

	/**
	 * @tests java.util.zip.ZipFile#finalize()
	 */
	public void test_finalize() {
		try {
			InputStream in = Support_Resources.getStream("hyts_ZipFile.zip");
			File file = Support_Resources.createTempFile(".jar");
			OutputStream out = new FileOutputStream(file);
			int result;
			byte[] buf = new byte[4096];
			while ((result = in.read(buf)) != -1)
				out.write(buf, 0, result);
			in.close();
			out.close();
			/*
			 * ZipFile zip = new ZipFile(file); ZipEntry entry1 =
			 * zip.getEntry("File1.txt"); assertTrue("Did not find entry",
			 * entry1 != null); entry1 = null; zip = null;
			 */

			assertTrue("Did not find entry",
					test_finalize1(test_finalize2(file)) != null);
			System.gc();
			System.gc();
			System.runFinalization();
			file.delete();
			assertTrue("Zip should not exist", !file.exists());
		} catch (IOException e) {
			fail("Unexpected exception: " + e);
		}
	}

	/**
	 * @tests java.util.zip.ZipFile#close()
	 */
	public void test_close() {
		// Test for method void java.util.zip.ZipFile.close()
		try {
			zfile.close();
			zfile.getInputStream(zfile.getEntry("ztest/file1.txt"));
		} catch (Exception e) {
			return;
		}
		fail("Close test failed");
	}

	/**
	 * @tests java.util.zip.ZipFile#entries()
	 */
	public void test_entries() {
		// Test for method java.util.Enumeration java.util.zip.ZipFile.entries()
		java.util.Enumeration enumer = zfile.entries();
		int c = 0;
		while (enumer.hasMoreElements()) {
			++c;
			enumer.nextElement();
		}
		assertTrue("Incorrect number of entries returned: " + c, c == 6);

		try {
			Enumeration enumeration = zfile.entries();
			zfile.close();
			zfile = null;
			boolean pass = false;
			try {
				enumeration.hasMoreElements();
			} catch (IllegalStateException e) {
				pass = true;
			}
			assertTrue("did not detect closed jar file", pass);
		} catch (Exception e) {
			fail("Exception during entries test: " + e.toString());
		}
	}

	/**
	 * @tests java.util.zip.ZipFile#getEntry(java.lang.String)
	 */
	public void test_getEntryLjava_lang_String() {
		// Test for method java.util.zip.ZipEntry
		// java.util.zip.ZipFile.getEntry(java.lang.String)
		java.util.zip.ZipEntry zentry = zfile.getEntry("File1.txt");
		assertTrue("Could not obtain ZipEntry", zentry != null);

		zentry = zfile.getEntry("testdir1/File1.txt");
		assertTrue("Could not obtain ZipEntry: testdir1/File1.txt",
				zentry != null);
		try {
			int r;
			InputStream in;
			zentry = zfile.getEntry("testdir1/");
			assertTrue("Could not obtain ZipEntry: testdir1/", zentry != null);
			in = zfile.getInputStream(zentry);
			assertTrue("testdir1/ should not have null input stream",
					in != null);
			r = in.read();
			in.close();
			assertTrue("testdir1/ should not contain data", r == -1);

			zentry = zfile.getEntry("testdir1");
			assertTrue("Could not obtain ZipEntry: testdir1", zentry != null);
			in = zfile.getInputStream(zentry);
			assertTrue("testdir1 should not have null input stream", in != null);
			r = in.read();
			in.close();
			assertTrue("testdir1 should not contain data", r == -1);

			zentry = zfile.getEntry("testdir1/testdir1");
			assertTrue("Could not obtain ZipEntry: testdir1/testdir1",
					zentry != null);
			in = zfile.getInputStream(zentry);
			byte[] buf = new byte[256];
			r = in.read(buf);
			in.close();
			assertTrue("incorrect contents", new String(buf, 0, r)
					.equals("This is also text"));
		} catch (IOException e) {
			fail("Unexpected: " + e);
		}
	}

	/**
	 * @tests java.util.zip.ZipFile#getInputStream(java.util.zip.ZipEntry)
	 */
	public void test_getInputStreamLjava_util_zip_ZipEntry() {
		// Test for method java.io.InputStream
		// java.util.zip.ZipFile.getInputStream(java.util.zip.ZipEntry)
		java.io.InputStream is = null;
		try {
			java.util.zip.ZipEntry zentry = zfile.getEntry("File1.txt");
			is = zfile.getInputStream(zentry);
			byte[] rbuf = new byte[1000];
			int r;
			is.read(rbuf, 0, r = (int) zentry.getSize());
			assertTrue("getInputStream read incorrect data", new String(rbuf,
					0, r).equals("This is text"));
		} catch (java.io.IOException e) {
			fail("IOException during getInputStream");
		} finally {
			try {
				is.close();
			} catch (java.io.IOException e) {
				fail("Failed to close input stream");
			}
		}
	}

	/**
	 * @tests java.util.zip.ZipFile#getName()
	 */
	public void test_getName() {
		// Test for method java.lang.String java.util.zip.ZipFile.getName()
		assertTrue("Returned incorrect name: " + zfile.getName(), zfile
				.getName().equals(tempFileName));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		try {
			byte[] rbuf = new byte[2000];
			// Create a local copy of the file since some tests want to alter
			// information.
			tempFileName = System.getProperty("user.dir");
			String separator = System.getProperty("file.separator");
			if (tempFileName.charAt(tempFileName.length() - 1) == separator
					.charAt(0))
				tempFileName = Support_PlatformFile.getNewPlatformFile(
						tempFileName, "gabba.zip");
			else
				tempFileName = Support_PlatformFile.getNewPlatformFile(
						tempFileName + separator, "gabba.zip");

			File f = new File(tempFileName);
			f.delete();
			InputStream is = Support_Resources.getStream("hyts_ZipFile.zip");
			FileOutputStream fos = new FileOutputStream(f);
			rbuf = new byte[(int) is.available()];
			is.read(rbuf, 0, rbuf.length);
			fos.write(rbuf, 0, rbuf.length);
			is.close();
			fos.close();
			zfile = new ZipFile(f);
		} catch (Exception e) {
			System.out.println("Exception during ZipFile setup:");
			e.printStackTrace();
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		try {
			if (zfile != null)
				// Note zfile is a user-defined zip file used by other tests and
				// should not be deleted
				zfile.close();
		} catch (Exception e) {
		}
	}

}
