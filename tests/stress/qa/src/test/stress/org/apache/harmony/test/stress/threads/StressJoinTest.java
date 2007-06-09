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
package org.apache.harmony.test.stress.vm.threads;

import org.apache.harmony.test.stress.vm.threads.share.StressTestParameters;
import org.apache.harmony.test.stress.vm.threads.share.StressTestThreadSO;

import org.apache.harmony.share.Test;
import org.apache.harmony.share.DRLLogging;

/**
 * @author Alexander D. Shipilov
 * @version $Revision: 1.3 $
 */

public class StressJoinTest extends Test {

    /**
     * Fail flag.
     */
    static boolean failFlag = false;

    /**
     * String with fail info.
     */
    static String failInfo;

    /**
     * Threads will be register in this array that they worked fine.
     */
    static int verificationArray[];

    /**
     * Array of threads.
     */
    static StressJoinTestThread threads[];

    /**
     * Leader thread.
     */
    static StressJoinTestLeader leader = null;

    /**
     * Ballast object to prevent repeated OOME.
     */
    static Object ballastObj = new int[100][100][100];

    public int test() {
        return test(testArgs);
    }

    /**
     * Method parses 8 parameters (String[] args).<br/> -numThreads, number of
     * concurrent threads<br/> -smallObjSize, size of small padding objects<br/>
     * -freeMem, quantity of free memory in small objects<br/> -sleepTime, time
     * to leader thread sleeping<br/> -oOMEr (on or off), OutOfMemoryError (low
     * memory conditions)<br/> -sOEr (on or off), StackOverflowError (dedicated
     * thread throws SOE)<br/> -joinMthd, (chain or leader) join method (every
     * thread joins to previous or all threads join to one)
     */
    public int test(String[] args) {

        /**
         * Class with parameters.
         */
        StressTestParameters params = new StressTestParameters(log);

        /* Parse parameters. */
        if (!params.parse(args)) {
            return error("Unexpected error during parsing parameters");
        }

        try {
            verificationArray = new int[params.numThreads];
            for (int k = 0; k < params.numThreads; k++) {
                verificationArray[k] = 0;
            }
        } catch (OutOfMemoryError er) {
            ballastObj = null;
            return error("OutOfMemoryError during creating verificationArray."
                    + " Try to decrease number of threads.");
        }

        /* Log params to the output. */
        printParams(params);

        /* Create arrays. */
        try {
            threads = new StressJoinTestThread[params.numThreads];
        } catch (OutOfMemoryError er) {
            ballastObj = null;
            return error("OutOfMemoryError during creating threads array."
                    + " Try to decrease number of threads.");
        }

        /* Create threads. */
        try {
            if (params.joinMthd == 1) {
                leader = new StressJoinTestLeader(params.sleepTime);
            }

            for (int i = 0; i < params.numThreads; i++) {
                threads[i] = new StressJoinTestThread(i, params);
            }
        } catch (OutOfMemoryError er) {
            ballastObj = null;
            return error("OutOfMemoryError during creating threads."
                    + " Try to decrease number of threads.");
        }

        log.info("Threads created (ok)");

        /* StackOverflow conditions if needed. */
        if (params.sOEr == 1) {
            try {
                StressTestThreadSO sOThread = new StressTestThreadSO(log);
                sOThread.setDaemon(true);
                sOThread.start();
            } catch (Throwable t) {
                ballastObj = null;
                return error("Unexpected error" + t.toString());
            }
        }

        /* Pad memory if needed. */
        if (params.oOMEr == 1) {
            try {
                StressTestParameters.padMemory(params.smallObjSize,
                        params.freeMem);
            } catch (Throwable t) {
                ballastObj = null;
                return error("Unexpected error" + t.toString());
            }
        }

        /* Start threads. */
        try {
            if (params.joinMthd == 1) {
                leader.start();
            }

            for (int i = 0; i < params.numThreads; i++) {
                threads[i].start();
            }
        } catch (OutOfMemoryError er) {
            ballastObj = null;
            return error("OutOfMemoryError in main thread during starting"
                    + "threads - try to release more memory");
        }

        /* Join to all threads. */

        /* Almost excepts OutOfMemoryError. */
        while (true) {
            try {
                for (int j = 0; j < params.numThreads; j++) {
                    threads[j].join();
                }
                break;
            } catch (OutOfMemoryError er) {
            } catch (Throwable t) {
                return error("Error: " + t.toString());
            }
        }

        /* Release resources. */
        StressTestParameters.releaseMemory();

        /* Check fail flag */
        if (failFlag) {
            return fail(failInfo);
        }

        /* Check register array. It should contain all 1. */
        if (StressTestParameters.checkVerifArray(verificationArray,
                params.numThreads)) {
            return pass("Test pass");
        } else {
            return fail("Test fail");
        }
    }

