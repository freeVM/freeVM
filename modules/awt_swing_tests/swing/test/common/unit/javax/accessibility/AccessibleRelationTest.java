/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Dennis Ushakov
 * @version $Revision$
 */

package javax.accessibility;

import javax.swing.BasicSwingTestCase;

public class AccessibleRelationTest extends BasicSwingTestCase {
    private AccessibleRelation relation;

    public void setUp() {
        relation = new AccessibleRelation(AccessibleRelation.LABEL_FOR);
    }

    public void tearDown() {
        relation = null;
    }

    public void testGetKey() {
        assertEquals(relation.key, relation.getKey());
    }

    public void testAccessibleRelation() {
        assertEquals(0, relation.getTarget().length);
    }

    public void testSetGetTarget() {
        StringBuffer target = new StringBuffer("text");
        relation.setTarget(target);
        assertEquals(1, relation.getTarget().length);
        assertSame(target, relation.getTarget()[0]);

        StringBuffer[] targets = new StringBuffer[]{target, target};
        relation.setTarget(targets);
        assertEquals(2, relation.getTarget().length);
        assertNotSame(targets, relation.getTarget());

        relation.setTarget((Object[])null);
        assertNotNull(relation.getTarget());
        assertEquals(0, relation.getTarget().length);

        relation.setTarget((Object)null);
        assertNotNull(relation.getTarget());
        assertEquals(1, relation.getTarget().length);
        assertNull(relation.getTarget()[0]);
    }
}

