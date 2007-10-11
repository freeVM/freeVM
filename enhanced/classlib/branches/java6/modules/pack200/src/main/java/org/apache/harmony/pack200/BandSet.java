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
import java.io.OutputStream;

public abstract class BandSet {
    
    public abstract void unpack(InputStream inputStream) throws IOException, Pack200Exception;
    
    public abstract void pack(OutputStream outputStream);
    
    protected Segment segment;
    
    protected SegmentHeader header;

    public BandSet(Segment segment) {
        this.segment = segment;
        this.header = segment.getSegmentHeader();
    }

    /**
     * Decode a band and return an array of <code>int[]</code> values
     * 
     * @param name
     *            the name of the band (primarily for logging/debugging
     *            purposes)
     * @param in
     *            the InputStream to decode from
     * @param defaultCodec
     *            the default codec for this band
     * @param count
     *            the number of elements to read
     * @return an array of decoded <code>int[]</code> values
     * @throws IOException
     *             if there is a problem reading from the underlying input
     *             stream
     * @throws Pack200Exception
     *             if there is a problem decoding the value or that the value is
     *             invalid
     */
    public int[] decodeBandInt(String name, InputStream in,
            BHSDCodec defaultCodec, int count) throws IOException,
            Pack200Exception {
        // TODO Might be able to improve this directly.
        int[] result = new int[count];

        // TODO We need to muck around in the scenario where the first value
        // read indicates
        // an uber-codec
        long[] longResult = decodeBandLong(name, in, defaultCodec, count);
        for (int i = 0; i < count; i++) {
            result[i] = (int) longResult[i];
        }
        return result;
    }
    
    public int[][] decodeBandInt(String name, InputStream in, BHSDCodec defaultCodec, int[] counts) throws IOException, Pack200Exception {
        int[][] result = new int[counts.length][];
        int totalCount = 0;
        for (int i = 0; i < counts.length; i++) {
            totalCount += counts[i];
        }
        int[] twoDResult = decodeBandInt(name, in, defaultCodec, totalCount);
        int index = 0;
        for (int i = 0; i < result.length; i++) {
            result[i] = new int[counts[i]];
            for(int j = 0; j < result[i].length; j++) {
                result[i][j] = twoDResult[index];
                index++;
            }
        }
        return result;
    }
    
    /**
     * Decode a band and return an array of <code>long[]</code> values
     * 
     * @param name
     *            the name of the band (primarily for logging/debugging
     *            purposes)
     * @param in
     *            the InputStream to decode from
     * @param codec
     *            the default codec for this band
     * @param count
     *            the number of elements to read
     * @return an array of decoded <code>long[]</code> values
     * @throws IOException
     *             if there is a problem reading from the underlying input
     *             stream
     * @throws Pack200Exception
     *             if there is a problem decoding the value or that the value is
     *             invalid
     */
    public long[] decodeBandLong(String name, InputStream in, BHSDCodec codec,
            int count) throws IOException, Pack200Exception {
        if (codec.getB() == 1 || count == 0) {
            return codec.decode(count, in);
        }
        long[] getFirst = codec.decode(1, in);
        if (getFirst.length == 0) {
            return getFirst;
        }
        long first = getFirst[0];
        if (codec.isSigned() && first >= -256 && first <= -1) {
            // Non-default codec should be used
            Codec nonDefaultCodec = CodecEncoding.getCodec((int) (-1 - first),
                    header.getBandHeadersInputStream(), codec);
            return nonDefaultCodec.decode(count, in);
        } else if (!codec.isSigned() && first >= codec.getL()
                && first <= codec.getL() + 255) {
            // Non-default codec should be used
            Codec nonDefaultCodec = CodecEncoding.getCodec((int) first
                    - codec.getL(), header.getBandHeadersInputStream(), codec);
            return nonDefaultCodec.decode(count, in);
        } else {
            // First element should not be discarded
            return codec.decode(count - 1, in, first);
        }
    }
    
    public long[] parseFlags(String name, InputStream in, int count,
            BHSDCodec codec, boolean hasHi) throws IOException, Pack200Exception {
        return parseFlags(name, in, 1, new int[] { count }, (hasHi ? codec
                : null), codec)[0];
    }

    public long[][] parseFlags(String name, InputStream in, int count,
            int counts[], BHSDCodec codec, boolean hasHi) throws IOException,
            Pack200Exception {
        return parseFlags(name, in, count, counts, (hasHi ? codec : null),
                codec);
    }

