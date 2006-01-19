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

package java.security.cert;

import java.util.HashSet;

import org.apache.harmony.security.cert.MyCertificate;
import org.apache.harmony.security.cert.TestUtils;
import org.apache.harmony.security.test.PerformanceTest;


/**
 * Tests for <code>PKIXCertPathChecker</code>
 * 
 */
public class PKIXCertPathCheckerTest extends PerformanceTest {

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
     * Constructor for PKIXCertPathCheckerTest.
     * @param name
     */
    public PKIXCertPathCheckerTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    public final void testClone() {
        PKIXCertPathChecker pc1 = TestUtils.getTestCertPathChecker();
        PKIXCertPathChecker pc2 = (PKIXCertPathChecker) pc1.clone();
        assertNotSame("notSame", pc1, pc2);
    }

    //
    // the following tests just call methods
    // that are abstract in <code>PKIXCertPathChecker</code>
    // (So they just like signature tests)
    //

    public final void testIsForwardCheckingSupported() {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.isForwardCheckingSupported();
    }

    public final void testInit()
        throws CertPathValidatorException {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.init(true);
    }

    public final void testGetSupportedExtensions() {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.getSupportedExtensions();
    }

    public final void testCheck()
        throws CertPathValidatorException {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.check(new MyCertificate("", null), new HashSet());
    }

}
