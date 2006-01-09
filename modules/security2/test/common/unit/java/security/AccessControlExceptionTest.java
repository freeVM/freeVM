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

import com.openintel.drl.security.test.PerformanceTest;

/**
 * Unit test for AccessControlException.
 */

public class AccessControlExceptionTest extends PerformanceTest {

    /**
     * Entry point for standalone run.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AccessControlExceptionTest.class);
    }

    /**
     * Tests AccessControlException(String)
     */
    public void testAccessControlExceptionString() {
        new AccessControlException(null);
        new AccessControlException("Failure");
    }

    /**
     * Tests AccessControlException(String, Permission)
     */
    public void testAccessControlExceptionStringPermission() {
        Permission perm = new AllPermission();
        AccessControlException ex = new AccessControlException("001", perm);
    }

    /**
     * 
     * Tests AccessControlException.getPermission()
     */
    public void testGetPermission() {
        Permission perm = new UnresolvedPermission("unresolvedType",
                "unresolvedName", "unresolvedActions", null);
        AccessControlException ex = new AccessControlException("001", perm);
        assertSame(ex.getPermission(), perm);
    }
}