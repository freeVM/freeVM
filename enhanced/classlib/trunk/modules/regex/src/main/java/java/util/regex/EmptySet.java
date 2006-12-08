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
 * @version $Revision: 1.4.2.2 $
 */
package java.util.regex;

/**
 * Valid constatnt zero character match.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.4.2.2 $
 */
class EmptySet extends LeafSet {

    public EmptySet(AbstractSet next) {
        super(next);
        charCount = 0;
    }

    /*
     * @see java.util.regex.LeafSet#accepts(int, java.lang.CharSequence)
     */
    public int accepts(int stringIndex, CharSequence testString) {
        return 0;
    }

    public int find(int stringIndex, CharSequence testString,
            MatchResultImpl matchResult) {
        int strLength = matchResult.getRightBound();
        int startStr = matchResult.getLeftBound();
        
        while (stringIndex <= strLength) {
            
            //check for supplementary codepoints
            if (stringIndex < strLength) {
                char low = testString.charAt(stringIndex);
                
                if (Character.isLowSurrogate(low)) {
                    
                   if (stringIndex > startStr) {
                       char high = testString.charAt(stringIndex - 1);
                       if (Character.isHighSurrogate(high)) {
                           stringIndex++;
                           continue;
                       }
                   }
                }
            }
            
            if (next.matches(stringIndex, testString, matchResult) >= 0) {
                return stringIndex;
            }
            stringIndex++;
        }
        
        return -1;
    }

    public int findBack(int stringIndex, int startSearch,
            CharSequence testString, MatchResultImpl matchResult) {
        int strLength = matchResult.getRightBound();
        int startStr = matchResult.getLeftBound();
        
        while (startSearch >= stringIndex) {
            
            //check for supplementary codepoints
            if (startSearch < strLength) {
                char low = testString.charAt(startSearch);
                
                if (Character.isLowSurrogate(low)) {
                
                   if (startSearch > startStr) {
                      char high = testString.charAt(startSearch - 1);
                      if (Character.isHighSurrogate(high)) {
                          startSearch--;
                          continue;
                      }
                   }
                }
            }
            
            if (next.matches(startSearch, testString, matchResult) >= 0) {
                return startSearch;
            }
            startSearch--;        
        }
        
        return -1;
    }
    
    /*
     * @see java.util.regex.AbstractSet#getName()
     */
    protected String getName() {
        return "<Empty set>"; //$NON-NLS-1$
    }

    public boolean hasConsumed(MatchResultImpl mr) {
        return false;
    }

}
