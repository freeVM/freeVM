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

package com.openintel.drl.security.provider.cert;

import java.io.*;
import java.math.*;
import java.util.*;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BitString;
import org.apache.harmony.security.asn1.ObjectIdentifier;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import com.openintel.drl.security.x501.Name;
import com.openintel.drl.security.x509.*;
import org.apache.harmony.security.asn1.ASN1BitString;

import java.security.cert.CertificateParsingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * X509CertFactoryPerfTest
 */
public class X509CertFactoryPerfTest extends TestCase {

    //
    // The values of certificate's fields:
    //
    
    static int         version         = 2; //v3
    static BigInteger  serialNumber    = BigInteger.valueOf(555555555555555555L);

    // Algorithm name and its OID (http://oid.elibel.tm.fr)
    static String      algOID          = "1.2.840.10040.4.3";
    static String      algName         = "SHA1withDSA";

    // DER boolean false encoding (http://asn1.elibel.tm.fr)
    // Makes no sence. For testing purposes we need just provide 
    // some ASN.1 structure:
    static byte[]      algParams       = {1, 1, 0};
    static String      issuerName      = "O=Certificate Issuer";
    static long        notBefore       = 1000000000L;
    static long        notAfter        = 2000000000L;
    static String      subjectName     = "O=Subject Organization";

    // keys are using to make signature and to verify it
    static PublicKey   publicKey;
    static PrivateKey  privateKey;
    static byte[]      key             = new byte[] {1, 2, 3, 4, 5, 6, 7, 8}; // random value
    static byte[]      keyEncoding     = null;
    static boolean[]   issuerUniqueID  = new boolean[] 
                {true, false, true, false, true, false, true, false}; // random value
    static boolean[]   subjectUniqueID = new boolean[]
                {false, true, false, true, false, true, false, true}; // random value

    // Extensions' values
    static byte[]      extValEncoding  = new byte[] {1, 1, 1}; // random value
    static boolean[]   extnKeyUsage    = new boolean[] 
                {true, false, true, false, true, false, true, false, true}; // random value
    static List    extnExtendedKeyUsage = Arrays.asList(new int[][] {
        // Extended key usage values as specified in rfc 3280:
        // (http://www.ietf.org/rfc/rfc3280.txt)
        ObjectIdentifier.toIntArray("2.5.29.37.0"),       // Any extended key usage
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.1"), // TLS Web server authentication
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.1"), // TLS Web server authentication
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.2"), // TLS Web client authentication
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.3"), // Code Signing
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.4"), // E-mail protection
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.5"), // IP security end system
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.6"), // IP security tunnel termination
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.7"), // IP security user
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.8"), // Timestamping
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.7.3.9"), // OCSP signing
        ObjectIdentifier.toIntArray("1.3.6.1.5.5.8.2.2"), // iKEIntermediate
        ObjectIdentifier.toIntArray("1.3.6.1.4.1.311.10.3.3"), // MS Server Gated Cryptography
        ObjectIdentifier.toIntArray("2.16.840.1.113730.4.1"), // Netscape Server Gated Cryptography
    });
    static int extnBCLen = 5;
    static GeneralNames extnSANames;
    static GeneralNames extnIANames;
    
