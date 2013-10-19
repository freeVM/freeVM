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

/**
 * @author Alexander D. Shipilov
 * @version $Revision: 1.5 $
 */

package org.apache.harmony.test.stress.misc.stressloads;

import org.apache.harmony.test.share.stress.ReliabilityRunner;

/* Creation of long chain of objects. */
public class StressLoadsThread20 extends Thread {
    StressLoadsThread20Chain prev;

    public void run() {
        prev = new StressLoadsThread20Chain(this);
        while (true) {
            try {
                prev = new StressLoadsThread20Chain(prev);
            } catch (OutOfMemoryError er) {
                prev = null;
                System.gc();
                prev = new StressLoadsThread20Chain(this);
            } catch (Throwable thr) {
                ReliabilityRunner.debug("StressLoadsThread20 test error");
                ReliabilityRunner.mainTest.addError(
                        StressLoadsRunner.loadsRunnerTest, thr);
                return;
            }
        }
    }
}

class StressLoadsThread20Chain {
    Object prev;

    public StressLoadsThread20Chain(Object prev) {
        this.prev = prev;
    }
}
