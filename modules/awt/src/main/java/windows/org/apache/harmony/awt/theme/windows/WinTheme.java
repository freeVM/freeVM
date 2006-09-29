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
 * @author Pavel Dolgov
 * @version $Revision$
 */
package org.apache.harmony.awt.theme.windows;

import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.apache.harmony.awt.ContextStorage;
import org.apache.harmony.awt.Theme;
import org.apache.harmony.awt.state.ButtonState;
import org.apache.harmony.awt.state.CheckboxState;
import org.apache.harmony.awt.state.ChoiceState;
import org.apache.harmony.awt.state.ScrollbarState;
import org.apache.harmony.awt.state.TextComponentState;
import org.apache.harmony.awt.wtk.windows.WinEventQueue;


/**
 * WinTheme
 */
public class WinTheme extends Theme {

    private WinEventQueue.ThemeMap themeMap;

    private WinEventQueue.ThemeMap getThemeMap() {
        if (themeMap != null) {
            return themeMap;
        }
        WinEventQueue eq = (WinEventQueue)ContextStorage.getNativeEventQueue();
        themeMap = eq.getThemeMap();
        return themeMap;
    }

    protected long getXpTheme(String s) {
        return getThemeMap().get(s);
    }

    protected boolean isXpThemeActive() {
        return getThemeMap().isEnabled();
    }

    protected void drawButtonBackground(Graphics g, ButtonState s) {

        if (! s.isBackgroundSet()) {
            WinButton.drawBackground(g, s, this);
        } else {
            super.drawButtonBackground(g, s);
        }
    }

    protected void drawCheckboxBackground(Graphics g, CheckboxState s,
            Rectangle focusRect) {
        if (! s.isBackgroundSet()) {
            WinCheckbox.drawBackground(g, s, focusRect, this);
        } else {
            super.drawCheckboxBackground(g, s, focusRect);
        }
    }

    public void drawScrollbar(Graphics g, ScrollbarState s) {
        WinScrollbar.draw(g, s, this);
    }


    protected void drawChoiceBackground(Graphics g, ChoiceState s) {
        WinChoice.drawBackground(g, s, this);
    }

    public void drawTextComponentBackground(Graphics g, TextComponentState s) {
        WinTextComponent.drawBackground(g, s, this);
    }

    public boolean showFileDialog(FileDialog fd) {
        WinFileDialog dlg = WinFileDialog.getInstance(fd);
        if (dlg == null) {
            dlg = new WinFileDialog(fd);
        }                
        return dlg.show();
    }

    public boolean hideFileDialog(FileDialog fd) {
        WinFileDialog dlg = WinFileDialog.getInstance(fd);
        if (dlg != null) {
            dlg.close();
        }
        return false;
    }
}
