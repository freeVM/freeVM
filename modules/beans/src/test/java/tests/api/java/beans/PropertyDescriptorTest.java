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

package tests.api.java.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Unit test for PropertyDescriptor.
 */
public class PropertyDescriptorTest extends TestCase {

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

	public void testEquals() throws IntrospectionException, SecurityException,
			NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);

		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);

		pd.setName("different name");
		assertTrue(pd.equals(pd2));
		assertTrue(pd.equals(pd));
		assertTrue(pd2.equals(pd));
		assertFalse(pd.equals(null));
	}

	public void testEquals_ReadMethod() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, null);

		String propertyName2 = "PropertyThree";
		Method readMethod2 = beanClass.getMethod("get" + propertyName2, null);
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2,
				readMethod2, null);

		assertFalse(pd.equals(pd2));
	}

	public void testEquals_ReadMethod_Null() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = null;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, null);

		String propertyName2 = "PropertyThree";
		Method readMethod2 = beanClass.getMethod("get" + propertyName2, null);
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2,
				readMethod2, null);

		assertFalse(pd.equals(pd2));
	}

	public void testEquals_ReadMethod_Null_Null()
			throws IntrospectionException, SecurityException,
			NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = null;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, null);

		String propertyName2 = "PropertyThree";
		Method readMethod2 = null;
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2,
				readMethod2, null);

		assertTrue(pd.equals(pd2));
	}

	public void testEquals_WriteMethod() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null,
				writeMethod);

		String propertyName2 = "PropertyThree";
		Method writeMethod2 = beanClass.getMethod("set" + propertyName2,
				new Class[] { String.class });
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2, null,
				writeMethod2);

		assertFalse(pd.equals(pd2));
	}

	public void testEquals_WriteMethod_Null() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method writeMethod = null;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null,
				writeMethod);

		String propertyName2 = "PropertyThree";
		Method writeMethod2 = beanClass.getMethod("set" + propertyName2,
				new Class[] { String.class });
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2, null,
				writeMethod2);

		assertFalse(pd.equals(pd2));
	}

	public void testEquals_Bound() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		String propertyName2 = "PropertyThree";
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2, null,
				null);
		pd.setBound(true);
		assertFalse(pd.equals(pd2));
	}

	public void testEquals_Contrained() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		String propertyName2 = "PropertyThree";
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2, null,
				null);
		pd.setConstrained(true);
		assertFalse(pd.equals(pd2));
	}

	public void testEquals_PropertyEditorClass() throws IntrospectionException,
			SecurityException, NoSuchMethodException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		String propertyName2 = "PropertyThree";
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName2, null,
				null);

		pd.setPropertyEditorClass(String.class);
		assertFalse(pd.equals(pd2));
	}

	public void testEquals_PropertyType() throws IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);

		Class beanClass2 = PropertyDescriptorTest.MockBeanPropertyDesc.class;
		PropertyDescriptor pd2 = new PropertyDescriptor(propertyName,
				beanClass2);
		assertFalse(pd.equals(pd2));
	}

	/*
	 * Class under test for void PropertyDescriptor(String, Class)
	 */
	public void testPropertyDescriptorStringClass()
			throws IntrospectionException {
		String propertyName = "propertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);

		String capitalName = propertyName.substring(0, 1).toUpperCase()
				+ propertyName.substring(1);
		assertEquals(String.class, pd.getPropertyType());
		assertEquals("get" + capitalName, pd.getReadMethod().getName());
		assertEquals("set" + capitalName, pd.getWriteMethod().getName());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringClass_PropertyNameCapital()
			throws IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);
		assertEquals(propertyName, pd.getName());
	}

	public void testPropertyDescriptorStringClass_PropertyNameEmpty()
			throws IntrospectionException {
		String propertyName = "";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException exception) {
		}
	}

	public void testPropertyDescriptorStringClass_PropertyNameNull() {
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(null, beanClass);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException exception) {
		}
	}

	public void testPropertyDescriptorStringClass_BeanClassNull()
			throws IntrospectionException {
		String propertyName = "propertyOne";
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName, null);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException exception) {
		}
	}

	public void testPropertyDescriptorStringClass_PropertyNameInvalid() {
		String propertyName = "not a property name";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException exception) {
		}
	}

	public void testPropertyDescriptorStringClass_ProtectedGetter() {
		String propertyName = "protectedProp";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException exception) {
		}
	}

	/*
	 * Class under test for void PropertyDescriptor(String, Class, String,
	 * String)
	 */
	public void testPropertyDescriptorStringClassStringString()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass,
				"get" + propertyName, "set" + propertyName);

		assertEquals(Integer.class, pd.getPropertyType());
		assertEquals("get" + propertyName, pd.getReadMethod().getName());
		assertEquals("set" + propertyName, pd.getWriteMethod().getName());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringClassStringString_PropertyNameNull() {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(null, beanClass,
					"get" + propertyName, "set" + propertyName);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	public void testPropertyDescriptorStringClassStringString_BeanClassNull()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = null;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass, "get" + propertyName, "set" + propertyName);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	public void testPropertyDescriptorStringClassStringString_ReadMethodNull()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass,
				null, "set" + propertyName);

		assertEquals(Integer.class, pd.getPropertyType());
		assertNull(pd.getReadMethod());
		assertEquals("set" + propertyName, pd.getWriteMethod().getName());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringClassStringString_ReadMethodInvalid()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass, "getXX", "set" + propertyName);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	public void testPropertyDescriptorStringClassStringString_WriteMethodNull()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass,
				"get" + propertyName, null);

		assertEquals(Integer.class, pd.getPropertyType());
		assertEquals("get" + propertyName, pd.getReadMethod().getName());
		assertNull(pd.getWriteMethod());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringClassStringString_WriteMethodEmpty()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass, "get" + propertyName, "");
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}

	}

	public void testPropertyDescriptorStringClassStringString_WriteMethodInvalid()
			throws IntrospectionException {
		String propertyName = "PropertyTwo";
		Class beanClass = MockJavaBean.class;
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					beanClass, "get" + propertyName, "setXXX");
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}

	}

	/*
	 * Class under test for void PropertyDescriptor(String, Method, Method)
	 */
	public void testPropertyDescriptorStringMethodMethod()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);

		assertEquals(String.class, pd.getPropertyType());
		assertEquals("get" + propertyName, pd.getReadMethod().getName());
		assertEquals("set" + propertyName, pd.getWriteMethod().getName());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringMethodMethod_PropertyNameNull()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		try {
			PropertyDescriptor pd = new PropertyDescriptor(null, readMethod,
					writeMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}

	}

	public void testPropertyDescriptorStringMethodMethod_ReadMethodNull()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = null;
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);

		assertEquals(String.class, pd.getPropertyType());
		assertNull(pd.getReadMethod());
		assertEquals("set" + propertyName, pd.getWriteMethod().getName());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringMethodMethod_ReadMethodInvalid()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		String anotherProp = "PropertyTwo";

		Method readMethod = beanClass.getMethod("get" + anotherProp, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					readMethod, writeMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}

	}

	public void testPropertyDescriptorStringMethodMethod_WriteMethodNull()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = null;
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);

		assertEquals(String.class, pd.getPropertyType());
		assertEquals("get" + propertyName, pd.getReadMethod().getName());
		assertNull(pd.getWriteMethod());

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testPropertyDescriptorStringMethodMethod_WriteMethodInvalid()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		String propertyName = "PropertyOne";
		Class beanClass = MockJavaBean.class;
		String anotherProp = "PropertyTwo";

		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + anotherProp,
				new Class[] { Integer.class });
		try {
			PropertyDescriptor pd = new PropertyDescriptor(propertyName,
					readMethod, writeMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}

	}

	/**
	 * @throws Exception
	 */
	public void testPropertyDescriptorStringClassMethodMethod_SubClass()
			throws Exception {
		try {
			PropertyDescriptor pd = new PropertyDescriptor(
					"prop1", SubMockJavaBean.class, null, "setPropertyOne"); //$NON-NLS-1$ //$NON-NLS-2$
			assertNull(pd.getReadMethod());
			assertEquals(pd.getWriteMethod().getName(), "setPropertyOne"); //$NON-NLS-1$			

			pd = new PropertyDescriptor(
					"prop1", SubMockJavaBean.class, "getPropertyOne", "setPropertyOne"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			assertEquals(pd.getReadMethod().getName(), "getPropertyOne"); //$NON-NLS-1$
			assertEquals(pd.getWriteMethod().getName(), "setPropertyOne"); //$NON-NLS-1$			

			pd = new PropertyDescriptor(
					"prop1", SubMockJavaBean.class, "getPropertyOne", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			assertEquals(pd.getReadMethod().getName(), "getPropertyOne"); //$NON-NLS-1$
			assertNull(pd.getWriteMethod()); //$NON-NLS-1$		
		} catch (Exception e) {
			fail("Throw " + e.getClass().getName()); //$NON-NLS-1$
		}
	}

	public void testSetReadMethod() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getReadMethod());
		pd.setReadMethod(readMethod);
		assertSame(readMethod, pd.getReadMethod());
	}

	public void testSetReadMethod_Null() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, null);

		assertSame(readMethod, pd.getReadMethod());
		pd.setReadMethod(null);
		assertNull(pd.getReadMethod());
	}

	/**
	 * Read method is incompatible with property name getPropertyTwo vs.
	 * PropertyOne (writeMethod=null)
	 */
	public void testSetReadMethod_Invalid() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + "PropertyTwo", null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getReadMethod());
		pd.setReadMethod(readMethod);
		assertSame(readMethod, pd.getReadMethod());
	}

	/**
	 * String invalidGetMethod(String arg)
	 */
	public void testSetReadMethod_Invalid_withArg() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("invalidGetMethod",
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getReadMethod());
		try {
			pd.setReadMethod(readMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	/**
	 * String invalidGetMethod(String arg)
	 */
	public void testSetReadMethod_Invalid_returnVoid()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("invalidGetMethod", null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getReadMethod());
		try {
			pd.setReadMethod(readMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	/**
	 * Read method is incompatible with write method getPropertyOn vs.
	 * setPropertyTow
	 */
	public void testSetReadMethod_ReadWriteIncompatible()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + "PropertyOne", null);
		Method writeMethod = beanClass.getMethod("set" + "PropertyTwo",
				new Class[] { Integer.class });

		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null,
				writeMethod);

		assertNull(pd.getReadMethod());
		try {
			pd.setReadMethod(readMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	/**
	 * normal input
	 */
	public void testSetWriteMethod() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getWriteMethod());
		pd.setWriteMethod(writeMethod);
		assertSame(writeMethod, pd.getWriteMethod());
	}

	/**
	 * setWriteMethod(null)
	 */
	public void testSetWriteMethod_Null() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null,
				writeMethod);

		assertSame(writeMethod, pd.getWriteMethod());
		pd.setWriteMethod(null);
		assertNull(pd.getWriteMethod());
	}

	/**
	 * write method is incompatible with property name (read method is null)
	 */
	public void testSetWriteMethod_Invalid() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method writeMethod = beanClass.getMethod("set" + "PropertyTwo",
				new Class[] { Integer.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getWriteMethod());
		pd.setWriteMethod(writeMethod);
		assertSame(writeMethod, pd.getWriteMethod());
	}

	/**
	 * write method without argument
	 */
	public void testSetWriteMethod_Invalid_NoArgs() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method writeMethod = beanClass.getMethod("get" + "PropertyTwo", null);
		PropertyDescriptor pd = new PropertyDescriptor(propertyName, null, null);

		assertNull(pd.getWriteMethod());
		try {
			pd.setWriteMethod(writeMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	/**
	 * write method is incompatible with read method
	 */
	public void testSetWriteMethod_WriteReadIncompatible()
			throws SecurityException, NoSuchMethodException,
			IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + "PropertyTwo", null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, null);

		assertNull(pd.getWriteMethod());
		try {
			pd.setWriteMethod(writeMethod);
			fail("Should throw IntrospectionException.");
		} catch (IntrospectionException e) {
		}
	}

	public void testSetBound_true() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);
		pd.setBound(true);

		assertTrue(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testSetBound_false() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);
		pd.setBound(false);

		assertFalse(pd.isBound());
		assertFalse(pd.isConstrained());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testSetConstrained_true() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);
		pd.setConstrained(true);

		assertTrue(pd.isConstrained());
		assertFalse(pd.isBound());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	public void testSetConstrained_false() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);
		pd.setConstrained(false);

		assertFalse(pd.isConstrained());
		assertFalse(pd.isBound());
		assertNull(pd.getPropertyEditorClass());

		assertEquals(propertyName, pd.getDisplayName());
		assertEquals(propertyName, pd.getName());
		assertEquals(propertyName, pd.getShortDescription());

		assertNotNull(pd.attributeNames());

		assertFalse(pd.isExpert());
		assertFalse(pd.isHidden());
		assertFalse(pd.isPreferred());
	}

	/**
	 * PropertyDescriptor does not ensure if the editor class is valid or not.
	 */
	public void testSetPropertyEditorClass() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);
		Class editorClass = "This is an invalid editor class".getClass();
		pd.setPropertyEditorClass(editorClass);
		assertSame(editorClass, pd.getPropertyEditorClass());
	}

	public void testSetPropertyEditorClass_null() throws SecurityException,
			NoSuchMethodException, IntrospectionException {
		Class beanClass = MockJavaBean.class;
		String propertyName = "PropertyOne";
		Method readMethod = beanClass.getMethod("get" + propertyName, null);
		Method writeMethod = beanClass.getMethod("set" + propertyName,
				new Class[] { String.class });
		PropertyDescriptor pd = new PropertyDescriptor(propertyName,
				readMethod, writeMethod);

		pd.setPropertyEditorClass(null);
		assertNull(pd.getPropertyEditorClass());
	}

	public void testConstructor_1() throws IntrospectionException {
		PropertyDescriptor pd = new PropertyDescriptor("fox01", FakeFox01.class);
	}

	static class FakeFox01 {
		public String getFox01() {
			return null;
		}

		public void setFox01(String value) {

		}

	}

	class MockBeanPropertyDesc implements Serializable {
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;

		Integer propertyOne;

		/**
		 * @return Returns the propertyOne.
		 */
		public Integer getPropertyOne() {
			return propertyOne;
		}

		/**
		 * @param propertyOne
		 *            The propertyOne to set.
		 */
		public void setPropertyOne(Integer propertyOne) {
			this.propertyOne = propertyOne;
		}
	}

	class SubMockJavaBean extends MockJavaBean {
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 7423254295680570566L;
		//
	}
}
