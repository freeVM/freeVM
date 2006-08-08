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

/**
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * @com.intel.drl.spec_ref
 */
public class RC5ParameterSpec implements AlgorithmParameterSpec {

    private final int version;
    private final int rounds;
    private final int wordSize;
    private final byte[] iv;

    /**
     * @com.intel.drl.spec_ref
     */
    public RC5ParameterSpec(int version, int rounds, int wordSize) {
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv) {
        if (iv == null) {
            throw new IllegalArgumentException("iv is null");
        }
        if (iv.length < 2 * (wordSize / 8)) {
            throw new IllegalArgumentException(
                    "iv.length < 2 * (wordSize / 8)");
        }
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = new byte[2*(wordSize/8)];
        System.arraycopy(iv, 0, this.iv, 0, 2*(wordSize/8));
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public RC5ParameterSpec(int version, int rounds,
                                int wordSize, byte[] iv, int offset) {
        if (iv == null) {
            throw new IllegalArgumentException("iv is null");
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset is negative");
        }
        if (iv.length - offset < 2 * (wordSize / 8)) {
            throw new IllegalArgumentException(
                    "iv.length - offset < 2 * (wordSize / 8)");
        }
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        this.iv = new byte[offset+2*(wordSize/8)];
        System.arraycopy(iv, offset, this.iv, 0, 2*(wordSize/8));
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getVersion() {
        return version;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getWordSize() {
        return wordSize;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public byte[] getIV() {
        if (iv == null) {
            return null;
        }
        byte[] result = new byte[iv.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        return result;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC5ParameterSpec)) {
            return false;
        }
        RC5ParameterSpec ps = (RC5ParameterSpec) obj;
        return (version == ps.version)
            && (rounds == ps.rounds)
            && (wordSize == ps.wordSize)
            && (Arrays.equals(iv, ps.iv));
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        int result = version + rounds + wordSize;
        if (iv == null) {
            return result;
        }
        for (int i=0; i<iv.length; i++) {
            result += iv[i] & 0xFF;
        }
        return result;
    }
}

