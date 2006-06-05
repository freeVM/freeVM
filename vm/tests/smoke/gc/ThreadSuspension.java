/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
 * @author Salikh Zakirov
 * @version $Revision: 1.8.28.4 $
 */  
package gc;

/**
 * GC tests have no dependecy on class library.
 *
 * VM currently is not able to suspend thread which is running
 * a computation loop.
 *
 * @keyword XXX
 */
public class ThreadSuspension implements Runnable {

    static boolean passed = false;

    public static void main (String[] args) {
        Thread x1 = new Thread(new ThreadSuspension(1)); 
        x1.setDaemon(true); x1.start();
        Thread x2 = new Thread(new ThreadSuspension(2)); 
        x2.setDaemon(true); x2.start();
        Thread x3 = new Thread(new ThreadSuspension(3)); 
        x3.setDaemon(true); x3.start(); 
        try { 
            synchronized(x1) {
                x1.wait(3000);
            } 
        } catch (Throwable e) {}
        if (passed) {
            trace("PASS");
        } else {
            trace("FAIL");
        }
    }

    public ThreadSuspension(int n) {
        number = n;
    }

    public void run() {
        switch (number) {
            case 1:
                try { Thread.sleep(1000); } catch (Throwable e) {}
                trace("forcing gc after 1 s delay");
                System.gc();
                trace("gc completed");
                passed = true;
                synchronized (this) {
                    notify();
                }
                break;
            case 2:
                int j =0;
                trace("-- starting unsuspendable computation --");
                for (int i=0; i<1000000000; i++) {
                    j = 1000 + j/(i+1);
                }
                trace("-- unsuspendable computation finished --");
                break;
            case 3:
                trace("-- starting suspendable computation --");
                for (int i=0; i<1000000000; i++) {
                    Thread.yield();
                }
                trace("-- suspendable computation finished --");
                break;
        }
    }

    public synchronized static void trace(Object o) {
        System.out.println(o);
        System.out.flush();
    }

    int number; // the number of the thread
}
