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
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
package java.util.regex;

/**
 * Greedy quantifier node for the case where there is no intersection with
 * next node and normal quantifiers could be treated as greedy and possessive.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
class UnifiedQuantifierSet extends LeafQuantifierSet {
    
    public UnifiedQuantifierSet(LeafSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
    }

    public UnifiedQuantifierSet(LeafQuantifierSet quant) {
        super((LeafSet) quant.getInnerSet(), quant.getNext(), quant.getType());
        innerSet.setNext(this);

    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        while (stringIndex + leaf.charCount() <= matchResult.getRightBound()
                && leaf.accepts(stringIndex, testString) > 0)
            stringIndex++;

        return next.matches(stringIndex, testString, matchResult);
    }

    public int find(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int startSearch = next.find(stringIndex, testString, matchResult);
        if (startSearch < 0)
            return -1;
        int newSearch = startSearch - 1;
        while (newSearch >= stringIndex
                && leaf.accepts(newSearch, testString) > 0) {
            startSearch = newSearch;
            newSearch--;
        }

        return startSearch;
    }
}
