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

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

/**
 * 
 * 
 * @author Diego Ra�l Mercado
 * @version 1.2
 * @ar.org.fitc.spec_ref
 */
public class DHParameterSpec implements AlgorithmParameterSpec { 
    private BigInteger p;

    private BigInteger g;

    private int l;
    
    /** @ar.org.fitc.spec_ref */
    public DHParameterSpec(BigInteger p, BigInteger g) {
        this.p = p;
        this.g = g;
    }

    /** @ar.org.fitc.spec_ref */
    public DHParameterSpec(BigInteger p, BigInteger g, int l) {
        this.p = p;
        this.g = g;
        this.l = l;
    }

    /** @ar.org.fitc.spec_ref */
    public BigInteger getG() {
        return g;
    }
    
    /** @ar.org.fitc.spec_ref */
    public int getL() {
        return l;
    }
    
    /** @ar.org.fitc.spec_ref */
    public BigInteger getP() {
        return p;
    }
}
