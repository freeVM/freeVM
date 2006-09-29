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
/**
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.awt.PopupMenu.AccessibleAWTPopupMenu;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import junit.framework.TestCase;

public class AccessibleAWTPopupMenuTest extends TestCase {
    PopupMenu popup;
    AccessibleContext ac;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AccessibleAWTPopupMenuTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        popup = new PopupMenu();
        ac = popup.getAccessibleContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'java.awt.PopupMenu.AccessibleAWTPopupMenu.getAccessibleRole()'
     */
    public void testGetAccessibleRole() {
        assertSame(AccessibleRole.POPUP_MENU, ac.getAccessibleRole());
    }

    /*
     * Test method for 'java.awt.PopupMenu.AccessibleAWTPopupMenu.AccessibleAWTPopupMenu(PopupMenu)'
     */
    public void testAccessibleAWTPopupMenu() {
        assertTrue(ac instanceof AccessibleAWTPopupMenu);
    }

}
