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
package org.apache.harmony.jndi.tests.javax.naming.spi.mock.ftp;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.apache.harmony.jndi.tests.javax.naming.spi.NamingManagerTest;

// import util.Log;

/**
 * Allways succeed.
 */
public class ftpURLContextFactory implements ObjectFactory {
	public Object getObjectInstance(Object o, Name n, Context c, Hashtable h)
			throws Exception {

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
		return new NamingManagerTest.MockContext(r);
	}

}
