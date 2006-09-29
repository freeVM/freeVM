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

import java.lang.reflect.*;

public class VMClass 
{
    // VMClass was created by looking at the native methods contained in Apache JCHEVM
    static public final native Class forName(String ss, boolean bb, ClassLoader cl)
        throws java.lang.ClassNotFoundException;

    static public final native ClassLoader getClassLoader(Class cl);

    static public final native Class getComponentType(Class cl);

    static public final native Class[] getDeclaredClasses(Class cl, boolean bb);

    static public final native Constructor[] getDeclaredConstructors(Class cl, boolean bb);

    static public final native Field[] getDeclaredFields(Class cl, boolean bl);

    static public final native Method[] getDeclaredMethods(Class cl, boolean bl);

    static public final native Class getDeclaringClass(Class cl);

    static public final native Class[] getInterfaces(Class cl);

    static public final native int getModifiers(Class cl, boolean bb);

    static public final native String getName(Class cl);

    static public final native Class getSuperclass(Class cl);

    static public final native boolean isArray(Class cl);

    static public final native boolean isAssignableFrom(Class cl, Class cl2);

    static public final native boolean isInstance(Class cl, Object obj);

    static public final native boolean isInterface(Class cl);

    static public final native boolean isPrimitive(Class cl);

    static public final native void throwException(Throwable thr);




    
}
