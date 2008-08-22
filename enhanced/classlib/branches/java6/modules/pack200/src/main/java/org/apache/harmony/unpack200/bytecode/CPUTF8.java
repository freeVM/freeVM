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
package org.apache.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * UTF8 constant pool entry, used for storing long Strings.
 */
public class CPUTF8 extends ConstantPoolEntry {

    private final String utf8;

    /**
     * Creates a new CPUTF8 instance
     *
     * @param utf8
     * @param domain
     * @throws NullPointerException
     *             if utf8 is null
     */
    public CPUTF8(String utf8, int globalIndex) {
        super(ConstantPoolEntry.CP_UTF8, globalIndex);
        this.utf8 = utf8;
        if (utf8 == null) {
            throw new NullPointerException("Null arguments are not allowed");
        }
    }

    public CPUTF8(String string) {
        this(string, -1);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        final CPUTF8 other = (CPUTF8) obj;
        return utf8.equals(other.utf8);
    }

    private boolean hashcodeComputed;
    private int cachedHashCode;

    private void generateHashCode() {
        hashcodeComputed = true;
        final int PRIME = 31;
        cachedHashCode = PRIME + utf8.hashCode();
    }

    public int hashCode() {
        if (!hashcodeComputed)
            generateHashCode();
        return cachedHashCode;
    }

    public String toString() {
        return "UTF8: " + utf8;
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeUTF(utf8);
    }

    public String underlyingString() {
        return utf8;
    }

    public void setGlobalIndex(int index) {
        globalIndex = index;
    }
}