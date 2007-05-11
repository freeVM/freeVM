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

package org.apache.harmony.archive.util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {
    String s1 = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
    String s2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$

    public void testASCIIIgnoreCaseRegionMatches() {
        for (int i = 0; i < s1.length(); i++) {
            assertTrue(Util.ASCIIIgnoreCaseRegionMatches(s1, i, s2, i, s1
                    .length()
                    - i));
        }
    }

    public void testToASCIILowerCase() {
        assertEquals("abcdefghijklmnopqrstuvwxyz", Util //$NON-NLS-1$
                .toASCIILowerCase("ABCDEFGHIJKLMNOPQRSTUVWXYZ")); //$NON-NLS-1$

        for (int i = 0; i < 255; i++) {
            if (i >= 'a' && i <= 'z') {
                continue;
            }
            if (i >= 'A' && i <= 'Z') {
                continue;
            }
            String cString = "" + (char) i; //$NON-NLS-1$
            assertEquals(cString, Util.toASCIILowerCase(cString));
        }
    }

    public void testToASCIIUpperCase() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", Util //$NON-NLS-1$
                .toASCIIUpperCase("abcdefghijklmnopqrstuvwxyz")); //$NON-NLS-1$

        for (int i = 0; i < 255; i++) {
            if (i >= 'a' && i <= 'z') {
                continue;
            }
            if (i >= 'A' && i <= 'Z') {
                continue;
            }
            String cString = "" + (char) i; //$NON-NLS-1$
            assertEquals(cString, Util.toASCIIUpperCase(cString));
        }
    }
    
    public void testEqualsIgnoreCase(){
        assertTrue(Util.equalsIgnoreCase(s1, s2));
    }

}
