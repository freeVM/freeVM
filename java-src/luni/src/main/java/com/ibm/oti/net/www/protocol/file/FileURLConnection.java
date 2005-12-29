/* Copyright 1998, 2004 The Apache Software Foundation or its licensors, as applicable
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

package com.ibm.oti.net.www.protocol.file;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import com.ibm.oti.net.www.MimeTable;

/**
 * This subclass extends <code>URLConnection</code>.
 * <p>
 * This class is responsible for connecting, getting content and input stream of
 * the file.
 */
public class FileURLConnection extends URLConnection {

	String fileName;

	private InputStream is;

	private int length = -1;

	private boolean isDir = false;

	private FilePermission permission;

	/**
	 * Creates an instance of <code>FileURLConnection</code> for establishing
	 * a connection to the file pointed by this <code>URL<code>
	 *
	 * @param 		url 		The URL this connection is connected to
	 */
	public FileURLConnection(URL url) {
		super(url);
		if ((fileName = url.getFile()) == null)
			fileName = "";
		String host = url.getHost();
		if (host != null && host.length() > 0)
			fileName = "//" + host + fileName;
		fileName = com.ibm.oti.util.Util.decode(fileName, false);
	}

	/**
	 * This methods will attempt to obtain the input stream of the file pointed
	 * by this <code>URL</code>. If the file is a directory, it will return
	 * that directory listing as an input stream.
	 * 
	 * @throws IOException
	 *             if an IO error occurs while connecting
	 */
	public void connect() throws IOException {
		File f = new File(fileName);
		if (f.isDirectory()) {
			isDir = true;
			is = getDirectoryListing(f);
			// use -1 for the contentLength
		} else {
			is = new BufferedInputStream(new FileInputStream(f));
			length = is.available();
		}
		connected = true;
	}

	/**
	 * Answers the length of the file in bytes.
	 * 
	 * @return the length of the file
	 * 
	 * @see #getContentType()
	 */
	public int getContentLength() {
		try {
			if (!connected)
				connect();
		} catch (IOException e) {
			// default is -1
		}
		return length;
	}

	/**
	 * Answers the content type of the resource. Just takes a guess based on the
	 * name.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		try {
			if (!connected)
				connect();
		} catch (IOException e) {
			return MimeTable.UNKNOWN;
		}
		if (isDir)
			return "text/html";
		String result = guessContentTypeFromName(url.getFile());
		if (result == null)
			return MimeTable.UNKNOWN;
		return result;
	}

	/**
	 * Answers the directory listing of the file component as an input stream.
	 * 
	 * @return the input stream of the directory listing
	 */
	private InputStream getDirectoryListing(File f) {
		String fileList[] = f.list();
		ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
		PrintStream out = new PrintStream(bytes);
		out.print("<title>Directory Listing</title>\n");
		out.print("<base href=\"file:");
		out.print(f.getPath().replace('\\', '/') + "/\"><h1>" + f.getPath()
				+ "</h1>\n<hr>\n");
		int i;
		for (i = 0; i < fileList.length; i++)
			out.print(fileList[i] + "<br>\n");
		out.close();
		return new ByteArrayInputStream(bytes.toByteArray());
	}

	/**
	 * Answers the input stream of the object refered to by this
	 * <code>URLConnection</code>
	 * 
	 * File Sample : "/ZIP211/+/ibm/tools/javac/resources/javac.properties"
	 * Invalid File Sample: "/ZIP/+/ibm/tools/javac/resources/javac.properties"
	 * "ZIP211/+/ibm/tools/javac/resources/javac.properties"
	 * 
	 * @return input stream of the object
	 * 
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public InputStream getInputStream() throws IOException {
		if (!connected)
			connect();
		return is;
	}

	/**
	 * Answers the permission, in this case the subclass, FilePermission object
	 * which represents the permission necessary for this URLConnection to
	 * establish the connection.
	 * 
	 * @return the permission required for this URLConnection.
	 * 
	 * @exception IOException
	 *                if an IO exception occurs while creating the permission.
	 */
	public java.security.Permission getPermission() throws IOException {
		if (permission == null) {
			String path = fileName;
			if (File.separatorChar != '/')
				path = path.replace('/', File.separatorChar);
			permission = new FilePermission(path, new String("read"));
		}
		return permission;
	}
}
