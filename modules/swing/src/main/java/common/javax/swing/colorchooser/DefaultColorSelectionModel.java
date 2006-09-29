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
 * @author Dennis Ushakov
 * @version $Revision$
 */
package javax.swing.colorchooser;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultColorSelectionModel implements ColorSelectionModel, Serializable {
    protected transient ChangeEvent changeEvent;
    protected EventListenerList listenerList = new EventListenerList();

    private Color color;

    public DefaultColorSelectionModel() {
        this(Color.WHITE);
    }

    public DefaultColorSelectionModel(final Color color) {
        this.color = color;
    }

    public Color getSelectedColor() {
        return color;
    }

    public void setSelectedColor(final Color color) {
        if (color == null) {
            return;
        }
        Color oldColor = this.color;
        this.color = color;
        if (!oldColor.equals(color)) {
            fireStateChanged();
        }
    }

    public void addChangeListener(final ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }
        ChangeListener[] listeners = getChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].stateChanged(changeEvent);
        }
    }

}
