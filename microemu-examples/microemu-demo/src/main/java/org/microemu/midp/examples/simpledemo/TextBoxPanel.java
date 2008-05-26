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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class TextBoxPanel extends BaseExamplesList {

	TextBox textBoxes[] = { new TextBox("Any character", null, 128, TextField.ANY),
			new TextBox("Email", null, 128, TextField.EMAILADDR),
			new TextBox("Numeric", null, 128, TextField.NUMERIC),
			new TextBox("Phone number", null, 128, TextField.PHONENUMBER),
			new TextBox("URL", null, 128, TextField.URL),
			new TextBox("Decimal", null, 128, TextField.DECIMAL),
			new TextBox("Password", null, 128, TextField.PASSWORD), };

	Command okCommand = new Command("Ok", Command.OK, 2);

	public TextBoxPanel() {
		super("TextBox", List.IMPLICIT);

		for (int i = 0; i < textBoxes.length; i++) {
			textBoxes[i].addCommand(BaseExamplesForm.backCommand);
			textBoxes[i].addCommand(okCommand);
			textBoxes[i].setCommandListener(this);
			append(textBoxes[i].getTitle(), null);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == List.SELECT_COMMAND) {
				SimpleDemoMIDlet.setCurrentDisplayable(textBoxes[getSelectedIndex()]);
			}
		} else if ((c == BaseExamplesForm.backCommand) || (c == okCommand)) {
			for (int i = 0; i < textBoxes.length; i++) {
				if (d == textBoxes[i]) {
					SimpleDemoMIDlet.setCurrentDisplayable(this);
				}
			}
		}
		super.commandAction(c, d);
	}

}
