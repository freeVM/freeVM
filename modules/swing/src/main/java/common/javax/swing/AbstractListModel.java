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

import java.io.Serializable;
import java.util.EventListener;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class AbstractListModel implements ListModel, Serializable {
    protected EventListenerList listenerList = new EventListenerList();

    public void addListDataListener(final ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    public void removeListDataListener(final ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public ListDataListener[] getListDataListeners() {
        return (ListDataListener[])getListeners(ListDataListener.class);
    }

    public EventListener[] getListeners(final Class listenerType) {
        return listenerList.getListeners(listenerType);
    }

    protected void fireContentsChanged(final Object source, final int index0, final int index1) {
        ListDataEvent event = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
        ListDataListener[] listeners = getListDataListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].contentsChanged(event);
        }
    }

    protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
        ListDataEvent event = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
        ListDataListener[] listeners = getListDataListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].intervalAdded(event);
        }
    }

    protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
        ListDataEvent event = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
        ListDataListener[] listeners = getListDataListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].intervalRemoved(event);
        }
    }
}
