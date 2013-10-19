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
 * This class represents [+*]? constructs over LeafSets.
 * 
 * @see java.util.regex.LeafSet
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
class ReluctantQuantifierSet extends LeafQuantifierSet {

    public ReluctantQuantifierSet(LeafSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int i = 0;
        int shift = 0;

        do {
            shift = next.matches(stringIndex, testString, matchResult);
            if (shift >= 0) {
                return shift;
            }

            if (stringIndex + leaf.charCount() <= matchResult.getRightBound()) {
                shift = leaf.accepts(stringIndex, testString);
                stringIndex += shift;
            }
        } while (shift >= 1);

        return -1;
    }
}
