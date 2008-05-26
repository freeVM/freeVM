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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

public abstract class BaseTestsCanvas extends Canvas implements CommandListener, DisplayableUnderTests {

	protected boolean fullScreenMode = false;
	
	protected static final Command fullScreenModeCommand = new Command("Full Screen", Command.ITEM, 5);
	
	public BaseTestsCanvas(String title) {
		super();
		super.setTitle(title);
		
		addCommand(DisplayableUnderTests.backCommand);
		addCommand(fullScreenModeCommand);
		setCommandListener(this);
	}

	public int writeln(Graphics g, int line, String s) {
		int y = (g.getFont().getHeight() + 1) * line;
		g.drawString(s, 0, y, Graphics.LEFT | Graphics.TOP);
		return y;
	}
	
	public void commandAction(Command c, Displayable d) {
		if (d == this) {
			if (c == DisplayableUnderTests.backCommand) {
				Manager.midletInstance.showMainPage();
			} else if (c == fullScreenModeCommand) {
				setFullScreenMode(!fullScreenMode);
				repaint();
			}
		}
	}
	
	public void setFullScreenMode(boolean mode) {
		fullScreenMode = mode;
		super.setFullScreenMode(mode);
	}
}
