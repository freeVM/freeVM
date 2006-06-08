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

package org.apache.harmony.beans.tests.java.beans.beancontext.mock;

import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServices;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.beans.tests.java.beans.beancontext.MethodInvocationRecords;

/**
 * Mock of BeanContextServiceProvider
 */
public class MockBeanContextServiceProvider implements
		BeanContextServiceProvider {

	public MethodInvocationRecords records = new MethodInvocationRecords();

	public List selectors = Arrays.asList(new Object[] { Integer.class });

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#getService(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Object, java.lang.Class, java.lang.Object)
	 */
	public Object getService(BeanContextServices bcs, Object requestor,
			Class serviceClass, Object serviceSelector) {
		Object result = Collections.EMPTY_SET;
		records.add("getService", bcs, requestor, serviceClass,
				serviceSelector, result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#releaseService(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void releaseService(BeanContextServices bcs, Object requestor,
			Object service) {
		records.add("releaseService", bcs, requestor, service, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Class)
	 */
	public Iterator getCurrentServiceSelectors(BeanContextServices bcs,
			Class serviceClass) {
		Iterator result = selectors.iterator();
		records.add("getCurrentServiceSelectors", bcs, serviceClass, result);
		return result;
	}

}
