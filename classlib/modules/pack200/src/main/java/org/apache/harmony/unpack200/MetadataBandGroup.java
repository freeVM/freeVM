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
package org.apache.harmony.unpack200;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.unpack200.bytecode.AnnotationDefaultAttribute;
import org.apache.harmony.unpack200.bytecode.AnnotationsAttribute.Annotation;
import org.apache.harmony.unpack200.bytecode.AnnotationsAttribute.ElementValue;
import org.apache.harmony.unpack200.bytecode.Attribute;
import org.apache.harmony.unpack200.bytecode.CPDouble;
import org.apache.harmony.unpack200.bytecode.CPFloat;
import org.apache.harmony.unpack200.bytecode.CPInteger;
import org.apache.harmony.unpack200.bytecode.CPLong;
import org.apache.harmony.unpack200.bytecode.CPUTF8;
import org.apache.harmony.unpack200.bytecode.RuntimeVisibleorInvisibleAnnotationsAttribute;
import org.apache.harmony.unpack200.bytecode.RuntimeVisibleorInvisibleParameterAnnotationsAttribute;
import org.apache.harmony.unpack200.bytecode.RuntimeVisibleorInvisibleParameterAnnotationsAttribute.ParameterAnnotation;

/**
 * A group of metadata bands, such as class_RVA_bands, method_AD_bands etc.
 */
public class MetadataBandGroup {

    private final String type;
    private final CpBands cpBands;

    private static CPUTF8 rvaUTF8;
    private static CPUTF8 riaUTF8;
    private static CPUTF8 rvpaUTF8;
    private static CPUTF8 ripaUTF8;

    public static void setRvaAttributeName(CPUTF8 cpUTF8Value) {
        rvaUTF8 = cpUTF8Value;
    }

    public static void setRiaAttributeName(CPUTF8 cpUTF8Value) {
        riaUTF8 = cpUTF8Value;
    }

    public static void setRvpaAttributeName(CPUTF8 cpUTF8Value) {
        rvpaUTF8 = cpUTF8Value;
    }

    public static void setRipaAttributeName(CPUTF8 cpUTF8Value) {
        ripaUTF8 = cpUTF8Value;
    }

    public MetadataBandGroup(String type, CpBands cpBands) {
        this.type = type;
        this.cpBands = cpBands;
    }

    private List attributes;

    public int[] param_NB;
    public int[] anno_N;
    public CPUTF8[][] type_RS;
    public int[][] pair_N;
    public CPUTF8[] name_RU;
    public int[] T;
    public CPInteger[] caseI_KI;
    public CPDouble[] caseD_KD;
    public CPFloat[] caseF_KF;
    public CPLong[] caseJ_KJ;
    public CPUTF8[] casec_RS;
    public String[] caseet_RS;
    public String[] caseec_RU;
    public CPUTF8[] cases_RU;
    public int[] casearray_N;
    public CPUTF8[] nesttype_RS;
    public int[] nestpair_N;
    public CPUTF8[] nestname_RU;

    private int caseI_KI_Index;

    private int caseD_KD_Index;

    private int caseF_KF_Index;

    private int caseJ_KJ_Index;

    private int casec_RS_Index;

    private int caseet_RS_Index;

    private int caseec_RU_Index;

    private int cases_RU_Index;

    private int casearray_N_Index;

    private int T_index;

    private int nesttype_RS_Index;

    private int nestpair_N_Index;

    private Iterator nestname_RU_Iterator;

    private int anno_N_Index;

    private int pair_N_Index;

