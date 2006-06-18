/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @author Oleg V. Khaschansky
 * @version $Revision$
 */

package org.apache.harmony.awt.gl.linux;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.AffineTransformOp;

import org.apache.harmony.awt.gl.*;
import org.apache.harmony.awt.gl.render.Blitter;
import org.apache.harmony.awt.gl.render.NativeImageBlitter;
import org.apache.harmony.awt.nativebridge.linux.X11;
import org.apache.harmony.awt.nativebridge.linux.X11Defs;

public class XBlitter implements Blitter {
    static final XBlitter inst = new XBlitter();

    public static XBlitter getInstance(){
        return inst;
    }

    public void blit(
            int srcX, int srcY, Surface srcSurf,
            int dstX, int dstY, Surface dstSurf,
            int width, int height,
            AffineTransform sysxform, AffineTransform xform,
            Composite comp, Color bgcolor, MultiRectArea clip
    ) {
        int type = xform.getType();
        switch (type) {
            case AffineTransform.TYPE_TRANSLATION:
                dstX += xform.getTranslateX();
                dstY += xform.getTranslateY();
            case AffineTransform.TYPE_IDENTITY:
                 blit(srcX, srcY, srcSurf, dstX, dstY, dstSurf,
                         width, height, sysxform, comp, bgcolor, clip);
                break;
            default:
                XSurface xDstSurf = (XSurface) dstSurf;

                BufferedImage compIm;
                int w = srcSurf.getWidth();
                int h = srcSurf.getHeight();

                if (!(srcSurf instanceof ImageSurface)) {
                    compIm = xDstSurf.g2d.xConfig.createCompatibleImage(w, h);

                    NativeImageBlitter.getInstance().blit(
                            srcX, srcY, srcSurf,
                            srcX, srcY,
                            AwtImageBackdoorAccessor.getInstance().getImageSurface(compIm),
                            w, h,
                            AlphaComposite.Src, null, null
                    );
                } else {
                    ColorModel cm = srcSurf.getColorModel();
                    compIm = new BufferedImage(
                            cm,
                            srcSurf.getRaster(),
                            cm.isAlphaPremultiplied(),
                            null
                    );
                }

                WritableRaster compRaster = compIm.getRaster();

                AffineTransform at = (AffineTransform) sysxform.clone();
                at.concatenate(xform);

                // Want to transform without translation to fit into destination image
                // Translation will be added then when blitting to final dest surface
                dstX += at.getTranslateX();
                dstY += at.getTranslateY();
                AffineTransform untranslated =
                        AffineTransform.getTranslateInstance(
                                -at.getTranslateX(),
                                -at.getTranslateY()
                        );
                untranslated.concatenate(at);

                AffineTransformOp atop =
                        new AffineTransformOp(untranslated, xDstSurf.g2d.getRenderingHints());

                Rectangle r = atop.getBounds2D(compRaster).getBounds();
                int tWidth = r.width;
                int tHeight = r.height;

                BufferedImage transformed;
                if (compIm.getColorModel().getTransparency() == Transparency.OPAQUE) {
                    transformed = xDstSurf.g2d.xConfig.createCompatibleImage(tWidth, tHeight);
                } else {
                    ColorModel cm = compIm.getColorModel();
                    transformed =
                            new BufferedImage(
                                    cm,
                                    compIm.getRaster().createCompatibleWritableRaster(
                                            tWidth,
                                            tHeight
                                    ),
                                    cm.isAlphaPremultiplied(),
                                    null
                            );
                }

                atop.filter(compIm, transformed);

                if (dstX < 0){
                    tWidth += dstX;
                    dstX = 0;
                }

                if (dstY < 0){
                    tHeight += dstY;
                    dstY = 0;
                }

                blit(
                        0, 0, AwtImageBackdoorAccessor.getInstance().getImageSurface(transformed),
                        dstX, dstY, dstSurf,
                        tWidth, tHeight,
                        comp, bgcolor, clip
                );
        }
    }

