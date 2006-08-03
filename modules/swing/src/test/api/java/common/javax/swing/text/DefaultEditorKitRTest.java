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
package javax.swing.text;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.BasicSwingTestCase;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class DefaultEditorKitRTest extends BasicSwingTestCase {

    protected DefaultEditorKit kit = null;

    protected JFrame frame;

    protected void setUp() throws Exception {
        super.setUp();
        kit = new DefaultEditorKit();
    }

    protected void tearDown() throws Exception {
        kit = null;
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        super.tearDown();
    }

    protected Action getAction(final String actionName) {
        Action[] actions = kit.getActions();
        for (int i = 0; i < actions.length; i++) {
            if (actionName.equals(actions[i].getValue(Action.NAME))) {
                return actions[i];
            }
        }

        return null;
    }

    protected void performAction(final Object source, final Action action,
                                 final String command) throws InterruptedException, InvocationTargetException {

        final ActionEvent actionEvent = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, command);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                action.actionPerformed(actionEvent);
            }
        });
    }

    protected JTextArea getInitedComponent(final int startPos,
                                           final int endPos, final String text) throws InterruptedException, InvocationTargetException {
        JTextArea c = new JTextArea();
        return initComponent(c, startPos, endPos, text);
    }

    private JTextArea initComponent(final JTextArea c, final int startPos,
                                    final int endPos, final String text) throws InterruptedException, InvocationTargetException {

        if (frame != null) {
            frame.dispose();
        }
        c.setText(text);
        frame = new JFrame();
        JScrollPane scroll = new JScrollPane(c);
        ((JViewport) c.getParent()).setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        int strHeight = c.getFontMetrics(c.getFont()).getHeight();
        scroll.setPreferredSize(new Dimension(300, strHeight*5));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scroll);
        frame.pack();
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                c.setCaretPosition(startPos);
                if (endPos >= 0) {
                    c.moveCaretPosition(endPos);
                }
            }
        });
        return c;
    }

    public void testInsertContentActionPerformed() throws Exception {
        Action action = getAction(DefaultEditorKit.insertContentAction);
        JTextArea c = getInitedComponent(2, 7, "0123456789");
        performAction(new JTextArea(), action, "command\ncontent");
        assertEquals("resulted string", "0123456789", c.getText());

        c = getInitedComponent(2, 7, "0123456789");
        c.setEditable(false);
        performAction(c, action, "command\ncontent");
        assertEquals("resulted string", "0123456789", c.getText());

        c = getInitedComponent(2, 7, "0123456789");
        performAction(c, action, null);
        assertEquals("resulted string", "0123456789", c.getText());
    }

    public void testInsertContentActionPerformed_NullEvent() throws Exception {
        final TextAction action = (TextAction)getAction(DefaultEditorKit.insertContentAction);
        final JTextArea c = getInitedComponent(2, 7, "0123456789");
        frame.setVisible(true);
        final Marker thrown = new Marker();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    action.actionPerformed(null);
                    thrown.setOccurred();
                } catch (NullPointerException e) {
                }
            }
        });
        assertTrue(thrown.isOccurred());
    }

    public void testRead() throws Exception {
        String str1 = "Windows line-end\r\nUnix-style\nMacOS\rUnknown\n\r";
        String str2 = "Windows line-end\nUnix-style\nMacOS\nUnknown\n\n";
        InputStream reader = new ByteArrayInputStream(str1.getBytes());

        Document doc = new PlainDocument();
        kit.read(reader, doc, 0);
        assertEquals(str2, doc.getText(0, doc.getLength()));
    }
}
