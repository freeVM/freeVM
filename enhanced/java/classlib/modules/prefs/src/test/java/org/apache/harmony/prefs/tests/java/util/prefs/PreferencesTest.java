/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.prefs.tests.java.util.prefs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * 
 */
public class PreferencesTest extends TestCase {

    MockSecurityManager manager = new MockSecurityManager();

    MockInputStream stream = null;

    InputStream in;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        in = new ByteArrayInputStream("<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\"><preferences><root type=\"user\"><map></map></root></preferences>".getBytes("UTF-8"));
        stream = new MockInputStream(in);
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stream.close();
    }

    public void testSystemNodeForPackage() throws BackingStoreException {
        Preferences p = null;
        try {
            p = Preferences.systemNodeForPackage(Object.class);
        } catch (SecurityException e) {
            // may be caused by absence of privileges on the underlying OS 
            return;
        }
        assertEquals("/java/lang", p.absolutePath());
        assertTrue(p instanceof AbstractPreferences);
        Preferences root = Preferences.systemRoot();
        Preferences parent = root.node("java");
        assertSame(parent, p.parent());
        assertFalse(p.isUserNode());
        assertEquals("lang", p.name());
        assertEquals("System Preference Node: " + p.absolutePath(), p
                .toString());

        assertEquals(0, p.childrenNames().length);
        assertEquals(0, p.keys().length);
        parent.removeNode();
        try {
            Preferences.userNodeForPackage(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testSystemRoot() throws BackingStoreException {
        Preferences p = Preferences.systemRoot();
        assertTrue(p instanceof AbstractPreferences);
        assertEquals("/", p.absolutePath());
        assertSame(null, p.parent());
        assertFalse(p.isUserNode());
        assertEquals("", p.name());
        assertEquals("System Preference Node: " + p.absolutePath(), p
                .toString());
    }

    public void testConsts() {
        assertEquals(80, Preferences.MAX_KEY_LENGTH);
        assertEquals(80, Preferences.MAX_NAME_LENGTH);
        assertEquals(8192, Preferences.MAX_VALUE_LENGTH);
    }

    public void testUserNodeForPackage() throws BackingStoreException {
        Preferences p = Preferences.userNodeForPackage(Object.class);
        assertEquals("/java/lang", p.absolutePath());
        assertTrue(p instanceof AbstractPreferences);
        Preferences root = Preferences.userRoot();
        Preferences parent = root.node("java");
        assertSame(parent, p.parent());
        assertTrue(p.isUserNode());
        assertEquals("lang", p.name());
        assertEquals("User Preference Node: " + p.absolutePath(), p.toString());
        assertEquals(0, p.childrenNames().length);
        assertEquals(0, p.keys().length);

        try {
            Preferences.userNodeForPackage(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void testUserRoot() throws BackingStoreException {
        Preferences p = Preferences.userRoot();
        assertTrue(p instanceof AbstractPreferences);
        assertEquals("/", p.absolutePath());
        assertSame(null, p.parent());
        assertTrue(p.isUserNode());
        assertEquals("", p.name());
        assertEquals("User Preference Node: " + p.absolutePath(), p.toString());
    }

    public void testImportPreferences() throws Exception {
        Preferences prefs = null;
        try {
            prefs = Preferences.userNodeForPackage(PreferencesTest.class);
            // assertEquals(0, prefs.childrenNames().length);
            // assertFalse(prefs.nodeExists("mock/child/grandson"));

            prefs.put("prefskey", "oldvalue");
            prefs.put("prefskey2", "oldvalue2");
            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs.xml");
            Preferences.importPreferences(in);

            prefs = Preferences.userNodeForPackage(PreferencesTest.class);
            assertEquals(1, prefs.childrenNames().length);
            assertTrue(prefs.nodeExists("mock/child/grandson"));
            assertEquals("newvalue", prefs.get("prefskey", null));
            assertEquals("oldvalue2", prefs.get("prefskey2", null));
            assertEquals("newvalue3", prefs.get("prefskey3", null));

            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs-badform.xml");
            try {
                Preferences.importPreferences(in);
                fail("should throw InvalidPreferencesFormatException");
            } catch (InvalidPreferencesFormatException e) {
                // Expected
            }

            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs-badtype.xml");
            try {
                Preferences.importPreferences(in);
                fail("should throw InvalidPreferencesFormatException");
            } catch (InvalidPreferencesFormatException e) {
                // Expected
            }

            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs-badencoding.xml");
            try {
                Preferences.importPreferences(in);
                fail("should throw InvalidPreferencesFormatException");
            } catch (InvalidPreferencesFormatException e) {
                // Expected
            }

            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs-higherversion.xml");
            try {
                Preferences.importPreferences(in);
                fail("should throw InvalidPreferencesFormatException");
            } catch (InvalidPreferencesFormatException e) {
                // Expected
            }

            in = PreferencesTest.class
                    .getResourceAsStream("/prefs/java/util/prefs/userprefs-ascii.xml");
            Preferences.importPreferences(in);
            prefs = Preferences.userNodeForPackage(PreferencesTest.class);
        } finally {
            try {
                prefs = Preferences.userNodeForPackage(PreferencesTest.class);
                prefs.removeNode();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    public void testImportPreferencesException() throws Exception {
        try {
            Preferences.importPreferences(null);
            fail("should throw MalformedURLException");
        } catch (MalformedURLException e) {
            // Expected
        }

        byte[] source = new byte[0];
        InputStream in = new ByteArrayInputStream(source);
        try {
            Preferences.importPreferences(in);
            fail("should throw InvalidPreferencesFormatException");
        } catch (InvalidPreferencesFormatException e) {
            // Expected
        }

        stream.setResult(MockInputStream.exception);
        try {
            Preferences.importPreferences(stream);
            fail("should throw IOException");
        } catch (IOException e) {
            // Expected
        }

        stream.setResult(MockInputStream.runtimeException);
        try {
            Preferences.importPreferences(stream);
            fail("should throw RuntimeException");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    public void testSecurity() throws InvalidPreferencesFormatException,
    IOException {
        try {
            manager.install();
            try {
                Preferences.userRoot();
                fail("should throw SecurityException");
            } catch (SecurityException e) {
                // Expected
            }
            try {
                Preferences.systemRoot();
                fail("should throw SecurityException");
            } catch (SecurityException e) {
                // Expected
            }
            try {
                Preferences.userNodeForPackage(null);
                fail("should throw SecurityException");
            } catch (SecurityException e) {
                // Expected
            }

            try {
                Preferences.systemNodeForPackage(null);
                fail("should throw SecurityException");
            } catch (SecurityException e) {
                // Expected
            }

            try {
                Preferences.importPreferences(stream);
                fail("should throw SecurityException");
            } catch (SecurityException e) {
                // Expected
            }
        } finally {
            manager.restoreDefault();
        }
    }

    static class MockInputStream extends InputStream {

        static final int normal = 0;

        static final int exception = 1;

        static final int runtimeException = 2;

        int result = normal;

        InputStream wrapper;

        public void setResult(int i) {
            result = i;
        }

        private void checkException() throws IOException {
            switch (result) {
            case normal:
                return;
            case exception:
                throw new IOException("test");
            case runtimeException:
                throw new RuntimeException("test");
            }
        }

        public MockInputStream(InputStream in) {
            wrapper = in;
        }

        @Override
        public int read() throws IOException {
            checkException();
            return wrapper.read();
        }
    }

    static class MockPreferences extends Preferences {

        public MockPreferences() {
            super();
        }

        @Override
        public String absolutePath() {
            return null;
        }

        @Override
        public String[] childrenNames() throws BackingStoreException {
            return null;
        }

        @Override
        public void clear() throws BackingStoreException {
        }

        @Override
        public void exportNode(OutputStream ostream) throws IOException,
        BackingStoreException {
        }

        @Override
        public void exportSubtree(OutputStream ostream) throws IOException,
        BackingStoreException {
        }

        @Override
        public void flush() throws BackingStoreException {
        }

        @Override
        public String get(String key, String deflt) {
            return null;
        }

        @Override
        public boolean getBoolean(String key, boolean deflt) {
            return false;
        }

        @Override
        public byte[] getByteArray(String key, byte[] deflt) {
            return null;
        }

        @Override
        public double getDouble(String key, double deflt) {
            return 0;
        }

        @Override
        public float getFloat(String key, float deflt) {
            return 0;
        }

        @Override
        public int getInt(String key, int deflt) {
            return 0;
        }

        @Override
        public long getLong(String key, long deflt) {
            return 0;
        }

        @Override
        public boolean isUserNode() {
            return false;
        }

        @Override
        public String[] keys() throws BackingStoreException {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public Preferences node(String name) {
            return null;
        }

        @Override
        public boolean nodeExists(String name) throws BackingStoreException {
            return false;
        }

        @Override
        public Preferences parent() {
            return null;
        }

        @Override
        public void put(String key, String value) {

        }

        @Override
        public void putBoolean(String key, boolean value) {

        }

        @Override
        public void putByteArray(String key, byte[] value) {

        }

        @Override
        public void putDouble(String key, double value) {

        }

        @Override
        public void putFloat(String key, float value) {

        }

        @Override
        public void putInt(String key, int value) {

        }

        @Override
        public void putLong(String key, long value) {

        }

        @Override
        public void remove(String key) {

        }

        @Override
        public void removeNode() throws BackingStoreException {

        }

        @Override
        public void addNodeChangeListener(NodeChangeListener ncl) {

        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {

        }

        @Override
        public void removeNodeChangeListener(NodeChangeListener ncl) {

        }

        @Override
        public void removePreferenceChangeListener(PreferenceChangeListener pcl) {

        }

        @Override
        public void sync() throws BackingStoreException {

        }

        @Override
        public String toString() {
            return null;
        }

    }

}

