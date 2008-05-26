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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

public class ItemsOnForm extends Form implements CommandListener, DisplayableUnderTests {

	public static final Command addCommand = new Command("add", Command.ITEM, 1);
	
	public ItemsOnForm(Item[] items) {
		super("Form with Items", items);
		addCommand(addCommand);
		addCommand(DisplayableUnderTests.backCommand);
		setCommandListener(this);
    }
	
	public ItemsOnForm() {
		this(null);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == DisplayableUnderTests.backCommand) {
				Manager.midletInstance.showMainPage();
			} if (c == addCommand) {
				append(new StringItem("si:", "StringItem" + Manager.sequenceNext()));
			}
		}
	}

}
