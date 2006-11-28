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
 * @author Vitaly A. Provodin
 * @version $Revision: 1.5 $
 */

/**
 * Created on 28.02.2005
 */
package org.apache.harmony.jpda.tests.jdwp.VirtualMachine;

import org.apache.harmony.jpda.tests.framework.TestErrorException;
import org.apache.harmony.jpda.tests.framework.jdwp.CommandPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPCommands;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants;
import org.apache.harmony.jpda.tests.framework.jdwp.ReplyPacket;
import org.apache.harmony.jpda.tests.jdwp.share.JDWPSyncTestCase;
import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;

/**
 * JDWP Unit test for VirtualMachine.HoldEvents command.
 */
public class HoldEventsTest extends JDWPSyncTestCase {

    CommandPacket event = null;

    protected String getDebuggeeClassName() {
        return "org.apache.harmony.jpda.tests.jdwp.VirtualMachine.HoldEventsDebuggee";
    }

    /**
     * This testcase exercises VirtualMachine.HoldEvents command.
     * <BR>At first the test starts HoldEventsDebuggee.
     * <BR> Then the test sends request for TESTED_THREAD and
     * performs VirtualMachine.HoldEvents command.
     * Next, the test waits for debuggee to start the 'TESTED_THREAD'
     * thread and checks that no any events (including requested TESTED_THREAD event)
     * are sent to test.
     */
    public void testHoldEvents001() {
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_READY);

        debuggeeWrapper.vmMirror.setThreadStart();
        
        //send HoldEvents command
        logWriter.println("send HoldEvents");
        CommandPacket packet = new CommandPacket(
                JDWPCommands.VirtualMachineCommandSet.CommandSetID,
                JDWPCommands.VirtualMachineCommandSet.HoldEventsCommand);
       
        ReplyPacket reply = debuggeeWrapper.vmMirror.performCommand(packet);
        checkReplyPacket(reply, "VirtualMachine::HoldEvents command");

        logWriter.println("allow to start thread");
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);

        EventReceiver thread = new EventReceiver(); 
        thread.start();
        try {
            thread.join(settings.getTimeout());
        } catch (InterruptedException e) {
            
        }

        if (event == null) {
            logWriter.println("no events were received");
        } else {
            logWriter.printError("unexpected event");
            fail("unexpected event");
        }

        logWriter.println("send ReleaseEvents");
        packet = new CommandPacket(
                JDWPCommands.VirtualMachineCommandSet.CommandSetID,
                JDWPCommands.VirtualMachineCommandSet.ReleaseEventsCommand);
        debuggeeWrapper.vmMirror.performCommand(packet);
       
        event = debuggeeWrapper.vmMirror.receiveCertainEvent(JDWPConstants.EventKind.THREAD_START);
        debuggeeWrapper.vmMirror.resume();
    }

    class EventReceiver extends Thread {
        public void run() {
            try {
                event = debuggeeWrapper.vmMirror.receiveCertainEvent(JDWPConstants.EventKind.THREAD_START);
            } catch (TestErrorException e) {
                logWriter.println(e.toString());
            }
        }
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HoldEventsTest.class);
    }
}