    public void blit(
            int srcX, int srcY, Surface srcSurf,
            int dstX, int dstY, Surface dstSurf,
            int width, int height,
            AffineTransform sysxform,
            Composite comp, Color bgcolor, MultiRectArea clip
    ) {
        int type = sysxform.getType();
        switch (type) {
            case AffineTransform.TYPE_TRANSLATION:
            case AffineTransform.TYPE_IDENTITY:
                 blit(
                         srcX, srcY, srcSurf,
                         dstX + (int) sysxform.getTranslateX(),
                         dstY + (int) sysxform.getTranslateY(),
                         dstSurf,
                         width, height,
                         comp, bgcolor, clip
                 );
                break;
            default:
                XSurface xDstSurf = (XSurface) dstSurf;

                BufferedImage compIm;
                int w = srcSurf.getWidth();
                int h = srcSurf.getHeight();

                if (!(srcSurf instanceof ImageSurface)) {
                    compIm = xDstSurf.g2d.xConfig.createCompatibleImage(w, h);

                    NativeImageBlitter.getInstance().blit(
                            srcX, srcY, srcSurf,
                            srcX, srcY,
                            AwtImageBackdoorAccessor.getInstance().getImageSurface(compIm),
                            w, h,
                            AlphaComposite.Src, null, null
                    );
                } else {
                    ColorModel cm = srcSurf.getColorModel();
                    compIm = new BufferedImage(
                            cm,
                            srcSurf.getRaster(),
                            cm.isAlphaPremultiplied(),
                            null
                    );
                }

                WritableRaster compRaster = compIm.getRaster();

                AffineTransform at = (AffineTransform) sysxform.clone();

                AffineTransformOp atop =
                        new AffineTransformOp(at, xDstSurf.g2d.getRenderingHints());

                Rectangle r = atop.getBounds2D(compRaster).getBounds();
                int tWidth = r.width;
                int tHeight = r.height;

                BufferedImage transformed;
                if (compIm.getColorModel().getTransparency() == Transparency.OPAQUE) {
                    transformed = xDstSurf.g2d.xConfig.createCompatibleImage(tWidth, tHeight);
                } else {
                    ColorModel cm = compIm.getColorModel();
                    transformed =
                            new BufferedImage(
                                    cm,
                                    compIm.getRaster().createCompatibleWritableRaster(
                                            tWidth,
                                            tHeight
                                    ),
                                    cm.isAlphaPremultiplied(),
                                    null
                            );
                }

                atop.filter(compIm, transformed);

                if (dstX < 0){
                    tWidth += dstX;
                    dstX = 0;
                }

                if (dstY < 0){
                    tHeight += dstY;
                    dstY = 0;
                }

                blit(
                        0, 0, AwtImageBackdoorAccessor.getInstance().getImageSurface(transformed),
                        dstX, dstY, dstSurf,
                        tWidth, tHeight,
                        comp, bgcolor, clip
                );
        }
    }

