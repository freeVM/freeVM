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
 * @author Elena Semukhina
 * @version $Revision$
 */

package java.lang;

import junit.framework.TestCase;

public class InheritableThreadLocalTest extends TestCase {
    
    public void testInheritableThreadLocal(){
        InheritableThreadLocalSupport t = new InheritableThreadLocalSupport();
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            fail("thread interrupted");
        }
        assertTrue("Wrong local Object value in parent",
                   t.parentInheritableLocalObjectOK);
        assertTrue("Wrong inheritable local Object value in child",
                   t.childInheritableLocalObjectOK);
        assertTrue("Wrong inheritable local Object value in grandchild",
                   t.grandChildInheritableLocalObjectOK);
        assertTrue("Wrong local Integer value in parent",
                   t.parentInheritableLocalIntegerOK);
        assertTrue("Wrong inheritable local Integer value in child",
                   t.childInheritableLocalIntegerOK);
        assertTrue("Wrong inheritable local Integer value in grandchild",
                   t.grandChildInheritableLocalIntegerOK);

    }
}
