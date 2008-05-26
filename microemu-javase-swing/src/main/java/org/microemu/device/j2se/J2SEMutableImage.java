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

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import org.microemu.device.MutableImage;
import org.microemu.log.Logger;


public class J2SEMutableImage extends MutableImage
{
	private BufferedImage img;
	private PixelGrabber grabber = null;
	private int[] pixels;


	public J2SEMutableImage(int width, int height)
	{
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics g = img.getGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
	}


	public javax.microedition.lcdui.Graphics getGraphics()
	{
        java.awt.Graphics2D g = (java.awt.Graphics2D) img.getGraphics();
        g.setClip(0, 0, getWidth(), getHeight());
        J2SEDisplayGraphics displayGraphics = new J2SEDisplayGraphics(g, this);
		displayGraphics.setColor(0x00000000);
		displayGraphics.translate(-displayGraphics.getTranslateX(), -displayGraphics.getTranslateY());
		
		return displayGraphics;
	}


	public boolean isMutable()
	{
		return true;
	}


	public int getHeight()
	{
		return img.getHeight();
	}


	public java.awt.Image getImage()
	{
		return img;
	}


	public int getWidth()
	{
		return img.getWidth();
	}


	public int[] getData()
	{
		if (grabber == null) {
			pixels = new int[getWidth() * getHeight()];
			grabber = new PixelGrabber(img, 0, 0, getWidth(), getHeight(), pixels, 0, getWidth());
		}

		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			Logger.error(e);
		}

		return pixels;
	}

        // Andres Navarro
        public void getRGB(int []argb, int offset, int scanlength,
                int x, int y, int width, int height) {

            if (width <= 0 || height <= 0)
                return;
            if (x < 0 || y < 0 || x + width > getWidth() || y + height > getHeight())
                throw new IllegalArgumentException("Specified area exceeds bounds of image");
            if ((scanlength < 0? -scanlength:scanlength) < width)
                throw new IllegalArgumentException("abs value of scanlength is less than width");
            if (argb == null)
                throw new NullPointerException("null rgbData");
            if (offset < 0 || offset + width > argb.length)
                throw new ArrayIndexOutOfBoundsException();
            if (scanlength < 0) {
                if (offset + scanlength*(height-1) < 0)
                    throw new ArrayIndexOutOfBoundsException();
            } else {
                if (offset + scanlength*(height-1) + width > argb.length)
                    throw new ArrayIndexOutOfBoundsException();
            }

            try {
                (new PixelGrabber(img, x, y, width, height, argb, offset, scanlength)).grabPixels();
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
        // Andres Navarro

}
