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

import javax.microedition.pki.Certificate;

public class HttpsConnectionTest extends BaseTestHttpConnection {
	
	private static final String testInetHTTPUrl = "https://" + TEST_HOST + testFile;
	
	protected void setUp() throws Exception {
		super.setUp();
		super.setupSSLContext();
	}
	
    protected HttpConnection openHttpConnection(String query) throws IOException {
    	return (HttpsConnection)Connector.open("https://" + TEST_HOST + query, Connector.READ, true);
    }
    
    public void testConnection() throws IOException {
    	HttpsConnection hc = (HttpsConnection)Connector.open(testInetHTTPUrl, Connector.READ, true);
        try {
			assertEquals("getResponseCode()", HttpConnection.HTTP_OK, hc.getResponseCode());
			assertEquals("getPort()", 443, hc.getPort());
			assertEquals("getProtocol()", "https", hc.getProtocol());
			assertEquals("getURL()", testInetHTTPUrl, hc.getURL());
			assertHttpConnectionMethods(hc);
		} finally {
			hc.close();
		}
    }
    
    public void testSecurityInfo() throws IOException {
    	HttpsConnection hc = (HttpsConnection)Connector.open(testInetHTTPUrl, Connector.READ, true);
        try {
			assertEquals("getResponseCode()", HttpConnection.HTTP_OK, hc.getResponseCode());
			SecurityInfo si = hc.getSecurityInfo();
			assertNotNull("HttpsConnection.getSecurityInfo()", si);
			assertNotNull("SecurityInfo.getProtocolVersion()", si.getProtocolVersion());
			assertNotNull("SecurityInfo.getProtocolName()", si.getProtocolName());
			assertNotNull("SecurityInfo.getCipherSuite()", si.getCipherSuite());
			Certificate cert = si.getServerCertificate(); 
			assertNotNull("SecurityInfo.getServerCertificate()", cert);
			assertNotNull("Certificate.getSubject()", cert.getSubject());
			assertNotNull("Certificate.getIssuer()", cert.getIssuer());
			assertNotNull("Certificate.getType()", cert.getType());
			assertNotNull("Certificate.getVersion()", cert.getVersion());
			assertNotNull("Certificate.getSigAlgName()", cert.getSigAlgName());
			assertTrue("Certificate.getNotBefore()", cert.getNotBefore() >= 0);
			assertTrue("Certificate.getNotAfter()", cert.getNotAfter() >= 0);
			String serialNumber = cert.getSerialNumber();
			assertNotNull("Certificate.getSerialNumber()", serialNumber);
		} finally {
			hc.close();
		}
    }
    
    public void testWrongCertificate() throws IOException {
    	HttpsConnection hc = (HttpsConnection)Connector.open("https://www.pyx4me.com" + testFile, Connector.READ, true);
        try {
        	// Produce java.io.IOException: HTTPS hostname wrong:  should be <www.pyx4me.com>
        	hc.getResponseCode();
        	fail("Should produce IOException");
        } catch (IOException e) {
		} finally {
			hc.close();
		}
    }
}