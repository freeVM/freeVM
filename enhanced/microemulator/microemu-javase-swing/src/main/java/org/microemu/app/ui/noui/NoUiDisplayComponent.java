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

package org.microemu.app.ui.noui;

import java.awt.Graphics;

import javax.microedition.lcdui.Displayable;

import org.microemu.DisplayAccess;
import org.microemu.DisplayComponent;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.app.ui.DisplayRepaintListener;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.device.MutableImage;
import org.microemu.device.j2se.J2SEDeviceDisplay;
import org.microemu.device.j2se.J2SEMutableImage;

public class NoUiDisplayComponent implements DisplayComponent {

	private J2SEMutableImage displayImage = null;
	private DisplayRepaintListener displayRepaintListener;
	
	public void addDisplayRepaintListener(DisplayRepaintListener l) {
		displayRepaintListener = l;
	}

	public void removeDisplayRepaintListener(DisplayRepaintListener l) {
		if (displayRepaintListener == l) {
			displayRepaintListener = null;
		}
	}

	public MutableImage getDisplayImage() {
		return displayImage;
	}

	public void repaintRequest(int x, int y, int width, int height) 
	{
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return;
		}
		DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return;
		}
		Displayable current = da.getCurrent();
		if (current == null) {
			return;
		}

		Device device = DeviceFactory.getDevice();

		if (device != null) {
			if (displayImage == null) {
				displayImage = new J2SEMutableImage(
						device.getDeviceDisplay().getFullWidth(), device.getDeviceDisplay().getFullHeight());
			}
					
			Graphics gc = displayImage.getImage().getGraphics();

			J2SEDeviceDisplay deviceDisplay = (J2SEDeviceDisplay) device.getDeviceDisplay();				
			if (!deviceDisplay.isFullScreenMode()) {
				deviceDisplay.paintControls(gc);
			}
			deviceDisplay.paintDisplayable(gc, x, y, width, height);

			fireDisplayRepaint(displayImage);
		}	
	}


	private void fireDisplayRepaint(MutableImage image)
	{
		if (displayRepaintListener != null) {
			displayRepaintListener.repaintInvoked(image);
		}
	}

}
