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
import java.security.Security;

import org.apache.harmony.security.SpiEngUtils;
import org.apache.harmony.security.test.PerformanceTest;



/**
 * Tests for <code>CertPathBuilder</code> class constructors and
 * methods.
 * 
 */

public class CertPathBuilderTest1 extends PerformanceTest {

    /**
     * Constructor for CertPathBuilderTests.
     * @param name
     */
    public CertPathBuilderTest1(String name) {
        super(name);
    }
    public static final String srvCertPathBuilder = "CertPathBuilder";

    public static final String defaultType = "PKIX";    
    public static final String [] validValues = {
            "PKIX", "pkix", "PkiX", "pKiX" };
     
    private static String [] invalidValues = SpiEngUtils.invalidValues;
    
    private static boolean PKIXSupport = false;

    private static Provider defaultProvider;
    private static String defaultProviderName;
    
    private static String NotSupportMsg = "";

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                srvCertPathBuilder);
        PKIXSupport = (defaultProvider != null);
        defaultProviderName = (PKIXSupport ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");
    }
    private static CertPathBuilder[] createCPBs() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertPathBuilder[] certPBs = new CertPathBuilder[3];
            certPBs[0] = CertPathBuilder.getInstance(defaultType);
            certPBs[1] = CertPathBuilder.getInstance(defaultType,
                    defaultProviderName);
            certPBs[2] = CertPathBuilder.getInstance(defaultType,
                    defaultProvider);
            return certPBs;
        } catch (Exception e) {
            return null;
        }
    }    
    
    /**
     * Test for <code>getDefaultType()</code> method
	 * Assertion: 
	 * returns security property "certpathbuild.type" or "PKIX"
     */    
    public void testCertPathBuilder01() {
        if (!PKIXSupport) {
            return;
        }
        String propName = "certpathbuild.type";
        String defCPB = Security.getProperty(propName);
        
        String dt = CertPathBuilder.getDefaultType();
        String resType = defCPB; 
        if (resType == null) {
            resType = defaultType;
        }
        assertNotNull("Default type have not be null", dt);
        assertEquals("Incorrect default type", dt, resType);
        
        if (defCPB == null) {
            Security.setProperty(propName, defaultType);
            dt = CertPathBuilder.getDefaultType();
            resType = Security.getProperty(propName);
            assertNotNull("Incorrect default type", resType);
            assertNotNull("Default type have not be null", dt);
            assertEquals("Incorrect default type", dt, resType);            
        }
    }
    
    /**
     * Test for <code>getInstance(String algorithm)</code> method
	 * Assertion:
	 * throws NullPointerException when algorithm is null 
	 * throws NoSuchAlgorithmException when algorithm  is not correct
	 * or it is not available
     */
    public void testCertPathBuilder02() {
        try {
            CertPathBuilder.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    
    /**
     * Test for <code>getInstance(String algorithm)</code> method
	 * Assertion: returns CertPathBuilder object
     */ 
    public void testCertPathBuilder03() throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            CertPathBuilder cpb = CertPathBuilder.getInstance(validValues[i]);
            assertTrue("Not CertPathBuilder object", cpb instanceof CertPathBuilder);
            assertEquals("Incorrect algorithm", cpb.getAlgorithm(), validValues[i]);
        }
    }
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: throws IllegalArgumentException when provider is null or empty
	 * 
	 * FIXME: verify what exception will be thrown if provider is empty
     */  
    public void testCertPathBuilder04()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {        
            try {
                CertPathBuilder.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
            try {
                CertPathBuilder.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: 
	 * throws NoSuchProviderException when provider has invalid value
     */
    public void testCertPathBuilder05()
            throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++ ) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    CertPathBuilder.getInstance(validValues[i], invalidValues[j]);
                    fail("NoSuchProviderException must be hrown");
                } catch (NoSuchProviderException e1) {
                }
            }
        }        
    }
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: 
	 * throws NullPointerException when algorithm is null 
	 * throws NoSuchAlgorithmException when algorithm  is not correct
     */
    public void testCertPathBuilder06()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathBuilder.getInstance(null, defaultProviderName);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }        
    }
    
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: returns CertPathBuilder object
     */
    public void testCertPathBuilder07()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilder certPB;
        for (int i = 0; i < validValues.length; i++) {
            certPB = CertPathBuilder.getInstance(validValues[i], defaultProviderName);
            assertTrue("Not CertPathBuilder object", certPB instanceof CertPathBuilder);
            assertEquals("Incorrect algorithm", certPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider name", certPB.getProvider().getName(), defaultProviderName);
        }        
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code> method
	 * Assertion: throws IllegalArgumentException when provider is null
     */
    public void testCertPathBuilder08()
            throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        Provider prov = null;
        for (int t = 0; t < validValues.length; t++ ) {
            try {
                CertPathBuilder.getInstance(validValues[t], prov);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e1) {
            }
        }        
    }
    
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: 
	 * throws NullPointerException when algorithm is null 
	 * throws NoSuchAlgorithmException when algorithm  is not correct
     */
    public void testCertPathBuilder09()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathBuilder.getInstance(null, defaultProvider);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], defaultProvider);
                fail("NoSuchAlgorithm must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }
    }
    /**
     * Test for <code>getInstance(String algorithm, String provider)</code> method
	 * Assertion: returns CertPathBuilder object
     */
    public void testCertPathBuilder10()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilder certPB;
        for (int i = 0; i < invalidValues.length; i++) {
            certPB = CertPathBuilder.getInstance(validValues[i], defaultProvider);
            assertTrue("Not CertPathBuilder object", certPB instanceof CertPathBuilder);
            assertEquals("Incorrect algorithm", certPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider name", certPB.getProvider(), defaultProvider);
        }        
    }
    /**
     * Test for <code>build(CertPathParameters params)</code> method
	 * Assertion: throws InvalidAlgorithmParameterException params is null
     */
    public void testCertPathBuilder11()
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            CertPathBuilderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }        
        CertPathBuilder [] certPB = createCPBs();
        assertNotNull("CertPathBuilder objects were not created", certPB);
        for (int i = 0; i < certPB.length; i++ ){
            try {
                certPB[i].build(null);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
        }
    }
    /**
     * Test for 
     * <code>CertPathBuilder</code> constructor
     * Assertion: returns CertPathBuilder object
     */
    public void testCertPathBuilder12()
            throws CertificateException, NoSuchProviderException, 
            NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            CertPathBuilderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathBuilderSpi spi = new MyCertPathBuilderSpi();
        CertPathBuilder certPB = new myCertPathBuilder(spi, 
                    defaultProvider, defaultType);
        assertTrue("Not CertPathBuilder object", certPB instanceof CertPathBuilder);
        assertEquals("Incorrect algorithm", certPB.getAlgorithm(), defaultType);
        assertEquals("Incorrect provider", certPB.getProvider(), defaultProvider);
        try {
            certPB.build(null);
            fail("CertPathBuilderException must be thrown ");
        } catch (CertPathBuilderException e) {            
        }
        certPB = new myCertPathBuilder(null, null, null);
        assertTrue("Not CertPathBuilder object", certPB instanceof CertPathBuilder);
        assertNull("Incorrect algorithm", certPB.getAlgorithm());
        assertNull("Incorrect provider", certPB.getProvider());            
        try {
            certPB.build(null);
            fail("NullPointerException must be thrown ");
        } catch (NullPointerException e) {            
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(CertPathBuilderTest1.class);
    }  
    
}
/**
 * Addifional class to verify CertPathBuilder constructor
 */
class myCertPathBuilder extends CertPathBuilder {

    public myCertPathBuilder(CertPathBuilderSpi spi, Provider prov, String type) {
        super(spi, prov, type);
    }
}