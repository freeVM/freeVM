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
package javax.imageio.stream;

/**
 * @author Sergey I. Salishev
 * @version $Revision: 1.2 $
 */
public class IIOByteBuffer {
    
    private byte[] data;
    private int offset;
    private int length;

    public IIOByteBuffer(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}

