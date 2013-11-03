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
 * @author Nikolay A. Kuznetsov
 */
class AtomicFSet extends FSet {
    
    int index;

    public AtomicFSet(int groupIndex) {
        super(groupIndex);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {

        int gr = getGroupIndex();
        matchResult.setConsumed(gr, stringIndex - matchResult.getConsumed(gr));
        index = stringIndex;

        return stringIndex;
    }

    public int getIndex() {
        return index;
    }

    protected String getName() {
        return "AtomicFSet"; //$NON-NLS-1$
    }

    public boolean hasConsumed(MatchResultImpl mr) {
        return false;
    }
}
