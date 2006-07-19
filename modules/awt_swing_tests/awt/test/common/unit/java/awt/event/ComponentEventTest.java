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
 * @author Michael Danilov
 * @version $Revision$
 */
package java.awt.event;

import java.awt.Button;

import junit.framework.TestCase;

public class ComponentEventTest extends TestCase {

    public final void testComponentEvent() {
        Button button = new Button("Button");
        ComponentEvent event = new ComponentEvent(button, ComponentEvent.COMPONENT_MOVED);

        assertEquals(event.getSource(), button);
        assertEquals(event.getID(), ComponentEvent.COMPONENT_MOVED);
        assertEquals(event.getComponent(), button);
    }

    public final void testParamString() {
        Button button = new Button("Button");
        ComponentEvent event = new ComponentEvent(button, ComponentEvent.COMPONENT_MOVED);

        assertEquals(event.paramString(), "COMPONENT_MOVED (0,0 0x0)");
        event = new ComponentEvent(button, ComponentEvent.COMPONENT_MOVED + 1024);
        assertEquals(event.paramString(), "unknown type");
    }

}
