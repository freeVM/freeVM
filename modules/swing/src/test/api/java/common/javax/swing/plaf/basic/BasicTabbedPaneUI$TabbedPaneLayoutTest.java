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
 * @author Vadim L. Bogdanov
 * @version $Revision$
 */
package javax.swing.plaf.basic;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingTestCase;


public class BasicTabbedPaneUI$TabbedPaneLayoutTest extends SwingTestCase {

    private JTabbedPane tabbed;
    private BasicTabbedPaneUI ui;
    private BasicTabbedPaneUI.TabbedPaneLayout layout;
    private Dimension emptySize = new Dimension();

    protected void setUp() throws Exception {
        super.setUp();

        tabbed = new JTabbedPane();
        ui = new BasicTabbedPaneUI();
        tabbed.setUI(ui);
        layout = (BasicTabbedPaneUI.TabbedPaneLayout)tabbed.getLayout();

        tabbed.addTab("tab1", new JLabel());
        tabbed.setIconAt(0, new ImageIcon());
        tabbed.setDisabledIconAt(0, new ImageIcon());

        tabbed.addTab("tabtab2", new JLabel());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public BasicTabbedPaneUI$TabbedPaneLayoutTest(String name) {
        super(name);
    }

    public void testCalculateLayoutInfo() {
        // the documentation is empty, results are implementation specific
    }

    public void testPreferredTabAreaHeight() {
        // the documentation is empty, results are implementation specific
    }

    public void testPreferredTabAreaWidth() {
        // the documentation is empty, results are implementation specific
    }

    public void testCalculateTabRects() {
        // the documentation is empty, results are implementation specific
    }

    public void testPadSelectedTab() {
        layout.padSelectedTab(JTabbedPane.TOP, -1);
    }

    public void testRotateTabRuns() {
        // the documentation is empty, results are implementation specific
    }

    public void testNormalizeTabRuns() {
        // the documentation is empty, results are implementation specific
    }

    public void testPadTabRun() {
        // the documentation is empty, results are implementation specific
    }

    public void testAddLayoutComponentStringComponent() {
        // the documentation is empty, results are implementation specific
    }

    public void testCalculateSize() {
        Dimension falseSize = layout.calculateSize(false);
        assertFalse(emptySize == falseSize);
        assertEquals(falseSize, layout.calculateSize(true));
    }

    public void testLayoutContainer() {
        // the documentation is empty, results are implementation specific
    }

    public void testMinimumLayoutSize() {
        Dimension size = layout.minimumLayoutSize(tabbed);
        assertFalse(emptySize == size);
        assertEquals(layout.calculateSize(true), size);
    }

    public void testPreferredLayoutSizeContainer() {
        Dimension size = layout.preferredLayoutSize(tabbed);
        assertFalse(emptySize == size);
        assertEquals(layout.calculateSize(false), size);
    }

    public void testRemoveLayoutComponent() {
        // the documentation is empty, results are implementation specific
    }
}
