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

package org.microemu.device.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

import org.microemu.device.DeviceDisplay;

public interface DeviceDisplayImpl extends DeviceDisplay {

	Image createSystemImage(URL url) throws IOException;

	/**
	 * @param name
	 * @param shape
	 * @param keyCode -
	 *            Integer.MIN_VALUE when unspecified
	 * @param keyboardKeys
	 * @param keyboardChars
	 * @param chars
	 * @param modeChange
	 * @return
	 */
	Button createButton(int skinVersion, String name, Shape shape, int keyCode, String keyboardKeys,
			String keyboardChars, Hashtable inputToChars, boolean modeChange);

	/**
	 * @param name
	 * @param rectangle
	 * @param keyCode -
	 *            Integer.MIN_VALUE when unspecified
	 * @param keyName
	 * @param paintable
	 * @param alignmentName
	 * @param commands
	 * @param font
	 * @return
	 */
	SoftButton createSoftButton(int skinVersion, String name, Shape shape, int keyCode, String keyName,
			Rectangle paintable, String alignmentName, Vector commands, Font font);

	SoftButton createSoftButton(int skinVersion, String name, Rectangle paintable, Image normalImage, Image pressedImage);

	/**
	 * @param i
	 */
	void setNumColors(int i);

	/**
	 * @param b
	 */
	void setIsColor(boolean b);

	void setNumAlphaLevels(int i);

	/**
	 * @param color
	 */
	void setBackgroundColor(Color color);

	/**
	 * @param color
	 */
	void setForegroundColor(Color color);

	/**
	 * @param rectangle
	 */
	void setDisplayRectangle(Rectangle rectangle);

	/**
	 * @param rectangle
	 */
	void setDisplayPaintable(Rectangle rectangle);

	/**
	 * @param object
	 */
	void setMode123Image(PositionedImage object);

	/**
	 * @param object
	 */
	void setModeAbcLowerImage(PositionedImage object);

	/**
	 * @param object
	 */
	void setModeAbcUpperImage(PositionedImage object);

	boolean isResizable();

	void setResizable(boolean state);

}
