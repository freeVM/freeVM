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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

public class AccessControlContextTest extends junit.framework.TestCase {

	/**
	 * @tests java.security.AccessControlContext#AccessControlContext(java.security.ProtectionDomain[])
	 */
	public void test_Constructor$Ljava_security_ProtectionDomain() {
		// Test for method
		// java.security.AccessControlContext(java.security.ProtectionDomain [])

		// Create a permission which is not normally granted
		final Permission perm = new PropertyPermission("java.class.path",
				"read");
		PermissionCollection col = perm.newPermissionCollection();
		col.add(perm);
		final ProtectionDomain pd = new ProtectionDomain(null, col);
		AccessControlContext acc = new AccessControlContext(
				new ProtectionDomain[] { pd });
		try {
			acc.checkPermission(perm);
		} catch (SecurityException e) {
			fail("Should have permission");
		}

		final boolean[] result = new boolean[] { false };
		Thread th = new Thread(new Runnable() {
			public void run() {
				AccessControlContext acc = new AccessControlContext(
						new ProtectionDomain[] { pd });
				try {
					acc.checkPermission(perm);
					result[0] = true;
				} catch (SecurityException e) {
				}
			}
		});
		th.start();
		try {
			th.join();
		} catch (InterruptedException e) {
			// ignore
		}
		assertTrue("Thread should have permission", result[0]);
	}

	/**
	 * @tests java.security.AccessControlContext#AccessControlContext(java.security.AccessControlContext,
	 *        java.security.DomainCombiner)
	 */
	public void test_ConstructorLjava_security_AccessControlContextLjava_security_DomainCombiner() {
		AccessControlContext context = AccessController.getContext();
		try {
			new AccessControlContext(context, null);
		} catch (NullPointerException e) {
			fail("should not throw NullPointerException");
		}
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