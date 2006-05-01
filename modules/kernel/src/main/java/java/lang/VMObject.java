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

public class VMObject 
{
    //VMObject was created by looking at the native methods contained in Apache JCHEVM
    static public final native Object clone(Cloneable clo);

    static public final native Class getClass(Object obj);

    static public final native void wait(Object obj, long time1, int time2)
      throws java.lang.IllegalMonitorStateException,
        java.lang.InterruptedException;

    static public final native void notify(Object obj)
        throws java.lang.IllegalMonitorStateException;

    static public final native void notifyAll(Object obj)
        throws java.lang.IllegalMonitorStateException;
}
