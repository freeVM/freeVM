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

package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.SpiEngUtils;
import junit.framework.TestCase;


/**
 * Tests for KeyGenerator constructor and methods
 * 
 */

public class KeyGeneratorTest1 extends TestCase {

    /**
     * Constructor for KeyGeneratorTest1.
     * 
     * @param arg0
     */
    public KeyGeneratorTest1(String arg0) {
        super(arg0);
    }
    
    public static final String srvKeyGenerator = "KeyGenerator";
    
    public static final String validAlgorithmsKeyGenerator [] =
        {"DESede", "DES", "Blowfish", "AES", "HmacMD5"};
    
    private static final int [] validKeySizes = { 168, 56, 56, 256, 56};

    private static int defaultKeySize = -1;
    
    private static String defaultAlgorithm = null;
    
    private static String defaultProviderName = null;

    private static Provider defaultProvider = null;

    private static boolean DEFSupported = false;

    private static final String NotSupportMsg = "There is no suitable provider for KeyGenerator";

    private static final String[] invalidValues = SpiEngUtils.invalidValues;

    private static String[] validValues = new String[3];

    static {
        for (int i = 0; i < validAlgorithmsKeyGenerator.length; i++) {
            defaultProvider = SpiEngUtils.isSupport(validAlgorithmsKeyGenerator[i],
                srvKeyGenerator);
            DEFSupported = (defaultProvider != null);
            if (DEFSupported) {
                defaultAlgorithm = validAlgorithmsKeyGenerator[i];
                defaultKeySize = validKeySizes[i];
                defaultProviderName = defaultProvider.getName();                
                validValues[0] = defaultAlgorithm;
                validValues[1] = defaultAlgorithm.toUpperCase();
                validValues[2] = defaultAlgorithm.toLowerCase();
                break;
            }
        }
    }
    
    private KeyGenerator[] createKGs() throws Exception {
        if (!DEFSupported) {
            fail(NotSupportMsg);
        }

        KeyGenerator [] kg = new KeyGenerator[3];
        kg[0] = KeyGenerator.getInstance(defaultAlgorithm);
        kg[1] = KeyGenerator.getInstance(defaultAlgorithm, defaultProvider);
        kg[2] = KeyGenerator.getInstance(defaultAlgorithm, defaultProviderName);
        return kg;
    }


