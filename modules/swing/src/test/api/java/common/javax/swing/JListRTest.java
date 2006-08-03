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
 * @author Vladimir Ivanov
 * @version $Revision$
 */
package javax.swing;

import junit.framework.TestCase;

public class JListRTest extends TestCase {

    public void testAddSelectionInterval() throws Exception {
        try {
            JList jl = new JList();
            jl.addSelectionInterval(10000000, 1);
        } catch (Exception e) {
            fail("Unexpected exception :" + e);
        }
    }

    public void testSetSelectedIndicies() throws Exception {
        JList l = new JList(new String[] {"", "", "", "", ""});
        l.setSelectedIndices(new int [] {-1, 2, 3, 4, 200, 250});
        assertEquals(2, l.getSelectionModel().getMinSelectionIndex());
        assertEquals(4, l.getSelectionModel().getMaxSelectionIndex());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JListRTest.class);
    }
}
