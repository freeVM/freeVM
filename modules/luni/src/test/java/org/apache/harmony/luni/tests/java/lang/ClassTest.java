/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.tests.java.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.BasicPermission;
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import tests.support.resource.Support_Resources;

public class ClassTest extends junit.framework.TestCase {

    static class StaticMember$Class {
        class Member2$A {
        }
    }

    class Member$Class {
        class Member3$B {
        }
    }

    public static class TestClass {
        @SuppressWarnings("unused")
        private int privField = 1;

        public int pubField = 2;

        private Object cValue = null;

        public Object ack = new Object();

        @SuppressWarnings("unused")
        private int privMethod() {
            return 1;
        }

        public int pubMethod() {
            return 2;
        }

        public Object cValue() {
            return cValue;
        }

        public TestClass() {
        }

        @SuppressWarnings("unused")
        private TestClass(Object o) {
        }
    }

    public static class SubTestClass extends TestClass {
    }

    /**
     * @tests java.lang.Class#forName(java.lang.String)
     */
    public void test_forNameLjava_lang_String() throws Exception {
        assertSame("Class for name failed for java.lang.Object", Object.class, Class
                .forName("java.lang.Object"));
        assertSame("Class for name failed for [[Ljava.lang.Object;", Object[][].class, Class
                .forName("[[Ljava.lang.Object;"));

        assertSame("Class for name failed for [I", int[].class, Class.forName("[I"));

        try {
            Class.forName("int");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("byte");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("char");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("void");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("short");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("long");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("boolean");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("float");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("double");
            fail();
        } catch (ClassNotFoundException e) {
        }
    }

    /**
     * @tests java.lang.Class#getClasses()
     */
    public void test_getClasses() {
        assertEquals("Incorrect class array returned", 2, ClassTest.class.getClasses().length);
    }

