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
 * @author Hugo Beilis
 * @author Osvaldo Demo
 * @author Jorge Rafael
 * @version 1.0
 */
package ar.org.fitc.test.rmi.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import javax.crypto.*;

public class CipherSocket extends Socket {

    public CipherSocket() {
        super();
    }

    public CipherSocket(Proxy proxy) {
        super(proxy);
    }

    public CipherSocket(SocketImpl impl) throws SocketException {
        super(impl);
    }

    public CipherSocket(String host, int port) throws UnknownHostException,
            IOException {
        super(host, port);
    }

    public CipherSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    public CipherSocket(String host, int port, InetAddress localAddr,
            int localPort) throws IOException {
        super(host, port, localAddr, localPort);
    }

    public CipherSocket(InetAddress address, int port, InetAddress localAddr,
            int localPort) throws IOException {
        super(address, port, localAddr, localPort);
    }

    public CipherSocket(String host, int port, boolean stream)
            throws IOException {
        super(host, port, stream);
    }

    public CipherSocket(InetAddress host, int port, boolean stream)
            throws IOException {
        super(host, port, stream);
    }

    public OutputStream getOutputStream() throws IOException {
        return new CipherOutputStream(super.getOutputStream(), RemoteCipherImpl
                .getCipher(Cipher.ENCRYPT_MODE));
    }

    public InputStream getInputStream() throws IOException {
        return new CipherInputStream(super.getInputStream(), RemoteCipherImpl
                .getCipher(Cipher.DECRYPT_MODE));
    }
}
