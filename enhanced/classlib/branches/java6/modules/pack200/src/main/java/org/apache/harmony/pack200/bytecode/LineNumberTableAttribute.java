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
package org.apache.harmony.pack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

public class LineNumberTableAttribute extends BCIRenumberedAttribute {

    private int line_number_table_length;
    private int[] start_pcs;
    private int[] line_numbers;

    public LineNumberTableAttribute(int line_number_table_length, int[] start_pcs, int[] line_numbers) {
        super("LineNumberTable");
        this.line_number_table_length = line_number_table_length;
        this.start_pcs = start_pcs;
        this.line_numbers = line_numbers;
    }

    protected int getLength() {
        return 2 + (4 * line_number_table_length);
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(line_number_table_length);
        for (int i = 0; i < line_number_table_length; i++) {
            dos.writeShort(start_pcs[i]);
            dos.writeShort(line_numbers[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.bytecode.ClassFileEntry#toString()
     */
    public String toString() {
        return "LineNumberTable: " + line_number_table_length + " lines";
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.bytecode.Attribute#getNestedClassFileEntries()
     */
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[] {getAttributeName()};
    }

	/* (non-Javadoc)
	 * @see org.apache.harmony.pack200.bytecode.Attribute#resolve(org.apache.harmony.pack200.bytecode.ClassConstantPool)
	 */
	protected void resolve(ClassConstantPool pool) {
		super.resolve(pool);
	}

    protected int[] getStartPCs() {
        return start_pcs;
    }
}
