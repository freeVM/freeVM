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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security.spec;

import org.apache.harmony.security.test.PerformanceTest;

/**
 * Test for MGF1ParameterSpec class
 * 
 */
public class MGF1ParameterSpecTest extends PerformanceTest {

    /**
     * Meaningless algorithm name just for testing purposes
     */
    private static final String testAlgName = "TEST";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for MGF1ParameterSpecTest.
     * @param arg0
     */
    public MGF1ParameterSpecTest(String arg0) {
        super(arg0);
    }

    //
    // Tests
    //

    /**
     * Test #1 for <code>MGF1ParameterSpec</code> constructor<br>
     * Assertion: constructs new <code>MGF1ParameterSpec</code>
     * object using valid parameter
     */
    public final void testMGF1ParameterSpec01() {
        AlgorithmParameterSpec aps = new MGF1ParameterSpec(testAlgName);
    }

    /**
     * Test #2 for <code>MGF1ParameterSpec</code> constructor<br>
     * Assertion: <code>NullPointerException</code> if parameter is <code>null</code>
     */
    public final void testMGF1ParameterSpec02() {
        try {
            AlgorithmParameterSpec aps = new MGF1ParameterSpec(null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
            logln(getName() + ": " + ok);
        }
    }

    /**
     * Test for <code>getDigestAlgorithm</code> method<br>
     * Assertion: returns the algorithm name of the message
     * digest used by the mask generation function
     */
    public final void testGetDigestAlgorithm() {
        MGF1ParameterSpec aps = new MGF1ParameterSpec(testAlgName);
        assertTrue(testAlgName.equals(aps.getDigestAlgorithm()));
    }

    /**
     * Test for public static fields and <code>getDigestAlgorithm</code> method<br>
     * Assertion: returns the algorithm name of the message
     * digest used by the mask generation function
     */
    public final void testFieldsGetDigestAlgorithm() {
        assertTrue("SHA-1".equals(MGF1ParameterSpec.SHA1.getDigestAlgorithm()));
        assertTrue("SHA-256".equals(MGF1ParameterSpec.SHA256.getDigestAlgorithm()));
        assertTrue("SHA-384".equals(MGF1ParameterSpec.SHA384.getDigestAlgorithm()));
        assertTrue("SHA-512".equals(MGF1ParameterSpec.SHA512.getDigestAlgorithm()));
    }
}
