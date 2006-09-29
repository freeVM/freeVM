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
package java.lang;

final class VMThread 
{
    VMThread(Thread t1) 
    {
        this.thread = t1;
        this.vmdata = null;
    }

    final Thread thread;
    Object vmdata;
    private boolean started = false;
    private boolean running = false;

    void run() {
        attach();
        try {
            synchronized (this) {
                if (started) return;
                started = true;
                running = true;
                notifyAll();
            }

            thread.run();

        } catch (Throwable t) {
			try {
				if (thread.group != null)
					thread.group.uncaughtException(thread, t);
			} catch(Throwable ignore) {
				// ignore double fault
			}
        } finally {
            synchronized (this) {
                running = false;
                notifyAll();
            }
		}
        destroy();
    }

    boolean isAlive() {
        synchronized (this) {
            return running;
        }
    }

	void stop(Throwable t) {
		nativeStop(t);
	}

    synchronized void join(long millis, int nanos) throws InterruptedException {
		if (nanos != 0)
			millis++;		// we only use millisecond precision
		long finish = 0;
		if (millis != 0) {
			long now = System.currentTimeMillis();
			finish = now + millis;
			if (finish < now)
				finish = Long.MAX_VALUE;	// handle overflow
		}
		for (long remain = millis; !started || running; ) {
			wait(remain);
			if (finish != 0) {
				remain = finish - System.currentTimeMillis();
				if (remain <= 0)
					break;
			}
		}
	}

    static void sleep(long millis, int nanos) throws InterruptedException {
		if (nanos != 0)
			millis++;		// we only use millisecond precision
		long now = System.currentTimeMillis();
		long finish = now + millis;
		if (finish < now)
			finish = Long.MAX_VALUE;	// handle overflow
		Object sleeper = new Object();
		synchronized (sleeper) {
			while (now < finish) {
				sleeper.wait(finish - now, 0);
				now = System.currentTimeMillis();
			}
		}
	}

    private native void attach();
    private native void destroy();

    native int countStackFrames();

    static native Thread currentThread();

    native void interrupt();

    static native boolean interrupted();

    native boolean isInterrupted();

    native void nativeSetPriority(int pri);

    native void nativeStop(Throwable thr);

    native void resume();

    native void start(long stacklength);

    native void suspend();

    static native void yield();
}

