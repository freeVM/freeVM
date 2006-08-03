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

package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import org.apache.harmony.security.fortress.Engine;

public class ExemptionMechanism {

    // Store spi implementation service name
    private static final String SERVICE = "ExemptionMechanism";

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Warning for reporting about not initializes ExemptionMechanism
    private static final String NOTINITEMECH = "ExemptionMechanism is not initialized";

    // Store used provider
    private final Provider provider;

    // Store used spi implementation
    private final ExemptionMechanismSpi spiImpl;

    // Store mechanism name
    private final String mechanism;

    // Store state (initialized or not)
    private boolean isInit;

    // Store initKey value
    private Key initKey;

    // Indicates if blob generated successfully
    private boolean generated;

    protected ExemptionMechanism(ExemptionMechanismSpi exmechSpi,
            Provider provider, String mechanism) {
        this.mechanism = mechanism;
        this.spiImpl = exmechSpi;
        this.provider = provider;
        isInit = false;
    }

    public final String getName() {
        return mechanism;
    }

    public static final ExemptionMechanism getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new ExemptionMechanism((ExemptionMechanismSpi) engine.spi,
                    engine.provider, algorithm);
        }
    }

    public static final ExemptionMechanism getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        if (algorithm == null) {
            throw new NullPointerException("Algorithm is null");
        }
        return getInstance(algorithm, impProvider);
    }

    public static final ExemptionMechanism getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm is null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new ExemptionMechanism((ExemptionMechanismSpi) engine.spi,
                    provider, algorithm);
        }
    }

    public final Provider getProvider() {
        return provider;
    }

    public final boolean isCryptoAllowed(Key key)
            throws ExemptionMechanismException {

        if (generated
                && (initKey.equals(key) || Arrays.equals(initKey.getEncoded(),
                        key.getEncoded()))) {
            return true;
        }
        return false;
    }

    public final int getOutputSize(int inputLen) throws IllegalStateException {
        if (!isInit) {
            throw new IllegalStateException(NOTINITEMECH);
        }
        return spiImpl.engineGetOutputSize(inputLen);
    }

    public final void init(Key key) throws InvalidKeyException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key);
        initKey = key;
        isInit = true;
    }

    public final void init(Key key, AlgorithmParameters param)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key, param);
        initKey = key;
        isInit = true;
    }

    public final void init(Key key, AlgorithmParameterSpec param)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key, param);
        initKey = key;
        isInit = true;
    }

    public final byte[] genExemptionBlob() throws IllegalStateException,
            ExemptionMechanismException {
        if (!isInit) {
            throw new IllegalStateException(NOTINITEMECH);
        }
        generated = false;
        byte[] result = spiImpl.engineGenExemptionBlob();
        generated = true;
        return result;
    }

    public final int genExemptionBlob(byte[] output)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        return genExemptionBlob(output, 0);
    }

    public final int genExemptionBlob(byte[] output, int outputOffset)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        if (!isInit) {
            throw new IllegalStateException(NOTINITEMECH);
        }
        generated = false;
        int len = spiImpl.engineGenExemptionBlob(output, outputOffset);
        generated = true;
        return len;
    }

    protected void finalize() {
        initKey = null;
    }
}