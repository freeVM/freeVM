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
package javax.swing.plaf.metal;

import junit.framework.TestCase;

public class MetalCheckBoxIconTest extends TestCase {
    protected MetalCheckBoxIcon icon;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        icon = new MetalCheckBoxIcon();
    }

    @Override
    protected void tearDown() throws Exception {
        icon = null;
        super.tearDown();
    }

    /*
     * Test method for 'javax.swing.plaf.metal.MetalCheckBoxIcon.getControlSize()'
     */
    public void testGetControlSize() {
        assertEquals(13, icon.getControlSize());
    }

    /*
     * Test method for 'javax.swing.plaf.metal.MetalCheckBoxIcon.getIconWidth()'
     */
    public void testGetIconWidth() {
        assertEquals(icon.getControlSize(), icon.getIconWidth());
    }

    /*
     * Test method for 'javax.swing.plaf.metal.MetalCheckBoxIcon.getIconHeight()'
     */
    public void testGetIconHeight() {
        assertEquals(icon.getControlSize(), icon.getIconHeight());
    }
}
