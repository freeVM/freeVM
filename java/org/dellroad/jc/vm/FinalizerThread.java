
//
// Copyright 2005 The Apache Software Foundation or its licensors,
// as applicable.
// 
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
// $Id: FinalizerThread.java,v 1.1.1.1 2004/02/20 05:15:26 archiecobbs Exp $
//

package org.dellroad.jc.vm;

/**
 * JC finalizer thread.
 */
class FinalizerThread extends Thread {

	FinalizerThread() {
		super("finalizer");
		setPriority(MIN_PRIORITY);
	}

	public void run() {
		while (true) {
			waitForInterrupt();
			finalizeObjects();
		}
	}

	// VM will call Thread.interrupt() after each GC cycle
	synchronized void waitForInterrupt() {
		try {
			wait();
		} catch (InterruptedException e) {
		}
	}

	static native void finalizeObjects();
}

