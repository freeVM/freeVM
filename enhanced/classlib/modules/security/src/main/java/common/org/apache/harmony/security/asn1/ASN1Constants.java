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

package org.apache.harmony.security.asn1;

/**
 * ASN.1 and some other constants holder interface
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */
public interface ASN1Constants {
    /**
     * Tag classes
     */
    int CLASS_UNIVERSAL = 0;
    int CLASS_APPLICATION = 64;
    int CLASS_CONTEXTSPECIFIC = 128;
    int CLASS_PRIVATE = 192;

    /**
     * Tag Primitive/Conctructed (P/C) flag
     */
    int PC_PRIMITIVE = 0;
    int PC_CONSTRUCTED = 32;

    /**
     * Universal class tag assignments
     */
    int TAG_BOOLEAN = 1;
    int TAG_INTEGER = 2;
    int TAG_BITSTRING = 3;
    int TAG_OCTETSTRING = 4;
    int TAG_NULL = 5;
    int TAG_OID = 6;
    int TAG_OBJDESCRIPTOR = 7;
    int TAG_EXTERNAL = 8;
    int TAG_INSTANCEOF = TAG_EXTERNAL;
    int TAG_REAL = 9;
    int TAG_ENUM = 10;
    int TAG_EMBEDDEDPDV = 11;
    int TAG_UTF8STRING = 12;
    int TAG_RELATIVEOID = 13;
    int TAG_SEQUENCE = 16;
    int TAG_SEQENCEOF = TAG_SEQUENCE;
    int TAG_SET = 17;
    int TAG_SETOF = TAG_SET;
    int TAG_NUMERICSTRING = 18;
    int TAG_PRINTABLESTRING = 19;
    int TAG_TELETEXSTRING = 20;
    int TAG_T61STRING = TAG_TELETEXSTRING;
    int TAG_VIDEOTEXSTRING = 21;
    int TAG_IA5STRING = 22;
    int TAG_UTCTIME = 23;
    int TAG_GENERALIZEDTIME = 24;
    int TAG_GRAPHICSTRING = 25;
    int TAG_VISIBLESTRING = 26;
    int TAG_ISO646STRING = TAG_VISIBLESTRING;
    int TAG_GENERALSTRING = 27;
    int TAG_UNIVERSALSTRING = 28;
    int TAG_BMPSTRING = 30;
 
    /**
     * Not from the ASN.1 specs. For implementation purposes.
     */
    int TAG_ANY = 0;
    int TAG_CHOICE = TAG_ANY;
}
