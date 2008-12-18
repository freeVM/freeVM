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
package org.apache.harmony.luni.tests.internal.net.www.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for <code>HttpURLConnection</code> class constructors and methods.
 */
public class HttpURLConnectionTest extends TestCase {

    private static final boolean DEBUG = false;

    private final static Object bound = new Object();

    static class MockServer extends Thread {
        ServerSocket serverSocket;
        boolean accepted = false;
        boolean started = false;

        public MockServer(String name) throws IOException {
            super(name);
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(5000);
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void run() {
            try {
                synchronized (bound) {
                    started = true;
                    bound.notify();
                }
                try {
                    serverSocket.accept().close();
                    accepted = true;
                } catch (SocketTimeoutException ignore) {
                }
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class MockHTTPServer extends MockServer {
        // HTTP response codes
        static final int OK_CODE = 200;
        static final int NOT_FOUND_CODE = 404;
        // how many times persistent connection will be used
        // by server
        int persUses;
        // result code to be sent to client
        int responseCode;
        // response content to be sent to client
        String response = "<html></html>";
        // client's POST message
        String clientPost = "Hello from client!";

        public MockHTTPServer(String name, int persUses) throws IOException {
            this(name, persUses, OK_CODE);
        }

        public MockHTTPServer(String name, int persUses, int responseCode)
                throws IOException {
            super(name);
            this.persUses = persUses;
            this.responseCode = responseCode;
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void run() {
            try {
                synchronized (bound) {
                    started = true;
                    bound.notify();
                }
                InputStream is = null;
                Socket client = null;
                try {
                    client = serverSocket.accept();
                    accepted = true;
                    for (int i = 0; i < persUses; i++) {
                        if (DEBUG) {
                            System.out.println("*** Using connection for "
                                    + (i + 1) + " time ***");
                        }
                        byte[] buff = new byte[1024];
                        is = client.getInputStream();
                        int num = 0; // number of read bytes
                        int bytik; // read byte value
                        boolean wasEOL = false;
                        // read header (until empty string)
                        while (((bytik = is.read()) > 0)) {
                            if (bytik == '\r') {
                                bytik = is.read();
                            }
                            if (wasEOL && (bytik == '\n')) {
                                break;
                            }
                            wasEOL = (bytik == '\n');
                            buff[num++] = (byte) bytik;
                        }
                        // int num = is.read(buff);
                        String message = new String(buff, 0, num);
                        if (DEBUG) {
                            System.out
                                    .println("---- Server got request: ----\n"
                                            + message
                                            + "-----------------------------");
                        }

                        // Act as Server (not Proxy) side
                        if (message.startsWith("POST")) {
                            // client connection sent some data
                            // if the data was not read with header
                            if (DEBUG) {
                                System.out
                                        .println("---- Server read client's data: ----");
                            }
                            num = is.read(buff);
                            message = new String(buff, 0, num);
                            if (DEBUG) {
                                System.out.println("'" + message + "'");
                                System.out
                                        .println("------------------------------------");
                            }
                            // check the received data
                            assertEquals(clientPost, message);
                        }

                        client
                                .getOutputStream()
                                .write(
                                        ("HTTP/1.1 " + responseCode + " OK\n"
                                                + "Content-type: text/html\n"
                                                + "Content-length: "
                                                + response.length() + "\n\n" + response)
                                                .getBytes());

                        if (responseCode != OK_CODE) {
                            // wait while test case check closed connection
                            // and interrupt this thread
                            try {
                                while (!isInterrupted()) {
                                    Thread.sleep(1000);
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                } catch (SocketTimeoutException ignore) {
                    ignore.printStackTrace();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                    serverSocket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class MockProxyServer extends MockServer {

        boolean acceptedAuthorizedRequest;

        public MockProxyServer(String name) throws Exception {
            super(name);
        }

        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(5000);
                byte[] buff = new byte[1024];
                int num = socket.getInputStream().read(buff);
                socket
                        .getOutputStream()
                        .write(
                                ("HTTP/1.0 407 Proxy authentication required\n"
                                        + "Proxy-authenticate: Basic realm=\"remotehost\"\n\n")
                                        .getBytes());
                num = socket.getInputStream().read(buff);
                if (num == -1) {
                    // this connection was closed, create new one:
                    socket = serverSocket.accept();
                    socket.setSoTimeout(5000);
                    num = socket.getInputStream().read(buff);
                }
                String request = new String(buff, 0, num);
                acceptedAuthorizedRequest = request.toLowerCase().indexOf(
                        "proxy-authorization:") > 0;
                if (acceptedAuthorizedRequest) {
                    socket.getOutputStream().write(
                            ("HTTP/1.1 200 OK\n\n").getBytes());
                }
            } catch (IOException e) {
            }
        }
    }

    public void setUp() {
        if (DEBUG) {
            System.out.println("\n==============================");
            System.out.println("===== Execution: " + getName());
            System.out.println("==============================");
        }
    }

    /**
     * ProxySelector implementation used in the test.
     */
    static class TestProxySelector extends ProxySelector {
        // proxy port
        private int proxy_port;
        // server port
        private int server_port;

        /**
         * Creates proxy selector instance. Selector will return the proxy, only
         * if the connection is made to localhost:server_port. Otherwise it will
         * return NO_PROXY. Address of the returned proxy will be
         * localhost:proxy_port.
         */
        public TestProxySelector(int server_port, int proxy_port) {
            this.server_port = server_port;
            this.proxy_port = proxy_port;
        }

        @Override
        public java.util.List<Proxy> select(URI uri) {
            Proxy proxy = Proxy.NO_PROXY;
            if (("localhost".equals(uri.getHost()))
                    && (server_port == uri.getPort())) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                        "localhost", proxy_port));
            }
            ArrayList<Proxy> result = new ArrayList<Proxy>();
            result.add(proxy);
            return result;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            // do nothing
        }
    }

    /**
     * @tests HttpURLConnection.getHeaderFields
     */
    public void test_getHeaderFields() throws Exception {
        URL url = new URL("http://www.apache.org");
        HttpURLConnection httpURLConnect = (HttpURLConnection) url
                .openConnection();
        assertEquals(200, httpURLConnect.getResponseCode());
        assertEquals("OK", httpURLConnect.getResponseMessage());
        Map headers = httpURLConnect.getHeaderFields();
        // there should be at least 2 headers
        assertTrue(headers.size() > 1);
        List list = (List) headers.get("Content-Length");
        if (list == null) {
            list = (List) headers.get("content-length");
        }
        assertNotNull(list);
        try {
            headers.put("key", "value");
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        try {
            list.set(0, "value");
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        try {
            httpURLConnect.setRequestProperty("key", "value");
            fail("should throw IlegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    /**
     * @tests org.apache.harmony.luni.internal.net.www.http.getOutputStream()
     */
    public void testGetOutputStream() throws Exception {
        // Regression for HARMONY-482
        MockServer httpServer = new MockServer(
                "ServerSocket for HttpURLConnectionTest");
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:"
                + httpServer.port()).openConnection();
        c.setDoOutput(true);
        // use new String("POST") instead of simple "POST" to obtain other
        // object instances then those that are in HttpURLConnection classes
        c.setRequestMethod(new String("POST"));
        c.getOutputStream();
        httpServer.join();
    }

    /**
     * Test whether getOutputStream can work after connection
     */
    public void test_getOutputStream_AfterConnect() throws Exception {
        URL url = new URL("http://www.apache.org");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.connect();
        String str_get = connection.getRequestMethod();
        assertTrue(str_get.equalsIgnoreCase("GET"));

        // call to getOutputStream should implicitly set req. method to POST
        connection.getOutputStream();
        String str_post = connection.getRequestMethod();
        assertTrue(str_post.equalsIgnoreCase("POST"));
    }

    /**
     * Test checks if the proxy specified in openConnection method will be used
     * for connection to the server
     */
    public void testUsingProxy() throws Exception {
        // Regression for HARMONY-570
        MockServer server = new MockServer("server");
        MockServer proxy = new MockServer("proxy");

        URL url = new URL("http://localhost:" + server.port());

        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress("localhost", proxy.port())));
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);

        server.start();
        synchronized (bound) {
            if (!server.started)
                bound.wait(5000);
        }
        proxy.start();
        synchronized (bound) {
            if (!proxy.started)
                bound.wait(5000);
        }

        connection.connect();

        // wait while server and proxy run
        server.join();
        proxy.join();

        assertTrue("Connection does not use proxy", connection.usingProxy());
        assertTrue("Proxy server was not used", proxy.accepted);

        HttpURLConnection huc = (HttpURLConnection) url
                .openConnection(Proxy.NO_PROXY);
        assertFalse(huc.usingProxy());
    }

    /**
     * @tests HttpURLConnection.usingProxy
     */
    public void testUsingProxy2() throws Exception {
        try {
            System.setProperty("http.proxyHost", "www.apache.org");
            URL url = new URL("http://www.apache.org");
            HttpURLConnection urlConnect = (HttpURLConnection) url
                    .openConnection();
            urlConnect.getInputStream();
            assertTrue(urlConnect.usingProxy());
            
            System.setProperty("http.proxyPort", "81");
            url = new URL("http://www.apache.org");
            urlConnect = (HttpURLConnection) url.openConnection();
            urlConnect.getInputStream();
            assertFalse(urlConnect.usingProxy());
            
            url = new URL("http://localhost");
            urlConnect = (HttpURLConnection) url.openConnection();
            try {
                urlConnect.getInputStream();
                fail("should throw ConnectException");
            } catch (ConnectException e) {
                // Expected
            }
            assertFalse(urlConnect.usingProxy());
        } finally {
            System.setProperties(null);
        }
    }

    /**
     * Test checks if the proxy provided by proxy selector will be used for
     * connection to the server
     */
    public void testUsingProxySelector() throws Exception {
        // Regression for HARMONY-570
        MockServer server = new MockServer("server");
        MockServer proxy = new MockServer("proxy");

        URL url = new URL("http://localhost:" + server.port());

        // keep default proxy selector
        ProxySelector defPS = ProxySelector.getDefault();
        // replace selector
        ProxySelector.setDefault(new TestProxySelector(server.port(), proxy
                .port()));

        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            server.start();
            synchronized (bound) {
                if (!server.started)
                    bound.wait(5000);
            }
            proxy.start();
            synchronized (bound) {
                if (!proxy.started)
                    bound.wait(5000);
            }
            connection.connect();

            // wait while server and proxy run
            server.join();
            proxy.join();

            assertTrue("Connection does not use proxy", connection.usingProxy());
            assertTrue("Proxy server was not used", proxy.accepted);
        } finally {
            // restore default proxy selector
            ProxySelector.setDefault(defPS);
        }
    }

    public void testProxyAuthorization() throws Exception {
        // Set up test Authenticator
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("user", "password"
                        .toCharArray());
            }
        });

        try {
            MockProxyServer proxy = new MockProxyServer("ProxyServer");

            URL url = new URL("http://remotehost:55555/requested.data");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress("localhost", proxy.port())));
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            proxy.start();

            connection.connect();
            assertEquals("unexpected response code", 200, connection
                    .getResponseCode());
            proxy.join();
            assertTrue("Connection did not send proxy authorization request",
                    proxy.acceptedAuthorizedRequest);
        } finally {
            // remove previously set authenticator
            Authenticator.setDefault(null);
        }
    }

    /**
     * Test that a connection is not closed if the client reads all the data but
     * not closes input stream. read until -1.
     */
    public void testConnectionPersistence() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }

        c.setDoInput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        InputStream is = c.getInputStream();
        byte[] buffer = new byte[128];
        int totalBytes = 0;
        int bytesRead = 0;
        while ((bytesRead = is.read(buffer)) > 0) {
            if (DEBUG) {
                System.out.println("Client got response: '"
                        + new String(buffer, 0, bytesRead) + "'");
            }
            totalBytes += bytesRead;
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        is = c2.getInputStream();
        buffer = new byte[128];
        totalBytes = 0;
        bytesRead = 0;
        while ((bytesRead = is.read(buffer)) > 0) {
            if (DEBUG) {
                System.out.println("Client got response: '"
                        + new String(buffer, 0, bytesRead) + "'");
                totalBytes += bytesRead;
            }
        }
    }

    /**
     * Test that a connection is not closed if the client reads all the data but
     * not closes input stream. read() not receives -1.
     */
    public void testConnectionPersistence2() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }

