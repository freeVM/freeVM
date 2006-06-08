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
import java.io.Serializable;
import java.util.Iterator;

/**
 * Mock of BeanContextServiceProvider
 */
public class MockBeanContextServiceProviderS implements
		BeanContextServiceProvider, Serializable {

	public String id;

	public MockBeanContextServiceProviderS(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#getService(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Object, java.lang.Class, java.lang.Object)
	 */
	public Object getService(BeanContextServices bcs, Object requestor,
			Class serviceClass, Object serviceSelector) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#releaseService(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void releaseService(BeanContextServices bcs, Object requestor,
			Object service) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextServiceProvider#getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices,
	 *      java.lang.Class)
	 */
	public Iterator getCurrentServiceSelectors(BeanContextServices bcs,
			Class serviceClass) {
		return null;
	}

}
