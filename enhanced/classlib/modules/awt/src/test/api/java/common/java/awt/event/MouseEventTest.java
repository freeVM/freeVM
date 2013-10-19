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
 * @author Michael Danilov
 * @version $Revision$
 */
package java.awt.event;

import java.awt.Button;
import java.awt.Point;

import junit.framework.TestCase;

public class MouseEventTest extends TestCase {

    public final void testMouseEventComponentintlongintintintintboolean() {
        Button button = new Button("Button");
        MouseEvent event = new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 1000000000,
                InputEvent.BUTTON2_DOWN_MASK, 100, 200,
                10, true);

        assertEquals(event.getSource(), button);
        assertEquals(event.getID(), MouseEvent.MOUSE_PRESSED);
        assertEquals(event.getButton(), MouseEvent.NOBUTTON);
        assertEquals(event.getClickCount(), 10);
        assertEquals(event.getPoint(), new Point(100, 200));
        assertEquals(event.getX(), 100);
        assertEquals(event.getY(), 200);
        assertTrue(event.isPopupTrigger());
    }

    public final void testMouseEventComponentintlongintintintintbooleanint() {
        Button button = new Button("Button");
        MouseEvent event = new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 1000000000,
                InputEvent.BUTTON2_DOWN_MASK, 100, 200,
                10, true, MouseEvent.BUTTON1);

        assertEquals(event.getSource(), button);
        assertEquals(event.getID(), MouseEvent.MOUSE_PRESSED);
        assertEquals(event.getButton(), MouseEvent.BUTTON1);
        assertEquals(event.getClickCount(), 10);
        assertEquals(event.getPoint(), new Point(100, 200));
        assertEquals(event.getX(), 100);
        assertEquals(event.getY(), 200);
        assertTrue(event.isPopupTrigger());
    }

    public final void testTranslatePoint() {
        Button button = new Button("Button");
        MouseEvent event = new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 1000000000,
                InputEvent.BUTTON2_DOWN_MASK, 100, 200,
                10, true);

        event.translatePoint(10, 10);
        assertEquals(event.getPoint(), new Point(110, 210));
        event.translatePoint(-20, -20);
        assertEquals(event.getPoint(), new Point(90, 190));
    }

    public final void testGetMouseModifiersText() {
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.ALT_DOWN_MASK).indexOf("Alt") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.ALT_GRAPH_DOWN_MASK).indexOf("Alt Graph") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.CTRL_DOWN_MASK).indexOf("Ctrl") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.SHIFT_DOWN_MASK).indexOf("Shift") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.META_DOWN_MASK).indexOf("Meta") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.BUTTON1_DOWN_MASK).indexOf("Button1") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.BUTTON2_DOWN_MASK).indexOf("Button2") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.BUTTON3_DOWN_MASK).indexOf("Button3") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.BUTTON3_MASK).indexOf("Button3") != -1);
        assertTrue(MouseEvent.getMouseModifiersText(InputEvent.BUTTON3_MASK).indexOf("Meta") != -1);
    }

    public final void testParamString() {
        Button button = new Button("Button");
        MouseEvent event = new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 1000000000,
                InputEvent.BUTTON2_DOWN_MASK, 100, 200,
                10, true, MouseEvent.BUTTON1);

        assertEquals(event.paramString(),
                "MOUSE_PRESSED,(100,200),button=1,modifiers=Button2,extModifiers=Button2,clickCount=10");
        event = new MouseEvent(button, MouseEvent.MOUSE_PRESSED + 1024, 1000000000,
                InputEvent.BUTTON2_DOWN_MASK, 100, 200,
                10, true, MouseEvent.BUTTON1);
        assertEquals(event.paramString(),
                "unknown type,(100,200),button=1,modifiers=Button2,extModifiers=Button2,clickCount=10");
    }

}
