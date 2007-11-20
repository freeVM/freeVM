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

import org.apache.harmony.pack200.IMatcher;
import org.apache.harmony.pack200.SegmentUtils;

public class SegmentUtilsTest extends TestCase {
	private static class MultipleMatches implements IMatcher {
		private final int divisor;

		public MultipleMatches(int divisor) {
			this.divisor = divisor;
		}

		public boolean matches(long value) {
			return value % divisor == 0;
		}

	}

	public static final IMatcher even = new MultipleMatches(2);
	public static final IMatcher five = new MultipleMatches(5);

	public void testCountArgs() {
		assertEquals(0, SegmentUtils.countArgs("()V"));
		assertEquals(1, SegmentUtils.countArgs("(D)V"));
		assertEquals(1, SegmentUtils.countArgs("([D)V"));
		assertEquals(1, SegmentUtils.countArgs("([[D)V"));
		assertEquals(2, SegmentUtils.countArgs("(DD)V"));
		assertEquals(3, SegmentUtils.countArgs("(DDD)V"));
		assertEquals(2, SegmentUtils.countArgs("(Lblah/blah;D)V"));
		assertEquals(3, SegmentUtils.countArgs("(Lblah/blah;DLbLah;)V"));
	}

	public void testMatches() {
		long[] oneToTen = new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		assertEquals(6, SegmentUtils.countMatches(new long[][] { oneToTen,
				new long[] { 5, 6, 7 } }, even));
		assertEquals(5, SegmentUtils.countMatches(new long[][] { oneToTen },
				even));
		assertEquals(5, SegmentUtils.countMatches(oneToTen, even));
		assertEquals(3, SegmentUtils.countMatches(new long[][] { oneToTen,
				new long[] { 5, 6, 7 } }, five));
		assertEquals(2, SegmentUtils.countMatches(new long[][] { oneToTen },
				five));
		assertEquals(2, SegmentUtils.countMatches(oneToTen, five));
	}
}
