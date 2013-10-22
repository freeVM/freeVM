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
 * @author: Vera Y.Petrashkova
 * @version: $revision$
 */

package org.apache.harmony.test.stress.classloader.share.CorrectClasses;

public class testManyClasses_C59 extends testManyClasses_CA {
    protected static int cntClss = 0;

    public static int PUBSTAT_F = 59;

    protected int cntArObj;

    testManyClasses_C59[] arObj;

    public boolean initArObj(int cnt) {
        cntArObj = -1;
        arObj = new testManyClasses_C59[cnt];
        for (int i = 0; i < cnt; i++) {
            try {
                arObj[i] = new testManyClasses_C59();
            } catch (Throwable e) {
                e.printStackTrace(System.out);
                return false;
            }
        }
        cntArObj = cnt;
        return true;
    }

    public int getCntArObj() {
        return cntArObj;
    }

    public testManyClasses_C59() {
        super();
        cntClss++;
    }

    public int getCntClss() {
        return cntClss;
    }

    public int getPUBSTAT_F() {
        return PUBSTAT_F;
    }
}
