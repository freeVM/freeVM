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
* @author Alexey V. Varlamov
* @version $Revision$
*/

package java.security;

import junit.framework.TestCase;

/**
 * Tests for <code>UnresolvedPermission</code> class fields and methods
 * 
 */

public class UnresolvedPermissionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(UnresolvedPermissionTest.class);
    }

    /**
     * Constructor for UnresolvedPermissionTest.
     * @param arg0
     */
    public UnresolvedPermissionTest(String arg0) {
        super(arg0);
    }
    
    /**
     * Creates an Object with given name, type, action, certificaties. 
     * Empty or null type is not allowed - exception should be thrown.
     */
    public void testCtor()
    {
        String type = "laskjhlsdk 2345346";
        String name = "^%#UHVKU^%V  887y";
        String action = "JHB ^%(*&T klj3h4";
        UnresolvedPermission up = new UnresolvedPermission(type, name, action, null);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + " " + name + " " + action + ")", up.toString());
        
        up = new UnresolvedPermission(type, null, null, null);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + " null null)", up.toString());
        
        up = new UnresolvedPermission(type, "", "", new java.security.cert.Certificate[0]);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + "  )", up.toString());
        
        try {
            new UnresolvedPermission(null, name, action, null);
            fail("exception is not thrown on null type");
        }
        catch (Exception ok) {}
        /*try {
            new UnresolvedPermission("", name, action, null);
            fail("exception is not thrown on empty type");
        }
        catch (Exception ok) {}*/
    }
    
    /**
     * This test is valid since 1.5 release only. Checks that UnresolvedPermission returns the proper 
     * data for target permission. For non-empty certificates array, 
     * returns a new array each time this method is called.
     */
    public void testTargetData()
    {
        String type = "laskjhlsdk 2345346";
        String name = "^%#UHVKU^%V  887y";
        String action = "JHB ^%(*&T klj3h4";
        UnresolvedPermission up = new UnresolvedPermission(type, name, action, null);
        assertEquals(type, up.getUnresolvedType());
        assertEquals(name, up.getUnresolvedName()); 
        assertEquals(action, up.getUnresolvedActions()); 
        assertNull(up.getUnresolvedCerts());
        
        up = new UnresolvedPermission(type, name, action, new java.security.cert.Certificate[0]);
        assertNull("Empty array should be the same as null", up.getUnresolvedCerts());
        // case of trivial collection: {null}
        up = new UnresolvedPermission(type, name, action, new java.security.cert.Certificate[3]);
        assertNull(up.getUnresolvedCerts());
        //assertNotSame(up.getUnresolvedCerts(), up.getUnresolvedCerts());
        //assertEquals(1, up.getUnresolvedCerts().length);
    }
    
    public void testEquals()
    {
        String type = "KJHGUiy 24y";
        String name = "kjhsdkfj ";
        String action = "T klj3h4";
        UnresolvedPermission up = new UnresolvedPermission(type, name, action, null);
        UnresolvedPermission up2 = new UnresolvedPermission(type, name, action, null);
        assertFalse(up.equals(null));
        assertFalse(up.equals(new Object()));
        assertFalse(up.equals(new BasicPermission("df"){}));
        assertTrue(up.equals(up));
        assertTrue(up.equals(up2));
        assertTrue(up.hashCode() == up2.hashCode());
        up2 = new UnresolvedPermission(type, name, action, new java.security.cert.Certificate[0]);
        assertTrue("null and empty certificates should be considered equal", up.equals(up2));
        assertTrue(up.hashCode() == up2.hashCode());
        up2 = new UnresolvedPermission(type, name, action, new java.security.cert.Certificate[2]);
        assertTrue(up.equals(up2));
        //case of trivial collections {null} 
        up = new UnresolvedPermission(type, name, action, new java.security.cert.Certificate[10]);
        assertTrue(up.equals(up2));
        assertTrue(up.hashCode() == up2.hashCode());
    }
    
    /** 
     * UnresolvedPermission never implies any other permission.
     */
    public void testImplies()
    {
        UnresolvedPermission up = new UnresolvedPermission("java.security.SecurityPermission", "a.b.c", null, null);
        assertFalse(up.implies(up));
        assertFalse(up.implies(new AllPermission()));
        assertFalse(up.implies(new SecurityPermission("a.b.c")));
    }
    
    /**
     * newPermissionCollection() should return new BasicPermissionCollection on every invokation
     */
    public void testCollection()
    {
        UnresolvedPermission up = new UnresolvedPermission("a.b.c", null, null, null);
        PermissionCollection pc1 = up.newPermissionCollection();
        PermissionCollection pc2 = up.newPermissionCollection();
        assertTrue((pc1 instanceof UnresolvedPermissionCollection) && (pc2 instanceof UnresolvedPermissionCollection));
        assertNotSame(pc1, pc2);
    }

    /**
     * resolve the unresolved permission to the permission of specified class.
     */
    public void testResolve()
    {
        String name = "abc";
        UnresolvedPermission up = new UnresolvedPermission("java.security.SecurityPermission", name, null, null);
        Permission expected = new SecurityPermission(name);
        //test valid input
        assertEquals(expected, up.resolve(SecurityPermission.class));
        
        //test invalid class
        assertNull(up.resolve(Object.class));
        
        //test invalid signers
        //up = new UnresolvedPermission("java.security.SecurityPermission", name, null, new java.security.cert.Certificate[1]);
        //assertNull(up.resolve(SecurityPermission.class));
        
        //another valid case
        up = new UnresolvedPermission("java.security.AllPermission", null, null, new java.security.cert.Certificate[0]);
        assertEquals(new AllPermission(name, ""), up.resolve(AllPermission.class));
    }
}
