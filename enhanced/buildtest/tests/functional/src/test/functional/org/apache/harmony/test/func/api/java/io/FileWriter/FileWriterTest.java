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
package org.apache.harmony.test.func.api.java.io.FileWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.harmony.test.func.api.java.io.share.PrepareTestCleanupRunner;
import org.apache.harmony.test.func.api.java.io.share.FileWriter.FileWriterTestShared;
import org.apache.harmony.share.Result;

public final class FileWriterTest extends FileWriterTestShared {
    public static void main(String[] args) {
        try {
            System.exit(PrepareTestCleanupRunner
                    .run(args, new FileWriterTest()));
        } catch (IOException e) {
            System.err.println("Got exception - " + e.getMessage());
        }
    }

    public int cleanup(File dir) throws IOException {
        return super.cleanup(dir);
    }
    protected Writer getTestedWriter() throws IOException {
        return super.getTestedWriter();
    }
    protected Writer getTestedWriter(Object lock) {
        return super.getTestedWriter(lock);
    }
    protected String getWriterAsString(Writer w) throws IOException {
        return super.getWriterAsString(w);
    }
    public int prepare(File dir) throws IOException {
        return super.prepare(dir);
    }
    public void setTestDir(File dir) {
        super.setTestDir(dir);
    }
    public Result testAppend() throws IOException {
        return super.testAppend();
    }
    public Result testClose() throws IOException {
        return super.testClose();
    }
    public Result testDoubleOpen() throws IOException {
        return super.testDoubleOpen();
    }
    public Result testFileDescriptor() throws IOException {
        return super.testFileDescriptor();
    }
    public Result testFlush() throws IOException {
        return super.testFlush();
    }
    public Result testOpenDirectory() throws IOException {
        return super.testOpenDirectory();
    }
    public Result testReadonly() throws IOException {
        return super.testReadonly();
    }
    public Result testWriteArray() throws IOException {
        return super.testWriteArray();
    }
    public Result testWriteArrayBigLength() throws IOException {
        return super.testWriteArrayBigLength();
    }
    public Result testWriteArrayNegativeLength() throws IOException {
        return super.testWriteArrayNegativeLength();
    }
    public Result testWriteArrayNegativeStart() throws IOException {
        return super.testWriteArrayNegativeStart();
    }
    public Result testWriteArraySlice() throws IOException {
        return super.testWriteArraySlice();
    }
    public Result testWriteInt() throws IOException {
        return super.testWriteInt();
    }
    public Result testWriteNullArray() throws IOException {
        return super.testWriteNullArray();
    }
    public Result testWriteNullString() throws IOException {
        return super.testWriteNullString();
    }
    public Result testWriteString() throws IOException {
        return super.testWriteString();
    }
    public Result testWriteStringBigLength() throws IOException {
        return super.testWriteStringBigLength();
    }
    public Result testWriteStringNegativeLength() throws IOException {
        return super.testWriteStringNegativeLength();
    }
    public Result testWriteStringNegativeStart() throws IOException {
        return super.testWriteStringNegativeStart();
    }
    public Result testWriteStringSlice() throws IOException {
        return super.testWriteStringSlice();
    }
}
