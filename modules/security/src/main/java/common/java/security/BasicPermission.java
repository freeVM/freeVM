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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @com.intel.drl.spec_ref
 */
public abstract class BasicPermission extends Permission implements
    Serializable {

    private static final long serialVersionUID = 6279438298436773498L;

    /**
     * @com.intel.drl.spec_ref.
     */
    public BasicPermission(String name) {
        super(name);
        checkName(name);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BasicPermission(String name, String action) {
        super(name);
        checkName(name);
    }

    /**
     * Checks name parameter
     */ 
    private final void checkName(String name) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("name must not be empty");
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj.getClass() == this.getClass()) {
            return this.getName().equals(((Permission)obj).getName());
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String getActions() {
        return "";
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean implies(Permission permission) {
        if (permission != null && permission.getClass() == this.getClass()) {
            return nameImplies(getName(), permission.getName());
        }
        return false;
    }

    /**
     * Checks if <code>thisName</code> implies <code>thatName</code>,
     * accordingly to hierarchical property naming convention.
     * It is assumed that names cannot be null or empty.
     */
    static boolean nameImplies(String thisName, String thatName) {
        if (thisName == thatName) {
            return true;
        }
        int end = thisName.length();
        if (end > thatName.length()) {
            return false;
        }
        if (thisName.charAt(--end) == '*'
            && (end == 0 || thisName.charAt(end - 1) == '.')) {
            //wildcard found
            end--;
        } else if (end != (thatName.length()-1)) {
            //names are not equal
            return false;
        }
        for (int i = end; i >= 0; i--) {
            if (thisName.charAt(i) != thatName.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PermissionCollection newPermissionCollection() {
        return new BasicPermissionCollection();
    }

    /**
     * Checks name after default deserialization.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();
        checkName(this.getName());
    }
}

/**
 * Specific PermissionCollection for storing BasicPermissions of arbitrary type.
 * 
 */

final class BasicPermissionCollection extends PermissionCollection {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = 739301742472979399L;

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("all_allowed", Boolean.TYPE),
        new ObjectStreamField("permissions", Hashtable.class),
        new ObjectStreamField("permClass", Class.class), };

    //should be final, but because of writeObject() cannot be
    private transient Map items = new HashMap();

    // tru if this Collection contains a BasicPermission with '*' as its permission name
    private transient boolean allEnabled; // = false;

    /**
     * @com.intel.drl.spec_ref
     */
    private Class permClass;

    /**
     * Adds a permission to the collection. The first added permission must be a
     * subclass of BasicPermission, next permissions must be of the same class
     * as the first one.
     * 
     * @see java.security.PermissionCollection#add(java.security.Permission)
     */
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new SecurityException("collection is read-only");
        }
        if (permission == null) {
            throw new IllegalArgumentException("invalid permission: null");
        }

        Class inClass = permission.getClass();
        if (permClass != null) {
            if (permClass != inClass) {
                throw new IllegalArgumentException("invalid permission: "
                    + permission);
            }
        } else if( !(permission instanceof BasicPermission)) {
            throw new IllegalArgumentException("invalid permission: "
                + permission);
        } else { 
            // this is the first element provided that another thread did not add
            synchronized (items) {
                if (permClass != null && inClass != permClass) {
                    throw new IllegalArgumentException("invalid permission: "
                        + permission);
                }
                permClass = inClass;
            }
        }

        String name = permission.getName();
        items.put(name, permission);
        allEnabled = allEnabled || (name.length() == 1 && '*' == name.charAt(0));
    }

    /**
     * Returns enumeration of contained elements.
     */
    public Enumeration elements() {
        return Collections.enumeration(items.values());
    }

    /**
     * Checks if the particular permission is implied by this collection.
     * 
     * @see java.security.BasicPermission
     * @see java.security.PermissionCollection#implies(java.security.Permission)
     */
    public boolean implies(Permission permission) {
        if (permission == null || permission.getClass() != permClass) {
            return false;
        }
        if (allEnabled) {
            return true;
        }
        String checkName = permission.getName();
        //first check direct coincidence
        if (items.containsKey(checkName)) {
            return true;
        }
        //now check if there are suitable wildcards
        //suppose we have "a.b.c", let's check "a.b.*" and "a.*" 
        char[] name = checkName.toCharArray();
        //I presume that "a.b.*" does not imply "a.b." 
        //so the dot at end is ignored 
        int pos = name.length - 2; 
        for (; pos >= 0; pos--) {
            if (name[pos] == '.') {
                break;
            }
        }
        while (pos >= 0) {
            name[pos + 1] = '*'; 
            if (items.containsKey(new String(name, 0, pos + 2))) {
                return true;
            }
            for (--pos; pos >= 0; pos--) {
                if (name[pos] == '.') {
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Expected format is the following:
     * <dl>
     * <dt>boolean all_allowed
     * <dd>This is set to true if this BasicPermissionCollection contains a
     * BasicPermission with '*' as its permission name.
     * <dt>Class&lt;T&gt; permClass
     * <dd>The class to which all BasicPermissions in this
     * BasicPermissionCollection belongs.
     * <dt>Hashtable&lt;K,V&gt; permissions
     * <dd>The BasicPermissions in this BasicPermissionCollection. All
     * BasicPermissions in the collection must belong to the same class. The
     * Hashtable is indexed by the BasicPermission name; the value of the
     * Hashtable entry is the permission.
     * </dl>
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("all_allowed", allEnabled);
        fields.put("permissions", new Hashtable(items));
        fields.put("permClass", permClass);
        out.writeFields();
    }

    /**
     * Reads the object from stream and checks its consistency: all contained
     * permissions must be of the same subclass of BasicPermission.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();

        items = new HashMap();
        synchronized (items) {
            permClass = (Class)fields.get("permClass", null);
            items.putAll((Map)fields.get("permissions", new Hashtable()));
            for (Iterator iter = items.values().iterator(); iter.hasNext();) {
                if (iter.next().getClass() != permClass) {
                    throw new InvalidObjectException(
                        "Inconsistent types of contained permissions");
                }
            }
            allEnabled = fields.get("all_allowed", false);
            if (allEnabled && !items.containsKey("*")) {
                throw new InvalidObjectException(
                    "Invalid state of wildcard flag");
            }
        }
    }
}