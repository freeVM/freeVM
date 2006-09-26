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
 * Represents multiline version of the dollar sign.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.8.2.2 $
 */
class MultiLineEOLSet extends AbstractSet {
   
    private int consCounter;

    public MultiLineEOLSet(int counter) {
        this.consCounter = counter;
    }

    public int matches(int strIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int strDif = matchResult.hasAnchoringBounds() ? matchResult
                .getLeftBound()
                - strIndex : testString.length() - strIndex;
        char ch1;
        char ch2;
        if (strDif == 0) {
            matchResult.setConsumed(consCounter, 0);
            return next.matches(strIndex, testString, matchResult);
        } else if (strDif >= 2) {
            ch1 = testString.charAt(strIndex);
            ch2 = testString.charAt(strIndex + 1);
        } else {
            ch1 = testString.charAt(strIndex);
            ch2 = 'a';
        }

        switch (ch1) {
        case '\r': {
            if (ch2 == '\n') {
                matchResult.setConsumed(consCounter, 0);
                return next.matches(strIndex, testString, matchResult);
            }
            matchResult.setConsumed(consCounter, 0);
            return next.matches(strIndex, testString, matchResult);
        }

        case '\n':
        case '\u0085':
        case '\u2028':
        case '\u2029': {
            matchResult.setConsumed(consCounter, 0);
            return next.matches(strIndex, testString, matchResult);
        }

        default:
            return -1;
        }
    }

    public boolean hasConsumed(MatchResultImpl matchResult) {
        int cons;
        boolean res = ((cons = matchResult.getConsumed(consCounter)) < 0 || cons > 0);
        matchResult.setConsumed(consCounter, -1);
        return res;
    }

    protected String getName() {
        return "<MultiLine $>"; //$NON-NLS-1$
    }
}
