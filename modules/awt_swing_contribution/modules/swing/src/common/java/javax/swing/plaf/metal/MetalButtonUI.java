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
package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.apache.harmony.x.swing.ButtonCommons;


public class MetalButtonUI extends BasicButtonUI {

    protected Color disabledTextColor;
    protected Color focusColor;
    protected Color selectColor;

    private Color pressedButtonBG;

    private static MetalButtonUI commonMetalButtonUI;

    public static ComponentUI createUI(final JComponent component) {
        if (commonMetalButtonUI == null) {
            commonMetalButtonUI = new MetalButtonUI();
        }
        return commonMetalButtonUI;
    }

    public void installDefaults(final AbstractButton button) {
        super.installDefaults(button);
        pressedButtonBG = UIManager.getColor(getPropertyPrefix() + "shadow");
    }

    public void uninstallDefaults(final AbstractButton button) {
        super.uninstallDefaults(button);
    }

    protected Color getDisabledTextColor() {
        disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
        return disabledTextColor;
    }

    protected Color getFocusColor() {
        focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
        return focusColor;
    }

    protected Color getSelectColor() {
        selectColor = UIManager.getColor(getPropertyPrefix() + "select");
        return selectColor;
    }

    protected void paintButtonPressed(final Graphics g, final AbstractButton button) {
        if (button.getModel().isArmed() && button.isContentAreaFilled()) {
            ButtonCommons.paintPressed(g, button, pressedButtonBG);
        }
    }

    protected void paintFocus(final Graphics g, final AbstractButton b, final Rectangle viewRect, final Rectangle textRect, final Rectangle iconRect) {
        ButtonCommons.paintFocus(g, ButtonCommons.getFocusRect(viewRect, textRect, iconRect), getFocusColor());
    }

    protected void paintText(final Graphics g, final JComponent c, final Rectangle textRect, final String text) {
        final Color color = c.isEnabled() ? c.getForeground() : getDisabledTextColor();
        final int offset = getTextShiftOffset();
        textRect.translate(offset, offset);
        ButtonCommons.paintText(g, (AbstractButton)c, textRect, text, color);
    }
}

