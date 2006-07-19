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
 * @author Vadim L. Bogdanov
 * @version $Revision$
 */
package javax.swing.plaf.metal;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingTestCase;

public class MetalRootPaneUIRTest extends SwingTestCase {
    public MetalRootPaneUIRTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDialogCloseButton() {
        JDialog.setDefaultLookAndFeelDecorated(true);
        JDialog d = new JDialog();
        Component[] comps = d.getLayeredPane().getComponents();
        assertEquals(2, comps.length);

        Component titlePane = null;
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] != d.getContentPane()) {
                titlePane = comps[i];
            }
        }
        assertTrue(titlePane instanceof JComponent);
        assertEquals(1, ((JComponent)titlePane).getComponentCount());
    }
}
