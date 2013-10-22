/*
    Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
*/
/** 
 * @author Maxim V. Makarov
 * @version $Revision: 1.1 $
 */
package org.apache.harmony.vts.test.vm.jvms.classFile.attributes.annotation.annotationDefault.methodinfo04;

import java.lang.annotation.*;
import java.lang.reflect.*;

public class annotation04p {

   public int test(String [] args) throws Exception {
      Class cl = Class.forName("org.apache.harmony.vts.test.vm.jvms.classFile.attributes.annotation.annotationDefault.methodinfo04.annotation04");
      Object o = cl.getMethod("value", new Class[]{}).getDefaultValue();
      
      if(new Byte((byte)127).equals(o)) 
          return 104;
      return 105;
   }

   public static void main(String [] args) throws Exception {
      System.exit((new annotation04p()).test(args));
   }
}