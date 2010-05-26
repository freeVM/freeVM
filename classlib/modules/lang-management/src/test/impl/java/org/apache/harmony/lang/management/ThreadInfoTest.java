/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 */

package org.apache.harmony.lang.management;

import java.lang.management.ThreadInfo;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import junit.framework.TestCase;
import tests.support.Support_Excludes;

/**
 * ThreadInfo objects can only be obtained from the ThreadMXBean or else by
 * calling the static from() method with an existing CompositeData object that
 * maps to a ThreadInfo. Trying to unit test using the former approach only gets
 * us so far as we have no idea what the expected values should be. We only know
 * their types, ranges etc etc. This testcase goes the long way round by
 * creating a CompositeData representing a ThreadInfo with well understood
 * values that we can test for once we have passed the CompositeData into the
 * from() method to get a ThreadInfo.
 * <p>
 * The "problem" with this approach is that the hand-rolled CompositeData
 * created in this testcase is not liked by the RI which has its own internal
 * way of validating CompositeData and CompositeType instances. So, while this
 * test case is creating CompositeData and CompositeType objects which adhere to
 * the spec, the below tests will all fail when run against the RI. For that
 * reason, this testcase cannot be considered as implementation independent.
 * 
 */
public class ThreadInfoTest extends TestCase {

    private static final boolean GOOD_SUSPENDED = false;

    private static final boolean GOOD_IN_NATIVE = false;

    private static final int GOOD_STACK_SIZE = 3;

    private static final boolean GOOD_STACK_NATIVEMETHOD = false;

    private static final int GOOD_STACK_LINENUMBER = 2100;

    private static final String GOOD_STACK_FILENAME = "Blobby.java";

    private static final String GOOD_STACK_METHODNAME = "takeOverWorld";

    private static final String GOOD_STACK_CLASSNAME = "foo.bar.Blobby";

    private static final Thread.State GOOD_THREAD_STATE = Thread.State.RUNNABLE;

    private static final String GOOD_THREAD_NAME = "Marty";

    private static final int GOOD_THREAD_ID = 46664;

    private static final String GOOD_LOCK_OWNER_NAME = "Noam Chomsky";

    private static final int GOOD_LOCK_OWNER_ID = 24141;

    private static final int GOOD_WAITED_TIME = 3779;

    private static final int GOOD_WAITED_COUNT = 21;

    private static final int GOOD_BLOCKED_TIME = 3309;

    private static final int GOOD_BLOCKED_COUNT = 250;

    private static final String GOOD_LOCK_NAME = "foo.Bar@1234567";

    private static final String GOOD_THREADINFO_CLASSNAME = ThreadInfo.class.getName();

    private CompositeData tiCD;

    private ThreadInfo ti;

    protected void setUp() throws Exception {
        super.setUp();
        tiCD = createGoodCompositeData();
        ti = ThreadInfo.from(tiCD);
        assertNotNull(ti);
    }

