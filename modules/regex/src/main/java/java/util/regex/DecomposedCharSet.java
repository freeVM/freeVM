/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

package java.util.regex;

/**
 * Represents canonical decomposition of
 * Unicode character. Is used when
 * CANON_EQ flag of Pattern class
 * is specified.
 */
class DecomposedCharSet extends JointSet {
     
    /**
     * Contains information about number of chars
     * that were read for a codepoint last time
     */
    private int readCharsForCodePoint = 1;

    /**
     * UTF-16 encoding of decomposedChar
     */
    private String decomposedCharUTF16 = null;
    
    /**
     * Decomposition of the Unicode codepoint
     */
    private int [] decomposedChar; 
    
    /**
     * Length of useful part of decomposedChar
     * decomposedCharLength <= decomposedChar.length
     */
    private int decomposedCharLength;
    
    public DecomposedCharSet(int [] decomposedChar, int decomposedCharLength) {
        this.decomposedChar = decomposedChar;
        this.decomposedCharLength = decomposedCharLength;
    }
    
    /**
     * Returns the next.
     */
    public AbstractSet getNext() {
        return this.next;
    }
    
    /**
     * Sets next abstract set.
     * @param next
     *            The next to set.
     */
    public void setNext(AbstractSet next) {
        this.next = next;
    }
        
    public int matches(int strIndex, CharSequence testString, MatchResultImpl matchResult) {
        
        /*
         * All decompositions have length that 
         * is less or equal Lexer.MAX_DECOMPOSITION_LENGTH
         */
        int [] decCurCodePoint;
        int [] decCodePoint = new int [Lexer.MAX_DECOMPOSITION_LENGTH];
        int readCodePoints = 0;
        int rightBound = matchResult.getRightBound();                
        int curChar;        
        int i = 0;
        
        if (strIndex >= rightBound) {
           return -1;
        }
        
        /*
         * We read testString and decompose it gradually to compare with
         * this decomposedChar at position strIndex 
         */
        curChar = codePointAt(strIndex, testString, rightBound);
        strIndex += readCharsForCodePoint;        
        decCurCodePoint = Lexer.getDecomposition(curChar);
        if (decCurCodePoint == null) {
            decCodePoint[readCodePoints++] = curChar;
        } else {
            i = decCurCodePoint.length;
            System.arraycopy(decCurCodePoint, 0, decCodePoint, 0, i);
            readCodePoints += i;
        }
                
        if (strIndex < rightBound) {
            curChar = codePointAt(strIndex, testString, rightBound);
            
            /*
             * Read testString until we met a decomposed char boundary
             * and decompose obtained portion of testString
             */
            while ((readCodePoints < Lexer.MAX_DECOMPOSITION_LENGTH)                
                    && !Lexer.isDecomposedCharBoundary(curChar)) {
                
                    if (Lexer.hasDecompositionNonNullCanClass(curChar)) {
                        
                        /*
                         * A few codepoints have decompositions and non null
                         * canonical classes, we have to take them into
                         * consideration, but general rule is: 
                         * if canonical class != 0 then no decomposition
                         */
                        decCurCodePoint = Lexer.getDecomposition(curChar);
                        
                        /*
                         * Length of such decomposition is 1 or 2. See 
                         * UnicodeData file 
                         * http://www.unicode.org/Public/4.0-Update
                         *        /UnicodeData-4.0.0.txt
                         */
                        if (decCurCodePoint.length == 2) {
                           decCodePoint[readCodePoints++] = decCurCodePoint[0];
                           decCodePoint[readCodePoints++] = decCurCodePoint[1];
                           } else {
                              decCodePoint[readCodePoints++] = decCurCodePoint[0];
                           }
                    } else {           
                        decCodePoint[readCodePoints++] = curChar;
                    }
                
                    strIndex += readCharsForCodePoint;
                
                    if (strIndex < rightBound) {
                        curChar = codePointAt(strIndex, testString, rightBound);
                    } else {
                        break;
                    }
            }                
        }
        
        /*
         * Some optimization since length of decomposed char is <= 3 usually 
         */
        switch (readCodePoints) {
             case 0:               
             case 1:               
               case 2:       
                 break;
                 
             case 3:
                 int i1 = Lexer.getCanonicalClass(decCodePoint[1]);
                 int i2 = Lexer.getCanonicalClass(decCodePoint[2]);
            
                 if ((i2 != 0) && (i1 > i2)) {
                      i1 = decCodePoint[1];
                      decCodePoint[1] = decCodePoint[2];
                      decCodePoint[2] = i1;
                 }
                 break;
                 
             default:
                 decCodePoint = Lexer.getCanonicalOrder(decCodePoint,
                         readCodePoints);
         }
                
        /*
         * Compare decomposedChar with decomposed char
         * that was just read from testString
         */
        if (readCodePoints != decomposedCharLength) {
            return -1;
        } 
        
        for (i = 0; i < readCodePoints; i++) {
            if (decCodePoint[i] != decomposedChar[i]) {
                return -1;
            }
        }

        return next.matches(strIndex, testString, matchResult);                                    
    }
    
    /**
     * Return UTF-16 encoding of given Unicode codepoint.
     * 
     * @return UTF-16 encoding
     */
    private String getDecomposedChar() {
        if (decomposedCharUTF16 == null) {
            StringBuffer strBuff = new StringBuffer();
            
            for (int i = 0; i < decomposedCharLength; i++) {
                strBuff.append(Lexer.toChars(decomposedChar[i]));
            }
            decomposedCharUTF16 = strBuff.toString();
        }
        return decomposedCharUTF16;
    }

    protected String getName() {
        return "decomposed char:" + getDecomposedChar(); //$NON-NLS-1$
    }

    /**
     * Reads Unicode codepoint from input.
     * 
     * @param strIndex - index to read codepoint at
     * @param testString - input
     * @param matchResult - auxilary object
     * @return codepoint at given strIndex at testString and
     */
    public int codePointAt(int strIndex, CharSequence testString,
            int rightBound) {
        
        /*
         * We store information about number of codepoints
         * we read at variable readCharsForCodePoint.
         */                
        int curChar;
        
        readCharsForCodePoint = 1;
        if (strIndex < rightBound - 1) {        
            char high = testString.charAt(strIndex++);
            char low = testString.charAt(strIndex);
            
            if (Lexer.isSurrogatePair(high, low)) {
                char [] curCodePointUTF16 = new char [] {high, low};
                curChar = Lexer.codePointAt(curCodePointUTF16, 0);
                readCharsForCodePoint = 2;
            } else {
                curChar = high;
            }
        } else {
            curChar = testString.charAt(strIndex);
        }
                
        return curChar;
    }

    public boolean first(AbstractSet set) {
        return (set instanceof DecomposedCharSet)
               ? ((DecomposedCharSet) set).getDecomposedChar()
                       .equals(getDecomposedChar())
               : true;
    }
    
    public boolean hasConsumed(MatchResultImpl matchResult) {         
        return true;
    }
}

