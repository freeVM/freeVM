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
 * @author Alexander T. Simbirtsev
 * @version $Revision$
 */
package javax.swing;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class JCheckBox extends JToggleButton {
    protected class AccessibleJCheckBox extends AccessibleJToggleButton {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CHECK_BOX;
        }
    };

    public static final String BORDER_PAINTED_FLAT_CHANGED_PROPERTY = "borderPaintedFlat";

    private static final String UI_CLASS_ID = "CheckBoxUI";

    private boolean borderPaintedFlat;

    public JCheckBox() {
        super(null, null, false);
    }

    public JCheckBox(final Action action) {
        super(action);
    }

    public JCheckBox(final Icon icon) {
        super(null, icon, false);
    }

    public JCheckBox(final Icon icon, final boolean selected) {
        super(null, icon, selected);
    }

    public JCheckBox(final String text) {
        super(text, null, false);
    }

    public JCheckBox(final String text, final boolean selected) {
        super(text, null, selected);
    }

    public JCheckBox(final String text, final Icon icon) {
        super(text, icon, false);
    }

    public JCheckBox(final String text, final Icon icon, final boolean selected) {
        super(text, icon, selected);
    }

    void configurePropertyFromAction(final Action action, final Object propertyName) {
        if (propertyName == null || propertyName.equals(Action.SMALL_ICON)) {
            return;
        }
        super.configurePropertyFromAction(action, propertyName);
    }

    protected void init(final String text, final Icon icon) {
        setHorizontalAlignment(LEADING);
        super.init(text, icon);
    }

    public AccessibleContext getAccessibleContext() {
        return (accessibleContext == null) ? (accessibleContext = new AccessibleJCheckBox())
                : accessibleContext;
    }

    public String getUIClassID() {
        return UI_CLASS_ID;
    }

    public boolean isBorderPaintedFlat() {
        return borderPaintedFlat;
    }

    public void setBorderPaintedFlat(final boolean paintedFlat) {
        boolean oldValue = borderPaintedFlat;
        borderPaintedFlat = paintedFlat;
        firePropertyChange(BORDER_PAINTED_FLAT_CHANGED_PROPERTY, oldValue, borderPaintedFlat);
    }

    Object getActionPropertiesFilter() {
        return JRadioButton.NO_ICON_ACTION_PROPERTIES;
    }
}

