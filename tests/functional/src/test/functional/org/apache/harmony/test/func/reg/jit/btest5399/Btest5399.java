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
 
package org.apache.harmony.test.func.reg.jit.btest5399;

import org.apache.harmony.test.share.reg.RegressionTest;

public class Btest5399 extends RegressionTest {   

    public static void main(String[] args) throws Exception {
        System.exit(new Btest5399().test(args));
    }
    
    public int test(String [] args) {
        boolean ret = true;
        
        try {
            System.err.println("Verify private method invocation...");
            int tt = new Test5399_2().test(args);
            if (tt != 104) {
                System.err.println("Testcase failed: " + tt);
                ret = false;
            } else {
                System.err.println("Testcase passed");
            }
        } catch (IllegalAccessError e) {
            System.err.println("Expected exception was thrown: " + e);
            System.err.println("Testcase passed");
        } catch (Throwable e) {
            System.err.println("Unexpected exception was thrown: " + e);
            System.err.println("Testcase failed");
            ret = false;
        }
        System.err.println();   
        
        try { 
            System.err.println("Verify package access method invocation...");
            int tt = new Test5399_4().test(args);
            if (tt != 104) {
                System.err.println("Testcase failed: " + tt);
                ret = false;
            } else {
                System.err.println("Testcase passed");
            }
        } catch (IllegalAccessError e) {
            System.err.println("Expected exception was thrown: " + e);
            System.err.println("Testcase passed");
        } catch (Throwable e) {
            System.err.println("Unexpected exception was thrown: " + e);
            System.err.println("Testcase failed");
            ret = false;
        }
        
        return ret ? pass() : fail();
    }
}
