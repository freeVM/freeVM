/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Revision: 1.4 $
 */

/**
 * Created on 25.02.2005
 */
package org.apache.harmony.jpda.tests.jdwp.ThreadGroupReference;

import org.apache.harmony.jpda.tests.framework.jdwp.CommandPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPCommands;
import org.apache.harmony.jpda.tests.framework.jdwp.ReplyPacket;
import org.apache.harmony.jpda.tests.jdwp.share.JDWPSyncTestCase;
import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;


/**
 * JDWP Unit test for ThreadGroupReference.Name command.
 */
public class NameTest extends JDWPSyncTestCase {

    protected String getDebuggeeClassName() {
        return "org.apache.harmony.jpda.tests.jdwp.ThreadGroupReference.NameDebuggee";
    }

    /**
     * This testcase exercises ThreadGroupReference.Name command.
     * <BR>At first the test starts NameDebuggee.
     * <BR> Then the test with help of the ThreadGroupReference.Name command checks
     * that for the thread 'TESTED_THREAD' the group name is 'CHILD_GROUP'.
     *  
     */
    public void testName001() {
        logWriter.println("wait for SGNL_READY");
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_READY);

        // getting ID of the tested thread
        CommandPacket packet;
        long threadID = debuggeeWrapper.vmMirror.getThreadID(NameDebuggee.TESTED_THREAD);

        long groupID;
        String groupName;

        // getting the thread group ID
        packet = new CommandPacket(
                JDWPCommands.ThreadReferenceCommandSet.CommandSetID,
                JDWPCommands.ThreadReferenceCommandSet.ThreadGroupCommand);
        packet.setNextValueAsThreadID(threadID);
        
        ReplyPacket reply = debuggeeWrapper.vmMirror.performCommand(packet);
        checkReplyPacket(reply, "ThreadReference::ThreadGroup command");

        groupID = reply.getNextValueAsThreadGroupID();
        groupName = debuggeeWrapper.vmMirror.getThreadGroupName(groupID);

        logWriter.println("\tthreadID=" + threadID
                    + "; threadName=" + NameDebuggee.TESTED_THREAD
                    + "; groupID=" + groupID
                    + "; groupName=" + groupName);

        assertString("ThreadReference::ThreadGroup command returned invalid group name,",
                NameDebuggee.CHILD_GROUP, groupName);

        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(NameTest.class);
    }
}
