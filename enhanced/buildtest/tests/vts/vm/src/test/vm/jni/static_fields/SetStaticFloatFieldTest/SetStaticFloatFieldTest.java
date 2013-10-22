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
 * @author Gregory Shimansky, Petr Ivanov
 * @version $Revision: 1.3 $
 */  
/*
 * Created on 15.11.2004
 */
package org.apache.harmony.vts.test.vm.jni.static_fields;

import org.apache.harmony.vts.test.vm.jni.share.JNITest;

/**
 * @author Gregory Shimansky
 *
 * Test for SetStaticFloatField function.
 */
public class SetStaticFloatFieldTest extends JNITest {
    private native boolean nativeExecute(Class cl, String field, float val); 

    /**
     * Native code tries to set a field of an object
     * to a value. Test checks that field value is correct.
     * @see org.apache.harmony.vts.test.vm.jni.share.JNITest#execute()
     */
    public boolean execute() throws Exception {
        boolean res1, res2, res3;
        float arg1 = 19.19E-12f, arg2 = -732.22E+11f, arg3 = 17.09f;
        float f1, f2, f3;

        res1 = nativeExecute(TestClass.class, "fpub", arg1);
        res2 = nativeExecute(TestClass.class, "fprot", arg2);
        res3 = nativeExecute(TestClass.class, "fpriv", arg3);
        f1 = TestClass.getFpub();
        f2 = TestClass.getFprot();
        f3 = TestClass.getFpriv();

        return res1 && res2 && res3 && f1 == arg1 && f2 == arg2 && f3 == arg3;
    }
    public static void main(String[] args){
        System.exit(new SetStaticFloatFieldTest().test());
    }
}