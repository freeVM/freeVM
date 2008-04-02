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
package org.apache.harmony.pack200.tests;

import junit.framework.TestCase;

import org.apache.harmony.pack200.bytecode.CPUTF8;
import org.apache.harmony.pack200.bytecode.ClassConstantPool;

public class CPUTF8Test extends TestCase {

    public void testEquality() {
        CPUTF8 one = new CPUTF8("(III)V", ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ);
        CPUTF8 two = new CPUTF8("((I[II)V", ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ);
        CPUTF8 three = new CPUTF8("([III)V", ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ);
        assertFalse(one.equals(two));
        assertFalse(one.equals(three));
        assertFalse(two.equals(three));

        assertFalse(two.equals(one));
        assertFalse(three.equals(one));
        assertFalse(three.equals(two));
    }
}
