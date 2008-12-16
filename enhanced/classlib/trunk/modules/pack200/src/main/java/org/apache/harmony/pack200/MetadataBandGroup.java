/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A group of metadata (annotation) bands, such as class_RVA_bands,
 * method_AD_bands etc.
 */
public class MetadataBandGroup extends BandSet {

    public static final int CONTEXT_CLASS = 0;
    public static final int CONTEXT_FIELD = 1;
    public static final int CONTEXT_METHOD = 2;

    private final String type;
    private int numBackwardsCalls = 0;

    public List param_NB = new ArrayList(); // TODO: Lazy instantiation?
    public List anno_N = new ArrayList();
    public List type_RS = new ArrayList();
    public List pair_N = new ArrayList();
    public List name_RU = new ArrayList();
    public List T = new ArrayList();
    public List caseI_KI = new ArrayList();
    public List caseD_KD = new ArrayList();
    public List caseF_KF = new ArrayList();
    public List caseJ_KJ = new ArrayList();
    public List casec_RS = new ArrayList();
    public List caseet_RS = new ArrayList();
    public List caseec_RU = new ArrayList();
    public List cases_RU = new ArrayList();
    public List casearray_N = new ArrayList();
    public List nesttype_RS = new ArrayList();
    public List nestpair_N = new ArrayList();
    public List nestname_RU = new ArrayList();

    private final CpBands cpBands;
    private final int context;

    /**
     * Constructs a new MetadataBandGroup
     * @param type - must be either AD, RVA, RIA, RVPA or RIPA.
     */
    public MetadataBandGroup(String type, int context, CpBands cpBands) {
        this.type = type;
        this.cpBands = cpBands;
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.harmony.pack200.BandSet#pack(java.io.OutputStream)
     */
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        if(hasContent()) {
            String contextStr;
            if(context == CONTEXT_CLASS) {
                contextStr = "Class";
            } else if (context == CONTEXT_FIELD) {
                contextStr = "Field";
            } else {
                contextStr = "Method";
            }
            if(!type.equals("AD")) {
                out.write(encodeBandInt(contextStr + "_" + type + " anno_N",  listToArray(anno_N), Codec.UNSIGNED5));
                out.write(encodeBandInt(contextStr + "_" + type + " type_RS",  cpEntryListToArray(type_RS), Codec.UNSIGNED5));
                out.write(encodeBandInt(contextStr + "_" + type + " pair_N",  listToArray(pair_N), Codec.UNSIGNED5));
                out.write(encodeBandInt(contextStr + "_" + type + " name_RU",  cpEntryListToArray(name_RU), Codec.UNSIGNED5));
            }
            out.write(encodeBandInt(contextStr + "_" + type + " T",  tagListToArray(T), Codec.BYTE1));
            out.write(encodeBandInt(contextStr + "_" + type + " caseI_KI",  cpEntryListToArray(caseI_KI), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " caseD_KD",  cpEntryListToArray(caseD_KD), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " caseF_KF",  cpEntryListToArray(caseF_KF), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " caseJ_KJ",  cpEntryListToArray(caseJ_KJ), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " casec_RS",  cpEntryListToArray(casec_RS), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " caseet_RS",  cpEntryListToArray(caseet_RS), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " caseec_RU",  cpEntryListToArray(caseec_RU), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " cases_RU",  cpEntryListToArray(cases_RU), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " casearray_N",  listToArray(casearray_N), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " nesttype_RS",  cpEntryListToArray(nesttype_RS), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " nestpair_N",  listToArray(nestpair_N), Codec.UNSIGNED5));
            out.write(encodeBandInt(contextStr + "_" + type + " nestname_RU",  cpEntryListToArray(nestname_RU), Codec.UNSIGNED5));
        }
    }

    private int[] tagListToArray(List t2) {
        int[] ints = new int[t2.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ((String)t2.get(i)).charAt(0);
        }
        return ints;
    }

    public void addAnnotation(String desc, List nameRU, List t, List values, List caseArrayN, List nestTypeRS, List nestNameRU, List nestPairN) {
        type_RS.add(cpBands.getCPSignature(desc));
        pair_N.add(new Integer(t.size()));

        for (Iterator iterator = nameRU.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            name_RU.add(cpBands.getCPUtf8(name));
        }

        Iterator valuesIterator = values.iterator();
        for (Iterator iterator = t.iterator(); iterator.hasNext();) {
            String tag = (String) iterator.next();
            T.add(tag);
            if (tag.equals("B") || tag.equals("C") || tag.equals("I")
                    || tag.equals("S") || tag.equals("Z")) {
                Integer value = (Integer)valuesIterator.next();
                caseI_KI.add(cpBands.getConstant(value));
            } else if (tag.equals("D")) {
                Double value = (Double)valuesIterator.next();
                caseD_KD.add(cpBands.getConstant(value));
            } else if (tag.equals("F")) {
                Float value = (Float)valuesIterator.next();
                caseF_KF.add(cpBands.getConstant(value));
            } else if (tag.equals("J")) {
                Long value = (Long)valuesIterator.next();
                caseJ_KJ.add(cpBands.getConstant(value));
            } else if (tag.equals("C")) {
                String value = (String)valuesIterator.next();
                casec_RS.add(cpBands.getCPSignature(value));
            } else if (tag.equals("e")) {
                String value = (String)valuesIterator.next();
                String value2 = (String)valuesIterator.next();
                caseet_RS.add(cpBands.getCPSignature(value));
                caseec_RU.add(cpBands.getCPUtf8(value2));
            } else if (tag.equals("s")) {
                String value = (String)valuesIterator.next();
                cases_RU.add(cpBands.getCPUtf8(value));
            }
            // do nothing here for [ or @ (handled below)
        }
        for (Iterator iterator = caseArrayN.iterator(); iterator.hasNext();) {
            Integer arraySize = (Integer) iterator.next();
            casearray_N.add(arraySize);
            numBackwardsCalls += arraySize.intValue();
        }
        for (Iterator iterator = nesttype_RS.iterator(); iterator.hasNext();) {
            String type = (String) iterator.next();
            nesttype_RS.add(cpBands.getCPSignature(type));
        }
        for (Iterator iterator = nestname_RU.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            nestname_RU.add(cpBands.getCPUtf8(name));
        }
        for (Iterator iterator = nestPairN.iterator(); iterator.hasNext();) {
            Integer numPairs = (Integer) iterator.next();
            nestPairN.add(numPairs);
            numBackwardsCalls += numPairs.intValue();
        }
    }

    public boolean hasContent() {
        return T.size() > 0;
    }

    public int numBackwardsCalls() {
        return numBackwardsCalls;
    }

    public void incrementAnnoN() {
        Integer latest = (Integer)anno_N.remove(anno_N.size() -1);
        anno_N.add(new Integer(latest.intValue() + 1));
    }

    public void newEntryInAnnoN() {
        anno_N.add(new Integer(1));
    }

}
