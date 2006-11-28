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
 * @version $Revision: 1.4 $
 */

/**
 * Created on 11.04.2005
 */
package org.apache.harmony.jpda.tests.jdwp.Events;

import org.apache.harmony.jpda.tests.framework.jdwp.EventPacket;
import org.apache.harmony.jpda.tests.framework.jdwp.JDWPConstants;
import org.apache.harmony.jpda.tests.framework.jdwp.ParsedEvent;
import org.apache.harmony.jpda.tests.framework.jdwp.TaggedObject;
import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;



/**
 * JDWP Unit test for caught EXCEPTION event.
 */
public class ExceptionTest extends JDWPEventTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ExceptionTest.class);
    }
    
    protected String getDebuggeeClassName() {
        return ExceptionDebuggee.class.getName();
    }
    
    /**
     * This testcase is for caught EXCEPTION event.
     * <BR>It runs ExceptionDebuggee that throws caught DebuggeeException
     * and verify that requested EXCEPTION event occurs.
     */
    public void testExceptionEvent() {
        logWriter.println(">> testExceptionEvent: STARTED...");
        
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_READY);

        String exceptionSignature = "Lorg/apache/harmony/jpda/tests/jdwp/Events/DebuggeeException;";
        boolean isCatch = true;
        boolean isUncatch = true;
        logWriter.println("\n>> testExceptionEvent: => setException(...)...");
        debuggeeWrapper.vmMirror.setException(exceptionSignature, isCatch, isUncatch);
        logWriter.println(">> testExceptionEvent: setException(...) DONE");

        logWriter.println("\n>> testExceptionEvent: send to Debuggee SGNL_CONTINUE...");
        synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);

        logWriter.println("\n>> testExceptionEvent: => receiveEvent()...");
        EventPacket event = debuggeeWrapper.vmMirror.receiveEvent();
        logWriter.println(">> testExceptionEvent: Event is received! Check it ...");
        ParsedEvent[] parsedEvents = ParsedEvent.parseEventPacket(event);
        
        // assert that event is the expected one 
        logWriter.println(">> testExceptionEvent: parsedEvents.length = " + parsedEvents.length);
        logWriter.println(">> testExceptionEvent: parsedEvents[0].getEventKind() = " + parsedEvents[0].getEventKind());
        assertEquals("Invalid number of events,", 1, parsedEvents.length);
        assertEquals("Invalid event kind,",
                JDWPConstants.EventKind.EXCEPTION,
                parsedEvents[0].getEventKind(),
                JDWPConstants.EventKind.getName(JDWPConstants.EventKind.EXCEPTION),
                JDWPConstants.EventKind.getName(parsedEvents[0].getEventKind()));
        TaggedObject returnedException =((ParsedEvent.Event_EXCEPTION)parsedEvents[0]).getException();
        
        // assert that exception class is the expected one
        long typeID = getObjectReferenceType(returnedException.objectID);
        String returnedExceptionSignature = getClassSignature(typeID);
        logWriter.println(">> testExceptionEvent: returnedExceptionSignature = |" + returnedExceptionSignature+"|");
        assertString("Invalid signature of returned exception,", exceptionSignature, returnedExceptionSignature);
        
        // resume debuggee 
        logWriter.println("\n>> testExceptionEvent: resume debuggee...");
        debuggeeWrapper.vmMirror.resume();
    }
}
