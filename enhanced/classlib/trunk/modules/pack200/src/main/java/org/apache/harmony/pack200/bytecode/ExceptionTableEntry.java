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
import java.util.List;

public class ExceptionTableEntry {

    private int startPC;
    private int endPC;
    private int handlerPC;
    private CPClass catchType;

    private int startPcRenumbered;
    private int endPcRenumbered;
    private int handlerPcRenumbered;
    private int catchTypeIndex;

    /**
     * Create a new ExceptionTableEntry. Exception tables are
     * of two kinds: either a normal one (with a Throwable as
     * the catch_type) or a finally clause (which has no
     * catch_type). In the class file, the finally clause is
     * represented as catch_type == 0.
     *
     * To create a finally clause with this method, pass in
     * null for the catchType.
     *
     * @param startPC int
     * @param endPC int
     * @param handlerPC int
     * @param catchType CPClass (if it's a normal catch) or null
     *  (if it's a finally clause).
     */
    public ExceptionTableEntry(int startPC, int endPC, int handlerPC, CPClass catchType) {
        this.startPC = startPC;
        this.endPC = endPC;
        this.handlerPC = handlerPC;
        this.catchType = catchType;
    }

	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(startPcRenumbered);
		dos.writeShort(endPcRenumbered);
		dos.writeShort(handlerPcRenumbered);
		dos.writeShort(catchTypeIndex);
	}

    public void renumber(List byteCodeOffsets) {
        startPcRenumbered = ((Integer)byteCodeOffsets.get(startPC)).intValue();
        int endPcIndex = startPC + endPC;
        endPcRenumbered = ((Integer)byteCodeOffsets.get(endPcIndex)).intValue();
        int handlerPcIndex = endPcIndex + handlerPC;
        handlerPcRenumbered = ((Integer)byteCodeOffsets.get(handlerPcIndex)).intValue();
    }

    public CPClass getCatchType() {
        return catchType;
    }

    public void resolve(ClassConstantPool pool) {
        if(catchType == null) {
            // If the catch type is a finally clause
            // the index is always 0.
            catchTypeIndex = 0;
            return;
        }
        catchType.resolve(pool);
        catchTypeIndex = pool.indexOf(catchType);
    }
}
