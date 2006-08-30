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
 * @version $Revision: 1.4 $
 */
package org.apache.harmony.x.management.console.controller;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * Handles standard operations on MBeans, such as registering, 
 * setting attributes, etc. No difference made for MBeans that are also
 * JMX services. This class is intended to be initiated from Controller class
 * and to be used by GUI.      
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.4 $
 */
public class MBeanOperations {
    
    /**
     * A map containing primitives as its keys and their corresponding
     * wrapper classes as its values.
     */
	private static Hashtable primWrappers = new Hashtable(8);
    static {
    	primWrappers.put("boolean", Boolean.class);
    	primWrappers.put("byte", Byte.class);
    	primWrappers.put("char", Character.class);
    	primWrappers.put("double", Double.class);
    	primWrappers.put("float", Float.class);
    	primWrappers.put("int", Integer.class);
    	primWrappers.put("long", Long.class);            
    	primWrappers.put("short", Short.class);
    }
    
    /**
     * A map of primitive types as values and its string representation as keys.
     */
    private static HashMap primitives = new HashMap(8);
    static {
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);            
        primitives.put("short", Short.TYPE);
    }
	
    /**
     * A reference to MBeanServer.
     */
    private MBeanServerConnection con;
    
    /**
     * Construct this object. It may be called from the Controller only.
     * 
     * @param con A reference to MBeanServer this class is initialized with.
     *            Can not be null.  
     */
    MBeanOperations(MBeanServerConnection con) {
        this.con = con;
    }
    
    /**
     * Invoke a method on MBean converting the given parameters
     * from Strings to their actual Objects. Only primWrappers and Objects 
     * that have constructor with single String parameter are allowed.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * @param operation - A name of the method to be invoked.
     * @param params - An array of params in String format received from GUI.
     * @param sign - The signature of the method to be invoked.
     * 
     * @return The result of the method's execution.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public Object execute(String name, String operation, String[] params,
            String[] sign) throws ControllerOperationException {
        
        try {
            Object[] p = null;
            
            if (sign != null) {
                p = new Object[sign.length];
                
                for (int i = 0; i < p.length; i++) {
                    Class cl = (Class) primWrappers.get(sign[i]);
                    
                    if (cl == null) {
                        cl = Class.forName(sign[i]);
                    }
                    
                    p[i] = cl.getConstructor(
                            new Class[] { String.class }).newInstance(
                                    new Object[] { params[i] });
                }
            }
            
            return con.invoke(new ObjectName(name), operation, p, sign);
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Convert String to ObjectName and invoke the corresponding method
     * of the MBeanServerConnection.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * @param att - A name of the attribute.
     * 
     * @return The attribute's value.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public Object getAttribute(String name, String att) 
            throws ControllerOperationException {
        
        try {
            return this.con.getAttribute(new ObjectName(name), att);
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Get all unique domains that are used for currently registered MBeans.
     * 
     * @return An array of domains sorted in lexographical order.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public String[] getAllDomains() throws ControllerOperationException {
        String[] dom = null;
        
        try {
            dom = this.con.getDomains();
        } catch (IOException e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        }
        
        Arrays.sort(dom);
        return dom; 
    }

    /**
     * Get MBeanInfo for the MBean specified.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @return MBeanInfo of a given MBean.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public MBeanInfo getMBeanInfo(String name) 
            throws ControllerOperationException {
        
        try {
            return this.con.getMBeanInfo(new ObjectName(name));
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Get all properties of the MBean specified.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @return A Hashtable contating keys and values of the MBean's properties.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public Hashtable getMBeanProperties(String name) 
            throws ControllerOperationException {
        
        ObjectName on = null;
        
        try {
            on = new ObjectName(name);
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
        
        return on.getKeyPropertyList();
    }
    
    /**
     * Get all MBeans that are registered under given domain.
     * 
     * @param domain - A domain for which MBeans will be listed.
     * 
     * @return An array of the matching ObjectNames in canonical form.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public String[] getMBeansOfDomain(String domain) 
            throws ControllerOperationException {
        
        Set set = null;
        
        try {
            set = this.con.queryNames(new ObjectName(domain + ":*"), null);
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException();
            coe.initCause(e);
            throw coe;
        } 
        
        String[] mbeans = new String[set.size()];
        int i = 0;
        
        for (Iterator it = set.iterator(); it.hasNext(); i++) {
            ObjectName on = (ObjectName) it.next();
            mbeans[i] = on.getCanonicalName();
        }
        
        return mbeans;
    }
    
    /**
     * Get all attributes, their values and 
     * read/write access flags for the MBean specified.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @return A collection of all attributes, their values and 
     *         read/write access flags for the MBean specified.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public List listAttributes(String name) throws ControllerOperationException {
        MBeanAttributeInfo[] ai = getMBeanInfo(name).getAttributes();
        List list = new ArrayList(ai.length);
        
        for (int i = 0; i < ai.length; i++) {
            AttributeInfo info = new AttributeInfo(
                    ai[i].getName(), 
                    getAttribute(name, ai[i].getName()), 
                    ai[i].isReadable(),
                    ai[i].isWritable(),
                    ai[i].getType());
            list.add(info);
        }
        
        return list;
    }
    
    /**
     * 
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @return
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public List listNotifications(String name) 
            throws ControllerOperationException {
        
        MBeanNotificationInfo[] ni = getMBeanInfo(name).getNotifications();
        List list = new ArrayList(ni.length);
        
        for (int i = 0; i < ni.length; i++) {
            //OperationInfo info = new OperationInfo(); // Notifications
            //list.add(info);
        }
        
        return list;
    }
    
    /**
     * Get all operations, their return types and 
     * signatures for the MBean specified. For each operation set a flag
     * if it can be executed from GUI. To qualify all parameters of the
     * operation must be either primWrappers or Objects that have a constructor 
     * with a single String parameter.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @return A collection of all operations, their return types and 
     *         signatures for the MBean specified.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public List listOperations(String name) throws ControllerOperationException {
        MBeanOperationInfo[] oi = getMBeanInfo(name).getOperations();
        List list = new ArrayList(oi.length);
        
        for (int i = 0; i < oi.length; i++) {
            MBeanOperationInfo in = oi[i];
            MBeanParameterInfo[] pi = in.getSignature();
            String[] sign = new String[pi.length];
            boolean executable = true;
            
            for (int j = 0; j < pi.length; j++) {
                sign[j] = pi[j].getType();
                Class cl = (Class) primitives.get(sign[j]);
                
                if (cl == null) {
                    try {
                        cl = Class.forName(sign[j]); 
                    } catch(ClassNotFoundException cnfe) {
                        executable = false;
                    }
                }
                
                try {
                    cl.getConstructor(new Class[] { String.class });
                } catch (SecurityException e) {
                    executable = false;
                } catch (NoSuchMethodException e) {
                    executable = false;
                }
            }
            
            OperationInfo info = new OperationInfo(
                    in.getName(),
                    null,
                    sign,
                    in.getReturnType(),
                    executable);
            list.add(info);
        }
        
        return list;
    }
    
    /**
     * Register a new MBean on MBeanServer. The MBean's class should be
     * loadable through the MBeanServer's repository.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public void registerMBean(String className, String name) 
            throws ControllerOperationException {
        
        try {
            con.createMBean(className, 
                    (name == null ? null : new ObjectName(name)));
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }

    /**
     * Set a new attribute value for the MBean's attribute.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * @param key - A name of the attribute.
     * @param value - A value of the attribute.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public void setAttribute(String name, String key, String val, String type) 
        throws ControllerOperationException {

    	try {
    		Class cl = (Class) primWrappers.get(type); 

            if (cl == null) {
                cl = Class.forName(type);
            }
            
            Constructor constructor = cl.getConstructor(
                    new Class[] { String.class });
            Object obj = constructor.newInstance(new Object[] { val });
            con.setAttribute(new ObjectName(name), new Attribute(key, obj));
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /**
     * Unregister MBean specified by its ObjectName.
     * 
     * @param name - A String representation of the MBean's ObjectName.
     * 
     * @throws ControllerOperationException - Wraps any actual Exception.
     */
    public void unregisterMBean(String name) 
            throws ControllerOperationException {
        
        try {
            this.con.unregisterMBean(new ObjectName(name));
        } catch (Exception e) {
            ControllerOperationException coe = new ControllerOperationException(
                    getCauseMsg(e));
            coe.initCause(e);
            throw coe;
        } 
    }
    
    /*
     * 
     */
    private String getCauseMsg(Throwable e) {
        Throwable t = e.getCause();
        
        if (t == null) {
            return e.getMessage();
        } else {
            return getCauseMsg(t);
        }
    }
}
