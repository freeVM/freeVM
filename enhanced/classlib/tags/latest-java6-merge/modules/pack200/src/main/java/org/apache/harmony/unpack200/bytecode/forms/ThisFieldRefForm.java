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

import org.apache.harmony.unpack200.SegmentConstantPool;
import org.apache.harmony.unpack200.bytecode.OperandManager;

/**
 * This class implements references to fields defined in the current class,
 * which is set by this class in the OperandManager. Pack200 allows the current
 * class to be inferred from context; this class tracks previous field reference
 * classes to allow this.
 */
public class ThisFieldRefForm extends ClassSpecificReferenceForm {

    public ThisFieldRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public int getOperandType() {
        return TYPE_THISFIELDREF;
    }

    public boolean hasThisFieldRefOperand() {
        return true;
    }

    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextThisFieldRef();
    }

    protected int getPoolID() {
        return SegmentConstantPool.CP_FIELD;
    }

    protected String context(OperandManager operandManager) {
        return operandManager.getCurrentClass();
    }
}
