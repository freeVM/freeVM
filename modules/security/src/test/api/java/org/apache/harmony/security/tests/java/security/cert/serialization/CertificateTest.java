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

package org.apache.harmony.security.tests.java.security.cert.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.apache.harmony.security.tests.support.cert.MyCertificate;
import org.apache.harmony.security.tests.support.cert.TestUtils;
import org.apache.harmony.testframework.serialization.SerializationTest;

import junit.framework.TestCase;


/**
 * Tests for <code>Certificate</code> serialization
 * 
 */
public class CertificateTest extends TestCase {

    // certificate type to be created during testing
    private static final String certType = "X.509";

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        CertificateFactory cf = CertificateFactory.getInstance(certType);

        Certificate cert = cf.generateCertificate(new ByteArrayInputStream(
                TestUtils.getEncodedX509Certificate()));

        SerializationTest.verifySelf(cert);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        CertificateFactory cf = CertificateFactory.getInstance(certType);

        Certificate cert = cf.generateCertificate(new ByteArrayInputStream(
                TestUtils.getEncodedX509Certificate()));

        SerializationTest.verifyGolden(this, cert);
    }

    /**
     * Test for <code>Certificate.CertificateRep.readResolve()</code> method<br>
     * 
     * Assertion: ObjectStreamException if a <code>CertPath</code> could not
     * be constructed
     * 
     * @throws CertificateException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public final void testSerialization03()
        throws CertificateException,
               IOException,
               ClassNotFoundException {
        boolean passed = false;
        // Create object to be serialized
        Certificate c1 = new MyCertificate("DUMMY", new byte[] {(byte)0, (byte)1});
        // This testcase uses ByteArray streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Serialize cert
        serialize(c1, bos);
        // try to deserialize it
        try {
            deserialize(new ByteArrayInputStream(bos.toByteArray()));
        } catch (Exception e) {
        	System.out.println(getName() + ": " + e);
            if (e instanceof ObjectStreamException) {
                passed = true;
            }
        }
        // check that exception has been thrown
        assertTrue(passed);
    }

    /**
     * Test for <code>writeReplace()</code> method<br>
     * ByteArray streams used.
     * 
     * @throws CertificateException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public final void testSerialization04()
        throws CertificateException,
               IOException,
               ClassNotFoundException {
        boolean passed = false;
        // Create object to be serialized
        Certificate c1 = new MyCertificate("DUMMY", null);
        // This testcase uses ByteArray streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Try to serialize cert
        try {
            serialize(c1, bos);
        } catch (Exception e) {
        	System.out.println(getName() + ": " + e);
            // OSE and NPE are possible
            passed = true;
        }
        assertTrue(passed);
    }

    //
    // private stuff
    //

    /**
     * Test case start template - serialization
     *
     * @param c
     * <code>Certificate</code> object to be serialized
     * @param os
     * Serialization <code>OutputStream</code> for <code>c</code>
     */
    private void serialize(Certificate c, OutputStream os)
            throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        try {
            // Serialize it to the os
            oos.writeObject(c);
            oos.flush();
        } finally {
            oos.close();
        }
    }
    /**
     * Test case end template - deserialization and checks
     *
     * @param is
     * <code>Certificate</code> object deserialization <code>InputStream</code>
     */
    private Certificate deserialize(InputStream is)
            throws IOException,
                   ClassNotFoundException {
        // deserialize our object
        ObjectInputStream ois = new ObjectInputStream(is);
        Certificate cert = null;
        try {
            cert = (Certificate)ois.readObject();
            return cert;
        } finally {
            ois.close();
        }
    }

}
