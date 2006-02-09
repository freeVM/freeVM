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

import junit.framework.TestCase;


/**
 * Tests for <code>SSLServerSocket</code> class constructors.
 *  
 */
public class SSLServerSocketTest extends TestCase {

    /*
     * Class under test for void SSLServerSocket()
     */
    public void testSSLServerSocket() {
        try {
            SSLServerSocket soc = new MySSLServerSocket();
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /*
     * Class under test for void SSLServerSocket(int)
     */
    public void testSSLServerSocketint() {
        try {
            SSLServerSocket soc = new MySSLServerSocket(0);
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /*
     * Class under test for void SSLServerSocket(int, int)
     */
    public void testSSLServerSocketintint() {
        try {
            SSLServerSocket soc = new MySSLServerSocket(0, 10);
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /*
     * Class under test for void SSLServerSocket(int, int, InetAddress)
     */
    public void testSSLServerSocketintintInetAddress() {
        try {
            SSLServerSocket soc = new MySSLServerSocket(0, 10, InetAddress
                    .getLocalHost());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

}

class MySSLServerSocket extends SSLServerSocket {

    protected MySSLServerSocket() throws IOException {
        super();
    }

    protected MySSLServerSocket(int port) throws IOException {
        super(port);
    }

    protected MySSLServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    protected MySSLServerSocket(int port, int backlog, InetAddress address)
            throws IOException {
        super(port, backlog, address);
    }

    public String[] getEnabledCipherSuites() {
        return null;
    }

    public void setEnabledCipherSuites(String[] suites) {
    }

    public String[] getSupportedCipherSuites() {
        return null;
    }

    public String[] getSupportedProtocols() {
        return null;
    }

    public String[] getEnabledProtocols() {
        return null;
    }

    public void setEnabledProtocols(String[] protocols) {
    }

    public void setNeedClientAuth(boolean need) {
    }

    public boolean getNeedClientAuth() {
        return false;
    }

    public void setWantClientAuth(boolean want) {
    }

    public boolean getWantClientAuth() {
        return false;
    }

    public void setUseClientMode(boolean mode) {
    }

    public boolean getUseClientMode() {
        return false;
    }

    public void setEnableSessionCreation(boolean flag) {
    }

    public boolean getEnableSessionCreation() {
        return false;
    }
}

