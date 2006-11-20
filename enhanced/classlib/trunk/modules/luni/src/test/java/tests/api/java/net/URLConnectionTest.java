/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.net;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import tests.support.Support_Configuration;
import tests.support.Support_HttpServer;
import tests.support.Support_HttpServerSocket;
import tests.support.Support_HttpTests;
import tests.support.Support_PortManager;
import tests.support.Support_URLConnector;
import tests.support.resource.Support_Resources;

public class URLConnectionTest extends junit.framework.TestCase {

	URL url;

	URLConnection uc;

	/**
	 * @tests java.net.URLConnection#getAllowUserInteraction()
	 */
	public void test_getAllowUserInteraction() {
		uc.setAllowUserInteraction(false);
		assertTrue("getAllowUserInteraction should have returned false", !uc
				.getAllowUserInteraction());
		uc.setAllowUserInteraction(true);
		assertTrue("getAllowUserInteraction should have returned true", uc
				.getAllowUserInteraction());
	}

	/**
	 * @tests java.net.URLConnection#getContent()
	 */
	public void test_getContent() {
		try {
			byte[] ba = new byte[600];
			((InputStream) uc.getContent()).read(ba, 0, 600);
			String s = new String(ba);
			assertTrue("Incorrect content returned", s.indexOf("20060107") > 0);
		} catch (Exception e) {
			fail("Exception during test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.net.URLConnection#getContentEncoding()
	 */
	public void test_getContentEncoding() {
		// should not be known for a file
		assertNull("getContentEncoding failed: " + uc.getContentEncoding(), uc
				.getContentEncoding());
	}

	/**
	 * @tests java.net.URLConnection#getContentLength()
	 */
	public void test_getContentLength() {
		assertTrue("getContentLength failed: " + uc.getContentLength(), uc
				.getContentLength() == 943);
	}

	/**
	 * @tests java.net.URLConnection#getContentType()
	 */
	public void test_getContentType() {
		// should not be known for a file
		assertTrue("getContentType failed: " + uc.getContentType(), uc
				.getContentType().equals("text/html"));

		try {
			File resources = Support_Resources.createTempFolder();
			Support_Resources.copyFile(resources, null, "Harmony.GIF");
			URL url = new URL("file:/" + resources.toString() + "/Harmony.GIF");
			URLConnection conn = url.openConnection();
			assertEquals("type not GIF", "image/gif", conn.getContentType()
					);
		} catch (MalformedURLException e) {
			fail("MalformedURLException for .gif");
		} catch (IOException e) {
			fail("IOException for .gif");
		}
	}

	/**
	 * @tests java.net.URLConnection#getDate()
	 */
	public void test_getDate() {
		// should be greater than 930000000000L which represents the past
		if (uc.getDate() == 0) {
			System.out
					.println("WARNING: server does not support 'Date', in test_getDate");
		} else {
			assertTrue("getDate gave wrong date: " + uc.getDate(),
					uc.getDate() > 930000000000L);
		}
	}

	/**
	 * @tests java.net.URLConnection#getDefaultAllowUserInteraction()
	 */
	public void test_getDefaultAllowUserInteraction() {
		boolean oldSetting = URLConnection.getDefaultAllowUserInteraction();
		URLConnection.setDefaultAllowUserInteraction(false);
		assertTrue("getDefaultAllowUserInteraction should have returned false",
				!URLConnection.getDefaultAllowUserInteraction());
		URLConnection.setDefaultAllowUserInteraction(true);
		assertTrue("getDefaultAllowUserInteraction should have returned true",
				URLConnection.getDefaultAllowUserInteraction());
		URLConnection.setDefaultAllowUserInteraction(oldSetting);
	}

	/**
	 * @tests java.net.URLConnection#getDefaultRequestProperty(java.lang.String)
	 */
	public void test_getDefaultRequestPropertyLjava_lang_String() {
		try {
			URLConnection.setDefaultRequestProperty("Shmoo", "Blah");
			assertNull(
					"setDefaultRequestProperty should have returned: null, but returned: "
							+ URLConnection.getDefaultRequestProperty("Shmoo"),
					URLConnection.getDefaultRequestProperty("Shmoo"));
			URLConnection.setDefaultRequestProperty("Shmoo", "Boom");
			assertNull(
					"setDefaultRequestProperty should have returned: null, but returned: "
							+ URLConnection.getDefaultRequestProperty("Shmoo"),
					URLConnection.getDefaultRequestProperty("Shmoo"));
			assertNull(
					"setDefaultRequestProperty should have returned: null, but returned: "
							+ URLConnection.getDefaultRequestProperty("Kapow"),
					URLConnection.getDefaultRequestProperty("Kapow"));
			URLConnection.setDefaultRequestProperty("Shmoo", null);
		} catch (Exception e) {
			fail("Exception during test : " + e.getMessage());
		}

	}

	/**
	 * @tests java.net.URLConnection#getDefaultUseCaches()
	 */
	public void test_getDefaultUseCaches() {
		boolean oldSetting = uc.getDefaultUseCaches();
		uc.setDefaultUseCaches(false);
		assertTrue("getDefaultUseCaches should have returned false", !uc
				.getDefaultUseCaches());
		uc.setDefaultUseCaches(true);
		assertTrue("getDefaultUseCaches should have returned true", uc
				.getDefaultUseCaches());
		uc.setDefaultUseCaches(oldSetting);
	}

	/**
	 * @tests java.net.URLConnection#getDoInput()
	 */
	public void test_getDoInput() {
		assertTrue("Should be set to true by default", uc.getDoInput());
		uc.setDoInput(true);
		assertTrue("Should have been set to true", uc.getDoInput());
		uc.setDoInput(false);
		assertTrue("Should have been set to false", !uc.getDoInput());
	}

	/**
	 * @tests java.net.URLConnection#getDoOutput()
	 */
	public void test_getDoOutput() {
		assertTrue("Should be set to false by default", !uc.getDoOutput());
		uc.setDoOutput(true);
		assertTrue("Should have been set to true", uc.getDoOutput());
		uc.setDoOutput(false);
		assertTrue("Should have been set to false", !uc.getDoOutput());
	}

	/**
	 * @tests java.net.URLConnection#getExpiration()
	 */
	public void test_getExpiration() {
		// should be unknown
		assertTrue("getExpiration returned wrong expiration: "
				+ uc.getExpiration(), uc.getExpiration() == 0);
	}

	/**
	 * @tests java.net.URLConnection#getFileNameMap()
	 */
	public void test_getFileNameMap() {
		URLConnection.setFileNameMap(new FileNameMap() {
			public String getContentTypeFor(String fileName) {
				return "Spam!";
			}
		});
		try {
			assertEquals("Incorrect FileNameMap returned", "Spam!", URLConnection.getFileNameMap()
					.getContentTypeFor(null));
		} finally {
			// unset the map so other tests don't fail
			URLConnection.setFileNameMap(null);
		}
	}

	/**
	 * @tests java.net.URLConnection#getHeaderField(int)
	 */
	public void test_getHeaderFieldI() {
		int i = 0;
		String hf;
		boolean foundResponse = false;
		while ((hf = uc.getHeaderField(i++)) != null) {
			if (hf.equals(Support_Configuration.HomeAddressSoftware))
				foundResponse = true;
		}
		assertTrue("Could not find header field containing \""
				+ Support_Configuration.HomeAddressSoftware + "\"",
				foundResponse);

		i = 0;
		foundResponse = false;
		while ((hf = uc.getHeaderField(i++)) != null) {
			if (hf.equals(Support_Configuration.HomeAddressResponse))
				foundResponse = true;
		}
		assertTrue("Could not find header field containing \""
				+ Support_Configuration.HomeAddressResponse + "\"",
				foundResponse);
	}

	/**
	 * @tests java.net.URLConnection#addRequestProperty(java.lang.String,java.lang.String)
	 */
	public void test_addRequestPropertyLjava_lang_StringLjava_lang_String() {
		uc.setRequestProperty("prop", "yo");
		uc.setRequestProperty("prop", "yo2");
		assertEquals("yo2", uc.getRequestProperty("prop"));
		Map map = uc.getRequestProperties();
		List props = (List) uc.getRequestProperties().get("prop");
		assertEquals(1, props.size());

		try {
			// the map should be unmodifiable
			map.put("hi", "bye");
			fail();
		} catch (UnsupportedOperationException e) {
		}
		try {
			// the list should be unmodifiable
			props.add("hi");
			fail();
		} catch (UnsupportedOperationException e) {
		}

		try {
			File resources = Support_Resources.createTempFolder();
			Support_Resources.copyFile(resources, null, "hyts_att.jar");
			URL fUrl1 = new URL("jar:file:" + resources.getPath()
					+ "/hyts_att.jar!/");
			JarURLConnection con1 = (JarURLConnection) fUrl1.openConnection();
			map = con1.getRequestProperties();
			assertNotNull(map);
			assertEquals(0, map.size());
			try {
				// the map should be unmodifiable
				map.put("hi", "bye");
				fail();
			} catch (UnsupportedOperationException e) {
			}
		} catch (IOException e) {
			fail();
		}
	}

	/**
	 * @tests java.net.URLConnection#getHeaderFields()
	 */
	public void test_getHeaderFields() {
		try {
			uc.getInputStream();
		} catch (IOException e) {
			fail();
		}

		Map headers = uc.getHeaderFields();
		assertNotNull(headers);

		// content-length should always appear
		List list = (List) headers.get("Content-Length");
		if (list == null) {
			list = (List) headers.get("content-length");
		}
		assertNotNull(list);
		String contentLength = (String) list.get(0);
		assertNotNull(contentLength);

		// there should be at least 2 headers
		assertTrue(headers.size() > 1);
		try {
			File resources = Support_Resources.createTempFolder();
			Support_Resources.copyFile(resources, null, "hyts_att.jar");
			URL fUrl1 = new URL("jar:file:" + resources.getPath()
					+ "/hyts_att.jar!/");
			JarURLConnection con1 = (JarURLConnection) fUrl1.openConnection();
			headers = con1.getHeaderFields();
			assertNotNull(headers);
			assertEquals(0, headers.size());
			try {
				// the map should be unmodifiable
				headers.put("hi", "bye");
				fail();
			} catch (UnsupportedOperationException e) {
			}
		} catch (IOException e) {
			fail();
		}
	}

	/**
	 * @tests java.net.URLConnection#getRequestProperties()
	 */
	public void test_getRequestProperties() {

		uc.setRequestProperty("whatever", "you like");
		Map headers = uc.getRequestProperties();

		// content-length should always appear
		List header = (List) headers.get("whatever");
		assertNotNull(header);

		assertEquals("you like", header.get(0));

		assertTrue(headers.size() >= 1);

		try {
			// the map should be unmodifiable
			headers.put("hi", "bye");
			fail();
		} catch (UnsupportedOperationException e) {
		}
		try {
			// the list should be unmodifiable
			header.add("hi");
			fail();
		} catch (UnsupportedOperationException e) {
		}

	}

	/**
	 * @tests java.net.URLConnection#getHeaderField(java.lang.String)
	 */
	public void test_getHeaderFieldLjava_lang_String() {
		String hf;
		hf = uc.getHeaderField("Content-Encoding");
		if (hf != null) {
			assertNull(
					"Wrong value returned for header field 'Content-Encoding': "
							+ hf, hf);
		}
		hf = uc.getHeaderField("Content-Length");
		if (hf != null) {
			assertTrue(
					"Wrong value returned for header field 'Content-Length': "
							+ hf, hf.equals("943"));
		}
		hf = uc.getHeaderField("Content-Type");
		if (hf != null) {
			assertTrue("Wrong value returned for header field 'Content-Type': "
					+ hf, hf.equals("text/html"));
		}
		hf = uc.getHeaderField("content-type");
		if (hf != null) {
			assertTrue("Wrong value returned for header field 'content-type': "
					+ hf, hf.equals("text/html"));
		}
		hf = uc.getHeaderField("Date");
		if (hf != null) {
			assertTrue("Wrong value returned for header field 'Date': " + hf,
					Integer.parseInt(hf.substring(hf.length() - 17,
							hf.length() - 13)) >= 1999);
		}
		hf = uc.getHeaderField("Expires");
		if (hf != null) {
			assertNull(
					"Wrong value returned for header field 'Expires': " + hf,
					hf);
		}
		hf = uc.getHeaderField("SERVER");
		if (hf != null) {
			assertTrue("Wrong value returned for header field 'SERVER': " + hf
					+ " (expected " + Support_Configuration.HomeAddressSoftware
					+ ")", hf.equals(Support_Configuration.HomeAddressSoftware));
		}
		hf = uc.getHeaderField("Last-Modified");
		if (hf != null) {
			assertTrue(
					"Wrong value returned for header field 'Last-Modified': "
							+ hf,
					hf
							.equals(Support_Configuration.URLConnectionLastModifiedString));
		}
		hf = uc.getHeaderField("accept-ranges");
		if (hf != null) {
			assertTrue(
					"Wrong value returned for header field 'accept-ranges': "
							+ hf, hf.equals("bytes"));
		}
		hf = uc.getHeaderField("DoesNotExist");
		if (hf != null) {
			assertNull("Wrong value returned for header field 'DoesNotExist': "
					+ hf, hf);
		}
	}

	/**
	 * @tests java.net.URLConnection#getHeaderFieldDate(java.lang.String, long)
	 */
	public void test_getHeaderFieldDateLjava_lang_StringJ() {

		if (uc.getHeaderFieldDate("Date", 22L) == 22L) {
			System.out
					.println("WARNING: Server does not support 'Date', test_getHeaderFieldDateLjava_lang_StringJ not run");
			return;
		}
		assertTrue("Wrong value returned: "
				+ uc.getHeaderFieldDate("Date", 22L), uc.getHeaderFieldDate(
				"Date", 22L) > 930000000000L);

		try {
			URL url = new URL(Support_Resources.getResourceURL("/RESOURCE.TXT"));
			URLConnection connection = url.openConnection();
			long time = connection.getHeaderFieldDate("Last-Modified", 0);
			assertTrue("Wrong date: " + time,
					time == Support_Configuration.URLConnectionDate);
		} catch (MalformedURLException e) {
			fail("MalformedURLException : " + e.getMessage());
		} catch (IOException e) {
			fail("IOException : " + e.getMessage());
		}
	}

	/**
	 * @tests java.net.URLConnection#getHeaderFieldKey(int)
	 */
	public void test_getHeaderFieldKeyI() {
		String hf;
		boolean foundResponse = false;
		for (int i = 0; i < 100; i++) {
			hf = uc.getHeaderFieldKey(i);
			if (hf != null && hf.toLowerCase().equals("content-type"))
				foundResponse = true;
		}
		assertTrue(
				"Could not find header field key containing \"content-type\"",
				foundResponse);
	}

	/**
	 * @tests java.net.URLConnection#getIfModifiedSince()
	 */
	public void test_getIfModifiedSince() {
		uc.setIfModifiedSince(200);
		assertEquals("Returned wrong ifModifiedSince value", 200, uc
				.getIfModifiedSince());
	}

	/**
	 * @tests java.net.URLConnection#getInputStream()
	 */
	public void test_getInputStream() {
		try {
			InputStream is = uc.getInputStream();
			byte[] ba = new byte[600];
			is.read(ba, 0, 600);
			is.close();
			String s = new String(ba);
			assertTrue("Incorrect input stream read", s.indexOf("20060107") > 0);

			boolean exception = false;
			try {
				is.available();
			} catch (IOException e) {
				exception = true;
			}
			assertTrue("available() after close() should cause IOException",
					exception);
		} catch (Exception e) {
			fail("Exception during test1 : " + e.getMessage());
		}

		try {
			// open an non-existent file
			URL url = new URL(Support_Resources.getResourceURL("/fred-zz6.txt"));
			InputStream is = url.openStream();
			assertTrue("available() less than 0", is.available() >= 0);
			is.close();
			fail("Error: data returned on opening a non-existent file.");
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			fail("Exception during test2: " + e);
		}

		// create a serversocket
		Support_HttpServerSocket serversocket = new Support_HttpServerSocket();

		// create a client connector
		Support_URLConnector client = new Support_URLConnector();

		// pass both to the HttpTest
		Support_HttpTests test = new Support_HttpTests(serversocket, client);

		// run various tests common to both HttpConnections and
		// HttpURLConnections
		test.runTests(this);

		// Authentication test is separate from other tests because it is only
		// in HttpURLConnection and not supported in HttpConnection

		serversocket = new Support_HttpServerSocket();
		Support_HttpServer server = new Support_HttpServer(serversocket, this);
		int p = Support_PortManager.getNextPort();
		server.startServer(p);

		// it is the Support_HttpServer's responsibility to close this
		// serversocket
		serversocket = null;

		final String authTestUrl = "http://localhost:" + server.getPort()
				+ Support_HttpServer.AUTHTEST;
		InputStream is;

		// Authentication test
		try {
			// set up a very simple authenticator
			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("test", "password"
							.toCharArray());
				}
			});
			try {
				client.open(authTestUrl);
				is = client.getInputStream();
				int c = is.read();
				while (c > 0)
					c = is.read();
				c = is.read();
				is.close();
			} catch (FileNotFoundException e) {
				fail("Error performing authentication test: " + e);
			}
		} catch (Exception e) {
			fail("Exception during test3: " + e);
			e.printStackTrace();
		}

		final String invalidLocation = "/missingFile.htm";
		final String redirectTestUrl = "http://localhost:" + server.getPort()
				+ Support_HttpServer.REDIRECTTEST;

		// test redirecting to a non-existent URL on the same host
		try {
			// append the response code for the server to return

			client.open(redirectTestUrl + "/" + Support_HttpServer.MOVED_PERM
					+ "-" + invalidLocation);
			is = client.getInputStream();

			int c = is.read();
			while (c > 0)
				c = is.read();
			c = is.read();
			is.close();
			fail("Incorrect data returned on redirect to non-existent file.");
		} catch (FileNotFoundException e) {
		} catch (Exception e) {

			e.printStackTrace();
			fail("Exception during test4: " + e);
		}
		server.stopServer();

	}

