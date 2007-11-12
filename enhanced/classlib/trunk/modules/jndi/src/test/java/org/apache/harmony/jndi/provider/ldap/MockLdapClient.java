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

package org.apache.harmony.jndi.provider.ldap;

import java.io.IOException;

import javax.naming.ldap.Control;

import org.apache.harmony.jndi.provider.ldap.asn1.ASN1Decodable;
import org.apache.harmony.jndi.provider.ldap.asn1.ASN1Encodable;
import org.apache.harmony.jndi.provider.ldap.asn1.Utils;
import org.apache.harmony.security.asn1.ASN1Integer;

public class MockLdapClient extends LdapClient {

    private ASN1Encodable request;

    private ASN1Decodable response;

    public MockLdapClient() {
        super();
    }

    @Override
    public LdapMessage doOperation(int opIndex, ASN1Encodable requestOp,
            ASN1Decodable responseOp, Control[] controls) throws IOException {
        request = requestOp;
        response = responseOp;
        if (response instanceof LdapResult) {
            LdapResult result = (LdapResult) response;
            Object[] values = new Object[] { ASN1Integer.fromIntValue(0),
                    Utils.getBytes(""), Utils.getBytes(""), null };
            result.decodeValues(values);
        }
        return new LdapMessage(response);
    }

    @Override
    public void doOperationWithoutResponse(int opIndex, ASN1Encodable op,
            Control[] controls) throws IOException {
        request = op;
    }

    public ASN1Encodable getRequest() {
        return request;
    }

    public ASN1Decodable getResponse() {
        return response;
    }

    public void setResponse(ASN1Decodable response) {
        this.response = response;
    }

}
