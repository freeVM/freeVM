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

/**
 * @author Hugo Beilis
 * @author Leonardo Soler
 * @author Gabriel Miretti
 * @version 1.0
 */

package org.apache.harmony.jndi.tests.javax.naming.ldap;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * 
 * <p>
 * Test cases for all methods of the class LdapName.
 * </p>
 * 
 * <p>
 * The next two tables contains a list of the methods to be tested, with the
 * return of each method.
 * </p>
 * <table class="t" cellspacing="0"> <tbody>
 * <th>Constructors:</th>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="LdapName(List<Rdn> rdns)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="LdapName(String name)" id="f10"></td>
 * 
 * </tr>
 * </tbody> <table> <tbody>
 * <th>Method Summary:</th>
 * <tr>
 * <TD>Return</TD>
 * <TD>Method</TD>
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></TD>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="add(int posn, Rdn comp)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="add(int posn, String comp)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="add(Rdn comp)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="add(String comp)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="addAll(int posn, List<Rdn> suffixRdns)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="addAll(int posn, Name suffix)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="addAll(List<Rdn> suffixRdns)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="addAll(Name suffix)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Object" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="clone()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="int" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="compareTo(Object obj)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="endsWith(List<Rdn> rdns)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="endsWith(Name n)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="equals(Object obj)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="String" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="get(int posn)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Enumeration<String>" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="getAll()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="getPrefix(int posn)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Rdn" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="getRdn(int posn)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="List<Rdn>" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="getRdns()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Name" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="getSuffix(int posn)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="int" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="hashCode()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="isEmpty()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="Object" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="remove(int posn)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="int" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="size()" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="startsWith(List<Rdn> rdns)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="boolean" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="startsWith(Name n)" id="f10"></td>
 * 
 * </tr>
 * <tr>
 * <td class="c0" id="c00"><input class="a0" size="40" name="sas9nt11"
 * readonly="readonly" value="String" id="f00"></td>
 * <td class="c0" id="c10"><input class="a0" size="40" name="sas9nt21"
 * readonly="readonly" value="toString()" id="f10"></td>
 * 
 * </tr>
 * </tbody> </table>
 * 
 */