    public List getAttributes() {
        // TODO: Optimize iterators!
        if (attributes == null) {
            attributes = new ArrayList();
            if (name_RU != null) {
                Iterator name_RU_Iterator = Arrays.asList(name_RU).iterator();
                if (!type.equals("AD")) {
                    T_index = 0;
                }
                caseI_KI_Index = 0;
                caseD_KD_Index = 0;
                caseF_KF_Index = 0;
                caseJ_KJ_Index = 0;
                casec_RS_Index = 0;
                caseet_RS_Index = 0;
                caseec_RU_Index = 0;
                cases_RU_Index = 0;
                casearray_N_Index = 0;
                nesttype_RS_Index = 0;
                nestpair_N_Index = 0;
                nestname_RU_Iterator = Arrays.asList(nestname_RU).iterator();
                if (type.equals("RVA") || type.equals("RIA")) {
                    for (int i = 0; i < anno_N.length; i++) {
                        attributes.add(getAttribute(anno_N[i], type_RS[i],
                                pair_N[i], name_RU_Iterator));
                    }
                } else if (type.equals("RVPA") || type.equals("RIPA")) {
                    anno_N_Index = 0;
                    pair_N_Index = 0;
                    for (int i = 0; i < param_NB.length; i++) {
                        attributes.add(getParameterAttribute(param_NB[i],
                                name_RU_Iterator));
                    }
                } else { // type.equals("AD")
                    for (int i = 0; i < T.length; i++) {
                        attributes.add(new AnnotationDefaultAttribute(
                                new ElementValue(T[i], getNextValue(T[i]))));
                    }
                }
            }
        }
        return attributes;
    }

    private Attribute getAttribute(int numAnnotations, CPUTF8[] types,
            int[] pairCounts, Iterator namesIterator) {
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = getAnnotation(types[i], pairCounts[i],
                    namesIterator);
        }
        return new RuntimeVisibleorInvisibleAnnotationsAttribute(type
                .equals("RVA") ? rvaUTF8 : riaUTF8, annotations);
    }

    private Attribute getParameterAttribute(int numParameters,
            Iterator namesIterator) {
        ParameterAnnotation[] parameter_annotations = new ParameterAnnotation[numParameters];
        for (int i = 0; i < numParameters; i++) {
            int numAnnotations = anno_N[anno_N_Index++];
            int[] pairCounts = pair_N[pair_N_Index++];
            Annotation[] annotations = new Annotation[numAnnotations];
            for (int j = 0; j < annotations.length; j++) {
                annotations[j] = getAnnotation(type_RS[anno_N_Index - 1][j],
                        pairCounts[j], namesIterator);
            }
            parameter_annotations[i] = new ParameterAnnotation(annotations);
        }
        return new RuntimeVisibleorInvisibleParameterAnnotationsAttribute(
                type.equals("RVPA") ? rvpaUTF8 : ripaUTF8,
                parameter_annotations);
    }

    private Annotation getAnnotation(CPUTF8 type, int pairCount,
            Iterator namesIterator) {
        CPUTF8[] elementNames = new CPUTF8[pairCount];
        ElementValue[] elementValues = new ElementValue[pairCount];
        for (int j = 0; j < elementNames.length; j++) {
            elementNames[j] = (CPUTF8) namesIterator.next();
            int t = T[T_index++];
            elementValues[j] = new ElementValue(t, getNextValue(t));
        }
        return new Annotation(pairCount, type, elementNames, elementValues);
    }

    private Object getNextValue(int t) {
        switch (t) {
        case 'B':
        case 'C':
        case 'I':
        case 'S':
        case 'Z':
            return caseI_KI[caseI_KI_Index++];
        case 'D':
            return caseD_KD[caseD_KD_Index++];
        case 'F':
            return caseF_KF[caseF_KF_Index++];
        case 'J':
            return caseJ_KJ[caseJ_KJ_Index++];
        case 'c':
            return casec_RS[casec_RS_Index++];
        case 'e':
            // TODO: check this - it may not work if the first string already
            // has a colon in it
            String enumString = caseet_RS[caseet_RS_Index++] + ":"
                    + caseec_RU[caseec_RU_Index++];
            return cpBands.cpNameAndTypeValue(enumString);
        case 's':
            return cases_RU[cases_RU_Index++];
        case '[':
            int arraySize = casearray_N[casearray_N_Index++];
            ElementValue[] nestedArray = new ElementValue[arraySize];
            for (int i = 0; i < arraySize; i++) {
                int nextT = T[T_index++];
                nestedArray[i] = new ElementValue(nextT, getNextValue(nextT));
            }
            return nestedArray;
        case '@':
            CPUTF8 type = nesttype_RS[nesttype_RS_Index++];
            int numPairs = nestpair_N[nestpair_N_Index++];

            return getAnnotation(type, numPairs, nestname_RU_Iterator);
        }
        return null;
    }

}