    public CompositeData createGoodCompositeData() {
        CompositeData result = null;
        String[] names = { "threadId", "threadName", "threadState",
                "suspended", "inNative", "blockedCount", "blockedTime",
                "waitedCount", "waitedTime", "lockName", "lockOwnerId",
                "lockOwnerName", "stackTrace" };
        Object[] values = {
        /* threadId */new Long(GOOD_THREAD_ID),
        /* threadName */new String(GOOD_THREAD_NAME),
        /* threadState */new String(GOOD_THREAD_STATE.toString()),
        /* suspended */new Boolean(GOOD_SUSPENDED),
        /* inNative */new Boolean(GOOD_IN_NATIVE),
        /* blockedCount */new Long(GOOD_BLOCKED_COUNT),
        /* blockedTime */new Long(GOOD_BLOCKED_TIME),
        /* waitedCount */new Long(GOOD_WAITED_COUNT),
        /* waitedTime */new Long(GOOD_WAITED_TIME),
        /* lockName */new String(GOOD_LOCK_NAME),
        /* lockOwnerId */new Long(GOOD_LOCK_OWNER_ID),
        /* lockOwnerName */new String(GOOD_LOCK_OWNER_NAME),
        /* stackTrace */createGoodStackTraceCompositeData() };
        CompositeType cType = createGoodThreadInfoCompositeType();
        try {
            result = new CompositeDataSupport(cType, names, values);
        } catch (OpenDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return new array of <code>CompositeData</code> representing an array
     *         of <code>StackTraceElement</code>.
     */
    public static CompositeData[] createGoodStackTraceCompositeData() {
        // Let's make the array have three elements. Doesn't matter that
        // they are all identical.
        CompositeData[] result = new CompositeData[GOOD_STACK_SIZE];
        CompositeType cType = createGoodStackTraceElementCompositeType();
        String[] names = { "className", "methodName", "fileName", "lineNumber",
                "nativeMethod" };
        Object[] values = { GOOD_STACK_CLASSNAME, GOOD_STACK_METHODNAME,
                GOOD_STACK_FILENAME, new Integer(GOOD_STACK_LINENUMBER),
                new Boolean(GOOD_STACK_NATIVEMETHOD) };

        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = new CompositeDataSupport(cType, names, values);
            } catch (OpenDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }// end for
        return result;
    }

    /**
     * @return <code>CompositeType</code> for use when wrapping up
     *         <code>ThreadInfo</code> objects in <code>CompositeData</code>
     *         s.
     */
    private static CompositeType createGoodThreadInfoCompositeType() {
        CompositeType result = null;
        try {
            String[] typeNames = { "threadId", "threadName", "threadState",
                    "suspended", "inNative", "blockedCount", "blockedTime",
                    "waitedCount", "waitedTime", "lockName", "lockOwnerId",
                    "lockOwnerName", "stackTrace" };
            String[] typeDescs = { "threadId", "threadName", "threadState",
                    "suspended", "inNative", "blockedCount", "blockedTime",
                    "waitedCount", "waitedTime", "lockName", "lockOwnerId",
                    "lockOwnerName", "stackTrace", };
            OpenType[] typeTypes = {
                    SimpleType.LONG,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.BOOLEAN,
                    SimpleType.BOOLEAN,
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.STRING,
                    SimpleType.LONG,
                    SimpleType.STRING,
                    new ArrayType(1, createGoodStackTraceElementCompositeType()) };
            result = new CompositeType(ThreadInfo.class.getName(),
                    ThreadInfo.class.getName(), typeNames, typeDescs, typeTypes);
        } catch (OpenDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private static CompositeType createGoodStackTraceElementCompositeType() {
        CompositeType result = null;
        String[] typeNames = { "className", "methodName", "fileName",
                "lineNumber", "nativeMethod" };
        String[] typeDescs = { "className", "methodName", "fileName",
                "lineNumber", "nativeMethod" };
        OpenType[] typeTypes = { SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.INTEGER, SimpleType.BOOLEAN };
        try {
            result = new CompositeType(StackTraceElement.class.getName(),
                    StackTraceElement.class.getName(), typeNames, typeDescs,
                    typeTypes);
        } catch (OpenDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getBlockedCount()'
     */
    public void testGetBlockedCount() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_BLOCKED_COUNT, ti.getBlockedCount());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getBlockedTime()'
     */
    public void testGetBlockedTime() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_BLOCKED_TIME, ti.getBlockedTime());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getLockOwnerId()'
     */
    public void testGetLockOwnerId() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_LOCK_OWNER_ID, ti.getLockOwnerId());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getLockOwnerName()'
     */
    public void testGetLockOwnerName() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_LOCK_OWNER_NAME, ti.getLockOwnerName());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getStackTrace()'
     */
    public void testGetStackTrace() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        StackTraceElement[] stack = ti.getStackTrace();
        assertEquals(GOOD_STACK_SIZE, stack.length);
        for (StackTraceElement element : stack) {
            assertEquals(GOOD_STACK_CLASSNAME, element.getClassName());
            assertEquals(GOOD_STACK_NATIVEMETHOD, element.isNativeMethod());
            assertEquals(GOOD_STACK_FILENAME, element.getFileName());
            assertEquals(GOOD_STACK_LINENUMBER, element.getLineNumber());
            assertEquals(GOOD_STACK_METHODNAME, element.getMethodName());
        }
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getThreadId()'
     */
    public void testGetThreadId() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_THREAD_ID, ti.getThreadId());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getThreadName()'
     */
    public void testGetThreadName() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_THREAD_NAME, ti.getThreadName());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getThreadState()'
     */
    public void testGetThreadState() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_THREAD_STATE, (ti.getThreadState()));
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getWaitedCount()'
     */
    public void testGetWaitedCount() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_WAITED_COUNT, ti.getWaitedCount());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.getWaitedTime()'
     */
    public void testGetWaitedTime() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_WAITED_TIME, ti.getWaitedTime());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.isInNative()'
     */
    public void testIsInNative() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_IN_NATIVE, ti.isInNative());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.isSuspended()'
     */
    public void testIsSuspended() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(GOOD_SUSPENDED, ti.isSuspended());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.toString()'
     */
    public void testToString() {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        assertEquals(getGoodToStringVal(), ti.toString());
    }

    /*
     * Test method for 'java.lang.management.ThreadInfo.from(CompositeData)'
     * with more than 13 essential fields
     */
    public void test_from_fields() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        Object stackTraceElementData = createGoodStackTraceCompositeData();
        CompositeType stackTraceElementType = createGoodStackTraceElementCompositeType();
        String[] names = { "threadId", "threadName", "threadState",
                "suspended", "inNative", "blockedCount", "blockedTime",
                "waitedCount", "waitedTime", "lockName", "lockOwnerId",
                "lockOwnerName", "stackTrace", "additionalName" };
        Object[] values = { 1L, "threadName", GOOD_THREAD_STATE.toString(),
                true, false, 1L, 500L, 1L, 1L, "lock", 2L, "lockOwner",
                stackTraceElementData, "additionalValue" };
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, new ArrayType(1, stackTraceElementType),
                SimpleType.STRING };
        CompositeType compositeType = new CompositeType(ThreadInfo.class
                .getName(), ThreadInfo.class.getName(), names, names, types);
        CompositeData data = new CompositeDataSupport(compositeType, names,
                values);
        ThreadInfo threadInfo = ThreadInfo.from(data);
        assertEquals(values[0], threadInfo.getThreadId());
        assertEquals(values[1], threadInfo.getThreadName());
        assertEquals(GOOD_THREAD_STATE, threadInfo.getThreadState());
        assertEquals(values[3], threadInfo.isSuspended());
        assertEquals(values[4], threadInfo.isInNative());
        assertEquals(values[5], threadInfo.getBlockedCount());
        assertEquals(values[6], threadInfo.getBlockedTime());
        assertEquals(values[7], threadInfo.getWaitedCount());
        assertEquals(values[8], threadInfo.getWaitedTime());
        assertEquals(values[9], threadInfo.getLockName());
        assertEquals(values[10], threadInfo.getLockOwnerId());
        assertEquals(values[11], threadInfo.getLockOwnerName());
        StackTraceElement[] stackElements = threadInfo.getStackTrace();
        assertEquals(GOOD_STACK_SIZE, stackElements.length);
        for (StackTraceElement element : stackElements) {
            assertEquals(GOOD_STACK_CLASSNAME, element.getClassName());
            assertEquals(GOOD_STACK_NATIVEMETHOD, element.isNativeMethod());
            assertEquals(GOOD_STACK_FILENAME, element.getFileName());
            assertEquals(GOOD_STACK_LINENUMBER, element.getLineNumber());
            assertEquals(GOOD_STACK_METHODNAME, element.getMethodName());
        }
    }

    private static final Object stackTraceElementData = createGoodStackTraceCompositeData();

    private static final CompositeType stackTraceElementType = createGoodStackTraceElementCompositeType();

    private String[] initialNames = { "threadId", "threadName", "threadState",
            "suspended", "inNative", "blockedCount", "blockedTime",
            "waitedCount", "waitedTime", "lockName", "lockOwnerId",
            "lockOwnerName", "stackTrace", "additionalName" };

    private Object[] initialValues = { 1L, "threadName",
            GOOD_THREAD_STATE.toString(), true, false, 1L, 500L, 1L, 1L,
            "lock", 2L, "lockOwner", stackTraceElementData, "additionalValue" };

    public void test_from_scenario1() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        ThreadInfo threadInfo = ThreadInfo.from(data);
        assertEquals(initialValues[0], threadInfo.getThreadId());
        assertEquals(initialValues[1], threadInfo.getThreadName());
        assertEquals(GOOD_THREAD_STATE, threadInfo.getThreadState());
        assertEquals(initialValues[3], threadInfo.isSuspended());
        assertEquals(initialValues[4], threadInfo.isInNative());
        assertEquals(initialValues[5], threadInfo.getBlockedCount());
        assertEquals(initialValues[6], threadInfo.getBlockedTime());
        assertEquals(initialValues[7], threadInfo.getWaitedCount());
        assertEquals(initialValues[8], threadInfo.getWaitedTime());
        assertEquals(initialValues[9], threadInfo.getLockName());
        assertEquals(initialValues[10], threadInfo.getLockOwnerId());
        assertEquals(initialValues[11], threadInfo.getLockOwnerName());
        StackTraceElement[] stackElements = threadInfo.getStackTrace();
        assertEquals(GOOD_STACK_SIZE, stackElements.length);
        for (StackTraceElement element : stackElements) {
            assertEquals(GOOD_STACK_CLASSNAME, element.getClassName());
            assertEquals(GOOD_STACK_NATIVEMETHOD, element.isNativeMethod());
            assertEquals(GOOD_STACK_FILENAME, element.getFileName());
            assertEquals(GOOD_STACK_LINENUMBER, element.getLineNumber());
            assertEquals(GOOD_STACK_METHODNAME, element.getMethodName());
        }
    }

    public void test_from_scenario2() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[0] = "1";
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void test_from_scenario3() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        int length = 10;
        String[] names = new String[length];
        for (int index = 0; index < length; index++) {
            names[index] = initialNames[index];
        }
        Object[] values = new Object[length];
        for (int index = 0; index < length; index++) {
            values[index] = initialValues[index];
        }
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(names, types);
        CompositeData data = new CompositeDataSupport(compositeType, names,
                values);
        try {
            ThreadInfo.from(data);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        values[0] = null;
        compositeType = getCompositeType(names, types);
        data = new CompositeDataSupport(compositeType, names, values);
        try {
            ThreadInfo.from(data);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void test_from_scenario4() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[0] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario5() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[1] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void test_from_scenario6() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[2] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario7() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[3] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario8() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[4] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario9() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[5] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario10() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[6] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario11() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[7] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario12() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[8] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario13() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[9] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        ThreadInfo threadInfo = ThreadInfo.from(data);
        assertEquals(initialValues[0], threadInfo.getThreadId());
        assertEquals(initialValues[1], threadInfo.getThreadName());
        assertEquals(GOOD_THREAD_STATE, threadInfo.getThreadState());
        assertEquals(initialValues[3], threadInfo.isSuspended());
        assertEquals(initialValues[4], threadInfo.isInNative());
        assertEquals(initialValues[5], threadInfo.getBlockedCount());
        assertEquals(initialValues[6], threadInfo.getBlockedTime());
        assertEquals(initialValues[7], threadInfo.getWaitedCount());
        assertEquals(initialValues[8], threadInfo.getWaitedTime());
        assertNull(threadInfo.getLockName());
        assertEquals(initialValues[10], threadInfo.getLockOwnerId());
        assertEquals(initialValues[11], threadInfo.getLockOwnerName());
        StackTraceElement[] stackElements = threadInfo.getStackTrace();
        assertEquals(GOOD_STACK_SIZE, stackElements.length);
        for (StackTraceElement element : stackElements) {
            assertEquals(GOOD_STACK_CLASSNAME, element.getClassName());
            assertEquals(GOOD_STACK_NATIVEMETHOD, element.isNativeMethod());
            assertEquals(GOOD_STACK_FILENAME, element.getFileName());
            assertEquals(GOOD_STACK_LINENUMBER, element.getLineNumber());
            assertEquals(GOOD_STACK_METHODNAME, element.getMethodName());
        }
    }

    public void test_from_scenario14() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[10] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_from_scenario15() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[11] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        ThreadInfo.from(data);
        ThreadInfo threadInfo = ThreadInfo.from(data);
        assertEquals(initialValues[0], threadInfo.getThreadId());
        assertEquals(initialValues[1], threadInfo.getThreadName());
        assertEquals(GOOD_THREAD_STATE, threadInfo.getThreadState());
        assertEquals(initialValues[3], threadInfo.isSuspended());
        assertEquals(initialValues[4], threadInfo.isInNative());
        assertEquals(initialValues[5], threadInfo.getBlockedCount());
        assertEquals(initialValues[6], threadInfo.getBlockedTime());
        assertEquals(initialValues[7], threadInfo.getWaitedCount());
        assertEquals(initialValues[8], threadInfo.getWaitedTime());
        assertEquals(initialValues[9], threadInfo.getLockName());
        assertEquals(initialValues[10], threadInfo.getLockOwnerId());
        assertNull(threadInfo.getLockOwnerName());
        StackTraceElement[] stackElements = threadInfo.getStackTrace();
        assertEquals(GOOD_STACK_SIZE, stackElements.length);
        for (StackTraceElement element : stackElements) {
            assertEquals(GOOD_STACK_CLASSNAME, element.getClassName());
            assertEquals(GOOD_STACK_NATIVEMETHOD, element.isNativeMethod());
            assertEquals(GOOD_STACK_FILENAME, element.getFileName());
            assertEquals(GOOD_STACK_LINENUMBER, element.getLineNumber());
            assertEquals(GOOD_STACK_METHODNAME, element.getMethodName());
        }
    }

    public void test_from_scenario16() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        initialValues[12] = null;
        ArrayType stackTraceArray = new ArrayType(1, stackTraceElementType);
        OpenType[] types = { SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN,
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.STRING, SimpleType.LONG,
                SimpleType.STRING, stackTraceArray, SimpleType.STRING };
        CompositeType compositeType = getCompositeType(initialNames, types);
        CompositeData data = new CompositeDataSupport(compositeType,
                initialNames, initialValues);
        try {
            ThreadInfo.from(data);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    protected CompositeType getCompositeType(String[] typeNames,
            OpenType[] typeTypes) throws Exception {
        return new CompositeType(GOOD_THREADINFO_CLASSNAME,
                GOOD_THREADINFO_CLASSNAME, typeNames, typeNames, typeTypes);
    }

    String getGoodToStringVal() {
        StringBuilder result = new StringBuilder();
        result.append("Thread " + GOOD_THREAD_NAME + " (Id = " + GOOD_THREAD_ID
                + ") " + GOOD_THREAD_STATE + " " + GOOD_LOCK_NAME);
        return result.toString();
    }
}
