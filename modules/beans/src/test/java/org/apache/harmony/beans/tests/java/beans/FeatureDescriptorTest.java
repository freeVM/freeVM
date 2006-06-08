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

package org.apache.harmony.beans.tests.java.beans;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.TestCase;

/**
 * Unit test for FeatureDescriptor.
 */
public class FeatureDescriptorTest extends TestCase {

	private FeatureDescriptor fd;

	protected void setUp() throws Exception {
		super.setUp();
		fd = new FeatureDescriptor();
	}

	public void testFeatureDescriptor() {
		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetDisplayName() {
		String displayName = "FeatureDescriptor.displayName";
		fd.setDisplayName(displayName);
		assertSame(displayName, fd.getDisplayName());
		assertNull(fd.getName());
		assertSame(displayName, fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetDisplayName_DisplayNameNull() {
		String displayName = null;
		fd.setDisplayName(displayName);
		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetExpert_False() {
		fd.setExpert(false);
		assertFalse(fd.isExpert());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetExpert_True() {
		fd.setExpert(true);
		assertTrue(fd.isExpert());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetHidden_False() {
		fd.setHidden(false);
		assertFalse(fd.isHidden());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isPreferred());
	}

	public void testSetHidden_True() {
		fd.setHidden(true);
		assertTrue(fd.isHidden());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isPreferred());
	}

	public void testSetName() {
		String name = "FeatureDescriptor.name";
		fd.setName(name);
		assertSame(name, fd.getName());

		assertSame(name, fd.getDisplayName());
		assertSame(name, fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetName_null() {
		fd.setName("FeatureDescriptor.name");
		fd.setName(null);
		assertNull(fd.getName());

		assertNull(fd.getDisplayName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetPreferred_False() {
		fd.setPreferred(false);
		assertFalse(fd.isPreferred());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
	}

	public void testSetPreferred_True() {
		fd.setPreferred(true);
		assertTrue(fd.isPreferred());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());
		assertNull(fd.getShortDescription());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
	}

	public void testSetShortDescription() {
		String shortDesc = "FeatureDescriptor.ShortDescription";
		fd.setShortDescription(shortDesc);
		assertSame(shortDesc, fd.getShortDescription());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetShortDescription_ShortDescNull() {
		String shortDesc = "FeatureDescriptor.ShortDescription";
		fd.setShortDescription(shortDesc);
		assertSame(shortDesc, fd.getShortDescription());
		fd.setShortDescription(null);
		assertNull(fd.getShortDescription());

		assertNull(fd.getDisplayName());
		assertNull(fd.getName());

		assertNotNull(fd.attributeNames());

		assertFalse(fd.isExpert());
		assertFalse(fd.isHidden());
		assertFalse(fd.isPreferred());
	}

	public void testSetValue() {
		String[] attributeNames = { "Blue", "Yellow", "Red", };
		Object[] values = { "Blue.value", "Yellow.value", "Red.value", };
		for (int i = 0; i < attributeNames.length; i++) {
			fd.setValue(attributeNames[i], values[i]);
		}

		for (int i = 0; i < attributeNames.length; i++) {
			assertSame(values[i], fd.getValue(attributeNames[i]));
		}
	}

	public void testSetValue_ExistAttribute() {
		String attributeName = "blue";
		Object value = "Anyone";
		fd.setValue(attributeName, value);
		assertSame(value, fd.getValue(attributeName));

		Object newValue = "Another";
		fd.setValue(attributeName, newValue);
		assertSame(newValue, fd.getValue(attributeName));
	}

	public void testSetValue_ValueNull() {
		String attributeName = "blue";
		Object value = "Anyone";
		fd.setValue(attributeName, value);
		assertSame(value, fd.getValue(attributeName));

		Object newValue = null;
		try {
			fd.setValue(attributeName, newValue);
			fail("Should throw NullPointerException.");
		} catch (NullPointerException e) {
		}
	}

	public void testattributeNames() {
		assertFalse(fd.attributeNames().hasMoreElements());

		String[] attributeNames = { "Blue", "Yellow", "Red", };
		Object[] values = { "Blue.value", "Yellow.value", "Red.value", };
		for (int i = 0; i < attributeNames.length; i++) {
			fd.setValue(attributeNames[i], values[i]);
		}
		Enumeration names = fd.attributeNames();
		Hashtable table = new Hashtable();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			table.put(name, fd.getValue(name));
		}

		assertEquals(attributeNames.length, table.size());
		for (int i = 0; i < attributeNames.length; i++) {
			assertTrue(table.containsKey(attributeNames[i]));
		}
	}
}
