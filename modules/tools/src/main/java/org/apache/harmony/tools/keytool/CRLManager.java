/*
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.tools.keytool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class for managing Certificate Revocation Lists (CRLs).
 */
public class CRLManager {
    /**
     * Checks if the certificate given in the file is contained in the CRL which
     * is stored in the certstore. If the file name is not given, stdin is used.
     * File with CRL and the checked certificate file are specified in param.
     * 
     * @param param
     * @throws KeytoolException
     * @throws IOException
     * @throws CRLException
     * @throws NoSuchProviderException
     * @throws CertificateException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException 
     */
    static void checkRevoked(KeytoolParameters param) throws FileNotFoundException,
            CertificateException, NoSuchProviderException, CRLException,
            IOException, KeytoolException, NoSuchAlgorithmException {

        String providerName = param.getProvider();
        // firstly, get CRLs from the file 
        Collection crls = CertReader.readCRLs(param.getCrlFile(), providerName);
        // quit, if couldn't read anything
        if (crls.isEmpty()) {
            throw new CRLException("Failed to generate a CRL from the input. ");
        }

        // secondly, get certificates from another file
        Collection certs = CertReader.readCerts(param.getFileName(), false,
                param.getProvider());
        if (certs.isEmpty()) {
            throw new CertificateException(
                    "Failed to generate a certificate from the input. ");
        }

        boolean foundRevoked = false;
        
        // search in the CRLs for revocations of the certificates
        Iterator crlIter = crls.iterator();
        while (crlIter.hasNext()) {
            X509CRL crl = (X509CRL) crlIter.next();
            Iterator certIter = certs.iterator();
            while (certIter.hasNext()){
                X509Certificate cert = (X509Certificate)certIter.next();
                X509CRLEntry entry = crl.getRevokedCertificate(cert); 
                if (entry != null) {
                    System.out.println("The certificate ...");
                    KeyStoreCertPrinter.printX509CertDetailed(cert, providerName);
                    System.out.println("... is revoked on "
                            + entry.getRevocationDate() + "\n");
                    foundRevoked = true;
                    continue;
                } 
            }
        }
        
        if (certs.size() == 1 && !foundRevoked){
            System.out.println("The certificate ...");
            KeyStoreCertPrinter.printX509CertDetailed((X509Certificate) certs
                    .iterator().next(), providerName);
            System.out.println("... is not found in CRLs given");
        } else if (!foundRevoked){
            System.out.println("The certificates are not found in CRLs given");
        }
    }
}
