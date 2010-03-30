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
 * Represents "?" quantifier over leaf sets.
 * 
 * @author Nikolay A. Kuznetsov
 */
class AltQuantifierSet extends LeafQuantifierSet {

    public AltQuantifierSet(LeafSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int shift = 0;

        if ((shift = innerSet.matches(stringIndex, testString, matchResult)) >= 0) {
            return shift;
        } else {
            return next.matches(stringIndex, testString, matchResult);
        }
    }

    public void setNext(AbstractSet next) {
        super.setNext(next);
        innerSet.setNext(next);
    }
}
