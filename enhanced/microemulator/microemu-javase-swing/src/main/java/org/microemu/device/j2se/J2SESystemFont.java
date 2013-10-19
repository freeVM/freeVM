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

package org.microemu.device.j2se;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class J2SESystemFont implements J2SEFont {
	
	private final static Graphics2D graphics = (Graphics2D) new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB).getGraphics();

	private String name;
	
	private String style;
	
	private int size;
	
	private boolean antialiasing;
	
	private boolean initialized;
	
	private FontMetrics fontMetrics;

	public J2SESystemFont(String name, String style, int size, boolean antialiasing) {
		this.name = name;
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
	
	public int charWidth(char ch) {
		checkInitialized();

		return fontMetrics.charWidth(ch);
	}

	public int charsWidth(char[] ch, int offset, int length) {
		checkInitialized();

		return fontMetrics.charsWidth(ch, offset, length);
	}

	public int getBaselinePosition() {
		checkInitialized();

		return fontMetrics.getAscent();
	}

	public int getHeight() {
		checkInitialized();

		return fontMetrics.getHeight();
	}

	public int stringWidth(String str) {
		checkInitialized();

		return fontMetrics.stringWidth(str);
	}

	public Font getFont() {
		checkInitialized();

		return fontMetrics.getFont();
	}
	
	private synchronized void checkInitialized() {
		if (!initialized) {
			int awtStyle = 0;
			if (style.indexOf("plain") != -1) {
				awtStyle |= Font.PLAIN;
			}
			if (style.indexOf("bold") != -1) {
				awtStyle |= Font.BOLD;
			}
			if (style.indexOf("italic") != -1) {
				awtStyle |= Font.ITALIC;
			}
			if (style.indexOf("underlined") != -1) {
				// TODO underlined style not implemented
			}
			if (antialiasing) {
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			} else {
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
			fontMetrics = graphics.getFontMetrics(new Font(name, awtStyle, size));
			initialized = true;
		}
	}

}
