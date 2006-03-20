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
 * @version $Revision: 1.6.2.2 $
 */
package java.util.regex;

/**
 * This is base class for special tokens like character classes 
 * and quantifiers.
 * 
 * @author Nikolay A. Kuznetsov
 * @version $Revision: 1.6.2.2 $
 */
abstract class SpecialToken {
    
    public static final int TOK_CHARCLASS = 1 << 0;

    public static final int TOK_QUANTIFIER = 1 << 1;
    
    /**
     * Returns the type of the token, may return following values:
     * TOK_CHARCLASS  - token representing character class;
     * TOK_QUANTIFIER - token representing quantifier;
     * 
     * @return character type.
     */
    public abstract int getType();
}
