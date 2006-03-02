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

import java.util.Enumeration;

import org.apache.harmony.security.fortress.DefaultPolicy;
import org.apache.harmony.security.fortress.PolicyUtils;


/**
 * @com.intel.drl.spec_ref
 * @deprecated Use
 *             {@link java.security.Policy#getPermissions(java.security.ProtectionDomain)
 *             Policy.getPermissions(ProtectionDomain)} and
 *             {@link java.security.ProtectionDomain#ProtectionDomain(java.security.CodeSource, java.security.PermissionCollection, ClassLoader, java.security.Principal[])
 *             ProtectionDomain(CodeSource, PermissionCollection, ClassLoader,
 *             Principal[]} to establish a policy's permissions for a principal.
 */

public abstract class Policy {
    
    // Key to security properties, defining default policy provider.
    private static final String POLICY_PROVIDER = "policy.provider";

    // The SecurityPermission required to set custom Policy.
    private static final SecurityPermission SET_POLICY = new SecurityPermission(
            "setPolicy");

    // The SecurityPermission required to get current Policy.
    private static final SecurityPermission GET_POLICY = new SecurityPermission(
            "getPolicy");

    // The policy currently in effect. 
    private static Policy activePolicy;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract PermissionCollection getPermissions(CodeSource cs);

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void refresh();

    /**
     * @com.intel.drl.spec_ref
     * The returned collection does not include static permissions of the domain.
     */
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (domain != null) {
            return getPermissions(domain.getCodeSource());
        }
        return new Permissions();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean implies(ProtectionDomain domain, Permission permission) {
        if (domain != null) {
            PermissionCollection total = getPermissions(domain);
            PermissionCollection inherent = domain.getPermissions();
            if (total == null) {
                total = inherent;
            } else if (inherent != null) {
                for (Enumeration en = inherent.elements(); en.hasMoreElements();) {
                    total.add((Permission)en.nextElement());
                }
            }
            if (total != null && total.implies(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref
     * If policy was set to <code>null</code>, loads default provider, 
     * so this method never returns <code>null</code>.
     */
    public static Policy getPolicy() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GET_POLICY);
        }
        return getAccessiblePolicy();
    }

     // Reads name of default policy provider from security.properties,
     // loads the class and instantiates the provider.<br> 
     // In case of any error, including undefined provider name, 
     // returns new instance of org.apache.harmony.security.FilePolicy provider. 
    private static Policy getDefaultProvider() {
        final String defaultClass = (String) AccessController
                .doPrivileged(new PolicyUtils.SecurityPropertyAccessor(
                        POLICY_PROVIDER));
        if (defaultClass == null) {
            //TODO log warning
            //System.err.println("No policy provider specified. Loading the " 
            //           + DefaultPolicy.class.getName());
            return new DefaultPolicy();
        }

        // TODO accurate classloading
        return (Policy) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    return Class.forName(defaultClass, true,
                            ClassLoader.getSystemClassLoader()).newInstance();
                }
                catch (Exception e) {
                    //TODO log error 
                    //System.err.println("Error loading policy provider <" 
                    //                 + defaultClass + "> : " + e 
                    //                 + "\nSwitching to the default " 
                    //                 + DefaultPolicy.class.getName());
                    return new DefaultPolicy();
                }
            }
        });

    }
    
    /**
     * Returns true if system policy provider is instantiated.
     */
    static boolean isSet() {
        return activePolicy != null;
    }

    /**
     * Shortcut accessor for friendly classes, to skip security checks.
     * If active policy was set to <code>null</code>, loads default provider, 
     * so this method never returns <code>null</code>. <br>
     * This method is synchronized with setPolicy()
     */
    static Policy getAccessiblePolicy() {
        Policy current = activePolicy;
        if (current == null) {
            synchronized (Policy.class) {
                // double check in case value has been reassigned 
                // while we've been awaiting monitor
                if (activePolicy == null) {
                    activePolicy = getDefaultProvider();
                }
                return activePolicy;
            }
        }
        return current;
    }

    /**
     * @com.intel.drl.spec_ref
     * Policy assigment is synchronized with default provider loading, to avoid 
     * non-deterministic behavior.
     */
    public static void setPolicy(Policy policy) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SET_POLICY);
        }
        synchronized (Policy.class) {
            activePolicy = policy;
        }
    }
}
