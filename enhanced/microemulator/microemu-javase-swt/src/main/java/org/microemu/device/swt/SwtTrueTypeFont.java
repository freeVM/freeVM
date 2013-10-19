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

package org.microemu.device.swt;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

public class SwtTrueTypeFont implements SwtFont {

	private URL url;
	
	private String style;
	
	private int size;
	
	private boolean antialiasing;
	
	private boolean initialized;
	
	private Font font;

	public SwtTrueTypeFont(URL url, String style, int size, boolean antialiasing) {
		this.url = url;
		this.style = style.toLowerCase();
		this.size = size;
		this.antialiasing = antialiasing;
		
		this.initialized = false;
	}

	public void setAntialiasing(boolean antialiasing) {
		if (this.antialiasing != antialiasing) {
			this.antialiasing = antialiasing;
			initialized = false;
		}
	}

	public Font getFont() {
		checkInitialized();

		return font;
	}

	private synchronized void checkInitialized() {
		if (!initialized) {
			int swtStyle = 0;
			if (style.indexOf("plain") != -1) {
				swtStyle |= SWT.NORMAL;
			}
			if (style.indexOf("bold") != -1) {
				swtStyle |= SWT.BOLD;
			}
			if (style.indexOf("italic") != -1) {
				swtStyle |= SWT.ITALIC;
			}
			if (style.indexOf("underlined") != -1) {
				// TODO underlined style not implemented
			}

//			font = SwtDeviceComponent.getFont(name, size, swtStyle, antialiasing);
			initialized = true;
			try {
				throw new RuntimeException("not implemented");
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw ex;
			}
		}
	}

	public int charWidth(char ch) {
		try {
			throw new RuntimeException("not implemented");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public int charsWidth(char[] ch, int offset, int length) {
		try {
			throw new RuntimeException("not implemented");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public int getBaselinePosition() {
		try {
			throw new RuntimeException("not implemented");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public int getHeight() {
		try {
			throw new RuntimeException("not implemented");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public int stringWidth(String str) {
		try {
			throw new RuntimeException("not implemented");
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
