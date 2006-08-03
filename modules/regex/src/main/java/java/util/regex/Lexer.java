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
 * @version $Revision: 1.21.2.2 $
 */
package java.util.regex;

import java.util.MissingResourceException;

/**
 * The purpose of this class is to break given pattern into RE tokens; 
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.21.2.2 $
 */
class Lexer {

    public static final int CHAR_DOLLAR = 0xe0000000 | '$';

    public static final int CHAR_RIGHT_PARENTHESIS = 0xe0000000 | ')';

    public static final int CHAR_LEFT_SQUARE_BRACKET = 0xe0000000 | '[';

    public static final int CHAR_RIGHT_SQUARE_BRACKET = 0xe0000000 | ']';

    public static final int CHAR_CARET = 0xe0000000 | '^';

    public static final int CHAR_VERTICAL_BAR = 0xe0000000 | '|';

    public static final int CHAR_AMPERSAND = 0xe0000000 | '&';

    public static final int CHAR_HYPHEN = 0xe0000000 | '-';

    public static final int CHAR_DOT = 0xe0000000 | '.';

    public static final int QMOD_GREEDY = 0xe0000000;

    public static final int QMOD_RELUCTANT = 0xc0000000;

    public static final int QMOD_POSSESSIVE = 0x80000000;

    public static final int QUANT_STAR = QMOD_GREEDY | '*';

    public static final int QUANT_STAR_P = QMOD_POSSESSIVE | '*';

    public static final int QUANT_STAR_R = QMOD_RELUCTANT | '*';

    public static final int QUANT_PLUS = QMOD_GREEDY | '+';

    public static final int QUANT_PLUS_P = QMOD_POSSESSIVE | '+';

    public static final int QUANT_PLUS_R = QMOD_RELUCTANT | '+';

    public static final int QUANT_ALT = QMOD_GREEDY | '?';

    public static final int QUANT_ALT_P = QMOD_POSSESSIVE | '?';

    public static final int QUANT_ALT_R = QMOD_RELUCTANT | '?';

    public static final int QUANT_COMP = QMOD_GREEDY | '{';

    public static final int QUANT_COMP_P = QMOD_POSSESSIVE | '{';

    public static final int QUANT_COMP_R = QMOD_RELUCTANT | '{';

    public static final int CHAR_LEFT_PARENTHESIS = 0x80000000 | '(';

    public static final int CHAR_NONCAP_GROUP = 0xc0000000 | '(';

    public static final int CHAR_POS_LOOKAHEAD = 0xe0000000 | '(';

    public static final int CHAR_NEG_LOOKAHEAD = 0xf0000000 | '(';

    public static final int CHAR_POS_LOOKBEHIND = 0xf8000000 | '(';

    public static final int CHAR_NEG_LOOKBEHIND = 0xfc000000 | '(';

    public static final int CHAR_ATOMIC_GROUP = 0xfe000000 | '(';

    public static final int CHAR_FLAGS = 0xff000000 | '(';

    public static final int CHAR_START_OF_INPUT = 0x80000000 | 'A';

    public static final int CHAR_WORD_BOUND = 0x80000000 | 'b';

    public static final int CHAR_NONWORD_BOUND = 0x80000000 | 'B';

    public static final int CHAR_PREVIOUS_MATCH = 0x80000000 | 'G';

    public static final int CHAR_END_OF_INPUT = 0x80000000 | 'z';

    public static final int CHAR_END_OF_LINE = 0x80000000 | 'Z';

    public static final int MODE_PATTERN = 1 << 0;

    public static final int MODE_RANGE = 1 << 1;

    public static final int MODE_ESCAPE = 1 << 2;
    
    //maximum length of decomposition
    static final int MAX_DECOMPOSITION_LENGTH = 4;
        
    /*
     * maximum length of Hangul decomposition
     * note that MAX_HANGUL_DECOMPOSITION_LENGTH <= MAX_DECOMPOSITION_LENGTH
     */
    static final int MAX_HANGUL_DECOMPOSITION_LENGTH = 3;
        
    //maximum value of codepoint for basic multilingual pane of Unicode
    static final int MAX_CODEPOINT_BASIC_MULTILINGUAL_PANE = 0xFFFF;
        
    /*
     * Following constants are needed for Hangul canonical decomposition.
     * Hangul decomposition algorithm and constants are taken according
     * to description at http://www.unicode.org/versions/Unicode4.0.0/ch03.pdf
     * "3.12 Conjoining Jamo Behavior"
     */    
    static final int SBase = 0xAC00;
        
