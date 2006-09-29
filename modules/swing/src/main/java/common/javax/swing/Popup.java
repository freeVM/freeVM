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

import java.awt.Component;
import java.awt.Window;

import org.apache.harmony.awt.ComponentInternals;


public class Popup {
    class PopupWindow extends JWindow {
        public PopupWindow(final Window owner) {
            super(owner);
            setFocusableWindowState(false);
            ComponentInternals.getComponentInternals().makePopup(this);
        }

        public void init(final Component c, final int x, final int y) {
            getContentPane().add(c);

            pack();
            setLocation(x, y);
        }

        public void reset() {
            getContentPane().removeAll();
        }

        public void hide() {
            reset();
            super.hide();
        }

        public void dispose() {
            reset();
            super.dispose();
        }
    }

    PopupWindow popupWindow;

    protected Popup() {
    }

    protected Popup(final Component owner, final Component content, final int x, final int y) {
        if (content == null) {
            throw new IllegalArgumentException("Popup content should be specified");
        }

        Window ownerWindow = owner instanceof Window ? (Window)owner : SwingUtilities.getWindowAncestor(owner);
        popupWindow = new PopupWindow(ownerWindow != null ? ownerWindow : JFrame.getSharedOwner());
        popupWindow.init(content, x, y);
    }

    public void show() {
        popupWindow.show();
    }

    public void hide() {
        popupWindow.dispose();
    }
}
