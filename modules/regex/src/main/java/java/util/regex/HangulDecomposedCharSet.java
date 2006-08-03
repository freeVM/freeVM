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
 * Hangul syllable. Is used when
 * CANON_EQ flag of Pattern class
 * is specified.
 */
class HangulDecomposedCharSet extends JointSet {    

    /**
     * Decomposed Hangul syllable.
     */
    private char [] decomposedChar; 
    
    /**
     * String representing syllable 
     */
    private String decomposedCharUTF16 = null;
    
    /**
     * Length of useful part of decomposedChar
     * decomposedCharLength <= decomposedChar.length
     */
    private int decomposedCharLength;
    
    public HangulDecomposedCharSet(char [] decomposedChar, int decomposedCharLength) {
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
    
    /**
     * Give string representation of this.
     *
     * @return - string representation.
     */
    private String getDecomposedChar() {
        return (decomposedCharUTF16 == null)
               ? (decomposedCharUTF16 = new String(decomposedChar))
               : decomposedCharUTF16;
    }
    
    protected String getName() {
        return "decomposed Hangul syllable:" + getDecomposedChar();
    }
    
    public int matches(int strIndex, CharSequence testString, MatchResultImpl matchResult) {
        
        /*
         * All decompositions for Hangul syllables have length that 
         * is less or equal Lexer.MAX_DECOMPOSITION_LENGTH
         */
        int rightBound = matchResult.getRightBound();
        int SyllIndex = 0;
        int [] decompSyllable = new int [Lexer
                                         .MAX_HANGUL_DECOMPOSITION_LENGTH]; 
        int [] decompCurSymb;
        char curSymb;
        
        /*
         * For details about Hangul composition and decomposition see
         * http://www.unicode.org/versions/Unicode4.0.0/ch03.pdf
         * "3.12 Conjoining Jamo Behavior"
         */
        int LIndex = -1;
        int VIndex = -1;
        int TIndex = -1;
        
        if (strIndex >= rightBound) {
            return -1;
        }
        curSymb = testString.charAt(strIndex++);
        decompCurSymb = Lexer.getHangulDecomposition(curSymb);
                
        if (decompCurSymb == null) {
            
            /*
             * We deal with ordinary letter or sequence of jamos
             * at strIndex at testString.
             */
            decompSyllable[SyllIndex++] = curSymb;            
            LIndex = curSymb - Lexer.LBase;
            
            if ((LIndex < 0) || (LIndex >= Lexer.LCount)) {
                
                /*
                 * Ordinary letter, that doesn't match this
                 */
                return -1; 
            }
            
            if (strIndex < rightBound) {
                curSymb = testString.charAt(strIndex);                
                VIndex = curSymb  - Lexer.VBase;                
            }    
            
            if ((VIndex < 0) || (VIndex >= Lexer.VCount)) {
                
                /*
                 * Single L jamo doesn't compose Hangul syllable,
                 * so doesn't match
                 */
                return -1;
            }
            strIndex++;
            decompSyllable[SyllIndex++] = curSymb;
            
            if (strIndex < rightBound) {
                curSymb = testString.charAt(strIndex);                
                TIndex = curSymb  - Lexer.TBase;
            }    
            
            if ((TIndex < 0) || (TIndex >= Lexer.TCount)) {
                
                /*
                 * We deal with LV syllable at testString, so
                 * compare it to this
                 */
                return ((decomposedCharLength == 2) 
                        && (decompSyllable[0] == decomposedChar[0])
                        && (decompSyllable[1] == decomposedChar[1]))
                       ? next.matches(strIndex, testString, matchResult)
                       : -1;
            }
            strIndex++;
            decompSyllable[SyllIndex++] = curSymb;
            
            /*
             * We deal with LVT syllable at testString, so
             * compare it to this
             */
            return ((decomposedCharLength == 3) 
                    && (decompSyllable[0] == decomposedChar[0])
                    && (decompSyllable[1] == decomposedChar[1])
                    && (decompSyllable[2] == decomposedChar[2]))
                   ? next.matches(strIndex, testString, matchResult)
                   : -1;
        } else {
            
            /*
             * We deal with Hangul syllable at strIndex at testString.
             * So we decomposed it to compare with this. 
             */            
            int i = 0;
            
            if (decompCurSymb.length != decomposedCharLength) {
                return -1;
            }
            
            for (; i < decomposedCharLength; i++) {
                if (decompCurSymb[i] != decomposedChar[i]) {
                    return -1;
                }
            }
            return next.matches(strIndex, testString, matchResult);
        }                             
    }
    
    public boolean first(AbstractSet set) {
        return (set instanceof HangulDecomposedCharSet)
               ? ((HangulDecomposedCharSet) set).getDecomposedChar()
                       .equals(getDecomposedChar())
               : true;
    }
    
    public boolean hasConsumed(MatchResultImpl matchResult) {         
        return true;
    }
}

