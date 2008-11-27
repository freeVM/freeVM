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
package org.apache.harmony.pack200;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

/**
 * Wrapper for ClassReader than enables pack200 to obtain extra class file
 * information
 */
public class Pack200ClassReader extends ClassReader {

    private boolean lastConstantHadWideIndex;
    private int lastUnsignedShort;
    private static boolean anySyntheticAttributes;

    /**
     * @param b
     */
    public Pack200ClassReader(byte[] b) {
        super(b);
    }

    /**
     * @param is
     * @throws IOException
     */
    public Pack200ClassReader(InputStream is) throws IOException {
        super(is);
    }

    /**
     * @param name
     * @throws IOException
     */
    public Pack200ClassReader(String name) throws IOException {
        super(name);
    }

    public int readUnsignedShort(int index) {
        // Doing this to check whether last load-constant instruction was ldc (18) or ldc_w (19)
        // TODO:  Assess whether this impacts on performance
        int unsignedShort = super.readUnsignedShort(index);
        if(b[index - 1] == 19) {
            lastUnsignedShort = unsignedShort;
        } else {
            lastUnsignedShort = Short.MIN_VALUE;
        }
        return unsignedShort;
    }

    public Object readConst(int item, char[] buf) {
        lastConstantHadWideIndex = item == lastUnsignedShort;
        return super.readConst(item, buf);
    }

    public String readUTF8(int arg0, char[] arg1) {
        String utf8 = super.readUTF8(arg0, arg1);
        if(!anySyntheticAttributes && utf8.equals("Synthetic")) {
            anySyntheticAttributes = true;
        }
        return utf8;
    }

    /**
     * @param b
     * @param off
     * @param len
     */
    public Pack200ClassReader(byte[] b, int off, int len) {
        super(b, off, len);
    }

    public boolean lastConstantHadWideIndex() {
        return lastConstantHadWideIndex;
    }

    public static boolean hasSyntheticAttributes() {
        return anySyntheticAttributes;
    }

    public static void resetSyntheticCounter() {
        anySyntheticAttributes = false;
    }

}
