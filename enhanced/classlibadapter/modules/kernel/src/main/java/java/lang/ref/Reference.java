/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.ref;

/**
 * This class must be implemented by the vm vendor. The documented methods must
 * be implemented to support the provided subclass implementations. As the
 * provided subclass implementations are trivial and simply call
 * initReference(Object) and initReference(Object, ReferenceQueue) from their
 * constructors, the vm vendor may elect to implement the subclasses as well.
 * Abstract class which describes behavior common to all reference objects.
 * 
 * @since JDK1.2
 */
public abstract class Reference<T> extends Object {

    T referent;
    ReferenceQueue<T> queue;
    private static ReferenceQueue dummy = new ReferenceQueue();

	/**
	 * Make the referent null. This does not force the reference object to be
	 * enqueued.
	 * 
	 */
	public void clear() {
        referent = null;
		return;
	}

	/**
	 * Force the reference object to be enqueued if it has been associated with
	 * a queue.
	 * 
	 * @return boolean true if Reference is enqueued. false otherwise.
	 */
	public boolean enqueue() {
        ReferenceQueue<T> q;
        synchronized (this) {
            if (queue == null || queue == dummy) return false;
            q = queue;
            queue = null;
        }

        try {
            q.enqueue(this);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
	}

	/**
	 * Return the referent of the reference object.
	 * 
	 * @return Object Referent to which reference refers, or null if object has
	 *         been cleared.
	 */
	public T get() {
		return referent;
	}

	/**
	 * Return whether the reference object has been enqueued.
	 * 
	 * @return boolean true if Reference has been enqueued. false otherwise.
	 */
	public boolean isEnqueued() {
		return queue == null;
	}

	/**
	 * Constructs a new instance of this class.
	 * 
	 */
	Reference() {
		super();
	}

	/**
	 * Implement this method to support the provided subclass implementations.
	 * Initialize a newly created reference object. Associate the reference
	 * object with the referent.
	 * 
	 * @param r
	 *            the referent
	 */
	void initReference(T r) {
        referent = r;
        queue = dummy;
		return;
	}

	/**
	 * Implement this method to support the provided subclass implementations.
	 * Initialize a newly created reference object. Associate the reference
	 * object with the referent, and the specified ReferenceQueue.
	 * 
	 * @param r
	 *            the referent
	 * @param q
	 *            the ReferenceQueue
	 */
	void initReference(T r, ReferenceQueue<T> q) {
        referent = r;
        queue = q;
		return;
	}

	/**
	 * Called when a Reference has been removed from its ReferenceQueue.
	 * Set the enqueued field to false.
	 */
	void dequeue() {
		return;
	}
}

