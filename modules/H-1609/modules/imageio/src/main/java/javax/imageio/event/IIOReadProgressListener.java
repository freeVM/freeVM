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
 * @author Sergey I. Salishev
 * @version $Revision: 1.2 $
 */
package javax.imageio.event;

import java.util.EventListener;
import javax.imageio.ImageReader;

/**
 * @author Sergey I. Salishev
 * @version $Revision: 1.2 $
 */
public interface IIOReadProgressListener extends EventListener {

    void imageComplete(ImageReader source);
    void imageProgress(ImageReader source, float percentageDone);
    void imageStarted(ImageReader source, int imageIndex);
    void readAborted(ImageReader source);
    void sequenceComplete(ImageReader source);
    void sequenceStarted(ImageReader source, int minIndex);
    void thumbnailComplete(ImageReader source);
    void thumbnailProgress(ImageReader source, float percentageDone);
    void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex);
}

