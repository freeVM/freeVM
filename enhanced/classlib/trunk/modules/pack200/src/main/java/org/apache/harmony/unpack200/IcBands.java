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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.harmony.pack200.Codec;
import org.apache.harmony.pack200.Pack200Exception;
import org.apache.harmony.unpack200.bytecode.CPClass;
import org.apache.harmony.unpack200.bytecode.ClassConstantPool;

/**
 * Inner Class Bands
 */
public class IcBands extends BandSet {

    private IcTuple[] icAll;

    private final String[] cpUTF8;

    private final String[] cpClass;

    /**
     * @param segment
     */
    public IcBands(Segment segment) {
        super(segment);
        this.cpClass = segment.getCpBands().getCpClass();
        this.cpUTF8 = segment.getCpBands().getCpUTF8();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.harmony.unpack200.BandSet#unpack(java.io.InputStream)
     */
    public void unpack(InputStream in) throws IOException, Pack200Exception {
        // Read IC bands
        int innerClassCount = header.getInnerClassCount();
        int[] icThisClassInts = decodeBandInt("ic_this_class", in,
                Codec.UDELTA5, innerClassCount);
        String[] icThisClass = getReferences(icThisClassInts, cpClass);
        int[] icFlags = decodeBandInt("ic_flags", in, Codec.UNSIGNED5,
                innerClassCount);
        int outerClasses = SegmentUtils.countBit16(icFlags);
        int[] icOuterClassInts = decodeBandInt("ic_outer_class", in,
                Codec.DELTA5, outerClasses);
        String[] icOuterClass = new String[outerClasses];
        for (int i = 0; i < icOuterClass.length; i++) {
            if (icOuterClassInts[i] == 0) {
                icOuterClass[i] = null;
            } else {
                icOuterClass[i] = cpClass[icOuterClassInts[i] - 1];
            }
        }
        int[] icNameInts = decodeBandInt("ic_name", in, Codec.DELTA5,
                outerClasses);
        String[] icName = new String[outerClasses];
        for (int i = 0; i < icName.length; i++) {
            if (icNameInts[i] == 0) {
                icName[i] = null;
            } else {
                icName[i] = cpUTF8[icNameInts[i] - 1];
            }
        }

        // Construct IC tuples
        icAll = new IcTuple[icThisClass.length];
        int index = 0;
        for (int i = 0; i < icThisClass.length; i++) {
            String icTupleC = icThisClass[i];
            int icTupleF = icFlags[i];
            String icTupleC2 = null;
            String icTupleN = null;
            int cIndex = icThisClassInts[i];
            int c2Index = -1;
            int nIndex = -1;
            if ((icFlags[i] & 1 << 16) != 0) {
                icTupleC2 = icOuterClass[index];
                icTupleN = icName[index];
                c2Index = icOuterClassInts[index] - 1;
                nIndex = icNameInts[index] - 1;
                index++;
            }
            icAll[i] = new IcTuple(icTupleC, icTupleF, icTupleC2, icTupleN, cIndex, c2Index, nIndex);
        }
    }

    public IcTuple[] getIcTuples() {
        return icAll;
    }

    /**
     * Answer the relevant IcTuples for the specified className and class
     * constant pool.
     *
     * @param className
     *            String name of the class X for ic_relevant(X)
     * @param cp
     *            ClassConstantPool used to generate ic_relevant(X)
     * @return array of IcTuple
     */
    public IcTuple[] getRelevantIcTuples(String className, ClassConstantPool cp) {
        Set relevantTuplesContains = new HashSet();
        List relevantTuples = new ArrayList();
        IcTuple[] allTuples = getIcTuples();
        int allTuplesSize = allTuples.length;
        for (int index = 0; index < allTuplesSize; index++) {
            if (allTuples[index].shouldAddToRelevantForClassName(className)) {
                relevantTuplesContains.add(allTuples[index]);
                relevantTuples.add(allTuples[index]);
            }
        }

        List classPoolClasses = cp.allClasses();
        boolean changed = true;
        // For every class constant in both ic_this_class and cp,
        // add it to ic_relevant. Repeat until no more
        // changes to ic_relevant.

        while (changed) {
            changed = false;
            for (int allTupleIndex = 0; allTupleIndex < allTuplesSize; allTupleIndex++) {
                for(int cpcIndex = 0; cpcIndex < classPoolClasses.size(); cpcIndex++) {
                    CPClass classInPool = (CPClass) classPoolClasses.get(cpcIndex);
                    String poolClassName = classInPool.name;
                    if (poolClassName.equals(allTuples[allTupleIndex]
                            .thisClassString())) {
                        // If the tuple isn't already in there, then add it
                        if (relevantTuplesContains.add(allTuples[allTupleIndex])) {
                            relevantTuples.add(allTuples[allTupleIndex]);
                            changed = true;
                        }
                    }
                }
            }
        }

        // Not part of spec: fix up by adding to relevantTuples the parents
        // of inner classes which are themselves inner classes.
        // i.e., I think that if Foo$Bar$Baz gets added, Foo$Bar needs to be
        // added
        // as well.

        boolean changedFixup = true;
        ArrayList tuplesToAdd = new ArrayList();
        while (changedFixup) {
            changedFixup = false;
            for (int index = 0; index < relevantTuples.size(); index++) {
                IcTuple aRelevantTuple = (IcTuple) relevantTuples.get(index);
                for (int allTupleIndex = 0; allTupleIndex < allTuplesSize; allTupleIndex++) {
                    if (aRelevantTuple.outerClassString().equals(
                            allTuples[allTupleIndex].thisClassString())) {
                        if (!aRelevantTuple.outerIsAnonymous()) {
                            tuplesToAdd.add(allTuples[allTupleIndex]);
                        }
                    }
                }
            }
            if (tuplesToAdd.size() > 0) {
                for(int index = 0; index < tuplesToAdd.size(); index++) {
                    IcTuple tuple = (IcTuple) tuplesToAdd.get(index);
                    if (relevantTuplesContains.add(tuple)) {
                        changedFixup = true;
                        relevantTuples.add(tuple);
                    }
                }
                tuplesToAdd = new ArrayList();
            }
        }
        // End not part of the spec. Ugh.

        // Now order the result as a subsequence of ic_all
        IcTuple[] orderedRelevantTuples = new IcTuple[relevantTuples.size()];
        int orderedRelevantIndex = 0;
        for (int index = 0; index < allTuplesSize; index++) {
            if (relevantTuplesContains.contains(allTuples[index])) {
                orderedRelevantTuples[orderedRelevantIndex] = allTuples[index];
                orderedRelevantIndex++;
            }
        }
        if (orderedRelevantIndex != orderedRelevantTuples.length) {
            // This should never happen. If it does, we have a
            // logic error in the ordering code.
            throw new Error("Missing a tuple when ordering them");
        }
        return orderedRelevantTuples;
    }

}