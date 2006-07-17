/* Copyright 1998, 2006 The Apache Software Foundation or its licensors, as applicable
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


import java.lang.reflect.Array;

/**
 * AbstractCollection is an abstract implementation of the Collection interface.
 * This implementation does not support adding. A subclass must implement the
 * abstract methods iterator() and size().
 * @since 1.2
 */
public abstract class AbstractCollection<E> implements Collection<E> {

	/**
	 * Constructs a new instance of this AbstractCollection.
	 */
	protected AbstractCollection() {
		super();
	}

    /**
     * If the specified element is not contained within this collection, and
     * addition of this element succeeds, then true will be returned. If the
     * specified element is already contained within this collection, or
     * duplication is not permitted, false will be returned. Different
     * implementations may add specific limitations on this method to filter
     * permitted elements. For example, in some implementation, null element may
     * be denied, and NullPointerException will be thrown out. These limitations
     * should be explicitly documented by specific collection implmentation.
     * 
     * Add operation is not supported in this implementation, and
     * UnsupportedOperationException will always be thrown out.
     * 
     * @param object
     *            the element to be added.
     * @return true if the collection is changed successfully after invoking
     *         this method. Otherwise, false.
     * @exception UnsupportedOperationException
     *                if add operation is not supported by this class.
     * @exception NullPointerException
     *                if null is used to invoke this method, and null is not
     *                permitted by this collection.
     * @exception ClassCastException
     *                if the class type of the specified element is not
     *                compatible with the permitted class type.
     * @exception IllegalArgumentException
     *                if limitations of this collection prevent the specified
     *                element from being added
     */
	public boolean add(E object) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds the objects in the specified Collection to this Collection.
	 * 
	 * @param collection
	 *            the Collection of objects
	 * @return true if this Collection is modified, false otherwise
	 * 
	 * @exception UnsupportedOperationException
	 *                when adding to this Collection is not supported
	 * @exception ClassCastException
	 *                when the class of an object is inappropriate for this
	 *                Collection
	 * @exception IllegalArgumentException
	 *                when an object cannot be added to this Collection
	 */
	public boolean addAll(Collection<? extends E> collection) {
		boolean result = false;
		Iterator<? extends E> it = collection.iterator();
		while (it.hasNext()) {
            if (add(it.next())) {
                result = true;
            }
        }
		return result;
	}

	public void clear() {
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	/**
	 * Searches this Collection for the specified object.
	 * 
	 * @param object
	 *            the object to search for
	 * @return true if <code>object</code> is an element of this Collection,
	 *         false otherwise
	 */
	public boolean contains(Object object) {
		Iterator<E> it = iterator();
		if (object != null) {
			while (it.hasNext()) {
                if (object.equals(it.next())) {
                    return true;
                }
            }
		} else {
			while (it.hasNext()) {
                if (it.next() == null) {
                    return true;
                }
            }
		}
		return false;
	}

	/**
	 * Searches this Collection for all objects in the specified Collection.
	 * 
	 * @param collection
	 *            the Collection of objects
	 * @return true if all objects in the specified Collection are elements of
	 *         this Collection, false otherwise
	 */
	public boolean containsAll(Collection<?> collection) {
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
		return true;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Answers an Iterator on the elements of this Collection. A subclass must
	 * implement the abstract methods iterator() and size().
	 * 
	 * @return an Iterator on the elements of this Collection
	 * 
	 * @see Iterator
	 */
	public abstract Iterator<E> iterator();

	/**
	 * Removes the first occurrence of the specified object from this
	 * Collection.
	 * 
	 * @param object
	 *            the object to remove
	 * @return true if this Collection is modified, false otherwise
	 * 
	 * @exception UnsupportedOperationException
	 *                when removing from this Collection is not supported
	 */
	public boolean remove(Object object) {
		Iterator<?> it = iterator();
		if (object != null) {
			while (it.hasNext()) {
				if (object.equals(it.next())) {
					it.remove();
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (it.next() == null) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes all occurrences in this Collection of each object in the
	 * specified Collection.
	 * 
	 * @param collection
	 *            the Collection of objects to remove
	 * @return true if this Collection is modified, false otherwise
	 * 
	 * @exception UnsupportedOperationException
	 *                when removing from this Collection is not supported
	 */
	public boolean removeAll(Collection<?> collection) {
		boolean result = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (collection.contains(it.next())) {
				it.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Removes all objects from this Collection that are not contained in the
	 * specified Collection.
	 * 
	 * @param collection
	 *            the Collection of objects to retain
	 * @return true if this Collection is modified, false otherwise
	 * 
	 * @exception UnsupportedOperationException
	 *                when removing from this Collection is not supported
	 */
	public boolean retainAll(Collection<?> collection) {
		boolean result = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (!collection.contains(it.next())) {
				it.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Answers the number of elements in this Collection.
	 * 
	 * @return the number of elements in this Collection
	 */
	public abstract int size();

	/**
	 * Answers a new array containing all elements contained in this Collection.
	 * 
	 * @return an array of the elements from this Collection
	 */
	public Object[] toArray() {
		int size = size(), index = 0;
		Iterator<?> it = iterator();
		Object[] array = new Object[size];
		while (index < size) {
            array[index++] = it.next();
        }
		return array;
	}

	/**
	 * Answers an array containing all elements contained in this Collection. If
	 * the specified array is large enough to hold the elements, the specified
	 * array is used, otherwise an array of the same type is created. If the
	 * specified array is used and is larger than this Collection, the array
	 * element following the collection elements is set to null.
	 * 
	 * @param contents
	 *            the array
	 * @return an array of the elements from this Collection
	 * 
	 * @exception ArrayStoreException
	 *                when the type of an element in this Collection cannot be
	 *                stored in the type of the specified array
	 */
	@SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
		int size = size(), index = 0;
		if (size > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
			contents = (T[])Array.newInstance(ct, size);
        }
		for (E entry: this) {
			contents[index++] = (T)entry;
        }
		if (index < contents.length) {
			contents[index] = null;
        }
		return contents;
	}

	/**
	 * Answers the string representation of this Collection.
	 * 
	 * @return the string representation of this Collection
	 */
	@Override
    public String toString() {
		if (isEmpty()) {
            return "[]"; //$NON-NLS-1$
        }

		StringBuilder buffer = new StringBuilder(size() * 16);
		buffer.append('[');
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			Object next = it.next();
			if (next != this) {
				buffer.append(next);
			} else {
				buffer.append("(this Collection)");
			}
            if(it.hasNext()) {
                buffer.append(", ");
            }
		}
		buffer.append(']');
		return buffer.toString();
	}
}
