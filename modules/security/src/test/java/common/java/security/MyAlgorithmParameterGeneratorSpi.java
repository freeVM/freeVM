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
 * Additional class for verification AlgorithmParameterGeneratorSpi and
 * AlgorithmParameterGenerator classes
 * 
 */

public class MyAlgorithmParameterGeneratorSpi 
        extends AlgorithmParameterGeneratorSpi {
    
    protected void engineInit(int keysize, SecureRandom random) {
        if (keysize < 0) {
            throw new IllegalArgumentException("keysize < 0");
        }
    }

    protected void engineInit(AlgorithmParameterSpec genParamSpec,
            SecureRandom random) {
        if (random == null) {
            throw new IllegalArgumentException("random is null");
        }
    }
    
    protected AlgorithmParameters engineGenerateParameters() {
        return null;
    }
}