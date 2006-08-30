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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * An implementation of Controller interface.
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class ControllerImpl implements Controller {
    
    /**
     * A reference to MBeanServer.
     */
    private MBeanServerConnection con = null;
    
    /**
     * {@inheritDoc} 
     */
    public void connect() 
            throws ConnectionException {
         
        this.con = ManagementFactory.getPlatformMBeanServer();
        
        if (con == null) {
            throw new ConnectionException(
                    "Connection to a platform MBean server failed");
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    public void connect(String defaultDomain) 
            throws ConnectionException {
         
        this.con = MBeanServerFactory.createMBeanServer(defaultDomain);
        
        if (con == null) {
            throw new ConnectionException(
                    "Connection to a local MBean server failed");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void connect(String url, String login, String pass) 
            throws ConnectionException, AuthorizationException {
        
        try {
            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
            this.con = jmxc.getMBeanServerConnection();
        }
        catch(Exception e) {
            ConnectionException snfe = new ConnectionException(
                    "Connection to a remote MBean server failed: " +
                    e.getMessage());
            snfe.initCause(e);
            throw snfe;
        }
        
        if (con == null) {
            throw new ConnectionException(
                    "Connection to a remote MBean server failed");
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public void disconnect() {
        this.con = null;
    }
    
    /**
     * Create new instance of this class.
     * 
     * @return This instance.
     */
    public static Controller getController() {
        return new ControllerImpl();
    }
    
    /**
     * {@inheritDoc}
     */
    public MBeanOperations getMBeanOperations() {
        if (isConnected()) {
            return new MBeanOperations(con);
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public MLetService getMLetService(String objectName) 
            throws ServiceNotInstalledException {
        
        if (isConnected()) {
            try {
                return new MLetService(con, new ObjectName(objectName));
            } catch(MalformedObjectNameException e) {
                ServiceNotInstalledException snie = 
                    new ServiceNotInstalledException(e.getMessage());
                snie.initCause(e);
                throw snie;
            }
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List getMLetServices() throws ControllerOperationException, 
            ServiceNotInstalledException {
        
        return getService("javax.management.loading.MLet", "MLet");
    }
    
    /**
     * {@inheritDoc}
     */
    public MonitorService getMonitorService(String objectName) 
            throws ServiceNotInstalledException {
        
        if (isConnected()) {
            try {
                return new MonitorService(con, new ObjectName(objectName));
            } catch(MalformedObjectNameException e) {
                ServiceNotInstalledException snie = 
                    new ServiceNotInstalledException(e.getMessage());
                snie.initCause(e);
                throw snie;
            }
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List getMonitorServices() throws ControllerOperationException, 
            ServiceNotInstalledException {
        
        return getService("javax.management.monitor.Monitor", "Monitor");
    }
    
    /**
     * {@inheritDoc}
     */
    public NotificationHandler getNotificationService() {
        if (isConnected()) {
            return new NotificationHandler(con);
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public RService getRelationService(String objectName) 
            throws ServiceNotInstalledException {
        
        if (isConnected()) {
            try {
                return new RService(con, new ObjectName(objectName));
            } catch(MalformedObjectNameException e) {
                ServiceNotInstalledException snie = 
                    new ServiceNotInstalledException(e.getMessage());
                snie.initCause(e);
                throw snie;
            }
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List getRelationServices() throws ControllerOperationException, 
            ServiceNotInstalledException {
        
        return getService("javax.management.relation.RelationService", 
                "Relation");
    }
    
    /**
     * {@inheritDoc}
     */
    public TimerService getTimerService(String objectName) 
            throws ServiceNotInstalledException {
        
        if (isConnected()) {
            try {
                return new TimerService(con, new ObjectName(objectName));
            } catch(MalformedObjectNameException e) {
                ServiceNotInstalledException snie = 
                    new ServiceNotInstalledException(e.getMessage());
                snie.initCause(e);
                throw snie;
            }
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List getTimerServices() throws ControllerOperationException, 
            ServiceNotInstalledException {
        
        return getService("javax.management.timer.Timer", "Timer");
    }
    
    /**
     * {@inheritDoc}
     */
    public VMMonitor getVMMonitor() throws ServiceNotInstalledException {
        if (isConnected()) {
            return new VMMonitor(con);
        } else {
            throw new IllegalStateException("Not connected to MBean server");
        }
    }
    
    /*
     * Query MBeans using a complete pattern, so all currently registered
     * MBeans will be selected initially. Then filter MBeans by selecting
     * only those that are instances of the class specified.  
     * 
     * @param serviceClass
     * @param serviceName
     * 
     * @return
     * 
     * @throws ControllerOperationException
     * @throws ServiceNotInstalledException
     */
    private List getService(String serviceClass, String serviceName) 
            throws ControllerOperationException, ServiceNotInstalledException {
        
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to MBean server");
        }
        
        List list = new ArrayList();
        
        try {
            Set set = con.queryNames(new ObjectName("*:*"), null);
            
            for (Iterator it = set.iterator(); it.hasNext(); ) {
                ObjectName on = (ObjectName) it.next();
                
                if (con.isInstanceOf(on, serviceClass)) {
                    list.add(on);
                }
            }
        } catch(Exception e) {
            ControllerOperationException snie = 
                new ControllerOperationException(e.getMessage());
            snie.initCause(e);
            throw snie;
        }
        
        if (list.size() == 0) {
            throw new ServiceNotInstalledException(
                    "No " + serviceName + " service found on this MBean server");
        } else {
            return list;
        }
    }
    
    /**
     * Check whether a connection to MBeanServer has been established.
     */
    private boolean isConnected() {
        return (con != null);
    }
}
