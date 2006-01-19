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
* @author Alexander V. Esin, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.x501;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.ASN1Constants;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1StringType;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.BerOutputStream;
import org.apache.harmony.security.utils.ObjectIdentifier;


/**
 * X.501 AttributeTypeAndValue
 */
public class AttributeTypeAndValue {

    // Country code attribute (name from RFC 1779)
    private static final ObjectIdentifier C;

    // Common name attributem (name from RFC 1779)
    private static final ObjectIdentifier CN;

    // Domain component attribute (name from RFC 2253)
    private static final ObjectIdentifier DC;

    // DN qualifier attribute (name from API spec)
    private static final ObjectIdentifier DNQ;

    private static final ObjectIdentifier DNQUALIFIER;

    // Email Address attribute (name from API spec)
    private static final ObjectIdentifier EMAILADDRESS;

    // Generation attribute (qualifies an individual's name)
    // (name from API spec)
    private static final ObjectIdentifier GENERATION;

    // Given name attribute (name from API spec)
    private static final ObjectIdentifier GIVENNAME;

    // Initials attribute (initials of an individual's name)
    // (name from API spec)
    private static final ObjectIdentifier INITIALS;

    // Name of a locality attribute (name from RFC 1779)
    private static final ObjectIdentifier L;

    // Organization name attribute (name from RFC 1779)
    private static final ObjectIdentifier O;

    // Organizational unit name attribute (name from RFC 1779)
    private static final ObjectIdentifier OU;

    // Serial number attribute (serial number of a device)
    // (name from API spec)
    private static final ObjectIdentifier SERIALNUMBER;

    // Attribute for the full name of a state or province
    // (name from RFC 1779)
    private static final ObjectIdentifier ST;

    // Street attribute (name from RFC 1779)
    private static final ObjectIdentifier STREET;

    // Surname attribute (comes from an individual's parent name) 
    // (name from API spec)
    private static final ObjectIdentifier SURNAME;

    // Title attribute (object in an organzation)(name from API spec)
    private static final ObjectIdentifier T;

    // User identifier attribute (name from RFC 2253)
    private static final ObjectIdentifier UID;

    //
    // OID's pool
    //

    // pool's capacity
    private static final int CAPACITY;

    // pool's size
    private static final int SIZE;

    // pool: contains all recognizable attribute type keywords
    private static final ObjectIdentifier[][] KNOWN_OIDS;

    // known keywords attribute
    private static final HashMap KNOWN_NAMES = new HashMap(30);

    // known attribute types for RFC1779 (see Table 1)
    private static final HashMap RFC1779_NAMES = new HashMap(10);

    // known attribute types for RFC2253
    // (see 2.3.  Converting AttributeTypeAndValue)
    private static final HashMap RFC2253_NAMES = new HashMap(10);

    // known attribute types for RFC2459 (see API spec.)
    private static final HashMap RFC2459_NAMES = new HashMap(10);

