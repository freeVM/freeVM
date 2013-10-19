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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class MainTestMIDlet extends MIDlet implements CommandListener, MIDletUnderTests {

	// static final Command exitCommand = MIDletUnderTests.exitCommand;

	List menuList = null;

	Vector testPanels;

	static {
		System.out.println("MainTestMIDlet static init");
		ThreadTestsForm.onMIDletInit();
	}

	public MainTestMIDlet() {

	}

	protected void startApp() throws MIDletStateChangeException {
		Manager.midletInstance = this;

		if (menuList == null) {
			testPanels = new Vector();
			testPanels.addElement(new ItemsOnForm());
			testPanels.addElement(new ErrorHandlingForm());
			testPanels.addElement(new ErrorHandlingCanvas());
			testPanels.addElement(new RecordStoreForm());
			testPanels.addElement(new ThreadTestsForm());
			if (OverrideNewJSRCanvas.enabled) {
				testPanels.addElement(new OverrideNewJSRCanvas());
			}
			if (OverrideNewJSR2Canvas.enabled) {
				testPanels.addElement(new OverrideNewJSR2Canvas());
			}
			if (OverrideNewJSR2Canvas.enabled) {
				testPanels.addElement(new OverrideNewJSR2Canvas());
			}
			if (PreporcessorTestCanvas.enabled) {
				testPanels.addElement(new PreporcessorTestCanvas());
			}

			menuList = new List("Manual Tests", List.IMPLICIT);

			for (Enumeration iter = testPanels.elements(); iter.hasMoreElements();) {
				menuList.append(((Displayable) iter.nextElement()).getTitle(), null);
			}
			menuList.addCommand(exitCommand);
			menuList.setCommandListener(this);
		}
		setCurrentDisplayable(menuList);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == menuList) {
			if (c == List.SELECT_COMMAND) {
				setCurrentDisplayable((Displayable) testPanels.elementAt(menuList.getSelectedIndex()));
			} else if (c == exitCommand) {
				try {
					destroyApp(true);
				} catch (MIDletStateChangeException e) {
				}
				notifyDestroyed();
			}
		}
	}

	public void showMainPage() {
		setCurrentDisplayable(menuList);
	}

	public void setCurrentDisplayable(Displayable nextDisplayable) {
		Display display = Display.getDisplay(this);
		// Displayable current = display.getCurrent();
		display.setCurrent(nextDisplayable);
	}

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

}
