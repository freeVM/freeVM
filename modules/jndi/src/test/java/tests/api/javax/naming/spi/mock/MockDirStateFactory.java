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
package tests.api.javax.naming.mock;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirStateFactory;

import tests.api.javax.naming.spi.TestNamingManager;
import tests.api.javax.naming.util.Log;

public class MockDirStateFactory implements DirStateFactory {

	static Log log = new Log(MockDirStateFactory.class);

	public MockDirStateFactory() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.spi.DirStateFactory#getStateToBind(java.lang.Object,
	 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable,
	 *      javax.naming.directory.Attributes)
	 */
	public Result getStateToBind(Object o, Name n, Context c, Hashtable h,
			Attributes a) throws NamingException {
		TestNamingManager.issueIndicatedExceptions(h);
		if (TestNamingManager.returnNullIndicated(h)) {
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
		if (null != a) {
			r.put("a", a);
		}
		return new Result(r, a);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.spi.StateFactory#getStateToBind(java.lang.Object,
	 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	public Object getStateToBind(Object o, Name n, Context c, Hashtable h)
			throws NamingException {
		return getStateToBind(o, n, c, h, null);
	}

}
