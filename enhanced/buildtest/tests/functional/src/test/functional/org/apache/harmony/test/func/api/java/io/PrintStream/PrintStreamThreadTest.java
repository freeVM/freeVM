/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
 */
/*
 * Created on 24.12.2004
 *
 */
package org.apache.harmony.test.func.api.java.io.PrintStream;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.harmony.test.func.api.java.io.share.PrintStream.PrintStreamThreadTestShared;
import org.apache.harmony.share.Result;

public final class PrintStreamThreadTest extends PrintStreamThreadTestShared {
    public static void main(String[] args) {
        System.exit(new PrintStreamThreadTest().test(args));
    }
    
    protected OutputStream getTestedOutputStream() throws IOException {
        return super.getTestedOutputStream();
    }
    protected int[] getWritten(OutputStream os) {
        return super.getWritten(os);
    }
    public Result testWrite0001() throws IOException {
        return super.testWrite0001();
    }
    public Result testWriteBytes() throws IOException {
        return super.testWriteBytes();
    }
    public Result testWriteSlice() throws IOException {
        return super.testWriteSlice();
    }
}
