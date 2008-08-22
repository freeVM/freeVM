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
package org.apache.harmony.unpack200.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.harmony.pack200.Pack200Exception;
import org.apache.harmony.unpack200.AttrDefinitionBands;
import org.apache.harmony.unpack200.BcBands;
import org.apache.harmony.unpack200.ClassBands;
import org.apache.harmony.unpack200.CpBands;
import org.apache.harmony.unpack200.Segment;
import org.apache.harmony.unpack200.SegmentConstantPool;
import org.apache.harmony.unpack200.bytecode.CPClass;
import org.apache.harmony.unpack200.bytecode.CPDouble;
import org.apache.harmony.unpack200.bytecode.CPFieldRef;
import org.apache.harmony.unpack200.bytecode.CPFloat;
import org.apache.harmony.unpack200.bytecode.CPInteger;
import org.apache.harmony.unpack200.bytecode.CPInterfaceMethodRef;
import org.apache.harmony.unpack200.bytecode.CPLong;
import org.apache.harmony.unpack200.bytecode.CPMethodRef;
import org.apache.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.harmony.unpack200.bytecode.CPString;
import org.apache.harmony.unpack200.bytecode.CPUTF8;

/**
 * Tests for Pack200 bytecode bands
 */

/*
 * TODO: The number 8 is used in most of the tests in this class as a low
 * (non-zero) number that is not likely to indicate a multiple byte number, but
 * should be replaced with properly encoded byte arrays when encoding is
 * implemented.
 */
public class BcBandsTest extends AbstractBandsTestCase {

    public class MockCpBands extends CpBands {

        private final CPUTF8 cpUTF8 = new CPUTF8("java/lang/String");
        private final CPClass cpClass = new CPClass(cpUTF8, -1);
        private final CPNameAndType descriptor = new CPNameAndType(new CPUTF8(
                "Hello"), new CPUTF8("(a, b, c)"), -1);

        public MockCpBands(Segment segment) {
            super(segment);
        }

        public CPString cpStringValue(int index) {
            return new CPString(cpUTF8, index);
        }

        public CPInteger cpIntegerValue(int index) {
            return new CPInteger(new Integer(21), index);
        }

        public CPClass cpClassValue(int index) {
            return cpClass;
        }

        public CPFloat cpFloatValue(int index) {
            return new CPFloat(new Float(2.5F), index);
        }

        public CPLong cpLongValue(int index) {
            return new CPLong(new Long(21L), index);
        }

        public CPDouble cpDoubleValue(int index) {
            return new CPDouble(new Double(2.5D), index);
        }

        public CPFieldRef cpFieldValue(int index) {
            return new CPFieldRef(cpClass, descriptor, index);
        }

        public CPMethodRef cpMethodValue(int index) {
            return new CPMethodRef(cpClass, descriptor, index);
        }

        public CPInterfaceMethodRef cpIMethodValue(int index) {
            return new CPInterfaceMethodRef(cpClass, descriptor, index);
        }

        public String[] getCpClass() {
            return new String[] {"Hello"};
        }

        public String[] getCpFieldClass() {
            return new String[]{};
        }

        public String[] getCpMethodClass() {
            return new String[]{};
        }

        public String[] getCpIMethodClass() {
            return new String[]{};
        }
    }

    public class MockClassBands extends ClassBands {

        public MockClassBands(Segment segment) {
            super(segment);
        }

        public long[][] getMethodFlags() {
            long[][] flags = new long[numClasses][];
            for (int i = 0; i < flags.length; i++) {
                flags[i] = new long[numMethods[i]];
            }
            return flags;
        }

        public int[] getCodeMaxStack() {
            int totalMethods = 0;
            for (int i = 0; i < numClasses; i++) {
                totalMethods += numMethods[i];
            }
            return new int[totalMethods];
        }

        public int[] getCodeMaxNALocals() {
            int totalMethods = 0;
            for (int i = 0; i < numClasses; i++) {
                totalMethods += numMethods[i];
            }
            return new int[totalMethods];
        }

        public String[][] getMethodDescr() {
            String[][] descr = new String[numClasses][];
            for (int i = 0; i < descr.length; i++) {
                descr[i] = new String[numMethods[i]];
                for (int j = 0; j < descr[i].length; j++) {
                    descr[i][j] = "hello()";
                }
            }
            return descr;
        }

