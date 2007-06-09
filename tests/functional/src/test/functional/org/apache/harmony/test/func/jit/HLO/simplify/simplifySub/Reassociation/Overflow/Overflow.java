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
package org.apache.harmony.test.func.jit.HLO.simplify.simplifySub.Reassociation.Overflow;

import org.apache.harmony.share.MultiCase;
import org.apache.harmony.share.Result;

/**
 */

/*
 * Created on 26.06.2006
 */

public class Overflow extends MultiCase {
    
        int varInt = -100;
        byte varByte = 1;
        long varLong = Long.MIN_VALUE;
        short varShort = 100;

        /* Testing reassociation of the following expressions:
           (C1 - s1) - C2 -> (C1 - C2) - s1
           (s1 - C1) - C2 -> s1 + (-(C1 + C2))
           (s + C1) - C2 -> s + (C1 - C2)
           (C1 + s) - C2 -> s + (C1 - C2)
           C1 - (C2 - s) -> (C1-C2) + s
           C1 - (s - C2) -> (C1+C2) � s 
           C1 - (s + C2) -> (C1-C2) - s
           C1 - (C2 + s) -> (C1-C2) � s */
        
        public static void main(String[] args) {
            log.info("Start Overflow simplification test...");
            System.exit((new Overflow()).test(args));
        }
        
        public Result test1() {
            log.info("Test1 simplifying (C1 - s1) - C2 -> (C1 - C2) - s1 :");
            final int constInt1 = -101;
            final int constInt2 = Integer.MAX_VALUE;
            int result =  (constInt1 - varInt) - constInt2;
            log.info("result = " + result);
            if (result == Integer.MIN_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Integer.MIN_VALUE);
        }
        
        public Result test2() {
            log.info("Test2 simplifying  (s1 - C1) - C2 -> s1 + (-(C1 + C2)) :");
            final byte constByte1 = 2;
            final byte constByte2 = Byte.MAX_VALUE;
            byte result = (byte) ((varByte - constByte1) - constByte2);
            log.info("result = " + result);
            if (result == Byte.MIN_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Byte.MIN_VALUE);
        }
        
        public Result test3() {
            log.info("Test3 simplifying (s + C1) - C2 -> s + (C1 - C2) :");
            final long constLong1 = 1L;
            final long constLong2 = -Long.MAX_VALUE;
            long result = (varLong + constLong1) - constLong2;
            log.info("result = " + result);
            if (result == 0L) return passed();
            else return failed("TEST FAILED: result != " + 0);
        }
        
        public Result test4() {
            log.info("Test4 simplifying (C1 + s) - C2 -> s + (C1 - C2) :");
            final short constShort1 = -101;
            final short constShort2 = Short.MIN_VALUE;
            short result = (short)((constShort1 + varShort) - constShort2);
            log.info("result = " + result);
            if (result == Short.MAX_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Short.MAX_VALUE);
        }
        
        public Result test5() {
            log.info("Test5 simplifying C1 - (C2 - s) -> (C1-C2) + s :");
            final int constInt1 = Integer.MAX_VALUE;
            final int constInt2 = -100;
            int result =  constInt1 - (constInt2 - varInt);
            log.info("result = " + result);
            if (result == Integer.MAX_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Integer.MAX_VALUE);
        }
        
        public Result test6() {
            log.info("Test6 simplifying  C1 - (s - C2) -> (C1+C2) - s :");
            final byte constByte1 = 1;
            final byte constByte2 = Byte.MAX_VALUE;
            byte result = (byte) (constByte1 - (varByte - constByte2));
            log.info("result = " + result);
            if (result == Byte.MAX_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Byte.MAX_VALUE);
        }
        
        public Result test7() {
            log.info("Test7 simplifying C1 - (s + C2) -> (C1-C2) - s :");
            final long constLong1 = -1L;
            final long constLong2 = Long.MAX_VALUE;
            long result = constLong1 - (varLong + constLong2);
            log.info("result = " + result);
            if (result == 0L) return passed();
            else return failed("TEST FAILED: result != " + 0);
        }
        
        public Result test8() {
            log.info("Test8 simplifying C1 - (C2 + s) -> (C1-C2) - s :");
            final short constShort1 = 100;
            final short constShort2 = Short.MIN_VALUE;
            short result = (short)(constShort1 - (constShort2 + varShort));
            log.info("result = " + result);
            if (result == Short.MIN_VALUE) return passed();
            else return failed("TEST FAILED: result != " + Short.MIN_VALUE);
        }
}
