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
package java.awt;


import java.util.Set;

import junit.framework.TestCase;


public class ContainerRTest extends TestCase {


    public final void testSetFocusTraversalKeys() {
        try {
        Button btn = new Button();
        btn.setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, (Set<AWTKeyStroke>)new Container().getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        fail("IllegalArgumentException expected");
    }catch (IllegalArgumentException e) {
    }
    }

    public final void testAddNotify() {
        Container c1 = new Container(){};
        Container c2 = new Container(){};
        assertFalse(c1.isDisplayable());
        assertFalse(c2.isDisplayable());

        c1.add(c2);
        c1.addNotify();

        assertTrue(c1.isDisplayable());
        assertTrue(c2.isDisplayable());
    }

//    public final void testRemoveComponent() {
//        Button b = new Button();
//        boolean npeThrown = false;
//        Container c = new Container();
//        c.remove(b); // no exception is thrown
//        try {
//            c.remove(b = null);
//        } catch (NullPointerException npe) {
//            npeThrown = true;
//        }        
//        assertTrue("remove(null) throws NPE", npeThrown);
//    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ContainerRTest.class);
    }
}
