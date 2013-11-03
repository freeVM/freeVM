/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.app.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.microemu.app.classloader.MIDletClassLoaderTest;

import junit.framework.TestCase;

public class IOUtilsTest extends TestCase {

	private static final String TEST_JAR = MIDletClassLoaderTest.TEST_APP_JAR; 

	public void testCanonicalFileURL() throws Exception {
		
		ClassLoader parent = IOUtilsTest.class.getClassLoader();
		
		URL jarURL = parent.getResource(TEST_JAR);
		assertNotNull("Can't find jar", jarURL);
		
		File file = new File(jarURL.getPath());
		
		assertTrue("is real file",  file.canRead());
		
		String urlString = IOUtils.getCanonicalFileURL(file);
		System.out.println("local file url [" + urlString + "]");
		URL testURL = new URL(urlString);
		
		InputStream is = testURL.openStream();
		assertNotNull("Can't openStream jar", is);
		IOUtils.closeQuietly(is);
		
		URLClassLoader ucl = new URLClassLoader(new URL[]{testURL});
		
		final String testFile = "META-INF/MANIFEST.MF";
		
		is = null;
		try {
			is = ucl.getResourceAsStream(testFile);
			assertNotNull("URLClassLoader", is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	
	/**
	 * This manual tests. Just remove x_ to test on your system
	 * @throws Exception
	 */
	public void x_testCanonicalNetworkFileURL() throws Exception {
	
		File file;
		
		/*  tested on Windows */
		//file = new File("//vladsdesk/test/dir/" + TEST_JAR);
		/*  tested on Windows */
		//file = new File("//linux/vlads/test/dir/" + TEST_JAR);
		
		// Can't make this work on Linux connectin to XP!
		//file = new File("//vladsdesk/test/dir/" + TEST_JAR);
		//file = new File("//linux/vlads/test/dir/" + TEST_JAR);
		file = new File("//localhost/home/vlads/test/dir/" + TEST_JAR);
		
		assertTrue("is real file",  file.canRead());
		
		String urlString = IOUtils.getCanonicalFileURL(file);
		
		System.out.println("network URL [" + urlString + "]");
		
		URL testURL = new URL(urlString);
		
		InputStream is = testURL.openStream();
		assertNotNull("Can't openStream jar", is);
		IOUtils.closeQuietly(is);
		
		URLClassLoader ucl = new URLClassLoader(new URL[]{testURL});
		
		final String testFile = "META-INF/MANIFEST.MF";
		
		is = null;
		try {
			is = ucl.getResourceAsStream(testFile);
			assertNotNull("URLClassLoader", is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
