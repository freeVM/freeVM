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

package org.apache.harmony.beans.tests.java.beans.mock;

import java.applet.Applet;
import java.io.Serializable;

/**
 * test java.beans.Beans
 */
public class MockAppletBean2 extends Applet implements Serializable {
	static final long serialVersionUID = 1L;

	private String propertyOne;

	private boolean initBeenCalled;

	public void init() {
		super.init();
		this.initBeenCalled = true;
	}

	public boolean initHasBeenCalled() {
		return this.initBeenCalled;
	}

	public void setInitHasBeenCalled(boolean value) {
		this.initBeenCalled = value;
	}

	/**
	 * @return Returns the propertyOne.
	 */
	public String getPropertyOne() {
		return propertyOne;
	}

	/**
	 * @param propertyOne
	 *            The propertyOne to set.
	 */
	public void setPropertyOne(String propertyOne) {
		this.propertyOne = propertyOne;
	}
}
