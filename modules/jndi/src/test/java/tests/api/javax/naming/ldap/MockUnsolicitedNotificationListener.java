/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.javax.naming.ldap;

import javax.naming.event.NamingExceptionEvent;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;

public class MockUnsolicitedNotificationListener implements
		UnsolicitedNotificationListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.ldap.UnsolicitedNotificationListener#notificationReceived(javax.naming.ldap.UnsolicitedNotificationEvent)
	 */
	public void notificationReceived(UnsolicitedNotificationEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.event.NamingListener#namingExceptionThrown(javax.naming.event.NamingExceptionEvent)
	 */
	public void namingExceptionThrown(NamingExceptionEvent namingexceptionevent) {

	}

}
