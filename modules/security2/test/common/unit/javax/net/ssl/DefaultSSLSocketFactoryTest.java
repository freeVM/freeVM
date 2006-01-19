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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>DefaultSSLSocketFactory</code> class constructors and
 * methods.
 *  
 */
public class DefaultSSLSocketFactoryTest extends PerformanceTest {

    /*
     * Class under test for Socket createSocket(String, int)
     */
    public void testCreateSocketStringint() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        try {
            f.createSocket("localhost", 0);
            fail("No expected SocketException");
        } catch (SocketException e) {
        } catch (IOException e) {
            fail(e.toString());
        }

    }

    /*
     * Class under test for Socket createSocket(String, int, InetAddress, int)
     */
    public void testCreateSocketStringintInetAddressint() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        try {
            f.createSocket("localhost", 0, InetAddress.getLocalHost(), 1);
            fail("No expected SocketException");
        } catch (SocketException e) {
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /*
     * Class under test for Socket createSocket(InetAddress, int)
     */
    public void testCreateSocketInetAddressint() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        try {
            f.createSocket(InetAddress.getLocalHost(), 1);
            fail("No expected SocketException");
        } catch (SocketException e) {
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /*
     * Class under test for Socket createSocket(InetAddress, int, InetAddress,
     * int)
     */
    public void testCreateSocketInetAddressintInetAddressint() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        try {
            f.createSocket(InetAddress.getLocalHost(), 1, InetAddress
                    .getLocalHost(), 2);
            fail("No expected SocketException");
        } catch (SocketException e) {
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testGetDefaultCipherSuites() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        String[] res = f.getDefaultCipherSuites();
        if (res == null || res.length != 0) {
            fail("incorrect result");
        }
    }

    public void testGetSupportedCipherSuites() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        String[] res = f.getSupportedCipherSuites();
        if (res == null || res.length != 0) {
            fail("incorrect result");
        }
    }

    /*
     * Class under test for Socket createSocket(Socket, String, int, boolean)
     */
    public void testCreateSocketSocketStringintboolean() {
        DefaultSSLSocketFactory f = new DefaultSSLSocketFactory("ERROR");
        try {
            f.createSocket(new Socket(), "localhost", 1, true);
            fail("No expected SocketException");
        } catch (SocketException e) {
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}