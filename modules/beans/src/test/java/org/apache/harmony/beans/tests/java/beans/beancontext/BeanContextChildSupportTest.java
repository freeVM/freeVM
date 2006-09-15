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

package org.apache.harmony.beans.tests.java.beans.beancontext;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.IOException;
import java.io.Serializable;

import org.apache.harmony.beans.tests.support.beancontext.Utils;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockBeanContext;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockBeanContextChild;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockBeanContextChildDelegateS;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockPropertyChangeListener;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockPropertyChangeListenerS;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockVetoChangeListener;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockVetoableChangeListener;
import org.apache.harmony.beans.tests.support.beancontext.mock.MockVetoableChangeListenerS;

import tests.util.SerializationTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test BeanContextChildSupport
 */
public class BeanContextChildSupportTest extends TestCase {

	private static class MockBeanContextChildSupport extends
			BeanContextChildSupport {

        static final long serialVersionUID = -8602521752077435319L;
        
		public BeanContext lastInitBeanContext = null;

		public BeanContext lastReleaseBeanContext = null;

		public boolean vetoBeanContext = false;

		/**
         * 
         */
		public MockBeanContextChildSupport() {
			super();
			assertNull(this.beanContext);
			assertSame(this, this.beanContextChildPeer);
			assertFalse(this.rejectedSetBCOnce);
		}

		/**
         * @param bcc
         */
		public MockBeanContextChildSupport(BeanContextChild bcc) {
			super(bcc);
			assertNull(this.beanContext);
			assertSame(bcc, this.beanContextChildPeer);
			assertFalse(this.rejectedSetBCOnce);
		}

		public boolean rejectedSetBCOnce() {
			return rejectedSetBCOnce;
		}

		/*
         * (non-Javadoc)
         * 
         * @see java.beans.beancontext.BeanContextChildSupport#initializeBeanContextResources()
         */
		protected void initializeBeanContextResources() {
			lastInitBeanContext = this.beanContext;
		}

		/*
         * (non-Javadoc)
         * 
         * @see java.beans.beancontext.BeanContextChildSupport#releaseBeanContextResources()
         */
		protected void releaseBeanContextResources() {
			lastReleaseBeanContext = this.beanContext;
		}

		public void clearLastRecords() {
			lastInitBeanContext = null;
			lastReleaseBeanContext = null;
		}

		/*
         * (non-Javadoc)
         * 
         * @see java.beans.beancontext.BeanContextChildSupport#validatePendingSetBeanContext(java.beans.beancontext.BeanContext)
         */
		public boolean validatePendingSetBeanContext(BeanContext newValue) {
			if (vetoBeanContext) {
				return false;
			}
			return super.validatePendingSetBeanContext(newValue);
		}
	}

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(BeanContextChildSupportTest.class);
	}

	public void testAddPropertyChangeListener_NullParam() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();

		try {
			support.addPropertyChangeListener(null,
					new MockPropertyChangeListener());
			fail();
		} catch (NullPointerException e) {
			// expected
		}

		try {
			support.addPropertyChangeListener("property name", null);
			support.firePropertyChange("property name", "old value",
					"new value");
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testAddPropertyChangeListener() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockPropertyChangeListener l2 = new MockPropertyChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		support.addPropertyChangeListener(propName, l1);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);

		support.addPropertyChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange("xxx", oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);
	}

	public void testAddVetoableChangeListener_NullParam()
			throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();

		try {
			support.addVetoableChangeListener(null,
					new MockVetoableChangeListener());
			fail();
		} catch (NullPointerException e) {
			// expected
		}

		try {
			support.addVetoableChangeListener("property name", null);
			support.fireVetoableChange("property name", "old value",
					"new value");
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testAddVetoableChangeListener() throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		support.addVetoableChangeListener(propName, l1);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);

		support.addVetoableChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange("xxx", oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);
	}

	/*
     * Class under test for void BeanContextChildSupport()
     */
	public void testBeanContextChildSupport() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		assertSame(support, support.getBeanContextChildPeer());
		assertSame(support, support.beanContextChildPeer);
	}

	/*
     * Class under test for void
     * BeanContextChildSupport(java.beans.beancontext.BeanContextChild)
     */
	public void testBeanContextChildSupportBeanContextChild() {
		BeanContextChild c = new MockBeanContextChild();
		BeanContextChildSupport support = new MockBeanContextChildSupport(c);
		assertSame(c, support.getBeanContextChildPeer());
		assertSame(c, support.beanContextChildPeer);
	}

	public void testFirePropertyChange_NullParam() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.firePropertyChange(null, "a", "b");
	}

	public void testFirePropertyChange() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockPropertyChangeListener l2 = new MockPropertyChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		support.addPropertyChangeListener(propName, l1);
		support.addPropertyChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);
	}

	public void testFirePropertyChange_OldEqualsNew() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockPropertyChangeListener l2 = new MockPropertyChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(1);

		support.addPropertyChangeListener(propName, l1);
		support.addPropertyChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);
	}

	public void testFirePropertyChange_OldEqualsNew_IsNull() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockPropertyChangeListener l2 = new MockPropertyChangeListener();
		String propName = "property name";
		Object oldValue = null;
		Object newValue = null;

		support.addPropertyChangeListener(propName, l1);
		support.addPropertyChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertNull(l1.lastEvent.getOldValue());
		assertNull(l1.lastEvent.getNewValue());
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);
	}

	public void testFireVetoableChange_NullParam() throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.fireVetoableChange(null, "a", "b");
	}

	public void testFireVetoableChange() throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		support.addVetoableChangeListener(propName, l1);
		support.addVetoableChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);
	}

	public void testFireVetoableChange_Vetoed() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		MockVetoChangeListener l3 = new MockVetoChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		support.addVetoableChangeListener(propName, l1);
		support.addVetoableChangeListener(propName, l2);
		support.addVetoableChangeListener(propName, l3);
		l1.clearLastEvent();
		l2.clearLastEvent();
		l3.clearLastEvent();
		try {
			support.fireVetoableChange(propName, oldValue, newValue);
			fail();
		} catch (PropertyVetoException e) {
			// expected
		}
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), newValue);
		assertSame(l1.lastEvent.getNewValue(), oldValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), newValue);
		assertSame(l2.lastEvent.getNewValue(), oldValue);
		assertSame(l2.lastEvent.getSource(), support);
		assertEquals(l3.lastEvent.getPropertyName(), propName);
		assertSame(l3.lastEvent.getOldValue(), newValue);
		assertSame(l3.lastEvent.getNewValue(), oldValue);
		assertSame(l3.lastEvent.getSource(), support);
	}

	public void testFireVetoableChange_OldEqualsNew()
			throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(1);

		support.addVetoableChangeListener(propName, l1);
		support.addVetoableChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);
	}

	public void testFireVetoableChange_OldEqualsNew_IsNull()
			throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		String propName = "property name";
		Object oldValue = null;
		Object newValue = null;

		support.addVetoableChangeListener(propName, l1);
		support.addVetoableChangeListener("xxx", l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertNull(l1.lastEvent.getOldValue());
		assertNull(l1.lastEvent.getNewValue());
		assertSame(l1.lastEvent.getSource(), support);
		assertNull(l2.lastEvent);
	}

	public void testGetBeanContext() throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockBeanContext mockBeanContext = new MockBeanContext();
		assertNull(support.getBeanContext());
		support.setBeanContext(mockBeanContext);
		assertSame(mockBeanContext, support.getBeanContext());
	}

	public void testGetBeanContextChildPeer() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		assertSame(support, support.beanContextChildPeer);
		assertSame(support, support.getBeanContextChildPeer());

		BeanContextChild mockChild = new MockBeanContextChild();
		support = new MockBeanContextChildSupport(mockChild);
		assertSame(mockChild, support.beanContextChildPeer);
		assertSame(mockChild, support.getBeanContextChildPeer());
        
        try {
            BeanContextChildSupport sup = new BeanContextChildSupport();

            if (!sup.getBeanContextChildPeer().equals(sup)) {
                fail("The objects should be equal");
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e + " caused by: " + e.getCause());
        }
	}

	public void testInitializeBeanContextResources()
			throws PropertyVetoException {
		MockBeanContextChildSupport support = new MockBeanContextChildSupport();
		assertNull(support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);
		MockBeanContext ctx1 = new MockBeanContext();
		MockBeanContext ctx2 = new MockBeanContext();

		support.clearLastRecords();
		support.setBeanContext(ctx1);
		assertSame(ctx1, support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(ctx1);
		assertNull(support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(ctx2);
		assertSame(ctx2, support.lastInitBeanContext);
		assertSame(ctx1, support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(null);
		assertNull(support.lastInitBeanContext);
		assertSame(ctx2, support.lastReleaseBeanContext);
	}

	public void testIsDelegated() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		assertFalse(support.isDelegated());

		BeanContextChild mockChild = new MockBeanContextChild();
		support = new MockBeanContextChildSupport(mockChild);
		assertTrue(support.isDelegated());

		support.beanContextChildPeer = support;
		assertFalse(support.isDelegated());
        
        try {
            BeanContextChildSupport sup = new BeanContextChildSupport();

            if (sup.isDelegated()) {
                fail("Child is not supposed to be delegated");
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e + " caused by: " + e.getCause());
        }
	}

	public void testReleaseBeanContextResources() throws PropertyVetoException {
		MockBeanContextChildSupport support = new MockBeanContextChildSupport();
		assertNull(support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);
		MockBeanContext ctx1 = new MockBeanContext();
		MockBeanContext ctx2 = new MockBeanContext();

		support.clearLastRecords();
		support.setBeanContext(ctx1);
		assertSame(ctx1, support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(ctx1);
		assertNull(support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(ctx2);
		assertSame(ctx2, support.lastInitBeanContext);
		assertSame(ctx1, support.lastReleaseBeanContext);

		support.clearLastRecords();
		support.setBeanContext(null);
		assertNull(support.lastInitBeanContext);
		assertSame(ctx2, support.lastReleaseBeanContext);
	}

	public void testRemovePropertyChangeListener_NullParam() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.removePropertyChangeListener("property name", null);
	}

	public void testRemovePropertyChangeListener() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockPropertyChangeListener l2 = new MockPropertyChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		support.addPropertyChangeListener(propName, l1);
		support.addPropertyChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		support.removePropertyChangeListener(propName, l1);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		support.removePropertyChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.firePropertyChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		// remove not-registered listener
		support.removePropertyChangeListener(propName, l1);
	}

	public void testRemoveVetoableChangeListener_NullParam() {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.removeVetoableChangeListener("property name", null);
	}

	public void testRemoveVetoableChangeListener() throws PropertyVetoException {
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		MockVetoableChangeListener l1 = new MockVetoableChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		String propName = "property name";
		Object oldValue = new Integer(1);
		Object newValue = new Integer(5);

		support.addVetoableChangeListener(propName, l1);
		support.addVetoableChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertEquals(l1.lastEvent.getPropertyName(), propName);
		assertSame(l1.lastEvent.getOldValue(), oldValue);
		assertSame(l1.lastEvent.getNewValue(), newValue);
		assertSame(l1.lastEvent.getSource(), support);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		support.removeVetoableChangeListener(propName, l1);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertEquals(l2.lastEvent.getPropertyName(), propName);
		assertSame(l2.lastEvent.getOldValue(), oldValue);
		assertSame(l2.lastEvent.getNewValue(), newValue);
		assertSame(l2.lastEvent.getSource(), support);

		support.removeVetoableChangeListener(propName, l2);
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.fireVetoableChange(propName, oldValue, newValue);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		// remove not-registered listener
		support.removeVetoableChangeListener(propName, l1);
	}

	public void testServiceAvailable() {
		// guess the impl is empty
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.serviceAvailable(null);
        
        // Regression for HARMONY-372
        (new java.beans.beancontext.BeanContextChildSupport()).serviceAvailable(null);
        (new java.beans.beancontext.BeanContextChildSupport()).serviceRevoked(null); 
	}

	public void testServiceRevoked() {
		// guess the impl is empty
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		support.serviceRevoked(null);
	}

	public void testSetBeanContext() throws PropertyVetoException {
		BeanContextChild peer = new MockBeanContextChild();
		MockBeanContextChildSupport support = new MockBeanContextChildSupport(
				peer);
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		support.addPropertyChangeListener("beanContext", l1);
		support.addVetoableChangeListener("beanContext", l2);

		MockBeanContext ctx = new MockBeanContext();
		assertNull(support.getBeanContext());

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.setBeanContext(null);
		assertNull(support.getBeanContext());
		assertNull(support.lastInitBeanContext);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.setBeanContext(ctx);
		assertSame(ctx, support.getBeanContext());
		assertSame(ctx, support.lastInitBeanContext);
		assertEquals(l1.lastEvent.getPropertyName(), "beanContext");
		assertNull(l1.lastEvent.getOldValue());
		assertSame(l1.lastEvent.getNewValue(), ctx);
		assertSame(l1.lastEvent.getSource(), peer);
		assertEquals(l2.lastEvent.getPropertyName(), "beanContext");
		assertNull(l2.lastEvent.getOldValue());
		assertSame(l2.lastEvent.getNewValue(), ctx);
		assertSame(l2.lastEvent.getSource(), peer);

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.setBeanContext(ctx);
		assertSame(ctx, support.getBeanContext());
		assertNull(support.lastInitBeanContext);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		support.setBeanContext(null);
		assertNull(support.getBeanContext());
		assertNull(support.lastInitBeanContext);
		assertSame(ctx, support.lastReleaseBeanContext);
		assertEquals(l1.lastEvent.getPropertyName(), "beanContext");
		assertNull(l1.lastEvent.getNewValue());
		assertSame(l1.lastEvent.getOldValue(), ctx);
		assertSame(l1.lastEvent.getSource(), peer);
		assertEquals(l2.lastEvent.getPropertyName(), "beanContext");
		assertNull(l2.lastEvent.getNewValue());
		assertSame(l2.lastEvent.getOldValue(), ctx);
		assertSame(l2.lastEvent.getSource(), peer);
	}

	public void testSetBeanContext_VetoedByListener()
			throws PropertyVetoException {
		MockBeanContextChildSupport support = new MockBeanContextChildSupport();
		MockBeanContext oldCtx = new MockBeanContext();
		support.setBeanContext(oldCtx);
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockVetoChangeListener l2 = new MockVetoChangeListener();
		support.addPropertyChangeListener("beanContext", l1);
		support.addVetoableChangeListener("beanContext", l2);

		MockBeanContext ctx = new MockBeanContext();

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		try {
			support.setBeanContext(ctx);
			fail();
		} catch (PropertyVetoException e) {
			// expected
		}
		assertSame(oldCtx, support.getBeanContext());
		assertNull(support.lastInitBeanContext);
		assertNull(support.lastReleaseBeanContext);
		assertNull(l1.lastEvent);
		assertEquals(l2.lastEvent.getPropertyName(), "beanContext");
		assertSame(l2.lastEvent.getNewValue(), oldCtx);
		assertSame(l2.lastEvent.getOldValue(), ctx);
		assertSame(l2.lastEvent.getSource(), support);
		assertTrue(support.rejectedSetBCOnce());
	}

	public void testSetBeanContext_VetoedByValidateMethod() {
		MockBeanContextChildSupport support = new MockBeanContextChildSupport();
		support.vetoBeanContext = true;
		MockPropertyChangeListener l1 = new MockPropertyChangeListener();
		MockVetoableChangeListener l2 = new MockVetoableChangeListener();
		support.addPropertyChangeListener("beanContext", l1);
		support.addVetoableChangeListener("beanContext", l2);

		MockBeanContext ctx = new MockBeanContext();
		assertNull(support.getBeanContext());

		support.clearLastRecords();
		l1.clearLastEvent();
		l2.clearLastEvent();
		try {
			support.setBeanContext(ctx);
			fail();
		} catch (PropertyVetoException e) {
			// expected
		}
		assertNull(support.getBeanContext());
		assertNull(support.lastInitBeanContext);
		assertNull(l1.lastEvent);
		assertNull(l2.lastEvent);
		assertTrue(support.rejectedSetBCOnce());
	}

	public void testValidatePendingSetBeanContext() {
		// guess the impl always returns true
		BeanContextChildSupport support = new MockBeanContextChildSupport();
		assertTrue(support.validatePendingSetBeanContext(new MockBeanContext()));
		assertTrue(support.validatePendingSetBeanContext(null));
	}

	public void testSerialization_NoPeer() throws IOException,
			ClassNotFoundException {
		BeanContextChildSupport support = new BeanContextChildSupport();
		MockPropertyChangeListener pcl1 = new MockPropertyChangeListener();
		MockPropertyChangeListenerS pcl2 = new MockPropertyChangeListenerS(
				"id of pcl2");
		MockVetoableChangeListener vcl1 = new MockVetoableChangeListener();
		MockVetoableChangeListenerS vcl2 = new MockVetoableChangeListenerS(
				"id of vcl2");
		support.addPropertyChangeListener("beanContext", pcl1);
		support.addPropertyChangeListener("beanContext", pcl2);
		support.addVetoableChangeListener("beanContext", vcl1);
		support.addVetoableChangeListener("beanContext", vcl2);

		assertEqualsSerially(support,
				(BeanContextChildSupport) SerializationTester
						.getDeserilizedObject(support));
	}

	public void testSerialization_WithNonSerializablePeer() throws IOException,
			ClassNotFoundException {
		MockBeanContextChild peer = new MockBeanContextChild();
		BeanContextChildSupport support = new BeanContextChildSupport(peer);
		MockPropertyChangeListener pcl1 = new MockPropertyChangeListener();
		MockPropertyChangeListenerS pcl2 = new MockPropertyChangeListenerS(
				"id of pcl2");
		MockVetoableChangeListener vcl1 = new MockVetoableChangeListener();
		MockVetoableChangeListenerS vcl2 = new MockVetoableChangeListenerS(
				"id of vcl2");
		support.addPropertyChangeListener("beanContext", pcl1);
		support.addPropertyChangeListener("beanContext", pcl2);
		support.addVetoableChangeListener("beanContext", vcl1);
		support.addVetoableChangeListener("beanContext", vcl2);

		try {
			SerializationTester.getDeserilizedObject(support);
			fail();
		} catch (IOException e) {
			// expected
		}
	}

	public void testSerialization_WithPeer() throws IOException,
			ClassNotFoundException {
		MockBeanContextChildDelegateS peer = new MockBeanContextChildDelegateS(
				"id of peer");
		BeanContextChildSupport support = peer.support;
		MockPropertyChangeListener pcl1 = new MockPropertyChangeListener();
		MockPropertyChangeListenerS pcl2 = new MockPropertyChangeListenerS(
				"id of pcl2");
		MockVetoableChangeListener vcl1 = new MockVetoableChangeListener();
		MockVetoableChangeListenerS vcl2 = new MockVetoableChangeListenerS(
				"id of vcl2");
		support.addPropertyChangeListener("beanContext", pcl1);
		support.addPropertyChangeListener("beanContext", pcl2);
		support.addVetoableChangeListener("beanContext", vcl1);
		support.addVetoableChangeListener("beanContext", vcl2);

		assertEqualsSerially(support,
				(BeanContextChildSupport) SerializationTester
						.getDeserilizedObject(support));
	}

	public void testSerialization_Compatibility() throws IOException,
			ClassNotFoundException, Exception {
		MockBeanContextChildDelegateS peer = new MockBeanContextChildDelegateS(
				"id of peer");
		BeanContextChildSupport support = peer.support;
		MockPropertyChangeListener pcl1 = new MockPropertyChangeListener();
		MockPropertyChangeListenerS pcl2 = new MockPropertyChangeListenerS(
				"id of pcl2");
		MockVetoableChangeListener vcl1 = new MockVetoableChangeListener();
		MockVetoableChangeListenerS vcl2 = new MockVetoableChangeListenerS(
				"id of vcl2");
		support.addPropertyChangeListener("beanContext", pcl1);
		support.addPropertyChangeListener("beanContext", pcl2);
		support.addVetoableChangeListener("beanContext", vcl1);
		support.addVetoableChangeListener("beanContext", vcl2);

		assertEqualsSerially(
				support,
				(BeanContextChildSupport) SerializationTester
						.readObject(support,
								"serialization/java/beans/beancontext/BeanContextChildSupport.ser"));
	}

	public static void assertEqualsSerially(BeanContextChildSupport orig,
			BeanContextChildSupport ser) {
		// check peer
		if (orig == orig.getBeanContextChildPeer()) {
			assertSame(ser, ser.getBeanContextChildPeer());
		} else {
			assertSame(orig.getBeanContextChildPeer().getClass(), ser
					.getBeanContextChildPeer().getClass());
			if (orig.getBeanContextChildPeer() instanceof MockBeanContextChildDelegateS) {
				MockBeanContextChildDelegateS origPeer = (MockBeanContextChildDelegateS) orig
						.getBeanContextChildPeer();
				MockBeanContextChildDelegateS serPeer = (MockBeanContextChildDelegateS) ser
						.getBeanContextChildPeer();
				assertEquals(origPeer.id, serPeer.id);
			}
		}

		// check property change listeners
		PropertyChangeSupport origPCS = (PropertyChangeSupport) Utils.getField(
				orig, "pcSupport");
		PropertyChangeSupport serPCS = (PropertyChangeSupport) Utils.getField(
				ser, "pcSupport");
		PropertyChangeListener origPCL[] = origPCS
				.getPropertyChangeListeners("beanContext");
		PropertyChangeListener serPCL[] = serPCS
				.getPropertyChangeListeners("beanContext");
		int i = 0, j = 0;
		while (i < origPCL.length) {
			if (origPCL[i] instanceof Serializable) {
				assertSame(origPCL[i].getClass(), serPCL[j].getClass());
				if (origPCL[i] instanceof MockPropertyChangeListenerS) {
					assertEquals(((MockPropertyChangeListenerS) origPCL[i]).id,
							((MockPropertyChangeListenerS) serPCL[j]).id);
				}
				i++;
				j++;
			} else {
				i++;
			}
		}
		assertEquals(serPCL.length, j);

		// check vetoable change listeners
		VetoableChangeSupport origVCS = (VetoableChangeSupport) Utils.getField(
				orig, "vcSupport");
		VetoableChangeSupport serVCS = (VetoableChangeSupport) Utils.getField(
				ser, "vcSupport");
		VetoableChangeListener origVCL[] = origVCS
				.getVetoableChangeListeners("beanContext");
		VetoableChangeListener serVCL[] = serVCS
				.getVetoableChangeListeners("beanContext");
		i = 0;
		j = 0;
		while (i < origVCL.length) {
			if (origVCL[i] instanceof Serializable) {
				assertSame(origVCL[i].getClass(), serVCL[j].getClass());
				if (origVCL[i] instanceof MockVetoableChangeListenerS) {
					assertEquals(((MockVetoableChangeListenerS) origVCL[i]).id,
							((MockVetoableChangeListenerS) serVCL[j]).id);
				}
				i++;
				j++;
			} else {
				i++;
			}
		}
		assertEquals(serVCL.length, j);
	}
    
    
    
    /** TEST CONSTRUCTORS * */

    /**
     * * Test constructor with BeanContextChild parameter.
     * <p>
     * 
     * @see BeanContextChildSupport#BeanContextChildSupport(BeanContextChild)
     */
    public void testConstructorBeanContextChild() throws Exception {
        new BeanContextChildSupport(null);
    }

    /**
     * * Test constructor with no parameters.
     * <p>
     * 
     * @see BeanContextChildSupport#BeanContextChildSupport()
     */
    public void testConstructor() throws Exception {
        new BeanContextChildSupport();
    }

    /** TEST METHODS * */

    /**
     * Test method setBeanContext() with BeanContext parameter.
     * <p>
     */
    public void testSetBeanContextBeanContext() throws Exception {
        BeanContextChildSupport sup = new BeanContextChildSupport();
        sup.setBeanContext(new BeanContextSupport());

        assertNotNull("BeanContext should not be null", sup.getBeanContext());
    }
    

    /** UTILITY METHODS * */

    /** STANDARD ENDING * */

    /**
     * Start testing from the command line.
     * <p>
     */
    public static Test suite() {
        return new TestSuite(BeanContextChildSupportTest.class);
    }
}
