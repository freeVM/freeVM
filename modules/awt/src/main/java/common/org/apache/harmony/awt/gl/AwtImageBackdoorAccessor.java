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
 * Created on 23.11.2005
 *
 */


package org.apache.harmony.awt.gl;

import java.awt.Image;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/**
 * This class give an opportunity to get access to private data of 
 * some java.awt.image classes 
 * Implementation of this class placed in java.awt.image package
 */
public abstract class AwtImageBackdoorAccessor {

    static protected AwtImageBackdoorAccessor inst;

    public static AwtImageBackdoorAccessor getInstance(){
        return inst;
    }

    public abstract Surface getImageSurface(Image image);
    public abstract boolean isGrayPallete(IndexColorModel icm);

    public abstract Object getData(DataBuffer db);
    public abstract int[] getDataInt(DataBuffer db);
    public abstract byte[] getDataByte(DataBuffer db);
    public abstract short[] getDataShort(DataBuffer db);
    public abstract short[] getDataUShort(DataBuffer db);
    public abstract double[] getDataDouble(DataBuffer db);
    public abstract float[] getDataFloat(DataBuffer db);
}
