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
 * @author Sergey Burlak
 */
package javax.swing.plaf.metal;

import javax.swing.Icon;
import javax.swing.SwingTestCase;
import javax.swing.plaf.UIResource;
import tests.support.Support_Excludes;

public class MetalIconFactoryTest extends SwingTestCase {
    public void testGetCheckBoxIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getCheckBoxIcon();
        checkIcon(icon, 13, 13);
        assertTrue(MetalIconFactory.getCheckBoxIcon() == MetalIconFactory.getCheckBoxIcon());
    }

    public void testGetCheckBoxMenuItemIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getCheckBoxMenuItemIcon();
        checkIcon(icon, 10, 10);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getCheckBoxMenuItemIcon() == MetalIconFactory
                .getCheckBoxMenuItemIcon());
    }

    public void testGetFileChooserDetailViewIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getFileChooserDetailViewIcon();
        checkIcon(icon, 18, 18);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getFileChooserDetailViewIcon() == MetalIconFactory
                .getFileChooserDetailViewIcon());
    }

    public void testGetFileChooserHomeFolderIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getFileChooserHomeFolderIcon();
        checkIcon(icon, 18, 18);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getFileChooserHomeFolderIcon() == MetalIconFactory
                .getFileChooserHomeFolderIcon());
    }

    public void testGetFileChooserListViewIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getFileChooserListViewIcon();
        checkIcon(icon, 18, 18);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getFileChooserListViewIcon() == MetalIconFactory
                .getFileChooserListViewIcon());
    }

    public void testGetFileChooserNewFolderIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getFileChooserNewFolderIcon();
        checkIcon(icon, 18, 18);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getFileChooserNewFolderIcon() == MetalIconFactory
                .getFileChooserNewFolderIcon());
    }

    public void testGetFileChooserUpFolserIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getFileChooserUpFolderIcon();
        checkIcon(icon, 18, 18);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getFileChooserUpFolderIcon() == MetalIconFactory
                .getFileChooserUpFolderIcon());
    }

    public void testGetHorizontalSliderThumbIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getHorizontalSliderThumbIcon();
        checkIcon(icon, 16, 15);
        assertTrue(icon instanceof UIResource);
        assertSame(MetalIconFactory.getHorizontalSliderThumbIcon(), MetalIconFactory
                .getHorizontalSliderThumbIcon());
    }

    public void testGetInternalFrameAltMaximizeIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        int size = 10;
        Icon icon = MetalIconFactory.getInternalFrameAltMaximizeIcon(size);
        checkIcon(icon, size, size);
        assertTrue(icon instanceof UIResource);
        assertFalse(MetalIconFactory.getInternalFrameAltMaximizeIcon(size) == MetalIconFactory
                .getInternalFrameAltMaximizeIcon(size));
    }

    public void testGetInternalFrameCloseIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        int size = 14;
        Icon icon = MetalIconFactory.getInternalFrameCloseIcon(size);
        checkIcon(icon, size, size);
        assertTrue(icon instanceof UIResource);
        assertFalse(MetalIconFactory.getInternalFrameCloseIcon(size) == MetalIconFactory
                .getInternalFrameCloseIcon(size));
    }

    public void testGetInternalFrameDefaultMenuIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getInternalFrameDefaultMenuIcon();
        checkIcon(icon, 16, 16);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getInternalFrameDefaultMenuIcon() == MetalIconFactory
                .getInternalFrameDefaultMenuIcon());
    }

    public void testGetInternalFrameMaximizeIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        int size = 14;
        Icon icon = MetalIconFactory.getInternalFrameMaximizeIcon(size);
        checkIcon(icon, size, size);
        assertTrue(icon instanceof UIResource);
        assertFalse(MetalIconFactory.getInternalFrameMaximizeIcon(size) == MetalIconFactory
                .getInternalFrameMaximizeIcon(size));
    }

    public void testGetInternalFrameMinimizeIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        int size = 15;
        Icon icon = MetalIconFactory.getInternalFrameMinimizeIcon(size);
        checkIcon(icon, size, size);
        assertTrue(icon instanceof UIResource);
        assertFalse(MetalIconFactory.getInternalFrameMinimizeIcon(size) == MetalIconFactory
                .getInternalFrameMinimizeIcon(size));
    }

    public void testGetMenuArrowIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getMenuArrowIcon();
        checkIcon(icon, 8, 4);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getMenuArrowIcon() == MetalIconFactory.getMenuArrowIcon());
    }

    public void testGetMenuItemArrowIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getMenuItemArrowIcon();
        checkIcon(icon, 8, 4);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getMenuItemArrowIcon() == MetalIconFactory
                .getMenuItemArrowIcon());
    }

    public void testGetMenuItemCheckIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertNull(MetalIconFactory.getMenuItemCheckIcon());
    }

    public void testGetRadioButtonIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getRadioButtonIcon();
        checkIcon(icon, 13, 13);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getRadioButtonIcon() == MetalIconFactory
                .getRadioButtonIcon());
    }

    public void testGetRadioButtonMenuItemIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getRadioButtonMenuItemIcon();
        checkIcon(icon, 10, 10);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getRadioButtonMenuItemIcon() == MetalIconFactory
                .getRadioButtonMenuItemIcon());
    }

    public void testGetTreeComputerIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getTreeComputerIcon();
        checkIcon(icon, 16, 16);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getTreeComputerIcon() == MetalIconFactory
                .getTreeComputerIcon());
    }

    public void testGetTreeControlIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.TreeControlIcon icon = (MetalIconFactory.TreeControlIcon) MetalIconFactory
                .getTreeControlIcon(true);
        checkIcon(icon, 18, 18);
        assertFalse(icon instanceof UIResource);
        assertTrue(icon.isLight);
        assertFalse(MetalIconFactory.getTreeControlIcon(true) == MetalIconFactory
                .getTreeControlIcon(true));
    }

    public void testGetTreeFloppyDriveIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getTreeFloppyDriveIcon();
        checkIcon(icon, 16, 16);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getTreeFloppyDriveIcon() == MetalIconFactory
                .getTreeFloppyDriveIcon());
    }

    public void testGetTreeFolderIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.TreeFolderIcon icon = (MetalIconFactory.TreeFolderIcon) MetalIconFactory
                .getTreeFolderIcon();
        checkIcon(icon, 18, 16);
        assertFalse(icon instanceof UIResource);
        assertEquals(-1, icon.getShift());
        assertEquals(2, icon.getAdditionalHeight());
        assertFalse(MetalIconFactory.getTreeFolderIcon() == MetalIconFactory
                .getTreeFolderIcon());
    }

    public void testGetTreeHardDriveIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getTreeHardDriveIcon();
        checkIcon(icon, 16, 16);
        assertTrue(icon instanceof UIResource);
        assertTrue(MetalIconFactory.getTreeHardDriveIcon() == MetalIconFactory
                .getTreeHardDriveIcon());
    }

    public void testGetTreeLeafIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.TreeLeafIcon icon = (MetalIconFactory.TreeLeafIcon) MetalIconFactory
                .getTreeLeafIcon();
        checkIcon(icon, 20, 16);
        assertEquals(2, icon.getShift());
        assertEquals(4, icon.getAdditionalHeight());
        assertFalse(icon instanceof UIResource);
        assertFalse(MetalIconFactory.getTreeLeafIcon() == MetalIconFactory.getTreeLeafIcon());
    }

    public void testGetVerticalSliderThumbIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Icon icon = MetalIconFactory.getVerticalSliderThumbIcon();
        checkIcon(icon, 15, 16);
        assertTrue(icon instanceof UIResource);
        assertSame(MetalIconFactory.getVerticalSliderThumbIcon(), MetalIconFactory
                .getVerticalSliderThumbIcon());
    }

    public void testPaletteCloseIcon() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.PaletteCloseIcon icon = new MetalIconFactory.PaletteCloseIcon();
        if (isHarmony()) {
            checkIcon(icon, 8, 8);
        } else {
            checkIcon(icon, 7, 7);
        }
    }

    public void testFolderIcon16() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.FolderIcon16 icon = new MetalIconFactory.FolderIcon16();
        checkIcon(icon, 16, 16);
        assertFalse(icon instanceof UIResource);
        assertEquals(0, icon.getAdditionalHeight());
        assertEquals(0, icon.getShift());
    }

    public void testFileIcon16() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        MetalIconFactory.FileIcon16 icon = new MetalIconFactory.FileIcon16();
        checkIcon(icon, 16, 16);
        assertFalse(icon instanceof UIResource);
        assertEquals(0, icon.getAdditionalHeight());
        assertEquals(0, icon.getShift());
    }

    private static void checkIcon(final Icon icon, final int height, final int width) {
        assertNotNull(icon);
        assertEquals(height, icon.getIconHeight());
        assertEquals(width, icon.getIconWidth());
    }
}
