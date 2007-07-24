/**
 * @author Aleksander V. Budniy
 * @version $Revision: 0.0 $
 */

/**
 * Created on 25.05.2006
 */
package org.apache.harmony.jpda.tests.jdwp.Events;

import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;
import org.apache.harmony.jpda.tests.share.SyncDebuggee;



/**
 * Debuggee for JDWP unit tests for CombinedEvents.
 * Calls it's own sampleMethod method.
 */
public class CombinedEventsDebuggee extends SyncDebuggee {
    public static CombinedEventsDebuggee combinedEventsDebuggee = null;

    public static void main(String[] args) {
        runDebuggee(CombinedEventsDebuggee.class);
    }
    
    public void sampleMethod() {
        logWriter.println("-> CombinedEventsDebuggee: inside of sampleMethod()!");
    }
    
       
    public void run() {
        logWriter.println("-> CombinedEventsDebuggee: Starting...");
        combinedEventsDebuggee = this;
        
        //DBG synchronizer.sendMessage(JPDADebuggeeSynchronizer.SGNL_READY);
        String mainThreadName = Thread.currentThread().getName();
        synchronizer.sendMessage(mainThreadName);
              
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
        logWriter.println("-> CombinedEventsDebuggee: Before calling sampleMethod");
        
        sampleMethod();
        // new CombinedEventsDebuggee_Extra().proxiMethod();
        
        logWriter.println("-> CombinedEventsDebuggee: Finishing...");
    }
}

class CombinedEventsDebuggee_Extra {
    public void proxiMethod() {
        CombinedEventsDebuggee.combinedEventsDebuggee.sampleMethod();
    }
}
