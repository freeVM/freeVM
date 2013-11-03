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

package org.apache.harmony.test.stress.exceptions.catcher;

import org.apache.harmony.test.share.stress.ReliabilityRunner;

public class StackUnwindingManyObjects extends Catcher {

    public void testUnwinding() {
        int deepUnwinding;

        /* Get deep unwinding */
        String propetyName = "org.apache.harmony.test.stress.exceptions."
                + "catcher.StackUnwindingManyObjects";
        deepUnwinding = Integer
                .getInteger(propetyName, 100).intValue();

        try {
            new ScrewAndThrowManyObjects().screwAndThrow(deepUnwinding);
            ReliabilityRunner.mainTest.addError(this, new ExceptionWasntThrownError());
        } catch (Throwable thr) {
            if (classToCatch.isAssignableFrom(thr.getClass())) {
                ReliabilityRunner.debug(classToCatch.toString()
                        + " successfully catched");
            } else {
                ReliabilityRunner.mainTest.addError(this, thr);
            }
        }
    }

    private class ScrewAndThrowManyObjects {
        protected void screwAndThrow(int deepUnwinding) throws Throwable {
            if (deepUnwinding > 0) {
                new ScrewAndThrowManyObjects().screwAndThrow(deepUnwinding - 1);
            } else {
                doThrow();
            }
        }
    }
}
