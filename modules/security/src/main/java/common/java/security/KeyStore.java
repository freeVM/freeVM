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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.CallbackHandler;

import org.apache.harmony.security.fortress.Engine;


public class KeyStore {

    // Store KeyStore SERVICE name
    private static final String SERVICE = "KeyStore";

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    //  Store KeyStore property name
    private static final String PROPERTYNAME = "keystore.type";

    //  Store default KeyStore type
    private static final String DEFAULT_KEYSTORE_TYPE = "jks";

    // Message to report about non-initialized key store object
    private static final String NOTINITKEYSTORE = "KeyStore was not initialized";

    // Store KeyStore state (initialized or not)
    private boolean isInit;

    // Store used KeyStoreSpi
    private final KeyStoreSpi implSpi;

    // Store used provider
    private final Provider provider;

    // Store used type
    private final String type;

    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
        this.type = type;
        this.provider = provider;
        this.implSpi = keyStoreSpi;
        isInit = false;
    }

    /**
     * @throws NullPointerException if type is null
     */
    public static KeyStore getInstance(String type) throws KeyStoreException {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        synchronized (engine) {
            try {
                engine.getInstance(type, null);
                return new KeyStore((KeyStoreSpi) engine.spi, engine.provider, type);
            } catch (NoSuchAlgorithmException e) {
                throw new KeyStoreException(e.getMessage());
            }
        }
    }

    /**
     * 
     * 
     * @throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyStore getInstance(String type, String provider)
            throws KeyStoreException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null or empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        try {
            return getInstance(type, impProvider);
        } catch (Exception e) {
            throw new KeyStoreException(e.getMessage(), e);
        }
    }

    /**
     * 
     * 
     * throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyStore getInstance(String type, Provider provider)
            throws KeyStoreException {
        // check parameters
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        // return KeyStore instance
        synchronized (engine) {
            try {
                engine.getInstance(type, provider, null);
                return new KeyStore((KeyStoreSpi) engine.spi, provider, type);
            } catch (Exception e) {
            // override exception
                throw new KeyStoreException(e.getMessage());
            }
        }
    }

    /**
     * 
     *  
     */
    public static final String getDefaultType() {
        String dt = AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                }
            );
        return (dt == null ? DEFAULT_KEYSTORE_TYPE : dt);
    }

    /**
     * 
     *  
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * 
     *  
     */
    public final String getType() {
        return type;
    }

    /**
     * 
     *  
     */
    public final Key getKey(String alias, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetKey(alias, password);
    }

    /**
     * 
     *  
     */
    public final Certificate[] getCertificateChain(String alias)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetCertificateChain(alias);
    }

    /**
     * 
     *  
     */
    public final Certificate getCertificate(String alias)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetCertificate(alias);
    }

    /**
     * 
     *  
     */
    public final Date getCreationDate(String alias) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetCreationDate(alias);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerException -
     * when alias is null IllegalArgumentException - when password is null
     * IllegalArgumentException - when key is instance of PrivateKey and chain
     * is null or empty
     */
    public final void setKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        if (key == null) {
            throw new KeyStoreException("key is null");
        }
        // Certificate chain is required for PrivateKey
        if ((key instanceof PrivateKey)
                && ((chain == null) || chain.length == 0)) {
            throw new KeyStoreException(
                    "Certificate chain is not defined for Private key ");
        }
        implSpi.engineSetKeyEntry(alias, key, password, chain);
    }

    /**
     * 
     *  
     */
    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        implSpi.engineSetKeyEntry(alias, key, chain);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerExcedption
     * when alias is null
     */
    public final void setCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        implSpi.engineSetCertificateEntry(alias, cert);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw NullPointerExcedption when alias is null
     */
    public final void deleteEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        implSpi.engineDeleteEntry(alias);
    }

    /**
     * 
     */
    public final Enumeration<String> aliases() throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineAliases();
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerException when
     * alias is null
     */
    public final boolean containsAlias(String alias) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        return implSpi.engineContainsAlias(alias);
    }

    /**
     * 
     *  
     */
    public final int size() throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineSize();
    }

    /**
     * 
     * 
     * jdk1.4.2 and 1.5 releases throw unspecified NullPointerException
     * when alias is null
     */
    public final boolean isKeyEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        return implSpi.engineIsKeyEntry(alias);
    }

    /**
     * 
     * 
     * jdk1.4.2 and 1.5 releases throw unspecified NullPointerException
     * when alias is null
     */
    public final boolean isCertificateEntry(String alias)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        return implSpi.engineIsCertificateEntry(alias);
    }

    /**
     * 
     *  
     */
    public final String getCertificateAlias(Certificate cert)
            throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetCertificateAlias(cert);
    }

    /**
     * 
     * 
     * throws IOException when stream or password is null
     */
    public final void store(OutputStream stream, char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (stream == null) {
            throw new IOException("stream is null");
        }
        if (password == null) {
            throw new IOException("password is null");
        }
        implSpi.engineStore(stream, password);
    }

    /**
     * 
     *  
     */
    public final void store(LoadStoreParameter param) throws KeyStoreException,
            IOException, NoSuchAlgorithmException, CertificateException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (param == null) {
            throw new IOException("LoadSroreParameter is null");
        }
        implSpi.engineStore(param);
    }

    /**
     * 
     *  
     */
    public final void load(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        implSpi.engineLoad(stream, password);
        isInit = true;
    }

    /**
     * 
     *  
     */
    public final void load(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            throw new IOException("LoadSroreParameter is null");
        }
        implSpi.engineLoad(param);
        isInit = true;
    }

    /**
     * 
     *  
     */
    public final Entry getEntry(String alias, ProtectionParameter param)
            throws NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineGetEntry(alias, param);
    }

    /**
     * 
     * 
     * 1.5 release throws unspecified NullPointerExcedption when alias or
     * entry is null
     */
    public final void setEntry(String alias, Entry entry,
            ProtectionParameter param) throws KeyStoreException {
        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        if (entry == null) {
            throw new NullPointerException("entry is null");
        }
        implSpi.engineSetEntry(alias, entry, param);
    }

    /**
     * 
     */
    public final boolean entryInstanceOf(String alias, 
            Class<? extends KeyStore.Entry> entryClass)
            throws KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("alias is null");
        }
        if (entryClass == null) {
            throw new NullPointerException("entryClass is null");
        }

        if (!isInit) {
            throw new KeyStoreException(NOTINITKEYSTORE);
        }
        return implSpi.engineEntryInstanceOf(alias, entryClass);
    }

    /**
     * 
     * 
     * 
     */
    public abstract static class Builder {
        /**
         * 
         *  
         */
        protected Builder() {
        }

        /**
         * 
         *  
         */
        public abstract KeyStore getKeyStore() throws KeyStoreException;

        /**
         * 
         *  
         */
        public abstract ProtectionParameter getProtectionParameter(String alise)
                throws KeyStoreException;

        /**
         * 
         *  
         */
        public static Builder newInstance(KeyStore keyStore,
                ProtectionParameter protectionParameter) {
            if (keyStore == null) {
                throw new NullPointerException("keystore is null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter is null");
            }

            if (!keyStore.isInit) {
                throw new IllegalArgumentException(NOTINITKEYSTORE);
            }
            return new BuilderImpl(keyStore, protectionParameter,
                    null, null, null, null);
        }

        /**
         * 
         *  
         */
        public static Builder newInstance(String type, Provider provider,
                File file, ProtectionParameter protectionParameter) {
            // check null parameters
            if (type == null) {
                throw new NullPointerException("type  is null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter is null");
            }
            if (file == null) {
                throw new NullPointerException("file is null");
            }
            // protection parameter should be PasswordProtection or
            // CallbackHandlerProtection
            if (!(protectionParameter instanceof PasswordProtection)
                    && !(protectionParameter instanceof CallbackHandlerProtection)) {
                throw new IllegalArgumentException(
                        "protectionParameter is neither PasswordProtection nor CallbackHandlerProtection instance");
            }
            // check file parameter
            if (!file.exists()) {
                throw new IllegalArgumentException("File: " + file.getName()
                        + " does not exist");
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException(file.getName()
                        + " does not refer to a normal file");
            }
            // create new instance
            return new BuilderImpl(null, protectionParameter, file,
                    type, provider, AccessController.getContext());
        }

        /**
         * 
         *  
         */
        public static Builder newInstance(String type, Provider provider,
                ProtectionParameter protectionParameter) {
            if (type == null) {
                throw new NullPointerException("type is null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter is null");
            }
            return new BuilderImpl(null, protectionParameter, null,
                    type, provider, AccessController.getContext());
        }

        /*
         * This class is implementation of abstract class KeyStore.Builder
         * 
         * @author Vera Petrashkova
         * 
         */
        private static class BuilderImpl extends Builder {
            // Store used KeyStore
            private KeyStore keyStore;

            // Store used ProtectionParameter
            private ProtectionParameter protParameter;

            // Store used KeyStore type
            private final String typeForKeyStore;

            // Store used KeyStore provider
            private final Provider providerForKeyStore;

            // Store used file for KeyStore loading
            private final File fileForLoad;

            // Store getKeyStore method was invoked or not for KeyStoreBuilder
            private boolean isGetKeyStore = false;

            // Store last Exception in getKeyStore()
            private KeyStoreException lastException;

            // Store AccessControlContext which is used in getKeyStore() method
            private final AccessControlContext accControlContext;

            //
            // Constructor BuilderImpl initializes private fields: keyStore,
            // protParameter, typeForKeyStore providerForKeyStore fileForLoad,
            // isGetKeyStore
            //
            BuilderImpl(KeyStore ks, ProtectionParameter pp, File file,
                    String type, Provider provider, AccessControlContext context) {
                super();
                keyStore = ks;
                protParameter = pp;
                fileForLoad = file;
                typeForKeyStore = type;
                providerForKeyStore = provider;
                isGetKeyStore = false;
                lastException = null;
                accControlContext = context;
            }

            //
            // Implementation of abstract getKeyStore() method If
            // KeyStoreBuilder encapsulates KeyStore object then this object is
            // returned
            // 
            // If KeyStoreBuilder encapsulates KeyStore type and provider then
            // KeyStore is created using these parameters. If KeyStoreBuilder
            // encapsulates file and ProtectionParameter then KeyStore data are
            // loaded from FileInputStream that is created on file. If file is
            // not defined then KeyStore object is initialized with null
            // InputStream and null password.
            // 
            // Result KeyStore object is returned.
            //
            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                // If KeyStore was created but in final block some exception was
                // thrown
                // then it was stored in lastException variable and will be
                // thrown
                // all subsequent calls of this method.
                if (lastException != null) {
                    throw lastException;
                }
                if (keyStore != null) {
                    isGetKeyStore = true;
                    return keyStore;
                }

                try {
                    final KeyStore ks;
                    final char[] passwd;

                    // get KeyStore instance using type or type and provider
                    ks = (providerForKeyStore == null ? KeyStore
                            .getInstance(typeForKeyStore) : KeyStore
                            .getInstance(typeForKeyStore, providerForKeyStore));
                    // protection parameter should be PasswordProtection
                    // or CallbackHandlerProtection
                    if (protParameter instanceof PasswordProtection) {
                        passwd = ((PasswordProtection) protParameter)
                                .getPassword();
                    } else if (protParameter instanceof CallbackHandlerProtection) {
                        passwd = KeyStoreSpi
                                .getPasswordFromCallBack(protParameter);
                    } else {
                        throw new KeyStoreException(
                                "ProtectionParameter object is not PasswordProtection "
                                        + "and  CallbackHandlerProtection");
                    }

                    // load KeyStore from file
                    AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Object>() {
                                public Object run() throws Exception {
                                    if (fileForLoad != null) {
                                        FileInputStream fis = null;
                                        try {
                                            fis = new FileInputStream(fileForLoad);
                                            ks.load(fis, passwd);
                                        } finally {
                                            // close file input stream
                                            if( fis != null ) {
                                                fis.close();   
                                            }
                                        }
                                    } else {
                                        ks.load(new TmpLSParameter(
                                                protParameter));
                                    }
                                    return null;
                                }
                            }, accControlContext);

                    
                    isGetKeyStore = true;
                    keyStore = ks;
                    return keyStore;
                } catch (KeyStoreException e) {
                    // Store exception
                    throw lastException = e;
                } catch (Exception e) {
                    // Override exception
                    throw lastException = new KeyStoreException(e);
                }
            }

            //
            // This is implementation of abstract method
            // getProtectionParameter(String alias)
            // 
            // Return: ProtectionParameter to get Entry which was saved in
            // KeyStore with defined alias
            //
            public synchronized ProtectionParameter getProtectionParameter(
                    String alias) throws KeyStoreException {
                if (alias == null) {
                    throw new NullPointerException("alias is null");
                }
                if (!isGetKeyStore) {
                    throw new IllegalStateException(
                            "getKeyStore() was not invoked");
                }
                return protParameter;
            }
        }

        /*
         * Implementation of LoadStoreParameter interface
         * 
         * @author Vera Petrashkova
         */
        private class TmpLSParameter implements LoadStoreParameter {

            // Store used protection parameter
            private final ProtectionParameter protPar;

            /**
             * Creates TmpLoadStoreParameter object
             */
            public TmpLSParameter(ProtectionParameter protPar) {
                this.protPar = protPar;
            }

            /**
             * This method returns protection parameter
             */
            public ProtectionParameter getProtectionParameter() {
                return protPar;
            }
        }
    }

    /**
     * 
     * 
     * 
     */
    public static class CallbackHandlerProtection implements
            ProtectionParameter {
        // Store CallbackHandler
        private final CallbackHandler callbackHandler;

        /**
         * 
         *  
         */
        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler is null");
            }
            this.callbackHandler = handler;
        }

        /**
         * 
         *  
         */
        public CallbackHandler getCallbackHandler() {
            return callbackHandler;
        }
    }

    /**
     * 
     * 
     * 
     */
    public static interface Entry {
    }

    /**
     * 
     * 
     * 
     */
    public static interface LoadStoreParameter {
        /**
         * 
         *  
         */
        public ProtectionParameter getProtectionParameter();
    }

    /**
     * 
     * 
     * 
     */
    public static class PasswordProtection implements ProtectionParameter,
            Destroyable {

        // Store password
        private char[] password;

        /**
         * 
         *  
         */
        public PasswordProtection(char[] password) {
            this.password = password;
        }

        /**
         * 
         *  
         */
        public synchronized char[] getPassword() {
            if (password == null) {
                throw new IllegalStateException("password was destroyed");
            }
            return password;
        }

        /**
         * 
         *  
         */
        public synchronized void destroy() throws DestroyFailedException {
            Arrays.fill(password, '\u0000');
            password = null;
        }

        /**
         * 
         *  
         */
        public synchronized boolean isDestroyed() {
            return (password == null);
        }
    }

    /**
     * 
     * 
     * 
     */
    public static interface ProtectionParameter {
    }

    /**
     * 
     * 
     * 
     */
    public static final class PrivateKeyEntry implements Entry {
        // Store Certificate chain
        private Certificate[] chain;

        // Store PrivateKey
        private PrivateKey privateKey;

        /**
         * 
         *  
         */
        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            if (privateKey == null) {
                throw new NullPointerException("privateKey is null");
            }
            if (chain == null) {
                throw new NullPointerException("chain is null");
            }

            if (chain.length == 0) {
                throw new IllegalArgumentException("chain length equals 0");
            }
            // Match algorithm of private key and algorithm of public key from
            // the end certificate
            String s = chain[0].getType();
            if (!(chain[0].getPublicKey().getAlgorithm()).equals(privateKey
                    .getAlgorithm())) {
                throw new IllegalArgumentException(
                        "Algorithm of private key does not "
                                + "match algorithm of public key in end certificate of entry "
                                + "(with index number: 0)");
            }
            // Match certificate types
            for (int i = 1; i < chain.length; i++) {
                if (!s.equals(chain[i].getType())) {
                    throw new IllegalArgumentException(
                            "Certificates from the given chain have different types");
                }
            }
            // clone chain - this.chain = (Certificate[])chain.clone();
            this.chain = new Certificate[chain.length];
            System.arraycopy(chain, 0, this.chain, 0, chain.length);
            this.privateKey = privateKey;
        }

        /**
         * 
         *  
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 
         *  
         */
        public Certificate[] getCertificateChain() {
            return chain;
        }

        /**
         * 
         *  
         */
        public Certificate getCertificate() {
            return chain[0];
        }

        /**
         * 
         *  
         */
        public String toString() {
            StringBuffer sb = new StringBuffer(
                    "PrivateKeyEntry: number of elements in certificate chain is ");
            sb.append(Integer.toString(chain.length));
            sb.append("\n");
            for (int i = 0; i < chain.length; i++) {
                sb.append(chain[i].toString());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 
     * 
     * 
     */
    public static final class SecretKeyEntry implements Entry {

        // Store SecretKey
        private final SecretKey secretKey;

        /**
         * 
         *  
         */
        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("secretKey is null");
            }
            this.secretKey = secretKey;
        }

        /**
         * 
         *  
         */
        public SecretKey getSecretKey() {
            return secretKey;
        }

        /**
         * 
         *  
         */
        public String toString() {
            StringBuffer sb = new StringBuffer("SecretKeyEntry: algorithm - ");
            sb.append(secretKey.getAlgorithm());
            return sb.toString();
        }
    }

    /**
     * 
     * 
     * 
     */
    public static final class TrustedCertificateEntry implements Entry {

        // Store trusted Certificate
        private final Certificate trustCertificate;

        /**
         * 
         *  
         */
        public TrustedCertificateEntry(Certificate trustCertificate) {
            if (trustCertificate == null) {
                throw new NullPointerException("trustCertificate is null");
            }
            this.trustCertificate = trustCertificate;
        }

        /**
         * 
         *  
         */
        public Certificate getTrustedCertificate() {
            return trustCertificate;
        }

        /**
         * 
         *  
         */
        public String toString() {
            return "TrustedCertificateEntry: \n".concat(trustCertificate
                    .toString());
        }
    }
}
