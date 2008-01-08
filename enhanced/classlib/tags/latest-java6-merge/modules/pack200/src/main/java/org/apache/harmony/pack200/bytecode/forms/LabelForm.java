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
package org.apache.harmony.pack200.bytecode.forms;

import org.apache.harmony.pack200.bytecode.ByteCode;
import org.apache.harmony.pack200.bytecode.CodeAttribute;
import org.apache.harmony.pack200.bytecode.OperandManager;

/**
 * This class implements the byte code form for those
 * bytecodes which have label references (and only
 * label references).
 */
public class LabelForm extends ByteCodeForm {

    protected boolean widened = false;
    
    public LabelForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
        // TODO Auto-generated constructor stub
    }

    public LabelForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    public int getOperandType() {
        return TYPE_LABEL;
    }

    public boolean hasLabelOperand() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.bytecode.forms.ByteCodeForm#fixUpByteCodeTarget(org.apache.harmony.pack200.bytecode.ByteCode, org.apache.harmony.pack200.bytecode.CodeAttribute)
     */
    public void fixUpByteCodeTargets(ByteCode byteCode, CodeAttribute codeAttribute) {
        // LabelForms need to fix up the target of label operations
        int originalTarget = byteCode.getByteCodeTargets()[0];
        int sourceIndex = byteCode.getByteCodeIndex();
        int absoluteInstructionTargetIndex = sourceIndex + originalTarget;
        int targetValue = ((Integer)codeAttribute.byteCodeOffsets.get(absoluteInstructionTargetIndex)).intValue();
        int sourceValue = ((Integer)codeAttribute.byteCodeOffsets.get(sourceIndex)).intValue();
        // The operand is the difference between the source instruction
        // and the destination instruction.
        // TODO: Probably have to do something other than setOperandInt if this is widened.
        byteCode.setOperandSigned2Bytes(targetValue - sourceValue, 0);
        if(widened) {
            byteCode.setNestedPositions(new int[][] {{0,4}});
        } else {
            byteCode.setNestedPositions(new int[][] {{0,2}});
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.bytecode.forms.ByteCodeForm#setByteCodeOperands(org.apache.harmony.pack200.bytecode.ByteCode, org.apache.harmony.pack200.bytecode.OperandTable, org.apache.harmony.pack200.SegmentConstantPool)
     */
    public void setByteCodeOperands(ByteCode byteCode,
            OperandManager operandManager, int codeLength) {
        byteCode.setByteCodeTargets(new int[] {operandManager.nextLabel()});
        // The byte code operands actually get set later -
        // once we have all the bytecodes - in fixUpByteCodeTarget().
        return;
    }
}
