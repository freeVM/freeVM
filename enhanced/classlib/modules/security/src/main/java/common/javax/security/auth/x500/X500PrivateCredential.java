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
* @author Alexander V. Esin
* @version $Revision$
*/

package javax.security.auth.x500;

import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import javax.security.auth.Destroyable;

/**
 * @com.intel.drl.spec_ref
 */
public final class X500PrivateCredential implements Destroyable {

    //X509 certificate
    private X509Certificate cert;

    //Private key
    private PrivateKey key;

    //Alias
    private String alias;

    /**
     * @com.intel.drl.spec_ref
     */
    public X500PrivateCredential(X509Certificate cert, PrivateKey key) {
        if (cert == null) {
            throw new IllegalArgumentException("X509 certificate is null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Private key is null");
        }
        this.cert = cert;
        this.key = key;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public X500PrivateCredential(X509Certificate cert, PrivateKey key,
            String alias) {
        this(cert, key);
        if (alias == null) {
            throw new IllegalArgumentException("Alias is null");
        }
        this.alias = alias;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public X509Certificate getCertificate() {
        return cert;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PrivateKey getPrivateKey() {
        return key;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void destroy() {
        cert = null;
        key = null;
        alias = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean isDestroyed() {
        return (cert == null && key == null && alias == null);
    }
}