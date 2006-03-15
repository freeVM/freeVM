/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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

package tests.api.java.io;

import java.io.SerializablePermission;

public class SerializablePermissionTest extends junit.framework.TestCase {

	/**
	 * @tests java.io.SerializablePermission#SerializablePermission(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.io.SerializablePermission(java.lang.String)
		assertTrue("permission ill-formed", new SerializablePermission(
				"enableSubclassImplementation").getName().equals(
				"enableSubclassImplementation"));
	}

	/**
	 * @tests java.io.SerializablePermission#SerializablePermission(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.io.SerializablePermission(java.lang.String,
		// java.lang.String)
		assertTrue("permission ill-formed", new SerializablePermission(
				"enableSubclassImplementation", "").getName().equals(
				"enableSubclassImplementation"));
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