    /**
     * Test for <code>KeyGenerator</code> constructor Assertion: returns
     * KeyGenerator object
     */
    public void testKeyGenerator() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyGeneratorSpi spi = new MyKeyGeneratorSpi();
        KeyGenerator keyG = new myKeyGenerator(spi, defaultProvider,
                defaultAlgorithm);
        assertEquals("Incorrect algorithm", keyG.getAlgorithm(),
                defaultAlgorithm);
        assertEquals("Incorrect provider", keyG.getProvider(), defaultProvider);
        AlgorithmParameterSpec params = null;
        int keysize = 0;
        try {
            keyG.init(params, null);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        try {
            keyG.init(keysize, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        keyG = new myKeyGenerator(null, null, null);
        assertNull("Algorithm must be null", keyG.getAlgorithm());
        assertNull("Provider must be null", keyG.getProvider());

        try {
            keyG.init(params, null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            keyG.init(keysize, null);
            fail("NullPointerException or InvalidParameterException must be thrown");
        } catch (InvalidParameterException e) {
        } catch (NullPointerException e) {
        }
    }

    /*
     * Test for <code> getInstance(String algorithm) </code> method Assertions:
     * throws NullPointerException when algorithm is null throws
     * NoSuchAlgorithmException when algorithm isnot available
     */
    public void testGetInstanceString01() throws NoSuchAlgorithmException {
        try {
            KeyGenerator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException should be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    /*
     * Test for <code> getInstance(String algorithm) </code> method 
     * Assertions: returns KeyGenerator object
     */
    public void testGetInstanceString02() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyGenerator keyG;
        for (int i = 0; i < validValues.length; i++) {
            keyG = KeyGenerator.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", keyG.getAlgorithm(), validValues[i]);
        }
    }

    /*
     * Test for <code> getInstance(String algorithm, String provider)</code> method 
     * Assertions:
     * throws NullPointerException when algorithm is null
     * throws NoSuchAlgorithmException when algorithm isnot available
     */
    public void testGetInstanceStringString01() throws
            NoSuchAlgorithmException, IllegalArgumentException, 
            NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            KeyGenerator.getInstance(null, defaultProviderName);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyGenerator.getInstance(invalidValues[i], defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    /*
     * Test for <code> getInstance(String algorithm, String provider)</code> method 
     * Assertions:
     * throws IllegalArgumentException when provider is null or empty
     * throws NoSuchProviderException when provider has not be configured
     */
    public void testGetInstanceStringString02() throws IllegalArgumentException,
            NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                KeyGenerator.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                KeyGenerator.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty");
            } catch (IllegalArgumentException e) {
            }
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyGenerator.getInstance(validValues[i], invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(validValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }

    /*
     * Test for <code> getInstance(String algorithm, String provider)</code> method 
     * Assertions: returns KeyGenerator object
     */
    public void testGetInstanceStringString03() throws IllegalArgumentException,
            NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyGenerator keyG;
        for (int i = 0; i < validValues.length; i++) {
            keyG = KeyGenerator.getInstance(validValues[i], defaultProviderName);
            assertEquals("Incorrect algorithm", keyG.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider", keyG.getProvider().getName(), defaultProviderName);
        }
    }

    /*
     * Test for <code> getInstance(String algorithm, Provider provider)</code> method 
     * Assertions:
     * throws NullPointerException when algorithm is null
     * throws NoSuchAlgorithmException when algorithm isnot available
     */
    public void testGetInstanceStringProvider01() throws NoSuchAlgorithmException, 
            IllegalArgumentException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        try {
            KeyGenerator.getInstance(null, defaultProvider);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown if algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyGenerator.getInstance(invalidValues[i], defaultProvider);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    /*
     * Test for <code> getInstance(String algorithm, Provider provider)</code> method 
     * Assertions:
     * throws IllegalArgumentException when provider is null
     */
    public void testGetInstanceStringProvider02() throws NoSuchAlgorithmException, 
            IllegalArgumentException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyGenerator.getInstance(invalidValues[i], provider);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    
    /*
     * Test for <code> getInstance(String algorithm, Provider provider)</code> method 
     * Assertions: returns KeyGenerator object
     */
    public void testGetInstanceStringProvider03() throws IllegalArgumentException,
            NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyGenerator keyA;
        for (int i = 0; i < validValues.length; i++) {
            keyA = KeyGenerator.getInstance(validValues[i], defaultProvider);
            assertEquals("Incorrect algorithm", keyA.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider", keyA.getProvider(), defaultProvider);
        }
    }

    /*
     * Test for <code>init(int keysize)</code> and
     * <code>init(int keysize, SecureRandom random)</code> methods 
     * Assertion: throws InvalidParameterException if keysize is wrong
     * 
     */    
    public void testInitKey() throws Exception {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        if (defaultAlgorithm
                .equals(validAlgorithmsKeyGenerator[validAlgorithmsKeyGenerator.length - 1])) {
            return;
        }
        int[] size = { Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE };
        KeyGenerator[] kgs = createKGs();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < kgs.length; i++) {
            for (int j = 0; j < size.length; j++) {
                try {
                    kgs[i].init(size[j]);
                } catch (InvalidParameterException ignore) {
                }

                try {
                    kgs[i].init(size[j], random);
                } catch (InvalidParameterException ignore) {
                }
            }
        }
    }

    /*
     * Test for <code>init(AlgorithmParameterSpec params)</code> and 
     * <code>init(AlgorithmParameterSpec params, SecureRandom random)</code> methods
     * Assertion: throws InvalidAlgorithmParameterException when params is null
     */
    public void testInitParams() throws Exception {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        KeyGenerator [] kgs = createKGs();
        AlgorithmParameterSpec aps = null;

        for (int i = 0; i < kgs.length; i++) {
            try {
                kgs[i].init(aps);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
            try {
                kgs[i].init(aps, new SecureRandom());
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
        }
    }

    /*
     * Test for <code>generateKey()</code> and
     * <code>init(SecureRandom random)</code> methods 
     * <code>init(int keysize, SecureRandom random)</code> methods 
     * <code>init(int keysize)</code> methods 
     * <code>init(AlgorithmParameterSpec params, SecureRandom random)</code> methods 
     * <code>init(AlgorithmParameterSpec params)</code> methods 
     * Assertions:
     * initializes KeyGenerator; 
     * returns SecretKey object
     * 
     */ 
    public void testGenerateKey() throws Exception {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SecretKey sKey;
        String dAl = defaultAlgorithm.toUpperCase();

        KeyGenerator[] kgs = createKGs();

        for (int i = 0; i < kgs.length; i++) {
            sKey = kgs[i].generateKey();
            assertEquals("Incorect algorithm", sKey.getAlgorithm()
                    .toUpperCase(), dAl);
            kgs[i].init(new SecureRandom());
            sKey = kgs[i].generateKey();
            assertEquals("Incorect algorithm", sKey.getAlgorithm()
                    .toUpperCase(), dAl);
            kgs[i].init(defaultKeySize);
            sKey = kgs[i].generateKey();
            assertEquals("Incorect algorithm", sKey.getAlgorithm()
                    .toUpperCase(), dAl);
            kgs[i].init(defaultKeySize, new SecureRandom());
            sKey = kgs[i].generateKey();
            assertEquals("Incorect algorithm", sKey.getAlgorithm()
                    .toUpperCase(), dAl);
        }
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(KeyGeneratorTest1.class);
    }     
}

/**
 * Additional class for KeyGenerator constructor verification
 */
class myKeyGenerator extends KeyGenerator {

    public myKeyGenerator(KeyGeneratorSpi keyAgreeSpi, Provider provider,
            String algorithm) {
        super(keyAgreeSpi, provider, algorithm);
    }
}
