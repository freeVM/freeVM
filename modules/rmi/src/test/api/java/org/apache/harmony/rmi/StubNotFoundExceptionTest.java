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

import java.rmi.StubNotFoundException;

import junit.framework.TestCase;

public class StubNotFoundExceptionTest extends TestCase {

    /**
     * {@link java.rmi.StubNotFoundException#StubNotFoundException(java.lang.String, java.lang.Exception)}.
     */
    public void testStubNotFoundExceptionStringException() {
        NullPointerException npe = new NullPointerException();
        StubNotFoundException e = new StubNotFoundException("fixture", npe);
        assertTrue(e.getMessage().indexOf("fixture") > -1);
        assertSame(npe, e.getCause());
        assertSame(npe, e.detail);
    }

    /**
     * {@link java.rmi.StubNotFoundException#StubNotFoundException(java.lang.String)}.
     */
    public void testStubNotFoundExceptionString() {
        StubNotFoundException e = new StubNotFoundException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
        assertNull(e.detail);
    }

}
