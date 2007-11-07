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

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.ldap.LdapName;

import junit.framework.TestCase;

public class LdapContextImplTest extends TestCase {
    private LdapContextImpl context;

    public void test_composeName_LNameLName() throws Exception {
        context = new LdapContextImpl(new LdapClient(), null, "");
        Name name = new LdapName("cn=happy,dc=test");
        Name prefix = new LdapName("o=harmony");
        Name result = context.composeName(name, prefix);
        assertTrue(result instanceof LdapName);
        assertEquals("cn=happy,dc=test,o=harmony", result.toString());

        try {
            context.composeName(null, prefix);
            fail("Should throws NPE");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            context.composeName(name, null);
            fail("Should throws NPE");
        } catch (NullPointerException e) {
            // expected
        }

        CompositeName compositeName = new CompositeName("usr/bin");
        result = context.composeName(compositeName, prefix);
        assertTrue(result instanceof CompositeName);
        assertEquals("o=harmony/usr/bin", result.toString());

        result = context.composeName(name, compositeName);
        assertTrue(result instanceof CompositeName);
        assertEquals("usr/bin/cn=happy,dc=test", result.toString());

        compositeName = new CompositeName("usr");
        CompositeName cName = new CompositeName("bin/cn=ok");
        result = context.composeName(compositeName, cName);
        assertTrue(result instanceof CompositeName);
        assertEquals("bin/cn=ok/usr", result.toString());
    }
}
