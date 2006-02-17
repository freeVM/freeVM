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
* @author Stepan M. Mishura, Maxim V. Makarov
* @version $Revision$
*/

package javax.security.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;

/** 
 * @com.intel.drl.spec_ref 
 *
 */

public final class PrivateCredentialPermission extends Permission {

    /** 
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = 5284372143517237068L;

    // allowed action
    private static final String READ = "read";

    /** 
     * @com.intel.drl.spec_ref 
     */
    private String credentialClass;

    /** 
     * @com.intel.drl.spec_ref 
     */
    private Set principals;

    /** 
     * @com.intel.drl.spec_ref 
     */
    private boolean testing;
    
    // current offset        
    private transient int offset;
    // owners set
    private transient CredOwner[] set;

    // Initialize a PivateCredentialPermission object and checks that a target name has 
    // a correct format: CredentialClass 1*(PrincipalClass "PrincipalName")
    private void initTargetName(String name) {

        if (name == null) {
            throw new NullPointerException("target name is null");
        }

        // check empty string
        name = name.trim();
        if (name.length() == 0) {
            throw new IllegalArgumentException("target name has a length of 0");
        }

        // get CredentialClass
        int beg = name.indexOf(' ');
        if (beg == -1) {
            throw new IllegalArgumentException(
                    "Target name MUST have the following syntax: "
                            + "CredentialClass 1*(PrincipalClass \"PrincipalName\")");
        }
        this.credentialClass = name.substring(0, beg);

        // get a number of pairs: PrincipalClass "PrincipalName"
        beg++;
        int count = 0;
        int nameLength = name.length();
        for (int i, j = 0; beg < nameLength; beg = j + 2, count++) {
            i = name.indexOf(' ', beg);
            j = name.indexOf('"', i + 2);

            if (i == -1 || j == -1 || name.charAt(i + 1) != '"') {
                throw new IllegalArgumentException(
                        "Target name MUST have the following syntax: "
                                + "CredentialClass 1*(PrincipalClass \"PrincipalName\")");
            }
        }

        // name MUST have one pair at least
        if (count < 1) {
            throw new IllegalArgumentException(
                    "Target name MUST have the following syntax: "
                            + "CredentialClass 1*(PrincipalClass \"PrincipalName\")");
        }

        beg = name.indexOf(' ');
        beg++;

        // populate principal set with instances of CredOwner class
        String principalClass;
        String principalName;

        set = new CredOwner[count];
        for (int index = 0, i, j; index < count; beg = j + 2, index++) {
            i = name.indexOf(' ', beg);
            j = name.indexOf('"', i + 2);

            principalClass = name.substring(beg, i);
            principalName = name.substring(i + 2, j);

            CredOwner element = new CredOwner(principalClass, principalName);
            // check for duplicate elements
            boolean found = false;
            for (int ii = 0; ii < offset; ii++) {
                if (set[ii].equals(element)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                set[offset++] = element;
            }
        }
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        initTargetName(getName());
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public PrivateCredentialPermission(String name, String action) {
        super(name);
        if (READ.equalsIgnoreCase(action)) {
            initTargetName(name);
        } else {
            throw new IllegalArgumentException("Action must be \"read\" only");
        }
    }

    /**
     * Creates a <code>PrivateCredentialPermission</code> from the Credential Class 
     * and Set of Principals
     * 
     * @param credentialClass - credential class name
     * @param principals - principal set
     */
    PrivateCredentialPermission(String credentialClass, Set principals) {

        super(credentialClass);
        this.credentialClass = credentialClass;

        set = new CredOwner[principals.size()];
        for (Iterator i = principals.iterator(); i.hasNext();) {
            Principal p = (Principal) i.next();
            CredOwner element = new CredOwner(p.getClass().getName(), p
                    .getName());
            // check for duplicate elements
            boolean found = false;
            for (int ii = 0; ii < offset; ii++) {
                if (set[ii].equals(element)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                set[offset++] = element;
            }
        }
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public String[][] getPrincipals() {

        String[][] s = new String[offset][2];

        for (int i = 0; i < s.length; i++) {
            s[i][0] = set[i].principalClass;
            s[i][1] = set[i].principalName;
        }
        return s;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public String getActions() {
        return READ;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public String getCredentialClass() {
        return credentialClass;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < offset; i++) {
            hash = hash + set[i].hashCode();
        }
        return getCredentialClass().hashCode() + hash;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        PrivateCredentialPermission that = (PrivateCredentialPermission) obj;

        return this.credentialClass.equals(that.credentialClass) &&
                (this.offset == that.offset) &&
                sameMembers(this.set, that.set, offset);
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public boolean implies(Permission permission) {

        if (permission == null || this.getClass() != permission.getClass()) {
            return false;
        }

        PrivateCredentialPermission that = (PrivateCredentialPermission) permission;

        if (!("*".equals(this.credentialClass) || this.credentialClass
                .equals(that.getCredentialClass()))) {
            return false;
        }

        if (that.offset == 0) {
            return true;
        }

        CredOwner[] thisCo = set;
        CredOwner[] thatCo = that.set;
        int thisPrincipalsSize = offset;
        int thatPrincipalsSize = that.offset;
        for (int i = 0, j; i < thisPrincipalsSize; i++) {
            for (j = 0; j < thatPrincipalsSize; j++) {
                if (thisCo[i].implies(thatCo[j])) {
                    break;
                }
            }
            if (j == thatCo.length) {
                return false;
            }
        }
        return true;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public PermissionCollection newPermissionCollection() {
        return null;
    }
    
    // Returns true if the two arrays have the same length, 
    // and every member of one array is contained in another array 
    private boolean sameMembers(Object[] ar1, Object[] ar2, int length) {
        if (ar1 == null && ar2 == null) {
            return true;
        }
        if (ar1 == null || ar2 == null) {
            return false;
        }
        boolean found;
        for (int i = 0; i < length; i++) {
            found = false;
            for (int j = 0; j < length; j++) {
                if (ar1[i].equals(ar2[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /** 
     * @com.intel.drl.spec_ref
     *
     */
    private static final class CredOwner implements Serializable {

        /** 
         * @com.intel.drl.spec_ref 
         */
        private static final long serialVersionUID = -5607449830436408266L;

        /** 
         * @com.intel.drl.spec_ref 
         */
        String principalClass;

        /** 
         * @com.intel.drl.spec_ref 
         */
        String principalName;
        // whether class name contains wildcards
        private transient boolean isClassWildcard;
        // whether pname contains wildcards
        private transient boolean isPNameWildcard;

        // Creates a new CredOwner with the specified Principal Class and Principal Name 
        CredOwner(String principalClass, String principalName) {

            if ("*".equals(principalClass)) {
                isClassWildcard = true;
            }

            if ("*".equals(principalName)) {
                isPNameWildcard = true;
            }

            if (isClassWildcard && !isPNameWildcard) {
                throw new IllegalArgumentException(
                        "invalid syntax: Principal Class can not be a wildcard (*)"
                                + "value if Principal Name is not a wildcard (*) value");
            }

            this.principalClass = principalClass;
            this.principalName = principalName;
        }

        // Checks if this CredOwner implies the specified Object. 
        boolean implies(Object obj) {
            if (obj == this) {
                return true;
            }

            CredOwner co = (CredOwner) obj;

            if (isClassWildcard
                    || this.principalClass.equals(co.principalClass)) {
                if (isPNameWildcard
                        || this.principalName.equals(co.principalName)) {
                    return true;
                }
            }
            return false;
        }

        // Checks two CredOwner objects for equality. 
        public boolean equals(Object obj) {
            return this.principalClass.equals(((CredOwner) obj).principalClass)
                    && this.principalName
                            .equals(((CredOwner) obj).principalName);
        }

        // Returns the hash code value for this object.
        public int hashCode() {
            return principalClass.hashCode() + principalName.hashCode();
        }
    }
}