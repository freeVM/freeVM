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
package org.apache.harmony.unpack200.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

import org.apache.harmony.unpack200.Segment;

/**
 * Tests for org.apache.harmony.unpack200.Segment.
 */
public class SegmentTest extends TestCase {

    InputStream in;
    JarOutputStream out;
    File file;

    protected void tearDown() throws Exception {
        super.tearDown();
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
    }

    public void testJustResources() throws Exception {
        in = Segment.class
                .getResourceAsStream("/org/apache/harmony/pack200/tests/JustResources.pack");
        file = File.createTempFile("just", "resources.jar");
        out = new JarOutputStream(new FileOutputStream(file));
        Segment segment = new Segment();
        segment.unpack(in, out);
    }

    public void testInterfaceOnly() throws Exception {
        in = Segment.class
                .getResourceAsStream("/org/apache/harmony/pack200/tests/InterfaceOnly.pack");
        file = File.createTempFile("Interface", "Only.jar");
        out = new JarOutputStream(new FileOutputStream(file));
        Segment segment = new Segment();
        segment.unpack(in, out);
    }

    public void testHelloWorld() throws Exception {
        in = Segment.class
                .getResourceAsStream("/org/apache/harmony/pack200/tests/HelloWorld.pack");
        file = File.createTempFile("hello", "world.jar");
        out = new JarOutputStream(new FileOutputStream(file));
        Segment segment = new Segment();
        segment.unpack(in, out);
        out.close();
        out = null;
        JarFile jarFile = new JarFile(file);
        file.deleteOnExit();

        JarEntry entry = jarFile
                .getJarEntry("org/apache/harmony/archive/tests/internal/pack200/HelloWorld.class");
        assertNotNull(entry);
        InputStream ours = jarFile.getInputStream(entry);

        JarFile jarFile2 = new JarFile(new File(Segment.class.getResource(
                "/org/apache/harmony/pack200/tests/hw.jar").toURI()));
        JarEntry entry2 = jarFile2
                .getJarEntry("org/apache/harmony/archive/tests/internal/pack200/HelloWorld.class");
        assertNotNull(entry2);

        InputStream expected = jarFile2.getInputStream(entry2);

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(ours));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(expected));
        String line1 = reader1.readLine();
        String line2 = reader2.readLine();
        int i = 1;
        while (line1 != null || line2 != null) {
            assertEquals("Unpacked class files differ", line2, line1);
            line1 = reader1.readLine();
            line2 = reader2.readLine();
            i++;
        }
        reader1.close();
        reader2.close();

//        try {
//            Process process = Runtime
//                    .getRuntime()
//                    .exec(
//                            "java -cp "
//                                    + file.getName()
//                                    + " org.apache.harmony.archive.tests.internal.pack200.HelloWorld",
//                            new String[] {}, file.getParentFile());
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    process.getInputStream()));
//            String line = reader.readLine();
//            assertEquals("Hello world", line);
//            reader.close();
//
//            Process process2 = Runtime
//                    .getRuntime()
//                    .exec(
//                            "javap -c -verbose -classpath "
//                                    + file.getName()
//                                    + " org.apache.harmony.archive.tests.internal.pack200.HelloWorld",
//                            new String[] {}, file.getParentFile());
//            BufferedReader reader1 = new BufferedReader(new InputStreamReader(process2
//                    .getInputStream()));
//            InputStream javapCompareFile = Segment.class
//                    .getResourceAsStream("/org/apache/harmony/pack200/tests/HelloWorldJavap.out");
//            BufferedReader reader2 = new BufferedReader(new InputStreamReader(
//                    javapCompareFile));
//            String line1 = readNextLine(reader1);
//            String line2 = readNextLine(reader2);
//            int i = 1;
//            while (line1 != null || line2 != null) {
//                assertEquals(line2, line1);
//                line1 = readNextLine(reader1);
//                line2 = readNextLine(reader2);
//                i++;
//            }
//            reader1.close();
//            reader2.close();
//        } catch (IOException e) {
//            String message = e.getMessage();
//            if (message.startsWith("Unable to start program") || message.startsWith("The creation of the Process has just failed")) {
//                System.out.println("Warning: org.apache.harmony.unpack200.tests.SegmentTest.testHelloWorld() was not completed as java or javap could not be found");
//            } else {
//                throw e;
//            }
//        }
    }

    private String readNextLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while ("".equals(line)) {
            line = reader.readLine();
        }
        return line;
    }

}
