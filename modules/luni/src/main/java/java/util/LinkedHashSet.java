/* Copyright 2005, 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;


import java.io.Serializable;

/**
 * LinkedHashSet is a variant on HashSet. Its entries are kept in a doubly-linked list.
 * The iteration order is the order in which entries were inserted.
 * <p>
 * Null elements are allowed, and all the optional Set operations are supported.
 * <p>
 * Like HashSet, LinkedHashSet is not thread safe, so access by multiple threads must be synchronized
 * by an external mechanism such as Collections.synchronizedSet.
 */
public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable,
		Serializable {
	
	private static final long serialVersionUID = -2851667679971038690L;

	/**
	 * Contructs a new empty instance of LinkedHashSet.
	 */
	public LinkedHashSet() {
		super(new LinkedHashMap());
	}

	/**
	 * Constructs a new instance of LinkedHashSet with the specified capacity.
	 * 
	 * @param capacity
	 *            the initial capacity of this HashSet
	 */
	public LinkedHashSet(int capacity) {
		super(new LinkedHashMap(capacity));
	}

	/**
	 * Constructs a new instance of LinkedHashSet with the specified capacity
	 * and load factor.
	 * 
	 * @param capacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the initial load factor
	 */
	public LinkedHashSet(int capacity, float loadFactor) {
		super(new LinkedHashMap(capacity, loadFactor));
	}

	/**
	 * Constructs a new instance of LinkedHashSet containing the unique elements
	 * in the specified collection.
	 * 
	 * @param collection
	 *            the collection of elements to add
	 */
	public LinkedHashSet(Collection<? extends E> collection) {
		super(new LinkedHashMap(collection.size() < 6 ? 11
				: collection.size() * 2));
		Iterator it = collection.iterator();
		while (it.hasNext())
			add(it.next());
	}

	/* overrides method in HashMap */
	HashMap createBackingMap(int capacity, float loadFactor) {
		return new LinkedHashMap(capacity, loadFactor);
	}
}
