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
package org.apache.harmony.guitests;

import junit.extensions.abbot.ScriptFixture;
import junit.framework.TestCase;

/**
 * This test launches jEdit application and checks that some work
 * scenario can be performed in jEdit given number of times (iterations).
 * The scenario (script) is played using Abbot Java GUI test automation
 * framework (see http://sourceforge.net/projects/abbot).
 */
public class JEditStressTest extends TestCase {
    private final String scriptsDir = System.getProperty("test.scripts.dir");
    // test prolog script must be invoked once
    private final RepeatableScript prolog =
        new RepeatableScript(scriptsDir + "/jedit_st_00.xml");
    // test body script must be invoked specified
    // number of times (ITERATIONS_NUMBER)
    private final RepeatableScript loop =
        new RepeatableScript(scriptsDir + "/jedit_st_01.xml");
    // test body iterations number
    private final int ITERATIONS_NUMBER =
        Integer.parseInt(System.getProperty("JEditStressTest.it_num", "1"));
    
    public JEditStressTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        prolog.setUp();
        loop.setUp();
    }

    protected void tearDown() throws Exception {
        loop.tearDown();
        prolog.tearDown();
    }

    public void test() throws Throwable {
        prolog.runTest(1);
        loop.runTest(ITERATIONS_NUMBER);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JEditStressTest.class);
    }

    //
    // ScriptFixture Wrapper.
    // Makes underlying test(script) repeatable.
    //
    private static class RepeatableScript extends ScriptFixture {
        RepeatableScript(String name) {
            super(name);
        }
        
        protected void setUp() throws Exception {
            super.setUp();
        }

        protected void tearDown() throws Exception {
            super.tearDown();
        }

        void runTest(int itNumber) throws Throwable {
            for (int i=0; i<itNumber; i++) {
                super.runTest();
            }
        }
    }
}
