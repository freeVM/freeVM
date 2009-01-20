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
package org.apache.harmony.pack200.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

import org.apache.harmony.pack200.Archive;
import org.apache.harmony.pack200.Pack200Exception;
import org.apache.harmony.unpack200.Segment;

public class ArchiveTest extends TestCase {

    JarInputStream in;
    OutputStream out;
    File file;

    public void testHelloWorld() throws IOException, Pack200Exception, URISyntaxException {
        in = new JarInputStream(
                Archive.class
                        .getResourceAsStream("/org/apache/harmony/pack200/tests/hw.jar"));
        file = File.createTempFile("helloworld", ".pack.gz");
        out = new FileOutputStream(file);
        new Archive(in, out, true).pack();
        in.close();
        out.close();

        // now unpack
        InputStream in2 = new FileInputStream(file);
        File file2 = File.createTempFile("helloworld", ".jar");
        JarOutputStream out2 = new JarOutputStream(new FileOutputStream(file2));
        org.apache.harmony.unpack200.Archive archive = new org.apache.harmony.unpack200.Archive(
                in2, out2);
        archive.unpack();
        out2.close();
        in2.close();

        JarFile jarFile = new JarFile(file2);
        file2.deleteOnExit();
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
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(
                expected));
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
    }

    public void testSQL() throws IOException, Pack200Exception, URISyntaxException {
        in = new JarInputStream(Archive.class
                .getResourceAsStream("/org/apache/harmony/pack200/tests/sqlUnpacked.jar"));
        file = File.createTempFile("sql", ".pack");
        out = new FileOutputStream(file);
        new Archive(in, out, false).pack();
        in.close();
        out.close();

        // now unpack
        InputStream in2 = new FileInputStream(file);
        File file2 = File.createTempFile("sqlout", ".jar");
        JarOutputStream out2 = new JarOutputStream(new FileOutputStream(file2));
        org.apache.harmony.unpack200.Archive archive = new org.apache.harmony.unpack200.Archive(in2, out2);
        archive.unpack();
        JarFile jarFile = new JarFile(file2);
        file2.deleteOnExit();

        JarFile jarFile2 = new JarFile(new File(Archive.class.getResource(
                "/org/apache/harmony/pack200/tests/sqlUnpacked.jar").toURI()));

        compareFiles(jarFile, jarFile2);
    }

    public void testJNDI() throws IOException, Pack200Exception, URISyntaxException {
        in = new JarInputStream(Archive.class
                .getResourceAsStream("/org/apache/harmony/pack200/tests/jndi.jar"));
        file = File.createTempFile("jndi", ".pack");
        out = new FileOutputStream(file);
        new Archive(in, out, false).pack();
        in.close();
        out.close();

        // now unpack
        InputStream in2 = new FileInputStream(file);
        File file2 = File.createTempFile("jndiout", ".jar");
        JarOutputStream out2 = new JarOutputStream(new FileOutputStream(file2));
        org.apache.harmony.unpack200.Archive archive = new org.apache.harmony.unpack200.Archive(in2, out2);
        archive.unpack();
        JarFile jarFile = new JarFile(file2);
        file2.deleteOnExit();
        JarFile jarFile2 = new JarFile(new File(Archive.class.getResource(
                "/org/apache/harmony/pack200/tests/jndiUnpacked.jar").toURI()));

        compareFiles(jarFile, jarFile2);
    }

    public void testSegmentLimits() throws IOException, Pack200Exception {
        in = new JarInputStream(
                Archive.class
                        .getResourceAsStream("/org/apache/harmony/pack200/tests/hw.jar"));
        file = File.createTempFile("helloworld", ".pack.gz");
        out = new FileOutputStream(file);
        Archive archive = new Archive(in, out, true);
        archive.setSegmentLimit(1);
        try {
            archive.pack();
            fail("Should throw an execption with a 1-byte segment limit");
        } catch (Pack200Exception pe) {
            assertEquals("Expected limit too small message", "Segment limit is too small for the files you are trying to pack", pe.getMessage());
        }
        in.close();
        out.close();

        in = new JarInputStream(
                Archive.class
                        .getResourceAsStream("/org/apache/harmony/pack200/tests/hw.jar"));
        file = File.createTempFile("helloworld", ".pack.gz");
        out = new FileOutputStream(file);
        archive = new Archive(in, out, true);
        archive.setSegmentLimit(0);
        archive.pack();
        in.close();
        out.close();

        in = new JarInputStream(
                Archive.class
                        .getResourceAsStream("/org/apache/harmony/pack200/tests/hw.jar"));
        file = File.createTempFile("helloworld", ".pack.gz");
        out = new FileOutputStream(file);
        archive = new Archive(in, out, true);
        archive.setSegmentLimit(-1);
        archive.pack();
        in.close();
        out.close();

        in = new JarInputStream(
                Archive.class
                        .getResourceAsStream("/org/apache/harmony/pack200/tests/hw.jar"));
        file = File.createTempFile("helloworld", ".pack.gz");
        out = new FileOutputStream(file);
        archive = new Archive(in, out, true);
        archive.setSegmentLimit(5000);
        archive.pack();
        in.close();
        out.close();
    }

    private void compareFiles(JarFile jarFile, JarFile jarFile2)
            throws IOException {
        Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {

            JarEntry entry = (JarEntry) entries.nextElement();
            assertNotNull(entry);

            String name = entry.getName();
            JarEntry entry2 = jarFile2.getJarEntry(name);
            assertNotNull(entry2);
            if(!name.equals("META-INF/MANIFEST.MF")) { // Manifests aren't necessarily byte-for-byte identical

                InputStream ours = jarFile.getInputStream(entry);
                InputStream expected = jarFile2.getInputStream(entry2);

                BufferedReader reader1 = new BufferedReader(new InputStreamReader(ours));
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(
                        expected));
                String line1 = reader1.readLine();
                String line2 = reader2.readLine();
                int i = 1;
                while (line1 != null || line2 != null) {
                    assertEquals("Unpacked files differ for " + name, line2, line1);
                    line1 = reader1.readLine();
                    line2 = reader2.readLine();
                    i++;
                }
                reader1.close();
                reader2.close();
            }
        }
    }

}
