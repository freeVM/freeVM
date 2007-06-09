/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.test.func.api.java.security.cert.F_CertificateFactoryTest_02.auxiliary;

import java.security.Provider;

/**
 * Created on 25.08.2005
 */
public class F_CertificateFactoryProvider extends Provider {

    /**
     * @param name
     * @param version
     * @param info
     */
    public F_CertificateFactoryProvider(String name, double version, String info) {
        super(name, version, info);
        put("CertificateFactory.X.509", "org.apache.harmony.test.func.api.java.security.cert.F_CertificateFactoryTest_02.auxiliary.F_CertificateFactorySPImplementation");
        put("CertificateFactory.X.509 ImplementedIn", "Software");
        System.out.println("F_CertificateFactoryProvider ctor has been called");
    }

}
