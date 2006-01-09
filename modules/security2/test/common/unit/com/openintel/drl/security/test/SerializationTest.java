/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Alexey V. Varlamov
* @version $Revision$
*/

package com.openintel.drl.security.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Framework for serialization testing. Subclasses only need to override
 * getData() method and, optionally, assertDeserialized() method. The first one
 * returns array of objects to be de/serialized in tests, and the second
 * compares reference and deserialized objects (needed only if tested objects do
 * not provide specific method equals()). <br>
 * There are two modes of test run: <b>reference generation mode </b> and
 * <b>testing mode </b>. The actual mode is selected via
 * <b>&quot;test.mode&quot; </b> system property. The <b>testing mode </b> is
 * the default mode. <br>
 * To turn on the <b>reference generation mode </b>, the test.mode property
 * should be set to value &quot;serial.reference&quot;. In this mode, no testing
 * is performed but golden files are produced, which contain reference
 * serialized objects. This mode should be run on a pure 
 * Implementation classes, which are targeted for compartibility. <br>
 * The location of golden files (in both modes) is controlled via
 * <b>&quot;TEST_SRC_DIR&quot; </b> system property.
 * 
 */
public abstract class SerializationTest extends PerformanceTest {

    /**
     * Reference files generation mode switch.
     */
    public static final String SERIAL_REFERENCE_MODE = "serial.reference";

    /**
     * Key to a system property defining root location of golden files.
     */
    public static final String GOLDEN_PATH = "TEST_SRC_DIR";

    private static String outputPath = System.getProperty(GOLDEN_PATH,
                                                          "test/common/unit");

    /**
     * Parameterized c-tor inherited from superclass.
     */
    public SerializationTest(String name) {
        super(name);
    }

    /**
     * Default c-tor inherited from superclass.
     */
    public SerializationTest() {
        super();
    }

    /**
     * Depending on testing mode, produces golden files or performs testing.
     */
    public void runBare() throws Throwable {

        if (mode != null && mode.equals(SERIAL_REFERENCE_MODE)) {
            produceGoldenFiles();
        } else {
            super.runBare();
        }
    }

    /**
     * Returns zero value to exclude serialization tests from performance runs.
     */
    public long getRepeatCount() {
        return 0;
    }

    /**
     * This is the main working method of this framework. Subclasses must
     * override it to provide actual objects for testing.
     * 
     * @return array of objects to be de/serialized in tests.
     */
    protected abstract Object[] getData();

    /**
     * Compares deserialized and reference objects. This default implementation
     * just asserts equality of the two objects. Should be overriden if a class
     * under test does not provide specific equals() method and it's instances
     * should to be compared manually.
     */
    protected void assertDeserialized(Object reference, Object test) {
        assertEquals(reference, test);
    }

    /**
     * Tests that data objects can be serialized and deserialized without
     * exceptions, and that deserialization really produces deeply cloned
     * objects.
     */
    public void testSelf() throws Throwable {
        Object[] data = getData();
        for (int i = 0; i < data.length; i++) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            putObjectToStream(data[i], bos);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos
                .toByteArray());
            assertDeserialized(data[i], getObjectFromStream(bis));
        }
    }

    /**
     * Tests that data objects can be deserialized from golden files, to verify
     * compartibility with Reference Implementation.
     */
    public void testGolden() throws Throwable {
        Object[] data = getData();
        for (int i = 0; i < data.length; i++) {
            assertDeserialized(data[i],
                               getObjectFromStream(new FileInputStream(
                                   getDataFile(i))));
        }
    }

    /**
     * Returns golden file for an object being tested.
     * 
     * @param index array index of tested data (as returned by
     *        {@link #getData() getData()})
     * @return corresponding golden file
     */
    protected File getDataFile(int index) {
        String name = this.getClass().getName();
        int dot = name.lastIndexOf(".");
        String path = name.substring(0, dot).replace('.', File.separatorChar);
        if (outputPath != null && outputPath.length() != 0) {
            path = outputPath + File.separator + path;
        }

        return new File(path, name.substring(dot + 1) + "." + index + ".dat");
    }

    /**
     * Working method for files generation mode. Serializes test objects
     * returned by {@link #getData() getData()}to golden files, each object to
     * a separate file.
     * 
     * @throws IOException
     */
    protected void produceGoldenFiles() throws IOException {
        Object[] data = getData();
        for (int i = 0; i < data.length; i++) {
            File gf = getDataFile(i);
            gf.getParentFile().mkdirs();
            gf.createNewFile();
            putObjectToStream(data[i], new FileOutputStream(gf));
        }
    }

    /**
     * Serializes specified object to an output stream.
     */
    protected void putObjectToStream(Object obj, OutputStream os)
        throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }

    /**
     * Deserializes single object from an input stream.
     */
    protected Object getObjectFromStream(InputStream is) throws IOException,
        ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}