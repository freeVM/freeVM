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
 * Special node for ".*" construction for any character 
 * including line terminators.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
class DotAllQuantifierSet extends LeafQuantifierSet {

    public DotAllQuantifierSet(LeafSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {

        int strLength = testString.length();

        if (strLength <= stringIndex) {
            return next.matches(stringIndex, testString, matchResult);
        }
        return next.findBack(stringIndex, strLength, testString, matchResult);
    }

    public int find(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int strLength = testString.length();
        if (next.findBack(stringIndex, strLength, testString, matchResult) >= 0) {
            return stringIndex;
        } else {
            return -1;
        }
    }
}
