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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.awt.Menu.AccessibleAWTMenu;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import junit.framework.TestCase;

public class AccessibleAWTMenuTest extends TestCase {
    Menu menu;
    AccessibleContext ac;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AccessibleAWTMenuTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        menu = new Menu();
        ac = menu.getAccessibleContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'java.awt.Menu.AccessibleAWTMenu.getAccessibleRole()'
     */
    public void testGetAccessibleRole() {
        assertSame(AccessibleRole.MENU, ac.getAccessibleRole());

    }

    /*
     * Test method for 'java.awt.Menu.AccessibleAWTMenu.AccessibleAWTMenu(Menu)'
     */
    public void testAccessibleAWTMenu() {
        assertTrue(ac instanceof AccessibleAWTMenu);

    }

}
