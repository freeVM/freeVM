/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.pack200;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Provides generic JavaBeans support for the Pack/UnpackAdapters
 */
public abstract class Pack200Adapter {

	protected static final int DEFAULT_BUFFER_SIZE = 8192;

	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	private SortedMap<String, String> properties = new TreeMap<String, String>();

	public SortedMap<String, String> properties() {
		return properties;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		support.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/** 
	 * Completion between 0..1
	 * @param value
	 */
	protected void completed(double value) {
		firePropertyChange("pack.progress", null, String.valueOf((int)(100*value))); //$NON-NLS-1$
	}
}
