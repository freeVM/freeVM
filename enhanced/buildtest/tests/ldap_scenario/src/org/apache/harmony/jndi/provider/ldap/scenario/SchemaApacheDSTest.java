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

package org.apache.harmony.jndi.provider.ldap.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.naming.CompositeName;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SchemaViolationException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import tests.support.Support_LdapTest;

public class SchemaApacheDSTest extends Support_LdapTest {

    public static final String CLASS_DEFINITION = "classdefinition";

    public static final String ATTRIBUTE_DEFINITION = "attributedefinition";

    public static final String SYNTAX_DEFINITION = "syntaxdefinition";

    public static final String MATCHING_RULE = "matchingrule";

    public static final String OBJECT_CLASSES = "objectclasses";

    public static final String ATTRIBUTE_TYPES = "attributetypes";

    public static final String LDAP_SYNTAXES = "ldapsyntaxes";

    public static final String MATCHING_RULES = "matchingrules";

    private static final HashSet<String> ldapSchemaDef = new HashSet<String>();
    static {
        ldapSchemaDef.add(ATTRIBUTE_DEFINITION);
        ldapSchemaDef.add(CLASS_DEFINITION);
        ldapSchemaDef.add(MATCHING_RULE);
        ldapSchemaDef.add(SYNTAX_DEFINITION);
    }

    private static final Hashtable<String, ArrayList<String>> person = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> dcObject = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> top = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> organization = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> applicationentity = new Hashtable<String, ArrayList<String>>();

    // attribute definition
    private static final Hashtable<String, ArrayList<String>> cn = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> dc = new Hashtable<String, ArrayList<String>>();

    // syntax definition
    private static final Hashtable<String, ArrayList<String>> ds = new Hashtable<String, ArrayList<String>>();

    private static final Hashtable<String, ArrayList<String>> ia5string = new Hashtable<String, ArrayList<String>>();

    // matching rule definition
    private static final Hashtable<String, ArrayList<String>> caseExactOrderingMatch = new Hashtable<String, ArrayList<String>>();

    private static final List<String> schemaClassDefName;
    
    private static final List<String> schemaClassDefNameAll;

    private static final String[] names = { "account", "alias",
            "apachecatalogentry", "apachefactoryconfiguration",
            "apacheserviceconfiguration", "applicationentity",
            "applicationprocess", "certificationauthority",
            "certificationauthority-v2", "country", "crldistributionpoint",
            "dcobject", "deltacrl", "device", "dmd", "dnsdomain", "document",
            "documentseries", "domain", "domainrelatedobject", "dsa",
            "dynamicobject", "extensibleobject", "friendlycountry",
            "groupofnames", "groupofuniquenames", "inetorgperson",
            "javacontainer", "javamarshalledobject", "javanamingreference",
            "javaobject", "javaserializedobject", "labeleduriobject",
            "ldaprootdse", "locality", "newpilotperson", "openldaprootdse",
            "organization", "organizationalperson", "organizationalrole",
            "organizationalunit", "person", "pilotdsa", "pilotorganization",
            "pilotperson", "pkica", "pkiuser", "prefnode",
            "qualitylabelleddata", "referral", "residentialperson",
            "rfc822localpart", "room", "simplesecurityobject",
            "strongauthenticationuser", "subentry", "uidobject", "unixfile",
            "usersecurityinformation", "windowsfile", };

