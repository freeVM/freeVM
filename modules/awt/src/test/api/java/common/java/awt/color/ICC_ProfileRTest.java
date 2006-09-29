/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
  * @author Oleg V. Khaschansky
  * @version $Revision$
  * 
  */
   
package java.awt.color;

import junit.framework.TestCase;

public class ICC_ProfileRTest extends TestCase {

    public void testGetInstance() {
        try {
            byte[]ba = new byte [] {(byte)0x58, (byte)0x59, (byte)0x5A, (byte)0x20,
                        (byte) 0x00, (byte) 0x00,(byte) 0x00,(byte) 0x00,
                        (byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,
                        (byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,
                        (byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};

            ICC_Profile iccp = ICC_Profile.getInstance(ba);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("IllegalArgumentExceptione expected");
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ICC_ProfileRTest.class);
    }
}