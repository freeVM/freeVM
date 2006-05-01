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
    //VMThread was created by looking at the native methods for Apache JCHEVM
    VMThread(Thread t1) 
    {
        this.thread = t1;
        //the below is a diagnostic/debug aid (you can set breakpoint in JCHEVM where this string object is created
        String s1 = "VMThread.<init>(Thread) is not fully implemented";
    }
    VMThread()
    {
        //the below is a diagnostic/debug aid (you can set breakpoint in JCHEVM where this string object is created
        String s1 = "VMThread.<init>() is not fully implemented";
    }
    Thread thread;
    Object vmdata;
    void run() 
    {
        //the below is a diagnostic/debug aid (you can set breakpoint in JCHEVM where this string object is created
        String s1 = "NNNNNNNNNNNNNNNN VMThread.run() has been called";
        thread.run();
    }
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
