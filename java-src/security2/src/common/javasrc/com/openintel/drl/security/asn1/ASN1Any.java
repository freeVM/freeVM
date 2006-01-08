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
 * This class represents ASN.1 ANY type.
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ASN1Any extends ASN1Type {

    // default implementation
    private static final ASN1Any ASN1= new ASN1Any();

    /**
     * Constructs ASN.1 ANY type
     * 
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 ANY type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1Any() {
        super(TAG_ANY); // has not tag number
    }

    /**
     * Returns ASN.1 ANY type default implementation
     * 
     * The default implementation works with full encoding
     * that is represented as raw byte array.
     *
     * @return ASN.1 ANY type default implementation
     */
    public static ASN1Any getInstance() {
        return ASN1;
    }

    //
    //
    // Decode
    //
    //

    /**
     * Tests provided identifier.
     *
     * @param identifier - identifier to be verified
     * @return - true
     */
    public final boolean checkTag(int identifier) {
        return true; //all tags are OK
    }

    public Object getDecodedObject(BerInputStream in) throws IOException {
        byte[] bytesEncoded = new byte[in.offset - in.tagOffset];
        System.arraycopy(in.buffer, in.tagOffset, bytesEncoded, 0,
                bytesEncoded.length);
        return bytesEncoded;
    }

    //
    //
    // Encode
    //
    //

    public void encodeASN(BerOutputStream out) {
        out.encodeANY();
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeANY();
    }

    public void setEncodingContent(BerOutputStream out) {
        out.length = ((byte[]) out.content).length;
    }

    public int getEncodedLength(BerOutputStream out) {
        return out.length;
    }
}