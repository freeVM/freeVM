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
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

class ViewerAppletContext implements AppletContext {
	public ViewerAppletContext() {
	}

	public Applet getApplet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<Applet> getApplets() {
		// TODO Auto-generated method stub
		return null;
	}

	public AudioClip getAudioClip(URL url) {
		return new ViewerAudioClip(url);
	}

	public Image getImage(URL url) {
		return Toolkit.getDefaultToolkit().createImage(url);
	}

	public InputStream getStream(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<String> getStreamKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setStream(String key, InputStream stream) throws IOException {
		// TODO Auto-generated method stub

	}

	public void showDocument(URL url) {
		// TODO Auto-generated method stub

	}

	public void showDocument(URL url, String target) {
		// TODO Auto-generated method stub

	}

	public void showStatus(String status) {
		// TODO Auto-generated method stub

	}

}
