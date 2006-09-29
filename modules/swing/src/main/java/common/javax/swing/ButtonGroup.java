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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class ButtonGroup implements Serializable {

    protected Vector buttons = new Vector();

    private ButtonModel selection;

    public void add(final AbstractButton button) {
        if (button == null) {
            return;
        }
        
        buttons.add(button);

        ButtonModel model = button.getModel();
        if (button.isSelected()) {
            if (selection != null && model != selection) {
                button.setSelected(false);
            } else {
                selection = model;
            }
        }
        model.setGroup(this);
    }

    public int getButtonCount() {
        return buttons.size();
    }

    public Enumeration getElements() {
        return buttons.elements();
    }

    public ButtonModel getSelection() {
        return selection;
    }

    public boolean isSelected(final ButtonModel model) {
        return (model == selection);
    }

    public void remove(final AbstractButton button) {
        if (button == null) {
            return;
        }

        buttons.remove(button);
        ButtonModel model = button.getModel();
        if (selection == model) {
            selection = null;
        }
        model.setGroup(null);
    }

    public void setSelected(final ButtonModel model, final boolean selected) {
        if (!selected || model == null || selection == model) {
            return;
        }
        if (!model.isSelected()) {
            model.setSelected(true);
        }
        ButtonModel prevSelection = selection;
        selection = model;
        if (prevSelection != null) {
            prevSelection.setSelected(false);
        }
    }

}

