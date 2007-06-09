/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */    

/*
 * @author Alexander V. Esin
 * @version $Revision: 1.8 $
 * Created on 29.11.2005
 * 
 */
package org.apache.harmony.test.stress.gc.frag.share;

import junit.framework.TestCase;
import org.apache.harmony.test.share.stress.ReliabilityRunner;

/**
 * FragmentationAbstractTest [maxmem] [maxmem_factor] [blob_size] [period]
 * [min_blob_size]
 */
public abstract class FragmentationAbstractTest extends TestCase {

    // number of iterations
    public final static int NUM = 1048576; // 1 M

    // 2-dimensional array, which keeps references to byte arrays
    public byte[][] ref = new byte[NUM][];

    public int MAX_MEM = 0;

    public double FACTOR = 1.0;

    public int PERIOD = 0;

    public int BLOB_SIZE = 0;

    public int MIN_BLOB_SIZE = 1;

    protected int count = 0;

    transient protected int totalSize = 0;

    public abstract void perform();

    public void test() {

        MAX_MEM = Integer.getInteger(
                "org.apache.harmony.test."
                        + "stress.gc.frag.share."
                        + "FragmentationAbstractTest.MAX_MEM", 536870912)
                .intValue();

        try {
            FACTOR = Double.parseDouble(System
                    .getProperty("org.apache.harmony.test."
                            + "stress.gc.frag.share."
                            + "FragmentationAbstractTest.FACTOR"));
        } catch (Exception exc) {
            FACTOR = 1.0;
        }

        MAX_MEM = (int) (MAX_MEM * FACTOR);

        BLOB_SIZE = Integer.getInteger(
                "org.apache.harmony.test."
                        + "stress.gc.frag.share."
                        + "FragmentationAbstractTest.BLOB_SIZE", 65536)
                .intValue();
        
        PERIOD = Integer.getInteger(
                "org.apache.harmony.test."
                        + "stress.gc.frag.share."
                        + "FragmentationAbstractTest.PERIOD", 25000)
                .intValue();

        MIN_BLOB_SIZE = Integer.getInteger(
                "org.apache.harmony.test."
                        + "stress.gc.frag.share."
                        + "FragmentationAbstractTest.MIN_BLOB_SIZE", 1)
                .intValue();

        ReliabilityRunner.debug("MAX_MEM=" + MAX_MEM + ", FACTOR="
                + FACTOR + ", PERIOD=" + PERIOD + ", BLOB_SIZE=" + BLOB_SIZE
                + ", MIN_BLOB_SIZE=" + MIN_BLOB_SIZE);
        perform();
    }
}
