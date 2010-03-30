/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


public class DirectByteBufferTest {

    static { System.loadLibrary("DirectByteBufferTest");}

    public static void main(String[] args) {
        new DirectByteBufferTest().testValidBuffer();
    }

    private native String testValidBuffer0();
    
    public void testValidBuffer() {
        assertNull(testValidBuffer0());
    }
    
    public void assertNull(Object o) {
        if (o == null) {
            System.out.println("PASSED");
        } else {
            fail(o.toString());
        }
    }
    
    public void fail(String s) {
        System.out.println("FAILED: " + s);
    }
}
