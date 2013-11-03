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
 
package org.microemu.device.swt;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.microemu.app.ui.swt.ImageFilter;
import org.microemu.app.ui.swt.SwtDeviceComponent;
import org.microemu.device.DeviceFactory;



public final class GrayImageFilter implements ImageFilter
{

  private double Yr, Yg, Yb;
  private double Rr, Rg, Rb;
  private Color foregroundColor;

  public GrayImageFilter ()
	{
    this(0.2126d, 0.7152d, 0.0722d);
  }


  public GrayImageFilter (double Yr, double Yg, double Yb)
	{
    this.Yr = Yr;
    this.Yg = Yg;
    this.Yb = Yb;
    Color backgroundColor = SwtDeviceComponent.getColor(new RGB(
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getBackgroundColor().getRed(),
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getBackgroundColor().getGreen(),
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getBackgroundColor().getBlue()));
    foregroundColor = SwtDeviceComponent.getColor(new RGB(
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getForegroundColor().getRed(),
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getForegroundColor().getGreen(),
    		((SwtDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay()).getForegroundColor().getBlue()));
    Rr = (backgroundColor.getRed() - foregroundColor.getRed()) / 256d;
    Rg = (backgroundColor.getGreen() - foregroundColor.getGreen()) / 256d;
    Rb = (backgroundColor.getBlue() - foregroundColor.getBlue()) / 256d;
  }


  public RGB filterRGB (int x, int y, RGB rgb)
	{
    int Y = (int)(Yr * rgb.red + Yg * rgb.green + Yb * rgb.blue) % 256;
    if (Y > 255) {
      Y = 255;
    }
    
    return new RGB(    
    		(int) (Rr * Y) + foregroundColor.getRed(),
    		(int) (Rg * Y) + foregroundColor.getGreen(),
    		(int) (Rb * Y) + foregroundColor.getBlue());
  }

}
