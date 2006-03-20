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
 * @version $Revision: 1.3.2.2 $
 */
package java.util.regex;

/**
 * Unix line terminator, accepting only \n.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.3.2.2 $
 */
final class UEOLSet extends AbstractSet {

    private int consCounter;

    public UEOLSet(int counter) {
        this.consCounter = counter;
    }

    public int matches(int strIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int rightBound = matchResult.hasAnchoringBounds() ? matchResult
                .getRightBound() : testString.length();

        if (strIndex >= rightBound) {
            matchResult.setConsumed(consCounter, 0);
            return next.matches(strIndex, testString, matchResult);
        }
        // check final line terminator;

        if ((rightBound - strIndex) == 1 && testString.charAt(strIndex) == '\n') {
            matchResult.setConsumed(consCounter, 1);
            return next.matches(strIndex + 1, testString, matchResult);
        }

        return -1;
    }

    public boolean hasConsumed(MatchResultImpl matchResult) {
        int cons;
        boolean res = ((cons = matchResult.getConsumed(consCounter)) < 0 || cons > 0);
        matchResult.setConsumed(consCounter, -1);
        return res;
    }

    protected String getName() {
        return "<EOL>";
    }
}
