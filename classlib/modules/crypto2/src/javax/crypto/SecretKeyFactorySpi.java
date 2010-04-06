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

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * 
 * 
 * @author Diego Ra�l Mercado
 * @version 1.2
 * @ar.org.fitc.spec_ref
 */
public abstract class SecretKeyFactorySpi {

    /** @ar.org.fitc.spec_ref */
    public SecretKeyFactorySpi() {
    }

    /** @ar.org.fitc.spec_ref */
    protected abstract SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException;

    /** @ar.org.fitc.spec_ref */
    protected abstract KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException;

    /** @ar.org.fitc.spec_ref */
    protected abstract SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException;
}