    static {

        // pool initialization
        CAPACITY = 10;
        SIZE = 10;
        KNOWN_OIDS = new ObjectIdentifier[SIZE][CAPACITY];

        // init known attribute type keywords
        C = new ObjectIdentifier(new int[] { 2, 5, 4, 6 }, "C", RFC1779_NAMES);
        CN = new ObjectIdentifier(new int[] { 2, 5, 4, 3 }, "CN", RFC1779_NAMES);

        DC = new ObjectIdentifier(
                new int[] { 0, 9, 2342, 19200300, 100, 1, 25 }, "DC",
                RFC2253_NAMES);
        // DN qualifier aliases
        DNQ = new ObjectIdentifier(new int[] { 2, 5, 4, 46 }, "DNQ",
                RFC2459_NAMES);
        DNQUALIFIER = new ObjectIdentifier(new int[] { 2, 5, 4, 46 },
                "DNQUALIFIER", RFC2459_NAMES);

        EMAILADDRESS = new ObjectIdentifier(new int[] { 1, 2, 840, 113549, 1,
                9, 1 }, "EMAILADDRESS", RFC2459_NAMES);

        GENERATION = new ObjectIdentifier(new int[] { 2, 5, 4, 44 },
                "GENERATION", RFC2459_NAMES);
        GIVENNAME = new ObjectIdentifier(new int[] { 2, 5, 4, 42 },
                "GIVENNAME", RFC2459_NAMES);

        INITIALS = new ObjectIdentifier(new int[] { 2, 5, 4, 43 }, "INITIALS",
                RFC2459_NAMES);

        L = new ObjectIdentifier(new int[] { 2, 5, 4, 7 }, "L", RFC1779_NAMES);

        O = new ObjectIdentifier(new int[] { 2, 5, 4, 10 }, "O", RFC1779_NAMES);
        OU = new ObjectIdentifier(new int[] { 2, 5, 4, 11 }, "OU",
                RFC1779_NAMES);

        SERIALNUMBER = new ObjectIdentifier(new int[] { 2, 5, 4, 5 },
                "SERIALNUMBER", RFC2459_NAMES);
        ST = new ObjectIdentifier(new int[] { 2, 5, 4, 8 }, "ST", RFC1779_NAMES);
        STREET = new ObjectIdentifier(new int[] { 2, 5, 4, 9 }, "STREET",
                RFC1779_NAMES);
        SURNAME = new ObjectIdentifier(new int[] { 2, 5, 4, 4 }, "SURNAME",
                RFC2459_NAMES);

        T = new ObjectIdentifier(new int[] { 2, 5, 4, 12 }, "T", RFC2459_NAMES);

        UID = new ObjectIdentifier(
                new int[] { 0, 9, 2342, 19200300, 100, 1, 1 }, "UID",
                RFC2253_NAMES);

        //
        // RFC1779
        //
        RFC1779_NAMES.put(CN.getName(), CN);
        RFC1779_NAMES.put(L.getName(), L);
        RFC1779_NAMES.put(ST.getName(), ST);
        RFC1779_NAMES.put(O.getName(), O);
        RFC1779_NAMES.put(OU.getName(), OU);
        RFC1779_NAMES.put(C.getName(), C);
        RFC1779_NAMES.put(STREET.getName(), STREET);

        //
        // RFC2253: includes all from RFC1779
        //
        RFC2253_NAMES.putAll(RFC1779_NAMES);

        RFC2253_NAMES.put(DC.getName(), DC);
        RFC2253_NAMES.put(UID.getName(), UID);

        //
        // RFC2459
        //
        RFC2459_NAMES.put(DNQ.getName(), DNQ);
        RFC2459_NAMES.put(DNQUALIFIER.getName(), DNQUALIFIER);
        RFC2459_NAMES.put(EMAILADDRESS.getName(), EMAILADDRESS);
        RFC2459_NAMES.put(GENERATION.getName(), GENERATION);
        RFC2459_NAMES.put(GIVENNAME.getName(), GIVENNAME);
        RFC2459_NAMES.put(INITIALS.getName(), INITIALS);
        RFC2459_NAMES.put(SERIALNUMBER.getName(), SERIALNUMBER);
        RFC2459_NAMES.put(SURNAME.getName(), SURNAME);
        RFC2459_NAMES.put(T.getName(), T);

        //
        // Init KNOWN_OIDS pool
        //

        // add from RFC2253 (includes RFC1779) 
        Iterator it = RFC2253_NAMES.values().iterator();
        while (it.hasNext()) {
            addOID((ObjectIdentifier) it.next());
        }

        // add attributes from RFC2459
        it = RFC2459_NAMES.values().iterator();
        while (it.hasNext()) {
            Object o = it.next();

            //don't add DNQUALIFIER because it has the same oid as DNQ
            if (!(o == DNQUALIFIER)) {
                addOID((ObjectIdentifier) o);
            }
        }

        //
        // Init KNOWN_NAMES pool
        //

        KNOWN_NAMES.putAll(RFC2253_NAMES); // RFC2253 includes RFC1779
        KNOWN_NAMES.putAll(RFC2459_NAMES);
    }

    //Attribute type
    private final ObjectIdentifier oid;

    //Attribute value
    private AttributeValue value;

    // for decoder only
    private AttributeTypeAndValue(int[] oid, AttributeValue value)
            throws IOException {

        ObjectIdentifier thisOid = getOID(oid);
        if (thisOid == null) {
            thisOid = new ObjectIdentifier(oid);
        }
        this.oid = thisOid;
        this.value = value;
    }

    /**
     * Creates AttributeTypeAndValue with OID and AttributeValue. Parses OID
     * string representation
     * 
     * @param sOid
     *            string representation of OID
     * @param value
     *            attribute value
     * @throws IOException
     *             if OID can not be created from its string representation
     */
    public AttributeTypeAndValue(String sOid, AttributeValue value) {
        if (sOid.charAt(0) >= '0' && sOid.charAt(0) <= '9') {

            int[] array = org.apache.harmony.security.asn1.ObjectIdentifier
                    .toIntArray(sOid);

            ObjectIdentifier thisOid = getOID(array);
            if (thisOid == null) {
                thisOid = new ObjectIdentifier(array);
            }
            this.oid = thisOid;

        } else {
            this.oid = (ObjectIdentifier) KNOWN_NAMES.get(sOid.toUpperCase());
            if (this.oid == null) {
                throw new IllegalArgumentException(
                        "Unrecognizable attribute name: " + sOid);
            }
        }
        this.value = value;
    }

