/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.jndi.tests.javax.naming.ldap;

import javax.naming.ldap.SortKey;
import junit.framework.TestCase;

public class SortKeyTest extends TestCase {

    public void testAll() {
        SortKey sk = new SortKey("attributeId");
        assertEquals("attributeId", sk.getAttributeID());
        assertTrue(sk.isAscending());
        assertNull(sk.getMatchingRuleID());
        
        try {
            new SortKey(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
            //expected
        }
        
        
        
        sk = new SortKey("attributeId", false, "matchingRuleId");
        assertEquals("attributeId", sk.getAttributeID());
        assertFalse(sk.isAscending());
        assertEquals("matchingRuleId", sk.getMatchingRuleID());
        
        try {
            new SortKey(null, true, null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
            //expected
        }
        
        sk = new SortKey("attributeId", true, null);
        assertEquals("attributeId", sk.getAttributeID());
        assertTrue(sk.isAscending());
        assertNull(sk.getMatchingRuleID());
    }
}
