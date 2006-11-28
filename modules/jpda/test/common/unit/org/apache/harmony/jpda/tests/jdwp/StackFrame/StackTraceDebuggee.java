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
 * Created on 10.02.2005
 */
package org.apache.harmony.jpda.tests.jdwp.StackFrame;

import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;
import org.apache.harmony.jpda.tests.share.SyncDebuggee;

public class StackTraceDebuggee extends SyncDebuggee {

    public static void main(String [] args) {
        runDebuggee(StackTraceDebuggee.class);
    }

    public void run() {
        String currentThreadName = Thread.currentThread().getName();
        synchronizer.sendMessage(currentThreadName);
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
        nestledMethod1();
    }
    
    private void nestledMethod1() {
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
        nestledMethod2();
    }

    private void nestledMethod2() {
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
        nestledMethod3();
    }

    private void nestledMethod3() {
        boolean boolLocalVariable = true;
        int intLocalVariable = -512;
        String strLocalVariable = "test string";
        logWriter.println("boolLocalVariable = " + boolLocalVariable);
        logWriter.println("intLocalVariable = " + intLocalVariable);
        logWriter.println("strLocalVariable = " +strLocalVariable);
                
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
    }

}