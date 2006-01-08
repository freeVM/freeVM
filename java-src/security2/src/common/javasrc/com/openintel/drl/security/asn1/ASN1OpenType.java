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
* @author Stepan M. Mishura
* @version $Revision$
*/

package com.openintel.drl.security.asn1;

import java.io.IOException;

import com.openintel.drl.security.x501.AttributeType;

/**
 * Represents ASN.1 open type that is defined by Oid
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ASN1OpenType extends ASN1Any {

    private final Id key;

    private final InformationObjectSet pool;

    public ASN1OpenType(Id key, InformationObjectSet pool) {
        this.key = key;
        this.pool = pool;
    }

    public void verify(BerInputStream in) throws IOException {

        int[] oid = (int[]) in.get(key);
        if (oid == null) {
            throw new RuntimeException("");//FIXME message & type
        }

        AttributeType attr = (AttributeType) pool.get(oid);
        if (in.isVerify) {
            if (attr == null) {
                in.readContent(); //FIXME what about unknown oids???
            } else {
                attr.type.verify(in);
            }
        } else {
            if (attr == null || (!attr.type.checkTag(in.tag))) {
                in.content = (byte[]) super.getDecodedObject(in);
            } else {
                in.content = attr.type.decode(in);
            }
        }
    }

    public Object getDecodedObject(BerInputStream in) throws IOException {
        return in.content;
    }

    public static class Id extends ASN1Oid {

        public void verify(BerInputStream in) throws IOException {
            super.verify(in);

            in.put(this, super.getDecodedObject(in));
        }

        public Object getDecodedObject(BerInputStream in) throws IOException {
            return in.get(this);
        }
    }
}