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
 */

package java.lang;

import junit.framework.TestCase;

/*
 * Created on March 29, 2006
 *
 * This RuntimeAdditionalTest class is used to test the Core API Runtime class
 * 
 */

/**
 * ###############################################################################
 * ###############################################################################
 * TODO: 1.
 * ###############################################################################
 * ###############################################################################
 */

public class RuntimeAdditionalTest28 extends TestCase {
    /**
     * wait for (via exitValue loop) finish of the java process then destroy,
     * get jvm process' streams, read err stream, then exitValue
     */
    public void test_28() {
        System.out.println("==test_28===");
        if (RuntimeAdditionalTest0.os.equals("Unk")) {
            fail("WARNING (test_28): unknown operating system.");
        }
        try {
            String cmnd = RuntimeAdditionalTest0.javaStarter+" MAIN";
            Process pi3 = Runtime.getRuntime().exec(cmnd);
            while (true) {
                try {
                    Thread.sleep(50);
                    /*System.out.println(*/pi3.exitValue()/*)*/;
                    break;
                } catch (IllegalThreadStateException e) {
                    continue;
                }
            }
            pi3.destroy();
            Thread.sleep(100);
            pi3.getOutputStream();
            java.io.InputStream es = pi3.getErrorStream();
            pi3.getInputStream();
            /*System.out.println(*/pi3.waitFor()/*)*/;
            int ia;
            while (true) {
                while ((ia = es.available()) != 0) {
                    byte[] bbb = new byte[ia];
                    es.read(bbb);
                    //System.out.println(new String(bbb));
                }
                try {
                    pi3.exitValue();
                    while ((ia = es.available()) != 0) {
                        byte[] bbb = new byte[ia];
                        es.read(bbb);
                        //System.out.println(new String(bbb));
                    }
                    break;
                } catch (IllegalThreadStateException e) {
                    continue;
                }
            }
            /*System.out.println(*/pi3.exitValue()/*)*/;
        } catch (Exception eeee) {
            eeee.printStackTrace();
            fail("ERROR (test_28): unexpected exception.");
        }
    }

}