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
package javax.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class JToggleButton extends AbstractButton implements Accessible {

    // TODO implement
    protected class AccessibleJToggleButton extends AccessibleAbstractButton implements ItemListener {
        public AccessibleJToggleButton() {
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TOGGLE_BUTTON;
        }

        public void itemStateChanged(final ItemEvent e) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    };

    public static class ToggleButtonModel extends DefaultButtonModel {
        public void setPressed(final boolean pressed) {
            boolean oldPressed = isPressed();
            if (oldPressed != pressed && !pressed && isArmed()) {
                setSelected(!isSelected());
            }
            super.setPressed(pressed);
        }

        public void setSelected(final boolean selected) {
            if (group != null && group.getSelection() == this) {
                return;
            }
            super.setSelected(selected);
            if (selected && group != null) {
                group.setSelected(this, true);
            }
        }
    }

    private static final String UI_CLASS_ID = "ToggleButtonUI";

    public JToggleButton(final String text, final Icon icon, final boolean selected) {
        setModel(new ToggleButtonModel());
        setSelected(selected);
        init(text, icon);
    }

    public JToggleButton(final String text, final Icon icon) {
        this(text, icon, false);
    }

    public JToggleButton(final Icon icon, final boolean selected) {
        this(null, icon, selected);
    }

    public JToggleButton(final Icon icon) {
        this(null, icon, false);
    }

    public JToggleButton(final Action action) {
        setModel(new ToggleButtonModel());
        setAction(action);
        init(getText(), getIcon());
    }

    public JToggleButton(final String text, final boolean selected) {
        this(text, null, selected);
    }

    public JToggleButton(final String text) {
        this(text, null, false);
    }

    public JToggleButton() {
        this(null, null, false);
    }

    public AccessibleContext getAccessibleContext() {
        return (accessibleContext == null) ? (accessibleContext = new AccessibleJToggleButton())
                : accessibleContext;
    }

    public String getUIClassID() {
        return UI_CLASS_ID;
    }
}

