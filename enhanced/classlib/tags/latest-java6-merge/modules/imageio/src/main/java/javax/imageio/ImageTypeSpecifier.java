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
 * @author Rustem V. Rafikov
 * @version $Revision: 1.3 $
 */
package javax.imageio;

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.color.ColorSpace;

import org.apache.harmony.luni.util.NotImplementedException;

/**
 * TODO implement all the methods
 */
public class ImageTypeSpecifier {
    
    protected ColorModel colorModel;
    protected SampleModel sampleModel;

    public ImageTypeSpecifier(ColorModel colorModel, SampleModel sampleModel) {
        if (colorModel == null) {
            throw new IllegalArgumentException("color model should not be NULL");
        }
        if (sampleModel == null) {
            throw new IllegalArgumentException("sample model should not be NULL");
        }
        if (!colorModel.isCompatibleSampleModel(sampleModel)) {
            throw new IllegalArgumentException("color and sample models are not compatible");
        }

        this.colorModel = colorModel;
        this.sampleModel = sampleModel;
    }

    public ImageTypeSpecifier(RenderedImage renderedImage) {
        if (renderedImage == null) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        this.colorModel = renderedImage.getColorModel();
        this.sampleModel = renderedImage.getSampleModel();
    }

    public static ImageTypeSpecifier createPacked(ColorSpace colorSpace,
                                                  int redMask,
                                                  int greenMask,
                                                  int blueMask,
                                                  int alphaMask,
                                                  int transferType,
                                                  boolean isAlphaPremultiplied) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createInterleaved(ColorSpace colorSpace,
                                                       int[] bandOffsets,
                                                       int dataType,
                                                       boolean hasAlpha,
                                                       boolean isAlphaPremultiplied) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }


    public static ImageTypeSpecifier createBanded(ColorSpace colorSpace,
                                                  int[] bankIndices,
                                                  int[] bandOffsets,
                                                  int dataType,
                                                  boolean hasAlpha,
                                                  boolean isAlphaPremultiplied) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createGrayscale(int bits,
                                                     int dataType,
                                                     boolean isSigned) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createGrayscale(int bits,
                                                     int dataType,
                                                     boolean isSigned,
                                                     boolean isAlphaPremultiplied) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createIndexed(byte[] redLUT,
                                                   byte[] greenLUT,
                                                   byte[] blueLUT,
                                                   byte[] alphaLUT,
                                                   int bits,
                                                   int dataType) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createFromBufferedImageType(int bufferedImageType) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public static ImageTypeSpecifier createFromRenderedImage(RenderedImage image) {
        if (null == image) {
            throw new IllegalArgumentException("image should not be NULL");
        }
        return new ImageTypeSpecifier(image);
    }

    public int getBufferedImageType() throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    public int getNumComponents() {
        return colorModel.getNumComponents();
    }

    public int getNumBands() {
        return sampleModel.getNumBands();
    }

    public int getBitsPerBand(int band) {
        if (band < 0 || band >= getNumBands()) {
            throw new IllegalArgumentException();
        }
        return sampleModel.getSampleSize(band);
    }

    public SampleModel getSampleModel() {
        return sampleModel;
    }

    public SampleModel getSampleModel(int width, int height) {
        if ((long)width*height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("width * height > Integer.MAX_VALUE");
        }
        return sampleModel.createCompatibleSampleModel(width, height);
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public BufferedImage createBufferedImage(int width, int height) throws NotImplementedException {
        // TODO: implement
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(Object o) {
        boolean rt = false;
        if (o instanceof ImageTypeSpecifier) {
            ImageTypeSpecifier ts = (ImageTypeSpecifier) o;
            rt = colorModel.equals(ts.colorModel) && sampleModel.equals(ts.sampleModel);
        }
        return rt;
    }
}