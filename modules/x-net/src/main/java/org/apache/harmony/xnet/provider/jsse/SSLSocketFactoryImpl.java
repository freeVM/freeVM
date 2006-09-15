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

import org.apache.harmony.xnet.provider.jsse.SSLSocketWrapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;

import javax.net.ssl.SSLSocketFactory;

/**
 * Implementation of SSLSocketFactory.
 */
public class SSLSocketFactoryImpl extends SSLSocketFactory {

    private SSLParameters sslParameters;
    private IOException instantiationException;

    /**
     * Constructor.
     */
    public SSLSocketFactoryImpl() {
        super();
        try {
            sslParameters = SSLParameters.getDefault();
        } catch (KeyManagementException e) {
            instantiationException =
                new IOException("Delayed instantiation exception:");
            instantiationException.initCause(e);
        }
    }

    /**
     * Constructor.
     */
    protected SSLSocketFactoryImpl(SSLParameters sslParameters) {
        super();
        this.sslParameters = sslParameters;
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
     */
    public String[] getDefaultCipherSuites() {
        if (instantiationException != null) {
            return new String[0];
        }
        return sslParameters.getEnabledCipherSuites();
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
     */
    public String[] getSupportedCipherSuites() {
        if (instantiationException != null) {
            return new String[0];
        }
        return CipherSuite.getSupportedCipherSuiteNames();
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#createSocket(Socket,String,int,boolean)
     */
    public Socket createSocket(Socket s, String host, int port,
            boolean autoClose) throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketWrapper(s, autoClose, (SSLParameters) sslParameters
                .clone());
    }

    // -------------- Methods inherided from SocketFactory --------------

    /**
     * @see javax.net.SocketFactory#createSocket()
     */
    public Socket createSocket() throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketImpl((SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.SocketFactory#createSocket(String,int)
     */
    public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketImpl(host, port,
                (SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.SocketFactory#createSocket(String,int,InetAddress,int)
     */
    public Socket createSocket(String host, int port,
            InetAddress localHost, int localPort) throws IOException,
            UnknownHostException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketImpl(host, port, localHost, localPort,
                (SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.SocketFactory#createSocket(InetAddress,int)
     */
    public Socket createSocket(InetAddress host, int port)
            throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketImpl(host, port,
                (SSLParameters) sslParameters.clone());
    }

    /**
     * @see javax.net.SocketFactory#createSocket(InetAddress,int,InetAddress,int)
     */
    public Socket createSocket(InetAddress address, int port,
            InetAddress localAddress, int localPort) throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new SSLSocketImpl(address, port, localAddress, localPort,
                (SSLParameters) sslParameters.clone());
    }

    // ------------------------------------------------------------------
}

