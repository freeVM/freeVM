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

import java.security.ProtectionDomain;

public class VMClassLoader 
{
    // VMClassLoader was created by looking at the native methods contained in Apache JCHEVM
    static public final native Class defineClass(ClassLoader cl,
       String ss, byte[] ba, int i1, int i2, ProtectionDomain pd) throws ClassFormatError;

    static public final native Class getPrimitiveClass(char cc);

    static public final native Class loadClass(String ss, boolean bb)
       throws java.lang.ClassNotFoundException;

    static public native Class findLoadedClass(ClassLoader cl, String ss);

    static public final native void resolveClass(Class cl);

}
