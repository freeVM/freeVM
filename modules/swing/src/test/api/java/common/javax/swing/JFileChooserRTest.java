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
 * @author Anton Avtamonov
 * @version $Revision$
 */
package javax.swing;

import java.io.File;

import javax.swing.BasicSwingTestCase.PropertyChangeController;
import javax.swing.filechooser.FileFilter;

public class JFileChooserRTest extends BasicSwingTestCase {
    private JFileChooser chooser;

    public JFileChooserRTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        chooser = new JFileChooser();
    }

    protected void tearDown() throws Exception {
        chooser = null;
    }

    public void testAddChoosableFileFilter() throws Exception {
        FileFilter ff = new FileFilter() {
            public boolean accept(File file) {
                return false;
            }

            public String getDescription() {
                return "any";
            }
        };

        assertEquals(1, chooser.getChoosableFileFilters().length);

        chooser.addChoosableFileFilter(ff);
        assertEquals(2, chooser.getChoosableFileFilters().length);

        chooser.addChoosableFileFilter(ff);
        assertEquals(2, chooser.getChoosableFileFilters().length);

        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        assertEquals(2, chooser.getChoosableFileFilters().length);

        assertSame(chooser.getAcceptAllFileFilter(), chooser.getChoosableFileFilters()[0]);
        assertSame(ff, chooser.getChoosableFileFilters()[1]);
    }

    public void testGetSetSelectedFile() throws Exception {
        propertyChangeController = new PropertyChangeController();
        chooser.addPropertyChangeListener(propertyChangeController);

        assertNull(chooser.getSelectedFile());
        File selectedFile = new File(new File(".").getCanonicalPath() + File.separator + "testFile");
        selectedFile.deleteOnExit();
        chooser.setSelectedFile(selectedFile);

        assertEquals(selectedFile, chooser.getSelectedFile());
        assertEquals(0, chooser.getSelectedFiles().length);
        assertEquals(selectedFile.getAbsoluteFile().getParentFile().getCanonicalFile(),
                     chooser.getCurrentDirectory().getCanonicalFile());

        selectedFile.mkdir();
        chooser.setSelectedFile(selectedFile);
        assertEquals(selectedFile, chooser.getSelectedFile());
        assertTrue(propertyChangeController.isChanged("SelectedFileChangedProperty"));
    }
}
