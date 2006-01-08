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

import java.security.InvalidAlgorithmParameterException;
import java.util.Collection;

/**
 * Additional class for verification CertStoreSpi
 * and CertStore
 * 
 */

public class MyCertStoreSpi extends CertStoreSpi {
    
    public MyCertStoreSpi(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
        super(params);        
        if (!(params instanceof MyCertStoreParameters)) {
            throw new InvalidAlgorithmParameterException("Invalid params");
        }
    }

    public Collection engineGetCertificates(CertSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }

    public Collection engineGetCRLs(CRLSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }
}
