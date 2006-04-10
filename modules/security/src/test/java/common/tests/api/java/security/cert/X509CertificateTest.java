/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.security.cert;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Vector;

import tests.support.resource.Support_Resources;

public class X509CertificateTest extends junit.framework.TestCase {
	private X509Certificate pemCert = null;

	protected void setUp() {
		try {
			InputStream is = Support_Resources
					.getStream("hyts_certificate_PEM.txt");
			if (is != null) {
				CertificateFactory certFact = CertificateFactory
						.getInstance("X509");
				pemCert = (X509Certificate) certFact.generateCertificate(is);
			} else {
				fail("Problem occurred opening hyts_certificate_PEM.txt");
			}
		} catch (CertificateException e) {
			fail("Unexpected CertificateException : " + e);
		}
	}

	protected void tearDown() {
	}

	/**
	 * @tests java.security.cert.X509Certificate#getExtensionValue(java.lang.String)
	 */
	public void test_getExtensionValueLjava_lang_String() {
		if (pemCert != null) {
			Vector extensionOids = new Vector();
			extensionOids.addAll(pemCert.getCriticalExtensionOIDs());
			extensionOids.addAll(pemCert.getNonCriticalExtensionOIDs());
			Iterator i = extensionOids.iterator();
			while (i.hasNext()) {
				String oid = (String) i.next();
				byte[] value = pemCert.getExtensionValue(oid);
				if (value != null && value.length > 0) {
					// check that it is an encoded as a OCTET STRING
					assertTrue("The extension value for the oid " + oid
							+ " was not encoded as an OCTET STRING",
							value[0] == 0x04);
				}
			}
		} else {
			fail("Unable to obtain X509Certificate");
		}
	}
}