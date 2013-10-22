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
package tests.api.javax.naming.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirObjectFactory;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

import junit.framework.TestCase;
import tests.api.javax.naming.util.Log;

public class TestDirectoryManagerBuilder extends TestCase {

	private static final String OBJ = "mockDirObject by Builder";

	static Log log = new Log(TestDirectoryManagerBuilder.class);

	/**
	 * Constructor for TestDirectoryManagerBuilder.
	 * 
	 * @param arg0
	 */
	public TestDirectoryManagerBuilder(String arg0) {
		super(arg0);
	}

	/*
	 * -------------------------------------------------------------------
	 * Methods
	 * -------------------------------------------------------------------
	 */

	private void invokeMyTestMethod(String methodName) {
		log.setMethod(methodName);
		try {
			Method m = this.getClass().getMethod(methodName, new Class[0]);
			m.invoke(this, new Object[0]);
			// log.log("Succeeded!");
		} catch (Throwable t) {
			String errMsg = t.getMessage();

			if (t instanceof InvocationTargetException) {
				errMsg = ((InvocationTargetException) t).getTargetException()
						.getMessage();
			}

			log.log("Failed: " + t.getClass().getName() + " - " + errMsg);
		}
	}

	/**
	 * Test the normal condition when factory builder is properly set.
	 */
	public void myTestGetObjectInstance_HasBuilder_Normal() throws Exception {
		log.setMethod("myTestGetObjectInstance_HasBuilder_Normal");
		Attributes a = new BasicAttributes();
		Object obj = DirectoryManager.getObjectInstance(null, null, null, null,
				null);
		assertSame(OBJ, obj);

		obj = DirectoryManager.getObjectInstance("String", null, null, null, a);
		assertSame(OBJ, obj);

		Reference r = new Reference(null,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException",
				null);
		obj = DirectoryManager.getObjectInstance(r, null, null, null, a);
		assertSame(OBJ, obj);

		obj = DirectoryManager.getObjectInstance(null, new CompositeName(
				"compositename"), null, null, a);
		assertSame(OBJ, obj);

		TestNamingManager.MockContext cxt = new TestNamingManager.MockContext(
				null);

		obj = DirectoryManager.getObjectInstance(null, null, cxt, null, a);
		assertSame(OBJ, obj);

		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		obj = DirectoryManager.getObjectInstance(null, null, null, env, a);

		assertSame(OBJ, obj);
	}

	/**
	 * Test the behavior when factory builder throws NullPointerException.
	 */
	public void myTestGetObjectInstance_HasBuilder_BuilderNullPointerException()
			throws Exception {
		log
				.setMethod("myTestGetObjectInstance_HasBuilder_BuilderNullPointerException");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateNullPointerException(env, 1);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();
		try {
			Object obj = DirectoryManager.getObjectInstance(null, null, null,
					env, a);
			fail("Should throw NullPointerException.");
		} catch (NullPointerException e) {
			// log.log(e);
		}
	}

	/**
	 * Test the behavior when factory builder throws NamingException.
	 */
	public void myTestGetObjectInstance_HasBuilder_BuilderNamingException()
			throws Exception {
		log
				.setMethod("myTestGetObjectInstance_HasBuilder_BuilderNamingException");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateNamingException(env, 1);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();
		try {
			Object obj = DirectoryManager.getObjectInstance(null, null, null,
					env, a);
			fail("Should throw NamingException.");
		} catch (NamingException e) {
			// log.log(e);
		}
	}

	/**
	 * Test the behavior when factory throws RuntimeException.
	 */
	public void myTestGetObjectInstance_HasBuilder_FactoryRuntimeException()
			throws Exception {
		log
				.setMethod("myTestGetObjectInstance_HasBuilder_FactoryRuntimeException");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateRuntimeException(env, 2);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();
		try {
			Object obj = DirectoryManager.getObjectInstance(null, null, null,
					env, a);
			fail("Should throw RuntimeException.");
		} catch (RuntimeException e) {
			// log.log(e);
		}
	}

	/**
	 * Test the behavior when factory throws NamingException.
	 */
	public void myTestGetObjectInstance_HasBuilder_FactoryNamingException()
			throws Exception {
		log
				.setMethod("myTestGetObjectInstance_HasBuilder_FactoryNamingException");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateNamingException(env, 2);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();
		try {
			Object obj = DirectoryManager.getObjectInstance(null, null, null,
					env, a);
			fail("Should throw NamingException.");
		} catch (NamingException e) {
		}
	}

