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

package org.apache.harmony.testframework;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

import java.io.OutputStream;
import java.util.Random;

/**
 * Tests behaviour common to all implementations of {@link OutputStream}. This
 * adapts streams that collects untransformed bytes so that they may be tested.
 */
public abstract class SinkTester {

    /**
     * Creates a new output stream ready to receive an arbitrary number of
     * bytes. Each time this method is invoked, any previously returned output
     * streams may be discarded.
     */
    public abstract OutputStream create() throws Exception;

    /**
     * Returns the current set of bytes written to the output stream last
     * returned by {@link #create}, and releases any resources held by that
     * stream.
     */
    public abstract byte[] getBytes() throws Exception;

    public final TestSuite createTests() {
        TestSuite result = new TestSuite();
        result.addTest(new SinkTestCase("sinkTestNoWriting"));
        result.addTest(new SinkTestCase("sinkTestWriteZeroBytes"));
        result.addTest(new SinkTestCase("sinkTestWriteByteByByte"));
        result.addTest(new SinkTestCase("sinkTestWriteArray"));
        result.addTest(new SinkTestCase("sinkTestWriteOffset"));
        result.addTest(new SinkTestCase("sinkTestWriteLargeArray"));
        return result;
    }

    @Override public String toString() {
        return getClass().getName();
    }

    public class SinkTestCase extends TestCase {

        private SinkTestCase(String name) {
            super(name);
        }

        public void sinkTestNoWriting() throws Exception {
            byte[] expected = new byte[] {};

            OutputStream out = create();
            out.close();
            Assert.assertArrayEquals(expected, getBytes());
        }

        public void sinkTestWriteZeroBytes() throws Exception {
            byte[] expected = new byte[] {};

            OutputStream out = create();
            byte[] a = new byte[1024];
            out.write(a, 1000, 0);
            out.write(a, 0, 0);
            out.write(new byte[] {});

            out.close();
            Assert.assertArrayEquals(expected, getBytes());
        }

        public void sinkTestWriteByteByByte() throws Exception {
            byte[] expected = new byte[] { 5, 6, 7, 3, 4, 5, 3, 2, 1 };

            OutputStream out = create();
            for (byte b : expected) {
                out.write(b);
            }

            out.close();
            Assert.assertArrayEquals(expected, getBytes());
        }

        public void sinkTestWriteArray() throws Exception {
            byte[] expected = new byte[] {
                    5, 6,
                    7, 3, 4, 5,
                    3, 2, 1
            };

            OutputStream out = create();

            byte[] a = new byte[] { 5, 6 };
            out.write(a);

            byte[] b = new byte[] { 7, 3, 4, 5 };
            out.write(b);

            byte[] c = new byte[] { 3, 2, 1 };
            out.write(c);

            out.close();
            Assert.assertArrayEquals(expected, getBytes());
        }

        public void sinkTestWriteOffset() throws Exception {
            byte[] expected = new byte[] {
                    5, 6,
                    7, 3, 4, 5,
                    3, 2, 1
            };

            OutputStream out = create();

            byte[] a = new byte[1024];
            a[1000] = 5;
            a[1001] = 6;
            out.write(a, 1000, 2);

            byte[] b = new byte[1024];
            b[1020] = 7;
            b[1021] = 3;
            b[1022] = 4;
            b[1023] = 5;
            out.write(b, 1020, 4);

            byte[] c = new byte[1024];
            c[0] = 3;
            c[1] = 2;
            c[2] = 1;
            out.write(c, 0, 3);

            out.close();
            Assert.assertArrayEquals(expected, getBytes());
        }

        public void sinkTestWriteLargeArray() throws Exception {
            byte[] expected = new byte[(1024 * 1024) + 1]; // 1 MB + 1 byte
            new Random().nextBytes(expected);

            OutputStream out = create();
            out.write(expected);
            out.close();

            Assert.assertArrayEquals(expected, getBytes());
        }

        // adding a new test? Don't forget to update createTests().

        @Override public String getName() {
            return SinkTester.this.toString() + ":" + super.getName();
        }
    }
}