    /**
     * @tests java.lang.Class#getClasses()
     */
    public void test_getClasses_subtest0() {
        final Permission privCheckPermission = new BasicPermission("Privilege check") {
            private static final long serialVersionUID = 1L;
        };

        class MyCombiner implements DomainCombiner {
            boolean combine;

            public ProtectionDomain[] combine(ProtectionDomain[] executionDomains,
                    ProtectionDomain[] parentDomains) {
                combine = true;
                return new ProtectionDomain[0];
            }

            public boolean isPriviledged() {
                combine = false;
                try {
                    AccessController.checkPermission(privCheckPermission);
                } catch (SecurityException e) {
                }
                return !combine;
            }
        }

        final MyCombiner combiner = new MyCombiner();
        class SecurityManagerCheck extends SecurityManager {
            String reason;

            Class<?> checkClass;

            int checkType;

            int checkPermission;

            int checkMemberAccess;

            int checkPackageAccess;

            public void setExpected(String reason, Class<?> cls, int type) {
                this.reason = reason;
                checkClass = cls;
                checkType = type;
                checkPermission = 0;
                checkMemberAccess = 0;
                checkPackageAccess = 0;
            }

            @Override
            public void checkPermission(Permission perm) {
                if (combiner.isPriviledged())
                    return;
                checkPermission++;
            }

            @Override
            public void checkMemberAccess(Class<?> cls, int type) {
                if (combiner.isPriviledged())
                    return;
                checkMemberAccess++;
                assertTrue(reason + " unexpected class: " + cls, cls == checkClass);
                assertTrue(reason + "unexpected type: " + type, type == checkType);
            }

            @Override
            public void checkPackageAccess(String packageName) {
                if (combiner.isPriviledged())
                    return;
                checkPackageAccess++;
                String name = checkClass.getName();
                int index = name.lastIndexOf('.');
                String checkPackage = name.substring(0, index);
                assertTrue(reason + " unexpected package: " + packageName, packageName
                        .equals(checkPackage));
            }

            public void assertProperCalls() {
                assertTrue(reason + " unexpected checkPermission count: " + checkPermission,
                        checkPermission == 0);
                assertTrue(
                        reason + " unexpected checkMemberAccess count: " + checkMemberAccess,
                        checkMemberAccess == 1);
                assertTrue(reason + " unexpected checkPackageAccess count: "
                        + checkPackageAccess, checkPackageAccess == 1);
            }
        }

        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        AccessControlContext acc2 = new AccessControlContext(acc, combiner);

        PrivilegedAction<?> action = new PrivilegedAction<Object>() {
            public Object run() {
                File resources = Support_Resources.createTempFolder();
                try {
                    Support_Resources.copyFile(resources, null, "hyts_security.jar");
                    File file = new File(resources.toString() + "/hyts_security.jar");
                    URL url = new URL("file:" + file.getPath());
                    ClassLoader loader = new URLClassLoader(new URL[] { url }, null);
                    Class<?> cls = Class.forName("packB.SecurityTestSub", false, loader);
                    SecurityManagerCheck sm = new SecurityManagerCheck();
                    System.setSecurityManager(sm);
                    try {
                        sm.setExpected("getClasses", cls, Member.PUBLIC);
                        cls.getClasses();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredClasses", cls, Member.DECLARED);
                        cls.getDeclaredClasses();
                        sm.assertProperCalls();

                        sm.setExpected("getConstructor", cls, Member.PUBLIC);
                        cls.getConstructor(new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getConstructors", cls, Member.PUBLIC);
                        cls.getConstructors();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredConstructor", cls, Member.DECLARED);
                        cls.getDeclaredConstructor(new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredConstructors", cls, Member.DECLARED);
                        cls.getDeclaredConstructors();
                        sm.assertProperCalls();

                        sm.setExpected("getField", cls, Member.PUBLIC);
                        cls.getField("publicField");
                        sm.assertProperCalls();

                        sm.setExpected("getFields", cls, Member.PUBLIC);
                        cls.getFields();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredField", cls, Member.DECLARED);
                        cls.getDeclaredField("publicField");
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredFields", cls, Member.DECLARED);
                        cls.getDeclaredFields();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredMethod", cls, Member.DECLARED);
                        cls.getDeclaredMethod("publicMethod", new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredMethods", cls, Member.DECLARED);
                        cls.getDeclaredMethods();
                        sm.assertProperCalls();

                        sm.setExpected("getMethod", cls, Member.PUBLIC);
                        cls.getMethod("publicMethod", new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getMethods", cls, Member.PUBLIC);
                        cls.getMethods();
                        sm.assertProperCalls();

                        sm.setExpected("newInstance", cls, Member.PUBLIC);
                        cls.newInstance();
                        sm.assertProperCalls();
                    } finally {
                        System.setSecurityManager(null);
                    }
                } catch (Exception e) {
                    if (e instanceof RuntimeException)
                        throw (RuntimeException) e;
                    fail("unexpected exception: " + e);
                }
                return null;
            }
        };
        AccessController.doPrivileged(action, acc2);
    }

    /**
     * @tests java.lang.Class#getComponentType()
     */
    public void test_getComponentType() {
        assertSame("int array does not have int component type", int.class, int[].class
                .getComponentType());
        assertSame("Object array does not have Object component type", Object.class,
                Object[].class.getComponentType());
        assertNull("Object has non-null component type", Object.class.getComponentType());
    }

    /**
     * @tests java.lang.Class#getConstructor(java.lang.Class[])
     */
    public void test_getConstructor$Ljava_lang_Class() {
        try {
            TestClass.class.getConstructor(new Class[0]);
            try {
                TestClass.class.getConstructor(Object.class);
            } catch (NoSuchMethodException e) {
                // Correct - constructor with obj is private
                return;
            }
            fail("Found private constructor");
        } catch (NoSuchMethodException e) {
            fail("Exception during getConstructor test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.Class#getConstructors()
     */
    public void test_getConstructors() {
        try {
            Constructor[] c = TestClass.class.getConstructors();
            assertEquals("Incorrect number of constructors returned", 1, c.length);
        } catch (Exception e) {
            fail("Exception during getDeclaredConstructor test:" + e.toString());
        }
    }

    /**
     * @tests java.lang.Class#getDeclaredClasses()
     */
    public void test_getDeclaredClasses() {
        assertEquals("Incorrect class array returned", 2, ClassTest.class.getClasses().length);
    }

    /**
     * @tests java.lang.Class#getDeclaredConstructor(java.lang.Class[])
     */
    public void test_getDeclaredConstructor$Ljava_lang_Class() throws Exception {
        Constructor<TestClass> c = TestClass.class.getDeclaredConstructor(new Class[0]);
        assertNull("Incorrect constructor returned", c.newInstance().cValue());
        c = TestClass.class.getDeclaredConstructor(Object.class);
    }

    /**
     * @tests java.lang.Class#getDeclaredConstructors()
     */
    public void test_getDeclaredConstructors() throws Exception {
        Constructor[] c = TestClass.class.getDeclaredConstructors();
        assertEquals("Incorrect number of constructors returned", 2, c.length);
    }

    /**
     * @tests java.lang.Class#getDeclaredField(java.lang.String)
     */
    public void test_getDeclaredFieldLjava_lang_String() throws Exception {
        Field f = TestClass.class.getDeclaredField("pubField");
        assertEquals("Returned incorrect field", 2, f.getInt(new TestClass()));
    }

    /**
     * @tests java.lang.Class#getDeclaredFields()
     */
    public void test_getDeclaredFields() throws Exception {
        Field[] f = TestClass.class.getDeclaredFields();
        assertEquals("Returned incorrect number of fields", 4, f.length);
        f = SubTestClass.class.getDeclaredFields();
        // Declared fields do not include inherited
        assertEquals("Returned incorrect number of fields", 0, f.length);

    }

    /**
     * @tests java.lang.Class#getDeclaredMethod(java.lang.String,
     *        java.lang.Class[])
     */
    public void test_getDeclaredMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("pubMethod", new Class[0]);
        assertEquals("Returned incorrect method", 2, ((Integer) (m.invoke(new TestClass())))
                .intValue());
        m = TestClass.class.getDeclaredMethod("privMethod", new Class[0]);
        try {
            // Invoking private non-sub, non-package
            m.invoke(new TestClass());
            fail("Should throw IllegalAccessException");
        } catch (IllegalAccessException e) {
            // Correct
            return;
        }
    }

    /**
     * @tests java.lang.Class#getDeclaredMethods()
     */
    public void test_getDeclaredMethods() throws Exception {
        Method[] m = TestClass.class.getDeclaredMethods();
        assertEquals("Returned incorrect number of methods", 3, m.length);
        m = SubTestClass.class.getDeclaredMethods();
        assertEquals("Returned incorrect number of methods", 0, m.length);
    }

    /**
     * @tests java.lang.Class#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        assertEquals(ClassTest.class, TestClass.class.getDeclaringClass());
    }

    /**
     * @tests java.lang.Class#getField(java.lang.String)
     */
    public void test_getFieldLjava_lang_String() throws Exception {
        Field f = TestClass.class.getField("pubField");
        assertEquals("Returned incorrect field", 2, f.getInt(new TestClass()));
        try {
            f = TestClass.class.getField("privField");
            fail("Private field access failed to throw exception");
        } catch (NoSuchFieldException e) {
            // Correct
            return;
        }
    }

    /**
     * @tests java.lang.Class#getFields()
     */
    public void test_getFields() throws Exception {
        Field[] f = TestClass.class.getFields();
        assertTrue("Incorrect number of fields returned: " + f.length, f.length == 2);
        f = SubTestClass.class.getFields();
        // Check inheritance of pub fields
        assertTrue("Incorrect number of fields returned: " + f.length, f.length == 2);
    }

    /**
     * @tests java.lang.Class#getInterfaces()
     */
    public void test_getInterfaces() {
        Class[] interfaces;
        List<?> interfaceList;
        interfaces = Object.class.getInterfaces();
        assertEquals("Incorrect interface list for Object", 0, interfaces.length);
        interfaceList = Arrays.asList(Vector.class.getInterfaces());
        assertTrue("Incorrect interface list for Vector", interfaceList
                .contains(Cloneable.class)
                && interfaceList.contains(Serializable.class)
                && interfaceList.contains(List.class));
    }

    /**
     * @tests java.lang.Class#getMethod(java.lang.String, java.lang.Class[])
     */
    public void test_getMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
        Method m = TestClass.class.getMethod("pubMethod", new Class[0]);
        assertEquals("Returned incorrect method", 2, ((Integer) (m.invoke(new TestClass())))
                .intValue());
        try {
            m = TestClass.class.getMethod("privMethod", new Class[0]);
            fail("Failed to throw exception accessing private method");
        } catch (NoSuchMethodException e) {
            // Correct
            return;
        }
    }

    /**
     * @tests java.lang.Class#getMethods()
     */
    public void test_getMethods() throws Exception {
        Method[] m = TestClass.class.getMethods();
        assertTrue("Returned incorrect number of methods: " + m.length,
                m.length == 2 + Object.class.getMethods().length);
        m = SubTestClass.class.getMethods();
        assertTrue("Returned incorrect number of sub-class methods: " + m.length,
                m.length == 2 + Object.class.getMethods().length);
        // Number of inherited methods
    }

    private static final class PrivateClass {
    }
    /**
     * @tests java.lang.Class#getModifiers()
     */
    public void test_getModifiers() {
        int dcm = PrivateClass.class.getModifiers();
        assertFalse("default class is public", Modifier.isPublic(dcm));
        assertFalse("default class is protected", Modifier.isProtected(dcm));
        assertTrue("default class is not private", Modifier.isPrivate(dcm));

        int ocm = Object.class.getModifiers();
        assertTrue("public class is not public", Modifier.isPublic(ocm));
        assertFalse("public class is protected", Modifier.isProtected(ocm));
        assertFalse("public class is private", Modifier.isPrivate(ocm));
    }

    /**
     * @tests java.lang.Class#getName()
     */
    public void test_getName() throws Exception {
        String className = Class.forName("java.lang.Object").getName();
        assertNotNull(className);

        assertEquals("Class getName printed wrong value", "java.lang.Object", className);
        assertEquals("Class getName printed wrong value", "int", int.class.getName());
        className = Class.forName("[I").getName();
        assertNotNull(className);
        assertEquals("Class getName printed wrong value", "[I", className);

        className = Class.forName("[Ljava.lang.Object;").getName();
        assertNotNull(className);

        assertEquals("Class getName printed wrong value", "[Ljava.lang.Object;", className);
    }

    /**
     * @tests java.lang.Class#getResource(java.lang.String)
     */
    public void test_getResourceLjava_lang_String() {
        final String name = "/org/apache/harmony/luni/tests/test_resource.txt";
        URL res = getClass().getResource(name);
        assertNotNull(res);
    }

    /**
     * @tests java.lang.Class#getResourceAsStream(java.lang.String)
     */
    public void test_getResourceAsStreamLjava_lang_String() throws Exception {
        final String name = "/org/apache/harmony/luni/tests/test_resource.txt";
        assertNotNull("the file " + name + " can not be found in this directory", getClass()
                .getResourceAsStream(name));

        final String nameBadURI = "org/apache/harmony/luni/tests/test_resource.txt";
        assertNull("the file " + nameBadURI + " should not be found in this directory",
                getClass().getResourceAsStream(nameBadURI));

        InputStream str = Object.class.getResourceAsStream("Class.class");
        assertNotNull("java.lang.Object couldn't find its class with getResource...", str);

        assertTrue("Cannot read single byte", str.read() != -1);
        assertEquals("Cannot read multiple bytes", 5, str.read(new byte[5]));
        str.close();

        InputStream str2 = getClass().getResourceAsStream("ClassTest.class");
        assertNotNull("Can't find resource", str2);
        assertTrue("Cannot read single byte", str2.read() != -1);
        assertEquals("Cannot read multiple bytes", 5, str2.read(new byte[5]));
        str2.close();
    }

    /**
     * @tests java.lang.Class#getSuperclass()
     */
    public void test_getSuperclass() {
        assertNull("Object has a superclass???", Object.class.getSuperclass());
        assertSame("Normal class has bogus superclass", InputStream.class,
                FileInputStream.class.getSuperclass());
        assertSame("Array class has bogus superclass", Object.class, FileInputStream[].class
                .getSuperclass());
        assertNull("Base class has a superclass", int.class.getSuperclass());
        assertNull("Interface class has a superclass", Cloneable.class.getSuperclass());
    }

    /**
     * @tests java.lang.Class#isArray()
     */
    public void test_isArray() {
        assertTrue("Non-array type claims to be.", !int.class.isArray());
        Class<?> clazz = null;
        try {
            clazz = Class.forName("[I");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [I");
        }
        assertTrue("int Array type claims not to be.", clazz.isArray());

        try {
            clazz = Class.forName("[Ljava.lang.Object;");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [Ljava.lang.Object;");
        }

        assertTrue("Object Array type claims not to be.", clazz.isArray());

        try {
            clazz = Class.forName("java.lang.Object");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class java.lang.Object");
        }
        assertTrue("Non-array Object type claims to be.", !clazz.isArray());
    }

    /**
     * @tests java.lang.Class#isAssignableFrom(java.lang.Class)
     */
    public void test_isAssignableFromLjava_lang_Class() {
        Class<?> clazz1 = null;
        Class<?> clazz2 = null;

        clazz1 = Object.class;
        clazz2 = Class.class;
        assertTrue("returned false for superclass", clazz1.isAssignableFrom(clazz2));

        clazz1 = TestClass.class;
        assertTrue("returned false for same class", clazz1.isAssignableFrom(clazz1));

        clazz1 = Runnable.class;
        clazz2 = Thread.class;
        assertTrue("returned false for implemented interface", clazz1.isAssignableFrom(clazz2));
    }

    /**
     * @tests java.lang.Class#isInterface()
     */
    public void test_isInterface() {
        assertTrue("Prim type claims to be interface.", !int.class.isInterface());
        Class<?> clazz = null;
        try {
            clazz = Class.forName("[I");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [I");
        }
        assertTrue("Prim Array type claims to be interface.", !clazz.isInterface());

        try {
            clazz = Class.forName("java.lang.Runnable");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class java.lang.Runnable");
        }
        assertTrue("Interface type claims not to be interface.", clazz.isInterface());

        try {
            clazz = Class.forName("java.lang.Object");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class java.lang.Object");
        }
        assertTrue("Object type claims to be interface.", !clazz.isInterface());

        try {
            clazz = Class.forName("[Ljava.lang.Object;");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [Ljava.lang.Object;");
        }
        assertTrue("Array type claims to be interface.", !clazz.isInterface());
    }

    /**
     * @tests java.lang.Class#isPrimitive()
     */
    public void test_isPrimitive() {
        assertFalse("Interface type claims to be primitive.", Runnable.class.isPrimitive());
        assertFalse("Object type claims to be primitive.", Object.class.isPrimitive());
        assertFalse("Prim Array type claims to be primitive.", int[].class.isPrimitive());
        assertFalse("Array type claims to be primitive.", Object[].class.isPrimitive());
        assertTrue("Prim type claims not to be primitive.", int.class.isPrimitive());
        assertFalse("Object type claims to be primitive.", Object.class.isPrimitive());
    }

    /**
     * @tests java.lang.Class#newInstance()
     */
    public void test_newInstance() {
        Class<?> clazz = null;
        try {
            try {
                clazz = Class.forName("java.lang.Object");
            } catch (ClassNotFoundException e) {
                fail("Should be able to find the class java.lang.Object");
            }
            assertNotNull("new object instance was null", clazz.newInstance());
        } catch (Exception e) {
            fail("Unexpected exception " + e + " in newInstance()");
        }
        try {
            try {
                clazz = Class.forName("java.lang.Throwable");
            } catch (ClassNotFoundException e) {
                fail("Should be able to find the class java.lang.Throwable");
            }
            assertTrue("new Throwable instance was not a throwable", clazz.newInstance()
                    .getClass() == clazz);
        } catch (Exception e) {
            fail("Unexpected exception " + e + " in newInstance()");
        }
        int r = 0;
        try {
            try {
                clazz = Class.forName("java.lang.Integer");
            } catch (ClassNotFoundException e) {
                fail("Should be able to find the class java.lang.Integer");
            }
            assertTrue("Allowed to do newInstance, when no default constructor", clazz
                    .newInstance() != null
                    || clazz.newInstance() == null);
        } catch (Exception e) {
            r = 1;
        }
        assertEquals(
                "Exception for instantiating a newInstance with no default constructor is not thrown",
                1, r);
    }

    /**
     * @tests java.lang.Class#toString()
     */
    public void test_toString() {
        assertTrue("Class toString printed wrong value:" + int.class.toString(), int.class
                .toString().equals("int"));
        Class<?> clazz = null;
        try {
            clazz = Class.forName("[I");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [I");
        }
        assertTrue("Class toString printed wrong value:" + clazz.toString(), clazz.toString()
                .equals("class [I"));

        try {
            clazz = Class.forName("java.lang.Object");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class java.lang.Object");
        }
        assertTrue("Class toString printed wrong value:" + clazz.toString(), clazz.toString()
                .equals("class java.lang.Object"));

        try {
            clazz = Class.forName("[Ljava.lang.Object;");
        } catch (ClassNotFoundException e) {
            fail("Should be able to find the class [Ljava.lang.Object;");
        }
        assertTrue("Class toString printed wrong value:" + clazz.toString(), clazz.toString()
                .equals("class [Ljava.lang.Object;"));
    }
}
