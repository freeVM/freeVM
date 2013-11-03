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

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.android.lcdui.Graphics;
import javax.microedition.android.lcdui.Image;
import javax.microedition.android.lcdui.game.Sprite;

import org.microemu.DisplayAccess;
import org.microemu.EmulatorContext;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.device.ui.AndroidCanvasUI;
import org.microemu.device.Device;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.DeviceFactory;
import org.microemu.device.MutableImage;
import org.microemu.device.ui.CanvasUI;
import org.microemu.device.ui.DisplayableUI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

public class AndroidDeviceDisplay implements DeviceDisplay {
	
	private EmulatorContext context;
	
	// TODO change this
	public int displayRectangleWidth;
	
	// TODO change this
	public int displayRectangleHeight;
	
	public AndroidDeviceDisplay(EmulatorContext context) {
		this.context = context;
	}

	public Image createImage(String arg0) throws IOException {
		System.out.println("createImage: " + arg0);
		// TODO Auto-generated method stub
		return null;
	}

	public Image createImage(Image arg0) {
		System.out.println("createImage: " + arg0);
		// TODO Auto-generated method stub
		return null;
	}

	public Image createImage(InputStream arg0) throws IOException {
		System.out.println("createImage: " + arg0);
		// TODO Auto-generated method stub
		return null;
	}

	public Image createImage(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}

		return new AndroidMutableImage(width, height);
	}

	public Image createImage(byte[] imageData, int imageOffset, int imageLength) {
		return new AndroidImmutableImage(BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength));
	}

	public Image createImage(Image image, int x, int y, int width, int height, int transform) {
		// TODO AndroidDisplayGraphics.drawRegion code is similar
		if (image == null)
			throw new NullPointerException();
		if (x + width > image.getWidth() || y + height > image.getHeight() || width <= 0 || height <= 0 || x < 0
				|| y < 0)
			throw new IllegalArgumentException("Area out of Image");

		int[] rgbData = new int[height * width];
		int[] rgbTransformedData = new int[height * width];
		if (image instanceof AndroidImmutableImage) {
			((AndroidImmutableImage) image).getRGB(rgbData, 0, width, x, y, width, height);
		} else {
			((AndroidMutableImage) image).getRGB(rgbData, 0, width, x, y, width, height);
		}

		int colIncr, rowIncr, offset;

		switch (transform) {
		case Sprite.TRANS_NONE: {
			offset = 0;
			colIncr = 1;
			rowIncr = 0;
			break;
		}
		case Sprite.TRANS_ROT90: {
			offset = (height - 1) * width;
			colIncr = -width;
			rowIncr = (height * width) + 1;
			int temp = width;
			width = height;
			height = temp;
			break;
		}
		case Sprite.TRANS_ROT180: {
			offset = (height * width) - 1;
			colIncr = -1;
			rowIncr = 0;
			break;
		}
		case Sprite.TRANS_ROT270: {
			offset = width - 1;
			colIncr = width;
			rowIncr = -(height * width) - 1;
			int temp = width;
			width = height;
			height = temp;
			break;
		}
		case Sprite.TRANS_MIRROR: {
			offset = width - 1;
			colIncr = -1;
			rowIncr = width << 1;
			break;
		}
		case Sprite.TRANS_MIRROR_ROT90: {
			offset = (height * width) - 1;
			colIncr = -width;
			rowIncr = (height * width) - 1;
			int temp = width;
			width = height;
			height = temp;
			break;
		}
		case Sprite.TRANS_MIRROR_ROT180: {
			offset = (height - 1) * width;
			colIncr = 1;
			rowIncr = -(width << 1);
			break;
		}
		case Sprite.TRANS_MIRROR_ROT270: {
			offset = 0;
			colIncr = width;
			rowIncr = -(height * width) + 1;
			int temp = width;
			width = height;
			height = temp;
			break;
		}
		default:
			throw new IllegalArgumentException("Bad transform");
		}

		// now the loops!
		for (int row = 0, i = 0; row < height; row++, offset += rowIncr) {
			for (int col = 0; col < width; col++, offset += colIncr, i++) {
/*			    int a = (rgbData[offset] & 0xFF000000);
			    int b = (rgbData[offset] & 0x00FF0000) >>> 16;
			    int g = (rgbData[offset] & 0x0000FF00) >>> 8;
			    int r = (rgbData[offset] & 0x000000FF);

			    rgbTransformedData[i] = a | (r << 16) | (g << 8) | b;*/
				rgbTransformedData[i] = rgbData[offset];
			}
		}
		// to aid gc
		rgbData = null;
		image = null;

		return createRGBImage(rgbTransformedData, width, height, true);
	}

	public Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
		if (rgb == null)
			throw new NullPointerException();
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException();
		
		// TODO processAlpha is not handled natively, check whether we need to create copy of rgb
		int[] newrgb = rgb;
		if (!processAlpha) {
			newrgb = new int[rgb.length];
			for (int i = 0; i < rgb.length; i++) {
				newrgb[i] = (0x00ffffff & rgb[i]) | 0xff000000;
			}
		}
		return new AndroidImmutableImage(Bitmap.createBitmap(newrgb, width, height, Bitmap.Config.ARGB_8888));
	}

	public MutableImage getDisplayImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFullHeight() {
		return displayRectangleHeight;
	}

	public int getFullWidth() {
		return displayRectangleWidth;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		return displayRectangleHeight;
	}

	public int getWidth() {
		// TODO Auto-generated method stub
		return displayRectangleWidth;
	}

	public boolean isColor() {
		return true;
	}

	public boolean isFullScreenMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public int numAlphaLevels() {
		return 256;
	}

	public int numColors() {
		return 65536;
	}

	public void repaint(int x, int y, int width, int height) {
		paintDisplayable(x, y, width, height);
	}

	public void setScrollDown(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setScrollUp(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void paintDisplayable(int x, int y, int width, int height) {
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return;
		}
		DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return;
		}
		DisplayableUI current = da.getCurrentUI();
		if (current == null) {
			return;
		}

		// TODO
		// g.save(android.graphics.Canvas.CLIP_SAVE_FLAG);
		// TODO
		// if (!(current instanceof Canvas) || ((Canvas) current).getWidth() != displayRectangle.width
		// 		|| ((Canvas) current).getHeight() != displayRectangle.height) {
		// 	g.translate(displayPaintable.x, displayPaintable.y);
		// }
		// TODO
		// Font oldf = g.getFont();
		if (current instanceof CanvasUI) {
			Device device = DeviceFactory.getDevice();
			
			// TODO take region size into account
			if (device != null) {
				synchronized (current) {
					Image displayImage = ((AndroidCanvasUI) current).getImage();
					synchronized (displayImage) {
						Graphics canvas = displayImage.getGraphics();

						canvas.clipRect(x, y, x + width, y + height);
						ma.getDisplayAccess().paint(canvas);
						// TODO
						// if (!deviceDisplay.isFullScreenMode()) {
						// 	deviceDisplay.paintControls(canvas);
						// }
					}
				}
			}
			View view = ((AndroidCanvasUI) current).getView(); 
			view.postInvalidate();
		} else {
			// TODO extend DisplayableUI interface
			//current.paint();
		}
		// TODO
		// g.setFont(oldf);
		// TODO
		// if (!(current instanceof Canvas) || ((Canvas) current).getWidth() != displayRectangle.width
		//		|| ((Canvas) current).getHeight() != displayRectangle.height) {
		// 	g.translate(-displayPaintable.x, -displayPaintable.y);
		//}
		// TODO
		// g.restore();
	}
	
}