    /**
     * Appends AttributeTypeAndValue string representation
     * 
     * @param attrFormat - format of DN
     * @param buf - string buffer to be used
     */
    public void appendName(String attrFormat, StringBuffer buf) {

        boolean hexFormat = false;
        if (attrFormat == X500Principal.RFC1779) {
            if (RFC1779_NAMES == oid.getGroup()) {
                buf.append(oid.getName());
            } else {
                buf.append(oid.toOIDString());
            }

            buf.append('=');
            if (value.escapedString == value.getHexString()) {
                //FIXME all chars in upper case
                buf.append(value.getHexString().toUpperCase());
            } else if (value.escapedString.length() != value.rawString.length()) {
                // was escaped
                value.appendQEString(buf);
            } else {
                buf.append(value.escapedString);
            }
        } else {
            Object group = oid.getGroup();
            // RFC2253 includes names from RFC1779
            if (RFC1779_NAMES == group || RFC2253_NAMES == group) {
                buf.append(oid.getName());

                if (attrFormat == X500Principal.CANONICAL) {
                    // only PrintableString and UTF8String in string format
                    // all others are output in hex format
                    int tag = value.getTag();
                    if (!ASN1StringType.UTF8STRING.checkTag(tag)
                            && !ASN1StringType.PRINTABLESTRING.checkTag(tag)) {
                        hexFormat = true;
                    }
                }

            } else {
                buf.append(oid.toString());
                hexFormat = true;
            }

            buf.append('=');

            if (hexFormat) {
                buf.append(value.getHexString());
            } else {
                if (attrFormat == X500Principal.CANONICAL) {
                    buf.append(value.makeCanonical());
                } else {
                    buf.append(value.escapedString);
                }
            }
        }
    }

    /**
     * Gets type of the AttributeTypeAndValue
     * 
     * @return ObjectIdentifier
     */
    public ObjectIdentifier getType() {
        return oid;
    }

    /**
     * According to RFC 3280 (http://www.ietf.org/rfc/rfc3280.txt) 
     * X.501 AttributeTypeAndValue structure is defined as follows:
     *  
     *   AttributeTypeAndValue ::= SEQUENCE {
     *      type     AttributeType,
     *      value    AttributeValue }
     *   
     *    AttributeType ::= OBJECT IDENTIFIER
     *  
     *    AttributeValue ::= ANY DEFINED BY AttributeType
     *    ...
     *    DirectoryString ::= CHOICE {
     *          teletexString           TeletexString (SIZE (1..MAX)),
     *          printableString         PrintableString (SIZE (1..MAX)),
     *          universalString         UniversalString (SIZE (1..MAX)),
     *          utf8String              UTF8String (SIZE (1.. MAX)),
     *          bmpString               BMPString (SIZE (1..MAX)) }
     *  
     */

