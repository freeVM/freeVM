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
 * Abstract superclass for reference constant pool entries, such as a method or
 * field reference.
 */
public abstract class CPRef extends ConstantPoolEntry {

    CPClass className;
    transient int classNameIndex;

    protected CPNameAndType nameAndType;
    transient int nameAndTypeIndex;

    /**
     * Create a new CPRef
     * 
     * @param type
     * @param className
     * @param descriptor
     * @throws NullPointerException
     *             if descriptor or className is null
     */
    public CPRef(byte type, CPClass className, CPNameAndType descriptor) {
        super(type);
        this.className = className;
        this.nameAndType = descriptor;
        if (descriptor == null || className == null) {
            throw new NullPointerException("Null arguments are not allowed");
        }
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        final CPRef other = (CPRef) obj;
        if (!className.equals(other.className))
            return false;
        if (!nameAndType.equals(other.nameAndType))
            return false;
        return true;
    }

    protected ClassFileEntry[] getNestedClassFileEntries() {
        ClassFileEntry[] entries = new ClassFileEntry[2];
        entries[0] = className;
        entries[1] = nameAndType;
        return entries;
    }

    public int hashCode() {
        final int PRIME = 37;
        return (PRIME * className.hashCode()) + nameAndType.hashCode();
    }

    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        nameAndTypeIndex = pool.indexOf(nameAndType);
        classNameIndex = pool.indexOf(className);
    }

    protected String cachedToString = null;

    public String toString() {
        if (cachedToString == null) {
            String type;
            if (getTag() == ConstantPoolEntry.CP_Fieldref) {
                type = "FieldRef"; //$NON-NLS-1$
            } else if (getTag() == ConstantPoolEntry.CP_Methodref) {
                type = "MethoddRef"; //$NON-NLS-1$
            } else if (getTag() == ConstantPoolEntry.CP_InterfaceMethodref) {
                type = "InterfaceMethodRef"; //$NON-NLS-1$
            } else {
                type = "unknown"; //$NON-NLS-1$
            }
            cachedToString = type + ": " + className + "#" + nameAndType; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return cachedToString;
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(classNameIndex);
        dos.writeShort(nameAndTypeIndex);
    }

}