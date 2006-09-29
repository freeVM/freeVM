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
 * @author Pavel Dolgov
 * @version $Revision$
 */
package org.apache.harmony.awt.wtk;

import junit.framework.TestCase;

public class ShutdownThreadTest extends TestCase {
    
    private ShutdownThread t;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ShutdownThreadTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        t = new ShutdownThread();
    }

    public void testStartAndShutdown() {
        assertFalse(t.isAlive());
        t.start();
        assertTrue(t.isAlive());
        t.shutdown();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(t.isAlive());
    }
}
