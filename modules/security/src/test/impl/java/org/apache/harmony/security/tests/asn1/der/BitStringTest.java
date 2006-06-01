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

package org.apache.harmony.security.tests.asn1.der;

import java.io.IOException;
import java.util.Arrays;

import org.apache.harmony.security.asn1.ASN1Exception;
import org.apache.harmony.security.asn1.BitString;
import org.apache.harmony.security.asn1.DerInputStream;
import org.apache.harmony.security.asn1.DerOutputStream;

import junit.framework.TestCase;

import org.apache.harmony.security.asn1.ASN1BitString;
import org.apache.harmony.security.asn1.ASN1BitString.ASN1NamedBitList;

/**
 * ASN.1 DER test for Bitstring type
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class BitStringTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(BitStringTest.class);
    }

    private static Object[][] testcase = new Object[][] {
    //bitstring array format: bitstring object/ byte array
            // 0
            new Object[] { new BitString(new byte[] {}, 0), // object 
                    new byte[] { 0x03, 0x01, 0x00 } },
            // 1 
            new Object[] { new BitString(new byte[] { 0x05 }, 0), // object 
                    new byte[] { 0x03, 0x02, 0x00, 0x05 } },
            // 2
            new Object[] { new BitString(new byte[] { (byte) 0x80 }, 7), // object
                    new byte[] { 0x03, 0x02, 0x07, (byte) 0x80 } } };

    public void testDecode_Valid() throws IOException {

        for (int i = 0; i < testcase.length; i++) {
            DerInputStream in = new DerInputStream((byte[]) testcase[i][1]);

            BitString expected = (BitString) testcase[i][0];
            BitString decoded = (BitString) ASN1BitString.getInstance().decode(
                    in);

            assertEquals("Testcase: " + i, expected.unusedBits,
                    decoded.unusedBits);

            assertTrue("Testcase: " + i, Arrays.equals(expected.bytes,
                    decoded.bytes));
        }
    }

    public void testDecode_Invalid() throws IOException {
        byte[][] invalid = new byte[][] {
        // wrong tag: tag is not 0x03
                new byte[] { 0x02, 0x01, 0x00 },
                // wrong length: length is 0
                new byte[] { 0x03, 0x00 },
                // wrong content: unused bits value > 7
                new byte[] { 0x03, 0x03, 0x09, 0x0F, 0x0F },
                // wrong content: not 0 unused bits for empty string
                new byte[] { 0x03, 0x01, 0x01 },
                // wrong content: unused bits in final octet are not 0
                new byte[] { 0x03, 0x02, 0x01, 0x01 },
                // wrong content: constructed encoding
                new byte[] { 0x23, 0x03, 0x03, 0x01, 0x00 } };

        for (int i = 0; i < invalid.length; i++) {
            try {
                DerInputStream in = new DerInputStream(invalid[i]);
                ASN1BitString.getInstance().decode(in);
                fail("No expected ASN1Exception for: " + i);
            } catch (ASN1Exception e) {
            }
        }
    }

    public void testEncode() throws IOException {

        for (int i = 0; i < testcase.length; i++) {
            DerOutputStream out = new DerOutputStream(ASN1BitString
                    .getInstance(), testcase[i][0]);
            assertTrue("Testcase: " + i, Arrays.equals((byte[]) testcase[i][1],
                    out.encoded));
        }
    }

    //
    //
    // Named Bit List
    //
    //

    public void testDecodeNamedBitList() throws IOException {

        Object[][] testcaseBoolean = new Object[][] {
                //bitstring array format: bitstring object/ byte array
                // 0
                new Object[] { new boolean[] {}, // object 
                        new byte[] { 0x03, 0x01, 0x00 } },
                // 1
                new Object[] { new boolean[] { true }, // object 
                        new byte[] { 0x03, 0x02, 0x07, (byte) 0x80 } },
                // 2 
                new Object[] { new boolean[] { true, false, true }, // object 
                        new byte[] { 0x03, 0x02, 0x05, (byte) 0xA0 } },
                // 3
                new Object[] {
                        new boolean[] { true, true, true, true, true, true,
                                true, true }, // object
                        new byte[] { 0x03, 0x02, 0x00, (byte) 0xFF } },
                // 4
                new Object[] {
                        new boolean[] { false, false, false, false, false,
                                false, false, false, true }, // object
                        new byte[] { 0x03, 0x03, 0x07, 0x00, (byte) 0x80 } } };

        ASN1NamedBitList decoder = new ASN1NamedBitList();

        for (int i = 0; i < testcaseBoolean.length; i++) {
            DerInputStream in = new DerInputStream(
                    (byte[]) testcaseBoolean[i][1]);

            assertTrue("Testcase: " + i, Arrays.equals(
                    (boolean[]) testcaseBoolean[i][0], (boolean[]) decoder
                            .decode(in)));
        }
    }

    public void testDecodeNamedBitList_SizeConstraints() throws IOException {

        Object[][] testcaseBoolean = new Object[][] {
                //bitstring array format: bitstring object/ byte array
                // 0
                new Object[] {
                        new boolean[] { false, false, false, false, false,
                                false, false, false }, // object 
                        new byte[] { 0x03, 0x01, 0x00 } },
                // 1
                new Object[] {
                        new boolean[] { true, false, false, false, false,
                                false, false, false }, // object 
                        new byte[] { 0x03, 0x02, 0x07, (byte) 0x80 } },
                // 2 
                new Object[] {
                        new boolean[] { true, false, true, false, false, false,
                                false, false }, // object 
                        new byte[] { 0x03, 0x02, 0x05, (byte) 0xA0 } },
                // 3
                new Object[] {
                        new boolean[] { true, true, true, true, true, true,
                                true, true }, // object
                        new byte[] { 0x03, 0x02, 0x00, (byte) 0xFF } },
                // 4
                new Object[] {
                        new boolean[] { false, false, false, false, false,
                                false, false, false, true }, // object
                        new byte[] { 0x03, 0x03, 0x07, 0x00, (byte) 0x80 } } };

        ASN1NamedBitList decoder = new ASN1NamedBitList(8);

        for (int i = 0; i < testcaseBoolean.length; i++) {
            DerInputStream in = new DerInputStream(
                    (byte[]) testcaseBoolean[i][1]);

            assertTrue("Testcase: " + i, Arrays.equals(
                    (boolean[]) testcaseBoolean[i][0], (boolean[]) decoder
                            .decode(in)));
        }
    }

    public void testEncodeNamedBitList() throws IOException {

        Object[][] testcaseBoolean = new Object[][] {
                //bitstring array format: bitstring object/ byte array
                // 0
                new Object[] { new boolean[] {}, // object 
                        new byte[] { 0x03, 0x01, 0x00 } },
                // 1
                new Object[] { new boolean[] { false }, // object 
                        new byte[] { 0x03, 0x01, 0x00 } },
                // 2
                new Object[] { new boolean[] { true }, // object 
                        new byte[] { 0x03, 0x02, 0x07, (byte) 0x80 } },
                // 3 
                new Object[] { new boolean[] { true, false, true }, // object 
                        new byte[] { 0x03, 0x02, 0x05, (byte) 0xA0 } },
                // 4
                new Object[] {
                        new boolean[] { true, true, true, true, true, true,
                                true, true }, // object
                        new byte[] { 0x03, 0x02, 0x00, (byte) 0xFF } },
                // 5
                new Object[] {
                        new boolean[] { false, false, false, false, false,
                                false, false, false, true }, // object
                        new byte[] { 0x03, 0x03, 0x07, 0x00, (byte) 0x80 } } };

        ASN1NamedBitList encoder = new ASN1NamedBitList();

        for (int i = 0; i < testcaseBoolean.length; i++) {
            DerOutputStream out = new DerOutputStream(encoder,
                    testcaseBoolean[i][0]);
            assertTrue("Testcase: " + i, Arrays.equals(
                    (byte[]) testcaseBoolean[i][1], out.encoded));
        }
    }
}