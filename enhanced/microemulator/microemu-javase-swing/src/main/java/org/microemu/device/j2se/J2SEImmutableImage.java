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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import org.microemu.log.Logger;

public class J2SEImmutableImage extends javax.microedition.lcdui.Image {
    private Image img;

    private int width;

    private int height;

    public J2SEImmutableImage(Image image) {
        this.img = image;
        this.width = -1;
        this.height = -1;
    }

    public J2SEImmutableImage(J2SEMutableImage image) {
        img = Toolkit.getDefaultToolkit().createImage(image.getImage().getSource());
        this.width = -1;
        this.height = -1;
    }

    public int getHeight() {
        if (height == -1) {
            ImageObserver observer = new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    if ((infoflags & ImageObserver.WIDTH) != 0) {
                        J2SEImmutableImage.this.width = width;
                    }
                    if ((infoflags & ImageObserver.HEIGHT) != 0) {
                        synchronized (this) {
                            J2SEImmutableImage.this.height = height;
                            this.notify();
                        }
                        return false;
                    }

                    return true;
                }                
            };
            synchronized (observer) {
                // Fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4905411 (Java < 1.5)
                try {
                    height = img.getHeight(observer);
                } catch (NullPointerException ex) {
                }
                if (height == -1) {
                    try {
                        observer.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        return height;
    }

    public Image getImage() {
        return img;
    }

    public int getWidth() {
        if (width == -1) {
            ImageObserver observer = new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    if ((infoflags & ImageObserver.HEIGHT) != 0) {
                        J2SEImmutableImage.this.height = height;
                    }
                    if ((infoflags & ImageObserver.WIDTH) != 0) {
                        synchronized (this) {
                            J2SEImmutableImage.this.width = width;
                            this.notify();
                        }
                        return false;
                    }

                    return true;
                }                
            };
            synchronized (observer) {
                // Fix for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4905411 (Java < 1.5)
                try {
                    width = img.getWidth(observer);
                } catch (NullPointerException ex) {
                }
                if (width == -1) {
                    try {
                        observer.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        return width;
    }

    public void getRGB(int[] argb, int offset, int scanlength, int x, int y, int width, int height) {

        if (width <= 0 || height <= 0)
            return;
        if (x < 0 || y < 0 || x + width > getWidth() || y + height > getHeight())
            throw new IllegalArgumentException("Specified area exceeds bounds of image");
        if ((scanlength < 0 ? -scanlength : scanlength) < width)
            throw new IllegalArgumentException("abs value of scanlength is less than width");
        if (argb == null)
            throw new NullPointerException("null rgbData");
        if (offset < 0 || offset + width > argb.length)
            throw new ArrayIndexOutOfBoundsException();
        if (scanlength < 0) {
            if (offset + scanlength * (height - 1) < 0)
                throw new ArrayIndexOutOfBoundsException();
        } else {
            if (offset + scanlength * (height - 1) + width > argb.length)
                throw new ArrayIndexOutOfBoundsException();
        }

        try {
            (new java.awt.image.PixelGrabber(img, x, y, width, height, argb, offset, scanlength)).grabPixels();
        } catch (InterruptedException e) {
            Logger.error(e);
        }
    }

}
