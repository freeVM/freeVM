/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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

package tests.api.java.security;

import java.security.SecurityPermission;

public class SecurityPermissionTest extends junit.framework.TestCase {

	/**
	 * @tests java.security.SecurityPermission#SecurityPermission(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.security.SecurityPermission(java.lang.String)
		assertTrue("create securityPermission constructor(string) failed",
				new SecurityPermission("SecurityPermission(string)").getName()
						.equals("SecurityPermission(string)"));

	}

	/**
	 * @tests java.security.SecurityPermission#SecurityPermission(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.security.SecurityPermission(java.lang.String,
		// java.lang.String)
		SecurityPermission sp = new SecurityPermission("security.file", "write");
		assertTrue(
				"creat securityPermission constructor(string,string) failed",
				sp.getName().equals("security.file"));

	}
}