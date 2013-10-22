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

import java.security.spec.AlgorithmParameterSpec;

/**
 * 
 * 
 * @author Diego Ra�l Mercado
 * @version 1.2
 * @ar.org.fitc.spec_ref
 */
public class IvParameterSpec implements AlgorithmParameterSpec { 
	private byte[] iv;

    /** @ar.org.fitc.spec_ref */
    public IvParameterSpec(byte[] iv) {
        this(iv, 0, iv.length);
    }

    /** @ar.org.fitc.spec_ref */
    public IvParameterSpec(byte[] iv, int offset, int len) {
        if (iv == null || (iv.length - offset) < len) {
            throw new IllegalArgumentException(
                    "IV missing or invalid offset/length combination");
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("len is negative");
        }
        this.iv = new byte[len];
        System.arraycopy(iv, offset, this.iv, 0, len);
    }

    /** @ar.org.fitc.spec_ref */
    public byte[] getIV() {
        return iv.clone();
    }
}