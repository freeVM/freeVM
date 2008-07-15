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
 * String constant pool entry.
 */
public class CPString extends CPConstant {

    private transient int nameIndex;
    private final CPUTF8 name;

    public CPString(CPUTF8 value, int globalIndex) {
        super(ConstantPoolEntry.CP_String, value, globalIndex);
        this.name = value;
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(nameIndex);
    }

    public String toString() {
        return "String: " + getValue();
    }

    /**
     * Allows the constant pool entries to resolve their nested entries
     *
     * @param pool
     */
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        nameIndex = pool.indexOf(name);
    }

    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[] { name };
    }

    private boolean hashcodeComputed = false;
    private int cachedHashCode;

    private void generateHashCode() {
        hashcodeComputed = true;
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + name.hashCode();
        cachedHashCode = result;
    }

    public int hashCode() {
        if (!hashcodeComputed)
            generateHashCode();
        return cachedHashCode;
    }
}