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

/*
*/


package org.apache.harmony.test.func.api.java.lang.reflect.Array.getBoolean.getBoolean10.getBoolean1010;


import java.lang.reflect.Array;

import org.apache.harmony.share.Test;
import org.apache.harmony.test.func.share.MyLog;


public class getBoolean1010 extends Test {

    boolean  results[] = new boolean[100];
    String  logArray[] = new String[100];
    int     logIndex   = 0; 

    boolean myObjects[] = { true, false};

    Object myObject = (Object) new String("ObjectToTest");

    void addLog(String s) {
        if ( logIndex < logArray.length )
            logArray[logIndex] = s;
        logIndex++;
    }

    void getBoolean1010() {

       int i = 0;     
//-1
        try {
            if ( Array.getBoolean(myObjects, 0) != true ) {
                addLog("ERROR: testcase1: true");
                results[i] = false;
            }
            i++;
            if ( Array.getBoolean(myObjects, 1) != false ) {
                addLog("ERROR: testcase1: false");
                results[i] = false;
            }
        } catch (Exception e) {
            addLog("ERROR: EXCEPTION: testcase1: " + e);
            results[i] = false;
        }
//-1)
//-2
        i++;
        try { Array.getBoolean(null, 1);
            addLog("ERROR: testcase2: ");
            results[i] = false;
        } catch ( NullPointerException e ) {
            addLog("correct NullPointerException");
        }
//-2)
//-3
        i++;
        try { Array.getBoolean(myObjects, myObjects.length);
            addLog("ERROR: testcase3: ");
            results[i] = false;
        } catch ( ArrayIndexOutOfBoundsException e ) {
            addLog("correct ArrayIndexOutOfBoundsException");
        }
//-3)
//-4
        i++;
        try { Array.getBoolean(myObjects, -1);
            addLog("ERROR: testcase4: ");
            results[i] = false;
        } catch ( ArrayIndexOutOfBoundsException e ) {
            addLog("correct ArrayIndexOutOfBoundsException");
        }
//-4)
//-5
        i++;
        try { Array.getBoolean(myObject, 1);
            addLog("ERROR: testcase5: ");
            results[i] = false;
        } catch ( IllegalArgumentException e ) {
            addLog("correct IllegalArgumentException");
        }
//-5)
        return ;
    }

    public int test() {

        logIndex = 0;

        String texts[] = { "Testcase FAILURE, results[#] = " ,
                           "Test P A S S E D"                ,
                           "Test F A I L E D"                ,
                           "#### U N E X P E C T E D : "     };

        int    failed   = 105;
        int    passed   = 104;
        int  unexpected = 106;

        int    toReturn = 0;
        String toPrint  = null;

        for ( int i = 0; i < results.length; i++ )
            results[i] = true;

        try {

            addLog("*********  Test getBoolean1010 begins ");
getBoolean1010();
            addLog("*********  Test getBoolean1010 results: ");

            boolean result = true;
            for ( int i = 1 ; i < results.length ; i++ ) {
                result &= results[i];
                if ( ! results[i] )
                    addLog(texts[0] + i);
            }
            if ( ! result ) {
                toPrint  = texts[2];
                toReturn = failed;
            }
            if ( result ) {
                toPrint  = texts[1];
                toReturn = passed;
            }
        } catch (Exception e) {
            toPrint  = texts[3] + e;
            toReturn = unexpected;
        }
        if ( toReturn != passed )
            for ( int i = 0; i < logIndex; i++ )
                MyLog.toMyLog(logArray[i]);

        MyLog.toMyLog(toPrint);
        return toReturn;
    }

    public static void main(String args[]) {
        System.exit(new getBoolean1010().test());
    }
}



