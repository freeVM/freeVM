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

package com.openintel.drl.security.x509;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

import org.apache.harmony.security.asn1.*;

/**
 * The class incapsulates the ASN.1 DER encoding/decoding work 
 * with the CRL Distribution Points which is the part of X.509 CRL
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  CRLDistributionPoints ::= SEQUENCE SIZE (1..MAX) OF DistributionPoint
 *
 *  DistributionPoint ::= SEQUENCE {
 *        distributionPoint       [0]     DistributionPointName OPTIONAL,
 *        reasons                 [1]     ReasonFlags OPTIONAL,
 *        cRLIssuer               [2]     GeneralNames OPTIONAL 
 *  }
 *
 *  DistributionPointName ::= CHOICE {
 *        fullName                [0]     GeneralNames,
 *        nameRelativeToCRLIssuer [1]     RelativeDistinguishedName 
 *  }
 *
 *  ReasonFlags ::= BIT STRING {
 *        unused                  (0),
 *        keyCompromise           (1),
 *        cACompromise            (2),
 *        affiliationChanged      (3),
 *        superseded              (4),
 *        cessationOfOperation    (5),
 *        certificateHold         (6),
 *        privilegeWithdrawn      (7),
 *        aACompromise            (8) 
 *  }
 * </pre>
 */
public class CRLDistributionPoints {
    
    private List distributionPoints;
    private byte[] encoding;
    
    public CRLDistributionPoints(List distributionPoints) {
        if ((distributionPoints == null) 
                || (distributionPoints.size() == 0)) {
            throw new IllegalArgumentException("permittedSubtrees are empty");
        }
        this.distributionPoints = distributionPoints;
    }

    public CRLDistributionPoints(List distributionPoints, byte[] encoding) {
        if ((distributionPoints == null) 
                || (distributionPoints.size() == 0)) {
            throw new IllegalArgumentException("permittedSubtrees are empty");
        }
        this.distributionPoints = distributionPoints;
        this.encoding = encoding;
    }

    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    
    /**
     * Custom X.509 decoder.
     */
    public static final ASN1Type ASN1 = 
        new ASN1SequenceOf(DistributionPoint.ASN1) {

        public Object getDecodedObject(BerInputStream in) {
            return new CRLDistributionPoints((List)in.content, 
                    in.getEncoded());
        }

        public Collection getValues(Object object) {
            CRLDistributionPoints dps = (CRLDistributionPoints) object;
            return (dps.distributionPoints == null) 
                                        ? new ArrayList() 
                                        : dps.distributionPoints;
        }
    };
}

