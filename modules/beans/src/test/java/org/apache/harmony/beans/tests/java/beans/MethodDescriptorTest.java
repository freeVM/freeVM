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

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Unit test for MethodDescriptor
 */
public class MethodDescriptorTest extends TestCase {

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
     * Class under test for void MethodDescriptor(Method)
     */
    public void testMethodDescriptorMethod() throws SecurityException,
            NoSuchMethodException {
        String beanName = "MethodDescriptorTest.bean";
        MockJavaBean bean = new MockJavaBean(beanName);
        Method method = bean.getClass().getMethod("getBeanName", (Class[])null);
        MethodDescriptor md = new MethodDescriptor(method);

        assertSame(method, md.getMethod());
        assertNull(md.getParameterDescriptors());
        
        assertEquals(method.getName(), md.getDisplayName());
        assertEquals(method.getName(), md.getName());
        assertEquals(method.getName(), md.getShortDescription());

        assertNotNull(md.attributeNames());

        assertFalse(md.isExpert());
        assertFalse(md.isHidden());
        assertFalse(md.isPreferred());
    }

    public void testMethodDescriptorMethod_Null() {
        Method method = null;
        try {
            MethodDescriptor md = new MethodDescriptor(method);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }

    }

    /*
     * Class under test for void MethodDescriptor(Method, ParameterDescriptor[])
     */
    public void testMethodDescriptorMethodParameterDescriptorArray()
            throws SecurityException, NoSuchMethodException {
        String beanName = "MethodDescriptorTest.bean";
        MockJavaBean bean = new MockJavaBean(beanName);
        Method method = bean.getClass().getMethod("setPropertyOne",
                new Class[] { String.class });
        ParameterDescriptor[] pds = new ParameterDescriptor[1];
        pds[0] = new ParameterDescriptor();
        pds[0].setValue(method.getName(), method.getReturnType());
        MethodDescriptor md = new MethodDescriptor(method, pds);

        assertSame(method, md.getMethod());
        assertSame(pds, md.getParameterDescriptors());
        assertEquals(pds[0].getValue(method.getName()), md
                .getParameterDescriptors()[0].getValue(method.getName()));

        assertEquals(method.getName(), md.getDisplayName());
        assertEquals(method.getName(), md.getName());
        assertEquals(method.getName(), md.getShortDescription());

        assertNotNull(md.attributeNames());

        assertFalse(md.isExpert());
        assertFalse(md.isHidden());
        assertFalse(md.isPreferred());
    }

    public void testMethodDescriptorMethodParameterDescriptorArray_MethodNull() {
        Method method = null;
        ParameterDescriptor[] pds = new ParameterDescriptor[1];
        pds[0] = new ParameterDescriptor();
        try {
            MethodDescriptor md = new MethodDescriptor(method, pds);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    public void testMethodDescriptorMethodParameterDescriptorArray_PDNull()
            throws SecurityException, NoSuchMethodException {
        String beanName = "MethodDescriptorTest.bean";
        MockJavaBean bean = new MockJavaBean(beanName);
        Method method = bean.getClass().getMethod("setPropertyOne",
                new Class[] { String.class });
        MethodDescriptor md = new MethodDescriptor(method, null);

        assertSame(method, md.getMethod());
        assertNull(md.getParameterDescriptors());

        assertEquals(method.getName(), md.getDisplayName());
        assertEquals(method.getName(), md.getName());
        assertEquals(method.getName(), md.getShortDescription());

        assertNotNull(md.attributeNames());

        assertFalse(md.isExpert());
        assertFalse(md.isHidden());
        assertFalse(md.isPreferred());
    }
    
    /**
     * @tests java.beans.MethodDescriptor#MethodDescriptor(
     *        java.lang.reflect.Method)
     */
    public void test_Ctor1_NullPointerExpection() {
        try {
            // Regression for HARMONY-226
            new MethodDescriptor(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.beans.MethodDescriptor#MethodDescriptor(
     *        java.lang.reflect.Method,
     *        java.beans.ParameterDescriptor[])
     */
    public void test_Ctor2_NullPointerExpection() {
        try {
            // Regression for HARMONY-226
            new MethodDescriptor(null, new ParameterDescriptor[0]);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}
