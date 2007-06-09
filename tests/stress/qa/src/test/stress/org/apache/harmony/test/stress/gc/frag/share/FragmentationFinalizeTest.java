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
 * @version $Revision: 1.6 $
 * Created on 22.12.2005
 * 
 */
package org.apache.harmony.test.stress.gc.frag.share;

import org.apache.harmony.test.stress.gc.share.GCTestError;
import org.apache.harmony.test.share.stress.ReliabilityRunner;

public class FragmentationFinalizeTest extends FragmentationAbstractTest {

    boolean finFlag = false;

    public void perform() {
        ref = null;
        FinalizedObject[] objects = new FinalizedObject[NUM];

        int i = 0;
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
                    objects[i] = new FinalizedObject(this, size);
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
                        if (objects[j] != null) {
                            totalSize -= objects[j].bytes.length; // recalculate
                                                                    // total
                                                                    // size
                            objects[j] = null;// clear array
                        }

                    }
                    count = 0;
                    // System.out.println("Total size = " + totalSize);

                }
            }
        } catch (OutOfMemoryError e) {
            ReliabilityRunner.debug("OutOfMemoryError is thrown. Total size = " + totalSize
                    + " i = " + i);
                ReliabilityRunner.mainTest.addError(this, e);
        }
        if (!finFlag) {
            ReliabilityRunner.debug("Non finalize function was invoked.");
            ReliabilityRunner.mainTest.addError(this, new GCTestError());
        }
    }

    class FinalizedObject {
        FragmentationFinalizeTest test = null;

        byte[] bytes = null;

        FinalizedObject(FragmentationFinalizeTest test, int size) {
            this.test = test;
            bytes = new byte[size];
        }

        protected void finalize() {
            test.finFlag = true;
        }

    }

}
