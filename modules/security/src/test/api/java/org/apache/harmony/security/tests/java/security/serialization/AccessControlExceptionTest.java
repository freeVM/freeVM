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

package org.apache.harmony.security.tests.java.security.serialization;

import java.io.Serializable;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.Permission;

import org.apache.harmony.testframework.serialization.SerializationTest;


/**
 * Serialization tests for <code>AccessControlException</code>
 * 
 */

public class AccessControlExceptionTest extends SerializationTest implements
        SerializationTest.SerializableAssert {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AccessControlExceptionTest.class);
    }

    protected Object[] getData() {
        AllPermission allperms = new AllPermission();
        return new Object[] { new AccessControlException(null),
                new AccessControlException("string1"),
                new AccessControlException(null, null),
                new AccessControlException("string2", allperms) };
    }

    public void assertDeserialized(Serializable oref, Serializable otest) {
        
        // common checks
        THROWABLE_COMPARATOR.assertDeserialized(oref, otest);

        // class specific checks
        AccessControlException ref = (AccessControlException) oref;
        AccessControlException test = (AccessControlException) otest;
        Permission p = ref.getPermission();
        if (p == null) {
            assertNull(test.getPermission());
        } else {
            assertEquals(p, test.getPermission());
        }
    }
}