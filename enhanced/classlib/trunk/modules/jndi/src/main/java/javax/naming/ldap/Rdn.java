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

package javax.naming.ldap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import org.apache.harmony.jndi.internal.nls.Messages;
import org.apache.harmony.jndi.internal.parser.LdapRdnParser;

/**
 * 
 * @ar.org.fitc.spec_ref
 * 
 * @version 1.0
 * @author Osvaldo C. Demo
 * 
 */
public class Rdn implements Serializable, Comparable<Object> {

    private static final long serialVersionUID = -5994465067210009656L;

    /**
     * @ar.org.fitc.spec_ref
     */
    public static String escapeValue(Object val) {
        if (val == null) {
            throw new NullPointerException("val "+Messages.getString("ldap.00"));
        }
        return LdapRdnParser.escapeValue(val);
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public static Object unescapeValue(String val) {
        if (val == null) {
            throw new NullPointerException("val "+Messages.getString("ldap.00"));
        }
        return LdapRdnParser.unescapeValue(val);
    }

    private List<Attribute> list;

    private transient LdapRdnParser parser;

    /**
     * @ar.org.fitc.spec_ref
     */
    public Rdn(Attributes attrSet) throws InvalidNameException {
        if (attrSet == null) {
            throw new NullPointerException("attrSet "+Messages.getString("ldap.00"));
        }

        if (attrSet.size() == 0) {
            throw new InvalidNameException("atrrSet "+Messages.getString("ldap.03"));
        }

        //check all the elements to follow RI's behavior
        NamingEnumeration<? extends Attribute> ne = attrSet.getAll();
        while (ne.hasMoreElements()) {
            Attribute at = ne.nextElement();
            try {
                at.get();
            } catch (NamingException e) {
            }
        }

        list = convertToAttributeArrayList(attrSet);
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public Rdn(Rdn rdn) {
        if (rdn == null) {
            throw new NullPointerException("rdn "+Messages.getString("ldap.00"));
        }

        list = convertToAttributeArrayList(rdn.toAttributes());
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public Rdn(String rdnString) throws InvalidNameException {
        if (rdnString == null) {
            throw new NullPointerException("rdnString "+Messages.getString("ldap.00"));
        }

        if (rdnString.length() != 0) {
            parser = new LdapRdnParser(rdnString);
            list = parser.getList();
        } else {
            list = new ArrayList<Attribute>();
        }
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public Rdn(String type, Object value) throws InvalidNameException {
        if (type == null) {
            throw new NullPointerException("type "
                    + Messages.getString("ldap.00"));
        }

        if (value == null) {
            throw new NullPointerException("value "
                    + Messages.getString("ldap.00"));
        }

        if (type.length() == 0) {
            throw new InvalidNameException("type "
                    + Messages.getString("ldap.04"));
        }

        if (value instanceof String && ((String) value).length() == 0) {
            throw new InvalidNameException("value "
                    + Messages.getString("ldap.04"));
        }

        list = convertToAttributeArrayList(new BasicAttributes(type, value,
                true));
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public int compareTo(Object obj) {
        if (!(obj instanceof Rdn)) {
            throw new ClassCastException(Messages.getString("ldap.06"));
        }
        Rdn rdn = (Rdn) obj;
        String s1 = "", s2 = "";

        for (Enumeration<?> iter = toAttributes().getAll(); iter.hasMoreElements();) {
            s1 = s1 + escapeValue(iter.nextElement().toString());
            
            //this one does not seem necessary. Spec does not require it, if there are apps that depend on commas, uncomment it
            //if (iter.hasMoreElements()) {
            //    s1 = s1 + ",";
            //}
        }
        for (Enumeration<?> iter = rdn.toAttributes().getAll(); iter.hasMoreElements();) {
            s2 = s2 + escapeValue(iter.nextElement().toString());

            //this one does not seem necessary. Spec does not require it, if there are apps that depend on commas, uncomment it
            //if (iter.hasMoreElements()) {
            //    s2 = s2 + ",";
            //}
        }
        return s1.toLowerCase().compareTo(s2.toLowerCase());
    }

    private List<Attribute> convertToAttributeArrayList(Attributes attrList) {
        List<Attribute> myList = new ArrayList<Attribute>();

        NamingEnumeration<? extends Attribute> ne = attrList.getAll();
        while (ne.hasMoreElements()) {
            myList.add((Attribute)ne.nextElement().clone());
        }
        return myList;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public boolean equals(Object obj) {

        if (!(obj instanceof Rdn) || this.size() != ((Rdn) obj).size()) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        NamingEnumeration<? extends Attribute> iter1 = toAttributes().getAll();
        NamingEnumeration<? extends Attribute> iter2 = ((Rdn) obj).toAttributes().getAll();

        while (iter1.hasMoreElements()) {
            Attribute a1 = iter1.nextElement();
            Attribute a2 = iter2.nextElement();

            if (!(a1.getID().toLowerCase().equals(a2.getID().toLowerCase()))
                    || a1.size() != a2.size()) {
                return false;
            }

            Enumeration<?> en1 = null;
            Enumeration<?> en2 = null;
            try {
                en1 = a1.getAll();
                en2 = a2.getAll();
            } catch (NamingException e) {
                //what is the correct way for this?
                return false;
            }

            while (en1.hasMoreElements()) {
                Object o1 = en1.nextElement();
                String s1 = (o1 instanceof String) ? (String)o1 : escapeValue(o1);

                Object o2 = en2.nextElement();
                String s2 = (o2 instanceof String) ? (String)o2 : escapeValue(o2);

                if (!(s1.toLowerCase().equals(s2.toLowerCase()))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public String getType() {
        return list.get(0).getID();
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public Object getValue() {
        Object a = null;
        try {
            a = list.get(0).get();
        } catch (NamingException e) {
        } catch (NullPointerException e) {
        }
        return a;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public int hashCode() {
        int sum = 0;

        for (Iterator<Attribute> attr = list.iterator(); attr.hasNext();) {
            Attribute a = attr.next();
            NamingEnumeration<?> en = null;
            sum += a.getID().toLowerCase().hashCode();

            try {
                en = a.getAll();
            } catch (NamingException e) {
                continue;
            }

            while (en.hasMoreElements()) {
                Object obj = en.nextElement();
                try {
                    String s = (String) obj;
                    sum += escapeValue(s.toLowerCase()).hashCode();
                } catch (ClassCastException e) {
                    sum += obj.hashCode();
                }
            }
        }
        return sum;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public int size() {
        int result = 0;
        for (Iterator<Attribute> iter = list.iterator(); iter.hasNext();) {
            result += iter.next().size();
        }
        return result;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public Attributes toAttributes() {
        BasicAttributes ba = new BasicAttributes(true);
        for (Iterator<Attribute> iter = list.iterator(); iter.hasNext();) {
            ba.put((Attribute) iter.next().clone());
        }
        return ba;
    }

    /**
     * @ar.org.fitc.spec_ref
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator<Attribute> iter = list.iterator(); iter.hasNext();) {
            Attribute element = iter.next();
            NamingEnumeration<?> ne = null;

            try {
                ne = element.getAll();
            } catch (NamingException e) {
            }

            while (ne.hasMoreElements()) {
                sb.append(element.getID());
                sb.append('=');
                sb.append(escapeValue(ne.nextElement()));

                if (ne.hasMoreElements()) {
                    sb.append('+');
                }
            }

            if (iter.hasNext()) {
                sb.append('+');
            }
        }
        return sb.toString();
    }
}