	/**
	 * @tests java.net.URLConnection#getLastModified()
	 */
	public void test_getLastModified() {
		if (uc.getLastModified() == 0) {
			System.out
					.println("WARNING: Server does not support 'Last-Modified', test_getLastModified() not run");
			return;
		}
		assertTrue(
				"Returned wrong getLastModified value.  Wanted: "
						+ Support_Configuration.URLConnectionLastModified
						+ " got: " + uc.getLastModified(),
				uc.getLastModified() == Support_Configuration.URLConnectionLastModified);
	}

	/**
	 * @tests java.net.URLConnection#getOutputStream()
	 */
	public void test_getOutputStream() {
		boolean exception = false;
		URL test;
		java.net.URLConnection conn2 = null;
		try {
			test = new URL("http://" + Support_Configuration.HomeAddress
					+ "/cgi-bin/test.pl");
			conn2 = (java.net.URLConnection) test.openConnection();
		} catch (IOException e) {
			fail("Unexpected I/O exception: " + e);
		}

		try {
			conn2.getOutputStream();
		} catch (java.net.ProtocolException e) {
			// correct
			exception = true;
		} catch (IOException e) {
			fail("Wrong kind of exception thrown : " + e);
		}
		assertTrue("Failed to throw ProtocolException", exception);

		try {
			conn2.setDoOutput(true);
			conn2.getOutputStream();
			conn2.connect();
			conn2.getOutputStream();
		} catch (IOException e) {
			fail("Unexpected IOException : " + e.getMessage());
		}

		exception = false;
		try {
			conn2.getInputStream();
			conn2.getOutputStream();
		} catch (ProtocolException e) {
			exception = true;
		} catch (IOException e) {
			e.printStackTrace();
			fail("Wrong exception thrown2: " + e);
		}
		assertTrue("Failed to throw ProtocolException2", exception);

		try {
			URL u = new URL("http://" + Support_Configuration.HomeAddress
					+ "/cgi-bin/test.pl");
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u
					.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			OutputStream out = conn.getOutputStream();
			String posted = "this is a test";
			out.write(posted.getBytes());
			out.close();
			conn.getResponseCode();
			InputStream is = conn.getInputStream();
			String response = "";
			byte[] b = new byte[1024];
			int count = 0;
			while ((count = is.read(b)) > 0)
				response += new String(b, 0, count);
			assertTrue("Response to POST method invalid 1", response
					.equals(posted));
		} catch (Exception e) {
			fail("Unexpected exception 1 : " + e.getMessage());
		}

		try {
			String posted = "just a test";
			URL u = new URL("http://" + Support_Configuration.HomeAddress
					+ "/cgi-bin/test.pl");
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u
					.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-length", String.valueOf(posted
					.length()));
			OutputStream out = conn.getOutputStream();
			out.write(posted.getBytes());
			out.close();
			conn.getResponseCode();
			InputStream is = conn.getInputStream();
			String response = "";
			byte[] b = new byte[1024];
			int count = 0;
			while ((count = is.read(b)) > 0)
				response += new String(b, 0, count);
			assertTrue("Response to POST method invalid 2", response
					.equals(posted));
		} catch (Exception e) {
			fail("Unexpected exception 2 : " + e.getMessage());
		}

		try {
			String posted = "just another test";
			URL u = new URL("http://" + Support_Configuration.HomeAddress
					+ "/cgi-bin/test.pl");
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u
					.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-length", String.valueOf(posted
					.length()));
			OutputStream out = conn.getOutputStream();
			out.write(posted.getBytes());
			// out.close();
			conn.getResponseCode();
			InputStream is = conn.getInputStream();
			String response = "";
			byte[] b = new byte[1024];
			int count = 0;
			while ((count = is.read(b)) > 0)
				response += new String(b, 0, count);
			assertTrue("Response to POST method invalid 3", response
					.equals(posted));
		} catch (Exception e) {
			fail("Unexpected exception 3 : " + e.getMessage());
		}

		try {
			URL u = new URL("http://" + Support_Configuration.HomeAddress
					+ "/cgi-bin/test.pl");
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u
					.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			int result = conn.getResponseCode();
			assertTrue("Unexpected response code: " + result, result == 200);
		} catch (Exception e) {
			fail("Unexpected exception 4 : " + e.getMessage());
		}

	}

	/**
	 * @tests java.net.URLConnection#getPermission()
	 */
	public void test_getPermission() {
		try {
			java.security.Permission p = uc.getPermission();
			assertTrue("Permission of wrong type: " + p.toString(),
					p instanceof java.net.SocketPermission);
			assertTrue("Permission has wrong name: " + p.getName(), p.getName()
					.indexOf(Support_Configuration.HomeAddress + ":80") >= 0);

			URL fileUrl = new URL("file:myfile");
			Permission perm = new FilePermission("myfile", "read");
			Permission result = fileUrl.openConnection().getPermission();
			assertTrue("Wrong file: permission 1:" + perm + " , " + result,
					result.equals(perm));

			fileUrl = new URL("file:/myfile/");
			perm = new FilePermission("/myfile", "read");
			result = fileUrl.openConnection().getPermission();
			assertTrue("Wrong file: permission 2:" + perm + " , " + result,
					result.equals(perm));

			fileUrl = new URL("file://host/volume/file");
			perm = new FilePermission("//host/volume/file", "read");
			result = fileUrl.openConnection().getPermission();
			assertTrue("Wrong file: permission 3:" + perm + " , " + result,
					result.equals(perm));

			URL httpUrl = new URL("http://home/myfile/");
			assertTrue("Wrong http: permission", httpUrl.openConnection()
					.getPermission().equals(
							new SocketPermission("home:80", "connect")));
			httpUrl = new URL("http://home2:8080/myfile/");
			assertTrue("Wrong http: permission", httpUrl.openConnection()
					.getPermission().equals(
							new SocketPermission("home2:8080", "connect")));
			URL ftpUrl = new URL("ftp://home/myfile/");
			assertTrue("Wrong ftp: permission", ftpUrl.openConnection()
					.getPermission().equals(
							new SocketPermission("home:21", "connect")));
			ftpUrl = new URL("ftp://home2:22/myfile/");
			assertTrue("Wrong ftp: permission", ftpUrl.openConnection()
					.getPermission().equals(
							new SocketPermission("home2:22", "connect")));

			URL jarUrl = new URL("jar:file:myfile!/");
			perm = new FilePermission("myfile", "read");
			result = jarUrl.openConnection().getPermission();
			assertTrue("Wrong jar: permission:" + perm + " , " + result, result
					.equals(new FilePermission("myfile", "read")));
		} catch (Exception e) {
			fail("Exception during test : " + e.getMessage());
		}

	}

	/**
	 * @tests java.net.URLConnection#getRequestProperty(java.lang.String)
	 */
	public void test_getRequestPropertyLjava_lang_String() {
		uc.setRequestProperty("Yo", "yo");
		assertTrue("Wrong property returned: " + uc.getRequestProperty("Yo"),
				uc.getRequestProperty("Yo").equals("yo"));
		assertNull("Wrong property returned: " + uc.getRequestProperty("No"),
				uc.getRequestProperty("No"));
	}
	
	/**
	 * @tests java.net.URLConnection#getRequestProperty(java.lang.String)
	 */
	public void test_getRequestProperty_LString_Exception() throws IOException {
        class NewHandler extends URLStreamHandler {
            protected URLConnection openConnection(URL u)
                    throws java.io.IOException {
                return new HttpURLConnection(u) {
                    @Override
                    public void disconnect() {
                        // do nothing
                    }
                    @Override
                    public boolean usingProxy() {
                        return false;
                    }
                    @Override
                    public void connect() throws IOException {
                        connected = true;
                    }
                };
            }
        }
        URL url = new URL("http", "test", 80, "index.html", new NewHandler());
        URLConnection urlCon = url.openConnection();
        urlCon.setRequestProperty("test", "testProperty");
        assertNull(urlCon.getRequestProperty("test"));
        
        urlCon.connect();
        try {
            urlCon.getRequestProperty("test");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

	/**
	 * @tests java.net.URLConnection#getURL()
	 */
	public void test_getURL() {
		assertTrue("Incorrect URL returned", uc.getURL().equals(url));
	}

	/**
	 * @tests java.net.URLConnection#getUseCaches()
	 */
	public void test_getUseCaches() {
		uc.setUseCaches(false);
		assertTrue("getUseCaches should have returned false", !uc
				.getUseCaches());
		uc.setUseCaches(true);
		assertTrue("getUseCaches should have returned true", uc.getUseCaches());
	}

	/**
	 * @tests java.net.URLConnection#guessContentTypeFromStream(java.io.InputStream)
	 */
	public void test_guessContentTypeFromStreamLjava_io_InputStream() {
		try {
			InputStream in = uc.getInputStream();
			byte[] bytes = new byte[in.available()];
			in.read(bytes, 0, bytes.length);
			in.close();
			assertEquals("Should have returned text/html",
					"text/html", URLConnection.guessContentTypeFromStream(
							new ByteArrayInputStream(bytes))
							);
		} catch (Exception e) {
			fail("Exception during test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.net.URLConnection#setAllowUserInteraction(boolean)
	 */
	public void test_setAllowUserInteractionZ() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setDefaultAllowUserInteraction(boolean)
	 */
	public void test_setDefaultAllowUserInteractionZ() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setDefaultRequestProperty(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_setDefaultRequestPropertyLjava_lang_StringLjava_lang_String() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setDefaultUseCaches(boolean)
	 */
	public void test_setDefaultUseCachesZ() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setDoInput(boolean)
	 */
	public void test_setDoInputZ() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setDoOutput(boolean)
	 */
	public void test_setDoOutputZ() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setFileNameMap(java.net.FileNameMap)
	 */
	public void test_setFileNameMapLjava_net_FileNameMap() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setIfModifiedSince(long)
	 */
	public void test_setIfModifiedSinceJ() {
		try {
			URL url = new URL("http://localhost:8080/");
			URLConnection connection = url.openConnection();
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			cal.clear();
			cal.set(2000, Calendar.MARCH, 5);
			connection.setIfModifiedSince(cal.getTime().getTime());
			assertEquals("Wrong date set", "Sun, 05 Mar 2000 00:00:00 GMT", connection.getRequestProperty(
					"If-Modified-Since")
					);
		} catch (MalformedURLException e) {
			fail("MalformedURLException : " + e.getMessage());
		} catch (IOException e) {
			fail("IOException : " + e.getMessage());
		}
	}

	/**
	 * @tests java.net.URLConnection#setRequestProperty(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_setRequestPropertyLjava_lang_StringLjava_lang_String() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.net.URLConnection#setUseCaches(boolean)
	 */
	public void test_setUseCachesZ() {
		assertTrue("Used to test", true);
	}
  
    /**
     * @tests java.net.URLConnection#setConnectTimeout(int)
     */
    public void test_setConnectTimeoutI() throws Exception {
        URLConnection uc = new URL("http://localhost").openConnection();
        assertEquals(0, uc.getConnectTimeout());
        uc.setConnectTimeout(0);
        assertEquals(0, uc.getConnectTimeout());
        try{
            uc.setConnectTimeout(-100);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e){
            // correct
        }
        assertEquals(0, uc.getConnectTimeout());
        uc.setConnectTimeout(100);
        assertEquals(100, uc.getConnectTimeout());
        try{
            uc.setConnectTimeout(-1);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e){
            // correct
        }
        assertEquals(100, uc.getConnectTimeout());
    }
    
    /**
     * @tests java.net.URLConnection#setReadTimeout(int)
     */
    public void test_setReadTimeoutI() throws Exception {
        URLConnection uc = new URL("http://localhost").openConnection();
        assertEquals(0, uc.getReadTimeout());
        uc.setReadTimeout(0);
        assertEquals(0, uc.getReadTimeout());
        try{
            uc.setReadTimeout(-100);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e){
            // correct
        }
        assertEquals(0, uc.getReadTimeout());
        uc.setReadTimeout(100);
        assertEquals(100, uc.getReadTimeout());
        try{
            uc.setReadTimeout(-1);
            fail("should throw IllegalArgumentException");
        }
        catch(IllegalArgumentException e){
            // correct
        }
        assertEquals(100,uc.getReadTimeout());
    }

	/**
	 * @tests java.net.URLConnection#toString()
	 */
	public void test_toString() {
		assertTrue("Wrong toString: " + uc.toString(), uc.toString().indexOf(
				"URLConnectionTest/Harmony.html") > 0);
	}

	protected void setUp() {
		try {
			url = new URL(Support_Resources
					.getResourceURL("/URLConnectionTest/Harmony.html"));
			uc = url.openConnection();
		} catch (Exception e) {
			fail("Exception during setup : " + e.getMessage());
		}
	}

	protected void tearDown() {
		((HttpURLConnection) uc).disconnect();
	}
}
