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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package javax.net.ssl;

import java.io.IOException;
import java.security.Security;
import java.net.SocketException;

import junit.framework.TestCase;


/**
 * Tests for <code>SSLSocketFactory</code> class methods.
 * 
 */
public class SSLSocketFactoryTest extends TestCase {

    private SSLSocketFactory customSocketFactory;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        String defaultName = Security.getProperty("ssl.SocketFactory.provider");
        if (defaultName != null) {    
            try {
                customSocketFactory = (SSLSocketFactory) Class.forName(
                        defaultName, true, ClassLoader.getSystemClassLoader())
                        .newInstance();
             } catch (Exception e) {
             }
        }
        if (customSocketFactory == null) {
            SSLContext context = DefaultSSLContext.getContext();
            if (context != null) {
                customSocketFactory = context.getSocketFactory();
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
        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        if (customSocketFactory != null) {
            if (!factory.getClass().getName().equals(customSocketFactory.getClass().getName())) {
                fail("incorrect instance: " + factory.getClass()+
                        " expected: " + customSocketFactory.getClass().getName());
            }
        } else {
            if (!(factory instanceof DefaultSSLSocketFactory)) {
                fail("incorrect instance " + factory.getClass());
            }
            if (factory.getDefaultCipherSuites().length != 0) {
                fail("incorrect result: DefaultSSLSocketFactory.getDefaultCipherSuites()");
            }
            if (factory.getSupportedCipherSuites().length != 0) {
                fail("incorrect result: DefaultSSLSocketFactory.getDefaultCipherSuites()");
            }
            try {
                factory.createSocket("localhost", 2021);
                fail("No expected SocketException");
            } catch (SocketException e) {
            } catch (IOException e) {
                fail(e.toString());
            }              
        }
    }

}
