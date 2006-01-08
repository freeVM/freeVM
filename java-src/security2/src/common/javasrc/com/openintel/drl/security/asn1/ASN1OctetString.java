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
* @author Vladimir N. Molotkov, Stepan M. Mishura
* @version $Revision$
*/

package com.openintel.drl.security.asn1;

import java.io.IOException;

/**
 * This class represents ASN.1 octet string type.
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ASN1OctetString extends ASN1StringType {

    // default implementation
    private static final ASN1OctetString ASN1 = new ASN1OctetString();

    /**
     * Constructs ASN.1 octet string type
     * 
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 octet string type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1OctetString() {
        super(TAG_OCTETSTRING);
    }

    /**
     * Returns ASN.1 octet string type default implementation
     * 
     * The default implementation works with encoding
     * that is represented as byte array.
     *
     * @return ASN.1 octet string type default implementation
     */
    public static ASN1OctetString getInstance() {
        return ASN1;
    }

    //
    //
    // Decode
    //
    //

    public void verify(BerInputStream in) throws IOException {
        if (!checkTag(in.tag)) {
            //FIXME message: what about constr tag?
            throw new ASN1Exception("ASN.1 Octetstring is expected at ["
                    + in.tagOffset + "]. Expected tag: "
                    + Integer.toHexString(tag) + ", but encountered tag "
                    + Integer.toHexString(in.tag));
        }
        in.readOctetString();
    }

    /**
     * Extracts array of bytes from BER input stream.
     *
     * @param in - BER input stream
     * @return array of bytes
     */
    public Object getDecodedObject(BerInputStream in) throws IOException {
        byte[] bytesEncoded = new byte[in.length];
        System.arraycopy(in.buffer, in.contentOffset, bytesEncoded, 0,
                in.length);
        return bytesEncoded;
    }

    //
    //
    // Encode
    //
    //

    public void encodeContent(BerOutputStream out) {
        out.encodeOctetString();
    }

    public void setEncodingContent(BerOutputStream out) {
        out.length = ((byte[]) out.content).length;
    }
}