        c.setDoInput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        InputStream is = c.getInputStream();
        int bytes2Read = httpServer.response.length();
        byte[] buffer = new byte[httpServer.response.length()];
        while ((bytes2Read -= is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        is = c2.getInputStream();
        buffer = new byte[httpServer.response.length()];
        bytes2Read = httpServer.response.length();
        while ((bytes2Read -= is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }
    }

    /**
     * Test that a connection is not closed if it firstly does POST, and then
     * does GET requests.
     */
    public void testConnectionPersistence3() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }

        c.setDoInput(true);
        c.setDoOutput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.getOutputStream().write(httpServer.clientPost.getBytes());

        InputStream is = c.getInputStream();
        int bytes2Read = httpServer.response.length();
        byte[] buffer = new byte[httpServer.response.length()];
        while ((bytes2Read -= is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        is = c2.getInputStream();
        buffer = new byte[httpServer.response.length()];
        bytes2Read = httpServer.response.length();
        while ((bytes2Read -= is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }
    }

    /**
     * Test that a connection is not closed if it firstly does GET, and then
     * does POST requests.
     */
    public void testConnectionPersistence4() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }

        c.setDoInput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);

        InputStream is = c.getInputStream();
        int bytes2Read = httpServer.response.length();
        byte[] buffer = new byte[httpServer.response.length()];
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        c2.setDoOutput(true);
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        c2.getOutputStream().write(httpServer.clientPost.getBytes());
        is = c2.getInputStream();
        buffer = new byte[httpServer.response.length()];
        bytes2Read = httpServer.response.length();
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }
    }

