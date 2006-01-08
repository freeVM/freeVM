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

package com.openintel.drl.security.asn1.der;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.openintel.drl.security.asn1.ASN1Boolean;
import com.openintel.drl.security.asn1.ASN1Exception;
import com.openintel.drl.security.asn1.ASN1Integer;
import com.openintel.drl.security.asn1.ASN1Sequence;
import com.openintel.drl.security.asn1.ASN1SequenceOf;
import com.openintel.drl.security.asn1.ASN1Type;
import com.openintel.drl.security.asn1.BerInputStream;
import com.openintel.drl.security.asn1.DerInputStream;
import com.openintel.drl.security.asn1.DerOutputStream;

/**
 * ASN.1 DER test for Sequence type
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class SequenceTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }

    private static ASN1SequenceOf sequenceOf = new ASN1SequenceOf(ASN1Boolean
            .getInstance());

    private static ArrayList defaultList;

    private static ASN1Sequence sequence;

    private static Object[][] testcases;

    protected void setUp() throws Exception {
        super.setUp();

        //
        // sequence ::= SEQUENCE {
        //     boolean BOOLEAN, DEFAULT true
        //     list SEQUENCE OF BOOLEAN, DEFAULT list(false)
        // }
        //

        defaultList = new ArrayList();
        defaultList.add(Boolean.FALSE);

        sequence = new ASN1Sequence(new ASN1Type[] { ASN1Boolean.getInstance(),
                sequenceOf }) {
            {
                setDefault(Boolean.TRUE, 0);
                setDefault(defaultList, 1);
            }

            protected Object getDecodedObject(BerInputStream in)
                    throws IOException {
                Object[] values = (Object[]) in.content;
                return new AppClass((Boolean) values[0], (List) values[1]);
            }

            protected void getValues(Object object, Object[] values) {
                AppClass obj = (AppClass) object;

                values[0] = obj.ok;
                values[1] = obj.list;
            }
        };

        //
        // Test Cases
        //

        testcases = new Object[][] {
                // format: object to encode / byte array 

                // sequence : all values are default
                new Object[] { new AppClass(Boolean.TRUE, defaultList),
                        new byte[] { 0x30, 0x00 } },

                // sequence : true, default
                new Object[] { new AppClass(Boolean.FALSE, defaultList),
                        new byte[] { 0x30, 0x03, 0x01, 0x01, 0x00 } },

                // sequence = default, empty sequence
                new Object[] { new AppClass(Boolean.TRUE, new ArrayList()),
                        new byte[] { 0x30, 0x02, 0x30, 0x00 } },

                // sequence = false, empty sequence
                new Object[] { new AppClass(Boolean.FALSE, new ArrayList()),
                        new byte[] { 0x30, 0x05, 0x01, 0x01, 0x00, 0x30, 0x00 } },

        //TODO add testcase for another ASN.1 type` 

        };
    }

    //
    // Application class
    //

    public static class AppClass {

        public Boolean ok;

        public List list;

        public AppClass(Boolean ok, List list) {
            this.ok = ok;
            this.list = list;
        }

        public boolean equals(Object o) {
            if (o instanceof AppClass) {
                AppClass obj = (AppClass) o;
                return ok.equals(obj.ok) && list.equals(obj.list);
            }
            return false;
        }
    }

    public void testDecode_Valid() throws IOException {

        for (int i = 0; i < testcases.length; i++) {
            try {
                DerInputStream in = new DerInputStream((byte[]) testcases[i][1]);
                assertEquals("Test case: " + i, testcases[i][0], sequence
                        .decode(in));
            } catch (ASN1Exception e) {
                fail("Test case: " + i + "\n" + e.getMessage());
            }
        }
    }

    //FIXME need testcase for decoding invalid encodings

    public void testEncode() throws IOException {

        for (int i = 0; i < testcases.length; i++) {
            DerOutputStream out = new DerOutputStream(sequence, testcases[i][0]);
            assertTrue("Test case: " + i, Arrays.equals(
                    (byte[]) testcases[i][1], out.encoded));
        }
    }

    public void testVerify() throws IOException {

        ASN1Sequence seqVerify = new ASN1Sequence(new ASN1Type[] {
                ASN1Boolean.getInstance(), sequenceOf }) {
            {
                setDefault(Boolean.TRUE, 0);
                setDefault(defaultList, 1);
            }

            protected Object getDecodedObject(BerInputStream in)
                    throws IOException {
                throw new IOException(
                        "Method getDecodedObject MUST not be invoked");
            }
        };

        for (int i = 0; i < testcases.length; i++) {
            DerInputStream in = new DerInputStream((byte[]) testcases[i][1]);
            in.setVerify();
            seqVerify.verify(in);
        }
    }

    public void testEncodeDefault() throws IOException {

        //
        // Boolean as default
        //
        ASN1Sequence s = new ASN1Sequence(new ASN1Type[] { ASN1Boolean
                .getInstance() }) {
            {
                setDefault(Boolean.TRUE, 0);
            }

            protected void getValues(Object object, Object[] values) {
                values = (Object[]) object;
            }
        };

        byte[] expectedArray = new byte[] { 0x30, 0x00 };

        byte[] encoded = s.encode(new Object[] { Boolean.TRUE });
        assertTrue("Encoded boolean:", Arrays.equals(expectedArray, encoded));

        //
        // Integer as default
        //
        s = new ASN1Sequence(new ASN1Type[] { ASN1Integer.getInstance() }) {
            {
                setDefault(new byte[] { 0x01 }, 0);
            }

            protected void getValues(Object object, Object[] values) {
                values = (Object[]) object;
            }
        };

        encoded = s.encode(new Object[] { new byte[] { 0x01 } });
        assertTrue("Encoded integer:", Arrays.equals(expectedArray, encoded));
    }
}