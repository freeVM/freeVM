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
 * @version $Revision: 1.4.2.2 $
 */
package java.util.regex;

import java.util.ArrayList;

/**
 * This class represent atomic group (?>X), once X matches,
 * this match become unchangable till the end of the match.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.4.2.2 $
 */
class AtomicJointSet extends NonCapJointSet {
    
    public AtomicJointSet(ArrayList children, FSet fSet) {
        super(children, fSet);
    }

    /**
     * Returns stringIndex+shift, the next position to match
     */
    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int start = matchResult.getConsumed(groupIndex);
        matchResult.setConsumed(groupIndex, stringIndex);

        int size = children.size();
        for (int i = 0; i < size; i++) {
            AbstractSet e = (AbstractSet) children.get(i);
            int shift = e.matches(stringIndex, testString, matchResult);
            if (shift >= 0) {
                // AtomicFset always returns true, but saves the index to run
                // this next.match() from;
                return next.matches(((AtomicFSet) fSet).getIndex(), testString,
                        matchResult);
            }
        }

        matchResult.setConsumed(groupIndex, start);
        return -1;
    }

    public void setNext(AbstractSet next) {
        this.next = next;
    }

    public AbstractSet getNext() {
        return next;
    }

    protected String getName() {
        return "NonCapJointSet"; //$NON-NLS-1$
    }
}