    public long[][] parseFlags(String name, InputStream in, int count,
            int counts[], BHSDCodec hiCodec, BHSDCodec loCodec) throws IOException,
            Pack200Exception {
        // TODO Move away from decoding into a parseBand type structure
        if (count == 0) {
            return new long[][] { {} };
        }
        long[][] result = new long[count][];
        for (int j = 0; j < count; j++) {
            int[] hi;
            if(hiCodec != null) {
                hi = decodeBandInt(name, in, hiCodec, counts[j]);
                result[j] = decodeBandLong(name, in, loCodec, counts[j]);
                for (int i = 0; i < counts[j]; i++) {                    
                    result[j][i] = (hi[i] << 32) |result[j][i];
                }
            } else {
                result[j] = decodeBandLong(name, in, loCodec, counts[j]);
            }
        }
        // TODO Remove debugging code
        debug("Parsed *" + name + " (" + result.length + ")");
        return result;        
    }
    
    /**
     * Helper method to parse <i>count</i> references from <code>in</code>,
     * using <code>codec</code> to decode the values as indexes into
     * <code>reference</code> (which is populated prior to this call). An
     * exception is thrown if a decoded index falls outside the range
     * [0..reference.length-1].
     * 
     * @param name
     *            TODO
     * @param in
     *            the input stream to read from
     * @param codec
     *            the codec to use for decoding
     * @param count
     *            the number of references to decode
     * @param reference
     *            the array of values to use for the indexes; often
     *            {@link #cpUTF8}
     * 
     * @throws IOException
     *             if a problem occurs during reading from the underlying stream
     * @throws Pack200Exception
     *             if a problem occurs with an unexpected value or unsupported
     *             codec
     */
    public String[] parseReferences(String name, InputStream in,
            BHSDCodec codec, int count, String[] reference) throws IOException,
            Pack200Exception {
        return parseReferences(name, in, codec, new int[] { count },
                reference)[0];
    }
    
    /**
     * Helper method to parse <i>count</i> references from <code>in</code>,
     * using <code>codec</code> to decode the values as indexes into
     * <code>reference</code> (which is populated prior to this call). An
     * exception is thrown if a decoded index falls outside the range
     * [0..reference.length-1]. Unlike the other parseReferences, this
     * post-processes the result into an array of results.
     * 
     * @param name
     *            TODO
     * @param in
     *            the input stream to read from
     * @param codec
     *            the codec to use for decoding
     * @param count
     *            the number of references to decode
     * @param reference
     *            the array of values to use for the indexes; often
     *            {@link #cpUTF8}
     * 
     * @throws IOException
     *             if a problem occurs during reading from the underlying stream
     * @throws Pack200Exception
     *             if a problem occurs with an unexpected value or unsupported
     *             codec
     */
    public String[][] parseReferences(String name, InputStream in,
            BHSDCodec codec, int counts[], String[] reference)
            throws IOException, Pack200Exception {
        int count = counts.length;
        if (count == 0) {
            return new String[][] { {} };
        }
        String[][] result = new String[count][];
        int sum = 0;
        for (int i = 0; i < count; i++) {
            result[i] = new String[counts[i]];
            sum += counts[i];
        }
        // TODO Merge the decode and parsing of a multiple structure into one
        String[] result1 = new String[sum];
        int[] decode = decodeBandInt(name, in, codec, sum);
        for (int i1 = 0; i1 < sum; i1++) {
            int index = decode[i1];
            if (index < 0 || index >= reference.length)
                throw new Pack200Exception(
                        "Something has gone wrong during parsing references");
            result1[i1] = reference[index];
        }
        String[] refs = result1;
        // TODO Merge the decode and parsing of a multiple structure into one
        int pos = 0;
        for (int i = 0; i < count; i++) {
            int num = counts[i];
            result[i] = new String[num];
            System.arraycopy(refs, pos, result[i], 0, num);
            pos += num;
        }
        return result;
    }

    /**
     * This is a local debugging message to aid the developer in writing this
     * class. It will be removed before going into production. If the property
     * 'debug.pack200' is set, this will generate messages to stderr; otherwise,
     * it will be silent.
     * 
     * @param message
     * @deprecated this should be removed from production code
     */
    protected void debug(String message) {
        segment.debug(message);
    }


}
