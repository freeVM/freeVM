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
 * @version $Revision: 1.5 $
 * Created on 29.11.2005
 * 
 */
package org.apache.harmony.test.stress.gc.frag.share;

import java.lang.ref.*;
import org.apache.harmony.test.share.stress.ReliabilityRunner;

public class FragmentationSoftRefTest extends FragmentationAbstractTest {

    public static final int SOFTREF_PORTION = 8;

    public static final int SOFTREF_COUNT = 2048;

    public void perform() {
        SoftReference[] sr = new SoftReference[SOFTREF_COUNT];
        int i = 0;
        int indexSR = 0;
        try {
            for (; i < NUM; ++i) {
                // size varies from MIN_BLOB_SIZE to (BLOB_SIZE + MIN_BLOB_SIZE)
                int size = ((i + BLOB_SIZE) % BLOB_SIZE) + MIN_BLOB_SIZE;
                while (totalSize + size > MAX_MEM) {
                    size = size / 2;
                    if (size < MIN_BLOB_SIZE)
                        break;
                }
                if (size >= MIN_BLOB_SIZE) {
                    ref[i] = new byte[size];
                    totalSize += size;
                }
                count++;
                // every PERIOD step perform clearing of arrays
                if (count > PERIOD) {
                    // clearing of arrays is perfromed with the step,
                    // whcih varies from 2 to 13, so, the heap becomes
                    // fragmented
                    int step = (i % 13) + 2;

                    int start = (i % PERIOD) % 2; // only 0 or 1
                    // System.out.println("step = " + step + " start = " +
                    // start);
                    for (int j = start; j < i; j += step) {
                        if (ref[j] != null) {
                            totalSize -= ref[j].length; // recalculate total
                            // size
                            if ((i + j) % SOFTREF_PORTION == 0) {

                                if (indexSR == sr.length) {
                                    indexSR = 0;
                                }
                                // System.out.println("j = " + j + ", indexSR =
                                // " + indexSR);
                                sr[indexSR++] = new SoftReference(ref[j]);
                            }
                            ref[j] = null;// clear array
                        }

                    }
                    count = 0;
                    // System.out.println("Total size = " + totalSize);

                }
            }
        } catch (OutOfMemoryError e) {
            ReliabilityRunner.debug("OutOfMemoryError is thrown. Total size = "
                    + totalSize + " i = " + i);
            ReliabilityRunner.mainTest.addError(this, e);
        }
    }
}
