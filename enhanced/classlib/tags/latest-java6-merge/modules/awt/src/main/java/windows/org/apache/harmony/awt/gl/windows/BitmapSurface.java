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
 * @author Igor V. Stolyarov
 * @version $Revision$
 * Created on 19.12.2005
 *
 */
package org.apache.harmony.awt.gl.windows;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.harmony.awt.gl.Surface;


public class BitmapSurface extends Surface {

    public BitmapSurface(long gi, int width, int height){
        surfaceDataPtr = createSurfData(gi, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void dispose() {
        dispose(surfaceDataPtr);
        surfaceDataPtr = 0L;
    }

    private native long createSurfData(long gi, int width, int height);

    private native void dispose(long structPtr);

    @Override
    public long lock() {
        return 0;
    }

    @Override
    public void unlock() {
    }

    @Override
    public ColorModel getColorModel() {
        return null;
    }

    @Override
    public WritableRaster getRaster() {
        return null;
    }

    @Override
    public int getSurfaceType() {
        return 0;
    }

    @Override
    public Surface getImageSurface() {
        return this;
    }

}
