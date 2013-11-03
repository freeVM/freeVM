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

package org.apache.harmony.test.func.api.java.lang.Integer;

import org.apache.harmony.share.MultiCase;
import org.apache.harmony.share.Result;

public class IntegerTest extends MultiCase {
    public static void main(String[] args) {
        System.exit(new IntegerTest().test(args));
    }

    public static final int[] ints = {
        Integer.MIN_VALUE,
        -65537,
        -48
        -1,
        0,
        1,
        5,
        1000000,
        Integer.MAX_VALUE
    };

   /*
    * java.lang.Integer.compareTo(Object)
    */
    public Result testCompareTo() {
        Integer[] Ints = new Integer[ints.length];
        
        for (int i = 0; i < ints.length; i++) {
            Ints[i] = new Integer(ints[i]);
        }
        
        for (int i = 0; i < ints.length; i++) {
            for (int j = 0; j < ints.length; j++) {
                if (cmpz(Ints[i].compareTo(Ints[j])) != cmp(ints[i], ints[j])) {
                    return failed("Wrong compareTo result: " 
                        + Ints[i].toString() + " compareTo "
                        + Ints[j].toString() + " = " 
                        + Ints[i].compareTo(Ints[j]));
                }
            }
        }
        return passed("OK");
    }

    int cmp(int a, int b) {
        return a==b ? 0 : a>b ? 1 : -1;
    }
    
    int cmpz(int a) {
        return cmp(a, 0);
    }
}
