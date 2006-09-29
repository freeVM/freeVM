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

import junit.framework.TestCase;

public class InputEventTest extends TestCase {

    public final void testInputEvent() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000,
                InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_MASK) {};

        assertEquals(event.getSource(), button);
        assertEquals(event.getID(), 0);
        assertEquals(event.getWhen(), 1000000000);
        assertEquals(event.getModifiers(), InputEvent.ALT_MASK | InputEvent.BUTTON1_MASK);
    }

    public final void testConsuming() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000,
                InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_MASK) {};

        assertFalse(event.isConsumed());
        event.consume();
        assertTrue(event.isConsumed());
    }

    public final void testGetModifiers() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        event.setModifiers(InputEvent.BUTTON1_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON1_MASK);
        event.setModifiers(InputEvent.BUTTON2_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON2_MASK);
        event.setModifiers(InputEvent.BUTTON3_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON3_MASK);
        event.setModifiers(InputEvent.BUTTON1_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON1_MASK);
        event.setModifiers(InputEvent.BUTTON2_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON2_MASK);
        event.setModifiers(InputEvent.BUTTON3_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.BUTTON3_MASK);
        event.setModifiers(InputEvent.ALT_MASK);
        assertEquals(event.getModifiers(), InputEvent.ALT_MASK);
        event.setModifiers(InputEvent.ALT_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.ALT_MASK);
        event.setModifiers(InputEvent.ALT_GRAPH_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.ALT_GRAPH_MASK);
        event.setModifiers(InputEvent.ALT_GRAPH_MASK);
        assertEquals(event.getModifiers(), InputEvent.ALT_GRAPH_MASK);
        event.setModifiers(InputEvent.CTRL_MASK);
        assertEquals(event.getModifiers(), InputEvent.CTRL_MASK);
        event.setModifiers(InputEvent.CTRL_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.CTRL_MASK);
        event.setModifiers(InputEvent.SHIFT_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.SHIFT_MASK);
        event.setModifiers(InputEvent.SHIFT_MASK);
        assertEquals(event.getModifiers(), InputEvent.SHIFT_MASK);
        event.setModifiers(InputEvent.META_MASK);
        assertEquals(event.getModifiers(), InputEvent.META_MASK);
        event.setModifiers(InputEvent.META_DOWN_MASK);
        assertEquals(event.getModifiers(), InputEvent.META_MASK);
    }

    public final void testGetModifiersEx() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        event.setModifiers(InputEvent.BUTTON1_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON1_DOWN_MASK);
        event.setModifiers(InputEvent.BUTTON2_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON2_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        event.setModifiers(InputEvent.BUTTON3_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON3_DOWN_MASK | InputEvent.META_DOWN_MASK);
        event.setModifiers(InputEvent.BUTTON1_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON1_DOWN_MASK);
        event.setModifiers(InputEvent.BUTTON2_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON2_DOWN_MASK);
        event.setModifiers(InputEvent.BUTTON3_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON3_DOWN_MASK);
        event.setModifiers(InputEvent.ALT_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON2_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        event.setModifiers(InputEvent.ALT_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.ALT_DOWN_MASK);
        event.setModifiers(InputEvent.ALT_GRAPH_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.ALT_GRAPH_DOWN_MASK);
        event.setModifiers(InputEvent.ALT_GRAPH_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.ALT_GRAPH_DOWN_MASK);
        event.setModifiers(InputEvent.CTRL_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.CTRL_DOWN_MASK);
        event.setModifiers(InputEvent.CTRL_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.CTRL_DOWN_MASK);
        event.setModifiers(InputEvent.SHIFT_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.SHIFT_DOWN_MASK);
        event.setModifiers(InputEvent.SHIFT_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.SHIFT_DOWN_MASK);
        event.setModifiers(InputEvent.META_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.BUTTON3_DOWN_MASK | InputEvent.META_DOWN_MASK);
        event.setModifiers(InputEvent.META_DOWN_MASK);
        assertEquals(event.getModifiersEx(), InputEvent.META_DOWN_MASK);
    }

    public final void testIsAltDown() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        assertFalse(event.isAltDown());
        event.setModifiers(InputEvent.ALT_DOWN_MASK);
        assertTrue(event.isAltDown());
    }

    public final void testIsAltGraphDown() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        assertFalse(event.isAltGraphDown());
        event.setModifiers(InputEvent.ALT_GRAPH_DOWN_MASK);
        assertTrue(event.isAltGraphDown());
    }

    public final void testIsControlDown() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        assertFalse(event.isControlDown());
        event.setModifiers(InputEvent.CTRL_DOWN_MASK);
        assertTrue(event.isControlDown());
    }

    public final void testIsMetaDown() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        assertFalse(event.isMetaDown());
        event.setModifiers(InputEvent.META_DOWN_MASK);
        assertTrue(event.isMetaDown());
    }

    public final void testIsShiftDown() {
        Button button = new Button("Button");
        InputEvent event = new InputEvent(button, 0, 1000000000, 0) {};

        assertFalse(event.isShiftDown());
        event.setModifiers(InputEvent.SHIFT_DOWN_MASK);
        assertTrue(event.isShiftDown());
    }

    public final void testGetModifiersExText() {
        assertTrue(InputEvent.getModifiersExText(InputEvent.ALT_DOWN_MASK).indexOf("Alt") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.ALT_GRAPH_DOWN_MASK).indexOf("Alt Graph") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.CTRL_DOWN_MASK).indexOf("Ctrl") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.SHIFT_DOWN_MASK).indexOf("Shift") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.META_DOWN_MASK).indexOf("Meta") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.BUTTON1_DOWN_MASK).indexOf("Button1") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.BUTTON2_DOWN_MASK).indexOf("Button2") != -1);
        assertTrue(InputEvent.getModifiersExText(InputEvent.BUTTON3_DOWN_MASK).indexOf("Button3") != -1);
    }

}
