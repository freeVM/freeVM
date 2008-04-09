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

public class CPClass extends ConstantPoolEntry {

	private int index;

	public String name;

	private final CPUTF8 utf8;

	/**
	 * Creates a new CPClass
	 * @param name
	 * @throws NullPointerException if name is null
	 */
    public CPClass(CPUTF8 name) {
		super(ConstantPoolEntry.CP_Class);
		this.name = name.underlyingString();
		this.domain = ClassConstantPool.DOMAIN_CLASSREF;
		this.utf8 = name;
		if(name == null) {
            throw new NullPointerException("Null arguments are not allowed");
		}
	}

    public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final CPClass other = (CPClass) obj;
		return utf8.equals(other.utf8);
	}

	protected ClassFileEntry[] getNestedClassFileEntries() {
		return new ClassFileEntry[] { utf8, };
	}


	public int hashCode() {
		return utf8.hashCode();
	}

	protected void resolve(ClassConstantPool pool) {
		super.resolve(pool);
		index = pool.indexOf(utf8);
	}

	public String toString() {
		return "Class: " + getName();
	}

	public String getName() {
		return name;
	}

	protected void writeBody(DataOutputStream dos) throws IOException {
		dos.writeShort(index);
	}
}