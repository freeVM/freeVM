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

package java.security.cert;


import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

import org.apache.harmony.security.SpiEngUtils;
import org.apache.harmony.security.cert.MyCertPath;
import org.apache.harmony.security.cert.TestUtils;
import junit.framework.TestCase;

/**
 * Tests for <code>CertPathValidator</code> class  methods.
 * 
 */

public class CertPathValidator3Test extends TestCase {

    /**
     * Constructor for CertPathValidatorTests.
     * @param name
     */
    public CertPathValidator3Test(String name) {
        super(name);
    }
    private static final String defaultType = CertPathBuilder1Test.defaultType;    
    private static final String [] validValues = CertPathBuilder1Test.validValues;
     
    private static String [] invalidValues = SpiEngUtils.invalidValues;
    
    private static boolean PKIXSupport = false;

    private static Provider defaultProvider;
    private static String defaultProviderName;
    
    private static String NotSupportMsg = "";

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                CertPathValidator1Test.srvCertPathValidator);
        PKIXSupport = (defaultProvider != null);
        defaultProviderName = (PKIXSupport ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");
    }
    
    private static CertPathValidator[] createCPVs() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertPathValidator[] certPVs = new CertPathValidator[3];
            certPVs[0] = CertPathValidator.getInstance(defaultType);
            certPVs[1] = CertPathValidator.getInstance(defaultType,
                    defaultProviderName);
            certPVs[2] = CertPathValidator.getInstance(defaultType,
                    defaultProvider);
            return certPVs;
        } catch (Exception e) {
            return null;
        }
    }    
    /**
     * Test for <code>validate(CertPath certpath, CertPathParameters params)</code> method
	 * Assertion: throws InvalidAlgorithmParameterException 
	 * when params is instance of PKIXParameters and
	 * certpath is not X.509 type
	 * 
	 * FIXME: jrockit-j2re1.4.2_04 throws NullPointerException when certPath is null
     */
    public void testValidate01()
            throws NoSuchAlgorithmException, NoSuchProviderException, 
                    CertPathValidatorException, InvalidAlgorithmParameterException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        MyCertPath mCP = new MyCertPath(new byte[0]);
        CertPathParameters params = (CertPathParameters)new PKIXParameters(TestUtils.getTrustAnchorSet()); 
        CertPathValidator [] certPV = createCPVs();
        assertNotNull("CertPathValidator objects were not created", certPV);
        for (int i = 0; i < certPV.length; i++) {            
            try {
                certPV[i].validate(mCP, null);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
            try {
                certPV[i].validate(null, params);
                fail("NullPointerException must be thrown");
            } catch(NullPointerException e) {
            }            
        }
    }

}