    public static ASN1Type AttributeValue = new ASN1Type(
            ASN1Constants.TAG_PRINTABLESTRING) {

        public boolean checkTag(int tag) {
            return true;
        }

        public void verify(BerInputStream in) throws IOException {

            switch (in.tag) {
            case ASN1Constants.TAG_TELETEXSTRING:
                ASN1StringType.TELETEXSTRING.verify(in);
                break;
            case ASN1Constants.TAG_PRINTABLESTRING:
                ASN1StringType.PRINTABLESTRING.verify(in);
                break;
            case ASN1Constants.TAG_UNIVERSALSTRING:
                ASN1StringType.UNIVERSALSTRING.verify(in);
                break;
            case ASN1Constants.TAG_UTF8STRING:
                ASN1StringType.UTF8STRING.verify(in);
                break;
            case ASN1Constants.TAG_BMPSTRING:
                ASN1StringType.BMPSTRING.verify(in);
                break;
            }
            in.readContent();
        }

        public Object decode(BerInputStream in) throws IOException {

            // FIXME what about constr???
            String str = null;

            switch (in.tag) {
            case ASN1Constants.TAG_TELETEXSTRING:
                str = (String) ASN1StringType.TELETEXSTRING.decode(in);
                break;
            case ASN1Constants.TAG_PRINTABLESTRING:
                str = (String) ASN1StringType.PRINTABLESTRING.decode(in);
                break;
            case ASN1Constants.TAG_UNIVERSALSTRING:
                str = (String) ASN1StringType.UNIVERSALSTRING.decode(in);
                break;
            case ASN1Constants.TAG_UTF8STRING:
                str = (String) ASN1StringType.UTF8STRING.decode(in);
                break;
            case ASN1Constants.TAG_BMPSTRING:
                str = (String) ASN1StringType.BMPSTRING.decode(in);
                break;
            default:
                in.readContent();
            }

            byte[] bytesEncoded = new byte[in.getOffset() - in.getTagOffset()];
            System.arraycopy(in.getBuffer(), in.getTagOffset(), bytesEncoded,
                    0, bytesEncoded.length);

            return new AttributeValue(str, bytesEncoded, in.tag);
        }

        public Object getDecodedObject(BerInputStream in) throws IOException {
            // stub to avoid wrong decoder usage
            throw new RuntimeException(
                    "AttributeValue getDecodedObject MUST not be invoked");
        }

        //
        // Encode
        //
        public void encodeASN(BerOutputStream out) {

            AttributeValue av = (AttributeValue) out.content;

            if (av.encoded != null) {
                out.content = av.encoded;
                out.encodeANY();
            } else {
                out.encodeTag(av.getTag());
                out.content = av.bytes;
                out.encodeString();
            }
        }

        public void setEncodingContent(BerOutputStream out) {
            
            AttributeValue av = (AttributeValue) out.content;

            if (av.encoded != null) {
                out.length = av.encoded.length;
            } else {

                if (av.getTag() == ASN1Constants.TAG_UTF8STRING) {

                    out.content = av.rawString;

                    ASN1StringType.UTF8STRING.setEncodingContent(out);

                    av.bytes = (byte[]) out.content;
                    out.content = av;
                } else {
                    av.bytes = av.rawString.getBytes();
                    out.length = av.bytes.length;
                }
            }
        }

        public void encodeContent(BerOutputStream out) {
            // stub to avoid wrong encoder usage
            throw new RuntimeException(
                    "AttributeValue encodeContent MUST not be invoked");
        }

        public int getEncodedLength(BerOutputStream out) { //FIXME name

            AttributeValue av = (AttributeValue) out.content;

            if (av.encoded != null) {
                return out.length;
            } else {
                return super.getEncodedLength(out);
            }
        }
    };

    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), AttributeValue }) {

        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            return new AttributeTypeAndValue((int[]) values[0],
                    (AttributeValue) values[1]);
        }

        protected void getValues(Object object, Object[] values) {
            AttributeTypeAndValue atav = (AttributeTypeAndValue) object;

            values[0] = atav.oid.getOid();
            values[1] = atav.value;
        }
    };

    // returns known OID or null
    private static ObjectIdentifier getOID(int[] oid) {

        int index = hashIntArray(oid) % CAPACITY;

        // look for OID in the pool 
        ObjectIdentifier[] list = KNOWN_OIDS[index];
        for (int i = 0; list[i] != null; i++) {
            if (Arrays.equals(oid, list[i].getOid())) {
                return list[i];
            }
        }
        return null;
    }

    // adds known OID to pool
    // for static AttributeTypeAndValue initialization only
    private static void addOID(ObjectIdentifier oid) {

        int[] newOid = oid.getOid();
        int index = hashIntArray(newOid) % CAPACITY;

        // look for OID in the pool 
        ObjectIdentifier[] list = KNOWN_OIDS[index];
        int i = 0;
        for (; list[i] != null; i++) {

            // check wrong static initialization: no duplicate OIDs
            if (Arrays.equals(newOid, list[i].getOid())) {
                throw new Error(
                        "ObjectIdentifier: invalid static initialization"
                                + " - duplicate OIDs:" + oid.getName() + ", "
                                + list[i].getName());
            }
        }

        // check : to avoid NPE
        if (i == (CAPACITY - 1)) {
            throw new Error("ObjectIdentifier: invalid static initialization"
                    + " - small OID pool capacity");
        }
        list[i] = oid;
    }

    // returns hash for array of integers
    private static int hashIntArray(int[] oid) {
        int intHash = 0;
        for (int i = 0; i < oid.length && i < 4; i++) {
            intHash += oid[i] << (8 * i); //TODO what about to find better one?
        }
        return intHash & 0x7FFFFFFF; // only positive
    }
}