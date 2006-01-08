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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package javax.net.ssl;

import java.io.IOException;
import java.security.Security;
import java.net.SocketException;

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>SSLSocketFactory</code> class methods.
 * 
 */
public class SSLServerSocketFactoryTest extends PerformanceTest {

     private SSLServerSocketFactory customServerSocketFactory;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        String defaultName = Security.getProperty("ssl.ServerSocketFactory.provider");
        if (defaultName != null) {    
            try {
                customServerSocketFactory = (SSLServerSocketFactory) Class.forName(
                        defaultName, true, ClassLoader.getSystemClassLoader())
                        .newInstance();
             } catch (Exception e) {
             }
        }
        if (customServerSocketFactory == null) {
            SSLContext context = DefaultSSLContext.getContext();
            if (context != null) {
                customServerSocketFactory = context.getServerSocketFactory();
            }
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public final void testGetDefault() {
        SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        if (customServerSocketFactory != null) {
            if (!factory.getClass().getName().equals(customServerSocketFactory.getClass().getName())) {
                fail("incorrect instance: " + factory.getClass()+
                        " expected: " + customServerSocketFactory.getClass().getName());
            }
        } else {
            if (!(factory instanceof DefaultSSLServerSocketFactory)) {
                fail("incorrect instance " + factory.getClass());
            }
            if (factory.getDefaultCipherSuites().length != 0) {
                fail("incorrect result: DefaultSSLServerSocketFactory.getDefaultCipherSuites()");
            }
            if (factory.getSupportedCipherSuites().length != 0) {
                fail("incorrect result: DefaultSSLServerSocketFactory.getDefaultCipherSuites()");
            }
            try {
                factory.createServerSocket(0);
                fail("No expected SocketException");
            } catch (SocketException e) {
            } catch (IOException e) {
                fail(e.toString());
            }              
        }
    }

}