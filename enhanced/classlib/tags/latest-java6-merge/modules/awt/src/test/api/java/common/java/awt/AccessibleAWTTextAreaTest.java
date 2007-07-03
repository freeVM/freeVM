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
 * @author Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.awt.TextArea.AccessibleAWTTextArea;

import javax.accessibility.AccessibleState;

import junit.framework.TestCase;

public class AccessibleAWTTextAreaTest extends TestCase {
    TextArea textArea;
    AccessibleAWTTextArea aTextArea;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        textArea = new TextArea();
        aTextArea = textArea.new AccessibleAWTTextArea();
        assertTrue(textArea.getAccessibleContext()
                   instanceof AccessibleAWTTextArea);
    }

    /*
     * Test method for 'java.awt.TextArea.AccessibleAWTTextArea.getAccessibleStateSet()'
     */
    public void testGetAccessibleStateSet() {
        AccessibleState state = AccessibleState.MULTI_LINE;
        assertTrue("text area is multi-line text",
                   aTextArea.getAccessibleStateSet().contains(state));

    }

}
