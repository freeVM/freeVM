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

import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.apache.harmony.beans.tests.support.mock.MockJavaBean;

import junit.framework.TestCase;

/**
 * Unit test for IndexedPropertyDescriptor.
 */
public class IndexedPropertyDescriptorTest extends TestCase {

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

    public void testEquals() throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertTrue(ipd.equals(ipd2));
        assertTrue(ipd.equals(ipd));
        assertTrue(ipd2.equals(ipd));
        assertFalse(ipd.equals(null));
    }

    /*
     * Read method
     */
    public void testEquals_ReadMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("getPropertyFive",
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * read method null.
     */
    public void testEquals_ReadMethodNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = null;
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    public void testEquals_WriteMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("setPropertyFive",
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * write method null.
     */
    public void testEquals_WriteMethodNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = null;
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed read method.
     */
    public void testEquals_IndexedR() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("getPropertyFive",
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed read method null.
     */
    public void testEquals_IndexedRNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = null;
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * indexed write method.
     */
    public void testEquals_IndexedW() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("setPropertyFive",
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed write method null.
     */
    public void testEquals_IndexWNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = null;

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Property Type.
     */
    public void testEquals_PropertyType() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                "PropertySix", beanClass);
        assertFalse(ipd.getPropertyType().equals(ipd2.getPropertyType()));
        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Class)
     */
    public void testIndexedPropertyDescriptorStringClass()
            throws IntrospectionException, SecurityException,
            NoSuchMethodException {
        String propertyName = "propertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        String capitalName = propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
        Method readMethod = beanClass.getMethod("get" + capitalName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + capitalName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + capitalName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + capitalName,
                new Class[] { Integer.TYPE, String.class });

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());
        assertNull(ipd.getPropertyEditorClass());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameNull()
            throws IntrospectionException {
        String propertyName = null;
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameEmpty()
            throws IntrospectionException {
        String propertyName = "";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameInvalid()
            throws IntrospectionException {
        String propertyName = "Not a property";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClass_NotIndexedProperty()
            throws IntrospectionException {
        String propertyName = "propertyOne";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClass_ClassNull()
            throws IntrospectionException {
        String propertyName = "propertyFour";
        Class beanClass = null;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    /*
     * bean class does not implements java.io.Serializable
     */
    public void testIndexedPropertyDescriptorStringClass_NotBeanClass()
            throws IntrospectionException {
        String propertyName = "propertyOne";
        Class beanClass = NotJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass);
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Class,
     * String, String, String, String)
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString()
            throws IntrospectionException, SecurityException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + propertyName);

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());
        assertNull(ipd.getPropertyEditorClass());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());

    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(null, beanClass,
                    "get" + propertyName, "set" + propertyName, "get"
                            + propertyName, "set" + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propEmpty() {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor("", beanClass, "get" + propertyName,
                    "set" + propertyName, "get" + propertyName, "set"
                            + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propInvalid()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String invalidProp = "Not a prop";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                invalidProp, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + propertyName);
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(invalidProp, ipd.getName());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_BeanClassNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = null;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "set" + propertyName, "get" + propertyName,
                    "set" + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_ReadMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, "set" + propertyName, "get"
                        + propertyName, "set" + propertyName);
        assertNull(ipd.getReadMethod());
        assertNotNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_WriteMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, null, "get"
                        + propertyName, "set" + propertyName);
        assertNotNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedReadMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, null, "set" + propertyName);
        assertNull(ipd.getIndexedReadMethod());
        assertNotNull(ipd.getIndexedWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedWriteMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, null);
        assertNotNull(ipd.getIndexedReadMethod());
        assertNull(ipd.getIndexedWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    /**
     * indexed read/write null
     * 
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RWNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, null, "get" + propertyName,
                "set" + propertyName);
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertNull(ipd.getPropertyType());
    }

    /**
     * indexed read/write null
     * 
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedRWNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "set" + propertyName, null, null);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    /**
     * index read /read null
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, "set" + propertyName, null,
                "set" + propertyName);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertNotNull(ipd.getWriteMethod());
        assertNotNull(ipd.getIndexedWriteMethod());
    }

    /**
     * index write /write null
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_WNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, null, "get"
                        + propertyName, null);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertNotNull(ipd.getReadMethod());
        assertNotNull(ipd.getIndexedReadMethod());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_allNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, null, null, null);
        assertEquals(null, ipd.getIndexedPropertyType());
        assertEquals(null, ipd.getPropertyType());
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getIndexedReadMethod());
    }

    /*
     * read/write incompatible
     * 
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RWIncompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + anotherProp, "get" + propertyName, "set"
                        + propertyName);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getWriteMethod().getName());
    }

    /**
     * IndexedRead/IndexedWrite incompatible
     * 
     * @throws IntrospectionException
     * 
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedRWIncompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + anotherProp);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getIndexedWriteMethod().getName());
    }

    /*
     * ReadMethod/IndexedReadMethod incompatible
     * 
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RIndexedRcompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + anotherProp, "set"
                        + anotherProp);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getIndexedWriteMethod().getName());
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Method,
     * Method, Method, Method)
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());
        assertNull(ipd.getPropertyEditorClass());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());
    }

    /*
     * propertyName=null
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        try {
            new IndexedPropertyDescriptor(null, readMethod, writeMethod,
                    indexedReadMethod, indexedWriteMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    /*
     * propertyname="";
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propEmpty()
            throws SecurityException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        try {
            new IndexedPropertyDescriptor("", readMethod, writeMethod,
                    indexedReadMethod, indexedWriteMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propInvalid()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String invalidName = "An Invalid Property name";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                invalidName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(invalidName, ipd.getName());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_ReadMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertNull(ipd.getReadMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_WriteMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, null, indexedReadMethod,
                indexedWriteMethod);
        assertNull(ipd.getWriteMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedReadMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, null, indexedWriteMethod);
        assertNull(ipd.getIndexedReadMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedWriteMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedRWNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        try {
            new IndexedPropertyDescriptor(propertyName, readMethod,
                    writeMethod, null, null);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}

    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_RWNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, null, indexedReadMethod, indexedWriteMethod);

        assertNull(ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    /*
     * read/write incompatible
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_RWIncompatible()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + anotherProp,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(propertyName, ipd.getName());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    /*
     * IndexedRead/IndexedWrite incompatible
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedRWIncompatible()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + anotherProp,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(propertyName, ipd.getName());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    public void testSetIndexedReadMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, null, indexedWriteMethod);
        assertNull(ipd.getIndexedReadMethod());
        ipd.setIndexedReadMethod(indexedReadMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
    }

    public void testSetIndexedReadMethod_invalid() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, null, indexedReadMethod, indexedWriteMethod);
        Method indexedReadMethod2 = beanClass.getMethod("getPropertySix",
                new Class[] { Integer.TYPE });
        try {
            ipd.setIndexedReadMethod(indexedReadMethod2);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {

        }
    }

    public void testSetIndexedReadMethod_null() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        ipd.setIndexedReadMethod(null);
        assertNull(ipd.getIndexedReadMethod());
    }

    /*
     * indexed read method without args
     */
    public void testSetIndexedReadMethod_RInvalidArgs()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        try {
            ipd.setIndexedReadMethod(readMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    /*
     * indexed read method with invalid arg type (!Integer.TYPE)
     */
    public void testSetIndexedReadMethod_RInvalidArgType()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        try {
            ipd.setIndexedReadMethod(writeMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    /*
     * indexed read method with void return.
     */
    public void testSetIndexedReadMethod_RInvalidReturn()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        Method voidMethod = beanClass.getMethod("getPropertyFourInvalid",
                new Class[] { Integer.TYPE });
        try {
            ipd.setIndexedReadMethod(voidMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    public void testSetIndexedWriteMethod_null() throws IntrospectionException,
            NoSuchMethodException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedWriteMethod, ipd.getIndexedWriteMethod());
        ipd.setIndexedWriteMethod(null);
        assertNull(ipd.getIndexedWriteMethod());
    }

    public void testSetIndexedWriteMethod() throws IntrospectionException,
            NoSuchMethodException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        ipd.setIndexedWriteMethod(indexedWriteMethod);
        assertSame(indexedWriteMethod, ipd.getIndexedWriteMethod());
    }

    /*
     * bad arg count
     */
    public void testSetIndexedWriteMethod_noargs()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        try {
            ipd.setIndexedWriteMethod(indexedReadMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {}
    }

    /*
     * bad arg type
     */
    public void testSetIndexedWriteMethod_badargtype()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, Integer.TYPE });
        try {
            ipd.setIndexedWriteMethod(badArgType);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    public void testSetIndexedWriteMethod_return()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("setPropertyFourInvalid",
                new Class[] { Integer.TYPE, String.class });
        ipd.setIndexedWriteMethod(badArgType);

        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(Integer.TYPE, ipd.getIndexedWriteMethod().getReturnType());
    }

    public void testSetIndexedWriteMethod_InvalidIndexType()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("setPropertyFourInvalid2",
                new Class[] { String.class, String.class });
        try {
            ipd.setIndexedWriteMethod(badArgType);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {}
    }

    class NotJavaBean {

        private String[] propertyOne;

        /**
         * @return Returns the propertyOne.
         */
        public String[] getPropertyOne() {
            return propertyOne;
        }

        /**
         * @param propertyOne
         *            The propertyOne to set.
         */
        public void setPropertyOne(String[] propertyOne) {
            this.propertyOne = propertyOne;
        }

        public String getPropertyOne(int i) {
            return getPropertyOne()[i];
        }

        public void setPropertyOne(int i, String value) {
            this.propertyOne[i] = value;
        }

    }
}
