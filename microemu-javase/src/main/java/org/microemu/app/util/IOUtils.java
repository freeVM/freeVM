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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * General IO stream manipulation utilities.
 * Some functions are based on org.apache.commons.io
 * 
 * <p>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li>closeQuietly - these methods close a stream ignoring nulls and exceptions
 * </ul>
 * <p>
 */

public class IOUtils {

	/**
	 * Solution for JVM bug 
	 *  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6351751
	 */
	public static String getCanonicalFileURL(File file) {
		String path = file.getAbsoluteFile().getPath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}
		// Not network path
		if (!path.startsWith("//")) {
			if (path.startsWith("/")) {
				path = "//" + path;
			} else {
				path = "///" + path;
			}
		}
		return "file:" + path;
	}
	
	public static String getCanonicalFileClassLoaderURL(File file) {
		String url = getCanonicalFileURL(file);
		if ((file.isDirectory()) && (!url.endsWith("/"))) {
			url += "/";
		}
		return url;
	}
	
	public static void copyFile(File src, File dst) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(src);
			copyToFile(fis, dst);
		} finally {
			closeQuietly(fis); 
		}
	}
	
	public static void copyToFile(InputStream is, File dst) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dst);
			byte[] buf = new byte[1024]; 
			int i = 0;
			while ((i = is.read(buf)) != -1) { 
				fos.write(buf, 0, i);
			}
		} finally {
			closeQuietly(fos);	
		}
	}
	
    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignore) {
            // ignore
        }
    }
    
    public static void closeQuietly(OutputStream output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ignore) {
            // ignore
        }
    }
    
    public static void closeQuietly(Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
