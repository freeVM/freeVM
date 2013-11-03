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
 * Signature class file attribute
 */
public class SignatureAttribute extends Attribute {

    private int signature_index;
    private final CPUTF8 signature;

    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public SignatureAttribute(CPUTF8 value) {
        super(attributeName);
        this.signature = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.harmony.unpack200.bytecode.Attribute#getLength()
     */
    protected int getLength() {
        return 2;
    }

    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[] { getAttributeName(), signature };
    }

    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        signature.resolve(pool);
        signature_index = pool.indexOf(signature);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.harmony.unpack200.bytecode.Attribute#writeBody(java.io.DataOutputStream)
     */
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(signature_index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.harmony.unpack200.bytecode.ClassFileEntry#toString()
     */
    public String toString() {
        // TODO Auto-generated method stub
        return "Signature: " + signature;
    }

}
