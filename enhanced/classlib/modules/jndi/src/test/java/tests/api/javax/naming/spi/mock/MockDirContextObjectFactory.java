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
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.DirObjectFactory;

import tests.api.javax.naming.util.Log;

public class MockDirContextObjectFactory implements DirObjectFactory {

	Log log = new Log(MockDirContextObjectFactory.class);

	public static final DirContext DIR_CONTEXT = new MockDirContext(
			new Hashtable());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.spi.DirObjectFactory#getObjectInstance(java.lang.Object,
	 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable,
	 *      javax.naming.directory.Attributes)
	 */
	public Object getObjectInstance(Object o, Name n, Context c,
			Hashtable envmt, Attributes a) throws Exception {
		log.setMethod("getObjectInstance");
		log.log("wrong method call");
		return DIR_CONTEXT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
	 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	public Object getObjectInstance(Object o, Name n, Context c, Hashtable envmt)
			throws Exception {
		return DIR_CONTEXT;
	}
}
