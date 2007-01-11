/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance    
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package java.awt.image;

import junit.framework.TestCase;

public class ComponentSampleModelTest extends TestCase {
  
    public void testGetPixelsMaxValue()  throws Exception {
        ComponentSampleModel csm = new ComponentSampleModel(0, 10, 10, 1, 10, new int[]{0}); 
        DataBufferInt dbi = new DataBufferInt(100); 

        try { 
            csm.getPixels(8, Integer.MAX_VALUE, 1, 1, (int[]) null, dbi);
            fail("Exception expected");
        } catch(ArrayIndexOutOfBoundsException expectedException) { 
            // expected
        } 
    }
}
