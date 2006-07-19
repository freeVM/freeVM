/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Alexander Y. Kleymenov
 * @version $Revision$
 */

package org.apache.harmony.xnet.provider.jsse;

import org.apache.harmony.xnet.provider.jsse.SSLParameters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Implementation of SSLServerSocketFactory.
 */
public class SSLServerSocketFactoryImpl extends SSLServerSocketFactory {

    private SSLParameters sslParameters;
    private IOException instantiationException;

    /**
     * Constructor.
     */
    public SSLServerSocketFactoryImpl() {
        super();
        try {
            this.sslParameters = SSLParameters.getDefault();
            this.sslParameters.setUseClientMode(false);
        } catch (KeyManagementException e) {
            instantiationException =
                new IOException("Delayed instantiation exception:");
            instantiationException.initCause(e);
        }
    }

    /**
     * Constructor.
     */
    protected SSLServerSocketFactoryImpl(SSLParameters sslParameters) {
        super();
        this.sslParameters = (SSLParameters) sslParameters.clone();
        this.sslParameters.setUseClientMode(false);
    }

    /**
     * @see javax.net.ssl.SSLServerSocketFactory#getDefaultCipherSuites()
     */
    public String[] getDefaultCipherSuites() {
        if (instantiationException != null) {
            return new String[0];
        }
        return sslParameters.getEnabledCipherSuites();
    }

    /**
     * @see javax.net.ssl.SSLServerSocketFactory#getSupportedCipherSuites()
     */
    public String[] getSupportedCipherSuites() {
        if (instantiationException != null) {
            return new String[0];
        }
        return CipherSuite.getSupportedCipherSuiteNames();
    }

    /**
     * @see javax.net.ServerSocketFactory#createServerSocket()
     */
    public ServerSocket createServerSocket() throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLServerSocketImpl((SSLParameters) sslParameters.clone());
    }


    /**
     * @see javax.net.ServerSocketFactory#createServerSocket(int)
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLServerSocketImpl(port,
                (SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.ServerSocketFactory#createServerSocket(int,int)
     */
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLServerSocketImpl(port, backlog,
                (SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.ServerSocketFactory#createServerSocket(int,int,InetAddress)
     */
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress iAddress) throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLServerSocketImpl(port, backlog, iAddress,
                (SSLParameters) sslParameters.clone());
    }
}

