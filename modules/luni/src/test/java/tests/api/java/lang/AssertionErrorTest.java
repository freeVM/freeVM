/* Copyright 1998, 2005 The Apache Software Foundation or its licensors, as applicable
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
package tests.api.java.lang;

public class AssertionErrorTest extends junit.framework.TestCase {

	/**
	 * @tests java.lang.AssertionError#AssertionError(java.lang.Object)
	 */
	public void test_ObjectConstructor() {
		AssertionError error = new AssertionError(new String("hi"));
		assertTrue("non-null cause", error.getCause() == null);
		assertTrue(error.getMessage().equals("hi"));
		Exception exc = new NullPointerException();
		error = new AssertionError(exc);
		assertTrue("non-null cause", error.getCause() == exc);
		assertTrue(error.getMessage().equals(exc.toString()));
	}
}