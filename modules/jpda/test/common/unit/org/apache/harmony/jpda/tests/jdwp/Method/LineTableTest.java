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
 * @version $Revision: 1.3 $
 */

/**
 * Created on 14.02.2005
 */
package org.apache.harmony.jpda.tests.jdwp.Method;

import java.io.UnsupportedEncodingException;

import org.apache.harmony.jpda.tests.framework.jdwp.ReplyPacket;
import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;



/**
 * JDWP Unit test for Method.LineTable command.
 */
public class LineTableTest extends JDWPMethodTestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LineTableTest.class);
    }

    /**
     * This testcase exercises Method.LineTable command.
     * <BR>It runs MethodDebuggee, receives methods of debuggee. 
     * For each received method sends Method.LineTable command
     * and prints returned LineTable.
     */
    public void testLineTableTest001() throws UnsupportedEncodingException {
        logWriter.println("testLineTableTest001 started");
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_READY);

        long classID = getClassIDBySignature("L"+getDebuggeeClassName().replace('.', '/')+";");

        MethodInfo[] methodsInfo = jdwpGetMethodsInfo(classID);
        assertFalse("Invalid number of methods: 0", methodsInfo.length == 0);

        for (int i = 0; i < methodsInfo.length; i++) {
            logWriter.println(methodsInfo[i].toString());

            // get variable table for this class
            ReplyPacket reply = getLineTable(classID, methodsInfo[i].getMethodID());

            long start = reply.getNextValueAsLong();
            logWriter.println("start = " + start);
            long end = reply.getNextValueAsLong();
            logWriter.println("end = " + end);

            int lines = reply.getNextValueAsInt();
            logWriter.println("lines = "+lines);

            for (int j = 0; j < lines; j++) {
                long lineCodeIndex = reply.getNextValueAsLong();
                logWriter.println("lineCodeIndex = "+lineCodeIndex);
                int lineNumber = reply.getNextValueAsInt();
                logWriter.println("lineNumber = "+lineNumber);
            }
        }

        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
    }
}
