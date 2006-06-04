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
package java.lang;

class VMThread 
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

            synchronized (this) {
                running = false;
                notifyAll();
            }
        } catch (Throwable t) {
            System.err.println("Thread dead with exception:");
            t.printStackTrace();
        }
        destroy();
    }

    final boolean isAlive() {
        synchronized (this) {
            return running;
        }
    }

    private final native void attach();
    private final native void destroy();


    final native int countStackFrames();

    static final native Thread currentThread();

    final native void interrupt();

    static final native boolean interrupted();

    final native boolean isInterrupted();

    final native void nativeSetPriority(int pri);

    final native void nativeStop(Throwable thr);

    final native void resume();

    final native void start(long stacklength);

    final native void suspend();

    static final native void yield();

}
