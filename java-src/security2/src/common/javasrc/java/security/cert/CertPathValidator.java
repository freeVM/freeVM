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

package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import com.openintel.fortress.drl.security.Engine;

/**
 * @com.intel.drl.spec_ref
 * 
 */

public class CertPathValidator {
    // Store CertPathValidator implementation service name
    private static final String SERVICE = "CertPathValidator";

    // Used to accesess common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTYNAME = "certpathvalidator.type";

    // Default value of CertPathBuilder type. It returns if certpathbuild.type
    // property is not defined in java.security file
    private static final String DEFAULTPROPERTY = "PKIX";

    // Store used provider
    private final Provider provider;

    // Store used spi implementation
    private final CertPathValidatorSpi spiImpl;

    // Store used algorithm value
    private final String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected CertPathValidator(CertPathValidatorSpi validatorSpi,
            Provider provider, String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = validatorSpi;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null
     */
    public static CertPathValidator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new CertPathValidator((CertPathValidatorSpi) engine.spi,
                    engine.provider, algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null
     */
    public static CertPathValidator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null or empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }

    /**
     * @com.intel.drl.spec_ref*
     * 
     * throws NullPointerException if algorithm is null
     */
    public static CertPathValidator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new CertPathValidator((CertPathValidatorSpi) engine.spi,
                    provider, algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public final CertPathValidatorResult validate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException {
        return spiImpl.engineValidate(certPath, params);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public static final String getDefaultType() {
        String defaultType = (String) AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                }
            );
        return (defaultType != null ? defaultType : DEFAULTPROPERTY);
    }
}