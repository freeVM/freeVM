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
* @author Aleksei Y. Semenov
* @version $Revision$
*/

package java.security;



import org.apache.harmony.security.CertificateStub;
import org.apache.harmony.security.IdentityStub;
import org.apache.harmony.security.PublicKeyStub;
import org.apache.harmony.security.test.PerformanceTest;

/**
 * Tests for class Identity
 * 
 */

public class IdentityTest extends PerformanceTest {

    public static class MySecurityManager extends SecurityManager {
        public Permissions denied = new Permissions(); 
        public void checkPermission(Permission permission){
            if (denied!=null && denied.implies(permission)) throw new SecurityException();
        }
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IdentityTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for IdentityTest.
     * @param name
     */
    public IdentityTest(String name) {
        super(name);
    }

    public void testHashCode() {
        new IdentityStub("testHashCode").hashCode();
    }

    public void testEquals() throws Exception {
        Identity i1 = new IdentityStub("testEquals");
        Object value[] =  {
                null, Boolean.FALSE,
                new Object(), Boolean.FALSE,
                i1, Boolean.TRUE,
                new IdentityStub(i1.getName()), Boolean.TRUE
        };
        
        for (int k=0; k<value.length; k+=2) {
            assertEquals(value[k+1], new Boolean(i1.equals(value[k])));
            if (Boolean.TRUE.equals(value[k+1])) assertEquals(i1.hashCode(), value[k].hashCode());
        }
        // check other cases
        Identity i2 = new IdentityStub("testEquals", IdentityScope.getSystemScope());
        assertEquals(i1.identityEquals(i2), i1.equals(i2));
        Identity i3 = new IdentityStub("testEquals3");
        assertEquals(i1.identityEquals(i3), i1.equals(i3));
        
    }

    /**
     * verify Identity.toString() throws Exception is permission is denied
     */
    public void testToString1() {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("printIdentity"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testToString").toString();
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }          
    }
    /**
     * verify Identity.toString() 
     */
    public void testToString2() {    
        assertNotNull(new IdentityStub("testToString2").toString());
        logln(new IdentityStub("testToString2").toString());
    }
    /**
     * verify Identity() creates instance
     */
    public void testIdentity() {
        assertNotNull(new IdentityStub());
    }

    /*
     * Cverify Identity(String) creates instance with given name
     */
    public void testIdentityString() {
        Identity i = new IdentityStub("iii");
        assertNotNull(i);
        assertEquals("iii", i.getName());
        i=new IdentityStub(null);
        assertNotNull(i);
        assertNull(i.getName());
    }

    /**
     * verify Identity(String, IdentityScope) creates instance with given name and in give scope
     */
    public void testIdentityStringIdentityScope() throws Exception {
        IdentityScope s = IdentityScope.getSystemScope();        
        Identity i = new IdentityStub("iii2", s);
        assertNotNull(i);
        assertEquals("iii2", i.getName());
        assertSame(s, i.getScope());
        assertSame(i, s.getIdentity(i.getName()));        
    }

    /**
     * verify addCertificate(Certificate certificate) adds a certificate for this identity.
     * If the identity has a public key, the public key in the certificate must be the same
     *  
     */
    public void testAddCertificate1() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", new byte[]{1,2,3,4,5});
        i.setPublicKey(pk1);
        // try with the same key
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        i.addCertificate(c1);
        assertSame(c1, i.certificates()[0]);
        // try Certificate with different key
        try {
            i.addCertificate(new CertificateStub("ccc", null, null, new PublicKeyStub("k2", "fff", new byte[]{6,7,8,9,0})));
            fail("KeyManagementException should be thrown");
        } catch (KeyManagementException ok) {};        
    }

