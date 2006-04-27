/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.logging.tests.java.util.logging;

import java.util.logging.LoggingPermission;

import junit.framework.TestCase;
import tests.util.SerializationTester;

/**
 */
public class LoggingPermissionTest extends TestCase {
	private static String className = LoggingPermissionTest.class.getName();

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for LoggingPermissionTest.
	 * 
	 * @param arg0
	 */
	public LoggingPermissionTest(String arg0) {
		super(arg0);
	}

	public void testSerializationCompability() throws Exception {
		LoggingPermission lp = new LoggingPermission("control", "");
		SerializationTester.assertCompabilityEquals(lp,
				"serialization/java/util/logging/LogPermission.ser");
	}

	public void testLoggingPermission() {
		try {
			LoggingPermission lp = new LoggingPermission(null, null);
			fail("should throw IllegalArgumentException");
		} catch (NullPointerException e) {
		}
		try {
			LoggingPermission lp = new LoggingPermission("", null);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			LoggingPermission lp = new LoggingPermission("bad name", null);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			LoggingPermission lp = new LoggingPermission("Control", null);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			LoggingPermission lp = new LoggingPermission("control",
					"bad action");
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		LoggingPermission lp = new LoggingPermission("control", "");
		lp = new LoggingPermission("control", null);
	}

}
