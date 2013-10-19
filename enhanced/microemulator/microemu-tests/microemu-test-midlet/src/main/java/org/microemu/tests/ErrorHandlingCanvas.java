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

import javax.microedition.lcdui.Graphics;

public class ErrorHandlingCanvas extends BaseTestsCanvas {
	
	boolean makeErrorInPaint = false;
	
	int lastKeyCode = 0;
	
	public ErrorHandlingCanvas() {
		super("Canvas with Errors");
	}

	protected void paint(Graphics g) {
		int width = getWidth();
        int height = getHeight();

		g.setGrayScale(255);
		g.fillRect(0, 0, width, height);
		
		g.setColor(0);
		int line = 0;
		writeln(g, line++, "Make Error Canvas");
		if (fullScreenMode) {
			writeln(g, line++, "0 - Normal Mode");
		}
		writeln(g, line++, "1 - Error in keyPressed");
		writeln(g, line++, "2 - Error in pain");
		if (makeErrorInPaint) {
			makeErrorInPaint = false;
			writeln(g, line++, "Making error");
			throw new IllegalArgumentException("Emulator Should still work");
		}
		if (lastKeyCode != 0) {
			writeln(g, line++, "KeyCode: " + lastKeyCode);
		}
		
	}
	
	protected void keyPressed(int keyCode) {
		switch (keyCode) {
		case '0':
			if (fullScreenMode) {
				setFullScreenMode(false);
			}
			break;
		case '1':
			throw new IllegalArgumentException("Emulator Should still work");
		case '2':
			makeErrorInPaint = true;
			break;
		}
		lastKeyCode = keyCode;
		repaint();
	}
}