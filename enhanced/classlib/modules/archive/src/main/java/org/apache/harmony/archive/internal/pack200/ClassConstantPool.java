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
package org.apache.harmony.archive.internal.pack200;

// NOTE: Do not use generics in this code; it needs to run on JVMs < 1.5
// NOTE: Do not extract strings as messages; this code is still a
// work-in-progress
// NOTE: Also, don't get rid of 'else' statements for the hell of it ...
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClassConstantPool {
	private List others = new ArrayList();

	private List entries = new ArrayList();

	public void add(ClassFileEntry entry) {
		// TODO this should be a set - we don't want duplicates
		// Only add in constant pools, but resolve all types since they may
		// introduce new constant pool entries
		if (entry instanceof ConstantPoolEntry) {
			if (!entries.contains(entry))
				entries.add(entry);
		} else {
			if (!others.contains(entry))
				others.add(entry);
		}
		ClassFileEntry[] nestedEntries = entry.getNestedClassFileEntries();
		for (int i = 0; i < nestedEntries.length; i++) {
			add(nestedEntries[i]);
		}
	}

	public int indexOf(ClassFileEntry entry) {
		return entries.indexOf(entry) + 1;
	}

	public int size() {
		return entries.size();
	}

	public ClassFileEntry get(int i) {
		return (ClassFileEntry) entries.get(--i);
	}

	public void resolve() {
		Iterator it = entries.iterator();
		while (it.hasNext()) {
			ClassFileEntry entry = (ClassFileEntry) it.next();
			entry.resolve(this);
		}
		it = others.iterator();
		while (it.hasNext()) {
			ClassFileEntry entry = (ClassFileEntry) it.next();
			entry.resolve(this);
		}
	}

}
