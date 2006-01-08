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
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package java.security;

/**
 * @com.intel.drl.spec_ref 
 */

public class AccessControlException extends SecurityException {

    private static final long serialVersionUID = 5138225684096988535L;

    /**
     * @com.intel.drl.spec_ref 
     */
    private Permission perm; // Named as demanded by Serialized Form.

    /**
     * @com.intel.drl.spec_ref 
     */
    public AccessControlException(String message) {
        super(message);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public AccessControlException(String message, Permission perm) {
        super(message);
        this.perm = perm;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public Permission getPermission() {
        return perm;
    }
}
