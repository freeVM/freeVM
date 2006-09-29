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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

final class ActionProxy implements Action, Serializable {
    private final String command;
    private final ActionListener listener;

    public ActionProxy(final String command, final ActionListener listener) {
        this.command = command;
        this.listener = listener;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(final boolean enabled) {
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
    }

    public Object getValue(final String valueName) {
        if (Action.NAME.equals(valueName)) {
            return listener;
        }
        if (Action.ACTION_COMMAND_KEY.equals(valueName)) {
            return command;
        }
        return null;
    }

    public void putValue(final String valueName, final Object value) {
    }

    public void actionPerformed(final ActionEvent event) {
        listener.actionPerformed(event);
    }
}

