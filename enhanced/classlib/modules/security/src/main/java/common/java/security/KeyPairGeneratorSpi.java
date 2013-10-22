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

import java.security.spec.AlgorithmParameterSpec;

/**
 * @com.intel.drl.spec_ref
 * 
 */

public abstract class KeyPairGeneratorSpi {
    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public KeyPairGeneratorSpi() {
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public abstract KeyPair generateKeyPair();

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public abstract void initialize(int keysize, SecureRandom random);

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException(
                "Method initialize(AlgorithmParameterSpec params, SecureRandom random)is not supported");
    }
}