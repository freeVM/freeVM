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
 * @version $Revision: 1.7.2.2 $
 */
package java.util.regex;

/**
 * Posessive + quantifier node over groups.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.7.2.2 $
 */
class PosPlusGroupQuantifierSet extends GroupQuantifierSet {
   
    public PosPlusGroupQuantifierSet(AbstractSet innerSet, AbstractSet next,
            int type) {
        super(innerSet, next, type);
        ((JointSet) innerSet).fSet.setNext(FSet.posFSet);

    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {

        int nextIndex;
        if ((nextIndex = innerSet.matches(stringIndex, testString, matchResult)) < 0) {
            return -1;
        } else if (nextIndex > stringIndex) {
            stringIndex = nextIndex;
            while ((nextIndex = innerSet.matches(stringIndex, testString,
                    matchResult)) > stringIndex) {
                stringIndex = nextIndex;
            }
        }

        return next.matches(stringIndex, testString, matchResult);
    }
}
