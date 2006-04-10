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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarFile;

import tests.support.Support_GetLocal;

public class SecureClassLoaderTest extends junit.framework.TestCase {

	/**
	 * @tests java.security.SecureClassLoader#getPermissions(java.security.CodeSource)
	 */
	public void test_getPermissionsLjava_security_CodeSource() {
		class MyClassLoader extends SecureClassLoader {
			public PermissionCollection getPerms() {
				return super.getPermissions(new CodeSource(null,
						(Certificate[]) null));
			}

			public Class define(String name, byte[] bytes) {
				return defineClass(name, bytes, 0, bytes.length,
						(ProtectionDomain) null);
			}
		};

		MyClassLoader myloader = new MyClassLoader();
		PermissionCollection pc = myloader.getPerms();
		Enumeration e1 = pc.elements();
		int count = 0;
		while (e1.hasMoreElements()) {
			e1.nextElement();
			count++;
		}
		assertEquals("expected no permissions", 0, count);

		byte[] bytes = null;
		try {
			File file = Support_GetLocal.getLocalFile("hyts_security.jar");
			JarFile jar = new JarFile(file);
			InputStream in = jar.getInputStream(jar
					.getEntry("packA/SecurityTest.class"));
			bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
		} catch (IOException e) {
			fail("unexpected IOException : " + e);
		}
		Class c = myloader.define("packA.SecurityTest", bytes);
		ProtectionDomain pd = c.getProtectionDomain();
		assertNotNull("Expected dynamic policy", pd.getClassLoader());
		assertNull("Expected null permissions", pd.getPermissions());
	}
}