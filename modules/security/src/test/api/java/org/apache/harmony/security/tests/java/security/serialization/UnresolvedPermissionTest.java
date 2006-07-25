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

package org.apache.harmony.security.tests.java.security.serialization;

import java.security.UnresolvedPermission;
import java.security.cert.Certificate;

import org.apache.harmony.testframework.serialization.SerializationTest;



/**
 * Serialization tests for <code>UnresolvedPermission</code>
 * 
 */

public class UnresolvedPermissionTest extends SerializationTest {

    /**
     * @see com.intel.drl.test.SerializationTest#getData()
     */
    protected Object[] getData() {
        // test with real certificates ?
        return new Object[] {new UnresolvedPermission("sjdvb.asef", "", "", null),
                new UnresolvedPermission("&^%$m,3298746", null, null, new Certificate[0])};
    }
}
