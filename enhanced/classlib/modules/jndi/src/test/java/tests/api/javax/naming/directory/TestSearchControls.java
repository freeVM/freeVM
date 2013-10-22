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
package tests.api.javax.naming.directory;

import java.util.Arrays;

import javax.naming.directory.SearchControls;

import tests.api.javax.naming.util.Log;
import junit.framework.TestCase;

public class TestSearchControls extends TestCase {

	static Log log = new Log(TestSearchControls.class);

	/**
	 * Constructor for TestSearchControls.
	 * 
	 * @param arg0
	 */
	public TestSearchControls(String arg0) {
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test for void SearchControls()
	 */
	public void testSearchControls() {
		log.setMethod("testSearchControls()");
		SearchControls ctrl;

		ctrl = new SearchControls();
		assertEquals(SearchControls.ONELEVEL_SCOPE, ctrl.getSearchScope());
		assertEquals(0, ctrl.getCountLimit());
		assertEquals(0, ctrl.getTimeLimit());
		assertEquals(null, ctrl.getReturningAttributes());
		assertEquals(false, ctrl.getDerefLinkFlag());
		assertEquals(false, ctrl.getReturningObjFlag());
	}

	/*
	 * Test for void SearchControls(int, long, int, String[], boolean, boolean)
	 */
	public void testSearchControls_Full() {
		log.setMethod("testSearchControls_Full()");
		SearchControls ctrl;

		ctrl = new SearchControls(SearchControls.OBJECT_SCOPE, 100, 200,
				new String[] { "id1", "id2" }, true, true);
		assertEquals(SearchControls.OBJECT_SCOPE, ctrl.getSearchScope());
		assertEquals(100, ctrl.getCountLimit());
		assertEquals(200, ctrl.getTimeLimit());
		assertTrue(Arrays.equals(new String[] { "id1", "id2" }, ctrl
				.getReturningAttributes()));
		assertEquals(true, ctrl.getDerefLinkFlag());
		assertEquals(true, ctrl.getReturningObjFlag());
	}

	public void testSearchControls_Illegal_Scope() {
		log.setMethod("testSearchControls_Illegal_Scope()");
		SearchControls ctrl;

		ctrl = new SearchControls(-1, 100, 200, new String[] { "id1", "id2" },
				true, true);

	}

	public void testSearchControls_Illegal_CountLimit() {
		log.setMethod("testSearchControls_Illegal_CountLimit()");
		SearchControls ctrl;

		ctrl = new SearchControls(SearchControls.OBJECT_SCOPE, -1, 200,
				new String[] { "id1", "id2" }, true, true);

	}

	public void testSearchControls_Illegal_TimeLimit() {
		log.setMethod("testSearchControls_Illegal_TimeLimit()");
		SearchControls ctrl;

		ctrl = new SearchControls(SearchControls.OBJECT_SCOPE, 100, -1,
				new String[] { "id1", "id2" }, true, true);

	}

	/*
	 * Test for getter and setter methods
	 */
	public void testGetterAndSetter() {
		log.setMethod("testGetterAndSetter()");
		SearchControls ctrl;

		ctrl = new SearchControls();
		ctrl.setReturningAttributes(new String[] { "id1", "id2" });
		ctrl.setCountLimit(100);
		ctrl.setDerefLinkFlag(true);
		ctrl.setReturningObjFlag(true);
		ctrl.setSearchScope(SearchControls.OBJECT_SCOPE);
		ctrl.setTimeLimit(200);
		assertEquals(SearchControls.OBJECT_SCOPE, ctrl.getSearchScope());
		assertEquals(100, ctrl.getCountLimit());
		assertEquals(200, ctrl.getTimeLimit());
		assertTrue(Arrays.equals(new String[] { "id1", "id2" }, ctrl
				.getReturningAttributes()));
		assertEquals(true, ctrl.getDerefLinkFlag());
		assertEquals(true, ctrl.getReturningObjFlag());
	}

	/*
	 * Test for String toString()
	 */
	public void testToString() {
		log.setMethod("testToString()");
		SearchControls ctrl;

		ctrl = new SearchControls();
		assertTrue(ctrl.toString() != null);
	}

}
