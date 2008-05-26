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

import org.microemu.SSLContextSetup;
import org.microemu.TestEnvPropertiesHelper;

import junit.framework.TestCase;

/**
 * @author vlads
 *
 */
public class BaseGCFTestCase extends TestCase {

	/**
	 * This is the server where I keep HTTPS and Socket test response server.
	 * If change add server to test-servers.keystore if it self-signed certificate.
	 * 
	 * War sources here https://pyx4j.com/svn/pyx4me/pyx4me-host/pyx4me-test-server
	 * 
	 */
	public static final String TEST_HOST = findNoProxyTestHost();

	/**
	 * File  ${home}/.microemulator/tests.properties is used for configuration
	 * @return
	 */
	private static String findNoProxyTestHost() {
		return TestEnvPropertiesHelper.getProperty("gcf.no-proxy-test-host", "pyx4j.com");
	}

	/**
	 * TODO Support proxy configuration.
	 */
	protected void setUp() throws Exception {
		// Some tests may run via proxy but not all.
		if (System.getProperty("http.proxyHost") == null) {
			//System.setProperty("http.proxyHost", "genproxy");
			//System.setProperty("http.proxyPort", String.valueOf(8900));
		}
	}

	/**
	 * Trust our self-signed test-servers
	 */
	protected void setupSSLContext() {
	    SSLContextSetup.setUp();
	}
}
