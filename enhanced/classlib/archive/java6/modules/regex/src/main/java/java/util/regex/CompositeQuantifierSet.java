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

/**
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
package java.util.regex;

/**
 * Composite (i.e. {n,m}) quantifier node over the leaf nodes ("a{n,m}")
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
class CompositeQuantifierSet extends LeafQuantifierSet {
    
    protected Quantifier quantifier = null;

    public CompositeQuantifierSet(Quantifier quant, LeafSet innerSet,
            AbstractSet next, int type) {
        super(innerSet, next, type);
        this.quantifier = quant;
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int min = quantifier.min();
        int max = quantifier.max();
        int i = 0;

        for (; i < min; i++) {

            if (stringIndex + leaf.charCount() > matchResult.getRightBound()) {
                matchResult.hitEnd = true;
                return -1;
            }

            int shift = leaf.accepts(stringIndex, testString);
            if (shift < 1) {
                return -1;
            }
            stringIndex += shift;
        }

        for (; i < max; i++) {
            int shift;
            if (stringIndex + leaf.charCount() > matchResult.getRightBound()
                    || (shift = leaf.accepts(stringIndex, testString)) < 1) {
                break;
            }
            stringIndex += shift;
        }

        for (; i >= min; i--) {
            int shift = next.matches(stringIndex, testString, matchResult);
            if (shift >= 0) {
                return shift;
            }
            stringIndex -= leaf.charCount();
        }
        return -1;

    }

    public void reset() {
        quantifier.resetCounter();
    }

    protected String getName() {
        return quantifier.toString();
    }

    void setQuantifier(Quantifier quant) {
        this.quantifier = quant;
    }
}
