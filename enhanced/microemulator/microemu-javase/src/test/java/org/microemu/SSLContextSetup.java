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

package org.microemu;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author vlads
 * 
 * See src/test/ssl/read-me.txt
 * 
 */
public class SSLContextSetup {
	
	private static boolean initialized = false;
	
	public static synchronized void setUp() {
    	if (initialized) {
    		return;
    	}
    	InputStream is = null;
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            is = SSLContextSetup.class.getResourceAsStream("/test-servers.keystore"); 
            if (is == null) {
            	new Error("keystore not found");
            }
            trustStore.load(is, "microemu2006".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");  
            trustManagerFactory.init(trustStore);  
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, secureRandom);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            initialized = true;
        } catch (Throwable e) {
            throw new Error(e);
        } finally {
        	if (is != null) {
        		try {
					is.close();
				} catch (IOException ignore) {
				}
        	}
        }
    }
}
