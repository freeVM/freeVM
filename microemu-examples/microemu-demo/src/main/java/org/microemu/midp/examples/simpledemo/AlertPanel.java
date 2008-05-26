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

package org.microemu.midp.examples.simpledemo;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

public class AlertPanel extends BaseExamplesList {

	static final Command okCommand = new Command("Agree", Command.OK, 1);
	
	Alert alertCmd;
	
	Alert alerts[] = {
			new Alert("Alarm alert", "This is alarm alert", null, AlertType.ALARM),
			new Alert("Confirmation alert", "This is confirmation alert with 5 sec timeout", null, AlertType.CONFIRMATION),
			new Alert("Error alert", "This is error alert", null, AlertType.ERROR),
			new Alert("Info alert", "This is info alert with 5 sec timeout", null, AlertType.INFO),
			new Alert("Warning alert", "This is warning alert", null, AlertType.WARNING),
			alertCmd = new Alert("Command alert", "This is alert with command", null, AlertType.INFO)};

	public AlertPanel() {
		super("Alert", List.IMPLICIT);

		for (int i = 0; i < alerts.length; i++) {
			if (i == 1 || i == 3) {
				alerts[i].setTimeout(5000);
			}
			append(alerts[i].getTitle(), null);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == List.SELECT_COMMAND) {
				if (alertCmd == alerts[getSelectedIndex()]) {
					alertCmd.addCommand(okCommand);
				}
				SimpleDemoMIDlet.setCurrentDisplayable(alerts[getSelectedIndex()]);
			}
		}
		super.commandAction(c, d);
	}

}
