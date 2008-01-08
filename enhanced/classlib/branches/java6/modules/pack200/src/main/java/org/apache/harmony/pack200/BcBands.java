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
package org.apache.harmony.pack200;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.pack200.bytecode.Attribute;
import org.apache.harmony.pack200.bytecode.BCIRenumberedAttribute;
import org.apache.harmony.pack200.bytecode.ByteCode;
import org.apache.harmony.pack200.bytecode.CodeAttribute;
import org.apache.harmony.pack200.bytecode.OperandManager;

/**
 * Pack200 Bytecode bands
 */
public class BcBands extends BandSet {

    // The bytecodes for each method in each class as they come (i.e. in their packed format)
    private byte[][][] methodByteCodePacked;
    
    // The bands
    // TODO:  Haven't resolved references yet.  Do we want to?
    private int[] bcCaseCount;
    private int[] bcCaseValue;
    private int[] bcByte;
    private int[] bcLocal;
    private int[] bcShort;
    private int[] bcLabel;
    private int[] bcIntRef;
    private int[] bcFloatRef;
    private int[] bcLongRef;
    private int[] bcDoubleRef;
    private int[] bcStringRef;
    private int[] bcClassRef;
    private int[] bcFieldRef;
    private int[] bcMethodRef;
    private int[] bcIMethodRef;
    private int[] bcThisField;
    private int[] bcSuperField;
    private int[] bcThisMethod;
    private int[] bcSuperMethod;
    private int[] bcInitRef;
    private int[] bcEscRef;
    private int[] bcEscRefSize;
    private int[] bcEscSize;
    private int[][] bcEscByte;

