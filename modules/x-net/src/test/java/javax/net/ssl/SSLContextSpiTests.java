/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.net.ssl;

import java.security.KeyManagementException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContextSpi;

import junit.framework.TestCase;


/**
 * Tests for <code>SSLContextSpi</code> class constructors and methods.
 * 
 */

public class SSLContextSpiTests extends TestCase {
    /**
     * Constructor for SSLContextSpiTests.
     * 
     * @param arg0
     */
    public SSLContextSpiTests(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>SSLContextSpi</code> constructor
     * Assertion: constructs SSLContextSpi
     */
    public void testSSLContextSpi01() throws KeyManagementException {
        SSLContextSpi sslConSpi = new MySSLContextSpi();
        try {
            sslConSpi.engineGetSocketFactory();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }
        try {
            sslConSpi.engineGetServerSocketFactory();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }
        try {
            sslConSpi.engineGetServerSessionContext();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }
        try {
            sslConSpi.engineGetClientSessionContext();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }       
        try {
            sslConSpi.engineCreateSSLEngine();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }
        try {
            sslConSpi.engineCreateSSLEngine("host",1);
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", e.getMessage(),"Not initialiazed");
        }
        sslConSpi.engineInit(null, null, new SecureRandom());
        assertNull("Not null result", sslConSpi.engineGetSocketFactory());
        assertNull("Not null result", sslConSpi.engineGetServerSocketFactory());
        assertNotNull("Null result", sslConSpi.engineCreateSSLEngine());
        assertNotNull("Null result", sslConSpi.engineCreateSSLEngine("host",1));
        assertNull("Not null result", sslConSpi.engineGetServerSessionContext());
        assertNull("Not null result", sslConSpi.engineGetClientSessionContext());
    }
}
