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
 * @author Boris Kuznetsov
 * @version $Revision$
 */

package org.apache.harmony.security.provider.jsse;

import java.io.IOException;

/**
 * 
 * Represents certificate verify message
 * @see TLS 1.0 spec., 7.4.8. Certificate verify
 * (http://www.ietf.org/rfc/rfc2246.txt)
 */
public class CertificateVerify extends Message {

    /**
     * Signature
     */
    byte[] signedHash;

    /**
     * Creates outbound message
     * 
     * @param hash
     */
    public CertificateVerify(byte[] hash) {
        this.signedHash = hash;
        length = hash.length;
    }

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public CertificateVerify(HandshakeIODataStream in, int length)
            throws IOException {
        if (length == 0) {
            signedHash = new byte[0];
        } else if (length == 20 || length == 36) {
            signedHash = in.read(length);
        } else {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect CertificateVerify");
        }
        this.length = length;
    }

    /**
     * Sends message
     * 
     * @param out
     */
    public void send(HandshakeIODataStream out) {
        if (signedHash.length != 0) {
            out.write(signedHash);
        }
    }

    /**
     * Returns message type
     * 
     * @return
     */
    public int getType() {
        return Handshake.CERTIFICATE_VERIFY;
    }
}