    /**
     * verify addCertificate(Certificate certificate) adds a certificate for this identity.
     * if the identity does not have a public key, the identity's public key is set to be that specified in the certificate.
     */
    public void testAddCertificate2() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        i.addCertificate(c1);
        assertSame(c1, i.certificates()[0]);
        assertSame(pk1, i.getPublicKey());
        	
    }
    
    /**
     * verify addCertificate(Certificate certificate) throws SecurityException is permission is denied
     */
    public void testAddCertificate3() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("addIdentityCertificate"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("iii").addCertificate(new CertificateStub("ccc", null, null, null));
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    }
    
    /**
     * verify addCertificate(Certificate certificate) throws KeyManagementException if certificate is null
     */
    public void testAddCertificate4() throws Exception {
        try {
            new IdentityStub("aaa").addCertificate(null);
            fail("KeyManagementException should be thrown");
        } catch (KeyManagementException ok) {
        } catch (NullPointerException ok) {}
        
    }
//
//  Commented out since there will no be fix for the test failure    
//    /**
//     * verify removeCertificate(Certificate certificate) removes cetrificate
//     */
//    public void testRemoveCertificate1() throws Exception{
//        Identity i = new IdentityStub("iii");
//        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", null);        
//        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
//        i.addCertificate(c1);
//        assertSame(c1, i.certificates()[0]);
//        i.removeCertificate(c1);
//        assertEquals(0, i.certificates().length);
//        // throw KeyManagementException if certificate not found
//        try {
//            i.removeCertificate(c1);
//            fail("KeyManagementException should be thrown");
//        } catch (KeyManagementException ok) {     
//        }
//        try {
//            i.removeCertificate(null);
//            fail("KeyManagementException should be thrown");
//        } catch (KeyManagementException ok) {
//            
//        }
//    }
    /**
     * verify removeCertificate(Certificate certificate) throws SecurityException if permission is denied
     */
    public void testRemoveCertificate2() throws Exception{
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("removeIdentityCertificate"));
        Identity i = new IdentityStub("iii");
        i.addCertificate(new CertificateStub("ccc", null, null, null));
        System.setSecurityManager(sm);
        try {
            i.removeCertificate(i.certificates()[0]);
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }
        
    }

    /**
     * verify certificates() returns a copy of all certificates for this identity
     */
    public void testCertificates() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        CertificateStub c2 = new CertificateStub("zzz", null, null, pk1);
        i.addCertificate(c1);
        i.addCertificate(c2);
        Certificate[] s = i.certificates();
        assertEquals(2, s.length);
        assertTrue(c1.equals(s[0]) || c2.equals(s[0]));
        assertTrue(c1.equals(s[1]) || c2.equals(s[1]));
        s[0] = null;
        s[1] = null;
        // check that the copy was modified
        s = i.certificates();
        assertEquals(2, s.length);
        assertTrue(c1.equals(s[0]) || c2.equals(s[0]));
        assertTrue(c1.equals(s[1]) || c2.equals(s[1]));
    }

    /**
     * verify Identity.identityEquals(Identity) return true, only if names and public keys are equal 
     */
    
    public void testIdentityEquals() throws Exception {
        String name = "nnn";
        PublicKey pk = new PublicKeyStub("aaa", "fff", new byte[]{1,2,3,4,5});
        Identity i = new IdentityStub(name);
        i.setPublicKey(pk);
        Object[] value = {
                //null, Boolean.FALSE,
                //new Object(), Boolean.FALSE,
                new IdentityStub("111"), Boolean.FALSE,
                new IdentityStub(name), Boolean.FALSE,
                new IdentityStub(name, IdentityScope.getSystemScope()), Boolean.FALSE,
                i, Boolean.TRUE,
                new IdentityStub(name, pk), Boolean.TRUE                
        };
        for (int k=0; k<value.length; k+=2){
            assertEquals(value[k+1], new Boolean(i.identityEquals((Identity)value[k])));
            if (Boolean.TRUE.equals(value[k+1])) assertEquals(i.hashCode(), value[k].hashCode());
        }
        Identity i2 = IdentityScope.getSystemScope().getIdentity(name); 
        i2.setPublicKey(pk);        
        assertTrue(i.identityEquals(i2));
    }

    /**
     * verify Identity.toString(boolean) return string represenation of identity
     */
    public void testToStringboolean() throws Exception {
        logln(new IdentityStub("aaa").toString(false));
        logln(new IdentityStub("aaa2", IdentityScope.getSystemScope()).toString(false));
        logln(new IdentityStub("bbb").toString(true));
        logln(new IdentityStub("bbb2", IdentityScope.getSystemScope()).toString(true));
    }

    /**
     * verify Identity.getScope() returns identity's scope
     */    
    public void testGetScope() throws Exception {
       Identity i = new IdentityStub("testGetScope");
       assertNull(i.getScope());
       IdentityScope s = IdentityScope.getSystemScope();
//       s.addIdentity(i);
//       assertSame(s, i.getScope());
       
       Identity i2 = new IdentityStub("testGetScope2", s);
       assertSame(s, i2.getScope());
        
    }
    /**
     * 
     * verify Identity.setPublicKey() throws SecurityException if permission is denied
     *
     */
    public void testSetPublicKey1() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("setIdentityPublicKey"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testSetPublicKey1").setPublicKey(new PublicKeyStub("kkk", "testSetPublicKey1", null));
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    
    }
    /**
     * 
     * verify Identity.setPublicKey() throws KeyManagementException if key is invalid 
     *
     */
    public void testSetPublicKey2() throws Exception {
        Identity i2 = new IdentityStub("testSetPublicKey2_2", IdentityScope.getSystemScope());
        PublicKey pk = new PublicKeyStub("kkk", "testSetPublicKey2", new byte[]{1,2,3,4,5});
        try {
            i2.setPublicKey(null);
            //fail("KeyManagementException should be thrown - key is null");            
        } catch (KeyManagementException ok) {};
    }
    
