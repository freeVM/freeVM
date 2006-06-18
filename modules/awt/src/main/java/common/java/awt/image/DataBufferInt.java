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

public final class DataBufferInt extends DataBuffer {

    int data[][];

    public DataBufferInt(int dataArrays[][], int size, int offsets[]) {
        super(TYPE_INT, size, dataArrays.length, offsets);
        data = (int[][]) dataArrays.clone();
    }

    public DataBufferInt(int dataArrays[][], int size) {
        super(TYPE_INT, size, dataArrays.length);
        data = (int[][]) dataArrays.clone();
    }

    public DataBufferInt(int dataArray[], int size, int offset) {
        super(TYPE_INT, size, 1, offset);
        data = new int[1][];
        data[0] = dataArray;
    }

    public DataBufferInt(int dataArray[], int size) {
        super(TYPE_INT, size);
        data = new int[1][];
        data[0] = dataArray;
    }

    public DataBufferInt(int size, int numBanks) {
        super(TYPE_INT, size, numBanks);
        data = new int[numBanks][];
        int i = 0;
        while (i < numBanks) {
            data[i++] = new int[size];
        }
    }

    public DataBufferInt(int size) {
        super(TYPE_INT, size);
        data = new int[1][];
        data[0] = new int[size];
    }

    public void setElem(int bank, int i, int val) {
        data[bank][offsets[bank] + i] = val;
    }

    public void setElem(int i, int val) {
        data[0][offset + i] = val;
    }

    public int getElem(int bank, int i) {
        return data[bank][offsets[bank] + i];
    }

    public int[] getData(int bank) {
        return data[bank];
    }

    public int getElem(int i) {
        return data[0][offset + i];
    }

    public int[][] getBankData() {
        return (int[][]) data.clone();
    }

    public int[] getData() {
        return data[0];
    }
}

