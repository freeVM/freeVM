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
 * @author Dennis Ushakov
 * @version $Revision$
 */

package javax.accessibility;

import java.util.Iterator;
import java.util.Vector;

public class AccessibleStateSet {
    protected Vector states;

    public AccessibleStateSet() {
        initStorage();
    }

    public AccessibleStateSet(final AccessibleState[] states) {
        initStorage(states.length);
        addAll(states);
    }

    public boolean add(final AccessibleState state) {
        initStorage();
        if (states.contains(state)) {
            return false;
        }
        states.add(state);
        return true;
    }

    public void addAll(final AccessibleState[] states) {
        initStorage(states.length);
        for (int i = 0; i < states.length; i++) {
            add(states[i]);
        }
    }

    public boolean contains(final AccessibleState state) {
        return states == null ? false : states.contains(state);
    }

    public boolean remove(final AccessibleState state) {
        return states == null ? false : states.remove(state);
    }

    public void clear() {
        if (states != null) {
            states.clear();
        }
    }

    public AccessibleState[] toArray() {
        return states == null ? new AccessibleState[0] :
            (AccessibleState[])states.toArray(new AccessibleState[states.size()]);
    }

    public String toString() {
        if (states == null) return "";
        StringBuffer str = new StringBuffer();
        for (Iterator it = states.iterator(); it.hasNext(); ) {
            str.append(it.next());
            if (it.hasNext()) {
                str.append(",");
            }
        }
        return str.toString();
    }

    private void initStorage(final int capacity) {
        if (states == null) {
            states = new Vector(capacity);
        }
    }

    private void initStorage() {
        if (states == null) {
            states = new Vector();
        }
    }
}

