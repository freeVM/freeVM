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
 * @version $Revision: 1.3 $
 */
package org.apache.harmony.x.management.console.controller;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;


/**
 * Prepare the data collected by the implementation of java.lang.management.* 
 * package to be shown in GUI.  
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class VMMonitor {

    /**
     * A reference to MBeanServer where platform MXBeans are registered.
     */
    private MBeanServerConnection con = null;
    
    /**
     * Construct the object.
     * 
     * @param con - A reference to MBeanServer where platform MXBeans 
     *        are registered.
     */
    public VMMonitor(MBeanServerConnection con) 
            throws ServiceNotInstalledException {
        
        this.con = con;
        checkMXMBeans();
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public long[] findMonitorDeadlockedThreads() 
            throws ControllerOperationException {
        
        try {
            return (long[]) con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "findMonitorDeadlockedThreads", null, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public long[] getAllThreadIds() throws ControllerOperationException {
        try {
            return (long[]) con.getAttribute(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "AllThreadIds");
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public String[] getBootClassPath() throws ControllerOperationException {
        try {
            String cp = (String) con.getAttribute(
                    new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME), 
                    "BootClassPath");
            StringTokenizer st = new StringTokenizer(cp, ";");
            String[] tokens = new String[st.countTokens()];
            int i = 0;
            
            while (st.hasMoreTokens()) {
                tokens[i] = st.nextToken();
                i++;
            }
            
            return tokens;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getClassLoadingInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
            Hashtable table = new Hashtable(4);
            table.put("Number of classes loaded", 
                    con.getAttribute(on, "LoadedClassCount"));
            table.put("Number of total classes loaded",  
                    con.getAttribute(on, "TotalLoadedClassCount"));
            table.put("Number of unloaded classes", 
                    con.getAttribute(on, "UnloadedClassCount"));
            table.put("Verbose", con.getAttribute(on, "Verbose"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Collection getCollectionUsage(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            return ((CompositeDataSupport) con.getAttribute(
                    on, "CollectionUsage")).values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getCompilationInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
            Hashtable table = new Hashtable(3);
            table.put("Name", con.getAttribute(on, "Name"));
            table.put("Total compilation time", 
                    con.getAttribute(on, "TotalCompilationTime"));
            table.put("Compilation time monitoring supported", 
                    con.getAttribute(on, "CompilationTimeMonitoringSupported"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getGarbageCollectorInfo(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            Hashtable table = new Hashtable(2);
            table.put("Number of collections",  
                    con.getAttribute(on, "CollectionCount"));
            table.put("Collection time", con.getAttribute(on, "CollectionTime"));                
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Set getGarbageCollectorMBeans() throws ControllerOperationException {
        try {
            ObjectName on = new ObjectName(
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
            return (Set) con.queryNames(on, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Collection getHeapMemoryUsage() throws ControllerOperationException {
        try {
            
            CompositeDataSupport hmu = (CompositeDataSupport) con.getAttribute(
                    new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), 
                    "HeapMemoryUsage");
            return hmu.values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public String[] getInputArguments() throws ControllerOperationException {
        try {
            return (String[]) con.getAttribute(
                    new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME), 
                    "InputArguments");
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public String[] getLibraryPath() throws ControllerOperationException {
        try {
            String cp = (String) con.getAttribute(
                    new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME), 
                    "LibraryPath");
            StringTokenizer st = new StringTokenizer(cp, ";");
            String[] tokens = new String[st.countTokens()];
            int i = 0;
            
            while (st.hasMoreTokens()) {
                tokens[i] = st.nextToken();
                i++;
            }
            
            return tokens;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getMemoryInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
            Hashtable table = new Hashtable(2);
            table.put("Number of object pending finalization", 
                    con.getAttribute(on, "ObjectPendingFinalizationCount"));
            table.put("Verbose", con.getAttribute(on, "Verbose")); 
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getMemoryManagerInfo(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            
            Hashtable table = new Hashtable(2);
            table.put("Name", con.getAttribute(on, "Name"));
            table.put("Valid", con.getAttribute(on, "Valid"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Set getMemoryManagerMBeans() throws ControllerOperationException {
        try {
            ObjectName on = new ObjectName(
                    ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE + ",*");
            return (Set) con.queryNames(on, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public String[] getMemoryManagerNames(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            return (String[]) con.getAttribute(on, "MemoryManagerNames");
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException();
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public String[] getMemoryManagerPoolNames(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            return (String[]) con.getAttribute(on, "MemoryPoolNames");
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getMemoryPoolInfo(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            Hashtable table = new Hashtable(11);
            table.put("Collection usage threshold", 
                    con.getAttribute(on, "CollectionUsageThreshold"));
            table.put("Collection usage threshold count", 
                    con.getAttribute(on, "CollectionUsageThresholdCount"));
            table.put("Name", con.getAttribute(on, "Name"));
            table.put("Type", con.getAttribute(on, "Type"));
            table.put("Usage threshold", con.getAttribute(on, "UsageThreshold"));
            table.put("Usage threshold count", 
                    con.getAttribute(on, "UsageThresholdCount"));
            table.put("Collection usage threshold exceeded", 
                    con.getAttribute(on, "CollectionUsageThresholdExceeded"));
            table.put("Collection usage threshold supported", 
                    con.getAttribute(on, "CollectionUsageThresholdSupported"));
            table.put("Usage threshold exceeded", 
                    con.getAttribute(on, "UsageThresholdExceeded"));
            table.put("Usage threshold supported", 
                    con.getAttribute(on, "UsageThresholdSupported"));
            table.put("Valid", con.getAttribute(on, "Valid"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Set getMemoryPoolMBeans() throws ControllerOperationException {
        try {
            ObjectName on = new ObjectName(
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
            return (Set) con.queryNames(on, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Collection getNonHeapMemoryUsage() throws ControllerOperationException {
        try {
            
            CompositeDataSupport nhmu = (CompositeDataSupport) con.getAttribute(
                    new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), 
                    "NonHeapMemoryUsage");
            return nhmu.values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getOSInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            Hashtable table = new Hashtable(4);
            table.put("Arch", con.getAttribute(on, "Arch"));
            table.put("AvailableProcessors", 
                    con.getAttribute(on, "AvailableProcessors"));
            table.put("Name", con.getAttribute(on, "Name"));
            table.put("Version", con.getAttribute(on, "Version"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Collection getPeakUsage(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            return ((CompositeDataSupport) con.getAttribute(
                    on, "PeakUsage")).values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getRuntimeInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
            Hashtable table = new Hashtable(12);
            table.put("Classpath", con.getAttribute(on, "ClassPath"));
            table.put("Management specification version", 
                    con.getAttribute(on, "ManagementSpecVersion"));
            table.put("Name", con.getAttribute(on, "Name"));
            table.put("Specification name", con.getAttribute(on, "SpecName"));
            table.put("Specification vendor", con.getAttribute(on, "SpecVendor"));
            table.put("Specification version", 
                    con.getAttribute(on, "SpecVersion"));
            table.put("Start time", con.getAttribute(on, "StartTime"));
            table.put("Uptime", con.getAttribute(on, "Uptime"));
            table.put("VM name", con.getAttribute(on, "VmName"));
            table.put("VM vendor", con.getAttribute(on, "VmVendor"));
            table.put("VM version", con.getAttribute(on, "VmVersion"));
            table.put("Boot classpath supported", 
                    con.getAttribute(on, "BootClassPathSupported"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getSystemProperties() throws ControllerOperationException {
        try {
            Hashtable table = new Hashtable();
            TabularDataSupport tds = (TabularDataSupport) con.getAttribute(
                    new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME), 
                    "SystemProperties");
            
            for (Iterator it = tds.values().iterator(); it.hasNext(); ) {
                CompositeDataSupport cds = (CompositeDataSupport) it.next();
                Collection col = (Collection) cds.values();
                
                for (Iterator iter = col.iterator(); iter.hasNext(); ) {
                    table.put(iter.next(), iter.next());
                }
            }
            
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws ControllerOperationException
     */
    public Object getThreadCpuTime(long id) throws ControllerOperationException {
        
        try {
            return con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadCpuTime", 
                    new Object[] { new Long(id) }, 
                    new String[] { "long" });
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @return
     * @throws ControllerOperationException
     */
    public Hashtable getThreadInfo() throws ControllerOperationException {
        try {
            ObjectName on = 
                new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
            Hashtable table = new Hashtable(11);
            table.put("Thread count", con.getAttribute(on, "ThreadCount")); 
            table.put("Peak thread count", 
                    con.getAttribute(on, "PeakThreadCount"));
            table.put("Total started thread count", 
                    con.getAttribute(on, "TotalStartedThreadCount"));
            table.put("Daemon thread count", 
                    con.getAttribute(on, "DaemonThreadCount"));
            table.put("Thread contention monitoring supported", 
                    con.getAttribute(on, "ThreadContentionMonitoringSupported"));
            table.put("Thread contention monitoring enabled", 
                    con.getAttribute(on, "ThreadContentionMonitoringEnabled"));
            table.put("Current thread CPU time", 
                    con.getAttribute(on, "CurrentThreadCpuTime"));
            table.put("Current thread user time", 
                    con.getAttribute(on, "CurrentThreadUserTime"));
            table.put("Thread CPU time supported", 
                    con.getAttribute(on, "ThreadCpuTimeSupported"));
            table.put("Current thread CPU time supported", 
                    con.getAttribute(on, "CurrentThreadCpuTimeSupported"));
            table.put("Thread CPU time enabled", 
                    con.getAttribute(on, "ThreadCpuTimeEnabled"));
            return table;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws ControllerOperationException
     */
    public Collection getThreadInfo(long id) throws ControllerOperationException {
        try {
            return ((CompositeDataSupport) con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadInfo", 
                    new Object[] { new Long(id) }, 
                    new String[] { "long" })).values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param id
     * @param maxDepth
     * @return
     * @throws ControllerOperationException
     */
    public Collection getThreadInfo(long id, int maxDepth) 
            throws ControllerOperationException {
        
        try {
            return ((CompositeDataSupport) con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadInfo", 
                    new Object[] { new Long(id), new Integer(maxDepth) }, 
                    new String[] { "long", "int" })).values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param ids
     * @return
     * @throws ControllerOperationException
     */
    public Collection[] getThreadInfo(long[] ids) throws ControllerOperationException {
        try {
            CompositeData[] cd = (CompositeData[]) con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadInfo", 
                    new Object[] { ids }, 
                    new String[] { "[J" });
            Collection[] cols = new Collection[cd.length];
            
            for (int i = 0; i < cd.length; i++) {
                cols[i] = cd[i].values();
            }
            
            return cols;
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param ids
     * @param maxDepth
     * @return
     * @throws ControllerOperationException
     */
    public Object getThreadInfo(long[] ids, int maxDepth) 
            throws ControllerOperationException {
        
        try {
            return con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadInfo", 
                    new Object[] { ids, new Integer(maxDepth) }, 
                    new String[] { "[J", "int" });
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws ControllerOperationException
     */
    public Object getThreadUserTime(long id) throws ControllerOperationException {
        
        try {
            return con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "getThreadUserTime", 
                    new Object[] { new Long(id) }, 
                    new String[] { "long" });
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @return
     * @throws ControllerOperationException
     */
    public Collection getUsage(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            return ((CompositeDataSupport) con.getAttribute(
                    on, "Usage")).values();
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @throws ControllerOperationException
     */
    public void resetPeakThreadCount() throws ControllerOperationException {
        
        try {
            con.invoke(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
                    "resetPeakThreadCount", null, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    
    /**
     * 
     * @param on
     * @throws ControllerOperationException
     */
    public void resetPeakUsage(ObjectName on) 
            throws ControllerOperationException {
        
        try {
            con.invoke(on, "resetPeakUsage", null, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @throws ControllerOperationException
     */
    public void runGC() throws ControllerOperationException {
        try {
            con.invoke(new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME),
                    "gc", null, null);
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @param threhsold
     * @throws ControllerOperationException
     */
    public void setCollectionUsageThreshold(ObjectName on, long threhsold) 
            throws ControllerOperationException {
        
        try {
            con.setAttribute(on, new Attribute(
                    "CollectionUsageThreshold", new Long(threhsold)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param enable
     * @throws ControllerOperationException
     */
    public void setThreadContentionMonitoringEnabled(boolean enable) 
            throws ControllerOperationException {
        
        try {
            con.setAttribute(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME), 
                    new Attribute(
                            "ThreadContentionMonitoringEnabled", 
                            new Boolean(enable)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param enable
     * @throws ControllerOperationException
     */
    public void setThreadCpuTimeEnabled(boolean enable) 
            throws ControllerOperationException {
        
        try {
            con.setAttribute(
                    new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME), 
                    new Attribute(
                            "ThreadCpuTimeEnabled", 
                            new Boolean(enable)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param on
     * @param threhsold
     * @throws ControllerOperationException
     */
    public void setUsageThreshold(ObjectName on, long threhsold) 
            throws ControllerOperationException {
        
        try {
            con.setAttribute(on, new Attribute(
                    "UsageThreshold", new Long(threhsold)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param value
     */
    public void setVerboseCL(boolean value) throws ControllerOperationException {
        try {
            con.setAttribute(
                    new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME), 
                    new Attribute("Verbose", new Boolean(value)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /**
     * 
     * @param value
     */
    public void setVerboseM(boolean value) throws ControllerOperationException {
        try {
            con.setAttribute(
                    new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), 
                    new Attribute("Verbose", new Boolean(value)));
        } catch(Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
    
    /*
     * 
     */
    private void checkMXMBean(String on) 
            throws IOException, MalformedObjectNameException {
        
        if (!con.isRegistered(new ObjectName(on))) {
            throw new IllegalStateException(
                    "The specified MBeanServer reference is not valid. " +
                    "MXMBean " + on + " is not registerd.");
        }
    }
    
    /*
     * Further work with this class becomes useless if MXMBeans are
     * not registerd with MBeanServer reference specified in the constructor.
     */
    private void checkMXMBeans() throws ServiceNotInstalledException {
        try {
            checkMXMBean(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
            checkMXMBean(ManagementFactory.COMPILATION_MXBEAN_NAME);
            checkMXMBean(ManagementFactory.MEMORY_MXBEAN_NAME);
            checkMXMBean(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            checkMXMBean(ManagementFactory.RUNTIME_MXBEAN_NAME);
            checkMXMBean(ManagementFactory.THREAD_MXBEAN_NAME);
            
            if (getGarbageCollectorMBeans().size() == 0) {
                throw new IllegalStateException(
                        "Garbage collector MBeans are not registered");
            }
            
            if (getMemoryManagerMBeans().size() == 0) {
                throw new IllegalStateException(
                        "Memory manager MBeans are not registered");
            }
            
            if (getMemoryPoolMBeans().size() == 0) {
                throw new IllegalStateException(
                        "Memory poools MBeans are not registered");
            }
        } catch(Exception e) {
            ServiceNotInstalledException coe = new ServiceNotInstalledException(
                    e.getMessage());
            coe.initCause(e);
            throw coe;
        }
    }
}
