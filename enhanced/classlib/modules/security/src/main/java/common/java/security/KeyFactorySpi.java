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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package java.security;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public abstract class KeyFactorySpi {
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract PublicKey engineGeneratePublic(KeySpec keySpec) 
                                    throws InvalidKeySpecException;
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract PrivateKey engineGeneratePrivate(KeySpec keySpec)
                                    throws InvalidKeySpecException;
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract KeySpec engineGetKeySpec(Key key, Class keySpec)
                                    throws InvalidKeySpecException;
    //FIXME 1.5 signature: protected abstract <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract Key engineTranslateKey(Key key) throws InvalidKeyException;

}
