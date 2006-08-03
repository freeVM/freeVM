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
 * @author Alexander T. Simbirtsev
 * @version $Revision$
 */
package javax.swing;

import java.awt.Container;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import junit.framework.TestCase;

public class JFormattedTextField_CommitActionRTest extends TestCase {

    private JFrame frame;
    private JPanel c;
    private JTextField textField;

    protected void setUp() throws Exception {
        super.setUp();

        frame = new JFrame();
    }

    protected void tearDown() throws Exception {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        super.tearDown();
    }

    public void testActionPerformed() throws Exception {
        c = new JPanel();
        Container parent = new Panel();
        textField = new JFormattedTextField("   ");
        frame.getContentPane().add(c);
        c.add(parent);
        parent.add(textField);
        frame.pack();
        frame.setVisible(true);

        final Rectangle flag = new Rectangle();
        final ActionListener parentListener = new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                flag.x = 100;
            }
        };
        final ActionListener textFieldListener = new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                flag.y = 100;
            }
        };
        c.registerKeyboardAction(parentListener,
                                 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        SwingWaitTestCase.requestFocusInWindowForComponent(textField);

        textField.dispatchEvent(new KeyEvent(textField,
                                             KeyEvent.KEY_PRESSED,
                                             0, 0,
                                             KeyEvent.VK_ENTER, (char)13));
        assertEquals("parent's action hasn't been fired", 0, flag.x);
        assertEquals("textFields action hasn't been fired", 0, flag.y);

        textField.addActionListener(textFieldListener);
        textField.dispatchEvent(new KeyEvent(textField,
                                             KeyEvent.KEY_PRESSED,
                                             0, 0,
                                             KeyEvent.VK_ENTER, (char)13));
        assertEquals("parent's action hasn't been fired", 0, flag.x);
        assertEquals("textFields action has been fired", 100, flag.y);
    }
}