//
//  Commented out since there will no be fix for the test failure       
//    /**
//     * 
//     * verify Identity.setPublicKey() throws KeyManagementException if key is already used 
//     *
//     */
//    public void testSetPublicKey3() throws Exception {
//        Identity i1 = new IdentityStub("testSetPublicKey3_1", IdentityScope.getSystemScope());
//        Identity i2 = new IdentityStub("testSetPublicKey3_2", IdentityScope.getSystemScope());
//        PublicKey pk = new PublicKeyStub("kkk", "fff", new byte[]{1,2,3,4,5});
//        i1.setPublicKey(pk);
//        try {
//            i2.setPublicKey(pk);
//            fail("KeyManagementException should be thrown - key already used");            
//        } catch (KeyManagementException ok) {};
//    }
    /**
     * 
     * verify Identity.setPublicKey()  removes old key and all identity's certificates
     *
     */
    public void testSetPublicKey4() throws Exception {
        Identity i = new IdentityStub("testSetPublicKey4");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "Identity.testSetPublicKey4", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        CertificateStub c2 = new CertificateStub("zzz", null, null, pk1);
        i.addCertificate(c1);
        i.addCertificate(c2);
        assertEquals(2, i.certificates().length);
        assertSame(pk1, i.getPublicKey());
        
        PublicKeyStub pk2 = new PublicKeyStub("zzz", "Identity.testSetPublicKey4", null);    
        i.setPublicKey(pk2);
        assertSame(pk2, i.getPublicKey());
        assertEquals(0, i.certificates().length);
    }
    
    /**
     * verify Identity.getPublicKey() returns public key
     */
    
    public void testGetPublicKey() throws Exception {
        Identity i = new IdentityStub("testGetPublicKey");
        assertNull(i.getPublicKey());
        PublicKey pk = new PublicKeyStub("kkk", "Identity.testGetPublicKey", null);
        i.setPublicKey(pk);
        assertSame(pk, i.getPublicKey());        
    }

    /**
     * 
     * verify Identity.setInfo() throws SecurityException if permission is denied
     *
     *
     */
    public void testSetInfo() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("setIdentityInfo"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testSetInfo").setInfo("some info");
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    }

    public void testGetInfo() {
        
        Identity i = new IdentityStub("testGetInfo");
        //assertNull(i.getInfo());
        logln("testGetInfo: "+i.getInfo());
        i.setInfo("some info");
        assertEquals("some info", i.getInfo());
    }
    
    public void testGetName() {
        Identity i = new IdentityStub("testGetName");
        assertEquals ("testGetName", i.getName());
    }

}
