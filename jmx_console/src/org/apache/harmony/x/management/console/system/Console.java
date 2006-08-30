/* Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @author Sergey A. Krivenko
 * @version $Revision: 1.5 $
 */

package org.apache.harmony.x.management.console.system;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.harmony.x.management.console.controller.AttributeInfo;
import org.apache.harmony.x.management.console.controller.Controller;
import org.apache.harmony.x.management.console.controller.ControllerFactory;
import org.apache.harmony.x.management.console.controller.MBeanOperations;
import org.apache.harmony.x.management.console.controller.OperationInfo;
import org.apache.harmony.x.management.console.controller.VMMonitor;


/**
 * Plain text version of the JMX Administrative Console. 
 * May serve as an example of how Controller should be used in
 * different types of GUI.
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.5 $
 */
public class Console {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            Controller c = ControllerFactory.getController();
            c.connect();
            System.out.println("Controller conected...");    
            
            // Standard MBean operations.........................
            
            MBeanOperations mbo = c.getMBeanOperations();
            String[] domains = mbo.getAllDomains();
            System.out.println("Listing all domains:");
            
            for (int i = 0; i < domains.length; i++) {
                System.out.println("\t" + domains[i]);
            }
            
            String sampleBean = null;
            
            for (int i = 0; i < domains.length; i++) {
                String[] mbeans = mbo.getMBeansOfDomain(domains[i]);
                System.out.println("Listing MBeans of domain " + domains[i] + "...");
            
                for (int j = 0; j < mbeans.length; j++) {
                    sampleBean = mbeans[j];
                    System.out.println("\t" + mbeans[j]);
                }
            }
            
            List attrs = mbo.listAttributes(sampleBean);
            System.out.println("Listing attributes for MBean - " + sampleBean);
            
            for (Iterator it = attrs.iterator(); it.hasNext(); ) {
                AttributeInfo info = (AttributeInfo) it.next();
                System.out.println("\t" + info.getName() + 
                        ":\t" + info.getValue());
            }
            
            List ops = mbo.listOperations(sampleBean);
            System.out.println("Listing operations for MBean - " + sampleBean);
            
            for (Iterator it = ops.iterator(); it.hasNext(); ) {
                OperationInfo info = (OperationInfo) it.next();
                System.out.println("\t" + info);
            }
           
            //mbo.listNotifications();
            
            Hashtable props = mbo.getMBeanProperties(sampleBean);
            System.out.println("Listing properties for " + sampleBean);
            
            for (Enumeration en = props.keys(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                String value = (String) props.get(key);
                System.out.println("\t" + key + "=" + value);
            }
            
            // VM monitor........................................
            
            VMMonitor mon = c.getVMMonitor();
            Hashtable clInfo = mon.getClassLoadingInfo();
            System.out.println("Getting class loading info...");
            
            for (Enumeration en = clInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + clInfo.get(key));
            }
            
            Hashtable cInfo = mon.getCompilationInfo();
            System.out.println("Getting compillation info...");
            
