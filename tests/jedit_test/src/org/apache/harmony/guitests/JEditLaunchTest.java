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

/**
 * This test just checks that jEdit application can be launched.
 * The test scenario (script) is played using Abbot Java GUI test
 * automation framework (see http://sourceforge.net/projects/abbot).
 */
public class JEditLaunchTest extends ScriptFixture {

    public JEditLaunchTest(String name) {
        super(System.getProperty("test.scripts.dir") + "/jedit_ln_00.xml");
    }
    
    public void test() throws Throwable {
        runTest();
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JEditLaunchTest.class);
    }
}
