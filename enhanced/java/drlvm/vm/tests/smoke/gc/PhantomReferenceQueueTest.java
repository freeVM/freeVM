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
 * @author Salikh Zakirov
 */  

package gc;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public class PhantomReferenceQueueTest {

    static boolean passed = false;

    public static void main(String[] args) {
        final ReferenceQueue queue = new ReferenceQueue();
        final Reference ref = new PhantomReference(new PhantomReferenceTest(), queue);
        System.gc();
        Thread x = new Thread() {
            public void run() {
                try {
                    queue.remove();
                    passed = true;
                    synchronized (PhantomReferenceTest.class) {
                        PhantomReferenceTest.class.notify();
                    }
                } catch (InterruptedException e) {}
            }
        }; 
        x.setDaemon(true); x.start();
        synchronized (PhantomReferenceTest.class) {
            try {
                PhantomReferenceTest.class.wait(5000);
            } catch (InterruptedException e) {}
        }
        System.out.println("Reference itself is at " + ref);
        if (passed) 
            System.out.println("PASS");
        else
            System.out.println("FAIL, reference not enqueued after 5 second wait");
    }
}
