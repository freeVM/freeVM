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

import org.apache.harmony.pack200.CpBands;
import org.apache.harmony.pack200.Segment;
import org.apache.harmony.pack200.SegmentConstantPool;

/**
 * Tests for org.apache.harmony.pack200.SegmentConstantPool.
 */
public class SegmentConstantPoolTest extends TestCase {

    public class MockSegmentConstantPool extends SegmentConstantPool {
        public MockSegmentConstantPool() {
        	super(new CpBands(new Segment()));
        };

        public int matchSpecificPoolEntryIndex(String[] classNameArray, String desiredClassName, int desiredIndex) {
        	return super.matchSpecificPoolEntryIndex(classNameArray, desiredClassName, desiredIndex);
        };

        public int matchSpecificPoolEntryIndex(String[] classNameArray, String[] methodNameArray, String desiredClassName, String desiredMethodRegex, int desiredIndex) {
        	return super.matchSpecificPoolEntryIndex(classNameArray, methodNameArray, desiredClassName, desiredMethodRegex, desiredIndex);
        };
    }

	String[] testClassArray = {"Object", "Object" , "java/lang/String", "java/lang/String", "Object", "Other" };
	String[] testMethodArray = {"<init>()", "clone()" , "equals()", "<init>", "isNull()", "Other" };

	public void testMatchSpecificPoolEntryIndex_SingleArray() throws Exception {
		MockSegmentConstantPool mockInstance = new MockSegmentConstantPool();
		// Elements should be found at the proper position.
		assertEquals(0, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "Object", 0));
		assertEquals(1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "Object", 1));
		assertEquals(2, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "java/lang/String", 0));
		assertEquals(3, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "java/lang/String", 1));
		assertEquals(4, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "Object", 2));
		assertEquals(5, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "Other", 0));

		// Elements that don't exist shouldn't be found
		assertEquals(-1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "NotThere", 0));

		// Elements that exist but don't have the requisite number
		// of hits shouldn't be found.
		assertEquals(-1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, "java/lang/String", 2));
	}

	public void testMatchSpecificPoolEntryIndex_DoubleArray() throws Exception {
		MockSegmentConstantPool mockInstance = new MockSegmentConstantPool();
		// Elements should be found at the proper position.
		assertEquals(0, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "Object", "<init>.*", 0));
		assertEquals(1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "Object", "clone.*", 0));
		assertEquals(2, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "java/lang/String", "equals.*", 0));
		assertEquals(3, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "java/lang/String", "<init>.*", 0));
		assertEquals(4, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "Object", "isNull.*", 0));
		assertEquals(5, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "Other", "Other", 0));

		// Elements that don't exist shouldn't be found
		assertEquals(-1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "NotThere", "NotThere", 0));
		assertEquals(-1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "Object", "NotThere", 0));

		// Elements that exist but don't have the requisite number
		// of hits shouldn't be found.
		assertEquals(-1, mockInstance.matchSpecificPoolEntryIndex(testClassArray, testMethodArray, "java/lang/String", "<init>.*", 1));
	}

}