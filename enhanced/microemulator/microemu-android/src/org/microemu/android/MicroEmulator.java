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

package org.microemu.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.android.lcdui.Command;
import javax.microedition.android.lcdui.CommandListener;

import org.microemu.DisplayAccess;
import org.microemu.DisplayComponent;
import org.microemu.EmulatorContext;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.AndroidDevice;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.android.device.AndroidFontManager;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.ui.AndroidDisplayableUI;
import org.microemu.android.util.AndroidLoggerAppender;
import org.microemu.android.util.AndroidRecordStoreManager;
import org.microemu.app.Common;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.log.Logger;

import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MicroEmulator extends MicroEmulatorActivity {
	
	public static final String LOG_TAG = "MicroEmulator";
		
	protected Common common;

	protected EmulatorContext emulatorContext = new EmulatorContext() {

		private InputMethod inputMethod = new AndroidInputMethod();

		private DeviceDisplay deviceDisplay = new AndroidDeviceDisplay(this);
		
		private FontManager fontManager = new AndroidFontManager();

		public DisplayComponent getDisplayComponent() {
			// TODO consider removal of EmulatorContext.getDisplayComponent()
			System.out.println("MicroEmulator.emulatorContext::getDisplayComponent()");
			return null;
		}

		public InputMethod getDeviceInputMethod() {
			return inputMethod;
		}

		public DeviceDisplay getDeviceDisplay() {
			return deviceDisplay;
		}

		public FontManager getDeviceFontManager() {
			return fontManager;
		}

		public InputStream getResourceAsStream(String name) {
			try {
				if (name.startsWith("/")) {
					return MicroEmulator.this.getAssets().open(name.substring(1));
				} else {
					return MicroEmulator.this.getAssets().open(name);
				}
			} catch (IOException e) {
				Logger.debug(e);
				return null;
			}
		}
				
	};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        Logger.removeAllAppenders();
        Logger.setLocationEnabled(false);
        Logger.addAppender(new AndroidLoggerAppender());
        
        System.setOut(new PrintStream(new OutputStream() {
        	
        	StringBuffer line = new StringBuffer();

			@Override
			public void write(int oneByte) throws IOException {
				if (((char) oneByte) == '\n') {
					Logger.debug(line.toString());
					line.delete(0, line.length() - 1);
				} else {
					line.append((char) oneByte);
				}
			}
        	
        }));
        
        System.setErr(new PrintStream(new OutputStream() {
        	
        	StringBuffer line = new StringBuffer();

			@Override
			public void write(int oneByte) throws IOException {
				if (((char) oneByte) == '\n') {
					Logger.debug(line.toString());
					line.delete(0, line.length() - 1);
				} else {
					line.append((char) oneByte);
				}
			}
        	
        }));
        
        String midletClassName = getResources().getString(0x7f020001);

        java.util.List params = new ArrayList();
        params.add("--usesystemclassloader");
        params.add(midletClassName);
        
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        ((AndroidDeviceDisplay) emulatorContext.getDeviceDisplay()).displayRectangleWidth = display.getWidth();
        ((AndroidDeviceDisplay) emulatorContext.getDeviceDisplay()).displayRectangleHeight = display.getHeight() - 25;
        
        common = new Common(emulatorContext);
        common.setRecordStoreManager(new AndroidRecordStoreManager(this));
        common.setDevice(new AndroidDevice(emulatorContext, this));        
        common.initParams(params, null, AndroidDevice.class);
               
        System.setProperty("microedition.platform", "microemulator-android");

        common.getLauncher().setSuiteName(midletClassName);
        common.initMIDlet(true);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return false;
		}
		final DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return false;
		}
		AndroidDisplayableUI ui = (AndroidDisplayableUI) da.getCurrentUI();
		if (ui == null) {
			return false;
		}		
		
		menu.clear();	
		boolean result = false;
		final CommandListener l = ui.getCommandListener();
		ArrayList commands = new ArrayList(ui.getCommands());
		for (Iterator it = commands.iterator(); it.hasNext(); ) {
			result = true;
			final Command cmd = (Command) it.next();
			menu.add(0, 0, cmd.getLabel(), new Runnable() {
				public void run() {
					if (l != null) {
						l.commandAction(cmd, da.getCurrent());
					}
				}				
			});
		}

		return result;
	}

}