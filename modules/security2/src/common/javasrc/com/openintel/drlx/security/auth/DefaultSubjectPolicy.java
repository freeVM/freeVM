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

package com.openintel.drlx.security.auth;

import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.security.auth.AuthPermission;
import javax.security.auth.Policy;
import javax.security.auth.Subject;

import org.apache.harmony.security.PolicyEntry;

import com.openintel.fortress.drl.security.DefaultPolicyParser;
import com.openintel.fortress.drl.security.PolicyUtils;

/**
 * Default implementation for subject-based policy 
 */
public class DefaultSubjectPolicy extends Policy {

    private static final AuthPermission REFRESH_POLICY = new AuthPermission(
            "refreshPolicy");

    // System property for dynamically added policy location.
    private static final String JAAS_SECURITY_POLICY = "java.security.auth.policy";

    // Prefix for numbered Policy locations specified in security.properties.
    private static final String POLICY_URL_PREFIX = "auth.policy.url.";

    // A flag to denote whether this policy object was initialized or not. 
    private boolean isInitialized;

    // A set of PolicyEntries constituting this Policy.
    private HashSet set;

    // A specific parser for a particular policy file format.
    // The implementation of parse thread-safe, so static instance is used 
    private static final DefaultPolicyParser parser = new DefaultPolicyParser();

    // empty source object for getPermissions method
    private static final CodeSource emptySource = new CodeSource(null,
            (Certificate[]) null);

    public DefaultSubjectPolicy() {
        isInitialized = false;
    }

    public PermissionCollection getPermissions(Subject subject, CodeSource cs) {
        if (!isInitialized) {
            init();
        }

        Collection pc = new HashSet();
        Iterator it = set.iterator();

        if (subject != null) {
            int size = subject.getPrincipals().size();
            Principal[] p = new Principal[size];
            subject.getPrincipals().toArray(p);

            if (cs == null) {
                cs = emptySource;
            }

            while (it.hasNext()) {
                PolicyEntry ge = (PolicyEntry) it.next();
                if (ge.impliesCodeSource(cs) && ge.impliesPrincipals(p)) {
                    pc.addAll(ge.getPermissions());
                }
            }
        }
        // TODO what about caching returned objects??? 

        return PolicyUtils.toPermissionCollection(pc);
    }

    public void refresh() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(REFRESH_POLICY);
        }
        init();
    }

    private synchronized void init() {

        set = new HashSet();

        Properties system = new Properties((Properties) AccessController
                .doPrivileged(new PolicyUtils.SystemKit()));
        system.setProperty("/", File.separator);
        URL[] policyLocations = PolicyUtils.getPolicyURLs(system,
                JAAS_SECURITY_POLICY, POLICY_URL_PREFIX);

        for (int i = 0; i < policyLocations.length; i++) {
            try {
                //TODO debug log
                //System.err.println("Parsing policy file: " + policyLocations[i]);
                set.addAll(parser.parse(policyLocations[i], system));
            } catch (Exception e) {
                // TODO log warning
                //System.err.println("Ignoring policy file: " 
                //                 + policyLocations[i] + ". Reason:\n"+ e);
            }
        }

        isInitialized = true;
    }
}
