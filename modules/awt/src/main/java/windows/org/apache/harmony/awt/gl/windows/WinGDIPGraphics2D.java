/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Map;

import org.apache.harmony.awt.gl.CommonGraphics2D;
import org.apache.harmony.awt.gl.MultiRectArea;
import org.apache.harmony.awt.gl.font.NativeFont;
import org.apache.harmony.awt.wtk.NativeWindow;


/**
 * Graphics2D implementation for Windows GDI+ library
 *
 */
public class WinGDIPGraphics2D extends CommonGraphics2D {
    private NativeWindow nw = null;
    private long hdc = 0;
    private long gi = 0;

    private final Dimension size;

    GraphicsConfiguration config = null;

    private WinVolatileImage img = null;

    // These two flags shows are current Stroke and
    // Paint transferred to native objects or not.
    private boolean nativePen = false;
    private boolean nativeBrush = false;

    // This array is used for passing Path data to
    // native code.
    // It is not thread safe.
    // But WTK guys think that Graphics should not
    // be called from different threads
    private float []pathArray = null;
    private float []pathPoints = null;

    private static final long gdipToken;

    static {
        System.loadLibrary("gl");

        // GDI+ startup
        gdipToken = gdiPlusStartup();

        // Prepare GDI+ shutdown
        GDIPShutdown hook = new GDIPShutdown();
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public WinGDIPGraphics2D(NativeWindow nw, int tx, int ty, MultiRectArea clip) {
        super(tx, ty, clip);
        this.nw = nw;

        Rectangle b = clip.getBounds();
        size = new Dimension(b.width, b.height);

        gi = createGraphicsInfo(this.nw.getId(), tx, ty, b.width, b.height);
        setTransformedClip(this.clip);
        jtr = GDIPTextRenderer.inst;
        dstSurf = new GDISurface(gi);
        blitter = GDIBlitter.getInstance();
        setTransform(getTransform());
    }

    public WinGDIPGraphics2D(NativeWindow nw, int tx, int ty, int width, int height) {
        super(tx, ty);
        this.nw = nw;

        size = new Dimension(width, height);

        gi = createGraphicsInfo(this.nw.getId(), tx, ty, width, height);
        setTransformedClip(this.clip);
        jtr = GDIPTextRenderer.inst;

        dstSurf = new GDISurface(gi);
        blitter = GDIBlitter.getInstance();
        if (debugOutput)
            System.err.println("WinGDIPGraphics2D("+nw+", "+tx+", "+ty+", "+width+", "+height+")");
        setTransform(getTransform());

    }

    public WinGDIPGraphics2D(WinVolatileImage img, int width, int height) {
        this(img, 0, width, height);
    }

    public WinGDIPGraphics2D(WinVolatileImage img, long ogi, int width, int height) {
        super();
        size = new Dimension(width, height);
        this.img = img;
        if (ogi != 0)
            this.gi = copyImageInfo(ogi);
        else
            this.gi = copyImageInfo(img.gi);
        setTransformedClip(this.clip);
        dstSurf = img.getImageSurface();
        blitter = GDIBlitter.getInstance();
        jtr = GDIPTextRenderer.inst;
        setTransform(getTransform());
    }

    public void addRenderingHints(Map<?,?> hints) {
        super.addRenderingHints(hints);
        Object value = this.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (value == RenderingHints.VALUE_ANTIALIAS_ON) 
            NativeFont.setAntialiasing(gi,true);
        else
            NativeFont.setAntialiasing(gi,false);
    }
    
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        copyArea(gi, x, y, width, height, dx, dy);
    }

    public Graphics create() {
        if (debugOutput)
            System.err.println("WinGDIPGraphics2D.create()");

        WinGDIPGraphics2D res = null;
        if (img == null) {
            res = new WinGDIPGraphics2D(nw, origPoint.x, origPoint.y, size.width, size.height);
        } else {
            res = new WinGDIPGraphics2D(img, gi, size.width, size.height);
        }
        copyInternalFields(res);
        return res;
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        if (config == null) {
            if (img == null) {
                config = new WinGraphicsConfiguration(nw.getId(), getDC());
            } else {
                config = new WinGraphicsConfiguration(getDC());
            }
        }

        return config;
    }

    protected void fillMultiRectAreaPaint(MultiRectArea mra) {
        if (nativeBrush)
            fillRects(gi, mra.rect, mra.rect[0]-1);
        else
            super.fillMultiRectAreaPaint(mra);
    }




    /***************************************************************************
     *
     *  Overriden methods
     *
     ***************************************************************************/

