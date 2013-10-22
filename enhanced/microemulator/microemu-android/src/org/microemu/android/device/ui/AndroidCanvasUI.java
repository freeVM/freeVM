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

package org.microemu.android.device.ui;

import javax.microedition.android.lcdui.Canvas;
import javax.microedition.android.lcdui.Image;

import org.microemu.MIDletBridge;
import org.microemu.android.MicroEmulatorActivity;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.device.AndroidMutableImage;
import org.microemu.device.Device;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.DeviceFactory;
import org.microemu.device.ui.CanvasUI;

import android.content.Context;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class AndroidCanvasUI extends AndroidDisplayableUI implements CanvasUI {

	private MicroEmulatorActivity activity;
	
	private CanvasView view;
	
	public AndroidCanvasUI(final MicroEmulatorActivity activity, Canvas canvas) {
		this.activity = activity;
		
		activity.post(new Runnable() {
			public void run() {
				AndroidCanvasUI.this.view = new CanvasView(activity);
			}
		});
	}
	
	public View getView() {
		return view;
	}
	
	public Image getImage() {
		// TODO improve method that waits for for view being initialized
		while (view == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return view.getImage();
	}

	//
	// DisplayableUI
	//

	public void hideNotify() {
	}

	public void showNotify() {
		activity.post(new Runnable() {
			public void run() {
				activity.setContentView(view);
				view.requestFocus();
			}
		});
	}
	
	public void invalidate() {
		// TODO implement title painting		
	}

	private class CanvasView extends View {
		
		public CanvasView(Context context) {
			super(context);
			
			setFocusable(true);
		}

		private AndroidMutableImage displayImage = null;
		
		private Paint paint = new Paint();
		
		public Image getImage() {
			synchronized(this) {
				if (displayImage == null) {
					DeviceDisplay deviceDisplay = DeviceFactory.getDevice().getDeviceDisplay();
					displayImage = new AndroidMutableImage(deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight());
				}
			}

			return displayImage;
		}

		//
		// View
		//
		
		@Override
		protected void onDraw(android.graphics.Canvas canvas) {
			if (displayImage != null) {
				synchronized (displayImage) {
					canvas.drawBitmap(displayImage.getBitmap(), 0, 0, paint);
				}
			}
		}	
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (MIDletBridge.getCurrentMIDlet() == null) {
				return false;
			}
			
			// KEYCODE_SOFT_LEFT == menu key 
			if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
				return false;
			}
			
			Device device = DeviceFactory.getDevice();
			((AndroidInputMethod) device.getInputMethod()).buttonPressed(event);
			
			return true;
		}

		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			if (MIDletBridge.getCurrentMIDlet() == null) {
				return false;
			}

			// KEYCODE_SOFT_LEFT == menu key 
			if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
				return false;
			}

			Device device = DeviceFactory.getDevice();
			((AndroidInputMethod) device.getInputMethod()).buttonReleased(event);

			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			Device device = DeviceFactory.getDevice();
			AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				inputMethod.pointerPressed((int) event.getX(), (int) event.getY());
				break;
			case MotionEvent.ACTION_UP :
				inputMethod.pointerReleased((int) event.getX(), (int) event.getY());
				break;
			case MotionEvent.ACTION_MOVE :
				inputMethod.pointerDragged((int) event.getX(), (int) event.getY());
				break;
			default:
				return false;
			}
			
			return true;
		}

	}

}
