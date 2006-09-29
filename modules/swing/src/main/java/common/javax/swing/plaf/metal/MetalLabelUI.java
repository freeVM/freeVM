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
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

import org.apache.harmony.x.swing.Utilities;


public class MetalLabelUI extends BasicLabelUI {
    protected static MetalLabelUI metalLabelUI;

    private Color disabledForeground = UIManager.getColor("Label.disabledForeground");

    public static ComponentUI createUI(final JComponent c) {
        if (metalLabelUI == null) {
            metalLabelUI = new MetalLabelUI();
        }

        return metalLabelUI;
    }

    protected void paintDisabledText(final JLabel label, final Graphics g, final String clippedText, final int textX, final int textY) {
        int underscore = Utilities.getClippedUnderscoreIndex(label.getText(), clippedText,
                label.getDisplayedMnemonicIndex());
        Utilities.drawString(g, clippedText, textX, textY, Utilities
                .getFontMetrics(label), disabledForeground, underscore);
    }
}
