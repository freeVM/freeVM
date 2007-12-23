/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tools.appletviewer;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;

class AppletFrame extends JFrame {
	private final AppletInfo appletInfo;
	private final Applet applet;
	
	public AppletFrame(AppletInfo appletInfo) throws Exception {
		this.appletInfo = appletInfo;
		
		// Load applet class
		URL []urls = new URL[1];
		urls[0] = appletInfo.getCodeBase();
		URLClassLoader cl = new URLClassLoader(urls);
		Class clz = cl.loadClass(this.appletInfo.getCode());
		applet = (Applet)clz.newInstance();
		applet.setStub(new ViewerAppletStub(applet, appletInfo));
		
		// Create applet pane
		setLayout(new BorderLayout());
		applet.setPreferredSize(new Dimension(appletInfo.getWidth(), appletInfo.getHeight()));
		add(applet, BorderLayout.CENTER);

		// Start applet
		applet.init();
		applet.start();

		// Make frame visible
		pack();
		setVisible(true);		
	}
}
