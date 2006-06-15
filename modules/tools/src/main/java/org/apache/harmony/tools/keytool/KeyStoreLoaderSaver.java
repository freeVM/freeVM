/*
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tools.keytool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

/**
 * Class for loading keystores, saving ang changing the main keystore password.
 */
public class KeyStoreLoaderSaver {

    /**
     * Creates an instance of class KeyStore and loads a keystore to it.
     * param.getStorePass() is used to check the integrity of the keystore. If
     * the password is not set in param, the integrity is not checked. If the
     * path to the store is not defined an empty keystore is created.
     * 
     * @param param - KeytoolParameters object which is used to get path to the
     *        store and password to unlock it or check its integrity.
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws KeyStoreException
     * @throws KeytoolException
     * @throws NoSuchProviderException
     */
    static void loadStore(KeytoolParameters param)
            throws NoSuchAlgorithmException, CertificateException,
            FileNotFoundException, IOException, KeyStoreException,
            NoSuchProviderException, KeytoolException {

        // If the path to the store is not specified, try to open
        // the store using the default path.
        if (param.getStorePath() == null) {
            param.setStorePath(KeytoolParameters.defaultKeystorePath);
        }
        KeyStore keyStore;
        if (new File(param.getStorePath()).exists()) {
            // load an existing store
            keyStore = loadStore(param.getStorePath(), param.getStoreType(),
                    param.getStorePass(), param.getProvider());
        } else {
            // create a new store if it doesn't exist
            keyStore = loadStore(null, param.getStoreType(), param
                    .getStorePass(), param.getProvider());
            param.setNeedSaveKS(true);
        }
        param.setKeyStore(keyStore);
    }

    /**
     * Creates an instance of class KeyStore and loads a keystore of the
     * specified type to it. If the type is not specified the default one is
     * used. Password storePass is used to check the integrity of the keystore.
     * If the password is null, the integrity is not checked. If the path to the
     * store is not defined, an empty keystore is created.
     * 
     * @param path
     * @param storeType
     * @param storePass
     * @param providerName
     * @return
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws KeytoolException
     */
    static KeyStore loadStore(String path, String storeType, char[] storePass,
            String providerName) throws FileNotFoundException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, NoSuchProviderException, KeytoolException {

        FileInputStream fis;
        // if the path is given, make a FileInputStream on it
        if (path != null) {
            File ksFile = new File(path);
            if (ksFile.length() == 0) {
                throw new KeytoolException("Keystore file exists but is empty");
            }
            fis = new FileInputStream(ksFile);
        } else { // if the path is not given, a new keystore will be created
            fis = null;
        }

        // Set the store type to default if it is not given.
        // The default value is set up in java.security file.
        String checkedStoreType = (storeType != null) ? storeType : KeyStore
                .getDefaultType();
        KeyStore keyStore;
        // if the provider name is not set
        if (providerName == null) {
            keyStore = KeyStore.getInstance(checkedStoreType);
        } else {
            try {
                keyStore = KeyStore.getInstance(storeType, providerName);
            } catch (NoSuchProviderException e) {
                throw (NoSuchProviderException) new NoSuchProviderException(
                        "The provider " + providerName
                                + " is not found in the environment.")
                        .initCause(e);
            }
        }

        try {
            // try to load the keystore
            keyStore.load(fis, storePass);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(
                    "Failed to find the algorithm to check the keystore integrity",
                    e);
        } catch (CertificateException e) {
            throw new CertificateException(
                    "Failed to load a certificate from the keystore. ", e);
        } catch (IOException e) {
            throw (IOException) new IOException("Failed to load the keystore. ")
                    .initCause(e);
        }
        return keyStore;
    }

    /**
     * Saves the keystore to the file and protects its integrity with password.
     * 
     * @param param
     */
    static void saveStore(KeytoolParameters param) {
        // TODO
        throw new RuntimeException("The method is not implemented yet.");
    }

    /**
     * Changes the keystore password to the new one.
     * 
     * @param param
     */
    static void storePasswd(KeytoolParameters param) {
        // TODO
        throw new RuntimeException("The method is not implemented yet.");
    }
}
