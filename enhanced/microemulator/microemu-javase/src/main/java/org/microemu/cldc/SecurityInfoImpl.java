/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.cldc;

import javax.microedition.io.SecurityInfo;
import javax.microedition.pki.Certificate;
import org.microemu.log.Logger;

public class SecurityInfoImpl implements SecurityInfo {

	private String cipherSuite;
	private String protocolName;
	private Certificate certificate;

	public SecurityInfoImpl(String cipherSuite, String protocolName, Certificate certificate) {
		this.cipherSuite = cipherSuite;
		this.protocolName = protocolName;
		this.certificate = certificate;
	}

	public String getCipherSuite() {
		return cipherSuite;
	}

	public String getProtocolName() {
		if (protocolName.startsWith("TLS")) {
			return "TLS";
		} else if (protocolName.startsWith("SSL")) {
			return "SSL";
		} else {
			// TODO Auto-generated method stub
			try {
				throw new RuntimeException();
			} catch (RuntimeException ex) {
				Logger.error(ex);
				throw ex;
			}
		}
	}

	public String getProtocolVersion() {
		if (protocolName.startsWith("TLS")) {
			return "3.1";
		} else if (getProtocolName().equals("SSL")) {
			return "3.0";
		} else {
			// TODO Auto-generated method stub
			try {
				throw new RuntimeException();
			} catch (RuntimeException ex) {
				Logger.error(ex);
				throw ex;
			}
		}
	}

	public Certificate getServerCertificate() {
		return certificate;
	}

}
