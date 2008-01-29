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

import org.apache.harmony.pack200.SegmentConstantPool;
import org.apache.harmony.pack200.bytecode.OperandManager;

/**
 * This class implements the byte code form for those
 * bytecodes which have class references (and only
 * class references).
 */
public class ClassRefForm extends ReferenceForm {

    protected boolean widened = false;

    public ClassRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
        // TODO Auto-generated constructor stub
    }

    public ClassRefForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    public int getOperandType() {
        return TYPE_CLASSREF;
    }

    public boolean hasClassRefOperand() {
        return true;
    }

    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextClassRef();
    }

    protected int getPoolID() {
        return SegmentConstantPool.CP_CLASS;
    }
}
