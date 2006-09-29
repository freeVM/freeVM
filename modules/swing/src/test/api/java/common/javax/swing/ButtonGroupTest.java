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
 * @author Alexander T. Simbirtsev
 * @version $Revision$
 * Created on 15.04.2005

 */
package javax.swing;

import java.util.Enumeration;

import junit.framework.TestCase;

public class ButtonGroupTest extends TestCase {

    protected ButtonGroup group;
    protected AbstractButton[] buttons;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        group = new ButtonGroup();
        buttons = new AbstractButton[5];
        buttons[0] = new JCheckBox();
        buttons[1] = new JCheckBox();
        buttons[2] = new JRadioButton();
        buttons[3] = new JRadioButton();
        buttons[4] = new JButton();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testButtonGroup() {
        assertEquals("initial number of buttons", 0, group.getButtonCount());
        assertEquals("initial selection", null, group.getSelection());
        assertTrue("initial enumeration isn't null", group.getElements() != null);
        assertFalse("initial enumeration is empty", group.getElements().hasMoreElements());
    }

    public void testAdd() {
        buttons[0].setSelected(true);
        final DefaultButtonModel model = (DefaultButtonModel)buttons[0].getModel();
        assertNull(model.getGroup());
        group.add(buttons[0]);
        assertSame(group, model.getGroup());
        assertEquals("number of buttons", 1, group.getButtonCount());
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertEquals("selected model", true, buttons[0].getModel().isSelected());

        group.add(buttons[0]);
        assertEquals("number of buttons", 2, group.getButtonCount());
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertEquals("selected model", true, buttons[0].getModel().isSelected());

        buttons[3].setSelected(true);
        group.add(buttons[3]);
        assertEquals("number of buttons", 3, group.getButtonCount());
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertEquals("selected model", true, buttons[0].getModel().isSelected());
        assertEquals("unselected model", false, buttons[3].getModel().isSelected());

        buttons[4].setSelected(true);
        group.add(buttons[4]);
        assertEquals("number of buttons", 4, group.getButtonCount());
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertEquals("selected model", true, buttons[0].getModel().isSelected());
        assertEquals("unselected model", false, buttons[4].getModel().isSelected());
        assertEquals("unselected model", false, buttons[3].getModel().isSelected());
        
        group.add(null);
    }

    public void testRemove() {
        group.add(buttons[0]);
        group.add(buttons[0]);
        group.add(buttons[3]);
        group.add(buttons[4]);

        final DefaultButtonModel model = (DefaultButtonModel)buttons[0].getModel();
        assertSame(group, model.getGroup());
        assertEquals("number of buttons", 4, group.getButtonCount());

        buttons[0].setSelected(true);
        assertEquals("selection", buttons[0].getModel(), group.getSelection());

        group.remove(buttons[0]);
        assertEquals("number of buttons", 3, group.getButtonCount());
        assertEquals("selection", null, group.getSelection());

        buttons[0].setSelected(true);
        assertEquals("selection", null, group.getSelection());

        group.remove(buttons[0]);
        assertEquals("number of buttons", 2, group.getButtonCount());
        assertNull(model.getGroup());

        group.remove(buttons[2]);
        assertEquals("number of buttons", 2, group.getButtonCount());

        group.remove(buttons[3]);
        assertEquals("selection", null, group.getSelection());
        group.remove(buttons[4]);
        assertEquals("number of buttons", 0, group.getButtonCount());
        
        group.remove(null);
    }

    /**
     * method is being tested by other testcases
     */
    public void testGetButtonCount() {
    }

    public void testGetElements() {
        group.add(buttons[0]);
        group.add(buttons[0]);
        group.add(buttons[3]);
        group.add(buttons[4]);

        Enumeration i = group.getElements();
        assertEquals("button", buttons[0], i.nextElement());
        assertEquals("button", buttons[0], i.nextElement());
        assertEquals("button", buttons[3], i.nextElement());
        assertEquals("button", buttons[4], i.nextElement());
        assertFalse("no more buttons", i.hasMoreElements());
    }

    public void testGetSelection() {
        group.add(buttons[0]);
        group.add(buttons[1]);
        group.add(buttons[3]);
        group.add(buttons[4]);
        assertEquals("initial selection", null, group.getSelection());

        group.setSelected(buttons[0].getModel(), false);
        assertEquals("selection", null, group.getSelection());

        group.setSelected(buttons[4].getModel(), true);
        assertEquals("selection", buttons[4].getModel(), group.getSelection());
        assertFalse("unselected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertTrue("selected model", buttons[4].getModel().isSelected());

        group.setSelected(buttons[0].getModel(), true);
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertTrue("selected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        group.setSelected(buttons[0].getModel(), false);
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertTrue("selected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        buttons[0].setSelected(false);
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertTrue("selected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        group.setSelected(buttons[3].getModel(), true);
        assertEquals("selection", buttons[3].getModel(), group.getSelection());
        assertFalse("unselected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertTrue("selected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        group.setSelected(buttons[2].getModel(), true);
        assertEquals("selection", buttons[2].getModel(), group.getSelection());
        assertFalse("unselected model", buttons[0].getModel().isSelected());
        assertFalse("unselected model", buttons[1].getModel().isSelected());
        assertTrue("selected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        buttons[1].setSelected(true);
        assertEquals("selection", buttons[1].getModel(), group.getSelection());
        assertFalse("unselected model", buttons[0].getModel().isSelected());
        assertTrue("selected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertFalse("unselected model", buttons[4].getModel().isSelected());

        buttons[4].setSelected(true);
        assertEquals("selection", buttons[1].getModel(), group.getSelection());
        assertFalse("unselected model", buttons[0].getModel().isSelected());
        assertTrue("selected model", buttons[1].getModel().isSelected());
        assertFalse("unselected model", buttons[2].getModel().isSelected());
        assertFalse("unselected model", buttons[3].getModel().isSelected());
        assertTrue("unselected model", buttons[4].getModel().isSelected());
    }

    public void testIsSelected() {
        buttons[0].setSelected(true);
        group.add(buttons[0]);
        buttons[3].setSelected(true);
        group.add(buttons[3]);
        assertEquals("selected model", true, group.isSelected(buttons[0].getModel()));
        assertEquals("unselected model", false, group.isSelected(buttons[3].getModel()));
        assertEquals("selection", buttons[0].getModel(), group.getSelection());
        assertEquals("selected model", true, buttons[0].getModel().isSelected());
        assertEquals("unselected model", false, buttons[3].getModel().isSelected());

        buttons[1].setSelected(true);
        buttons[2].setSelected(true);
        assertEquals("selected model", false, group.isSelected(buttons[1].getModel()));
        assertEquals("selected model", false, group.isSelected(buttons[2].getModel()));

    }

    public void testSetSelected() {
        buttons[0].setSelected(true);
        group.add(buttons[0]);
        group.setSelected(null, false);
        group.setSelected(null, true);
    }

}