    private static final Hashtable<String, Hashtable<String, ArrayList<String>>> classDefs = new Hashtable<String, Hashtable<String, ArrayList<String>>>();
    static {
        schemaClassDefName = Arrays.asList(names);

        schemaClassDefNameAll = new ArrayList<String>();
        schemaClassDefNameAll.addAll(schemaClassDefName);
        schemaClassDefNameAll.add("top");
        schemaClassDefNameAll.add("collectiveattributesubentry");
        schemaClassDefNameAll.add("accesscontrolsubentry");
        schemaClassDefNameAll.add("subschema");

        classDefs.put("top", top);
        classDefs.put("organization", organization);
        classDefs.put("dcobject", dcObject);
        classDefs.put("person", person);

        // person class definition
        ArrayList<String> values = new ArrayList<String>();
        values.add("sn");
        values.add("cn");
        person.put("must", values);

        values = new ArrayList<String>();
        values.add("top");
        person.put("sup", values);

        values = new ArrayList<String>();
        values.add("true");
        person.put("structural", values);

        values = new ArrayList<String>();
        values.add("person");
        person.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.6.6");
        person.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("userpassword");
        values.add("telephonenumber");
        values.add("seealso");
        values.add("description");
        person.put("may", values);

        // dcObject
        values = new ArrayList<String>();
        values.add("dc");
        dcObject.put("must", values);

        values = new ArrayList<String>();
        values.add("top");
        dcObject.put("sup", values);

        values = new ArrayList<String>();
        values.add("true");
        dcObject.put("auxiliary", values);

        values = new ArrayList<String>();
        values.add("dcobject");
        dcObject.put("name", values);

        values = new ArrayList<String>();
        values.add("1.3.6.1.4.1.1466.344");
        dcObject.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("rfc2247: domain component object");
        dcObject.put("desc", values);

        // top
        values = new ArrayList<String>();
        values.add("objectclass");
        top.put("must", values);

        values = new ArrayList<String>();
        values.add("true");
        top.put("abstract", values);

        values = new ArrayList<String>();
        values.add("top");
        top.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.6.0");
        top.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("top of the superclass chain");
        top.put("desc", values);

        // organization
        values = new ArrayList<String>();
        values.add("o");
        organization.put("must", values);

        values = new ArrayList<String>();
        values.add("top");
        organization.put("sup", values);

        values = new ArrayList<String>();
        values.add("true");
        organization.put("structural", values);

        values = new ArrayList<String>();
        values.add("organization");
        organization.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.6.4");
        organization.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("userpassword");
        values.add("searchguide");
        values.add("seealso");
        values.add("businesscategory");
        values.add("x121address");
        values.add("registeredaddress");
        values.add("destinationindicator");
        values.add("preferreddeliverymethod");
        values.add("telexnumber");
        values.add("teletexterminalidentifier");
        values.add("telephonenumber");
        values.add("internationalisdnnumber");
        values.add("facsimiletelephonenumber");
        values.add("street");
        values.add("postofficebox");
        values.add("postalcode");
        values.add("postaladdress");
        values.add("physicaldeliveryofficename");
        values.add("st");
        values.add("l");
        values.add("description");
        organization.put("may", values);

        // attribute cn
        values = new ArrayList<String>();
        values.add("name");
        cn.put("sup", values);

        values = new ArrayList<String>();
        values.add("cn");
        values.add("commonname");
        cn.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.4.3");
        cn.put("numericoid", values);

        // attribute dc
        values = new ArrayList<String>();
        values.add("1.3.6.1.4.1.1466.115.121.1.26");
        dc.put("syntax", values);

        values = new ArrayList<String>();
        values.add("dc");
        values.add("domaincomponent");
        dc.put("name", values);

        values = new ArrayList<String>();
        values.add("0.9.2342.19200300.100.1.25");
        dc.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("caseignoreia5match");
        dc.put("equality", values);

        values = new ArrayList<String>();
        values.add("true");
        dc.put("single-value", values);

        values = new ArrayList<String>();
        values.add("caseignoreia5substringsmatch");
        dc.put("substr", values);

        values = new ArrayList<String>();
        values.add("rfc1274/2247: domain component");
        dc.put("desc", values);

        // syntax directory string
        values = new ArrayList<String>();
        values.add("1.3.6.1.4.1.1466.115.121.1.15");
        ds.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("directory string");
        ds.put("desc", values);

        // syntax ia5 string
        values = new ArrayList<String>();
        values.add("ia5 string");
        ia5string.put("desc", values);

        values = new ArrayList<String>();
        values.add("1.3.6.1.4.1.1466.115.121.1.26");
        ia5string.put("numericoid", values);

        // matching rule caseExactOrderingMatch
        values = new ArrayList<String>();
        values.add("caseexactorderingmatch");
        caseExactOrderingMatch.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.13.6");
        caseExactOrderingMatch.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("1.3.6.1.4.1.1466.115.121.1.15");
        caseExactOrderingMatch.put("syntax", values);

        // matching rule caseExactOrderingMatch
        values = new ArrayList<String>();
        values.add("applicationentity");
        applicationentity.put("name", values);

        values = new ArrayList<String>();
        values.add("2.5.6.12");
        applicationentity.put("numericoid", values);

        values = new ArrayList<String>();
        values.add("presentationaddress");
        values.add("cn");
        applicationentity.put("must", values);

        values = new ArrayList<String>();
        values.add("top");
        applicationentity.put("sup", values);

        values = new ArrayList<String>();
        values.add("rfc2256: an application entity");
        applicationentity.put("desc", values);

        values = new ArrayList<String>();
        values.add("true");
        applicationentity.put("structural", values);

        values = new ArrayList<String>();
        values.add("supportedapplicationcontext");
        values.add("seealso");
        values.add("ou");
        values.add("o");
        values.add("l");
        values.add("description");
        applicationentity.put("may", values);
    }

