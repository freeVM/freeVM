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

/**
 * @author vlads
 *  To test if MIDlet can override javax.microedition package on the device.
 */
public class OverrideNewJSRCanvas extends BaseTestsCanvas {

	public static final boolean enabled = true;
	
	public OverrideNewJSRCanvas() {
		super("OverrideNew IO JSR");
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
	 */
	protected void paint(Graphics g) {
		int width = getWidth();
        int height = getHeight();

		g.setGrayScale(255);
		g.fillRect(0, 0, width, height);
		
		g.setColor(0);
		int line = 0;
		writeln(g, line++, "Override New JSR");
		
		String result;
		
		try {
			result = new OverrideNewJSRClient().doJSRStuff("Can use new classes");
			writeln(g, line++, "success");
		} catch (Throwable e) {
			writeln(g, line++, "failure");
			result = e.toString();
		}
		
		writeln(g, line++, result);
	}

}
