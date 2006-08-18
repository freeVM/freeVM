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

package org.apache.harmony.security.tests.java.security.cert;

import java.io.File;
import java.io.FileInputStream;
import java.security.Provider;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.SpiEngUtils;
import org.apache.harmony.security.tests.support.TestUtils;

/**
 * Tests for <code>CertificateFactory</code> class methods
 */

public class CertificateFactory3Test extends TestCase {

    private static String defaultProviderName = null;

    private static Provider defaultProvider = null;

    private static String defaultType = CertificateFactory1Test.defaultType;

    public static String fileCertificateX509 = "java/security/cert/serialization/Certificate.X.509";

    public static String fileCertPathPki = "java/security/cert/serialization/CertPath.PkiPath";

    private static boolean X509Support = false;

    private static String NotSupportMsg = "";

    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                CertificateFactory1Test.srvCertificateFactory);
        X509Support = defaultProvider != null;
        defaultProviderName = X509Support ? defaultProvider.getName() : null;

        NotSupportMsg = defaultType.concat(" is not supported");

        fileCertificateX509 = SpiEngUtils.getFileName(TestUtils.TEST_ROOT,
                fileCertificateX509);
        fileCertificateX509 = fileCertificateX509.replace('/',
                File.separatorChar);

        fileCertPathPki = SpiEngUtils.getFileName(TestUtils.TEST_ROOT,
                fileCertPathPki);
        fileCertPathPki = fileCertPathPki.replace('/', File.separatorChar);

    }

    private static CertificateFactory[] initCertFs() throws Exception {
        if (!X509Support) {
            fail(NotSupportMsg);
        }

        CertificateFactory[] certFs = new CertificateFactory[3];
        certFs[0] = CertificateFactory.getInstance(defaultType);
        certFs[1] = CertificateFactory.getInstance(defaultType,
                defaultProviderName);
        certFs[2] = CertificateFactory
                .getInstance(defaultType, defaultProvider);
        return certFs;
    }

    /**
     * Test for <code>generateCertificate(InputStream inStream)</code> method
     * Assertion: returns Certificate
     */
    public void testGenerateCertificate() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        FileInputStream fis = null;
        Certificate[] certs = new Certificate[3];
        for (int i = 0; i < certFs.length; i++) {
            fis = new FileInputStream(fileCertificateX509);
            certs[i] = certFs[i].generateCertificate(fis);
            fis.close();
        }
        assertEquals(certs[0], certs[1]);
        assertEquals(certs[0], certs[2]);
    }

    /**
     * Test for <code>generateCertificates(InputStream inStream)</code> method
     * Assertion: returns Collection which consists of 1 Certificate
     */
    public void testeGnerateCertificates() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        FileInputStream fis = new FileInputStream(fileCertificateX509);
        Certificate cert = certFs[0].generateCertificate(fis);
        fis.close();
        for (int i = 0; i < certFs.length; i++) {
            Collection col = null;
            fis = new FileInputStream(fileCertificateX509);
            col = certFs[i].generateCertificates(fis);
            fis.close();
            Iterator it = col.iterator();
            assertEquals("Incorrect Collection size", col.size(), 1);
            assertEquals("Incorect Certificate in Collection", cert, it.next());
        }
    }

    /**
     * Test for <code>generateCertPath(List certificates)</code> method
     * Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath01() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        // create list of certificates with one certificate
        FileInputStream fis = new FileInputStream(fileCertificateX509);
        Certificate cert = certFs[0].generateCertificate(fis);
        fis.close();
        List list = new Vector();
        list.add(cert);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            certPath = certFs[i].generateCertPath(list);
            assertEquals(cert.getType(), certPath.getType());
            List list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
            Iterator it = list1.iterator();
            assertEquals("Incorrect Certificate in CertPath", cert, it.next());
        }
    }

    /**
     * Test for
     * <code>generateCertPath(InputStream inStream, String encoding)</code>
     * method Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath02() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            FileInputStream fis = new FileInputStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis, "PkiPath");
            fis.close();
            assertEquals(defaultType, certPath.getType());

            List list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }

    /**
     * Test for <code>generateCertPath(InputStream inStream)</code> method
     * Assertion: returns CertPath with 1 Certificate
     */
    public void testGenerateCertPath03() throws Exception {
        String certPathEncoding = "PkiPath";
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            Iterator it = certFs[0].getCertPathEncodings();

            assertTrue("no CertPath encodings", it.hasNext());

            assertEquals("Incorrect default encoding", certPathEncoding, it
                    .next());

            CertPath certPath = null;
            FileInputStream fis = new FileInputStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis);
            fis.close();
            assertEquals(defaultType, certPath.getType());

            List list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }
}
