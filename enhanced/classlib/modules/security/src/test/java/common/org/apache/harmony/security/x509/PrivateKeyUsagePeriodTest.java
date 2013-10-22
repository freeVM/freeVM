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

package org.apache.harmony.security.x509;

import java.util.Date;

import org.apache.harmony.security.x509.PrivateKeyUsagePeriod;

import junit.framework.TestCase;

public class PrivateKeyUsagePeriodTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PrivateKeyUsagePeriodTest.class);
    }

    public void testEncodeDecode() throws Exception {

        Date notBeforeDate = new Date(200000000);
        Date notAfterDate = new Date(300000000);

        PrivateKeyUsagePeriod pkup = new PrivateKeyUsagePeriod(notBeforeDate,
                notAfterDate);

        byte[] encoded = pkup.getEncoded();

        pkup = (PrivateKeyUsagePeriod) PrivateKeyUsagePeriod.ASN1
                .decode(encoded);

        assertEquals("notBeforeDate", notBeforeDate, pkup.getNotBefore());
        assertEquals("notAfterDate", notAfterDate, pkup.getNotAfter());
    }
}