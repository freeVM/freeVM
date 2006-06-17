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
 * @author Igor V. Stolyarov
 * @version $Revision$
 */
package java.awt.image;

public class PixelInterleavedSampleModel extends ComponentSampleModel {

    public PixelInterleavedSampleModel(int dataType, int w, int h,
            int pixelStride, int scanlineStride, int bandOffsets[]) {

        super(dataType, w, h, pixelStride, scanlineStride, bandOffsets);

        int maxOffset = bandOffsets[0];
        int minOffset = bandOffsets[0];
        for (int i = 1; i < bandOffsets.length; i++) {
            if (bandOffsets[i] > maxOffset)
                maxOffset = bandOffsets[i];
            if (bandOffsets[i] < minOffset)
                minOffset = bandOffsets[i];
        }

        maxOffset -= minOffset;

        if (maxOffset > scanlineStride)
            throw new IllegalArgumentException("Any offset between bands " +
                    "is greater than the Scanline stride");

        if (maxOffset > pixelStride)
            throw new IllegalArgumentException("Pixel stride is less than " +
                    "any offset between bands");

        if (pixelStride * w > scanlineStride)
            throw new IllegalArgumentException("Product of Pixel stride and " +
                    "w is greater than Scanline stride ");

    }

    public SampleModel createSubsetSampleModel(int bands[]) {
        int newOffsets[] = new int[bands.length];
        for (int i = 0; i < bands.length; i++)
            newOffsets[i] = bandOffsets[bands[i]];

        return new PixelInterleavedSampleModel(dataType, width, height,
                pixelStride, scanlineStride, newOffsets);
    }

    public SampleModel createCompatibleSampleModel(int w, int h) {
        int newOffsets[];
        int minOffset = bandOffsets[0];

        for (int i = 1; i < numBands; i++)
            if (bandOffsets[i] < minOffset)
                minOffset = bandOffsets[i];

        if (minOffset > 0) {
            newOffsets = new int[numBands];
            for (int i = 0; i < numBands; i++)
                newOffsets[i] = bandOffsets[i] - minOffset;
        } else {
            newOffsets = bandOffsets;
        }

        return new PixelInterleavedSampleModel(dataType, w, h, pixelStride,
                pixelStride * w, newOffsets);
    }

    public int hashCode() {
        int hash = super.hashCode();
        int tmp = hash >>> 8;
        hash <<= 8;
        hash |= tmp;

        return hash ^ 0x66;
    }

}

