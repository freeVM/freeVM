/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Anton V. Karnachuk
 * @version $Revision: 1.2 $
 */

/**
 * Created on 11.03.2005
 */
package org.apache.harmony.jpda.tests.jdwp.Events;

import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;
import org.apache.harmony.jpda.tests.share.SyncDebuggee;

/**
 * Basic debuggee for events unit tests.
 * Runs new thread
 */
public class EventDebuggee extends SyncDebuggee {
    
    public static final String testedThreadName = "SimpleThread";
    
    private class SimpleThread extends Thread {
        
        public SimpleThread () {
            super(testedThreadName);   
        }
        
        public void run() {
            logWriter.println("-> SimpleThread: Running...");
        } 
    }

    public static void main(String[] args) {
        runDebuggee(EventDebuggee.class);
    }
    
    public void run() {
        logWriter.println("-> EventDebuggee: STARTED");
        
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
                
        SimpleThread testThread = new SimpleThread();
        testThread.start();
        logWriter.println("-> EventDebuggee: SimpleThread started");
        try {
            testThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logWriter.println("-> EventDebuggee: SimpleThread finished");
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        
        logWriter.println("-> EventDebuggee: wait for signal to continue...");
        // do NOT finish without signal (in acceptable time) in order 
        // to do NOT generate unexpected events
        String infoMessage = "-> EventDebuggee: TIMED OUT in waiting for signal!";
        for (int i=0; i < 10; i++ ) {
            if ( synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE) ) {
                infoMessage = "-> EventDebuggee: signal received!";
               break;
            }
        }
        logWriter.println(infoMessage);
        
        logWriter.println("-> EventDebuggee: FINISH...");
    }

}

