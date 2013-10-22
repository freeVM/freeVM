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
 * @author Dennis Ushakov
 * @version $Revision$
 */

package javax.accessibility;

import javax.swing.BasicSwingTestCase;

public class AccessibleBundleTest extends BasicSwingTestCase {
    AccessibleBundle bundle;
    Object[][] resources;

    public void setUp() {
        bundle = new AccessibleBundle() {};
        resources = new AccessibleResourceBundle().getContents();
    }

    public void tearDown() {
        bundle = null;
        resources = null;
    }

    public void testToDisplayString() throws Exception {
        for (int  i = 0; i < resources.length; i++) {
            bundle.key = (String)resources[i][0];
            assertEquals("DisplayString don't match resource: " + bundle.key,
                         bundle.toDisplayString(), resources[i][1]);
            assertEquals("toDisplayString don't match toString: " + bundle.key,
                         bundle.toString(), bundle.toDisplayString());
        }
        bundle.key = "ShouldNotFindSuchAString";
        assertEquals("Not bundled DisplayString should match itself",
                     "ShouldNotFindSuchAString", bundle.toDisplayString());

        testExceptionalCase(new ExceptionalCase() {
            public void exceptionalAction() throws Exception {
                bundle.key = null;
                bundle.toDisplayString();
            }
        });
    }
}

