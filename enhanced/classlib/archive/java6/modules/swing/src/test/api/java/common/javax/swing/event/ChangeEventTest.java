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
 * @author Anton Avtamonov
 * @version $Revision$
 */
package javax.swing.event;

import junit.framework.TestCase;

public class ChangeEventTest extends TestCase {
    private Object source;

    public ChangeEventTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        source = new Object();
    }

    @Override
    protected void tearDown() throws Exception {
        source = null;
    }

    public void testChangeEvent() throws Exception {
        ChangeEvent event = new ChangeEvent(source);
        assertEquals(source, event.getSource());
    }
}
