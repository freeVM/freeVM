/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

public class ActionMapRTest extends SwingTestCase {
    public void testGet() {
        ActionMap map = new ActionMap();
        final AbstractAction action = new AbstractAction("result") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
            }
        };
        map.setParent(new ActionMap() {
            private static final long serialVersionUID = 1L;

            @Override
            public Action get(Object key) {
                return action;
            }
        });
        assertSame(action, map.get("key"));
    }
}
