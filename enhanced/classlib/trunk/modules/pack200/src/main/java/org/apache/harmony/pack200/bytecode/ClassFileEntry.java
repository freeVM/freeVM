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
 * The abstract superclass for all types of class file entries.
 */
public abstract class ClassFileEntry {

	protected static final ClassFileEntry[] NONE = new ClassFileEntry[0];
	private boolean resolved;

	protected abstract void doWrite(DataOutputStream dos) throws IOException;

	public abstract boolean equals(Object arg0);

	protected ClassFileEntry[] getNestedClassFileEntries() {
		return NONE;
	}

	public abstract int hashCode();

	/**
	 * Allows the constant pool entries to resolve their nested entries
	 *
	 * @param pool
	 */
	protected void resolve(ClassConstantPool pool) {
		resolved = true;
	}

	protected boolean isResolved() {
	    return resolved;
	}

	public abstract String toString();

	public final void write(DataOutputStream dos) throws IOException {
		if (!resolved)
			throw new IllegalStateException("Entry has not been resolved");
		doWrite(dos);
	}

    /**
     * Answer true if the receiver must be at the beginning
     * of the class pool (because it is the target of a
     * single-byte ldc command). Otherwise answer false.
     *
     * @return boolean true if the receiver must be under
     *   256; otherwise false.
     */
    public boolean mustStartClassPool() {
        return false;
    }
}
