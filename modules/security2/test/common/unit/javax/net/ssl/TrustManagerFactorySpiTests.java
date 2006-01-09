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

package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.TrustManagerFactorySpi;
import com.openintel.drl.security.test.PerformanceTest;

/**
 * Tests for <code>TrustManagerFactorySpi</code> class constructors and
 * methods.
 * 
 */

public class TrustManagerFactorySpiTests extends PerformanceTest {
    /**
     * Constructor for TrustManegerFactorySpiTests.
     * 
     * @param arg0
     */
    public TrustManagerFactorySpiTests(String arg0) {
        super(arg0);
    }

    /**
     * Test for <code>TrustManagerFactorySpi</code> constructor 
     * Assertion: constructs TrustManagerFactorySpi
     */
    public void testTrustManagerFactorySpi01() throws InvalidAlgorithmParameterException {
        TrustManagerFactorySpi kmfSpi = (TrustManagerFactorySpi) new MyTrustManagerFactorySpi();        
        assertTrue("Not TrustManagerFactorySpi",
                kmfSpi instanceof TrustManagerFactorySpi);
        assertNull("Not null results", kmfSpi.engineGetTrustManagers());
        KeyStore kStore = null;
        ManagerFactoryParameters mfp = null;
        
        try {
            kmfSpi.engineInit(kStore);
            fail("KeyStoreException must be thrown");
        } catch (KeyStoreException e) {
        }
        try {
            kmfSpi.engineInit(mfp);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        assertNull("getTrustManagers() should return null object", 
                kmfSpi.engineGetTrustManagers());     
        
        try {
            kStore = KeyStore.getInstance(KeyStore.getDefaultType());
            kStore.load(null, null);            
        } catch (KeyStoreException e) {
            fail("default keystore is not supported");
            return;
        } catch (Exception e) {
            fail("Unexpected: "+e.toString());
            return;            
        }
        try {
            kmfSpi.engineInit(kStore);
        } catch (KeyStoreException e) {
            fail("Unexpected KeyStoreException was thrown");            
        }
        mfp = (ManagerFactoryParameters) new MyTrustManagerFactorySpi.Parameters(null);
        try {
            kmfSpi.engineInit(mfp);
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertTrue("Incorrect exception", e.getCause() instanceof KeyStoreException);
        } catch (InvalidAlgorithmParameterException e) {
            fail("Unexpected: ".concat(e.toString()));
        }
        mfp = (ManagerFactoryParameters) new MyTrustManagerFactorySpi.Parameters(kStore);
        kmfSpi.engineInit(mfp);
    }
}