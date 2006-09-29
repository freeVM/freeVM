/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Alexey A. Petrenko
 * @version $Revision$
 */
package org.apache.harmony.awt.gl.windows;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.VolatileImage;

import org.apache.harmony.awt.nativebridge.windows.Win32;
import org.apache.harmony.awt.nativebridge.windows.WindowsDefs;

/**
 * Windows GraphicsConfiguration implementation
 *
 */
public class WinGraphicsConfiguration extends GraphicsConfiguration {
    private static final Win32 win32 = Win32.getInstance();

    private WinGraphicsDevice device;
    private ColorModel cm;
    private long flags;
    private byte pixelType;

    private int bits = -1;
    private byte redBits = -1;
    private byte redShift = -1;
    private int rmask = -1;
    private byte greenBits = -1;
    private byte greenShift = -1;
    private int gmask = -1;
    private byte blueBits = -1;
    private byte blueShift = -1;
    private int bmask = -1;
    private byte alphaBits = -1;
    private byte alphaShift = -1;
    private int amask = -1;

    private int index;

    public WinGraphicsConfiguration(WinGraphicsDevice device, int index, Win32.PIXELFORMATDESCRIPTOR pfd) {
        this.device = device;
        this.index = index;
        init(pfd);
    }

    public WinGraphicsConfiguration(long hwnd, long hdc) {
        this(hdc);
        this.device = new WinGraphicsDevice(hwnd);
    }

    public WinGraphicsConfiguration(long hdc) {
        this.device = null;
        this.index = -1;

        int dci = win32.GetPixelFormat(hdc);
        dci = (dci > 0)?dci:1;
        Win32.PIXELFORMATDESCRIPTOR pfd = win32.createPIXELFORMATDESCRIPTOR(false);
        win32.DescribePixelFormat(hdc, dci, pfd.size(), pfd);
        init(pfd);
        pfd.free();
    }

    /**
     * Initializes private fileds with info from
     * native PIXELFORMATDESCRIPTOR structure.
     * 
     * @param pfd PIXELFORMATDESCRIPTOR structure.
     */
    private void init(Win32.PIXELFORMATDESCRIPTOR pfd) {
        flags = pfd.get_dwFlags();
        pixelType = pfd.get_iPixelType();
        if ((pixelType & WindowsDefs.PFD_TYPE_COLORINDEX) == WindowsDefs.PFD_TYPE_COLORINDEX) {
            cm = null;
            return;
        }

        bits = pfd.get_cColorBits();
        redBits = pfd.get_cRedBits();
        redShift = pfd.get_cRedShift();
        rmask = (int)(Math.pow(2,redBits)-1) << redShift;

        greenBits = pfd.get_cGreenBits();
        greenShift = pfd.get_cGreenShift();
        gmask = (int)(Math.pow(2,greenBits)-1) << greenShift;

        blueBits = pfd.get_cBlueBits();
        blueShift = pfd.get_cBlueShift();
        bmask = (int)(Math.pow(2,blueBits)-1) << blueShift;

        alphaBits = pfd.get_cAlphaBits();
        alphaShift = pfd.get_cAlphaShift();
        amask = (int)(Math.pow(2,alphaBits)-1) << alphaShift;

        cm = new DirectColorModel(bits, rmask, gmask, bmask, amask);
    }

    public GraphicsDevice getDevice() {
        return device;
    }

    public Rectangle getBounds() {
        return device.getBounds();
    }

    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }

    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }

    public BufferedImage createCompatibleImage(int width, int height) {
        return new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), false, null);
    }

    public BufferedImage createCompatibleImage(int width, int height, int transparency) {
        ColorModel cmt = getColorModel(transparency);
        if (cmt == null)
            throw new IllegalArgumentException("Transparency is not supported.");

        return new BufferedImage(cmt, cmt.createCompatibleWritableRaster(width, height), false, null);
    }

    public ColorModel getColorModel() {
        return cm;
    }

    public ColorModel getColorModel(int transparency) {
        return cm;
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        return new WinVolatileImage(this, width, height);
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
        return createCompatibleVolatileImage(width, height);
    }

    public long getFlags() {
        return flags;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof WinGraphicsConfiguration))
            return false;

        WinGraphicsConfiguration gc = (WinGraphicsConfiguration)obj;

        // We do not use flags now. So GraphicsConfigurations with
        // different flags are same for us.
        //if (flags != gc.flags)
        //  return false;

        if (pixelType != gc.pixelType)
            return false;

        if (bits != gc.bits)
            return false;

        if (redBits != gc.redBits)
            return false;

        if (redShift != gc.redShift)
            return false;

        if (greenBits != gc.greenBits)
            return false;

        if (greenShift != gc.greenShift)
            return false;

        if (blueBits != gc.blueBits)
            return false;

        if (blueShift != gc.blueShift)
            return false;

        if (alphaBits != gc.alphaBits)
            return false;

        if (alphaShift != gc.alphaShift)
            return false;

        return true;
    }

    public int getIndex() {
        return index;
    }
}
