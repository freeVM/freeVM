/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.rmi;

import java.rmi.RMISecurityException;

import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class RMISecurityExceptionTest extends TestCase {

    /**
     * {@link java.rmi.RMISecurityException#RMISecurityException(java.lang.String, java.lang.String)}.
     */
    public void testRMISecurityExceptionStringString() {
        RMISecurityException e = new RMISecurityException("fixture", "ignored");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

    /**
     * {@link java.rmi.RMISecurityException#RMISecurityException(java.lang.String)}.
     */
    public void testRMISecurityExceptionString() {
        RMISecurityException e = new RMISecurityException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

}
