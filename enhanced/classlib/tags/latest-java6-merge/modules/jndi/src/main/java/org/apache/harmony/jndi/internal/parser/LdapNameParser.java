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

package org.apache.harmony.jndi.internal.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * A Distinguised Name Parser according with the RFC2253 and RFC1779 for use
 * with LdapName only
 */
public class LdapNameParser implements NameParser, LdapParser {
    private String s = null;

    /**
     * Constructor
     * 
     * @param s
     *            the string to parse
     */
    public LdapNameParser(String s) {
        this.s = s;
    }

    /**
     * For using with the NameParser interface
     * 
     * @return an LdapName if possible
     */
    public Name parse(String arg0) throws NamingException {
        return new LdapName(arg0);
    }

    /**
     * Returns the parsed list of Rdns
     */
    public List getList() throws InvalidNameException {
        List list = new ArrayList();
        int from = 0;
        char[] c = s.toCharArray();

        if (s.equals("")) {
            return list;
        }

        if (s.startsWith(",") || s.startsWith(";")) {
            throw new InvalidNameException("Invalid name: " + s);
        }

        for (int i = 0; i < c.length; i++) {
            if ((c[i] == ',' || c[i] == ';') && c[i - 1] != '\\') {

                String sub = s.substring(from, i);
                if (sub.equals("")) {
                    throw new InvalidNameException("Invalid name: " + s);
                }
                list.add(new Rdn(sub));
                from = i + 1;
            }
        }
        list.add(new Rdn(s.substring(from, s.length())));
        Collections.reverse(list);
        return list;
    }
}
