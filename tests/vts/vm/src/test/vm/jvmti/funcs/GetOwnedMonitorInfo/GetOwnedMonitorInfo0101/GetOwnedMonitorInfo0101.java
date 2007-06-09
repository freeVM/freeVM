/*
    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.apache.harmony.vts.test.vm.jvmti;

/** 
 * @author Valentin Al. Sitnick
 * @version $Revision: 1.1 $
 *
 */ 
public class GetOwnedMonitorInfo0101 {
    public static boolean all_threads_can_start = false;
    public final static Object sync0 = new Object();
    public final static Object sync1 = new Object();
    public final static Object sync2 = new Object();
    public final static Object sync3 = new Object();
    public final static Object sync4 = new Object();
    public final static Object sync5 = new Object();
    public final static Object sync6 = new Object();

    public static void main(String[] args) {

	TestThread_T_10 tr = new TestThread_T_10("Owner");
        tr.start();        
        
        while (!all_threads_can_start) {
	    try {
	        Thread.sleep(500);
            } catch (Throwable te) {
                te.printStackTrace();
            }
        }

        new Thread() { // 0
            public void run() {
                synchronized (sync0) {
                    return;
                }
            }
        }.start();

        new Thread() { // 1
            public void run() {
                synchronized (sync1) {
                    return;
                }
            }
        }.start();

        new Thread() { // 2
            public void run() {
                synchronized (sync2) {
                    return;
                }
            }
        }.start();

        new Thread() { // 3
            public void run() {
                synchronized (sync3) {
                    return;
                }
            }
        }.start();

        new Thread() { // 4
            public void run() {
                synchronized (sync4) {
                    return;
                }
            }
        }.start();

        new Thread() { // 5
            public void run() {
                synchronized (sync5) {
                    return;
                }
            }
        }.start();

        new Thread() { // 6
            public void run() {
                synchronized (sync6) {
                    return;
                }
            }
        }.start();
    }
}

class TestThread_T_10 extends Thread {

    TestThread_T_10(String name) {
        super(name);
    }

    public void special_method() {
        new Thread("agent") {
            public void run() {
                return;
            }
        }.start();
    }

    public void run() {
        synchronized (GetOwnedMonitorInfo0101.sync0) {
            synchronized (GetOwnedMonitorInfo0101.sync1) {
                synchronized (GetOwnedMonitorInfo0101.sync2) {
                    synchronized (GetOwnedMonitorInfo0101.sync3) {
                        synchronized (GetOwnedMonitorInfo0101.sync4) {
                            synchronized (GetOwnedMonitorInfo0101.sync5) {
                                synchronized (GetOwnedMonitorInfo0101.sync6) {

                                    GetOwnedMonitorInfo0101.all_threads_can_start = true;

                                    try {
                                        Thread.sleep(5000);
                                    } catch (Throwable te) {
                                        te.printStackTrace();
                                    }

                                    special_method();

                                    try {
                                        Thread.sleep(1000);
                                    } catch (Throwable te) {
                                        te.printStackTrace();
                                    }
                                    
                                    return;
                                }
                            }
                        }
                    }
		}
	    }
        }
    }
}