    /**
     * Method logging input parameters to the output.
     */
    private int printParams(StressTestParameters p) {
        log.info("Number of threads: " + p.numThreads);
        log.info("Size of small padding objects: " + p.smallObjSize);
        log.info("Free memory (in int[" + p.smallObjSize + "]: " + p.freeMem);
        log.info("Time to leader sleeping: " + p.sleepTime);

        if (p.oOMEr == 0) {
            log.info("Low memory conditions off");
        } else if (p.oOMEr == 1) {
            log.info("Low memory conditions on");
        } else {
            return error("Wrong oOMEr parameter");
        }

        if (p.sOEr == 0) {
            log.info("Stack overflow conditions off");
        } else if (p.sOEr == 1) {
            log.info("Stack overflow conditions on");
        } else {
            return error("Wrong sOEr parameter");
        }

        if (p.joinMthd == 0) {
            log.info("Every thread will join to previous");
        } else if (p.joinMthd == 1) {
            log.info("All threads will join to leader");
        } else {
            return error("Wrong joinMthd parameter");
        }

        return 0;
    }

    public static void main(String[] args) {
        System.exit(new StressJoinTest().test(args));
    }
}

/**
 * Thread class.
 */
class StressJoinTestThread extends Thread {

    int currentThread; // number of current thread

    StressTestParameters p; // class with parameters

    /**
     * Constructor
     */
    public StressJoinTestThread(int currentThread, StressTestParameters p) {
        this.currentThread = currentThread; // number of current thread
        this.p = p; // class with parameters
    }

    public void run() {

        try {
            if (p.joinMthd == 0) { // every thread joins to previous
                if ((currentThread % 2) == 0) { // even thread. Must became
                    // leader.
                    try {
                        sleep(p.sleepTime);
                    } catch (InterruptedException ex) {
                        StressJoinTest.ballastObj = null;
                        StressJoinTest.failFlag = true;
                        StressJoinTest.failInfo = ("Unexpected error: " + ex
                                .toString());
                        return;
                    }

                    /* After awakening, store 1 into result array. */
                    StressJoinTest.verificationArray[currentThread] = 1;

                } else { // odd thread. Must join to previous.

                    try {
                        StressJoinTest.threads[currentThread - 1].join();
                    } catch (InterruptedException ex) {
                        StressJoinTest.ballastObj = null;
                        StressJoinTest.failFlag = true;
                        StressJoinTest.failInfo = ("Unexpected error: " + ex
                                .toString());
                        return;
                    }

                    /* After awakening, store 1 into result array. */
                    StressJoinTest.verificationArray[currentThread] = 1;

                }
            } else { // all threads join to leader

                try {
                    StressJoinTest.leader.join();
                } catch (InterruptedException ex) {
                    StressJoinTest.ballastObj = null;
                    StressJoinTest.failFlag = true;
                    StressJoinTest.failInfo = ("Unexpected error: " + ex
                            .toString());
                    return;
                }

                /* After awakening, store 1 into result array. */
                StressJoinTest.verificationArray[currentThread] = 1;

            }
        } catch (OutOfMemoryError er) {
        } catch (Throwable t) {
            StressJoinTest.ballastObj = null;
            StressJoinTest.failFlag = true;
            StressJoinTest.failInfo = ("Unexpected error: " + t.toString());
            return;
        }
    }
}

/**
 * Leader thread
 */

class StressJoinTestLeader extends Thread {
    int sleepTime;

    public StressJoinTestLeader(int sleepTime) {
        sleepTime = sleepTime;
    }

    public void run() {
        try {
            this.sleep(sleepTime);
        } catch (InterruptedException ex) {
            StressJoinTest.ballastObj = null;
            StressJoinTest.failFlag = true;
            StressJoinTest.failInfo = ("Unexpected error: " + ex.toString());
            return;
        }
    }
}
