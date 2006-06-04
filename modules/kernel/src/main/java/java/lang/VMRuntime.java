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

public class VMRuntime 
{
    //this class was created by looking at the native methods contained in Apache JCHEVM
    static final native int availableProcessors();
    static final native void exit(int code);
    static final native long freeMemory();
    static final native void gc();
    static final native String mapLibraryName(String ss);
    static final native long maxMemory();
    static final native int nativeLoad(String ss, ClassLoader cl);
    static final native void runFinalization();
    static final native void runFinalizationForExit();
    static final native void runFinalizersOnExit(boolean bb);
    static final native long totalMemory();
    static final native void traceInstructions(boolean bb);
    static final native void traceMethodCalls(boolean bb);

}