public class LdapNameTest extends TestCase {

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     */
    public void testLdapNameString001() throws Exception {
        try {
            new LdapName((String) null);
            fail("Failed the null argument is invalid.");
        } catch (NullPointerException e) {}

        //check that no exception happens
        new LdapName("CN=test");
        new LdapName("L=test,C=common");
        new LdapName("ST=test;CN=common");
        new LdapName("O=test+CM=common");
        new LdapName("OU=test this");
        new LdapName("C=test\\, this");
        new LdapName("S=#04024869");
        new LdapName("DC=test,T=time+CM=common V<this C>this,S=#04024869");
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     */
    public void testLdapNameString002() throws Exception {
        String str = "t=\\20\\ te\\ s\\20t\\20\\20 + t2 = test1\\20\\ ";
        LdapName ln = new LdapName(str);
        assertEquals(ln.toString(), str);
        ln.get(0);
        assertEquals(ln.toString(), str);
        ln.add("t=test");
        assertEquals(ln.toString(), "t=test,t=\\ \\ te s t\\ +t2=test1\\ \\ ");
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a non-null
     * String, which must be a valid string like SN=Lu\C4\8Di\C4\87.
     * </p>
     * <p>
     * The expected result is an instance of LdapName.
     * </p>
     * <p>
     * The String is an example of an RDN surname value consisting of 5 letters
     * </p>
     */
    public void testLdapNameString010() throws Exception {
        new LdapName("SN=Lu\\C4\\8Di\\C4\\87");
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a
     * non-null String, and the DNs must be case insensitive.
     * </p>
     * <p>
     * The expected result is an assertion between two names one upper and other
     * lower case.
     * </p>
     */
    public void testLdapNameString011() throws Exception {
        String testUpperCase = "UID=TEST";
        String testLowerCase = "uid=test";
        assertEquals(new LdapName(testUpperCase), new LdapName(testLowerCase));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept an
     * empty String.
     * </p>
     * <p>
     * The expected result an empty name, not null, empty.
     * </p>
     */
    public void testLdapNameString012() throws Exception {
        LdapName ln = new LdapName("");
        assertTrue(ln.isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a String
     * with the correct format like "a=b", In this case we are testing the
     * special characters "<" and ">".
     * </p>
     * <p>
     * The expected result is an instance of ldapname.
     * </p>
     */
    public void testLdapNameString013() throws Exception {
        new LdapName("a=<");
        new LdapName("a=<a");
        new LdapName("a=a<");
        new LdapName("a=a<b");
        new LdapName("a=a<b<");
        new LdapName("a=>");
        new LdapName("a=>a");
        new LdapName("a=a>");
        new LdapName("a=a>b");
        new LdapName("a=a>b>");
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should not accept a
     * String with an incorrect format like "test".
     * </p>
     * <p>
     * The expected result is an InvalidNameException.
     * </p>
     */
    public void testLdapNameString014() throws Exception {
        try {
            new LdapName("test");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("t=test,control");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("t=test,");
        try {
            new LdapName(",t=test");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName(",t=test,");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should not accept a
     * String with an incorrect format like "test".
     * </p>
     * <p>
     * The expected result is an InvalidNameException.
     * </p>
     */
    public void testLdapNameString015() throws Exception {
        new LdapName("t=");
        try {
            new LdapName("=t");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("=");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("=t=t");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("a=t=");
        try {
            new LdapName("=a=t=");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("a=b=t=z");

        try {
            new LdapName(";");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("a=b;");
        try {
            new LdapName(";a=b");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("a=b;c");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName(";a=b;");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should not accept a
     * String with an incorrect format like "test".
     * </p>
     * <p>
     * The expected result is an InvalidNameException.
     * </p>
     */
    public void testLdapNameString016() throws Exception {

        new LdapName("a=a#");
        try {
            new LdapName("a=#a");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("#a=a");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("#a=#a");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("#a=a#");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a
     * String notice here that here that we are testing the the special
     * character "<".
     * </p>
     * <p>
     * The expected result is an Exception.
     * </p>
     */
    public void testLdapNameString036() {
        try {
            new LdapName("a<a");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("<a=a");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("<a=a<");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("a>c");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName(">a=c");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName(">a=c>");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a
     * String notice here that here that we are testing the the special
     * character "\".
     * </p>
     * <p>
     * The expected result is an Exception.
     * </p>
     */
    public void testLdapNameString042() {
        try {
            new LdapName("a=b\\");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("\\a=b");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("\\a=b\\");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("a=b\\s");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a
     * String notice here that here that we are testing the the special
     * character "+".
     * </p>
     * <p>
     * The expected result is an instance of the class.
     * </p>
     */
    public void testLdapNameString046() throws Exception {
        new LdapName("a=b+");
        try {
            new LdapName("+a=b");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("+a=b+");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("a=b+s=");
        try {
            new LdapName("a=b+s");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        new LdapName("b= ");
        try {
            new LdapName(" =b");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        LdapName ldapName = new LdapName("cn=a+");
        assertEquals("cn=a+", ldapName.toString());
        assertEquals("cn=a", ldapName.getRdns().get(0).toString());
        
        ldapName = new LdapName("cn=\\+");
        assertEquals("cn=\\+", ldapName.toString());
        assertEquals("cn=\\+", ldapName.getRdns().get(0).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor with a String containing two
     * consecutive commas.
     * </p>
     * <p>
     * The expected result is an exception.
     * </p>
     */
    public void testLdapNameString053() {
        try {
            new LdapName("cn=pucho,,o=fitc");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}

        try {
            new LdapName("cn=pucho,o=#fitc");
            fail("InvalidNameException expected");
        } catch (InvalidNameException e) {}
    }
    
    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName(String)'
     * </p>
     * <p>
     * Here we are testing the constructor, this method should accept a
     * String notice here that here that we are testing the special case
     * in which the name is quoted and the meta characters in it are ignored.
     * </p>
     * <p>
     * The expected result is an instance of the class.
     * </p>
     */
    public void testLdapNameString054() throws Exception {
        LdapName ldapName;
        String stringName;
        
        stringName = "dc=apacheorg\"";
        ldapName = new LdapName(stringName);
        assertEquals(stringName, ldapName.toString());
        
        stringName = "dc=\"apache,org\"";
        ldapName = new LdapName(stringName);
        assertEquals(stringName, ldapName.toString());
        
        stringName = "dc=\"apache;org\"";
        ldapName = new LdapName(stringName);
        assertEquals(stringName, ldapName.toString());
        
        stringName = "dc=\"apache\\\";org\"";
        ldapName = new LdapName(stringName);
        assertEquals(stringName, ldapName.toString());
        
        stringName = "dc=apache\\\"org,O=org";
        ldapName = new LdapName(stringName);
        assertEquals(stringName, ldapName.toString());
        
        stringName = "\"az=a,O=a\"";
        try{
            new LdapName(stringName);
            fail("Should throw InvalidNameException");
        }catch(InvalidNameException e){
            //expected
        }
        
        stringName = "dc=apache\\\";org,O=org";
        try{
            new LdapName(stringName);
            fail("Should throw InvalidNameException");
        }catch(InvalidNameException e){
            //expected
        }
        
        try{
            new LdapName("dc=apache,org");
            fail("Should throw InvalidNameException");
        }catch(InvalidNameException e){
            //expected
        }
        
        try{
            new LdapName("dc=apache;org");
            fail("Should throw InvalidNameException");
        }catch(InvalidNameException e){
            //expected
        }
        
        try{
            new LdapName("dc=\"apache\"harmony\"org\"");
            fail("Should throw InvalidNameException");
        }catch(InvalidNameException e){
            //expected
        }
        
        stringName = "DC=\"Apache,org\",DC=\"Apacheorg\"";
        String expectedRdnsName = "DC=\"Apache,org\",DC=\"Apacheorg\"";
        ldapName = new LdapName(stringName);
        List rdns = ldapName.getRdns();
        assertEquals(2, rdns.size());
        assertEquals(expectedRdnsName, ldapName.toString());
        
        stringName= "abc=\"DC:O=ab,DC=COM\",cn=apache\"org,O=harmony";
        new LdapName(stringName);

        try {
            stringName = "DC=A\"pache,org\",DC=\"Apacheorg\"";
            ldapName = new LdapName(stringName);
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }

        try {
            stringName = "DC=\"Apache,org,DC=\"Apacheorg\"";
            ldapName = new LdapName(stringName);
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }

        try {
            stringName = "DC=\"Apache,org,DC=\"Apacheorg";
            ldapName = new LdapName(stringName);
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }
        
        try {
            stringName = "+";
            ldapName = new LdapName(stringName);
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }
        
        try {
            stringName = ";";
            ldapName = new LdapName(stringName);
            fail("Should throw InvalidNameException");
        } catch (InvalidNameException e) {
            // expected
        }
    }
    
    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor of LdapName with a list of
     * valid names.
     * </p>
     * <p>
     * The expected result is an instance of an object of LdapName.
     * </p>
     */
    public void testLdapNameListOfRdn001() throws Exception {

        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("CN=commonName"));
        test.add(new Rdn("L=localityName"));
        test.add(new Rdn("ST=stateOrProvinceName"));
        test.add(new Rdn("O=organizationName"));
        test.add(new Rdn("OU=organizationalUnitName"));
        assertNotNull(new LdapName(test));

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor of LdapName with null.
     * </p>
     * <p>
     * The expected result is a NullPointerException.
     * </p>
     */
    public void testLdapNameListOfRdn002() {
        LinkedList<Rdn> test = null;
        try {
            new LdapName(test);
            fail("NPE expected");
        } catch (NullPointerException e) {}

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor of LdapName with a non-null but empty
     * list.
     * </p>
     * <p>
     * The expected result is an instance of LdapName.
     * </p>
     */
    public void testLdapNameListOfRdn003() {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        assertNotNull(new LdapName(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor of LdapName with a list of valid
     * names.
     * </p>
     * <p>
     * The expected result is an instance of LdapName, with the indexing
     * correct.
     * </p>
     */
    public void testLdapNameListOfRdn004() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("CN=commonName"));
        test.add(new Rdn("L=localityName"));
        test.add(new Rdn("ST=stateOrProvinceName"));
        test.add(new Rdn("O=organizationName"));
        test.add(new Rdn("OU=organizationalUnitName"));

        LdapName x = new LdapName(test);
        assertNotNull(x);
        List t = x.getRdns();

        int i = 0;
        for (Iterator iter = test.iterator(); iter.hasNext();) {
            Rdn element = (Rdn) iter.next();
            assertEquals(element.toString(), t.get(i).toString());
            i++;
        }
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor of LdapName with a
     * non-null list containing invalid Rdns.
     * </p>
     * <p>
     * The expected result is an instance of LdapName.
     * </p>
     */
    public void testLdapNameListOfRdn005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName x = null;

        test.add(new Rdn("CN", new LinkedList()));
        test.add(new Rdn("L", new LinkedList()));
        test.add(new Rdn("ST", new LinkedList()));
        test.add(new Rdn("O", new LinkedList()));
        test.add(new Rdn("OU", new LinkedList()));

        x = new LdapName(test);
        assertNotNull(x);

        try {
            x.toString();
            fail("Failed, because the list of rdn was incorrect so a class cast exception must be thrown.");
        } catch (ClassCastException e) {}

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.LdapName(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing the constructor method of LdapName reciving a list of
     * valid names.
     * </p>
     * <p>
     * The expected result is an instance of an object of LdapName, and also
     * that the indexing is made like the other way around.
     * </p>
     */    
    public void testLdapNameListOfRdn006() throws Exception {
        try {
            BasicAttributes bas = new BasicAttributes();
            bas.put("test2", "test2");
            bas.put("test1", "test1");
            bas.put("test3", "test3");
            Rdn rdn1 = new Rdn(bas);
            LinkedList<Rdn> rdns = new LinkedList<Rdn>();
            rdns.add(rdn1);
            LdapName ln = new LdapName(rdns);
            assertEquals("test1=test1+test2=test2+test3=test3", ln.getAll().nextElement());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.hashCode()'
     * </p>
     * <p>
     * Here we are testing the hash code of an empty String and the RDN in the
     * LdapName. The String is a valid input.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testHashCode001() throws Exception {
        assertEquals(0, new LdapName("").hashCode());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.hashCode()'
     * </p>
     * <p>
     * Here we are testing the hash code of a list of RDNs and the
     * hashcode of all LdapName. The list is a valid input.
     * </p>
     * <p>
     * The expected result is an assertion between the hashcode of the all RDNs
     * in the LdapName and the hash of the list, this method returns the sum of
     * all Rdns hashcodes.
     * </p>
     */
    public void testHashCode002() throws Exception {
        Rdn t = new Rdn("");
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(t);
        LdapName ln = new LdapName(test);
        assertEquals(0, t.hashCode() & ln.hashCode());
        assertEquals(t.hashCode(), ln.hashCode());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.hashCode()'
     * </p>
     * <p>
     * Here we are testing the hash code of an LdapName created with a list of
     * RDNs. The list is valid input.
     * </p>
     * <p>
     * The expected result is an assertion between the hashcode of the all RDNs
     * in the LdapName and the list, this method returns the sum of all Rdns
     * hashcodes.
     * </p>
     */
    public void testHashCode003() throws Exception {
        Rdn rdn1 = new Rdn("CN=commonName");
        Rdn rdn2 = new Rdn("t=test");
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(rdn1);
        test.add(rdn2);
        LdapName ln = new LdapName(test);
        assertEquals(rdn1.hashCode() + rdn2.hashCode(), ln.hashCode());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.hashCode()'
     * </p>
     * <p>
     * Here we are testing the hash code of a non-empty String and the
     * RDN in the LdapName. The String is a valid input.
     * </p>
     * <p>
     * The expected result is the equals hash of two objects.
     * </p>
     */
    public void testHashCode004() throws Exception {
        String test = "t=test,h=asd";
        LdapName x = new LdapName(test);
        LdapName y = new LdapName(test);
        assertNotSame(0, x.hashCode() & y.hashCode());
        assertEquals(x.hashCode(), y.hashCode());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.equals(Object)'
     * </p>
     * <p>
     * Here we are testing the equals method for two LdapNames that are not
     * equal.
     * </p>
     * <p>
     * The expected result is false.
     * </p>
     */
    public void testEquals003() throws Exception {
        LdapName ln = new LdapName("o=other");
        assertFalse(new LdapName("t=test").equals(ln));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.equals(Object)'
     * </p>
     * <p>
     * Here we are testing the equals method for two equal LdapNames.
     * </p>
     * <p>
     * The expected result is true.
     * </p>
     */
    public void testEquals004() throws Exception {
        assertTrue(new LdapName("t=test").equals(new LdapName("t=test")));
        assertTrue(new LdapName("t=test").equals(new LdapName("T=TEST")));
        assertTrue(new LdapName("t=test").equals(new LdapName("T=  TEST")));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.clone()'
     * </p>
     * <p>
     * Here we are testing if a clone of an object of LdapName is equal to the
     * original.
     * </p>
     * <p>
     * The expected result in this case is true.
     * </p>
     */
    public void testClone001() throws Exception {
        LdapName ln = new LdapName("t=test");
        LdapName copy = (LdapName) ln.clone();
        assertEquals(ln, copy);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.clone()'
     * </p>
     * <p>
     * Here we are testing if this method correctly clones this LdapName.
     * </p>
     * <p>
     * The expected result in this case is if a change in primary object no
     * affect the clone.
     * </p>
     */
    public void testClone002() throws Exception {
        LdapName ln = new LdapName("t=test");
        LdapName copy = (LdapName) ln.clone();
        ln.add("ch=change");
        assertNotSame(ln.toString(), copy.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.clone()'
     * </p>
     * <p>
     * Here we are testing if this method correctly clones this LdapName.
     * </p>
     * <p>
     * The expected result in this case is if a change in the clone object no
     * affect the primary.
     * </p>
     */
    public void testClone003() throws Exception {
        LdapName ln = new LdapName("t=test");
        LdapName copy = (LdapName) ln.clone();
        copy.add("ch=change");
        assertNotSame(ln.toString(), copy.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.clone()'
     * </p>
     * <p>
     * Here we are testing if this method correctly clones this LdapName.
     * </p>
     * <p>
     * The expected result in this case is if clone of an empty object is equal
     * to its primary.
     * </p>
     */
    public void testClone004() throws Exception {
        LdapName ln = new LdapName("");
        LdapName copy = (LdapName) ln.clone();
        assertEquals(ln, copy);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.toString()'
     * </p>
     * <p>
     * Here we are testing if the method returns the correct string for an
     * LdapName according to RFC 2253.
     * </p>
     * <p>
     * The expected results is a representation of this LDAP as we created it.
     * </p>
     */
    public void testToString001() throws Exception {
        LdapName ln = new LdapName("t=test");
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.toString()'
     * </p>
     * <p>
     * Here we are testing if the method returns the correct string for an
     * LdapName according to RFC 2253.
     * </p>
     * <p>
     * The expected results is a representation of this LDAP as we created it,
     * in this case the in the String are three names, the ldapname should
     * return the strings in the LIFO way.
     * </p>
     */
    public void testToString002() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("c1=common"));
        test.add(new Rdn("c2=common"));
        test.add(new Rdn("c3=common"));
        LdapName ln = new LdapName(test);
        String comp = "";
        for (Rdn rdn : test) {
            if (test.getFirst() == rdn) {
                comp = rdn.toString();
            } else {
                comp = rdn.toString() + "," + comp;
            }
        }
        assertEquals(comp, ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.toString()'
     * </p>
     * <p>
     * Here we are testing if the method returns the correct string for an
     * LdapName according to RFC 2253.
     * </p>
     * <p>
     * The expected results is that the name returns an empty string.
     * </p>
     */
    public void testToString003() throws Exception {
        LdapName ln = new LdapName("");
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.toString()'
     * </p>
     * <p>
     * Here we are testing if the method returns the correct string for an
     * LdapName according to RFC 2253.
     * </p>
     * <p>
     * The expected result is that the name returns the string exactly as given
     * in the constructor.
     * </p>
     */
    public void testToString004() throws Exception {
        LdapName ln = new LdapName("t=ll");
        assertEquals("T=LL", ln.toString().toUpperCase());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.toString()'
     * </p>
     * <p>
     * Here we are testing if the method returns the correct string for an
     * LdapName according to RFC 2253.
     * </p>
     * <p>
     * The expected results is that the name returns the string exactly as given
     * in the constructor.
     * </p>
     */
    public void testToString005() throws Exception {
        LdapName ln = new LdapName("t=#08");
        assertEquals("t=#08", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.size()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the size of an
     * LdapName.
     * </p>
     * <p>
     * The expected result is zero because the name is empty.
     * </p>
     */
    public void testSize001() throws Exception {
        LdapName ln = new LdapName("");
        assertEquals(0, ln.size());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.size()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the size of an
     * LdapName.
     * </p>
     * <p>
     * The expected result is the correct number of Rdns.
     * </p>
     */
    public void testSize002() throws Exception {
        String test = ("CN=commonName,L=localityName,ST=stateOrProvinceName,O=organizationName,OU=organizationalUnitName,C=countryName,STREET=streetAddress,DC=domainComponent,UID=userid");
        LdapName ln = new LdapName(test);
        assertEquals(9, ln.size());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.size()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the size of an
     * LdapName.
     * </p>
     * <p>
     * The expected result is the correct number of Rdns. In this case we are
     * using distincts special characters to create the name. Notice that the
     * special character "+", associates attributes Types And Values.
     * </p>
     */
    public void testSize003() throws Exception {
        LdapName ln = new LdapName("t1=test+t2=test,t3=test;t4=test");
        assertEquals(3, ln.size());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.isEmpty()'
     * </p>
     * <p>
     * Here we are testing if this method returns true when an LdapName is empty
     * and false when it is not.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testIsEmpty001() throws Exception {
        assertFalse(new LdapName("t=test").isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.isEmpty()'
     * </p>
     * <p>
     * Here we are testing if this method returns true when an LdapName is empty
     * and false when it is not. 
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testIsEmpty002() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.remove(0);
        assertTrue(ln.isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.isEmpty()'
     * </p>
     * <p>
     * Here we are testing if this method returns true when an LdapName is empty
     * and false when it is not.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testIsEmpty003() throws Exception {
        LdapName ln = new LdapName("");
        assertTrue(ln.isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getAll()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns all the RDNs that
     * make up this LdapName as Strings.
     * </p>
     * <p>
     * The expected result is if an empty name returns a non-null enumeration.
     * </p>
     */
    public void testGetAll001() throws Exception {
        LdapName ln = new LdapName("");
        Enumeration<String> x = ln.getAll();
        assertNotNull(x);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getAll()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns all the RDNs that
     * make up this LdapName as Strings.
     * </p>
     * <p>
     * The expected result is if a non empty name returns a non-null
     * enumeration, and ordered like it should be.
     * </p>
     */
    public void testGetAll002() throws Exception {
        LdapName ln = new LdapName("t=test");
        Enumeration<String> e = ln.getAll();
        assertEquals("t=test", e.nextElement());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getAll()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns all the RDNs that
     * make up this LdapName as Strings.
     * </p>
     * <p>
     * The expected result is if a non empty name returns a non-null
     * enumeration, and ordered like it should be.
     * </p>
     */
    public void testGetAll003() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        Rdn a = new Rdn("cn", "test");
        Rdn b = new Rdn("uid", "test");
        Rdn c = new Rdn("l", "test");
        Rdn d = new Rdn("st", "test");
        test.add(0, a);
        test.add(1, b);
        test.add(2, c);
        test.add(3, d);
        LdapName ln = new LdapName(test);
        Enumeration<String> e = ln.getAll();
        for (Rdn rdn : test) {
            assertEquals(rdn.toString(), e.nextElement());
        }
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is if the returned string by this method is the
     * variable wich we create the Name.
     * </p>
     */
    public void testGet001() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        assertEquals(test, ln.get(0));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an index out of bounds exception.
     * </p>
     */
    public void testGet002() throws Exception {
        try {
            String test = "t=test";
            LdapName ln = new LdapName(test);
            ln.get(1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index
     * </p>
     * <p>
     * The expected result is an index out of bounds exception.
     * </p>
     */
    public void testGet003() throws Exception {
        String test = "";
        try {
            LdapName ln = new LdapName(test);
            ln.get(0);
            fail("The name is empty.");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an index out of bounds exception.
     * </p>
     */
    public void testGet004() throws Exception {
        String test = "t=test";
        try {
            LdapName ln = new LdapName(test);
            ln.get(-1);
            fail("Fail, the index is negative.");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is in this case the non null strings of the name that
     * was created with two Rdn.
     * </p>
     */
    public void testGet005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(0, new Rdn("t=test"));
        test.add(1, new Rdn("t1=test"));
        LdapName ln = new LdapName(test);
        assertNotNull(ln.get(0));// the range of this name is 0-1
        assertNotNull(ln.get(1));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an exception like indexoutofbounds.
     * </p>
     */
    public void testGet006() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName ln = null;

        test.add(0, new Rdn("t=test"));
        test.add(1, new Rdn("t1=test"));
        ln = new LdapName(test);

        try {
            ln.get(-1);// the range of this name is 0-1
            fail("Should raise an exception.");
        } catch (IndexOutOfBoundsException e) {}

        try {
            ln.get(2);// the range of this name is 0-1
            fail("Should raise an exception.");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.get(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the part of the
     * LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is if the returned string by this method is the
     * variable wich we create the Name.
     * </p>
     */
    public void testGet007() throws Exception {
        String test1 = "t=\\ test\\ +t1=\\ test1";
        LdapName ln1 = new LdapName(test1);
        assertEquals(test1, ln1.get(0));
        
        String test2 = "t=\\20\\ te\\ s\\20t\\20\\20 + t2 = test1\\20\\ ";
        LdapName ln2 = new LdapName(test2);
        assertEquals("t=\\ \\ te s t\\ +t2=test1\\ \\ ", ln2.get(0));
    }
    
    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdn(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the RDN contained in
     * the LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is a non null Rdn.
     * </p>
     */
    public void testGetRdn001() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        LdapName ln = new LdapName(test);
        assertNotNull(ln.getRdn(0));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdn(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the RDN contained in
     * the LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an exception like IndexOutOfBounds.
     * </p>
     */
    public void testGetRdn002() throws Exception {
        try {
            LinkedList<Rdn> test = new LinkedList<Rdn>();
            test.add(new Rdn("t=test"));
            LdapName ln = new LdapName(test);
            ln.getRdn(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdn(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the RDN contained in
     * the LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an exception like IndexOutOfBounds.
     * </p>
     */
    public void testGetRdn003() throws Exception {
        try {
            LinkedList<Rdn> test = new LinkedList<Rdn>();
            test.add(new Rdn("t=test"));
            LdapName ln = new LdapName(test);
            ln.getRdn(1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdn(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the RDN contained in
     * the LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testGetRdn004() throws Exception {
        try {
            LdapName ln = new LdapName("");
            ln.getRdn(0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdn(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the RDN contained in
     * the LdapName that is at the specified index.
     * </p>
     * <p>
     * The expected result is the component RDNs returned in the correct order.
     * </p>
     */
    public void testGetRdn005() throws Exception {
        LdapName ln = new LdapName("o=other,t=test,uid=userid");
        assertEquals("uid=userid", ln.getRdn(0).toString());
        assertEquals("t=test", ln.getRdn(1).toString());
        assertEquals("o=other", ln.getRdn(2).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getPrefix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * prefix of this LdapName.
     * </p>
     * <p>
     * The expected result is in the position zero an empty name and in the
     * position one another name like "t=test".
     * </p>
     */
    public void testGetPrefix001() throws Exception {
        LdapName ln = new LdapName("t=test");
        assertEquals("", ln.getPrefix(0).toString());
        assertEquals("t=test", ln.getPrefix(1).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getPrefix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * prefix of this LdapName.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBaundsException if the range is not
     * in [0,size()].
     * </p>
     */
    public void testGetPrefix003() throws Exception {
        String test = "t=test";
        try {
            LdapName ln = new LdapName(test);
            ln.getPrefix(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getPrefix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * prefix of this LdapName.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBaundsException if the range is not
     * in [0,size()].
     * </p>
     */
    public void testGetPrefix004() throws Exception {
        String test = "t=test";
        try {
            LdapName ln = new LdapName(test);
            ln.getPrefix(2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getPrefix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * prefix of this LdapName.
     * </p>
     * <p>
     * The expected result is if the method accepts a correct index like the
     * size(), the expected result is the complete prefix name.
     * </p>
     */
    public void testGetPrefix005() throws Exception {
        String test = "t=test,t1=test,t2=test";
        LdapName ln = new LdapName(test);
        assertEquals(test, ln.getPrefix(ln.size()).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getPrefix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * prefix of this LdapName.
     * </p>
     * <p>
     * The expected result is if the method accepts a correct index like the
     * size(), the expected result is the complete prefix name, in this case
     * blank.
     * </p>
     */
    public void testGetPrefix006() throws Exception {
        String test = "";
        LdapName ln = new LdapName(test);
        assertEquals(test, ln.getPrefix(ln.size()).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is a not null name.
     * </p>
     */
    public void testGetSuffix001() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        assertNotNull(ln.getSuffix(0));
        assertEquals("t=test", ln.getSuffix(0).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is a not null name but empty.
     * </p>
     */
    public void testGetSuffix002() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        assertTrue(ln.getSuffix(1).isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is the suffix with the correct index.
     * </p>
     */
    public void testGetSuffix003() throws Exception {
        String test = "t1=test,t2=test,t3=test";
        LdapName ln = new LdapName(test);
        assertEquals("t1=test,t2=test", ln.getSuffix(1).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is an exception like IndexOutOfBounds.
     * </p>
     */
    public void testGetSuffix004() throws Exception {
        String test = "t=test";
        try {
            LdapName ln = new LdapName(test);
            ln.getSuffix(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is an exception like IndexOutOfBounds.
     * </p>
     */
    public void testGetSuffix005() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        try {
            ln.getSuffix(2);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is the complete suffix with the correct index.
     * </p>
     */
    public void testGetSuffix006() throws Exception {
        String test = "t1=test,t2=test,t3=test";
        LdapName ln = new LdapName(test);
        assertEquals(test, ln.getSuffix(0).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is the suffix with the correct index.
     * </p>
     */
    public void testGetSuffix007() throws Exception {
        String test = "t1=test,t2=test,t3=test";
        LdapName ln = new LdapName(test);
        assertEquals("t1=test", ln.getSuffix(2).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getSuffix(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns a Name that is a
     * suffix of this LdapName.
     * </p>
     * <p>
     * The expected result is a not null name but empty.
     * </p>
     */
    public void testGetSuffix008() throws Exception {
        String test = "";
        LdapName ln = new LdapName(test);
        assertNotNull(ln.getSuffix(0));
        assertTrue(ln.getSuffix(0).isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName001() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        LdapName t = null;
        assertFalse(ln.startsWith(t));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testStartsWithName002() throws Exception {
        String test = "t=test,cn=test";
        LdapName ln = new LdapName(test);
        LdapName n = new LdapName("cn=test");
        assertTrue(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName003() throws Exception {
        String test = "t=test,cn=test";
        LdapName ln = new LdapName(test);
        LdapName n = new LdapName("t=test");
        assertFalse(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName004() throws Exception {
        String test = "t=test,cn=test,o=other";
        LdapName ln = new LdapName(test);
        LdapName n = (LdapName) ln.getPrefix(ln.size());
        assertTrue(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName005() throws Exception {
        LdapName ln = new LdapName("");
        LdapName n = new LdapName("t=test");
        assertFalse(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName006() throws Exception {
        LdapName ln = new LdapName("");
        LdapName n = new LdapName("");
        assertTrue(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given prefix.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testStartsWithName007() throws Exception {
        LdapName ln = new LdapName("t=test");
        LdapName n = new LdapName("");
        assertTrue(ln.startsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is false.
     * </p>
     */
    public void testStartsWithListOfRdn001() throws Exception {
        LinkedList<Rdn> test = null;
        assertFalse(new LdapName("t=test").startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is true.
     * </p>
     */
    public void testStartsWithListOfRdn002() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        Rdn a = new Rdn("t=test");
        test.add(a);
        assertTrue(new LdapName("t=test").startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is true.
     * </p>
     */
    public void testStartsWithListOfRdn003() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        assertTrue(new LdapName("t=test").startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is false.
     * </p>
     */
    public void testStartsWithListOfRdn004() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        assertFalse(new LdapName("").startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is true.
     * </p>
     */
    public void testStartsWithListOfRdn005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        assertTrue(new LdapName("").startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.startsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * starts with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is true.
     * </p>
     */
    public void testStartsWithListOfRdn006() throws Exception {

        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        test.add(new Rdn("t1=test"));
        test.add(new Rdn("t2=test"));
        test.add(new Rdn("t3=test"));
        assertTrue(new LdapName("t3=test,t2=test,t1=test,t=test")
                .startsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testEndsWithName001() throws Exception {
        String test = "t=test";
        LdapName ln = new LdapName(test);
        LdapName t = null;
        assertFalse(ln.endsWith(t));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testEndsWithName002() throws Exception {
        String test = "t=test,cn=test";
        LdapName ln = new LdapName(test);
        LdapName n = new LdapName("t=test");
        assertTrue(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testEndsWithName003() throws Exception {
        String test = "t=test,cn=test";
        LdapName ln = new LdapName(test);
        LdapName n = new LdapName(test);
        assertTrue(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testEndsWithName004() throws Exception {
        LdapName ln = new LdapName("");
        LdapName n = new LdapName("");
        assertTrue(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a true.
     * </p>
     */
    public void testEndsWithName005() throws Exception {
        LdapName ln = new LdapName("t=test");
        LdapName n = new LdapName("");
        assertTrue(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testEndsWithName006() throws Exception {
        LdapName ln = new LdapName("");
        LdapName n = new LdapName("t=test");
        assertFalse(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given name.
     * </p>
     * <p>
     * The expected result is a false.
     * </p>
     */
    public void testEndsWithName007() throws Exception {
        String test = "t=test,cn=test";
        String test2 = "cn=test,t=test";
        LdapName ln = new LdapName(test);
        LdapName n = new LdapName(test2);
        assertFalse(ln.endsWith(n));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a null list of Rdns is sended, is a false.
     * </p>
     */
    public void testEndsWithListOfRdn001() throws Exception {
        LinkedList<Rdn> test = null;
        assertFalse(new LdapName("t=test").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a true.
     * </p>
     */
    public void testEndsWithListOfRdn002() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t", "test"));
        assertTrue(new LdapName("t=test").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a true.
     * </p>
     */
    public void testEndsWithListOfRdn003() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        assertTrue(new LdapName("").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a false.
     * </p>
     */
    public void testEndsWithListOfRdn004() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        assertFalse(new LdapName("").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a true.
     * </p>
     */
    public void testEndsWithListOfRdn005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        assertTrue(new LdapName("t=test").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns true if an LdapName
     * ends with the given list of RDNs.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a true.
     * </p>
     */
    public void testEndsWithListOfRdn006() throws Exception {

        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t", "test"));
        test.add(new Rdn("t2", "test"));
        test.add(new Rdn("t3", "test"));
        assertTrue(new LdapName("t3=test,t2=test,t=test").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.endsWith(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method determines whether the specified RDN
     * sequence forms a suffix of this LDAP name.
     * </p>
     * <p>
     * The expected result is if a non null list of Rdns is sended, is a true.
     * </p>
     */
    public void testEndsWithListOfRdn007() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t3=test3"));
        assertTrue(new LdapName("t3=test3,t2=test2,t=test").endsWith(test));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a null name is sended to add an
     * NullPointerException is thrown.
     * </p>
     */
    public void testAddAllName001() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            LdapName toadd = null;
            ln.addAll(toadd);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a non null name is sended to add, it must be in
     * order.
     * </p>
     */
    public void testAddAllName003() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.addAll(new LdapName("uid=userid"));
        assertEquals("uid=userid,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a non null name is sended to add, it must be in
     * order.
     * </p>
     */
    public void testAddAllName004() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.addAll(new LdapName("cn=common,uid=userid"));
        assertEquals("cn=common,uid=userid,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a non null name is sended to add, it must be in
     * order.
     * </p>
     */
    public void testAddAllName005() throws Exception {
        LdapName ln = new LdapName("");
        ln.addAll(new LdapName("cn=common,uid=userid"));
        assertEquals("cn=common,uid=userid", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a non null name is sended to add, it must be in
     * order.
     * </p>
     */
    public void testAddAllName006() throws Exception {
        LdapName ln = new LdapName("");
        ln.addAll(new LdapName(""));
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the components of a
     * name to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is if a non null name is sended to add, it must be in
     * order.
     * </p>
     */
    public void testAddAllName007() throws Exception {
        LdapName ln = new LdapName("cn=common,uid=userid");
        ln.addAll(new LdapName(""));
        assertEquals("cn=common,uid=userid", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the Rdns in the
     * given List to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is NullPointerException.
     * </p>
     */
    public void testAddAllListOfRdn001() throws Exception {
        LinkedList<Rdn> test = null;
        try {
            LdapName ln = new LdapName("t=test");
            ln.addAll(test);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the Rdns in the
     * given List to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is the adding in the especified order.
     * </p>
     */
    public void testAddAllListOfRdn002() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName ln = new LdapName("");
        ln.addAll(test);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the Rdns in the
     * given List to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is the adding in the especified order.
     * </p>
     */
    public void testAddAllListOfRdn003() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName ln = new LdapName("t=test");
        ln.addAll(test);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the Rdns in the
     * given List to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is the adding in the especified order.
     * </p>
     */
    public void testAddAllListOfRdn004() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        LdapName ln = new LdapName("");
        ln.addAll(test);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends the Rdns in the
     * given List to the end of this LdapName.
     * </p>
     * <p>
     * The expected result is the adding in the especified order.
     * </p>
     */
    public void testAddAllListOfRdn005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        test.add(new Rdn("t2=test"));
        LdapName ln = new LdapName("t3=test");
        ln.addAll(test);
        assertEquals("t2=test,t=test,t3=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the components of the
     * given Name to an LdapName at the given index.
     * <p>
     * The expected result is an IndexOutOfBounds Exception.
     * </p>
     */
    public void testAddAllIntName001() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            ln.addAll(2, new LdapName("uid=userid"));
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the components of the
     * given Name to an LdapName at the given index.
     * <p>
     * The expected result is an IndexOutOfBounds Exception.
     * </p>
     */
    public void testAddAllIntName002() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            ln.addAll(-1, new LdapName("uid=userid"));
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the components of the
     * given Name to an LdapName at the given index.
     * <p>
     * The expected result is the adding of the name in the especified order.
     * </p>
     */
    public void testAddAllIntName003() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.addAll(1, new LdapName("uid=userid"));
        assertEquals("uid=userid,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the components of the
     * given Name to an LdapName at the given index.
     * <p>
     * The expected result is an NullPointer Exception.
     * </p>
     */
    public void testAddAllIntName004() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            LdapName add = null;
            ln.addAll(1, add);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, Name)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the components of the
     * given Name to an LdapName at the given index.
     * <p>
     * The expected result is the adding of the name in the specified order.
     * </p>
     */
    public void testAddAllIntName005() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.addAll(0, new LdapName("uid=userid"));
        assertEquals("t=test,uid=userid", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is a NullPointerException.
     * </p>
     */
    public void testAddAllIntListOfRdn001() throws Exception {
        LinkedList<Rdn> test = null;
        try {
            LdapName ln = new LdapName("t=test");
            ln.addAll(0, test);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testAddAllIntListOfRdn002() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        try {
            test.add(new Rdn("t=test"));
            test.add(new Rdn("cn=common"));
            LdapName ln = new LdapName("t=test");
            ln.addAll(-1, test);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBounds Exception.
     * </p>
     */
    public void testAddAllIntListOfRdn003() throws Exception {

        LinkedList<Rdn> test = new LinkedList<Rdn>();
        try {
            test.add(new Rdn("t=test"));
            test.add(new Rdn("cn=common"));
            LdapName ln = new LdapName("t=test");
            ln.addAll(2, test);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is the Rdns added in the correct order.
     * </p>
     */
    public void testAddAllIntListOfRdn004() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName ln = new LdapName("");
        ln.addAll(0, test);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is the Rdns added in the correct order.
     * </p>
     */
    public void testAddAllIntListOfRdn005() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        test.add(new Rdn("t=test"));
        LdapName ln = new LdapName("");
        ln.addAll(0, test);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.addAll(int, List<Rdn>)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds the Rdns to an LdapName
     * at the given index.
     * </p>
     * <p>
     * The expected result is the Rdns added in the correct order.
     * </p>
     */
    public void testAddAllIntListOfRdn006() throws Exception {
        LinkedList<Rdn> test = new LinkedList<Rdn>();
        LdapName ln = new LdapName("t=test");
        ln.addAll(0, test);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is NullPointerException.
     * </p>
     */
    public void testAddString001() throws Exception {
        LdapName ln = new LdapName("t=test");
        try {
            ln.add((String) null);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString002() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.add("");
        assertEquals(",t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString003() throws Exception {
        LdapName ln = new LdapName("t=test");
        String x = "cn=common";
        ln.add(x);
        assertEquals("cn=common,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString004() throws Exception {
        LdapName ln = new LdapName("t=test");
        String x = "cn=common";
        assertSame(ln, ln.add(x));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString005() throws Exception {
        LdapName ln = new LdapName("");
        String x = "";
        ln.add(x);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString006() throws Exception {
        LdapName ln = new LdapName("");
        String x = "t=test";
        ln.add(x);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends a name to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the name added to the end of the LdapName.
     * </p>
     */
    public void testAddString007() throws Exception {
        LinkedList ll = new LinkedList();
        ll.add(new Rdn("t=test"));
        LdapName ln = new LdapName(ll);
        ln.add("t1=test1");
        ll.remove(0);
        assertEquals(2, ln.size());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddRdn001() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("cn=common");
        assertNotNull(ln.add(toadd));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddRdn002() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("cn=common");
        assertEquals("cn=common,t=test", ln.add(toadd).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is a NullPointerException.
     * </p>
     */
    public void testAddRdn003() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            Rdn toadd = null;
            ln.add(toadd);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddRdn004() throws Exception {
        LdapName ln = new LdapName("");
        Rdn toadd = new Rdn("");
        ln.add(toadd);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddRdn005() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("");
        ln.add(toadd);
        assertEquals(",t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly appends an RDN to the end of
     * an LdapName.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddRdn006() throws Exception {
        LdapName ln = new LdapName("");
        Rdn toadd = new Rdn("t=test");
        ln.add(toadd);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testAddIntString001() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            ln.add(-1, "cn=common");
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testAddIntString002() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            ln.add(2, "cn=common");
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is a NullPointerException.
     * </p>
     */
    public void testAddIntString003() throws Exception {
        try {
            String toadd = null;
            LdapName ln = new LdapName("t=test");
            ln.add(1, toadd);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the name being inserted in the correct position.
     * </p>
     */
    public void testAddIntString004() throws Exception {
        String toadd = "cn=common";
        LdapName ln = new LdapName("t=test");
        ln.add(1, toadd);
        assertEquals("cn=common,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the name being inserted in the correct position.
     * </p>
     */
    public void testAddIntString005() throws Exception {
        String toadd = "cn=common";
        LdapName ln = new LdapName("t=test");
        assertSame(ln, ln.add(1, toadd));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the name being inserted in the correct position.
     * </p>
     */
    public void testAddIntString006() throws Exception {
        String toadd = "";
        LdapName ln = new LdapName("");
        ln.add(0, toadd);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the name being inserted in the correct position.
     * </p>
     */
    public void testAddIntString007() throws Exception {
        String toadd = "";
        LdapName ln = new LdapName("t=test");
        ln.add(1, toadd);
        assertEquals(",t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, String)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds a name to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the name being inserted in the correct position.
     * </p>
     */
    public void testAddIntString008() throws Exception {
        String toadd = "t=test";
        LdapName ln = new LdapName("");
        ln.add(0, toadd);
        assertEquals("t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testAddIntRdn001() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            Rdn toadd = new Rdn("cn=common");
            ln.add(-1, toadd);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testAddIntRdn002() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            Rdn toadd = new Rdn("cn=common");
            ln.add(3, toadd);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is a NullPointerException.
     * </p>
     */
    public void testAddIntRdn003() throws Exception {
        try {
            LdapName ln = new LdapName("t=test");
            Rdn toadd = null;
            ln.add(1, toadd);
            fail("NPE expected");
        } catch (NullPointerException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddIntRdn004() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("cn=common");
        ln.add(1, toadd);
        assertEquals("cn=common,t=test", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddIntRdn005() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("cn=common");
        ln.add(0, toadd);
        assertEquals("t=test,cn=common", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddIntRdn006() throws Exception {
        LdapName ln = new LdapName("");
        Rdn toadd = new Rdn("");
        ln.add(0, toadd);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
     * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddIntRdn007() throws Exception {
        LdapName ln = new LdapName("");
        Rdn toadd = new Rdn("t=test");
        assertEquals("t=test", ln.add(0, toadd).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.add(int, Rdn)'
     * </p>
     * <p>
      * Here we are testing if this method correctly adds an RDN to an
     * LdapName at the given index.
     * </p>
     * <p>
     * The expected result is the Rdn being inserted in the correct position.
     * </p>
     */
    public void testAddIntRdn008() throws Exception {
        LdapName ln = new LdapName("t=test");
        Rdn toadd = new Rdn("");
        assertEquals(",t=test", ln.add(1, toadd).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.remove(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly removes the name at the
     * specified index from an LdapName.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testRemove001() throws Exception {
        LdapName ln = new LdapName("t=test");
        try {
            ln.remove(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}

        try {
            ln.remove(3);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.remove(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly removes the name at the
     * specified index from an LdapName.
     * </p>
     * <p>
     * The expected result is that the name at the specified index is removed.
     * </p>
     */
    public void testRemove003() throws Exception {
        Rdn x = new Rdn("t=test");
        LdapName ln = new LdapName("t=test");
        assertEquals(x.toString(), ln.remove(0).toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.remove(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly removes the name at the
     * specified index from an LdapName.
     * </p>
     * <p>
     * The expected result is that the name at the specified index is removed.
     * </p>
     */
    public void testRemove004() throws Exception {
        LdapName ln = new LdapName("t=test");
        ln.remove(0);
        assertEquals("", ln.toString());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.remove(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly removes the name at the
     * specified index from an LdapName.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testRemove005() throws Exception {
        try {
            LdapName ln = new LdapName("");
            ln.remove(0);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.remove(int)'
     * </p>
     * <p>
     * Here we are testing if this method correctly removes the name at the
     * specified index from an LdapName.
     * </p>
     * <p>
     * The expected result is an IndexOutOfBoundsException.
     * </p>
     */
    public void testRemove006() throws Exception {
        LdapName ln = new LdapName("t=test, t1=test1");
        assertEquals("t=test", (String)ln.remove(1));
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdns()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the contents of this
     * LdapName as a list of Rdns.
     * </p>
     * <p>
     * The expected result is the list of Rdns.
     * </p>
     */
    public void testGetRdns001() throws Exception {
        LdapName ln = new LdapName("");
        List<Rdn> empty = ln.getRdns();
        assertTrue(empty.isEmpty());
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.getRdns()'
     * </p>
     * <p>
     * Here we are testing if this method correctly returns the contents of this
     * LdapName as a list of Rdns.
     * </p>
     * <p>
     * The expected result is the list of Rdns.
     * </p>
     */
    public void testGetRdns002() throws Exception {
        LdapName ln = new LdapName("uid=userid,t=test,cn=common");
        LinkedList<Rdn> compare = new LinkedList<Rdn>();
        compare.add(0, new Rdn("cn=common"));
        compare.add(1, new Rdn("t=test"));
        compare.add(2, new Rdn("uid=userid"));
        LinkedList<Rdn> notempty = new LinkedList<Rdn>(ln.getRdns());

        assertEquals(compare.size(), notempty.size());

        for (int j = 0; j < notempty.size(); j++) {
            assertEquals(compare.get(j), notempty.get(j));
        }
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a classcastException.
     * </p>
     */
    public void testCompareTo001() throws Exception {
        try {
            LdapName ln = new LdapName("t=test,cn=common");
            Rdn tocomp = null;
            ln.compareTo(tocomp);
            fail("The string is null.");
        } catch (ClassCastException e) {}
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a positive int.
     * </p>
     */
    public void testCompareTo002() throws Exception {
        LdapName ln = new LdapName("cn=common,t=test");
        LdapName ln2 = new LdapName("t=test");
        assertFalse(ln.compareTo(ln2) <= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares the LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo003() throws Exception {
        LdapName ln = new LdapName("t=test,cn=common");
        LdapName ln2 = new LdapName("t=test,cn=common");
        assertFalse(ln.compareTo(ln2) != 0);

        
        //make it compatible to RI
        ln = new LdapName("Z=Z,A=A");
        ln2 = new LdapName("A=A,Z=Z");
        assertTrue(ln.compareTo(ln2) < 0);

        ln = new LdapName("Z=Z");
        ln2 = new LdapName("A=A,Z=Z");
        assertTrue(ln.compareTo(ln2) < 0);
        assertTrue(ln2.compareTo(ln) > 0);

    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a negative int.
     * </p>
     */
    public void testCompareTo004() throws Exception {
        LdapName ln = new LdapName("t=test,cn=common");
        LdapName ln2 = new LdapName("t=test,cn=common,uid=userid");
        assertFalse(ln.compareTo(ln2) >= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a positive int.
     * </p>
     */
    public void testCompareTo005() throws Exception {
        LdapName ln = new LdapName("t=test,cn=common");
        LdapName ln2 = new LdapName("");
        assertFalse(ln.compareTo(ln2) <= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a negative int.
     * </p>
     */
    public void testCompareTo006() throws Exception {
        LdapName ln = new LdapName("");
        LdapName ln2 = new LdapName("t=test,cn=common");
        assertFalse(ln.compareTo(ln2) >= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo007() throws Exception {
        LdapName ln = new LdapName("");
        LdapName ln2 = new LdapName("");
        assertFalse(ln.compareTo(ln2) != 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a negative int.
     * </p>
     */
    public void testCompareTo008() throws Exception {
        LdapName ln = new LdapName("t=test,cn=common");
        LdapName ln2 = new LdapName("cn=common,t=test");
        assertFalse(ln.compareTo(ln2) >= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a negative int.
     * </p>
     */
    public void testCompareTo009() throws Exception {
        LdapName ln = new LdapName("t=test,cn=common");
        LdapName ln2 = new LdapName("t=test");
        assertFalse(ln.compareTo(ln2) >= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a negative int.
     * </p>
     */
    public void testCompareTo010() throws Exception {
        LdapName ln = new LdapName("t=test1,cn=common");
        LdapName ln2 = new LdapName("t=test,cn=common1");
        assertFalse(ln.compareTo(ln2) >= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is a positive int.
     * </p>
     */
    public void testCompareTo011() throws Exception {
        LdapName ln = new LdapName("t=test1");
        LdapName ln2 = new LdapName("t=tes1t");
        assertFalse(ln.compareTo(ln2) <= 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo012() throws Exception {
        LdapName ln = new LdapName("T=teSt1");
        LdapName ln2 = new LdapName("t=test1");
        assertTrue(ln.compareTo(ln2) == 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo013() throws Exception {
        LdapName ln = new LdapName("T= teSt1");
        LdapName ln2 = new LdapName("t=test1 ");
        assertTrue(ln.compareTo(ln2) == 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo014() throws Exception {
        LdapName ln = new LdapName("T= teSt1,d=   here,h =THAT");
        LdapName ln2 = new LdapName("t=teSt1,d=here,h=that");
        assertTrue(ln.compareTo(ln2) == 0);
    }

    /**
     * <p>
     * Test method for 'javax.naming.ldap.LdapName.compareTo(Object)'
     * </p>
     * <p>
     * Here we are testing if this method correctly compares an LdapName with
     * the given object.
     * </p>
     * <p>
     * The expected result is zero.
     * </p>
     */
    public void testCompareTo015() throws Exception {
        LdapName ln = new LdapName(
                "T= teSt1+f   =  anything,d=   here; j=uos<asd,h =THAT,");
        LdapName ln2 = new LdapName(
                "t=test1+f=anything,d=here;j=uos<asd,h=that,");
        assertTrue(ln.compareTo(ln2) == 0);
    }

    public void testSerializationCompatibility() throws Exception{
        LdapName object = new LdapName("t=test\\, , t1=\\ test1");
        object.add(new Rdn("t2=te\\ st2"));
        SerializationTest.verifyGolden(this, object);
    }
}