	/**
	 * Test the behavior when factory builder is set but the factory builder
	 * returns null.
	 */
	public void myTestGetObjectInstance_HasBuilder_BuilderReturnNull()
			throws Exception {
		log.setMethod("myTestGetObjectInstance_HasBuilder_BuilderReturnNull");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateReturnNull(env, 1);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();
		try {
			Object obj = DirectoryManager.getObjectInstance(null, null, null,
					env, a);
			fail("Should throw NullPointerException.");
		} catch (NullPointerException e) {
		}
	}

	/**
	 * Test the behavior when factory builder is set but the factory returns
	 * null.
	 */
	public void myTestGetObjectInstance_HasBuilder_FactoryReturnNull()
			throws Exception {
		log.setMethod("myTestGetObjectInstance_HasBuilder_FactoryReturnNull");
		Hashtable env = new Hashtable();
		TestNamingManager.indicateReturnNull(env, 2);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"dazzle.jndi.testing.spi.DazzleContextFactory");
		env.put(Context.STATE_FACTORIES,
				"tests.api.javax.naming.mock.MockDirObjectFactoryNoException");
		Attributes a = new BasicAttributes();

		Object obj = DirectoryManager.getObjectInstance("string", null, null,
				env, a);
		assertNull(obj);
	}

	/**
	 * Set the object factory builder to a mock instance.
	 * 
	 */
	public void myTestSetObjectFactoryBuilder_SetNormal()
			throws NamingException {
		log.setMethod("myTestSetObjectFactoryBuilder_SetNormal");
		ObjectFactoryBuilder objectFactoryBuilder = MockObjectFactoryBuilder
				.getInstance();
		DirectoryManager.setObjectFactoryBuilder(objectFactoryBuilder);
		// NamingManager.setObjectFactoryBuilder(objectFactoryBuilder);
	}

	public void testSetObjectFactoryBuilder_AfterSet() {

		// myTestSetObjectFactoryBuilder_SetNormal();
		invokeMyTestMethod("myTestSetObjectFactoryBuilder_SetNormal");

		// myTestGetObjectInstance_HasBuilder_Normal();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_Normal");
		// myTestGetObjectInstance_HasBuilder_BuilderNullPointerException();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_BuilderNullPointerException");
		// myTestGetObjectInstance_HasBuilder_BuilderNamingException();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_BuilderNamingException");
		// myTestGetObjectInstance_HasBuilder_FactoryRuntimeException();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_FactoryRuntimeException");
		// myTestGetObjectInstance_HasBuilder_FactoryNamingException();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_FactoryNamingException");
		// myTestGetObjectInstance_HasBuilder_BuilderReturnNull();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_BuilderReturnNull");
		// myTestGetObjectInstance_HasBuilder_FactoryReturnNull();
		invokeMyTestMethod("myTestGetObjectInstance_HasBuilder_FactoryReturnNull");

	}

	public static class MockObjectFactoryBuilder implements
			ObjectFactoryBuilder {

		private static MockObjectFactoryBuilder _builder = new MockObjectFactoryBuilder();

		public static MockObjectFactoryBuilder getInstance() {
			return _builder;
		}

		public ObjectFactory createObjectFactory(Object o, Hashtable envmt)
				throws NamingException {
			TestNamingManager.issueIndicatedExceptions(envmt);
			if (TestNamingManager.returnNullIndicated(envmt)) {
				return null;
			}
			return new MockDirObjectFactory();
		}
	}

	public static class MockDirObjectFactory implements DirObjectFactory {

		public Object getObjectInstance(Object o, Name n, Context c,
				Hashtable envmt, Attributes a) throws Exception {
			TestNamingManager.issueIndicatedExceptions(envmt);
			if (TestNamingManager.returnNullIndicated(envmt)) {
				return null;
			}
			return OBJ;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
		 *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
		 */
		public Object getObjectInstance(Object o, Name n, Context c,
				Hashtable envmt) throws Exception {
			log.setMethod("getObjectInstance");
			log.log("wrong method call");
			return getObjectInstance(o, n, c, envmt, null);
		}

	}
}
