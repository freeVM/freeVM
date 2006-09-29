/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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

import junit.framework.TestCase;

public class WindowRTest extends TestCase {

    public final void testDispose() {
        Frame frm = new Frame("MyApp");
        Window wnd = new Window(frm);
        wnd.setVisible(true);
        wnd.dispose();
        assertFalse(wnd.isValid());
        frm.dispose();
    }

    public final void testHide() {
        final Frame f = new Frame();
        f.setSize(100, 100);
        f.setVisible(true);
        assertTrue(f.isVisible());
        Window connected = new Window(f);
        connected.setBackground(Color.RED);
        connected.setSize(100, 100);
        connected.setLocation(200, 200);
        connected.setVisible(true);
        assertTrue(connected.isVisible());
        f.hide();
        assertFalse(connected.isVisible());
        assertFalse(f.isVisible());

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(WindowRTest.class);
    }
}
