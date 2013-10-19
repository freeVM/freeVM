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

import java.awt.image.RGBImageFilter;

import org.microemu.device.DeviceFactory;
import org.microemu.device.impl.Color;



public class GrayImageFilter extends RGBImageFilter
{

  private double Yr, Yg, Yb;
  private double Rr, Rg, Rb;


  public GrayImageFilter ()
	{
    this(0.2126d, 0.7152d, 0.0722d);
  }


  public GrayImageFilter (double Yr, double Yg, double Yb)
	{
    this.Yr = Yr;
    this.Yg = Yg;
    this.Yb = Yb;
    canFilterIndexColorModel = true;
    Color backgroundColor = 
        ((J2SEDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getBackgroundColor();    
    Color foregroundColor = 
        ((J2SEDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getForegroundColor();    
    Rr = (backgroundColor.getRed() - foregroundColor.getRed()) / 256d;
    Rg = (backgroundColor.getGreen() - foregroundColor.getGreen()) / 256d;
    Rb = (backgroundColor.getBlue() - foregroundColor.getBlue()) / 256d;
  }


  public int filterRGB (int x, int y, int rgb)
	{
    int a = (rgb & 0xFF000000);
    int r = (rgb & 0x00FF0000) >>> 16;
    int g = (rgb & 0x0000FF00) >>> 8;
    int b = (rgb & 0x000000FF);
    int Y = (int)(Yr * r + Yg * g + Yb * b) % 256;
    if (Y > 255) {
      Y = 255;
    }
    Color foregroundColor = 
        ((J2SEDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getForegroundColor();    
    r = (int) (Rr * Y) + foregroundColor.getRed();
    g = (int) (Rg * Y) + foregroundColor.getGreen();
    b = (int) (Rb * Y) + foregroundColor.getBlue();

    return a | (r << 16) | (g << 8) | b;
  }

}
