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
 * @author Victor A. Martynov
 * @version $Revision: 1.4 $
 */
package org.apache.harmony.x.management.console.plugin;

import java.net.MalformedURLException;
import java.util.Vector;

import javax.management.remote.JMXServiceURL;

import org.apache.harmony.x.management.console.controller.Controller;
import org.apache.harmony.x.management.console.controller.ControllerFactory;
import org.apache.harmony.x.management.console.controller.MBeanOperations;
import org.apache.harmony.x.management.console.controller.NotificationHandler;
import org.apache.harmony.x.management.console.controller.VMMonitor;


public class Conn {

	public static final int NOT_CONNECTED = 0;
	public static final int VM_MODE = 1;
	public static final int NEW_SERVER_MODE = 2;
	public static final int URL_MODE = 3;
	public static final boolean DEBUG = true;
	
	private static int mode = 0;
	private static int port;
	private static boolean startJMXConnector;
	private static String  defaultDomain;
	private static String url;
	private static String user;
	private static String pass;
	private static MBeanTreeNode node;

	private static Controller controller;
	
	private static MBeanOperations mbo;
	
	private static VMMonitor vmmon = null;

	private static NotificationHandler notificationHandler = null;
	
	private static MBeanTreeView mbeanTreeView = null;
	
	private static Vector views = new Vector();

	/*
	 * Setters 
	 */
	static void setMode(int value) {
		mode = value;
	}

	static void setPort(int value) {
		port=value;
	}
	
	static void setStartJMXConnector(boolean value) {
		startJMXConnector = value;
	}
	
	static void setDefaultDomain(String value) {
		defaultDomain = value;
	}

	static void setUrl(String value) {
		url=value;
	}
	
	static void setUser(String value) {
		user=value;
		
	}

	static void setPass(String value) {
		pass=value;
		
	}
	
	/*
	 * Getters
	 */
	static int getMode() {
		return mode;
	}

	
	static int getPort() {
		return port;
	}
	
	static boolean getStartJMXConnector() {
		return startJMXConnector;
	}
	
	static String getDefaultDomain() {
		return defaultDomain;
	}

	static String getUrl() {
		 return url;
	}
	
	static String getUser() {
		 return user;
		
	}

	static String  getPass() {
		 return pass;
	}
	
	static void printConnection() {
	}
	
	static Controller getController() {
		if(controller == null) {
			controller = ControllerFactory.getController();
		}
		return controller;
	}

	public static MBeanOperations getMBeanOperations() {
		if(mbo == null) { 
			mbo = getController().getMBeanOperations();
		}
		return mbo;
	}

	static NotificationHandler getNotificationHandler() {
		if(notificationHandler == null) {
			 notificationHandler = getController().getNotificationService();
		}
		return notificationHandler;
	}

	static VMMonitor getVMMonitor() {
		if(vmmon == null) {
			try {
				vmmon = Conn.getController().getVMMonitor();
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		
		return vmmon;
	}

/*	public static void setMBeanTree(Tree tree) {
		mbeanTree = tree;
	}
	
	public static Tree getMBeanTree() {
		return mbeanTree;
	}*/
	
	public static String getStatus() {
		switch(mode) {
			case NOT_CONNECTED:
				return "Not Connected";
			case VM_MODE:
				return "Local VM";
			case NEW_SERVER_MODE:
				String s = startJMXConnector ? defaultDomain + ", port=" + port	: defaultDomain;

				return s;
			case URL_MODE:
				try {
					JMXServiceURL surl = new JMXServiceURL(url);
					return surl.getHost() + ":" + surl.getPort();
				}
				catch(MalformedURLException mue) {
					mue.printStackTrace();
					return "Exception: "+mue;
				}
			default: 
				return "Unrecognized Mode"; 
		}	
	}
	
	public static void setMBeanTreeView(MBeanTreeView view) {
		mbeanTreeView = view;
	}

	public static MBeanTreeView getMBeanTreeView() {
		return mbeanTreeView;
	}

	public static void resetController() {
		controller = null;
		mbo = null;
		vmmon = null;
		notificationHandler = null;
	}
	
	public static void addView(IMBeanTreeDependant view) {
		views.add(view);
	}
	
	public static void removeView(IMBeanTreeDependant view) {
		views.remove(view);
	}
	
	public static IMBeanTreeDependant[] getViews() {
		return (IMBeanTreeDependant[])views.toArray(new IMBeanTreeDependant[0]);
	}

	
//public static String getMBeanName() {
//		return mbeanName;
//}
	
//	public static void setMBeanName(String mbeanName) {
//		Conn.mbeanName = mbeanName;
//	}
	
	public static void setActiveNode(MBeanTreeNode newNode) {
		node = newNode;
	}
	
	public static MBeanTreeNode getActiveNode() {
		return node;
	}
}