    static final int LBase = 0x1100;
        
    static final int VBase = 0x1161;
        
    static final int TBase = 0x11A7;
        
    static final int SCount = 11172;
        
    static final int LCount = 19;
        
    static final int VCount = 21;
        
    static final int TCount = 28;
        
    static final int NCount = 588;
        
    //table that contains canonical decomposition mappings
    private static IntArrHash decompTable = null;
        
    //table that contains canonical combining classes
    private static IntHash canonClassesTable = null;
        
    private static int canonClassesTableSize;
        
    /*
     * Table that contains information about Unicode codepoints with
     * single codepoint decomposition
     */
    private static IntHash singleDecompTable = null;
        
    private static int singleDecompTableSize;
    
    private char[] pattern = null;

    private int flags = 0;

    private int mode = 1;

    // when in literal mode, this field will save the previous one
    private int saved_mode = 0;

    // previous char read
    private int lookBack;

    //current character read
    private int ch;

    //next character
    private int lookAhead;
    
    //index of last char in pattern plus one
    private int patternFullLength = 0;

    // cur special token
    private SpecialToken curST = null;
    
    // next special token
    private SpecialToken lookAheadST = null;

    //  cur char being processed
    private int index = 0; 

    //  previous non-whitespace character index;
    private int prevNW = 0; 

    //  cur token start index
    private int curToc = 0; 

    //  look ahead token index
    private int lookAheadToc = 0; 

    //  original string representing pattern    
    private String orig = null; 

    public Lexer(String pattern, int flags) {
        orig = pattern;
        if ((flags & Pattern.LITERAL) > 0) {
            pattern = Pattern.quote(pattern);
        } else if ((flags & Pattern.CANON_EQ) > 0) {
            pattern = Lexer.normalize(pattern);
        }

        this.pattern = new char[pattern.length() + 2];
        System.arraycopy(pattern.toCharArray(), 0, this.pattern, 0, 
 		       pattern.length());
        this.pattern[this.pattern.length - 1] = 0;
        this.pattern[this.pattern.length - 2] = 0;        
        patternFullLength = this.pattern.length;
        this.flags = flags;
        // read first two tokens;
        movePointer();
        movePointer();

    }

    /**
     * Returns current character w/o reading next one; if there are no more
     * characters returns 0;
     * 
     * @return current character;
     */
    public int peek() {
        return ch;
    }

    /**
     * Set the Lexer to PATTERN or RANGE mode; Lexer interpret character two
     * different ways in parser or range modes.
     * 
     * @param mode
     *            Lexer.PATTERN or Lexer.RANGE
     */
    public void setMode(int mode) {
        if (mode > 0 && mode < 3) {
            this.mode = mode;
        }

        if (mode == Lexer.MODE_PATTERN) {
            reread();
        }
    }

    /**
     * Restores flags for Lexer
     * 
     * @param flags
     */
    public void restoreFlags(int flags) {
        this.flags = flags;
    	lookAhead = ch;
    	lookAheadST = curST;
    	        
    	//curToc is an index of closing bracket )
    	index = curToc + 1;
        lookAheadToc = curToc;        
    	movePointer();
    }

    public SpecialToken peekSpecial() {
        return curST;
    }

    /**
     * Returns true, if current token is special, i.e. quantifier, or other 
     * compound token.
     * 
     * @return - true if current token is special, false otherwise.
     */
    public boolean isSpecial() {
        return curST != null;
    }

    public boolean isQuantifier() {
        return isSpecial() && curST.getType() == SpecialToken.TOK_QUANTIFIER;
    }

    public boolean isNextSpecial() {
        return lookAheadST != null;
    }

    /**
     * Returns current character and moves string index to the next one;
     * 
     */
    public int next() {
        movePointer();
        return lookBack;
    }

    /**
     * Returns current special token and moves string index to the next one;
     */
    public SpecialToken nextSpecial() {
        SpecialToken res = curST;
        movePointer();
        return res;
    }

    /**
     * Returns nest symbol read.
     */
    public int lookAhead() {
        return lookAhead;
    }

    /**
     * Returns previous character.
     */
    public int back() {
        return lookBack;
    }

