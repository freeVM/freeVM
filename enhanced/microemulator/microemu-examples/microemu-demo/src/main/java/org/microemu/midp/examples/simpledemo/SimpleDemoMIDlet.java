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
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;

public class SimpleDemoMIDlet extends MIDlet implements CommandListener {

	static SimpleDemoMIDlet instance;

	static final Command exitCommand = new Command("Exit", Command.EXIT, 1);

	List menuList = null;

	Displayable screenPanels[];

	public SimpleDemoMIDlet() {
		instance = this;
	}

	public void destroyApp(boolean unconditional) {
		if (screenPanels != null) {
			for (int i = 0; i < screenPanels.length; i++) {
				if (screenPanels[i] instanceof HasRunnable) {
					((HasRunnable) screenPanels[i]).stopRunnable();
				}
			}
		}
	}

	public void pauseApp() {
	}

	public void startApp() {
		if (menuList == null) {
			screenPanels = new Displayable[] {
					new AlertPanel(),
					new CanvasPanel(),
					new GameCanvasPanel(),
					new KeyCanvasPanel(),
					new PointerCanvasPanel(),
					new DateFieldPanel(),
					new GaugePanel(),
					new ImageItemPanel(),
					new ListPanel(),
					new TextFieldPanel(),
					new TextBoxPanel(),
					new HTTPPanel()};

			Ticker ticker = new Ticker("This is SimpleDemo ticker");

			menuList = new List("SimpleDemo", List.IMPLICIT);

			for (int i = 0; i < screenPanels.length; i++) {
				menuList.append(screenPanels[i].getTitle(), null);
				if ((screenPanels[i] instanceof Screen) && (i < 4)) {
					((Screen)screenPanels[i]).setTicker(ticker);
				}
			}
			menuList.addCommand(exitCommand);
			menuList.setCommandListener(this);
		}

		showMenu();
	}

	public static SimpleDemoMIDlet getInstance() {
		return instance;
	}

	public static void showMenu() {
		setCurrentDisplayable(instance.menuList);
	}
	
	public static void setCurrentDisplayable(Displayable nextDisplayable) {
		Display display = Display.getDisplay(instance);
		Displayable current = display.getCurrent();
		if (current instanceof HasRunnable) {
			((HasRunnable) current).stopRunnable();
		}
		if (nextDisplayable instanceof HasRunnable) {
			((HasRunnable) nextDisplayable).startRunnable();
		}
		display.setCurrent(nextDisplayable);
	}
	
	public void commandAction(Command c, Displayable d) {
		if (d == menuList) {
			if (c == List.SELECT_COMMAND) {
				setCurrentDisplayable(screenPanels[menuList.getSelectedIndex()]);
			} else if (c == exitCommand) {
				destroyApp(true);
				notifyDestroyed();
			}
		}
	}

}
