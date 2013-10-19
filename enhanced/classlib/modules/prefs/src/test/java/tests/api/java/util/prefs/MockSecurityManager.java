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

package tests.api.java.util.prefs;

import java.security.Permission;

/**
 * utility class for java.util.prefs test
 * 
 */
class MockSecurityManager extends SecurityManager {

	SecurityManager dflt;

	public MockSecurityManager() {
		super();
		dflt = System.getSecurityManager();
	}

	public void install() {
		System.setSecurityManager(this);
	}

	public void restoreDefault() {
		System.setSecurityManager(dflt);
	}

	public void checkPermission(Permission perm) {
		if (perm instanceof RuntimePermission
				&& perm.getName().equals("preferences")) {
			throw new SecurityException();
		} else if (dflt != null) {
			dflt.checkPermission(perm);
		}
	}

	public void checkPermission(Permission perm, Object ctx) {
		if (perm instanceof RuntimePermission
				&& perm.getName().equals("preferences")) {
			throw new SecurityException();
		} else if (dflt != null) {
			dflt.checkPermission(perm, ctx);
		}
	}

}
