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
/**
 */
package org.apache.harmony.test.func.api.javax.management.timer;
import org.apache.harmony.share.Result;
public class LibTimer {
    
    //constants
    static final String TIMER_TYPE = "TestType";
    static final String TEST_MESSAGE = "TestMessage";
    static final String TEST_DATA = "TestData";
    public static final long PERIOD = 100;
    public static final long WAIT = 10000;
     // The number of repeats for occurances tests 
    public static final long REPEATS = 2;
    
    public static final int     PASS     = Result.PASS;
    public static final int     FAIL     = Result.FAIL;
    public static void sleep(long ms) throws Exception {
        Thread.currentThread().sleep(ms);
    }
}
    
  

