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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.pack200.Segment;

public class CodeAttribute extends BCIRenumberedAttribute {

    public List attributes = new ArrayList();
    // instances
    public List byteCodeOffsets = new ArrayList();
    public List byteCodes = new ArrayList();
    public int codeLength;
    public List exceptionTable; // of ExceptionTableEntry
    public int maxLocals;
    public int maxStack;
    private static final CPUTF8 attributeName = new CPUTF8("Code", ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ);

    public CodeAttribute(int maxStack, int maxLocals, byte codePacked[],
            Segment segment, OperandManager operandManager, List exceptionTable) {
        super(attributeName);
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.codeLength = 0;
        this.exceptionTable = exceptionTable;
        byteCodeOffsets.add(new Integer(0));
        int byteCodeIndex = 0;
        for (int i = 0; i < codePacked.length; i++) {
            ByteCode byteCode = ByteCode.getByteCode(codePacked[i] & 0xff);
            // Setting the offset must happen before extracting operands
            // because label bytecodes need to know their offsets.
            byteCode.setByteCodeIndex(byteCodeIndex);
            byteCodeIndex++;
            byteCode.extractOperands(operandManager, segment, codeLength);
            byteCodes.add(byteCode);
            codeLength += byteCode.getLength();
            int lastBytecodePosition = ((Integer) byteCodeOffsets
                    .get(byteCodeOffsets.size() - 1)).intValue();
            // This code assumes all multiple byte bytecodes are
            // replaced by a single-byte bytecode followed by
            // another bytecode.
            if (byteCode.hasMultipleByteCodes()) {
                byteCodeOffsets.add(new Integer(lastBytecodePosition + 1));
                byteCodeIndex++;
            }
            // I've already added the first element (at 0) before
            // entering this loop, so make sure I don't add one
            // after the last element.
            if (i < (codePacked.length - 1)) {
                byteCodeOffsets.add(new Integer(lastBytecodePosition
                        + byteCode.getLength()));
            }
            if(byteCode.getOpcode() == 0xC4) {
                // Special processing for wide bytecode - it knows what its
                // instruction is from the opcode manager, so ignore the
                // next instruction
                i++;
            }
        }
        // Now that all the bytecodes know their positions and
        // sizes, fix up the byte code targets
        // At this point, byteCodes may be a different size than
        // codePacked because of wide bytecodes.
        for (int i = 0; i < byteCodes.size(); i++) {
            ByteCode byteCode = (ByteCode)byteCodes.get(i);
            byteCode.applyByteCodeTargetFixup(this);
        }
    }

    protected int getLength() {
        int attributesSize = 0;
        Iterator it = attributes.iterator();
        while (it.hasNext()) {
            Attribute attribute = (Attribute) it.next();
            attributesSize += attribute.getLengthIncludingHeader();
        }
        return 2 + 2 + 4 + codeLength + 2 + exceptionTable.size()
                * (2 + 2 + 2 + 2) + 2 + attributesSize;
    }

    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList nestedEntries = new ArrayList();
        nestedEntries.add(getAttributeName());
        nestedEntries.addAll(byteCodes);
        nestedEntries.addAll(attributes);
        // Don't forget to add the ExceptionTable catch_types
        for (Iterator iter = exceptionTable.iterator(); iter.hasNext();) {
            ExceptionTableEntry entry = (ExceptionTableEntry) iter.next();
            CPClass catchType = entry.getCatchType();
            // If the catch type is null, this is a finally
            // block. If it's not null, we need to add the
            // CPClass to the list of nested class file entries.
            if(catchType != null) {
                nestedEntries.add(catchType);
            }
        }
        ClassFileEntry[] nestedEntryArray = new ClassFileEntry[nestedEntries
                .size()];
        nestedEntries.toArray(nestedEntryArray);
        return nestedEntryArray;
    }

    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        Iterator it = attributes.iterator();
        while (it.hasNext()) {
            Attribute attribute = (Attribute) it.next();
            attribute.resolve(pool);
        }
        it = byteCodes.iterator();
        while (it.hasNext()) {
            ByteCode byteCode = (ByteCode) it.next();
            byteCode.resolve(pool);
        }
        for (Iterator iter = exceptionTable.iterator(); iter.hasNext();) {
            ExceptionTableEntry entry = (ExceptionTableEntry) iter.next();
            entry.resolve(pool);
        }
    }

    public String toString() {
        return "Code: " + getLength() + " bytes";
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(maxStack);
        dos.writeShort(maxLocals);
        dos.writeInt(codeLength);
        Iterator it = byteCodes.iterator();
        while (it.hasNext()) {
            ByteCode byteCode = (ByteCode) it.next();
            byteCode.write(dos);
        }
        dos.writeShort(exceptionTable.size());
        Iterator exceptionTableEntries = exceptionTable.iterator();
        while (exceptionTableEntries.hasNext()) {
            ExceptionTableEntry entry = (ExceptionTableEntry) exceptionTableEntries
                    .next();
            entry.write(dos);
        }
        dos.writeShort(attributes.size());
        it = attributes.iterator();
        while (it.hasNext()) {
            Attribute attribute = (Attribute) it.next();
            attribute.write(dos);
        }
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public List attributes() {
        return attributes;
    }

    protected int[] getStartPCs() {
        // Do nothing here as we've overriden renumber
        return null;
    }

    public void renumber(List byteCodeOffsets) {
        for (Iterator iter = exceptionTable.iterator(); iter.hasNext();) {
            ExceptionTableEntry entry = (ExceptionTableEntry) iter.next();
            entry.renumber(byteCodeOffsets);
        }
    }
}