    public void test_schema_search_String() throws NamingException {
        DirContext root = ctx.getSchema("");
        NamingEnumeration<NameClassPair> pairs = root.list("");
        while (pairs.hasMore()) {
            NameClassPair next = pairs.next();
            assertTrue(ldapSchemaDef.contains(next.getName().toLowerCase()));

        }
        NamingEnumeration<SearchResult> e = null;
        try {
            e = root.search("test/cn", (Attributes) null, null);
        } catch (NameNotFoundException ex) {
            // expected
        }

        BasicAttributes attrFilter = new BasicAttributes();
        Attribute attr = new BasicAttribute("name", "applicationentity");
        attrFilter.put(attr);

        e = root.search("classDefinition", attrFilter, null);
        while (e.hasMoreElements()) {
            SearchResult element = (SearchResult) e.nextElement();
            assertTrue(applicationentity.get("name")
                    .contains(element.getName()));
            NamingEnumeration<? extends Attribute> attributes = element
                    .getAttributes().getAll();
            while (attributes.hasMore()) {
                Attribute at = attributes.next();
                assertTrue(applicationentity.keySet().contains(at.getID()));
                NamingEnumeration<?> values = at.getAll();
                while (values.hasMore()) {
                    Object value = values.next();
                    if (value instanceof ArrayList) {
                        assertTrue(applicationentity.get(at.getID()).equals(
                                value));
                    } else {
                        assertTrue(applicationentity.get(at.getID()).contains(
                                value.toString().toLowerCase()));
                    }
                }
            }
        }

        // Search all the schema object names
        NamingEnumeration<SearchResult> schemas = (NamingEnumeration<SearchResult>) root
                .search("", (Attributes) null, new String[] { "objectclass",
                        "objectclass", "test" });
        while (schemas.hasMore()) {
            SearchResult result = schemas.next();
            assertTrue(ldapSchemaDef.contains(result.getName().toLowerCase()));
            Attributes attrs = result.getAttributes();
            NamingEnumeration<String> ids = attrs.getIDs();
            while (ids.hasMore()) {
                attr = attrs.get(ids.next());
                NamingEnumeration<?> values = attr.getAll();
                assertEquals("objectclass", attr.getID());
                while (values.hasMoreElements()) {
                    Object value = (Object) values.nextElement();
                    assertTrue(ldapSchemaDef.contains(((String) value)
                            .toLowerCase()));
                }
            }
        }

        schemas = (NamingEnumeration<SearchResult>) root.search("",
                (Attributes) null, new String[] { "test" });
        while (schemas.hasMore()) {
            SearchResult result = schemas.next();
            assertTrue(ldapSchemaDef.contains(result.getName().toLowerCase()));
            Attributes attrs = result.getAttributes();
            NamingEnumeration<String> ids = attrs.getIDs();
            assertFalse(ids.hasMore());
        }

        attrFilter = new BasicAttributes();
        attr = new BasicAttribute("objectclass", "SyntaxDefinition");
        attrFilter.put(attr);
        schemas = (NamingEnumeration<SearchResult>) root.search("", attrFilter,
                new String[] { "objectclass" });
        assertTrue(schemas.hasMore());

        SearchResult result = schemas.next();
        assertEquals(SYNTAX_DEFINITION, result.getName().toLowerCase());
        Attributes attrs = result.getAttributes();
        NamingEnumeration<String> ids = attrs.getIDs();
        assertTrue(ids.hasMore());
        String id = ids.next();
        assertEquals("objectclass", id);
        Attribute attr1 = attrs.get(id);

        assertEquals(1, attr1.size());
        assertEquals(SYNTAX_DEFINITION, ((String) attr1.get(0)).toLowerCase());

    }

