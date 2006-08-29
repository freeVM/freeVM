/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.harmony.auth.internal.kerberos.v5;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1StringType;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * Kerberos PrincipalName type.
 * 
 * @see http://www.ietf.org/rfc/rfc4120.txt
 */
public class PrincipalName {

    public static final int NT_UNKNOWN = 0;

    public static final int NT_PRINCIPAL = 1;

    public static final int NT_SRV_INST = 2;

    public static final int NT_SRV_HST = 3;

    public static final int NT_SRV_XHST = 4;

    public static final int NT_UID = 5;

    public static final int NT_X500_PRINCIPAL = 6;

    public static final int NT_SMTP_NAME = 7;

    public static final int NT_ENTERPRISE = 10;

    private final int type;

    private final String name[];

    public PrincipalName(int type, String[] name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String[] getName() {
        return name;
    }

    /** PrincipalName ::= SEQUENCE {
     *      name-type   [0] Int32,
     *      name-string [1] SEQUENCE OF KerberosString
     *  }
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Explicit(0, ASN1Integer.getInstance()),
            new ASN1Explicit(1,
                    new ASN1SequenceOf(ASN1StringType.GENERALSTRING)), }) {

        protected Object getDecodedObject(BerInputStream in) throws IOException {

            Object[] values = (Object[]) in.content;

            int type = ASN1Integer.toIntValue(values[0]);
            
            // TODO: list to array conversion should be done by framework
            List list = (List) values[1];
            String[] name = new String[list.size()];
            list.toArray(name);

            return new PrincipalName(type, name);
        }

        protected void getValues(Object object, Object[] values) {

            PrincipalName name = (PrincipalName) object;

            values[0] = BigInteger.valueOf(name.getType()).toByteArray();
            
            values[1] = Arrays.asList(name.getName());
        }
    };
}
