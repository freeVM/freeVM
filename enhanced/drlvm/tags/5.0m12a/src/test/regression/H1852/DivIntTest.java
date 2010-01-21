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

package org.apache.harmony.drlvm.tests.regression.h1852;

import junit.framework.TestCase;

public class DivIntTest extends TestCase {

    public void testIDIV() {
        int i_min = Integer.MIN_VALUE;
        int i_1 = -1;
        int res = i_min / i_1;

        assertEquals(Integer.MIN_VALUE, res);
    }

    public void testIREM() {
        int i_min = Integer.MIN_VALUE;
        int i_1 = -1;
        int res = i_min % i_1;

        assertEquals(0, res);
    }
}
