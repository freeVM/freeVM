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

import junit.framework.TestCase;

import org.apache.harmony.jndi.provider.ldap.asn1.ASN1TestUtils;
import org.apache.harmony.jndi.provider.ldap.asn1.LdapASN1Constant;

public class AddOpTest extends TestCase {
    public void test_encodeValues_$LObject() {
        String entry = "add entry";
        AddOp op = new AddOp(entry);

        ASN1TestUtils.checkEncode(op.getRequest(), LdapASN1Constant.AddRequest);

        LdapAttribute attr = new LdapAttribute("attr1");

        op.addAttribute(attr);
        ASN1TestUtils.checkEncode(op.getRequest(), LdapASN1Constant.AddRequest);

        attr.add("value");
        attr.add("test");
        ASN1TestUtils.checkEncode(op.getRequest(), LdapASN1Constant.AddRequest);
    }
}
