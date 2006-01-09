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
* @author Stepan M. Mishura
* @version $Revision$
*/

package javax.security.auth;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import com.openintel.drl.security.test.SecurityTest;

/**
 * Tests SubjectDomainCombiner class
 */

public class SubjectDomainCombinerTest extends SecurityTest {

    public static void main(String[] args) {
        junit.textui.TestRunner
                .run(javax.security.auth.SubjectDomainCombinerTest.class);
    }

    /**
     * Testing getSubject() & constructor
     */
    public final void testGetSubject() {

        Subject subject = new Subject();

        SubjectDomainCombiner combiner = new SubjectDomainCombiner(subject);

        assertTrue("Subject is not null", subject == combiner.getSubject());

        try {
            combiner = new SubjectDomainCombiner(null);
        } catch (NullPointerException e) {
            if(!testing){
                throw e;
            }
            return;
        }
        assertNull("Subject is null", combiner.getSubject());
    }

    /**
     * Testing combine(ProtectionDomain[], ProtectionDomain[]) 
     */
    public final void testCombine() throws Exception {

        Principal principal = new Principal() {
            public String getName() {
                return "principal";
            }
        };

        Subject subject = new Subject();

        subject.getPrincipals().add(principal);

        SubjectDomainCombiner combiner = new SubjectDomainCombiner(subject);

        ProtectionDomain[] pd;

        // both prarameters are null
        pd = combiner.combine(null, null);
        assertEquals("Returned protection domain array", null, pd);

        // check assigned principals
        URL url = new URL("file://foo.txt");

        CodeSource source = new CodeSource(url, (Certificate[]) null);
        PermissionCollection permissions = new Permissions();
        ClassLoader classLoader = new URLClassLoader(new URL[] { url });

        Principal p = new Principal() {
            public String getName() {
                return "p";
            }
        };
        Principal[] principals = new Principal[] { p };

        ProtectionDomain domain = new ProtectionDomain(source, permissions,
                classLoader, principals);

        pd = combiner.combine(new ProtectionDomain[] { domain }, null);

        assertTrue("CodeSource", source == pd[0].getCodeSource());
        assertTrue("PermissionCollection", permissions == pd[0]
                .getPermissions());
        assertTrue("ClassLoader", classLoader == pd[0].getClassLoader());

        assertEquals("Size", 1, pd[0].getPrincipals().length);
        assertTrue("Principal", principal == (pd[0].getPrincipals())[0]);

        // check inherited domains
        pd = combiner.combine(null, new ProtectionDomain[] { domain });
        assertTrue("Inherited domain", domain == pd[0]);
    }

    public final void testCombine_NullSubject() throws Exception {

        if(testing){
            return;
        }
        
        SubjectDomainCombiner combiner = new SubjectDomainCombiner(null);

        // check assigned principals
        URL url = new URL("file://foo.txt");

        CodeSource source = new CodeSource(url, (Certificate[]) null);
        PermissionCollection permissions = new Permissions();
        ClassLoader classLoader = new URLClassLoader(new URL[] { url });

        Principal p = new Principal() {
            public String getName() {
                return "p";
            }
        };
        Principal[] principals = new Principal[] { p };

        ProtectionDomain domain = new ProtectionDomain(source, permissions,
                classLoader, principals);

        ProtectionDomain[] pd = combiner.combine(
                new ProtectionDomain[] { domain }, null);
        
        assertTrue("Size", pd[0].getPrincipals().length==0);

    }

    public final void testSecurityException() {

        denyPermission(new AuthPermission("getSubjectFromDomainCombiner"));

        Subject subject = new Subject();

        SubjectDomainCombiner combiner = new SubjectDomainCombiner(subject);

        try {
            combiner.getSubject();
        } catch (AccessControlException e) {
            assertEquals(e, AuthPermission.class);
        }
    }
}