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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.support.cert;

import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Stub class for <code>java.security.cert.CertPath</code> tests
 * 
 */
public class MyCertPath extends CertPath {
    /**
     * my certificates list
     */
    private final Vector certificates;
    /**
     * List of encodings supported
     */
    private final Vector encodingNames;
    /**
     * my cert path the only encoding
     */
    private final byte[] encoding;

    /**
     * Constucts new instance of <code>MyCertPath</code>
     * 
     * @param type
     * @param encoding
     */
    public MyCertPath(byte[] encoding) {
        super("MyEncoding");
        this.encoding = encoding;
        certificates = new Vector();
        certificates.add(new MyCertificate("MyEncoding", encoding));
        encodingNames = new Vector();
        encodingNames.add("MyEncoding");
    }

    /**
     * @return certificates list
     * @see java.security.cert.CertPath#getCertificates()
     */
    public List getCertificates() {
        return Collections.unmodifiableList(certificates);
    }

    /**
     * @return default encoded form of this cert path
     * @see java.security.cert.CertPath#getEncoded()
     */
    public byte[] getEncoded() throws CertificateEncodingException {
        return encoding.clone();
    }

    /**
     * @return encoded form of this cert path as specified by
     * <code>encoding</code> parameter
     * @throws CertificateEncodingException if <code>encoding</code>
     * not equals "MyEncoding" 
     * @see java.security.cert.CertPath#getEncoded(java.lang.String)
     */
    public byte[] getEncoded(String encoding)
            throws CertificateEncodingException {
        if (getType().equals(encoding)) {
            return this.encoding.clone();
        }
        throw new CertificateEncodingException("Encoding not supported: " +
                encoding);
    }

    /**
     * @return iterator through encodings supported
     * @see java.security.cert.CertPath#getEncodings()
     */
    public Iterator getEncodings() {
        return Collections.unmodifiableCollection(encodingNames).iterator();
    }

}
