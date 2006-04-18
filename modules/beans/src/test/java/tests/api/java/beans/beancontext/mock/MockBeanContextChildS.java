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

package tests.api.java.beans.beancontext.mock;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.io.Serializable;

/**
 * Mock of BeanContextChild
 */
public class MockBeanContextChildS implements BeanContextChild, Serializable {

	public String id;

	private BeanContext ctx;

	public MockBeanContextChildS(String id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (o instanceof MockBeanContextChildS) {
			return id.equals(((MockBeanContextChildS) o).id);
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#setBeanContext(java.beans.beancontext.BeanContext)
	 */
	public void setBeanContext(BeanContext bc) throws PropertyVetoException {
		ctx = bc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#getBeanContext()
	 */
	public BeanContext getBeanContext() {
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String name,
			PropertyChangeListener pcl) {
		// Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#removePropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String name,
			PropertyChangeListener pcl) {
		// Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#addVetoableChangeListener(java.lang.String,
	 *      java.beans.VetoableChangeListener)
	 */
	public void addVetoableChangeListener(String name,
			VetoableChangeListener vcl) {
		// Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.beancontext.BeanContextChild#removeVetoableChangeListener(java.lang.String,
	 *      java.beans.VetoableChangeListener)
	 */
	public void removeVetoableChangeListener(String name,
			VetoableChangeListener vcl) {
		// Auto-generated method stub

	}

}
