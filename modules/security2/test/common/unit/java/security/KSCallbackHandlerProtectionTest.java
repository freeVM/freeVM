/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security;



import javax.security.auth.callback.CallbackHandler;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyStore.CallbackHandlerProtection> class constructor and methods
 * 
 */

public class KSCallbackHandlerProtectionTest extends TestCase {

    /**
     * Constructor for KSCallbackHandlerProtectionTest.
     * @param arg0
     */
    public KSCallbackHandlerProtectionTest(String arg0) {
        super(arg0);
    }
    
    /**
     * Test for <code>KeyStore.CallbackHandlerProtection(CallbackHandler handler)</code>
     * constructor
     * Assertion: throws NullPointerException when hendler is null
     */
    public void testCallbackHandlerProtection() {
        try {
            new KeyStore.CallbackHandlerProtection(null);
            fail("NullPointerException must be thrown when handler is null");
        } catch (NullPointerException e) {
        }
    }
    
    /**
     * Test for <code>getCallbackHandler()</code> method
     * Assertion: returns CallbackHandler 
     */
    public void testGetCallBackHandler() {
        CallbackHandler cbh = new tmpCallbackHandler();
        KeyStore.CallbackHandlerProtection ksCBH = new KeyStore.CallbackHandlerProtection(cbh);
        assertTrue("Not KeyStore.CallbackHandlerProtection object",
                ksCBH instanceof KeyStore.CallbackHandlerProtection);
        assertEquals("Incorrect CallbackHandler", cbh,
                ksCBH.getCallbackHandler());
    }
}

