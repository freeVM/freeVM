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
 * @author Alexander V. Esin
 * @version $Revision: 1.2 $
 */
package org.apache.harmony.vts.test.vm.jvms.classFile.attributes.annotation.runtimeInvisibleAnnotations.classinfo01;

import java.lang.annotation.*;

public class annotation01p {

   public int test(String [] args) throws Exception {
      Class cl = Class.forName("org.apache.harmony.vts.test.vm.jvms.classFile.attributes.annotation.runtimeInvisibleAnnotations.share.classinfo_annotation");
      if(!cl.isAnnotation()) return 105;
      Annotation [] annotations = cl.getAnnotations();
      if(annotations.length != 0) 
        return 110;
      return 104;
   }

   public static void main(String [] args) throws Exception {
      System.exit((new annotation01p()).test(args));
   }
}
