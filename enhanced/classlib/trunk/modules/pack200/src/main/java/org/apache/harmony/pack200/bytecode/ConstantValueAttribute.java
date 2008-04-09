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

/**
 * An {@link Attribute} representing a constant.
 */
public class ConstantValueAttribute extends Attribute {

    private int constantIndex;

	private final ClassFileEntry entry;

    private static final CPUTF8 attributeName = new CPUTF8(
            "ConstantValue", ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ); //$NON-NLS-1$

	public ConstantValueAttribute(ClassFileEntry entry) {
		super(attributeName);
        if(entry == null) {
            throw new NullPointerException();
        }
		this.entry = entry;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final ConstantValueAttribute other = (ConstantValueAttribute) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		return true;
	}


	protected int getLength() {
		return 2;
	}

	protected ClassFileEntry[] getNestedClassFileEntries() {
		return new ClassFileEntry[] { getAttributeName(), entry };
	}


	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((entry == null) ? 0 : entry.hashCode());
		return result;
	}

	protected void resolve(ClassConstantPool pool) {
		super.resolve(pool);
		entry.resolve(pool);
		this.constantIndex = pool.indexOf(entry);
	}

	public String toString() {
		return "Constant:" + entry;
	}


	protected void writeBody(DataOutputStream dos) throws IOException {
		dos.writeShort(constantIndex);
	}

}