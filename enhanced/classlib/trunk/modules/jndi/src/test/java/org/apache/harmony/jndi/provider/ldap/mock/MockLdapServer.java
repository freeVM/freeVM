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

package org.apache.harmony.jndi.provider.ldap.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.harmony.jndi.provider.ldap.LdapMessage;
import org.apache.harmony.security.asn1.ASN1Integer;

/**
 * This class is a mock ldap server which only support one connection.
 * 
 * NOTE: before client send request to the mock server, must set expected
 * response message sequence using <code>etResponseSeq(LdapMessage[])</code>
 * method, so the server will send response messages in order of parameter
 * <code>LdapMessage[]</code>.
 */
public class MockLdapServer implements Runnable {

    private ServerSocket server;

    private Socket socket;

    private LdapMessage[] responses;

    private int port;

    private Object lock = new Object();

    private boolean isStopped;

    private static int DEFAULT_PORT = 1024;

    public void start() {
        port = DEFAULT_PORT;
        while (true) {
            try {
                server = new ServerSocket(port);
                break;
            } catch (IOException e) {
                ++port;
            }
        }

        isStopped = false;
        new Thread(this).start();
    }

    public void stop() {
        isStopped = true;

        synchronized (lock) {
            lock.notify();
        }

        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                // ignore
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public int getPort() {
        return port;
    }

    public void setResponseSeq(LdapMessage[] msges) {
        this.responses = msges;

        synchronized (lock) {
            lock.notify();
        }
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            socket = server.accept();
            in = socket.getInputStream();
            out = socket.getOutputStream();
            while (!isStopped) {
                if (responses == null) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } else {

                    for (int i = 0; i < responses.length; i++) {
                        final MockLdapMessage response = new MockLdapMessage(
                                responses[i]);
                        LdapMessage request = new LdapMessage(null) {
                            public void decodeValues(Object[] values) {
                                response.setMessageId(ASN1Integer
                                        .toIntValue(values[0]));
                            }
                        };
                        request.decode(in);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            //ignore
                        }
                        out.write(response.encode());
                    }
                    responses = null;
                }
            }
        } catch (IOException e) {
            // FIXME deal with the exception
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }

                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public String getURL() {
        return "ldap://localhost:" + port;
    }
}
