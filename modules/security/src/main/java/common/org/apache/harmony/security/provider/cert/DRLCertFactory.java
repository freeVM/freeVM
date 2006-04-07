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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.provider.cert;

import java.security.AccessController;
import java.security.Provider;


/**
 * DRLCertFactory
 */
public final class DRLCertFactory extends Provider {

    public DRLCertFactory() {
        super("DRLCertFactory", 1.0, "DRL Certificate Factory");
        AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                put("CertificateFactory.X509", 
                    "org.apache.harmony.security.provider.cert.X509CertFactoryImpl");
                put("Alg.Alias.CertificateFactory.X.509", "X509");
                    return null;
            }
        });
    }
}