    /**
     * Test that a connection is not closed if it does POST for 2 times.
     */
    public void testConnectionPersistence5() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }
        c.setDoOutput(true);
        c.setDoInput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.getOutputStream().write(httpServer.clientPost.getBytes());
        InputStream is = c.getInputStream();
        int bytes2Read = httpServer.response.length();
        byte[] buffer = new byte[httpServer.response.length()];
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL("http://localhost:"
                + httpServer.port()).openConnection();
        c2.setDoOutput(true);
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        c2.getOutputStream().write(httpServer.clientPost.getBytes());
        is = c2.getInputStream();
        buffer = new byte[httpServer.response.length()];
        bytes2Read = httpServer.response.length();
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }
    }

    /**
     * Test that a connection made through proxy will be reused for connection
     * establishing without proxy.
     */
    public void testProxiedConnectionPersistence() throws Exception {
        MockHTTPServer httpServer = new MockHTTPServer(
                "HTTP Server for persistence checking", 2);
        httpServer.start();
        synchronized (bound) {
            if (!httpServer.started) {
                bound.wait(5000);
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL(
                "http://some.host:1234").openConnection(new Proxy(
                Proxy.Type.HTTP, new InetSocketAddress("localhost", httpServer
                        .port())));
        if (DEBUG) {
            System.out.println("Actual connection class: " + c.getClass());
        }
        c.setDoOutput(true);
        c.setDoInput(true);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.getOutputStream().write(httpServer.clientPost.getBytes());
        InputStream is = c.getInputStream();
        int bytes2Read = httpServer.response.length();
        byte[] buffer = new byte[httpServer.response.length()];
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }

        HttpURLConnection c2 = (HttpURLConnection) new URL(
                "http://some.host:1234").openConnection();
        c2.setDoOutput(true);
        c2.setDoInput(true);
        c2.setConnectTimeout(5000);
        c2.setReadTimeout(5000);
        c2.getOutputStream().write(httpServer.clientPost.getBytes());
        is = c2.getInputStream();
        buffer = new byte[httpServer.response.length()];
        bytes2Read = httpServer.response.length();
        while ((bytes2Read = is.read(buffer)) > 0) {
        }
        if (DEBUG) {
            System.out.println("Client got response: '" + new String(buffer)
                    + "'");
        }
    }

    public void testSecurityManager() throws MalformedURLException,
            IOException, InterruptedException {
        try {
            MockHTTPServer httpServer = new MockHTTPServer(
                    "HTTP Server for persistence checking", 2);
            httpServer.start();
            synchronized (bound) {
                if (!httpServer.started) {
                    bound.wait(5000);
                }
            }
            MySecurityManager sm = new MySecurityManager();
            System.setSecurityManager(sm);

            // Check that a first connection calls checkConnect
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(
                        "http://localhost:" + httpServer.port())
                        .openConnection();
                if (DEBUG) {
                    System.out.println("Actual connection class: "
                            + c.getClass());
                }
                c.connect();
                fail("Should have thrown a SecurityException upon connection");

            } catch (SecurityException e) {
            }

            // Now create a connection properly
            System.setSecurityManager(null);
            HttpURLConnection c = (HttpURLConnection) new URL(
                    "http://localhost:" + httpServer.port()).openConnection();
            c.setDoInput(true);
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[128];
            int totalBytes = 0;
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer)) > 0) {
                if (DEBUG) {
                    System.out.println("Client got response: '"
                            + new String(buffer, 0, bytesRead) + "'");
                }
                totalBytes += bytesRead;
            }

            // Now check that a second connection also calls checkConnect
            System.setSecurityManager(sm);
            try {
                HttpURLConnection c2 = (HttpURLConnection) new URL(
                        "http://localhost:" + httpServer.port())
                        .openConnection();
                c2.setDoInput(true);
                c2.setConnectTimeout(5000);
                c2.setReadTimeout(5000);
                is = c2.getInputStream();
                buffer = new byte[128];
                totalBytes = 0;
                bytesRead = 0;
                while ((bytesRead = is.read(buffer)) > 0) {
                    if (DEBUG) {
                        System.out.println("Client got response: '"
                                + new String(buffer, 0, bytesRead) + "'");
                        totalBytes += bytesRead;
                    }
                }
                fail("Expected a SecurityException to be thrown");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }

    private static class MySecurityManager extends SecurityManager {

        @Override
        public void checkConnect(String host, int port) {
            throw new SecurityException();
        }

        @Override
        public void checkConnect(String host, int port, Object context) {
            throw new SecurityException();
        }

        @Override
        public void checkPermission(Permission permission) {
            // allows a new security manager to be set
        }

    }

}