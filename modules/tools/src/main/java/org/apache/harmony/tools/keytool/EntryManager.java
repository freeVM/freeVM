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

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

/**
 * Class for managing keystore entries - cloning, deleting, changing entry
 * password.
 */
public class EntryManager {
    /**
     * Copies the private key and the certificate chain from the keystore entry
     * identifiad by given alias into a newly created one with given destination
     * alias. alias and destination alias are specified in param.
     * 
     * @param param
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeytoolException
     */
    static void keyClone(KeytoolParameters param) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException,
            KeytoolException {
        KeyStore keyStore = param.getKeyStore();
        String alias = param.getAlias();
        Key srcKey;
        try {
            srcKey = keyStore.getKey(alias, param.getKeyPass());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(
                    "Cannot find the algorithm to recover the key. ", e);
        }
        // if the entry is a not a KeyEntry
        if (srcKey == null) {
            throw new KeytoolException("The entry <" + alias + "> has no key.");
        }
        Certificate[] certChain = keyStore
                .getCertificateChain(param.getAlias());
        keyStore.setKeyEntry(param.getDestAlias(), srcKey,
                param.getNewPasswd(), certChain);
        param.setNeedSaveKS(true);
    }

    /**
     * Removes from the keystore the entry associated with alias.
     * 
     * @param param
     * @throws KeyStoreException
     */
    static void delete(KeytoolParameters param) throws KeyStoreException {
        param.getKeyStore().deleteEntry(param.getAlias());
        param.setNeedSaveKS(true);
    }

    /**
     * Changes the key password to the new one.
     * 
     * @param param
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    static void keyPasswd(KeytoolParameters param) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = param.getKeyStore();
        String alias = param.getAlias();
        Key key;
        Certificate[] chain;
        try {
            key = keyStore.getKey(alias, param.getKeyPass());
            chain = keyStore.getCertificateChain(alias);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(
                    "Cannot find the algorithm to recover the key. ", e);
        }

        keyStore.deleteEntry(alias);
        keyStore.setKeyEntry(alias, key, param.getNewPasswd(), chain);
        param.setKeyPass(param.getNewPasswd());
        param.setNeedSaveKS(true);
    }

}

