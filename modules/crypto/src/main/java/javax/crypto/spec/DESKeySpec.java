/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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

package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

/**
 * @com.intel.drl.spec_ref
 */
public class DESKeySpec implements KeySpec {

    /**
     * @com.intel.drl.spec_ref
     */
    public static final int DES_KEY_LEN = 8;

    private final byte[] key;

    // DES weak and semi-weak keys
    // Got from:
    // FIP PUB 74
    // FEDERAL INFORMATION PROCESSING STANDARDS PUBLICATION 1981
    // GUIDELINES FOR IMPLEMENTING AND USING THE NBS DATA ENCRYPTION STANDARD 
    // http://www.dice.ucl.ac.be/crypto/standards/fips/fip74/fip74-1.pdf
    private static final byte[][] SEMIWEAKS = {
                {(byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01,
                 (byte) 0xF1, (byte) 0x01, (byte) 0xF1, (byte) 0x01},

                {(byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xE0,
                 (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1},

                {(byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x1F,
                 (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE,
                 (byte) 0x0E, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE},

                {(byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x1F,
                 (byte) 0xF1, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xE0,
                 (byte) 0x0E, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1},

                {(byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE,
                 (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE},

                {(byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01,
                 (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01},

                {(byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x1F,
                 (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E},

                {(byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01,
                 (byte) 0x0E, (byte) 0x01, (byte) 0x0E, (byte) 0x01},

                {(byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE,
                 (byte) 0xF1, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE},

                {(byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0,
                 (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1},

                {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, 
                 (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01},

                {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                 (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE},

                {(byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xE0,
                 (byte) 0xF1, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1},

                {(byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x1F,
                 (byte) 0x0E, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E},

                };

    /**
     * @com.intel.drl.spec_ref
     */
    public DESKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public DESKeySpec(byte[] key, int offset)
                throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("Key material is null.");
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    "The key material is shorter than 8 bytes");
        }
        this.key = new byte[DES_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, DES_KEY_LEN);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public byte[] getKey() {
        byte[] result = new byte[DES_KEY_LEN];
        System.arraycopy(this.key, 0, result, 0, DES_KEY_LEN);
        return result;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static boolean isParityAdjusted(byte[] key, int offset)
            throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key material is null.");
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    "The key material is shorter than 8 bytes");
        }

        int byteKey = 0;

        for (int i = offset; i < DES_KEY_LEN; i++) {
            byteKey = key[i];

            byteKey ^= byteKey >> 1;
            byteKey ^= byteKey >> 2;
            byteKey ^= byteKey >> 4;

            if ((byteKey & 1) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static boolean isWeak(byte[] key, int offset)
              throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key material is null.");
        }
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException(
                    "The key material is shorter than 8 bytes");
        }
        I:
        for (int i=0; i<SEMIWEAKS.length; i++) {
            for (int j=0; j<DES_KEY_LEN; j++) {
                if (SEMIWEAKS[i][j] != key[offset+j]) {
                    continue I;
                }
            }
            return true;
        }
        return false;
    }
}

