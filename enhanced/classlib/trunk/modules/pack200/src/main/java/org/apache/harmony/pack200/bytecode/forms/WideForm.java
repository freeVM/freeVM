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
import org.apache.harmony.pack200.bytecode.OperandManager;

/**
 * This class implements the byte code form for the
 * wide instruction. Unlike other instructions, it
 * can take multiple forms, depending on what is being
 * widened.
 */
public class WideForm extends VariableInstructionForm {

    public WideForm(int opcode, String name) {
        super(opcode, name);
    }

    public WideForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public int getOperandType() {
        return TYPE_WIDE;
    }
    
    public boolean hasWideOperand() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.bytecode.forms.ByteCodeForm#setByteCodeOperands(org.apache.harmony.pack200.bytecode.ByteCode, org.apache.harmony.pack200.bytecode.OperandTable, org.apache.harmony.pack200.SegmentConstantPool)
     */
    public void setByteCodeOperands(ByteCode byteCode,
            OperandManager operandManager, int codeLength) {
        int instruction = operandManager.nextWideByteCode();
        if(instruction == 132) {
            setByteCodeOperandsFormat2(instruction, byteCode, operandManager, codeLength);
        } else {
            setByteCodeOperandsFormat1(instruction, byteCode, operandManager, codeLength);
        }
    }

    /**
     * This method sets the rewrite array for the bytecode
     * using Format 1 of the JVM spec: an opcode and two index
     * bytes. This is used for ?load/?store/ret
     * @param instruction should be 132
     * @param byteCode the byte code whose rewrite array should be updated
     * @param operandManager the source of the operands
     * @param codeLength ignored
     */
    protected void setByteCodeOperandsFormat1(int instruction, ByteCode byteCode,
            OperandManager operandManager, int codeLength) {
        
        // Even though this code is really similar to the
        // code for setByteCodeOperandsFormat2, I've left it
        // distinct here. This is so changing one will
        // not change the other - if there is a need to change,
        // there's a good chance that the formats will
        // differ, so an updater will not have to disentangle
        // it.
        int local = operandManager.nextLocal();
        
        // Unlike most byte codes, the wide bytecode is a
        // variable-sized bytecode. Because of this, the
        // rewrite array has to be defined here individually
        // for each bytecode, rather than in the ByteCodeForm
        // class.
        
        int[] newRewrite = new int[4];
        int rewriteIndex = 0;
        
        // Fill in what we can now
        // wide opcode
        newRewrite[rewriteIndex++] = byteCode.getOpcode();
        
        // "real" instruction that is widened
        newRewrite[rewriteIndex++] = instruction;

        // Index bytes
        setRewrite2Bytes(local, rewriteIndex, newRewrite);
        rewriteIndex +=2;
        
        byteCode.setRewrite(newRewrite);
    }

    
    /**
     * This method sets the rewrite array for the bytecode
     * using Format 2 of the JVM spec: an opcode, two index
     * bytes, and two constant bytes. This is used for iinc.
     * @param instruction int should be 132
     * @param byteCode ByteCode whose rewrite array should be updated
     * @param operandManager OperandManager source of the operands
     * @param codeLength ignored
     */
    protected void setByteCodeOperandsFormat2(int instruction, ByteCode byteCode,
            OperandManager operandManager, int codeLength) {
    
        int local = operandManager.nextLocal();        
        int constWord = operandManager.nextShort();
        
        // Unlike most byte codes, the wide bytecode is a
        // variable-sized bytecode. Because of this, the
        // rewrite array has to be defined here individually
        // for each bytecode, rather than in the ByteCodeForm
        // class.
        
        int[] newRewrite = new int[6];
        int rewriteIndex = 0;
        
        // Fill in what we can now
        // wide opcode
        newRewrite[rewriteIndex++] = byteCode.getOpcode();
        
        // "real" instruction that is widened
        newRewrite[rewriteIndex++] = instruction;

        // Index bytes
        setRewrite2Bytes(local, rewriteIndex, newRewrite);
        rewriteIndex +=2;
        
        // constant bytes
        setRewrite2Bytes(constWord, rewriteIndex, newRewrite);
        rewriteIndex += 2; // not strictly necessary, but just in case something comes along later
        
        byteCode.setRewrite(newRewrite);
    }
}
