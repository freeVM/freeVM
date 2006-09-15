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

package org.apache.harmony.xnet.provider.jsse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * 
 * TrustManagerFactory service provider interface implementation.
 * 
 * @see javax.net.ssl.TrustManagerFactorySpi
 */
public class TrustManagerFactoryImpl extends TrustManagerFactorySpi {

    private KeyStore keyStore;

    /**
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(KeyStore)
     */
    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks != null) {
            keyStore = ks;
        } else {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String keyStoreName = (String) AccessController
                    .doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            return System.getProperty("javax.net.ssl.trustStore");
                        }
                    });
            String keyStorePwd = null;
            if (keyStoreName == null || keyStoreName.equalsIgnoreCase("NONE")
                    || keyStoreName.length() == 0) {
                try {
                    keyStore.load(null, null);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            } else {
                keyStorePwd = (String) AccessController
                        .doPrivileged(new java.security.PrivilegedAction() {
                            public Object run() {
                                return System
                                        .getProperty("javax.net.ssl.trustStorePassword");
                            }
                        });
                char[] pwd;
                if (keyStorePwd == null) {
                    pwd = new char[0];
                } else {
                    pwd = keyStorePwd.toCharArray();
                }
                try {
                    keyStore.load(new FileInputStream(new File(keyStoreName)), pwd);
                } catch (FileNotFoundException e) {
                    throw new KeyStoreException(e);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            }
        }

    }

    /**
     * @see javax.net.ssl.engineInit(ManagerFactoryParameters)
     */
    public void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException(
                "ManagerFactoryParameters not supported");
    }

    /**
     * @see javax.net.ssl.engineGetTrustManagers()
     */
    public TrustManager[] engineGetTrustManagers() {
        if (keyStore == null) {
            throw new IllegalStateException(
                    "TrustManagerFactory is not initialized");
        }
        return new TrustManager[] { new TrustManagerImpl(keyStore) };
    }
}