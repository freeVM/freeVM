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
package javax.swing.event;

import java.util.EventObject;

import javax.swing.table.TableColumnModel;

public class TableColumnModelEvent extends EventObject {
    protected int fromIndex;
    protected int toIndex;

    public TableColumnModelEvent(final TableColumnModel source, final int from, final int to) {
        super(source);
        fromIndex = from;
        toIndex = to;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }
}
