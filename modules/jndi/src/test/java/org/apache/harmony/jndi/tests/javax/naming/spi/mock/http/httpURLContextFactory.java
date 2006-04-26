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
package org.apache.harmony.jndi.tests.javax.naming.spi.mock.http;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.apache.harmony.jndi.tests.javax.naming.spi.mock.MockDirContext;
import org.apache.harmony.jndi.tests.javax.naming.spi.NamingManagerTest;

public class httpURLContextFactory implements ObjectFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
	 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	public Object getObjectInstance(Object o, Name n, Context c, Hashtable h)
			throws Exception {

		NamingManagerTest.issueIndicatedExceptions(h);
		if (NamingManagerTest.returnNullIndicated(h)) {
			return null;
		}

		Hashtable r = new Hashtable();
		if (null != o) {
			r.put("o", o);
		}
		if (null != n) {
			r.put("n", n);
		}
		if (null != c) {
			r.put("c", c);
		}
		if (null != h) {
			r.put("h", h);
		}
		r.put("url.schema", "http");
		return new MockDirContext(r);
	}

}
