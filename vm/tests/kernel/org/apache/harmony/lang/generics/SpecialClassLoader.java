/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Serguei S.Zapreyev
 * @version $Revision$
 **/

package org.apache.harmony.lang.generics;
import java.io.File;
import java.io.FileInputStream;

@SuppressWarnings(value={"all"}) public class SpecialClassLoader extends ClassLoader {
    public Class findClass(String name) {
        try {
            String s="";
            String s2="";
            if (ClassLoaderTest.flag == 0) {
				s="org"+File.separator+"apache"+File.separator+"harmony"+File.separator+"lang"+File.separator+"generics"+File.separator+"SpecialD";
				s2="org.apache.harmony.lang.generics.SpecialD";
            } else {
				s="org"+File.separator+"apache"+File.separator+"harmony"+File.separator+"lang"+File.separator+"generics"+File.separator+"SpecialC";
                s2="org.apache.harmony.lang.generics.SpecialC";
            }
            FileInputStream fis;
            fis = new FileInputStream(System.getProperty("java.ext.dirs")+File.separator+"classes"+File.separator+s+".class");
            byte[] classToBytes = new byte[fis.available()];
            fis.read(classToBytes);
            return defineClass(s2, classToBytes, 0, classToBytes.length);
        } catch (Exception e) {
            System.err.println("Unexpected exception during classloading: ");
            e.printStackTrace();
            return null;
        }
    }

    public Class checkFind(String name) {
        return findLoadedClass(name);
    }
}
