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

/**
 * A kind of collection provides advanced operations than other basic
 * collections, such as insertion, extraction, and inspection.
 * 
 * Generally, a queue orders its elements by means of first-in-first-out. While
 * priority queue orders its elements according to a comparator specified or the
 * elements' natural order. Furthermore, a stack orders its elements
 * last-in-first out.
 * 
 * A typical queue does not allow null to be inserted as its element, while some
 * implementations such as LinkedList allow it. But null should not be inserted
 * even in these implementations, since method poll return null to indicate that
 * there is no element left in the queue.
 * 
 * Queue does not provide blocking queue methods, which will block until the
 * operation of the method is allowed. BlockingQueue interface defines such
 * methods.
 */
public interface Queue<E> extends Collection<E> {

    /**
     * Inserts the specified element into the queue provided that the condition
     * allows such an operation. The method is generally preferable to the
     * collection.add(E), since the latter might throw an exception if the
     * operation fails.
     * 
     * @param o
     *            the specified element to insert into the queue.
     * @return true if the operation succeeds and false if it fails.
     */
    public boolean offer(E o);

    /**
     * Gets and removes the element in the head of the queue, or returns null if
     * there is no element in the queue.
     * 
     * @return the element in the head of the queue or null if there is no
     *         element in the queue.
     */
    public E poll();

    /**
     * Gets and removes the element in the head of the queue. Throws a
     * NoSuchElementException if there is no element in the queue.
     * 
     * @return the element in the head of the queue.
     * @throws NoSuchElementException
     *             if there is no element in the queue.
     */
    public E remove();

    /**
     * Gets but not removes the element in the head of the queue, or throws
     * exception if there is no element in the queue.
     * 
     * @return the element in the head of the queue or null if there is no
     *         element in the queue.
     */
    public E peek();

    /**
     * Gets but not removes the element in the head of the queue. Throws a
     * NoSuchElementException if there is no element in the queue.
     * 
     * @return the element in the head of the queue.
     * @throws NoSuchElementException
     *             if there is no element in the queue.
     */
    public E element();

}
