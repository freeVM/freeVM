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
package org.apache.harmony.pack200.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.harmony.pack200.BHSDCodec;
import org.apache.harmony.pack200.CodecEncoding;
import org.apache.harmony.pack200.Pack200Exception;

/**
 * Tests for BHSDCodec
 */
public class BHSDCodecTest extends TestCase {

    
    public void testEncodeDecode() throws IOException, Pack200Exception {
        for (int i = 1; i < 116; i++) {
            
            BHSDCodec codec = (BHSDCodec) CodecEncoding.getCodec(i, null, null);
            
            // Test encode-decode with a selection of numbers within the range of the codec
            long delta = (codec.largest() - codec.smallest()) / 4;
            for (long j = codec.smallest(); j <= codec.largest() + 1; j += delta) {
                byte[] encoded = codec.encode(j, 0);
                long decoded = codec.decode(new ByteArrayInputStream(encoded),
                        0);
                if (j != decoded) {
                    fail("Failed with codec: " + codec + " expected: " + j
                            + ", got: " + decoded);
                }
            }
            
            // Test encode-decode with 0            
            assertEquals(0, codec.decode(new ByteArrayInputStream(codec.encode(
                    0, 0)), 0));
        }
    }
    
}