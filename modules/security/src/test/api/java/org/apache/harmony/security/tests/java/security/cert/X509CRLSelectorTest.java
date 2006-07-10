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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security.cert;

import java.io.IOException;
import java.security.cert.X509CRLSelector;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

/**
 */

public class X509CRLSelectorTest extends TestCase {

    /**
     * @tests java.security.cert.X509CRLSelector#addIssuer(javax.security.auth.x500.X500Principal)
     */
    public void test_addIssuerLjavax_security_auth_x500_X500Principal()
            throws Exception {
        //Regression for HARMONY-465
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuer((X500Principal) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.security.cert.X509CRLSelector#addIssuerName(java.lang.String)
     */
    public void test_addIssuerNameLjava_lang_String() throws Exception {
        //Regression for HARMONY-465
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName("234");
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.security.cert.X509CRLSelector#addIssuerName(byte[])
     */
    public void test_addIssuerName$B_3() throws Exception {
        //Regression for HARMONY-465
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName(new byte[] { (byte) 2, (byte) 3, (byte) 4 });
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.security.cert.X509CRLSelector#addIssuerName(byte[])
     */
    public void test_addIssuerName$B_4() throws Exception {
        //Regression for HARMONY-465
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName((byte[]) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * addIssuerName(String name) method testing.
     */
    public void testAddIssuerName() throws IOException {
        //Regression for HARMONY-736
        X509CRLSelector selector = new X509CRLSelector();
        try {
            selector.addIssuerName("a");
            fail("IOException expected");
        } catch (IOException e) {}

        //no exception for null
        selector.addIssuerName((String) null);
    }
}
