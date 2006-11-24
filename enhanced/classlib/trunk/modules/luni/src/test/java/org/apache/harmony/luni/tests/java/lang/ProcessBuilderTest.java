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

package org.apache.harmony.luni.tests.java.lang;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ProcessBuilderTest extends TestCase {

    public void testProcessBuilderStringArray() {

    }

    public void testProcessBuilderListOfString() {
        try {
            new ProcessBuilder((List<String>) null);
            fail("no null pointer exception");
        } catch (NullPointerException e) {
        }
    }

    public void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertEquals(1, pb.command().size());
        assertEquals("command", pb.command().get(0));
    }

    public void testCommandStringArray() {
        ProcessBuilder pb = new ProcessBuilder("command");
        ProcessBuilder pbReturn = pb.command("cmd");
        assertSame(pb, pbReturn);
        assertEquals(1, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
    }
    
    public void testCommandListOfString() {
        ProcessBuilder pb = new ProcessBuilder("command");
        List<String> newCmd = new ArrayList<String>();
        newCmd.add("cmd");
        ProcessBuilder pbReturn = pb.command(newCmd);
        assertSame(pb, pbReturn);
        assertEquals(1, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
        
        newCmd.add("arg");
        assertEquals(2, pb.command().size());
        assertEquals("cmd", pb.command().get(0));
        assertEquals("arg", pb.command().get(1));
    }

    public void testDirectory() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertNull(pb.directory());
    }

    public void testDirectoryFile() {
        ProcessBuilder pb = new ProcessBuilder("command");
        File dir = new File(System.getProperty("java.io.tmpdir"));
        ProcessBuilder pbReturn = pb.directory(dir);
        assertSame(pb, pbReturn);
        assertEquals(dir, pb.directory());
        
        pbReturn = pb.directory(null);
        assertSame(pb, pbReturn);
        assertNull(pb.directory());
    }

    public void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("command");
        Map<String, String> env = pb.environment();
        assertEquals(System.getenv(), env);
        env.clear();
        env = pb.environment();
        assertTrue(env.isEmpty());
        try {
            env.put(null,"");
            fail("should throw NPE.");
        } catch (NullPointerException e) {
            // expected;
        }
        try {
            env.put("",null);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
            // expected;
        }
        try {
            env.get(null);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
            // expected;
        }
        try {
            env.get(new Object());
            fail("should throw ClassCastException.");
        } catch (ClassCastException e) {
            // expected;
        }
    }

    public void testRedirectErrorStream() {
        ProcessBuilder pb = new ProcessBuilder("command");
        assertFalse(pb.redirectErrorStream());
    }

    public void testRedirectErrorStreamBoolean() {
        ProcessBuilder pb = new ProcessBuilder("command");
        ProcessBuilder pbReturn = pb.redirectErrorStream(true);
        assertSame(pb, pbReturn);
        assertTrue(pb.redirectErrorStream());
    }

    public void testStart() {
    }
}
