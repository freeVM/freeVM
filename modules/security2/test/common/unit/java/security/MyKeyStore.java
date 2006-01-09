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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Additional class for KeyStoreSpi and KeyStore verification
 * 
 */

public class MyKeyStore extends KeyStoreSpi {
    private Hashtable Keys = new Hashtable();

    private Hashtable Cert = new Hashtable();

    private Hashtable Chain = new Hashtable();

    private Hashtable Dates = new Hashtable();

    private Hashtable KeysSL = new Hashtable();

    private Hashtable CertSL = new Hashtable();

    private Hashtable ChainSL = new Hashtable();

    private Hashtable DatesSL = new Hashtable();

    public Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (Keys.containsKey(alias)) {
            return (Key) Keys.get(alias);
        }
        return null;
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        if (Chain.containsKey(alias)) {
            return (Certificate[]) Chain.get(alias);
        }
        return null;
    }

    public Certificate engineGetCertificate(String alias) {
        if (Cert.containsKey(alias)) {
            return (Certificate) Cert.get(alias);
        }
        return null;
    }

    public Date engineGetCreationDate(String alias) {
        if (Dates.containsKey(alias)) {
            return (Date) Dates.get(alias);
        }
        return null;
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
        Keys.put(alias, key);
        if (chain != null) {
            Chain.put(alias, chain);
        }
        Dates.put(alias, new Date());
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        if (key == null) {
            throw new KeyStoreException("Not Supported for null key");
        }
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
        if (Chain.containsKey(alias)) {
            Chain.remove(alias);
        }
        KeyStoreTest1.MyPrivateKey keyK = new KeyStoreTest1("proba").new MyPrivateKey(
                alias, alias, key);
        Keys.put(alias, keyK);
        if (chain != null) {
            Chain.put(alias, chain);
        }
        Dates.put(alias, new Date());

    }

    public void engineSetCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        Cert.put(alias, cert);
        Dates.put(alias, new Date());
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (Keys.containsKey(alias)) {
            Keys.remove(alias);
            Chain.remove(alias);
            return;
        }
        if (Cert.containsKey(alias)) {
            Cert.remove(alias);
        }
    }

    public Enumeration engineAliases() {
        return null;
    }

    public boolean engineContainsAlias(String alias) {
        if (Keys.containsKey(alias)) {
            return true;
        }
        if (Cert.containsKey(alias)) {
            return true;
        }
        return false;
    }

    public int engineSize() {
        return (Keys.size() + Cert.size());
    }

    public boolean engineIsKeyEntry(String alias) {
        if (Keys.containsKey(alias)) {
            return true;
        }
        return false;
    }

    public boolean engineIsCertificateEntry(String alias) {
        if (Cert.containsKey(alias)) {
            return true;
        }
        return false;
    }

    public String engineGetCertificateAlias(Certificate cert) {
        return "";
    }

    public void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(stream instanceof ByteArrayOutputStream)) {
            throw new IOException("Incorrect stream");
        }
        String alias;
        Enumeration e = Keys.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            KeysSL.put(alias, (Key) Keys.get(alias));
            DatesSL.put(alias, (Date) Dates.get(alias));
            if (Chain.containsKey(alias)) {
                ChainSL.put(alias, (Certificate[]) Chain.get(alias));
            }
        }
        e = Cert.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            CertSL.put(alias, (Certificate) Cert.get(alias));
            DatesSL.put(alias, (Date) Dates.get(alias));
        }
    }

    public void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        Keys.clear();
        Cert.clear();
        Chain.clear();
        Dates.clear();
        String alias;
        Enumeration e = KeysSL.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            Keys.put(alias, (Key) KeysSL.get(alias));
            Dates.put(alias, (Date) DatesSL.get(alias));
            if (ChainSL.containsKey(alias)) {
                Chain.put(alias, (Certificate[]) ChainSL.get(alias));
            }
        }
        e = CertSL.keys();
        while (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            Cert.put(alias, (Certificate) CertSL.get(alias));
            Dates.put(alias, (Date) DatesSL.get(alias));
        }
    }

    public void engineStore(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            throw new IOException("param is null");
        }
    }

    public void engineLoad(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(param instanceof MyLoadStoreParams)) {
            throw new IllegalArgumentException("param is not MyLoadStoreParams: " + param);
        }
    }
}