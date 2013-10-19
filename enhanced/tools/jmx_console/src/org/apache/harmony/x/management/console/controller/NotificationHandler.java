/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Hashtable;
import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;


/**
 * Monitor Notifications sent by a specific broadcaster.
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class NotificationHandler implements NotificationListener {

    /**
     * A collection of broadcasters this listener is currently registered with.
     */
    private Hashtable broadcasters;
    
    /**
     * A reference to MBeanServer.
     */
    private MBeanServerConnection con;
    
    /**
     * Cobstruct this object.
     * 
     * @param con A reference to MBeanServer.
     */
    public NotificationHandler(MBeanServerConnection con) {
        this.broadcasters = new Hashtable();
        this.con = con;
    }
    
    /**
     * Get Notifications of a broadcaster received since 
     * last call of this method. 
     * 
     * @param broadcaster The ObjectName of a broadcaster.
     * 
     * @return A List of Notifications.
     */
    public Vector getNotifications(String broadcaster) 
            throws ControllerOperationException {
        
        try {
            ObjectName on = new ObjectName(broadcaster);
            
            synchronized (broadcasters) {
                Vector list = (Vector) broadcasters.get(on);
                broadcasters.put(on, new Vector());
                
                return list;
            }
        } catch (MalformedObjectNameException e) {
            ControllerOperationException coe = 
                new ControllerOperationException(e.getMessage());
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Store Notification in the collection under its broadcaster's ObjectName. 
     * 
     * @see NotificationListener#handleNotification(Notification, Object)
     */
    public void handleNotification(Notification not, Object object) {
        Object on = not.getSource();
        
        synchronized (broadcasters) {
            if (broadcasters.containsKey(on)) {
                Vector list = (Vector) broadcasters.get(on);
                list.add(not);
            }
        }
    }
    
    /**
     * Call MBeanServer to add this Listener to the broadcaster whose
     * ObjectName is specified.
     * 
     * @param broadcaster The ObjectName of a broadcaster.
     * 
     * @throws ControllerOperationException Wrap any Exception.
     */
    public void subscribe(String broadcaster) 
            throws ControllerOperationException {
        
        try {
            ObjectName on = new ObjectName(broadcaster); 
            con.addNotificationListener(on, this, null, null);
            
            synchronized (broadcasters) {
                broadcasters.put(on, new Vector());
            }
        } catch (Exception e) {
            ControllerOperationException coe = 
                new ControllerOperationException(e.getMessage());
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Call MBeanServer to remove this Listener from the broadcaster whose
     * ObjectName is specified.
     * 
     * @param broadcaster The ObjectName of a broadcaster. 
     * 
     * @throws ControllerOperationException Wrap any Exception.
     */
    public void unsubscribe(String broadcaster) 
            throws ControllerOperationException {
        
        try {
            ObjectName on = new ObjectName(broadcaster); 
            con.removeNotificationListener(on, this);
            
            synchronized (broadcasters) {
                broadcasters.remove(on);
            }
        } catch (Exception e) {
            ControllerOperationException coe = 
                new ControllerOperationException(e.getMessage());
            coe.initCause(e);
            throw coe;
        } 
    }
}
