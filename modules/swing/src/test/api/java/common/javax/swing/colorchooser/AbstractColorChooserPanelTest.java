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
 * @author Dennis Ushakov
 * @version $Revision$
 */
package javax.swing.colorchooser;

import javax.swing.BasicSwingTestCase;
import javax.swing.Icon;
import javax.swing.JColorChooser;

public class AbstractColorChooserPanelTest extends BasicSwingTestCase {
    AbstractColorChooserPanel panel;

    public void setUp() throws Exception {
        panel = new AbstractColorChooserPanel() {
            public String getDisplayName() {
                return "";
            }
            public Icon getSmallDisplayIcon() {
                return null;
            }
            public Icon getLargeDisplayIcon() {
                return null;
            }
            public void updateChooser() {}
            protected void buildChooser() {}
        };
    }

    public void tearDown() throws Exception {
        panel = null;
    }

    public void testAbstractColorChooserPanel() {
        assertEquals(0, panel.getMnemonic());
        assertEquals(-1, panel.getDisplayedMnemonicIndex());
    }

    public void testInstallUninstallChooserPanel() {
        testExceptionalCase(new NullPointerCase() {
            public void exceptionalAction() throws Exception {
                panel.getColorSelectionModel();
            }
        });
        JColorChooser chooser = new JColorChooser();
        int oldListenersCount = ((DefaultColorSelectionModel)chooser.getSelectionModel()).getChangeListeners().length;
        panel.installChooserPanel(chooser);
        assertSame(chooser.getColor(), panel.getColorFromModel());
        assertEquals(oldListenersCount + 1, ((DefaultColorSelectionModel)chooser.getSelectionModel()).getChangeListeners().length);

        panel.uninstallChooserPanel(chooser);
        assertEquals(oldListenersCount, ((DefaultColorSelectionModel)chooser.getSelectionModel()).getChangeListeners().length);
    }
}