    public void test_Schema_search_filter() throws NamingException {
        DirContext root = ctx.getSchema("");
        try {
            root.search("test/cn", "(&(sup={0})(name={1}))", new Object[] {
                    "top", "applicationentity" }, null);
        } catch (NameNotFoundException e) {
            // expected
        }

        SearchControls controls = new SearchControls();
        controls.setReturningObjFlag(false);
        NamingEnumeration<SearchResult> results = root.search(
                "classDefinition", "(&(sup={0})(name={1}))", new Object[] {
                        "top", "applicationentity" }, null);
        while (results.hasMoreElements()) {
            SearchResult element = (SearchResult) results.nextElement();
            assertTrue(applicationentity.get("name").contains(
                    element.getName().toLowerCase()));
            NamingEnumeration<? extends Attribute> attributes = element
                    .getAttributes().getAll();
            while (attributes.hasMore()) {
                Attribute at = attributes.next();
                assertTrue(applicationentity.keySet().contains(
                        at.getID().toLowerCase()));
                NamingEnumeration<?> values = at.getAll();
                while (values.hasMore()) {
                    Object value = values.next();
                    if (value instanceof ArrayList) {
                        assertTrue(applicationentity.get(
                                at.getID().toLowerCase()).equals(value));
                    } else {
                        assertTrue(applicationentity.get(
                                at.getID().toLowerCase()).contains(
                                value.toString().toLowerCase()));
                    }
                }
            }
        }

        try {
            results = root.search("classdefinition", "", null);
        } catch (StringIndexOutOfBoundsException e) {
            // expected
        }

        try {
            results = root.search("classdefinition", "*", null);
        } catch (InvalidSearchFilterException e) {
            // expected
        }

        try {
            results = root.search("classdefinition", "(*)", null);
        } catch (InvalidSearchFilterException e) {
            // expected
        }

        SearchControls ctls = new SearchControls();
        ctls.setReturningObjFlag(false);
        ctls.setReturningAttributes(new String[] { "name" });
        results = root.search("", "sup=top", ctls);
        assertFalse(results.hasMore());

        results = root.search("classdefinition", "sup=*", ctls);
        int i = 0;
        while (results.hasMore()) {
            SearchResult result = results.next();
            Attributes attributes = result.getAttributes();
            NamingEnumeration<String> ids = attributes.getIDs();
            while (ids.hasMore()) {
                String id = ids.next();
                Attribute attribute = attributes.get(id);
                NamingEnumeration<?> all = attribute.getAll();
                while (all.hasMore()) {
                    Object next = all.next();
                    if (next instanceof ArrayList) {
                        i += ((ArrayList) next).size();
                        assertTrue(schemaClassDefName
                                .containsAll((Collection<?>) next));
                    } else {
                        assertTrue(schemaClassDefName.contains(((String) next)
                                .toLowerCase()));
                        i++;
                    }
                }
            }
        }
        assertSame(schemaClassDefName.size(), i);
    }
    
