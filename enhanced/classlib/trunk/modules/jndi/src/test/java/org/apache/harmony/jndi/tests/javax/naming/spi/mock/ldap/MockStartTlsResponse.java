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
/**
 * @author Hugo Beilis
 * @author Leonardo Soler
 * @author Gabriel Miretti
 * @version 1.0
 */
package org.apache.harmony.jndi.tests.javax.naming.spi.mock.ldap;

import java.io.IOException;

import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>This mock give us our Implementation of the class StartTlsResponse. This class has the intention of
 * help us to test another classes.</p> 
 *
 */
public class MockStartTlsResponse extends StartTlsResponse {

	/**
	 * The serial of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor method to our TLSResponse.
	 *
	 */
	public MockStartTlsResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Method not implemented yet.</p>
	 */
	public void setEnabledCipherSuites(String[] arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * <p>Method not implemented yet.</p>
	 */
	public void setHostnameVerifier(HostnameVerifier arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * <p>Method not implemented yet.</p>
	 */
	public SSLSession negotiate() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>Method not implemented yet.</p>
	 */
	public SSLSession negotiate(SSLSocketFactory arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method to close our TlsResponse.
	 */
	public void close() throws IOException {
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
