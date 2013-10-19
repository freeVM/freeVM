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

package javax.microedition.io;

import java.io.IOException;

public class HttpConnectionTest extends BaseTestHttpConnection {

	private static final String testInetHTTPUrl = "http://" + TEST_HOST + testFile;

    public void testConnection() throws IOException {
    	HttpConnection hc = (HttpConnection)Connector.open(testInetHTTPUrl, Connector.READ, true);
        try {
			assertEquals("getResponseCode()", HttpConnection.HTTP_OK, hc.getResponseCode());
			assertEquals("getPort()", 80, hc.getPort());
			assertEquals("getProtocol()", "http", hc.getProtocol());
			assertEquals("getURL()", testInetHTTPUrl, hc.getURL());
			assertHttpConnectionMethods(hc);
		} finally {
			hc.close();
		}
    }
    
    protected HttpConnection openHttpConnection(String query) throws IOException {
    	return (HttpConnection)Connector.open("http://" + TEST_HOST + query, Connector.READ, true);
    }
}
