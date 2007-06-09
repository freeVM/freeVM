/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */    

/**
 * @author: Vera Y.Petrashkova
 * @version: $Revision: 1.6 $
 */
package org.apache.harmony.test.stress.classloader.share.WrongClasses;

import org.apache.harmony.test.share.stress.ReliabilityRunner;

public class testCFE_RInt_00 extends testCFE_RInt {

    public static int cnt = 0;

    public int test(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if(args[i] != null) {
                System.setProperty("org.apache.harmony.test."
                        + "stress.classloader.share."
                        + "WrongClasses.testCFE_RInt.arg" + i, args[i]);
            }
        }
        super.test();
        return ReliabilityRunner.RESULT_PASS;
    }
}