    public void setColor(Color color) {
        if (color == null)
            return;
        super.setColor(color);
        setSolidBrush(gi, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        nativeBrush = true;
        setStroke(getStroke());
    }


    //REMARK: It seems that transfrom affects paints too
    //REMARK: Think how to implement this
    public void setPaint(Paint paint) {
        if (paint instanceof Color) {
            setColor((Color)paint);
        } else {
            this.paint = paint;
            nativeBrush = false;
            if (paint instanceof GradientPaint) {
                GradientPaint p = (GradientPaint)paint;
                if (!p.isCyclic())
                    return;
                Color c1 = p.getColor1();
                Color c2 = p.getColor2();
                Point2D p1 = transform.transform(p.getPoint1(), null);
                Point2D p2 = transform.transform(p.getPoint2(), null);
                setLinearGradientBrush(gi, (int)Math.round(p1.getX()), (int)Math.round(p1.getY()), c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha(),
                                           (int)Math.round(p2.getX()), (int)Math.round(p2.getY()), c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha(), p.isCyclic());
                nativeBrush = true;
            }
            setStroke(getStroke());
        }
    }

    public void dispose() {
        if (gi == 0)
            return;
        if (dstSurf instanceof GDISurface)
            dstSurf.dispose();
        disposeGraphicsInfo(gi);
        gi = 0;
        super.dispose();
        if (debugOutput)
            System.err.println("WinGDIPGraphics2D.dispose()");
    }

    public void drawGlyphVector(GlyphVector gv, float x, float y) {
        jtr.drawGlyphVector(this, gv, x, y);
    }

    public void drawString(String str, float x, float y) {
//        XXX: GDITextRenderer provide faster text drawing,
//             but there still conflict between GDI and GDI+ hdc usage.
//             This problem is to be investigated.
//        
//        AffineTransform at = this.getTransform();
//        AffineTransform fAT = this.getFont().getTransform();
//        if (((at == null) || at.isIdentity() || 
//             (at.getType() == AffineTransform.TYPE_TRANSLATION)) 
//             && (fAT.isIdentity() || (fAT.getType() == AffineTransform.TYPE_TRANSLATION))){
//
//            // Set graphics hdc clip
//            long hOldGDIRgn = 0;
//            long gi = getGraphicsInfo();
//
//            if (clip != null && clip.rect[0] != 0 ){
//                hOldGDIRgn = WinThemeGraphics.setGdiClip(gi, clip.rect, clip.rect[0]-1);
//            } else {
//                WinThemeGraphics.restoreGdiClip(gi, hOldGDIRgn);
//            }
//            gtr.drawString(this, str, (float)(x + fAT.getTranslateX()), (float)(y + fAT.getTranslateY()));
//            
//            // Restore graphics hdc clip
//            WinThemeGraphics.restoreGdiClip(gi, hOldGDIRgn);
//            return;
//        }    
        jtr.drawString(this, str, x, y);
    }

    public void setStroke(Stroke stroke) {
        super.setStroke(stroke);
        nativePen = nativeBrush && stroke instanceof BasicStroke;
        if (!nativePen) {
            deletePen(gi);
            return;
        }

        BasicStroke bs = (BasicStroke)stroke;
        float []dash = bs.getDashArray();
        setPen(gi, bs.getLineWidth(), bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(),
                dash, (dash != null)?dash.length:0, bs.getDashPhase());
    }

    public void draw(Shape s) {
        if (!nativePen) {
            super.draw(s);
            return;
        }

        PathIterator pi = s.getPathIterator(transform, 0.5);
        int len = getPathArray(pi);
        drawShape(gi, pathArray, len, pi.getWindingRule());
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        if (!nativePen) {
            super.drawLine(x1, y1, x2, y2);
            return;
        }

        drawLine(gi, x1, y1, x2, y2);
    }

    public void drawRect(int x, int y, int width, int height) {
        if (!nativePen) {
            super.drawRect(x, y, width, height);
            return;
        }

        drawRect(gi, x, y, width, height);
    }

    public void fill(Shape s) {
        if (!nativeBrush) {
            super.fill(s);
            return;
        }

        PathIterator pi = s.getPathIterator(transform, 0.5);
        int len = getPathArray(pi);
        fillShape(gi, pathArray, len, pi.getWindingRule());
    }

    public void fillRect(int x, int y, int width, int height) {
        if (!nativeBrush) {
            super.fillRect(x, y, width, height);
            return;
        }

        fillRect(gi, x, y, width, height);
    }

    /**
     * Sets native clip to specified area
     * 
     * @param clip Transformed clip to set
     */
    protected void setTransformedClip(MultiRectArea clip) {
        super.setTransformedClip(clip);
        if (gi == 0)
            return;
        if (clip == null)
            resetClip(gi);
        else
            setClip(gi, clip.rect, clip.rect[0]-1);
    }

    /***************************************************************************
    *
    *  Transformation methods
    *
    ***************************************************************************/

    public void setTransform(AffineTransform transform) {
        super.setTransform(transform);
        if (gi == 0)
            return;

        setNativeTransform(gi, matrix);
    }

    public void rotate(double theta) {
        super.rotate(theta);

        setNativeTransform(gi, matrix);
    }

    public void rotate(double theta, double x, double y) {
        super.rotate(theta, x, y);

        setNativeTransform(gi, matrix);
    }

    public void scale(double sx, double sy) {
        super.scale(sx, sy);

        setNativeTransform(gi, matrix);
    }

    public void shear(double shx, double shy) {
        super.shear(shx, shy);

        setNativeTransform(gi, matrix);
    }

    public void transform(AffineTransform at) {
        super.transform(at);

        setNativeTransform(gi, matrix);
    }

    public void translate(double tx, double ty) {
        super.translate(tx, ty);

        setNativeTransform(gi, matrix);
    }

    public void translate(int tx, int ty) {
        super.translate(tx, ty);

        setNativeTransform(gi, matrix);
    }

    /***************************************************************************
    *
    *  Class specific methods
    *
    ***************************************************************************/

    /**
     * Returns handle to underlying device context
     */
    public long getDC() {
        if (hdc == 0)
            hdc = getDC(gi);
        return hdc;
    }

    /**
     * Returns pointer to underlying native GraphicsInfo structure
     *  
     * @return Pointer to GraphicsInfo structure
     */
    public long getGraphicsInfo() {
        return gi;
    }

   /***************************************************************************
    *
    *  Private methods
    *
    ***************************************************************************/
    /**
     * Converts PathIterator into array of int values. This array is
     * stored in pathArray field.
     * Array then used to pass Shape to native drawing routines
     * 
     * @param pi PathIterator recieved from Shape
     * @return Number of result array elements.
     */
    private int getPathArray(PathIterator pi) {
        if (pathArray == null) {
            pathArray = new float[8192];
            pathPoints = new float[6];
        }

        int i = 0;

        while (!pi.isDone()) {
            int seg = pi.currentSegment(pathPoints);
            pathArray[i++] = seg;
            switch (seg) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    pathArray[i++] = pathPoints[0];
                    pathArray[i++] = pathPoints[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
            pi.next();
        }
        return i;
    }

    /***************************************************************************
     *
     *  Native methods
     *
     ***************************************************************************/

    // GDI+ system startup/shutdown methods
    private native static long gdiPlusStartup();
    private native static void gdiPlusShutdown(long token);

    // Creates native GraphicsInfo structure
    private native long createGraphicsInfo(long hwnd, int x, int y, int width, int height);
    static native long createCompatibleImageInfo(long hwnd, int width, int height);
    static native long createCompatibleImageInfo(byte[] bytes, int width, int height);
    private native long copyImageInfo(long gi);

    // Releases GraphicsInfo structure
    static native void disposeGraphicsInfo(long gi);

    private native void copyArea(long gi, int x, int y, int width, int height, int dx, int dy);
    
    // Methods to set solid and gradient brushes
    private native void setSolidBrush(long gi, int r, int g, int b, int a);
    private native void setLinearGradientBrush(long gi, int x1, int y1, int r1, int g1, int b1, int a1, int x2, int y2, int r2, int g2, int b2, int a2, boolean cyclic);
    
    // Fills specified rectangles by native brush
    private native void fillRects(long gi, int []vertices, int len);

    private native long getDC(long gi);

    //Pen manipulation routins
    private native boolean setPen(long gi, float lineWidth, int endCap, int lineJoin, float miterLimit, float[] dashArray, int dashLen, float dashPhase);
    private native void deletePen(long gi);

    // Draw/Fill Shape/GraphicsPath
    private native void drawShape(long gi, float []path, int len, int winding);
    private native void fillShape(long gi, float []path, int len, int winding);

    // Draw native primitives
    private native void drawLine(long gi, int x1, int y1, int x2, int y2);
    private native void drawRect(long gi, int x, int y, int width, int height);

    // Fill native primitives
    private native void fillRect(long gi, int x, int y, int width, int height);

    public void setRenderingHint(RenderingHints.Key key, Object value) {
        super.setRenderingHint(key,value);
        Object val = this.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (val == RenderingHints.VALUE_ANTIALIAS_ON) 
            NativeFont.setAntialiasing(gi,true);
        else
            NativeFont.setAntialiasing(gi,false);
    }

    public void setRenderingHints(Map<?,?> hints) {
        super.setRenderingHints(hints);
        Object value = this.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (value == RenderingHints.VALUE_ANTIALIAS_ON) 
            NativeFont.setAntialiasing(gi,true);
        else
            NativeFont.setAntialiasing(gi,false);
    }


    // Set native clip
    private native void setClip(long gi, int[] vertices, int len);
    private native void resetClip(long gi);

    // Update native affine transform matrix
    private native void setNativeTransform(long gi, double[] matrix);



    /***************************************************************************
     *
     *  Shutdown class
     *
     ***************************************************************************/
    /**
     * We need to shutdown GDI+ before exit.
     */
    private static class GDIPShutdown extends Thread {
        public void run() {
            WinGDIPGraphics2D.gdiPlusShutdown(WinGDIPGraphics2D.gdipToken);
        }
    }
}