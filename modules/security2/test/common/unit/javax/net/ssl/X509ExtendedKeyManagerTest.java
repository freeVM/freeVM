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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509ExtendedKeyManager;

import junit.framework.TestCase;


/**
 * Tests for <code>X509ExtendedKeyManager</code> class constructors and methods.
 * 
 */
public class X509ExtendedKeyManagerTest extends TestCase {

    public final void testChooseEngineClientAlias() {
        X509ExtendedKeyManager km = new MyX509ExtendedKeyManager();
        if (km.chooseEngineClientAlias(null, null, null) != null) {
            fail("non null result");
        }
    }

    public final void testChooseEngineServerAlias() {
        X509ExtendedKeyManager km = new MyX509ExtendedKeyManager();
        if (km.chooseEngineServerAlias(null, null, null) != null) {
            fail("non null result");
        }
    }

}

class MyX509ExtendedKeyManager extends X509ExtendedKeyManager {

    /*
     * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[],
     *      java.security.Principal[], java.net.Socket)
     */
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        return null;
    }

    /*
     * @see javax.net.ssl.X509KeyManager#chooseServerAlias(java.lang.String,
     *      java.security.Principal[], java.net.Socket)
     */
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        return null;
    }

    /*
     * @see javax.net.ssl.X509KeyManager#getCertificateChain(java.lang.String)
     */
    public X509Certificate[] getCertificateChain(String alias) {
        return null;
    }

    /*
     * @see javax.net.ssl.X509KeyManager#getClientAliases(java.lang.String,
     *      java.security.Principal[])
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return null;
    }

    /*
     * @see javax.net.ssl.X509KeyManager#getServerAliases(java.lang.String,
     *      java.security.Principal[])
     */
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
    }

    /*
     * @see javax.net.ssl.X509KeyManager#getPrivateKey(java.lang.String)
     */
    public PrivateKey getPrivateKey(String alias) {
        return null;
    }

}
