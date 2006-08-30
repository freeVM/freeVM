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
 * @author Victor A. Martynov
 * @version $Revision: 1.3 $
 */

package org.apache.harmony.x.management.console.plugin;

import java.util.Vector;

public class MBeanTreeNode {
	boolean isSubscribed;

	Vector notifications;
	String name;

	public MBeanTreeNode(String name) {
		isSubscribed = false;
		this.name = name;
	}

	public void setSubscribed(boolean val) {
		isSubscribed = val;
		if (val) {
			notifications = new Vector();
		} else {
			notifications = null;
		}
	}

	public boolean getSubscribed() {
		return isSubscribed;
	}

	public Vector getNotifications() {
		return notifications;
	}
	
	public String getName() {
		return name;
	}
	

	public String toString() {
		return "MBeanTreeNode{"+name+"}";
	}
}