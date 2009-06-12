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
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.harmony.pack200.Archive.File;
import org.objectweb.asm.ClassReader;

/**
 * Bands containing information about files in the pack200 archive and the file
 * contents for non-class-files. Corresponds to the <code>file_bands</code> set
 * of bands described in the specification.
 */
public class FileBands extends BandSet {

    private final CPUTF8[] fileName;
    private int[] file_name;
    private final int[] file_modtime;
    private final long[] file_size;
    private final int[] file_options;
    private final byte[][] file_bits;

    public FileBands(CpBands cpBands, SegmentHeader segmentHeader,
            List files, List classes, int effort) {
        super(effort, segmentHeader);
        int size =  files.size();
        fileName = new CPUTF8[size];
        file_modtime = new int[size];
        file_size = new long[size];
        file_options = new int[size];
        int totalSize = 0;
        file_bits = new byte[files.size()][];
        int archiveModtime = segmentHeader.getArchive_modtime();

        Set classNames = new HashSet();
        for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
            ClassReader reader = (ClassReader) iterator.next();
            classNames.add(reader.getClassName());
        }
        CPUTF8 emptyString = cpBands.getCPUtf8("");
        for (int i = 0; i < files.size(); i++) {
             File file = (File)files.get(i);
             String name = file.getName();
             if(name.endsWith(".class")) {
                 file_options[i] |= (1 << 1);
                 if(classNames.contains(name.substring(0, name.length() - 6))) {
                     fileName[i] = emptyString;
                 } else {
                     fileName[i] = cpBands.getCPUtf8(name);
                 }
             } else {
                 fileName[i] = cpBands.getCPUtf8(name);
             }
             byte[] bytes = file.getContents();
             file_size[i] = bytes.length;
             totalSize += file_size[i];
             file_modtime[i] = (int)(file.getModtime() - archiveModtime);
             file_bits[i] = file.getContents();
         }
    }

    /**
     * All input classes for the segment have now been read in, so this method
     * is called so that this class can calculate/complete anything it could not
     * do while classes were being read.
     */
    public void finaliseBands() {
        file_name = new int[fileName.length];
        for (int i = 0; i < file_name.length; i++) {
            file_name[i] = fileName[i].getIndex();
        }
    }

    public void pack(OutputStream out) throws IOException, Pack200Exception {
        out.write(encodeBandInt("file_name", file_name, Codec.UNSIGNED5));
        out.write(encodeFlags("file_size", file_size, Codec.UNSIGNED5,
                Codec.UNSIGNED5, segmentHeader.have_file_size_hi()));
        if (segmentHeader.have_file_modtime()) {
            out.write(encodeBandInt("file_modtime", file_modtime, Codec.DELTA5));
        }
        if (segmentHeader.have_file_options()) {
            out.write(encodeBandInt("file_options", file_options,
                    Codec.UNSIGNED5));
        }
        out.write(encodeBandInt("file_bits", flatten(file_bits), Codec.BYTE1));
    }

    private int[] flatten(byte[][] bytes) {
        int total = 0;
        for (int i = 0; i < bytes.length; i++) {
            total += bytes[i].length;
        }
        int[] band = new int[total];
        int index = 0;
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < bytes[i].length; j++) {
                band[index++] = bytes[i][j] & 0xFF;
            }
        }
        return band;
    }

}
