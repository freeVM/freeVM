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

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.util.Collection;


/**
 * This abstract class represents ASN.1 collection type.
 * 
 * The value for such type is a collection of zero or
 * more occurrences of a provided type. 
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public abstract class ASN1ValueCollection extends ASN1Constructured {

    /**
     * A value collection of this ASN.1 type
     */
    public final ASN1Type type;

    /**
     * Constructs ASN1 collection type.
     * 
     * @param tagNumber - ASN.1 tag number
     * @param type - ASN.1 type
     */
    public ASN1ValueCollection(int tagNumber, ASN1Type type) {
        super(tagNumber);

        this.type = type;
    }

    /**
     * Creates decoded object.
     * 
     * Derived classes should override this method to provide
     * creation for a selected class of objects during decoding. 
     * 
     * The default implementation returns list of decoded objects.
     *
     * @param - input stream
     * @return - created object
     */
    public Object getDecodedObject(BerInputStream in) throws IOException {
        return in.content;
    }

    /**
     * Provides an object's values to be encoded
     * 
     * Derived classes should override this method to provide
     * encoding for a selected class of objects. 
     * 
     * @param - an object to be encoded
     * @return - a collection of object's values to be encoded 
     */
    public Collection getValues(Object object) {
        return (Collection)object;
    }
}