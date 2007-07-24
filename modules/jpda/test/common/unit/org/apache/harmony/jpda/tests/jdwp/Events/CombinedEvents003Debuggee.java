/**
 * @author Anatoly F. Bondarenko
 * @version $Revision: 1.1 $
 */

/**
 * Created on 26.10.2006
 */
package org.apache.harmony.jpda.tests.jdwp.Events;

import org.apache.harmony.jpda.tests.share.JPDADebuggeeSynchronizer;
import org.apache.harmony.jpda.tests.share.SyncDebuggee;



/**
 * Debuggee for JDWP unit tests for CombinedEvents.
 */
public class CombinedEvents003Debuggee extends SyncDebuggee {

    public static void main(String[] args) {
        runDebuggee(CombinedEvents003Debuggee.class);
    }
    
    public void emptyMethod() {
    }
     
    public void run() {
        logWriter.println("-> CombinedEvents003Debuggee: Starting...");
        
        String mainThreadName = Thread.currentThread().getName();
        synchronizer.sendMessage(mainThreadName);
              
        synchronizer.receiveMessage(JPDADebuggeeSynchronizer.SGNL_CONTINUE);
        
        emptyMethod();
        
        logWriter.println("-> CombinedEvents003Debuggee: Finishing...");
    }
}