    public void test_schema_list_Ljavax_naming_Name() throws NamingException {
        try {
            ctx.list("classdefinition");
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }
        
        DirContext root = ctx.getSchema("");
        
        // TODO ======== refactor these cases into unit test ========
        try {
            root.list((String) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            root.list("test");
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        
        try {
            root.list("classdefinition/test");
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        
        try {
            root.list("test/classdefinition");
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        
        //==========================================================
        
        NamingEnumeration<NameClassPair> pairs = root.list("");
        while (pairs.hasMore()) {
            NameClassPair pair = pairs.next();
            assertTrue(ldapSchemaDef.contains(pair.getName()
                    .toLowerCase()));
        }
        
        pairs = root.list("classdefinition");
        
        while (pairs.hasMore()) {
            NameClassPair pair = pairs.next();
            assertTrue(schemaClassDefNameAll.contains(pair.getName()
                    .toLowerCase()));
        }

    }
    
    public void test_schema_lookup_Ljavax_naming_Name() throws NamingException {
        // TODO =========== refactor these cases into unit test ==============
        DirContext root = ctx.getSchema("");
        
        try {
            root.lookup((String) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
        // ===================================================================

        DirContext classdefs = (DirContext)root.lookup("");
        Attributes classAttrs = classdefs.getAttributes("");
        assertSame(0, classAttrs.size());
        
        
        classdefs = (DirContext)root.lookup("classdefinition");
        
        // Get "classdefinition" object's attributes
        classAttrs = classdefs.getAttributes("");
        
        assertNotSame(classAttrs, classdefs.getAttributes(""));
        
        assertTrue(classAttrs.getIDs().hasMore());
        
        String id = classAttrs.getIDs().next();
        assertEquals("objectclass", id);
        
        Attribute attr = classAttrs.get(id);
        
        assertTrue(attr.getAll().hasMore());
        assertEquals("classdefinition", ((String) attr.getAll().next())
                .toLowerCase());

        classdefs = (DirContext)root.lookup("");
        
        try {
            classAttrs = classdefs.getAttributes("classdefinition/cn");
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        
        classAttrs = classdefs.getAttributes("classdefinition/top");
        NamingEnumeration<String> ids = classAttrs.getIDs();
        while(ids.hasMore()) {
            id = ids.next();
            assertTrue(top.keySet().contains(id.toLowerCase()));
            attr = classAttrs.get(id);
            NamingEnumeration<?> values = attr.getAll();
            while(values.hasMore()) {
                Object value = values.next();
                if (value instanceof ArrayList) {
                    assertTrue(top.get(id.toLowerCase()).equals(value));
                } else {
                    assertTrue(top.get(id.toLowerCase()).contains(
                            value.toString().toLowerCase()));
                }
            }
        }
    }

    public void test_CreateSubcontext_LName_LAttributes()
            throws NamingException {
        DirContext root = ctx.getSchema("");
        // Specify attributes for the schema object
        Attributes attrs = new BasicAttributes(true); // Ignore case
        attrs.put("NUMERICOID", "1.3.6.1.4.1.42.2.27.4.2.3.1.1.1");
        attrs.put("NAME", "fooObjectClass");
        attrs.put("DESC", "for JNDITutorial example only");
        attrs.put("SUP", "top");
        attrs.put("STRUCTURAL", "true");
        Attribute must = new BasicAttribute("MUST", "cn");
        must.add("objectclass");
        attrs.put(must);
        try {
            root.createSubcontext(new CompositeName("test"), attrs);
            fail("Should throw SchemaViolationException");
        } catch (SchemaViolationException e) {
            // expected
        }

        attrs.remove("NUMERICOID");
        try {
            root.createSubcontext(new CompositeName(
                    "ClassDefinition/fooObjectClass"), attrs);
            fail("Should throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected
        }
    }
    
    public void testGetAttributesNameStringArray() throws NamingException {
        DirContext root = ctx.getSchema("");
        try {
            root.getAttributes((Name) null, new String[] {});
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            root.getAttributes("test", null);
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
    }

    public void test_modifyAttributes_LString_LModificationItem() throws NamingException {
        DirContext root = ctx.getSchema("");
        try {
            root.modifyAttributes((String)null, new ModificationItem[]{});
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            root.modifyAttributes("test", null);
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        try {
            root.modifyAttributes("AttributeDefinition/cn", null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_search_LString_LAttributes_LString() throws NamingException {
        DirContext root = ctx.getSchema("");
        try {
            root.search((String)null, new BasicAttributes(), new String[]{});
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            root.modifyAttributes("test", null);
            fail("Should throw NameNotFoundException");
        } catch (NameNotFoundException e) {
            // expected
        }
        try {
            root.modifyAttributes("AttributeDefinition/cn", null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    private static DirContext ctx = null;

    private static Hashtable<String, String> env = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (null != ctx)
            return;
        env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.harmony.jndi.provider.ldap.LdapContextFactory");
        env.put(Context.PROVIDER_URL, "ldap://0.0.0.0:" + port
                + "/uid=admin,ou=system");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");

        ctx = new InitialDirContext(env);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (null == ctx)
            return;
        ctx.close();
        ctx = null;
    }
}