    /**
     * Normalize given expression.
     * 
     * @param input - expression to normalize
     * @return normalized expression.
     */
    static String normalize(String input) {                       
        char [] inputChars = input.toCharArray();
        int inputLength = inputChars.length;
        int resCodePointsIndex = 0;
        int inputCodePointsIndex = 0;
        int decompHangulIndex = 0;
        
        //codePoints of input
        int [] inputCodePoints = new int [inputLength];
        
        //result of canonical decomposition of input
        int [] resCodePoints = new int [inputLength * MAX_DECOMPOSITION_LENGTH];
        
        //current symbol's codepoint
        int ch;
        
        //current symbol's decomposition
        int [] decomp;
                
        //result of canonical and Hangul decomposition of input
        int [] decompHangul;
        
        //result of canonical decomposition of input in UTF-16 encoding
        StringBuffer result = new StringBuffer();
        
        decompTable = HashDecompositions.getHashDecompositions();
        canonClassesTable = CanClasses.getHashCanClasses();
        canonClassesTableSize = canonClassesTable.size;
        singleDecompTable = SingleDecompositions.getHashSingleDecompositions();
        singleDecompTableSize = singleDecompTable.size;
        
        for (int i = 0; i < inputLength; i += Lexer.charCount(ch)) {
            ch = Lexer.codePointAt(inputChars, i);
            inputCodePoints[inputCodePointsIndex++] = ch;
        }
                        
        /*
         * Canonical decomposition based on mappings in decompTable
         */
        for (int i = 0; i < inputCodePointsIndex; i++) {            
            ch = inputCodePoints[i];
            
            decomp = Lexer.getDecomposition(ch);
            if (decomp == null) {
                resCodePoints[resCodePointsIndex++] = ch;
            } else {
                int curSymbDecompLength = decomp.length;
                
                for (int j = 0; j < curSymbDecompLength; j++) {
                    resCodePoints[resCodePointsIndex++] = decomp[j];
                } 
            }
        }
        
        /*
         * Canonical ordering.
         * See http://www.unicode.org/reports/tr15/#Decomposition for
         * details
         */        
        resCodePoints = Lexer.getCanonicalOrder(resCodePoints,
                resCodePointsIndex);
        
        /*
         * Decomposition for Hangul syllables.
         * See http://www.unicode.org/reports/tr15/#Hangul for
         * details
         */        
        decompHangul = new int [resCodePoints.length];
        
        for (int i = 0; i < resCodePointsIndex; i++) {
            int curSymb = resCodePoints[i];
            
            decomp = getHangulDecomposition(curSymb);
            if (decomp == null) {
                decompHangul[decompHangulIndex++] = curSymb;
            } else{
                
                /*
                 * Note that Hangul decompositions have length that is
                 * equal 2 or 3.
                 */
                decompHangul[decompHangulIndex++] = decomp[0];
                decompHangul[decompHangulIndex++] = decomp[1];
                if (decomp.length == 3) {
                    decompHangul[decompHangulIndex++] = decomp[2];                    
                }
            }
        }
        
        /*
         * Translating into UTF-16 encoding
         */
        for (int i = 0; i < decompHangulIndex; i++) {
            result.append(Lexer.toChars(decompHangul[i]));
        }
        
        return result.toString();
    }

    /**
     * Rearrange codepoints according
     * to canonical order.
     * 
     * @param inputInts - array that contains Unicode codepoints
     * @param length - index of last Unicode codepoint plus 1
     * 
     * @return array that contains rearranged codepoints.
     */
    static int [] getCanonicalOrder(int [] inputInts, int length) {                      
        int inputLength = (length < inputInts.length)
                          ? length
                          :    inputInts.length;
        
        /*
         * Simple bubble-sort algorithm.
         * Note that many codepoints have 0
         * canonical class, so this algorithm works
         * almost lineary in overwhelming majority
         * of cases. This is due to specific of Unicode
         * combining classes and codepoints.
         */
        for (int i = 1; i < inputLength; i++) {
            int j = i - 1;
            int iCanonicalClass = getCanonicalClass(inputInts[i]);
            int ch;
            
            if (iCanonicalClass == 0) {
                continue;
            }
            
            while (j > -1) {                                
                if (getCanonicalClass(inputInts[j]) > iCanonicalClass) {
                    j = j - 1;
                } else {
                    break;
                }
            }
            
            ch = inputInts [i];            
            for (int k = i; k > j + 1; k--) {
                inputInts[k] = inputInts [k - 1];
            }
            inputInts[j + 1] = ch;
        }

        return inputInts;
    }
    
