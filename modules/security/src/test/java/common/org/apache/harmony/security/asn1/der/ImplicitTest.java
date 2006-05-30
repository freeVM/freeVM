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

package org.apache.harmony.security.asn1.der;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.harmony.security.asn1.ASN1Boolean;
import org.apache.harmony.security.asn1.ASN1Constants;
import org.apache.harmony.security.asn1.ASN1Exception;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.DerInputStream;
import org.apache.harmony.security.asn1.DerOutputStream;


/**
 * ASN.1 DER test for Implicitly tagged type
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ImplicitTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ImplicitTest.class);
    }

    private static ASN1SequenceOf sequence = new ASN1SequenceOf(ASN1Boolean
            .getInstance());

    private static Object[][] taggedType = {
    // format: object to encode / ASN.1 tagged type / byte array

            //
            // Boolean = false
            //

            // [UNIVERSAL 5] Boolean
            {
                    Boolean.FALSE,
                    new byte[] { 0x05, 0x01, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_UNIVERSAL, 5,
                            ASN1Boolean.getInstance()) },

            // [APPLICATION 5] Boolean
            {
                    Boolean.FALSE,
                    new byte[] { 0x45, 0x01, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_APPLICATION, 5,
                            ASN1Boolean.getInstance()) },

            // [CONTEXT-SPECIFIC 5] Boolean
            {
                    Boolean.FALSE,
                    new byte[] { (byte) 0x85, 0x01, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_CONTEXTSPECIFIC, 5,
                            ASN1Boolean.getInstance()) },

            // [5] Boolean (default = CONTEXT-SPECIFIC)
            { Boolean.FALSE, new byte[] { (byte) 0x85, 0x01, 0x00 },
                    new ASN1Implicit(5, ASN1Boolean.getInstance()) },

            // [PRIVATE 5] Boolean
            {
                    Boolean.FALSE,
                    new byte[] { (byte) 0xC5, 0x01, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_PRIVATE, 5,
                            ASN1Boolean.getInstance()) },

            //
            // Boolean = true
            //

            // [UNIVERSAL 5] Boolean
            {
                    Boolean.TRUE,
                    new byte[] { 0x05, 0x01, (byte) 0xFF },
                    new ASN1Implicit(ASN1Constants.CLASS_UNIVERSAL, 5,
                            ASN1Boolean.getInstance()) },

            // [APPLICATION 5] Boolean
            {
                    Boolean.TRUE,
                    new byte[] { 0x45, 0x01, (byte) 0xFF },
                    new ASN1Implicit(ASN1Constants.CLASS_APPLICATION, 5,
                            ASN1Boolean.getInstance()) },

            // [CONTEXT-SPECIFIC 5] Boolean
            {
                    Boolean.TRUE,
                    new byte[] { (byte) 0x85, 0x01, (byte) 0xFF },
                    new ASN1Implicit(ASN1Constants.CLASS_CONTEXTSPECIFIC, 5,
                            ASN1Boolean.getInstance()) },

            // [5] Boolean (default = CONTEXT-SPECIFIC)
            {
                    Boolean.TRUE,
                    new byte[] { (byte) 0x85, 0x01, (byte) 0xFF },
                    new ASN1Implicit(ASN1Constants.CLASS_CONTEXTSPECIFIC, 5,
                            ASN1Boolean.getInstance()) },

            // [PRIVATE 5] Boolean
            {
                    Boolean.TRUE,
                    new byte[] { (byte) 0xC5, 0x01, (byte) 0xFF },
                    new ASN1Implicit(ASN1Constants.CLASS_PRIVATE, 5,
                            ASN1Boolean.getInstance()) },
            //
            // SequenceOf - testing constructed ASN.1 type
            //

            // [UNIVERSAL 5] SequenceOf
            {
                    new ArrayList(),
                    new byte[] { 0x25, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_UNIVERSAL, 5, sequence) },

            // [APPLICATION 5] SequenceOf
            {
                    new ArrayList(),
                    new byte[] { 0x65, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_APPLICATION, 5,
                            sequence) },

            // [CONTEXT-SPECIFIC 5] SequenceOf
            {
                    new ArrayList(),
                    new byte[] { (byte) 0xA5, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_CONTEXTSPECIFIC, 5,
                            sequence) },

            // [5] SequenceOf (default = CONTEXT-SPECIFIC)
            {
                    new ArrayList(),
                    new byte[] { (byte) 0xA5, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_CONTEXTSPECIFIC, 5,
                            sequence) },

            // [PRIVATE 5] SequenceOf
            { new ArrayList(), new byte[] { (byte) 0xE5, 0x00 },
                    new ASN1Implicit(ASN1Constants.CLASS_PRIVATE, 5, sequence) } };

    public void testDecode_Valid() throws IOException {

        for (int i = 0; i < taggedType.length; i++) {
            DerInputStream in = new DerInputStream((byte[]) taggedType[i][1]);
            assertEquals("Test case: " + i, taggedType[i][0],
                    ((ASN1Type) taggedType[i][2]).decode(in));
        }
    }

    // FIXME need testcase for decoding invalid encodings

    public void testEncode() throws IOException {

        for (int i = 0; i < taggedType.length; i++) {
            DerOutputStream out = new DerOutputStream(
                    (ASN1Type) taggedType[i][2], taggedType[i][0]);
            assertTrue("Test case: " + i, Arrays.equals(
                    (byte[]) taggedType[i][1], out.encoded));
        }
    }
    
    /**
     * Tests 2 consecutive implicit string type tagging
     * 
     * TYPE1 = [1] IMPLICIT OCTET STRING
     * TYPE2 = [2] IMPLICIT TYPE1
     */
    public void testConsecutiveStringTagging() throws Exception {
        ASN1Implicit type1 = new ASN1Implicit(1, ASN1OctetString.getInstance());

        ASN1Implicit type2 = new ASN1Implicit(2, type1);

        byte[] primitiveEncoding = new byte[] {
        // tag: class(CONTEXT SPECIFIC) + number (2) 
                (byte) 0x82,
                // length
                0x03,
                // value
                0x00, 0x01, 0x02 };

        byte[] constructedEncoding = new byte[] {
        // tag: class(CONTEXT SPECIFIC) + constructed +number (2) 
                (byte) 0xA2,
                // length
                0x00 };

        byte[] array = new byte[] { 0x00, 0x01, 0x02 };

        // decode primitive
        assertTrue(Arrays.equals(array, (byte[]) type2
                .decode(primitiveEncoding)));

        // encode primitive
        assertTrue(Arrays.equals(primitiveEncoding, (byte[]) type2
                .encode(array)));

        // decode constructed
        try {
            type2.decode(constructedEncoding);
            fail("No expected ASN1Exception");
        } catch (ASN1Exception e) {
            // FIXME any other check instead of comparing the message???
            assertEquals(
                    "ASN.1 octetstring: constructed identifier at [0]. Not valid for DER.",
                    e.getMessage());
        }
    }
}