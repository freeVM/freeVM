/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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

package org.apache.harmony.beans.tests.support.beancontext.mock;

import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;

/**
 * Mock of BeanContextServiceRevokedListener
 */
public class MockBeanContextServiceRevokedListener implements
		BeanContextServiceRevokedListener {

	public BeanContextServiceRevokedEvent lastEvent;

	public void clearLastEvent() {
		lastEvent = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceRevokedListener#serviceRevoked(java.beans.beancontext.BeanContextServiceRevokedEvent)
	 */
	public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
		lastEvent = bcsre;
	}

}
