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

import java.io.NotSerializableException;
import java.util.prefs.BackingStoreException;

import junit.framework.TestCase;
import tests.util.SerializationTester;

/**
 * 
 * 
 */
public class BackingStoreExceptionTest extends TestCase {

	/*
	 * Class under test for void BackingStoreException(String)
	 */
	public void testBackingStoreExceptionString() {
		BackingStoreException e = new BackingStoreException("msg");
		assertNull(e.getCause());
		assertEquals("msg", e.getMessage());
	}

	/*
	 * Class under test for void BackingStoreException(Throwable)
	 */
	public void testBackingStoreExceptionThrowable() {
		Throwable t = new Throwable("msg");
		BackingStoreException e = new BackingStoreException(t);
		assertTrue(e.getMessage().indexOf(t.getClass().getName()) >= 0);
		assertTrue(e.getMessage().indexOf("msg") >= 0);
		assertEquals(t, e.getCause());
	}

	public void testSerialization() throws Exception {
		try {
			SerializationTester.writeObject(new BackingStoreException("msg"),
					"test.txt");
			fail();
		} catch (NotSerializableException e) {
		}
	}

}

