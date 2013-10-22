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

package org.microemu.android.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.microedition.android.lcdui.Alert;
import javax.microedition.android.lcdui.Canvas;
import javax.microedition.android.lcdui.Form;
import javax.microedition.android.lcdui.Image;
import javax.microedition.android.lcdui.List;
import javax.microedition.android.lcdui.TextBox;

import org.microemu.EmulatorContext;
import org.microemu.android.MicroEmulatorActivity;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.android.device.ui.AndroidListUI;
import org.microemu.android.device.ui.AndroidTextBoxUI;
import org.microemu.device.Device;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.device.ui.CanvasUI;
import org.microemu.device.ui.DisplayableUI;
import org.microemu.device.ui.ListUI;
import org.microemu.device.ui.TextBoxUI;
import org.microemu.device.ui.UIFactory;

public class AndroidDevice implements Device {

	private EmulatorContext emulatorContext;
	
	private MicroEmulatorActivity activity;
	
	private UIFactory ui = new UIFactory() {

		public DisplayableUI createAlertUI(Alert alert) {
			// TODO Not yet implemented
			return new AndroidCanvasUI(activity, null);
		}

		public CanvasUI createCanvasUI(Canvas canvas) {
			return new AndroidCanvasUI(activity, canvas);
		}

		public DisplayableUI createFormUI(Form form) {
			// TODO Not yet implemented
			return new AndroidCanvasUI(activity, null);
		}

		public ListUI createListUI(List list) {
			return new AndroidListUI(activity, list);
		}
		
		public TextBoxUI createTextBoxUI(TextBox textBox) {
			return new AndroidTextBoxUI(activity, textBox);
		}

	};
	
	private Map systemProperties = new HashMap();
	
	private Vector softButtons = new Vector();
	
	public AndroidDevice(EmulatorContext emulatorContext, MicroEmulatorActivity activity) {
		this.emulatorContext = emulatorContext;
		this.activity = activity;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public Vector getButtons() {
		// TODO Auto-generated method stub
		return null;
	}

	public DeviceDisplay getDeviceDisplay() {
		return emulatorContext.getDeviceDisplay();
	}

	public FontManager getFontManager() {
		return emulatorContext.getDeviceFontManager();
	}

	public InputMethod getInputMethod() {
		return emulatorContext.getDeviceInputMethod();
	}
	
	public UIFactory getUIFactory() {
		return ui;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getNormalImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getOverImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getPressedImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getSoftButtons() {
		return softButtons;
	}

	public Map getSystemProperties() {
		return systemProperties;
	}

	public boolean hasPointerEvents() {
		return true;
	}

	public boolean hasPointerMotionEvents() {
		return true;
	}

	public boolean hasRepeatEvents() {
		return true;
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	public boolean vibrate(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
