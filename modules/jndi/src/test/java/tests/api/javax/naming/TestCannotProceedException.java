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
package tests.api.javax.naming;

import java.util.Hashtable;

import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;

import tests.api.javax.naming.util.Log;
import junit.framework.TestCase;

public class TestCannotProceedException extends TestCase {

	static private Log log = new Log(TestCannotProceedException.class);

	/**
	 * Constructor for TestCannotProceedException.
	 * 
	 * @param arg0
	 */
	public TestCannotProceedException(String arg0) {
		super(arg0);
	}

	public void testConstructorAndGetterSetter() throws InvalidNameException {
		log.setMethod("testConstructorAndGetterSetter()");

		CannotProceedException cpe = new CannotProceedException();
		Name altName = new CompositeName("1");
		Context altContext = null;
		Hashtable h = new Hashtable();
		Name newName = new CompositeName("2");

		cpe.setAltName(altName);
		assertEquals(altName, cpe.getAltName());

		cpe.setAltNameCtx(altContext);
		assertEquals(altContext, cpe.getAltNameCtx());

		cpe.setEnvironment(h);
		assertEquals(h, cpe.getEnvironment());

		cpe.setRemainingNewName(newName);
		assertEquals(newName, cpe.getRemainingNewName());
	}

	public void testConstructor_defaultValue() {
		CannotProceedException cpe = new CannotProceedException();
		assertNull(cpe.getMessage());
		assertNull(cpe.getAltName());
		assertNull(cpe.getAltNameCtx());
		assertNull(cpe.getEnvironment());
		assertNull(cpe.getRemainingNewName());
	}

	public void testGetEnvironment() {
		CannotProceedException exception = new CannotProceedException("Test");
		assertNull(exception.getEnvironment());
	}
}