    /**
     * @param header
     */
    public BcBands(Segment segment) {
        super(segment);
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.BandSet#unpack(java.io.InputStream)
     */
    public void unpack(InputStream in) throws IOException,
            Pack200Exception {
        
       AttributeLayoutMap attributeDefinitionMap = segment.getAttrDefinitionBands().getAttributeDefinitionMap();
       int classCount = header.getClassCount();
       long[][] methodFlags = segment.getClassBands().getMethodFlags();
       int[] codeMaxNALocals = segment.getClassBands().getCodeMaxNALocals();
       int[] codeMaxStack = segment.getClassBands().getCodeMaxStack();
       ArrayList[][] methodAttributes = segment.getClassBands().getMethodAttributes();
       String[][] methodDescr = segment.getClassBands().getMethodDescr();
       
       int bcCaseCountCount = 0;
       int bcByteCount = 0;
       int bcShortCount = 0;
       int bcLocalCount = 0;
       int bcLabelCount = 0;
       int bcIntRefCount = 0;
       int bcFloatRefCount = 0;
       int bcLongRefCount = 0;
       int bcDoubleRefCount = 0;
       int bcStringRefCount = 0;
       int bcClassRefCount = 0;       
       int bcFieldRefCount = 0;
       int bcMethodRefCount = 0;
       int bcIMethodRefCount = 0;
       int bcThisFieldCount = 0;
       int bcSuperFieldCount = 0;
       int bcThisMethodCount = 0;
       int bcSuperMethodCount = 0;       
       int bcInitRefCount = 0;
       int bcEscCount = 0;
       int bcEscRefCount = 0;

       AttributeLayout abstractModifier = attributeDefinitionMap
               .getAttributeLayout(AttributeLayout.ACC_ABSTRACT,
                       AttributeLayout.CONTEXT_METHOD);
       AttributeLayout nativeModifier = attributeDefinitionMap
               .getAttributeLayout(AttributeLayout.ACC_NATIVE,
                       AttributeLayout.CONTEXT_METHOD);
       AttributeLayout staticModifier = attributeDefinitionMap
               .getAttributeLayout(AttributeLayout.ACC_STATIC,
                       AttributeLayout.CONTEXT_METHOD);
       methodByteCodePacked = new byte[classCount][][];
       int bcParsed = 0;
       
       List switchIsTableSwitch = new ArrayList();
       List wideByteCodes = new ArrayList();
       for (int c = 0; c < classCount; c++) {
           int numberOfMethods = methodFlags[c].length;
           methodByteCodePacked[c] = new byte[numberOfMethods][];
           for (int m = 0; m < numberOfMethods; m++) {
               long methodFlag = methodFlags[c][m];
               if (!abstractModifier.matches(methodFlag)
                       && !nativeModifier.matches(methodFlag)) {
                   ByteArrayOutputStream codeBytes = new ByteArrayOutputStream();
                   byte code;
                   while ((code = (byte) (0xff & in.read())) != -1)
                       codeBytes.write(code);
                   methodByteCodePacked[c][m] = codeBytes.toByteArray();
                   bcParsed += methodByteCodePacked[c][m].length;
                   int[] codes = new int[methodByteCodePacked[c][m].length];
                   for (int i = 0; i < codes.length; i++) {
                       codes[i] = methodByteCodePacked[c][m][i] & 0xff;
                   }
                   for (int i = 0; i < methodByteCodePacked[c][m].length; i++) {
                       int codePacked = 0xff & methodByteCodePacked[c][m][i];
                       // TODO a lot of this needs to be encapsulated in the
                       // place that
                       // calculates what the arguments are, since (a) it will
                       // need
                       // to know where to get them, and (b) what to do with
                       // them
                       // once they've been gotten. But that's for another
                       // time.
                       switch (codePacked) {
                       case 16: // bipush
                       case 188: // newarray
                           bcByteCount++;
                           break;
                       case 17: // sipush
                           bcShortCount++;
                           break;
                       case 18: // (a)ldc
                       case 19: // aldc_w
                           bcStringRefCount++;
                           break;
                       case 234: // ildc
                       case 237: // ildc_w
                           bcIntRefCount++;
                           break;
                       case 235: // fldc
                       case 238: // fldc_w
                           bcFloatRefCount++;
                           break;
                       case 197: // multianewarray
                           bcByteCount++;
                           // fallthrough intended
                       case 233: // cldc
                       case 236: // cldc_w
                       case 187: // new
                       case 189: // anewarray
                       case 192: // checkcast
                       case 193: // instanceof
                           bcClassRefCount++;
                           break;
                       case 20: // lldc2_w
                           bcLongRefCount++;
                           break;
                       case 239: // dldc2_w
                           bcDoubleRefCount++;
                           break;
                       case 169: // ret
                           bcLocalCount++;
                           break;
                       case 167: // goto
                       case 168: // jsr
                       case 200: // goto_w
                       case 201: // jsr_w
                           bcLabelCount++;
                           break;
                       case 170: // tableswitch
                           switchIsTableSwitch.add(new Boolean(true));
                           bcCaseCountCount++;
                           bcLabelCount++;
                           break;
                       case 171: // lookupswitch
                           switchIsTableSwitch.add(new Boolean(false));
                           bcCaseCountCount++;
                           bcLabelCount++;
                           break;
                       case 178: // getstatic
                       case 179: // putstatic
                       case 180: // getfield
                       case 181: // putfield
                           bcFieldRefCount++;
                           break;
                       case 182: // invokevirtual
                       case 183: // invokespecial
                       case 184: // invokestatic
                           bcMethodRefCount++;
                           break;
                       case 185: // invokeinterface
                           bcIMethodRefCount++;
                           break;
                       case 202: // getstatic_this
                       case 203: // putstatic_this
                       case 204: // getfield_this
                       case 205: // putfield_this
                       case 209: // aload_0_getstatic_this
                       case 210: // aload_0_putstatic_this
                       case 211: // aload_0_putfield_this
                       case 212: // aload_0_putfield_this
                           bcThisFieldCount++;
                           break;
                       case 206: // invokevirtual_this
                       case 207: // invokespecial_this
                       case 208: // invokestatic_this
                       case 213: // aload_0_invokevirtual_this
                       case 214: // aload_0_invokespecial_this
                       case 215: // aload_0_invokestatic_this
                           bcThisMethodCount++;
                           break;
                       case 216: // getstatic_super
                       case 217: // putstatic_super
                       case 218: // getfield_super
                       case 219: // putfield_super
                       case 223: // aload_0_getstatic_super
                       case 224: // aload_0_putstatic_super
                       case 225: // aload_0_getfield_super
                       case 226: // aload_0_putfield_super
                           bcSuperFieldCount++;
                           break;
                       case 220: // invokevirtual_super
                       case 221: // invokespecial_super
                       case 222: // invokestatic_super
                       case 227: // aload_0_invokevirtual_super
                       case 228: // aload_0_invokespecial_super
                       case 229: // aload_0_invokestatic_super
                           bcSuperMethodCount++;
                           break;
                       case 132: // iinc
                           bcLocalCount++;
                           bcByteCount++;
                           break;
                       case 196: // wide
                            int nextInstruction = 0xff & methodByteCodePacked[c][m][i+1];
                            wideByteCodes.add(new Integer(nextInstruction));
                            if (nextInstruction == 132) { // iinc
                                bcLocalCount += 2;
                                bcShortCount++;
                            } else if (endsWithLoad(nextInstruction)
                                    || endsWithStore(nextInstruction)
                                    || nextInstruction == 169) {
                                bcLocalCount += 2;
                            } else {
                                debug("Found unhandled " + ByteCode.getByteCode(nextInstruction));
                            }
                            i++;
                            break;
                       case 230: // invokespecial_this_init
                       case 231: // invokespecial_super_init
                       case 232: // invokespecial_new_init
                           bcInitRefCount++;
                           break;
                       case 253: // ref_escape
                            bcEscRefCount++;
                           break;
                       case 254: // byte_escape
                           bcEscCount++;
                           break;
                       default: // unhandled specifically at this stage
                           if(endsWithLoad(codePacked) || endsWithStore(codePacked)) {
                               bcLocalCount++;
                           } else if (startsWithIf(codePacked)) {
                               bcLabelCount++;
                           } else {
                               debug("Found unhandled " + codePacked + " " + ByteCode.getByteCode(codePacked));
                           }
                       }
                   }
               }
            }
        }
        // other bytecode bands
        debug("Parsed *bc_codes (" + bcParsed + ")");
        bcCaseCount = decodeBandInt("bc_case_count", in, Codec.UNSIGNED5, bcCaseCountCount);
        int bcCaseValueCount = 0;
        for (int i = 0; i < bcCaseCount.length; i++) {
            boolean isTableSwitch = ((Boolean)switchIsTableSwitch.get(i)).booleanValue();
            if(isTableSwitch) {
                bcCaseValueCount += 1;
            } else {
                bcCaseValueCount += bcCaseCount[i];
            }
        }
        bcCaseValue = decodeBandInt("bc_case_value", in, Codec.DELTA5, bcCaseValueCount );
        // Every case value needs a label. We weren't able to count these
        // above, because we didn't know how many cases there were.
        // Have to correct it now.
        for(int index=0; index < bcCaseCountCount; index++) {
            bcLabelCount += bcCaseCount[index];
        }
        bcByte = decodeBandInt("bc_byte", in, Codec.BYTE1, bcByteCount);
        bcShort = decodeBandInt("bc_short", in, Codec.DELTA5, bcShortCount);
        bcLocal = decodeBandInt("bc_local", in, Codec.UNSIGNED5, bcLocalCount);
        bcLabel = decodeBandInt("bc_label", in, Codec.BRANCH5, bcLabelCount);
        bcIntRef = decodeBandInt("bc_intref", in, Codec.DELTA5, bcIntRefCount);
        bcFloatRef = decodeBandInt("bc_floatref", in, Codec.DELTA5,
                bcFloatRefCount);
        bcLongRef = decodeBandInt("bc_longref", in, Codec.DELTA5,
                bcLongRefCount);
        bcDoubleRef = decodeBandInt("bc_doubleref", in, Codec.DELTA5,
                bcDoubleRefCount);
        bcStringRef = decodeBandInt("bc_stringref", in, Codec.DELTA5,
                bcStringRefCount);
        bcClassRef = decodeBandInt("bc_classref", in, Codec.UNSIGNED5,
                bcClassRefCount);
        bcFieldRef = decodeBandInt("bc_fieldref", in, Codec.DELTA5,
                bcFieldRefCount);
        bcMethodRef = decodeBandInt("bc_methodref", in, Codec.UNSIGNED5,
                bcMethodRefCount);
        bcIMethodRef = decodeBandInt("bc_imethodref", in, Codec.DELTA5,
                bcIMethodRefCount);
        bcThisField = decodeBandInt("bc_thisfield", in, Codec.UNSIGNED5,
                bcThisFieldCount);
        bcSuperField = decodeBandInt("bc_superfield", in, Codec.UNSIGNED5,
                bcSuperFieldCount);
        bcThisMethod = decodeBandInt("bc_thismethod", in, Codec.UNSIGNED5,
                bcThisMethodCount);
        bcSuperMethod = decodeBandInt("bc_supermethod", in, Codec.UNSIGNED5,
                bcSuperMethodCount);
        bcInitRef = decodeBandInt("bc_initref", in, Codec.UNSIGNED5,
                bcInitRefCount);
        bcEscRef = decodeBandInt("bc_escref", in, Codec.UNSIGNED5,
                bcEscRefCount);
        bcEscRefSize = decodeBandInt("bc_escrefsize", in, Codec.UNSIGNED5, bcEscRefCount);
        bcEscSize = decodeBandInt("bc_escsize", in, Codec.UNSIGNED5, bcEscCount);
        bcEscByte = decodeBandInt("bc_escbyte", in, Codec.BYTE1, bcEscSize);

        int[] wideByteCodeArray = new int[wideByteCodes.size()];
        for(int index=0; index < wideByteCodeArray.length; index++) {
            wideByteCodeArray[index] = ((Integer)wideByteCodes.get(index)).intValue();
        }
        OperandManager operandManager = new OperandManager(bcCaseCount, bcCaseValue,
                bcByte, bcShort, bcLocal, bcLabel, bcIntRef, bcFloatRef, bcLongRef,
                bcDoubleRef, bcStringRef, bcClassRef, bcFieldRef, bcMethodRef,
                bcIMethodRef, bcThisField, bcSuperField, bcThisMethod, bcSuperMethod,
                bcInitRef, wideByteCodeArray);
        operandManager.setSegment(segment);

        int i = 0;
        ArrayList orderedCodeAttributes = segment.getClassBands().getOrderedCodeAttributes();
        for (int c = 0; c < classCount; c++) {
           int numberOfMethods = methodFlags[c].length;
           for (int m = 0; m < numberOfMethods; m++) {
               long methodFlag = methodFlags[c][m];
               if (!abstractModifier.matches(methodFlag)
                       && !nativeModifier.matches(methodFlag)) {
                   int maxStack = codeMaxStack[i];
                   int maxLocal = codeMaxNALocals[i];
                   if (!staticModifier.matches(methodFlag))
                       maxLocal++; // one for 'this' parameter
                   maxLocal += SegmentUtils.countArgs(methodDescr[c][m]);
                   operandManager.setCurrentClass(segment.getClassBands().getClassThis()[c]);
                   operandManager.setSuperClass(segment.getClassBands().getClassSuper()[c]);
                   CodeAttribute codeAttr = new CodeAttribute(maxStack, maxLocal,
                           methodByteCodePacked[c][m], segment, operandManager);
                   methodAttributes[c][m].add(codeAttr);
                   // Should I add all the attributes in here?
                 ArrayList currentAttributes = (ArrayList)orderedCodeAttributes.get(i);
                 for(int index=0;index < currentAttributes.size(); index++) {
                     Attribute currentAttribute = (Attribute)currentAttributes.get(index);
                     codeAttr.addAttribute(currentAttribute);
                     // Fix up the line numbers if needed
                     if(currentAttribute.hasBCIRenumbering()) {
                         ((BCIRenumberedAttribute)currentAttribute).renumber(codeAttr.byteCodeOffsets);
                     }
                 }
                 i++;
               }
           }
       }
    }
    
    private boolean startsWithIf(int codePacked) {
        return (codePacked >= 153 && codePacked <= 166)
        || (codePacked == 198)
        || (codePacked == 199);
    }

    private boolean endsWithLoad(int codePacked) {
        return (codePacked >= 21 && codePacked <= 25);
    }

    private boolean endsWithStore(int codePacked) {
        return (codePacked >= 54 && codePacked <= 58);
    }

    public byte[][][] getMethodByteCodePacked() {
        return methodByteCodePacked;
    }

    public int[] getBcCaseCount() {
        return bcCaseCount;
    }

    public int[] getBcCaseValue() {
        return bcCaseValue;
    }

    public int[] getBcByte() {
        return bcByte;
    }

    public int[] getBcClassRef() {
        return bcClassRef;
    }

    public int[] getBcDoubleRef() {
        return bcDoubleRef;
    }

    public int[] getBcFieldRef() {
        return bcFieldRef;
    }

    public int[] getBcFloatRef() {
        return bcFloatRef;
    }

    public int[] getBcIMethodRef() {
        return bcIMethodRef;
    }

    public int[] getBcInitRef() {
        return bcInitRef;
    }

    public int[] getBcIntRef() {
        return bcIntRef;
    }

    public int[] getBcLabel() {
        return bcLabel;
    }

    public int[] getBcLocal() {
        return bcLocal;
    }

    public int[] getBcLongRef() {
        return bcLongRef;
    }

    public int[] getBcMethodRef() {
        return bcMethodRef;
    }

    public int[] getBcShort() {
        return bcShort;
    }

    public int[] getBcStringRef() {
        return bcStringRef;
    }

    public int[] getBcSuperField() {
        return bcSuperField;
    }

    public int[] getBcSuperMethod() {
        return bcSuperMethod;
    }

    public int[] getBcThisField() {
        return bcThisField;
    }

    public int[] getBcThisMethod() {
        return bcThisMethod;
    }

    public int[] getBcEscRef() {
        return bcEscRef;
    }

    public int[] getBcEscRefSize() {
        return bcEscRefSize;
    }

    public int[] getBcEscSize() {
        return bcEscSize;
    }

    public int[][] getBcEscByte() {
        return bcEscByte;
    }


}