        public int[] getClassThisInts() {
            int[] thisClasses = new int[numClasses];
            for (int index = 0; index < numClasses; index++) {
                thisClasses[index] = 0;
            }
            return thisClasses;
        }

        public int[] getClassSuperInts() {
            int[] superClasses = new int[numClasses];
            for (int index = 0; index < numClasses; index++) {
                superClasses[index] = 0;
            }
            return superClasses;
        }

        public ArrayList[][] getMethodAttributes() {
            ArrayList[][] attributes = new ArrayList[numClasses][];
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = new ArrayList[numMethods[i]];
                for (int j = 0; j < attributes[i].length; j++) {
                    attributes[i][j] = new ArrayList();
                }
            }
            return attributes;
        }

        public ArrayList getOrderedCodeAttributes() {
            int totalMethods = 0;
            for (int classIndex = 0; classIndex < numMethods.length; classIndex++) {
                totalMethods = totalMethods + numMethods[classIndex];
            }
            ArrayList orderedAttributeList = new ArrayList();
            for (int classIndex = 0; classIndex < totalMethods; classIndex++) {
                ArrayList currentAttributes = new ArrayList();
                orderedAttributeList.add(currentAttributes);
            }
            return orderedAttributeList;
        }
    }

    public class MockSegment extends AbstractBandsTestCase.MockSegment {

        public CpBands cpBands;

        protected AttrDefinitionBands getAttrDefinitionBands() {
            return new MockAttributeDefinitionBands(this);
        }

        protected CpBands getCpBands() {
            if (null == cpBands) {
                cpBands = new MockCpBands(this);
            }
            return cpBands;
        }

        protected ClassBands getClassBands() {
            return new MockClassBands(this);
        }

        public SegmentConstantPool getConstantPool() {
            return cpBands.getConstantPool();
        }
    }

    BcBands bcBands = new BcBands(new MockSegment());

    /**
     * Test with single byte instructions that mean all other bands apart from
     * bc_codes will be empty.
     *
     * @throws IOException
     * @throws Pack200Exception
     */
    public void testSimple() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
                39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75,
                76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91,
                92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
                106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117,
                118, 119, 120, 121, 122, 123, 124, 125, 126, 127, (byte) 128,
                (byte) 129, (byte) 130, (byte) 131, (byte) 133, (byte) 134,
                (byte) 135, (byte) 136, (byte) 137, (byte) 138, (byte) 139,
                (byte) 140, (byte) 141, (byte) 142, (byte) 143, (byte) 144,
                (byte) 145, (byte) 146, (byte) 147, (byte) 148, (byte) 149,
                (byte) 150, (byte) 151, (byte) 172, (byte) 173, (byte) 174,
                (byte) 175, (byte) 176, (byte) 177, (byte) 190, (byte) 191,
                (byte) 194, (byte) 195, (byte) 255 };
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(bytes.length - 1,
                bcBands.getMethodByteCodePacked()[0][0].length);
    }

    /**
     * Test with multiple classes but single byte instructions
     *
     * @throws IOException
     * @throws Pack200Exception
     */
    public void testMultipleClassesSimple() throws IOException,
            Pack200Exception {
        numClasses = 2;
        numMethods = new int[] { 1, 1 };
        byte[] bytes = new byte[] { 50, 50, (byte) 255, 50, 50, (byte) 255 };
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);

        numClasses = 1;
        numMethods = new int[] { 1 };
    }

    /**
     * Test with multiple classes and multiple methods but single byte
     * instructions
     *
     * @throws IOException
     * @throws Pack200Exception
     */
    public void testMultipleMethodsSimple() throws IOException,
            Pack200Exception {
        numClasses = 2;
        numMethods = new int[] { 3, 1 };
        byte[] bytes = new byte[] { 50, 50, (byte) 255, 50, 50, (byte) 255, 50,
                50, (byte) 255, 50, 50, (byte) 255 };
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);

        numClasses = 1;
        numMethods = new int[] { 1 };
    }

    /**
     * Test with codes that require entries in the bc_case_count and
     * bc_case_value bands
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcCaseBands() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 170, (byte) 171, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 255, 2, 5, // bc_case_count
                0, 0, 0, 0, 0, 0, 0, // bc_case_value
                0, 0, 0, 0, 0, 0, 0, 0, 0 }; // bc_label
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(18, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_case_count = bcBands.getBcCaseCount();
        assertEquals(2, bc_case_count.length);
        assertEquals(2, bc_case_count[0]);
        assertEquals(5, bc_case_count[1]);
        int[] bc_case_value = bcBands.getBcCaseValue();
        assertEquals(0, bc_case_value[0]);
        assertEquals(0, bc_case_value[1]);
        assertEquals(9, bcBands.getBcLabel().length);
    }

    /**
     * Test with codes that should require entries in the bc_byte band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcByteBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { 16, (byte) 132, (byte) 188, (byte) 197,
                (byte) 255, 8, 8, 8, 8, // bc_byte band
                8, // bc_locals band (required by iinc (132))
                8 }; // bc_class band (required by multianewarray (197))
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(4, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_byte = bcBands.getBcByte();
        assertEquals(4, bc_byte.length);
        for (int i = 0; i < bc_byte.length; i++) {
            assertEquals(8, bc_byte[i]);
        }
        assertEquals(1, bcBands.getBcLocal().length);
        assertEquals(1, bcBands.getBcClassRef().length);
    }

    /**
     * Test with codes that should require entries in the bc_short band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcShortBand() throws IOException, Pack200Exception {
        // TODO: Need to fix this testcase so it has enough data to pass.
        byte[] bytes = new byte[] { 17, (byte) 196, (byte) 132, (byte) 255, 8,
                8,// bc_short band
                8 }; // bc_locals band (required by wide iinc (196, 132))
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(3, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(2, bcBands.getBcShort().length);
        assertEquals(1, bcBands.getBcLocal().length);
    }

    /**
     * Test with codes that should require entries in the bc_local band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcLocalBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { 21, 22, 23, 24, 25, 54, 55, 56, 57, 58,
                (byte) 169, (byte) 255, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 }; // bc_local
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(11, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(11, bcBands.getBcLocal().length);
    }

    /**
     * Test with codes that should require entries in the bc_label band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcLabelBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 159, (byte) 160, (byte) 161,
                (byte) 162, (byte) 163, (byte) 164, (byte) 165, (byte) 166,
                (byte) 167, (byte) 168, (byte) 170, (byte) 171, (byte) 198,
                (byte) 199, (byte) 200, (byte) 201, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 255, 2, 2, // bc_case_count
                // (required
                // by
                // tableswitch
                // (170) and
                // lookupswitch
                // (171))
                0, 0, 0, 0, // bc_case_value
                // Now that we're actually doing real label lookup, need valid
                // labels
                // 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 }; // bc_label
                // band
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // bc_label
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(36, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(20, bcBands.getBcLabel().length);
    }

    public void testWideForms() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 196, (byte) 54, // wide istore
                (byte) 196, (byte) 132, // wide iinc
                (byte) 255, 0, // bc_short band
                0, 1 }; // bc_locals band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(4, bcBands.getMethodByteCodePacked()[0][0].length);
        assertEquals(2, bcBands.getBcLocal().length);
        assertEquals(1, bcBands.getBcShort().length);
    }

    /**
     * Test with codes that should require entries in the bc_intref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcIntRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 234, (byte) 237, (byte) 255, 8, 8 }; // bc_intref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_intref = bcBands.getBcIntRef();
        assertEquals(2, bc_intref.length);
    }

    /**
     * Test with codes that should require entries in the bc_floatref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcFloatRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 235, (byte) 238, (byte) 255, 8, 8 }; // bc_floatref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_floatref = bcBands.getBcFloatRef();
        assertEquals(2, bc_floatref.length);
    }

    /**
     * Test with codes that should require entries in the bc_longref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcLongRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { 20, (byte) 255, 8 }; // bc_longref band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(1, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_longref = bcBands.getBcLongRef();
        assertEquals(1, bc_longref.length);
    }

    /**
     * Test with codes that should require entries in the bc_doubleref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcDoubleRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 239, (byte) 255, 8 }; // bc_doubleref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(1, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_doubleref = bcBands.getBcDoubleRef();
        assertEquals(1, bc_doubleref.length);
    }

    /**
     * Test with codes that should require entries in the bc_stringref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcStringRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { 18, 19, (byte) 255, 8, 8 }; // bc_stringref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_stringref = bcBands.getBcStringRef();
        assertEquals(2, bc_stringref.length);
    }

    /**
     * Test with codes that should require entries in the bc_classref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcClassRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 233, (byte) 236, (byte) 255, 8, 8 }; // bc_classref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(2, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_classref = bcBands.getBcClassRef();
        assertEquals(2, bc_classref.length);
    }

    /**
     * Test with codes that should require entries in the bc_fieldref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcFieldRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 178, (byte) 179, (byte) 180,
                (byte) 181, (byte) 255, 8, 8, 8, 8 }; // bc_fieldref band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(4, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_fieldref = bcBands.getBcFieldRef();
        assertEquals(4, bc_fieldref.length);
    }

    /**
     * Test with codes that should require entries in the bc_methodref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcMethodRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 182, (byte) 183, (byte) 184,
                (byte) 255, 8, 8, 8 }; // bc_methodref band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(3, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_methodref = bcBands.getBcMethodRef();
        assertEquals(3, bc_methodref.length);
    }

    /**
     * Test with codes that should require entries in the bc_imethodref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcIMethodRefBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 185, (byte) 255, 8 }; // bc_imethodref
        // band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(1, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_imethodref = bcBands.getBcIMethodRef();
        assertEquals(1, bc_imethodref.length);
    }

    /**
     * Test with codes that should require entries in the bc_thisfieldref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcThisFieldBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 202, (byte) 203, (byte) 204,
                (byte) 205, (byte) 209, (byte) 210, (byte) 211, (byte) 212,
                (byte) 255, 8, 8, 8, 8, 8, 8, 8, 8 }; // bc_thisfieldref band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(8, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_thisfield = bcBands.getBcThisField();
        assertEquals(8, bc_thisfield.length);
    }

    /**
     * Test with codes that should require entries in the bc_superfield band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcSuperFieldBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 216, (byte) 217, (byte) 218,
                (byte) 219, (byte) 223, (byte) 224, (byte) 225, (byte) 226,
                (byte) 255, 8, 8, 8, 8, 8, 8, 8, 8 }; // bc_superfield band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(8, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_superfield = bcBands.getBcSuperField();
        assertEquals(8, bc_superfield.length);
    }

    /**
     * Test with codes that should require entries in the bc_thismethod band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcThisMethodBand() throws IOException, Pack200Exception {
        byte[] bytes = new byte[] { (byte) 206, (byte) 207, (byte) 208,
                (byte) 213, (byte) 214, (byte) 215, (byte) 255, 8, 8, 8, 8, 8,
                8 }; // bc_thismethod band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(6, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_thismethod = bcBands.getBcThisMethod();
        assertEquals(6, bc_thismethod.length);
    }

    /**
     * Test with codes that should require entries in the bc_supermethod band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcSuperMethodBand() throws IOException, Pack200Exception {
        // TODO: Need to fix this testcase so it has enough data to pass.
        if (true)
            return;
        byte[] bytes = new byte[] { (byte) 220, (byte) 221, (byte) 222,
                (byte) 227, (byte) 228, (byte) 229, (byte) 255, 8, 8, 8, 8, 8,
                8 }; // bc_supermethod band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(6, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_supermethod = bcBands.getBcSuperMethod();
        assertEquals(6, bc_supermethod.length);
    }

    /**
     * Test with codes that should require entries in the bc_initrefref band
     *
     * @throws Pack200Exception
     * @throws IOException
     */
    public void testBcInitRefRefBand() throws IOException, Pack200Exception {
        // TODO: Need to fix this testcase so it has enough data to pass.
        if (true)
            return;
        byte[] bytes = new byte[] { (byte) 230, (byte) 231, (byte) 232,
                (byte) 255, 8, 8, 8 }; // bc_initrefref band
        InputStream in = new ByteArrayInputStream(bytes);
        bcBands.unpack(in);
        assertEquals(3, bcBands.getMethodByteCodePacked()[0][0].length);
        int[] bc_initrefref = bcBands.getBcInitRef();
        assertEquals(3, bc_initrefref.length);
    }

    public void testBcEscRefBands() throws IOException, Pack200Exception {
        // TODO
    }

    public void testBcEscBands() throws IOException, Pack200Exception {
        // TODO
    }

}
