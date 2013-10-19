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
package org.apache.harmony.test.func.share.providers.java.lang.share;


public class ArraysToTest {

    public static int ARRAY1_LENGTH = 2;
    public static int ARRAY2_LENGTH = 3;
    public static int ARRAY3_LENGTH = 2;


    public static Object getArray1() {
        return new int[]{1,2};
    }
    public static Object getArray2() {
        return new int[]{3,4,5};
    }
    public static Object getArray3() {
        return new String[] {"1","2"};
    }

    public static boolean check(Object obj) {
        int[] array = (int[]) obj;
        return array.equals( new int[]{1,2,5} )   ||
               array.equals( new int[]{3,1,2} ) ;
    }
}
