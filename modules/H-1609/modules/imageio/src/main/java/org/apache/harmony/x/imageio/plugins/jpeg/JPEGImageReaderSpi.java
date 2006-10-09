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
 * @author Rustem V. Rafikov
 * @version $Revision: 1.3 $
 */
package org.apache.harmony.x.imageio.plugins.jpeg;


import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.harmony.awt.gl.image.DecodingImageSource;
import org.apache.harmony.awt.gl.image.JpegDecoder;
import org.apache.harmony.awt.gl.image.OffscreenImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class JPEGImageReaderSpi extends ImageReaderSpi {

    public JPEGImageReaderSpi() {
        super(JPEGSpiConsts.vendorName, JPEGSpiConsts.version,
                JPEGSpiConsts.names, JPEGSpiConsts.suffixes,
                JPEGSpiConsts.MIMETypes, JPEGSpiConsts.readerClassName,
                STANDARD_INPUT_TYPE, JPEGSpiConsts.writerSpiNames,
                JPEGSpiConsts.supportsStandardStreamMetadataFormat,
                JPEGSpiConsts.nativeStreamMetadataFormatName,
                JPEGSpiConsts.nativeStreamMetadataFormatClassName,
                JPEGSpiConsts.extraStreamMetadataFormatNames,
                JPEGSpiConsts.extraStreamMetadataFormatClassNames,
                JPEGSpiConsts.supportsStandardImageMetadataFormat,
                JPEGSpiConsts.nativeImageMetadataFormatName,
                JPEGSpiConsts.nativeImageMetadataFormatClassName,
                JPEGSpiConsts.extraImageMetadataFormatNames,
                JPEGSpiConsts.extraImageMetadataFormatClassNames);
    }


    public boolean canDecodeInput(Object source) throws IOException {
        ImageInputStream markable = (ImageInputStream) source;
        try {
            markable.mark();

            byte[] signature = new byte[3];
            markable.seek(0);
            markable.read(signature, 0, 3);
            markable.reset();

            if ((signature[0] & 0xFF) == 0xFF &&
                    (signature[1] & 0xFF) == JPEGConsts.SOI &&
                    (signature[2] & 0xFF) == 0xFF) { // JPEG
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new JPEGImageReader(this);
    }

    public String getDescription(Locale locale) {
        return "DRL JPEG decoder";
    }

    public void onRegistration(ServiceRegistry registry, Class category) {
        // super.onRegistration(registry, category);
    }
}