    /**
     * Reread current character, may be require if previous token changes mode
     * to one with different character interpretation.
     *
     */
    private void reread() {
        lookAhead = ch;
        lookAheadST = curST;
        index = lookAheadToc;
        lookAheadToc = curToc;
        movePointer();
    }

    /**
     * Moves pointer one position right; save current character to lookBack;
     * lookAhead to current one and finaly read one more to lookAhead;
     */
    private void movePointer() {
        // swap pointers
        lookBack = ch;
        ch = lookAhead;
        curST = lookAheadST;
        curToc = lookAheadToc;
        lookAheadToc = index;
        boolean reread;
        do {
            reread = false;
            // read next character analize it and construct token:
            // //
            lookAhead = (index < pattern.length) ? pattern[nextIndex()] : 0;
            lookAheadST = null;

            if (mode == Lexer.MODE_ESCAPE) {
                if (lookAhead == '\\') {
                    lookAhead = (index < pattern.length) ? pattern[nextIndex()]
                            : 0;

                    switch (lookAhead) {
                    case 'E': {
                    	mode = saved_mode;
                        lookAhead = (index <= pattern.length - 2) 
                                    ? pattern[nextIndex()] 
                                    : 0;
                        break;
                    }

                    default: {
                        lookAhead = '\\';
                        index = prevNW;
                        return;
                    }
                    }
                } else {
                    return;
                }
            }

            if (lookAhead == '\\') {
                lookAhead = (index < pattern.length - 2) ? pattern[nextIndex()]
                        : -1;
                switch (lookAhead) {
                case -1:
                    throw new PatternSyntaxException(I18n
                            .getMessage("Trailing \\"), this.toString(), index);
                case 'P':
                case 'p': {
                    String cs = parseCharClassName();
                    boolean negative = false;

                    if (lookAhead == 'P')
                        negative = true;
                    ;
                    try {
                        lookAheadST = AbstractCharClass.getPredefinedClass(cs,
                                negative);
                    } catch (MissingResourceException mre) {
                        throw new PatternSyntaxException(I18n
                                .getFormattedMessage(
                                        "Character Class \\p'{'{0}'}'"
                                                + "is not supported", cs), this
                                .toString(), index);
                    }
                    lookAhead = 0;
                    break;
                }

                case 'w':
                case 's':
                case 'd':
                case 'W':
                case 'S':
                case 'D': {
                    lookAheadST = CharClass.getPredefinedClass(new String(
                            pattern, prevNW, 1), false);
                    lookAhead = 0;
                    break;
                }

                case 'Q': {
                    saved_mode = mode;
                    mode = Lexer.MODE_ESCAPE;
                    reread = true;
                    break;
                }

                case 't':
                    lookAhead = '\t';
                    break;
                case 'n':
                    lookAhead = '\n';
                    break;
                case 'r':
                    lookAhead = '\r';
                    break;
                case 'f':
                    lookAhead = '\f';
                    break;
                case 'a':
                    lookAhead = '\u0007';
                    break;
                case 'e':
                    lookAhead = '\u001B';
                    break;

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    if (mode == Lexer.MODE_PATTERN) {
                        lookAhead = 0x80000000 | lookAhead;
                    }
                    break;
                }

                case '0':
                    lookAhead = readOctals();
                    break;
                case 'x':
                    lookAhead = readHex("hexadecimal", 2);
                    break;
                case 'u':
                    lookAhead = readHex("Unicode", 4);
                    break;

                case 'b':
                    lookAhead = CHAR_WORD_BOUND;
                    break;
                case 'B':
                    lookAhead = CHAR_NONWORD_BOUND;
                    break;
                case 'A':
                    lookAhead = CHAR_START_OF_INPUT;
                    break;
                case 'G':
                    lookAhead = CHAR_PREVIOUS_MATCH;
                    break;
                case 'Z':
                    lookAhead = CHAR_END_OF_LINE;
                    break;
                case 'z':
                    lookAhead = CHAR_END_OF_INPUT;
                    break;
                case 'c': {
                    if (index < pattern.length - 2) {
                        lookAhead = (pattern[nextIndex()] & 0x1f);
                        break;
                    } else {
                        throw new PatternSyntaxException("Illegal unsupported "
                                + "control sequence", this.toString(), index);
                    }
                }
                case 'C':
                case 'E':
                case 'F':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'R':
                case 'T':
                case 'U':
                case 'V':
                case 'X':
                case 'Y':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'o':
                case 'q':
                case 'y':
                    throw new PatternSyntaxException("Illegal unsupported "
                            + "escape sequence", this.toString(), index);

                default:
                    break;
                }
            } else if (mode == Lexer.MODE_PATTERN) {
                switch (lookAhead) {
                case '+':
                case '*':
                case '?': {
                    char mod = (index < pattern.length) ? pattern[index] : '*';
                    switch (mod) {
                    case '+': {
                        lookAhead = lookAhead | Lexer.QMOD_POSSESSIVE;
                        nextIndex();
                        break;
                    }
                    case '?': {
                        lookAhead = lookAhead | Lexer.QMOD_RELUCTANT;
                        nextIndex();
                        break;
                    }
                    default: {
                        lookAhead = lookAhead | Lexer.QMOD_GREEDY;
                        break;
                    }
                    }

                    break;
                }

                case '{': {
                    lookAheadST = processQuantifier(lookAhead);
                    break;
                }

                case '$':
                    lookAhead = CHAR_DOLLAR;
                    break;
                case '(': {
                    if (pattern[index] == '?') {
                        nextIndex();
                        char nonCap = pattern[index];
                        boolean behind = false;
                        do {
                            if (!behind) {
                                switch (nonCap) {
                                case '!':
                                    lookAhead = CHAR_NEG_LOOKAHEAD;
                                    nextIndex();
                                    break;
                                case '=':
                                    lookAhead = CHAR_POS_LOOKAHEAD;
                                    nextIndex();
                                    break;
                                case '>':
                                    lookAhead = CHAR_ATOMIC_GROUP;
                                    nextIndex();
                                    break;
                                case '<': {
                                    nextIndex();
                                    nonCap = pattern[index];
                                    behind = true;
                                    break;
                                }
                                default: {
                                    lookAhead = readFlags();
                                    
                                    /*
                                     * We return res = res | 1 << 8
                                     * from readFlags() if we read
                                     * (?idmsux-idmsux)
                                     */
                                    if (lookAhead >= 256) {
                                    	
                                    	//Erase auxiliaury bit
                                    	lookAhead = (lookAhead & 0xff);    
                                    	flags = lookAhead;
                                    	lookAhead = lookAhead << 16;
                                        lookAhead = CHAR_FLAGS | lookAhead;
                                    } else {
                                    	flags = lookAhead;
                                        lookAhead = lookAhead << 16;
                                        lookAhead = CHAR_NONCAP_GROUP
                                                    | lookAhead;
                                    }
                                    break;                                
                                }
                                }
                            } else {
                                behind = false;
                                switch (nonCap) {
                                case '!':
                                    lookAhead = CHAR_NEG_LOOKBEHIND;
                                    nextIndex();
                                    break;
                                case '=':
                                    lookAhead = CHAR_POS_LOOKBEHIND;
                                    nextIndex();
                                    break;
                                default:
                                    throw new PatternSyntaxException("Unknown "
                                            + "look behind", this.toString(),
                                            index);
                                }
                            }
                        } while (behind);
                    } else {
                        lookAhead = CHAR_LEFT_PARENTHESIS;
                    }
                    break;
                }

                case ')':
                    lookAhead = CHAR_RIGHT_PARENTHESIS;
                    break;
                case '[': {
                    lookAhead = CHAR_LEFT_SQUARE_BRACKET;
                    setMode(Lexer.MODE_RANGE);
                    break;
                }
                case ']': {
                    if (mode == Lexer.MODE_RANGE) {
                        lookAhead = CHAR_RIGHT_SQUARE_BRACKET;
                    }
                    break;
                }
                case '^':
                    lookAhead = CHAR_CARET;
                    break;
                case '|':
                    lookAhead = CHAR_VERTICAL_BAR;
                    break;
                case '.':
                    lookAhead = CHAR_DOT;
                    break;
                default:
                    break;
                }
            } else if (mode == Lexer.MODE_RANGE) {
                switch (lookAhead) {
                case '[':
                    lookAhead = CHAR_LEFT_SQUARE_BRACKET;
                    break;
                case ']':
                    lookAhead = CHAR_RIGHT_SQUARE_BRACKET;
                    break;
                case '^':
                    lookAhead = CHAR_CARET;
                    break;
                case '&':
                    lookAhead = CHAR_AMPERSAND;
                    break;
                case '-':
                    lookAhead = CHAR_HYPHEN;
                    break;
                default:
                    break;
                }
            }
        } while (reread);
    }

    /**
     * Parse character classes names and verifies correction of the syntax;
     */
    private String parseCharClassName() {
        StringBuffer sb = new StringBuffer(10);
        if (index < pattern.length - 2) {
            // one symbol family
            if (pattern[index] != '{') {
                return "Is" + new String(pattern, nextIndex(), 1);
            }

            nextIndex();
            char ch = 0;
            while (index < pattern.length - 2
                    && (ch = pattern[nextIndex()]) != '}') {
                sb.append((char) ch);
            }
            if (ch != '}')
                throw new PatternSyntaxException(I18n
                        .getMessage("Unclosed character family"), this
                        .toString(), index);
        }

        if (sb.length() == 0)
            throw new PatternSyntaxException(I18n
                    .getMessage("Empty character family"), this.toString(),
                    index);

        String res = sb.toString();
        if (res.length() == 1)
            return "Is" + res;
        return (res.length() > 3 && (res.startsWith("Is") || res
                .startsWith("In"))) ? res.substring(2) : res;
    }

    /**
     * Process given character in assumption that it's quantifier.
     */
    private Quantifier processQuantifier(int ch) {
        StringBuffer sb = new StringBuffer(4);
        int min = -1;
        int max = Integer.MAX_VALUE;
        while (index < pattern.length && (ch = pattern[nextIndex()]) != '}') {
            if (ch == ',' && min < 0) {
                try {
                    min = Integer.parseInt(sb.toString(), 10);
                    sb.delete(0, sb.length());
                } catch (NumberFormatException nfe) {
                    throw new PatternSyntaxException(I18n
                            .getMessage("Incorrect Quantifier Syntax"), this
                            .toString(), index);
                }
            } else {
                sb.append((char) ch);
            }
        }
        if (ch != '}') {
            throw new PatternSyntaxException(I18n
                    .getMessage("Incorrect Quantifier Syntax"),
                    this.toString(), index);
        }
        if (sb.length() > 0) {
            try {
                max = Integer.parseInt(sb.toString(), 10);
                if (min < 0)
                    min = max;
            } catch (NumberFormatException nfe) {
                throw new PatternSyntaxException(I18n
                        .getMessage("Incorrect Quantifier Syntax"), this
                        .toString(), index);
            }
        } else if (min < 0) {
            throw new PatternSyntaxException(I18n
                    .getMessage("Incorrect Quantifier Syntax"),
                    this.toString(), index);
        }
        if ((min | max | max - min) < 0) {
            throw new PatternSyntaxException(I18n
                    .getMessage("Incorrect Quantifier Syntax"),
                    this.toString(), index);
        }

        char mod = (index < pattern.length) ? pattern[index] : '*';

        switch (mod) {
        case '+':
            lookAhead = Lexer.QUANT_COMP_P;
            nextIndex();
            break;
        case '?':
            lookAhead = Lexer.QUANT_COMP_R;
            nextIndex();
            break;
        default:
            lookAhead = Lexer.QUANT_COMP;
            break;
        }
        return new Quantifier(min, max);
    }

    public String toString() {
        return orig;
    }

    /**
     * Checks if there are any characters in the pattern.
     * 
     * @return true if there are no more characters in the pattern.
     */
    public boolean isEmpty() {
    	return ch == 0 && lookAhead == 0 && index == patternFullLength && !isSpecial();
    }

    /**
     * Returns true if current character is plain token.
     */
    public static boolean isLetter(int ch) {
        return ch >= 0;
    }

    /**
     * Return true if current character is letter, false otherwise; This is
     * shortcut to static method isLetter to check the current character.
     * 
     * @return true if current character is letter, false otherwise
     */
    public boolean isLetter() {
        return !isEmpty() && !isSpecial() && isLetter(ch);
    }

    /**
     * Process hexadecimal integer. 
     */
    private int readHex(String radixName, int max) {
        StringBuffer st = new StringBuffer(max);
        int length = pattern.length - 2;
        int i;
        for (i = 0; i < max && index < length; i++) {
            st.append((char) pattern[nextIndex()]);
        }
        if (i == max) {
            try {
                return Integer.parseInt(st.toString(), 16);
            } catch (NumberFormatException nfe) {
            }
        }

        throw new PatternSyntaxException(I18n.getMessage("Invalid " + radixName
                + "escape sequence"), this.toString(), index);
    }

    /**
     * Process octal integer.
     */
    private int readOctals() {
        char ch;
        int max = 3;
        int i = 1;
        int first;
        int res;
        int length = pattern.length - 2;

        switch (first = Character.digit((ch = pattern[index]), 8)) {
        case -1:
            throw new PatternSyntaxException(I18n.getMessage("Invalid "
                    + "octal escape sequence"), this.toString(), index);
        default: {
            if (first > 3)
                max--;
            nextIndex();
            res = first;
        }
        }

        while (i < max && index < length
                && (first = Character.digit((ch = pattern[index]), 8)) >= 0) {
            res = res * 8 + first;
            nextIndex();
            i++;
        }

        return res;
    }

    /**
     * Process expression flags givent with (?idmsux-idmsux)
     */
    private int readFlags() {
        char ch;
        boolean pos = true;
        int res = flags;
        
        while (index < pattern.length) {
            ch = pattern[index];
            switch (ch) {
            case '-':
                if (!pos) {
                    throw new PatternSyntaxException("Illegal "
                            + "inline construct", this.toString(), index);
                }
                pos = false;
                break; 
                
            case 'i':
                res = pos 
                      ? res | Pattern.CASE_INSENSITIVE 
                      : (res ^ Pattern.CASE_INSENSITIVE) & res;
                break;
                
            case 'd':
                res = pos
                      ? res | Pattern.UNIX_LINES 
                	  : (res ^ Pattern.UNIX_LINES) & res;
                break;
                
            case 'm':
                res = pos 
                      ? res | Pattern.MULTILINE 
                      : (res ^ Pattern.MULTILINE) & res;
                break;
                
            case 's':
                res = pos 
                      ? res | Pattern.DOTALL 
                      : (res ^ Pattern.DOTALL) & res;
                break;
                
            case 'u':
                res = pos 
                      ? res | Pattern.UNICODE_CASE 
                      : (res ^ Pattern.UNICODE_CASE) & res;
                break;
                
            case 'x':
                res = pos 
                      ? res | Pattern.COMMENTS 
                      : (res ^ Pattern.COMMENTS) & res;
                break;
                
            case ':':
                nextIndex();
                return res;
                
            case ')':
                nextIndex();
                return res | (1 << 8);
                
            default:
                throw new PatternSyntaxException("Illegal inline construct",
                                               this.toString(), index);
            }
            nextIndex();
        }
        throw new PatternSyntaxException("Illegal inline construct", 
        		                       this.toString(), index);
    }


    /**
     * Returns next character index to read and moves pointer to the next one.
     * If comments flag is on this method will skip comments and whitespaces.
     * 
     * The following actions are equivalent if comments flag is off ch =
     * pattern[index++] == ch = pattern[nextIndex]
     * 
     * @return next character index to read.
     */
    private int nextIndex() {
        prevNW = index;
        if ((flags & Pattern.COMMENTS) != 0) {
            skipComments();
        } else {
            index++;
        }
        return prevNW;
    }

    /**
     * Skips comments and whitespaces
     */
    private int skipComments() {
        int length = pattern.length - 2;
        index++;
        do {
            while (index < length && Character.isWhitespace(pattern[index]))
                index++;
            if (index < length && pattern[index] == '#') {
                index++;
                while (index < length && !isLineSeparator(pattern[index]))
                    index++;
            } else
                return index;
        } while (true);
    }

    private boolean isLineSeparator(int ch) {
        return (ch == '\n' || ch == '\r' || ch == '\u0085' || (ch | 1) == '\u2029');
    }

    /**
     * Gets decomposition for given codepoint from
     * decomposition mappings table.
     * 
     * @param ch - Unicode codepoint
     * @return array of codepoints that is a canonical
     *         decomposition of ch.
     */
    static int [] getDecomposition(int ch) {
        return decompTable.get(ch);       
    }
    
    /**
     * Gets decomposition for given Hangul syllable. 
     * This is an implementation of Hangul decomposition algorithm 
     * according to http://www.unicode.org/versions/Unicode4.0.0/ch03.pdf
     * "3.12 Conjoining Jamo Behavior".
     * 
     * @param ch - given Hangul syllable
     * @return canonical decoposition of ch.
     */
    static int [] getHangulDecomposition(int ch) {
        int SIndex = ch - SBase;
        
        if (SIndex < 0 || SIndex >= SCount) {
            return null;
        } else {            
            int L = LBase + SIndex / NCount; 
            int V = VBase + (SIndex % NCount) / TCount; 
            int T = SIndex % TCount;
            int decomp [];
            
            if (T == 0) {
                decomp = new int [] {L, V};     
            } else {
                T = TBase + T;
                decomp = new int [] {L, V, T};
            }
            return decomp;            
        }
    }
    
    /**
     * Gets canonical class for given codepoint from
     * decomposition mappings table.
     * 
     * @param - ch Unicode codepoint
     * @return canonical class for given Unicode codepoint
     *         that is represented by ch.
     */
    static int getCanonicalClass(int ch) {   
        int canClass = canonClassesTable.get(ch);        
        
        return (canClass == canonClassesTableSize)
               ? 0
               : canClass;
    }
    
    /**
     * Simple stub to Character.charCount().
     * 
     * @param - ch Unicode codepoint
     * @return number of chars that are occupied by Unicode
     *         codepoint ch in UTF-16 encoding.
     */
    final static int charCount(int ch) {
            
        //return Character.charCount(ch);
        return 1;
    }
    
    /**
     * Simple stub to Character.codePointAt().
     * 
     * @param - source  
     * @param - index 
     * @return Unicode codepoint at given index at source.
     *         Note that codepoint can reside in two adjacent chars.
     */
    final static int codePointAt(char [] source, int index) {
        
        //return Character.codePointAt(source, index);
        return source[index];
    }
    
    /**
     * Simple stub to Character.toChars().
     * 
     * @param - ch Unicode codepoint
     * @return UTF-16 encoding of given code point.
     */
    final static char [] toChars(int ch) {            
        
        //return Character.toChars(ch);
        return new char [] {(char) ch};
    }
    
    /**
     * Simple stub to Character.isSurrogatePair().
     * 
     * @param high high-surrogate char
     * @param low low-surrogate char
     * @return true if high and low compose an UTF-16 encoding
     *         of some Unicode codepoint (we call such codepoint "surrogate")
     */
    final static boolean isSurrogatePair(char high, char low) {
        
        //return Character.isSurrogatePair(char, low)
        return false;
    }

    /**
     * Tests if given codepoint is a canonical decomposition of another
     * codepoint.
     * 
     * @param ch - codepoint to test
     * @return true if ch is a decomposition.
     */
    static boolean hasSingleCodepointDecomposition(int ch) {
        int hasSingleDecomp = singleDecompTable.get(ch);
        
        /*
         * singleDecompTable doesn't contain ch 
         * == (hasSingleDecomp == singleDecompTableSize)
         */
        return (hasSingleDecomp == singleDecompTableSize)
               ? false
               : true;
    }
    
    /**
     * Tests if given codepoint has canonical decomposition
     * and given codepoint's canonical class is not 0.
     * 
     * @param ch - codepoint to test
     * @return true if canonical class is not 0 and ch has a decomposition.
     */
    static boolean hasDecompositionNonNullCanClass(int ch) {
        return ch == 0x0340 | ch == 0x0341 | ch == 0x0343 | ch == 0x0344;
    }
    
    /**
     * Reads next Unicode codepoint.
     * 
     * @return current Unicode codepoint and moves string
     *         index to the next one.
     */
    int nextChar() {
           int ch = 0;
        
           if (!this.isEmpty()) {
               char nextChar = (char) lookAhead;
               char curChar = (char) ch;
               
               if (Lexer.isSurrogatePair(curChar, nextChar)){                                   
                   
                   /*
                    * Note that it's slow to create new arrays each time
                    * when calling to nextChar(). This should be optimized
                    * later when we will actively use surrogate codepoints.
                    * You can consider this as simple stub.
                    */
                   char [] curCodePointUTF16 = new char [] {curChar, nextChar};
                ch = Lexer.codePointAt(curCodePointUTF16, 0);                
                next();
                next();
            } else {
                ch = next();    
            }
        } 
        
           return ch;
    }
    
    /**
     * Tests Unicode codepoint if it is a boundary
     * of decomposed Unicode codepoint. 
     * 
     * @param ch - Unicode codepoint to test
     * @return true if given codepoint is a boundary.
     */
     static boolean isDecomposedCharBoundary(int ch) {  
         int canClass = canonClassesTable.get(ch);
     
         //Lexer.getCanonicalClass(ch) == 0
         boolean isBoundary = (canClass == canonClassesTableSize);
 
            return isBoundary;
    }
       
    /**
     * Returns the curr. character index.
     */
    public int getIndex() {
        return curToc;
    }
}