            for (Enumeration en = cInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + cInfo.get(key));
            }
            
            Set set = mon.getGarbageCollectorMBeans();
            System.out.println("Getting garbage collector info...");
            
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                ObjectName on = (ObjectName) it.next();
                System.out.println("\t" + on);
                Hashtable gcInfo = mon.getGarbageCollectorInfo(on);
                
                for (Enumeration en = gcInfo.keys(); en.hasMoreElements(); ) {
                    Object key = en.nextElement();
                    System.out.println("\t\t" + key + ": " + gcInfo.get(key));
                }
            }
            
            System.out.println("Running garbage collector...");
            mon.runGC();
            System.out.println("Getting garbage collector info again...");
            
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                ObjectName on = (ObjectName) it.next();
                System.out.println("\t" + on);
                Hashtable gcInfo = mon.getGarbageCollectorInfo(on);
                
                for (Enumeration en = gcInfo.keys(); en.hasMoreElements(); ) {
                    Object key = en.nextElement();
                    System.out.println("\t\t" + key + ": " + gcInfo.get(key));
                }
            }
            
            set = mon.getMemoryManagerMBeans();
            System.out.println("Getting memory manager info...");
            
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                ObjectName on = (ObjectName) it.next();
                System.out.println("\t" + on);
                Hashtable mmInfo = mon.getMemoryManagerInfo(on);
                
                for (Enumeration en = mmInfo.keys(); en.hasMoreElements(); ) {
                    Object key = en.nextElement();
                    System.out.println("\t\t" + key + ": " + mmInfo.get(key));
                }
                
                String[] poolNames = mon.getMemoryManagerPoolNames(on);
                System.out.println("\t\tPool names:");
                
                for (int i = 0; i < poolNames.length; i++) {
                    System.out.println("\t\t\t" + poolNames[i]);
                }
            }
            
            Hashtable mInfo = mon.getMemoryInfo();
            System.out.println("Getting memory info...");
            
            for (Enumeration en = mInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + mInfo.get(key));
            }
            
            System.out.println("\tHeap memory usage: ");
            Collection hmu = mon.getHeapMemoryUsage();
            
            for (Iterator it = hmu.iterator(); it.hasNext(); ) {
                System.out.println("\t\t" + it.next());
            }
            
            System.out.println("\tNon-heap memory usage: ");
            Collection nhmu = mon.getNonHeapMemoryUsage();
            
            for (Iterator it = nhmu.iterator(); it.hasNext(); ) {
                System.out.println("\t\t" + it.next());
            }
            
            set = mon.getMemoryPoolMBeans();
            System.out.println("Getting memory pool info...");
            
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                ObjectName on = (ObjectName) it.next();
                System.out.println("\t" + on);
                Hashtable mpInfo = mon.getMemoryPoolInfo(on);
                
                for (Enumeration en = mpInfo.keys(); en.hasMoreElements(); ) {
                    Object key = en.nextElement();
                    System.out.println("\t\t" + key + ": " + mpInfo.get(key));
                }
                
                Collection u = mon.getUsage(on);
                System.out.println("\t\tUsage:");
                
                for (Iterator iter = u.iterator(); iter.hasNext(); ) {
                    System.out.println("\t\t\t" + iter.next());
                }
                
                Collection pu = mon.getPeakUsage(on);
                System.out.println("\t\tPeak usage:");
                
                for (Iterator iter = pu.iterator(); iter.hasNext(); ) {
                    System.out.println("\t\t\t" + iter.next());
                }
                
                Collection cu = mon.getCollectionUsage(on);
                System.out.println("\t\tCollection usage:");
                
                for (Iterator iter = cu.iterator(); iter.hasNext(); ) {
                    System.out.println("\t\t\t" + iter.next());
                }
                
                String[] mmn = mon.getMemoryManagerNames(on);
                System.out.println("\t\tMemory manager names:");
                
                for (int i = 0; i < mmn.length; i++) {
                    System.out.println("\t\t\t" + mmn[i]);
                }
            }
            
            Hashtable osInfo = mon.getOSInfo();
            System.out.println("Getting operating system info...");
            
            for (Enumeration en = osInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + osInfo.get(key));
            }
            
            Hashtable rtInfo = mon.getRuntimeInfo();
            System.out.println("Getting runtime info...");
            
            for (Enumeration en = rtInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + rtInfo.get(key));
            }
            
            String[] ia = mon.getInputArguments();
            System.out.println("\tInput arguments:");
            
            for (int i = 0; i < ia.length; i++) {
                System.out.println("\t\t\t" + ia[i]);
            }
            
            System.out.println("Getting boot classpath...");
            String[] cp = mon.getBootClassPath();
            
            for (int i = 0; i < cp.length; i++) {
                System.out.println("\t" + cp[i]);
            }
            
            System.out.println("Getting library path...");
            String[] lp = mon.getBootClassPath();
            
            for (int i = 0; i < lp.length; i++) {
                System.out.println("\t" + lp[i]);
            }
            
            System.out.println("Getting system properties...");
            Hashtable sp = mon.getSystemProperties();
            
            for (Enumeration en = sp.keys(); en.hasMoreElements(); ){
                Object key = en.nextElement();
                Object value = sp.get(key);
                System.out.println("\t" + key + "=" + value);
            }
            
            Hashtable tInfo = mon.getThreadInfo();
            System.out.println("Getting thread info...");
            
            for (Enumeration en = tInfo.keys(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                System.out.println("\t" + key + ": " + tInfo.get(key));
            }
            
            long[] allThreads = mon.getAllThreadIds();
            System.out.println("Getting all threads...");
            
            for (int i = 0; i < allThreads.length; i++) {
                Collection col = mon.getThreadInfo(allThreads[i]);
                System.out.println("\tThread ID: " + allThreads[i]);
                
                for (Iterator it = col.iterator(); it.hasNext(); ) {
                    System.out.println("\t\t" + it.next());
                }
            }
            
            System.out.println("Getting thread user time...");
            
            for (int i = 0; i < allThreads.length; i++) {
                System.out.print("\tThread ID " + allThreads[i]);
                System.out.println(": " + mon.getThreadUserTime(allThreads[i]));
            }
            
            System.out.println("Getting thread CPU time...");
            
            for (int i = 0; i < allThreads.length; i++) {
                System.out.print("\tThread ID " + allThreads[i]);
                System.out.println(": " + mon.getThreadCpuTime(allThreads[i]));
            }
            
            System.out.println("Getting all threads as collections...");
            Collection[] ti = mon.getThreadInfo(allThreads);
            
            for (int i = 0; i < ti.length; i++) {
                System.out.println("\tNext (" + i + "):");
                
                for (Iterator it = ti[i].iterator(); it.hasNext(); ) {
                    System.out.println("\t\t" + it.next());
                }
            }
            
            System.out.println("\nTest completed.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
