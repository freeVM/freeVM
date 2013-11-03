/* 
 *  Licensed to the Apache Software Foundation (ASF) under one or more 
 *  contributor license agreements.  See the NOTICE file distributed with 
 *  this work for additional information regarding copyright ownership. 
 *  The ASF licenses this file to You under the Apache License, Version 2.0 
 *  (the "License"); you may not use this file except in compliance with 
 *  the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 */ 

package org.apache.harmony.jndi.internal.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ldap.Rdn;

import org.apache.harmony.jndi.internal.nls.Messages;
import org.apache.harmony.security.x509.DNParser;

/**
 * A relative distinguised name parser that does less checkings than DNParser
 * 
 * @author Osvaldo C. Demo
 */
public class RelaxedDnParser extends DNParser {

    private static final char[] specialChars = new char[] { '!', '$', '%', '&',
            '\'', '(', ')', '*', '/', ':', '<', '=', '>', '?', '@', '[', ']',
            '^', '_', '`', '{', '|', '}', '~', '\"', '#', '+', ',', '\\', ';' };

    /**
     * Constructor
     * 
     * @param dn
     *            the String to parse
     * @throws IOException
     */
    public RelaxedDnParser(String dn) throws IOException {
        super(dn);
    }

    protected String nextAT() throws IOException {
        int equalIndex = pos;
        try {
            while (chars[equalIndex] != '=') {
                equalIndex++; // points to the equals on the current attribute
                // type pair
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(Messages.getString("ldap.1C"));
        }

        // Check for quotations and special chars on the type
        for (int i = pos; i < equalIndex; i++) {
            if (chars[i] == '\\') {
                throw new IOException(Messages.getString("ldap.1D"));
            }
            for (int j = 0; j < specialChars.length; j++) {
                if (chars[i] == specialChars[j]) {
                    throw new IOException(Messages.getString("ldap.1E"));
                }
            }
        }
        return super.nextAT();
    }

    protected String hexAV() throws IOException {
        beg = pos; // store '#' position
        pos++;

        while (true) {
            // check for end of attribute value
            // looks for space and component separators
            if (pos == length || chars[pos] == '+' || chars[pos] == ','
                    || chars[pos] == ';') {
                end = pos;
                break;
            }

            if (chars[pos] >= 'A' && chars[pos] <= 'F') {
                chars[pos] += 32; // to low case
            }
            pos++;
        }

        // verify length of hex string
        // encoded byte array must be even number
        int hexLen = end - beg; // skip first '#' char
        // get byte encoding from string representation
        encoded = new byte[hexLen / 2];
        for (int i = 0, p = beg + 1; i < encoded.length; p += 2, i++) {
            encoded[i] = (byte) getByte(p);
        }
        return new String(chars, beg, hexLen);

    }

    /**
     * Begin to parse the string given in the constructor
     */
    public List parse() throws IOException {
        List list = new ArrayList();

        String attValue;
        String attType = nextAT();
        List atav = new ArrayList();
        while (true) {
            if (pos == length) {
                // empty Attribute Value
                atav.add(new AttributeTypeAndValuePair(attType, ""));
                list.add(0, atav);
                return list;
            }

            switch (chars[pos]) {
            case '"':
                attValue = quotedAV();
                atav.add(new AttributeTypeAndValuePair(attType, attValue));
                break;
            case '#':
                attValue = hexAV();
                atav.add(new AttributeTypeAndValuePair(attType, Rdn
                        .unescapeValue(attValue)));
                break;
            case '+':
                // empty attribute value
                atav.add(new AttributeTypeAndValuePair(attType, ""));
                break;
            default:
                attValue = escapedAV();
                atav.add(new AttributeTypeAndValuePair(attType, attValue));
            }

            if (pos >= length) {
                list.add(0, atav);
                return list;
            }

            if (chars[pos] == ',' || chars[pos] == ';') {
                throw new IOException(Messages.getString("ldap.1F"));
            }

            pos++;
            attType = nextAT();
        }
    }

    protected char getEscaped() throws IOException {
        pos++;
        if (pos == length) {
            throw new IOException(Messages.getString("ldap.1F"));
        }

        switch (chars[pos]) {
        case '"':
            return chars[pos];
        case '\\':
            hasQE = true;
            return hexToUTF8(new String(chars, pos++, 2));
        case ',':
        case '=':
        case '+':
        case '<':
        case '>':
        case '#':
        case ';':
        case ' ':
            // FIXME: escaping is allowed only for leading or trailing space
            // char
            return chars[pos];
        default:
            // RFC doesn't explicitly say that escaped hex pair is
            // interpreted as UTF-8 char. It only contains an example of such
            // DN.
            return super.getUTF8();
        }
    }

    /**
     * Converts an String in Hexadecimal to a character
     * 
     * @param hex
     *            the Hexadecimal string
     * @throws IOException
     *             when an invalid Digit is passed or when the size is different
     *             than 2
     * @return the corresponding character
     */
    public static Character hexToUTF8(String hex) throws IOException {
        try {
            return new Character((char) Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            throw new IOException(Messages.getString("ldap.20"));
        }
    }
}
