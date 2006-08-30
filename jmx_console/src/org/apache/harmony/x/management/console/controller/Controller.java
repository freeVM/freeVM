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

import java.util.List;


/**
 * An interface that works as a mid-layer between GUI and MBeanServer.
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public interface Controller {
    
    /**
     * Start a JMX compliant server in the same JVM as this Controller
     * registering MXBeans necessary to run VMMonitor.
     * 
     * @throws ConnectionException - If MBeanServer can not be created.
     */
    void connect() throws ConnectionException;
    
    /**
     * Start a JMX compliant server in the same JVM as this Controller.
     * 
     * @param defaultDomain - The defaul domain for a newly created MBeanServer.
     * @throws ConnectionException - If MBeanServer can not be created.
     */
    void connect(String defaultDomain) throws ConnectionException;
    
    /**
     * Connect remotely to a running instance of JMX compliant server.
     * 
     * @param url - A connection URL that contains protocol, host and port. 
     * @param login - A login.
     * @param pass - A password.
     * 
     * @throws ConnectionException - If MBean server not found at the URL.
     * @throws AuthorizationException - If credentials are not supplied or
     *         incorrect.
     */
    void connect(String url, String login, String pass) 
            throws ConnectionException, AuthorizationException;
    
    /** 
     * Erase any references to MBeanServerConnection interface.
     */
    void disconnect();
    
    /**
     * Get an access to common operations performed on MBeans.
     * 
     * @return MBeanOperations instance.
     * 
     * @throws ServerCommunicationException
     */
    MBeanOperations getMBeanOperations();
    
    /**
     * Get an access to a specific MLet service registered on MBeanServer
     * using its ObjectName.
     * 
     * @param objectName - The MLet service ObjectName.
     * 
     * @return MLetService instance.
     * 
     * @throws ServiceNotInstalledException - If such MLetService is not 
     *         registered.
     */
    MLetService getMLetService(String objectName) 
            throws ServiceNotInstalledException;
    
    /**
     * Get all MLet services registerd on MBeanServer.
     * 
     * @return A collection containing ObjectNames of MLet services.
     * 
     * @throws ControllerOperationException - If the Exception occurs while
     *         searching for MLet services.
     * @throws ServiceNotInstalledException - If no MLet service found on 
     *         a server.
     */
    List getMLetServices() throws ControllerOperationException, 
            ServiceNotInstalledException;
    
    /**
     * Get an access to a specific Monitor service registered on MBeanServer
     * using its ObjectName.
     * 
     * @param objectName - The Monitor service ObjectName.
     * 
     * @return MonitorService instance.
     * 
     * @throws ServiceNotInstalledException - If such MonitorService is not 
     *         registered.
     */
    MonitorService getMonitorService(String objectName) 
            throws ServiceNotInstalledException;
    
    /**
     * Get all Monitor services registerd on MBeanServer.
     * 
     * @return A collection containing ObjectNames of Monitor services.
     * 
     * @throws ControllerOperationException - If the Exception occurs while
     *         searching for Monitor services.
     * @throws ServiceNotInstalledException - If no Monitor service found on a 
     *         server.
     */
    List getMonitorServices() throws ControllerOperationException, 
            ServiceNotInstalledException;
    
    /**
     * Get service that allows to monitor some BroadCaster's activity.
     * 
     * @return NotificationHandler instance.
     */
    NotificationHandler getNotificationService();
    
    /**
     * Get an access to a specific Relation service registered on MBeanServer
     * using its ObjectName.
     * 
     * @param objectName - The RealtionService ObjectName.
     * 
     * @return RealtionService instance.
     * 
     * @throws ServiceNotInstalledException - If such RealtionService is not 
     *         registered.
     */
    RService getRelationService(String objectName) 
            throws ServiceNotInstalledException;
    
    /**
     * Get all Relation services registerd on MBeanServer.
     * 
     * @return A collection containing ObjectNames of Relation services.
     * 
     * @throws ControllerOperationException - If the Exception occurs while
     *         searching for Relation services.
     * @throws ServiceNotInstalledException - If no Relation service found on 
     *         a server.
     */
    List getRelationServices() throws ControllerOperationException, 
            ServiceNotInstalledException;
    
    /**
     * Get an access to a specific Timer service registered on MBeanServer
     * using its ObjectName.
     * 
     * @param objectName - The Timer service ObjectName.
     * 
     * @return Timer service instance.
     * 
     * @throws ServiceNotInstalledException - If such Timer service is not 
     *         registered.
     */
    TimerService getTimerService(String objectName) 
            throws ServiceNotInstalledException;
    
    /**
     * Get all Timer services registerd on MBeanServer.
     * 
     * @return A collection containing ObjectNames of Timer services.
     * 
     * @throws ControllerOperationException - If the Exception occurs while
     *         searching for Timer services.
     * @throws ServiceNotInstalledException - If no Timer service found on 
     *         a server.
     */
    List getTimerServices() throws ControllerOperationException, 
            ServiceNotInstalledException;
    
    /**
     * 
     * @return
     * @throws ServiceNotInstalledException
     */
    VMMonitor getVMMonitor() throws ServiceNotInstalledException;
}