    public void blit(
            int srcX, int srcY, Surface srcSurf,
            int dstX, int dstY, Surface dstSurf,
            int width, int height,
            Composite comp, Color bgcolor, MultiRectArea clip
    ) {
        clip = new MultiRectArea(clip);
        // XXX - todo - need to do smth with bgcolor
        ColorModel srcCM = srcSurf.getColorModel();
        XSurface xDstSurf = (XSurface) dstSurf;

        if (srcSurf.isNativeDrawable() && srcCM.equals(dstSurf.getColorModel())) {
            if (srcSurf instanceof XSurface) { // Blit from native to native
                XSurface xSrcSurf = (XSurface) srcSurf;

                XGraphics2D g2d = xDstSurf.g2d;

                if (comp instanceof AlphaComposite) {
                    switch (((AlphaComposite) comp).getRule()) {
                        case AlphaComposite.SRC:
                        case AlphaComposite.SRC_ATOP:
                        case AlphaComposite.SRC_IN:
                        case AlphaComposite.SRC_OVER:
                            break; // GXCopy - is default
                        case AlphaComposite.DST:
                        case AlphaComposite.DST_ATOP:
                        case AlphaComposite.DST_IN:
                        case AlphaComposite.DST_OVER:
                            g2d.setImageGCFunction(X11Defs.GXnoop);
                            break;
                        case AlphaComposite.SRC_OUT: // Clear
                        case AlphaComposite.DST_OUT: // Clear
                        case AlphaComposite.CLEAR:
                        case AlphaComposite.XOR: // Clear
                            g2d.setImageGCFunction(X11Defs.GXclear);
                            break;
                        default: // Do nothing
                    }
                } else {
                    imBlit(
                            srcX, srcY, srcSurf,
                            dstX, dstY, dstSurf,
                            width, height,
                            comp, bgcolor, clip
                    );
                    return;
                }

                // Get translated clip
                makeClip(dstX, dstY, width, height, clip);

                g2d.setXClip(clip, g2d.imageGC);
                X11.getInstance().XCopyArea(
                        g2d.display,
                        xSrcSurf.g2d.drawable, g2d.drawable,
                        g2d.imageGC,
                        srcX, srcY,
                        width, height,
                        dstX, dstY
                );
                g2d.resetXClip(g2d.imageGC);

                g2d.setImageGCFunction(X11Defs.GXcopy);
            } else if (srcSurf.getSurfaceType() == BufferedImage.TYPE_CUSTOM) {
                // source is custom image, slow blit
                imBlit(
                        srcX, srcY, srcSurf,
                        dstX, dstY, dstSurf,
                        width, height,
                        comp, bgcolor, clip
                );
            } else { // source could be compatible image
                int srcTransp = srcCM.getTransparency();
                if (srcTransp == Transparency.OPAQUE) {
                    if (comp instanceof AlphaComposite) {
                        AlphaComposite acomp = (AlphaComposite) comp;
                        if (
                                acomp.getRule() == AlphaComposite.SRC ||
                                (acomp.getAlpha() == 1 &&
                                 (acomp.getRule() == AlphaComposite.SRC_OVER ||
                                  acomp.getRule() == AlphaComposite.SRC_ATOP ||
                                  acomp.getRule() == AlphaComposite.SRC_IN)
                                )
                        ) {
                            // Get translated clip
                            makeClip(dstX, dstY, width, height, clip);
                            xDstSurf.putImage(
                                    clip,
                                    srcSurf.getRaster(),
                                    dstX, dstY,
                                    width, height
                            );
                        } else {
                            imBlit(
                                srcX, srcY, srcSurf,
                                dstX, dstY, dstSurf,
                                width, height,
                                comp, bgcolor, clip
                            );
                        }
                    }

                } else if (srcTransp == Transparency.BITMASK) {
                    // todo - XXX - optimize here - use native clip mask
                    imBlit(
                        srcX, srcY, srcSurf,
                        dstX, dstY, dstSurf,
                        width, height,
                        comp, bgcolor, clip
                    );
                } else { // have to compose in java - no native alpha composite
                    imBlit(
                        srcX, srcY, srcSurf,
                        dstX, dstY, dstSurf,
                        width, height,
                        comp, bgcolor, clip
                    );
                }
            }
        } else {
            imBlit(
                    srcX, srcY, srcSurf,
                    dstX, dstY, dstSurf,
                    width, height,
                    comp, bgcolor, clip
            );
        }
    }

    private static void imBlit(
            int srcX, int srcY, Surface srcSurf,
            int dstX, int dstY, Surface dstSurf,
            int width, int height,
            Composite comp,
            Color bgcolor,
            MultiRectArea clip
    ) {
        XSurface xDstSurf = ((XSurface) dstSurf);
        boolean srcNoAlpha =
                srcSurf.getColorModel().getTransparency() == Transparency.OPAQUE;
        if (comp instanceof AlphaComposite) {
            AlphaComposite acomp = (AlphaComposite) comp;
            if (
                    acomp.getRule() == AlphaComposite.SRC ||
                    (srcNoAlpha && acomp.getAlpha() == 1 &&
                     (acomp.getRule() == AlphaComposite.SRC_OVER ||
                      acomp.getRule() == AlphaComposite.SRC_ATOP ||
                      acomp.getRule() == AlphaComposite.SRC_IN)
                    )
            ) {
                xDstSurf.needServerData = false;
            }
        }

        AffineTransform t = xDstSurf.g2d.getTransform();
        int tx = (int) t.getTranslateX();
        int ty = (int) t.getTranslateY();

        if (xDstSurf.needServerData) {
            xDstSurf.setRoi(new Rectangle2D.Float(dstX - tx, dstY - ty, width, height));
        }

        clip.translate(-tx, -ty);

        NativeImageBlitter.getInstance().blit(
                srcX, srcY, srcSurf,
                dstX - tx, dstY - ty, xDstSurf.getImageSurface(),
                width, height,
                comp, bgcolor, clip
        );

        clip.translate(tx, ty);

        // Get translated clip
        makeClip(dstX, dstY, width, height, clip);
        xDstSurf.putImage(clip);
        xDstSurf.needServerData = true;
    }

    private static void makeClip(
            int dstX, int dstY,
            int width, int height,
            MultiRectArea clip
    ) {
        Rectangle destRect = new Rectangle(dstX, dstY, width, height);
        clip.intersect(destRect);
    }
}