    static {
        try {
            extnSANames = new GeneralNames(
                Arrays.asList(new GeneralName[] {
                    new GeneralName(1, "rfc@822.Name"),
                    new GeneralName(2, "dNSName"),
                    new GeneralName(4, "O=Organization"),
                    new GeneralName(6, "http://uniform.Resource.Id"),
                    new GeneralName(7, "255.255.255.0"),
                    new GeneralName(8, "1.2.3.4444.55555")
            }));
        } catch (IOException e) {
            // should not be thrown
            e.printStackTrace();
            extnSANames = new GeneralNames();
        }
        extnIANames = extnSANames;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            keyGen.initialize(1024);
            KeyPair keyPair = keyGen.genKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Extensions
    static Extension[] extensions      = new Extension[] {
        
        // Supported critical extensions (as specified in rfc 3280
        // http://www.ietf.org/rfc/rfc3280.txt):

        // Key Usage
        new Extension("2.5.29.15", true, 
                ASN1BitString.getInstance()
                .encode(new BitString(extnKeyUsage))),  
        // Basic Constraints    
        new Extension("2.5.29.19", true, 
                Extension.BasicConstraints.ASN1.encode(
                    new Object[] 
                    {Boolean.TRUE, BigInteger.valueOf(extnBCLen)})),
        // Certificate Policies with ANY policy
        new Extension("2.5.29.32", true, 
                new CertificatePolicies(Arrays.asList(new Object[] {
                    new PolicyInformation("2.5.29.32.0")}))
                .getEncoded()),//extValEncoding),
        // Subject Alternative Name
        new Extension("2.5.29.17", true, 
                GeneralNames.ASN1.encode(extnSANames)),
        // Name Constraints
        new Extension("2.5.29.30", true, 
                new NameConstraints().getEncoded()),
        // Policy Constraints
        new Extension("2.5.29.36", true, 
                new PolicyConstraints().getEncoded()),
        // Extended Key Usage
        new Extension("2.5.29.37", true, 
                Extension.ExtendedKeyUsage.ASN1.encode(
                    extnExtendedKeyUsage)),             
        // Inhibit Any-Policy
        new Extension("2.5.29.54", true,
                ASN1Integer.getInstance().encode(ASN1Integer.fromIntValue(1))),

        // Unsupported critical extensions:
        new Extension("1.2.77.777", true, extValEncoding),

        // Non-critical extensions (as specified in rfc 3280
        // http://www.ietf.org/rfc/rfc3280.txt):
 
        // Issuer Alternative Name
        new Extension("2.5.29.18", false, 
                GeneralNames.ASN1.encode(extnSANames)),
        // CRL Distribution Points
        new Extension("2.5.29.31", false, 
                new ASN1Sequence(new ASN1Type[] {}) {
                    protected void getValues(Object object, Object[] values) {
                    }
                }.encode(null)),
        // Authority Key Identifier
        new Extension("2.5.29.35", false, extValEncoding),
        // Subject Key Identifier
        new Extension("2.5.29.14", false, extValEncoding),
        // Policy Mappings
        new Extension("2.5.29.33", false, extValEncoding),
    };
    static List allCritical = Arrays.asList(new String[] {"2.5.29.15", "2.5.29.19",
        "2.5.29.32", "2.5.29.17", "2.5.29.30", "2.5.29.36", "2.5.29.37",
        "2.5.29.54", "1.2.77.777"});
    static List allNonCritical = Arrays.asList(new String[] {"2.5.29.18", "2.5.29.35",
        "2.5.29.14", "2.5.29.33", "2.5.29.31"});

    static X509Certificate certificate;
    static TBSCertificate tbsCertificate;
    static AlgorithmIdentifier signature;
    static CertificateFactory factory;
    static ByteArrayInputStream stream, streamCRL, stream_b64;
    static byte[] tbsCertEncoding;
    static byte[] signatureValue;
    // to minimize efforts on signature generation the signature will be
    // stored in this field
    static byte[] signatureValueBytes;
    static byte[] certEncoding, certEncoding_b64;
   
    static {
        try {
            signature = 
                new AlgorithmIdentifier(algOID, algParams);
            Name issuer = new Name(issuerName);
            Name subject = new Name(subjectName);
            Validity validity = 
                new Validity(new Date(notBefore), new Date(notAfter));

            SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) 
                SubjectPublicKeyInfo.ASN1.decode(publicKey.getEncoded());
            keyEncoding = subjectPublicKeyInfo.getEncoded();
            
            Extensions exts = new Extensions(Arrays.asList(extensions));
            
            tbsCertificate = 
                new TBSCertificate(version, serialNumber, 
                    signature, issuer, validity, subject, subjectPublicKeyInfo, 
                    issuerUniqueID, subjectUniqueID, exts);
            tbsCertEncoding = tbsCertificate.getEncoded();
           
            try {
                Signature sig= Signature.getInstance("DSA");
                sig.initSign(privateKey);
                sig.update(tbsCertEncoding, 0, tbsCertEncoding.length);
                signatureValueBytes = sig.sign();
            } catch (Exception e) {
                e.printStackTrace();
                signatureValueBytes = new byte[10];
            }
            factory = CertificateFactory.getInstance("X.509");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // the testing data was generated by using of classes 
    // from com.openintel.drl.security.asn1 package encoded
    // by com.openintel.drl.misc.Base64 class.

    private static String base64certEncoding = 
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIC+jCCAragAwIBAgICAiswDAYHKoZIzjgEAwEBADAdMRswGQYDVQQKExJDZXJ0a" +
        "WZpY2F0ZSBJc3N1ZXIwIhgPMTk3MDAxMTIxMzQ2NDBaGA8xOTcwMDEyNDAzMzMyMF" +
        "owHzEdMBsGA1UEChMUU3ViamVjdCBPcmdhbml6YXRpb24wGTAMBgcqhkjOOAQDAQE" +
        "AAwkAAQIDBAUGBwiBAgCqggIAVaOCAhQwggIQMA8GA1UdDwEB/wQFAwMBqoAwEgYD" +
        "VR0TAQH/BAgwBgEB/wIBBTAUBgNVHSABAf8ECjAIMAYGBFUdIAAwZwYDVR0RAQH/B" +
        "F0wW4EMcmZjQDgyMi5OYW1lggdkTlNOYW1lpBcxFTATBgNVBAoTDE9yZ2FuaXphdG" +
        "lvboYaaHR0cDovL3VuaWZvcm0uUmVzb3VyY2UuSWSHBP///wCIByoDolyDsgMwDAY" +
        "DVR0eAQH/BAIwADAMBgNVHSQBAf8EAjAAMIGZBgNVHSUBAf8EgY4wgYsGBFUdJQAG" +
        "CCsGAQUFBwMBBggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMDBggrBgEFBQcDB" +
        "AYIKwYBBQUHAwUGCCsGAQUFBwMGBggrBgEFBQcDBwYIKwYBBQUHAwgGCCsGAQUFBw" +
        "MJBggrBgEFBQgCAgYKKwYBBAGCNwoDAwYJYIZIAYb4QgQBMA0GA1UdNgEB/wQDAgE" +
        "BMA4GBCpNhgkBAf8EAwEBATBkBgNVHRIEXTBbgQxyZmNAODIyLk5hbWWCB2ROU05h" +
        "bWWkFzEVMBMGA1UEChMMT3JnYW5pemF0aW9uhhpodHRwOi8vdW5pZm9ybS5SZXNvd" +
        "XJjZS5JZIcE////AIgHKgOiXIOyAzAJBgNVHR8EAjAAMAoGA1UdIwQDAQEBMAoGA1" +
        "UdDgQDAQEBMAoGA1UdIQQDAQEBMAwGByqGSM44BAMBAQADMAAwLQIUAL4QvoazNWP" +
        "7jrj84/GZlhm09DsCFQCBKGKCGbrP64VtUt4JPmLjW1VxQA==\n" +
        "-----END CERTIFICATE-----\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIC+jCCAragAwIBAgICAiswDAYHKoZIzjgEAwEBADAdMRswGQYDVQQKExJDZXJ0a" +
        "WZpY2F0ZSBJc3N1ZXIwIhgPMTk3MDAxMTIxMzQ2NDBaGA8xOTcwMDEyNDAzMzMyMF" +
        "owHzEdMBsGA1UEChMUU3ViamVjdCBPcmdhbml6YXRpb24wGTAMBgcqhkjOOAQDAQE" +
        "AAwkAAQIDBAUGBwiBAgCqggIAVaOCAhQwggIQMA8GA1UdDwEB/wQFAwMBqoAwEgYD" +
        "VR0TAQH/BAgwBgEB/wIBBTAUBgNVHSABAf8ECjAIMAYGBFUdIAAwZwYDVR0RAQH/B" +
        "F0wW4EMcmZjQDgyMi5OYW1lggdkTlNOYW1lpBcxFTATBgNVBAoTDE9yZ2FuaXphdG" +
        "lvboYaaHR0cDovL3VuaWZvcm0uUmVzb3VyY2UuSWSHBP///wCIByoDolyDsgMwDAY" +
        "DVR0eAQH/BAIwADAMBgNVHSQBAf8EAjAAMIGZBgNVHSUBAf8EgY4wgYsGBFUdJQAG" +
        "CCsGAQUFBwMBBggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMDBggrBgEFBQcDB" +
        "AYIKwYBBQUHAwUGCCsGAQUFBwMGBggrBgEFBQcDBwYIKwYBBQUHAwgGCCsGAQUFBw" +
        "MJBggrBgEFBQgCAgYKKwYBBAGCNwoDAwYJYIZIAYb4QgQBMA0GA1UdNgEB/wQDAgE" +
        "BMA4GBCpNhgkBAf8EAwEBATBkBgNVHRIEXTBbgQxyZmNAODIyLk5hbWWCB2ROU05h" +
        "bWWkFzEVMBMGA1UEChMMT3JnYW5pemF0aW9uhhpodHRwOi8vdW5pZm9ybS5SZXNvd" +
        "XJjZS5JZIcE////AIgHKgOiXIOyAzAJBgNVHR8EAjAAMAoGA1UdIwQDAQEBMAoGA1" +
        "UdDgQDAQEBMAoGA1UdIQQDAQEBMAwGByqGSM44BAMBAQADMAAwLQIUAL4QvoazNWP" +
        "7jrj84/GZlhm09DsCFQCBKGKCGbrP64VtUt4JPmLjW1VxQA==\n" +
        "-----END CERTIFICATE-----\n";

    static {
        certEncoding_b64 = base64certEncoding.getBytes();
        stream_b64 = new ByteArrayInputStream(certEncoding_b64);
        stream_b64.mark(certEncoding_b64.length);
    }
    
    /**
     * Creates the master certificate on the base of which 
     * all functionality will be tested.
     * @return
     * @throws java.lang.Exception 
     */
    protected void setUp() throws java.lang.Exception {
        if ("testVerify3".equals(getName())) {
            signatureValue = new byte[signatureValueBytes.length];
            // make incorrect signature value:
            System.arraycopy(signatureValueBytes, 0, 
                    signatureValue, 0, signatureValueBytes.length);
            signatureValue[20]++;
        } else {
            signatureValue = signatureValueBytes;
        }
        Certificate cert = 
            new Certificate(tbsCertificate, signature, signatureValue);

        certEncoding = cert.getEncoded();
        stream = new ByteArrayInputStream(certEncoding);
        stream.mark(certEncoding.length);
        certificate = (X509Certificate) factory.generateCertificate(stream);

        streamCRL = new ByteArrayInputStream(certEncoding);
        streamCRL.mark(certEncoding.length);
        //System.out.println("\nUSING: "+certificate.getClass());
    }

    private static int XXX = 0, flag = 0;
    private static java.security.cert.Certificate prev = null;

    public void testCreationCRL() {
        try {
            byte[] stamp = new byte[10];
            if ((++flag)%2 != 0) {
                XXX++;
            }
            byte tmp[] = BigInteger.valueOf(XXX).toByteArray();
            System.arraycopy(tmp, 0, stamp, 0, tmp.length);
            System.arraycopy(stamp, 0, certEncoding, 
                    certEncoding.length-stamp.length, stamp.length);

            stream.reset();
            java.security.cert.Certificate c = factory.generateCertificate(stream);

            byte[] enc = c.getEncoded();
            byte[] stamp_chek = new byte[stamp.length];
            
            System.arraycopy(enc, enc.length - stamp.length, 
                    stamp_chek, 0, stamp.length);
           
            if (!Arrays.equals(stamp, stamp_chek)) {
                fail("Wrong encoding received.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Creation of a certificate from a stream failed:"+e.getMessage());
        }
    }
    
    public void testCreation1() {
        try {
            byte[] stamp = new byte[10];
            if ((++flag)%2 != 0) {
                XXX++;
            }
            byte tmp[] = BigInteger.valueOf(XXX).toByteArray();
            System.arraycopy(tmp, 0, stamp, 0, tmp.length);
            System.arraycopy(stamp, 0, certEncoding, 
                    certEncoding.length-stamp.length, stamp.length);

            stream.reset();
            java.security.cert.Certificate c = factory.generateCertificate(stream);

            byte[] enc = c.getEncoded();
            byte[] stamp_chek = new byte[stamp.length];
            
            System.arraycopy(enc, enc.length - stamp.length, 
                    stamp_chek, 0, stamp.length);
           
            if (!Arrays.equals(stamp, stamp_chek)) {
                fail("Wrong encoding received.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Creation of a certificate from a stream failed:"+e.getMessage());
        }
    }
    
    public void testCreation2() {
        try {
            stream_b64.reset();
            factory.generateCertificate(stream_b64);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Creation of a certificate from a stream failed:"+e.getMessage());
        }
    }
    
    /**
     * checkValidity() method testing.
     */
    public void testCheckValidity1() {
        try {
            certificate.checkValidity();
            fail("CertificateExpiredException should be thrown.");
        } catch (CertificateNotYetValidException e) {
            fail("Unexpected CertificateNotYetValidException was thrown.");
        } catch (CertificateExpiredException e) {
        }
    }
    
    /**
     * checkValidity(Date date) method testing.
     */
    public void testCheckValidity2() {
        try {
            certificate.checkValidity(new Date(3000000000L));
            fail("CertificateExpiredException should be thrown.");
        } catch (CertificateNotYetValidException e) {
            fail("Unexpected CertificateNotYetValidException was thrown.");
        } catch (CertificateExpiredException e) {
        }
        try {
            certificate.checkValidity(new Date(100000000L));
            fail("CertificateNotYetValidException be thrown.");
        } catch (CertificateExpiredException e) {
            fail("Unexpected CertificateExpiredException was thrown.");
        } catch (CertificateNotYetValidException e) {
        }
        try {
            certificate.checkValidity(new Date(1000000000L));
            certificate.checkValidity(new Date(1500000000L));
            certificate.checkValidity(new Date(2000000000L));
        } catch (CertificateExpiredException e) {
            fail("Unexpected CertificateExpiredException was thrown.");
        } catch (CertificateNotYetValidException e) {
            fail("Unexpected CertificateNotYetValidException was thrown.");
        }
    }
    
    /**
     * getVersion() method testing.
     */
    public void testGetVersion() {
        assertEquals("The version of the certificate should be 2", 
                3, certificate.getVersion());
    }
    
    /**
     * getSerialNumber() method testing.
     */
    public void testGetSerialNumber() {
        assertEquals("Incorrect value of version", 
                serialNumber, certificate.getSerialNumber());
    }
    
    /**
     * getIssuerDN() method testing.
     */
    public void testGetIssuerDN() {
        assertEquals("Incorrect issuer", 
                new X500Principal(issuerName).getName(), 
                certificate.getIssuerDN().getName());
    }
    
    /**
     * getIssuerX500Principal() method testing.
     */
    public void testGetIssuerX500Principal() {
        assertEquals("Incorrect issuer", 
                new X500Principal(issuerName), 
                certificate.getIssuerX500Principal());
    }
    
    /**
     * getSubjectDN() method testing.
     */
    public void testGetSubjectDN() {
        assertEquals("Incorrect subject", 
                new X500Principal(subjectName).getName(), 
                certificate.getSubjectDN().getName());
    }
    
    /**
     * getSubjectX500Principal() method testing.
     */
    public void testGetSubjectX500Principal() {
        assertEquals("Incorrect subject", 
                new X500Principal(subjectName), 
                certificate.getSubjectX500Principal());
    }
    
    /**
     * getNotBefore() method testing.
     */
    public void testGetNotBefore() {
        assertEquals("Incorrect notBefore date",
                new Date(notBefore), certificate.getNotBefore());
    }
    
    /**
     * getNotAfter() method testing.
     */
    public void testGetNotAfter() {
        assertEquals("Incorrect notAfter date",
                new Date(notAfter), certificate.getNotAfter());
    }
    
    public static void printAsHex(int perLine, String prefix, 
                                        String delimiter, byte[] data) {
        for (int i=0; i<data.length; i++) {
            String tail = Integer.toHexString(0x000000ff & data[i]);
            if (tail.length() == 1) {
                tail = "0" + tail; 
            }
            System.out.print(prefix + "0x" + tail + delimiter);
 
            if (((i+1)%perLine) == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }
    
    /**
     * getTBSCertificate() method testing.
     */
    public void testGetTBSCertificate() {
        try {
            if (!Arrays.equals(
                        tbsCertEncoding, certificate.getTBSCertificate())) {
                System.out.println("TBSCertificate encoding missmatch:");
                System.out.println("Expected:");
                printAsHex(20, "", " ", tbsCertEncoding);
                System.out.println("Got:");
                printAsHex(20, "", " ", certificate.getTBSCertificate());
                fail("Incorrect encoding of TBSCertificate.");
            }
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException was thrown.");
        }
    }
    
    /**
     * getSignature() method testing.
     */
    public void testGetSignature() {
        if (!Arrays.equals(signatureValue, certificate.getSignature())) {
            fail("Incorrect Signature value.");
        }
    }
    
    /**
     * getSigAlgName() method testing.
     */
    public void testGetSigAlgName() {
        assertEquals("Incorrect value of signature algorithm name",
                algName, certificate.getSigAlgName());
    }
    
    /**
     * getSigAlgOID() method testing.
     */
    public void testGetSigAlgOID() {
        assertEquals("Incorrect value of signature algorithm OID",
                algOID, certificate.getSigAlgOID());
    }
    
    /**
     * getSigAlgParams() method testing.
     */
    public void testGetSigAlgParams() {
        if (!Arrays.equals(algParams, certificate.getSigAlgParams())) {
            fail("Incorrect Signature value.");
        }
    }
    
    /**
     * getIssuerUniqueID() method testing.
     */
    public void testGetIssuerUniqueID() {
        if (!Arrays.equals(issuerUniqueID, certificate.getIssuerUniqueID())) {
            fail("Incorrect issuerUniqueID value.");
        }
    }
    
    /**
     * getSubjectUniqueID() method testing.
     */
    public void testGetSubjectUniqueID() {
        if (!Arrays.equals(subjectUniqueID, certificate.getSubjectUniqueID())) {
            fail("Incorrect subjectUniqueID value.");
        }
    }
    
    /**
     * getKeyUsage() method testing.
     */
    public void testGetKeyUsage() {
        boolean[] ku = certificate.getKeyUsage();
        if ((ku == null) || (ku.length < extnKeyUsage.length)) {
            fail("Incorrect Key Usage value.");
        }
        for (int i=0; i<extnKeyUsage.length; i++) {
            if (extnKeyUsage[i] != ku[i]) {
                fail("Incorrect Key Usage value.");
            }
        }
    }
    
    /**
     * getExtendedKeyUsage() method testing.
     */
    public void testGetExtendedKeyUsage() {
        try {
            List exku = certificate.getExtendedKeyUsage();
            if ((exku == null) 
                    || (exku.size() != extnExtendedKeyUsage.size())) {
                fail("Incorrect Extended Key Usage value.");
            }
            for (int i=0; i<extnExtendedKeyUsage.size(); i++) {
                String ku = ObjectIdentifier
                        .toString((int[]) extnExtendedKeyUsage.get(i));
                if (!exku.contains(ku)) {
                    fail("Missing value:" + ku);
                }
            }
        } catch (Exception e) {
            fail("Incorrect Key Usage value.");
        }
    }
    
    /**
     * getBasicConstraints() method testing.
     */
    public void testGetBasicConstraints() {
        assertEquals(extnBCLen, certificate.getBasicConstraints());
    }
    
    /**
     * getSubjectAlternativeNames() method testing.
     */
    public void testGetSubjectAlternativeNames() {
        try {
            Collection certSans = certificate.getSubjectAlternativeNames();
            if (certSans == null) {
                fail("Returned value should not be null.");
            }
            List sans = (List) extnSANames.getPairsList();
            if (sans.size() != certSans.size()) {
                fail("Size of returned collection does not match to the actual");
            }
            Iterator it = certSans.iterator();
            while (it.hasNext()) {
                List extnSan = (List) it.next();
                Integer tag = (Integer) extnSan.get(0);
                for (int i=0; i< sans.size(); i++) {
                    List san = (List) sans.get(i);
                    if (tag.equals((Integer) san.get(0))) {
                        assertEquals(
                                "Incorrect value of Subject Alternative Name",
                                extnSan.get(1), san.get(1));
                    }
                }
            }
        } catch (CertificateParsingException e) {
            fail("Subject Alternative Name extension was incorrectly encoded.");
        }
    }
    
    /**
     * getIssuerAlternativeNames() method testing.
     */
    public void testGetIssuerAlternativeNames() {
        try {
            Collection certIans = certificate.getIssuerAlternativeNames();
            if (certIans == null) {
                fail("Returned value should not be null.");
            }
            List ians = (List) extnSANames.getPairsList();
            if (ians.size() != certIans.size()) {
                fail("Size of returned collection does not match to the actual");
            }
            Iterator it = certIans.iterator();
            while (it.hasNext()) {
                List extnIan = (List) it.next();
                Integer tag = (Integer) extnIan.get(0);
                for (int i=0; i< ians.size(); i++) {
                    List ian = (List) ians.get(i);
                    if (tag.equals((Integer) ian.get(0))) {
                        assertEquals(
                                "Incorrect value of Issuer Alternative Name",
                                extnIan.get(1), ian.get(1));
                    }
                }
            }
        } catch (CertificateParsingException e) {
            fail("Issuer Alternative Name extension was incorrectly encoded.");
        }
    }
    
    /**
     * getEncoded() method testing.
     */
    public void testGetEncoded() {
        try {
            if (!Arrays.equals(certEncoding, certificate.getEncoded())) {
                fail("Incorrect encoding of Certificate.");
            }
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException was thrown.");
        }
    }
    
    /**
     * getPublicKey() method testing.
     */
    public void testGetPublicKey() {
        if (!Arrays.equals(keyEncoding, certificate.getPublicKey().getEncoded())) {
            fail("Incorrect Public Key.");
        }
    }
    
    /**
     * getExtensionValue(String oid) method testing.
     */
    public void testGetExtensionValue() {
        for (int i=0; i<extensions.length; i++) {
            String id = extensions[i].getExtnID();
            byte[] certExtnValue = certificate.getExtensionValue(id);
            byte[] certExtnValue2Check = extensions[i].getRawExtnValue();
                certificate.getExtensionValue(id);
            if (!Arrays.equals(certExtnValue2Check, certExtnValue)) {
                System.out.println("Extension encoding mismatch for "+id);
                System.out.println("Expected:");
                printAsHex(20, "", " ", certExtnValue2Check);
                System.out.println("But has been got:");
                if (certExtnValue == null) {
                    System.out.println("null");
                } else {
                    printAsHex(20, "", " ", certExtnValue);
                }
                //fail("The values for extension "+id+" differ.");
            }
        }
    }
    
    /**
     * getCriticalExtensionOIDs() method testing.
     */
    public void testGetCriticalExtensionOIDs() {
        Set certCEOids = certificate.getCriticalExtensionOIDs();
        if (!(certCEOids.containsAll(allCritical) 
                    && allCritical.containsAll(certCEOids))) {
            fail("Incorrect value of Critical Extension OIDs");
        }
    }
    
    /**
     * getNonCriticalExtensionOIDs() method testing.
     */
    public void testGetNonCriticalExtensionOIDs() {
        Set certNCEOids = certificate.getNonCriticalExtensionOIDs();
        if (!(certNCEOids.containsAll(allNonCritical) 
                    && allNonCritical.containsAll(certNCEOids))) {
            fail("Incorrect value of Non Critical Extension OIDs");
        }
    }
    
    /**
     * hasUnsupportedCriticalExtension() method testing.
     */
    public void testHasUnsupportedCriticalExtension() {
        assertEquals("Incorrect value of hasUnsupportedCriticalExtension", 
                true, certificate.hasUnsupportedCriticalExtension());

        if (!certificate.hasUnsupportedCriticalExtension()) {
            fail("Incorrect value of hasUnsupportedCriticalExtension");
        }
    }
    
    /**
     * toString() method testing.
     */
    public void testToString() {
        assertNotNull("String representation should not be null", 
                certificate.toString());
    }

    /**
     * TODO
     * verify(PublicKey key) method testing.
     */
    public void testVerify1() throws Exception {
        try {
            certificate.verify(publicKey);
        } catch (Exception e) {
            fail("Signature verifying process failed.");
        }
    }
    
    /**
     * TODO
     * verify(PublicKey key, String sigProvider) method testing.
     */
    public void testVerify2() {
        try {
            certificate.verify(publicKey, 
                    Signature.getInstance("SHA1withDSA")
                                    .getProvider().getName());
        } catch (Exception e) {
            fail("Signature verifying process failed.");
        }
    }
    
    /**
     * TODO
     * verify(PublicKey key) method testing.
     */
    public void testVerify3() throws Exception {
        try {
            certificate.verify(publicKey);
            fail("Incorrect signature succesfully verified.");
        } catch (Exception e) {
        }
    }
    
    public static Test suite() {
        return new TestSuite(X509CertFactoryPerfTest.class);
    }

    public static void main(String[] args) throws Exception {
        /*
        X509CertFactoryPerfTest test = new X509CertFactoryPerfTest();
        test.setUp();
        long startTime = System.currentTimeMillis();
        for (int i=0; i<100000; i++) {
            test.testCreation1();
        }
        System.out.println("time: "+(System.currentTimeMillis() - startTime));
        */
        junit.textui.TestRunner.run(suite());
    }
}

