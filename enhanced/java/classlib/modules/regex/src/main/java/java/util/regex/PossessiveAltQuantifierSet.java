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
 */
package java.util.regex;

/**
 * Possessive ? quantifier node.
 * 
 * @author Nikolay A. Kuznetsov
 */
class PossessiveAltQuantifierSet extends AltQuantifierSet {

    public PossessiveAltQuantifierSet(LeafSet innerSet, AbstractSet next,
            int type) {
        super(innerSet, next, type);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int shift = 0;

        if (stringIndex + leaf.charCount() <= matchResult.getRightBound()
                && (shift = leaf.accepts(stringIndex, testString)) >= 1) {
            stringIndex += shift;
        }

        return next.matches(stringIndex, testString, matchResult);
    }
}
