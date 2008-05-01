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
package org.apache.harmony.unpack200.bytecode.forms;

import org.apache.harmony.unpack200.Pack200Exception;
import org.apache.harmony.unpack200.SegmentConstantPool;
import org.apache.harmony.unpack200.bytecode.ByteCode;
import org.apache.harmony.unpack200.bytecode.CPString;
import org.apache.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.harmony.unpack200.bytecode.OperandManager;

/**
 * This class implements the byte code form for those bytecodes which have
 * string references (and only string references).
 */
public class StringRefForm extends SingleByteReferenceForm {

    public StringRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public StringRefForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    public int getOperandType() {
        return TYPE_STRINGREF;
    }

    public boolean hasStringRefOperand() {
        return true;
    }

    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextStringRef();
    }

    protected int getPoolID() {
        return SegmentConstantPool.CP_STRING;
    }

    protected void setNestedEntries(ByteCode byteCode,
            OperandManager operandManager, int offset) throws Pack200Exception {
        final SegmentConstantPool globalPool = operandManager
                .globalConstantPool();
        ClassFileEntry[] nested = null;
        nested = new ClassFileEntry[] { ((CPString) globalPool.getValue(
                getPoolID(), offset)) };
        byteCode.setNested(nested);
        if (widened) {
            byteCode.setNestedPositions(new int[][] { { 0, 2 } });
        } else {
            byteCode.setNestedPositions(new int[][] { { 0, 1 } });
        }
    }
}
