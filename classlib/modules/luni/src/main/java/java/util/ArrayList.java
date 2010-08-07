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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Array;

import org.apache.harmony.luni.internal.nls.Messages;

/**
 * ArrayList is an implementation of {@link List}, backed by an array. All
 * optional operations adding, removing, and replacing are supported. The
 * elements can be any objects.
 * 
 * @since 1.2
 */
public class ArrayList<E> extends AbstractList<E> implements List<E>,
        Cloneable, Serializable, RandomAccess {

    private static final long serialVersionUID = 8683452581122892189L;

    private transient int firstIndex;

    private transient int lastIndex;

    private transient E[] array;

    /**
     * Constructs a new instance of {@code ArrayList} with ten capacity.
     */
    public ArrayList() {
        this(10);
    }

    /**
     * Constructs a new instance of {@code ArrayList} with the specified
     * capacity.
     * 
     * @param capacity
     *            the initial capacity of this {@code ArrayList}.
     */
    public ArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        firstIndex = lastIndex = 0;
        array = newElementArray(capacity);
    }

    /**
     * Constructs a new instance of {@code ArrayList} containing the elements of
     * the specified collection. The initial size of the {@code ArrayList} will
     * be 10% larger than the size of the specified collection.
     * 
     * @param collection
     *            the collection of elements to add.
     */
    public ArrayList(Collection<? extends E> collection) {
        firstIndex = 0;
        Object[] objects = collection.toArray();
        int size = objects.length;

        // REVIEW: Created 2 array copies of the original collection here
        //         Could be better to use the collection iterator and
        //         copy once?
        array = newElementArray(size + (size / 10));
        System.arraycopy(objects, 0, array, 0, size);
        lastIndex = size;
        modCount = 1;
    }

    @SuppressWarnings("unchecked")
    private E[] newElementArray(int size) {
        return (E[]) new Object[size];
    }

    /**
     * Inserts the specified object into this {@code ArrayList} at the specified
     * location. The object is inserted before any previous element at the
     * specified location. If the location is equal to the size of this
     * {@code ArrayList}, the object is added at the end.
     * 
     * @param location
     *            the index at which to insert the object.
     * @param object
     *            the object to add.
     * @throws IndexOutOfBoundsException
     *             when {@code location < 0 || > size()}
     */
    @Override
    public void add(int location, E object) {
        int size = lastIndex - firstIndex;
        if (location < 0 || location > size) {
            throw new IndexOutOfBoundsException(
                    // luni.0A=Index: {0}, Size: {1}
                    Messages.getString("luni.0A", //$NON-NLS-1$
                            Integer.valueOf(location),
                            Integer.valueOf(size)));
        }
        if (location == 0) {
            // REVIEW: Does growAtFront() check the end to see if
            //         shifting the array is possible? Same for
            //         growAtEnd().
            if (firstIndex == 0) {
                growAtFront(1);
            }
            array[--firstIndex] = object;
        } else if (location == size) {
            // REVIEW: Why not just call add()? Matching RI behaviour?
            if (lastIndex == array.length) {
                growAtEnd(1);
            }
            array[lastIndex++] = object;
        } else { // must be case: (0 < location && location < size)
            if (firstIndex == 0 && lastIndex == array.length) {
                growForInsert(location, 1);
            } else if ((location < size / 2 && firstIndex > 0)
                    || lastIndex == array.length) {
                // REVIEW: Why not evaluate (lastIndex==array.length)
                //         first to save the divide?
                System.arraycopy(array, firstIndex, array, --firstIndex,
                        location);
            } else {
                int index = location + firstIndex;
                System.arraycopy(array, index, array, index + 1, size
                        - location);
                lastIndex++;
            }
            array[location + firstIndex] = object;
        }

        modCount++;
    }

    /**
     * Adds the specified object at the end of this {@code ArrayList}.
     * 
     * @param object
     *            the object to add.
     * @return always true
     */
    @Override
    public boolean add(E object) {
        if (lastIndex == array.length) {
            growAtEnd(1);
        }
        array[lastIndex++] = object;
        modCount++;
        return true;
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this List. The objects are added in the order they are returned from
     * the collection's iterator.
     * 
     * @param location
     *            the index at which to insert.
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this {@code ArrayList} is modified, {@code false}
     *         otherwise.
     * @throws IndexOutOfBoundsException
     *             when {@code location < 0 || > size()}
     */
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        int size = lastIndex - firstIndex;
        // REVIEW: Inconsistent exception case with
        //         add(location,object) method
        if (location < 0 || location > size) {
            throw new IndexOutOfBoundsException(
                    // luni.0A=Index: {0}, Size: {1}
                    Messages.getString("luni.0A", //$NON-NLS-1$
                            Integer.valueOf(location),
                            Integer.valueOf(size)));
        }

        Object[] dumparray = collection.toArray();
        int growSize = dumparray.length;
        // REVIEW: Why do this check here rather than check
        //         collection.size() earlier? RI behaviour?
        if (growSize == 0) {
            return false;
        }

        // REVIEW: we use array.length - growSize in 3 places -
        //         precalculate it?
        if (location == 0) {
            growAtFront(growSize);
            firstIndex -= growSize;
        } else if (location == size) {
            // REVIEW: Don't need the above check as it can be no
            //         other case. Make it just an else.
            if (lastIndex > array.length - growSize) {
                growAtEnd(growSize);
            }
            lastIndex += growSize;
        } else { // must be case: (0 < location && location < size)
            if (array.length - size < growSize) {
                // REVIEW: why grow growSize? Does growForInsert()
                //         check we don't allocate too much?
                growForInsert(location, growSize);
            } else if ((location < size / 2 && firstIndex > 0)
                    || lastIndex > array.length - growSize) {
                // REVIEW: If condition above could be switched so
                //         divide is done 2nd, same as add()
                int newFirst = firstIndex - growSize;
                // REVIEW: Have we optimised for the case where there
                //         is enough space at the end for growSize
                //         rather than doing 2 array copies?
                if (newFirst < 0) {
                    int index = location + firstIndex;
                    System.arraycopy(array, index, array, index - newFirst,
                            size - location);
                    lastIndex -= newFirst;
                    newFirst = 0;
                }
                System.arraycopy(array, firstIndex, array, newFirst, location);
                firstIndex = newFirst;
            } else {
                int index = location + firstIndex;
                System.arraycopy(array, index, array, index + growSize, size
                        - location);
                lastIndex += growSize;
            }
        }

        System.arraycopy(dumparray, 0, this.array, location + firstIndex,
                growSize);
        modCount++;
        return true;
    }

    /**
     * Adds the objects in the specified collection to this {@code ArrayList}.
     * 
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this {@code ArrayList} is modified, {@code false}
     *         otherwise.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Object[] dumpArray = collection.toArray();
        if (dumpArray.length == 0) {
            return false;
        }
        if (dumpArray.length > array.length - lastIndex) {
            growAtEnd(dumpArray.length);
        }
        System.arraycopy(dumpArray, 0, this.array, lastIndex, dumpArray.length);
        lastIndex += dumpArray.length;
        modCount++;
        return true;
    }

    /**
     * Removes all elements from this {@code ArrayList}, leaving it empty.
     * 
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        if (firstIndex != lastIndex) {
            // REVIEW: Should we use Arrays.fill() instead of just
            //         allocating a new array?  Should we use the same
            //         sized array?
            Arrays.fill(array, firstIndex, lastIndex, null);
            // REVIEW: Should the indexes point into the middle of the
            //         array rather than 0?
            firstIndex = lastIndex = 0;
            modCount++;
        }
    }

    /**
     * Returns a new {@code ArrayList} with the same elements, the same size and
     * the same capacity as this {@code ArrayList}.
     * 
     * @return a shallow copy of this {@code ArrayList}
     * @see java.lang.Cloneable
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            ArrayList<E> newList = (ArrayList<E>) super.clone();
            newList.array = array.clone();
            return newList;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Searches this {@code ArrayList} for the specified object.
     * 
     * @param object
     *            the object to search for.
     * @return {@code true} if {@code object} is an element of this
     *         {@code ArrayList}, {@code false} otherwise
     */
    @Override
    public boolean contains(Object object) {
        if (object != null) {
            for (int i = firstIndex; i < lastIndex; i++) {
                if (object.equals(array[i])) {
                    return true;
                }
            }
        } else {
            for (int i = firstIndex; i < lastIndex; i++) {
                if (array[i] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ensures that after this operation the {@code ArrayList} can hold the
     * specified number of elements without further growing.
     * 
     * @param minimumCapacity
     *            the minimum capacity asked for.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (array.length < minimumCapacity) {
            // REVIEW: Why do we check the firstIndex first? Growing
            //         the end makes more sense
            if (firstIndex > 0) {
                growAtFront(minimumCapacity - array.length);
            } else {
                growAtEnd(minimumCapacity - array.length);
            }
        }
    }

    @Override
    public E get(int location) {
        int size = lastIndex - firstIndex;
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException(
                // luni.0A=Index: {0}, Size: {1}
                Messages.getString("luni.0A", //$NON-NLS-1$
                        Integer.valueOf(location),
                        Integer.valueOf(size)));
        }
        return array[firstIndex + location];
    }

    private void growAtEnd(int required) {
        int size = lastIndex - firstIndex;
        if (firstIndex >= required - (array.length - lastIndex)) {
            // REVIEW: Should use size! We don't seem to need newLast
            //         - just use size calculated above
            int newLast = lastIndex - firstIndex;
            // REVIEW: Why not just a !=0 check here? And size cannot
            //         be 0 unless required is 0, which does not happen
            if (size > 0) {
                System.arraycopy(array, firstIndex, array, 0, size);
                int start = newLast < firstIndex ? firstIndex : newLast;
                Arrays.fill(array, start, array.length, null);
            }
            firstIndex = 0;
            lastIndex = newLast;
        } else {
            // REVIEW: If size is 0?
            //         Does size/2 seems a little high!
            int increment = size / 2;
            if (required > increment) {
                increment = required;
            }
            if (increment < 12) {
                increment = 12;
            }
            E[] newArray = newElementArray(size + increment);
            // REVIEW: Just check size !=0? same as earlier check -
            //         can be pulled out to earlier in the method?
            if (size > 0) {
                System.arraycopy(array, firstIndex, newArray, 0, size);
                firstIndex = 0;
                lastIndex = size;
            }
            array = newArray;
        }
    }

    private void growAtFront(int required) {
        int size = lastIndex - firstIndex;
        // REVIEW: replace lastIndex - firstIndex with size ... in all
        //         methods that have it
        if (array.length - lastIndex + firstIndex >= required) {
            int newFirst = array.length - size;
            // REVIEW: as growAtEnd, why not move size == 0 out as
            //         special case
            if (size > 0) {
                System.arraycopy(array, firstIndex, array, newFirst, size);
                // REVIEW: firstIndex + size == lastIndex ??
                int length = firstIndex + size > newFirst ? newFirst
                        : firstIndex + size;
                Arrays.fill(array, firstIndex, length, null);
            }
            firstIndex = newFirst;
            lastIndex = array.length;
        } else {
            int increment = size / 2;
            if (required > increment) {
                increment = required;
            }
            if (increment < 12) {
                increment = 12;
            }
            E[] newArray = newElementArray(size + increment);
            if (size > 0) {
                System.arraycopy(array, firstIndex, newArray, newArray.length
                        - size, size);
            }
            firstIndex = newArray.length - size;
            lastIndex = newArray.length;
            array = newArray;
        }
    }

    private void growForInsert(int location, int required) {
        // REVIEW: we grow too quickly because we are called with the
        //         size of the new collection to add without taking in
        //         to account the free space we already have
        int size = lastIndex - firstIndex;
        int increment = size / 2;
        if (required > increment) {
            increment = required;
        }
        if (increment < 12) {
            increment = 12;
        }
        E[] newArray = newElementArray(size + increment);
        // REVIEW: biased towards leaving space at the beginning?
        //         perhaps newFirst should be (increment-required)/2?
        int newFirst = increment - required;
        // Copy elements after location to the new array skipping inserted
        // elements
        System.arraycopy(array, location + firstIndex, newArray, newFirst
                + location + required, size - location);
        // Copy elements before location to the new array from firstIndex
        System.arraycopy(array, firstIndex, newArray, newFirst, location);
        firstIndex = newFirst;
        lastIndex = size + increment;

        array = newArray;
    }

    @Override
    public int indexOf(Object object) {
        // REVIEW: should contains call this method?
        if (object != null) {
            for (int i = firstIndex; i < lastIndex; i++) {
                if (object.equals(array[i])) {
                    return i - firstIndex;
                }
            }
        } else {
            for (int i = firstIndex; i < lastIndex; i++) {
                if (array[i] == null) {
                    return i - firstIndex;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return lastIndex == firstIndex;
    }

    @Override
    public int lastIndexOf(Object object) {
        if (object != null) {
            for (int i = lastIndex - 1; i >= firstIndex; i--) {
                if (object.equals(array[i])) {
                    return i - firstIndex;
                }
            }
        } else {
            for (int i = lastIndex - 1; i >= firstIndex; i--) {
                if (array[i] == null) {
                    return i - firstIndex;
                }
            }
        }
        return -1;
    }

    /**
     * Removes the object at the specified location from this list.
     * 
     * @param location
     *            the index of the object to remove.
     * @return the removed object.
     * @throws IndexOutOfBoundsException
     *             when {@code location < 0 || >= size()}
     */
    @Override
    public E remove(int location) {
        E result;
        int size = lastIndex - firstIndex;
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException(
                    // luni.0A=Index: {0}, Size: {1}
                    Messages.getString("luni.0A", //$NON-NLS-1$
                            Integer.valueOf(location),
                            Integer.valueOf(size)));
        }
        if (location == size - 1) {
            result = array[--lastIndex];
            array[lastIndex] = null;
        } else if (location == 0) {
            // REVIEW: this if test is simpler why isn't it first?
            //         (this moved up one place during the exception
            //         change but it still probably should move up one more)
            result = array[firstIndex];
            array[firstIndex++] = null;
        } else {
            int elementIndex = firstIndex + location;
            result = array[elementIndex];
            if (location < size / 2) {
                System.arraycopy(array, firstIndex, array, firstIndex + 1,
                                 location);
                array[firstIndex++] = null;
            } else {
                System.arraycopy(array, elementIndex + 1, array,
                                 elementIndex, size - location - 1);
                array[--lastIndex] = null;
            }
        }
        // REVIEW: we can move this to the first if case since it
        //         can only occur when size==1
        if (firstIndex == lastIndex) {
            firstIndex = lastIndex = 0;
        }

        modCount++;
        return result;
    }

    @Override
    public boolean remove(Object object) {
        int location = indexOf(object);
        if (location >= 0) {
            remove(location);
            return true;
        }
        return false;
    }

    /**
     * Removes the objects in the specified range from the start to the end, but
     * not including the end index.
     * 
     * @param start
     *            the index at which to start removing.
     * @param end
     *            the index one after the end of the range to remove.
     * @throws IndexOutOfBoundsException
     *             when {@code start < 0, start > end} or {@code end > size()}
     */
    @Override
    protected void removeRange(int start, int end) {
        // REVIEW: does RI call this from remove(location)
        int size = lastIndex - firstIndex;
        if (start < 0) {
            // REVIEW: message should indicate which index is out of range
            throw new IndexOutOfBoundsException(
                    // luni.0B=Array index out of range: {0}
                    Messages.getString("luni.0B", //$NON-NLS-1$
                                       Integer.valueOf(start)));
        } else if (end > size) {
            // REVIEW: message should indicate which index is out of range
            throw new IndexOutOfBoundsException(
                    // luni.0A=Index: {0}, Size: {1}
                    Messages.getString("luni.0A", //$NON-NLS-1$
                               Integer.valueOf(end), Integer.valueOf(size)));
        } else if (start > end) {
            throw new IndexOutOfBoundsException(
                    // luni.35=Start index ({0}) is greater than end index ({1})
                    Messages.getString("luni.35", //$NON-NLS-1$
                               Integer.valueOf(start), Integer.valueOf(end)));
        }

        if (start == end) {
            return;
        }
        if (end == size) {
            Arrays.fill(array, firstIndex + start, lastIndex, null);
            lastIndex = firstIndex + start;
        } else if (start == 0) {
            Arrays.fill(array, firstIndex, firstIndex + end, null);
            firstIndex += end;
        } else {
            // REVIEW: should this optimize to do the smallest copy?
            System.arraycopy(array, firstIndex + end, array, firstIndex
                             + start, size - end);
            int newLast = lastIndex + start - end;
            Arrays.fill(array, newLast, lastIndex, null);
            lastIndex = newLast;
        }
        modCount++;
    }

    /**
     * Replaces the element at the specified location in this {@code ArrayList}
     * with the specified object.
     * 
     * @param location
     *            the index at which to put the specified object.
     * @param object
     *            the object to add.
     * @return the previous element at the index.
     * @throws IndexOutOfBoundsException
     *             when {@code location < 0 || >= size()}
     */
    @Override
    public E set(int location, E object) {
        int size = lastIndex - firstIndex;
        if (location < 0 || location >= size) {
            throw new IndexOutOfBoundsException(
                    // luni.0A=Index: {0}, Size: {1}
                    Messages.getString("luni.0A", //$NON-NLS-1$
                            Integer.valueOf(location),
                            Integer.valueOf(size)));
        }
        E result = array[firstIndex + location];
        array[firstIndex + location] = object;
        return result;
    }

    /**
     * Returns the number of elements in this {@code ArrayList}.
     * 
     * @return the number of elements in this {@code ArrayList}.
     */
    @Override
    public int size() {
        // REVIEW: We should track firstIndex and size rather than lastIndex
        return lastIndex - firstIndex;
    }

    /**
     * Returns a new array containing all elements contained in this
     * {@code ArrayList}.
     * 
     * @return an array of the elements from this {@code ArrayList}
     */
    @Override
    public Object[] toArray() {
        int size = lastIndex - firstIndex;
        Object[] result = new Object[size];
        System.arraycopy(array, firstIndex, result, 0, size);
        return result;
    }

    /**
     * Returns an array containing all elements contained in this
     * {@code ArrayList}. If the specified array is large enough to hold the
     * elements, the specified array is used, otherwise an array of the same
     * type is created. If the specified array is used and is larger than this
     * {@code ArrayList}, the array element following the collection elements
     * is set to null.
     * 
     * @param contents
     *            the array.
     * @return an array of the elements from this {@code ArrayList}.
     * @throws ArrayStoreException
     *             when the type of an element in this {@code ArrayList} cannot
     *             be stored in the type of the specified array.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
        int size = lastIndex - firstIndex;
        if (size > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, size);
        }
        System.arraycopy(array, firstIndex, contents, 0, size);
        if (size < contents.length) {
            // REVIEW: do we use this incorrectly - i.e. do we null
            //         the rest out?
            contents[size] = null;
        }
        return contents;
    }

    /**
     * Sets the capacity of this {@code ArrayList} to be the same as the current
     * size.
     * 
     * @see #size
     */
    public void trimToSize() {
        int size = lastIndex - firstIndex;
        E[] newArray = newElementArray(size);
        System.arraycopy(array, firstIndex, newArray, 0, size);
        array = newArray;
        firstIndex = 0;
        lastIndex = array.length;
        modCount = 0;
    }

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField(
            "size", Integer.TYPE) }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("size", lastIndex - firstIndex); //$NON-NLS-1$
        stream.writeFields();
        stream.writeInt(array.length);
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            stream.writeObject(it.next());
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        lastIndex = fields.get("size", 0); //$NON-NLS-1$
        array = newElementArray(stream.readInt());
        for (int i = 0; i < lastIndex; i++) {
            array[i] = (E) stream.readObject();
        }
    }
}
