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


package org.apache.harmony.test.func.api.java.lang.ThreadGroup.destroy.destroy10.destroy1010;

import org.apache.harmony.share.Test;


import org.apache.harmony.test.func.share.MyLog;

public class destroy1010 extends Test {

    boolean  results[] = new boolean[100];
    String  logArray[] = new String[100];
    int     logIndex   = 0; 
        Object dObjects[] = { null, new Object(), new Object() };

    void addLog(String s) {
        if ( logIndex < logArray.length )
            logArray[logIndex] = s;
        logIndex++;
    }

    void destroy1010() {

            ThreadGroup tgObjects[] = { null,
                                       new ThreadGroup("tg1"),
                                       new ThreadGroup("tg1"),
                                       new ThreadGroup("tg2"),
                                       null, null, null
                                     };
                       tgObjects[4] =   new ThreadGroup(tgObjects[2], "tg1");
                       tgObjects[5] =   new ThreadGroup(tgObjects[3], "tg1");
                       tgObjects[6] =   new ThreadGroup(tgObjects[3], "tg2");

            ThreadGroup tgObjects2[] = { null,
                                       new ThreadGroup("tg1"),
                                       new ThreadGroup("tg1"),
                                       new ThreadGroup("tg2"),
                                       null, null, null
                                     };
                       tgObjects2[4] =   new ThreadGroup(tgObjects2[2], "tg1");
                       tgObjects2[5] =   new ThreadGroup(tgObjects2[3], "tg1");
                       tgObjects2[6] =   new ThreadGroup(tgObjects2[3], "tg2");
            label: {
                Threaddestroy1010 t1[] = { null,
                               new Threaddestroy1010(tgObjects[1], "t11"),
                               new Threaddestroy1010(tgObjects[2], "t12"),
                               new Threaddestroy1010(tgObjects[3], "t13"),
                               new Threaddestroy1010(tgObjects[4], "t14"),
                               new Threaddestroy1010(tgObjects[5], "t15"),
                               new Threaddestroy1010(tgObjects[6], "t16")
                             };
//-1
                for ( int i = 1; i < tgObjects.length; i++ ) {
                    try {
                        tgObjects[i].setDaemon(false);
                        tgObjects[i].destroy();
                        results[i] |= false;
                    } catch (IllegalThreadStateException e) {
                    }
                }
//-1)
//-2
                synchronized(dObjects[2]) {
                    synchronized(dObjects[1]) {
                        for (int j = 1; j < t1.length; j++ ) {
                            try {
                                t1[j].start();
                                dObjects[1].wait(60000);
                            } catch (InterruptedException e) {
                                addLog("ERROR: unexpectead InterruptedException");
                                results[results.length -1] = false;
                                break label;
                            }
                        }
                    }
                }
                for ( int j = t1.length -1; j > 0; j-- ) {
                    try {
                        t1[j].join();
                    } catch (InterruptedException e) {
                        addLog("ERROR: unexpectead InterruptedException");
                        results[results.length -1] = false;
                        break label;
                    }
                    try {
                        tgObjects[j].destroy();
                    } catch (IllegalThreadStateException e) {
                        results[j+7] |= false;
                    }
                }
//-2)
//-3
                for ( int j = t1.length -1; j > 0; j-- ) {
                    try {
                        tgObjects[j].destroy();
                        results[j+14] |= false;
                    } catch (IllegalThreadStateException e) {
                    }
                }
//-3)
//-4
            Threaddestroy1010 t2[] = { null,
                           new Threaddestroy1010(tgObjects2[1], "t21"),
                           new Threaddestroy1010(tgObjects2[2], "t22"),
                           new Threaddestroy1010(tgObjects2[3], "t23"),
                           new Threaddestroy1010(tgObjects2[4], "t24"),
                           new Threaddestroy1010(tgObjects2[5], "t25"),
                           new Threaddestroy1010(tgObjects2[6], "t26")
                         };

                boolean expected[][] = {
                                        {  true, false, false, false, false, false },
                                        { false, false, false, false, false, false },
                                        { false, false, false, false, false, false },
                                        { false,  true, false,  true, false, false },
                                        { false, false, false, false,  true, false },
                                        { false, false,  true, false, false,  true },
                                       };

                for ( int i = 1; i < tgObjects2.length; i++ ) {
                    tgObjects2[i].setDaemon(false);
                }

                synchronized(dObjects[1]) {
                    for (int j = 1; j < t2.length; j++ ) {


                        try {
                            t2[j].start();
                            dObjects[1].wait(60000);
                            t2[j].join();
                        } catch (InterruptedException e) {
                            addLog("ERROR: unexpectead InterruptedException");
                            results[results.length -1] = false;
                            break label;
                        }
                        for ( int k = 1; k < tgObjects2.length; k++ ) {
                            try {
                                tgObjects2[j].destroy();
                                results[j+21] |= expected[j-1][k-1];
                            } catch (IllegalThreadStateException e) {
                                results[j+21] |= ! expected[j-1][k-1];
                            }
                        }
                    }
                }
//-4)
            } //label:
        return ;
    }

class Threaddestroy1010 extends Thread {
    Threaddestroy1010(ThreadGroup tg, String s) {super(tg, s);}
    public void run() {
        synchronized(dObjects[1]) {
            dObjects[1].notify();
        }
        synchronized(dObjects[2]) {
        }
    }
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

            addLog("*********  Test destroy1010 begins ");
destroy1010();
            addLog("*********  Test destroy1010 results: ");

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
        System.exit(new destroy1010().test());
    }
}



