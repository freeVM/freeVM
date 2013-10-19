/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.tests;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDletStateChangeException;

import junit.framework.TestCase;

import cldcunit.runner.TestRunner;

public class UnitTestsMIDLet extends TestRunner implements MIDletUnderTests {

	protected void startApp() throws MIDletStateChangeException {
		Manager.midletInstance = this;
		try {
			start(new TestCase[] { new ItemsOnFormTest() });
		} catch (Exception e) {
			System.out.println("Exception while setting up tests: " + e);
			e.printStackTrace();
		}
	}
	
	public void showMainPage() {
		
	}
	
	public void setCurrentDisplayable(Displayable nextDisplayable) {
		Display display = Display.getDisplay(this);
		display.setCurrent(nextDisplayable);
	}

}
