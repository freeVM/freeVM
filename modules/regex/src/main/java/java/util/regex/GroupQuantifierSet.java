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
 * @version $Revision: 1.12.2.2 $
 */
package java.util.regex;

/**
 * Default quantifier over groups, in fact this type of quantifiier is
 * generally used for constructions we cant identify number of characters they 
 * consume.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.12.2.2 $
 */
class GroupQuantifierSet extends QuantifierSet {
   
    public GroupQuantifierSet(AbstractSet innerSet, AbstractSet next, int type) {
        super(innerSet, next, type);
    }

    public int matches(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {

        if (!innerSet.hasConsumed(matchResult))
            return next.matches(stringIndex, testString, matchResult);// return
                                                                        // -1;

        int nextIndex = innerSet.matches(stringIndex, testString, matchResult);

        if (nextIndex < 0) {
            return next.matches(stringIndex, testString, matchResult);
        } else {
            return nextIndex;
        }
    }

    protected String getName() {
        return "<GroupQuant>"; //$NON-NLS-1$
    }
}
