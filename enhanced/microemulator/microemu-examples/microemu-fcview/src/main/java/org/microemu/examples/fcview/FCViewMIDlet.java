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

package org.microemu.examples.fcview;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * @author vlads
 *
 */
public class FCViewMIDlet extends MIDlet {

	static FCViewMIDlet instance;

	FilesList list;
	
	public FCViewMIDlet() {
		super();
		instance = this;
		this.list = new FilesList();
	}

	
	protected void startApp() throws MIDletStateChangeException {
		try {
			System.out.println("FileConnection " + System.getProperty("microedition.io.file.FileConnection.version"));
			this.list.setDir(null);
			setCurrentDisplayable(this.list);
		} catch (SecurityException e) {
			Alert alert = new Alert("Error",  "Unable to access the restricted API", null, AlertType.ERROR);
	        alert.setTimeout(Alert.FOREVER);
	        setCurrentDisplayable(alert);
		}
	}


	protected void destroyApp(boolean unconditional) {
		
	}

	protected void pauseApp() {
		
	}

	public static void setCurrentDisplayable(Displayable nextDisplayable) {
		Display display = Display.getDisplay(instance);
		display.setCurrent(nextDisplayable);
	}


	public static void exit() {
		instance.destroyApp(true);
		instance.notifyDestroyed();
	}

}
