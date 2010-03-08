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
 * Base class for nodes representing leaf tokens of the RE, those who consumes
 * fixed number of characters.
 * 
 * @author Nikolay A. Kuznetsov
 */
abstract class LeafSet extends AbstractSet {
    
    protected int charCount = 1;

    public LeafSet(AbstractSet next) {
        super(next);
        setType(AbstractSet.TYPE_LEAF);
    }

    public LeafSet() {
    }

    /**
     * Returns "shift", the number of accepted chars commonly internal function,
     * but called by quantifiers.
     */
    public abstract int accepts(int stringIndex, CharSequence testString);

    /**
     * Checks if we can enter this state and pass the control to the next one.
     * Return positive value if match succeeds, negative otherwise.
     */
    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {

        if (stringIndex + charCount() > matchResult.getRightBound()) {
            matchResult.hitEnd = true;
            return -1;
        }

        int shift = accepts(stringIndex, testString);
        if (shift < 0) {
            return -1;
        }

        return next.matches(stringIndex + shift, testString, matchResult);
    }

    /**
     * Returns number of characters this node consumes.
     * @return number of characters this node consumes.
     */
    public int charCount() {
        return charCount;
    }

    public boolean hasConsumed(MatchResultImpl mr) {
        return true;
    }
}
